package com.example.android.inventoryappudacity;

import android.app.AlertDialog;
import android.app.LoaderManager;
import android.content.ContentValues;
import android.content.Context;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.InputFilter;
import android.text.Spanned;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import com.example.android.inventoryappudacity.data.StoreContract;

public class EditorActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor>{

    public static final String LOG_TAG = EditorActivity.class.getSimpleName();
    private static final int IMAGE_REQUEST_CODE = 0;
    private static final int EXISTING_ITEM_LOADER = 0;
    private static final String STATE_IMAGE_URI = "STATE_IMAGE_URI";
    final Context mContext = this;

    //UI COMPONENTS
    private EditText editNameTextView;
    private EditText editPriceTextView;
    private EditText editQuantityTextView;
    private EditText editSupplierNameTextView;
    private EditText editSUpplierPhoneTextView;
    private ImageView editImageImage;
    private Button editBrowseImageButton;

    private Uri mCurrentProductUri;
    private Uri mImageUri;

    //VARIABLES TO STORE USER INPUT
    private String productName;
    private Double productPrice;
    private String priceString;
    private int productQuantity;
    private String quantityString;
    private String productSupplierName;
    private String productSupplierPhone;
    private String imagePath;

    //TRACK THAT IF THE PRODUCT IS EDITED OR NOT
    private boolean mItemHasChanged = false;

