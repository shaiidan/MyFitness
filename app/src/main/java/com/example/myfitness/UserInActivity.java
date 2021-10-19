/* Assignment: 1
Campus: Ashdod
Author: Shimon Shai Idan, ID: 311324602,
Author:Harel jerbi, ID: 204223184
*/
package com.example.myfitness;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.Objects;

public class UserInActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // check authentication
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser(); // get user
        if(currentUser == null){
            // go to activity main
            Intent i = new Intent(this, MainActivity.class);
            startActivity(i);
            Objects.requireNonNull(this).finish(); // finish the activity and go to main activity
        }
        setContentView(R.layout.activity_user_in);
        BottomNavigationView btnMenu = findViewById(R.id.user_menu);
        btnMenu.setOnNavigationItemSelectedListener(navListener);
        btnMenu.setSelectedItemId(R.id.homepage);
    }

    // for menu navigation
    private BottomNavigationView.OnNavigationItemSelectedListener
            navListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {
        @SuppressLint("NonConstantResourceId")
        @Override
        public boolean onNavigationItemSelected(
                @NonNull MenuItem item)
        {
            Fragment selectedFragment = null;
            switch (item.getItemId()) {
                case R.id.homepage:
                    selectedFragment = new UserHomeFragment();// go to user home page
                    break;
                case R.id.fitness:
                    selectedFragment = new FitFragment(); // got to fit page
                    break;

                case R.id.profile:
                    selectedFragment = new UserProfileFragment(); // go to profile page
                    break;
            }

            // navigation to right fragment
            if(selectedFragment != null){
                getSupportFragmentManager()
                        .beginTransaction()
                        .replace(
                                R.id.fragment_container_with_menu,
                                selectedFragment)
                        .commit();
            }
            return true;
        }
    };
}