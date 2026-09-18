package com.sangampradhan.nordicpaws;

import android.os.Bundle;
import android.widget.ImageButton;

import androidx.appcompat.app.AppCompatActivity;

public class NotificationsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notifications);

        ImageButton btnClose = findViewById(R.id.btnCloseNotifications);
        if (btnClose != null) {
            btnClose.setOnClickListener(v -> finish());
        }

        // Tapping the scrim backdrop (FrameLayout root) will dismiss this activity
    }

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(0, android.R.anim.fade_out);
    }
}
