package com.sangampradhan.nordicpaws.fragments;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.sangampradhan.nordicpaws.MainActivity;
import com.sangampradhan.nordicpaws.R;
import com.sangampradhan.nordicpaws.models.Pet;
import com.sangampradhan.nordicpaws.utils.LocalStorageManager;

public class PetProfileDetailFragment extends Fragment {

    private static final String ARG_PET_NAME = "pet_name";
    private static final String ARG_PET_BREED = "pet_breed";
    private static final String ARG_PET_AGE = "pet_age";
    private static final String ARG_PET_WEIGHT = "pet_weight";
    private static final String ARG_PET_STATUS = "pet_status";
    private static final String ARG_PET_IMAGE_PATH = "pet_image_path";
    private static final String ARG_PET_AVATAR_RES = "pet_avatar_res";

    public PetProfileDetailFragment() {
        // Required empty public constructor
    }

    public static PetProfileDetailFragment newInstance(Pet pet) {
        PetProfileDetailFragment fragment = new PetProfileDetailFragment();
        Bundle args = new Bundle();
        if (pet != null) {
            args.putString(ARG_PET_NAME, pet.getName());
            args.putString(ARG_PET_BREED, pet.getBreedAndGender());
            args.putString(ARG_PET_AGE, pet.getTagAge());
            args.putString(ARG_PET_WEIGHT, pet.getStat1Val());
            args.putString(ARG_PET_STATUS, pet.getStatusBadge());
            args.putString(ARG_PET_IMAGE_PATH, pet.getLocalImagePath());
            args.putInt(ARG_PET_AVATAR_RES, pet.getAvatarResId());
        }
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_pet_profile_detail, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        ImageButton btnBack = view.findViewById(R.id.btnBack);
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> {
                if (getActivity() instanceof MainActivity) {
                    ((MainActivity) getActivity()).onBackPressed();
                }
            });
        }

        ImageView ivPetHeroPhoto = view.findViewById(R.id.ivPetHeroPhoto);
        TextView tvPetDetailName = view.findViewById(R.id.tvPetDetailName);
        TextView tvPetDetailStatus = view.findViewById(R.id.tvPetDetailStatus);
        TextView tvPetDetailBreed = view.findViewById(R.id.tvPetDetailBreed);
        TextView tvPetDetailAge = view.findViewById(R.id.tvPetDetailAge);
        TextView tvPetDetailWeight = view.findViewById(R.id.tvPetDetailWeight);

        if (getArguments() != null) {
            String name = getArguments().getString(ARG_PET_NAME, "Milo");
            String breed = getArguments().getString(ARG_PET_BREED, "Golden Retriever");
            String age = getArguments().getString(ARG_PET_AGE, "2 yrs");
            String weight = getArguments().getString(ARG_PET_WEIGHT, "28.5 kg");
            String status = getArguments().getString(ARG_PET_STATUS, "Healthy & Active");
            String imagePath = getArguments().getString(ARG_PET_IMAGE_PATH, "");
            int avatarRes = getArguments().getInt(ARG_PET_AVATAR_RES, R.drawable.dog);

            if (tvPetDetailName != null) tvPetDetailName.setText(name);
            if (tvPetDetailBreed != null) tvPetDetailBreed.setText(breed);
            if (tvPetDetailAge != null) tvPetDetailAge.setText(age);
            if (tvPetDetailWeight != null) tvPetDetailWeight.setText(weight);
            if (tvPetDetailStatus != null) tvPetDetailStatus.setText(status != null ? status : "Wellness Verified");

            if (ivPetHeroPhoto != null) {
                if (imagePath != null && !imagePath.isEmpty()) {
                    Bitmap bmp = LocalStorageManager.loadLocalBitmap(imagePath);
                    if (bmp != null) {
                        ivPetHeroPhoto.setImageBitmap(bmp);
                    } else {
                        ivPetHeroPhoto.setImageResource(avatarRes != 0 ? avatarRes : R.drawable.dog);
                    }
                } else {
                    ivPetHeroPhoto.setImageResource(avatarRes != 0 ? avatarRes : R.drawable.dog);
                }
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (getActivity() instanceof MainActivity) {
            ((MainActivity) getActivity()).setTopBarTitle("Pet Profile");
        }
    }
}
