package com.example.nhom5projectmobile;

import android.graphics.Color;
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
    private OnStoryClickListener listener;

    // 🔥 1. Thêm biến lưu chữ của thẻ (Mặc định là HOT)
    private String tagText = "HOT";

    public interface OnStoryClickListener {
        void onStoryClick(Story story);
    }

    public StoryAdapter(List<Story> storyList, OnStoryClickListener listener) {
        this.storyList = storyList;
        this.listener = listener;
    }

    // 🔥 2. Thêm hàm này để HomeFragment có thể gọi và đổi chữ
    public void setTagText(String tagText) {
        this.tagText = tagText;
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

        holder.tvTitle.setText(story.getTitle());
        holder.tvChapter.setText(story.getChapter());

        if (holder.tvViews != null) {
            holder.tvViews.setText(story.getViews() + " lượt xem");
        }
        if (holder.tvTime != null) {
            holder.tvTime.setText(story.getTimeAgo());
        }

        com.bumptech.glide.Glide.with(holder.itemView.getContext())
                .load(story.getCoverImage())
                .placeholder(R.drawable.ic_launcher_background)
                .into(holder.imgCover);

        // 🔥 3. Xét logic đổi chữ và màu cho Thẻ (HOT / NEW)
        if (holder.tvTag != null) {
            holder.tvTag.setText(tagText);

            if (tagText.equals("NEW")) {
                // Đổi thành màu xanh lá cây nếu là NEW
                holder.tvTag.setBackgroundColor(Color.parseColor("#4CAF50"));
            } else {
                // Giữ màu hồng đỏ mặc định nếu là HOT
                holder.tvTag.setBackgroundColor(Color.parseColor("#E91E63"));
            }
        }

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
        TextView tvTitle, tvChapter, tvViews, tvTime;

        // 🔥 4. Khai báo thêm biến cho cái Thẻ
        TextView tvTag;

        public StoryViewHolder(@NonNull View itemView) {
            super(itemView);
            imgCover = itemView.findViewById(R.id.imgCover);
            tvTitle = itemView.findViewById(R.id.tvTitle);
            tvChapter = itemView.findViewById(R.id.tvChapter);
            tvViews = itemView.findViewById(R.id.tvViews);
            tvTime = itemView.findViewById(R.id.tvTime);

            // 🔥 5. Ánh xạ Thẻ từ giao diện.
            // LƯU Ý QUAN TRỌNG: Hãy mở file 'item_story.xml' của bạn ra, tìm cái TextView
            // đang chứa chữ "HOT", và đặt android:id="@+id/tvTag" cho nó nhé!
            tvTag = itemView.findViewById(R.id.tvTag);
        }
    }
}