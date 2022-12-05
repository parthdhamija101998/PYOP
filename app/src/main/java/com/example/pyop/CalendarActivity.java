package com.example.pyop;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.PopupMenu;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.pyop.Notes.NotesActivity;
import com.example.pyop.Notes.Utility;
import com.example.pyop.Utilities.Constants;
import com.example.pyop.Utilities.PreferenceManager;
import com.example.pyop.adapters.CalendarAdapter;
import com.example.pyop.models.CalendarEvent;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

public class CalendarActivity extends AppCompatActivity {

    TextView name;
    Button notes,chat,split;
    RecyclerView calendarEventRecyclerView;
    GoogleSignInOptions gso;
    GoogleSignInClient gsc;
    PreferenceManager preferenceManager;
    ImageButton menuButton;
    FloatingActionButton newEvent;
    CalendarAdapter calendarAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calendar);
        preferenceManager = new PreferenceManager(getApplicationContext());

        name = findViewById(R.id.name);
        notes = findViewById(R.id.btnKeep);
        chat = findViewById(R.id.btnChat);
        split = findViewById(R.id.btnSplitwise);
        calendarEventRecyclerView = findViewById(R.id.calendarEventRecyclerView);
        newEvent = findViewById(R.id.newEvent);
        menuButton = findViewById(R.id.menuButton);
        menuButton.setOnClickListener((v)->showMenu());

        gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build();
        gsc = GoogleSignIn.getClient(this,gso);
        GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(this);

        name.setText("Hello " + preferenceManager.getString(Constants.KEY_NAME));
        chat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
                startActivity(new Intent(getApplicationContext(),ChatActivity.class));
            }
        });
        notes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
                startActivity(new Intent(getApplicationContext(), NotesActivity.class));
            }
        });
        split.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
                startActivity(new Intent(getApplicationContext(),SplitActivity.class));
            }
        });


        newEvent.setOnClickListener(v -> {
            startActivity(new Intent(getApplicationContext(), CalendarEventDetail.class));
        });

        setupRecyclerView(account);

    }


    private void setupRecyclerView(GoogleSignInAccount accountReceived) {
        Query otherQuery = Utility.getCollectionReferenceForCalendar()
                .whereEqualTo("userID",accountReceived.getId())
                .orderBy("startDateIndex", Query.Direction.ASCENDING);
        FirestoreRecyclerOptions<CalendarEvent> events = new FirestoreRecyclerOptions.Builder<CalendarEvent>()
                .setQuery(otherQuery,CalendarEvent.class).build();
        Log.d("Events",events.getSnapshots().toString());
        Log.d("accountReceived",accountReceived.getId());
        calendarEventRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        calendarAdapter = new CalendarAdapter(events,this);
        calendarEventRecyclerView.setAdapter(calendarAdapter);

    }

    private void showMenu() {
        PopupMenu popupMenu = new PopupMenu(CalendarActivity.this,menuButton);
        popupMenu.getMenu().add("Logout");
        popupMenu.show();
        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                if(menuItem.getTitle()=="Logout"){
                    FirebaseFirestore database = FirebaseFirestore.getInstance();
                    database.collection(Constants.KEY_COLLECTION_USERS)
                            .whereEqualTo("userID",preferenceManager.getString(Constants.KEY_USER_ID))
                            .get()
                            .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                @Override
                                public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                    if (task.isSuccessful()) {
                                        DocumentReference documentReference = database.collection(Constants.KEY_COLLECTION_USERS)
                                                .document(task.getResult().getDocuments().get(0).getId());
                                        documentReference.update(Constants.KEY_FCM_TOKEN, FieldValue.delete());
                                    }
                                }
                            });
                    gsc.signOut();
                    preferenceManager.clear();
                    startActivity(new Intent(CalendarActivity.this, SignInActivity.class));
                    finish();
                    return true;
                }
                return false;
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        calendarAdapter.startListening();
    }

    @Override
    protected void onStop() {
        super.onStop();
        calendarAdapter.stopListening();
    }

    @Override
    protected void onResume() {
        super.onResume();
        calendarAdapter.notifyDataSetChanged();
    }

}