package com.example.android.inventoryappudacity.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class StoreDbHelper extends SQLiteOpenHelper {

    //NAME OF THE DATABASE FILE
    private static final String DATABASE_NAME = "store.db";
    //DATABASE VERSION
    private static final int DATABASE_VERSION = 1;
    //DEFAULT CONSTRUCTOR
    public StoreDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    //THIS METHOD CREATESTHE DATABSE AT FIRST TIME RUN
    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        //CONSTANT STRINGS FOR THE VARIABLE TYPES AND KEYWORDS OF SQL
        final String TYPE_TEXT = " TEXT";
        final String TYPE_INTEGER = " INTEGER";
        final String TYPE_REAL = " REAL";
        final String NOT_NULL = " NOT NULL";
        final String PRIMARY_KEY = " PRIMARY KEY";
        final String AUTOINCREMENT_KEY = " AUTOINCREMENT";
        final String DEFAULT_VALUE = " DEFAULT ";
        final String COMMA_SEPARATOR = ", ";

        //CREATING THE SQL STATEMENT STRING OF CREATING THE TABLE
        String SQL_CREATE_TABLE = "CREATE TABLE " + StoreContract.ProductEntry.TABLE_NAME + " ("
                + StoreContract.ProductEntry._ID + TYPE_INTEGER + PRIMARY_KEY + AUTOINCREMENT_KEY + COMMA_SEPARATOR
                + StoreContract.ProductEntry.COLUMN_PRODUCT_NAME + TYPE_TEXT + NOT_NULL + COMMA_SEPARATOR
                + StoreContract.ProductEntry.COLUMN_PRODUCT_PRICE + TYPE_REAL + NOT_NULL + COMMA_SEPARATOR
                + StoreContract.ProductEntry.COLUMN_PRODUCT_QUANTITY + TYPE_INTEGER + NOT_NULL + DEFAULT_VALUE + "0" + COMMA_SEPARATOR
                + StoreContract.ProductEntry.COLUMN_PRODUCT_IMAGE + TYPE_TEXT + NOT_NULL + COMMA_SEPARATOR
                + StoreContract.ProductEntry.COLUMN_SUPPLIER_NAME + TYPE_TEXT + NOT_NULL + COMMA_SEPARATOR
                + StoreContract.ProductEntry.COLUMN_SUPPLIER_PHONE + TYPE_TEXT + NOT_NULL + ")";
        //EXECUTES THE SQL STATEMENT AND CREATES THE TABLE
        sqLiteDatabase.execSQL(SQL_CREATE_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int oldVersion, int newVersion) {
        sqLiteDatabase.execSQL("DROP TABLE IF EXITS " + StoreContract.ProductEntry.TABLE_NAME);
    }
}
