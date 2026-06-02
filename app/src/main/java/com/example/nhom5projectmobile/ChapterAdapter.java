package com.example.nhom5projectmobile;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class ChapterAdapter extends RecyclerView.Adapter<ChapterAdapter.ChapterViewHolder> {
    private Context context;
    private List<Chapter> chapterList;
    private String storyId;
    private String storyTitle = "Truyện Offline";
    private String coverUrl = "";
    public boolean isOffline = false;
    private String lastReadChapterId = null;
    private java.util.Set<String> readChapters = new java.util.HashSet<>();

    public void setReadHistory(String lastReadId, java.util.Set<String> readList) {
        this.lastReadChapterId = lastReadId;
        this.readChapters = readList;
        notifyDataSetChanged();
    }

    public ChapterAdapter(Context context, List<Chapter> chapterList, String storyId) {
        this.context = context;
        this.chapterList = chapterList;
        this.storyId = storyId;
    }

    public void setStoryTitle(String storyTitle) {
        this.storyTitle = storyTitle;
    }

    public void setCoverUrl(String coverUrl) {
        this.coverUrl = coverUrl;
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

        // 1. Kiểm tra xem chương này đã đọc chưa để đổi sang màu xanh dương (#2196F3)
        if (readChapters.contains(chapter.getChapterId())) {
            holder.tvChapterTitle.setTextColor(android.graphics.Color.parseColor("#2196F3"));
        } else {
            holder.tvChapterTitle.setTextColor(android.graphics.Color.parseColor("#333333"));
        }

        // 2. Nếu chương này là chương vừa mở gần nhất -> Hiện icon Bookmark kế bên
        if (chapter.getChapterId().equals(lastReadChapterId)) {
            holder.imgChapterBookmark.setVisibility(View.VISIBLE);
        } else {
            holder.imgChapterBookmark.setVisibility(View.GONE);
        }

        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, ReaderActivity.class);
            intent.putExtra("STORY_ID", storyId);
            intent.putExtra("CHAPTER_ID", chapter.getChapterId());
            if (isOffline) intent.putExtra("IS_OFFLINE", true);
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() { return chapterList.size(); }

    static class ChapterViewHolder extends RecyclerView.ViewHolder {
        TextView tvChapterTitle;
        ImageView imgChapterBookmark;

        public ChapterViewHolder(@NonNull View itemView) {
            super(itemView);
            tvChapterTitle = itemView.findViewById(R.id.tvChapterTitle);
            imgChapterBookmark = itemView.findViewById(R.id.imgChapterBookmark);
        }
    }
}