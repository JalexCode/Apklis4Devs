package com.jalexcode.apklis4devsmanager.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
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

import cu.kareldv.apklis.api2.model.Application;
import de.hdodenhof.circleimageview.CircleImageView;

public class AppItemAdapter extends RecyclerView.Adapter<AppItemAdapter.AppItemViewHolder> implements Filterable {
    private List<Application> applications = new ArrayList<>();
    private List<Application> filteredApplications = new ArrayList<>();
    private Context context;

    public AppItemAdapter(Context context) {
        this.context = context;
    }

    @NonNull
    @Override
    public AppItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View layoutView = LayoutInflater.from(parent.getContext()).inflate(R.layout.apps_item, parent, false);
        return new AppItemViewHolder(layoutView);
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    public void onBindViewHolder(@NonNull AppItemViewHolder holder, @SuppressLint("RecyclerView") int position) {
        Application application = filteredApplications.get(position);
        // IMAGE
        Glide.with(context).load(application.getIconURL()).into(holder.appImage);
        // REVISION
        if (application.inRevision()){
            holder.appInRevision.setImageDrawable(context.getResources().getDrawable(R.drawable.ic_review));
        }else{
            holder.appInRevision.setImageDrawable(context.getResources().getDrawable(R.drawable.ic_verified));
        }
        // TITLE
        holder.appTitle.setText(application.getName());
        // RATING
        holder.ratingBar.setRating(application.getRating());
        // DOWNLOADS
        holder.appDownloads.setText(String.format("Descargas: %d", application.getDownloadCount()));
        // SALES
        holder.appPurchases.setText(String.format("Compras: %d", application.getSaleCount()));
        // TOTAL MONEY
        holder.totalMoneyCount.setText(String.format("%d CUP", application.getPrice()));
    }

    @Override
    public int getItemCount() {
        return filteredApplications.size();
    }

    public void setApplications(List<Application> list){
        applications = list;
        filteredApplications.addAll(list);
        notifyDataSetChanged();
    }

    @Override
    public Filter getFilter() {
        return new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence query) {
                List<Application> listResult = new ArrayList<>();
                if (query == null)
                    listResult.addAll(applications);
                else {
                    query = query.toString().toLowerCase();
                    for (Application application : applications)
                        if (application.getName().toLowerCase().contains(query))
                            listResult.add(application);
                }

                FilterResults result = new FilterResults();
                result.values = listResult;
                result.count = listResult.size();
                return result;
            }

            @Override
            protected void publishResults(CharSequence query, FilterResults result) {
                filteredApplications.clear();
                if (result == null)
                    filteredApplications.addAll(applications);
                else
                    for (Application application : (List<Application>) result.values)
                        filteredApplications.add(application);
                notifyDataSetChanged();
            }
        };
    }

    class AppItemViewHolder extends RecyclerView.ViewHolder {
        private TextView appTitle,appDownloads, appPurchases, totalMoneyCount;
        private CardView cardView;
        private CircleImageView appImage;
        private ImageView appInRevision;
        private RatingBar ratingBar;

        public AppItemViewHolder(View itemView) {
            super(itemView);
            // onClick
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent appDetails = new Intent(context, AppDetailsActivity.class);
                    Bundle b = new Bundle();
                    b.putSerializable("application", applications.get(getAdapterPosition()));
                    appDetails.putExtras(b);
                    context.startActivity(appDetails);
                }
            });
            // initi ui
            cardView = itemView.findViewById(R.id.cardView);
            appTitle = itemView.findViewById(R.id.appTitle);
            appDownloads = itemView.findViewById(R.id.appDownloadsText);
            appPurchases = itemView.findViewById(R.id.appPurchases);
            totalMoneyCount = itemView.findViewById(R.id.totalMoneyCount);

            appImage = itemView.findViewById(R.id.appImage);
            appInRevision = itemView.findViewById(R.id.appInRevision);
            ratingBar = itemView.findViewById(R.id.ratingBar);
        }
    }
}
