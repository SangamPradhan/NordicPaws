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
import com.sangampradhan.nordicpaws.utils.DummyData;
import com.sangampradhan.nordicpaws.fragments.HomeFragment;
import com.sangampradhan.nordicpaws.fragments.PetProfileListFragment;
import com.sangampradhan.nordicpaws.fragments.PlacesFragment;
import com.sangampradhan.nordicpaws.fragments.ProfileFragment;
import com.sangampradhan.nordicpaws.fragments.RoutineFragment;

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



        // Tapping the top-bar profile avatar opens AccountDetailsActivity
        if (profileImage != null) {
            profileImage.setOnClickListener(v -> {
                Intent accountIntent = new Intent(this, AccountDetailsActivity.class);
                startActivity(accountIntent);
                overridePendingTransition(R.anim.slide_up, 0);
            });
        }

        navPaw.setOnClickListener(v -> {
            setTopBarTitle("Pet Profile");
            loadFragment(new PetProfileListFragment(), true);
        });

        // Load default tab
        selectTab(0);
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
        PopupMenu popup = new PopupMenu(this, profileImage);
        List<Pet> pets = DummyData.getPets();
        for (int i = 0; i < pets.size(); i++) {
            popup.getMenu().add(0, i, 0, pets.get(i).getName());
        }
        popup.setOnMenuItemClickListener(item -> {
            Pet selectedPet = pets.get(item.getItemId());
            selectedPetId = selectedPet.getId();
            profileImage.setImageResource(selectedPet.getAvatarResId());
            for (PetSelectionListener listener : petSelectionListeners) {
                listener.onPetSelected(selectedPetId);
            }
            return true;
        });
        popup.show();
    }
}