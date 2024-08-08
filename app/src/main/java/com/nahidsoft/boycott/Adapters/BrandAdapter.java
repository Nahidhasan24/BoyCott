package com.nahidsoft.boycott.Adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.nahidsoft.boycott.Models.BrandModel;
import com.nahidsoft.boycott.R;

import java.util.List;

public class BrandAdapter extends RecyclerView.Adapter<BrandAdapter.ViewHolder> {
    private List<BrandModel> brandList;
    private final OnBrandClickListener listener;

    public BrandAdapter(List<BrandModel> brandList, OnBrandClickListener listener) {
        this.brandList = brandList;
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
        BrandModel brand = brandList.get(position);
        holder.textViewBrand.setText(brand.getCompanyName());
    }

    @Override
    public int getItemCount() {
        return brandList.size();
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
                listener.onBrandClick(brandList.get(position));
            }
        }
    }

    public interface OnBrandClickListener {
        void onBrandClick(BrandModel brand);
    }
}
