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

public class RankingAdapter extends RecyclerView.Adapter<RankingAdapter.ViewHolder> {
    private List<StoryRanking> list;

    public RankingAdapter(List<StoryRanking> list) { this.list = list; }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_ranking, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        StoryRanking story = list.get(position);
        holder.tvName.setText(story.getName());
        holder.tvViews.setText(story.getViews());
        holder.tvRank.setText(String.valueOf(story.getRank()));
        holder.imgCover.setImageResource(story.getImageResId());

        // Đổi màu vòng tròn số thứ tự cho giống mẫu
        if (position == 0) holder.tvRank.getBackground().setTint(Color.parseColor("#FBC02D")); // Vàng Top 1
        else if (position == 1) holder.tvRank.getBackground().setTint(Color.parseColor("#80CBC4")); // Xanh Top 2
        else if (position == 2) holder.tvRank.getBackground().setTint(Color.parseColor("#BCAAA4")); // Nâu Top 3
        else holder.tvRank.getBackground().setTint(Color.parseColor("#BDBDBD")); // Xám còn lại
    }

    @Override
    public int getItemCount() { return list.size(); }

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
