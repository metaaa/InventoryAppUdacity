package com.example.android.inventoryappudacity.data;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.util.Log;
import com.example.android.inventoryappudacity.R;

public class StoreProvider extends ContentProvider{

    public static final String LOG_TAG = StoreProvider.class.getSimpleName();

    //URI MATCHER TO THE TABLE
    private static final int PRODUCTS = 100;
    //URI MATCHER TO A SINGLE ITEM IN A TABLE
    private static final int PRODUCT_ID = 101;
    //DATABASE HELPER
    private StoreDbHelper mDbHelper;
    //URI MATCHER TO MATCH CONTENT URIS WITH THE CODE
    private static final UriMatcher sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
    //STATIC INITIALIZER
    static {
        //CONTENT URI IN "content://com.example.android.inventoryappudacity/products" FORMAT
        sUriMatcher.addURI(StoreContract.CONTENT_AUTHORITY, StoreContract.PATH_PRODUCTS, PRODUCTS);
        //CONTENT URI IN "content://com.example.android.inventoryappudacity/products/#" FORMAT
        sUriMatcher.addURI(StoreContract.CONTENT_AUTHORITY, StoreContract.PATH_PRODUCTS + "/#", PRODUCT_ID);
    }

    @Override
    public boolean onCreate() {
        mDbHelper = new StoreDbHelper(getContext());
        return true;
    }
    //THIS METHOD WILL DO THE READ FROM THE TABLE
    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        //GET INSTANCE OF READABLE DATABASE
        SQLiteDatabase sqLiteDatabase = mDbHelper.getReadableDatabase();
        //THIS CURSOR WILL STORE THE RESULT OF THE QUERY
        Cursor cursor;
        //CHECKS THAT IF THE URI MATCHES THE URI CODE
        int match = sUriMatcher.match(uri);

