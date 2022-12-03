package com.example.pyop;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.example.pyop.Utilities.Constants;
import com.example.pyop.Utilities.PreferenceManager;
import com.example.pyop.adapters.UsersAdapter;
import com.example.pyop.databinding.ActivityUsersBinding;
import com.example.pyop.listeners.UserListener;
import com.example.pyop.models.User;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class UsersActivity extends BaseActivity implements UserListener {

    private ActivityUsersBinding binding;
    private PreferenceManager preferenceManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityUsersBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        preferenceManager = new PreferenceManager(getApplicationContext());
        setListeners();
        getUsers();
    }

    private void showErrorMessage() {
        binding.textErrorMessage.setText(String.format("%s", "No user available"));
        binding.textErrorMessage.setVisibility(View.VISIBLE);
    }

    private void setListeners(){
        binding.imageBack.setOnClickListener(v-> onBackPressed());
    }

    private void getUsers() {
        loading(true);
        FirebaseFirestore database = FirebaseFirestore.getInstance();
        database.collection(Constants.KEY_COLLECTION_USERS)
                .get()
                .addOnCompleteListener(task -> {
                    loading(false);
                    String currentUserID = preferenceManager.getString(Constants.KEY_USER_ID);
                    if (task.isSuccessful() && task.getResult() != null) {
                        List<User> users = new ArrayList<>();
                        for (QueryDocumentSnapshot queryDocumentSnapshot : task.getResult()) {
                            if (currentUserID.equals(queryDocumentSnapshot.getString(Constants.KEY_USER_ID))) {
                                continue;
                            } else {
                                User user = new User();
                                user.userId = queryDocumentSnapshot.getString(Constants.KEY_USER_ID);
                                user.firstName = queryDocumentSnapshot.getString("firstName");
                                user.lastName = queryDocumentSnapshot.getString("lastName");
                                user.emailId = queryDocumentSnapshot.getString("emailId");
                                user.fcmToken = queryDocumentSnapshot.getString(Constants.KEY_FCM_TOKEN);
                                users.add(user);
                            }
                            if (users.size() > 0) {
                                UsersAdapter usersAdapter = new UsersAdapter(users, this);
                                binding.usersRecyclerView.setAdapter(usersAdapter);
                                binding.usersRecyclerView.setVisibility(View.VISIBLE);
                            } else {
                                showErrorMessage();
                            }
                        }
                    } else {
                        showErrorMessage();
                    }
                });

    }

    private void loading(Boolean isLoading) {
        if (isLoading) {
            binding.progressBar.setVisibility(View.VISIBLE);
        } else {
            binding.progressBar.setVisibility(View.INVISIBLE);
        }
    }

    @Override
    public void onUserClicked(User user) {
        Intent intent = new Intent(getApplicationContext(),ChatMainActivity.class);
        intent.putExtra(Constants.KEY_USER,user);
        startActivity(intent);
        finish();
    }
}