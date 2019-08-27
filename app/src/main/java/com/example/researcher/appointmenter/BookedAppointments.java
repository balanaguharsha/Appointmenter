package com.example.researcher.appointmenter;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
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
    static int type = 0;
    private long mLastClickTime = 0;
    private long mLastClickTimeForRefresh = 0;
    myAdapter myAdapter;
    final ArrayList<String> dataset = new ArrayList<>();
    final ArrayList<String> datasetNotAccepted = new ArrayList<>();
    final ArrayList<String> datasetAccepted = new ArrayList<>();

    final ArrayList<String> docIds = new ArrayList<>();
    final ArrayList<String> docIdsSub = new ArrayList<>();
    final ArrayList<String> docIdsUnSub = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_booked_appointments);
        myAdapter = new myAdapter();

        Appointments = findViewById(R.id.Appointments);
        Appointments.setAdapter(myAdapter);
        Appointments.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, final int position, long id) {
                if(dataset.get(position).toString().charAt(0)=='F'){
                    Toast.makeText(getApplicationContext(),"Can't cancel appointments which are finished!",Toast.LENGTH_SHORT).show();
                    return true;
                }
                if (type == 0) {

                    db.collection("appointments").document(docIds.get(position))
                            .delete()
                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {

                                    if(dataset.get(position).charAt(0)=='R')
                                    Toast.makeText(getApplicationContext(), "Rejected Appointment removed from your list!", Toast.LENGTH_LONG).show();

                                    else
                                    Toast.makeText(getApplicationContext(), "Appointment deleted!\nHere on make sure you book it only when required!", Toast.LENGTH_LONG).show();
                                    onRefresh();

                                }
                            })
                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Toast.makeText(getApplicationContext(), "Appointment deletion failed :(\nDrop in a mail at appointmentsamuda@gmail.com for manual deletion!", Toast.LENGTH_LONG).show();

                                }
                            });
                } else if (type == 1) {
                    db.collection("appointments").document(docIdsSub.get(position))
                            .delete()
                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    Toast.makeText(getApplicationContext(), "Appointment deleted!\nHere on make sure you book it only when required!", Toast.LENGTH_LONG).show();

                                    onRefresh();
                                }
                            })
                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Toast.makeText(getApplicationContext(), "Appointment deletion failed :(\nDrop in a mail at appointmentsamuda@gmail.com for manual deletion!", Toast.LENGTH_LONG).show();

                                }
                            });

                } else {
                    db.collection("appointments").document(docIdsUnSub.get(position))
                            .delete()
                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    Toast.makeText(getApplicationContext(), "Appointment deleted!\nHere on make sure you book it only when required!", Toast.LENGTH_LONG).show();

                                    onRefresh();
                                }
                            })
                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Toast.makeText(getApplicationContext(), "Appointment deletion failed :(\nDrop in a mail at appointmentsamuda@gmail.com for manual deletion!", Toast.LENGTH_LONG).show();

                                }
                            });


                }
                return true;
            }
        });
        CollectionReference usersRef = db.collection("appointments");
        usersRef.whereEqualTo("username", getIntent().getStringExtra("username"))
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {

                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                docIds.add(document.getId());
                                Boolean isRejected;
                                if(document.getBoolean("isRejected")==null){
                                    isRejected=false;
                                }
                                else
                                    isRejected=document.getBoolean("isRejected");
                                if(isRejected){
                                    dataset.add("RAccepted       :\nFrom " + document.get("hour") + ":" + document.get("minute") + " for " + document.get("duration") + " minutes");
                                    continue;
                                }
                                if (Boolean.parseBoolean(document.get("Accepted").toString())) {
                                    String status = document.get("endtime").toString();
                                    if (status.contentEquals("2") || status.contentEquals( "3"))
                                        dataset.add("FAccepted       :\nFrom " + document.get("hour") + ":" + document.get("minute") + " for " + document.get("duration") + " minutes");
                                    else
                                        dataset.add("Accepted        :\nFrom " + document.get("hour") + ":" + document.get("minute") + " for " + document.get("duration") + " minutes");

                                    docIdsSub.add(docIds.get(docIds.size() - 1));
                                    datasetAccepted.add(dataset.get(dataset.size() - 1).substring(18));
                                } else {
                                    dataset.add("Not yet Accepted:\nFrom " + document.get("hour") + ":" + document.get("minute") + " for " + document.get("duration") + " minutes");
                                    docIdsUnSub.add(docIds.get(docIds.size() - 1));

                                    datasetNotAccepted.add(dataset.get(dataset.size() - 1).substring(18));
                                }
                            }
                            Appointments.setAdapter(myAdapter);

                        } else {

                        }

                    }
                });


        checkFirstRun();


    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.back) {
            if (SystemClock.elapsedRealtime() - mLastClickTime < 10000){

                return true;
            }
            mLastClickTime = SystemClock.elapsedRealtime();
            Intent in = new Intent(this, BookAppointment.class);
            in.putExtra("username", getIntent().getStringExtra("username"));
            in.putExtra("name", getIntent().getStringExtra("name"));
            in.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(in);

        } else if (item.getItemId() == R.id.Accepted) {

            type = 1;
            docIdsSub.clear();
            Appointments.setAdapter(myAdapter);
//
//           final ArrayList<String > datasetAccepted= new ArrayList<>();
//
//           int size=dataset.size();
//           for(int i=0;i<size;i++){
//               if(dataset.get(i).charAt(0)!='N'){
//                   docIdsSub.add(docIds.get(i));
//                   Log.d("test",dataset.get(i).charAt(0)+"");
//                   datasetAccepted.add(dataset.get(i).substring(18));
//               }
//           }
//
//           Appointments.setAdapter(new ArrayAdapter<>(this,android.R.layout.simple_list_item_1,datasetAccepted));
            return true;
        } else if (item.getItemId() == R.id.NotYetAccepted) {
            docIdsSub.clear();
            type = 2;
            Appointments.setAdapter(myAdapter);
            return true;
        } else if (item.getItemId() == R.id.All) {
            type = 0;
            Appointments.setAdapter(myAdapter);
            return true;
        } else if (item.getItemId() == R.id.refresh) {
            if (SystemClock.elapsedRealtime() - mLastClickTimeForRefresh < 3000){

                return true;
            }
            mLastClickTimeForRefresh = SystemClock.elapsedRealtime();
            onRefresh();
            return true;


        }
        return super.onOptionsItemSelected(item);
    }

    private void onRefresh() {
        type = 0;

        docIds.clear();
        docIdsSub.clear();
        docIdsUnSub.clear();
        datasetAccepted.clear();
        datasetNotAccepted.clear();
        dataset.clear();
        CollectionReference usersRef = db.collection("appointments");
        usersRef.whereEqualTo("username", getIntent().getStringExtra("username"))
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {

                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {

                                docIds.add(document.getId());
                                Boolean isRejected;
                                if(document.getBoolean("isRejected")==null){
                                    isRejected=false;
                                }
                                else
                                    isRejected=document.getBoolean("isRejected");
                                if(isRejected){
                                    dataset.add("RAccepted       :\nFrom " + document.get("hour") + ":" + document.get("minute") + " for " + document.get("duration") + " minutes");
                                    continue;
                                }
                                if (Boolean.parseBoolean(document.get("Accepted").toString())) {
                                    String status = document.get("endtime").toString();

                                    if (status.contentEquals("2") || status.contentEquals( "3"))
                                        dataset.add("FAccepted       :\nFrom " + document.get("hour") + ":" + document.get("minute") + " for " + document.get("duration") + " minutes");
                                    else
                                        dataset.add("Accepted        :\nFrom " + document.get("hour") + ":" + document.get("minute") + " for " + document.get("duration") + " minutes");

                                    datasetAccepted.add(dataset.get(dataset.size() - 1).substring(18));
                                    docIdsSub.add(docIds.get(docIds.size() - 1));
                                } else {
                                    dataset.add("Not yet Accepted:\nFrom " + document.get("hour") + ":" + document.get("minute") + " for " + document.get("duration") + " minutes");
                                    datasetNotAccepted.add(dataset.get(dataset.size() - 1).substring(18));
                                    docIdsUnSub.add(docIds.get(docIds.size() - 1));

                                }
                            }

                            Appointments.setAdapter(myAdapter);

                        } else {

                        }

                    }
                });


    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflate = getMenuInflater();
        inflate.inflate(R.menu.back, menu);
        return true;
    }

    public void checkFirstRun() {
        boolean isFirstRun = getSharedPreferences("PREFERENCE", MODE_PRIVATE).getBoolean("isFirstRun", true);
        if (isFirstRun) {
            AlertDialog dialog = new AlertDialog.Builder(this)
                    .setTitle("Usage directions!")
                    .setMessage("1. You can use the menu to sort out accepted and not accepted appointments\n\n2. Long press on any appointment to delete that appointment\n\n3. Use refresh button to update the status of acceptance")
                    .setPositiveButton("Ok",
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.dismiss();
                                }
                            }).create();
            dialog.setCanceledOnTouchOutside(false);
            dialog.setCancelable(false);
            dialog.show();

            getSharedPreferences("PREFERENCE", MODE_PRIVATE)
                    .edit()
                    .putBoolean("isFirstRun", false)
                    .apply();
        }
    }

    class myAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            if (type == 0)
                return dataset.size();
            else if (type == 1) {
                return datasetAccepted.size();
            } else {
                return datasetNotAccepted.size();
            }

        }

        @Override
        public Object getItem(int position) {
            return null;
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @SuppressLint("ResourceAsColor")
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View v = LayoutInflater.from(getApplicationContext()).inflate(R.layout.each_appointment, null);
            TextView title = v.findViewById(R.id.title);
            TextView full = v.findViewById(R.id.fullName);
            if (type == 0) {
                char g =dataset.get(position).charAt(0);
                boolean check = g  == 'N';
                if (check) {
                    title.setBackgroundColor(Color.RED);
                    title.setTextSize(30);
                    title.setText("Not Accepted");
                    full.setTextColor(Color.BLACK);


                } else if (g=='F') {
                    title.setBackgroundColor(Color.BLACK);
                    title.setTextSize(30);
                    title.setText("Finished");
                    full.setTextColor(Color.BLACK);
                    full.setText(dataset.get(position).substring(18));

                    return v;

                }
                else if (g=='R') {
                    title.setBackgroundColor(Color.BLACK);
                    title.setTextSize(30);
                    title.setText("Rejected");
                    full.setTextColor(Color.BLACK);
                    full.setText(dataset.get(position).substring(18));

                    return v;

                }
                else {

                    title.setBackgroundColor(Color.BLUE);
                    title.setTextSize(30);
                    title.setText("Accepted");
                    full.setTextColor(Color.BLACK);

                }

                full.setTextColor(Color.BLACK);
                full.setText(dataset.get(position).substring(18));
            } else if (type == 1) {
                title.setBackgroundColor(Color.BLUE);

                title.setText("Accepted");

                full.setText(dataset.get(position).substring(18));
                full.setTextColor(Color.BLACK);

            } else {
                title.setBackgroundColor(Color.RED);

                title.setTextSize(30);
                title.setText("Not Accepted");
                full.setText(dataset.get(position).substring(18));
                full.setTextColor(Color.BLACK);

            }
            return v;
        }
    }

}
