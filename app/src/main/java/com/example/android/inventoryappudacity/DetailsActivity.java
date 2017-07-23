package com.example.android.inventoryappudacity;

import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.app.NavUtils;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import com.example.android.inventoryappudacity.data.StoreContract;

public class DetailsActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    final Context mContext = this;
    private static final int PRODUCT_LOADER = 1;

    //VARIABLES FOR THE UI COMPONENTS
    private TextView mProductNameTextView;
    private TextView mProductPriceTextView;
    private TextView mProductQuantityTextView;
    private TextView mSupplierNameTextView;
    private TextView mSupplierPhoneTextView;
    private ImageButton mPhoneButton;
    private ImageButton mDecreaseButton;
    private ImageButton mIncreaseButton;
    private ImageView mProductImage;

    private Uri mCurrentProductUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_details);

        //INITIALIZE THE ELEMENTS OF THE UI
        mProductNameTextView = (TextView) findViewById(R.id.detailed_product_name);
        mProductPriceTextView = (TextView) findViewById(R.id.detailed_product_price);
        mProductQuantityTextView = (TextView) findViewById(R.id.detailed_quantity);
        mSupplierNameTextView = (TextView) findViewById(R.id.detailed_supplier_name);
        mSupplierPhoneTextView = (TextView) findViewById(R.id.detailed_supplier_phone);
        mPhoneButton = (ImageButton) findViewById(R.id.detailed_phone_button);
        mDecreaseButton = (ImageButton) findViewById(R.id.detailed_button_decrease);
        mIncreaseButton = (ImageButton) findViewById(R.id.detailed_button_increase);
        mProductImage = (ImageView) findViewById(R.id.detailed_product_image);

        //GETTING THE INTENT
        Intent intent = getIntent();
        mCurrentProductUri = intent.getData();
        if(mCurrentProductUri != null) {
            getSupportLoaderManager().initLoader(PRODUCT_LOADER, null, this);
        }
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return new CursorLoader(
                this,
                mCurrentProductUri,
                null,
                null,
                null,
                null
        );
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        //CHECKS IF THE CURSOR IS NULL OR < 1
        if (cursor == null || cursor.getCount() < 1){
            return;
        }
        //IF IT IS AN EXISTING PROJECT
        if (cursor.moveToFirst()){
            DatabaseUtils.dumpCursor(cursor);

            //GETTING THE COLUMNS INDEXES
            int productNameColumnIndex = cursor.getColumnIndex(StoreContract.ProductEntry.COLUMN_PRODUCT_NAME);
            int productPriceColumnIndex = cursor.getColumnIndex(StoreContract.ProductEntry.COLUMN_PRODUCT_PRICE);
            int productQuantityColumnIndex = cursor.getColumnIndex(StoreContract.ProductEntry.COLUMN_PRODUCT_QUANTITY);
            int productImageColumnIndex = cursor.getColumnIndex(StoreContract.ProductEntry.COLUMN_PRODUCT_IMAGE);
            int supplierNameColumnIndex = cursor.getColumnIndex(StoreContract.ProductEntry.COLUMN_SUPPLIER_NAME);
            int supplierPhoneColumnIndex = cursor.getColumnIndex(StoreContract.ProductEntry.COLUMN_SUPPLIER_PHONE);

            //GETTING DATA FROM THE CURSOR BASED ON THE COLUMN INDEX VALUES
            String productName = cursor.getString(productNameColumnIndex);
            Double productPrice = cursor.getDouble(productPriceColumnIndex);
            final int productQuantity = cursor.getInt(productQuantityColumnIndex);
            final String productImage = cursor.getString(productImageColumnIndex);
            String supplierName = cursor.getString(supplierNameColumnIndex);
            final String supplierPhone = cursor.getString(supplierPhoneColumnIndex);

            //SETTING THE DATAS TO THE TEXTVIEWS
            mProductNameTextView.setText(productName);
            mProductPriceTextView.setText(String.valueOf(productPrice));
            mProductQuantityTextView.setText(Integer.toString(productQuantity));
            mSupplierNameTextView.setText(supplierName);
            mSupplierPhoneTextView.setText(supplierPhone);

            //DISPLAY THE IMAGE
            ViewTreeObserver viewTreeObserver = mProductImage.getViewTreeObserver();
            viewTreeObserver.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                @Override
                public void onGlobalLayout() {
                    mProductImage.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                    mProductImage.setImageBitmap(Utils.getBitmapFromUri(Uri.parse(productImage), mContext, mProductImage));
                }
            });
            //SETTING ON CLICK LISTENERS TO THE BUTTONS
            mDecreaseButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    quantityChange(mCurrentProductUri, (productQuantity - 1));
                }
            });
            mIncreaseButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    quantityChange(mCurrentProductUri, (productQuantity + 1));
                }
            });
            mPhoneButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(Intent.ACTION_DIAL);
                    intent.setData(Uri.parse("tel:" + supplierPhone));
                    if (intent.resolveActivity(getPackageManager()) != null) {
                        startActivity(intent);
                    }
                }
            });
        }
    }
    //THIS METHOD WILL INCREASE AND DECREASE THE QUANTITY NUMBER
    private int quantityChange (Uri itemUri, int newQuantity) {
        if (newQuantity < 0) {
            return 0;
        }

        ContentValues values = new ContentValues();
        values.put(StoreContract.ProductEntry.COLUMN_PRODUCT_QUANTITY, newQuantity);
        int numRowsUpdated = getContentResolver().update(itemUri, values, null, null);
        return numRowsUpdated;
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        //NO FUNCTION
    }
    //MENU INFLATER FOR THE MENU IN THE DETAILS ACTIVITY
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_details, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);

        if (mCurrentProductUri == null) {
            MenuItem menuItem = menu.findItem(R.id.action_delete);
            menuItem.setVisible(false);
        }
        return true;
    }

    //THIS WILL HANDLE THE ACTIONS DEPENDING ON THE OPTION SELECTED
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_edit:
                editProduct();
                return true;

            case R.id.action_delete:
                confirmDeleteProduct();
                return true;

            case android.R.id.home:
                NavUtils.navigateUpFromSameTask(DetailsActivity.this);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    //THIS WILL LANUCH THE EDITOR ACTIVITY FOR EDIT
    public void editProduct() {
        Intent intent = new Intent(DetailsActivity.this, EditorActivity.class);
        intent.setData(mCurrentProductUri);
        startActivity(intent);
    }

    //ASK FOR CONFIRMATION ON ITEM DELETE
    private void confirmDeleteProduct() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(getString(R.string.dialog_delete));
        builder.setPositiveButton(getString(R.string.action_yes), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                deleteProduct();
                finish();
            }
        });
        builder.setNegativeButton(getString(R.string.action_no), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });

        //CREATE THE ALERT DIALOG
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    //THIS WILL DELETE THE ITEM(S)
    private void deleteProduct() {

        // Only perform the delete if this is an existing product
        if (mCurrentProductUri != null) {
            int rowsDeleted = getContentResolver().delete(mCurrentProductUri, null, null);
            // Show a toast message depending on whether or not the delete was successful.
            if (rowsDeleted == 0) {
                // If no rows were deleted, then there was an error with the delete
                Toast.makeText(this, getString(R.string.error_delete_failed), Toast.LENGTH_SHORT).show();
            } else {
                // Otherwise, the delete was successful
                Toast.makeText(this, getString(R.string.confirm_delete_successful), Toast.LENGTH_SHORT).show();
            }
        }

        //CLOSE THE ACTIVITY
        finish();
    }
}
