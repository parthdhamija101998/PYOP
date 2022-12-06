package com.example.pyop.Notes;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.pyop.R;
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;

public class NoteAdapter extends FirestoreRecyclerAdapter<Note, NoteAdapter.NoteViewHolder> {

    Context  context;

    public NoteAdapter(@NonNull FirestoreRecyclerOptions<Note> options, Context context) {
        super(options);
        this.context = context;
    }

    @NonNull
    @Override
    public NoteViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.recycler_note_item,parent,false);
        return new NoteViewHolder(view);
    }


    @Override
    public void onBindViewHolder(@NonNull NoteViewHolder holder, int position, @NonNull Note note) {
        holder.titleTextView.setText(note.title);
        holder.contentTextView.setText(note.content);
        holder.timestampTextView.setText(Utility.timestamptoString(note.timestamp));
        holder.itemView.setOnClickListener((v) -> {
            Intent intent = new Intent(context,NoteDetailsActivity.class);
            intent.putExtra("title",note.title);
            intent.putExtra("content",note.content);
            String docId = this.getSnapshots().getSnapshot(position).getId();
            intent.putExtra("docId",docId);
            context.startActivity(intent);
        });
        holder.pin_note_button.setOnClickListener(v -> {
            String docId = this.getSnapshots().getSnapshot(position).getId();
            Toast.makeText(context,"Clicked Pin for " + docId,Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(context,NotesActivity.class);
            boolean pin = note.isPinned();
            if (pin)
                intent.putExtra("pinIt","false");
            else
                intent.putExtra("pinIt","true");
            intent.putExtra("docId",docId);
            context.startActivity(intent);
        });
    }

    public class NoteViewHolder extends RecyclerView.ViewHolder{

        TextView titleTextView, contentTextView, timestampTextView;
        ImageButton pin_note_button;

        public NoteViewHolder(@NonNull View itemView) {
            super(itemView);
            titleTextView = itemView.findViewById(R.id.notes_title_text);
            contentTextView = itemView.findViewById(R.id.notes_content_text);
            timestampTextView = itemView.findViewById(R.id.notes_timestamp_text);
            pin_note_button = itemView.findViewById(R.id.pin_note_button);

        }

    }
}
