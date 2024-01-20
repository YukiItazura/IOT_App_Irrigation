package com.example.dam;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.drawerlayout.widget.DrawerLayout;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.navigation.NavigationView;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.lang.ref.Reference;

public class Account extends AppCompatActivity {

    public DrawerLayout drawerLayout;
    public ActionBarDrawerToggle actionBarDrawerToggle;

    private NavigationView navigationView;

    TextView MyName, MyEmail, MyUsername, MyPass;

    FirebaseDatabase database;
    DatabaseReference reference;

    private static final String USERNAME_KEY = "username_key";
    private String username;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account);

        //    navigation
        drawerLayout = findViewById(R.id.myAccount_activity);
        actionBarDrawerToggle = new ActionBarDrawerToggle(this, drawerLayout, R.string.nav_open, R.string.nav_close);
        drawerLayout.addDrawerListener(actionBarDrawerToggle);
        actionBarDrawerToggle.syncState();

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        navigationView = findViewById(R.id.navigation_view);
        setupDrawerContent(navigationView);

        // Initialize Firebase
        database = FirebaseDatabase.getInstance();
        reference = database.getReference("users"); // Update to your database reference

        MyName = findViewById(R.id.My_name);
        MyEmail = findViewById(R.id.My_email);
        MyUsername = findViewById(R.id.My_username);
        MyPass = findViewById(R.id.My_password);

        if (savedInstanceState != null) {
            // Restore the username from the saved state
            username = savedInstanceState.getString(USERNAME_KEY);
        } else {
            // Get username from intent if not restored from saved state
            username = getIntent().getStringExtra("USERNAME");
        }

        // Retrieve user details from the database based on the username
        reference.child(username).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    // Retrieve user details and set them to TextViews
                    String name = snapshot.child("name").getValue(String.class);
                    String email = snapshot.child("email").getValue(String.class);
                    String password = snapshot.child("password").getValue(String.class);

                    // Set the values to the TextViews
                    MyName.setText(name);
                    MyEmail.setText(email);
                    MyUsername.setText(username);
                    MyPass.setText(password);
                } else {
                    // Handle the case where user data is not found
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Handle the error if data retrieval is canceled
            }
        });
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        // Save the username to the instance state
        outState.putString(USERNAME_KEY, username);
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
                        Toast.makeText(Account.this, "Dashboard clicked", Toast.LENGTH_SHORT).show();
//                        startActivity(new Intent(Account.this, LandingPage.class));

                        String dash_username = getIntent().getStringExtra("USERNAME");
                        Intent dash_Intent = new Intent(Account.this, LandingPage.class);
                        dash_Intent.putExtra("USERNAME", dash_username);

                        startActivity(dash_Intent);
                        break;
                    case R.id.my_account:
                        // Handle the "My Account" item click (if needed)
                        Toast.makeText(Account.this, "My Account clicked", Toast.LENGTH_SHORT).show();
                        break;
                    case R.id.add_contacts:
                        // Handle the "Add Contacts" item click by starting the AddContactsActivity
                        Toast.makeText(Account.this, "Add Contacts clicked", Toast.LENGTH_SHORT).show();
//                        startActivity(new Intent(Account.this, AddContacts.class));
                        String ac_username = getIntent().getStringExtra("USERNAME");
                        Intent ac_Intent = new Intent(Account.this, AddContacts.class);
                        ac_Intent.putExtra("USERNAME", ac_username);

                        startActivity(ac_Intent);
                        break;
                    case R.id.send_message:
                        // Handle the "Add Contacts" item click by starting the AddContactsActivity
                        Toast.makeText(Account.this, "Send Message clicked", Toast.LENGTH_SHORT).show();
//                        startActivity(new Intent(Account.this, SendMessage.class));

                        String msg_username = getIntent().getStringExtra("USERNAME");
                        Intent msg_Intent = new Intent(Account.this, SendMessage.class);
                        msg_Intent.putExtra("USERNAME", msg_username);

                        startActivity(msg_Intent);
                        break;
                    case R.id.logout:
                        // Handle the "Logout" item click (if needed)
                        Toast.makeText(Account.this, "You have log out", Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(Account.this, SignupActivity.class));
                        break;
                }

                // Close the navigation drawer
                drawerLayout.closeDrawers();
                return true;
            }
        });
    }
}
