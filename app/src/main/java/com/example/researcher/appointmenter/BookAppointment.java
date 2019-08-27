package com.example.researcher.appointmenter;

import android.app.DatePickerDialog;
import android.app.DownloadManager;
import android.app.ProgressDialog;
import android.app.TimePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.design.widget.TextInputEditText;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import java.util.ArrayList;
import java.util.Calendar;

import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONArray;

import java.sql.Time;
import java.text.DateFormat;
import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Collections;



public class BookAppointment extends AppCompatActivity implements DatePickerDialog.OnDateSetListener,TimePickerDialog.OnTimeSetListener , ForceUpdateChecker.OnUpdateNeededListener {
    TextView name;
    TextInputLayout durationLayout;
    private long mLastClickTime = 0;




    static Calendar dateGiven;
    TextView datePicked,timePicked;
    TextInputEditText duration;
    FirebaseAuth mAuth;
    static Time timeGiven;
    static int gYear,gMonth,gDay;
    final FirebaseFirestore db = FirebaseFirestore.getInstance();
    Map<String, Object> appointment = new HashMap<>();

    Button date,time,check;
    String userName="";

    Date selected;

    Date dateAfter;

    Spinner typeOfMeeting;
    TextView isMaamIn;
    TextView isSlot;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_book_appointment);
        ForceUpdateChecker.with(this).onUpdateNeeded(this).check();
        checkFirstRun();
        mAuth=FirebaseAuth.getInstance();
        typeOfMeeting=findViewById(R.id.typeOfMeeting);

        List<String> spinnerArray1 =  new ArrayList<String>();
        spinnerArray1.add("Individual meeting");
        spinnerArray1.add("Group meeting");
        spinnerArray1.add("Assignment Discussion");
        spinnerArray1.add("Staff meeting");

        //spinnerArray1.add("Research meeting");
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(
                this, android.R.layout.simple_spinner_item, spinnerArray1);

        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

//
        typeOfMeeting.setAdapter(adapter);
