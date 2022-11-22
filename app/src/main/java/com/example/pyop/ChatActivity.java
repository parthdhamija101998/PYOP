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
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.pyop.Notes.NotesActivity;
import com.example.pyop.Utilities.Constants;
import com.example.pyop.Utilities.PreferenceManager;
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
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.messaging.FirebaseMessaging;

public class ChatActivity extends AppCompatActivity {

    TextView name;
    Button notes,calendar,split;
    FloatingActionButton newChat;
    GoogleSignInOptions gso;
    GoogleSignInClient gsc;
    ImageButton menuButton;
    PreferenceManager preferenceManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        calendar = findViewById(R.id.btnHome);
        notes = findViewById(R.id.btnKeep);
        split = findViewById(R.id.btnSplitwise);
        newChat = findViewById(R.id.newChat);
        preferenceManager = new PreferenceManager(getApplicationContext());

        gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build();
        gsc = GoogleSignIn.getClient(this,gso);
        GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(this);


        name = findViewById(R.id.name);
        name.setText("Hello " + preferenceManager.getString(Constants.KEY_NAME));

        menuButton = findViewById(R.id.menuButton);
        menuButton.setOnClickListener((v)->showMenu());

        getToken();


        newChat.setOnClickListener(v -> {
            startActivity(new Intent(getApplicationContext(),UsersActivity.class));
        });

        notes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
                startActivity(new Intent(getApplicationContext(), NotesActivity.class));
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



    private void updateToken(String token){
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
                            documentReference.update(Constants.KEY_FCM_TOKEN, token)
                                    .addOnFailureListener(e -> Log.d("Unable to update Token",e.toString()));
                        }
                    }
                });
    }

    private void showToast(String message) {
        Toast.makeText(getApplicationContext(),message,Toast.LENGTH_SHORT).show();
    }

    private void getToken(){
        FirebaseMessaging.getInstance().getToken().addOnSuccessListener(this::updateToken);
    }

    private void showMenu() {
        PopupMenu popupMenu = new PopupMenu(ChatActivity.this,menuButton);
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
                                        documentReference.update(Constants.KEY_FCM_TOKEN, FieldValue.delete())
                                                .addOnSuccessListener(unused -> showToast("Token Deleted Successfully"))
                                                .addOnFailureListener(e -> Log.d("Unable to Delete Token",e.toString()));
                                    }
                                }
                            });
                    preferenceManager.clear();
                    gsc.signOut();
                    startActivity(new Intent(ChatActivity.this, SignInActivity.class));
                    finish();
                    return true;
                }
                return false;
            }
        });
    }
}