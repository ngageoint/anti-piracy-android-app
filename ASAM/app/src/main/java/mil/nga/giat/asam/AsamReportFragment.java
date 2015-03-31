package mil.nga.giat.asam;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import mil.nga.giat.asam.map.SingleAsamMapActivity;
import mil.nga.giat.asam.model.AsamBean;
import mil.nga.giat.asam.util.AsamConstants;
import mil.nga.giat.asam.util.AsamUtils;


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
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.asam_report_fragment, container, false);

        mOccurrenceDateUI = (TextView) view.findViewById(R.id.asam_report_fragment_occurrence_date_ui);
        mAggressorUI = (TextView) view.findViewById(R.id.asam_report_fragment_aggressor_ui);
        mVictimUI = (TextView) view.findViewById(R.id.asam_report_fragment_victim_ui);
        mSubregionUI = (TextView) view.findViewById(R.id.asam_report_fragment_subregion_ui);
        mReferenceNumberUI = (TextView) view.findViewById(R.id.asam_report_fragment_reference_number_ui);
        mLocationUI = (TextView) view.findViewById(R.id.asam_report_fragment_location_ui);
        mDescriptionUI = (TextView) view.findViewById(R.id.asam_report_fragment_description_ui);

        return view;
    }
    
    public void mapAsamLocation(View view) {
        Intent intent = new Intent(getActivity(), SingleAsamMapActivity.class);
        intent.putExtra(AsamConstants.ASAM_KEY, mAsam);
        startActivity(intent);
    }

    public void updateContent(AsamBean asam) {
        mAsam = asam;

        // Sometimes eye sore if there is no entry. Just make a single " ".
        mOccurrenceDateUI.setText(AsamBean.OCCURRENCE_DATE_FORMAT.format(mAsam.getOccurrenceDate()));
        mAggressorUI.setText(AsamUtils.isEmpty(mAsam.getAggressor()) ? " " : mAsam.getAggressor());
        mVictimUI.setText(AsamUtils.isEmpty(mAsam.getVictim()) ? " " : mAsam.getVictim());
        mSubregionUI.setText(AsamUtils.isEmpty(mAsam.getGeographicalSubregion()) ? " " : mAsam.getGeographicalSubregion());
        mReferenceNumberUI.setText(AsamUtils.isEmpty(mAsam.getReferenceNumber()) ? " " : mAsam.getReferenceNumber());
        mLocationUI.setText(mAsam.formatLatitutdeDegMinSec() + ", " + mAsam.formatLongitudeDegMinSec());
        mDescriptionUI.setText(AsamUtils.isEmpty(mAsam.getDescription()) ? " " : mAsam.getDescription());
    }
}
