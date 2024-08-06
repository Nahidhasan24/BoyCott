package com.nahidsoft.boycott.Fragments;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;

import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;

import com.nahidsoft.boycott.Activitys.MainActivity;
import com.nahidsoft.boycott.Adapters.BoycpottAdapter;
import com.nahidsoft.boycott.Models.Product;
import com.nahidsoft.boycott.R;
import com.nahidsoft.boycott.Utilitis.ProductRepository;
import com.nahidsoft.boycott.databinding.ActivityMyListBinding;
import com.nahidsoft.boycott.databinding.FragmentMyListBinding;

import java.util.ArrayList;
import java.util.List;

public class MyListFragment extends Fragment {

    FragmentMyListBinding binding;
    BoycpottAdapter boycpottAdapter;
    List<Product> searchList = new ArrayList<>();
    List<Product> productList = new ArrayList<>();
    private ProductRepository repository;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding=FragmentMyListBinding.inflate(inflater,container,false);
        return binding.getRoot();
    }
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        repository = new ProductRepository(getActivity());
        productList = repository.getAllProducts();
        binding.productArrayList.setHasFixedSize(true);
        binding.productArrayList.setLayoutManager(new GridLayoutManager(getActivity(), 3));
        boycpottAdapter = new BoycpottAdapter(productList);
        binding.productArrayList.setAdapter(boycpottAdapter);
        updateMessage(productList.size());
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


}