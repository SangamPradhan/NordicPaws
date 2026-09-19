package com.sangampradhan.nordicpaws.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.sangampradhan.nordicpaws.R;
import com.sangampradhan.nordicpaws.models.Pet;
import java.util.List;

public class PetProfileAdapter extends RecyclerView.Adapter<PetProfileAdapter.PetViewHolder> {

    private List<Pet> petList;
    private OnPetClickListener clickListener;

    public interface OnPetClickListener {
        void onPetClick(Pet pet);
        void onMoreOptionsClick(Pet pet, View anchorView);
    }

    public PetProfileAdapter(List<Pet> petList, OnPetClickListener clickListener) {
        this.petList = petList;
        this.clickListener = clickListener;
    }

    @NonNull
    @Override
    public PetViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_pet_profile, parent, false);
        return new PetViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PetViewHolder holder, int position) {
        Pet pet = petList.get(position);
        holder.bind(pet, clickListener);
    }

    @Override
    public int getItemCount() {
        return petList != null ? petList.size() : 0;
    }

    public void removePet(int position) {
        if (position >= 0 && position < petList.size()) {
            petList.remove(position);
            notifyItemRemoved(position);
        }
    }

    public Pet getPet(int position) {
        return petList.get(position);
    }

    static class PetViewHolder extends RecyclerView.ViewHolder {
        ImageView ivPetImage, btnMoreOptions, ivStat2Icon;
        TextView tvPetName, tvPetBreed, tvStatusBadge, tvTagType, tvTagAge, tvTagOther, tvStat1Val, tvStat2Label, tvStat2Val;

        public PetViewHolder(@NonNull View itemView) {
            super(itemView);
            ivPetImage = itemView.findViewById(R.id.ivPetImage);
            btnMoreOptions = itemView.findViewById(R.id.btnMoreOptions);
            tvPetName = itemView.findViewById(R.id.tvPetName);
            tvPetBreed = itemView.findViewById(R.id.tvPetBreed);
            tvStatusBadge = itemView.findViewById(R.id.tvStatusBadge);
            tvTagType = itemView.findViewById(R.id.tvTagType);
            tvTagAge = itemView.findViewById(R.id.tvTagAge);
            tvTagOther = itemView.findViewById(R.id.tvTagOther);
            tvStat1Val = itemView.findViewById(R.id.tvStat1Val);
            tvStat2Label = itemView.findViewById(R.id.tvStat2Label);
            tvStat2Val = itemView.findViewById(R.id.tvStat2Val);
            ivStat2Icon = itemView.findViewById(R.id.ivStat2Icon);
        }

        public void bind(Pet pet, OnPetClickListener listener) {
            ivPetImage.setImageResource(pet.getAvatarResId());
            tvPetName.setText(pet.getName());
            tvPetBreed.setText(pet.getBreedAndGender());
            tvStatusBadge.setText(pet.getStatusBadge());
            tvTagType.setText(pet.getTagType());
            tvTagAge.setText(pet.getTagAge());
            tvTagOther.setText(pet.getTagOther());
            tvStat1Val.setText(pet.getStat1Val());
            tvStat2Label.setText(pet.getStat2Label());
            tvStat2Val.setText(pet.getStat2Val());
            ivStat2Icon.setImageResource(pet.getStat2IconResId());

            itemView.setOnClickListener(v -> {
                if (listener != null) listener.onPetClick(pet);
            });

            btnMoreOptions.setOnClickListener(v -> {
                if (listener != null) listener.onMoreOptionsClick(pet, v);
            });
        }
    }
}
