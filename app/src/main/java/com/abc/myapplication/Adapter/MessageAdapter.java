package com.example.chatapp.Adapter;

import android.content.Context;
import android.content.Intent;
import android.media.AudioAttributes;
import android.media.MediaPlayer;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.chatapp.MainActivity;
import com.example.chatapp.MessageActivity;
import com.example.chatapp.MicRecorderActivity;
import com.example.chatapp.Model.Chat;
import com.example.chatapp.Model.User;
import com.example.chatapp.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.storage.FirebaseStorage;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.ViewHolder> {

    public static final int MSG_TYPE_LEFT = 0;
    public static final int MSG_TYPE_RIGHT = 1;
    private Context mContext;
    private List<Chat> mChat;
    private String imageUrl;
    FirebaseUser firebaseUser;
    private boolean isPlaying = false;

    private MediaPlayer mediaPlayer;

    private Handler handler;
    private Runnable runnable;
    private long seconds;


    public MessageAdapter(Context mContext, List<Chat> mChats, String imageUrl) {
        this.mContext = mContext;
        this.mChat = mChats;
        this.imageUrl = imageUrl;
    }

    @NonNull
    @Override
    public MessageAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == MSG_TYPE_RIGHT) {
            View view = LayoutInflater.from(mContext).inflate(R.layout.chat_item_right, parent, false);
            return new MessageAdapter.ViewHolder(view);
        } else {
            View view = LayoutInflater.from(mContext).inflate(R.layout.chat_item_left, parent, false);
            return new MessageAdapter.ViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull MessageAdapter.ViewHolder holder, int position) {
        Chat chat = mChat.get(position);
        if (chat.getDuration() == 0) {
            holder.show_message.setText(chat.getMessage());
        } else {
            holder.show_message.setCompoundDrawablesWithIntrinsicBounds(ContextCompat.getDrawable(mContext, R.drawable.ic_play_arrow_24), null, null, null);
            seconds = chat.getDuration() / 1000;
            holder.show_message.setText(String.format("  %02d:%02d", seconds / 60, seconds % 60));
        }

        // Số long đại diện cho thời gian
        Date date = new Date(chat.getTime());
        SimpleDateFormat formatter = new SimpleDateFormat("HH:mm:ss dd-MM-yyyy");
        holder.show_time.setText(formatter.format(date));
        Glide.with(mContext).load(imageUrl).into(holder.profile_image);

        if (position == (mChat.size() - 1)) {
            if (chat.isIsseen()) {
                holder.txt_seen.setText("Seen");
            } else {
                holder.txt_seen.setText("Sent");
            }
        } else {
            holder.txt_seen.setVisibility(View.GONE);
        }

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (chat.getDuration() != 0 && !isPlaying) {
                    isPlaying = true;
                    seconds = chat.getDuration() / 1000;
                    holder.show_message.setCompoundDrawablesWithIntrinsicBounds(ContextCompat.getDrawable(mContext, R.drawable.ic_stop_24), null, null, null);

                    // Initialize MediaPlayer
                    mediaPlayer = new MediaPlayer();

                    // Set AudioAttributes for the MediaPlayer
                    mediaPlayer.setAudioAttributes(new AudioAttributes.Builder()
                            .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                            .setUsage(AudioAttributes.USAGE_MEDIA)
                            .build());

                    // Set DataSource for the MediaPlayer
                    try {
                        mediaPlayer.setDataSource(chat.getMessage());
                    } catch (IOException e) {
                        e.printStackTrace();
                        Toast.makeText(mContext, "Error setting data source", Toast.LENGTH_SHORT).show();
                    }

                    // Prepare the MediaPlayer asynchronously
                    mediaPlayer.prepareAsync();

                    handler = new Handler();
                    runnable = new Runnable() {
                        @Override
                        public void run() {
                            seconds--;
                            holder.show_message.setText(String.format("  %02d:%02d", seconds / 60, seconds % 60));
                            handler.postDelayed(this, 1000);
                        }
                    };

                    // Set a listener for when the MediaPlayer is prepared
                    mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                        @Override
                        public void onPrepared(MediaPlayer mp) {
                            // Start playing the audio
                            mediaPlayer.start();
                            handler.postDelayed(runnable, 1000);
                        }
                    });
                    mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                        @Override
                        public void onCompletion(MediaPlayer mp) {
                            isPlaying = false;
                            handler.removeCallbacks(runnable);
                            if (mediaPlayer != null) {
                                mediaPlayer.release();
                                mediaPlayer = null;
                            }
                            holder.show_message.setCompoundDrawablesWithIntrinsicBounds(ContextCompat.getDrawable(mContext, R.drawable.ic_play_arrow_24), null, null, null);
                            seconds = chat.getDuration() / 1000;
                            holder.show_message.setText(String.format("  %02d:%02d", seconds / 60, seconds % 60));
                        }
                    });
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return mChat.size();
    }

    @Override
    public int getItemViewType(int position) {
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        if (mChat.get(position).getSender().equals(firebaseUser.getUid())) {
            return MSG_TYPE_RIGHT;
        } else {
            return MSG_TYPE_LEFT;
        }
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public TextView show_message;
        public TextView show_time;
        public ImageView profile_image;
        public TextView txt_seen;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            this.show_message = itemView.findViewById(R.id.show_message);
            this.profile_image = itemView.findViewById(R.id.profile_image);
            this.show_time = itemView.findViewById(R.id.show_time);
            this.txt_seen = itemView.findViewById(R.id.txt_seen);
        }
    }
}