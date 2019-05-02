package zkagazapahtnajusz.paperproject.com.paperproject.Model;

public class PostBuffer{
    private String POSTID;
    private Post.PostType POSTTYPE;

    public PostBuffer(Post.PostType POSTTYPE){
        this.POSTID = null;
        this.POSTTYPE = POSTTYPE;
    }

    public PostBuffer(String POSTID, Post.PostType POSTTYPE) {
        this.POSTID = POSTID;
        this.POSTTYPE = POSTTYPE;
    }

    public String getPOSTID() {
        return POSTID;
    }

    public void setPOSTID(String POSTID) {
        this.POSTID = POSTID;
    }

    public Post.PostType getPOSTTYPE() {
        return POSTTYPE;
    }

    public void setPOSTTYPE(Post.PostType POSTTYPE) {
        this.POSTTYPE = POSTTYPE;
    }

    @Override
    public String toString() {
        return "PostBuffer{" +
                "POSTID='" + POSTID + '\'' +
                ", POSTTYPE=" + POSTTYPE +
                '}';
    }
}
