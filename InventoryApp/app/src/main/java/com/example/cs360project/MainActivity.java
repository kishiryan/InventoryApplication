package com.example.cs360project;

import android.os.Bundle;
import android.provider.ContactsContract;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MainActivity extends AppCompatActivity{

    String username;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        // connect to activity_main.xml
        setContentView(R.layout.activity_main);

        // Get bottom nav via id
        BottomNavigationView bottomNav = findViewById(R.id.bottomNav);

        // if no saved state exists default to
        if (savedInstanceState == null) {
            loadFragment(new DatabaseDisplayFragment());
        }

        // fragment load selection
        bottomNav.setOnItemSelectedListener(menuItem -> {
            Fragment selected = null;

            if (menuItem.getItemId() == R.id.nav_database_display) {
                selected = new DatabaseDisplayFragment();
            } else if (menuItem.getItemId() == R.id.nav_add) {
                selected = new AddFragment();
            } else if (menuItem.getItemId() == R.id.nav_edit) {
                selected = new EditFragment();
            } else if (menuItem.getItemId() == R.id.nav_sms) {
                selected = new SmsFragment();
            } else if (menuItem.getItemId() == R.id.nav_profile) {
                // pass username to profile frag
                ProfileFragment profileFragment = new ProfileFragment();
                Bundle args = new Bundle();
                args.putString("USERNAME", username);
                profileFragment.setArguments(args);
                selected = profileFragment;
            }
            if (selected != null) {
                loadFragment(selected);
                return true;
            }
            return false;
        });
    }

    private void loadFragment(Fragment fragment) {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragmentContainer, fragment)
                .commit();
    }
}
