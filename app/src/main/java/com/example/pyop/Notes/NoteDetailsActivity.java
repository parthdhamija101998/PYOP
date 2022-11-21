package com.example.pyop.Notes;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.pyop.R;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class NoteDetailsActivity extends AppCompatActivity {

    EditText titleEditText, contentEditText;
    ImageButton saveNoteButton;
    TextView page_title;
    TextView delete_note_textView_btn;

    GoogleSignInOptions gso;
    GoogleSignInClient gsc;

    String title,content,docId;
    boolean isEditMode = false;

    FirebaseFirestore db = FirebaseFirestore.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_note_details);

        titleEditText = findViewById(R.id.notes_title_text);
        contentEditText = findViewById(R.id.notes_content_text);
        saveNoteButton = findViewById(R.id.save_note_button);
        page_title = findViewById(R.id.page_title);
        delete_note_textView_btn = findViewById(R.id.delete_note_text_view_btn);

        // Receiving the Data
        title = getIntent().getStringExtra("title");
        content = getIntent().getStringExtra("content");
        docId = getIntent().getStringExtra("docId");

        titleEditText.setText(title);
        contentEditText.setText(content);

        if(docId!=null&&!docId.isEmpty()){
            isEditMode = true;
        }
        if(isEditMode){
            page_title.setText("Edit Your Note");
            delete_note_textView_btn.setVisibility(View.VISIBLE);
        }

        delete_note_textView_btn.setOnClickListener((v) -> deleteNoteFromFirebase());
        saveNoteButton.setOnClickListener((v) -> saveNote());
    }

    private void deleteNoteFromFirebase() {
            DocumentReference documentReference;
            documentReference = Utility.getCollectionReferenceForNotes().document(docId);
            documentReference.delete().addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if (task.isSuccessful()){
                        Utility.showToast(NoteDetailsActivity.this,"Note Deleted Successfully");
                        finish();
                    }else {
                        Utility.showToast(NoteDetailsActivity.this,"Failed to Delete Note");
                    }
                }
            });
    }

    private void saveNote() {
        String noteTitle = titleEditText.getText().toString();
        String noteContent = contentEditText.getText().toString();
        if (noteTitle == null || noteTitle.isEmpty()) {
            titleEditText.setError("Title is required");
            return;
        }
        if (noteContent == null || noteTitle.isEmpty()) {
            contentEditText.setError("Title is required");
            return;
        }

        Note note = new Note();
        note.setTitle(noteTitle);
        note.setContent(noteContent);
        note.setTimestamp(Timestamp.now());

        saveNoteToFirebase(note);
    }

    void saveNoteToFirebase(Note note) {
        gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build();
        gsc = GoogleSignIn.getClient(this, gso);
        GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(this);

        if (isEditMode){
            DocumentReference documentReference;
            documentReference = Utility.getCollectionReferenceForNotes().document(docId);
            Map<String, Object> editNote = new HashMap<>();
            editNote.put("userID", account.getId());
            editNote.put("title", note.getTitle());
            editNote.put("content", note.getContent());
            editNote.put("timestamp",note.getTimestamp());
            editNote.put("pinned", false);
            documentReference.set(editNote);
            finish();

        }
        else{
            Map<String, Object> addNote = new HashMap<>();
            addNote.put("userID", account.getId());
            addNote.put("title", note.getTitle());
            addNote.put("content", note.getContent());
            addNote.put("timestamp",note.getTimestamp());
            addNote.put("pinned", false);

            // Add a new document with a generated ID
            db.collection("notes")
                    .add(addNote)
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
            finish();
        }

    }
}