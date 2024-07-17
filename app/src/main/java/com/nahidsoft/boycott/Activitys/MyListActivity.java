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

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.GridLayoutManager;

import com.nahidsoft.boycott.Adapters.BoycpottAdapter;
import com.nahidsoft.boycott.Models.BrandModel;
import com.nahidsoft.boycott.Models.Product;
import com.nahidsoft.boycott.R;
import com.nahidsoft.boycott.Utilitis.ProductRepository;
import com.nahidsoft.boycott.databinding.ActivityBoycottBinding;
import com.nahidsoft.boycott.databinding.ActivityMyListBinding;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class MyListActivity extends AppCompatActivity {

    ActivityMyListBinding binding;
    BoycpottAdapter boycpottAdapter;
    List<Product> searchList = new ArrayList<>();
    List<Product> productList = new ArrayList<>();
    private ProductRepository repository;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding=ActivityMyListBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        repository = new ProductRepository(this);
        productList = repository.getAllProducts();
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
    private void updateMessage(int size) {
        if (size == 0) {
            binding.emptyTv.setVisibility(View.VISIBLE);
            binding.productCountTv.setText("No products found");
        } else {
            binding.emptyTv.setVisibility(View.GONE);
            binding.productCountTv.setText("Shows " + size + " Products");
        }
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

}