package com.nahidsoft.boycott.Adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.nahidsoft.boycott.Activitys.ResultGreenActivity;
import com.nahidsoft.boycott.Activitys.ResultRedActivity;
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
        holder.itemView.setOnClickListener(v -> {
            if (product.getStatus().equals("red")) {
                holder.itemView.getContext().startActivity(new Intent(holder.itemView.getContext(), ResultRedActivity.class)
                        .putExtra("status","green")
                        .putExtra("image", product.getImage())
                        .putExtra("note", product.getReason())
                        .putExtra("product", product)
                );
            } else if (product.getStatus().equals("green")) {
                holder.itemView.getContext().startActivity(new Intent(holder.itemView.getContext(), ResultGreenActivity.class)
                        .putExtra("status","green")
                        .putExtra("image", product.getImage())
                        .putExtra("note", product.getReason())
                        .putExtra("product", product)
                );
            }else if (product.getStatus().equals("yellow")) {
//                holder.itemView.getContext().startActivity(new Intent(holder.itemView.getContext(), ResultGreenActivity.class)
//                        .putExtra("status","green")
//                        .putExtra("image", product.getImage())
//                        .putExtra("note", product.getReason())
//                        .putExtra("product", product)
//                );
                Toast.makeText(holder.itemView.getContext(), "Yellow!", Toast.LENGTH_SHORT).show();
            }else if (product.getStatus().equals("yellow")) {
//                holder.itemView.getContext().startActivity(new Intent(holder.itemView.getContext(), ResultGreenActivity.class)
//                        .putExtra("status","green")
//                        .putExtra("image", product.getImage())
//                        .putExtra("note", product.getReason())
//                        .putExtra("product", product)
//                );
                Toast.makeText(holder.itemView.getContext(), "Yellow!", Toast.LENGTH_SHORT).show();
            }else if (product.getStatus().equals("default")) {
//                holder.itemView.getContext().startActivity(new Intent(holder.itemView.getContext(), ResultGreenActivity.class)
//                        .putExtra("status","green")
//                        .putExtra("image", product.getImage())
//                        .putExtra("note", product.getReason())
//                        .putExtra("product", product)
//                );
                Toast.makeText(holder.itemView.getContext(), "Default!", Toast.LENGTH_SHORT).show();
            }
        });

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
