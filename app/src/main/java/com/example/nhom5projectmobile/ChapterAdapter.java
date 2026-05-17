package com.example.nhom5projectmobile;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class ChapterAdapter extends RecyclerView.Adapter<ChapterAdapter.ChapterViewHolder> {
    private Context context;
    private List<Chapter> chapterList;
    private String storyId; // Biến mới để nhận mã truyện

    // CẬP NHẬT CONSTRUCTOR: Thêm String storyId
    public ChapterAdapter(Context context, List<Chapter> chapterList, String storyId) {
        this.context = context;
        this.chapterList = chapterList;
        this.storyId = storyId;
    }

    @NonNull
    @Override
    public ChapterViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_chapter, parent, false);
        return new ChapterViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ChapterViewHolder holder, int position) {
        Chapter chapter = chapterList.get(position);
        holder.tvChapterTitle.setText(chapter.getTitle());

        // BẮT SỰ KIỆN CLICK: Mở màn hình đọc truyện mới (ReaderActivity)
        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, ReaderActivity.class); // Trỏ sang Activity hiển thị ảnh
            intent.putExtra("STORY_ID", storyId);
            intent.putExtra("CHAPTER_ID", chapter.getChapterId());
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() { return chapterList.size(); }

    static class ChapterViewHolder extends RecyclerView.ViewHolder {
        TextView tvChapterTitle;
        public ChapterViewHolder(@NonNull View itemView) {
            super(itemView);
            tvChapterTitle = itemView.findViewById(R.id.tvChapterTitle);
        }
    }
}