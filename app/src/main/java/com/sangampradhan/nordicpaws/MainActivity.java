package com.sangampradhan.nordicpaws;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.PopupMenu;
import java.util.List;
import java.util.ArrayList;
import com.sangampradhan.nordicpaws.models.Pet;

import com.sangampradhan.nordicpaws.fragments.HomeFragment;
import com.sangampradhan.nordicpaws.fragments.PetProfileListFragment;
import com.sangampradhan.nordicpaws.fragments.PlacesFragment;
import com.sangampradhan.nordicpaws.fragments.ProfileFragment;
import com.sangampradhan.nordicpaws.fragments.RoutineFragment;
import com.sangampradhan.nordicpaws.fragments.AccountDetailsBottomSheetFragment;
import com.sangampradhan.nordicpaws.utils.FirestoreManager;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    private LinearLayout navHome, navPlaces, navRoutine, navProfile;
    private ImageView iconHome, iconPlaces, iconRoutine, iconProfile;
    private TextView textHome, textPlaces, textRoutine, textProfile;
    private FrameLayout navPaw;
    private TextView topBarTitleText;
    private ImageView profileImage;

    private String selectedPetId = "1"; // Default to Milo
    
    public interface PetSelectionListener {
        void onPetSelected(String petId);
    }
    private List<PetSelectionListener> petSelectionListeners = new ArrayList<>();

    public void addPetSelectionListener(PetSelectionListener listener) {
        petSelectionListeners.add(listener);
    }

    public void removePetSelectionListener(PetSelectionListener listener) {
        petSelectionListeners.remove(listener);
    }
    
    public String getSelectedPetId() {
        return selectedPetId;
    }

    private int primaryBlack;
    private int neutralSlate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        primaryBlack = getResources().getColor(R.color.primary_black, getTheme());
        neutralSlate = getResources().getColor(R.color.neutral_slate, getTheme());

        topBarTitleText = findViewById(R.id.topBarTitleText);
        profileImage = findViewById(R.id.profileImage);

        navHome = findViewById(R.id.navHome);
        navPlaces = findViewById(R.id.navPlaces);
        navRoutine = findViewById(R.id.navRoutine);
        navProfile = findViewById(R.id.navProfile);

        iconHome = findViewById(R.id.iconHome);
        iconPlaces = findViewById(R.id.iconPlaces);
        iconRoutine = findViewById(R.id.iconRoutine);
        iconProfile = findViewById(R.id.iconProfile);

        textHome = findViewById(R.id.textHome);
        textPlaces = findViewById(R.id.textPlaces);
        textRoutine = findViewById(R.id.textRoutine);
        textProfile = findViewById(R.id.textProfile);

        navPaw = findViewById(R.id.navPaw);

        navHome.setOnClickListener(v -> selectTab(0));
        navPlaces.setOnClickListener(v -> selectTab(1));
        navRoutine.setOnClickListener(v -> selectTab(2));
        navProfile.setOnClickListener(v -> selectTab(3));



        // Tapping the top-bar profile avatar opens AccountDetailsBottomSheetFragment
        if (profileImage != null) {
            profileImage.setOnClickListener(v -> {
                AccountDetailsBottomSheetFragment sheet = AccountDetailsBottomSheetFragment.newInstance();
                sheet.show(getSupportFragmentManager(), "AccountDetailsSheet");
            });
        }

        navPaw.setOnClickListener(v -> {
            setTopBarTitle("Pet Profile");
            loadFragment(new PetProfileListFragment(), true);
        });

        // Load default tab
        selectTab(0);
        loadUserProfileImage();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadUserProfileImage();
    }

    private void loadUserProfileImage() {
        com.google.firebase.auth.FirebaseUser fUser = com.google.firebase.auth.FirebaseAuth.getInstance().getCurrentUser();
        if (fUser != null && profileImage != null) {
            com.google.firebase.firestore.FirebaseFirestore.getInstance().collection("users").document(fUser.getUid()).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        com.sangampradhan.nordicpaws.models.User user = documentSnapshot.toObject(com.sangampradhan.nordicpaws.models.User.class);
                        if (user != null && user.getLocalProfileImagePath() != null && !user.getLocalProfileImagePath().isEmpty()) {
                            try {
                                profileImage.setImageURI(android.net.Uri.parse(user.getLocalProfileImagePath()));
                            } catch (Exception e) {
                                e.printStackTrace();
                                profileImage.setImageResource(R.drawable.person);
                            }
                        } else {
                            profileImage.setImageResource(R.drawable.person);
                        }
                    } else {
                        profileImage.setImageResource(R.drawable.person);
                    }
                })
                .addOnFailureListener(e -> {
                    profileImage.setImageResource(R.drawable.person);
                });
        }
    }

    private void selectTab(int index) {
        // Reset all
        iconHome.setColorFilter(neutralSlate);
        iconPlaces.setColorFilter(neutralSlate);
        iconRoutine.setColorFilter(neutralSlate);
        iconProfile.setColorFilter(neutralSlate);

        textHome.setTextColor(neutralSlate);
        textPlaces.setTextColor(neutralSlate);
        textRoutine.setTextColor(neutralSlate);
        textProfile.setTextColor(neutralSlate);

        Fragment selectedFragment = null;
        String title = "";

        switch (index) {
            case 0:
                iconHome.setColorFilter(primaryBlack);
                textHome.setTextColor(primaryBlack);
                selectedFragment = new HomeFragment();
                title = "HOME";
                break;
            case 1:
                iconPlaces.setColorFilter(primaryBlack);
                textPlaces.setTextColor(primaryBlack);
                selectedFragment = new PlacesFragment();
                title = "PLACES";
                break;
            case 2:
                iconRoutine.setColorFilter(primaryBlack);
                textRoutine.setTextColor(primaryBlack);
                selectedFragment = new RoutineFragment();
                title = "ROUTINE";
                break;
            case 3:
                iconProfile.setColorFilter(primaryBlack);
                textProfile.setTextColor(primaryBlack);
                selectedFragment = new ProfileFragment();
                title = "PROFILE";
                break;
        }

        setTopBarTitle(title);

        if (selectedFragment != null) {
            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            transaction.setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out);
            transaction.replace(R.id.fragment_container, selectedFragment);
            transaction.commit();
        }
    }

    /**
     * Public method for fragments to load other fragments.
     * @param fragment The fragment to load.
     * @param addToBackStack Whether to add this transaction to the back stack.
     */
    public void loadFragment(Fragment fragment, boolean addToBackStack) {
        Fragment currentFragment = getSupportFragmentManager().findFragmentById(R.id.fragment_container);
        if (currentFragment != null && currentFragment.getClass().equals(fragment.getClass())) {
            // Prevent reloading the same fragment to avoid back stack loop
            return;
        }

        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out);
        transaction.replace(R.id.fragment_container, fragment);
        if (addToBackStack) {
            transaction.addToBackStack(null);
        }
        transaction.commit();
    }

    /**
     * Update the top bar title.
     * @param title The new title to display.
     */
    public void setTopBarTitle(String title) {
        if (topBarTitleText != null) {
            topBarTitleText.setText(title);
        }
    }

    private void showPetSelectionPopup() {
        FirestoreManager fm = new FirestoreManager();
        fm.fetchPets(pets -> {
            if (pets == null || pets.isEmpty()) {
                Toast.makeText(this, "No pets found. Add a pet first.", Toast.LENGTH_SHORT).show();
                return;
            }
            PopupMenu popup = new PopupMenu(this, profileImage);
            for (int i = 0; i < pets.size(); i++) {
                popup.getMenu().add(0, i, 0, pets.get(i).getName());
            }
            popup.setOnMenuItemClickListener(item -> {
                Pet selectedPet = pets.get(item.getItemId());
                selectedPetId = selectedPet.getId();
                if (selectedPet.getLocalImagePath() != null && !selectedPet.getLocalImagePath().isEmpty()) {
                    try {
                        profileImage.setImageURI(android.net.Uri.parse(selectedPet.getLocalImagePath()));
                    } catch (Exception e) {
                        profileImage.setImageResource(R.drawable.dog);
                    }
                } else {
                    profileImage.setImageResource(selectedPet.getAvatarResId() != 0 ? selectedPet.getAvatarResId() : R.drawable.dog);
                }
                for (PetSelectionListener listener : petSelectionListeners) {
                    listener.onPetSelected(selectedPetId);
                }
                return true;
            });
            popup.show();
        }, e -> Toast.makeText(this, "Failed to load pets", Toast.LENGTH_SHORT).show());
    }
}