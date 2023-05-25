package com.example.shopingusers.Fragments;

import static android.content.Context.MODE_PRIVATE;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

import com.example.shopingusers.Activities.MainActivity;
import com.example.shopingusers.R;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;


public class FeedbackFragment extends Fragment {

    private EditText feedback;
    private DatabaseReference databaseReference;

    public FeedbackFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_feedback, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        databaseReference = FirebaseDatabase.getInstance().getReference("Feedback");
        feedback = view.findViewById(R.id.feedback);

        view.findViewById(R.id.sends).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String Feedback = feedback.getText().toString();
                if(Feedback.isEmpty())
                {
                    Toast.makeText(getActivity(), "Please Enter Feedback", Toast.LENGTH_SHORT).show();
                    return;
                }
                else
                {  SharedPreferences sharedPreferences = getActivity().getSharedPreferences("ShoppingRef",MODE_PRIVATE);
                    String userId = sharedPreferences.getString("userId","");

                    HashMap<String,Object> hashMap = new HashMap<>();
                    hashMap.put("Feedback",Feedback);
                    hashMap.put("userId",userId);
                    hashMap.put("status","");

                    databaseReference.child(String.valueOf(System.currentTimeMillis())).updateChildren(hashMap);
                    Toast.makeText(getActivity(), "Feedback has been sent to admin", Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(getActivity(), MainActivity.class));
                    getActivity().finish();

                }
            }
        });

    }
}