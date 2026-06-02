package com.example.nhom5projectmobile;

import android.content.Context;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class CommentAdapter extends RecyclerView.Adapter<CommentAdapter.CommentViewHolder> {

    private Context context;
    private List<Comment> commentList;

    public CommentAdapter(Context context, List<Comment> commentList) {
        this.context = context;
        this.commentList = commentList;
    }

    @NonNull
    @Override
    public CommentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_comment, parent, false);
        return new CommentViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CommentViewHolder holder, int position) {
        // Máy sẽ tự hiểu Comment ở đây là class Model nằm chung một thư mục gói vừa tạo ở Bước 1
        Comment comment = commentList.get(position);

        holder.tvCommentName.setText(comment.getUserName());
        holder.tvCommentContent.setText(comment.getContent());

        // Định dạng thời gian hiển thị tương đối dạng: "vài phút trước", "3 giờ trước"...
        if (comment.getTimestamp() != null) {
            long timeInMillis = comment.getTimestamp().toDate().getTime();
            String timeAgo = (String) DateUtils.getRelativeTimeSpanString(
                    timeInMillis,
                    System.currentTimeMillis(),
                    DateUtils.MINUTE_IN_MILLIS
            );
            holder.tvCommentTime.setText("• " + timeAgo);
        } else {
            holder.tvCommentTime.setText("• Vừa xong");
        }
    }

    @Override
    public int getItemCount() {
        return commentList != null ? commentList.size() : 0;
    }

    static class CommentViewHolder extends RecyclerView.ViewHolder {
        TextView tvCommentName, tvCommentTime, tvCommentContent;

        public CommentViewHolder(@NonNull View itemView) {
            super(itemView);
            tvCommentName = itemView.findViewById(R.id.tvCommentName);
            tvCommentTime = itemView.findViewById(R.id.tvCommentTime);
            tvCommentContent = itemView.findViewById(R.id.tvCommentContent);
        }
    }
}