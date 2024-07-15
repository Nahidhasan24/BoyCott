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

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.nahidsoft.boycott.Adapters.ProducrsAdapter;
import com.nahidsoft.boycott.CustomSpinnerAdapter;
import com.nahidsoft.boycott.MainActivity;
import com.nahidsoft.boycott.Models.BrandModel;
import com.nahidsoft.boycott.Models.Category;
import com.nahidsoft.boycott.Models.Country;
import com.nahidsoft.boycott.Models.Product;
import com.nahidsoft.boycott.R;
import com.nahidsoft.boycott.databinding.FragmentListBinding;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ListFragment extends Fragment {

    FragmentListBinding binding;
    ProducrsAdapter producrsAdapter;
    List<Product> searchList = new ArrayList<>();
    List<Product> mainList = new ArrayList<>();
    List<Product> matchedBrandProducts = new ArrayList<>();
    static final String PREFS_NAME = "MyPrefs";
    static final String BRAND_LIST_KEY = "brand_list";
    List<BrandModel> brandList;
    List<Product> productList;
    List<Category> categoryLis;
    private List<Country> countryNames;

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
        categoryLis = retrieveCategoriesFromSharedPreferences();
        countryNames = loadCountryListFromPreferences();
        MainActivity mainActivity = (MainActivity) getActivity();
        binding.productArrayList.setHasFixedSize(true);
        binding.productArrayList.setLayoutManager(new GridLayoutManager(getActivity(), 3));
        producrsAdapter = new ProducrsAdapter();
        binding.productArrayList.setAdapter(producrsAdapter);

        spinnerBrand(brandList);
        spinnerCategory(categoryLis);
        spinnerCountry(countryNames);

        if (mainActivity != null) {
            productList = mainActivity.getProductList();
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

    private void spinnerCountry(List<Country> countryNames) {
        List<String> items = new ArrayList<>();
        items.add("Location");

        for (Country country : countryNames) {
            items.add(country.getCountryName());
        }

        CustomSpinnerAdapter adapter = new CustomSpinnerAdapter(getActivity(), R.layout.spinner_item_with_arrow, items);
        adapter.setDropDownViewResource(R.layout.spinner_dropdown_item_with_arrow);
        binding.spinnerLocation.setAdapter(adapter);
        binding.spinnerLocation.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position == 0) {
                    updateProductList(productList);
                    matchedBrandProducts.clear();
                } else {
                    String selectedItem = items.get(position);
                    for (Product product:productList){
                        for (BrandModel bm : brandList) {
                            if (product.getParentCompany().toLowerCase().equals(bm.getCompanyName().toLowerCase())){
                                if (bm.getCountry().toLowerCase().equals(selectedItem.toLowerCase())){
                                    matchedBrandProducts.add(product);
                                    Toast.makeText(getActivity(), ""+product.getTitle(), Toast.LENGTH_SHORT).show();
                                }
                            }
                        }
                    }

                    if (matchedBrandProducts.size() == 0 | matchedBrandProducts.size() == -1) {
                        binding.emptyTv.setVisibility(View.VISIBLE);
                    } else {
                        binding.emptyTv.setVisibility(View.GONE);
                    }

                    producrsAdapter.setProductList(matchedBrandProducts);
                    producrsAdapter.notifyDataSetChanged();

                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Do nothing
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

    private List<Country> loadCountryListFromPreferences() {
        SharedPreferences sharedPreferences = requireContext().getSharedPreferences("MySharedPref", Context.MODE_PRIVATE);
        String jsonString = sharedPreferences.getString("countryList", null);
        List<Country> countryList = new ArrayList<>();

        if (jsonString != null) {
            try {
                JSONArray jsonArray = new JSONArray(jsonString);
                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject jsonObject = jsonArray.getJSONObject(i);
                    String id = jsonObject.getString("id");
                    String countryName = jsonObject.getString("countryName");

                    Country country = new Country(id, countryName);
                    countryList.add(country);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        return countryList;
    }

    private List<Category> retrieveCategoriesFromSharedPreferences() {
        SharedPreferences sharedPreferences = getActivity().getSharedPreferences("MySharedPref", Context.MODE_PRIVATE);
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
            if (productList.isEmpty()) {
                binding.emptyTv.setVisibility(View.VISIBLE);
            } else {
                binding.emptyTv.setVisibility(View.GONE);
            }
            producrsAdapter.setProductList(productList);
            producrsAdapter.notifyDataSetChanged();
        }
    }

    private void spinnerBrand(List<BrandModel> brandList) {

        List<String> items = new ArrayList<>();
        items.add("Brand");

        for (BrandModel brand : brandList) {
            items.add(brand.getCompanyName());
        }

        CustomSpinnerAdapter adapter = new CustomSpinnerAdapter(getActivity(), R.layout.spinner_item_with_arrow, items);
        adapter.setDropDownViewResource(R.layout.spinner_dropdown_item_with_arrow);
        binding.spinnerBrand.setAdapter(adapter);
        binding.spinnerBrand.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position == 0) {
                    updateProductList(productList);
                    matchedBrandProducts.clear();
                } else {
                    String selectedItem = items.get(position);
                    for (BrandModel bm : brandList) {
                        if (bm.getCompanyName().toLowerCase().equals(selectedItem.toLowerCase())) {
                            for (Product product : productList) {
                                if (product.getParentCompany().equals(bm.getCompanyName())) {
                                    matchedBrandProducts.add(product);
                                }
                            }
                            break;
                        }
                    }

                    if (matchedBrandProducts.size() == 0 | matchedBrandProducts.size() == -1) {
                        binding.emptyTv.setVisibility(View.VISIBLE);
                    } else {
                        binding.emptyTv.setVisibility(View.GONE);
                    }

                    producrsAdapter.setProductList(matchedBrandProducts);
                    producrsAdapter.notifyDataSetChanged();

                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Do nothing
            }
        });
    }

    private void spinnerCategory(List<Category> categoryList) {
        List<String> items = new ArrayList<>();
        items.add("Category");

        for (Category category : categoryList) {
            items.add(category.getName());
        }

        CustomSpinnerAdapter adapter = new CustomSpinnerAdapter(getActivity(), R.layout.spinner_item_with_arrow, items);
        adapter.setDropDownViewResource(R.layout.spinner_dropdown_item_with_arrow);
        binding.spinnerCategory.setAdapter(adapter);
        binding.spinnerCategory.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position == 0) {
                    updateProductList(productList);
                    matchedBrandProducts.clear();
                } else {
                    String selectedItem = items.get(position);
                    String selectedCategoryId = getIDByName(selectedItem);

                    matchedBrandProducts.clear();

                    for (Product product : productList) {
                        Set<String> productCategoryIds = new HashSet<>(Arrays.asList(product.getCategory().split(",")));
                        if (productCategoryIds.contains(selectedCategoryId)) {
                            matchedBrandProducts.add(product);
                        }
                    }

                    if (matchedBrandProducts.isEmpty()) {
                        binding.emptyTv.setVisibility(View.VISIBLE);
                    } else {
                        binding.emptyTv.setVisibility(View.GONE);
                    }

                    producrsAdapter.setProductList(matchedBrandProducts);
                    producrsAdapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Do nothing
            }
        });
    }


    private String getIDByName(String name) {
        String id = "";
        for (Category category : categoryLis) {
            if (category.getName().toLowerCase().equals(name.toLowerCase())) {
                id = category.getId();
            }
        }
        return id;
    }
}