package com.nahidsoft.boycott.Activitys;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.nahidsoft.boycott.Adapters.BoycpottAdapter;
import com.nahidsoft.boycott.Models.BrandModel;
import com.nahidsoft.boycott.Models.Product;
import com.nahidsoft.boycott.R;
import com.nahidsoft.boycott.databinding.ActivityBoycottBinding;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class BoycottActivity extends AppCompatActivity {

    ActivityBoycottBinding binding;
    BoycpottAdapter boycpottAdapter;
    List<Product> searchList = new ArrayList<>();
    static final String PREFS_NAME = "MyPrefs";
    static final String BRAND_LIST_KEY = "brand_list";
    List<BrandModel> brandList;
    List<Product> productList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityBoycottBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        loadProductListFromPreferences();
        brandList = loadBrandListFromPreferences();

        binding.productArrayList.setHasFixedSize(true);
        binding.productArrayList.setLayoutManager(new GridLayoutManager(getApplicationContext(), 3));
        boycpottAdapter = new BoycpottAdapter(productList);
        binding.productArrayList.setAdapter(boycpottAdapter);
        updateMessage(productList.size());

        setupSearchAutoComplete();
        binding.backBtn.setOnClickListener(v->{
            startActivity(new Intent(getApplicationContext(),MainActivity.class));
            finishAffinity();
        });
    }

    private void loadProductListFromPreferences() {
        SharedPreferences sharedPreferences = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
        Gson gson = new Gson();
        String json = sharedPreferences.getString("ProductList", null);

        if (json != null) {
            Type type = new TypeToken<List<Product>>() {}.getType();
            List<Product> products = gson.fromJson(json, type);

            if (products != null) {
                for (Product product : products) {
                    if ("red".equals(product.getStatus())) {
                        productList.add(product);
                    }
                }
            }
        } else {
            Log.d("BoycottActivity", "No product data found in SharedPreferences");
        }

        updateProductList(productList);
    }

    private void updateMessage(int size) {
        if (size == 0) {
            binding.emptyTv.setVisibility(View.VISIBLE);
            binding.productCountTv.setText("No products found");
        } else {
            binding.emptyTv.setVisibility(View.GONE);
            binding.productCountTv.setText("Shows " + size + " Products");
        }
    }

    private List<BrandModel> loadBrandListFromPreferences() {
        SharedPreferences sharedPreferences = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        String jsonString = sharedPreferences.getString(BRAND_LIST_KEY, null);
        List<BrandModel> brandList = new ArrayList<>();

        if (jsonString != null) {
            try {
                JSONArray jsonArray = new JSONArray(jsonString);
                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject jsonObject = jsonArray.getJSONObject(i);
                    BrandModel brand = new BrandModel(
                            jsonObject.getString("id"),
                            jsonObject.getString("title"),
                            jsonObject.getString("createdTime"),
                            jsonObject.getString("companyName"),
                            jsonObject.getString("image"),
                            jsonObject.getString("country"),
                            jsonObject.getString("owner"),
                            jsonObject.getString("status")
                    );
                    brandList.add(brand);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        } else {
            Log.d("BoycottActivity", "No brand data found in SharedPreferences");
        }

        return brandList;
    }

    private void setupSearchAutoComplete() {
        List<String> productTitles = new ArrayList<>();
        for (Product product : productList) { // Use productList instead of searchList
            productTitles.add(product.getTitle());
        }
        ArrayAdapter<String> adapter = new ArrayAdapter<>(getApplicationContext(), R.layout.simple_dropdown_item_custom, productTitles);
        binding.searchET.setAdapter(adapter);
        binding.searchET.setThreshold(1); // Start suggesting from the first character

        binding.searchET.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filter(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        binding.searchET.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String selectedItem = (String) parent.getItemAtPosition(position);
                filter(selectedItem);
            }
        });

        // Clear text button functionality
        binding.searchET.setOnTouchListener((v, event) -> {
            if (event.getAction() == MotionEvent.ACTION_UP) {
                if (event.getRawX() >= (binding.searchET.getRight() - binding.searchET.getCompoundDrawables()[2].getBounds().width())) {
                    binding.searchET.setText("");
                    return true;
                }
            }
            return false;
        });
    }

    private void filter(String text) {
        List<Product> filteredList = new ArrayList<>();
        for (Product item : productList) { // Use productList instead of searchList
            if (item.getTitle().toLowerCase().contains(text.toLowerCase())) {
                filteredList.add(item);
            }
        }
        updateMessage(filteredList.size());
        boycpottAdapter.setProductList(filteredList);
        boycpottAdapter.notifyDataSetChanged();
    }

    public void updateProductList(List<Product> productList) {
        if (boycpottAdapter != null) {
            searchList.clear();
            searchList.addAll(productList);
            updateMessage(productList.size());
            boycpottAdapter.setProductList(productList);
            boycpottAdapter.notifyDataSetChanged();

            List<String> productTitles = new ArrayList<>();
            for (Product product : productList) { // Use productList instead of searchList
                productTitles.add(product.getTitle());
            }
            ArrayAdapter<String> adapter = new ArrayAdapter<>(getApplicationContext(), R.layout.simple_dropdown_item_custom, productTitles);
            binding.searchET.setAdapter(adapter);
        }
    }
}
