package com.nahidsoft.boycott.Utilitis;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class ProductDatabaseHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "products.db";
    private static final int DATABASE_VERSION = 1;

    public static final String TABLE_PRODUCTS = "products";
    public static final String COLUMN_PRIMARY_KEY = "primaryKey";
    public static final String COLUMN_ID = "id";
    public static final String COLUMN_TITLE = "title";
    public static final String COLUMN_CREATED_TIME = "createdTime";
    public static final String COLUMN_BAR_CODE = "barCode";
    public static final String COLUMN_COMPANY_NAME = "companyName";
    public static final String COLUMN_PARENT_COMPANY = "parentCompany";
    public static final String COLUMN_REASON = "reason";
    public static final String COLUMN_CATEGORY = "category";
    public static final String COLUMN_STATUS = "status";
    public static final String COLUMN_IMAGE = "image";

    private static final String TABLE_CREATE =
            "CREATE TABLE " + TABLE_PRODUCTS + " (" +
                    COLUMN_PRIMARY_KEY + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    COLUMN_ID + " TEXT UNIQUE, " +
                    COLUMN_TITLE + " TEXT, " +
                    COLUMN_CREATED_TIME + " TEXT, " +
                    COLUMN_BAR_CODE + " TEXT, " +
                    COLUMN_COMPANY_NAME + " TEXT, " +
                    COLUMN_PARENT_COMPANY + " TEXT, " +
                    COLUMN_REASON + " TEXT, " +
                    COLUMN_CATEGORY + " TEXT, " +
                    COLUMN_STATUS + " TEXT, " +
                    COLUMN_IMAGE + " TEXT" +
                    ");";

    public ProductDatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(TABLE_CREATE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_PRODUCTS);
        onCreate(db);
    }
}