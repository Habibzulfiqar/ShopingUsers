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
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.shopingusers.Activities.ProductDetail;
import com.example.shopingusers.Modal.CategoryModal;
import com.example.shopingusers.Modal.ProductModal;
import com.example.shopingusers.R;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;

public class ProductFragment extends Fragment {

    private RecyclerView recyclerView,categoriesRecycler;
    ArrayList<ProductModal> productModals;
    ArrayList<CategoryModal> categoryModals;
    MyAdapter myAdapter;
    CategoryAdpater categoryAdapter;
    private DatabaseReference databaseReference;
    private String userId;
    private EditText searchHere;
    private ImageView closeImages;
    private RelativeLayout filters;
    private Spinner ascendingDescending;
    private Spinner priceWise;

    public ProductFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_product, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);


        SharedPreferences sharedPreferences = getActivity().getSharedPreferences("ShoppingRef", Context.MODE_PRIVATE);
        userId = sharedPreferences.getString("userId","");

        filters = view.findViewById(R.id.filters);
        priceWise = view.findViewById(R.id.priceWise);
        ascendingDescending = view.findViewById(R.id.ascendingDescending);
        closeImages = view.findViewById(R.id.closeImages);
        recyclerView = view.findViewById(R.id.exclusiveRecycler);
        searchHere = view.findViewById(R.id.searchHere);


        view.findViewById(R.id.closeImages).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                filters.setVisibility(View.GONE);
            }
        });
        view.findViewById(R.id.filtersImage).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                filters.setVisibility(View.VISIBLE);
            }
        });

        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity(), LinearLayoutManager.HORIZONTAL, false));


        productModals = new ArrayList<>();
        myAdapter=new MyAdapter(getActivity(),getActivity(),productModals);
        recyclerView.setAdapter(myAdapter);


        databaseReference = FirebaseDatabase.getInstance().getReference();


        categoriesRecycler = view.findViewById(R.id.categoriesRecycler);

        categoriesRecycler.setHasFixedSize(true);
        categoriesRecycler.setLayoutManager(new LinearLayoutManager(getActivity(), LinearLayoutManager.HORIZONTAL, false));


        categoryModals = new ArrayList<>();
        categoryAdapter=new CategoryAdpater(getActivity(),getActivity(),categoryModals);
        categoriesRecycler.setAdapter(categoryAdapter);


        view.findViewById(R.id.apply).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String PriceTags = priceWise.getSelectedItem().toString();
                if ((PriceTags.equals("Ascending")))
                {
                    Collections.sort(productModals, new Comparator<ProductModal>() {
                        @Override
                        public int compare(ProductModal lhs, ProductModal rhs) {

                            return rhs.getPrice().compareTo(lhs.getPrice());
                        }
                    });
                }
                if ((PriceTags.equals("Descending")))
                {
                    Collections.sort(productModals, new Comparator<ProductModal>() {
                        @Override
                        public int compare(ProductModal lhs, ProductModal rhs) {
                            return lhs.getPrice().compareTo(rhs.getPrice());
                        }
                    });
                }
                filters.setVisibility(View.GONE);
                myAdapter.notifyDataSetChanged();

            }
        });

        view.findViewById(R.id.clear).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getData();
                filters.setVisibility(View.GONE);
            }
        });

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
        getData();
        getCategoryes();
    }
    public void getCategoryes()
    {
        databaseReference.child("Categories").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                categoryModals.clear();
                CategoryModal moda1l = new CategoryModal();
                moda1l.setName("All");
                categoryModals.add(moda1l);
                for(DataSnapshot dataSnapshot:snapshot.getChildren())
                {
                    CategoryModal modal = new CategoryModal();
                    modal.setName(dataSnapshot.child("Name").getValue().toString());
                    modal.setId(dataSnapshot.getKey());
                    categoryModals.add(modal);
                }
                categoryAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    public void getData()
    {
        databaseReference.child("Products").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                productModals.clear();
                for(DataSnapshot dataSnapshot:snapshot.getChildren())
                {
                    ProductModal modal = new ProductModal();
                    modal.setCategory(dataSnapshot.child("Category").getValue().toString());
                    modal.setDescription(dataSnapshot.child("Description").getValue().toString());
                    modal.setImage(dataSnapshot.child("Image").getValue(String.class));
                    modal.setPrice(dataSnapshot.child("Price").getValue().toString());
                    modal.setQuantity(dataSnapshot.child("Quantity").getValue().toString());
                    modal.setTitle(dataSnapshot.child("Title").getValue().toString());
                    modal.setId(dataSnapshot.getKey());
                    productModals.add(modal);
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
                Glide.with(getActivity()).load(modal.getImage()).into(viewHolder.flowerImage);
            }
            catch (Exception ex)
            {

            }

            viewHolder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    startActivity(new Intent(getActivity(), ProductDetail.class).putExtra("productId",modal.getId()));
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
                    Toast.makeText(getActivity(), "Product Added into cart", Toast.LENGTH_SHORT).show();
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


    public class CategoryAdpater extends RecyclerView.Adapter<CategoryAdpater.MyViewHolder> {
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
        public CategoryAdpater(Context c, Activity a , ArrayList<CategoryModal> cartModelss){
            this.data =cartModelss;
            context=c;
            activity=a;
            TAG="***Adapter";
        }
        @Override
        public CategoryAdpater.MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View itemView = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.categories_shows_homoe, parent, false);
            return new CategoryAdpater.MyViewHolder(itemView);
        }

        @Override
        public void onBindViewHolder(final CategoryAdpater.MyViewHolder viewHolder, final int position) {
            CategoryModal modal = data.get(position);

            viewHolder.name.setText(modal.getName());
            viewHolder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if(position==0)
                    {

                        myAdapter.setFilter(productModals);
                    }
                    else
                    {
                        ArrayList<ProductModal> newList2=new ArrayList<>();
                        for (ProductModal prodInfo2 :productModals ){
                            String jobTitle=prodInfo2.getCategory().toLowerCase();

                            if (jobTitle.contains(modal.getName().toLowerCase()) ){
                                newList2.add(prodInfo2);
                            }
                        }
                        myAdapter.setFilter(newList2);
                    }


                }
            });

        }
        @Override
        public int getItemCount() {
//        return  5;
            return data.size();
        }


        @Override
        public long getItemId(int position) {
            return position;
        }
    }

}