package com.example.nhom5projectmobile;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.widget.Toast;

import com.google.firebase.firestore.FirebaseFirestore;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class DownloadHelper {

    // Thêm biến orderIndex vào đây
    public static void downloadChapter(Context context, String storyId, String storyTitle, String coverUrl, String chapterId, String chapterTitle, long orderIndex) {
        DatabaseHelper db = new DatabaseHelper(context);
        String localChapterId = storyId + "_" + chapterId; // Ghép id truyện để tránh trùng lặp khoá chính trong SQLite
        if (db.isChapterDownloaded(localChapterId)) {
            Toast.makeText(context, chapterTitle + " đã được tải từ trước!", Toast.LENGTH_SHORT).show();
            return;
        }

        Toast.makeText(context, "Bắt đầu tải " + chapterTitle + "...", Toast.LENGTH_SHORT).show();

        FirebaseFirestore.getInstance().collection("stories").document(storyId)
                .collection("chapters").document(chapterId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        List<String> pageUrls = (List<String>) documentSnapshot.get("pages");
                        if (pageUrls != null && !pageUrls.isEmpty()) {
                            // Truyền orderIndex xuống ngầm
                            startBackgroundDownload(context, storyId, storyTitle, coverUrl, chapterId, chapterTitle, pageUrls, orderIndex);
                        } else {
                            Toast.makeText(context, "Chương này không có nội dung để tải", Toast.LENGTH_SHORT).show();
                        }
                    }
                })
                .addOnFailureListener(e -> Toast.makeText(context, "Lỗi kết nối Firebase", Toast.LENGTH_SHORT).show());
    }

    private static void startBackgroundDownload(Context context, String storyId, String storyTitle, String coverUrl, String chapterId, String chapterTitle, List<String> pageUrls, long orderIndex) {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        Handler handler = new Handler(Looper.getMainLooper());

        executor.execute(() -> {
            List<String> localPaths = new ArrayList<>();
            boolean isSuccess = true;

            for (int i = 0; i < pageUrls.size(); i++) {
                try {
                    String imageUrl = pageUrls.get(i);
                    URL url = new URL(imageUrl);
                    HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                    connection.connect();

                    InputStream input = connection.getInputStream();
                    String fileName = storyId + "_" + chapterId + "_page_" + i + ".jpg";
                    FileOutputStream output = context.openFileOutput(fileName, Context.MODE_PRIVATE);

                    byte[] data = new byte[1024];
                    int count;
                    while ((count = input.read(data)) != -1) {
                        output.write(data, 0, count);
                    }
                    output.flush();
                    output.close();
                    input.close();

                    File file = new File(context.getFilesDir(), fileName);
                    localPaths.add(file.getAbsolutePath());
                } catch (Exception e) {
                    e.printStackTrace();
                    isSuccess = false;
                    break;
                }
            }

            boolean finalSuccess = isSuccess;
            handler.post(() -> {
                if (finalSuccess) {
                    String pathsString = TextUtils.join(",", localPaths);
                    DatabaseHelper db = new DatabaseHelper(context);
                    db.insertOfflineStory(storyId, storyTitle, coverUrl);

                    // Nhét thêm orderIndex vào Database
                    String localChapterId = storyId + "_" + chapterId;
                    db.insertOfflineChapter(localChapterId, storyId, chapterTitle, pathsString, orderIndex);

                    Toast.makeText(context, "Tải " + chapterTitle + " thành công!", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(context, "Lỗi mạng khi tải " + chapterTitle, Toast.LENGTH_SHORT).show();
                }
            });
        });
    }
}