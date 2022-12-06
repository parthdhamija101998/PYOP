package com.example.pyop;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.View;
import android.widget.DatePicker;
import android.widget.TextView;
import android.widget.TimePicker;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.DialogFragment;

import com.example.pyop.Notes.Utility;
import com.example.pyop.databinding.ActivityCalendarEventDetailBinding;
import com.example.pyop.models.CalendarEvent;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;


public class CalendarEventDetail extends AppCompatActivity {

    private ActivityCalendarEventDetailBinding binding;
    private static TextView startDate;
    private static TextView endDate;
    private static TextView eventTime;
    private static TextView indexingDate;
    private static int dateSelect = 0;
    GoogleSignInOptions gso;
    GoogleSignInClient gsc;
    boolean isEditMode = false;

    FirebaseFirestore db = FirebaseFirestore.getInstance();
    String eventTitle, eventDescription, docId, eventStartDate, eventEndDate, eventStartTime,startDateIndex;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityCalendarEventDetailBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        binding.saveEventBtn.setOnClickListener(v -> {
            saveEvent();
        });
        startDate = binding.startDateDisplay;
        endDate = binding.endDateDisplay;
        eventTime = binding.timeDisplay;
        indexingDate = binding.indexingDate;


        eventTitle = getIntent().getStringExtra("eventTitle");
        eventDescription = getIntent().getStringExtra("eventDescription");
        docId = getIntent().getStringExtra("docId");
        eventStartDate = getIntent().getStringExtra("eventStartDate");
        eventEndDate = getIntent().getStringExtra("eventEndDate");
        eventStartTime = getIntent().getStringExtra("eventTime");
        startDateIndex = getIntent().getStringExtra("startDateIndex");
        docId = getIntent().getStringExtra("docId");

