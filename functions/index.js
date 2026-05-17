const functions = require("firebase-functions/v1");
const admin = require("firebase-admin");
const path = require("path");
const os = require("os");
const fs = require("fs-extra");
const { fromPath } = require("pdf2pic");
const { v4: uuidv4 } = require("uuid");

admin.initializeApp();
const db = admin.firestore();

// Cấu hình Function: RAM 2GB, Timeout 9 phút (Tối đa) để xử lý PDF nặng
exports.processChapterPdf = functions
  .runWith({ timeoutSeconds: 540, memory: "2GB" })
  .storage.object()
  .onFinalize(async (object) => {
    const filePath = object.name; // VD: story_pdfs/ST0001/CT0001.pdf
    const bucket = admin.storage().bucket(object.bucket);

    // Lấy metadata từ Android
    const metadata = object.metadata || {};

    // MANG 2 DÒNG LOG VÀO ĐÚNG VỊ TRÍ BÊN TRONG HÀM
    console.log("--- BẮT ĐẦU XỬ LÝ FILE: ", filePath);
    console.log("--- METADATA NHẬN ĐƯỢC: ", JSON.stringify(metadata));

    // 1. CHỈ XỬ LÝ FILE TRONG THƯ MỤC "story_pdfs" VÀ LÀ FILE PDF
    if (!filePath.startsWith("story_pdfs/") || !filePath.endsWith(".pdf")) {
      return null;
    }

    // 2. KIỂM TRA METADATA
    if (!metadata.storyId || !metadata.chapterId) {
      console.log("File thiếu Metadata (storyId, chapterId). Bỏ qua.");
      return null;
    }

    const storyId = metadata.storyId;
    const chapterId = metadata.chapterId;
    const chapterTitle = metadata.chapterTitle || `Chương ${chapterId}`;

    const fileName = path.basename(filePath);
    const tempFilePath = path.join(os.tmpdir(), fileName);
    const tempImageDir = path.join(os.tmpdir(), `images_${chapterId}`);

    try {
      // 3. TẢI FILE PDF XUỐNG
      console.log(`Đang tải PDF của truyện ${storyId}, chương ${chapterId}...`);
      await bucket.file(filePath).download({ destination: tempFilePath });
      await fs.ensureDir(tempImageDir);

      // 4. CẤU HÌNH BỘ CẮT PDF
      const options = {
        density: 150,
        saveFilename: "page",
        savePath: tempImageDir,
        format: "jpg",
        width: 1080,
      };
      const storeAsImage = fromPath(tempFilePath, options);

      console.log("Đang tiến hành cắt PDF thành ảnh...");
      const convertResults = await storeAsImage.bulk(-1);

      const imageUrls = [];

      // 5. UPLOAD CÁC ẢNH VỪA CẮT
      for (let i = 0; i < convertResults.length; i++) {
        const result = convertResults[i];
        const pageNumber = result.page;
        const localImagePath = result.path;

        const destinationPath = `story_images/${storyId}/${chapterId}/page_${pageNumber}.jpg`;
        const token = uuidv4();

        await bucket.upload(localImagePath, {
          destination: destinationPath,
          metadata: {
            contentType: "image/jpeg",
            metadata: { firebaseStorageDownloadTokens: token }
          }
        });

        const publicUrl = `https://firebasestorage.googleapis.com/v0/b/${bucket.name}/o/${encodeURIComponent(destinationPath)}?alt=media&token=${token}`;
        imageUrls.push(publicUrl);
      }

      console.log(`Đã upload ${imageUrls.length} ảnh. Đang lưu vào Firestore...`);

      // 6. GHI VÀO FIRESTORE
      const batch = db.batch();
      const chapterRef = db.collection("stories").doc(storyId).collection("chapters").doc(chapterId);
      batch.set(chapterRef, {
        chapterId: chapterId,
        chapterNumber: 1,
        title: chapterTitle,
        pages: imageUrls,
        createdAt: admin.firestore.FieldValue.serverTimestamp(),
      });

      const storyRef = db.collection("stories").doc(storyId);
      batch.update(storyRef, {
        updatedAt: admin.firestore.FieldValue.serverTimestamp(),
        lastestChapterTitle: chapterTitle
      });

      await batch.commit();
      console.log("HOÀN TẤT TOÀN BỘ QUY TRÌNH!");

    } catch (error) {
      console.error("Có lỗi xảy ra trong quá trình xử lý:", error);
    } finally {
      // 7. DỌN DẸP RÁC
      fs.removeSync(tempFilePath); // Dùng removeSync an toàn hơn unlinkSync
      fs.emptyDirSync(tempImageDir);
    }
  });