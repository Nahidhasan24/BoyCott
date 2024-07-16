package com.nahidsoft.boycott.Fragments;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
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
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.zxing.BinaryBitmap;
import com.google.zxing.DecodeHintType;
import com.google.zxing.MultiFormatReader;
import com.google.zxing.NotFoundException;
import com.google.zxing.Result;
import com.google.zxing.common.HybridBinarizer;
import com.journeyapps.barcodescanner.BarcodeCallback;
import com.journeyapps.barcodescanner.BarcodeResult;
import com.journeyapps.barcodescanner.DecoratedBarcodeView;
import com.nahidsoft.boycott.R;
import com.nahidsoft.boycott.Utilitis.APIs;
import com.nahidsoft.boycott.databinding.FragmentScanBinding;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.EnumMap;
import java.util.HashMap;
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

        com.google.zxing.RGBLuminanceSource source = new com.google.zxing.RGBLuminanceSource(bitmap.getWidth(), bitmap.getHeight(), intArray);
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
                        progressDialog.dismiss();
                        try {
                            JSONObject jsonObject = new JSONObject(response);
                            String status = jsonObject.getString("status");

                            if (status.equals("error")) {
                                String error = jsonObject.getString("error");
                                Toast.makeText(getActivity(), "" + error, Toast.LENGTH_SHORT).show();
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
                                background.setBackground(ContextCompat.getDrawable(getActivity(),R.drawable.red_result_bg));
                                statusTv.setText("Boycott");

                            }else  if (productStatus.equals("green")) {
                                background.setBackground(ContextCompat.getDrawable(getActivity(),R.drawable.green_result_bg));
                                statusTv.setText("Not Boycott");

                            }else  if (productStatus.equals("yellow")) {
                                background.setBackground(ContextCompat.getDrawable(getActivity(),R.drawable.yellow_result_bg));
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
}
