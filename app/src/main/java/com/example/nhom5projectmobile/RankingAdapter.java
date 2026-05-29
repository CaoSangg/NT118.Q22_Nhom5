package com.example.nhom5projectmobile;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.List;

public class RankingAdapter extends RecyclerView.Adapter<RankingAdapter.ViewHolder> {
    private List<StoryRanking> list;

    public RankingAdapter(List<StoryRanking> list) {
        this.list = list;
    }

    // Hàm này được gọi từ Fragment sau khi fetch Firebase xong
    public void updateData(List<StoryRanking> newList) {
        this.list = newList;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_ranking, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        StoryRanking story = list.get(position);

        // Đã sửa thành getTitle() cho khớp với Firebase
        holder.tvName.setText(story.getTitle());

        // Format hiển thị "Lượt xem: 582K" hoặc số cụ thể
        String viewsText = "Lượt xem: " + formatViews(story.getCurrentDisplayViews());
        holder.tvViews.setText(viewsText);

        holder.tvRank.setText(String.valueOf(story.getRank()));

        // Đã sửa thành getCoverImage() cho khớp với Firebase
        Glide.with(holder.itemView.getContext())
                .load(story.getCoverImage())
                .placeholder(R.drawable.tag_hot) // Ảnh chờ tạm thời
                .into(holder.imgCover);

        // Đổi màu vòng tròn số thứ tự cho giống mẫu
        if (position == 0) holder.tvRank.getBackground().setTint(Color.parseColor("#FBC02D"));
        else if (position == 1) holder.tvRank.getBackground().setTint(Color.parseColor("#80CBC4"));
        else if (position == 2) holder.tvRank.getBackground().setTint(Color.parseColor("#BCAAA4"));
        else holder.tvRank.getBackground().setTint(Color.parseColor("#BDBDBD"));
    }

    @Override
    public int getItemCount() {
        return list != null ? list.size() : 0;
    }

    // Hàm tiện ích để chuyển đổi số lớn thành định dạng K (ví dụ: 582000 -> 582K)
    private String formatViews(int views) {
        if (views >= 1000) {
            return (views / 1000) + "K";
        }
        return String.valueOf(views);
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvViews, tvRank;
        ImageView imgCover;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvRankingName);
            tvViews = itemView.findViewById(R.id.tvRankingViews);
            tvRank = itemView.findViewById(R.id.tvRankNumber);
            imgCover = itemView.findViewById(R.id.imgRankingCover);
        }
    }
}