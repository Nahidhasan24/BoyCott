package com.nahidsoft.boycott.Utilitis;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.nahidsoft.boycott.Models.Product;

import java.util.ArrayList;
import java.util.List;

public class ProductRepository {
    private ProductDatabaseHelper dbHelper;

    public ProductRepository(Context context) {
        dbHelper = new ProductDatabaseHelper(context);
    }

    public void addProduct(Product product) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(ProductDatabaseHelper.COLUMN_ID, product.getId());
        values.put(ProductDatabaseHelper.COLUMN_TITLE, product.getTitle());
        values.put(ProductDatabaseHelper.COLUMN_CREATED_TIME, product.getCreatedTime());
        values.put(ProductDatabaseHelper.COLUMN_BAR_CODE, product.getBarCode());
        values.put(ProductDatabaseHelper.COLUMN_COMPANY_NAME, product.getCompanyName());
        values.put(ProductDatabaseHelper.COLUMN_PARENT_COMPANY, product.getParentCompany());
        values.put(ProductDatabaseHelper.COLUMN_REASON, product.getReason());
        values.put(ProductDatabaseHelper.COLUMN_CATEGORY, product.getCategory());
        values.put(ProductDatabaseHelper.COLUMN_STATUS, product.getStatus());
        values.put(ProductDatabaseHelper.COLUMN_IMAGE, product.getImage());

        db.insert(ProductDatabaseHelper.TABLE_PRODUCTS, null, values);
        db.close();
    }
    @SuppressLint("Range")
    public List<Product> getAllProducts() {
        List<Product> products = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.query(ProductDatabaseHelper.TABLE_PRODUCTS, null, null, null, null, null, null);

        if (cursor.moveToFirst()) {
            do {
                Product product = new Product(
                        cursor.getString(cursor.getColumnIndex(ProductDatabaseHelper.COLUMN_ID)),
                        cursor.getString(cursor.getColumnIndex(ProductDatabaseHelper.COLUMN_TITLE)),
                        cursor.getString(cursor.getColumnIndex(ProductDatabaseHelper.COLUMN_CREATED_TIME)),
                        cursor.getString(cursor.getColumnIndex(ProductDatabaseHelper.COLUMN_BAR_CODE)),
                        cursor.getString(cursor.getColumnIndex(ProductDatabaseHelper.COLUMN_COMPANY_NAME)),
                        cursor.getString(cursor.getColumnIndex(ProductDatabaseHelper.COLUMN_PARENT_COMPANY)),
                        cursor.getString(cursor.getColumnIndex(ProductDatabaseHelper.COLUMN_REASON)),
                        cursor.getString(cursor.getColumnIndex(ProductDatabaseHelper.COLUMN_CATEGORY)),
                        cursor.getString(cursor.getColumnIndex(ProductDatabaseHelper.COLUMN_STATUS)),
                        cursor.getString(cursor.getColumnIndex(ProductDatabaseHelper.COLUMN_IMAGE))
                );
                products.add(product);
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return products;
    }
    @SuppressLint("Range")
    public Product getProductById(String id) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.query(ProductDatabaseHelper.TABLE_PRODUCTS, null, ProductDatabaseHelper.COLUMN_ID + "=?", new String[]{id}, null, null, null);

        if (cursor != null && cursor.moveToFirst()) {
            Product product = new Product(
                    cursor.getString(cursor.getColumnIndex(ProductDatabaseHelper.COLUMN_ID)),
                    cursor.getString(cursor.getColumnIndex(ProductDatabaseHelper.COLUMN_TITLE)),
                    cursor.getString(cursor.getColumnIndex(ProductDatabaseHelper.COLUMN_CREATED_TIME)),
                    cursor.getString(cursor.getColumnIndex(ProductDatabaseHelper.COLUMN_BAR_CODE)),
                    cursor.getString(cursor.getColumnIndex(ProductDatabaseHelper.COLUMN_COMPANY_NAME)),
                    cursor.getString(cursor.getColumnIndex(ProductDatabaseHelper.COLUMN_PARENT_COMPANY)),
                    cursor.getString(cursor.getColumnIndex(ProductDatabaseHelper.COLUMN_REASON)),
                    cursor.getString(cursor.getColumnIndex(ProductDatabaseHelper.COLUMN_CATEGORY)),
                    cursor.getString(cursor.getColumnIndex(ProductDatabaseHelper.COLUMN_STATUS)),
                    cursor.getString(cursor.getColumnIndex(ProductDatabaseHelper.COLUMN_IMAGE))
            );
            cursor.close();
            db.close();
            return product;
        } else {
            return null;
        }
    }

    public void deleteProduct(Product product) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        db.delete(ProductDatabaseHelper.TABLE_PRODUCTS, ProductDatabaseHelper.COLUMN_ID + " = ?", new String[]{product.getId()});
        db.close();
    }
    public void deleteProductById(String id) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        db.delete(ProductDatabaseHelper.TABLE_PRODUCTS, ProductDatabaseHelper.COLUMN_ID + " = ?", new String[]{id});
        db.close();
    }
}