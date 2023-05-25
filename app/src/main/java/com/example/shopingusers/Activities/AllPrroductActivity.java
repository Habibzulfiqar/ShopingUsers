package com.example.shopingusers.Activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
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
import com.example.shopingusers.Fragments.ProductFragment;
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

public class AllPrroductActivity extends AppCompatActivity {
    private RecyclerView recyclerView,categoriesRecycler;
    ArrayList<ProductModal> productModals;
    MyAdapter myAdapter;
    private DatabaseReference databaseReference;
    private String userId;
    private EditText searchHere;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_all_prroduct);
        SharedPreferences sharedPreferences = getSharedPreferences("ShoppingRef", Context.MODE_PRIVATE);
        userId = sharedPreferences.getString("userId","");


        String idsCateg = getIntent().getStringExtra("categId");
        recyclerView = findViewById(R.id.exclusiveRecycler);
        searchHere = findViewById(R.id.searchHere);

        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(AllPrroductActivity.this, LinearLayoutManager.HORIZONTAL, false));


        productModals = new ArrayList<>();
        myAdapter=new MyAdapter(AllPrroductActivity.this,AllPrroductActivity.this,productModals);
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

                ArrayList<ProductModal> newList2=new ArrayList<>();
                for (ProductModal prodInfo2 :productModals ){
                    String jobTitle=prodInfo2.getTitle().toLowerCase();

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
        getData(idsCateg);
    }

    public void getData(String Categ)
    {
        databaseReference.child("Products").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                productModals.clear();
                for(DataSnapshot dataSnapshot:snapshot.getChildren())
                {
                    if(dataSnapshot.child("Category").getValue().equals(Categ)) {
                        ProductModal modal = new ProductModal();
                        modal.setCategory(dataSnapshot.child("Category").getValue().toString());
                        modal.setDescription(dataSnapshot.child("Description").getValue().toString());
                        modal.setImage(dataSnapshot.child("Image").getValue().toString());
                        modal.setPrice(dataSnapshot.child("Price").getValue().toString());
                        modal.setQuantity(dataSnapshot.child("Quantity").getValue().toString());
                        modal.setTitle(dataSnapshot.child("Title").getValue().toString());
                        modal.setId(dataSnapshot.getKey());
                        productModals.add(modal);
                    }
                }
                myAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
    public class MyAdapter extends RecyclerView.Adapter<MyAdapter.MyViewHolder> {
        ArrayList<ProductModal> data;
        Context context;
        Activity activity;
        String TAG;
        public class MyViewHolder extends RecyclerView.ViewHolder  {

            private ImageView flowerImage,plusIcon;
            private TextView name,quantities,price;
            public MyViewHolder(View view) {
                super(view);
                flowerImage = view.findViewById(R.id.flowerImage);
                plusIcon = view.findViewById(R.id.plusIcon);
                name = view.findViewById(R.id.name);
                quantities = view.findViewById(R.id.quantities);
                price = view.findViewById(R.id.price);
            }
        }
        public MyAdapter(Context c, Activity a , ArrayList<ProductModal> cartModelss){
            this.data =cartModelss;
            context=c;
            activity=a;
            TAG="***Adapter";
        }
        @Override
        public MyAdapter.MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View itemView = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.product_adapter, parent, false);
            return new MyAdapter.MyViewHolder(itemView);
        }

        @Override
        public void onBindViewHolder(final MyAdapter.MyViewHolder viewHolder, final int position) {
            ProductModal modal = data.get(position);

            viewHolder.name.setText(modal.getTitle());
            viewHolder.quantities.setText(modal.getQuantity()+" Quantities");
            viewHolder.price.setText("Rs. "+modal.getPrice());
            try {
                Glide.with(AllPrroductActivity.this).load(modal.getImage()).into(viewHolder.flowerImage);
            }
            catch (Exception ex)
            {

            }

            viewHolder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    startActivity(new Intent(AllPrroductActivity.this, ProductDetail.class).putExtra("productId",modal.getId()));
                }
            });
            viewHolder.plusIcon.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    HashMap<String,Object> hashMap = new HashMap<>();
                    hashMap.put("userId",userId);
                    hashMap.put("ProductId",modal.getId());
                    hashMap.put("dateTime",System.currentTimeMillis());
                    hashMap.put("quantity",1);
                    databaseReference.child("Cart").child(userId).child(String.valueOf(System.currentTimeMillis())).updateChildren(hashMap);
                    Toast.makeText(AllPrroductActivity.this, "Product Added into cart", Toast.LENGTH_SHORT).show();
                }
            });

        }
        @Override
        public int getItemCount() {
//        return  5;
            return data.size();
        }

        public void setFilter(ArrayList<ProductModal> newList){
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