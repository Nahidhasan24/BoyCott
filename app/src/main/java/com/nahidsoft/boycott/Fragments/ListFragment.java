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
import android.widget.ListView;
import android.widget.TextView;

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
    TextView sortTextView;
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

        sortTextView = binding.sortTextView;
        setupSpinners();
        if (mainActivity != null) {
            productList = mainActivity.getProductList();
            updateProductList(productList);
        }
        setupSearchAutoComplete();
//        binding.filterBtn.setOnClickListener(v -> {
//            clearSelections();
//            showDialog();
//        });
//        binding.boycottListBtn.setOnClickListener(v -> {
//            startActivity(new Intent(getActivity(), BoycottActivity.class));
//        });
//        binding.mylistBtn.setOnClickListener(v -> {
//            startActivity(new Intent(getActivity(), MyListActivity.class));
//        });
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

        cancelBtn.setOnClickListener(v -> {
            updateProductList(productList);
            dialog.dismiss();
        });

        searchViewLocation.setOnItemClickListener((parent, view, position, id) -> {
            searchViewLocation.setText("");
        });

        searchViewBrand.setOnItemClickListener((parent, view, position, id) -> {
            searchViewBrand.setText("");
        });

        searchViewCategory.setOnItemClickListener((parent, view, position, id) -> {
            searchViewCategory.setText("");
        });

        close = dialog.findViewById(R.id.closeBtn);
        close.setOnClickListener(v -> {
            updateProductList(productList);
            dialog.dismiss();
        });

        btnUpdate.setOnClickListener(v -> {
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

    @RequiresApi(api = Build.VERSION_CODES.N)
    private void setupSpinners() {
        binding.locationBtn.setOnClickListener(v -> showSearchableSpinnerDialog(
                countryNames.stream().map(Country::getCountryName).collect(Collectors.toList()),
                item -> {
                    if (item.equals("Remove selection")) {
                        selectedLocations.clear();
                        binding.textViewSpinnerLocation.setText("Location");
                    } else {
                        selectedLocations.clear();
                        selectedLocations.add(item);
                        binding.textViewSpinnerLocation.setText(item);
                    }
                    filterProducts();
                }
        ));

        binding.brandBtn.setOnClickListener(v -> showSearchableSpinnerDialog(
                brandList.stream().map(BrandModel::getCompanyName).collect(Collectors.toList()),
                item -> {
                    if (item.equals("Remove selection")) {
                        selectedBrands.clear();
                        binding.textViewSpinnerBrand.setText("Brand");
                    } else {
                        selectedBrands.clear();
                        selectedBrands.add(item);
                        binding.textViewSpinnerBrand.setText(item);
                    }
                    filterProducts();
                }
        ));

        binding.categoryBtn.setOnClickListener(v -> showSearchableSpinnerDialog(
                categoryLis.stream().map(Category::getName).collect(Collectors.toList()),
                item -> {
                    if (item.equals("Remove selection")) {
                        selectedCategories.clear();
                        binding.textViewSpinnerCategory.setText("Category");
                    } else {
                        selectedCategories.clear();
                        selectedCategories.add(item);
                        binding.textViewSpinnerCategory.setText(item);
                    }
                    filterProducts();
                }
        ));
    }

    private void showSearchableSpinnerDialog(List<String> items, OnItemSelectedListener listener) {
        Dialog dialog = new Dialog(getActivity(), R.style.CustomDialogTheme);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_searchable_spinner);
        dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);

        AutoCompleteTextView searchAutoCompleteTextView = dialog.findViewById(R.id.searchAutoCompleteTextView);
        ListView listView = dialog.findViewById(R.id.listView);

        // Add "Remove selection" option at the top
        List<String> itemsWithRemoveOption = new ArrayList<>();
        itemsWithRemoveOption.add("Remove selection");
        itemsWithRemoveOption.addAll(items);

        ArrayAdapter<String> adapter = new ArrayAdapter<>(getActivity(), R.layout.custom_list_item, itemsWithRemoveOption);
        listView.setAdapter(adapter);

        searchAutoCompleteTextView.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                adapter.getFilter().filter(s);
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        listView.setOnItemClickListener((parent, view, position, id) -> {
            String selectedItem = adapter.getItem(position);
            listener.onItemSelected(selectedItem);
            dialog.dismiss();
        });

        dialog.show();
    }


    interface OnItemSelectedListener {
        void onItemSelected(String item);
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
        Type type = new TypeToken<ArrayList<Category>>() {}.getType();
        List<Category> categoryList = gson.fromJson(json, type);
        if (categoryList == null) {
            categoryList = new ArrayList<>();
        }
        return categoryList;
    }

    private void setupSearchAutoComplete() {
//        List<String> productTitles = new ArrayList<>();
//        for (Product product : searchList) {
//            productTitles.add(product.getTitle());
//        }
//        ArrayAdapter<String> adapter = new ArrayAdapter<>(getActivity(), R.layout.simple_dropdown_item_custom, productTitles);
//        binding.searchET.setAdapter(adapter);
//        binding.searchET.setThreshold(1);
//
//        binding.searchET.addTextChangedListener(new TextWatcher() {
//            @Override
//            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
//
//            @Override
//            public void onTextChanged(CharSequence s, int start, int before, int count) {
//                filter(s.toString());
//            }
//
//            @Override
//            public void afterTextChanged(Editable s) {}
//        });
//
//        binding.searchET.setOnItemClickListener((parent, view, position, id) -> {
//            String selectedItem = (String) parent.getItemAtPosition(position);
//            filter(selectedItem);
//        });
//
//        binding.searchET.setOnTouchListener((v, event) -> {
//            if (event.getAction() == MotionEvent.ACTION_UP) {
//                if (event.getRawX() >= (binding.searchET.getRight() - binding.searchET.getCompoundDrawables()[2].getBounds().width())) {
//                    binding.searchET.setText("");
//                    return true;
//                }
//            }
//            return false;
//        });
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
            //binding.searchET.setAdapter(adapter);
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
