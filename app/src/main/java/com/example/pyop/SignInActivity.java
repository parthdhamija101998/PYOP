package com.example.pyop;

import static android.content.ContentValues.TAG;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.pyop.Utilities.Constants;
import com.example.pyop.Utilities.PreferenceManager;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.HashMap;
import java.util.Map;

public class SignInActivity extends AppCompatActivity {

    GoogleSignInOptions gso;
    GoogleSignInClient gsc;

    PreferenceManager preferenceManager;

    FirebaseFirestore db = FirebaseFirestore.getInstance();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signin);
        preferenceManager = new PreferenceManager(getApplicationContext());

        Button googleSignIn = findViewById(R.id.googleSignInButton);

        gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build();
        gsc = GoogleSignIn.getClient(this,gso);

        googleSignIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SignIn();
            }
        });

    }


    private void SignIn() {
        Intent intent = gsc.getSignInIntent();
        startActivityForResult(intent,100);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode==100){
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                task.getResult(ApiException.class);
                saveToFirestore();
                CalendarActivity();
            } catch (ApiException e) {
                Toast.makeText(this,"Error " + e.toString(),Toast.LENGTH_LONG).show();
            }
        }
    }

    private void saveToFirestore() {
        GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(this);
        preferenceManager.putBoolean(Constants.KEY_IS_SIGNED_IN,true);
        preferenceManager.putString(Constants.KEY_USER_ID,account.getId());
        preferenceManager.putString(Constants.KEY_EMAIL,account.getEmail());
        preferenceManager.putString(Constants.KEY_NAME,account.getDisplayName().split(" ")[0]);
        if (account!=null){
            String Name = account.getDisplayName().split(" ")[0];
            String LName = account.getFamilyName();
            String Email = account.getEmail();
            String uID = account.getId();

            db.collection("users")
                    .whereEqualTo("userID",uID)
                    .get()
                    .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<QuerySnapshot> task) {
                            if (task.isSuccessful()) {
                                if (task.getResult().isEmpty()) {
//                                     Create a new user with a first and last name
                                    Map<String, Object> user = new HashMap<>();
                                    user.put("firstName", Name);
                                    user.put("lastName", LName);
                                    user.put("emailId", Email);
                                    user.put("userID", uID);

                                    // Add a new document with a generated ID
                                    db.collection("users")
                                            .add(user)
                                            .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                                                @Override
                                                public void onSuccess(DocumentReference documentReference) {
                                                    Log.d("TAG", "DocumentSnapshot added with ID: " + documentReference.getId());
                                                }
                                            })
                                            .addOnFailureListener(new OnFailureListener() {
                                                @Override
                                                public void onFailure(@NonNull Exception e) {
                                                    Log.w("TAG", "Error adding document", e);
                                                }
                                            });
                                }

                            } else {
                                Log.w(TAG, "Error getting document", task.getException());
                            }
                        }
                    });
        }
    }

    private void CalendarActivity() {
        finish();
        Intent intent = new Intent(getApplicationContext(), CalendarActivity.class);
        startActivity(intent);
    }
}