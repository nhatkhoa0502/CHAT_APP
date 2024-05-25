package com.example.chatapp.Adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.chatapp.ListFriendRequestActivity;
import com.example.chatapp.MessageActivity;
import com.example.chatapp.Model.User;
import com.example.chatapp.ProfileUserViewActivity;
import com.example.chatapp.R;

import java.util.List;

public class ListFriendRequestAdapter extends RecyclerView.Adapter<ListFriendRequestAdapter.ViewHolder> {
    private Context mContext;
    private List<User> mUsers;
    private OnItemClickListener mListener;

    public interface OnItemClickListener {
        void onAcceptClick(int position);

        void onRejectClick(int position);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        mListener = listener;
    }

    public ListFriendRequestAdapter(Context mContext, List<User> mUsers) {
        this.mContext = mContext;
        this.mUsers = mUsers;
    }

    @NonNull
    @Override
    public ListFriendRequestAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.friend_request_item, parent, false);
        return new ListFriendRequestAdapter.ViewHolder(view, mListener);
    }

    @Override
    public void onBindViewHolder(@NonNull ListFriendRequestAdapter.ViewHolder holder, int position) {
        User user = mUsers.get(position);
        holder.username.setText(user.getName());
        Glide.with(mContext).load(user.getProfile()).into(holder.profileImage);
    }

    @Override
    public int getItemCount() {
        return mUsers.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public TextView username;
        public ImageView profileImage;
        public ImageButton btn_accept;
        public ImageButton btn_reject;

        public ViewHolder(@NonNull View itemView, final OnItemClickListener listener) {
            super(itemView);
            username = itemView.findViewById(R.id.username);
            profileImage = itemView.findViewById(R.id.profileImage);
            btn_accept = itemView.findViewById(R.id.btn_accept);
            btn_reject = itemView.findViewById(R.id.btn_reject);
            btn_accept.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (listener != null) {
                        int position = getAdapterPosition();
                        if (position != RecyclerView.NO_POSITION) {
                            listener.onAcceptClick(position);
//                            Toast.makeText(v.getContext(), "position: " + position, Toast.LENGTH_SHORT).show();
                        }
                    }
                }
            });

            btn_reject.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (listener != null) {
                        int position = getAdapterPosition();
                        if (position != RecyclerView.NO_POSITION) {
                            listener.onRejectClick(position);
//                            Toast.makeText(v.getContext(), "position: " + position, Toast.LENGTH_SHORT).show();
                        }
                    }
                }
            });
        }
    }
}
