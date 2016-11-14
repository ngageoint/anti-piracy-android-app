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
import mil.nga.giat.asam.util.AsamUtils;


public class AsamBeanClusterRenderer extends DefaultClusterRenderer<AsamBean> {
    private Context context;
    private Paint primaryColor;
    private Paint primaryDarkColor;
    private Paint accentColor;

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
        String numberOfPoints = "" + number;
        float density = context.getResources().getDisplayMetrics().density;
        float scaledDensity = context.getResources().getDisplayMetrics().scaledDensity;

        int px = 0;

        if (number > 100)  {
            px = 60 * (int) density;
        } else if (number > 50) {
            px = 50 * (int) density;
        } else if (number > 10) {
            px = 40 * (int) density;
        } else {
            px = 30 * (int) density;
        }

        Bitmap bitmap = Bitmap.createBitmap(px, px, Bitmap.Config.ARGB_8888);
        Canvas c = new Canvas(bitmap);



        c.drawCircle(px/2, px/2, px/2, accentColor);
        c.drawCircle(px/2, px/2, px/2 - px/10, primaryDarkColor);


        Bitmap.Config bitmapConfig = bitmap.getConfig();
        if (bitmapConfig == null) {
            bitmapConfig = android.graphics.Bitmap.Config.ARGB_8888;
        }
        bitmap = bitmap.copy(bitmapConfig, true);
        Canvas canvas = new Canvas(bitmap);
        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setColor(Color.rgb(255, 255, 255));
        paint.setTextSize((int)(AsamConstants.CLUSTER_TEXT_POINT_SIZE * scaledDensity));
        Rect bounds = new Rect();
        paint.getTextBounds(numberOfPoints, 0, numberOfPoints.length(), bounds);
        int centerX = (bitmap.getWidth() - bounds.width()) / 2;
        int centerY = (bitmap.getHeight() + bounds.height()) / 2;
        canvas.drawText(numberOfPoints, centerX, centerY, paint);
        return new BitmapDrawable(context.getResources(), bitmap).getBitmap();
    }
}