//        Button b=findViewById(R.id.count);
//        b.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                db.collection("appointments").get()
//                        .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
//                            @Override
//                            public void onComplete(@NonNull Task<QuerySnapshot> task) {
//
//                                if (task.isSuccessful()) {
//                                    int y=0;
//                                    for (QueryDocumentSnapshot document : task.getResult()) {
//
//                                       y++;
//                                    }
//                                    Log.d("Count:",""+y);
//
//
//                                } else {
//                                    Toast.makeText(getApplicationContext(),"Do you have internet?",Toast.LENGTH_SHORT).show();
//
//                                }
//                            }
//                        });
//
//
//
//            }
//        });


        userName=getIntent().getStringExtra("username");
        name=findViewById(R.id.name);
        durationLayout=findViewById(R.id.durationLayout);
        duration=findViewById(R.id.duration);
        Calendar c = Calendar.getInstance();
        String wish="";
        int timeOfDay = c.get(Calendar.HOUR_OF_DAY);

        if(timeOfDay >= 0 && timeOfDay < 12){
            wish="Good Morning";
        }else if(timeOfDay >= 12 && timeOfDay < 16){
            wish="Good Afternoon";
        }else if(timeOfDay >= 16 && timeOfDay < 21){
            wish="Good Evening";
        }else if(timeOfDay >= 21 && timeOfDay < 24){
            wish="Good Night";
        }
        String nameActual=getIntent().getStringExtra("name");
        String temp=nameActual.charAt(0)+"";
        temp=temp.toUpperCase();
        nameActual=temp+nameActual.substring(1);
        name.setText(wish+",\n "+nameActual);
        datePicked=findViewById(R.id.selectedDate);
        timePicked=findViewById(R.id.selectedTime);
        check=findViewById(R.id.check);
        datePicked.setText("Pick a date!");
        timePicked.setText("Pick a time!");
        date=findViewById(R.id.date);
        time=findViewById(R.id.time);
        isMaamIn=findViewById(R.id.isMaamIn);
        isSlot=findViewById(R.id.isSlot);

        final DocumentReference docRef = db.collection("features").document("isMaamIn");
        docRef.addSnapshotListener(new EventListener<DocumentSnapshot>() {
            private static final String TAG = "BookActivity";

            @Override
            public void onEvent(@Nullable DocumentSnapshot snapshot,
                                @Nullable FirebaseFirestoreException e) {
                if (e != null) {
                    Log.w(TAG, "Listen failed.", e);

                    return;
                }

                if (snapshot != null && snapshot.exists()) {
                    boolean isIn=Boolean.parseBoolean(snapshot.get("isMaamIn").toString());
                    if(isIn){
                        isMaamIn.setTextColor(Color.GREEN);
                        isMaamIn.setText("Status:\nMa'am is in her cabin!");


                    }
                    else{
                        isMaamIn.setTextColor(Color.RED);
                        isMaamIn.setText("Status:\nMa'am is not in her cabin\n(Her car's presence can give a clue!)");
                    }

                } else {
                    Log.d(TAG, "Current data: null");
                }
            }
        });


        Date d1=new Date();
        Calendar cal11 = Calendar.getInstance();
        cal11.setTime(d1);
        int year = cal11.get(Calendar.YEAR);
        int month = cal11.get(Calendar.MONTH);
        int day = cal11.get(Calendar.DAY_OF_MONTH);

        Log.d("date",year+"-"+(month+1)+"-"+day);

        final RequestQueue queue1 = Volley.newRequestQueue(this);
        //https://bookcalender.herokuapp.com/getSlot
        //http://192.168.43.82:5000/getSlot
        final String url1 = "https://bookcalender.herokuapp.com/getSlot"; // your URL

        queue1.start();

        HashMap<String, String> params1 = new HashMap<String,String>();
        params1.put("date",(year+"-"+(month+1)+"-"+day).toString());

        params1.put("time",(year+"-"+(month+1)+"-"+day)+" "+"00:40");

        params1.put("time1",(year+"-"+(month+1)+"-"+day)+" "+"23:40");

        JsonObjectRequest jsObjRequest1 = new
                JsonObjectRequest(Request.Method.POST,
                url1,
                new JSONObject(params1),
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            String str=response.getString("slot");
                            String str1[]=str.split("&");
                            isSlot.setTextColor(Color.RED);
                            isSlot.setText("Today Ma'am Busy Hours :-\n\n");
                            for(int i=0;i<str1.length;i++){
                                Log.d("backend", str1[i]);
                                isSlot.append(str1[i]+"\n");
                            }
                            Log.d("backend", str);


                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d("back",error.toString()+"     "+error.getMessage());

            }

        });
        jsObjRequest1.setRetryPolicy(new DefaultRetryPolicy(
                10000,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

        queue1.add(jsObjRequest1);



























        duration.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                durationLayout.setError(null);
            }
        });


        final RequestQueue queue = Volley.newRequestQueue(this);
        //https://bookcalender.herokuapp.com/checkCal
        //http://192.168.43.82:5000/checkCal
        final String url = "https://bookcalender.herokuapp.com/checkCal"; // your URL


        check.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (SystemClock.elapsedRealtime() - mLastClickTime < 10000){
                    return;
                }
                mLastClickTime = SystemClock.elapsedRealtime();
                String tempDuration=duration.getText().toString();
                if(datePicked.getText().toString().equals("Pick a date!")){
                    Toast.makeText(getApplicationContext(),"Sadly, We don't know the date",Toast.LENGTH_SHORT).show();
                    return;
                }
                if(timePicked.getText().toString().equals("Pick a time!")){
                    Toast.makeText(getApplicationContext(),"Sadly, We don't know the time",Toast.LENGTH_SHORT).show();
                    return;
                }
                if(tempDuration.isEmpty()){
                    tempDuration="0";
                }
                final int durationTime=Integer.parseInt(tempDuration);
                if(durationTime<=0){
                    durationLayout.setError("Enter a valid duration!");
                    return;

                }



                queue.start();

                HashMap<String, String> params = new HashMap<String,String>();
                // the entered data as the body.

                Calendar now1=Calendar.getInstance();
                now1.set(Calendar.YEAR,gYear);
                now1.set(Calendar.MONTH,gMonth);
                now1.set(Calendar.DAY_OF_MONTH,gDay);
                now1.set(Calendar.MINUTE,timeGiven.getMinutes());
                now1.set(Calendar.HOUR_OF_DAY,timeGiven.getHours());
                selected=now1.getTime();


                now1.add(Calendar.MINUTE,durationTime);
                dateAfter=now1.getTime();

                Timestamp ts1=new Timestamp(selected);
                ts1.toDate();
                Timestamp ts2=new Timestamp(dateAfter);
                ts2.toDate();

                params.put("date",(gYear+"-"+(gMonth+1)+"-"+gDay).toString());

