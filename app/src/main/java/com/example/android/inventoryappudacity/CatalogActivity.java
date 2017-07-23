package com.example.android.inventoryappudacity;

import android.app.AlertDialog;
import android.app.LoaderManager;
import android.content.ContentUris;
import android.content.Context;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.android.inventoryappudacity.data.StoreContract;


public class CatalogActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor>{

    public static final String LOG_TAG = CatalogActivity.class.getName();
    final Context mContext = this;
    private static final int PRODUCT_LOADER = 1;
    private ListView mListViewProducts;
    private ProductCursorAdapter mCursorAdapter;
    private View mEmptyStateView;
    //COMPONENTS OF THE UI
    private TextView mTextViewEmptyTitle;
    private TextView mTextViewEmptySubtitle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_catalog);

        //INITIALIZE THE COMPONENTS OF THE UI
        mTextViewEmptyTitle = (TextView) findViewById(R.id.text_empty_title);
        mTextViewEmptySubtitle = (TextView) findViewById(R.id.text_empty_subtitle);
        //SET THE FAB TO OPEN THE EDITOR ACTIVITY
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(CatalogActivity.this, EditorActivity.class);
                startActivity(intent);
            }
        });
        //FIND THE LISTVIEW
        mListViewProducts = (ListView) findViewById(R.id.list_products);

        //FIND AND SET EMPTY VIEW
        mEmptyStateView = findViewById(R.id.empty_view);
        mListViewProducts.setEmptyView(mEmptyStateView);

        //SETTING UP AN ADAPTER
        mCursorAdapter = new ProductCursorAdapter(this, null);
        mListViewProducts.setAdapter(mCursorAdapter);

        //FAB ON CLICK LISTENER
        mListViewProducts.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {

                //NEW INTENT TO DETAILS ACTIVITY
                Intent intent = new Intent(CatalogActivity.this, DetailsActivity.class);

                //SPECIFY THE URI OF THE ON CLICKED LIST ITEM
                Uri currentProductUri = ContentUris.withAppendedId(StoreContract.ProductEntry.CONTENT_URI, id);

                //SET DATA TO THE INTENT
                intent.setData(currentProductUri);

                //LAUNCH THE INTENT
                startActivity(intent);
            }
        });

        //START LOADER
        getLoaderManager().initLoader(PRODUCT_LOADER, null, this);
    }
    //THIS METHOD LOADS THE DATA FROM THE DATABASE INTO THE CURSOR
    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        String querySortOder = null;
        final String SORT_ORDER_ASC = " ASC";
        final String SORT_ORDER_DESC = " DESC";
        //GETTING PREFERENCE VALUE
        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);
        String orderBy = sharedPrefs.getString(
                getString(R.string.settings_order_by_key),
                getString(R.string.settings_order_by_default)
        );
        //ORDER-BY SETTINGS
        if (orderBy.equals(getString(R.string.settings_order_by_oldest_value))) {                   //ORDER BY ID ASC
            querySortOder = StoreContract.ProductEntry._ID + SORT_ORDER_ASC;
        } else if (orderBy.equals(getString(R.string.settings_order_by_newest_value))) {            //ORDER BY ID DESC
            querySortOder = StoreContract.ProductEntry._ID + SORT_ORDER_DESC;
        } else if (orderBy.equals(getString(R.string.settings_order_by_name_asc_value))) {          //ORDER BY  PRODUCT NAME ASC
            querySortOder = StoreContract.ProductEntry.COLUMN_PRODUCT_NAME + SORT_ORDER_ASC;
        } else if (orderBy.equals(getString(R.string.settings_order_by_name_desc_value))) {         //ORDER BY  PRODUCT NAME DESC
            querySortOder = StoreContract.ProductEntry.COLUMN_PRODUCT_NAME + SORT_ORDER_DESC;
        } else if (orderBy.equals(getString(R.string.settings_order_by_price_asc_value))) {         //ORDER BY  PRODUCT PRICE ASC
            querySortOder = StoreContract.ProductEntry.COLUMN_PRODUCT_PRICE + SORT_ORDER_ASC;
        } else if (orderBy.equals(getString(R.string.settings_order_by_price_desc_value))) {        //ORDER BY  PRODUCT PRICE DESC
            querySortOder = StoreContract.ProductEntry.COLUMN_PRODUCT_PRICE + SORT_ORDER_DESC;
        } else if (orderBy.equals(getString(R.string.settings_order_by_quantity_asc_value))) {         //ORDER BY  PRODUCT QUANTITY ASC
            querySortOder = StoreContract.ProductEntry.COLUMN_PRODUCT_QUANTITY + SORT_ORDER_ASC;
        } else if (orderBy.equals(getString(R.string.settings_order_by_quantity_desc_value))) {        //ORDER BY  PRODUCT QUANTITY DESC
            querySortOder = StoreContract.ProductEntry.COLUMN_PRODUCT_QUANTITY + SORT_ORDER_DESC;
        }
        //DEFINE A PROJECTION THAT SPECIFIES THE COLUMNS FROM THE GIVEN TABLE
        String[] projection = {
                StoreContract.ProductEntry._ID,
                StoreContract.ProductEntry.COLUMN_PRODUCT_NAME,
                StoreContract.ProductEntry.COLUMN_PRODUCT_PRICE,
                StoreContract.ProductEntry.COLUMN_PRODUCT_QUANTITY,
                StoreContract.ProductEntry.COLUMN_PRODUCT_IMAGE
        };
        //THIS LOADER EXECUTES THE QUERY METHOD ON A BACKGROUND THREAD
        return new CursorLoader(this,
                StoreContract.ProductEntry.CONTENT_URI,
                projection,
                null,
                null,
                querySortOder
        );
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        //UPDATE THE NEW ADAPTER WITH THE UPDATED DATA
        mCursorAdapter.swapCursor(data);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        //CALLBACK WHEN THE DATA NEEDS TO BE DELETED
        mCursorAdapter.swapCursor(null);
    }
    public boolean onCreateOptionsMenu(Menu menu) {
        //INFLATE THE MENU OPTIONS
        getMenuInflater().inflate(R.menu.menu_catalog, menu);
        return true;
    }

    //THIS METHOD INVOKES ACTIONS WHEN THE MENU ITEM IS CLICKED
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_delete_all_entries:
                showDeleteConfirmationDialog();
                return true;
            case R.id.action_settings:
                openSettingScreen();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    //THIS DISPLAYS CONFIRMATION DIALOG BEFORE DELETING ALL THE DATA
    private void showDeleteConfirmationDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.dialog_msg_delete);

        builder.setPositiveButton(R.string.action_yes, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                deleteAllItems();
            }
        });
        builder.setNegativeButton(R.string.action_no, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });

        //CREATE AND SHOWS THE ALERT DIALOG
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    //METHOD FOR DELETE ALL THE DATA MENU ITEM
    private void deleteAllItems() {
        int rowsDeleted = getContentResolver().delete(StoreContract.ProductEntry.CONTENT_URI, null, null);
        if (rowsDeleted > 0) {
            Toast.makeText(CatalogActivity.this, getString(R.string.confirm_delete_all_entries),
                    Toast.LENGTH_SHORT).show();
        } else {
            Log.e(LOG_TAG, getString(R.string.error_delete_all_entries));
        }
    }

    //METHODS FOR SETTING ACTIVITY
    public void openSettingScreen() {
        startActivity(new Intent(CatalogActivity.this, SettingsActivity.class));
    }
}
