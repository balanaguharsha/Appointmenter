package com.example.researcher.appointmenter;

import android.content.Intent;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputEditText;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class EntryActivity extends AppCompatActivity {
    static int backButtonCount=0;




    final FirebaseFirestore db = FirebaseFirestore.getInstance();
    TextInputLayout userNameLayout,passwordLayout;
    private static final String TAG = "EntryActivity";
    Map<String, Object> user = new HashMap<>();
    TextInputEditText userName,password;
    Button entryButton,registerButton;
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_entry);
        entryButton=findViewById(R.id.entryButton);
        userName = findViewById(R.id.userName);
        password= findViewById(R.id.password);
        registerButton=findViewById(R.id.register);
        userNameLayout=findViewById(R.id.userNameLayout);
        passwordLayout=findViewById(R.id.passwordLayout);
        userName.addTextChangedListener(new EditTextListener1());
        password.addTextChangedListener(new EditTextListener2());
        registerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent toRegisterActivity = new Intent(getApplicationContext(),RegisterActivity.class);

                startActivity(toRegisterActivity);
            }
        });

        entryButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                checkingUserName();
            }
        });




    }
    public void checkingUserName(){
        final String[] name = new String[1];
        CollectionReference usersRef = db.collection("users");
        usersRef.whereEqualTo("username",userName.getText().toString())
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {

                        String TAG="RegisterActivity";
                        boolean isThere=false,in=false;

                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                if(document.get("password").toString().contentEquals(password.getText().toString())) {
                                    name[0] =document.get("name").toString();
                                    passwordLayout.setError(null);
                                    isThere = true;
                                    break;
                                }
                                in=true;
                                userNameLayout.setError(null);
                            }
                            if(isThere){
                               Intent gotoAppointment=new Intent(getApplicationContext(),BookAppointment.class);
                               gotoAppointment.putExtra("name",name[0]);
                               startActivity(gotoAppointment);
                            }
                            else if(in){
                                passwordLayout.setError("Calm down! Remember your password");
                            }
                            else{
                                userNameLayout.setError("I doubt if you have registered :|");
                            }

                        } else {
                            Toast.makeText(getApplicationContext(),"Do you have internet?",Toast.LENGTH_SHORT).show();

                        }
                    }
                });



    }
    @Override
    public void onBackPressed(){

        if(backButtonCount >= 1)
        {
            backButtonCount=0;




            Intent intent = new Intent(Intent.ACTION_MAIN);
            intent.addCategory(Intent.CATEGORY_HOME);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        }
        else
        {
            Toast.makeText(this, "Press the back button once again to close the application.", Toast.LENGTH_SHORT).show();
            backButtonCount++;
        }
    }
    private class EditTextListener1 implements TextWatcher {

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {

        }

        @Override
        public void afterTextChanged(Editable s) {
            if (s.length() == 0) {
                userNameLayout.setError(null);
            }
        }

    }
    private class EditTextListener2 implements TextWatcher {

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {

        }

        @Override
        public void afterTextChanged(Editable s) {
            if (s.length() == 0) {
                passwordLayout.setError(null);
            }
        }

    }
}
