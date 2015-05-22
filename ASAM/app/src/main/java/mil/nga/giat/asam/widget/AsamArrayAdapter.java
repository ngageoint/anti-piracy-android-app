package mil.nga.giat.asam.widget;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.List;

import mil.nga.giat.asam.R;
import mil.nga.giat.asam.model.AsamBean;


public class AsamArrayAdapter extends ArrayAdapter<AsamBean> {

    private List<AsamBean> mAsams;

    public AsamArrayAdapter(Context context, int resourceId, List<AsamBean> asams) {
        super(context, resourceId, asams);
        mAsams = asams;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        View view = convertView;
        if (view == null) {
            LayoutInflater layoutInflater = (LayoutInflater)getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = layoutInflater.inflate(R.layout.asam_list_row, null);
            ViewHolder viewHolder = new ViewHolder();
            viewHolder.dateUI = (TextView)view.findViewById(R.id.asam_list_row_date_ui);
            viewHolder.aggressorUI = (TextView)view.findViewById(R.id.asam_list_row_aggressor_ui);
            viewHolder.victimUI = (TextView)view.findViewById(R.id.asam_list_row_victim_ui);
            view.setTag(viewHolder);
        }
        AsamBean asam = mAsams.get(position);
        if (asam != null) {
            ViewHolder viewHolder = (ViewHolder)view.getTag();
            viewHolder.dateUI.setText(AsamBean.OCCURRENCE_DATE_FORMAT.format(asam.getOccurrenceDate()));
            viewHolder.aggressorUI.setText(asam.getAggressor());
            viewHolder.victimUI.setText(asam.getVictim());
        }
        return view;
    }

    private static class ViewHolder {
        private TextView dateUI;
        private TextView aggressorUI;
        private TextView victimUI;
    }
}
