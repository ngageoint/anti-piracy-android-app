package mil.nga.giat.asam.util;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import mil.nga.giat.asam.R;


public class AsamUtils {
    
    public static String readStream(InputStream stream) throws IOException {
        BufferedReader in = new BufferedReader(new InputStreamReader(stream));
        StringBuilder contents = new StringBuilder();
        String line;
        while ((line = in.readLine()) != null) {
            contents.append(line);
        }
        return contents.toString();
    }

    public static Bitmap drawNumberOnClusterMarker(Context context, int number) {
        String numberOfPoints = "" + number;
        float scale = context.getResources().getDisplayMetrics().density;
        Bitmap bitmap = null;

        if (number > 1000)  {
            bitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.ic_cluster_xlarge);
        } else if (number > 100) {
            bitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.ic_cluster_large);
        } else if (number > 10) {
            bitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.ic_cluster_medium);
        } else {
            bitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.ic_cluster_small);
        }

        Bitmap.Config bitmapConfig = bitmap.getConfig();
        if (bitmapConfig == null) {
            bitmapConfig = android.graphics.Bitmap.Config.ARGB_8888;
        }
        bitmap = bitmap.copy(bitmapConfig, true);
        Canvas canvas = new Canvas(bitmap);
        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setColor(Color.rgb(255, 255, 255));
        paint.setTextSize((int)(AsamConstants.CLUSTER_TEXT_POINT_SIZE * scale));
        Rect bounds = new Rect();
        paint.getTextBounds(numberOfPoints, 0, numberOfPoints.length(), bounds);
        int centerX = (bitmap.getWidth() - bounds.width()) / 2;
        int centerY = (bitmap.getHeight() + bounds.height()) / 2;
        canvas.drawText(numberOfPoints, centerX, centerY, paint);
        return new BitmapDrawable(context.getResources(), bitmap).getBitmap();
    }
}
