package com.example.nhom5projectmobile;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;

import java.io.File; // <-- Bổ sung thư viện File để đọc bộ nhớ máy
import java.util.List;

public class ReaderAdapter extends RecyclerView.Adapter<ReaderAdapter.PageViewHolder> {

    private Context context;
    private List<String> pageUrls;

    public ReaderAdapter(Context context, List<String> pageUrls) {
        this.context = context;
        this.pageUrls = pageUrls;
    }

    @NonNull
    @Override
    public PageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_reader_page, parent, false);
        return new PageViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PageViewHolder holder, int position) {
        String path = pageUrls.get(position);

        // Kiểm tra xem đây là đường dẫn vật lý trong máy (Offline) hay link web (Online)
        if (path.startsWith("/")) {
            // CHẾ ĐỘ OFFLINE: Đọc file vật lý
            File imgFile = new File(path);
            if (imgFile.exists()) {
                Glide.with(context)
                        .load(imgFile)
                        .placeholder(R.drawable.ic_launcher_background) // Có thể thay bằng ảnh loading mờ
                        .into(holder.imgPage);
            }
        } else {
            // CHẾ ĐỘ ONLINE: Load từ Firebase qua mạng
            Glide.with(context)
                    .load(path)
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .placeholder(R.drawable.ic_launcher_background) // Có thể thay bằng ảnh loading mờ
                    .into(holder.imgPage);
        }
    }

    @Override
    public int getItemCount() {
        return pageUrls != null ? pageUrls.size() : 0;
    }

    static class PageViewHolder extends RecyclerView.ViewHolder {
        ImageView imgPage;

        public PageViewHolder(@NonNull View itemView) {
            super(itemView);
            imgPage = itemView.findViewById(R.id.imgPage);
        }
    }
}