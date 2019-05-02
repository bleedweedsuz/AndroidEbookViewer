package zkagazapahtnajusz.paperproject.com.paperproject.Utilities;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;

public class ImageHelper {
    private static String TAG = "Image Compress Helper:";

    private static int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
        // Raw height and width of image
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {
            final int halfHeight = height / 2;
            final int halfWidth = width / 2;
            // Calculate the largest inSampleSize value that is a power of 2 and keeps both
            // height and width larger than the requested height and width.
            while ((halfHeight / inSampleSize) > reqHeight
                    && (halfWidth / inSampleSize) > reqWidth) {
                inSampleSize *= 2;
            }
        }
        return inSampleSize;
    }

    public static Bitmap GetCompressBitmap(Bitmap bitmap, int reqWidth, int reqHeight) {
        /*
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 0, bos);
        byte[] bitmapdata = bos.toByteArray();
        ByteArrayInputStream bs = new ByteArrayInputStream(bitmapdata);

        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeByteArray(bitmapdata, 0, bitmapdata.length);
        // Calculate inSampleSize based on a preset ratio
        options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);
        // Decode bitmap with inSampleSize set
        options.inJustDecodeBounds = false;
        Bitmap compressedImage = BitmapFactory.decodeFile(filePath, options);
        return compressedImage;
        */

        Bitmap compressedBitmap = null;
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 0, byteArrayOutputStream);
        byte[] bufferBitmap = byteArrayOutputStream.toByteArray();

        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeByteArray(bufferBitmap, 0, bufferBitmap.length);
        options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);
        options.inJustDecodeBounds = false;

        compressedBitmap = bitmap;
        return compressedBitmap;
    }

    public static void SaveImage(Bitmap bitmap, String _dir, String fileStr){
        try {
            File file = new File(_dir);
            if (!file.exists()) {
                file.mkdirs();
            }
            try {
                File _bufferFile = new File(_dir + fileStr);
                if(_bufferFile.exists()){
                    _bufferFile.delete();
                }
                FileOutputStream out = new FileOutputStream(_bufferFile);
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out);
                out.flush();
                out.close();
                Log.i(TAG, "SAVE TO FILE:" + _bufferFile.getAbsolutePath());
            } catch (Exception ex) {
                Log.e(TAG, "" + ex.toString());
            }
        }
        catch (Exception ex){
            Log.e(TAG, ex.toString());
        }
    }
}
