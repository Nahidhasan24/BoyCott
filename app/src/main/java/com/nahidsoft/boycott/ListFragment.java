package com.nahidsoft.boycott;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Toast;

import com.nahidsoft.boycott.Adapters.ProducrsAdapter;
import com.nahidsoft.boycott.Models.ProductsModel;
import com.nahidsoft.boycott.databinding.FragmentListBinding;

import java.util.ArrayList;
import java.util.List;

public class ListFragment extends Fragment {

    FragmentListBinding binding;
    ProducrsAdapter producrsAdapter;
    ArrayList<ProductsModel> productsModelArrayList=new ArrayList<>();
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = FragmentListBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        loadData();

        binding.productArrayList.setHasFixedSize(true);
        binding.productArrayList.setLayoutManager(new GridLayoutManager(getActivity(),3));
        producrsAdapter=new ProducrsAdapter(getActivity(),productsModelArrayList);
        binding.productArrayList.setAdapter(producrsAdapter);
        binding.productCountTv.setText("Shows "+productsModelArrayList.size()+" Products");


        spinnerOne();
        spinnerTwo();
        spinnerThree();
    }

    private void loadData() {
        productsModelArrayList.add(new ProductsModel("Pepsi 1","https://upload.wikimedia.org/wikipedia/commons/thumb/6/68/Pepsi_2023.svg/140px-Pepsi_2023.svg.png"));
        productsModelArrayList.add(new ProductsModel("Pepsi 2","https://upload.wikimedia.org/wikipedia/commons/thumb/6/68/Pepsi_2023.svg/140px-Pepsi_2023.svg.png"));
        productsModelArrayList.add(new ProductsModel("Pepsi 3","https://upload.wikimedia.org/wikipedia/commons/thumb/6/68/Pepsi_2023.svg/140px-Pepsi_2023.svg.png"));
        productsModelArrayList.add(new ProductsModel("Pepsi 4","https://upload.wikimedia.org/wikipedia/commons/thumb/6/68/Pepsi_2023.svg/140px-Pepsi_2023.svg.png"));
        productsModelArrayList.add(new ProductsModel("Pepsi 5","https://upload.wikimedia.org/wikipedia/commons/thumb/6/68/Pepsi_2023.svg/140px-Pepsi_2023.svg.png"));
        productsModelArrayList.add(new ProductsModel("Pepsi 6","https://upload.wikimedia.org/wikipedia/commons/thumb/6/68/Pepsi_2023.svg/140px-Pepsi_2023.svg.png"));
        productsModelArrayList.add(new ProductsModel("Pepsi 7","https://upload.wikimedia.org/wikipedia/commons/thumb/6/68/Pepsi_2023.svg/140px-Pepsi_2023.svg.png"));
        productsModelArrayList.add(new ProductsModel("Pepsi 8","https://upload.wikimedia.org/wikipedia/commons/thumb/6/68/Pepsi_2023.svg/140px-Pepsi_2023.svg.png"));
        productsModelArrayList.add(new ProductsModel("Pepsi 9","https://upload.wikimedia.org/wikipedia/commons/thumb/6/68/Pepsi_2023.svg/140px-Pepsi_2023.svg.png"));
        productsModelArrayList.add(new ProductsModel("Pepsi 10","https://upload.wikimedia.org/wikipedia/commons/thumb/6/68/Pepsi_2023.svg/140px-Pepsi_2023.svg.png"));
        productsModelArrayList.add(new ProductsModel("Pepsi 11","https://upload.wikimedia.org/wikipedia/commons/thumb/6/68/Pepsi_2023.svg/140px-Pepsi_2023.svg.png"));
        productsModelArrayList.add(new ProductsModel("Pepsi 12","https://upload.wikimedia.org/wikipedia/commons/thumb/6/68/Pepsi_2023.svg/140px-Pepsi_2023.svg.png"));
        productsModelArrayList.add(new ProductsModel("Pepsi 13","https://upload.wikimedia.org/wikipedia/commons/thumb/6/68/Pepsi_2023.svg/140px-Pepsi_2023.svg.png"));

        productsModelArrayList.add(new ProductsModel("Coca-Cola 1","https://upload.wikimedia.org/wikipedia/commons/thumb/c/ce/Coca-Cola_logo.svg/512px-Coca-Cola_logo.svg.png?20231211133000"));
        productsModelArrayList.add(new ProductsModel("Coca-Cola 2","https://upload.wikimedia.org/wikipedia/commons/thumb/c/ce/Coca-Cola_logo.svg/512px-Coca-Cola_logo.svg.png?20231211133000"));
        productsModelArrayList.add(new ProductsModel("Coca-Cola 3","https://upload.wikimedia.org/wikipedia/commons/thumb/c/ce/Coca-Cola_logo.svg/512px-Coca-Cola_logo.svg.png?20231211133000"));

    }

    private void spinnerThree() {
        List<String> items = new ArrayList<>();
        items.add("Country");
        items.add("Afghanistan");
        items.add("Burundi");
        items.add("Bangladesh");
        items.add("Costa Rica");


        CustomSpinnerAdapter adapter = new CustomSpinnerAdapter(getActivity(), R.layout.spinner_item_with_arrow, items);
        adapter.setDropDownViewResource(R.layout.spinner_dropdown_item_with_arrow);
        binding.spinnerLocation.setAdapter(adapter);
        binding.spinnerLocation.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position == 0) {
                    // Placeholder selected
                } else {
                    // Handle the actual selection
                    String selectedItem = items.get(position);
                    Toast.makeText(getActivity(), "Selected: " + selectedItem, Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Do nothing
            }
        });
    }

    private void spinnerTwo() {
        List<String> items = new ArrayList<>();
        items.add("Category");
        items.add("Food");
        items.add("Electronic");
        items.add("Lifestyle");
        items.add("Health");


        CustomSpinnerAdapter adapter = new CustomSpinnerAdapter(getActivity(), R.layout.spinner_item_with_arrow, items);
        adapter.setDropDownViewResource(R.layout.spinner_dropdown_item_with_arrow);
        binding.spinnerCategory.setAdapter(adapter);
        binding.spinnerCategory.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position == 0) {
                    // Placeholder selected
                } else {
                    // Handle the actual selection
                    String selectedItem = items.get(position);
                    Toast.makeText(getActivity(), "Selected: " + selectedItem, Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Do nothing
            }
        });
    }

    private void spinnerOne() {
        List<String> items = new ArrayList<>();
        items.add("Brand");
        items.add("Nike");
        items.add("Patagonia");
        items.add("Virgin");
        items.add("Monocle");


        CustomSpinnerAdapter adapter = new CustomSpinnerAdapter(getActivity(), R.layout.spinner_item_with_arrow, items);
        adapter.setDropDownViewResource(R.layout.spinner_dropdown_item_with_arrow);
        binding.spinnerBrand.setAdapter(adapter);
        binding.spinnerBrand.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position == 0) {
                    // Placeholder selected
                } else {
                    // Handle the actual selection
                    String selectedItem = items.get(position);
                    Toast.makeText(getActivity(), "Selected: " + selectedItem, Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Do nothing
            }
        });

    }
}
