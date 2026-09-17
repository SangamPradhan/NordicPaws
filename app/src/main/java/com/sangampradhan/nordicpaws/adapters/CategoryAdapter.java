package com.sangampradhan.nordicpaws.adapters;

import android.graphics.Color;
import android.graphics.PorterDuff;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.sangampradhan.nordicpaws.R;

import java.util.List;

public class CategoryAdapter extends RecyclerView.Adapter<CategoryAdapter.CategoryViewHolder> {

    private List<String> categories;
    private String selectedCategory = "All";
    private OnCategorySelectedListener listener;

    public interface OnCategorySelectedListener {
        void onCategorySelected(String category);
    }

    public CategoryAdapter(List<String> categories, OnCategorySelectedListener listener) {
        this.categories = categories;
        this.listener = listener;
    }

    @NonNull
    @Override
    public CategoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_category_chip, parent, false);
        return new CategoryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CategoryViewHolder holder, int position) {
        String cat = categories.get(position);
        holder.bind(cat);
    }

    @Override
    public int getItemCount() {
        return categories.size();
    }

    class CategoryViewHolder extends RecyclerView.ViewHolder {
        LinearLayout chipContainer;
        TextView chipText;

        CategoryViewHolder(View itemView) {
            super(itemView);
            chipContainer = itemView.findViewById(R.id.chipContainer);
            chipText = itemView.findViewById(R.id.chipText);

            itemView.setOnClickListener(v -> {
                int pos = getAdapterPosition();
                if (pos != RecyclerView.NO_POSITION) {
                    selectedCategory = categories.get(pos);
                    notifyDataSetChanged();
                    if (listener != null) listener.onCategorySelected(selectedCategory);
                }
            });
        }

        void bind(String category) {
            chipText.setText(category);
            if (category.equals(selectedCategory)) {
                chipContainer.getBackground().setColorFilter(Color.parseColor("#1b1b1f"), PorterDuff.Mode.SRC_IN);
                chipText.setTextColor(Color.WHITE);
            } else {
                chipContainer.getBackground().setColorFilter(Color.parseColor("#eaf1ff"), PorterDuff.Mode.SRC_IN);
                chipText.setTextColor(Color.parseColor("#46464b"));
            }
        }
    }
}
