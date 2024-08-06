package com.nahidsoft.boycott.Fragments;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;

import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.nahidsoft.boycott.Activitys.MainActivity;
import com.nahidsoft.boycott.Adapters.BoycpottAdapter;
import com.nahidsoft.boycott.Models.BrandModel;
import com.nahidsoft.boycott.Models.Product;
import com.nahidsoft.boycott.R;
import com.nahidsoft.boycott.databinding.FragmentBoyCottBinding;
import com.nahidsoft.boycott.databinding.FragmentBoycottListBinding;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;


public class BoycottListFragment extends Fragment {

    FragmentBoycottListBinding binding;
    BoycpottAdapter boycpottAdapter;
    List<Product> searchList = new ArrayList<>();
    static final String PREFS_NAME = "MyPrefs";
    static final String BRAND_LIST_KEY = "brand_list";
    List<BrandModel> brandList;
    List<Product> productList = new ArrayList<>();
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding=FragmentBoycottListBinding.inflate(inflater,container,false);
        return binding.getRoot();
    }
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        loadProductListFromPreferences();
        brandList = loadBrandListFromPreferences();

        binding.productArrayList.setHasFixedSize(true);
        binding.productArrayList.setLayoutManager(new GridLayoutManager(getActivity(), 3));
        boycpottAdapter = new BoycpottAdapter(productList);
        binding.productArrayList.setAdapter(boycpottAdapter);
        updateMessage(productList.size());

    }
    private void loadProductListFromPreferences() {
        SharedPreferences sharedPreferences = getActivity().getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
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
        SharedPreferences sharedPreferences = getActivity().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
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

        }
    }
}