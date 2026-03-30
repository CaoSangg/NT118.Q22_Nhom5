package com.example.nhom5projectmobile;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class StoryAdapter extends RecyclerView.Adapter<StoryAdapter.StoryViewHolder> {
    private List<Story> storyList;
    private OnStoryClickListener listener; // Khai báo listener

    // Interface để HomeFragment triển khai
    public interface OnStoryClickListener {
        void onStoryClick(Story story);
    }

    public StoryAdapter(List<Story> storyList, OnStoryClickListener listener) {
        this.storyList = storyList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public StoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_story, parent, false);
        return new StoryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull StoryViewHolder holder, int position) {
        Story story = storyList.get(position);

        // 1. Gán Tên truyện và Tên chương
        holder.tvTitle.setText(story.getTitle());
        holder.tvChapter.setText(story.getChapter()); // Lỗi đã được sửa ở đây

        // 2. Gán dữ liệu ĐỘNG cho Lượt xem và Thời gian
        if (holder.tvViews != null) {
            holder.tvViews.setText(story.getViews() + " lượt xem");
        }
        if (holder.tvTime != null) {
            holder.tvTime.setText(story.getTimeAgo());
        }

        // 3. Load ảnh bìa
        com.bumptech.glide.Glide.with(holder.itemView.getContext())
                .load(story.getCoverImage())
                .placeholder(R.drawable.ic_launcher_background)
                .into(holder.imgCover);

        // Bắt sự kiện click vào TOÀN BỘ item (bao gồm cả Title và Chapter)
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onStoryClick(story);
            }
        });
    }

    @Override
    public int getItemCount() {
        return storyList.size();
    }

    static class StoryViewHolder extends RecyclerView.ViewHolder {
        ImageView imgCover;
        TextView tvTitle, tvChapter;

        // Khai báo thêm 2 TextView cho Lượt xem và Thời gian
        TextView tvViews, tvTime;

        public StoryViewHolder(@NonNull View itemView) {
            super(itemView);
            imgCover = itemView.findViewById(R.id.imgCover);
            tvTitle = itemView.findViewById(R.id.tvTitle);
            tvChapter = itemView.findViewById(R.id.tvChapter);

            // Ánh xạ ID từ giao diện.
            // BẠN LƯU Ý: Phải kiểm tra xem trong file item_story.xml của bạn
            // đã đặt ID cho 2 chỗ này là "tvViews" và "tvTime" chưa nhé!
            tvViews = itemView.findViewById(R.id.tvViews);
            tvTime = itemView.findViewById(R.id.tvTime);
        }
    }
}