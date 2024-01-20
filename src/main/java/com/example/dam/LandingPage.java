package com.example.dam;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.drawerlayout.widget.DrawerLayout;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.navigation.NavigationView;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class LandingPage extends AppCompatActivity {

    private DrawerLayout drawerLayout;
    private ActionBarDrawerToggle actionBarDrawerToggle;
    private NavigationView navigationView;
    FirebaseDatabase database;
    DatabaseReference reference;

    ProgressBar waterLevelProgressBar;
    TextView gateStatusTextView;
    Button openButton, closeButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_landing_page);

        drawerLayout = findViewById(R.id.landingPage_activity);
        actionBarDrawerToggle = new ActionBarDrawerToggle(this, drawerLayout, R.string.nav_open, R.string.nav_close);
        drawerLayout.addDrawerListener(actionBarDrawerToggle);
        actionBarDrawerToggle.syncState();

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        navigationView = findViewById(R.id.navigation_view);
        setupDrawerContent(navigationView);

        // Initialize Firebase
        database = FirebaseDatabase.getInstance();
        reference = database.getReference("dam");

        // Initialize UI components
        waterLevelProgressBar = findViewById(R.id.waterlevel);
        gateStatusTextView = findViewById(R.id.gate);
        openButton = findViewById(R.id.open);
        closeButton = findViewById(R.id.close);

        // Set initial values
//        reference.child("waterLevel").setValue(70);  // Initial water level
//        reference.child("gateStatus").setValue("Close");  // Initial gate status

        // Set button click listeners
        openButton.setOnClickListener(v -> {
            // Update gate status to "Open" in Firebase
            reference.child("gateStatus").setValue("Open");
            Toast.makeText(LandingPage.this, "The gate is open.", Toast.LENGTH_SHORT).show();
        });

        closeButton.setOnClickListener(v -> {
            // Update gate status to "Close" in Firebase
            reference.child("gateStatus").setValue("Close");
            Toast.makeText(LandingPage.this, "The gate is closed.", Toast.LENGTH_SHORT).show();
        });
    }

    @Override
    protected void onStart() {
        super.onStart();

        // Attach a listener to update water level
        reference.child("waterLevel").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                // Get the water level from the database
                int waterLevel = dataSnapshot.getValue(Integer.class);

                // Update the progress bar
                waterLevelProgressBar.setProgress(waterLevel);

                // Display the percentage of the progress bar
                waterLevelProgressBar.setIndeterminate(false);
                waterLevelProgressBar.setInterpolator(null);

                // Display the water level percentage
                updateWaterLevelText(waterLevel);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Handle errors
            }
        });

        // Attach a listener to update gate status
        reference.child("gateStatus").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                // Get the gate status from the database
                String gateStatus = dataSnapshot.getValue(String.class);

                // Update the gate status text view
                gateStatusTextView.setText("Gate Status: " + gateStatus);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Handle errors
            }
        });
    }
    private void updateWaterLevelText(int waterLevel) {
        // Display the water level percentage
        TextView waterLevelTextView = findViewById(R.id.textView2);
        waterLevelTextView.setText("Water Level: " + waterLevel + "%");
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
            public boolean onNavigationItemSelected(MenuItem menuItem) {
                int id = menuItem.getItemId();
                switch (id) {
                    case R.id.dashboard:
                        Toast.makeText(LandingPage.this, "Dashboard clicked", Toast.LENGTH_SHORT).show();
                        break;
                    case R.id.my_account:
                        Toast.makeText(LandingPage.this, "My Account clicked", Toast.LENGTH_SHORT).show();
                        String username = getIntent().getStringExtra("USERNAME");

                        // Start the Account activity and pass the username as an extra
                        Intent accountIntent = new Intent(LandingPage.this, Account.class);
                        accountIntent.putExtra("USERNAME", username);
                        startActivity(accountIntent);
                        break;
                    case R.id.add_contacts:
                        Toast.makeText(LandingPage.this, "Add Contacts clicked", Toast.LENGTH_SHORT).show();
//                        startActivity(new Intent(LandingPage.this, AddContacts.class));

                        String ac_username = getIntent().getStringExtra("USERNAME");

                        // Start the Account activity and pass the username as an extra
                        Intent ac_Intent = new Intent(LandingPage.this, AddContacts.class);
                        ac_Intent.putExtra("USERNAME", ac_username);
                        startActivity(ac_Intent);
                        break;

                    case R.id.send_message:
                        Toast.makeText(LandingPage.this, "Send Message clicked", Toast.LENGTH_SHORT).show();
//                        startActivity(new Intent(LandingPage.this, SendMessage.class));

                        String sm_username = getIntent().getStringExtra("USERNAME");

                        // Start the Account activity and pass the username as an extra
                        Intent sm_Intent = new Intent(LandingPage.this, SendMessage.class);
                        sm_Intent.putExtra("USERNAME", sm_username);
                        startActivity(sm_Intent);
                        break;
                    case R.id.logout:
                        Toast.makeText(LandingPage.this, "You have logged out", Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(LandingPage.this, SignupActivity.class));
                        break;
                }

                drawerLayout.closeDrawers();
                return true;
            }
        });
    }
}
