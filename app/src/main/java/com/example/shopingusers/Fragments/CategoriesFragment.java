package com.example.shopingusers.Fragments;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.shopingusers.Activities.AllPrroductActivity;
import com.example.shopingusers.Modal.CategoryModal;
import com.example.shopingusers.Modal.ProductModal;
import com.example.shopingusers.R;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;


public class CategoriesFragment extends Fragment {

    private RecyclerView recyclerView;
    ArrayList<CategoryModal> categoryModals;
    MyAdapter myAdapter;
    private RecyclerView shopsRecycler;
    private DatabaseReference databaseReference;
    private String userId;
    private EditText searchHere;

    public CategoriesFragment() {
        // Required empty public constructor
    }



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_categories, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        SharedPreferences sharedPreferences = getActivity().getSharedPreferences("ShoppingRef", Context.MODE_PRIVATE);
        userId = sharedPreferences.getString("userId","");

        recyclerView = view.findViewById(R.id.exclusiveRecycler);
        searchHere = view.findViewById(R.id.searchHere);

        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()
        ));


        categoryModals = new ArrayList<>();
        myAdapter=new MyAdapter(getActivity(),getActivity(),categoryModals);
        recyclerView.setAdapter(myAdapter);


        databaseReference = FirebaseDatabase.getInstance().getReference();



        searchHere.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

                String value = s.toString();
//                    System.out.println(results);

                ArrayList<CategoryModal> newList2=new ArrayList<>();
                for (CategoryModal prodInfo2 :categoryModals ){
                    String jobTitle=prodInfo2.getName().toLowerCase();

                    if (jobTitle.contains(value.toLowerCase()) ){
                        newList2.add(prodInfo2);
                    }
                }
                myAdapter.setFilter(newList2);

            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        getData();
    }
    public void getData()
    {
        databaseReference.child("Categories").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for(DataSnapshot dataSnapshot:snapshot.getChildren())
                {
                    CategoryModal modal = new CategoryModal();
                    modal.setName(dataSnapshot.child("Name").getValue().toString());
                    modal.setId(dataSnapshot.getKey());
                    categoryModals.add(modal);
                }
                myAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
    public class MyAdapter extends RecyclerView.Adapter<MyAdapter.MyViewHolder> {
        ArrayList<CategoryModal> data;
        Context context;
        Activity activity;
        String TAG;
        public class MyViewHolder extends RecyclerView.ViewHolder  {

            private TextView name;
            public MyViewHolder(View view) {
                super(view);
                name = view.findViewById(R.id.name);

            }
        }
        public MyAdapter(Context c, Activity a , ArrayList<CategoryModal> cartModelss){
            this.data =cartModelss;
            context=c;
            activity=a;
            TAG="***Adapter";
        }
        @Override
        public MyAdapter.MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View itemView = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.category_adapter, parent, false);
            return new MyAdapter.MyViewHolder(itemView);
        }

        @Override
        public void onBindViewHolder(final MyAdapter.MyViewHolder viewHolder, final int position) {
            CategoryModal modal = data.get(position);
            viewHolder.name.setText(modal.getName());
            viewHolder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    startActivity(new Intent(getActivity(), AllPrroductActivity.class).putExtra("categId",modal.getName()));
                }
            });
        }
        @Override
        public int getItemCount() {
//        return  5;
            return data.size();
        }

        public void setFilter(ArrayList<CategoryModal> newList){
            data=new ArrayList<>();
            data.addAll(newList);
            notifyDataSetChanged();
        }
        @Override
        public long getItemId(int position) {
            return position;
        }
    }
}