//                params.put("StartTime",(gYear+"-"+(gMonth+1)+"-"+gDay)+" "+timeGiven.getHours()+":"+timeGiven.getMinutes());
//
//                params.put("EndTime",(gYear+"-"+(gMonth+1)+"-"+gDay)+" "+now1.get(Calendar.HOUR_OF_DAY)+":"+now1.get(Calendar.MINUTE));

                params.put("StartTime",(ts1.toDate()).toString());

                params.put("EndTime", (ts2.toDate()).toString());

                params.put("time",(gYear+"-"+(gMonth+1)+"-"+gDay)+" "+"00:40");

                params.put("time1",(gYear+"-"+(gMonth+1)+"-"+gDay)+" "+"23:40");

                Log.d("backend","----------------------------------------------------------------------");
                Log.d("backend",(gYear+"-"+gMonth+"-"+gDay).toString());
                Log.d("backend",(new Timestamp(selected)).toString());
                Log.d("backend",(gYear+"-"+gMonth+"-"+gDay)+" "+now1.get(Calendar.HOUR_OF_DAY)+":"+now1.get(Calendar.MINUTE));
                Log.d("backend",(gYear+"-"+gMonth+"-"+gDay)+" "+"05:40");
                Log.d("backend",(gYear+"-"+gMonth+"-"+gDay)+" "+"23:40");

                JsonObjectRequest jsObjRequest = new
                        JsonObjectRequest(Request.Method.POST,
                        url,
                        new JSONObject(params),
                        new Response.Listener<JSONObject>() {
                            @Override
                            public void onResponse(JSONObject response) {
                                try {
                                    String str=response.getString("status");
                                    Log.d("backend", str);
                                    if(str.equalsIgnoreCase("sucess")){



                                        appointment.put("typeOfMeeting",typeOfMeeting.getSelectedItem().toString());
                                        appointment.put("hour",timeGiven.getHours());
                                        appointment.put("minute",timeGiven.getMinutes());
                                        appointment.put("day",gDay);
                                        appointment.put("month",gMonth);
                                        appointment.put("year",gYear);
                                        appointment.put("username",userName);
                                        appointment.put("name",getIntent().getStringExtra("name"));
                                        appointment.put("endtime","");
                                        appointment.put("StartedAt",null);
                                        appointment.put("isEnded",false);
                                        appointment.put("EndedAt",null);
                                        appointment.put("isRejected",false);

                                        appointment.put("duration",durationTime);
                                        appointment.put("Accepted",false);
                                        Calendar now=Calendar.getInstance();
                                        now.set(Calendar.YEAR,gYear);
                                        now.set(Calendar.MONTH,gMonth);
                                        now.set(Calendar.DAY_OF_MONTH,gDay);
                                        now.set(Calendar.MINUTE,timeGiven.getMinutes());
                                        now.set(Calendar.HOUR_OF_DAY,timeGiven.getHours());
                                        selected=now.getTime();
                                        appointment.put("StartTime",new Timestamp(selected));


                                        now.add(Calendar.MINUTE,durationTime);
                                        dateAfter=now.getTime();
                                        appointment.put("EndTime",new Timestamp(dateAfter));

                                        Log.d("hey",selected.toString()+",,,"+dateAfter.toString()+"********8" +
                                                "");
                                        Date dateobj = new Date();
                                        if(selected.before(dateobj)){
                                            Log.d("Hey",dateobj.toString()+",,,"+selected.toString());
                                            Toast.makeText(getApplicationContext(),"Time and tide waits for none!\nBook an appointment after current date and time...",Toast.LENGTH_LONG).show();
                                            return;
                                        }



                                        db.collection("appointments")
                                                .whereEqualTo("Accepted",true)
                                                .get()
                                                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                                    @Override
                                                    public void onComplete(@NonNull Task<QuerySnapshot> task) {

                                                        if (task.isSuccessful()) {

                                                            int y=0;
                                                            for (QueryDocumentSnapshot document : task.getResult()) {
//                                        Log.d("hey","Something");
//                                        y++;
                                                                Date dateCurr;
                                                                Date endDate;
//
                                                                try {

                                                                    Timestamp dateStamp = (Timestamp) document.get("StartTime");
                                                                    dateCurr = dateStamp.toDate();

                                                                    dateStamp = (Timestamp) document.get("EndTime");
                                                                    endDate = dateStamp.toDate();
                                                                }
                                                                catch(Exception e){
                                                                    Calendar now1=Calendar.getInstance();
                                                                    now1.set(Calendar.YEAR,Integer.parseInt(document.get("year").toString()));
                                                                    now1.set(Calendar.MONTH,Integer.parseInt(document.get("month").toString()));
                                                                    now1.set(Calendar.DAY_OF_MONTH,Integer.parseInt(document.get("day").toString()));
                                                                    now1.set(Calendar.MINUTE,Integer.parseInt(document.get("minute").toString()));
                                                                    now1.set(Calendar.HOUR_OF_DAY,Integer.parseInt(document.get("hour").toString()));
                                                                    dateCurr=now1.getTime();
                                                                    now1.add(Calendar.MINUTE,Integer.parseInt(document.get("duration").toString()));
                                                                    endDate=now1.getTime();
                                                                }





                                                                Log.d("hey","oyyy\n"+selected.toString()+"\n"+dateCurr.toString()+"\n"+dateAfter.toString()+"\n"+endDate.toString());


                                                                if((dateCurr.before(selected) && selected.before(endDate)) || (dateCurr.before(dateAfter) && dateAfter.before(endDate) ) || (selected.before(dateCurr) && dateAfter.after(endDate) ) ){
                                                                    Toast.makeText(getApplicationContext(),"Slot is already booked! Pick an another slot which is after "+endDate.getHours()+":"+endDate.getMinutes(),Toast.LENGTH_SHORT).show();
                                                                    return;
                                                                }

                                                            }
                                                            Log.d("Count in check:",""+y);
                                                            db.collection("appointments")
                                                                    .add(appointment)
                                                                    .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                                                                        @Override
                                                                        public void onSuccess(DocumentReference documentReference) {
                                                                            Log.d("hey","in adding");
                                                                            Toast.makeText(getApplicationContext(),"Success",Toast.LENGTH_SHORT).show();
                                                                            timePicked.setText("Pick a time!");
                                                                            datePicked.setText("Pick a date!");
                                                                            duration.setText("");

                                                                        }

                                                                    })
                                                                    .addOnFailureListener(new OnFailureListener() {
                                                                        @Override
                                                                        public void onFailure(@NonNull Exception e) {

                                                                            Toast.makeText(getApplicationContext(),"Failure:"+e.toString(),Toast.LENGTH_SHORT).show();
                                                                        }
                                                                    });


                                                        } else {
                                                            Toast.makeText(getApplicationContext(),"Do you have internet?",Toast.LENGTH_SHORT).show();

                                                        }
                                                    }
                                                });





                                    }

                                    else {

                                        Toast.makeText(getApplicationContext(),"Maam is busy during that slot!\n Pick an another slot",Toast.LENGTH_LONG).show();
                                        return;

                                    }


                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            }
                        }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.d("back",error.toString()+"     "+error.getMessage());

                    }

                });
                jsObjRequest.setRetryPolicy(new DefaultRetryPolicy(
                        10000,
                        DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                        DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

                queue.add(jsObjRequest);




            }

        });
        date.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DialogFragment datePicker = new datePickerFragment();
                datePicker.show(getSupportFragmentManager(),"date picker");
            }
        });
        time.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DialogFragment timePicker = new TimePickerFragment();
                timePicker.show(getSupportFragmentManager(),"Time Picker");
            }
        });
    }

    @Override
    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
        dateGiven= Calendar.getInstance();
        dateGiven.set(Calendar.YEAR,year);
        gDay=dayOfMonth;
        gYear=year;
        gMonth=month;
        dateGiven.set(Calendar.MONTH,month);
        dateGiven.set(Calendar.DAY_OF_MONTH,dayOfMonth);
        final String currentdatepicked = DateFormat.getDateInstance(DateFormat.FULL).format(dateGiven.getTime());
        datePicked.setText("Selected date:\n"+currentdatepicked);


        Log.d("date",year+"-"+(month+1)+"-"+dayOfMonth);

        final RequestQueue queue2 = Volley.newRequestQueue(this);
        //https://bookcalender.herokuapp.com/getSlot
        //http://192.168.43.82:5000/getSlot
        final String url2 = "https://bookcalender.herokuapp.com/getSlot"; // your URL

        queue2.start();

        HashMap<String, String> params2 = new HashMap<String,String>();
        params2.put("date",(year+"-"+(month+1)+"-"+dayOfMonth).toString());

        params2.put("time",(year+"-"+(month+1)+"-"+dayOfMonth)+" "+"00:40");

        params2.put("time1",(year+"-"+(month+1)+"-"+dayOfMonth)+" "+"23:40");

        JsonObjectRequest jsObjRequest2 = new
                JsonObjectRequest(Request.Method.POST,
                url2,
                new JSONObject(params2),
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            String str=response.getString("slot");
                            String str1[]=str.split("&");
                            isSlot.setTextColor(Color.RED);
                            isSlot.setText("On "+currentdatepicked+" Ma'am Busy Hours :-\n\n");
                            for(int i=0;i<str1.length;i++){
                                Log.d("backend", str1[i]);
                                isSlot.append(str1[i]+"\n");
                            }
                            Log.d("backend", str);


                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d("back",error.toString()+"     "+error.getMessage());

            }

        });
        jsObjRequest2.setRetryPolicy(new DefaultRetryPolicy(
                10000,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

        queue2.add(jsObjRequest2);













    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId()==R.id.logout){
            Intent in=new Intent(this,EntryActivity.class);
            mAuth.signOut();
            Toast.makeText(this,"See you soon...\n(When you want to meet ma'am :p)",Toast.LENGTH_SHORT).show();
            startActivity(in);

        }
        else if(item.getItemId()==R.id.appointments){
            Intent in=new Intent(this,BookedAppointments.class);
            in.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            in.putExtra("username",userName);
            in.putExtra("name",getIntent().getStringExtra("name"));
            startActivity(in);


        }
        else if(item.getItemId()==R.id.request){
            Intent in=new Intent(this,request_feature.class);
            in.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            in.putExtra("username",userName);
            in.putExtra("name",getIntent().getStringExtra("name"));
            startActivity(in);


        }
        else if(item.getItemId()==R.id.editProfile){
            Toast.makeText(getApplicationContext(),"In development!",Toast.LENGTH_SHORT).show();
            return true;

//            Intent in=new Intent(this,EditProfile.class);
//            in.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
//            in.putExtra("username",userName);
//            in.putExtra("name",getIntent().getStringExtra("name"));
//            startActivity(in);

        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflate = getMenuInflater();
        inflate.inflate(R.menu.logout,menu);
        return true;
    }

    @Override
    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
        timeGiven = new Time(hourOfDay,minute,0);
        Format formatter;
        formatter = new SimpleDateFormat("h:mm a");
        timePicked.setText("Selected time:\n"+formatter.format(timeGiven));
    }

    @Override
    public void onUpdateNeeded(final String updateUrl) {
        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle("Logged in, but...")
                .setMessage("Please, update app to new version to continue using our app.")
                .setPositiveButton("Get me that!",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                redirectStore(updateUrl);
                            }
                        }).setNegativeButton("I better quit this app!",
                        new DialogInterface.OnClickListener() {
                            @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                finishAffinity();
                            }
                        }).create();
        dialog.setCanceledOnTouchOutside(false);
        dialog.setCancelable(false);
        dialog.show();
    }

    private void redirectStore(String updateUrl) {
        final Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(updateUrl));
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }
    @RequiresApi(api = Build.VERSION_CODES.GINGERBREAD)
    public void checkFirstRun() {
        boolean isFirstRun = getSharedPreferences("PREFERENCE", MODE_PRIVATE).getBoolean("ver1_6_2", true);
        if (isFirstRun) {
            AlertDialog dialog = new AlertDialog.Builder(this)
                    .setTitle("What's new!")
                    .setMessage("1. Now you can request the slot by checking the busy hours after selecting the date!\n")
                    .setPositiveButton("That's Ok!",
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    Toast.makeText(getApplicationContext(),"Thanks! :)",Toast.LENGTH_SHORT).show();
                                    dialog.dismiss();
                                }
                            })
//                    .setNegativeButton("Can you send a mail on rejection?", new DialogInterface.OnClickListener() {
//                        @Override
//                        public void onClick(DialogInterface dialog, int which) {
//                            Toast.makeText(getApplicationContext(),"Please use the demand feature option!",Toast.LENGTH_LONG).show();
//                            dialog.dismiss();
//                        }
//                    })

                    .create()


                    ;

            dialog.setCanceledOnTouchOutside(false);
            dialog.setCancelable(false);
            dialog.show();

            getSharedPreferences("PREFERENCE", MODE_PRIVATE)
                    .edit()
                    .putBoolean("ver1_6_2", false)
                    .apply();
        }
    }


}
