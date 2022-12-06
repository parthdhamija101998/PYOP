package com.example.pyop.Notes;

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

import com.example.pyop.CalendarActivity;
import com.example.pyop.ChatActivity;
import com.example.pyop.R;
import com.example.pyop.SignInActivity;
import com.example.pyop.SplitActivity;
import com.example.pyop.Utilities.Constants;
import com.example.pyop.Utilities.PreferenceManager;
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

public class NotesActivity extends AppCompatActivity {

    TextView name;
    Button calendar, chat, split;
    GoogleSignInOptions gso;
    GoogleSignInClient gsc;

    RecyclerView recyclerViewPinned, recyclerViewOthers;
    ImageButton menuButton;

    FloatingActionButton addNoteButton;

    NoteAdapter otherNoteAdapter;
    NoteAdapter pinnedNoteAdapter;
    PreferenceManager preferenceManager;

    String docId, pinIt;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notes);
        calendar = findViewById(R.id.btnHome);
        chat = findViewById(R.id.btnChat);
        split = findViewById(R.id.btnSplitwise);

        gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build();
        gsc = GoogleSignIn.getClient(this, gso);
        GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(this);
        preferenceManager = new PreferenceManager(getApplicationContext());


        name = findViewById(R.id.name);
        name.setText("Hello " + preferenceManager.getString(Constants.KEY_NAME));

        addNoteButton = findViewById(R.id.addNoteButton);
        recyclerViewPinned = findViewById(R.id.recyclerViewPinned);
        recyclerViewOthers = findViewById(R.id.recyclerViewOthers);
        menuButton = findViewById(R.id.menuButton);

        docId = "1234";
        docId = getIntent().getStringExtra("docId");
        pinIt = getIntent().getStringExtra("pinIt");

        if (docId != null && !docId.isEmpty() && docId != "1234") {
            updatePin(pinIt,docId);
            Log.d("Pin IT",pinIt);
        }

        addNoteButton.setOnClickListener((v) -> startActivity(new Intent(NotesActivity.this, NoteDetailsActivity.class)));
        menuButton.setOnClickListener((v) -> showMenu());

        setupRecyclerView(account);
        chat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
                startActivity(new Intent(getApplicationContext(), ChatActivity.class));
            }
        });
        calendar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
                startActivity(new Intent(getApplicationContext(), CalendarActivity.class));
            }
        });
        split.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
                startActivity(new Intent(getApplicationContext(), SplitActivity.class));
            }
        });

    }

    private void setupRecyclerView(GoogleSignInAccount accountReceived) {

        Query otherQuery = Utility.getCollectionReferenceForNotes().whereEqualTo("userID", accountReceived.getId()).whereEqualTo("pinned", false);
        Query pinnedQuery = Utility.getCollectionReferenceForNotes().whereEqualTo("userID", accountReceived.getId()).whereEqualTo("pinned", true);
        FirestoreRecyclerOptions<Note> otherOptions = new FirestoreRecyclerOptions.Builder<Note>()
                .setQuery(otherQuery, Note.class).build();
        FirestoreRecyclerOptions<Note> pinnedOptions = new FirestoreRecyclerOptions.Builder<Note>()
                .setQuery(pinnedQuery, Note.class).build();
        recyclerViewOthers.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewPinned.setLayoutManager(new LinearLayoutManager(this));
        otherNoteAdapter = new NoteAdapter(otherOptions, this);
        pinnedNoteAdapter = new NoteAdapter(pinnedOptions, this);
        recyclerViewOthers.setAdapter(otherNoteAdapter);
        recyclerViewPinned.setAdapter(pinnedNoteAdapter);

    }

    @Override
    protected void onStart() {
        super.onStart();
        otherNoteAdapter.startListening();
        pinnedNoteAdapter.startListening();
    }

    @Override
    protected void onStop() {
        super.onStop();
        otherNoteAdapter.stopListening();
        pinnedNoteAdapter.stopListening();
    }

    @Override
    protected void onResume() {
        super.onResume();
        otherNoteAdapter.notifyDataSetChanged();
        pinnedNoteAdapter.notifyDataSetChanged();
    }

    private void showMenu() {
        PopupMenu popupMenu = new PopupMenu(NotesActivity.this, menuButton);
        popupMenu.getMenu().add("Logout");
        popupMenu.show();
        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                if (menuItem.getTitle() == "Logout") {
                    FirebaseFirestore database = FirebaseFirestore.getInstance();
                    database.collection(Constants.KEY_COLLECTION_USERS)
                            .whereEqualTo("userID", preferenceManager.getString(Constants.KEY_USER_ID))
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
                    startActivity(new Intent(NotesActivity.this, SignInActivity.class));
                    finish();
                    return true;
                }
                return false;
            }
        });
    }

    public void updatePin(String pinIt, String docId) {
        FirebaseFirestore database = FirebaseFirestore.getInstance();
        DocumentReference documentReference = database.collection("notes")
                .document(docId);
        if (pinIt.equals("true")) {
            documentReference.update("pinned", true)
                    .addOnFailureListener(e -> Log.d("Unable to update pinned", e.toString()));
        }
        else{
            documentReference.update("pinned", false)
                    .addOnFailureListener(e -> Log.d("Unable to update pinned", e.toString()));
        }
    }
}