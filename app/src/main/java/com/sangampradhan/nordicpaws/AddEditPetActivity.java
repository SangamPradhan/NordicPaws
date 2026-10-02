package com.sangampradhan.nordicpaws;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.sangampradhan.nordicpaws.models.Pet;
import com.sangampradhan.nordicpaws.utils.FirestoreManager;
import com.sangampradhan.nordicpaws.utils.LocalStorageManager;

import java.util.UUID;

public class AddEditPetActivity extends AppCompatActivity {

    private CardView cardPhoto;
    private ImageView ivPetPhoto;
    private EditText etPetName, etPetBreed, etPetAge, etPetWeight, etPetDiet, etPetAllergies, etPetNotes;
    private MaterialButton btnSavePet;

    private Uri selectedImageUri = null;
    private FirestoreManager firestoreManager;
    private android.hardware.SensorManager sensorManager;
    private com.sangampradhan.nordicpaws.utils.ShakeDetector shakeDetector;

    private final ActivityResultLauncher<Intent> imagePickerLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null && result.getData().getData() != null) {
                    selectedImageUri = result.getData().getData();
                    ivPetPhoto.setImageURI(selectedImageUri);
                    ivPetPhoto.setVisibility(View.VISIBLE);
                    Toast.makeText(this, "Pet photo selected! 🐾", Toast.LENGTH_SHORT).show();
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_edit_pet);

        firestoreManager = new FirestoreManager();

        // Initialize Shake Sensor
        sensorManager = (android.hardware.SensorManager) getSystemService(SENSOR_SERVICE);
        shakeDetector = new com.sangampradhan.nordicpaws.utils.ShakeDetector();
        shakeDetector.setOnShakeListener(this::resetFormFields);

        ImageButton btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> finish());

        cardPhoto = findViewById(R.id.cardPhoto);
        ivPetPhoto = findViewById(R.id.ivPetPhoto);

        etPetName = findViewById(R.id.etPetName);
        etPetBreed = findViewById(R.id.etPetBreed);
        etPetAge = findViewById(R.id.etPetAge);
        etPetWeight = findViewById(R.id.etPetWeight);
        etPetDiet = findViewById(R.id.etPetDiet);
        etPetAllergies = findViewById(R.id.etPetAllergies);
        etPetNotes = findViewById(R.id.etPetNotes);
        btnSavePet = findViewById(R.id.btnSavePet);

        // Open Gallery Image Picker
        cardPhoto.setOnClickListener(v -> openGalleryPicker());

        btnSavePet.setOnClickListener(v -> savePetProfile());
    }

    private void openGalleryPicker() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        imagePickerLauncher.launch(intent);
    }

    private void savePetProfile() {
        String name = etPetName.getText().toString().trim();
        String breed = etPetBreed.getText().toString().trim();
        String age = etPetAge.getText().toString().trim();
        String weight = etPetWeight.getText().toString().trim();
        String diet = etPetDiet.getText().toString().trim();
        String allergies = etPetAllergies.getText().toString().trim();
        String notes = etPetNotes.getText().toString().trim();

        // 1. Mandatory Name & Breed validation
        if (TextUtils.isEmpty(name)) {
            etPetName.setError("Pet name is required");
            etPetName.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(breed)) {
            etPetBreed.setError("Breed / Species is required");
            etPetBreed.requestFocus();
            return;
        }

        // 2. Data Sanitization: Age validation (must contain numbers)
        if (!TextUtils.isEmpty(age) && !age.matches(".*\\d+.*")) {
            etPetAge.setError("Age must contain a numeric value (e.g. 2 or 2 yrs)");
            etPetAge.requestFocus();
            return;
        }

        // 3. Data Sanitization: Weight validation (must contain numbers)
        if (!TextUtils.isEmpty(weight) && !weight.matches(".*\\d+.*")) {
            etPetWeight.setError("Weight must contain a numeric value (e.g. 12 or 12.5 kg)");
            etPetWeight.requestFocus();
            return;
        }

        // 4. Word count limit for Notes (max 150 words)
        int wordCount = getWordCount(notes);
        if (wordCount > 150) {
            etPetNotes.setError("Special notes must be within 150 words (currently " + wordCount + " words)");
            etPetNotes.requestFocus();
            return;
        }

        btnSavePet.setEnabled(false);
        btnSavePet.setText("Saving Pet Profile...");

        String userId = FirebaseAuth.getInstance().getCurrentUser() != null ?
                FirebaseAuth.getInstance().getCurrentUser().getUid() : "guest";

        String petId = "pet_" + UUID.randomUUID().toString().substring(0, 8);
        String localImagePath = null;

        // Save selected image to device local app storage if provided
        if (selectedImageUri != null) {
            localImagePath = LocalStorageManager.savePetImage(this, userId, petId, selectedImageUri);
        }

        Pet pet = new Pet();
        pet.setId(petId);
        pet.setName(name);
        pet.setBreedAndGender(breed);
        pet.setStatusBadge("Active Wellness Plan");
        pet.setTagType(breed.contains("Cat") ? "Cat" : "Dog");
        pet.setTagAge(age.isEmpty() ? "Unknown age" : age);
        pet.setTagOther(allergies.isEmpty() ? "No known allergies" : allergies);
        pet.setStat1Val(weight.isEmpty() ? "N/A" : weight);
        pet.setStat2Label(diet.isEmpty() ? "NUTRITION" : "DIET");
        pet.setStat2Val(diet.isEmpty() ? "Balanced Diet" : diet);
        pet.setAvatarResId(R.drawable.dog); // Default fallback icon
        pet.setLocalImagePath(localImagePath);

        // Save pet to Cloud Firestore
        firestoreManager.savePet(pet,
                aVoid -> {
                    Toast.makeText(AddEditPetActivity.this, name + "'s profile saved successfully! 🐾", Toast.LENGTH_LONG).show();
                    finish();
                },
                e -> {
                    btnSavePet.setEnabled(true);
                    btnSavePet.setText("Save Pet Profile");
                    Toast.makeText(AddEditPetActivity.this, "Failed to save pet: " + e.getLocalizedMessage(), Toast.LENGTH_LONG).show();
                });
    }

    private void resetFormFields() {
        if (etPetName != null) etPetName.setText("");
        if (etPetBreed != null) etPetBreed.setText("");
        if (etPetAge != null) etPetAge.setText("");
        if (etPetWeight != null) etPetWeight.setText("");
        if (etPetDiet != null) etPetDiet.setText("");
        if (etPetAllergies != null) etPetAllergies.setText("");
        if (etPetNotes != null) etPetNotes.setText("");
        if (ivPetPhoto != null) {
            ivPetPhoto.setImageDrawable(null);
            ivPetPhoto.setVisibility(View.GONE);
        }
        selectedImageUri = null;
        Toast.makeText(this, "Form reset by shake gesture! 📳", Toast.LENGTH_SHORT).show();
    }

    private int getWordCount(String text) {
        if (TextUtils.isEmpty(text)) return 0;
        String[] words = text.trim().split("\\s+");
        return words.length;
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (sensorManager != null && shakeDetector != null) {
            android.hardware.Sensor accelerometer = sensorManager.getDefaultSensor(android.hardware.Sensor.TYPE_ACCELEROMETER);
            if (accelerometer != null) {
                sensorManager.registerListener(shakeDetector, accelerometer, android.hardware.SensorManager.SENSOR_DELAY_UI);
            }
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (sensorManager != null && shakeDetector != null) {
            sensorManager.unregisterListener(shakeDetector);
        }
    }
}
