package com.nahidsoft.boycott.Fragments;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.nahidsoft.boycott.Activitys.MainActivity;
import com.nahidsoft.boycott.Adapters.BoycpottAdapter;
import com.nahidsoft.boycott.Adapters.BrandSortAdapter;
import com.nahidsoft.boycott.Adapters.CategorySortAdapter;
import com.nahidsoft.boycott.Adapters.LocationSortAdapter;
import com.nahidsoft.boycott.Adapters.ProducrsAdapter;
import com.nahidsoft.boycott.Models.BrandModel;
import com.nahidsoft.boycott.Models.Category;
import com.nahidsoft.boycott.Models.Country;
import com.nahidsoft.boycott.Models.Product;
import com.nahidsoft.boycott.R;
import com.nahidsoft.boycott.databinding.FragmentBoyCottBinding;
import com.nahidsoft.boycott.databinding.FragmentBoycottListBinding;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;


public class BoycottListFragment extends Fragment implements BrandSortAdapter.OnItemClickListener, CategorySortAdapter.OnItemClickListener, LocationSortAdapter.OnItemClickListener {

    FragmentBoycottListBinding binding;
    BoycpottAdapter boycpottAdapter;
    List<Product> searchList = new ArrayList<>();
    static final String PREFS_NAME = "MyPrefs";
    static final String BRAND_LIST_KEY = "brand_list";
    List<BrandModel> brandList;
    List<Product> productList=new ArrayList<>();
    List<Product> tmpProduct=new ArrayList<>();
    List<Category> categoryList;
    private List<Country> countryNames;
    TextView sortTextView;
    // To store selected filters
    private List<String> selectedLocations = new ArrayList<>();
    private List<String> selectedBrands = new ArrayList<>();
    private List<String> selectedCategories = new ArrayList<>();
    AlertDialog dialog;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = FragmentBoycottListBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        loadProductListFromPreferences();

        brandList = loadBrandListFromPreferences();
        categoryList = retrieveCategoriesFromSharedPreferences();
        countryNames = loadCountryListFromPreferences();
        binding.productArrayList.setHasFixedSize(true);
        binding.productArrayList.setLayoutManager(new GridLayoutManager(getActivity(), 3));
        boycpottAdapter = new BoycpottAdapter(productList);
        binding.productArrayList.setAdapter(boycpottAdapter);

        sortTextView = binding.sortTextView;


        binding.brandBtn.setOnClickListener(v -> {
            showDialog("Brand", binding.spinnerSection);
        });

        binding.categoryBtn.setOnClickListener(v -> {
            showDialog("Category", binding.spinnerSection);
        });

        binding.locationBtn.setOnClickListener(v -> {
            showDialog("Location", binding.spinnerSection);
        });
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

    @Override
    public void onItemClick(List<String> selectedFilters) {
        // This method can be used to handle item click events if needed
    }

    private void onBrandItemClick(List<String> selectedFilters) {
        this.selectedBrands = selectedFilters;
    }

    private void onCategoryItemClick(List<String> selectedFilters) {
        this.selectedCategories = selectedFilters;
    }

    private void onLocationItemClick(List<String> selectedFilters) {
        this.selectedLocations = selectedFilters;
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    private void showDialog(String filterType, View anchorLayout) {


        AlertDialog.Builder builder = new AlertDialog.Builder(getContext(), R.style.TransparentDialog);
        LayoutInflater inflater = this.getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.activity_filter, null);
        builder.setView(dialogView);

        dialog = builder.create();
        dialog.show();


        int[] location = new int[2];
        anchorLayout.getLocationOnScreen(location);
        int anchorX = location[0];
        int anchorY = location[1];

        // Get the window of the dialog and set the layout parameters
        WindowManager.LayoutParams layoutParams = new WindowManager.LayoutParams();
        layoutParams.copyFrom(dialog.getWindow().getAttributes());
        layoutParams.width = WindowManager.LayoutParams.MATCH_PARENT; // Full screen width
        layoutParams.height = WindowManager.LayoutParams.WRAP_CONTENT;

        // Calculate the center position below the anchor layout
        layoutParams.gravity = Gravity.TOP;
        layoutParams.x = 0;
        layoutParams.y = anchorY + anchorLayout.getHeight();

        // Apply the layout parameters to the dialog window
        dialog.getWindow().setAttributes(layoutParams);


        EditText searchBar = dialogView.findViewById(R.id.search_bar);
        RecyclerView recyclerView = dialogView.findViewById(R.id.recyclerView);
        TextView applyButton = dialogView.findViewById(R.id.apply_button);
        TextView clearAllButton = dialogView.findViewById(R.id.clear_all_button);

        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new GridLayoutManager(getActivity(), 2));

