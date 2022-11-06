package com.jalexcode.apklis4devsmanager.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.RatingBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.jalexcode.apklis4devsmanager.activity.AppDetailsActivity;
import com.jalexcode.apklis4devsmanager.R;

import java.util.ArrayList;
import java.util.List;

import cu.kareldv.apklis.api2.model.Comment;
import de.hdodenhof.circleimageview.CircleImageView;

public class CommentItemAdapter extends RecyclerView.Adapter<CommentItemAdapter.AppItemViewHolder> implements Filterable {
    private List<Comment> comments = new ArrayList<>();
    private List<Comment> filteredComments = new ArrayList<>();
    private Context context;

    public CommentItemAdapter(Context context) {
        this.context = context;
    }

    @NonNull
    @Override
    public AppItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View layoutView = LayoutInflater.from(parent.getContext()).inflate(R.layout.comments_item, parent, false);
        return new AppItemViewHolder(layoutView);
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    public void onBindViewHolder(@NonNull AppItemViewHolder holder, @SuppressLint("RecyclerView") int position) {
        Comment comment = filteredComments.get(position);
        // IMAGE
        String avatarUrl = comment.getUser().getAvatarUrl();
        if (!avatarUrl.isEmpty() && !avatarUrl.equals("null")) {
            Glide.with(context).load(avatarUrl).into(holder.userAvatarImage);
        }
        // TITLE
        holder.userNameComment.setText(comment.getUser().getUsername());
        // RATING
        holder.ratingBar.setRating(comment.getRating());
        // COMMENT
        holder.commentText.setText(comment.getComment());
    }

    @Override
    public int getItemCount() {
        return filteredComments.size();
    }

    public void setComments(List<Comment> list){
        comments = list;
        filteredComments.addAll(list);
        notifyDataSetChanged();
    }

    @Override
    public Filter getFilter() {
        return new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence query) {
                List<Comment> listResult = new ArrayList<>();
                if (query == null)
                    listResult.addAll(comments);
                else {
                    query = query.toString().toLowerCase();
                    for (Comment comment : comments)
                        if (comment.getComment().toLowerCase().contains(query))
                            listResult.add(comment);
                }

                FilterResults result = new FilterResults();
                result.values = listResult;
                result.count = listResult.size();
                return result;
            }

            @Override
            protected void publishResults(CharSequence query, FilterResults result) {
                filteredComments.clear();
                if (result == null)
                    filteredComments.addAll(comments);
                else
                    for (Comment comment : (List<Comment>) result.values)
                        filteredComments.add(comment);
                notifyDataSetChanged();
            }
        };
    }

    class AppItemViewHolder extends RecyclerView.ViewHolder {
        private TextView userNameComment,commentText;
        private CardView cardView;
        private CircleImageView userAvatarImage;
        private RatingBar ratingBar;

        public AppItemViewHolder(View itemView) {
            super(itemView);
            // onClick
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    AppDetailsActivity.showCommentInfoDialog(comments.get(getAdapterPosition()));
                }
            });
            // initi ui
            cardView = itemView.findViewById(R.id.cardView);
            userNameComment = itemView.findViewById(R.id.userNameComment);
            commentText = itemView.findViewById(R.id.commentText);
            userAvatarImage = itemView.findViewById(R.id.userAvatarImage);
            ratingBar = itemView.findViewById(R.id.ratingBar);
        }
    }
}
