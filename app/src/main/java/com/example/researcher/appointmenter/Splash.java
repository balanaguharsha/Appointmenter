package com.example.researcher.appointmenter;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreSettings;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

public class Splash extends AppCompatActivity {





    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_NO_TITLE); //will hide the title
        getSupportActionBar().hide(); // hide the title bar



        FirebaseAuth firebaseAuth=FirebaseAuth.getInstance();

        final FirebaseFirestore db = FirebaseFirestore.getInstance();

        FirebaseUser currentUser=firebaseAuth.getCurrentUser();


        if(currentUser!=null) {
            setContentView(R.layout.activity_splash);
            final Intent in = new Intent(this, BookAppointment.class);


            CollectionReference usersRef=db.collection("users");



            usersRef.whereEqualTo("email",currentUser.getEmail())
                    .get()
                    .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<QuerySnapshot> task) {

                            if (task.isSuccessful()) {
                                for (QueryDocumentSnapshot document : task.getResult()) {

                                    in.putExtra("name",document.get("name").toString());
                                    in.putExtra("username",document.get("username").toString());
                                    startActivity(in);
                                    finish();
                                }
                            }
                        }
                    });




        }
        else{

            Intent in= new Intent(getApplicationContext(),EntryActivity.class);
            startActivity(in);
        }


    }
}
