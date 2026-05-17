package com.example.nhom5projectmobile;

import android.app.ProgressDialog;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import android.graphics.Bitmap;
import android.graphics.pdf.PdfRenderer;
import android.os.ParcelFileDescriptor;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageMetadata;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ManageStoriesFragment extends Fragment {

    private RecyclerView rvAdminStories;
    private StoryAdminAdapter adapter;
    private List<Story> mockStoryList;
    private Button btnAddStory;

    private Story selectedStoryForUpload;
    private ActivityResultLauncher<String> pdfPickerLauncher;

    private String tempChapterId = "";
    private String tempChapterTitle = "";

    // --- CÁC BIẾN CHO TÍNH NĂNG CHỌN ẢNH BÌA ---
    private ActivityResultLauncher<String> imagePickerLauncher;
    private Uri selectedCoverUri = null;
    private Button btnPickCoverPreview; // Dùng để đổi text nút khi chọn ảnh xong

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        pdfPickerLauncher = registerForActivityResult(new ActivityResultContracts.GetContent(), uri -> {
            if (uri != null && selectedStoryForUpload != null) {
                uploadPdfToFirebase(uri, selectedStoryForUpload);
            } else {
                Toast.makeText(getContext(), "Chưa chọn file", Toast.LENGTH_SHORT).show();
            }
        });

        imagePickerLauncher = registerForActivityResult(new ActivityResultContracts.GetContent(), uri -> {
            if (uri != null) {
                selectedCoverUri = uri;
                if (btnPickCoverPreview != null) {
                    btnPickCoverPreview.setText("Đã chọn ảnh bìa ✓");
                    btnPickCoverPreview.setBackgroundColor(android.graphics.Color.parseColor("#4CAF50")); // Đổi màu xanh báo thành công
                }
            } else {
                Toast.makeText(getContext(), "Chưa chọn ảnh", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_manage_stories, container, false);

        rvAdminStories = view.findViewById(R.id.rvAdminStories);
        btnAddStory = view.findViewById(R.id.btnAddStory);

        mockStoryList = new ArrayList<>();

        rvAdminStories.setLayoutManager(new LinearLayoutManager(getContext()));

        adapter = new StoryAdminAdapter(mockStoryList, new StoryAdminAdapter.OnAdminStoryClickListener() {
            @Override
            public void onAddChapterClick(Story story) {
                selectedStoryForUpload = story;
                showAddChapterDialog();
            }

            @Override
            public void onDeleteStoryClick(Story story, int position) {
                // Hiển thị dialog xác nhận để xóa
                new android.app.AlertDialog.Builder(getContext())
                        .setTitle("Xác nhận xóa truyện")
                        .setMessage("Bạn có chắc chắn muốn xóa bộ truyện \"" + story.getTitle() + "\" không? Hành động này sẽ xóa vĩnh viễn và không thể hoàn tác.")
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .setPositiveButton("Xóa vĩnh viễn", (dialog, which) -> {
                            // Gọi hàm xóa thật trên Firestore
                            deleteStoryFromFirestore(story, position);
                        })
                        .setNegativeButton("Hủy", (dialog, cancel) -> dialog.dismiss())
                        .show();
            }

            @Override
            public void onEditStoryClick(Story story) {
                android.content.Intent intent = new android.content.Intent(getContext(), EditStoryActivity.class);
                intent.putExtra("STORY_ID", story.getId());
                startActivity(intent);
            }
        });

        rvAdminStories.setAdapter(adapter);

        // Gắn sk cho nút Đăng Truyện
        btnAddStory.setOnClickListener(v -> {
            android.content.Intent intent = new android.content.Intent(getActivity(), AddStoryActivity.class);
            startActivity(intent);
        });
        return view;
    }

    //Hàm up chương mới (add file zo storage)
    private void showAddChapterDialog() {
        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(getContext());
        builder.setTitle("Thông tin cho chương mới");

        android.widget.LinearLayout layout = new android.widget.LinearLayout(getContext());
        layout.setOrientation(android.widget.LinearLayout.VERTICAL);
        layout.setPadding(50, 20, 50, 20);

        final android.widget.EditText etId = new android.widget.EditText(getContext());
        etId.setHint("Mã chương (VD: CT0001)");
        layout.addView(etId);

        final android.widget.EditText etTitle = new android.widget.EditText(getContext());
        etTitle.setHint("Tên chương (VD: Chương 1: xxxxx)");
        layout.addView(etTitle);

        builder.setView(layout);

        builder.setPositiveButton("Chọn file PDF", (dialog, confirm) -> {
            tempChapterId = etId.getText().toString().trim();
            tempChapterTitle = etTitle.getText().toString().trim();

            if (!tempChapterId.isEmpty() && !tempChapterTitle.isEmpty()) {
                pdfPickerLauncher.launch("application/pdf");
            } else {
                Toast.makeText(getContext(), "Vui lòng nhập đủ thông tin", Toast.LENGTH_SHORT).show();
            }
        });

        builder.setNegativeButton("Hủy", (dialog, cancel) -> dialog.dismiss());
        builder.show();
    }

    private void uploadPdfToFirebase(Uri pdfUri, Story story) {
        String storyId = story.getId();

        ProgressDialog progressDialog = new ProgressDialog(getContext());
        progressDialog.setTitle("Đang xử lý truyện...");
        progressDialog.setMessage("Đang chuẩn bị bóc tách ảnh...");
        progressDialog.setCancelable(false);
        progressDialog.show();

        // 1. Chạy trên Thread riêng để không làm treo giao diện UI khi xử lý PDF lớn
        new Thread(() -> {
            try {
                // 2. Mở file PDF bằng PdfRenderer
                ParcelFileDescriptor fileDescriptor = getContext().getContentResolver().openFileDescriptor(pdfUri, "r");
                if (fileDescriptor == null) {
                    throw new IOException("Không thể đọc file PDF.");
                }

                PdfRenderer pdfRenderer = new PdfRenderer(fileDescriptor);
                final int pageCount = pdfRenderer.getPageCount();

                List<String> uploadedImageUrls = new ArrayList<>();
                List<byte[]> compressedPages = new ArrayList<>();

                // 3. Bóc tách từng trang PDF thành Bitmap
                getActivity().runOnUiThread(() -> progressDialog.setMessage("Đang cắt " + pageCount + " trang..."));

                for (int i = 0; i < pageCount; i++) {
                    PdfRenderer.Page page = pdfRenderer.openPage(i);

                    // Tạo Bitmap chất lượng tốt (scale up 2 lần để nét hơn trên màn hình)
                    int width = getResources().getDisplayMetrics().widthPixels;
                    int height = (int) (width * ((float) page.getHeight() / page.getWidth()));
                    Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);

                    // Tô nền trắng (tránh nền đen mờ) và render
                    bitmap.eraseColor(android.graphics.Color.WHITE);
                    page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY);

                    // Nén lại thành JPEG để nhẹ hơn khi upload
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 80, baos);
                    compressedPages.add(baos.toByteArray());

                    page.close();
                    bitmap.recycle(); // Tránh tràn RAM (Memory Leak)
                }

                pdfRenderer.close();
                fileDescriptor.close();

                // 4. Bắt đầu Upload từng ảnh lên Firebase Storage
                getActivity().runOnUiThread(() -> progressDialog.setMessage("Đang tải lên " + pageCount + " trang..."));

                for (int i = 0; i < compressedPages.size(); i++) {
                    final int pageIndex = i + 1; // Đánh số thứ tự trang từ 1
                    byte[] imageData = compressedPages.get(i);

                    // Đường dẫn mới: story_images/ST0001/CT0001/page_1.jpg
                    String pathString = "story_images/" + storyId + "/" + tempChapterId + "/page_" + pageIndex + ".jpg";
                    StorageReference imageRef = FirebaseStorage.getInstance().getReference().child(pathString);

                    // Upload đồng bộ (đợi cái này xong mới lên cái kia để đảm bảo đúng thứ tự mảng URL)
                    // Lưu ý: await() của Tasks chỉ dùng được trong background thread (Thread chúng ta đang đứng)
                    com.google.android.gms.tasks.Task<android.net.Uri> urlTask = imageRef.putBytes(imageData)
                            .continueWithTask(task -> {
                                if (!task.isSuccessful()) throw task.getException();
                                return imageRef.getDownloadUrl();
                            });

                    // Chờ đến khi lấy được URL (vì đang trong Thread riêng, việc block này là an toàn)
                    Uri downloadUrl = com.google.android.gms.tasks.Tasks.await(urlTask);
                    uploadedImageUrls.add(downloadUrl.toString());

                    // Cập nhật tiến độ UI
                    getActivity().runOnUiThread(() -> {
                        int progress = (int) (((float) pageIndex / pageCount) * 100);
                        progressDialog.setMessage("Đã tải " + progress + "% (" + pageIndex + "/" + pageCount + ")");
                    });
                }

                // 5. Lưu Array URL vào Firestore Database (Giống logic trong Cloud Function)
                getActivity().runOnUiThread(() -> progressDialog.setMessage("Đang cập nhật dữ liệu truyện..."));
                FirebaseFirestore db = FirebaseFirestore.getInstance();

                // Lưu vào sub-collection 'chapters'
                Map<String, Object> chapterData = new HashMap<>();
                chapterData.put("chapterId", tempChapterId);
                chapterData.put("title", tempChapterTitle);
                chapterData.put("pages", uploadedImageUrls); // Mảng URL quan trọng nhất
                chapterData.put("createdAt", FieldValue.serverTimestamp());

                com.google.android.gms.tasks.Tasks.await(
                        db.collection("stories").document(storyId)
                                .collection("chapters").document(tempChapterId)
                                .set(chapterData)
                );

                // Cập nhật trạng thái 'lastestChapterTitle' của truyện cha
                Map<String, Object> storyUpdate = new HashMap<>();
                storyUpdate.put("updatedAt", FieldValue.serverTimestamp());
                storyUpdate.put("lastestChapterTitle", tempChapterTitle);

                // Thêm lệnh tự động cộng 1 vào tổng số chương
                storyUpdate.put("chaptersCount", FieldValue.increment(1));

                // Sau khi put đầy đủ các trường cần update thì mới gọi await để đẩy lên
                com.google.android.gms.tasks.Tasks.await(
                        db.collection("stories").document(storyId).update(storyUpdate)
                );

                // HOÀN THÀNH TOÀN BỘ
                getActivity().runOnUiThread(() -> {
                    progressDialog.dismiss();
                    Toast.makeText(getContext(), "Đăng chương mới và xử lý ảnh thành công!", Toast.LENGTH_LONG).show();

                    tempChapterId = "";
                    tempChapterTitle = "";

                    // Cập nhật giao diện (cộng thêm 1 chương)
                    int currentChapters = Integer.parseInt(story.getChapter());
                    story.setChapter(String.valueOf(currentChapters + 1));
                    adapter.notifyDataSetChanged();
                });

            } catch (Exception e) {
                // Xử lý lỗi
                getActivity().runOnUiThread(() -> {
                    progressDialog.dismiss();
                    Toast.makeText(getContext(), "Lỗi khi xử lý PDF: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    e.printStackTrace(); // In ra Logcat để dễ debug
                });
            }
        }).start(); // Bắt đầu Thread
    }
    private void loadStoriesFromFirestore() {
        ProgressDialog progressDialog = new ProgressDialog(getContext());
        progressDialog.setMessage("Đang tải danh sách truyện quản lý...");
        progressDialog.setCancelable(false);
        progressDialog.show();

        FirebaseFirestore db = FirebaseFirestore.getInstance();

        // Lấy toàn bộ truyện, xếp truyện mới cập nhật lên đầu đầu danh sách
        db.collection("stories")
                .orderBy("updatedAt", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    progressDialog.dismiss();
                    mockStoryList.clear(); // Xóa sạch danh sách cũ chống trùng lặp dữ liệu

                    for (DocumentSnapshot doc : queryDocumentSnapshots.getDocuments()) {
                        String id = doc.getId();
                        String title = doc.getString("title");
                        String author = doc.getString("author");
                        String coverImage = doc.getString("coverImage");

                        long views = doc.contains("viewsCount") ? doc.getLong("viewsCount") : 0;

                        // Lấy số lượng chương từ trường chaptersCount, nếu tài liệu cũ chưa có thì mặc định là 0
                        long chaptersCount = doc.contains("chaptersCount") ? doc.getLong("chaptersCount") : 0;
                        String chapterStr = String.valueOf(chaptersCount);

                        String timeAgo = "Vừa xong";

                        // Ánh xạ thành đối tượng Story để nạp vào Adapter hiển thị
                        Story story = new Story(id, title, author, coverImage, views, chapterStr, timeAgo, "");
                        mockStoryList.add(story);
                    }
                    adapter.notifyDataSetChanged(); // Cập nhật lại giao diện bảng quản lý
                })
                .addOnFailureListener(e -> {
                    progressDialog.dismiss();
                    Toast.makeText(getContext(), "Lỗi tải danh sách: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
    private void deleteStoryFromFirestore(Story story, int position) {
        ProgressDialog progressDialog = new ProgressDialog(getContext());
        progressDialog.setMessage("Đang dọn dẹp dữ liệu truyện và các chương...");
        progressDialog.setCancelable(false);
        progressDialog.show();

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        String storyId = story.getId();

        // BƯỚC 1: Truy vấn lấy toàn bộ các chương nằm trong sub-collection 'chapters'
        db.collection("stories").document(storyId).collection("chapters")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {

                    // Khởi tạo WriteBatch để gom nhóm các lệnh xóa
                    com.google.firebase.firestore.WriteBatch batch = db.batch();

                    // Vòng lặp: Đưa lệnh xóa từng chương vào trong Batch
                    for (com.google.firebase.firestore.DocumentSnapshot doc : queryDocumentSnapshots.getDocuments()) {
                        batch.delete(doc.getReference());
                    }

                    // BƯỚC 2: Thực thi Batch để xóa sạch các chương
                    batch.commit().addOnSuccessListener(aVoid -> {

                        // BƯỚC 3: Xóa thành công thư mục con -> Tiến hành xóa Document Truyện cha
                        db.collection("stories").document(storyId)
                                .delete()
                                .addOnSuccessListener(aVoid2 -> {
                                    progressDialog.dismiss();
                                    Toast.makeText(getContext(), "Đã xóa sạch sẽ toàn bộ truyện và chương!", Toast.LENGTH_SHORT).show();

                                    // Cập nhật UI
                                    if (position < mockStoryList.size()) {
                                        mockStoryList.remove(position);
                                        adapter.notifyItemRemoved(position);
                                        adapter.notifyItemRangeChanged(position, mockStoryList.size());
                                    }
                                })
                                .addOnFailureListener(e -> {
                                    progressDialog.dismiss();
                                    Toast.makeText(getContext(), "Lỗi khi xóa truyện cha: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                });

                    }).addOnFailureListener(e -> {
                        progressDialog.dismiss();
                        Toast.makeText(getContext(), "Lỗi khi dọn dẹp các chương con: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });

                })
                .addOnFailureListener(e -> {
                    progressDialog.dismiss();
                    Toast.makeText(getContext(), "Lỗi truy xuất danh sách chương: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    @Override
    public void onResume() {
        super.onResume();
        // Hàm này sẽ tự động chạy mỗi khi màn hình Quản lý được hiển thị lại trên cùng
        loadStoriesFromFirestore();
    }
}