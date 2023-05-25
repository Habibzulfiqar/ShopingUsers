package com.example.shopingusers.Fragments;

import static android.app.Activity.RESULT_OK;
import static android.content.Context.MODE_PRIVATE;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.shopingusers.R;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;


public class Settingfragment extends Fragment {
    private EditText name,email,contact,password,city,address;
    private Button updateProfile;
    private ProgressDialog progressDialog;
    private FirebaseAuth firebaseAuth;
    private DatabaseReference databaseReference;
    private CircleImageView circleImageView;


    private Uri imageUri;
    StorageReference mStorageRef;
    private String ImageSelect=null;
    public Settingfragment() {
        // Required empty public constructor
    }



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_settingfragment, container, false);

    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        progressDialog = new ProgressDialog(getActivity());
        progressDialog.setTitle("Please Wait");
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.show();
        firebaseAuth = FirebaseAuth.getInstance();
        databaseReference = FirebaseDatabase.getInstance().getReference("Users");

        name = view.findViewById(R.id.name);
        email = view.findViewById(R.id.email);
        contact = view.findViewById(R.id.contact);
        password = view.findViewById(R.id.passwordText);
        updateProfile = view.findViewById(R.id.updateProfile);
        city = view.findViewById(R.id.city);
        address = view.findViewById(R.id.address);
        circleImageView = view.findViewById(R.id.profileImage);


        mStorageRef = FirebaseStorage.getInstance().getReference();
        databaseReference = FirebaseDatabase.getInstance().getReference("Users");

        SharedPreferences sharedPreferences = getActivity().getSharedPreferences("ShoppingRef",MODE_PRIVATE);
        String userId = sharedPreferences.getString("userId","");

        circleImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_PICK,android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(intent, 100);
            }
        });


        updateProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String Name = name.getText().toString();
                String Email = email.getText().toString();
                String Contact = contact.getText().toString();
                String City = city.getText().toString();
                String Address = address.getText().toString();
                String Password = password.getText().toString();
                String namepattern = "[a-zA-Z -]{3,65}";

                if(Name.isEmpty() || Email.isEmpty() || Contact.isEmpty()  || Password.isEmpty() || City.isEmpty() || Address.isEmpty())
                {
                    Toast.makeText(getActivity(), "All fields required", Toast.LENGTH_SHORT).show();
                    return;
                }
                if(!Name.matches(namepattern))
                {
                    Toast.makeText(getActivity(), "Please Enter correct name", Toast.LENGTH_SHORT).show();
                    return;
                }
                if(Contact.length()<10 || Contact.length()>14)
                {
                    Toast.makeText(getActivity(), "Contact minimum length is 10th characters and max 14", Toast.LENGTH_SHORT).show();
                    return;
                }
                if(Address.length()<6 || Address.length()>250)
                {
                    Toast.makeText(getActivity(), "Please Enter correct Address", Toast.LENGTH_SHORT).show();
                    return;
                }
                if(Password.length()<6)
                {
                    Toast.makeText(getActivity(), "Password minimum length is 6th characters", Toast.LENGTH_SHORT).show();
                    return;
                }
                if(ImageSelect==null)
                {
                    HashMap<String,Object> hashMap = new HashMap<>();
                    hashMap.put("Name",Name);
                    hashMap.put("Email",Email);
                    hashMap.put("Contact",Contact);
                    hashMap.put("City",City);
                    hashMap.put("Address",Address);
                    hashMap.put("Password",Password);
                    hashMap.put("userType","User");

                    databaseReference.child(userId).updateChildren(hashMap);
                    Toast.makeText(getActivity(), "Profile has been updated", Toast.LENGTH_SHORT).show();
                }
                else
                {


                    progressDialog.setTitle("Please Wait");
                    progressDialog.setMessage("Profile is updating");
                    progressDialog.setCanceledOnTouchOutside(false);
                    progressDialog.show();

                    final StorageReference riversRef = mStorageRef.child("User/_"+System.currentTimeMillis());

                    riversRef.putFile(imageUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            riversRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(Uri uri) {
                                    progressDialog.dismiss();
                                    HashMap<String,Object> hashMap = new HashMap<>();
                                    hashMap.put("Name",Name);
                                    hashMap.put("Email",Email);
                                    hashMap.put("Contact",Contact);
                                    hashMap.put("City",City);
                                    hashMap.put("Address",Address);
                                    hashMap.put("ProfileImage",uri.toString());
                                    hashMap.put("Password",Password);
                                    hashMap.put("userType","User");

                                    databaseReference.child(userId).updateChildren(hashMap);
                                    Toast.makeText(getActivity(), "Profile has been updated", Toast.LENGTH_SHORT).show();
                                }
                            });
                        }
                    });



                }
            }
        });

        getData(userId);


    }

    public void getData(String userId)
    {

        databaseReference.child(userId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists())
                {
                    progressDialog.dismiss();
                    String Name = snapshot.child("Name").getValue().toString();
                    String Email = snapshot.child("Email").getValue().toString();
                    String Contact = snapshot.child("Contact").getValue().toString();
                    String Password = snapshot.child("Password").getValue().toString();
                    name.setText(Name);
                    email.setText(Email);
                    contact.setText(Contact);
                    password.setText(Password);
                    try
                    {

                        String City = snapshot.child("City").getValue().toString();
                        String Address = snapshot.child("Address").getValue().toString();
                        String ProfileImage = snapshot.child("ProfileImage").getValue().toString();

                        city.setText(City);
                        address.setText(Address);
                        Glide.with(getActivity()).load(ProfileImage).fitCenter().into(circleImageView);
                    }
                    catch (Exception ex)
                    {

                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode==100 && resultCode==RESULT_OK){
            imageUri = data.getData();
            ImageSelect="123";
            circleImageView.setImageURI(imageUri);
        }
    }
}