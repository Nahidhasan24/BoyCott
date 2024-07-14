package com.nahidsoft.boycott.Adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.nahidsoft.boycott.Models.ProductsModel;
import com.nahidsoft.boycott.R;

import java.util.List;

public class ProducrsAdapter extends RecyclerView.Adapter<ProducrsAdapter.viewholder> {


    private Context context;
    private List<ProductsModel> productsModelList;

    public ProducrsAdapter(Context context, List<ProductsModel> productsModelList) {
        this.context = context;
        this.productsModelList = productsModelList;
    }

    @NonNull
    @Override
    public viewholder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view= LayoutInflater.from(context).inflate(R.layout.products_item,parent,false);
        return new viewholder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull viewholder holder, int position) {
        ProductsModel productsModel=productsModelList.get(position);
        Glide.with(context).load(productsModel.getImage()).into(holder.imageView);
        holder.textView.setText(""+productsModel.getTitle());
    }

    @Override
    public int getItemCount() {
        return productsModelList.size();
    }

    class viewholder extends RecyclerView.ViewHolder {
        ImageView imageView;
        TextView textView;
        public viewholder(@NonNull View itemView) {
            super(itemView);
            imageView=itemView.findViewById(R.id.productImage);
            textView=itemView.findViewById(R.id.productTitle);
        }
    }
}
