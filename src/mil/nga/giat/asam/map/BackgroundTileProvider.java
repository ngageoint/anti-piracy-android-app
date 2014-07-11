package mil.nga.giat.asam.map;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import android.content.Context;

import com.google.android.gms.maps.model.Tile;
import com.google.android.gms.maps.model.TileProvider;


public class BackgroundTileProvider implements  TileProvider {

    private static final int TILE_WIDTH = 256;
    private static final int TILE_HEIGHT = 256;
    private static final int BUFFER_SIZE = 16 * 1024;
    
    private Context context;
    private Tile tile;
    
    public BackgroundTileProvider(Context context) {
        this.context = context;
    }
    
    @Override
    public Tile getTile(int x, int y, int zoom) {        
        return getTile();
    }
    
    private Tile getTile() {
        if (tile == null) {
            InputStream in = null;
            ByteArrayOutputStream baos = null;

            try {
                in = context.getAssets().open("background_tile.png");
                baos = new ByteArrayOutputStream();

                int nRead;
                byte[] data = new byte[BUFFER_SIZE];

                while ((nRead = in.read(data, 0, BUFFER_SIZE)) != -1) {
                    baos.write(data, 0, nRead);
                }
                baos.flush();

                tile = new Tile(TILE_WIDTH, TILE_HEIGHT, baos.toByteArray());

            } catch (IOException ignored) {
            }  finally {
                if (in != null) try { in.close(); } catch (Exception ignored) {}
                if (baos != null) try { baos.close(); } catch (Exception ignored) {}
            }
        }
        
        return tile;
    }
}
