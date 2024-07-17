package com.nahidsoft.boycott.Activitys;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.bumptech.glide.Glide;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.nahidsoft.boycott.Adapters.SuggetionAdapter;
import com.nahidsoft.boycott.Models.Category;
import com.nahidsoft.boycott.Models.Product;
import com.nahidsoft.boycott.R;
import com.nahidsoft.boycott.Utilitis.APIs;
import com.nahidsoft.boycott.databinding.ActivityResultBinding;
import com.nahidsoft.boycott.databinding.ActivityResultGreen2Binding;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ResultGreenActivity extends AppCompatActivity {

    ActivityResultGreen2Binding binding;
    ArrayList<Product> productList = new ArrayList<>();
    SuggetionAdapter suggetionAdapter;
    String selectedStatus;
    String image, note;
    List<Category> categoryLis = new ArrayList<>();
    String TAG = "MyTag";

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityResultGreen2Binding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        image=getIntent().getStringExtra("image");
        note=getIntent().getStringExtra("note");
        selectedStatus=getIntent().getStringExtra("status");

        categoryLis = retrieveCategoriesFromSharedPreferences();

        loadProductListFromPreferences();
        loadData();

        binding.suggetionRecyclerView.setHasFixedSize(true);
        LinearLayoutManager horizontalLayoutManagaer = new LinearLayoutManager(getApplicationContext(), LinearLayoutManager.HORIZONTAL, false);
        binding.suggetionRecyclerView.setLayoutManager(horizontalLayoutManagaer);
        suggetionAdapter = new SuggetionAdapter(getApplicationContext(), productList);
        binding.suggetionRecyclerView.setAdapter(suggetionAdapter);
    }
    private void loadData() {
        binding.backBtn.setOnClickListener(v->{
            onBackPressed();
        });
        binding.noteTextTv.setText(""+note);
        if (image.contains("https")){
            Glide.with(getApplicationContext()).load(image).into(binding.productImage);
        }else{
            Glide.with(getApplicationContext()).load(APIs.IMAGE+image).into(binding.productImage);
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    private void loadProductListFromPreferences() {
        SharedPreferences sharedPreferences = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
        Gson gson = new Gson();
        String json = sharedPreferences.getString("ProductList", null);

        if (json != null) {
            Type type = new TypeToken<List<Product>>() {
            }.getType();
            List<Product> products = gson.fromJson(json, type);
            if (products != null) {
                productList.clear();
                for (Product product : products) {
                    List<String> categorySplit = Arrays.asList(product.getCategory().split(","));
                    Log.d(TAG, "loadProductListFromPreferences: "+categorySplit);
                    if (selectedStatus.equals(product.getStatus())) {
                        for (Category category : categoryLis) {
                            if (categorySplit.contains(category.getId())) {
                                productList.add(product);
                            }

                        }

                    }
                }
            }

        } else {
            Log.d("BoycottActivity", "No product data found in SharedPreferences");
        }
    }

    private List<Category> retrieveCategoriesFromSharedPreferences() {
        SharedPreferences sharedPreferences = getSharedPreferences("MySharedPref", Context.MODE_PRIVATE);
        Gson gson = new Gson();
        String json = sharedPreferences.getString("categoryList", null);
        Type type = new TypeToken<ArrayList<Category>>() {
        }.getType();
        List<Category> categoryList = gson.fromJson(json, type);
        if (categoryList == null) {
            categoryList = new ArrayList<>();
        }
        return categoryList;
    }
}