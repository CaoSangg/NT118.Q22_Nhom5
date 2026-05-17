package com.example.nhom5projectmobile;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class StoryAdminAdapter extends RecyclerView.Adapter<StoryAdminAdapter.ViewHolder> {

    private List<Story> storyList;
    private OnAdminStoryClickListener listener;

    // Tạo Interface để giao tiếp với Fragment
    public interface OnAdminStoryClickListener {
        void onAddChapterClick(Story story);
        void onDeleteStoryClick(Story story, int position);
        void onEditStoryClick(Story story);
    }

    // Constructor để nhận listener
    public StoryAdminAdapter(List<Story> storyList, OnAdminStoryClickListener listener) {
        this.storyList = storyList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Trỏ tới file giao diện bảng 3 cột của Admin
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_manage_story, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Story story = storyList.get(position);

        holder.tvStoryName.setText(story.getTitle());
        holder.tvChapterCount.setText(story.getChapter());

        // Gọi listener khi bấm nút Thêm chương
        holder.btnAddChapter.setOnClickListener(v -> {
            if (listener != null) {
                listener.onAddChapterClick(story);
            }
        });

        // Gọi listener khi bấm nút Sửa truyện
        holder.btnEditStory.setOnClickListener(v -> {
            if (listener != null) {
                listener.onEditStoryClick(story);
            }
        });

        // Gọi listener khi bấm nút Xóa truyện
        holder.btnDeleteStory.setOnClickListener(v -> {
            if (listener != null) {
                listener.onDeleteStoryClick(story, position);
            }
        });
    }

    @Override
    public int getItemCount() {
        return storyList != null ? storyList.size() : 0;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvStoryName, tvChapterCount, btnAddChapter, btnDeleteStory, btnEditStory;;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvStoryName = itemView.findViewById(R.id.tvStoryName);
            tvChapterCount = itemView.findViewById(R.id.tvChapterCount);
            btnAddChapter = itemView.findViewById(R.id.btnAddChapter);
            btnDeleteStory = itemView.findViewById(R.id.btnDeleteStory);
            btnEditStory = itemView.findViewById(R.id.btnEditStory);
        }
    }
}