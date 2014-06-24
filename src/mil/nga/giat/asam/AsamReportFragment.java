package mil.nga.giat.asam;

import mil.nga.giat.asam.model.AsamBean;
import mil.nga.giat.asam.util.AsamConstants;
import mil.nga.giat.asam.util.AsamLog;
import mil.nga.giat.asam.util.AsamUtils;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;


public class AsamReportFragment extends Fragment {
    
    private AsamBean mAsam;
    private TextView mOccurrenceDateUI;
    private TextView mAggressorUI;
    private TextView mVictimUI;
    private TextView mSubregionUI;
    private TextView mReferenceNumberUI;
    private TextView mLocationUI;
    private TextView mDescriptionUI;
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AsamLog.i(AsamReportFragment.class.getName() + ":onCreate");
    }
    
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.asam_report_fragment, container, false);
        return view;
    }
    
    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        AsamLog.i(AsamReportFragment.class.getName() + ":onActivityCreated");
        
        mOccurrenceDateUI = (TextView)getActivity().findViewById(R.id.asam_report_fragment_occurrence_date_ui);
        mAggressorUI = (TextView)getActivity().findViewById(R.id.asam_report_fragment_aggressor_ui);
        mVictimUI = (TextView)getActivity().findViewById(R.id.asam_report_fragment_victim_ui);
        mSubregionUI = (TextView)getActivity().findViewById(R.id.asam_report_fragment_subregion_ui);
        mReferenceNumberUI = (TextView)getActivity().findViewById(R.id.asam_report_fragment_reference_number_ui);
        mLocationUI = (TextView)getActivity().findViewById(R.id.asam_report_fragment_location_ui);
        mDescriptionUI = (TextView)getActivity().findViewById(R.id.asam_report_fragment_description_ui);
        
        Bundle args = getArguments();
        mAsam = (AsamBean)args.getSerializable(AsamConstants.ASAM_KEY);
        
        // Sometimes eye sore if there is no entry. Just make a single " ".
        mOccurrenceDateUI.setText(AsamBean.OCCURRENCE_DATE_FORMAT.format(mAsam.getOccurrenceDate()));
        mAggressorUI.setText(AsamUtils.isEmpty(mAsam.getAggressor()) ? " " : mAsam.getAggressor());
        mVictimUI.setText(AsamUtils.isEmpty(mAsam.getVictim()) ? " " : mAsam.getVictim());
        mSubregionUI.setText(AsamUtils.isEmpty(mAsam.getGeographicalSubregion()) ? " " : mAsam.getGeographicalSubregion());
        mReferenceNumberUI.setText(AsamUtils.isEmpty(mAsam.getReferenceNumber()) ? " " : mAsam.getReferenceNumber());
        mLocationUI.setText(mAsam.formatLatitutdeDegMinSec() + ", " + mAsam.formatLongitudeDegMinSec());
        mDescriptionUI.setText(AsamUtils.isEmpty(mAsam.getDescription()) ? " " : mAsam.getDescription());
    }
    
    public void mapAsamLocation(View view) {
        Intent intent = new Intent(getActivity(), SingleAsamMapActivity.class);
        intent.putExtra(AsamConstants.ASAM_KEY, mAsam);
        startActivity(intent);
    }
}
