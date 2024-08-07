package com.nahidsoft.boycott.Adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.nahidsoft.boycott.Models.Country;
import com.nahidsoft.boycott.R;

import java.util.ArrayList;
import java.util.List;

public class LocationSortAdapter extends RecyclerView.Adapter<LocationSortAdapter.ViewHolder> {

    private List<Country> countryList;
    private Context context;
    private List<String> selectedFilters;
    private OnItemClickListener onItemClickListener;

    public interface OnItemClickListener {
        void onItemClick(List<String> selectedFilters);
    }

    public LocationSortAdapter(OnItemClickListener onItemClickListener, Context context, List<Country> countryList, List<String> selectedFilters) {
        this.onItemClickListener = onItemClickListener;
        this.context = context;
        this.countryList = countryList;
        this.selectedFilters = new ArrayList<>(selectedFilters);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_sort, parent, false);
        return new ViewHolder(view);
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, @SuppressLint("RecyclerView") int position) {
        Country country = countryList.get(position);
        String filter = country.getCountryName();
        holder.filterButton.setText(filter);

        // Change background color if selected
        if (selectedFilters.contains(filter)) {
            holder.filterButton.setBackground(context.getResources().getDrawable(R.drawable.selected_bg));
            holder.filterButton.setTextColor(context.getResources().getColor(R.color.white));
        } else {
            holder.filterButton.setBackground(context.getResources().getDrawable(R.drawable.gray_bg));
            holder.filterButton.setTextColor(context.getResources().getColor(R.color.black));
        }

        holder.filterButton.setOnClickListener(view -> {
            if (selectedFilters.contains(filter)) {
                selectedFilters.remove(filter);
            } else {
                selectedFilters.add(filter);
            }
            notifyDataSetChanged();
            onItemClickListener.onItemClick(selectedFilters);
        });
    }

    @Override
    public int getItemCount() {
        return countryList.size();
    }

    public void clearSelection() {
        selectedFilters.clear();
        notifyDataSetChanged();
    }

    public List<String> getSelectedFilters() {
        return selectedFilters;
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        public TextView filterButton;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            filterButton = itemView.findViewById(R.id.sortTv);
        }
    }
}
