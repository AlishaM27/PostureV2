package com.example.postureV2;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;
/*import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;*/

import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.NavigationUI;

import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MainActivity extends AppCompatActivity {
    private MainViewModel viewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_posture_detect);

        // Initialize ViewModel
        viewModel = new ViewModelProvider(this).get(MainViewModel.class);

        // Get the NavHostFragment and NavController
        NavHostFragment navHostFragment = (NavHostFragment)
                getSupportFragmentManager().findFragmentById(R.id.fragment_container);
        /*NavController navController = navHostFragment.getNavController();

        // Set up bottom navigation
        BottomNavigationView bottomNavigationView = findViewById(R.id.navigation);
        NavigationUI.setupWithNavController(bottomNavigationView, navController);

        // Ignore reselection
        bottomNavigationView.setOnNavigationItemReselectedListener(item -> {
            // No action taken on reselection
        });

        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                finish(); // Perform your custom back press action
            }
        });

    }
}*/
        if (navHostFragment != null) {
            NavController navController = navHostFragment.getNavController();

            BottomNavigationView bottomNavigationView = findViewById(R.id.navigation);
            NavigationUI.setupWithNavController(bottomNavigationView, navController);

            bottomNavigationView.setOnNavigationItemReselectedListener(item -> {
                // No action
            });

            getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
                @Override
                public void handleOnBackPressed() {
                    finish();
                }
            });
        } else {
            // Handle error: Log or show a message
            throw new IllegalStateException("NavHostFragment not found in layout");
        }
    }
}