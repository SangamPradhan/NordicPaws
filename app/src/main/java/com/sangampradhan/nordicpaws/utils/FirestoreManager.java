package com.sangampradhan.nordicpaws.utils;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.sangampradhan.nordicpaws.R;
import com.sangampradhan.nordicpaws.models.Pet;
import com.sangampradhan.nordicpaws.models.RoutineTask;
import com.sangampradhan.nordicpaws.models.User;

import java.util.ArrayList;
import java.util.List;

public class FirestoreManager {

    private static final String USERS_COLLECTION = "users";
    private static final String PETS_COLLECTION = "pets";
    private static final String ROUTINES_COLLECTION = "routines";

    private final FirebaseFirestore db;
    private final FirebaseAuth auth;

    public FirestoreManager() {
        this.db = FirebaseFirestore.getInstance();
        this.auth = FirebaseAuth.getInstance();
    }

    public String getCurrentUserId() {
        FirebaseUser user = auth.getCurrentUser();
        return user != null ? user.getUid() : null;
    }

    /**
     * Returns collection reference for current user's pets: /users/{userId}/pets
     */
    private CollectionReference getPetsRef() {
        String userId = getCurrentUserId();
        if (userId == null) return null;
        return db.collection(USERS_COLLECTION).document(userId).collection(PETS_COLLECTION);
    }

    /**
     * Save or update a Pet profile in Firestore
     */
    public void savePet(Pet pet, OnSuccessListener<Void> successListener, OnFailureListener failureListener) {
        CollectionReference petsRef = getPetsRef();
        if (petsRef == null) return;

        // Default drawable avatar if local image path is not set
        if (pet.getAvatarResId() == 0 && (pet.getLocalImagePath() == null || pet.getLocalImagePath().isEmpty())) {
            pet.setAvatarResId(R.drawable.dog);
        }

        DocumentReference docRef;
        if (pet.getId() == null || pet.getId().isEmpty()) {
            docRef = petsRef.document();
            pet.setId(docRef.getId());
        } else {
            docRef = petsRef.document(pet.getId());
        }

        docRef.set(pet)
                .addOnSuccessListener(aVoid -> {
                    if (successListener != null) successListener.onSuccess(aVoid);
                })
                .addOnFailureListener(e -> {
                    if (failureListener != null) failureListener.onFailure(e);
                });
    }

    /**
     * Fetch all pets for the currently signed-in user
     */
    public void fetchPets(OnSuccessListener<List<Pet>> successListener, OnFailureListener failureListener) {
        CollectionReference petsRef = getPetsRef();
        if (petsRef == null) {
            if (successListener != null) successListener.onSuccess(new ArrayList<>());
            return;
        }

        petsRef.get()
                .addOnSuccessListener(querySnapshot -> {
                    List<Pet> pets = new ArrayList<>();
                    for (QueryDocumentSnapshot doc : querySnapshot) {
                        Pet pet = doc.toObject(Pet.class);
                        pets.add(pet);
                    }
                    if (successListener != null) successListener.onSuccess(pets);
                })
                .addOnFailureListener(e -> {
                    if (failureListener != null) failureListener.onFailure(e);
                });
    }

    /**
     * Save a routine task for a specific pet under /users/{userId}/pets/{petId}/routines
     */
    public void saveRoutineTask(RoutineTask task, OnSuccessListener<Void> successListener, OnFailureListener failureListener) {
        String userId = getCurrentUserId();
        if (userId == null || task.getPetId() == null) return;

        CollectionReference routinesRef = db.collection(USERS_COLLECTION)
                .document(userId)
                .collection(PETS_COLLECTION)
                .document(task.getPetId())
                .collection(ROUTINES_COLLECTION);

        DocumentReference docRef;
        if (task.getId() == null || task.getId().isEmpty()) {
            docRef = routinesRef.document();
            task.setId(docRef.getId());
        } else {
            docRef = routinesRef.document(task.getId());
        }

        docRef.set(task)
                .addOnSuccessListener(aVoid -> {
                    if (successListener != null) successListener.onSuccess(aVoid);
                })
                .addOnFailureListener(e -> {
                    if (failureListener != null) failureListener.onFailure(e);
                });
    }

