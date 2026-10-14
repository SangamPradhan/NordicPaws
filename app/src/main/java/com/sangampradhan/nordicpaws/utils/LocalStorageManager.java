package com.sangampradhan.nordicpaws.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;

public class LocalStorageManager {

    /**
     * Saves a user profile picture inside internal app storage under user's UID folder.
     * Path: /data/user/0/.../files/users/{userId}/profile/profile.jpg
     */
    public static String saveUserProfileImage(Context context, String userId, Uri imageUri) {
        File userDir = new File(context.getFilesDir(), "users/" + userId + "/profile");
        if (!userDir.exists()) {
            userDir.mkdirs();
        }

        File destFile = new File(userDir, "user_profile.jpg");
        return copyUriToFile(context, imageUri, destFile);
    }

    /**
     * Saves a pet image inside internal app storage under user's UID folder & petId folder.
     * Path: /data/user/0/.../files/users/{userId}/pets/{petId}/pet_avatar.jpg
     */
    public static String savePetImage(Context context, String userId, String petId, Uri imageUri) {
        File petDir = new File(context.getFilesDir(), "users/" + userId + "/pets/" + petId);
        if (!petDir.exists()) {
            petDir.mkdirs();
        }

        File destFile = new File(petDir, "pet_avatar.jpg");
        return copyUriToFile(context, imageUri, destFile);
    }

    /**
     * Helper to copy input stream from Uri to target File.
     */
    private static String copyUriToFile(Context context, Uri imageUri, File destFile) {
        try (InputStream inputStream = context.getContentResolver().openInputStream(imageUri);
             FileOutputStream outputStream = new FileOutputStream(destFile)) {

            byte[] buffer = new byte[4096];
            int bytesRead;
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
            }
            outputStream.flush();
            return destFile.getAbsolutePath();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Helper to load bitmap from local image path.
     */
    public static Bitmap loadLocalBitmap(String path) {
        if (path == null || path.isEmpty()) return null;
        File file = new File(path);
        if (file.exists()) {
            return BitmapFactory.decodeFile(file.getAbsolutePath());
        }
        return null;
    }
}
