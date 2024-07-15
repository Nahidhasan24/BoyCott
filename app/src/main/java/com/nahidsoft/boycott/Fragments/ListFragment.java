package com.nahidsoft.boycott.Fragments;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;

import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Toast;

import com.nahidsoft.boycott.Adapters.ProducrsAdapter;
import com.nahidsoft.boycott.CustomSpinnerAdapter;
import com.nahidsoft.boycott.MainActivity;
import com.nahidsoft.boycott.Models.BrandModel;
import com.nahidsoft.boycott.Models.Product;
import com.nahidsoft.boycott.R;
import com.nahidsoft.boycott.databinding.FragmentListBinding;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ListFragment extends Fragment {

    FragmentListBinding binding;
    ProducrsAdapter producrsAdapter;
    List<Product> searchList = new ArrayList<>();
    List<Product> mainList = new ArrayList<>();

    private static final String PREFS_NAME = "MyPrefs";
    private static final String BRAND_LIST_KEY = "brand_list";
    private List<BrandModel> brandList;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentListBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        brandList = loadBrandListFromPreferences();

        MainActivity mainActivity = (MainActivity) getActivity();
        binding.productArrayList.setHasFixedSize(true);
        binding.productArrayList.setLayoutManager(new GridLayoutManager(getActivity(), 3));
        producrsAdapter = new ProducrsAdapter();
        binding.productArrayList.setAdapter(producrsAdapter);
        spinnerBrand(brandList);

        if (mainActivity != null) {
            List<Product> productList = mainActivity.getProductList();
            updateProductList(productList);
        }

        binding.searchET.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filter(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
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
                    String id = jsonObject.getString("id");
                    String title = jsonObject.getString("title");
                    String createdTime = jsonObject.getString("createdTime");
                    String companyName = jsonObject.getString("companyName");
                    String image = jsonObject.getString("image");
                    String country = jsonObject.getString("country");
                    String owner = jsonObject.getString("owner");
                    String status = jsonObject.getString("status");

                    BrandModel brand = new BrandModel(id, title, createdTime, companyName, image, country, owner, status);
                    brandList.add(brand);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        return brandList;
    }

    private void filter(String text) {
        List<Product> filteredList = new ArrayList<>();
        for (Product item : searchList) {
            if (item.getTitle().toLowerCase().contains(text.toLowerCase())) {
                filteredList.add(item);
            }
        }
        producrsAdapter.setProductList(filteredList);
        producrsAdapter.notifyDataSetChanged();
    }

    public void updateProductList(List<Product> productList) {
        if (producrsAdapter != null) {
            binding.productCountTv.setText("Shows " + productList.size() + " Products");
            searchList.clear();
            searchList.addAll(productList);
            producrsAdapter.setProductList(productList);
            producrsAdapter.notifyDataSetChanged();
        }
    }

    private void spinnerBrand(List<BrandModel> list) {

        List<String> items = new ArrayList<>();

        for (int i = 0; i < list.size(); i++) {
            items.add(list.get(i).getTitle());
        }
        CustomSpinnerAdapter adapter = new CustomSpinnerAdapter(getActivity(), R.layout.spinner_item_with_arrow, items);
        adapter.setDropDownViewResource(R.layout.spinner_dropdown_item_with_arrow);
        binding.spinnerBrand.setAdapter(adapter);
        binding.spinnerBrand.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position == 0) {
                    // Placeholder selected
                } else {
                    // Handle the actual selection
                    String selectedItem = items.get(position);
                    Toast.makeText(getActivity(), "Selected: " + selectedItem, Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Do nothing
            }
        });
    }
}
