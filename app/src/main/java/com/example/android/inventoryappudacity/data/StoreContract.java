package com.example.android.inventoryappudacity.data;

import android.content.ContentResolver;
import android.net.Uri;
import android.provider.BaseColumns;

public class StoreContract {

    //SETTING EMPTY CONSTRUCTOR
    private StoreContract(){};
    //SETTING THE VARIABLE FOR PREPARE THE URI
    //NAME OF THE CONTENT PROVIDER
    public static final String CONTENT_AUTHORITY = "com.example.android.inventoryappudacity";
    //BASE CONTENT URI
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);
    //PATH TO THE TABLE
    public static final String PATH_PRODUCTS = "products";

    //INNER CLASS WHICH DEFINES THE CONSTANTS FOR THE PRODUCTS TABLE
    public static class ProductEntry implements BaseColumns{
        //URI TO ACCESS THE DATA IN THE PROVIDER
        public static final Uri CONTENT_URI = Uri.withAppendedPath(BASE_CONTENT_URI, PATH_PRODUCTS);
        //TYPE OF URI TO ACCESS A LIST OF ITEMS
        public static final String CONTENT_LIST_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_PRODUCTS;
        //TYPE OF URI TO ACCESS A SINGLE ITEM IN THE DATABASE
        public static final String CONTENT_ITEM_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_PRODUCTS;

        //NAME OF THE TABLE
        public static final String TABLE_NAME = "products";
        //VARIABLES FOR THE PRODUCTS = COLUMN NAMES
        //ID OF THE PRODUCT = INTEGER
        public static final String _ID = BaseColumns._ID;
        //NAME OF THE PRODUCT = TEXT
        public static final String COLUMN_PRODUCT_NAME = "product_name";
        //PRICE OF THE PRODUCTS = REAL
        public static final String COLUMN_PRODUCT_PRICE = "product_price";
        //QUANTITY DATE OF THE PRODUCTS = INTEGER
        public static final String COLUMN_PRODUCT_QUANTITY = "product_quantity";
        //IMAGE OF THE PRODUCT = TEXT
        public static final String COLUMN_PRODUCT_IMAGE = "product_image";
        //NAME OF THE SUPPLIER = TEXT
        public static final String COLUMN_SUPPLIER_NAME = "product_supplier_name";
        //PHONE NUMBER OF THE SUPPLIER = TEXT
        public static final String COLUMN_SUPPLIER_PHONE = "product_supplier_phone";
    }
}
