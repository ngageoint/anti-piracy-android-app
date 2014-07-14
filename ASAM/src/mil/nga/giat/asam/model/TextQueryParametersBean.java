package mil.nga.giat.asam.model;

import java.io.Serializable;

import mil.nga.giat.asam.util.AsamUtils;


@SuppressWarnings("serial")
public class TextQueryParametersBean implements Serializable {

    public String mDateFrom;
    public String mDateTo;
    public String mSubregion;
    public String mReferenceNumber;
    public String mVictim;
    public String mAggressor;
    
    public static TextQueryParametersBean newInstance(TextQueryParametersBean parameters) {
        TextQueryParametersBean copy = new TextQueryParametersBean();
        copy.mDateFrom = parameters.mDateFrom;
        copy.mDateTo = parameters.mDateTo;
        copy.mSubregion = parameters.mSubregion;
        copy.mReferenceNumber = parameters.mReferenceNumber;
        copy.mVictim = parameters.mVictim;
        copy.mAggressor = parameters.mAggressor;
        return copy;
    }
    
    public boolean isEmpty() {
        boolean empty = true;
        if (!AsamUtils.isEmpty(mDateFrom)) {
            empty = false;
        }
        else if (!AsamUtils.isEmpty(mDateTo)) {
            empty = false;
        }
        else if (!AsamUtils.isEmpty(mSubregion)) {
            empty = false;
        }
        else if (!AsamUtils.isEmpty(mReferenceNumber)) {
            empty = false;
        }
        else if (!AsamUtils.isEmpty(mVictim)) {
            empty = false;
        }
        else if (!AsamUtils.isEmpty(mAggressor)) {
            empty = false;
        }
        return empty;
    }
    
    public String getParametersAsFormattedHtml() {
        StringBuilder html = new StringBuilder();
        if (!AsamUtils.isEmpty(mDateFrom)) {
            html.append(String.format("<br/>&nbsp;&nbsp;- <b>Date From:</b> %s", mDateFrom));
        }
        if (!AsamUtils.isEmpty(mDateTo)) {
            html.append(String.format("<br/>&nbsp;&nbsp;- <b>Date To:</b> %s", mDateTo));
        }
        if (!AsamUtils.isEmpty(mSubregion)) {
            html.append(String.format("<br/>&nbsp;&nbsp;- <b>Subregion:</b> %s", mSubregion));
        }
        if (!AsamUtils.isEmpty(mReferenceNumber)) {
            html.append(String.format("<br/>&nbsp;&nbsp;- <b>Reference Number:</b> %s", mReferenceNumber));
        }
        if (!AsamUtils.isEmpty(mVictim)) {
            html.append(String.format("<br/>&nbsp;&nbsp;- <b>Victim:</b> %s", mVictim));
        }
        if (!AsamUtils.isEmpty(mAggressor)) {
            html.append(String.format("<br/>&nbsp;&nbsp;- <b>Aggressor:</b> %s", mAggressor));
        }
        return html.toString();
    }
}
