package com.example.shopingusers.Fragments;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.shopingusers.Modal.OrdersModal;
import com.example.shopingusers.R;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;


public class CancelledOrders extends Fragment {

    private RecyclerView recyclerView;
    ArrayList<OrdersModal> ordersModals;
    MyAdapter myAdapter;
    private DatabaseReference databaseReference;

    public CancelledOrders() {
        // Required empty public constructor
    }



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_completed_order, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        recyclerView = view.findViewById(R.id.recyclerView);

        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()
        ));

        databaseReference = FirebaseDatabase.getInstance().getReference();

        ordersModals = new ArrayList<>();
        myAdapter=new MyAdapter(getActivity(),getActivity(),ordersModals);
        recyclerView.setAdapter(myAdapter);
        SharedPreferences sharedPreferences = getActivity().getSharedPreferences("ShoppingRef", Context.MODE_PRIVATE);
        String userId = sharedPreferences.getString("userId","");

        getOrders(userId);
    }
    public void getOrders(String userId)
    {
        databaseReference.child("UserOrders").child(userId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                ordersModals.clear();
                if(snapshot.exists()) {

                    for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                        if(dataSnapshot.child("status").exists()) {
                            if(dataSnapshot.child("status").getValue().equals("Reject") || dataSnapshot.child("status").getValue().equals("Cancelled")) {
                                OrdersModal ordersModal = new OrdersModal();
                                ordersModal.setOrderId(dataSnapshot.getKey());
                                ordersModal.setCartDateTime(dataSnapshot.child("CartDateTime").getValue().toString());
                                ordersModal.setProductId(dataSnapshot.child("ProductId").getValue().toString());
                                ordersModal.setDateTime(dataSnapshot.child("OrderTime").getValue().toString());
                                ordersModal.setQuantity(dataSnapshot.child("Quantity").getValue().toString());
                                ordersModal.setUserId(dataSnapshot.child("userId").getValue().toString());
                                ordersModal.setStatus(dataSnapshot.child("status").getValue().toString());


                                ordersModals.add(ordersModal);
                            }
                        }
                    }
                    myAdapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
    public class MyAdapter extends RecyclerView.Adapter<MyAdapter.MyViewHolder> {
        ArrayList<OrdersModal> data;
        Context context;
        Activity activity;
        String TAG;
        public class MyViewHolder extends RecyclerView.ViewHolder  {

            private ImageView productImage,crossImage;
            private TextView names,price,TextProcess,totalPrices;
            public MyViewHolder(View view) {
                super(view);
                productImage = view.findViewById(R.id.image);
                crossImage = view.findViewById(R.id.crossIcons);
                names = view.findViewById(R.id.flowerName);
                price = view.findViewById(R.id.price);
                TextProcess = view.findViewById(R.id.TextProcess);
                totalPrices = view.findViewById(R.id.totalPrices);
                crossImage.setVisibility(View.INVISIBLE);
            }
        }
        public MyAdapter(Context c, Activity a , ArrayList<OrdersModal> cartModelss){
            this.data =cartModelss;
            context=c;
            activity=a;
            TAG="***Adapter";
        }
        @Override
        public MyAdapter.MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View itemView = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.cancelled_ordders, parent, false);
            return new MyAdapter.MyViewHolder(itemView);
        }

        @Override
        public void onBindViewHolder(final MyAdapter.MyViewHolder viewHolder, final int position) {
            OrdersModal modal = data.get(position);

            viewHolder.TextProcess.setText("Cancelled");





            databaseReference.child("Products").child(modal.getProductId()).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    try {
                        Glide.with(getActivity()).load(snapshot.child("Image").getValue().toString()).into(viewHolder.productImage);
                        viewHolder.names.setText(snapshot.child("Title").getValue().toString());
                        viewHolder.price.setText("Rs "+snapshot.child("Price").getValue().toString());
                        viewHolder.totalPrices.setText("Rs "+((Integer.parseInt(snapshot.child("Price").getValue().toString()))*Integer.parseInt(modal.getQuantity())));
                    }
                    catch (Exception ex)
                    {

                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });

        }
        @Override
        public int getItemCount() {
//        return  5;
            return data.size();
        }

        public void setFilter(ArrayList<OrdersModal> newList){
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