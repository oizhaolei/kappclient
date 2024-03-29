package org.fdroid.fdroid;

import android.annotation.TargetApi;
import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.os.AsyncTask;
import android.os.Build;
import android.util.Log;
import android.view.Display;
import android.widget.ImageView;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.encode.Contents;
import com.google.zxing.encode.QRCodeEncoder;

// zxing is android-8 and above
@TargetApi(8)
public class QrGenAsyncTask extends AsyncTask<String, Void, Void> {
    private static final String TAG = "QrGenAsyncTask";

    private Activity activity;
    private int viewId;
    private Bitmap qrBitmap;

    public QrGenAsyncTask(Activity activity, int viewId) {
        this.activity = activity;
        this.viewId = viewId;
    }

    /*
     * The method for getting screen dimens changed, so this uses both the
     * deprecated one and the 13+ one, and supports all Android versions.
     */
    @SuppressWarnings("deprecation")
    @TargetApi(13)
    @Override
    protected Void doInBackground(String... s) {
        String qrData = s[0];
        Display display = activity.getWindowManager().getDefaultDisplay();
        Point outSize = new Point();
        int x, y, qrCodeDimension;
        /* lame, got to use both the new and old APIs here */
        if (Build.VERSION.SDK_INT >= 13) {
            display.getSize(outSize);
            x = outSize.x;
            y = outSize.y;
        } else {
            x = display.getWidth();
            y = display.getHeight();
        }
        if (x < y)
            qrCodeDimension = x;
        else
            qrCodeDimension = y;
        Log.i(TAG, "generating QRCode Bitmap of " + qrCodeDimension + "x" + qrCodeDimension);
        QRCodeEncoder qrCodeEncoder = new QRCodeEncoder(qrData, null,
                Contents.Type.TEXT, BarcodeFormat.QR_CODE.toString(), qrCodeDimension);

        try {
            qrBitmap = qrCodeEncoder.encodeAsBitmap();
        } catch (WriterException e) {
            Log.e(TAG, e.getMessage());
        }
        return (Void) null;
    }

    @Override
    protected void onPostExecute(Void v) {
        ImageView qrCodeImageView = (ImageView) activity.findViewById(viewId);

        // If the generation takes too long for whatever reason, then this view, and indeed the entire
        // activity may not be around any more.
        if (qrCodeImageView != null) {
            qrCodeImageView.setImageBitmap(qrBitmap);
        }
    }
}
