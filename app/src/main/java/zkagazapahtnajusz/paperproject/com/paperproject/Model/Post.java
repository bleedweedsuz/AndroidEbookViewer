package zkagazapahtnajusz.paperproject.com.paperproject.Model;

/**
 * Created by Zw4R-TSTUD10 2018/4/1 [2018, April 1]
 */

public class Post {
    public enum PostType{NewPost, BookReference, Simple, DataNotFound}
    private String UID;
    private String postId;
    private String postDate;
    private PostType postType;
    private String postDescription;
    private PostBookReference postbookReference;
    private String postEdit;
    private String postPrivacy;
    public Post(PostType postType){
        this.postType = postType;
    }

    public Post(String UID, PostBookReference postBookReference, String postDate, String postDescription, String postEdit,String postId,String postPrivacy,PostType postType){
        this.UID = UID;
        this.postbookReference = postBookReference;
        this.postDate = postDate;
        this.postDescription = postDescription;
        this.postEdit = postEdit;
        this.postId = postId;
        this.postPrivacy = postPrivacy;
        this.postType = postType;
    }

    public String getUID() {
        return UID;
    }

    public void setUID(String UID) {
        this.UID = UID;
    }

    public String getPostId() {
        return postId;
    }

    public void setPostId(String postId) {
        this.postId = postId;
    }

    public String getPostDate() {
        return postDate;
    }

    public void setPostDate(String postDate) {
        this.postDate = postDate;
    }

    public PostType getPostType() {
        return postType;
    }

    public void setPostType(PostType postType) {
        this.postType = postType;
    }

    public String getPostDescription() {
        return postDescription;
    }

    public void setPostDescription(String postDescription) {
        this.postDescription = postDescription;
    }

    public PostBookReference getPostbookReference() {
        return postbookReference;
    }

    public void setPostbookReference(PostBookReference postbookReference) {
        this.postbookReference = postbookReference;
    }

    public String getPostEdit() {
        return postEdit;
    }

    public void setPostEdit(String postEdit) {
        this.postEdit = postEdit;
    }

    public String getPostPrivacy() {
        return postPrivacy;
    }

    public void setPostPrivacy(String postPrivacy) {
        this.postPrivacy = postPrivacy;
    }

    public static class PostBookReference {
        String bookId;
        String bookImage;
        String bookTitle;
        String bookDescription;

        public PostBookReference (String bookId){
            this.bookId = bookId;
            this.bookImage = null;
            this.bookTitle = null;
            this.bookImage = null;
            this.bookDescription = null;
        }

        public PostBookReference(String bookId, String bookImage, String bookTitle, String bookDescription) {
            this.bookId = bookId;
            this.bookImage = bookImage;
            this.bookTitle = bookTitle;
            this.bookDescription = bookDescription;
        }

        public String getBookId() {
            return bookId;
        }

        public void setBookId(String bookId) {
            this.bookId = bookId;
        }

        public String getBookImage() {
            return bookImage;
        }

        public void setBookImage(String bookImage) {
            this.bookImage = bookImage;
        }

        public String getBookTitle() {
            return bookTitle;
        }

        public void setBookTitle(String bookTitle) {
            this.bookTitle = bookTitle;
        }

        public String getBookDescription() {
            return bookDescription;
        }

        public void setBookDescription(String bookDescription) {
            this.bookDescription = bookDescription;
        }
    }
}






