package com.example.researcher.appointmenter;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RegisterActivity extends AppCompatActivity {
    EditText password,name,userName,email,type;
    static int wait=0;
    final FirebaseFirestore db = FirebaseFirestore.getInstance();

    Boolean userNameAvailable=false,emailValid=false,isStrongPassword=false;
    TextInputLayout emailLayout,passwordLayout;
    TextView availability;
    Button register,check;
    FirebaseAuth firebaseAuth;
    Map<String, Object> user = new HashMap<>();
    TextView isItOk;
    static boolean acceptPassword=false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        firebaseAuth=FirebaseAuth.getInstance();
        password=(EditText)findViewById(R.id.password);
        emailLayout=(TextInputLayout)findViewById(R.id.emailLayout);
        passwordLayout=findViewById(R.id.passwordLayout);
        List<String> spinnerArray =  new ArrayList<String>();
        spinnerArray.add("B.Tech. (Research project)");
        spinnerArray.add("M.Tech. (Research project)");
        spinnerArray.add("JRF");
        spinnerArray.add("Ph.D.");

        List<String> spinnerArray1 =  new ArrayList<String>();
        spinnerArray1.add("Idea formation");
        spinnerArray1.add("Core research");
        spinnerArray1.add("Paper work");
        spinnerArray1.add("Others");

        password.addTextChangedListener(new EditTextListener());
        isItOk=(TextView)findViewById(R.id.isItOk);
        isItOk.setText("");
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(
                this, android.R.layout.simple_spinner_item, spinnerArray);

        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        final Spinner sItems = (Spinner) findViewById(R.id.typeOfStudent);

        sItems.setAdapter(adapter);

        ArrayAdapter<String> adapter1 = new ArrayAdapter<String>(
                this, android.R.layout.simple_spinner_item, spinnerArray1);

        adapter1.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        final Spinner sItems1 = (Spinner) findViewById(R.id.stage);

        sItems1.setAdapter(adapter1);

        name=(EditText)findViewById(R.id.name);
        check=(Button)findViewById(R.id.check);
        availability=(TextView)findViewById(R.id.availability);
        availability.setText("");
        userName=(EditText)findViewById(R.id.userName);
        email=(EditText)findViewById(R.id.email);
        email.addTextChangedListener(new EditTextListener1());
        register=(Button)findViewById(R.id.register);



        check.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                checkingUserName();

            }
        });

        register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                wait=0;
                checkingUserName();

                Log.d("Let's see:","in main"+userNameAvailable);

                if(!userNameAvailable || !emailValid ||!isStrongPassword){
                    if(!emailValid){
                        emailLayout.setError("Oy! Its not a valid one!");
                    }
                    if(!isStrongPassword){
                        isItOk.setTextColor(Color.RED);
                        isItOk.setText("We don't like it!");



                    }
                    Toast.makeText(getApplicationContext(),"After resolving errors\n press register again!!!",Toast.LENGTH_SHORT).show();

//                    Toast.makeText(getApplicationContext(),"Registration failed! :(\n Check out errors!",Toast.LENGTH_SHORT).show();
                    return;
                }

                user.put("username", userName.getText().toString());
                user.put("name", name.getText().toString());
                user.put("email", email.getText().toString());
                user.put("type",sItems.getSelectedItem().toString());
                user.put("stage",sItems1.getSelectedItem().toString());

                user.put("password", md5(password.getText().toString()));


                firebaseAuth.createUserWithEmailAndPassword(email.getText().toString(),password.getText().toString())
                        .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                if(task.isSuccessful()){
                                    db.collection("users")
                                            .add(user)
                                            .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                                                @Override
                                                public void onSuccess(DocumentReference documentReference) {
                                                    Toast.makeText(getApplicationContext(),"Success",Toast.LENGTH_SHORT).show();

                                                }

                                            })
                                            .addOnFailureListener(new OnFailureListener() {
                                                @Override
                                                public void onFailure(@NonNull Exception e) {


                                                    Toast.makeText(getApplicationContext(),"Failure:"+e.toString(),Toast.LENGTH_SHORT).show();
                                                }
                                            });
                                    Intent gotoLogin = new Intent(getApplicationContext(),EntryActivity.class);
                                    startActivity(gotoLogin);
                                    finish();
                                }
                                else{
                                    Toast.makeText(getApplicationContext(),"Error occurred:(\nPlease try again!",Toast.LENGTH_SHORT).show();
                                    return;
                                }

                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                emailLayout.setError("If this is yours, you are already in!");
                                Log.d("Error",e.toString());
                            }
                        })
                ;

            }
        });







    }
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
    public void checkingUserName(){
        CollectionReference usersRef = db.collection("users");
        usersRef.whereEqualTo("username",userName.getText().toString())
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {

                        String TAG="RegisterActivity";
                        boolean isThere=false;
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                isThere=true;
                                break;

                            }
                            if(isThere){
                                userNameAvailable=false;
                                availability.setTextColor(Color.RED);
                                availability.setText("Oops! Someone had it already!");
                            }
                            else{
                                availability.setTextColor(Color.GREEN);
                                availability.setText("You can have it!");
                                userNameAvailable=true;
                            }

                        } else {
                            Toast.makeText(getApplicationContext(),"Do you have internet?",Toast.LENGTH_SHORT).show();

                        }
                    }
                });

       Log.d("Let's see:","in function"+userNameAvailable);

    }
    private class EditTextListener implements TextWatcher {

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {

        }

        @Override
        public void afterTextChanged(Editable s) {
            if(s.length()==0){
                isItOk.setText("");
                passwordLayout.setError(null);
                return;
            }
            if(s.length()<6){
                passwordLayout.setError("Keep typing! minimum 6 characters!");
                passwordLayout.setErrorTextColor(ColorStateList.valueOf(Color.BLUE));
                return;
            }
            else{
                passwordLayout.setError(null);
            }
            if(s.length()>0){
                boolean isdig=false,ischar=false,iscap=false;
                String g=password.getText().toString();
                char arr[]=g.toCharArray();
                for(int i=0;i<arr.length;i++){
                    if(!isdig && arr[i]<='9' && arr[i]>='0'){
                        isdig=true;
                    }
                    else if(!iscap && arr[i]<='Z' && arr[i]>='A'){
                        iscap=true;
                    }
                    else if (!ischar && ((arr[i]<=47 && arr[i]>=33)||(arr[i]<=64 && arr[i]>=58))){
                        ischar=true;
                    }
                    if(iscap && ischar && isdig){
                        isStrongPassword=true;
                        isItOk.setTextColor(Color.GREEN);
                        isItOk.setText("We shall accept it!");
                        break;
                    }
                }
                if(!iscap || !ischar || !isdig){
                    isStrongPassword=false;
                    isItOk.setTextColor(Color.RED);
                    isItOk.setText("We don't like it!");
                }

            }

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
            if(s.length()==0) {
                emailValid=false;
                emailLayout.setError(null);
                return;
            }
            Pattern pattern = Pattern.compile("[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,4}");
            Matcher mat = pattern.matcher(email.getText().toString());

            if(!mat.matches()){
                emailValid=false;
                emailLayout.setError("Oy! Its not a valid one!");
            }
            else{
                emailValid=true;
                emailLayout.setError(null);
            }
        }

        }
    }

