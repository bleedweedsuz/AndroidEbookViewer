package zkagazapahtnajusz.paperproject.com.paperproject.Model;

public class SearchedItems {
    private String id;
    private String imagelinkstr;
    private String titlestr;
    private String buttonstr;

    public SearchedItems(String id, String imagelinkstr, String titlestr, String buttonstr) {
        this.id = id;
        this.imagelinkstr = imagelinkstr;
        this.titlestr = titlestr;
        this.buttonstr = buttonstr;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getImagelinkstr() {
        return imagelinkstr;
    }

    public void setImagelinkstr(String imagelinkstr) {
        this.imagelinkstr = imagelinkstr;
    }

    public String getTitlestr() {
        return titlestr;
    }

    public void setTitlestr(String titlestr) {
        this.titlestr = titlestr;
    }

    public String getButtonstr() {
        return buttonstr;
    }

    public void setButtonstr(String buttonstr) {
        this.buttonstr = buttonstr;
    }
}
