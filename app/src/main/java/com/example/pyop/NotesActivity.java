package com.example.pyop;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class NotesActivity extends AppCompatActivity {

    FloatingActionButton addNoteButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notes);

        addNoteButton = findViewById(R.id.addNoteButton);

        addNoteButton.setOnClickListener((v) -> startActivity(new Intent(NotesActivity.this,NoteDetailsActivity.class)) );


    }
}