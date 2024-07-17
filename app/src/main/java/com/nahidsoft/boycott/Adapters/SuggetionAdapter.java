package com.nahidsoft.boycott.Adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.nahidsoft.boycott.Models.Product;
import com.nahidsoft.boycott.R;
import com.nahidsoft.boycott.Utilitis.APIs;

import java.util.ArrayList;
import java.util.List;

public class SuggetionAdapter extends RecyclerView.Adapter<SuggetionAdapter.viewholder> {


    private Context context;
    private List<Product> productList=new ArrayList<>();

    public SuggetionAdapter(Context context, List<Product> productList) {
        this.context = context;
        this.productList = productList;
    }

    @NonNull
    @Override
    public viewholder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.products_item, parent, false);
        return new SuggetionAdapter.viewholder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull viewholder holder, int position) {
        Product product = productList.get(position);
        if (product.getStatus().equals("red")) {
            holder.linearLayout.setBackground(ContextCompat.getDrawable(holder.itemView.getContext(), R.drawable.red_bg));
        } else if (product.getStatus().equals("green")) {
            holder.linearLayout.setBackground(ContextCompat.getDrawable(holder.itemView.getContext(), R.drawable.green_bg));
        } else if (product.getStatus().equals("yellow")) {
            holder.linearLayout.setBackground(ContextCompat.getDrawable(holder.itemView.getContext(), R.drawable.yellow_bg));
        }

        if (product.getImage().contains("https")){
            Glide.with(holder.itemView.getContext()).load(product.getImage()).into(holder.imageView);
            holder.textView.setText("" + product.getTitle());
        }else{
            Glide.with(holder.itemView.getContext()).load(APIs.IMAGE+product.getImage()).into(holder.imageView);
            holder.textView.setText("" + product.getTitle());
        }

    }

    @Override
    public int getItemCount() {
        return productList.size();
    }

    class viewholder extends RecyclerView.ViewHolder {
        ImageView imageView;
        TextView textView;
        LinearLayout linearLayout;

        public viewholder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.productImage);
            textView = itemView.findViewById(R.id.productTitle);
            linearLayout = itemView.findViewById(R.id.colorBg);
        }
    }
}
