package mil.nga.giat.asam;

import mil.nga.giat.asam.util.AsamConstants;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;

import com.google.android.gms.maps.GoogleMap;


public class MapTypeDialogFragment extends DialogFragment {
    
    static MapTypeDialogFragment newInstance(int mapType) {
        MapTypeDialogFragment fragment = new MapTypeDialogFragment();

        Bundle args = new Bundle();
        args.putInt(AsamConstants.MAP_TYPE_KEY, mapType);
        fragment.setArguments(args);

        return fragment;
    }
    
    private int mSelectedMapType = 0;
    private String[] mapTypeValues = null;
    
    public interface OnMapTypeChangedListener {
        public void onMapTypeChanged(int mapType);
    }
    
    private OnMapTypeChangedListener mapTypeChangedListener;
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(STYLE_NORMAL, 0);
        
        int mapType = getArguments().getInt(AsamConstants.MAP_TYPE_KEY);
        mSelectedMapType = mapType > 0 ? mapType : GoogleMap.MAP_TYPE_NORMAL;
        
        mapTypeValues = getResources().getStringArray(R.array.mapTypeValues);
    }
    
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        int selectedIndex = 0;
        while (selectedIndex < mapTypeValues.length) {
            if (Integer.parseInt(mapTypeValues[selectedIndex]) == mSelectedMapType) {
                break;
            }
            selectedIndex++;
        }
        
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder
            .setTitle(R.string.dialog_map_type)
            .setSingleChoiceItems(R.array.mapTypeEntries, selectedIndex, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    mSelectedMapType = Integer.parseInt(mapTypeValues[which]);
                }
            })
            .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    mapTypeChangedListener.onMapTypeChanged(mSelectedMapType);
                }
            }).
            setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    // Nothing to do here, just let the dialog dismiss
                }
            });
      
        return builder.create();
    }
    
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mapTypeChangedListener = (OnMapTypeChangedListener) activity;
        } catch (ClassCastException caught) {
            throw new ClassCastException(activity.toString() + " must implement OnMapTypeChangedListener");
        }
    }
}
