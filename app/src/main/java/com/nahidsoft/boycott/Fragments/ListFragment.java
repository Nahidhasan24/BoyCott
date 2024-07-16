package com.nahidsoft.boycott.Fragments;

import android.app.Dialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;

import android.text.Editable;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.nahidsoft.boycott.Adapters.ProducrsAdapter;
import com.nahidsoft.boycott.Utilitis.CustomSpinnerAdapter;
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
import java.util.List;

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

        setupSpinners();

        if (mainActivity != null) {
            productList = mainActivity.getProductList();
            updateProductList(productList);
        }

        setupSearchAutoComplete();
        binding.filterBtn.setOnClickListener(v -> {
            showDialog();
        });
    }

    private void showDialog() {

        final Dialog dialog = new Dialog(getActivity());
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.popup_view);


        dialog.show();
        dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.getWindow().getAttributes().windowAnimations = R.style.DialogAnimation;
        dialog.getWindow().setGravity(Gravity.BOTTOM);

    }

    private void setupSpinners() {
        setupSpinnerCountry(countryNames);
        setupSpinnerBrand(brandList);
        setupSpinnerCategory(categoryLis);

        binding.spinnerBrand.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @RequiresApi(api = Build.VERSION_CODES.N)
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                checkAndFilterProducts();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Do nothing
            }
        });

        binding.spinnerCategory.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @RequiresApi(api = Build.VERSION_CODES.N)
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                checkAndFilterProducts();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Do nothing
            }
        });

        binding.spinnerLocation.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @RequiresApi(api = Build.VERSION_CODES.N)
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                checkAndFilterProducts();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Do nothing
            }
        });
    }

    private void setupSpinnerCountry(List<Country> countryNames) {
        List<String> items = new ArrayList<>();
        items.add("Location");

        for (Country country : countryNames) {
            items.add(country.getCountryName());
        }

        CustomSpinnerAdapter adapter = new CustomSpinnerAdapter(getActivity(), R.layout.spinner_item_with_arrow, items);
        adapter.setDropDownViewResource(R.layout.spinner_dropdown_item_with_arrow);
        binding.spinnerLocation.setAdapter(adapter);
    }

    private void setupSpinnerBrand(List<BrandModel> brandList) {
        List<String> items = new ArrayList<>();
        items.add("Brand");

        for (BrandModel brand : brandList) {
            items.add(brand.getCompanyName());
        }

        CustomSpinnerAdapter adapter = new CustomSpinnerAdapter(getActivity(), R.layout.spinner_item_with_arrow, items);
        adapter.setDropDownViewResource(R.layout.spinner_dropdown_item_with_arrow);
        binding.spinnerBrand.setAdapter(adapter);
    }

    private void setupSpinnerCategory(List<Category> categoryList) {
        List<String> items = new ArrayList<>();
        items.add("Category");

        for (Category category : categoryList) {
            items.add(category.getName());
        }

        CustomSpinnerAdapter adapter = new CustomSpinnerAdapter(getActivity(), R.layout.spinner_item_with_arrow, items);
        adapter.setDropDownViewResource(R.layout.spinner_dropdown_item_with_arrow);
        binding.spinnerCategory.setAdapter(adapter);
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    private void checkAndFilterProducts() {
        String selectedBrand = binding.spinnerBrand.getSelectedItem().toString();
        String selectedCategory = binding.spinnerCategory.getSelectedItem().toString();
        String selectedLocation = binding.spinnerLocation.getSelectedItem().toString();

        if ("Brand".equals(selectedBrand) && "Category".equals(selectedCategory) && "Location".equals(selectedLocation)) {
            updateProductList(productList);
        } else {
            matchedBrandProducts.clear();
            for (Product product : productList) {
                boolean matchesBrand = "Brand".equals(selectedBrand) || product.getParentCompany().equalsIgnoreCase(selectedBrand);
                boolean matchesCategory = "Category".equals(selectedCategory) || product.getCategory().contains(getIDByName(selectedCategory));
                boolean matchesLocation = "Location".equals(selectedLocation) || brandList.stream().anyMatch(bm -> bm.getCompanyName().equalsIgnoreCase(product.getParentCompany()) && bm.getCountry().equalsIgnoreCase(selectedLocation));

                if (matchesBrand && matchesCategory && matchesLocation) {
                    matchedBrandProducts.add(product);
                }
            }

            updateMessage(matchedBrandProducts.size());

            producrsAdapter.setProductList(matchedBrandProducts);
            producrsAdapter.notifyDataSetChanged();
        }
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

    private void setupSearchAutoComplete() {
        List<String> productTitles = new ArrayList<>();
        for (Product product : searchList) {
            productTitles.add(product.getTitle());
        }
        ArrayAdapter<String> adapter = new ArrayAdapter<>(getActivity(), R.layout.simple_dropdown_item_custom, productTitles);
        binding.searchET.setAdapter(adapter);
        binding.searchET.setThreshold(1); // Start suggesting from the first character

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

        binding.searchET.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String selectedItem = (String) parent.getItemAtPosition(position);
                filter(selectedItem);
            }
        });

        // Clear text button functionality
        binding.searchET.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_UP) {
                    if (event.getRawX() >= (binding.searchET.getRight() - binding.searchET.getCompoundDrawables()[2].getBounds().width())) {
                        binding.searchET.setText("");
                        return true;
                    }
                }
                return false;
            }
        });
    }

    private void filter(String text) {
        List<Product> filteredList = new ArrayList<>();
        for (Product item : searchList) {
            if (item.getTitle().toLowerCase().contains(text.toLowerCase())) {
                filteredList.add(item);
            }
        }
        updateMessage(filteredList.size());
        producrsAdapter.setProductList(filteredList);
        producrsAdapter.notifyDataSetChanged();
    }

    public void updateProductList(List<Product> productList) {
        if (producrsAdapter != null) {
            searchList.clear();
            searchList.addAll(productList);
            updateMessage(productList.size());
            producrsAdapter.setProductList(productList);
            producrsAdapter.notifyDataSetChanged();

            List<String> productTitles = new ArrayList<>();
            for (Product product : searchList) {
                productTitles.add(product.getTitle());
            }
            ArrayAdapter<String> adapter = new ArrayAdapter<>(getActivity(), R.layout.simple_dropdown_item_custom, productTitles);
            binding.searchET.setAdapter(adapter);
        }
    }

    private String getIDByName(String name) {
        for (Category category : categoryLis) {
            if (category.getName().equalsIgnoreCase(name)) {
                return category.getId();
            }
        }
        return "";
    }
}
