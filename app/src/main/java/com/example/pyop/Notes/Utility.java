package com.example.pyop.Notes;

import android.content.Context;
import android.widget.Toast;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;

public class Utility {

    public static void showToast(Context context, String message){
        Toast.makeText(context,message,Toast.LENGTH_SHORT).show();
    }

    public static CollectionReference getCollectionReferenceForNotes(){
        return FirebaseFirestore.getInstance().collection("notes");
    }

    public static String timestamptoString(Timestamp timestamp){
        return new SimpleDateFormat("MM/dd/yyyy").format(timestamp.toDate());
    }

    public static CollectionReference getCollectionReferenceForCalendar(){
        return FirebaseFirestore.getInstance().collection("events");
    }
}
