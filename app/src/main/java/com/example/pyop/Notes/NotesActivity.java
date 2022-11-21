package com.example.pyop.Notes;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.widget.ImageButton;
import android.widget.PopupMenu;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.pyop.SignInActivity;
import com.example.pyop.R;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.firestore.Query;

public class NotesActivity extends AppCompatActivity {

    TextView name;
    GoogleSignInOptions gso;
    GoogleSignInClient gsc;

    RecyclerView recyclerViewPinned,recyclerViewOthers;
    ImageButton menuButton;

    FloatingActionButton addNoteButton;

    NoteAdapter otherNoteAdapter;
    NoteAdapter pinnedNoteAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notes);

        gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build();
        gsc = GoogleSignIn.getClient(this,gso);
        GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(this);


        name = findViewById(R.id.name);
        String Name = account.getDisplayName().split(" ")[0];
        name.setText("Hello " + Name);

        addNoteButton = findViewById(R.id.addNoteButton);
        recyclerViewPinned = findViewById(R.id.recyclerViewPinned);
        recyclerViewOthers =findViewById(R.id.recyclerViewOthers);
        menuButton = findViewById(R.id.menuButton);

        addNoteButton.setOnClickListener((v) -> startActivity(new Intent(NotesActivity.this,NoteDetailsActivity.class)) );
        menuButton.setOnClickListener((v)->showMenu());

        setupRecyclerView(account);


    }

    private void setupRecyclerView(GoogleSignInAccount accountReceived) {

        Query otherQuery = Utility.getCollectionReferenceForNotes().whereEqualTo("userID",accountReceived.getId()).whereEqualTo("pinned",false);
        Query pinnedQuery = Utility.getCollectionReferenceForNotes().whereEqualTo("userID",accountReceived.getId()).whereEqualTo("pinned",true);
        FirestoreRecyclerOptions<Note> otherOptions = new FirestoreRecyclerOptions.Builder<Note>()
                .setQuery(otherQuery,Note.class).build();
        Log.e("Notes",otherOptions.getSnapshots().toString());
        FirestoreRecyclerOptions<Note> pinnedOptions = new FirestoreRecyclerOptions.Builder<Note>()
                .setQuery(pinnedQuery,Note.class).build();
        recyclerViewOthers.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewPinned.setLayoutManager(new LinearLayoutManager(this));
        otherNoteAdapter = new NoteAdapter(otherOptions,this);
        pinnedNoteAdapter = new NoteAdapter(pinnedOptions,this);
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
        PopupMenu popupMenu = new PopupMenu(NotesActivity.this,menuButton);
        popupMenu.getMenu().add("Logout");
        popupMenu.show();
        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                if(menuItem.getTitle()=="Logout"){
                    gsc.signOut();
                    startActivity(new Intent(NotesActivity.this, SignInActivity.class));
                    finish();
                    return true;
                }
                return false;
            }
        });
    }
}