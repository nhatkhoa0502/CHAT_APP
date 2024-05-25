package com.example.chatapp.Adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.chatapp.MessageActivity;
import com.example.chatapp.Model.Chat;
import com.example.chatapp.Model.User;
import com.example.chatapp.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.List;

public class FriendAdapter extends RecyclerView.Adapter<FriendAdapter.ViewHolder> {
    private Context mContext;
    private List<User> mUsers;
    private boolean isChat;

    String theLastMessage;

    public FriendAdapter(Context mContext, List<User> mUsers, boolean isChat) {
        this.mContext = mContext;
        this.mUsers = mUsers;
        this.isChat = isChat;
    }

    @NonNull
    @Override
    public FriendAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.friend_item, parent, false);
        return new FriendAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull FriendAdapter.ViewHolder holder, int position) {
        User user = mUsers.get(position);
        holder.username.setText(user.getName());
        Glide.with(mContext).load(user.getProfile()).into(holder.profileImage);

        if (isChat) {
            lastMessage(user.getId(), user.getName(), holder.last_message);
        } else {
            holder.last_message.setVisibility(View.GONE);
        }

        if (isChat) {
            if (user.getStatus().equals("online")) {
                holder.img_on.setVisibility(View.VISIBLE);
                holder.img_off.setVisibility(View.GONE);
            } else {
                holder.img_on.setVisibility(View.GONE);
                holder.img_off.setVisibility(View.VISIBLE);
            }
        }

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(mContext, MessageActivity.class);
                intent.putExtra("userid", user.getId());
                mContext.startActivity(intent);
            }
        });

    }

    @Override
    public int getItemCount() {
        return mUsers.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public TextView username;
        public ImageView profileImage;
        private ImageView img_on;
        private ImageView img_off;
        private TextView last_message;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            this.username = itemView.findViewById(R.id.username);
            this.profileImage = itemView.findViewById(R.id.profileImage);
            this.img_on = itemView.findViewById(R.id.img_on);
            this.img_off = itemView.findViewById(R.id.img_off);
            this.last_message = itemView.findViewById(R.id.last_message);
        }
    }

    private void lastMessage(final String userid, final String username, final TextView last_message) {
        theLastMessage = "default";
        final FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("chats");

        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (firebaseUser!= null) {
                    for (DataSnapshot sn : snapshot.getChildren()) {
                        Chat chat = sn.getValue(Chat.class);
                        if (chat.getReceiver().equals(firebaseUser.getUid()) && chat.getSender().equals(userid)) {
                            int lastIndex = username.lastIndexOf(' ');
                            if(chat.getDuration() == 0){
                                theLastMessage = username.substring(lastIndex + 1) + ": " + chat.getMessage();
                            }else{
                                theLastMessage = username.substring(lastIndex + 1) + ": [Audio Message]";
                            }
                        } else if (chat.getReceiver().equals(userid) && chat.getSender().equals(firebaseUser.getUid())) {
                            if(chat.getDuration() == 0){
                                theLastMessage = "You: " + chat.getMessage();
                            }else{
                                theLastMessage = "You: [Audio Message]";
                            }
                        }
                    }
                    if (theLastMessage.equals("default")) {
                        last_message.setText("No Message!!!");
                    } else {
                        last_message.setText(theLastMessage);
                    }
                    theLastMessage = "default";
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
}
