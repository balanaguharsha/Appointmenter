package com.example.researcher.appointmenter;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;

public class AppointmentsList extends RecyclerView.Adapter<AppointmentsList.AppointmentsViewHolder> {

    String data[];
    public AppointmentsList(String[] data){
        this.data=data;
    }
    @NonNull
    @Override
    public AppointmentsViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        return null;
    }

    @Override
    public void onBindViewHolder(@NonNull AppointmentsViewHolder appointmentsViewHolder, int i) {

    }

    @Override
    public int getItemCount() {
        return data.length;
    }

    public class AppointmentsViewHolder extends RecyclerView.ViewHolder{

        public AppointmentsViewHolder(@NonNull View itemView) {
            super(itemView);
        }
    }
}
