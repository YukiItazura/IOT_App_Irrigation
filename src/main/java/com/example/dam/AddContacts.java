package com.example.dam;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import android.content.ContentProviderOperation;
import android.content.Intent;
import android.content.OperationApplicationException;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.Manifest;
import android.os.RemoteException;
import android.provider.ContactsContract;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.material.navigation.NavigationView;

import java.util.ArrayList;

public class AddContacts extends AppCompatActivity {
    public DrawerLayout drawerLayout;
    public ActionBarDrawerToggle actionBarDrawerToggle;

    private NavigationView navigationView;
    private EditText Name, Phone;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_contacts);

        drawerLayout = findViewById(R.id.addContacts_activity);
        actionBarDrawerToggle = new ActionBarDrawerToggle(this, drawerLayout, R.string.nav_open, R.string.nav_close);
        drawerLayout.addDrawerListener(actionBarDrawerToggle);
        actionBarDrawerToggle.syncState();

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        navigationView = findViewById(R.id.navigation_view);
        setupDrawerContent(navigationView);

        ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.WRITE_CONTACTS},
                PackageManager.PERMISSION_GRANTED);

        Name = findViewById(R.id.Contact_Name);
        Phone = findViewById(R.id.Contact_Phone);

        Phone.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    // When the EditText gains focus, set the text to "+639" and move the cursor to the end
                    Phone.setText("+639");
                    Phone.setSelection(Phone.getText().length());
                }
            }
        });
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
                        Toast.makeText(AddContacts.this, "Dashboard clicked", Toast.LENGTH_SHORT).show();
//                        startActivity(new Intent(AddContacts.this, LandingPage.class));
                        String dash_username = getIntent().getStringExtra("USERNAME");
                        Intent dash_Intent = new Intent(AddContacts.this, LandingPage.class);
                        dash_Intent.putExtra("USERNAME", dash_username);

                        startActivity(dash_Intent);
                        break;

                    case R.id.my_account:
                        // Handle the "My Account" item click (if needed)
                        Toast.makeText(AddContacts.this, "My Account clicked", Toast.LENGTH_SHORT).show();
//                        startActivity(new Intent(AddContacts.this, Account.class));

                        String ac_username = getIntent().getStringExtra("USERNAME");
                        Intent accountIntent = new Intent(AddContacts.this, Account.class);
                        accountIntent.putExtra("USERNAME", ac_username);

                        startActivity(accountIntent);

                        break;
                    case R.id.add_contacts:
                        // Handle the "Add Contacts" item click by starting the AddContactsActivity
                        Toast.makeText(AddContacts.this, "Add Contacts clicked", Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(AddContacts.this, AddContacts.class));
                        break;
                    case R.id.send_message:
                        // Handle the "Add Contacts" item click by starting the AddContactsActivity
                        Toast.makeText(AddContacts.this, "Send Message clicked", Toast.LENGTH_SHORT).show();
//                        startActivity(new Intent(AddContacts.this, SendMessage.class));
                        String msg_username = getIntent().getStringExtra("USERNAME");
                        Intent msg_Intent = new Intent(AddContacts.this, SendMessage.class);
                        msg_Intent.putExtra("USERNAME", msg_username);

                        startActivity(msg_Intent);
                        break;
                    case R.id.logout:
                        // Handle the "Logout" item click (if needed)
                        Toast.makeText(AddContacts.this, "You have log out", Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(AddContacts.this, SignupActivity.class));
                        break;
                }

                // Close the navigation drawer
                drawerLayout.closeDrawers();
                return true;
            }
        });
    }

    public void buttonAddContact(View view){
        try {
            String name = Name.getText().toString().trim();
            String phone = Phone.getText().toString().trim();

            if (isValidName(name) && isValidPhoneNumber(phone)) {
                ArrayList<ContentProviderOperation> contentProviderOperations
                        = new ArrayList<ContentProviderOperation>();

                contentProviderOperations.add(ContentProviderOperation.newInsert(
                                ContactsContract.RawContacts.CONTENT_URI)
                        .withValue(ContactsContract.RawContacts.ACCOUNT_TYPE, null)
                        .withValue(ContactsContract.RawContacts.ACCOUNT_NAME, null)
                        .build());

                // adding name
                contentProviderOperations.add(ContentProviderOperation
                        .newInsert(ContactsContract.Data.CONTENT_URI)
                        .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, 0)
                        .withValue(ContactsContract.Data.MIMETYPE,
                                ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE)
                        .withValue(ContactsContract.CommonDataKinds.StructuredName.DISPLAY_NAME,
                                Name.getText().toString())
                        .build());

                // adding numbers
                contentProviderOperations.add(ContentProviderOperation
                        .newInsert(ContactsContract.Data.CONTENT_URI)
                        .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, 0)
                        .withValue(ContactsContract.Data.MIMETYPE,
                                ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE)
                        .withValue(ContactsContract.CommonDataKinds.Phone.NUMBER,
                                Phone.getText().toString())
                        .withValue(ContactsContract.CommonDataKinds.Phone.TYPE,
                                ContactsContract.CommonDataKinds.Phone.TYPE_MOBILE)
                        .build());

                try {
                    getContentResolver().applyBatch(ContactsContract.AUTHORITY, contentProviderOperations);
                } catch (OperationApplicationException e) {
                    throw new RuntimeException(e);
                } catch (RemoteException e) {
                    throw new RuntimeException(e);
                }

                Toast.makeText(this, "Contact has been saved", Toast.LENGTH_SHORT).show();

                // Clear the input fields
                Name.getText().clear();
                Phone.getText().clear();
            } else {
                // Display an error toast message
                Toast.makeText(this, "Invalid name or phone number", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "An error occurred: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }

    }

    private boolean isValidName(String name) {
        // Use a regular expression to validate the name (letters and spaces)
        // This regex allows letters and spaces only.
        String namePattern = "^[a-zA-Z\\s]+$";
        return name.matches(namePattern);
    }

    private boolean isValidPhoneNumber(String phone) {
        // Use a regular expression to validate the phone number (+639 followed by 12 digits)
        String phonePattern = "^\\+639\\d{9}$";
        return phone.matches(phonePattern);
    }
}