    /**
     * Fetch all routine tasks for a specific pet
     */
    public void fetchTasksForPet(String petId, OnSuccessListener<List<RoutineTask>> successListener, OnFailureListener failureListener) {
        String userId = getCurrentUserId();
        if (userId == null) {
            if (successListener != null) successListener.onSuccess(new ArrayList<>());
            return;
        }

        CollectionReference routinesRef = db.collection(USERS_COLLECTION)
                .document(userId)
                .collection(PETS_COLLECTION)
                .document(petId)
                .collection(ROUTINES_COLLECTION);

        routinesRef.get()
                .addOnSuccessListener(querySnapshot -> {
                    List<RoutineTask> tasks = new ArrayList<>();
                    for (QueryDocumentSnapshot doc : querySnapshot) {
                        RoutineTask task = doc.toObject(RoutineTask.class);
                        tasks.add(task);
                    }
                    if (successListener != null) successListener.onSuccess(tasks);
                })
                .addOnFailureListener(e -> {
                    if (failureListener != null) failureListener.onFailure(e);
                });
    }

//    Delete a single routine task from Firestore: /users/{userId}/pets/{petId}/routines/{taskId}

    public void deleteTask(String petId, String taskId, OnSuccessListener<Void> successListener, OnFailureListener failureListener) {
        String userId = getCurrentUserId();
        if (userId == null || petId == null || taskId == null) return;

        db.collection(USERS_COLLECTION)
                .document(userId)
                .collection(PETS_COLLECTION)
                .document(petId)
                .collection(ROUTINES_COLLECTION)
                .document(taskId)
                .delete()
                .addOnSuccessListener(aVoid -> {
                    if (successListener != null) successListener.onSuccess(aVoid);
                })
                .addOnFailureListener(e -> {
                    if (failureListener != null) failureListener.onFailure(e);
                });
    }

    /**
     * Delete a pet and ALL of its routine tasks (cascading delete).
     * Firestore doesn't auto-delete subcollections, so we must delete them manually.
     */
    public void deletePetWithTasks(String petId, OnSuccessListener<Void> successListener, OnFailureListener failureListener) {
        String userId = getCurrentUserId();
        if (userId == null || petId == null) return;

        DocumentReference petDocRef = db.collection(USERS_COLLECTION)
                .document(userId)
                .collection(PETS_COLLECTION)
                .document(petId);

        // First, delete all routines under this pet
        petDocRef.collection(ROUTINES_COLLECTION).get()
                .addOnSuccessListener(querySnapshot -> {
                    // Delete each task document
                    for (QueryDocumentSnapshot doc : querySnapshot) {
                        doc.getReference().delete();
                    }
                    // Then delete the pet document itself
                    petDocRef.delete()
                            .addOnSuccessListener(aVoid -> {
                                if (successListener != null) successListener.onSuccess(aVoid);
                            })
                            .addOnFailureListener(e -> {
                                if (failureListener != null) failureListener.onFailure(e);
                            });
                })
                .addOnFailureListener(e -> {
                    if (failureListener != null) failureListener.onFailure(e);
                });
    }

    /**
     * Fetch a single pet by its ID from Firestore
     */
    public void fetchPetById(String petId, OnSuccessListener<Pet> successListener, OnFailureListener failureListener) {
        String userId = getCurrentUserId();
        if (userId == null || petId == null) {
            if (successListener != null) successListener.onSuccess(null);
            return;
        }

        db.collection(USERS_COLLECTION)
                .document(userId)
                .collection(PETS_COLLECTION)
                .document(petId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        Pet pet = documentSnapshot.toObject(Pet.class);
                        if (successListener != null) successListener.onSuccess(pet);
                    } else {
                        if (successListener != null) successListener.onSuccess(null);
                    }
                })
                .addOnFailureListener(e -> {
                    if (failureListener != null) failureListener.onFailure(e);
                });
    }
}

