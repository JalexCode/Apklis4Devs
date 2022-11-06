package com.jalexcode.apklis4devsmanager.activity;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.text.Html;
import android.view.Window;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.jalexcode.apklis4devsmanager.R;
import com.jalexcode.apklis4devsmanager.adapter.CommentItemAdapter;
import com.kaopiz.kprogresshud.KProgressHUD;

import cu.kareldv.apklis.api2.model.Application;
import cu.kareldv.apklis.api2.model.Comment;
import de.hdodenhof.circleimageview.CircleImageView;

public class AppDetailsActivity extends AppCompatActivity {
    private static Context context;

    private final String INTENT_NAME = "application";
    private Application application;

    private CircleImageView appImage;
    private TextView appNameText, appDownloadsText, appPurchases, priceBadge, descriptionText;
    private ImageView backButton;
    private RatingBar ratingBar;
    private RecyclerView commentsRecyclerView;
    private KProgressHUD progressHUD;

    private CommentItemAdapter commentItemAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_app_details);
        context = this;
        //
        setUpUI();
        //
        Intent app = getIntent();
        if (app != null) {
            if (app.hasExtra(INTENT_NAME)) {
                application = (Application) app.getSerializableExtra(INTENT_NAME);
            }
        }
        //
        loadInfoFromApp();
    }

    private void setUpUI() {
        // DASHBOARD
        backButton = findViewById(R.id.backButton);
        backButton.setOnClickListener(v -> onBackPressed());
        appImage = findViewById(R.id.appImage);
        appNameText = findViewById(R.id.appNameText);
        ratingBar = findViewById(R.id.ratingBar);
        appDownloadsText = findViewById(R.id.appDownloadsText);
        appPurchases = findViewById(R.id.appPurchases);
        priceBadge = findViewById(R.id.priceBadge);
        descriptionText = findViewById(R.id.descriptionText);
        // RECYCLER VIEW
        commentsRecyclerView = findViewById(R.id.commentsRecyclerView);
        //
        initProgressView();
    }

    private void initProgressView() {
        progressHUD = new KProgressHUD(this);
        progressHUD.setStyle(KProgressHUD.Style.SPIN_INDETERMINATE);
        progressHUD.setCancellable(true);
        progressHUD.setAnimationSpeed(2);
        progressHUD.setDimAmount(0.5f);
    }

    private void loadInfoFromApp() {
        Glide.with(this).load(application.getIconURL()).into(appImage);
        appNameText.setText(application.getName());
        ratingBar.setRating(application.getRating());
        appDownloadsText.setText("Descargas: " + application.getDownloadCount());
        appPurchases.setText("Compras: " + application.getSaleCount());
        priceBadge.setText(String.format("$%d", application.getPrice()));
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            descriptionText.setText(Html.fromHtml(application.getDescription(), Html.FROM_HTML_MODE_COMPACT));
        } else {
            descriptionText.setText(Html.fromHtml(application.getDescription()));
        }
        //
        commentItemAdapter = new CommentItemAdapter(this);
        commentsRecyclerView.setAdapter(commentItemAdapter);
        commentItemAdapter.setComments(application.getComments());
    }

    public static void showCommentInfoDialog(Comment comment) {
        new CommentInfoDialog(comment);
    }

    public static class CommentInfoDialog {
        public CommentInfoDialog(Comment comment) {
            final Dialog commentInfoDialog = new Dialog(context);

            commentInfoDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            commentInfoDialog.setCancelable(true);
            commentInfoDialog.getWindow().setBackgroundDrawable(new android.graphics.drawable.ColorDrawable(Color.TRANSPARENT));
            commentInfoDialog.setContentView(R.layout.dialog_comment_details);

            CircleImageView userAvatarImage = commentInfoDialog.findViewById(R.id.userAvatarImage);
            TextView userNameComment = commentInfoDialog.findViewById(R.id.userNameComment);
            TextView userFullNameComment = commentInfoDialog.findViewById(R.id.userFullNameComment);
            RatingBar ratingBar = commentInfoDialog.findViewById(R.id.ratingBar);
            TextView commentText = commentInfoDialog.findViewById(R.id.commentText);

            // PUT INFO
            String avatar = comment.getUser().getAvatarUrl();
            if (!avatar.isEmpty() && !avatar.equals("null")) {
                Glide.with(context).load(avatar).into(userAvatarImage);
            }
            userNameComment.setText(comment.getUser().getUsername());
            String userFullName = comment.getUser().getFirst_name() + " " + comment.getUser().getLast_name();
            if (!userFullName.trim().isEmpty() && !userFullName.contains("null")) {
                userFullNameComment.setText(userFullName);
            }
            ratingBar.setRating(comment.getRating());
            commentText.setText(comment.getComment());

            commentInfoDialog.show();
        }
    }

}