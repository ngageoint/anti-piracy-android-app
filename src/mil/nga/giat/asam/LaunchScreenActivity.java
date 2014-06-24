package mil.nga.giat.asam;

import mil.nga.giat.asam.util.AsamConstants;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;

public class LaunchScreenActivity extends ActionBarActivity {
    
    private View.OnClickListener mAllAsamsListener;
    private View.OnClickListener mSubregionsListener;
    private View.OnClickListener mTextQueryListener;
    private View.OnClickListener mSettingsListener;
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.launch_screen);
        
        GridView mainGrid = (GridView)findViewById(R.id.launch_screen_grid_view_ui);
        mainGrid.setAdapter(new ImageAdapter(this));
        
        mAllAsamsListener = new View.OnClickListener() {
            
            @Override
            public void onClick(View view) {
                allAsamsButtonClicked(view);
            }
        };
        
        mSubregionsListener = new View.OnClickListener() {
            
            @Override
            public void onClick(View view) {
                subregionsButtonClicked(view);
            }
        };
        
        mTextQueryListener = new View.OnClickListener() {
            
            @Override
            public void onClick(View view) {
                textQueryButtonClicked(view);
            }
        };
        
        mSettingsListener = new View.OnClickListener() {
            
            @Override
            public void onClick(View view) {
                settingsButtonClicked(view);
            }
        };
    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.launch_screen_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {        
        int itemId = item.getItemId();
        if (itemId == R.id.launch_screen_menu_information_ui) {
            Intent intent = new Intent(this, InfoActivity.class);
            startActivity(intent);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
    
    public void allAsamsButtonClicked(View view) {
        Intent intent = new Intent(this, AllAsamsMapActivity.class);
        startActivity(intent);
    }
    
    public void subregionsButtonClicked(View view) {
        Intent intent = new Intent(this, SubregionMapActivity.class);
        intent.putExtra(AsamConstants.SUBREGION_MAP_EXPECTING_RESULT_CODE_KEY, false);
        startActivity(intent);
    }
    
    public void textQueryButtonClicked(View view) {
        Intent intent = new Intent(this, TextQueryActivity.class);
        startActivity(intent);
    }
    
    public void settingsButtonClicked(View view) {
        Intent intent = new Intent(this, PreferencesActivity.class);
        startActivity(intent);
    }
    
    private class ImageAdapter extends BaseAdapter {

        private static final int NUM_BUTTONS = 4;
        private static final int ALL_ASAMS_BUTTON_POSITION = 0;
        private static final int SUBREGIONS_BUTTON_POSITION = 1;
        private static final int TEXT_QUERY_BUTTON_POSITION = 2;
        private static final int SETTINGS_BUTTON_POSITION = 3;
        private Context mContext;

        ImageAdapter(Context context) {
            mContext = context;
        }

        @Override
        public int getCount() {
            return NUM_BUTTONS;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View view = getLayoutInflater().inflate(R.layout.launch_screen_button, null);
            TextView labelUI = (TextView)view.findViewById(R.id.launch_screen_button_icon_label_ui);
            ImageView iconUI = (ImageView)view.findViewById(R.id.launch_screen_button_icon_ui);
            switch (position) {
                case ALL_ASAMS_BUTTON_POSITION:
                    labelUI.setText(mContext.getString(R.string.launch_screen_all_asams_label_text));
                    iconUI.setContentDescription(mContext.getString(R.string.launch_screen_all_asams_label_text));
                    iconUI.setImageResource(R.drawable.ic_mapview);
                    iconUI.setOnClickListener(mAllAsamsListener);
                    break;

                case SUBREGIONS_BUTTON_POSITION:
                    labelUI.setText(mContext.getString(R.string.launch_screen_subregions_label_text));
                    iconUI.setContentDescription(mContext.getString(R.string.launch_screen_subregions_label_text));
                    iconUI.setImageResource(R.drawable.ic_subregion);
                    iconUI.setOnClickListener(mSubregionsListener);
                    break;

                case TEXT_QUERY_BUTTON_POSITION:
                    labelUI.setText(mContext.getString(R.string.launch_screen_text_query_label_text));
                    iconUI.setContentDescription(mContext.getString(R.string.launch_screen_text_query_label_text));
                    iconUI.setImageResource(R.drawable.ic_query);
                    iconUI.setOnClickListener(mTextQueryListener);
                    break;

                case SETTINGS_BUTTON_POSITION:
                    labelUI.setText(mContext.getString(R.string.launch_screen_settings_label_text));
                    iconUI.setContentDescription(mContext.getString(R.string.launch_screen_settings_label_text));
                    iconUI.setImageResource(R.drawable.ic_settings);
                    iconUI.setOnClickListener(mSettingsListener);
                    break;
            }
            return view;
        }

        @Override
        public Object getItem(int position) {
            return null;
        }

        @Override
        public long getItemId(int position) {
            return position;
        }
    }
}