package mil.nga.giat.asam;

import mil.nga.giat.asam.util.AsamConstants;
import mil.nga.giat.asam.util.AsamLog;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;


public class LegalDetailsFragment extends Fragment {

    public static LegalDetailsFragment newInstance() {
        LegalDetailsFragment fragment = new LegalDetailsFragment();
        return fragment;
    }
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AsamLog.i(LegalDetailsFragment.class.getName() + ":onCreate");
    }
    
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.legal_details_fragment, container, false);
        return view;
    }
    
    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        TextView legalTextUI = (TextView)getActivity().findViewById(R.id.legal_details_fragment_legal_text_ui);
        if (getArguments() != null && getArguments().containsKey(AsamConstants.LEGAL_DETAILS_TEXT_KEY)) {
            legalTextUI.setText(Html.fromHtml(getActivity().getString(getArguments().getInt(AsamConstants.LEGAL_DETAILS_TEXT_KEY)))); // From tablet.
        }
        else if (getActivity().getIntent() != null && getActivity().getIntent().getExtras() != null) {
            legalTextUI.setText(Html.fromHtml(getActivity().getString(getActivity().getIntent().getExtras().getInt(AsamConstants.LEGAL_DETAILS_TEXT_KEY)))); // From phone.
        }
    }
}
