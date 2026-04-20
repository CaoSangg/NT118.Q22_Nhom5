package com.example.nhom5projectmobile;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class HotAdapter extends RecyclerView.Adapter<HotAdapter.ViewHolder> {
    private List<StoryHot> list;

    public HotAdapter(List<StoryHot> list) { this.list = list; }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_story_hot, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        StoryHot story = list.get(position);
        holder.tvName.setText(story.getName());
        holder.tvChapter.setText("Chapter " + story.getChapter());
        holder.imgCover.setImageResource(story.getImageRes());
    }

    @Override
    public int getItemCount() { return list.size(); }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView imgCover;
        TextView tvName, tvChapter;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            imgCover = itemView.findViewById(R.id.imgHotCover);
            tvName = itemView.findViewById(R.id.tvHotName);
            tvChapter = itemView.findViewById(R.id.tvHotChapter);
        }
    }
}