package com.sangampradhan.nordicpaws.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.sangampradhan.nordicpaws.R;

import java.util.List;

public class BannerPagerAdapter extends RecyclerView.Adapter<BannerPagerAdapter.BannerViewHolder> {

    public static class BannerItem {
        public String label;
        public String headline;
        public String body;
        public String action1;
        public String action2;

        public BannerItem(String label, String headline, String body, String action1, String action2) {
            this.label = label;
            this.headline = headline;
            this.body = body;
            this.action1 = action1;
            this.action2 = action2;
        }
    }

    private final List<BannerItem> items;

    public BannerPagerAdapter(List<BannerItem> items) {
        this.items = items;
    }

    @NonNull
    @Override
    public BannerViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_banner_card, parent, false);
        return new BannerViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull BannerViewHolder holder, int position) {
        BannerItem item = items.get(position);
        holder.label.setText(item.label);
        holder.headline.setText(item.headline);
        holder.body.setText(item.body);
        holder.action1.setText(item.action1);

        if (item.action2 != null && !item.action2.isEmpty()) {
            holder.action2.setVisibility(View.VISIBLE);
            holder.action2.setText(item.action2);
        } else {
            holder.action2.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class BannerViewHolder extends RecyclerView.ViewHolder {
        TextView label, headline, body, action1, action2;

        BannerViewHolder(@NonNull View itemView) {
            super(itemView);
            label = itemView.findViewById(R.id.bannerLabel);
            headline = itemView.findViewById(R.id.bannerHeadline);
            body = itemView.findViewById(R.id.bannerBody);
            action1 = itemView.findViewById(R.id.bannerAction1);
            action2 = itemView.findViewById(R.id.bannerAction2);
        }
    }
}
