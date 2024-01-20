package com.example.dam;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.LoaderManager;
import android.content.ContentResolver;
import android.content.Context;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.telephony.SmsManager;
import android.telephony.SubscriptionInfo;
import android.telephony.SubscriptionManager;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.google.android.material.navigation.NavigationView;

import java.util.ArrayList;
import java.util.List;

public class SendMessage extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {
    private DrawerLayout drawerLayout;
    private ActionBarDrawerToggle actionBarDrawerToggle;
    private NavigationView navigationView;

    private EditText Message;
    private Button btnSent;
    private Button btnPickContacts;
    private ListView contactListView;
    private ListView selectedNumbersListView;
    private SimpleCursorAdapter simpleCursorAdapter;
    private static final int CONTACT_PICKER_RESULT = 1001;
    private int selectedSim = 0;
    private ArrayList<String> selectedPhoneNumbers = new ArrayList<>();
    private Spinner messageSpinner;
    private boolean allContactsSelected = false;
    // Define a list of predefined messages
    private List<String> predefinedMessages;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_CONTACTS}, 101);
        }

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_send_message);

        messageSpinner = findViewById(R.id.message_spinner);

        // Create a list of predefined messages
        predefinedMessages = new ArrayList<>();
        predefinedMessages.add("Alert! Orange water level. Please be careful");
        predefinedMessages.add("Alert! Red water level. Please be careful");
        predefinedMessages.add("Manalangin tayo.");

        // Create an ArrayAdapter for the Spinner
        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, predefinedMessages);
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        // Set the ArrayAdapter to the Spinner
        messageSpinner.setAdapter(spinnerAdapter);

        // Initialize selectedNumbersListView after calling setContentView
        selectedNumbersListView = findViewById(R.id.contact_list);
        selectedPhoneNumbers = new ArrayList<>();
        updateSelectedNumbersListView();


        drawerLayout = findViewById(R.id.activity_sendMessage);
        actionBarDrawerToggle = new ActionBarDrawerToggle(this, drawerLayout, R.string.nav_open, R.string.nav_close);
        drawerLayout.addDrawerListener(actionBarDrawerToggle);
        actionBarDrawerToggle.syncState();

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        navigationView = findViewById(R.id.navigation_view);
        setupDrawerContent(navigationView);

        btnSent = findViewById(R.id.send_button);
        btnPickContacts = findViewById(R.id.pick_contacts_button);

        selectedNumbersListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, final int position, long id) {
                AlertDialog.Builder builder = new AlertDialog.Builder(SendMessage.this);
                builder.setMessage("Remove this contact?")
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                // Remove the selected contact
                                selectedPhoneNumbers.remove(position);
                                updateSelectedNumbersListView();
                            }
                        })
                        .setNegativeButton("No", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                // User cancelled the dialog
                            }
                        });
                builder.create().show();

                return true;
            }
        });

        btnPickContacts.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent contactPickerIntent = new Intent(Intent.ACTION_PICK, ContactsContract.CommonDataKinds.Phone.CONTENT_URI);
                startActivityForResult(contactPickerIntent, CONTACT_PICKER_RESULT);
            }
        });

        btnSent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (ContextCompat.checkSelfPermission(SendMessage.this, Manifest.permission.SEND_SMS) == PackageManager.PERMISSION_GRANTED) {
                    showSimCardSelectionDialog();
                } else {
                    ActivityCompat.requestPermissions(SendMessage.this, new String[]{Manifest.permission.SEND_SMS}, 100);
                }
            }
        });

        getLoaderManager().initLoader(0, null, this);
    }

    public void selectAllContacts(View view) {
        if (allContactsSelected) {
            // Deselect all contacts
            selectedPhoneNumbers.clear();
            Toast.makeText(this, "All contacts deselected", Toast.LENGTH_SHORT).show();
        } else {
            // Select all contacts
            Cursor cursor = simpleCursorAdapter.getCursor();
            if (cursor != null && cursor.moveToFirst()) {
                int phoneNumberColumnIndex = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER);
                do {
                    String selectedPhoneNumber = cursor.getString(phoneNumberColumnIndex);
                    if (!selectedPhoneNumbers.contains(selectedPhoneNumber)) {
                        selectedPhoneNumbers.add(selectedPhoneNumber);
                    }
                } while (cursor.moveToNext());
            }

            Toast.makeText(this, "All contacts selected", Toast.LENGTH_SHORT).show();
        }

        allContactsSelected = !allContactsSelected;
        updateSelectedNumbersListView();
    }



    private void updateSelectedNumbersListView() {
        if (selectedPhoneNumbers.isEmpty()) {
            // Display a Toast indicating that all contacts have been selected
            if (allContactsSelected) {
                Toast.makeText(this, "All contacts selected", Toast.LENGTH_SHORT).show();
            }

            // Clear the ListView
            selectedNumbersListView.setAdapter(null);
        } else {
            // Update the ListView with the names of selected contacts
            ArrayList<String> selectedContactNames = new ArrayList<>();
            for (String phoneNumber : selectedPhoneNumbers) {
                String contactName = getContactNameFromNumber(phoneNumber);
                selectedContactNames.add(contactName);
            }

            ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, selectedContactNames);
            selectedNumbersListView.setAdapter(adapter);
        }
    }


    private String getContactNameFromNumber(String phoneNumber) {
        String contactName = phoneNumber; // Default to the phone number if name not found

        Cursor cursor = getContentResolver().query(
                ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                new String[]{ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME},
                ContactsContract.CommonDataKinds.Phone.NUMBER + " = ?",
                new String[]{phoneNumber},
                null
        );

        if (cursor != null && cursor.moveToFirst()) {
            int nameColumnIndex = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME);
            contactName = cursor.getString(nameColumnIndex);
            cursor.close();
        }

        return contactName;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == CONTACT_PICKER_RESULT) {
            if (resultCode == Activity.RESULT_OK) {
                Uri contactUri = data.getData();
                Cursor cursor = getContentResolver().query(contactUri, null, null, null, null);

                if (cursor != null && cursor.moveToFirst()) {
                    int phoneNumberColumn = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER);
                    String selectedPhoneNumber = cursor.getString(phoneNumberColumn);
                    selectedPhoneNumbers.add(selectedPhoneNumber);

                    updateSelectedNumbersListView();

                    cursor.close();
                }
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 100) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission was granted, you can now proceed with sending SMS.
                showSimCardSelectionDialog();
            } else {
                Toast.makeText(SendMessage.this, "SEND_SMS permission denied", Toast.LENGTH_SHORT).show();
            }
        }
    }


    private void sendSMS(int selectedSim) {
        String selectedMessage = messageSpinner.getSelectedItem().toString();

        if (!selectedPhoneNumbers.isEmpty() && !selectedMessage.isEmpty()) {
            SubscriptionManager subscriptionManager = SubscriptionManager.from(this);
            List<SubscriptionInfo> subscriptionInfoList = subscriptionManager.getActiveSubscriptionInfoList();

            if (subscriptionInfoList != null && selectedSim < subscriptionInfoList.size()) {
                int subscriptionId = subscriptionInfoList.get(selectedSim).getSubscriptionId();

                SmsManager smsManager = SmsManager.getSmsManagerForSubscriptionId(subscriptionId);
                for (String selectedPhoneNumber : selectedPhoneNumbers) {
                    // Use the selected message from the spinner
                    smsManager.sendTextMessage(selectedPhoneNumber, null, selectedMessage, null, null);
                }
                Toast.makeText(this, "SMS sent successfully using SIM " + (selectedSim + 1), Toast.LENGTH_SHORT).show();
                // No need to clearEditText() as there's no EditText for the message
            } else {
                Toast.makeText(this, "Selected SIM card not available", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(this, "Please select phone numbers and a message", Toast.LENGTH_SHORT).show();
        }
    }


    private void showSimCardSelectionDialog() {
        SubscriptionManager subscriptionManager = SubscriptionManager.from(this);
        List<SubscriptionInfo> subscriptionInfoList = subscriptionManager.getActiveSubscriptionInfoList();

        if (subscriptionInfoList != null && subscriptionInfoList.size() > 1) {
            String[] simCardNames = new String[subscriptionInfoList.size()];
            for (int i = 0; i < subscriptionInfoList.size(); i++) {
                simCardNames[i] = "SIM " + (i + 1);
            }

            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Select SIM Card");
            builder.setItems(simCardNames, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    selectedSim = which;
                    sendSMS(selectedSim);
                }
            });
            builder.show();
        } else if (subscriptionInfoList != null && subscriptionInfoList.size() == 1) {
            selectedSim = 0;
            sendSMS(selectedSim);
        } else {
            Toast.makeText(this, "No SIM card available", Toast.LENGTH_SHORT).show();
        }
    }

    private void clearEditText() {
        Message.setText("");
        selectedPhoneNumbers.clear();
        updateSelectedNumbersListView();
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        Uri baseUri = ContactsContract.CommonDataKinds.Phone.CONTENT_URI;
        String select = "((" + ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME + " NOT NULL) AND ("
                + ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME + " != '' ))";
        return new CursorLoader(this, baseUri, null, select, null, ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME + " COLLATE NOCASE ASC");
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        if (simpleCursorAdapter == null) {
            // Initialize simpleCursorAdapter if it's null
            simpleCursorAdapter = new SimpleCursorAdapter(
                    this,
                    android.R.layout.simple_list_item_1,
                    data,
                    new String[]{ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME},
                    new int[]{android.R.id.text1},
                    0
            );

            // Set the adapter to your ListView
            selectedNumbersListView.setAdapter(simpleCursorAdapter);
        } else {
            // Swap the cursor if simpleCursorAdapter is not null
            simpleCursorAdapter.swapCursor(data);
        }

        // Check if allContactsSelected is true before updating the ListView
        if (!allContactsSelected) {
            updateSelectedNumbersListView();
        }
    }


    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        simpleCursorAdapter.swapCursor(null);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
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
                        Toast.makeText(SendMessage.this, "Dashboard clicked", Toast.LENGTH_SHORT).show();
