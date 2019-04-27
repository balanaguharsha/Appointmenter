package com.example.researcher.appointmenter;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Arrays;

public class BookedAppointments extends AppCompatActivity {

    final FirebaseFirestore db = FirebaseFirestore.getInstance();
    ListView Appointments;

    final ArrayList<String > dataset= new ArrayList<>();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_booked_appointments);

        Appointments=findViewById(R.id.Appointments);
        CollectionReference usersRef = db.collection("appointments");
        usersRef.whereEqualTo("username",getIntent().getStringExtra("username"))
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {

                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                if(Boolean.parseBoolean(document.get("Accepted").toString()))
                                    dataset.add("Accepted:\nFrom " + document.get("hour") + ":" + document.get("minute") + " for " + document.get("duration") + " minutes");
                                else
                                    dataset.add("Not yet Accepted:\nFrom " + document.get("hour") + ":" + document.get("minute") + " for " + document.get("duration") + " minutes");

                            }
                            ArrayAdapter arrayAdapter = new ArrayAdapter(BookedAppointments.this,android.R.layout.simple_list_item_1,dataset);

                            Appointments.setAdapter(arrayAdapter);

                        } else {

                        }

                    }
                });










    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
       if(item.getItemId()==R.id.back) {
           Intent in = new Intent(this, BookAppointment.class);
           in.putExtra("username", getIntent().getStringExtra("username"));
           in.putExtra("name",getIntent().getStringExtra("name"));
           in.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
           startActivity(in);

       }
       else if(item.getItemId()==R.id.Accepted){
           final ArrayList<String > datasetAccepted= new ArrayList<>();
           int size=dataset.size();
           for(int i=0;i<size;i++){
               if(dataset.get(i).charAt(0)!='N'){
                   Log.d("test",dataset.get(i).charAt(0)+"");
                   datasetAccepted.add(dataset.get(i).substring(18));
               }
           }

           Appointments.setAdapter(new ArrayAdapter<>(this,android.R.layout.simple_list_item_1,datasetAccepted));
        return true;
       }
       else if(item.getItemId()==R.id.NotYetAccepted){
           final ArrayList<String > datasetNotAccepted= new ArrayList<>();
           int size=dataset.size();
           for(int i=0;i<size;i++){
               if(dataset.get(i).charAt(0)=='N'){
                   datasetNotAccepted.add(dataset.get(i).substring(18));
               }
           }

           Appointments.setAdapter(new ArrayAdapter<>(this,android.R.layout.simple_list_item_1,datasetNotAccepted));
        return true;
       }
       else if(item.getItemId()==R.id.All){
           Appointments.setAdapter(new ArrayAdapter<>(this,android.R.layout.simple_list_item_1,dataset));
       }
       else if(item.getItemId()==R.id.refresh){
            dataset.clear();
           CollectionReference usersRef = db.collection("appointments");
           usersRef.whereEqualTo("username",getIntent().getStringExtra("username"))
                   .get()
                   .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                       @Override
                       public void onComplete(@NonNull Task<QuerySnapshot> task) {

                           if (task.isSuccessful()) {
                               for (QueryDocumentSnapshot document : task.getResult()) {
                                   if(Boolean.parseBoolean(document.get("Accepted").toString()))
                                       dataset.add("Accepted        : From " + document.get("hour") + ":" + document.get("minute") + " for " + document.get("duration") + " minutes");
                                   else
                                       dataset.add("Not yet Accepted: From " + document.get("hour") + ":" + document.get("minute") + " for " + document.get("duration") + " minutes");

                               }
                               ArrayAdapter arrayAdapter = new ArrayAdapter(BookedAppointments.this,android.R.layout.simple_list_item_1,dataset);

                               Appointments.setAdapter(arrayAdapter);

                           } else {

                           }

                       }
                   });








       }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflate = getMenuInflater();
        inflate.inflate(R.menu.back,menu);
        return true;
    }

}
