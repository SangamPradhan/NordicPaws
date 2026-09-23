package com.sangampradhan.nordicpaws.fragments;


import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.widget.ViewPager2;


import com.sangampradhan.nordicpaws.R;
import com.sangampradhan.nordicpaws.adapters.BannerPagerAdapter;

import java.util.ArrayList;
import java.util.List;

public class HomeFragment extends Fragment {

    private ViewPager2 bannerViewPager;
    private LinearLayout bannerDotsContainer;
    private BannerPagerAdapter bannerAdapter;

    private final Handler autoScrollHandler = new Handler(Looper.getMainLooper());
    private Runnable autoScrollRunnable;
    private static final long AUTO_SCROLL_INTERVAL_MS = 3000;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        // --- Notification Bell in greeting row ---
        FrameLayout homeBellBtn = view.findViewById(R.id.homeBellBtn);
        if (homeBellBtn != null) {
            homeBellBtn.setOnClickListener(v -> {
                NotificationsBottomSheetFragment sheet = NotificationsBottomSheetFragment.newInstance();
                sheet.show(getParentFragmentManager(), "NotificationsSheet");
            });
        }

        // --- ViewPager2 Banner Carousel ---
        bannerViewPager = view.findViewById(R.id.bannerViewPager);
        bannerDotsContainer = view.findViewById(R.id.bannerDotsContainer);

        if (bannerViewPager != null) {
            List<BannerPagerAdapter.BannerItem> slides = buildBannerSlides();
            bannerAdapter = new BannerPagerAdapter(slides);
            bannerViewPager.setAdapter(bannerAdapter);
            bannerViewPager.setOffscreenPageLimit(1);

            // Build dots
            buildDots(slides.size(), 0);

            // Page change listener — update dots
            bannerViewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
                @Override
                public void onPageSelected(int position) {
                    buildDots(slides.size(), position);
                    // Reset auto-scroll timer on manual swipe
                    autoScrollHandler.removeCallbacks(autoScrollRunnable);
                    autoScrollHandler.postDelayed(autoScrollRunnable, AUTO_SCROLL_INTERVAL_MS);
                }
            });

            // Auto-scroll runnable
            autoScrollRunnable = () -> {
                if (bannerViewPager == null || bannerAdapter == null) return;
                int count = bannerAdapter.getItemCount();
                if (count == 0) return;
                int next = (bannerViewPager.getCurrentItem() + 1) % count;
                bannerViewPager.setCurrentItem(next, true);
                autoScrollHandler.postDelayed(autoScrollRunnable, AUTO_SCROLL_INTERVAL_MS);
            };
        }

        return view;
    }

    private List<BannerPagerAdapter.BannerItem> buildBannerSlides() {
        List<BannerPagerAdapter.BannerItem> slides = new ArrayList<>();

        slides.add(new BannerPagerAdapter.BannerItem(
                "✨ DAILY CHECK-IN",
                "How is your day with your pets?",
                "Log activities, quick vitals, or mood in one tap.",
                "🐾 Joyful & Active",
                "💤 Resting"
        ));

        slides.add(new BannerPagerAdapter.BannerItem(
                "⏰ UPCOMING TASK • 08:00 AM",
                "Milo: Organic Kibble & Probiotic",
                "1.5 scoops kibble mixed with 1 capsule probiotic.",
                "Mark Completed ✓",
                ""
        ));

        slides.add(new BannerPagerAdapter.BannerItem(
                "🏥 UPCOMING APPOINTMENT • 02:30 PM",
                "Luna: Annual Wellness Checkup",
                "Evergreen Vet Clinic • Bring health records",
                "View Details →",
                ""
        ));

        return slides;
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
        if (getActivity() instanceof com.sangampradhan.nordicpaws.MainActivity) {
            ((com.sangampradhan.nordicpaws.MainActivity) getActivity()).setTopBarTitle("HOME");
        }
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
