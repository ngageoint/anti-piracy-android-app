package mil.nga.giat.asam.model;

import java.io.Serializable;


@SuppressWarnings("serial")
public class LegalBean implements Serializable {

    private String mTitle;
    private String mLegalText;
    
    public String getTitle() {
        return mTitle;
    }
    
    public void setTitle(String title) {
        this.mTitle = title;
    }
    
    public String getLegalText() {
        return mLegalText;
    }
    
    public void setLegalText(String legalText) {
        this.mLegalText = legalText;
    }
}
