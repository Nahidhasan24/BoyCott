package com.nahidsoft.boycott.Adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.nahidsoft.boycott.Models.BrandModel;
import com.nahidsoft.boycott.R;

import java.util.ArrayList;
import java.util.List;

public class BrandAdapter extends RecyclerView.Adapter<BrandAdapter.ViewHolder> {
    private List<BrandModel> brandList;
    private List<BrandModel> filteredList;
    private final OnBrandClickListener listener;

    public BrandAdapter(List<BrandModel> brandList, OnBrandClickListener listener) {
        this.brandList = brandList;
        this.filteredList = new ArrayList<>(brandList);
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_brand_tag, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        BrandModel brand = filteredList.get(position);
        holder.textViewBrand.setText(brand.getCompanyName());
    }

    @Override
    public int getItemCount() {
        return filteredList.size();
    }

    public void filter(String text) {
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
        notifyDataSetChanged();
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        TextView textViewBrand;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            textViewBrand = itemView.findViewById(R.id.textViewBrand);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            int position = getAdapterPosition();
            if (position != RecyclerView.NO_POSITION) {
                listener.onBrandClick(filteredList.get(position));
            }
        }
    }

    public interface OnBrandClickListener {
        void onBrandClick(BrandModel brand);
    }
}
