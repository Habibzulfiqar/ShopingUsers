package com.example.shopingusers.Fragments;

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.StrictMode;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.shopingusers.Modal.CartModal;
import com.example.shopingusers.R;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.stripe.android.ApiResultCallback;
import com.stripe.android.PaymentConfiguration;
import com.stripe.android.PaymentIntentResult;
import com.stripe.android.Stripe;
import com.stripe.android.model.Card;
import com.stripe.android.model.ConfirmPaymentIntentParams;
import com.stripe.android.model.PaymentIntent;
import com.stripe.android.model.PaymentMethodCreateParams;
import com.stripe.android.view.CardInputWidget;
import com.stripe.android.view.CardMultilineWidget;

import org.json.JSONObject;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class CartFragment extends Fragment {
    private RecyclerView recyclerView;
    ArrayList<CartModal> cartModals;
    MyAdapter myAdapter;

    private DatabaseReference databaseReference;

    public CartFragment() {
        // Required empty public constructor
    }

    private  String userId;
    private ImageView checkoutImage,crossImage;

    private RelativeLayout relativeLayout;
    private TextView master,cash;

    //    stripe
    Stripe stripe;

    private int REQUEST_CODE_PAYMENT=1;


    private Dialog dialog;

    private ProgressDialog progressDialog;


    String amount;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_cart, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        progressDialog = new ProgressDialog(getActivity());
        dialog = new Dialog(getActivity());
        databaseReference = FirebaseDatabase.getInstance().getReference();
        crossImage = view.findViewById(R.id.crossImages);
        relativeLayout = view.findViewById(R.id.layouts);
        master = view.findViewById(R.id.cards);
        cash = view.findViewById(R.id.cash);
        showStripeDialog();
        crossImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                relativeLayout.setVisibility(View.GONE);
            }
        });

        recyclerView = view.findViewById(R.id.recyclerView);

        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));


        cartModals = new ArrayList<>();
        myAdapter=new MyAdapter(getActivity(),getActivity(),cartModals);
        recyclerView.setAdapter(myAdapter);

        SharedPreferences sharedPreferences = getActivity().getSharedPreferences("ShoppingRef", Context.MODE_PRIVATE);
        userId = sharedPreferences.getString("userId","");

        getCartData(userId);

        checkoutImage = view.findViewById(R.id.checkoutImage);

        checkoutImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                relativeLayout.setVisibility(View.VISIBLE);
            }
        });
        cash.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                databaseReference.child("Users").child(userId).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if(snapshot.child("Address").exists())
                        {
                            databaseReference.child("Cart").child(userId).addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                    if(snapshot.exists()) {
                                        for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                                            checkoutImage.setVisibility(View.GONE);
                                            HashMap<String, Object> hashMap = new HashMap<>();
                                            hashMap.put("CartDateTime", dataSnapshot.child("dateTime").getValue().toString());
                                            hashMap.put("ProductId", dataSnapshot.child("ProductId").getValue().toString());
                                            hashMap.put("Quantity", dataSnapshot.child("quantity").getValue().toString());
                                            hashMap.put("userId", dataSnapshot.child("userId").getValue().toString());
                                            hashMap.put("status", "waiting");
                                            hashMap.put("payment", "Cash");
                                            hashMap.put("OrderTime", System.currentTimeMillis());
                                            String pushIds = String.valueOf(System.currentTimeMillis());
                                            databaseReference.child("UserOrders").child(userId).child(pushIds).updateChildren(hashMap);
                                            databaseReference.child("SellerOrders").child(pushIds).updateChildren(hashMap);
                                        }
                                        databaseReference.child("Cart").child(userId).removeValue();
                                        Toast.makeText(getActivity(), "Order has been placed Thank you", Toast.LENGTH_SHORT).show();
                                        relativeLayout.setVisibility(View.GONE);

                                    }
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError error) {

                                }
                            });
                        }
                        else
                        {
                            Toast.makeText(getActivity(), "Please Make your profile first", Toast.LENGTH_SHORT).show();
                            return;
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

            }
        });


        PaymentConfiguration.init(getActivity(), getResources().getString(R.string.publishableKey));
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        master.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.show();
                CardMultilineWidget cardMultilineWidget = dialog.findViewById(R.id.cardInputWidget);
                ImageView button = dialog.findViewById(R.id.payNowButton);

                button.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Card card = cardMultilineWidget.getCard();
                        if(card!=null) {
                            progressDialog.setTitle("Please wait");
                            progressDialog.show();

                            databaseReference.child("Users").child(userId).addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                    if(snapshot.child("Address").exists())
                                    {
                                        databaseReference.child("Cart").child(userId).addListenerForSingleValueEvent(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                                if(snapshot.exists()) {
                                                    for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                                                        checkoutImage.setVisibility(View.GONE);
                                                        HashMap<String, Object> hashMap = new HashMap<>();
                                                        hashMap.put("CartDateTime", dataSnapshot.child("dateTime").getValue().toString());
                                                        hashMap.put("ProductId", dataSnapshot.child("ProductId").getValue().toString());
                                                        hashMap.put("Quantity", dataSnapshot.child("quantity").getValue().toString());
                                                        hashMap.put("userId", dataSnapshot.child("userId").getValue().toString());
                                                        hashMap.put("status", "waiting");
                                                        hashMap.put("payment", "Card");
                                                        hashMap.put("OrderTime", System.currentTimeMillis());
                                                        String pushIds = String.valueOf(System.currentTimeMillis());
                                                        databaseReference.child("UserOrders").child(userId).child(pushIds).updateChildren(hashMap);
                                                        databaseReference.child("SellerOrders").child(pushIds).updateChildren(hashMap);
                                                    }
                                                    databaseReference.child("Cart").child(userId).removeValue();
                                                    Toast.makeText(getActivity(), "Order has been placed Thank you", Toast.LENGTH_SHORT).show();
                                                    dialog.dismiss();
                                                    relativeLayout.setVisibility(View.GONE);
                                                    progressDialog.dismiss();
                                                }
                                            }

                                            @Override
                                            public void onCancelled(@NonNull DatabaseError error) {

                                            }
                                        });
                                    }
                                    else
                                    {
                                        Toast.makeText(getActivity(), "Please Make your profile first", Toast.LENGTH_SHORT).show();
                                        return;
                                    }
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError error) {

                                }
                            });


                        }
                    }
                });
            }
        });
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);



        dialog.cancel();
        stripe.onPaymentResult(requestCode, data, new CartFragment.PaymentResultCallback(CartFragment.this));


    }

    public void pamymentCall(double price, String crdNmber, String month, String year, String cvvv){
        com.stripe.Stripe.apiKey = getResources().getString(R.string.secretKey);
        CardInputWidget cardInputWidget = new CardInputWidget(getActivity());
        cardInputWidget.setCardNumber(crdNmber);
        cardInputWidget.setExpiryDate(Integer.parseInt(month),Integer.parseInt(year));
        cardInputWidget.setCvcCode(cvvv);
        PaymentMethodCreateParams params = cardInputWidget.getPaymentMethodCreateParams();
        if (params != null) {
            List<Object> paymentMethodTypes =
                    new ArrayList<>();
            paymentMethodTypes.add("card");
            Map<String, Object> paymentIntentParams = new HashMap<>();
//            paymentIntentParams.put("amount", ((int)price * 100)); //1 is the amount to be deducted, 100 is must since it accepts cents & 1£=100cents
            paymentIntentParams.put("amount", ((int)Math.round(price) * 100)); //1 is the amount to be deducted, 100 is must since it accepts cents & 1£=100cents
            Log.e("TAG","rounded value "+(int)Math.round(price));
//            paymentIntentParams.put("currency", "usd");
            paymentIntentParams.put("currency", "USD");
            paymentIntentParams.put("payment_method_types", paymentMethodTypes);
            try {
                com.stripe.model.PaymentIntent paymentIntent = new com.stripe.model.PaymentIntent().create(paymentIntentParams);
                ConfirmPaymentIntentParams confirmParams = ConfirmPaymentIntentParams
                        .createWithPaymentMethodCreateParams(params, paymentIntent.getClientSecret());
                final Context context = getActivity();
                stripe = new Stripe(
                        context,
                        PaymentConfiguration.getInstance(context).getPublishableKey()
                );
                stripe.confirmPayment(CartFragment.this, confirmParams);
            }
            catch (Exception e){
                progressDialog.dismiss();
                Toast.makeText(getActivity(), e.toString(), Toast.LENGTH_SHORT).show();
                e.printStackTrace();
            }catch (Error e){
                progressDialog.dismiss();
                Toast.makeText(getActivity(), e.toString(), Toast.LENGTH_SHORT).show();
                e.printStackTrace();
            }
        }
    }



    private final class PaymentResultCallback implements ApiResultCallback<PaymentIntentResult> {
        @NonNull
        private final WeakReference<CartFragment> activityRef;

        PaymentResultCallback(@NonNull CartFragment activity) {
            activityRef = new WeakReference<>(activity);
        }

        @Override
        public void onSuccess(@NonNull PaymentIntentResult result) {
            final CartFragment activity = activityRef.get();
            if (activity == null) {
                return;
            }

            PaymentIntent paymentIntent = result.getIntent();
            PaymentIntent.Status status = paymentIntent.getStatus();
            if (status == PaymentIntent.Status.Succeeded) {
                // Payment completed successfully
                Gson gson = new GsonBuilder().setPrettyPrinting().create();
                Log.e("stripePayment", "Payment Completed: " + gson.toJson(paymentIntent));
                try{
                    JSONObject jsonObject=new JSONObject(gson.toJson(paymentIntent));
                    if(jsonObject.getString("status").equalsIgnoreCase("Succeeded")){
                        //add order to database
                        //succsess a gai
                        Log.e("responses","amount paid");









                    }
                }catch (Exception c){
                    progressDialog.dismiss();
                    c.printStackTrace();
                    Log.e("stripePayment","json exception gai ");
                }

            } else if (status == PaymentIntent.Status.RequiresPaymentMethod) {
                try{
                    progressDialog.dismiss();
                    //un secc
                }catch (Exception c){
                    c.printStackTrace();
                    progressDialog.dismiss();
                    Toast.makeText(getActivity(),"Unable to proceed",Toast.LENGTH_LONG).show();
                }

                Log.e("stripePayment", "Payment Failed: " + paymentIntent.getLastPaymentError().getMessage());
            }
        }

        @Override
        public void onError(@NonNull Exception e) {
            Toast.makeText(getActivity(), e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
            progressDialog.dismiss();
            final CartFragment activity = activityRef.get();
            if (activity == null) {
                return;
            }
            Log.e("stripePayment", "Error: " +e.getLocalizedMessage());
            e.printStackTrace();
            try{
            }catch (Exception c){
                c.printStackTrace();
            }
        }


    }
    public void showStripeDialog()
    {
        dialog.setContentView(R.layout.paymentmethod_dialog);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
    }





    public void getCartData(String userId)
    {
        databaseReference.child("Cart").child(userId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                cartModals.clear();
                for(DataSnapshot dataSnapshot:snapshot.getChildren())
                {
                    checkoutImage.setVisibility(View.VISIBLE);
                    CartModal cartModal = new CartModal();

                    cartModal.setCartId(dataSnapshot.getKey());
                    cartModal.setProductId(dataSnapshot.child("ProductId").getValue().toString());
                    cartModal.setQuantity(dataSnapshot.child("quantity").getValue().toString());
                    cartModals.add(cartModal);
                }
                myAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    public class MyAdapter extends RecyclerView.Adapter<MyAdapter.MyViewHolder> {
        ArrayList<CartModal> data;
        Context context;
        Activity activity;
        String TAG;
        public class MyViewHolder extends RecyclerView.ViewHolder  {

            private ImageView flowerImage,remove,plusIcon,minusIcon;
            private TextView price,name,quantity,totalPrices;
            public MyViewHolder(View view) {
                super(view);
                flowerImage = view.findViewById(R.id.flowerImage);
                remove = view.findViewById(R.id.remove);
                plusIcon = view.findViewById(R.id.plusIcon);
                minusIcon = view.findViewById(R.id.minusIcon);
                price = view.findViewById(R.id.price);
                name = view.findViewById(R.id.name);
                quantity = view.findViewById(R.id.quantity);
                totalPrices = view.findViewById(R.id.totalPrices);
            }
        }
        public MyAdapter(Context c, Activity a , ArrayList<CartModal> cartModelss){
            this.data =cartModelss;
            context=c;
            activity=a;
            TAG="***Adapter";
        }
        @Override
        public MyAdapter.MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View itemView = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.cart_fragment_adapter, parent, false);
            return new MyAdapter.MyViewHolder(itemView);
        }

        @Override
        public void onBindViewHolder(final MyAdapter.MyViewHolder viewHolder, final int position) {
            CartModal modal = data.get(position);


            viewHolder.quantity.setText(modal.getQuantity());
            databaseReference.child("Products").child(modal.getProductId()).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    Glide.with(getActivity()).load(snapshot.child("Image").getValue().toString()).into(viewHolder.flowerImage);
                    viewHolder.name.setText(snapshot.child("Title").getValue().toString());
                    viewHolder.price.setText("RM "+snapshot.child("Price").getValue().toString());
                    viewHolder.totalPrices.setText("Rs "+((Integer.parseInt(snapshot.child("Price").getValue().toString()))*Integer.parseInt(modal.getQuantity())));

                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
            viewHolder.remove.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    AlertDialog.Builder builder1 = new AlertDialog.Builder(context);
                    builder1.setMessage("Are you sure to remove this item from cart?");
                    builder1.setCancelable(true);

                    builder1.setPositiveButton(
                            "Yes",
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    databaseReference.child("Cart").child(userId).child(modal.getCartId()).removeValue();
                                    Toast.makeText(context, "Item has been removed", Toast.LENGTH_SHORT).show();
                                }
                            });

                    builder1.setNegativeButton(
                            "No",
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    dialog.cancel();
                                }
                            });

                    AlertDialog alert11 = builder1.create();
                    alert11.show();
                }
            });



            viewHolder.plusIcon.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    databaseReference.child("Products").child(modal.getProductId()).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            int Quantity = Integer.parseInt(snapshot.child("Quantity").getValue().toString());
                            if(Integer.parseInt(modal.getQuantity())>=Quantity)
                            {
                                Toast.makeText(context, "No enough Quantities available", Toast.LENGTH_SHORT).show();
                                viewHolder.plusIcon.setEnabled(false);
                            }
                            else
                            {
                                int totalQuantity = (Integer.parseInt(modal.getQuantity())+1);
                                HashMap<String,Object> hashMap = new HashMap<>();
                                hashMap.put("quantity",totalQuantity);

                                databaseReference.child("Cart").child(userId).child(modal.getCartId()).updateChildren(hashMap);
                                myAdapter.notifyDataSetChanged();
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });
                }
            });

            viewHolder.minusIcon.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    databaseReference.child("Products").child(modal.getProductId()).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            int Quantity = Integer.parseInt(snapshot.child("Quantity").getValue().toString());
                            if(modal.getQuantity().equals("1"))
                            {
                                AlertDialog.Builder builder1 = new AlertDialog.Builder(context);
                                builder1.setMessage("Are you sure to remove this item from cart?");
                                builder1.setCancelable(true);

                                builder1.setPositiveButton(
                                        "Yes",
                                        new DialogInterface.OnClickListener() {
                                            public void onClick(DialogInterface dialog, int id) {
                                                databaseReference.child("Cart").child(userId).child(modal.getCartId()).removeValue();
                                                Toast.makeText(context, "Item has been removed", Toast.LENGTH_SHORT).show();
                                            }
                                        });

                                builder1.setNegativeButton(
                                        "No",
                                        new DialogInterface.OnClickListener() {
                                            public void onClick(DialogInterface dialog, int id) {
                                                dialog.cancel();
                                            }
                                        });

                                AlertDialog alert11 = builder1.create();
                                alert11.show();
                            }
                            else
                            {                                viewHolder.plusIcon.setEnabled(true);
                                int totalQuantity = (Integer.parseInt(modal.getQuantity())-1);
                                HashMap<String,Object> hashMap = new HashMap<>();
                                hashMap.put("quantity",totalQuantity);

                                databaseReference.child("Cart").child(userId).child(modal.getCartId()).updateChildren(hashMap);
                                myAdapter.notifyDataSetChanged();
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });
                }
            });
        }
        @Override
        public int getItemCount() {
//        return  5;
            return data.size();
        }

        public void setFilter(ArrayList<CartModal> newList){
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