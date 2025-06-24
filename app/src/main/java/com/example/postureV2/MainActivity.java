package com.example.postureV2;

import android.os.Bundle;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;

import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.NavigationUI;

//import com.example.postureV2.fragment.ResultsFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import android.content.Intent;

public class MainActivity extends AppCompatActivity {
    private MainViewModel viewModel;
    private TextView resultText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_posture_detect);

        // Initialize ViewModel
        viewModel = new ViewModelProvider(this).get(MainViewModel.class);

        //Added by FitHit Developers
        viewModel.setExercise(this, "Jumping Jacks");

        //button listener
        findViewById(R.id.btnComplete).setOnClickListener(v -> {
            viewModel.completeExercise();
        });
        //observe exercise report
//        viewModel.getExerciseReport().observe(this, report -> {
//            if (report != null && !report.isEmpty()){
//                showResultsFragment(report);
//            }
//        });

        viewModel.getExerciseReport().observe(this, report ->{
            if (report != null && !report.isEmpty()){
                //Start ResultsActivity with the report
                Intent intent = new Intent(MainActivity.this, ResultsActivity.class);
                intent.putExtra("REPORT", report);
                startActivity(intent);
            }
        });

        resultText = findViewById(R.id.poseResultText);
//        viewModel.getAngleRead().observe(this, newText -> {
//            //resultText.setText(newText);
//        });

        //posture correction section
//        viewModel.getPostureFeedback().observe(this, feedback -> {
//            resultText.setText(feedback);
//        });
//
//        viewModel.getExerciseReport().observe(this, report->{
//            resultText.setText(report);
//        });

        // Get the NavHostFragment and NavController
        NavHostFragment navHostFragment = (NavHostFragment)
                getSupportFragmentManager().findFragmentById(R.id.fragment_container);

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