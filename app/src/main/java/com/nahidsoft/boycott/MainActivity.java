package com.nahidsoft.boycott;

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
import com.nahidsoft.boycott.Models.Product;
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
                            String message = response.getString("message");
                            JSONObject data = response.getJSONObject("data");
                            if (status.equals("success")){
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
                            }else{
                                Toast.makeText(MainActivity.this, ""+status, Toast.LENGTH_SHORT).show();
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
                        Toast.makeText(MainActivity.this, "Error "+error.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }
        ){
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();
                params.put("auth-key", "9LgNbthL5xdgWgGAYmY9LtOCUgAgSYqsRQC1kItF4pbIzp2oiw");
                return params;
            }
        };

        requestQueue.add(jsonObjectRequest);
    }
    private void saveBrandListToPreferences(List<BrandModel> brandList) {
        SharedPreferences sharedPreferences = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        JSONArray jsonArray = new JSONArray();
        for (BrandModel brand : brandList) {
            JSONObject jsonObject = new JSONObject();
            try {
                jsonObject.put("id", brand.getId());
                jsonObject.put("title", brand.getTitle());
                jsonObject.put("createdTime", brand.getCreatedTime());
                jsonObject.put("companyName", brand.getCompanyName());
                jsonObject.put("image", brand.getImage());
                jsonObject.put("country", brand.getCountry());
                jsonObject.put("owner", brand.getOwner());
                jsonObject.put("status", brand.getStatus());
                jsonArray.put(jsonObject);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        editor.putString(BRAND_LIST_KEY, jsonArray.toString());
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
                params.put("auth-key", "9LgNbthL5xdgWgGAYmY9LtOCUgAgSYqsRQC1kItF4pbIzp2oiw");
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
        Type type = new TypeToken<List<Product>>() {}.getType();
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
