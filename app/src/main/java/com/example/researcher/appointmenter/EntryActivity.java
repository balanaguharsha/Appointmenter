package com.example.researcher.appointmenter;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.TextInputEditText;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.FirebaseFirestoreSettings;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class EntryActivity extends AppCompatActivity implements ForceUpdateChecker.OnUpdateNeededListener {
    static int backButtonCount=0;
    private FirebaseAuth firebaseAuth;
    public String md5(String s) {
        try {
            // Create MD5 Hash
            MessageDigest digest = java.security.MessageDigest.getInstance("MD5");
            digest.update(s.getBytes());
            byte messageDigest[] = digest.digest();

            // Create Hex String
            StringBuffer hexString = new StringBuffer();
            for (int i=0; i<messageDigest.length; i++)
                hexString.append(Integer.toHexString(0xFF & messageDigest[i]));
            return hexString.toString();

        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return "";
    }


    final FirebaseFirestore db = FirebaseFirestore.getInstance();


    TextView isMaamIn;
    TextInputLayout userNameLayout,passwordLayout;
    private static final String TAG = "EntryActivity";
    Map<String, Object> user = new HashMap<>();
    TextInputEditText userName,password;
    Button entryButton,registerButton;

    @Override
    protected void onStart() {
        super.onStart();
        FirebaseUser currentUser=firebaseAuth.getCurrentUser();
        if(currentUser!=null){
            Intent in = new Intent(this,BookAppointment.class);
            in.putExtra("name","trial");
            in.putExtra("username","something");
            startActivity(in);
            finish();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_entry);

        firebaseAuth=FirebaseAuth.getInstance();




        FirebaseFirestoreSettings settings = new FirebaseFirestoreSettings.Builder()
                .setPersistenceEnabled(false)
                .build();
        db.setFirestoreSettings(settings);
        isMaamIn=findViewById(R.id.isMaamIn);

        final DocumentReference docRef = db.collection("features").document("isMaamIn");
        docRef.addSnapshotListener(new EventListener<DocumentSnapshot>() {
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







//        onUpdateNeeded("https://drive.google.com/folderview?id=1vjX1xP3h9ncBk09ZtnKaWdCfT6_GfvwI");
        ForceUpdateChecker.with(this).onUpdateNeeded(this).check();
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
        final String[] name = new String[2];
        CollectionReference usersRef = db.collection("users");

        firebaseAuth.signInWithEmailAndPassword(userName.getText().toString(),password.getText().toString()).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if(task.isSuccessful()){
                    Toast.makeText(getApplicationContext(),"Detected as email!", Toast.LENGTH_SHORT).show();

                    Intent gotoAppointment=new Intent(getApplicationContext(),BookAppointment.class);
                    gotoAppointment.putExtra("name",name[0]);
                    gotoAppointment.putExtra("username",name[1]);
                    startActivity(gotoAppointment);
                    finish();
                }
                else{
                    Toast.makeText(getApplicationContext(),"Detected as username!", Toast.LENGTH_SHORT).show();
                }
            }
        });

        usersRef.whereEqualTo("username",userName.getText().toString())
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {

                        String TAG="RegisterActivity";
                        boolean isThere=false,in=false;
                        String hashedPassword=md5(password.getText().toString());
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                if(document.get("password").toString().contentEquals(hashedPassword)) {
                                    name[0] =document.get("name").toString();
                                    name[1]=document.get("username").toString();
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
                                gotoAppointment.putExtra("username",name[1]);
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
    @Override
    public void onUpdateNeeded(final String updateUrl) {
        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle("New version available")
                .setMessage("Please, update app to new version to continue using our app.")
                .setPositiveButton("Update",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                redirectStore(updateUrl);
                            }
                        }).setNegativeButton("No, thanks",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                finish();
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
}
