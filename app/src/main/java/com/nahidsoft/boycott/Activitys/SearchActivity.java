package com.nahidsoft.boycott.Activitys;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.nahidsoft.boycott.Adapters.BrandAdapter;
import com.nahidsoft.boycott.Adapters.BrandSortAdapter;
import com.nahidsoft.boycott.Adapters.CategoryAdapter;
import com.nahidsoft.boycott.Adapters.CategorySortAdapter;
import com.nahidsoft.boycott.Adapters.LocationAdapter;
import com.nahidsoft.boycott.Adapters.LocationSortAdapter;
import com.nahidsoft.boycott.Adapters.ProducrsAdapter;
import com.nahidsoft.boycott.Models.BrandModel;
import com.nahidsoft.boycott.Models.Category;
import com.nahidsoft.boycott.Models.Country;
import com.nahidsoft.boycott.Models.Product;
import com.nahidsoft.boycott.R;
import com.nahidsoft.boycott.databinding.ActivitySearchBinding;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class SearchActivity extends AppCompatActivity implements BrandSortAdapter.OnItemClickListener, CategorySortAdapter.OnItemClickListener, LocationSortAdapter.OnItemClickListener {

    ActivitySearchBinding binding;
    ProducrsAdapter producrsAdapter;
    List<Product> searchList = new ArrayList<>();
    static final String PREFS_NAME = "MyPrefs";
    static final String BRAND_LIST_KEY = "brand_list";

    List<BrandModel> brandList = new ArrayList<>();
    List<Product> productList = new ArrayList<>();

    private List<Country> countryNames = new ArrayList<>();
    TextView sortTextView;
    ImageView close;

    List<Category> categoryList = new ArrayList<>();
    private List<String> selectedLocations = new ArrayList<>();
    private List<String> selectedBrands = new ArrayList<>();
    private List<String> selectedCategories = new ArrayList<>();
    AlertDialog dialog;

    private LocationAdapter locationAdapter;
    private BrandAdapter brandAdapter;
    private CategoryAdapter categoryAdapter;

    private AutoCompleteTextView searchViewBrand;
    private AutoCompleteTextView searchViewLocation;
    private AutoCompleteTextView searchViewCategory;
    private TextView btnUpdate, cancelBtn;

    private ChipGroup chipGroupLocations;
    private ChipGroup chipGroupBrands;
    private ChipGroup chipGroupCategories;

    private RecyclerView recyclerViewLocationSuggestions;
    private RecyclerView recyclerViewBrandSuggestions;
    private RecyclerView recyclerViewCategorySuggestions;

    private List<String> tempSelectedBrands = new ArrayList<>(); // Temporary storage for brands

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySearchBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        loadProductListFromPreferences();

        brandList = loadBrandListFromPreferences();
        categoryList = retrieveCategoriesFromSharedPreferences();
        countryNames = loadCountryListFromPreferences();

        binding.productArrayList.setHasFixedSize(true);
        binding.productArrayList.setLayoutManager(new GridLayoutManager(getApplicationContext(), 3));
        producrsAdapter = new ProducrsAdapter();
        binding.productArrayList.setAdapter(producrsAdapter);

        sortTextView = binding.sortTextView;

        updateProductList(productList);
        setupSearchAutoComplete();

        binding.filterBtn.setOnClickListener(v -> {
            tempSelectedBrands.clear();
            tempSelectedBrands.addAll(selectedBrands); // Save current selection temporarily
            showDialog();
        });

        binding.backpress.setOnClickListener(v -> {
            startActivity(new Intent(getApplicationContext(), MainActivity.class));
            finishAffinity();
        });
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
        AlertDialog.Builder builder = new AlertDialog.Builder(SearchActivity.this, R.style.TransparentDialog);
        LayoutInflater inflater = this.getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.activity_filter, null);
        builder.setView(dialogView);

        dialog = builder.create();
        dialog.show();

        int[] location = new int[2];
        anchorLayout.getLocationOnScreen(location);
        int anchorX = location[0];
        int anchorY = location[1];

        WindowManager.LayoutParams layoutParams = new WindowManager.LayoutParams();
        layoutParams.copyFrom(dialog.getWindow().getAttributes());
        layoutParams.width = WindowManager.LayoutParams.MATCH_PARENT;
        layoutParams.height = WindowManager.LayoutParams.WRAP_CONTENT;

        layoutParams.gravity = Gravity.TOP;
        layoutParams.x = 0;
        layoutParams.y = anchorY + anchorLayout.getHeight();

        dialog.getWindow().setAttributes(layoutParams);

        EditText searchBar = dialogView.findViewById(R.id.search_bar);
        RecyclerView recyclerView = dialogView.findViewById(R.id.recyclerView);
        TextView applyButton = dialogView.findViewById(R.id.apply_button);
        TextView clearAllButton = dialogView.findViewById(R.id.clear_all_button);

        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new GridLayoutManager(getApplicationContext(), 2));

        if (filterType.equals("Brand")) {
            List<BrandModel> filteredList = new ArrayList<>(brandList);
            BrandSortAdapter brandSortAdapter = new BrandSortAdapter(this::onBrandItemClick, getApplicationContext(), filteredList, selectedBrands);
            recyclerView.setAdapter(brandSortAdapter);

            searchBar.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    filterBrands(s.toString(), filteredList, brandSortAdapter);
                }

                @Override
                public void afterTextChanged(Editable s) { }
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
            CategorySortAdapter categorySortAdapter = new CategorySortAdapter(this::onCategoryItemClick, getApplicationContext(), filteredList, selectedCategories);
            recyclerView.setAdapter(categorySortAdapter);

            searchBar.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    filterCategories(s.toString(), filteredList, categorySortAdapter);
                }

                @Override
                public void afterTextChanged(Editable s) { }
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
            LocationSortAdapter locationSortAdapter = new LocationSortAdapter(this::onLocationItemClick, getApplicationContext(), filteredList, selectedLocations);
            recyclerView.setAdapter(locationSortAdapter);

            searchBar.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    filterLocations(s.toString(), filteredList, locationSortAdapter);
                }

                @Override
                public void afterTextChanged(Editable s) { }
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
    private void showDialog() {
        final Dialog dialog = new Dialog(SearchActivity.this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.popup_view);

        searchViewBrand = dialog.findViewById(R.id.searchViewBrand);
        searchViewLocation = dialog.findViewById(R.id.searchViewLocation);
        searchViewCategory = dialog.findViewById(R.id.searchViewCategory);
        btnUpdate = dialog.findViewById(R.id.btnUpdate);
        cancelBtn = dialog.findViewById(R.id.canelBtn);

        chipGroupLocations = dialog.findViewById(R.id.chipGroupLocations);
        chipGroupBrands = dialog.findViewById(R.id.chipGroupBrands);
        chipGroupCategories = dialog.findViewById(R.id.chipGroupCategories);

        recyclerViewLocationSuggestions = dialog.findViewById(R.id.recyclerViewLocationSuggestions);
        recyclerViewBrandSuggestions = dialog.findViewById(R.id.recyclerViewBrandSuggestions);
        recyclerViewCategorySuggestions = dialog.findViewById(R.id.recyclerViewCategorySuggestions);

        locationAdapter = new LocationAdapter(countryNames, this::onLocationItemClick);
        brandAdapter = new BrandAdapter(brandList, this::onBrandItemClick);
        categoryAdapter = new CategoryAdapter(categoryList, this::onCategoryItemClick);

        recyclerViewLocationSuggestions.setLayoutManager(new LinearLayoutManager(getApplicationContext(), LinearLayoutManager.HORIZONTAL, false));
        recyclerViewLocationSuggestions.setAdapter(locationAdapter);

        recyclerViewBrandSuggestions.setLayoutManager(new LinearLayoutManager(getApplicationContext(), LinearLayoutManager.HORIZONTAL, false));
        recyclerViewBrandSuggestions.setAdapter(brandAdapter);

        recyclerViewCategorySuggestions.setLayoutManager(new LinearLayoutManager(getApplicationContext(), LinearLayoutManager.HORIZONTAL, false));
        recyclerViewCategorySuggestions.setAdapter(categoryAdapter);

        cancelBtn.setOnClickListener(v -> {
            selectedBrands.clear();
            selectedBrands.addAll(tempSelectedBrands); // Restore saved selection
            updateProductList(productList);
            dialog.dismiss();
        });

        searchViewLocation.setOnItemClickListener((parent, view, position, id) -> {
            String selectedLocation = (String) parent.getItemAtPosition(position);
            addChipToGroup(chipGroupLocations, selectedLocation, selectedLocations);
            searchViewLocation.setText("");
        });

        searchViewBrand.setOnItemClickListener((parent, view, position, id) -> {
            String selectedBrand = (String) parent.getItemAtPosition(position);
            addChipToGroup(chipGroupBrands, selectedBrand, tempSelectedBrands);
            searchViewBrand.setText("");
        });

        searchViewCategory.setOnItemClickListener((parent, view, position, id) -> {
            String selectedCategory = (String) parent.getItemAtPosition(position);
            addChipToGroup(chipGroupCategories, selectedCategory, selectedCategories);
            searchViewCategory.setText("");
        });

        close = dialog.findViewById(R.id.closeBtn);
        close.setOnClickListener(v -> {
            updateProductList(productList);
            dialog.dismiss();
        });

        btnUpdate.setOnClickListener(v -> {
            selectedBrands.clear();
            selectedBrands.addAll(tempSelectedBrands); // Confirm selection
            tempSelectedBrands.clear(); // Clear temporary storage since changes are confirmed
            filterProducts();
            dialog.dismiss();
        });

        addSelectedChips(chipGroupBrands, tempSelectedBrands); // Show chips for selected brands
        dialog.show();
        dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.getWindow().getAttributes().windowAnimations = R.style.DialogAnimation;
        dialog.getWindow().setGravity(Gravity.BOTTOM);

        setupAutoCompleteAdapters();
    }

    private void clearSelections() {
        selectedLocations.clear();
        selectedBrands.clear();
        selectedCategories.clear();
        if (chipGroupLocations != null) {
            chipGroupLocations.removeAllViews();
        }
        if (chipGroupBrands != null) {
            chipGroupBrands.removeAllViews();
        }
        if (chipGroupCategories != null) {
            chipGroupCategories.removeAllViews();
        }
        sortTextView.setText("");
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    private void setupAutoCompleteAdapters() {
        List<String> locationNames = countryNames.stream().map(Country::getCountryName).collect(Collectors.toList());
        List<String> brandNames = brandList.stream().map(BrandModel::getCompanyName).collect(Collectors.toList());
        List<String> categoryNames = categoryList.stream().map(Category::getName).collect(Collectors.toList());

        ArrayAdapter<String> locationAdapter = new ArrayAdapter<>(getApplicationContext(), R.layout.simple_dropdown_item_custom, locationNames);
        ArrayAdapter<String> brandAdapter = new ArrayAdapter<>(getApplicationContext(), R.layout.simple_dropdown_item_custom, brandNames);
        ArrayAdapter<String> categoryAdapter = new ArrayAdapter<>(getApplicationContext(), R.layout.simple_dropdown_item_custom, categoryNames);

        searchViewLocation.setAdapter(locationAdapter);
        searchViewBrand.setAdapter(brandAdapter);
        searchViewCategory.setAdapter(categoryAdapter);

        searchViewLocation.setThreshold(1);
        searchViewBrand.setThreshold(1);
        searchViewCategory.setThreshold(1);
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    private void addSelectedChips(ChipGroup chipGroup, List<String> selectedItems) {
        if (chipGroup != null) {
            chipGroup.removeAllViews();
            for (String item : selectedItems) {
                addChipToGroup(chipGroup, item, selectedItems);
            }
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    private void addChipToGroup(ChipGroup chipGroup, String text, List<String> selectedItems) {
        if (chipGroup != null && text != null && !selectedItems.contains(text)) {
            selectedItems.add(text);
            Chip chip = new Chip(SearchActivity.this);
            chip.setText(text);
            chip.setCloseIconVisible(true);
            chip.setOnCloseIconClickListener(v -> {
                selectedItems.remove(text);
                chipGroup.removeView(chip);
                filterProducts(); // Update the product list when a chip is removed
            });
            chipGroup.addView(chip);
        }
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

    @RequiresApi(api = Build.VERSION_CODES.N)
    private void onLocationItemClick(Country selectedCountry) {
        addChipToGroup(chipGroupLocations, selectedCountry.getCountryName(), selectedLocations);
        filterProducts(); // Update the product list when an item is clicked
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    private void onBrandItemClick(BrandModel selectedBrand) {
        addChipToGroup(chipGroupBrands, selectedBrand.getCompanyName(), selectedBrands);
        filterProducts(); // Update the product list when an item is clicked
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    private void onCategoryItemClick(Category selectedCategory) {
        addChipToGroup(chipGroupCategories, selectedCategory.getName(), selectedCategories);
        filterProducts(); // Update the product list when an item is clicked
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
        SharedPreferences sharedPreferences = getSharedPreferences("MySharedPref", Context.MODE_PRIVATE);
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
        SharedPreferences sharedPreferences = getSharedPreferences("MySharedPref", Context.MODE_PRIVATE);
        Gson gson = new Gson();
        String json = sharedPreferences.getString("categoryList", null);
        Type type = new TypeToken<ArrayList<Category>>() {}.getType();
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
        ArrayAdapter<String> adapter = new ArrayAdapter<>(getApplicationContext(), R.layout.simple_dropdown_item_custom, productTitles);
        binding.searchET.setAdapter(adapter);
        binding.searchET.setThreshold(1);

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

        binding.searchET.setOnItemClickListener((parent, view, position, id) -> {
            String selectedItem = (String) parent.getItemAtPosition(position);
            filter(selectedItem);
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
            ArrayAdapter<String> adapter = new ArrayAdapter<>(getApplicationContext(), R.layout.simple_dropdown_item_custom, productTitles);
            binding.searchET.setAdapter(adapter);
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

    private void loadProductListFromPreferences() {
        SharedPreferences sharedPreferences = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
        Gson gson = new Gson();
        String json = sharedPreferences.getString("ProductList", null);
        Type type = new TypeToken<List<Product>>() {}.getType();
        productList = gson.fromJson(json, type);

        if (productList == null) {
            productList = new ArrayList<>();
        }
    }
}
