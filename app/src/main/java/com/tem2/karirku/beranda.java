package com.tem2.karirku;

import android.os.Bundle;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment; // IMPORT INI

import com.google.android.material.bottomnavigation.BottomNavigationView;

public class beranda extends AppCompatActivity {

    private BottomNavigationView bottomNavigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_beranda);

        bottomNavigationView = findViewById(R.id.bottomNavigationView);

        // LOAD DEFAULT FRAGMENT SAAT PERTAMA KALI
        if (savedInstanceState == null) {
            loadFragment(new scancvFragment());
        }

        bottomNavigationView.setSelectedItemId(R.id.home);
        setupBottomNavigation();

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.fragment_container), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    private void setupBottomNavigation() {
        bottomNavigationView.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();

            if (itemId == R.id.home) {
                loadFragment(new homeFragment());
                return true;
            } else if (itemId == R.id.scan) {
                loadFragment(new scancvFragment());
                return true;
            } else if (itemId == R.id.Chat) {
                loadFragment(new chatFragment());
                return true;
            } else if (itemId == R.id.Profil) {
                loadFragment(new profilFragment());
                return true;
            }
            return false;
        });
    }

    // TAMBAHKAN METHOD INI - YANG KAMU LUPA
    private void loadFragment(Fragment fragment) {
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .commit();
    }

    @Override
    protected void onResume() {
        super.onResume();
        bottomNavigationView.setSelectedItemId(R.id.home);
    }
}