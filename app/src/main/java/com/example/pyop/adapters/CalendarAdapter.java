package com.example.pyop.adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.pyop.CalendarEventDetail;
import com.example.pyop.R;
import com.example.pyop.models.CalendarEvent;
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class CalendarAdapter extends FirestoreRecyclerAdapter<CalendarEvent, CalendarAdapter.EventViewHolder> {

    Context context;
    public CalendarAdapter(@NonNull FirestoreRecyclerOptions<CalendarEvent> options, Context context) {
        super(options);
        this.context = context;
    }

    @NonNull
    @Override
    public EventViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.calendar_event_recycler_item,parent,false);
        return new EventViewHolder(view);
    }

    @Override
    protected void onBindViewHolder(@NonNull EventViewHolder holder, int position, @NonNull CalendarEvent calendarEvent) {

        holder.eventTitle.setText(calendarEvent.getEventTitle());
        holder.eventDescription.setText(calendarEvent.getEventDescription());
        try {
            Date date = new SimpleDateFormat("EEE, yyyy-MM-dd").parse(calendarEvent.getStartDate());
            holder.eventDay.setText(date.toString().split(" ")[0]);
            holder.eventMonth.setText(date.toString().split(" ")[1]);
            holder.eventDate.setText(date.toString().split(" ")[2]);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        holder.eventTime.setText(calendarEvent.getEventTime());

        holder.itemView.setOnClickListener((v) -> {
            Intent intent = new Intent(context, CalendarEventDetail.class);
            intent.putExtra("eventTitle",calendarEvent.getEventTitle());
            intent.putExtra("eventDescription",calendarEvent.getEventDescription());
            intent.putExtra("eventStartDate",calendarEvent.getStartDate());
            intent.putExtra("eventEndDate",calendarEvent.getEndDate());
            intent.putExtra("eventTime",calendarEvent.getEventTime());
            intent.putExtra("startDateIndex",calendarEvent.getStartDateIndex().toString());
            String docId = this.getSnapshots().getSnapshot(position).getId();
            intent.putExtra("docId",docId);
            context.startActivity(intent);
        });
    }

    class EventViewHolder extends RecyclerView.ViewHolder{
        TextView eventDay, eventDate, eventMonth, eventTitle, eventDescription, eventTime;

        public EventViewHolder(@NonNull View itemView) {
            super(itemView);
            eventTitle = itemView.findViewById(R.id.eventTitle);
            eventDescription = itemView.findViewById(R.id.eventDescription);
            eventDay = itemView.findViewById(R.id.eventDay);
            eventDate = itemView.findViewById(R.id.eventDate);
            eventMonth = itemView.findViewById(R.id.eventMonth);
            eventTime = itemView.findViewById(R.id.eventTime);
        }
    }
}
