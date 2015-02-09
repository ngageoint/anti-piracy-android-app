package mil.nga.giat.asam.util;

import mil.nga.giat.asam.R;

import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;


public class AsamConstants {
    
    public static final String ASAM_KEY = "asam";
    public static final int QUERY_TIMEOUT_THRESHOLD_IN_MILLISECONDS = 20 * 1000;
    public static final String LAST_SYNC_TIME = "last_sync_time";
    public static final String HIDE_DISCLAIMER_KEY = "hide_disclaimer";
    public static final String TABLET_IS_LAUNCHING_KEY = "tablet_is_launching";
    public static final String INITIAL_MAP_POSITION_KEY = "initial_map_position";
    public static final String SUBREGION_MAP_EXPECTING_RESULT_CODE_KEY = "subregion_map_expecting_result_code";
    public static final String LEGAL_DETAILS_TITLE_KEY = "legal_deatails_title";
    public static final String LEGAL_DETAILS_TEXT_KEY = "legal_deatails_text";
    public static final String TEXT_QUERY_DIALOG_TAG = "text_query_dialog";
    public static final String DISCLAIMER_DIALOG_TAG = "disclaimer_dialog";
    public static final String PREFERENCES_DIALOG_TAG = "preferences_dialog";
    public static final String INFO_DIALOG_TAG = "info_dialog";
    
    public static final String MAP_TYPE_KEY = "map_type_key";
    public static final int MAP_TYPE_OFFLINE_110M = 100;
    
    public static final String SORT_DIRECTION_KEY = "sort_direction";
    public static final String SPINNER_SELECTION_KEY = "sort_popup_spinner_selection";
    public static final int SORT_ASCENDING = 0;
    public static final int SORT_DESCENDING = 1;
    
    public static final int OCURRENCE_DATE_SORT = 0;
    public static final int SUBREGION_SORT = 1;
    public static final int REFERENCE_NUMBER_SORT = 2;
    public static final int VICTIM_SORT = 3;
    public static final int AGGRESSOR_SORT = 4;
    
    public static final String QUERY_TYPE_KEY = "query_type_key";
    public static final int ALL_ASAMS_QUERY = 0;
    public static final int SUBREGION_QUERY = 1;
    public static final int TEXT_QUERY = 2;
    
    public static final String SUBREGION_QUERY_TIME_SPAN_KEY = "subregion_query_time_span_key";
    public static final String SUBREGION_QUERY_SUBREGIONS_LIST_KEY = "subregion_query_subregions_list_key";
    public static final String TEXT_QUERY_PARAMETERS_KEY = "text_query_parameters";
    public static final int TIME_SPAN_60_DAYS = 60;
    public static final int TIME_SPAN_90_DAYS = 90;
    public static final int TIME_SPAN_180_DAYS = 180;
    public static final int TIME_SPAN_1_YEAR = 365; // days
    public static final int TIME_SPAN_5_YEARS = 365 * 5; // days
    public static final int TIME_SPAN_ALL = 365 * 50; // days
    
    public static final int ZOOM_LEVEL_TO_DRAW_ALL_CLUSTERS = 4;
    public static final int MIN_ZOOM_LEVEL_FOR_CLUSTERING = 5;
    public static final int MAX_ZOOM_LEVEL_FOR_CLUSTERING = 11;
    public static final int MAX_ZOOM_LEVEL_FOR_MERGE_CLUSTERING = 9;
    public static final int MAX_NUM_ASAMS_FOR_NO_CLUSTERING_WITH_ZOOM_LEVEL = 1000;
    
    public static final int SINGLE_ASAM_ZOOM_LEVEL = 5;
    public static final int CLUSTER_TEXT_POINT_SIZE = 13;
    public static final BitmapDescriptor PIRATE_MARKER = BitmapDescriptorFactory.fromResource(R.drawable.ic_pirate_marker);
    public static final BitmapDescriptor CLUSTER_MARKER = BitmapDescriptorFactory.fromResource(R.drawable.ic_cluster);
}
