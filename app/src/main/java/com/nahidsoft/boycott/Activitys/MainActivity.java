package com.nahidsoft.boycott.Activitys;

import android.Manifest;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.MultiplePermissionsReport;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.multi.MultiplePermissionsListener;
import com.nahidsoft.boycott.Fragments.ListFragment;
import com.nahidsoft.boycott.Fragments.ScanFragment;
import com.nahidsoft.boycott.Models.BrandModel;
import com.nahidsoft.boycott.Models.Category;
import com.nahidsoft.boycott.Models.Country;
import com.nahidsoft.boycott.Models.Product;
import com.nahidsoft.boycott.R;
import com.nahidsoft.boycott.Utilitis.APIs;
import com.nahidsoft.boycott.databinding.ActivityMainBinding;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainActivity extends AppCompatActivity {
    ActivityMainBinding binding;
    String TAG = "MyTag";
    private List<Product> productList = new ArrayList<>();
    private SharedPreferences sharedPreferences;
    private static final String PREFS_NAME = "MyPrefs";
    private static final String PRODUCT_LIST_KEY = "ProductList";
    private static final String BRAND_LIST_KEY = "brand_list";
    List<BrandModel> brandList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        sharedPreferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        getBrands();
        getCategory();
        getCountry();
        if (isNetworkAvailable()) {
            getProducts();
        } else {
            loadProductListFromPreferences();
        }
        checkPermissions();
        bottomNav();
        fragmentLoad(new ListFragment());
        binding.fab.setOnClickListener(v -> {
            fragmentLoad(new ScanFragment());
        });
    }

    private void getCountry() {
        RequestQueue queue = Volley.newRequestQueue(this);

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, APIs.COUNTRY, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        List<Country> countries = new ArrayList<>();
                        try {
                            if (response.getString("status").equals("success")) {
                                JSONObject data = response.getJSONObject("data");
                                JSONArray countriesArray = data.getJSONArray("countries");

                                for (int i = 0; i < countriesArray.length(); i++) {
                                    JSONObject countryObj = countriesArray.getJSONObject(i);
                                    String id = countryObj.getString("id");
                                    String countryName = countryObj.getString("CountryName");
                                    countries.add(new Country(id, countryName));
                                }

                                saveCountryListToPreferences(countries);
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(MainActivity.this, "" + error.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> headers = new HashMap<>();
                headers.put("auth-key", APIs.KEY);
                return headers;
            }
        };

        queue.add(jsonObjectRequest);
    }

    private void getCategory() {
        RequestQueue requestQueue = Volley.newRequestQueue(this);
        List<Category> categoryList = new ArrayList<>();
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(
                Request.Method.GET,
                APIs.CATEGORY,
                null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            if (response.getString("status").equals("success")) {
                                JSONArray categories = response.getJSONObject("data").getJSONArray("categories");

                                for (int i = 0; i < categories.length(); i++) {
                                    JSONObject category = categories.getJSONObject(i);
                                    String id = category.getString("id");
                                    String name = category.getString("name");
                                    String categoryType = category.getString("category");

                                    // Create Category object and add to list
                                    Category categoryObj = new Category(id, name, categoryType);
                                    categoryList.add(categoryObj);
                                }
                                saveCategoryListToPreferences(categoryList);
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        error.printStackTrace();
                        Toast.makeText(MainActivity.this, "" + error.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }
        ) {
            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> headers = new HashMap<>();
                headers.put("auth-key", APIs.KEY);
                return headers;
            }
        };

        requestQueue.add(jsonObjectRequest);
    }

    private void getBrands() {
        RequestQueue requestQueue = Volley.newRequestQueue(this);
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(
                Request.Method.GET,
                APIs.BRAND,
                null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        Log.d(TAG, "Response: " + response.toString());
                        try {
                            String status = response.getString("status");
                            if (status.equals("success")) {
                                JSONObject data = response.getJSONObject("data");
                                JSONArray products = data.getJSONArray("products");
                                for (int i = 0; i < products.length(); i++) {
                                    JSONObject product = products.getJSONObject(i);
                                    String id = product.getString("id");
                                    String title = product.getString("title");
                                    String createdTime = product.getString("createdTime");
                                    String companyName = product.getString("companyName");
                                    String image = product.getString("image");
                                    String country = product.getString("country");
                                    String owner = product.getString("owner");
                                    String statusProduct = product.getString("status");

                                    Log.d(TAG, "Product ID: " + id);
                                    Log.d(TAG, "Product Title: " + title);
                                    BrandModel brand = new BrandModel(id, title, createdTime, companyName, image, country, owner, statusProduct);

                                    brandList.add(brand);
                                }
                                saveBrandListToPreferences(brandList);
                            } else {
                                Toast.makeText(MainActivity.this, "" + status, Toast.LENGTH_SHORT).show();
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.e(TAG, "Error: " + error.toString());
                        Toast.makeText(MainActivity.this, "Error " + error.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }
        ) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();
                params.put("auth-key", APIs.KEY);
                return params;
            }
        };

        requestQueue.add(jsonObjectRequest);
    }

    private void saveBrandListToPreferences(List<BrandModel> brandList) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        Gson gson = new Gson();
        String json = gson.toJson(brandList);
        editor.putString(BRAND_LIST_KEY, json);
        editor.apply();
    }

    private void getProducts() {
        StringRequest request = new StringRequest(Request.Method.GET, APIs.PRODUCT, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Log.d(TAG, "onResponse: " + response);
                try {
                    JSONObject jsonObject = new JSONObject(response);
                    String request_status = jsonObject.getString("status");

                    if ("success".equals(request_status)) {
                        JSONObject data = jsonObject.getJSONObject("data");
                        JSONArray products = data.getJSONArray("products");

                        for (int i = 0; i < products.length(); i++) {
                            JSONObject product = products.getJSONObject(i);

                            String id = product.getString("id");
                            String title = product.getString("title");
                            String createdTime = product.getString("createdTime");
                            String barCode = product.getString("barCode");
                            String companyName = product.getString("companyName");
                            String parentCompany = product.getString("parantcompany");
                            String reason = product.getString("reason");
                            String category = product.getString("category");
                            String statusProduct = product.getString("status");
                            String image = product.getString("image");

                            Product prod = new Product(id, title, createdTime, barCode, companyName, parentCompany, reason, category, statusProduct, image);
                            productList.add(prod);
                        }

                        // Store the product list in SharedPreferences
                        storeProductListInPreferences(productList);

                        // Notify ListFragment about the data change
                        ListFragment listFragment = (ListFragment) getSupportFragmentManager().findFragmentById(R.id.fragment_container);
                        if (listFragment != null) {
                            listFragment.updateProductList(productList);
                        }
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(MainActivity.this, "Error " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();
                params.put("auth-key", APIs.KEY);
                return params;
            }
        };
        RequestQueue requestQueue = Volley.newRequestQueue(getApplicationContext());
        requestQueue.add(request);
    }

    private void storeProductListInPreferences(List<Product> productList) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        Gson gson = new Gson();
        String json = gson.toJson(productList);
        editor.putString(PRODUCT_LIST_KEY, json);
        editor.apply();
    }

    private void loadProductListFromPreferences() {
        Gson gson = new Gson();
        String json = sharedPreferences.getString(PRODUCT_LIST_KEY, null);
        Type type = new TypeToken<List<Product>>() {
        }.getType();
        productList = gson.fromJson(json, type);

        if (productList == null) {
            productList = new ArrayList<>();
        }

        // Notify ListFragment about the data change
        ListFragment listFragment = (ListFragment) getSupportFragmentManager().findFragmentById(R.id.fragment_container);
        if (listFragment != null) {
            listFragment.updateProductList(productList);
        }
    }

    private void saveCountryListToPreferences(List<Country> countryList) {
        SharedPreferences sharedPreferences = getSharedPreferences("MySharedPref", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        JSONArray jsonArray = new JSONArray();
        for (Country country : countryList) {
            JSONObject jsonObject = new JSONObject();
            try {
                jsonObject.put("id", country.getId());
                jsonObject.put("countryName", country.getCountryName());
                jsonArray.put(jsonObject);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        editor.putString("countryList", jsonArray.toString());
        editor.apply();
    }

    private void saveCategoryListToPreferences(List<Category> categoryList) {
        SharedPreferences sharedPreferences = getSharedPreferences("MySharedPref", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        JSONArray jsonArray = new JSONArray();
        for (Category category : categoryList) {
            JSONObject jsonObject = new JSONObject();
            try {
                jsonObject.put("id", category.getId());
                jsonObject.put("name", category.getName());
                jsonObject.put("categoryType", category.getCategoryType());
                jsonArray.put(jsonObject);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        editor.putString("categoryList", jsonArray.toString());
        editor.apply();
    }

    public boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    public List<Product> getProductList() {
        return productList;
    }

    private void bottomNav() {
        ColorStateList colorStateList = ContextCompat.getColorStateList(this, R.color.nav_item_color);
        binding.bottomNavigationView.setItemIconTintList(colorStateList);
        binding.bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                if (item.getItemId() == R.id.theList) {
                    fragmentLoad(new ListFragment());
                } else if (item.getItemId() == R.id.donate) {
                    Toast.makeText(MainActivity.this, "Donate", Toast.LENGTH_SHORT).show();
                } else if (item.getItemId() == R.id.about) {
                    Toast.makeText(MainActivity.this, "About", Toast.LENGTH_SHORT).show();
                } else if (item.getItemId() == R.id.profile) {
                    Toast.makeText(MainActivity.this, "Profile", Toast.LENGTH_SHORT).show();
                }
                return true;
            }
        });
    }

    private void checkPermissions() {
        Dexter.withContext(getApplicationContext())
                .withPermissions(Manifest.permission.CAMERA, Manifest.permission.READ_EXTERNAL_STORAGE)
                .withListener(new MultiplePermissionsListener() {
                    @Override
                    public void onPermissionsChecked(MultiplePermissionsReport multiplePermissionsReport) {
                    }

                    @Override
                    public void onPermissionRationaleShouldBeShown(List<PermissionRequest> list, PermissionToken permissionToken) {
                        permissionToken.continuePermissionRequest();
                    }
                }).check();
    }

    private void fragmentLoad(Fragment fragment) {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.fragment_container, fragment);
        transaction.commit();
    }
}
