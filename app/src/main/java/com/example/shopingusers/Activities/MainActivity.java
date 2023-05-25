package com.example.shopingusers.Activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;

import com.example.shopingusers.Fragments.AllOrders;
import com.example.shopingusers.Fragments.CancelledOrders;
import com.example.shopingusers.Fragments.CartFragment;
import com.example.shopingusers.Fragments.CategoriesFragment;
import com.example.shopingusers.Fragments.FeedbackFragment;
import com.example.shopingusers.Fragments.ProductFragment;
import com.example.shopingusers.Fragments.RunningOrderFragment;
import com.example.shopingusers.Fragments.Settingfragment;
import com.example.shopingusers.R;
import com.google.android.material.navigation.NavigationView;

public class MainActivity extends AppCompatActivity {
    private DrawerLayout dl;
    private ActionBarDrawerToggle t;
    private NavigationView nv;

    private ImageView menuIcon;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        menuIcon = findViewById(R.id.menuIcon);

        dl = (DrawerLayout)findViewById(R.id.drawer);
        t = new ActionBarDrawerToggle(this, dl,R.string.Open, R.string.Close);

        dl.addDrawerListener(t);
        t.syncState();



        nv = (NavigationView)findViewById(R.id.nv);

        nv.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int id = item.getItemId();
                switch(id)
                {
                    case R.id.logout:
                        logout();
                        break;
                    case R.id.dashboard:
                        homeFragment();
                        break;
                    case R.id.cart:
                        cart();
                        break;
                    case R.id.viewCategories:
                        viewCategories();
                        break;
                    case R.id.runningorders:
                        runningorders();
                        break;
                    case R.id.allOrders:
                        allOrders();
                        break;
                    case R.id.cancelledOrders:
                        cancelledOrders();
                        break;
                    case R.id.feedback:
                        feedback();
                        break;
                    case R.id.settings:
                        settings();
                        break;




                }
                return true;

            }
        });

        menuIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dl.openDrawer(Gravity.LEFT);
            }
        });
        findViewById(R.id.profiles).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                settings();
            }
        });
        findViewById(R.id.alOrders).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                allOrders();
            }
        });
        findViewById(R.id.homesF).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                homeFragment();
            }
        });
        findViewById(R.id.cartt).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                cart();
            }
        });
        homeFragment();
    }

    private void settings() {
        dl.closeDrawer(Gravity.LEFT);
        final Fragment fragment;
        fragment = new Settingfragment();
        loadFragment(fragment);
    }

    private void feedback() {
        dl.closeDrawer(Gravity.LEFT);
        final Fragment fragment;
        fragment = new FeedbackFragment();
        loadFragment(fragment);
    }

    private void cancelledOrders() {
        dl.closeDrawer(Gravity.LEFT);
        final Fragment fragment;
        fragment = new CancelledOrders();
        loadFragment(fragment);
    }

    private void allOrders() {
        dl.closeDrawer(Gravity.LEFT);
        final Fragment fragment;
        fragment = new AllOrders();
        loadFragment(fragment);
    }

    private void runningorders() {
        dl.closeDrawer(Gravity.LEFT);
        final Fragment fragment;
        fragment = new RunningOrderFragment();
        loadFragment(fragment);
    }

    private void viewCategories() {
        dl.closeDrawer(Gravity.LEFT);
        final Fragment fragment;
        fragment = new CategoriesFragment();
        loadFragment(fragment);
    }

    private void cart() {
        dl.closeDrawer(Gravity.LEFT);
        final Fragment fragment;
        fragment = new CartFragment();
        loadFragment(fragment);
    }

    public void homeFragment()
    {
        dl.closeDrawer(Gravity.LEFT);
        final Fragment fragment;
        fragment = new ProductFragment();
        loadFragment(fragment);
    }

    public void loadFragment(Fragment fragment)
    {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.framLayout, fragment);
        transaction.addToBackStack(null);
        transaction.commit();
    }
    private void logout() {
        AlertDialog.Builder builder1 = new AlertDialog.Builder(MainActivity.this);
        builder1.setMessage("Are you sure you want to logout?.");
        builder1.setCancelable(true);

        builder1.setPositiveButton(
                "Yes",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        SharedPreferences sharedPreferences = getSharedPreferences("ShoppingRef",MODE_PRIVATE);
                        SharedPreferences.Editor editor = sharedPreferences.edit();
                        editor.putString("userId",null);
                        editor.commit();
                        editor.apply();
                        startActivity(new Intent(MainActivity.this,SplashScreen.class));
                        finish();
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
}