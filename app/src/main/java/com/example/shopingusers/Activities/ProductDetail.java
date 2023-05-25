package com.example.shopingusers.Activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.shopingusers.Modal.ProductModal;
import com.example.shopingusers.R;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;

public class ProductDetail extends AppCompatActivity {
    private ImageView flowerImage,addToCart;
    private TextView price,quantity,detail,flowerName;
    private DatabaseReference databaseReference;

    private String userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product_detail);
        flowerImage = findViewById(R.id.flowerImage);
        price = findViewById(R.id.price);
        quantity = findViewById(R.id.quantity);
        detail = findViewById(R.id.detail);
        flowerName = findViewById(R.id.flowerName);
        addToCart = findViewById(R.id.addToCart);

        databaseReference  = FirebaseDatabase.getInstance().getReference();


        String productId = getIntent().getStringExtra("productId");

        exclusiveOffers(productId);
        SharedPreferences sharedPreferences = getSharedPreferences("ShoppingRef", Context.MODE_PRIVATE);
        userId = sharedPreferences.getString("userId","");

        findViewById(R.id.backBtns).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });
    }

    public void exclusiveOffers(String productId)
    {
        databaseReference.child("Products").child(productId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {


                ProductModal productModal = new ProductModal();
                productModal.setId(snapshot.getKey());
                productModal.setImage(snapshot.child("Image").getValue(String.class));
                productModal.setDescription(snapshot.child("Description").getValue().toString());
                productModal.setPrice(snapshot.child("Price").getValue().toString());
                productModal.setTitle(snapshot.child("Title").getValue().toString());
                productModal.setQuantity(snapshot.child("Quantity").getValue().toString());
                Glide.with(ProductDetail.this).load(snapshot.child("Image").getValue().toString()).into(flowerImage);

                flowerName.setText(snapshot.child("Title").getValue().toString());
                price.setText("Rs "+snapshot.child("Price").getValue().toString());
                quantity.setText("Quantity "+snapshot.child("Quantity").getValue().toString());
                detail.setText(snapshot.child("Description").getValue().toString());


                addToCart.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        HashMap<String,Object> hashMap = new HashMap<>();
                        hashMap.put("userId",userId);
                        hashMap.put("ProductId",snapshot.getKey());
                        hashMap.put("dateTime",System.currentTimeMillis());
                        hashMap.put("quantity",1);
                        databaseReference.child("Cart").child(userId).child(String.valueOf(System.currentTimeMillis())).updateChildren(hashMap);
                        Toast.makeText(ProductDetail.this, "Product Added into cart", Toast.LENGTH_SHORT).show();
                    }
                });

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

}