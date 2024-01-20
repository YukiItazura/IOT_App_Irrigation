package com.example.dam;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.drawerlayout.widget.DrawerLayout;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Toast;

import com.google.android.material.navigation.NavigationView;

public class MainActivity extends AppCompatActivity {

    public DrawerLayout drawerLayout;
    public ActionBarDrawerToggle actionBarDrawerToggle;

    private NavigationView navigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        drawerLayout = findViewById(R.id.main_activity);
        actionBarDrawerToggle = new ActionBarDrawerToggle(this, drawerLayout, R.string.nav_open, R.string.nav_close);
        drawerLayout.addDrawerListener(actionBarDrawerToggle);
        actionBarDrawerToggle.syncState();

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        navigationView = findViewById(R.id.navigation_view);
        setupDrawerContent(navigationView);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        if (actionBarDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void setupDrawerContent(NavigationView navigationView) {
        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                int id = menuItem.getItemId();
                switch (id) {
                    case R.id.dashboard:
                        // Handle the "Dashboard" item click (if needed)
                        Toast.makeText(MainActivity.this, "Dashboard clicked", Toast.LENGTH_SHORT).show();
                        break;
                    case R.id.my_account:
                        // Handle the "My Account" item click (if needed)
                        Toast.makeText(MainActivity.this, "My Account clicked", Toast.LENGTH_SHORT).show();
                        break;
                    case R.id.add_contacts:
                        // Handle the "Add Contacts" item click by starting the AddContactsActivity
                        Toast.makeText(MainActivity.this, "Add Contacts clicked", Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(MainActivity.this, AddContacts.class));
                        break;
                    case R.id.send_message:
                        // Handle the "Add Contacts" item click by starting the AddContactsActivity
                        Toast.makeText(MainActivity.this, "Send Message clicked", Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(MainActivity.this, SendMessage.class));
                        break;
                    case R.id.logout:
                        // Handle the "Logout" item click (if needed)
                        Toast.makeText(MainActivity.this, "Logout clicked", Toast.LENGTH_SHORT).show();
                        break;
                }

                // Close the navigation drawer
                drawerLayout.closeDrawers();
                return true;
            }
        });
    }
}