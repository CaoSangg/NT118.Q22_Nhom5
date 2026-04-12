package com.example.nhom5projectmobile;

import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.github.barteksc.pdfviewer.PDFView;
import com.github.barteksc.pdfviewer.scroll.DefaultScrollHandle;
import com.github.barteksc.pdfviewer.util.FitPolicy;
import okhttp3.OkHttpClient;
import okhttp3.Request;

import java.io.IOException;
import java.io.InputStream;

public class PdfViewActivity extends AppCompatActivity {
    private PDFView pdfView;
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pdf_view);

        pdfView = findViewById(R.id.pdfView);
        progressBar = findViewById(R.id.progressBar);

        String pdfUrl = getIntent().getStringExtra("PDF_URL");

        if (pdfUrl != null && !pdfUrl.isEmpty()) {
            loadPdf(pdfUrl);
        } else {
            Toast.makeText(this, "Không có link PDF", Toast.LENGTH_SHORT).show();
        }

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());
    }

    private void loadPdf(String pdfUrl) {
        progressBar.setVisibility(View.VISIBLE);

        new Thread(() -> {
            try {
                // Dùng OkHttp để stream PDF — nhanh hơn vì không cần tải hết file
                OkHttpClient client = new OkHttpClient();
                Request request = new Request.Builder().url(pdfUrl).build();
                InputStream inputStream = client.newCall(request).execute().body().byteStream();

                runOnUiThread(() ->
                        pdfView.fromStream(inputStream)
                                .defaultPage(0)
                                .scrollHandle(new DefaultScrollHandle(this))
                                .spacing(8)
                                .pageFitPolicy(FitPolicy.WIDTH) // Fit theo chiều ngang cho dễ đọc
                                .enableSwipe(true)
                                .swipeHorizontal(false)
                                .enableDoubletap(true)
                                .onLoad(nbPages -> progressBar.setVisibility(View.GONE))
                                .onError(t -> {
                                    progressBar.setVisibility(View.GONE);
                                    Toast.makeText(this, "Lỗi tải PDF", Toast.LENGTH_SHORT).show();
                                })
                                .load()
                );
            } catch (IOException e) {
                runOnUiThread(() -> {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(this, "Lỗi kết nối: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
            }
        }).start();
    }
}