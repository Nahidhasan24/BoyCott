package com.nahidsoft.boycott.Fragments;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
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
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

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
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.ImageView;

import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.nahidsoft.boycott.Activitys.BoycottActivity;
import com.nahidsoft.boycott.Activitys.MyListActivity;
import com.nahidsoft.boycott.Adapters.BrandAdapter;
import com.nahidsoft.boycott.Adapters.CategoryAdapter;
import com.nahidsoft.boycott.Adapters.LocationAdapter;
import com.nahidsoft.boycott.Adapters.ProducrsAdapter;
import com.nahidsoft.boycott.Utilitis.CustomSpinnerAdapter;
import com.nahidsoft.boycott.Activitys.MainActivity;
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
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

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
    Button boycott, mylist;

    ImageView close;

    private LocationAdapter locationAdapter;
    private BrandAdapter brandAdapter;
    private CategoryAdapter categoryAdapter;

    private AutoCompleteTextView searchViewBrand;
    private AutoCompleteTextView searchViewLocation;
    private AutoCompleteTextView searchViewCategory;
    private Button btnUpdate, cancelBtn;

    private ChipGroup chipGroupLocations;
    private ChipGroup chipGroupBrands;
    private ChipGroup chipGroupCategories;

    private RecyclerView recyclerViewLocationSuggestions;
    private RecyclerView recyclerViewBrandSuggestions;
    private RecyclerView recyclerViewCategorySuggestions;

    // To store selected filters
    private List<String> selectedLocations = new ArrayList<>();
    private List<String> selectedBrands = new ArrayList<>();
    private List<String> selectedCategories = new ArrayList<>();

    // To store previous selections temporarily
    private List<String> tempSelectedLocations = new ArrayList<>();
    private List<String> tempSelectedBrands = new ArrayList<>();
    private List<String> tempSelectedCategories = new ArrayList<>();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentListBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
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
        binding.boycottListBtn.setOnClickListener(v -> {
            startActivity(new Intent(getActivity(), BoycottActivity.class));
        });
        binding.mylistBtn.setOnClickListener(v -> {
            startActivity(new Intent(getActivity(), MyListActivity.class));
        });
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    private void showDialog() {

        final Dialog dialog = new Dialog(getActivity());
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
        categoryAdapter = new CategoryAdapter(categoryLis, this::onCategoryItemClick);

        recyclerViewLocationSuggestions.setLayoutManager(new LinearLayoutManager(getActivity(), LinearLayoutManager.HORIZONTAL, false));
        recyclerViewLocationSuggestions.setAdapter(locationAdapter);

        recyclerViewBrandSuggestions.setLayoutManager(new LinearLayoutManager(getActivity(), LinearLayoutManager.HORIZONTAL, false));
        recyclerViewBrandSuggestions.setAdapter(brandAdapter);

        recyclerViewCategorySuggestions.setLayoutManager(new LinearLayoutManager(getActivity(), LinearLayoutManager.HORIZONTAL, false));
        recyclerViewCategorySuggestions.setAdapter(categoryAdapter);

        // Restore previous selections
        addSelectedChips(chipGroupLocations, tempSelectedLocations);
        addSelectedChips(chipGroupBrands, tempSelectedBrands);
        addSelectedChips(chipGroupCategories, tempSelectedCategories);

        cancelBtn.setOnClickListener(v -> {
            selectedLocations.clear();
            selectedBrands.clear();
            selectedCategories.clear();
            updateProductList(productList);
            dialog.dismiss();
        });

        searchViewLocation.setOnItemClickListener((parent, view, position, id) -> {
            String query = (String) parent.getItemAtPosition(position);
            addChipToGroup(chipGroupLocations, query, selectedLocations);
        });

        searchViewBrand.setOnItemClickListener((parent, view, position, id) -> {
            String query = (String) parent.getItemAtPosition(position);
            addChipToGroup(chipGroupBrands, query, selectedBrands);
        });

        searchViewCategory.setOnItemClickListener((parent, view, position, id) -> {
            String query = (String) parent.getItemAtPosition(position);
            addChipToGroup(chipGroupCategories, query, selectedCategories);
        });

        close = dialog.findViewById(R.id.closeBtn);
        close.setOnClickListener(v -> {
            dialog.dismiss();
        });

        btnUpdate.setOnClickListener(v -> {
            // Save current selections
            tempSelectedLocations.clear();
            tempSelectedLocations.addAll(selectedLocations);
            tempSelectedBrands.clear();
            tempSelectedBrands.addAll(selectedBrands);
            tempSelectedCategories.clear();
            tempSelectedCategories.addAll(selectedCategories);

            filterProducts();
            dialog.dismiss();
        });

        dialog.show();
        dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.getWindow().getAttributes().windowAnimations = R.style.DialogAnimation;
        dialog.getWindow().setGravity(Gravity.BOTTOM);

        setupAutoCompleteAdapters();
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    private void setupAutoCompleteAdapters() {
        List<String> locationNames = countryNames.stream().map(Country::getCountryName).collect(Collectors.toList());
        List<String> brandNames = brandList.stream().map(BrandModel::getCompanyName).collect(Collectors.toList());
        List<String> categoryNames = categoryLis.stream().map(Category::getName).collect(Collectors.toList());

        ArrayAdapter<String> locationAdapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_dropdown_item_1line, locationNames);
        ArrayAdapter<String> brandAdapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_dropdown_item_1line, brandNames);
        ArrayAdapter<String> categoryAdapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_dropdown_item_1line, categoryNames);

        searchViewLocation.setAdapter(locationAdapter);
        searchViewBrand.setAdapter(brandAdapter);
        searchViewCategory.setAdapter(categoryAdapter);

        searchViewLocation.setThreshold(1);
        searchViewBrand.setThreshold(1);
        searchViewCategory.setThreshold(1);
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    private void addSelectedChips(ChipGroup chipGroup, List<String> selectedItems) {
        chipGroup.removeAllViews();
        for (String item : selectedItems) {
            addChipToGroup(chipGroup, item, selectedItems);
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    private void addChipToGroup(ChipGroup chipGroup, String text, List<String> selectedItems) {
        if (!selectedItems.contains(text)) {
            selectedItems.add(text);
            Chip chip = new Chip(getActivity());
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

    private void setupSpinners() {
        setupSpinnerCountry(countryNames);
        setupSpinnerBrand(brandList);
        setupSpinnerCategory(categoryLis);

        binding.spinnerBrand.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @RequiresApi(api = Build.VERSION_CODES.N)
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selectedBrand = parent.getItemAtPosition(position).toString();
                if (!selectedBrand.equals("Brand")) {
                    addChipToGroup(chipGroupBrands, selectedBrand, selectedBrands);
                    filterProducts();
                }
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
                String selectedCategory = parent.getItemAtPosition(position).toString();
                if (!selectedCategory.equals("Category")) {
                    addChipToGroup(chipGroupCategories, selectedCategory, selectedCategories);
                    filterProducts();
                }
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
                String selectedLocation = parent.getItemAtPosition(position).toString();
                if (!selectedLocation.equals("Location")) {
                    addChipToGroup(chipGroupLocations, selectedLocation, selectedLocations);
                    filterProducts();
                }
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
                boolean matchesBrand = "Brand".equals(selectedBrand) || selectedBrands.contains(product.getParentCompany());
                boolean matchesCategory = "Category".equals(selectedCategory) || containsAnyCategory(product.getCategory(), selectedCategories);
                boolean matchesLocation = "Location".equals(selectedLocation) || brandList.stream().anyMatch(bm -> bm.getCompanyName().equalsIgnoreCase(product.getParentCompany()) && selectedLocations.contains(bm.getCountry()));

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
