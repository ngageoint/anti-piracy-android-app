package mil.nga.giat.asam.widget;

import android.content.Context;
import android.graphics.Typeface;
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
            viewHolder.date = view.findViewById(R.id.asam_list_row_date_ui);
            viewHolder.aggressor = view.findViewById(R.id.asam_list_row_hostility_ui);
            viewHolder.victim = view.findViewById(R.id.asam_list_row_victim_ui);
            viewHolder.description = view.findViewById(R.id.asam_list_row_description_ui);
            view.setTag(viewHolder);
        }

        AsamBean asam = mAsams.get(position);
        if (asam != null) {
            ViewHolder viewHolder = (ViewHolder)view.getTag();
            viewHolder.date.setText(AsamBean.OCCURRENCE_DATE_FORMAT.format(asam.getOccurrenceDate()));

            String aggressor = asam.getAggressor();
            if (aggressor == null) {
                viewHolder.aggressor.setText("Aggressor Unknown");
                viewHolder.aggressor.setAlpha(.4f);
                viewHolder.aggressor.setTypeface(viewHolder.aggressor.getTypeface(), Typeface.ITALIC);
            } else {
                viewHolder.aggressor.setText(aggressor);
                viewHolder.aggressor.setAlpha(.87f);
                viewHolder.aggressor.setTypeface(viewHolder.aggressor.getTypeface(), Typeface.NORMAL);
            }

            String victim = asam.getVictim();
            if (victim == null) {
                viewHolder.victim.setText("Victim Unknown");
                viewHolder.victim.setAlpha(.4f);
                viewHolder.victim.setTypeface(viewHolder.victim.getTypeface(), Typeface.ITALIC);
            } else {
                viewHolder.victim.setText(victim);
                viewHolder.victim.setAlpha(.87f);
                viewHolder.victim.setTypeface(viewHolder.victim.getTypeface(), Typeface.NORMAL);
            }

            viewHolder.description.setText(asam.getDescription());
        }
        return view;
    }

    private static class ViewHolder {
        private TextView date;
        private TextView aggressor;
        private TextView victim;
        private TextView description;
    }
}