        switch (match){
            case PRODUCTS:
                cursor = sqLiteDatabase.query(StoreContract.ProductEntry.TABLE_NAME, projection, selection, selectionArgs, null, null, sortOrder);
                break;
            case PRODUCT_ID:
                selection = StoreContract.ProductEntry._ID + "=?";
                selectionArgs = new String[]{
                        String.valueOf(ContentUris.parseId(uri))
                };
                cursor = sqLiteDatabase.query(StoreContract.ProductEntry.TABLE_NAME, projection, selection, selectionArgs, null, null, sortOrder);
                break;
            default:
                throw new IllegalArgumentException(getContext().getString(R.string.error_unknown_uri));
        }
        //NOTIFICATION URI WHICH INDICATES THE URI WHEN TO BE UPDATED ON CHANGES
        cursor.setNotificationUri(getContext().getContentResolver(), uri);
        return cursor;
    }
    //THIS METHOD DETERMINES THE TYPE OF THE URI USED
    @Override
    public String getType(Uri uri) {
        final int match = sUriMatcher.match(uri);
        switch (match){
            case PRODUCTS:
                return StoreContract.ProductEntry.CONTENT_LIST_TYPE;
            case PRODUCT_ID:
                return StoreContract.ProductEntry.CONTENT_ITEM_TYPE;
            default:
                throw new IllegalArgumentException(getContext().getString(R.string.error_unknown_uri));
        }
    }


    //COLUMN TAGS USED FOR VALIDATION
    private static final String TAG_NAME = "name";
    private static final String TAG_PRICE = "price";
    private static final String TAG_QUANTITY = "quantity";
    private static final String TAG_SUPPLIER_NAME = "supplier_name";
    private static final String TAG_SUPPLIER_PHONE = "supplier_phone";
    @Override
    public Uri insert(Uri uri, ContentValues contentValues) {
        long id;
        String columnsToValidate = TAG_NAME + "|" + TAG_PRICE + "|" + TAG_QUANTITY + "|"
                + TAG_SUPPLIER_NAME + "|" + TAG_SUPPLIER_PHONE;

        boolean isValidInput = validateInput(contentValues, columnsToValidate);

        if (isValidInput) {
            SQLiteDatabase sqLiteDatabase = mDbHelper.getWritableDatabase();
            id = sqLiteDatabase.insert(StoreContract.ProductEntry.TABLE_NAME,
                    null, contentValues);
        } else {
            id = -1;
        }

        //CHECKS THE INSERTION STATUS
        if (id == -1) {
            Log.e(LOG_TAG, (getContext().getString(R.string.error_insert_failed, uri)));
            return null;
        }

        //NOTIFY ALL THE LISTENERS ABOUT THE CHANGE IN THE DATABASE
        getContext().getContentResolver().notifyChange(uri, null);

        //RETURN THE URI WITH THE ID APPENDED
        return ContentUris.withAppendedId(uri, id);
    }
    //THIS METHOD WILL UPDATE THE PRODUCTS
    @Override
    public int update(Uri uri, ContentValues contentValues, String selection, String[] selectionArgs) {
        final int match = sUriMatcher.match(uri);

        switch(match) {
            case PRODUCTS:
                return updateProduct(uri, contentValues, selection, selectionArgs);

            case PRODUCT_ID:
                selection = StoreContract.ProductEntry._ID + "=?";
                selectionArgs = new String[] { String.valueOf(ContentUris.parseId(uri))};
                return updateProduct(uri, contentValues, selection, selectionArgs);

            default:
                throw new IllegalArgumentException(getContext().getString(R.string.exception_unknown_uri, uri));
        }
    }


    //THIS METHOD VALIDATES THE INPUT AND DO THE UPDATE
    public int updateProduct(Uri uri, ContentValues contentValues, String selection, String[] selectionArgs) {

        String columnsToValidate = null;
        StringBuilder stringBuilder = new StringBuilder();
        final String SEPARATOR = "|";
        int rowsUpdated = 0;

        //IF THERE ARE NO VALUES TO UPDATE, DO NOT DO IT
        if (contentValues.size() == 0) {
            return 0;
        } else {
            if (contentValues.containsKey(StoreContract.ProductEntry.COLUMN_PRODUCT_NAME)) {
                stringBuilder.append(TAG_NAME);
            } else if (contentValues.containsKey(StoreContract.ProductEntry.COLUMN_PRODUCT_PRICE)) {
                stringBuilder.append(SEPARATOR).append(TAG_PRICE);
            } else if (contentValues.containsKey(StoreContract.ProductEntry.COLUMN_PRODUCT_QUANTITY)) {
                stringBuilder.append(SEPARATOR).append(TAG_QUANTITY);
            } else if (contentValues.containsKey(StoreContract.ProductEntry.COLUMN_SUPPLIER_NAME)) {
                stringBuilder.append(SEPARATOR).append(TAG_SUPPLIER_NAME);
            } else if (contentValues.containsKey(StoreContract.ProductEntry.COLUMN_SUPPLIER_PHONE)) {
                stringBuilder.append(SEPARATOR).append(TAG_SUPPLIER_PHONE);
            }

            columnsToValidate = stringBuilder.toString();
            boolean isValidInput = validateInput(contentValues, columnsToValidate);

            if (isValidInput) {
                SQLiteDatabase sqLiteDatabase = mDbHelper.getWritableDatabase();

                //DO THE UPDATE AND GET THE NUMBER OF ROWS AFFECTED
                rowsUpdated = sqLiteDatabase.update(StoreContract.ProductEntry.TABLE_NAME,
                        contentValues, selection, selectionArgs);

                //NOTIFY ALL THE LISTENER ABOUT THE CHANGE IN THE DATABASE
                if (rowsUpdated != 0) {
                    getContext().getContentResolver().notifyChange(uri, null);
                }
            }

            return rowsUpdated;
        }
    }


    //VALIDATE THE INPUTS BEFORE INSERTING OR UPDATING ANY PRODUCT INTO/IN THE DATABASE
    public boolean validateInput(ContentValues values, String columns) {

        String [] columnArgs = columns.split("|");
        String productName = null;
        Double productPrice = null;
        Integer productQuantity = null;
        String supplierName = null;
        String supplierPhone = null;


        for (int i = 0; i < columnArgs.length; i++ ) {

            if (columnArgs[i].equals(TAG_NAME)) {
                //CHECKS THE PRODUCT NAME
                productName = values.getAsString(StoreContract.ProductEntry.COLUMN_PRODUCT_NAME);
                if (productName == null || productName.trim().length() == 0) {
                    throw new IllegalArgumentException(getContext().getString(R.string.error_empty_product));
                }
            }
            else if (columnArgs[i].equals(TAG_PRICE)) {
                //CHECKS THE PRODUCT PRICE
                productPrice = values.getAsDouble(StoreContract.ProductEntry.COLUMN_PRODUCT_PRICE);
                if (productPrice == null || productPrice < 0) {
                    throw new IllegalArgumentException(getContext().getString(R.string.error_empty_price));
                }
            }
            else if (columnArgs[i].equals(TAG_QUANTITY)) {
                //CHECKS THE PRODUCT QUANTITY
                productQuantity = values.getAsInteger(StoreContract.ProductEntry.COLUMN_PRODUCT_QUANTITY);
                if (productQuantity == null || productQuantity < 0) {
                    throw new IllegalArgumentException(getContext().getString(R.string.error_empty_quantity));
                }
            }
            else if (columnArgs[i].equals(TAG_SUPPLIER_NAME)) {
                //CHECKS THE SUPPLIER NAME
                supplierName = values.getAsString(StoreContract.ProductEntry.COLUMN_SUPPLIER_NAME);
                if (supplierName == null || supplierName.trim().length() == 0) {
                    throw new IllegalArgumentException(getContext().getString(R.string.error_empty_supplier_name));
                }
            }
            else if (columnArgs[i].equals(TAG_SUPPLIER_PHONE)) {
                //CHECKS THE SUPPLIER PHONE
                supplierPhone = values.getAsString(StoreContract.ProductEntry.COLUMN_SUPPLIER_PHONE);
                if (supplierPhone == null || supplierPhone.trim().length() == 0) {
                    throw new IllegalArgumentException(getContext().getString(R.string.error_empty_supplier_phone));
                }
            }
        }

        return true;
    }
    //THIS METHOD WILL DO THE DELETING FROM THE TABLE
    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        //GET WRITABLE DATABASE
        SQLiteDatabase sqLiteDatabase = mDbHelper.getWritableDatabase();
        int rowsDeleted;
        final int match = sUriMatcher.match(uri);
        switch (match){
            case PRODUCTS:
                //DELETE ALL ROWS IN THE SELECTION
                rowsDeleted = sqLiteDatabase.delete(StoreContract.ProductEntry.TABLE_NAME, selection, selectionArgs);
                break;
            case PRODUCT_ID:
                //DELETE THE ROW WITH THE GIVEN ID
                selection = StoreContract.ProductEntry._ID + "=?";
                selectionArgs = new String[] {String.valueOf(ContentUris.parseId(uri))};
                rowsDeleted = sqLiteDatabase.delete(StoreContract.ProductEntry.TABLE_NAME, selection, selectionArgs);
                break;
            default:
                throw new IllegalArgumentException(getContext().getString(R.string.error_unknown_uri));
        }
        //CHECKS IF ANY ROWS WERE DELETED AND NOTIFY THE LISTENERS
        if (rowsDeleted != 0){
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return rowsDeleted;
    }
}