        if(docId!=null&&!docId.isEmpty()){
            isEditMode = true;
        }
        if(isEditMode){
            binding.pageTitle.setText("Edit Event");
            binding.deleteEventTextViewBtn.setVisibility(View.VISIBLE);
        }
        binding.deleteEventTextViewBtn.setOnClickListener((v) -> deleteNoteFromFirebase());
        binding.eventTitle.setText(eventTitle);
        binding.eventDescription.setText(eventDescription);
        binding.startDateDisplay.setText(eventStartDate);
        binding.endDateDisplay.setText(eventEndDate);
        binding.timeDisplay.setText(eventStartTime);
        binding.indexingDate.setText(startDateIndex);

    }


    private void saveEvent() {
        String eventTitle = binding.eventTitle.getText().toString();
        String eventDescription = binding.eventDescription.getText().toString();
        String eventStartDate = binding.startDateDisplay.getText().toString();
        String eventEndDate = binding.endDateDisplay.getText().toString();
        String eventTime = binding.timeDisplay.getText().toString();
        String indexingDate = binding.indexingDate.getText().toString();

        if (eventTitle == null || eventTitle.isEmpty()) {
            binding.eventTitle.setError("Title is required");
            return;
        }
        if (eventDescription == null || eventDescription.isEmpty()) {
            binding.eventDescription.setError("Description is required");
            return;
        }
        if (eventStartDate == null || eventStartDate.isEmpty()) {
            binding.eventStartDate.setError("Start Date is required");
            return;
        }
        if (eventEndDate == null || eventEndDate.isEmpty()) {
            binding.eventEndDate.setError("End Date is required");
            return;
        }
        if (eventTime == null || eventTime.isEmpty()) {
            binding.eventTime.setError("Start Time is required");
            return;
        }

        CalendarEvent event = new CalendarEvent();

        event.setEventTitle(eventTitle);
        event.setEventDescription(eventDescription);
        event.setStartDate(eventStartDate);
        event.setEndDate(eventEndDate);
        event.setEventTime(eventTime);


        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            String date = eventStartDate.split(",")[1].trim();
            Date parsedDate = sdf.parse(date);
            Timestamp myTimestamp = new Timestamp(parsedDate);
            event.setStartDateIndex(myTimestamp);
        } catch (Exception e) {
            e.printStackTrace();
            Log.d("Timestamp Error",e.toString());
        }
        saveEventToFirebase(event);
    }

    private void deleteNoteFromFirebase() {
        DocumentReference documentReference;
        documentReference = Utility.getCollectionReferenceForCalendar().document(docId);
        documentReference.delete().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    Utility.showToast(CalendarEventDetail.this, "Event Deleted Successfully");
                    finish();
                } else {
                    Utility.showToast(CalendarEventDetail.this, "Failed to Delete Event");
                }
            }
        });
    }


    private void saveEventToFirebase(CalendarEvent event) {
        gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build();
        gsc = GoogleSignIn.getClient(this, gso);
        GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(this);

        if (isEditMode) {
            DocumentReference documentReference;
            documentReference = Utility.getCollectionReferenceForCalendar().document(docId);
            Map<String, Object> editEvent = new HashMap<>();
            editEvent.put("userID", account.getId());
            editEvent.put("eventTitle", event.getEventTitle());
            editEvent.put("eventDescription", event.getEventDescription());
            editEvent.put("startDate", event.getStartDate());
            editEvent.put("endDate", event.getEndDate());
            editEvent.put("eventTime", event.getEventTime());
            editEvent.put("startDateIndex", event.getStartDateIndex());
            documentReference.set(editEvent);
        } else {
            Map<String, Object> addEvent = new HashMap<>();
            addEvent.put("userID", account.getId());
            addEvent.put("eventTitle", event.getEventTitle());
            addEvent.put("eventDescription", event.getEventDescription());
            addEvent.put("startDate", event.getStartDate());
            addEvent.put("endDate", event.getEndDate());
            addEvent.put("eventTime", event.getEventTime());
            addEvent.put("startDateIndex", event.getStartDateIndex());

            // Add a new document with a generated ID
            db.collection("events")
                    .add(addEvent)
                    .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                        @Override
                        public void onSuccess(DocumentReference documentReference) {
                            Log.d("TAG", "DocumentSnapshot added with ID: " + documentReference.getId());
                        }
                    });
        }
        finish();


    }

    public void showDatePickerDialog(View view) {
        dateSelect = 0;
        DialogFragment newFragment = new DatePickerFragment();
        newFragment.show(getSupportFragmentManager(), "datePicker");
    }

    public void showTimePickerDialog(View view) {
        DialogFragment newFragment = new TimePickerFragment();
        newFragment.show(getSupportFragmentManager(), "timePicker");
    }

    public void showEndDatePickerDialog(View view) {
        dateSelect = 1;
        DialogFragment newFragment = new DatePickerFragment();
        newFragment.show(getSupportFragmentManager(), "datePicker");
    }

    public static class DatePickerFragment extends DialogFragment
            implements DatePickerDialog.OnDateSetListener {

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            // Use the current date as the default date in the picker
            final Calendar c = Calendar.getInstance();
            int year = c.get(Calendar.YEAR);
            int month = c.get(Calendar.MONTH);
            int day = c.get(Calendar.DAY_OF_MONTH);

            return new DatePickerDialog(requireContext(), this, year, month, day);
        }

        public void onDateSet(DatePicker view, int year, int month, int day) {
            Calendar c = Calendar.getInstance();
            c.set(year, month, day, 00, 00, 00);

            SimpleDateFormat formatter = new SimpleDateFormat("EEE, yyyy-MM-dd");
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
            String indexDate = sdf.format(c.getTime());
            String date = formatter.format(c.getTime());
            if (dateSelect == 0) {
                startDate.setText(date);
                indexingDate.setText(indexDate);
            } else
                endDate.setText(date);
        }
    }

    public static class TimePickerFragment extends DialogFragment
            implements TimePickerDialog.OnTimeSetListener {


        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            final Calendar c = Calendar.getInstance();
            int hour = c.get(Calendar.HOUR_OF_DAY);
            int minute = c.get(Calendar.MINUTE);

            return new TimePickerDialog(getActivity(), this, hour, minute,
                    DateFormat.is24HourFormat(getActivity()));
        }

        public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
            Calendar c = Calendar.getInstance();
            c.set(2000, 01, 1, hourOfDay, minute);

            SimpleDateFormat formatter = new SimpleDateFormat("HH:mm a");
            String time = formatter.format(c.getTime());

            eventTime.setText(time);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
    }
}