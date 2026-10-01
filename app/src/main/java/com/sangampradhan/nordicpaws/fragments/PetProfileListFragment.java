package com.sangampradhan.nordicpaws.fragments;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.sangampradhan.nordicpaws.AddEditPetActivity;
import com.sangampradhan.nordicpaws.MainActivity;
import com.sangampradhan.nordicpaws.R;
import com.sangampradhan.nordicpaws.adapters.PetProfileAdapter;
import com.sangampradhan.nordicpaws.models.Pet;
import com.sangampradhan.nordicpaws.utils.FirestoreManager;

import java.util.ArrayList;
import java.util.List;

public class PetProfileListFragment extends Fragment {

    private RecyclerView rvPets;
    private PetProfileAdapter adapter;
    private List<Pet> petList = new ArrayList<>();
    private FirestoreManager firestoreManager;

    public PetProfileListFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_pet_profile_list, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        firestoreManager = new FirestoreManager();

        ImageButton btnBack = view.findViewById(R.id.btnBack);
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> {
                if (getActivity() instanceof MainActivity) {
                    ((MainActivity) getActivity()).onBackPressed();
                }
            });
        }
        
        view.findViewById(R.id.btnAddPet).setOnClickListener(v -> {
            Intent intent = new Intent(getContext(), AddEditPetActivity.class);
            startActivity(intent);
        });

        rvPets = view.findViewById(R.id.rvPets);
        rvPets.setLayoutManager(new LinearLayoutManager(getContext()));

        adapter = new PetProfileAdapter(petList, new PetProfileAdapter.OnPetClickListener() {
            @Override
            public void onPetClick(Pet pet) {
                if (getActivity() instanceof MainActivity) {
                    ((MainActivity) getActivity()).loadFragment(new PetProfileDetailFragment(), true);
                }
            }

            @Override
            public void onMoreOptionsClick(Pet pet, View anchorView) {
                androidx.appcompat.widget.PopupMenu popup = new androidx.appcompat.widget.PopupMenu(getContext(), anchorView);
                popup.getMenu().add("Edit");
                popup.getMenu().add("Delete");
                popup.setOnMenuItemClickListener(item -> {
                    if (item.getTitle().equals("Edit")) {
                        openEditScreen(pet);
                    } else if (item.getTitle().equals("Delete")) {
                        showDeleteConfirmation(pet, petList.indexOf(pet));
                    }
                    return true;
                });
                popup.show();
            }
        });

        rvPets.setAdapter(adapter);
        setupSwipeGestures();

        loadPetsFromFirestore();
    }
    
    private void loadPetsFromFirestore() {
        firestoreManager.fetchPets(pets -> {
            this.petList = pets;
            adapter = new PetProfileAdapter(petList, new PetProfileAdapter.OnPetClickListener() {
                @Override
                public void onPetClick(Pet pet) {
                    if (getActivity() instanceof MainActivity) {
                        ((MainActivity) getActivity()).loadFragment(new PetProfileDetailFragment(), true);
                    }
                }

                @Override
                public void onMoreOptionsClick(Pet pet, View anchorView) {
                    androidx.appcompat.widget.PopupMenu popup = new androidx.appcompat.widget.PopupMenu(getContext(), anchorView);
                    popup.getMenu().add("Edit");
                    popup.getMenu().add("Delete");
                    popup.setOnMenuItemClickListener(item -> {
                        if (item.getTitle().equals("Edit")) {
                            openEditScreen(pet);
                        } else if (item.getTitle().equals("Delete")) {
                            showDeleteConfirmation(pet, petList.indexOf(pet));
                        }
                        return true;
                    });
                    popup.show();
                }
            });
            rvPets.setAdapter(adapter);
        }, e -> {
            Toast.makeText(getContext(), "Error loading pets from Firestore", Toast.LENGTH_SHORT).show();
        });
    }

    private void setupSwipeGestures() {
        androidx.recyclerview.widget.ItemTouchHelper.SimpleCallback simpleCallback = new androidx.recyclerview.widget.ItemTouchHelper.SimpleCallback(0, androidx.recyclerview.widget.ItemTouchHelper.LEFT | androidx.recyclerview.widget.ItemTouchHelper.RIGHT) {
            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                int position = viewHolder.getAdapterPosition();
                Pet pet = adapter.getPet(position);
                if (direction == androidx.recyclerview.widget.ItemTouchHelper.RIGHT) {
                    adapter.notifyItemChanged(position);
                    openEditScreen(pet);
                } else if (direction == androidx.recyclerview.widget.ItemTouchHelper.LEFT) {
                    adapter.notifyItemChanged(position);
                    showDeleteConfirmation(pet, position);
                }
            }
        };

        androidx.recyclerview.widget.ItemTouchHelper itemTouchHelper = new androidx.recyclerview.widget.ItemTouchHelper(simpleCallback);
        itemTouchHelper.attachToRecyclerView(rvPets);
    }
    
    private void openEditScreen(Pet pet) {
        Intent intent = new Intent(getContext(), AddEditPetActivity.class);
        intent.putExtra("PET_ID", pet.getId());
        startActivity(intent);
    }
    
    private void showDeleteConfirmation(Pet pet, int position) {
        new androidx.appcompat.app.AlertDialog.Builder(getContext())
                .setTitle("Delete Pet")
                .setMessage("Are you sure you want to remove " + pet.getName() + " from your profile?")
                .setPositiveButton("Delete", (dialog, which) -> {
                    adapter.removePet(position);
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (getActivity() instanceof MainActivity) {
            ((MainActivity) getActivity()).setTopBarTitle("Pet Profile");
        }
        loadPetsFromFirestore();
    }
}
