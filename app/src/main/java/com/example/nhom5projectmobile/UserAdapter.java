package com.example.nhom5projectmobile;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;

import java.util.List;

public class UserAdapter
        extends RecyclerView.Adapter<UserAdapter.ViewHolder> {

    public interface OnToggleManager {
        void onToggle(UserModel user);
    }

    public interface OnDeleteUser {
        void onDelete(UserModel user);
    }

    private final Context context;

    private final List<UserModel> list;

    private final OnToggleManager toggleManager;

    private final OnDeleteUser deleteUser;

    public UserAdapter(
            Context context,
            List<UserModel> list,
            OnToggleManager toggleManager,
            OnDeleteUser deleteUser) {

        this.context = context;
        this.list = list;
        this.toggleManager = toggleManager;
        this.deleteUser = deleteUser;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(
            @NonNull ViewGroup parent,
            int viewType) {

        View view = LayoutInflater.from(context)
                .inflate(
                        R.layout.item_user,
                        parent,
                        false
                );

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(
            @NonNull ViewHolder holder,
            int position) {

        UserModel user = list.get(position);

        holder.tvName.setText(user.getUsername());

        holder.tvEmail.setText(user.getEmail());

        holder.btnManager.setOnClickListener(v -> {

            toggleManager.onToggle(user);
        });

        holder.btnDelete.setOnClickListener(v -> {

            deleteUser.onDelete(user);
        });
    }

    @Override
    public int getItemCount() {

        return list.size();
    }

    static class ViewHolder
            extends RecyclerView.ViewHolder {

        TextView tvName, tvEmail;

        MaterialButton btnManager,
                btnDelete;

        public ViewHolder(@NonNull View itemView) {

            super(itemView);

            tvName =
                    itemView.findViewById(R.id.tvName);

            tvEmail =
                    itemView.findViewById(R.id.tvEmail);

            btnManager =
                    itemView.findViewById(R.id.btnManager);

            btnDelete =
                    itemView.findViewById(R.id.btnDelete);
        }
    }
}