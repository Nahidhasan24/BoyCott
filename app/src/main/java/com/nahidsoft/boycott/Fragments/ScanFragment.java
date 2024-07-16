package com.nahidsoft.boycott.Fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.journeyapps.barcodescanner.BarcodeCallback;
import com.journeyapps.barcodescanner.BarcodeResult;
import com.nahidsoft.boycott.databinding.FragmentScanBinding;

public class ScanFragment extends Fragment {

    FragmentScanBinding binding;
    private boolean hasScanned = false;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentScanBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        startScan();
    }

    private void startScan() {
        binding.barcodeScanner.decodeContinuous(new BarcodeCallback() {
            @Override
            public void barcodeResult(BarcodeResult result) {
                if (result.getText() != null && !hasScanned) {
                    hasScanned = true;
                    checkTheResult(result.getText());
                }
            }
        });
    }

    private void checkTheResult(String text) {
        Toast.makeText(getActivity(), ""+text, Toast.LENGTH_SHORT).show();
        hasScanned = false;
        startScan();
    }

    @Override
    public void onResume() {
        super.onResume();
        hasScanned = false;  // Reset the flag when resuming the fragment
        binding.barcodeScanner.resume();
    }

    @Override
    public void onPause() {
        super.onPause();
        binding.barcodeScanner.pause();
    }
}

