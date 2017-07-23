package com.example.android.inventoryappudacity;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.CursorAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import com.example.android.inventoryappudacity.data.StoreContract;


public class ProductCursorAdapter extends CursorAdapter{

    public static final String LOG_TAG = ProductCursorAdapter.class.getSimpleName();

    private static Context mContext;
    //DEFAULT CONSTRUCTOR
    public ProductCursorAdapter(Context context, Cursor c) {
        super(context, c, 0);
        mContext = context;
    }

    public static class ProductViewHolder {

        TextView textViewProductName;
        TextView textViewPrice;
        TextView textViewQuantity;
        ImageView imageViewImage;
        ImageButton buttonSale;

        //VIEWS OF THE LISTVIEW
        public ProductViewHolder(View itemView) {
            textViewProductName = (TextView) itemView.findViewById(R.id.product_name_text);
            textViewPrice = (TextView) itemView.findViewById(R.id.product_price_text);
            textViewQuantity = (TextView) itemView.findViewById(R.id.product_quantity_text);
            imageViewImage = (ImageView) itemView.findViewById(R.id.product_image_image);
            buttonSale = (ImageButton) itemView.findViewById(R.id.product_sale_button);
        }
    }
    //MAKES A NEW BLANK LIST ITEM VIEW
    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.list_item, parent, false);
        ProductViewHolder holder = new ProductViewHolder(view);
        view.setTag(holder);

        return view;
    }
    //THIS METHOD BINDS THE DATA IN THE CURRENT ROW DEPENDING ON THE ID FROM THE CURSOR
    @Override
    public void bindView(View view, final Context context, Cursor cursor) {

        final ProductViewHolder holder = (ProductViewHolder) view.getTag();
        //SET THE DATA IN THE LISTVIEW
        final int productId = cursor.getInt(cursor.getColumnIndex(StoreContract.ProductEntry._ID));
        String productName = cursor.getString(cursor.getColumnIndex(StoreContract.ProductEntry.COLUMN_PRODUCT_NAME));
        Double productPrice = cursor.getDouble(cursor.getColumnIndex(StoreContract.ProductEntry.COLUMN_PRODUCT_PRICE));
        final int productQuantity = cursor.getInt(cursor.getColumnIndex(StoreContract.ProductEntry.COLUMN_PRODUCT_QUANTITY));
        final String productImage = cursor.getString(cursor.getColumnIndex(StoreContract.ProductEntry.COLUMN_PRODUCT_IMAGE));

        holder.textViewProductName.setText(productName);
        holder.textViewPrice.setText(mContext.getString(R.string.price_text_label, productPrice));
        holder.buttonSale.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Uri productUri = ContentUris.withAppendedId(StoreContract.ProductEntry.CONTENT_URI, productId);
                quantityChange(context, productUri, productQuantity);
            }
        });
        holder.textViewQuantity.setText(mContext.getString(R.string.quantity_text_label, productQuantity));
        //DISPLAY THE IMAGE
        ViewTreeObserver viewTreeObserver = holder.imageViewImage.getViewTreeObserver();
        viewTreeObserver.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                holder.imageViewImage.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                holder.imageViewImage.setImageBitmap(Utils.getBitmapFromUri(Uri.parse(productImage), mContext, holder.imageViewImage));
            }
        });
    }
    //THIS METHOD WILL UPDATE QUANTITY TEXT WHEN SALE BUTTON IS CLICKED
    public void quantityChange(Context context, Uri productUri, int currentQuantity){
        int newQuantity = (currentQuantity >= 1) ? currentQuantity - 1 : 0;
        //UPDATE THE QUANTITY WITH THE NEW VALUE
        ContentValues contentValues = new ContentValues();
        contentValues.put(StoreContract.ProductEntry.COLUMN_PRODUCT_QUANTITY, newQuantity);
        int numRowsUpdated = context.getContentResolver().update(productUri, contentValues, null, null);

        if (!(numRowsUpdated > 0)) {
            Log.e(LOG_TAG, context.getString(R.string.error_quantity_update));
        }
    }
}
