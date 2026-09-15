package com.sangampradhan.nordicpaws.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;
import com.sangampradhan.nordicpaws.MainActivity;
import com.sangampradhan.nordicpaws.R;
import com.sangampradhan.nordicpaws.adapters.PetProfileAdapter;
import com.sangampradhan.nordicpaws.models.Pet;
import java.util.List;

public class PetProfileListFragment extends Fragment {

    public PetProfileListFragment() {
        // Required empty public constructor
    }

    private RecyclerView rvPets;
    private PetProfileAdapter adapter;
    private List<Pet> petList;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_pet_profile_list, container, false);
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
        
        view.findViewById(R.id.btnAddPet).setOnClickListener(v -> {
            // TODO: Start AddEditPetActivity
            android.content.Intent intent = new android.content.Intent(getContext(), com.sangampradhan.nordicpaws.AddEditPetActivity.class);
            startActivity(intent);
        });

        rvPets = view.findViewById(R.id.rvPets);
        rvPets.setLayoutManager(new androidx.recyclerview.widget.LinearLayoutManager(getContext()));

        // Dummy data based on the old hardcoded cards
        petList = new java.util.ArrayList<>();
        petList.add(new Pet("1", "Milo", "Golden Retriever • Male", "Up to date on vaccines", R.drawable.dog, "Dog", "2 yrs 4 mos", "Microchipped", "31.4 kg", "WELLNESS SCORE", "98% Optimal", R.drawable.baseline_health_and_safety_24));
        petList.add(new Pet("2", "Luna", "British Shorthair • Female", "Vet checkup due in 2 weeks", R.drawable.cat, "Cat", "1 yr 8 mos", "Spayed", "4.2 kg", "NEXT ROUTINE", "Grooming (Oct 18)", R.drawable.ic_event_note));
        petList.add(new Pet("3", "Oliver", "French Bulldog • Male", "Allergy Sensitive Plan", R.drawable.puppy, "Dog", "4 yrs", "Daily Mobility", "12.8 kg", "FEEDING", "Grain-free Duck", R.drawable.baseline_restaurant_24));

        adapter = new PetProfileAdapter(petList, new PetProfileAdapter.OnPetClickListener() {
            @Override
            public void onPetClick(Pet pet) {
                if (getActivity() instanceof MainActivity) {
                    ((MainActivity) getActivity()).loadFragment(new PetProfileDetailFragment(), true);
                }
            }

            @Override
            public void onMoreOptionsClick(Pet pet, View anchorView) {
                // Show a simple popup menu
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
                    // Edit
                    adapter.notifyItemChanged(position); // reset view
                    openEditScreen(pet);
                } else if (direction == androidx.recyclerview.widget.ItemTouchHelper.LEFT) {
                    // Delete
                    adapter.notifyItemChanged(position); // reset view
                    showDeleteConfirmation(pet, position);
                }
            }
        };

        androidx.recyclerview.widget.ItemTouchHelper itemTouchHelper = new androidx.recyclerview.widget.ItemTouchHelper(simpleCallback);
        itemTouchHelper.attachToRecyclerView(rvPets);
    }
    
    private void openEditScreen(Pet pet) {
        android.content.Intent intent = new android.content.Intent(getContext(), com.sangampradhan.nordicpaws.AddEditPetActivity.class);
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
    }
}
