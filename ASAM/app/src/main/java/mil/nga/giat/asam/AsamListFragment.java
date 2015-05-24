package mil.nga.giat.asam;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import java.util.List;

import mil.nga.giat.asam.model.AsamBean;
import mil.nga.giat.asam.util.AsamConstants;
import mil.nga.giat.asam.util.AsamListContainer;
import mil.nga.giat.asam.util.AsamLog;
import mil.nga.giat.asam.widget.AsamArrayAdapter;


public class AsamListFragment extends Fragment implements AdapterView.OnItemClickListener {

    public interface OnAsamSelectedListener {
        public void onAsamSelected(AsamBean asam);
    }

    private int mSortDirection;
    private int mSortPopupSpinnerSelection;
    private List<AsamBean> mAsams;
    private ListView mAsamListViewUI;
    private AsamArrayAdapter mAsamArrayAdapter;
    private OnAsamSelectedListener mOnAsamSelectedListener;
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AsamLog.i(AsamListFragment.class.getName() + ":onCreate");
        setHasOptionsMenu(true);
    }
    
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.asam_list_fragment, container, false);
        return view;
    }
    
    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.asam_list_fragment_menu, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemId = item.getItemId();
        if (itemId == R.id.asam_list_fragment_menu_sort_ui) {
            DialogFragment dialogFragment = SortAsamListDialogFragment.newInstance(mSortDirection, mSortPopupSpinnerSelection);
            dialogFragment.show(getActivity().getSupportFragmentManager(), "dialog");
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        AsamLog.i(AsamListFragment.class.getName() + ":onActivityCreated");
        mAsamListViewUI = (ListView)getActivity().findViewById(R.id.asam_list_tablet_list_view_ui);
        
        TextView emptyListViewUI = new TextView(getActivity());
        emptyListViewUI.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
        emptyListViewUI.setText(getResources().getString(R.string.asam_list_empty_list_text));
        emptyListViewUI.setVisibility(View.GONE);
        mAsamListViewUI.setEmptyView(emptyListViewUI);
        
        mAsams = AsamListContainer.mAsams;
        mAsamArrayAdapter = new AsamArrayAdapter(getActivity(), R.layout.asam_list_row, mAsams);
        mSortDirection = AsamConstants.SORT_DESCENDING;
        mSortPopupSpinnerSelection = AsamConstants.OCURRENCE_DATE_SORT;
        mAsamArrayAdapter.sort(new AsamBean.DescendingOccurrenceDateComparator());
        mAsamListViewUI.setAdapter(mAsamArrayAdapter);
        mAsamListViewUI.setOnItemClickListener(this);
    }
    
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        AsamLog.i(AsamListFragment.class.getName() + ":onAttach");
        try {
            mOnAsamSelectedListener = (OnAsamSelectedListener)activity;
        } catch (ClassCastException caught) {
            throw new ClassCastException(activity.toString() + " must implement OnAsamSelectedListener");
        }
    }
        
    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        mOnAsamSelectedListener.onAsamSelected(mAsams.get(position));
    }
    
    public void onSortAsamList(int sortDirection, int sortPopupSpinnerSelection) {
        mSortDirection = sortDirection;
        mSortPopupSpinnerSelection = sortPopupSpinnerSelection;
        switch (mSortPopupSpinnerSelection) {
            case AsamConstants.AGGRESSOR_SORT:
                if (mSortDirection == AsamConstants.SORT_ASCENDING) {
                    mAsamArrayAdapter.sort(new AsamBean.AscendingAggressorComparator());
                } else {
                    mAsamArrayAdapter.sort(new AsamBean.DescendingAggressorComparator());
                }
                break;

            case AsamConstants.OCURRENCE_DATE_SORT:
                if (mSortDirection == AsamConstants.SORT_ASCENDING) {
                    mAsamArrayAdapter.sort(new AsamBean.AscendingOccurrenceDateComparator());
                } else {
                    mAsamArrayAdapter.sort(new AsamBean.DescendingOccurrenceDateComparator());
                }
                break;

            case AsamConstants.REFERENCE_NUMBER_SORT:
                if (mSortDirection == AsamConstants.SORT_ASCENDING) {
                    mAsamArrayAdapter.sort(new AsamBean.AscendingReferenceNumberComparator());
                } else {
                    mAsamArrayAdapter.sort(new AsamBean.DescendingReferenceNumberComparator());
                }
                break;

            case AsamConstants.SUBREGION_SORT:
                if (mSortDirection == AsamConstants.SORT_ASCENDING) {
                    mAsamArrayAdapter.sort(new AsamBean.AscendingSubregionComparator());
                } else {
                    mAsamArrayAdapter.sort(new AsamBean.DescendingSubregionComparator());
                }
                break;

            case AsamConstants.VICTIM_SORT:
                if (mSortDirection == AsamConstants.SORT_ASCENDING) {
                    mAsamArrayAdapter.sort(new AsamBean.AscendingVictimComparator());
                } else {
                    mAsamArrayAdapter.sort(new AsamBean.DescendingVictimComparator());
                }
                break;
        }
    }
}
