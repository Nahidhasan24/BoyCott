package com.nahidsoft.boycott.Fragments;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.HttpHeaderParser;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.google.zxing.BinaryBitmap;
import com.google.zxing.DecodeHintType;
import com.google.zxing.MultiFormatReader;
import com.google.zxing.NotFoundException;
import com.google.zxing.RGBLuminanceSource;
import com.google.zxing.Result;
import com.google.zxing.common.HybridBinarizer;
import com.journeyapps.barcodescanner.BarcodeCallback;
import com.journeyapps.barcodescanner.BarcodeResult;
import com.journeyapps.barcodescanner.DecoratedBarcodeView;
import com.nahidsoft.boycott.Models.Product;
import com.nahidsoft.boycott.R;
import com.nahidsoft.boycott.Utilitis.APIs;
import com.nahidsoft.boycott.databinding.FragmentScanBinding;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ScanFragment extends Fragment {

    private static final int REQUEST_IMAGE_SELECT = 1;
    FragmentScanBinding binding;
    boolean hasScanned = false;
    DecoratedBarcodeView barcodeView;
    ProgressDialog progressDialog;
    String productStatus;
    LinearLayout background;
    TextView statusTv;
    private List<Product> productList;
    private static final String TAG = "MainActivity";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentScanBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        progressDialog = new ProgressDialog(getActivity());
        progressDialog.setTitle("Loading....");
        progressDialog.setCancelable(false);
        loadProductListFromPreferences();
        barcodeView = view.findViewById(R.id.barcode_scanner);

        background = view.findViewById(R.id.backgroundColor);
        statusTv = view.findViewById(R.id.statusTv);

        RelativeLayout selectImageGallery = view.findViewById(R.id.selectImageGallery);
        selectImageGallery.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectImageFromGallery();
            }
        });
        startScan();
    }

    private void loadProductListFromPreferences() {
        SharedPreferences sharedPreferences = getActivity().getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
        Gson gson = new Gson();
        String json = sharedPreferences.getString("ProductList", null);
        Type type = new TypeToken<List<Product>>() {
        }.getType();
        productList = gson.fromJson(json, type);

        if (productList == null) {
            productList = new ArrayList<>();
        }
    }

    private void startScan() {
        barcodeView.decodeContinuous(new BarcodeCallback() {
            @Override
            public void barcodeResult(BarcodeResult result) {
                if (result.getText() != null && !hasScanned) {
                    hasScanned = true;
                    checkTheResult(result.getText());
                }
            }
        });
    }

    private void selectImageFromGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, REQUEST_IMAGE_SELECT);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_IMAGE_SELECT && resultCode == Activity.RESULT_OK && data != null) {
            Uri selectedImageUri = data.getData();
            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getActivity().getContentResolver(), selectedImageUri);
                scanBarcodeFromBitmap(bitmap);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void scanBarcodeFromBitmap(Bitmap bitmap) {
        int[] intArray = new int[bitmap.getWidth() * bitmap.getHeight()];
        bitmap.getPixels(intArray, 0, bitmap.getWidth(), 0, 0, bitmap.getWidth(), bitmap.getHeight());

        RGBLuminanceSource source = new RGBLuminanceSource(bitmap.getWidth(), bitmap.getHeight(), intArray);
        BinaryBitmap binaryBitmap = new BinaryBitmap(new HybridBinarizer(source));

        MultiFormatReader reader = new MultiFormatReader();
        Map<DecodeHintType, Object> hints = new EnumMap<>(DecodeHintType.class);
        hints.put(DecodeHintType.TRY_HARDER, Boolean.TRUE);
        reader.setHints(hints);

        try {
            Result result = reader.decode(binaryBitmap);
            checkTheResult(result.getText());
        } catch (NotFoundException e) {
            Toast.makeText(getActivity(), "No barcode found", Toast.LENGTH_SHORT).show();
        }
    }

    private void checkTheResult(String text) {
        progressDialog.show();
        RequestQueue requestQueue = Volley.newRequestQueue(getActivity());
        StringRequest stringRequest = new StringRequest(Request.Method.POST, APIs.CHECK,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {

                        try {
                            JSONObject jsonObject = new JSONObject(response);
                            String status = jsonObject.getString("status");

                            if (status.equals("error")) {
                                barCodeLookUp(text);
                            } else {
                                progressDialog.dismiss();
                            }
                            JSONObject productData = jsonObject.getJSONObject("product_data");
                            int id = productData.getInt("id");
                            String title = productData.getString("title");
                            String createdTime = productData.getString("createdTime");
                            String barCode = productData.getString("barCode");
                            String companyName = productData.getString("companyName");
                            String parentCompany = productData.getString("parantcompany");
                            String reason = productData.getString("reason");
                            String category = productData.getString("category");
                            productStatus = productData.getString("status");
                            String image = productData.getString("image");

                            if (productStatus.equals("red")) {
                                background.setBackground(ContextCompat.getDrawable(getActivity(), R.drawable.red_result_bg));
                                statusTv.setText("Boycott");

                            } else if (productStatus.equals("green")) {
                                background.setBackground(ContextCompat.getDrawable(getActivity(), R.drawable.green_result_bg));
                                statusTv.setText("Not Boycott");

                            } else if (productStatus.equals("yellow")) {
                                background.setBackground(ContextCompat.getDrawable(getActivity(), R.drawable.yellow_result_bg));
                                statusTv.setText("Yellow");

                            }


                            Log.d("Response", "Title: " + title);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        progressDialog.dismiss();
                        Toast.makeText(getActivity(), "" + error.getMessage(), Toast.LENGTH_SHORT).show();
                        error.printStackTrace();
                    }
                }) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();
                params.put("barCode", text);
                return params;
            }

            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> headers = new HashMap<>();
                headers.put("auth-key", APIs.KEY);
                return headers;
            }
        };

        requestQueue.add(stringRequest);
    }

    private void barCodeLookUp(String code) {

        RequestQueue requestQueue = Volley.newRequestQueue(getActivity());
        ProductRequest productRequest = new ProductRequest(
                Request.Method.GET, APIs.LOOKUP + code + ".json",
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            // Log the raw JSON response
                            Log.d(TAG, "Raw Response: " + response.toString());

                            int status = response.optInt("status", -1);
                            Log.d(TAG, "Status: " + status);

                            if (response.has("product")) {
                                JSONObject product = response.getJSONObject("product");
                                Log.d(TAG, "Product object found: " + product.toString());

                                String productName = product.optString("product_name", "N/A");
                                Log.d(TAG, "Product Name: " + productName);

                                String brands = product.optString("brands", "N/A");
                                Log.d(TAG, "Brands: " + brands);

                                String imageUrl = "N/A";
                                if (product.has("selected_images")) {
                                    JSONObject selectedImages = product.getJSONObject("selected_images");
                                    Log.d(TAG, "Selected Images object found: " + selectedImages.toString());

                                    if (selectedImages.has("front")) {
                                        JSONObject front = selectedImages.getJSONObject("front");
                                        Log.d(TAG, "Front object found: " + front.toString());

                                        if (front.has("display")) {
                                            JSONObject display = front.getJSONObject("display");
                                            Log.d(TAG, "Display object found: " + display.toString());

                                            imageUrl = display.optString("en", "N/A");
                                            Log.d(TAG, "Image URL: " + imageUrl);
                                        }
                                    }
                                }

                                if (status == 0) {
                                    progressDialog.dismiss();
                                    Toast.makeText(getActivity(), "Product not found !", Toast.LENGTH_SHORT).show();
                                } else {

                                    processData(productName, brands, imageUrl, code);
                                }

                            } else {
                                Log.e(TAG, "Product key not found in the response");
                                Toast.makeText(getActivity(), "Product details not found", Toast.LENGTH_LONG).show();
                            }
                        } catch (JSONException e) {
                            Log.e(TAG, "JSON Parsing Error: " + e.getMessage());
                            Toast.makeText(getActivity(), "Error parsing product details", Toast.LENGTH_LONG).show();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        // Handle error
                        barCodeLookUp(code);
                        Log.e(TAG, "Volley Error: " + error.toString());
                        //Toast.makeText(getActivity(), "Error fetching product details", Toast.LENGTH_LONG).show();
                    }
                }
        );

        requestQueue.add(productRequest);
    }

    private void processData(String productName, String brands, String imageUrl, String code) {
        String status = "", parentCompany = "", category = "", reason = "";
        for (int i = 0; i < productList.size(); i++) {
            if (productList.get(i).getCompanyName().toLowerCase().equals(brands.toLowerCase())) {
                Toast.makeText(getActivity(), "Status for " + productName + " " + productList.get(i).getStatus(), Toast.LENGTH_SHORT).show();
                status = productList.get(i).getStatus();
                parentCompany = productList.get(i).getParentCompany();
                category = productList.get(i).getCategory();
                reason = productList.get(i).getReason();
                break;
            }
        }

        sendAllData(productName,brands,parentCompany,category,imageUrl,code,status,reason);
    }

    private void sendAllData(String productName, String brands, String parentCompany, String category, String imageUrl, String code, String status, String reason) {


        RequestQueue requestQueue = Volley.newRequestQueue(getActivity());
        StringRequest stringRequest = new StringRequest(Request.Method.POST, APIs.CHECK,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONObject jsonObject = new JSONObject(response);
                            String status = jsonObject.getString("status");


                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        progressDialog.dismiss();
                        Toast.makeText(getActivity(), "" + error.getMessage(), Toast.LENGTH_SHORT).show();
                        error.printStackTrace();
                    }
                }) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();
                params.put("barCode", text);
                return params;
            }

            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> headers = new HashMap<>();
                headers.put("auth-key", APIs.KEY);
                return headers;
            }
        };

        requestQueue.add(stringRequest);

    }

    @Override
    public void onResume() {
        super.onResume();
        hasScanned = false;  // Reset the flag when resuming the fragment
        barcodeView.resume();
    }

    @Override
    public void onPause() {
        super.onPause();
        barcodeView.pause();
    }

    public static class ProductRequest extends Request<JSONObject> {

        private final Response.Listener<JSONObject> listener;

        public ProductRequest(int method, String url, Response.Listener<JSONObject> listener, Response.ErrorListener errorListener) {
            super(method, url, errorListener);
            this.listener = listener;
        }

        @Override
        protected Response<JSONObject> parseNetworkResponse(NetworkResponse response) {
            try {
                String jsonString = new String(response.data, HttpHeaderParser.parseCharset(response.headers));
                Log.d(TAG, "Raw Network Response: " + jsonString);

                JSONObject jsonResponse = new JSONObject(jsonString);
                return Response.success(jsonResponse, HttpHeaderParser.parseCacheHeaders(response));
            } catch (UnsupportedEncodingException | JSONException e) {
                Log.e(TAG, "Error parsing network response: " + e.getMessage());
                return Response.error(new VolleyError(e));
            }
        }

        @Override
        protected void deliverResponse(JSONObject response) {
            listener.onResponse(response);
        }
    }
}
