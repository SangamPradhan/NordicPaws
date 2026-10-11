package com.sangampradhan.nordicpaws.fragments;


import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.core.content.res.ResourcesCompat;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.widget.ViewPager2;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.sangampradhan.nordicpaws.AddEditPetActivity;
import com.sangampradhan.nordicpaws.MainActivity;
import com.sangampradhan.nordicpaws.R;
import com.sangampradhan.nordicpaws.adapters.BannerPagerAdapter;
import com.sangampradhan.nordicpaws.models.Pet;
import com.sangampradhan.nordicpaws.models.RoutineTask;
import com.sangampradhan.nordicpaws.utils.FirestoreManager;
import com.sangampradhan.nordicpaws.utils.LocalStorageManager;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class HomeFragment extends Fragment {

    private ViewPager2 bannerViewPager;
    private LinearLayout bannerDotsContainer;
    private BannerPagerAdapter bannerAdapter;

    private final Handler autoScrollHandler = new Handler(Looper.getMainLooper());
    private Runnable autoScrollRunnable;
    private static final long AUTO_SCROLL_INTERVAL_MS = 3000;

    private FirestoreManager firestoreManager;
    private LinearLayout llFurryFamilyContainer;

    private TextView tvGreetingTime;
    private TextView tvGreetingName;
    private ImageView ivGreetingImage;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        firestoreManager = new FirestoreManager();

        // --- Greetings Section ---
        tvGreetingTime = view.findViewById(R.id.tvGreetingTime);
        tvGreetingName = view.findViewById(R.id.tvGreetingName);
        ivGreetingImage = view.findViewById(R.id.ivGreetingImage);
        updateGreeting();

        // --- Notification Bell in greeting row ---
        FrameLayout homeBellBtn = view.findViewById(R.id.homeBellBtn);
        if (homeBellBtn != null) {
            homeBellBtn.setOnClickListener(v -> {
                NotificationsBottomSheetFragment sheet = NotificationsBottomSheetFragment.newInstance();
                sheet.show(getParentFragmentManager(), "NotificationsSheet");
            });
        }

        // --- Manage Pets ---
        TextView tvManagePets = view.findViewById(R.id.tvManagePets);
        if (tvManagePets != null) {
            tvManagePets.setOnClickListener(v -> {
                if (getActivity() instanceof MainActivity) {
                    ((MainActivity) getActivity()).loadFragment(new PetProfileListFragment(), true);
                }
            });
        }

        // --- Furry Family Section ---
        llFurryFamilyContainer = view.findViewById(R.id.llFurryFamilyContainer);

        // --- ViewPager2 Banner Carousel ---
        bannerViewPager = view.findViewById(R.id.bannerViewPager);
        bannerDotsContainer = view.findViewById(R.id.bannerDotsContainer);

        // Load pets and banner data dynamically
        loadDynamicData();

        return view;
    }

    private void updateGreeting() {
        if (tvGreetingTime == null || ivGreetingImage == null || tvGreetingName == null) return;

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        String name = user != null && user.getDisplayName() != null ? user.getDisplayName() : "Friend";
        tvGreetingName.setText("Hello " + name.split(" ")[0] + " \uD83D\uDC4B");

        Calendar calendar = Calendar.getInstance();
        int hourOfDay = calendar.get(Calendar.HOUR_OF_DAY);

        if (hourOfDay >= 5 && hourOfDay < 12) {
            tvGreetingTime.setText("Good morning!");
            ivGreetingImage.setImageResource(R.drawable.dog_morning);
        } else if (hourOfDay >= 12 && hourOfDay < 17) {
            tvGreetingTime.setText("Good afternoon!");
            ivGreetingImage.setImageResource(R.drawable.dog_afternoon);
        } else if (hourOfDay >= 17 && hourOfDay < 21) {
            tvGreetingTime.setText("Good evening!");
            ivGreetingImage.setImageResource(R.drawable.dog_evening);
        } else {
            tvGreetingTime.setText("Good night!");
            ivGreetingImage.setImageResource(R.drawable.dog_night);
        }
    }

    private void loadDynamicData() {
        firestoreManager.fetchPets(pets -> {
            populateFurryFamily(pets);
            
            // Build banners after pets are loaded
            List<BannerPagerAdapter.BannerItem> slides = new ArrayList<>();
            // Always have the daily check-in slide
            slides.add(new BannerPagerAdapter.BannerItem(
                    "✨ DAILY CHECK-IN",
                    "How is your day with your pets?",
                    "Log activities, quick vitals, or mood in one tap.",
                    "🐾 Joyful & Active",
                    "💤 Resting"
            ));

            if (pets.isEmpty()) {
                setupBanner(slides);
                return;
            }

            // Fetch tasks for all pets
            final int[] petsLoaded = {0};
            final List<RoutineTask> allTasks = new ArrayList<>();

            for (Pet pet : pets) {
                firestoreManager.fetchTasksForPet(pet.getId(), tasks -> {
                    allTasks.addAll(tasks);
                    petsLoaded[0]++;
                    if (petsLoaded[0] == pets.size()) {
                        processDynamicBanners(slides, allTasks, pets);
                    }
                }, e -> {
                    petsLoaded[0]++;
                    if (petsLoaded[0] == pets.size()) {
                        processDynamicBanners(slides, allTasks, pets);
                    }
                });
            }

        }, null);
    }

    private void processDynamicBanners(List<BannerPagerAdapter.BannerItem> slides, List<RoutineTask> tasks, List<Pet> pets) {
        // Find a few upcoming tasks (for today ideally, but we'll take top 2 tasks)
        // This is a simple representation taking up to 2 tasks for banner
        int count = 0;
        for (RoutineTask task : tasks) {
            if (count >= 2) break; // limit to 2 dynamic task banners
            String petName = getPetName(task.getPetId(), pets);
            String title = task.getScheduleType() != null && task.getScheduleType().equals("SPECIFIC_DATE") ? 
                    "🗓️ SCHEDULED EVENT" : "⏰ UPCOMING TASK • " + task.getTime();
                    
            slides.add(new BannerPagerAdapter.BannerItem(
                    title,
                    petName + ": " + task.getTitle(),
                    task.getCategory() + " routine",
                    "View Details →",
                    ""
            ));
            count++;
        }
        setupBanner(slides);
    }

    private String getPetName(String petId, List<Pet> pets) {
        if (petId == null) return "Your Pet";
        for (Pet p : pets) {
            if (petId.equals(p.getId())) return p.getName();
        }
        return "Your Pet";
    }

    private void setupBanner(List<BannerPagerAdapter.BannerItem> slides) {
        if (bannerViewPager == null) return;
        
        bannerAdapter = new BannerPagerAdapter(slides);
        bannerViewPager.setAdapter(bannerAdapter);
        bannerViewPager.setOffscreenPageLimit(1);

        buildDots(slides.size(), 0);

        bannerViewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                buildDots(slides.size(), position);
                autoScrollHandler.removeCallbacks(autoScrollRunnable);
                autoScrollHandler.postDelayed(autoScrollRunnable, AUTO_SCROLL_INTERVAL_MS);
            }
        });

        autoScrollRunnable = () -> {
            if (bannerViewPager == null || bannerAdapter == null) return;
            int count = bannerAdapter.getItemCount();
            if (count == 0) return;
            int next = (bannerViewPager.getCurrentItem() + 1) % count;
            bannerViewPager.setCurrentItem(next, true);
            autoScrollHandler.postDelayed(autoScrollRunnable, AUTO_SCROLL_INTERVAL_MS);
        };
        
        autoScrollHandler.postDelayed(autoScrollRunnable, AUTO_SCROLL_INTERVAL_MS);
    }

    private void populateFurryFamily(List<Pet> pets) {
        if (llFurryFamilyContainer == null || getContext() == null) return;
        llFurryFamilyContainer.removeAllViews();

        // 1. Add "Add Pet" Button
        View addPetView = createFurryFamilyItem("Add Pet", null, R.drawable.ic_add, true);
        addPetView.setOnClickListener(v -> {
            startActivity(new Intent(getContext(), AddEditPetActivity.class));
        });
        llFurryFamilyContainer.addView(addPetView);

        // 2. Add dynamic pets
        for (Pet pet : pets) {
            View petView = createFurryFamilyItem(pet.getName(), pet.getLocalImagePath(), pet.getAvatarResId(), false);
            petView.setOnClickListener(v -> {
                // Clicking pet from home could potentially navigate to PetProfileDetailFragment
                if (getActivity() instanceof MainActivity) {
                    PetProfileDetailFragment detailFragment = PetProfileDetailFragment.newInstance(pet);
                    ((MainActivity) getActivity()).loadFragment(detailFragment, true);
                }
            });
            llFurryFamilyContainer.addView(petView);
        }
    }

    private View createFurryFamilyItem(String title, String imagePath, int defaultResId, boolean isAddButton) {
        float density = getResources().getDisplayMetrics().density;
        
        LinearLayout layout = new LinearLayout(getContext());
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setGravity(Gravity.CENTER);
        
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        params.setMarginEnd((int) (16 * density));
        layout.setLayoutParams(params);

        if (isAddButton) {
            FrameLayout frame = new FrameLayout(getContext());
            LinearLayout.LayoutParams frameParams = new LinearLayout.LayoutParams((int) (64 * density), (int) (64 * density));
            frameParams.bottomMargin = (int) (6 * density);
            frame.setLayoutParams(frameParams);
            frame.setBackgroundResource(R.drawable.bg_bottom_nav);
            frame.setBackgroundTintList(android.content.res.ColorStateList.valueOf(Color.parseColor("#E2E8F0")));

            ImageView img = new ImageView(getContext());
            FrameLayout.LayoutParams imgParams = new FrameLayout.LayoutParams((int) (32 * density), (int) (32 * density));
            imgParams.gravity = Gravity.CENTER;
            img.setLayoutParams(imgParams);
            img.setImageResource(defaultResId);
            img.setColorFilter(Color.parseColor("#475569"));
            
            frame.addView(img);
            layout.addView(frame);
        } else {
            CardView card = new CardView(getContext());
            LinearLayout.LayoutParams cardParams = new LinearLayout.LayoutParams((int) (64 * density), (int) (64 * density));
            cardParams.bottomMargin = (int) (6 * density);
            card.setLayoutParams(cardParams);
            card.setRadius(32 * density);
            card.setCardElevation(2 * density);
            card.setUseCompatPadding(false);

            ImageView img = new ImageView(getContext());
            img.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
            img.setScaleType(ImageView.ScaleType.CENTER_CROP);
            img.setBackgroundColor(Color.parseColor("#D1D5DB"));

            if (imagePath != null && !imagePath.isEmpty()) {
                Bitmap bmp = LocalStorageManager.loadLocalBitmap(imagePath);
                if (bmp != null) {
                    img.setImageBitmap(bmp);
                } else {
                    img.setImageResource(defaultResId != 0 ? defaultResId : R.drawable.dog);
                }
            } else {
                img.setImageResource(defaultResId != 0 ? defaultResId : R.drawable.dog);
            }
            
            card.addView(img);
            layout.addView(card);
        }

        TextView tv = new TextView(getContext());
        tv.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT));
        tv.setText(title);
        tv.setTextColor(isAddButton ? Color.parseColor("#475569") : Color.parseColor("#121316"));
        tv.setTextSize(TypedValue.COMPLEX_UNIT_SP, 13);
        try {
            tv.setTypeface(ResourcesCompat.getFont(getContext(), R.font.plusjakartasansbold));
        } catch (Exception ignored) {}

        layout.addView(tv);

        return layout;
    }

    private void buildDots(int count, int activeIndex) {
        if (bannerDotsContainer == null || getContext() == null) return;
        bannerDotsContainer.removeAllViews();

        int activeDotDp = 20;  // active: wider pill
        int inactiveDotDp = 8;
        int dotHeightDp = 8;
        float density = getResources().getDisplayMetrics().density;

        for (int i = 0; i < count; i++) {
            View dot = new View(getContext());
            LinearLayout.LayoutParams params;
            if (i == activeIndex) {
                params = new LinearLayout.LayoutParams(
                        (int) (activeDotDp * density),
                        (int) (dotHeightDp * density));
            } else {
                params = new LinearLayout.LayoutParams(
                        (int) (inactiveDotDp * density),
                        (int) (dotHeightDp * density));
                dot.setAlpha(0.45f);
            }
            params.setMarginEnd((int) (4 * density));
            dot.setLayoutParams(params);
            dot.setBackgroundResource(R.drawable.bg_dot_white);
            bannerDotsContainer.addView(dot);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (getActivity() instanceof MainActivity) {
            ((MainActivity) getActivity()).setTopBarTitle("HOME");
        }
        
        updateGreeting();
        loadDynamicData();

        // Start auto-scroll
        if (autoScrollRunnable != null) {
            autoScrollHandler.postDelayed(autoScrollRunnable, AUTO_SCROLL_INTERVAL_MS);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        // Pause auto-scroll
        autoScrollHandler.removeCallbacks(autoScrollRunnable);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        autoScrollHandler.removeCallbacks(autoScrollRunnable);
        bannerViewPager = null;
        bannerDotsContainer = null;
    }
}
