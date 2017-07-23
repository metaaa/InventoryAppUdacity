package com.example.android.inventoryappudacity;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.util.Log;
import android.widget.ImageView;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;


public class Utils {

    public static final String LOG_TAG = Utils.class.getSimpleName();
    //EMPTY CONSTRUCTOR
    private void Utils() {
    }

    /**
     * Method to display the image
     * Credit => Used function from https://github.com/crlsndrsjmnz/MyShareImageExample
     * @param uri - image path
     * @return Bitmap
     */
    public static Bitmap getBitmapFromUri(Uri uri, Context context, ImageView imageView) {

        if (uri == null || uri.toString().isEmpty())
            return null;

        InputStream input = null;
        try {
            input = context.getContentResolver().openInputStream(uri);

            // Get the dimensions of the bitmap
            BitmapFactory.Options bmOptions = new BitmapFactory.Options();
            bmOptions.inJustDecodeBounds = true;
            BitmapFactory.decodeStream(input, null, bmOptions);
            input.close();
            // Decode the image file into a Bitmap sized to fill the View
            bmOptions.inJustDecodeBounds = false;

            input = context.getContentResolver().openInputStream(uri);
            Bitmap bitmap = BitmapFactory.decodeStream(input, null, bmOptions);
            input.close();
            return bitmap;

        } catch (FileNotFoundException fne) {
            Log.e(LOG_TAG, context.getString(R.string.exception_image_load_failed), fne);
            return null;
        } catch (Exception e) {
            Log.e(LOG_TAG, context.getString(R.string.exception_image_load_failed), e);
            return null;
        } finally {
            try {
                input.close();
            } catch (IOException ioe) {

            }
        }
    }
}
