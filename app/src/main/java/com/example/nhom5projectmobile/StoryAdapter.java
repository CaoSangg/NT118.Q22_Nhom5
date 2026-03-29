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

    public StoryAdapter(List<Story> storyList) {
        this.storyList = storyList;
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
        holder.tvChapter.setText(story.getLatestChapter());

        com.bumptech.glide.Glide.with(holder.itemView.getContext())
                .load(story.getCoverImage())
                .placeholder(R.drawable.ic_launcher_background)
                .into(holder.imgCover);
    }

    @Override
    public int getItemCount() {
        return storyList.size();
    }

    static class StoryViewHolder extends RecyclerView.ViewHolder {
        ImageView imgCover;
        TextView tvTitle, tvChapter;

        public StoryViewHolder(@NonNull View itemView) {
            super(itemView);
            imgCover = itemView.findViewById(R.id.imgCover);
            tvTitle = itemView.findViewById(R.id.tvTitle);
            tvChapter = itemView.findViewById(R.id.tvChapter);
        }
    }
}
