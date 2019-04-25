package com.example.researcher.appointmenter;

import android.app.DatePickerDialog;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.TextView;

import java.text.DateFormat;
import java.util.Calendar;

public class BookAppointment extends AppCompatActivity implements DatePickerDialog.OnDateSetListener {
    TextView name;
    TextView datePicked,timePicked;
    Button date,time;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_book_appointment);
        name=findViewById(R.id.name);
        name.setText("Hey " + getIntent().getStringExtra("name").toUpperCase()+" !!!\nWelcome to our App!\nLet's book an appointment,\nAfter all that is the reason you are here, isn't it???");
        datePicked=findViewById(R.id.selectedDate);
        timePicked=findViewById(R.id.selectedTime);
        datePicked.setText("Pick a date!");
        timePicked.setText("Pick a time!");
        date=findViewById(R.id.date);
        time=findViewById(R.id.time);
        date.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DialogFragment datePicker = new datePickerFragment();
                datePicker.show(getSupportFragmentManager(),"date picker");
            }
        });
    }

    @Override
    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
        Calendar c= Calendar.getInstance();
        c.set(Calendar.YEAR,year);
        c.set(Calendar.MONTH,month);
        c.set(Calendar.DAY_OF_MONTH,dayOfMonth);
        String currentdatepicked = DateFormat.getDateInstance(DateFormat.FULL).format(c.getTime());
        datePicked.setText("DATE SELECTED:\n"+currentdatepicked);


    }
}
