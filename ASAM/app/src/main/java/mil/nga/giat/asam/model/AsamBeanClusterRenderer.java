package mil.nga.giat.asam.model;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.util.TypedValue;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.maps.android.clustering.Cluster;
import com.google.maps.android.clustering.ClusterManager;
import com.google.maps.android.clustering.view.DefaultClusterRenderer;

import mil.nga.giat.asam.R;
import mil.nga.giat.asam.util.AsamConstants;

public class AsamBeanClusterRenderer extends DefaultClusterRenderer<AsamBean> {
    private Context context;
    private Paint primaryColor;
    private Paint primaryDarkColor;
    private Paint accentColor;
    private Paint textPaint;
    private Bitmap[] bitmapSizes;
    private Rect bounds;

    public AsamBeanClusterRenderer(Context context, GoogleMap map, ClusterManager<AsamBean> clusterManager) {
        super(context, map, clusterManager);
        this.context = context;
        primaryColor = new Paint();
        primaryColor.setColor(fetchPrimary(context));
        primaryColor.setStyle(Paint.Style.FILL);
        primaryColor.setAntiAlias(true);
        primaryDarkColor = new Paint();
        primaryDarkColor.setColor(fetchPrimaryDark(context));
        primaryDarkColor.setStyle(Paint.Style.FILL);
        primaryDarkColor.setAntiAlias(true);
        accentColor = new Paint();
        accentColor.setColor(fetchAccentColor(context));
        accentColor.setStyle(Paint.Style.FILL);
        accentColor.setAntiAlias(true);

        textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        textPaint.setColor(Color.rgb(255, 255, 255));
        textPaint.setTextSize((int)(AsamConstants.CLUSTER_TEXT_POINT_SIZE * context.getResources().getDisplayMetrics().scaledDensity));

        bounds = new Rect();

        initializeBitmaps();
    }

    /**
     * initialize the bitmaps
     */
    private void initializeBitmaps() {
        bitmapSizes = new Bitmap[4];
        float density = context.getResources().getDisplayMetrics().density;
        int i = 0;
        for (int px = 30; px <= 60; px+=10) {
            int pxD = px * (int) density;
            Bitmap bitmap = Bitmap.createBitmap(pxD, pxD, Bitmap.Config.ARGB_8888);
            Canvas c = new Canvas(bitmap);
            c.drawCircle(pxD / 2, pxD / 2, pxD / 2, accentColor);
            c.drawCircle(pxD / 2, pxD / 2, pxD / 2 - pxD / 10, primaryDarkColor);
            Bitmap.Config bitmapConfig = bitmap.getConfig();
            if (bitmapConfig == null) {
                bitmapConfig = android.graphics.Bitmap.Config.ARGB_8888;
            }
            bitmapSizes[i] = bitmap.copy(bitmapConfig, true);
            i++;
        }
    }

    @Override
    protected boolean shouldRenderAsCluster(Cluster cluster) {
        // Always render clusters.
        return cluster.getSize() > 1;
    }

    @Override
    protected void onBeforeClusterItemRendered(AsamBean item, MarkerOptions markerOptions) {
        markerOptions.icon(AsamConstants.ASAM_MARKER);
    }

    @Override
    protected void onBeforeClusterRendered(Cluster<AsamBean> cluster, MarkerOptions markerOptions) {
        markerOptions.icon(BitmapDescriptorFactory.fromBitmap(drawNumberOnClusterMarker(context, cluster.getSize())));
    }

    private int fetchAccentColor(Context context) {
        final TypedValue value = new TypedValue ();
        context.getTheme ().resolveAttribute (R.attr.colorAccent, value, true);
        return value.data;
    }

    private int fetchPrimaryDark(Context context) {
        final TypedValue value = new TypedValue ();
        context.getTheme ().resolveAttribute (R.attr.colorPrimaryDark, value, true);
        return value.data;
    }

    private int fetchPrimary(Context context) {
        final TypedValue value = new TypedValue ();
        context.getTheme ().resolveAttribute (R.attr.colorPrimary, value, true);
        return value.data;
    }

    public Bitmap drawNumberOnClusterMarker(Context context, int number) {
        String numberOfPoints = String.valueOf(number);

        Bitmap bitmap = Bitmap.createBitmap(bitmapSizes[0]);
        if (number > 100)  {
            bitmap = Bitmap.createBitmap(bitmapSizes[3]);
        } else if (number > 50) {
            bitmap = Bitmap.createBitmap(bitmapSizes[2]);
        } else if (number > 10) {
            bitmap = Bitmap.createBitmap(bitmapSizes[1]);
        }

        Canvas canvas = new Canvas(bitmap);
        textPaint.getTextBounds(numberOfPoints, 0, numberOfPoints.length(), bounds);
        int centerX = (bitmap.getWidth() - bounds.width()) / 2;
        int centerY = (bitmap.getHeight() + bounds.height()) / 2;
        canvas.drawText(numberOfPoints, centerX, centerY, textPaint);

        return new BitmapDrawable(context.getResources(), bitmap).getBitmap();
    }
}