        if (filterType.equals("Brand")) {
            List<BrandModel> filteredList = new ArrayList<>(brandList);
            BrandSortAdapter brandSortAdapter = new BrandSortAdapter(this::onBrandItemClick, getActivity(), filteredList, selectedBrands);
            recyclerView.setAdapter(brandSortAdapter);

            searchBar.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    filterBrands(s.toString(), filteredList, brandSortAdapter);
                }

                @Override
                public void afterTextChanged(Editable s) {
                }
            });

            clearAllButton.setOnClickListener(v -> {
                brandSortAdapter.clearSelection();
                selectedBrands.clear();
                binding.textViewSpinnerBrand.setText("Brand");
                updateProductList(productList);
                dialog.dismiss();
            });

            applyButton.setOnClickListener(v -> {
                applyFilters();
                dialog.dismiss();
                binding.textViewSpinnerBrand.setText(selectedBrands.size() + " Brand");
            });

        } else if (filterType.equals("Category")) {
            List<Category> filteredList = new ArrayList<>(categoryList);
            CategorySortAdapter categorySortAdapter = new CategorySortAdapter(this::onCategoryItemClick, getActivity(), filteredList, selectedCategories);
            recyclerView.setAdapter(categorySortAdapter);

            searchBar.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    filterCategories(s.toString(), filteredList, categorySortAdapter);
                }

                @Override
                public void afterTextChanged(Editable s) {
                }
            });

            clearAllButton.setOnClickListener(v -> {
                categorySortAdapter.clearSelection();
                selectedCategories.clear();
                binding.textViewSpinnerCategory.setText("Category");
                updateProductList(productList);
                dialog.dismiss();
            });

            applyButton.setOnClickListener(v -> {
                applyFilters();
                dialog.dismiss();
                binding.textViewSpinnerCategory.setText(selectedCategories.size() + " Category");
            });

        } else if (filterType.equals("Location")) {
            List<Country> filteredList = new ArrayList<>(countryNames);
            LocationSortAdapter locationSortAdapter = new LocationSortAdapter(this::onLocationItemClick, getActivity(), filteredList, selectedLocations);
            recyclerView.setAdapter(locationSortAdapter);

            searchBar.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    filterLocations(s.toString(), filteredList, locationSortAdapter);
                }

                @Override
                public void afterTextChanged(Editable s) {
                }
            });

            clearAllButton.setOnClickListener(v -> {
                locationSortAdapter.clearSelection();
                selectedLocations.clear();
                binding.textViewSpinnerLocation.setText("Location");
                updateProductList(productList);
                dialog.dismiss();
            });

            applyButton.setOnClickListener(v -> {
                applyFilters();
                dialog.dismiss();
                binding.textViewSpinnerLocation.setText(selectedLocations.size() + " Location");
            });
        }


    }

    private void filterBrands(String text, List<BrandModel> filteredList, BrandSortAdapter adapter) {
        filteredList.clear();
        if (text.isEmpty()) {
            filteredList.addAll(brandList);
        } else {
            for (BrandModel item : brandList) {
                if (item.getCompanyName().toLowerCase().contains(text.toLowerCase())) {
                    filteredList.add(item);
                }
            }
        }
        adapter.notifyDataSetChanged();
    }

    private void filterCategories(String text, List<Category> filteredList, CategorySortAdapter adapter) {
        filteredList.clear();
        if (text.isEmpty()) {
            filteredList.addAll(categoryList);
        } else {
            for (Category item : categoryList) {
                if (item.getName().toLowerCase().contains(text.toLowerCase())) {
                    filteredList.add(item);
                }
            }
        }
        adapter.notifyDataSetChanged();
    }

    private void filterLocations(String text, List<Country> filteredList, LocationSortAdapter adapter) {
        filteredList.clear();
        if (text.isEmpty()) {
            filteredList.addAll(countryNames);
        } else {
            for (Country item : countryNames) {
                if (item.getCountryName().toLowerCase().contains(text.toLowerCase())) {
                    filteredList.add(item);
                }
            }
        }
        adapter.notifyDataSetChanged();
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    private void applyFilters() {
        List<Product> filteredProducts = productList.stream()
                .filter(product -> (selectedLocations.isEmpty() || brandList.stream().anyMatch(brand -> brand.getCompanyName().equalsIgnoreCase(product.getParentCompany()) && selectedLocations.contains(brand.getCountry()))) &&
                        (selectedBrands.isEmpty() || selectedBrands.contains(product.getParentCompany())) &&
                        (selectedCategories.isEmpty() || containsAnyCategory(product.getCategory(), selectedCategories)))
                .collect(Collectors.toList());

        updateProductList(filteredProducts);
        updateSortTextView();
    }


    @RequiresApi(api = Build.VERSION_CODES.N)
    private void filterProducts() {
        List<Product> filteredProducts = productList.stream()
                .filter(product -> (selectedLocations.isEmpty() || brandList.stream().anyMatch(brand -> brand.getCompanyName().equalsIgnoreCase(product.getParentCompany()) && selectedLocations.contains(brand.getCountry()))) &&
                        (selectedBrands.isEmpty() || selectedBrands.contains(product.getParentCompany())) &&
                        (selectedCategories.isEmpty() || containsAnyCategory(product.getCategory(), selectedCategories)))
                .collect(Collectors.toList());

        updateProductList(filteredProducts);
        updateSortTextView();
    }

    private boolean containsAnyCategory(String categories, List<String> selectedCategories) {
        if (categories == null || categories.isEmpty()) {
            return false;
        }
        List<String> categoryList = Arrays.asList(categories.split(","));
        for (String selectedCategory : selectedCategories) {
            if (categoryList.contains(getIDByName(selectedCategory))) {
                return true;
            }
        }
        return false;
    }

    private void updateSortTextView() {
        String sortText = "Sort by: ";
        if (!selectedBrands.isEmpty()) {
            sortText += "Brand, ";
        }
        if (!selectedCategories.isEmpty()) {
            sortText += "Category, ";
        }
        if (!selectedLocations.isEmpty()) {
            sortText += "Location, ";
        }
        if (sortText.endsWith(", ")) {
            sortText = sortText.substring(0, sortText.length() - 2); // Remove the trailing comma
        }
        sortTextView.setText(sortText);
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

    public void updateProductList(List<Product> productList) {
        if (boycpottAdapter != null) {
            searchList.clear();
            searchList.addAll(productList);
            updateMessage(productList.size());
            boycpottAdapter.setProductList(productList);
            boycpottAdapter.notifyDataSetChanged();

            List<String> productTitles = new ArrayList<>();
            for (Product product : searchList) {
                productTitles.add(product.getTitle());
            }
            ArrayAdapter<String> adapter = new ArrayAdapter<>(getActivity(), R.layout.simple_dropdown_item_custom, productTitles);
            //binding.searchET.setAdapter(adapter);
        }
    }

    private String getIDByName(String name) {
        for (Category category : categoryList) {
            if (category.getName().equalsIgnoreCase(name)) {
                return category.getId();
            }
        }
        return "";
    }

}