//                        startActivity(new Intent(SendMessage.this, LandingPage.class));

                        String dash_username = getIntent().getStringExtra("USERNAME");
                        Intent dash_Intent = new Intent(SendMessage.this, LandingPage.class);
                        dash_Intent.putExtra("USERNAME", dash_username);

                        startActivity(dash_Intent);
                        break;
                    case R.id.my_account:
                        Toast.makeText(SendMessage.this, "My Account clicked", Toast.LENGTH_SHORT).show();
//                        startActivity(new Intent(SendMessage.this, Account.class));
                        String sm_username = getIntent().getStringExtra("USERNAME");
                        Intent accountIntent = new Intent(SendMessage.this, Account.class);
                        accountIntent.putExtra("USERNAME", sm_username);

                        startActivity(accountIntent);
                        break;
                    case R.id.add_contacts:
                        Toast.makeText(SendMessage.this, "Add Contacts clicked", Toast.LENGTH_SHORT).show();
//                        startActivity(new Intent(SendMessage.this, AddContacts.class));
                        String ac_username = getIntent().getStringExtra("USERNAME");
                        Intent ac_Intent = new Intent(SendMessage.this, AddContacts.class);
                        ac_Intent.putExtra("USERNAME", ac_username);

                        startActivity(ac_Intent);
                        break;
                    case R.id.send_message:
                        Toast.makeText(SendMessage.this, "Send Message clicked", Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(SendMessage.this, SendMessage.class));
                        break;
                    case R.id.logout:
                        Toast.makeText(SendMessage.this, "You have logged out", Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(SendMessage.this, SignupActivity.class));
                        break;
                }

                drawerLayout.closeDrawers();
                return true;
            }
        });
    }
}