package com.example.researcher.appointmenter;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputEditText;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class request_feature extends AppCompatActivity {
    TextInputEditText feature;
    TextInputLayout requestLayout;
    Map<String, Object> featurereq = new HashMap<>();
    final FirebaseFirestore db = FirebaseFirestore.getInstance();

    Button request;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_request_feature);
        final Intent gotoAppointment=new Intent(getApplicationContext(),BookAppointment.class);

        request=findViewById(R.id.request);
        requestLayout=findViewById(R.id.requestLayout);
        feature=findViewById(R.id.feature);
        feature.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                requestLayout.setError(null);
            }
        });
        request.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String featureRequested=feature.getText().toString();
                if(featureRequested.isEmpty()){
                    requestLayout.setError("Demand a feature! It cannot be empty!");
                    return;
                }
                featurereq.put("Feature",featureRequested);
                featurereq.put("Requested by",getIntent().getStringExtra("username"));
                featurereq.put("name",getIntent().getStringExtra("name"));

                db.collection("featuresRequested")
                        .add(featurereq)
                        .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                            @Override
                            public void onSuccess(DocumentReference documentReference) {
                                Toast.makeText(getApplicationContext(),"We shall still take it as a request, not a demand :)",Toast.LENGTH_LONG).show();
                                gotoAppointment.putExtra("name",getIntent().getStringExtra("name"));
                                gotoAppointment.putExtra("username",getIntent().getStringExtra("username"));
                                startActivity(gotoAppointment);
                                finish();
                            }

                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {


                                Toast.makeText(getApplicationContext(),"Oops! It failed!",Toast.LENGTH_SHORT).show();
                            }
                        });

            }
        });
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.back) {
            Intent in = new Intent(this, BookAppointment.class);
            in.putExtra("username", getIntent().getStringExtra("username"));
            in.putExtra("name", getIntent().getStringExtra("name"));
            in.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(in);
            finish();

        }



        return super.onOptionsItemSelected(item);
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflate = getMenuInflater();
        inflate.inflate(R.menu.backfromdemand, menu);
        return true;
    }

}
