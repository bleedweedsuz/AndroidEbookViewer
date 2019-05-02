package zkagazapahtnajusz.paperproject.com.paperproject.Model;

public class Comment {
    private String id;
    private String uid;
    private String comment;
    private String date;

    public Comment(String id, String uid, String comment, String date) {
        this.id = id;
        this.uid = uid;
        this.comment = comment;
        this.date = date;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }
}
