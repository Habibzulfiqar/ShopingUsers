package com.example.shopingusers.Activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.shopingusers.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;

public class SignUp extends AppCompatActivity {
    private EditText name,email,contact,password;
    private ImageView register;
    private ProgressDialog progressDialog;
    private FirebaseAuth firebaseAuth;
    private DatabaseReference databaseReference;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);


        progressDialog = new ProgressDialog(this);
        firebaseAuth = FirebaseAuth.getInstance();
        databaseReference = FirebaseDatabase.getInstance().getReference("Users");

        name = findViewById(R.id.editText);
        email = findViewById(R.id.editText2);
        contact = findViewById(R.id.contact);
        password = findViewById(R.id.passwordText);
        register = findViewById(R.id.registers);


        register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String Name = name.getText().toString();
                String Email = email.getText().toString();
                String Contact = contact.getText().toString();
                String Password = password.getText().toString();
                String namepattern = "[a-zA-Z -]{3,65}";

                if(Name.isEmpty() || Email.isEmpty() || Contact.isEmpty() || Password.isEmpty())
                {
                    Toast.makeText(SignUp.this, "All fields required", Toast.LENGTH_SHORT).show();
                    return;
                }
                if(!Name.matches(namepattern))
                {
                    Toast.makeText(SignUp.this, "Please Enter correct name", Toast.LENGTH_SHORT).show();
                    return;
                }
                if(Contact.length()<10 || Contact.length()>14)
                {
                    Toast.makeText(SignUp.this, "Contact minimum length is 10 characters and maximum length is 14 characters", Toast.LENGTH_SHORT).show();
                    return;
                }
                if(Password.length()<6)
                {
                    Toast.makeText(SignUp.this, "Password minimum length is 6th characters", Toast.LENGTH_SHORT).show();
                    return;
                }
                else
                {
                    HashMap<String,Object> hashMap = new HashMap<>();
                    hashMap.put("Name",Name);
                    hashMap.put("Email",Email);
                    hashMap.put("Contact",Contact);
                    hashMap.put("Password",Password);
                    hashMap.put("userType","User");
                    progressDialog.setTitle("Please Wait");
                    progressDialog.setMessage("until registration");
                    progressDialog.setCanceledOnTouchOutside(false);
                    progressDialog.show();

                    firebaseAuth.createUserWithEmailAndPassword(Email,Password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if(task.isSuccessful())
                            {
                                progressDialog.dismiss();
                                String userId = firebaseAuth.getCurrentUser().getUid().toString();
                                databaseReference.child(userId).updateChildren(hashMap);

                                SharedPreferences sharedPreferences = getSharedPreferences("ShoppingRef",MODE_PRIVATE);
                                SharedPreferences.Editor editor = sharedPreferences.edit();
                                editor.putString("userId",userId);
                                editor.putString("userType","user");
                                editor.commit();
                                editor.apply();
                                startActivity(new Intent(SignUp.this,MainActivity.class));
                                finish();
                            }
                            else
                            {
                                progressDialog.dismiss();
                                Toast.makeText(SignUp.this, "Please Enter correct information", Toast.LENGTH_SHORT).show();
                            }
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            progressDialog.dismiss();
                            Toast.makeText(SignUp.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });

                }
            }
        });

        findViewById(R.id.already).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(SignUp.this,Login.class));
            }
        });
    }
}