    //SETTING LISTENER FOR ANY TOUCHES
    private View.OnTouchListener mTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            mItemHasChanged = true;
            return false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editor);
        //ENABLE THE BACK ARROW ON THE MENUBAR
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        //THE FOLLOWING CODE DETECTS THAT THE USER WANT TO EDIT AN EXISTING PRODUCT OR CREATE A NEW ONE
        Intent intent = getIntent();
        mCurrentProductUri = intent.getData();

        if (mCurrentProductUri == null){
            setTitle(getTitle() + " Add");
        } else {
            setTitle(getTitle() + " Edit");
            getLoaderManager().initLoader(EXISTING_ITEM_LOADER, null, this);
        }
        //INITIALIZE UI COMPONENTS
        editNameTextView = (EditText) findViewById(R.id.edit_product_name);
        editPriceTextView = (EditText) findViewById(R.id.edit_product_price);
        editQuantityTextView = (EditText) findViewById(R.id.edit_product_quantity);
        editSupplierNameTextView = (EditText) findViewById(R.id.edit_supplier_name);
        editSUpplierPhoneTextView = (EditText) findViewById(R.id.edit_supplier_phone);
        editImageImage = (ImageView) findViewById(R.id.edit_image);
        editBrowseImageButton = (Button) findViewById(R.id.edit_browse_button);
        //SETTING ON TOUCH LISTENERS TO THE INPUT FIELDS
        editNameTextView.setOnTouchListener(mTouchListener);
        editPriceTextView.setOnTouchListener(mTouchListener);
        editPriceTextView.setFilters(new InputFilter[] {new PriceFormatFilter(5,2)});
        editQuantityTextView.setOnTouchListener(mTouchListener);
        editSupplierNameTextView.setOnTouchListener(mTouchListener);
        editSUpplierPhoneTextView.setOnTouchListener(mTouchListener);

        if (mCurrentProductUri == null) {
            editBrowseImageButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    buttonImageClick();
                }
            });
        } else {
            editBrowseImageButton.setVisibility(View.GONE);
        }
    }
    //THIS METHOD SETS UP THE FORMAT OF THE PRICE USING INPUTFILTER
    public class PriceFormatFilter implements InputFilter {

        Pattern mPattern;

        public PriceFormatFilter(int digitsBeforeZero, int digitsAfterZero) {
            mPattern = Pattern.compile("[0-9]{0," + (digitsBeforeZero-1) + "}+((\\.[0-9]{0," + (digitsAfterZero-1) + "})?)||(\\.)?");
        }

        @Override
        public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {
            Matcher matcher = mPattern.matcher(dest);
            if (!matcher.matches())
                return "";
            return null;
        }

    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (mImageUri != null) {
            outState.putString(STATE_IMAGE_URI, mImageUri.toString());
        }
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        if (savedInstanceState.containsKey(STATE_IMAGE_URI) &&
                !savedInstanceState.getString(STATE_IMAGE_URI).equals("")) {
            mImageUri = Uri.parse(savedInstanceState.getString(STATE_IMAGE_URI));

            ViewTreeObserver viewTreeObserver = editImageImage.getViewTreeObserver();
            viewTreeObserver.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                @Override
                public void onGlobalLayout() {
                    editImageImage.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                    editImageImage.setImageBitmap(Utils.getBitmapFromUri(mImageUri, mContext, editImageImage));
                }
            });
        }
    }
    //THIS METHOD HELPS TO BROWSE THE PHONES STORAGE
    private void buttonImageClick() {
        Intent intent = new Intent();

        if (Build.VERSION.SDK_INT < 19) {
            intent = new Intent(Intent.ACTION_GET_CONTENT);
        } else {
            intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
            intent.addCategory(Intent.CATEGORY_OPENABLE);
        }
        intent.setType("image/*");
        startActivityForResult(Intent.createChooser(intent, getString(R.string.action_select_picture)), IMAGE_REQUEST_CODE);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle bundle) {
        //FETCHING DATA FROM EXISTING PRODUCT INTO THE CURSOR
        //DEFINE THE PROJECTIONS TABLE
        String[] projection = {
                StoreContract.ProductEntry._ID,
                StoreContract.ProductEntry.COLUMN_PRODUCT_NAME,
                StoreContract.ProductEntry.COLUMN_PRODUCT_PRICE,
                StoreContract.ProductEntry.COLUMN_PRODUCT_QUANTITY,
                StoreContract.ProductEntry.COLUMN_PRODUCT_IMAGE,
                StoreContract.ProductEntry.COLUMN_SUPPLIER_NAME,
                StoreContract.ProductEntry.COLUMN_SUPPLIER_PHONE
        };
        //THIS WILL EXECUTE THE QUERY
        return new CursorLoader(this,
                mCurrentProductUri,
                projection,
                null,
                null,
                null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        //IF IT IS AN EXISTING PROJECT
        if (cursor.moveToFirst()) {
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
            String productImage = cursor.getString(productImageColumnIndex);
            String supplierName = cursor.getString(supplierNameColumnIndex);
            final String supplierPhone = cursor.getString(supplierPhoneColumnIndex);

            //SETTING THE DATAS TO THE TEXTVIEWS
            editNameTextView.setText(productName);
            editPriceTextView.setText(String.valueOf(String.valueOf(productPrice)));
            editQuantityTextView.setText(String.valueOf(productQuantity));
            editSupplierNameTextView.setText(supplierName);
            editSUpplierPhoneTextView.setText(supplierPhone);

            editImageImage.setImageBitmap(Utils.getBitmapFromUri(Uri.parse(productImage), mContext, editImageImage));
            mImageUri = Uri.parse(productImage);
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        //CLEAN OUT ALL THE DATA FROM THE FIELDS
        editNameTextView.setText("");
        editPriceTextView.setText("");
        editQuantityTextView.setText("");
        editSupplierNameTextView.setText("");
        editSUpplierPhoneTextView.setText("");
    }

    //THIS METHOD SETS THE IMAGE TO THE IMAGEVIEW
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == IMAGE_REQUEST_CODE && (resultCode == RESULT_OK)) {
            try {
                mImageUri = data.getData();
                int takeFlags = data.getFlags();
                takeFlags &= (Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                try {
                    getContentResolver().takePersistableUriPermission(mImageUri, takeFlags);
                }
                catch (SecurityException e){
                    e.printStackTrace();
                }

                editImageImage.setImageBitmap(Utils.getBitmapFromUri(mImageUri, mContext, editImageImage));

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
    //INFLATE THE EDITOR MENU TO THE MENUBAR
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_editor, menu);
        return true;
    }
    //
    @Override
    public Intent getSupportParentActivityIntent() {
        Intent intent = super.getSupportParentActivityIntent();
        if (intent != null) {
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        }
        return intent;
    }
    //THIS WILL HANDLE THE ACTIONS DEPENDING ON THE OPTION SELECTED
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_save:
                if (mCurrentProductUri == null) {
                    addProduct();
                } else {
                    updateProduct();
                }
                return true;

            case android.R.id.home:
                if (!mItemHasChanged && !hasEntry()) {
                    finish();
                    return true;
                }
                DialogInterface.OnClickListener discardButtonClickListener =
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                finish();
                            }
                        };
                //SHOWS A DIALOG ABOUT UNSAVED CHANGES
                showUnsavedChangesDialog(discardButtonClickListener);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
    //METHOD DEFINES WHAT SHOULD HAPPEN WHEN THE USER PRESSES THE BACK BUTTON
    @Override
    public void onBackPressed() {
        if (!mItemHasChanged && !hasEntry()) {
            super.onBackPressed();
            return;
        }
        DialogInterface.OnClickListener discardButtonClickListener =
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        finish();
                    }
                };
        //SHOWS A DIALOG ABOUT UNSAVED CHANGES
        showUnsavedChangesDialog(discardButtonClickListener);
    }
    //THIS METHOD WILL WARN THE USER ABOUT UNSAVED CHANGES
    private void showUnsavedChangesDialog(
            DialogInterface.OnClickListener discardButtonClickListener) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.dialog_unsaved_changes);
        builder.setPositiveButton(R.string.action_yes, discardButtonClickListener);
        builder.setNegativeButton(R.string.action_no, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }
    //THIS METHOD CHECKS IF ANY ENTRY HAS BEEN MADE
    public boolean hasEntry() {
        boolean hasInput = false;

        if (!TextUtils.isEmpty(editNameTextView.getText().toString()) ||
                !TextUtils.isEmpty(editPriceTextView.getText().toString()) ||
                !TextUtils.isEmpty(editQuantityTextView.getText().toString()) ||
                !TextUtils.isEmpty(editSupplierNameTextView.getText().toString()) ||
                !TextUtils.isEmpty(editSUpplierPhoneTextView.getText().toString()) ||
                (editImageImage.getDrawable() != null)) {
            hasInput = true;
        }
        return hasInput;
    }
    //THIS METHOD WILL ADD A NEW PRODUCT TO THE DATABASE
    public void addProduct(){
        //ADDING VALUES TO THE CONTENT VALUES
        if (getEditorInputs()){
            ContentValues contentValues = new ContentValues();
            contentValues.put(StoreContract.ProductEntry.COLUMN_PRODUCT_NAME, productName);
            contentValues.put(StoreContract.ProductEntry.COLUMN_PRODUCT_PRICE, productPrice);
            contentValues.put(StoreContract.ProductEntry.COLUMN_PRODUCT_QUANTITY, productQuantity);
            contentValues.put(StoreContract.ProductEntry.COLUMN_PRODUCT_IMAGE, imagePath);
            contentValues.put(StoreContract.ProductEntry.COLUMN_SUPPLIER_NAME, productSupplierName);
            contentValues.put(StoreContract.ProductEntry.COLUMN_SUPPLIER_PHONE, productSupplierPhone);

            mCurrentProductUri = getContentResolver().insert(StoreContract.ProductEntry.CONTENT_URI, contentValues);
            //MAKING TOAST MESSAGE TO INFORM THE USER ABOUT WAS THE INSERT SUCCESSFUL OR NOT
            if (mCurrentProductUri == null){
                Toast.makeText(this, "Product wasn't added!", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Product was added successfully!", Toast.LENGTH_SHORT).show();
                finish();
            }
        }
    }
    //THIS METHOD WILL UPDATE A PRODUCT IN THE DATABASE
    public void updateProduct(){
        //ADDING VALUES TO THE CONTENTVALUES
        if (getEditorInputs()){
            ContentValues contentValues = new ContentValues();
            contentValues.put(StoreContract.ProductEntry.COLUMN_PRODUCT_NAME, productName);
            contentValues.put(StoreContract.ProductEntry.COLUMN_PRODUCT_PRICE, productPrice);
            contentValues.put(StoreContract.ProductEntry.COLUMN_PRODUCT_QUANTITY, productQuantity);
            contentValues.put(StoreContract.ProductEntry.COLUMN_SUPPLIER_NAME, productSupplierName);
            contentValues.put(StoreContract.ProductEntry.COLUMN_SUPPLIER_PHONE, productSupplierPhone);

            int numRowsUpdated = getContentResolver().update(mCurrentProductUri, contentValues, null, null);
            //MAKING TOAST MESSAGE TO INFORM THE USER ABOUT WAS THE INSERT SUCCESSFUL OR NOT
            if (!(numRowsUpdated > 0)) {
                Toast.makeText(this, getString(R.string.error_update_failed), Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, getString(R.string.confirm_update_successful), Toast.LENGTH_SHORT).show();
                finish();
            }
        }
    }
    //THIS METHOD WILL VALIDATE THE INPUTS
    public boolean getEditorInputs() {

        productName = editNameTextView.getText().toString().trim();
        priceString = editPriceTextView.getText().toString().trim();
        quantityString = editQuantityTextView.getText().toString().trim();
        productSupplierName = editSupplierNameTextView.getText().toString().trim();
        productSupplierPhone = editSUpplierPhoneTextView.getText().toString().trim();

        //CHECKS THE PRODUCT'S NAME
        if (TextUtils.isEmpty(productName)) {
            editNameTextView.requestFocus();
            editNameTextView.setError(getString(R.string.error_empty_product));
            return false;
        }

        //CHECKS THE PRODUCT'S PRICE

        if (TextUtils.isEmpty(priceString)) {
            editPriceTextView.requestFocus();
            editPriceTextView.setError(getString(R.string.error_empty_price));
            return false;
        }

        //CHECKS THE QUANTITY
        if (TextUtils.isEmpty(quantityString)) {
            editQuantityTextView.requestFocus();
            editQuantityTextView.setError(getString(R.string.error_empty_quantity));
            return false;
        }

        //CHECKS TO SUPPLIER'S NAME
        if (TextUtils.isEmpty(productSupplierName)) {
            editSupplierNameTextView.requestFocus();
            editSupplierNameTextView.setError(getString(R.string.error_empty_supplier_name));
            return false;
        }
        //CHECKS SUPPLIER'S PHONE
        if (TextUtils.isEmpty(productSupplierPhone)){
            editSUpplierPhoneTextView.requestFocus();
            editSUpplierPhoneTextView.setError(getString(R.string.error_empty_supplier_phone));
        }
        //CHECKS IF AN IMAGE WAS SELECTED
        if (mCurrentProductUri == null) {
            if (mImageUri == null){
                Toast.makeText(this, "Do not forget to add an image!", Toast.LENGTH_SHORT).show();
                return false;
            } else {
                imagePath = mImageUri.toString();
            }
        }

        productPrice = Double.valueOf(priceString);
        productQuantity = Integer.valueOf(quantityString);

        return true;
    }
}
