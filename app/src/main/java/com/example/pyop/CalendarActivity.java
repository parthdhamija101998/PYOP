package com.example.pyop;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.PopupMenu;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.pyop.Notes.NotesActivity;
import com.example.pyop.Utilities.Constants;
import com.example.pyop.Utilities.PreferenceManager;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;

public class CalendarActivity extends AppCompatActivity {

    TextView name;
    Button notes,chat,split;
    GoogleSignInOptions gso;
    GoogleSignInClient gsc;

    PreferenceManager preferenceManager;

    ImageButton menuButton;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calendar);
        preferenceManager = new PreferenceManager(getApplicationContext());

        name = findViewById(R.id.name);
        notes = findViewById(R.id.btnKeep);
        chat = findViewById(R.id.btnChat);
        split = findViewById(R.id.btnSplitwise);
        menuButton = findViewById(R.id.menuButton);
        menuButton.setOnClickListener((v)->showMenu());

        gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build();
        gsc = GoogleSignIn.getClient(this,gso);

        name.setText("Hello" + preferenceManager.getString(Constants.KEY_NAME));

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

    }

    private void showMenu() {
        PopupMenu popupMenu = new PopupMenu(CalendarActivity.this,menuButton);
        popupMenu.getMenu().add("Logout");
        popupMenu.show();
        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                if(menuItem.getTitle()=="Logout"){
                    gsc.signOut();
                    startActivity(new Intent(CalendarActivity.this, SignInActivity.class));
                    finish();
                    return true;
                }
                return false;
            }
        });
    }

}