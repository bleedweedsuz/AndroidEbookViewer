package zkagazapahtnajusz.paperproject.com.paperproject;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.Html;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.github.marlonlom.utilities.timeago.TimeAgo;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.functions.HttpsCallableResult;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;
import zkagazapahtnajusz.paperproject.com.paperproject.Fragments.Fragment_Activity;
import zkagazapahtnajusz.paperproject.com.paperproject.Model.Comment;
import zkagazapahtnajusz.paperproject.com.paperproject.Model.Post;
import zkagazapahtnajusz.paperproject.com.paperproject.Utilities.FirebaseUtils;
import zkagazapahtnajusz.paperproject.com.paperproject.Utilities.Utils;

public class Activity_Comment extends AppCompatActivity implements SwipeRefreshLayout.OnRefreshListener{
    String TAG = "ACTIVITY COMMENT";

    public enum CommentFrom { Book, Post}
    CircleImageView userImage;
    String ID;
    LinearLayoutManager linearLayoutManager;
    RecyclerView mRecyclerView;
    AdapterComment adapter;
    AdapterComment_FBookDetails adapter2;

    Toolbar toolbar;
    EditText editComment;
    TextView textPost;
    List<Comment> mList = new ArrayList<>();
    ProgressBar commentProgress;
    CommentFrom commentFrom;
    SwipeRefreshLayout swipeRefreshLayout;
    String InComingType = null;
    ListenerRegistration listenerRegistration;

    public Post post;
    public boolean isFirstScroll = true;

    public boolean isBookDetailsComment = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_comment);

        //Set LinerLayoutManager
        linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setReverseLayout(false);

        //GET POST ID OR BOOK ID
        ID = getIntent().getExtras().getString("ID");
        commentFrom = CommentFrom.valueOf(getIntent().getExtras().getString("commentFrom"));

        if(getIntent().getExtras().containsKey("InComingType")){
            InComingType = getIntent().getExtras().getString("InComingType");
        }

        if(getIntent().getExtras().containsKey("isFirstScroll")){isFirstScroll = getIntent().getExtras().getBoolean("isFirstScroll");}

        if(getIntent().getExtras().containsKey("isBookDetailsComment")){
            isBookDetailsComment = getIntent().getExtras().getBoolean("isBookDetailsComment");
        }

        swipeRefreshLayout = findViewById(R.id.swipeRefresh);
        swipeRefreshLayout.setOnRefreshListener(this);

        //Circular Image
        userImage = findViewById(R.id.imageCommentUser);
        toolbar = findViewById(R.id.toolBarComment);
        editComment = findViewById(R.id.editComment);
        mRecyclerView = findViewById(R.id.recyclerViewComment);
        textPost = findViewById(R.id.textPost);
        commentProgress =findViewById(R.id.commentProgressCheck);
        setSupportActionBar(toolbar);
        if(getSupportActionBar() != null){
            getSupportActionBar().setDisplayShowTitleEnabled(false);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }
        editComment.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if(editComment.getText().toString().trim().length() > 0){
                    textPost.setTextColor(Color.parseColor("#333333"));
                }else{
                    textPost.setTextColor(Color.parseColor("#bbbbbb"));
                }

                float progress = s.length() * 0.5f;
                commentProgress.setProgress((int) progress);
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        //RecyclerView


        //editComment Listener
        editComment.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                //-->
            }
        });

        //Get Image From Auth
        LoadProfileImage();
        if(!isBookDetailsComment) {
            //LoadPost
            LoadPostFirst();
        }
        else{
            //Old Style Comment Section
            ShowAllComment();
        }
    }

    private void LoadProfileImage(){
        try {
            String UID = FirebaseUtils.firebaseAuth.getCurrentUser().getUid();
            String Root = getString(R.string.FIRESTORE_ROOT_USERDETAILS) + "/" + UID;
            FirebaseUtils.FirestoreGetData(Root, new OnSuccessListener<DocumentSnapshot>() {
                @Override
                public void onSuccess(DocumentSnapshot documentSnapshot) {
                    if (documentSnapshot.exists()) {
                        try {
                            String UserProfileLink =""; try { UserProfileLink = documentSnapshot.getString(getString(R.string.F_USERDETAILS_PROFILEURL)); } catch (Exception ex){ Log.e(TAG, ex.toString());}
                            ColorDrawable colorDrawableInfoPlaceholder = new ColorDrawable(ContextCompat.getColor(getApplicationContext(), R.color.colorDrawableInfoPlaceholder));
                            ColorDrawable colorDrawableErrorPlaceholder = new ColorDrawable(ContextCompat.getColor(getApplicationContext(), R.color.colorDrawableErrorPlaceholder));
                            Utils.SetImageViaUri(
                                    Activity_Comment.this,
                                    UserProfileLink,
                                    userImage,
                                    false,
                                    false,
                                    colorDrawableInfoPlaceholder,
                                    colorDrawableErrorPlaceholder,
                                    null
                            );
                        } catch (Exception ex) {
                            Log.e(TAG, ex.toString());
                        }
                    }
                }
            }, new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Log.e(TAG, e.toString());
                }
            });
        }
        catch (Exception ex){
            Log.e(TAG, ex.toString());
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    private void LoadPostFirst(){
        try{
            if(ID.equals("")){ return;}
            //Load Post Data
            String Root = getString(R.string.FIRESTORE_ROOT_POSTS) + "/" + ID;
            FirebaseUtils.FirestoreGetData(Root,
                    new OnSuccessListener<DocumentSnapshot>() {
                        @Override
                        public void onSuccess(DocumentSnapshot documentSnapshot) {
                            if(documentSnapshot.exists()){
                                post = new Post(
                                            documentSnapshot.getString(getString(R.string.F_POSTS_POST_UID)),
                                            new Post.PostBookReference(documentSnapshot.getString(getString(R.string.F_POSTS_POST_BOOK_REFRENCE))),
                                            documentSnapshot.get(getString(R.string.F_POSTS_POST_DATE)).toString(),
                                            documentSnapshot.getString(getString(R.string.F_POSTS_POST_DESCRIPTION)),
                                            documentSnapshot.getBoolean(getString(R.string.F_POSTS_POST_POSTEDITED))? " ( Edited )": "",
                                            documentSnapshot.getId(),
                                            documentSnapshot.getString(getString(R.string.F_POSTS_POST_PRIVACY)),
                                            Post.PostType.valueOf(documentSnapshot.getString(getString(R.string.F_POSTS_POST_TYPE)))
                                        );

                                //Show All Comments
                                ShowAllComment();
                            }
                            else{
                                Toast.makeText(Activity_Comment.this, "Oops! something wrong with this post.", Toast.LENGTH_SHORT).show();
                                finish();
                            }
                        }
                    }, new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Log.e(TAG, e.toString());
                        }
                    });
        }
        catch (Exception ex){
            Log.e(TAG, ex.toString());
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId() == android.R.id.home){
            if(InComingType != null && InComingType.equals("DIRECT")) {
                Intent intent = new Intent(this, Activity_Home.class);
                startActivity(intent);
            }
            finish();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        if(InComingType != null && InComingType.equals("DIRECT")) {
            Intent intent = new Intent(this, Activity_Home.class);
            startActivity(intent);
        }
        finish();
    }

    @Override
    protected void onStop() {
        super.onStop();
        try {
            if (commentFrom == CommentFrom.Book) {
                Activity_BookDetails.counterTextView.setText(Utils.Numberformat(mList.size() -1));
            } else if (commentFrom == CommentFrom.Post) {
                Fragment_Activity.counterTextView.setText(Utils.Numberformat(mList.size() -1));
            }
        }
        catch (Exception ex){
            Log.e(TAG, ex.getMessage());
        }
    }

    public void ShowAllComment(){
        if (commentFrom == CommentFrom.Post){
            adapter = new AdapterComment(this, mList, post);
            mRecyclerView.setLayoutManager(linearLayoutManager);
            mRecyclerView.setAdapter(adapter);
            ShowCommentFromPost();
        }
        else if(commentFrom == CommentFrom.Book){
            adapter2 = new AdapterComment_FBookDetails(this, mList);
            mRecyclerView.setLayoutManager(linearLayoutManager);
            mRecyclerView.setAdapter(adapter2);
            ShowCommentFromBook();
        }
    }

    public void AddComment_Click(View view){
        if (commentFrom == CommentFrom.Post){
            AddCommentFromPost();
        }
        else if(commentFrom == CommentFrom.Book){
            AddCommentFromBook();
        }
    }

    private void ShowCommentFromPost(){
        try{
            Utils.SetProgressDialogIndeterminate(this, "loading..");

            mList.clear();
            mList.add(null);
            adapter.notifyDataSetChanged();

            String Root = getString(R.string.FIRESTORE_ROOT_POSTS) + "/" + ID + "/" + getString(R.string.FIRESTORE_ROOT_POSTS_COMMENTS);
            CollectionReference collectionReference = FirebaseFirestore.getInstance().collection(Root);
            collectionReference.orderBy(getString(R.string.F_POSTS_COMMENTS_COMMENT_COMMENTDATE), Query.Direction.ASCENDING);

            listenerRegistration = FirebaseUtils.FirestoreGetAllData(
                    collectionReference,
                    this,
                    new EventListener<QuerySnapshot>() {
                        @Override
                        public void onEvent(QuerySnapshot queryDocumentSnapshots, FirebaseFirestoreException e) {
                            if(!queryDocumentSnapshots.isEmpty()) {
                                for(DocumentChange doc :  queryDocumentSnapshots.getDocumentChanges()) {
                                    if(doc.getType() == DocumentChange.Type.ADDED){
                                        String commentidstr = doc.getDocument().getId().toString();
                                        String uidstr = doc.getDocument().get(getString(R.string.F_POSTS_COMMENTS_COMMENT_UID)).toString();
                                        String commentstr = doc.getDocument().get(getString(R.string.F_POSTS_COMMENTS_COMMENT_DESCRIPTION)).toString();
                                        String datestr = doc.getDocument().get(getString(R.string.F_POSTS_COMMENTS_COMMENT_COMMENTDATE)).toString();

                                        Comment comment = new Comment(commentidstr,  uidstr, commentstr, datestr);

                                        mList.add(mList.size(), comment);
                                        adapter.notifyItemInserted(mList.size());
                                    }
                                }

                                try{if(isFirstScroll) {mRecyclerView.scrollToPosition(mList.size() - 1);}else{isFirstScroll = true; mRecyclerView.scrollToPosition(0);}} catch (Exception ex){Log.e(TAG, e.toString());}

                                Utils.UnSetProgressDialogIndeterminate();
                            }
                            else{
                                Log.i(TAG, "DATA NOT FOUND");
                                Utils.UnSetProgressDialogIndeterminate();
                            }
                            swipeRefreshLayout.setRefreshing(false);
                        }
                    });
        }
        catch (Exception ex){
            Utils.UnSetProgressDialogIndeterminate();
            swipeRefreshLayout.setRefreshing(false);
            Log.e("TAG", ex.toString());
        }
    }

    private void ShowCommentFromBook(){
        try{
            Utils.SetProgressDialogIndeterminate(this, "loading..");
            mList.clear();
            adapter2.notifyDataSetChanged();

            String Root = getString(R.string.FIRESTORE_ROOT_BOOKS) + "/" + ID + "/" + getString(R.string.FIRESTORE_ROOT_BOOKS_COMMENTS);
            CollectionReference collectionReference = FirebaseFirestore.getInstance().collection(Root);
            collectionReference.orderBy(getString(R.string.F_BOOKS_COMMENTS_COMMENT_COMMENTDATE), Query.Direction.ASCENDING);

            listenerRegistration = FirebaseUtils.FirestoreGetAllData(
                    collectionReference,
                    this,
                    new EventListener<QuerySnapshot>() {
                        @Override
                        public void onEvent(QuerySnapshot queryDocumentSnapshots, FirebaseFirestoreException e) {
                            if(!queryDocumentSnapshots.isEmpty()) {
                                for(DocumentChange doc :  queryDocumentSnapshots.getDocumentChanges()) {
                                    if(doc.getType() == DocumentChange.Type.ADDED){
                                        String commentidstr = doc.getDocument().getId();
                                        String uidstr = doc.getDocument().get(getString(R.string.F_BOOKS_COMMENTS_COMMENT_UID)).toString();
                                        String commentstr = doc.getDocument().get(getString(R.string.F_BOOKS_COMMENTS_COMMENT_DESCRIPTION)).toString();
                                        String datestr = doc.getDocument().get(getString(R.string.F_BOOKS_COMMENTS_COMMENT_COMMENTDATE)).toString();
                                        Comment comment = new Comment(commentidstr,  uidstr, commentstr, datestr);

                                        mList.add(mList.size(), comment);
                                        adapter2.notifyItemInserted(mList.size());
                                    }
                                }
                                mRecyclerView.scrollToPosition(mList.size() -1);
                                Utils.UnSetProgressDialogIndeterminate();
                            }
                            else{
                                Log.i(TAG, "DATA NOT FOUND");
                                Utils.UnSetProgressDialogIndeterminate();
                            }
                            swipeRefreshLayout.setRefreshing(false);
                        }
                    });
        }
        catch (Exception ex){
            Utils.UnSetProgressDialogIndeterminate();
            swipeRefreshLayout.setRefreshing(false);
            Log.e("TAG", ex.toString());
        }
    }

    private void AddCommentFromPost(){
        try{
            String Description = editComment.getText().toString().trim();

            if(Description.equals("")){
                return;
            }
            //region Add Comment
            String UIID = Utils.UUIDToken();
            String UID = FirebaseUtils.firebaseAuth.getCurrentUser().getUid();
            Timestamp commentTimeStamp = new Timestamp(Calendar.getInstance().getTime());
            String Root = getString(R.string.FIRESTORE_ROOT_POSTS) + "/" + ID + "/" + getString(R.string.FIRESTORE_ROOT_POSTS_COMMENTS) + "/" + UIID;

            Map<String, Object> map = new HashMap<>();
            map.put(getString(R.string.F_POSTS_COMMENTS_COMMENT_ID), UIID);
            map.put(getString(R.string.F_POSTS_COMMENTS_COMMENT_UID), UID);
            map.put(getString(R.string.F_POSTS_COMMENTS_COMMENT_COMMENTDATE), commentTimeStamp);
            map.put(getString(R.string.F_POSTS_COMMENTS_COMMENT_DESCRIPTION), Description);
            FirebaseUtils.FirestroreSetData(
                    Root,
                    map,
                    new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            Log.i(TAG, "Comment Posted");
                        }
                    },
                    new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Log.e("ERROR", e.toString());
                        }
                    });
            //endregion

            //region Call Server Function
            Map<String, Object> postMap = new HashMap<>();
            postMap.put("postid", ID);
            postMap.put("bookid", "");//Nothing
            postMap.put("uid", UID);
            postMap.put("description", Description);
            postMap.put("displayname", FirebaseUtils.firebaseAuth.getCurrentUser().getDisplayName());
            postMap.put("date", commentTimeStamp.toDate().toString());
            postMap.put("notificationtype", "postcomment");

            FirebaseUtils.RunServerFunction("SendNotification", postMap,
                    new Continuation<HttpsCallableResult, String>() {
                        @Override
                        public String then(@NonNull Task<HttpsCallableResult> task) {
                            return String.valueOf(task.getResult().getData());
                        }
                    }, new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Log.e("ERROR", e.toString());
                        }
                    });
            //endregion

            //Clear Field
            editComment.setText("");
        }
        catch (Exception ex){
            Log.e("ERROR", ex.toString());
        }
    }

    private void AddCommentFromBook() {
        try{
            String Description = editComment.getText().toString().trim();
            if(Description.equals("")){
                return;
            }

            //region Add Comment
            String UIID = Utils.UUIDToken();
            String UID = FirebaseUtils.firebaseAuth.getCurrentUser().getUid();
            Timestamp commentTimeStamp = new Timestamp(Calendar.getInstance().getTime());
            String Root = getString(R.string.FIRESTORE_ROOT_BOOKS) + "/" + ID + "/" + getString(R.string.FIRESTORE_ROOT_BOOKS_COMMENTS) + "/" + UIID;

            Map<String, Object> map = new HashMap<>();
            map.put(getString(R.string.F_BOOKS_COMMENTS_COMMENT_ID), UIID);
            map.put(getString(R.string.F_BOOKS_COMMENTS_COMMENT_UID), UID);
            map.put(getString(R.string.F_BOOKS_COMMENTS_COMMENT_COMMENTDATE), commentTimeStamp);
            map.put(getString(R.string.F_BOOKS_COMMENTS_COMMENT_DESCRIPTION), Description);
            FirebaseUtils.FirestroreSetData(
                    Root,
                    map,
                    new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {

                            String CounterRoot = getString(R.string.FIRESTORE_ROOT_BOOKS) + "/" + ID;
                            FirebaseUtils.BookCounter(CounterRoot, getString(R.string.F_BOOKS_COUNTERCOMMENT), 1);

                            Log.i(TAG, "Comment Posted");
                        }
                    },
                    new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Log.e("ERROR", e.toString());
                        }
                    });
            //endregion

            //region Call Server Function
            {
                Map<String, Object> postMap = new HashMap<>();
                postMap.put("uid", UID);
                postMap.put("bookid", ID);
                postMap.put("description", Description);
                postMap.put("displayname", FirebaseUtils.firebaseAuth.getCurrentUser().getDisplayName() + " commented on book ");
                postMap.put("notificationtype", "bookcomment");
                Timestamp postTimeStamp = new Timestamp(Calendar.getInstance().getTime());
                postMap.put("date", postTimeStamp.toDate().toString());

                FirebaseUtils.RunServerFunction("SendNotification", postMap,
                        new Continuation<HttpsCallableResult, String>() {
                            @Override
                            public String then(@NonNull Task<HttpsCallableResult> task) {
                                return String.valueOf(task.getResult().getData());
                            }
                        }, new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Log.e("ERROR", e.toString());
                            }
                        });
                //endregion
            }
            //endregion

            //Clear Field
            editComment.setText("");
        }
        catch (Exception ex){
            Log.e("ERROR", ex.toString());
        }
    }

    @Override
    public void onRefresh() {
        if(listenerRegistration !=null){
            listenerRegistration.remove();
            listenerRegistration = null;
        }
        ShowAllComment();
    }
}

class AdapterComment extends RecyclerView.Adapter<RecyclerView.ViewHolder>{
    private String TAG = "ACTIVITY COMMENT R View";
    private List<Comment> mList;
    private Context context;
    private Post post;

    AdapterComment(Context context, List<Comment> mList, Post post) {
        this.context = context;
        this.mList = mList;
        this.post = post;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        try {
            if (viewType == 0) {
                if (post.getPostType() == Post.PostType.Simple) {
                    View view = LayoutInflater.from(context.getApplicationContext()).inflate(R.layout.fragment_activity_recycler_simple_flat, parent, false);
                    return new Simple_ViewHolder(view);
                } else if (post.getPostType() == Post.PostType.BookReference) {
                    View view = LayoutInflater.from(context.getApplicationContext()).inflate(R.layout.fragment_activity_recycler_bookreference_flat, parent, false);
                    return new BookRefrence_ViewHolder(view);
                } else {
                    return null;
                }
            } else {
                View view = LayoutInflater.from(context.getApplicationContext()).inflate(R.layout.recycler_item_comment, parent, false);
                return new CommentHolder(view);
            }
        }
        catch (Exception ex){
            Log.e(TAG, ex.toString());
            return null;
        }
    }

    @Override
    public void onBindViewHolder(@NonNull final RecyclerView.ViewHolder holder, int position) {
        try {
            if(position == 0){
                if(post.getPostType() == Post.PostType.Simple){
                    //region Simple
                    final Simple_ViewHolder simple_viewHolder = (Simple_ViewHolder) holder;
                    simple_viewHolder.optionMenu.setVisibility(View.GONE);

                    //region Likes Counter
                    {
                        String Root = context.getString(R.string.FIRESTORE_ROOT_POSTS) + "/" + post.getPostId() + "/" + context.getString(R.string.FIRESTORE_ROOT_POSTS_LIKES);
                        FirebaseFirestore.getInstance().collection(Root).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                try {
                                    if (task.isSuccessful()) {
                                        int tCount = task.getResult().size();
                                        LikeCount(simple_viewHolder.PostLikeText, tCount);
                                    }
                                } catch (Exception ex) {
                                    Log.e(TAG, ex.toString());
                                }
                            }
                        });
                    }
                    //endregion

                    //region Comment Counter
                    {
                        String Root = context.getString(R.string.FIRESTORE_ROOT_POSTS) + "/" + post.getPostId() + "/" + context.getString(R.string.FIRESTORE_ROOT_POSTS_COMMENTS);
                        FirebaseFirestore.getInstance().collection(Root).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                try {
                                    if (task.isSuccessful()) {
                                        simple_viewHolder.PostCommentText.setText(Utils.Numberformat(task.getResult().getDocuments().size()));
                                    }
                                } catch (Exception ex) {
                                    Log.e(TAG, ex.toString());
                                }
                            }
                        });
                    }
                    //endregion

                    //region Date String
                    try {
                        String pDate;
                        pDate = new SimpleDateFormat("MMM dd, yyyy 'at' h:m a", Locale.US).format(new Date(post.getPostDate()));
                        pDate += post.getPostEdit();
                        simple_viewHolder.textPostDate.setText(pDate);
                    } catch (Exception ex) {
                        Log.e(TAG, ex.toString());
                    }
                    //endregion

                    //region Post Description
                    try {
                        simple_viewHolder.textPostDescription.setText(post.getPostDescription());
                    } catch (Exception ex) {
                        Log.e("ERROR: PD", ex.toString());
                    }
                    //endregion

                    //region Load Personal Details
                    PersonalDetails(simple_viewHolder.ProfileImage, simple_viewHolder.UserFullName, post.getUID());
                    //endregion

                    //region Like State
                    LikeState(post.getPostId(), post.getUID(), simple_viewHolder.imageLike);
                    //endregion

                    //region Like Button Click
                    simple_viewHolder.buttonLike.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            //for Reverting Image Back
                            LikePost(post.getPostId(), post.getUID(), simple_viewHolder.imageLike, simple_viewHolder.getAdapterPosition(), simple_viewHolder.PostLikeText, simple_viewHolder.buttonLike, post.getPostDate(), post.getPostDescription());
                        }
                    });
                    //endregion

                    //region Time Ago System
                    {
                        try {
                            //tempPost.get(position).postDate;
                            String timeAgo = TimeAgo.using(new Date(post.getPostDate()).getTime());
                            simple_viewHolder.textTimeAgo.setText(timeAgo);
                        } catch (Exception ex) {
                            Log.e(TAG, ex.toString());
                        }
                    }
                    //endregion
                    //endregion
                }
                else if(post.getPostType() == Post.PostType.BookReference){
                    //region Book Reference
                    final BookRefrence_ViewHolder bookRefrence_viewHolder = (BookRefrence_ViewHolder) holder;
                    bookRefrence_viewHolder.optionMenu.setVisibility(View.GONE);

                    //region Likes Counter
                    {
                        String Root = context.getString(R.string.FIRESTORE_ROOT_POSTS) + "/" + post.getPostId() + "/" + context.getString(R.string.FIRESTORE_ROOT_POSTS_LIKES);
                        FirebaseFirestore.getInstance().collection(Root).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                try {
                                    if (task.isSuccessful()) {
                                        int tCount = task.getResult().size();
                                        LikeCount(bookRefrence_viewHolder.PostLikeText, tCount);
                                    }
                                } catch (Exception ex) {
                                    Log.e(TAG, ex.toString());
                                }
                            }
                        });
                    }
                    //endregion

                    //region Comment Counter
                    {
                        String Root = context.getString(R.string.FIRESTORE_ROOT_POSTS) + "/" + post.getPostId() + "/" + context.getString(R.string.FIRESTORE_ROOT_POSTS_COMMENTS);
                        FirebaseFirestore.getInstance().collection(Root).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                try {
                                    if (task.isSuccessful()) {
                                        bookRefrence_viewHolder.PostCommentText.setText(Utils.Numberformat(task.getResult().getDocuments().size()));
                                    }
                                } catch (Exception ex) {
                                    Log.e(TAG, ex.toString());
                                }
                            }
                        });
                    }
                    //endregion

                    //region Date String
                    try {
                        String pDate;
                        pDate = new SimpleDateFormat("MMM dd, yyyy 'at' h:m a", Locale.US).format(new Date(post.getPostDate()));
                        pDate += post.getPostEdit();
                        bookRefrence_viewHolder.textPostDate.setText(pDate);
                    } catch (Exception ex) {
                        Log.e(TAG, ex.toString());
                    }
                    //endregion

                    //region Post Description
                    try {
                        bookRefrence_viewHolder.textPostDescription.setText(post.getPostDescription());
                    } catch (Exception ex) {
                        Log.e("ERROR: PD", ex.toString());
                    }
                    //endregion

                    //region Load Personal Details
                    PersonalDetails(bookRefrence_viewHolder.ProfileImage, bookRefrence_viewHolder.UserFullName, post.getUID());
                    //endregion

                    //region Like State
                    LikeState(post.getPostId(), post.getUID(), bookRefrence_viewHolder.imageLike);
                    //endregion

                    //region Like Button Click
                    bookRefrence_viewHolder.buttonLike.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            //for Reverting Image Back
                            LikePost(post.getPostId(), post.getUID(), bookRefrence_viewHolder.imageLike, bookRefrence_viewHolder.getAdapterPosition(), bookRefrence_viewHolder.PostLikeText, bookRefrence_viewHolder.buttonLike, post.getPostDate(), post.getPostDescription());
                        }
                    });
                    //endregion

                    //region Time Ago System
                    {
                        try {
                            //tempPost.get(position).postDate;
                            String timeAgo = TimeAgo.using(new Date(post.getPostDate()).getTime());
                            bookRefrence_viewHolder.textTimeAgo.setText(timeAgo);
                        } catch (Exception ex) {
                            Log.e(TAG, ex.toString());
                        }
                    }
                    //endregion

                    //region Book Details
                    {
                        String Root = context.getString(R.string.FIRESTORE_ROOT_BOOKS) + "/" + post.getPostbookReference().getBookId();
                        FirebaseUtils.FirestoreGetData(Root, new OnSuccessListener<DocumentSnapshot>() {
                            @Override
                            public void onSuccess(DocumentSnapshot documentSnapshot) {
                                if (documentSnapshot.exists()) {
                                    //region Set Image Data
                                    String imageURL = documentSnapshot.getString(context.getString(R.string.F_BOOKS_IMAGEURL));
                                    ColorDrawable colorDrawableInfoPlaceholder = new ColorDrawable(ContextCompat.getColor(context, R.color.colorDrawableInfoPlaceholder));
                                    ColorDrawable colorDrawableErrorPlaceholder = new ColorDrawable(ContextCompat.getColor(context, R.color.colorDrawableErrorPlaceholder));
                                    Utils.SetImageViaUri((Activity) context, imageURL, bookRefrence_viewHolder.bookImage, false, false, colorDrawableInfoPlaceholder, colorDrawableErrorPlaceholder, null);
                                    //endregion
                                    //region SetText
                                    bookRefrence_viewHolder.bookTitleBook.setText(documentSnapshot.getString(context.getString(R.string.F_BOOKS_TITLE)));
                                    bookRefrence_viewHolder.bookDescriptionBook.setText(documentSnapshot.getString(context.getString(R.string.F_BOOKS_DESCRIPTION)));
                                    //endregion
                                }
                            }
                        }, null);
                    }
                    //endregion

                    //region Book View
                    bookRefrence_viewHolder.holderBookReference.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Utils.GoToBookDetails(context, post.getPostbookReference().getBookId());
                        }
                    });
                    //endregion
                    //endregion
                }
                else{
                    Log.e(TAG, "No data found");
                }
            }
            else{
                final CommentHolder commentHolder = (CommentHolder) holder;
                //region Comment Data
                String Root = context.getString(R.string.FIRESTORE_ROOT_USERDETAILS) + "/" + mList.get(position).getUid();
                FirebaseUtils.FirestoreGetData(Root,
                        new OnSuccessListener<DocumentSnapshot>() {
                            @Override
                            public void onSuccess(DocumentSnapshot documentSnapshot) {
                                if (documentSnapshot.exists()) {
                                    try {
                                        String UserFullName, SourceString;
                                        UserFullName = documentSnapshot.get(context.getString(R.string.F_USERDETAILS_USERFULLNAME)).toString();
                                        String ProfileUrl =""; try{ ProfileUrl = documentSnapshot.get(context.getString(R.string.F_USERDETAILS_PROFILEURL)).toString();} catch (Exception ex){Log.e(TAG, ex.toString());}
                                        SourceString = "<font color='#000000'><b>" + UserFullName + "</b></font> " + mList.get(holder.getAdapterPosition()).getComment();
                                        commentHolder.userComment.setText(Html.fromHtml(SourceString));

                                        ColorDrawable colorDrawableInfoPlaceholder = new ColorDrawable(ContextCompat.getColor(context, R.color.colorDrawableInfoPlaceholder));
                                        ColorDrawable colorDrawableErrorPlaceholder = new ColorDrawable(ContextCompat.getColor(context, R.color.colorDrawableErrorPlaceholder));
                                        Utils.SetImageViaUri((Activity) context, ProfileUrl, commentHolder.imageUser, false,false, colorDrawableInfoPlaceholder, colorDrawableErrorPlaceholder,null);

                                        final String UID = mList.get(commentHolder.getAdapterPosition()).getUid();

                                        commentHolder.userComment.setOnClickListener(new View.OnClickListener() {
                                            @Override
                                            public void onClick(View v) {
                                                Utils.OpenProfile((Activity)context, UID);
                                            }
                                        });

                                        commentHolder.imageUser.setOnClickListener(new View.OnClickListener() {
                                            @Override
                                            public void onClick(View v) {
                                                Utils.OpenProfile((Activity)context, UID);
                                            }
                                        });

                                    } catch (Exception ex) {
                                        Log.e("ERROR", ex.toString());
                                    }
                                }
                            }
                        }, new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Log.e(TAG, e.toString());
                            }
                        });

                //Add Time
                String timeAgo = TimeAgo.using(new Date(mList.get(position).getDate()).getTime());
                commentHolder.commentTime.setText(timeAgo);
                //endregion
            }
        }
        catch (Exception ex){
            Log.e(TAG, ex.toString());
        }
    }

    private void LikePost(final String PostID, final String UID, final ImageView imageView, final int position , final TextView textView, final View MainBox, final String postDate, final String postDescription){
        try {
            MainBox.setEnabled(false);
            imageView.setImageResource(R.drawable.icon_loading);

            final Animation animation = AnimationUtils.loadAnimation(context, R.anim.progressanimation);
            imageView.startAnimation(animation);

            final String Root = context.getString(R.string.FIRESTORE_ROOT_POSTS) + "/" + PostID + "/" + context.getString(R.string.FIRESTORE_ROOT_POSTS_LIKES) + "/" + UID;
            final DocumentReference documentReference = FirebaseFirestore.getInstance().document(Root);
            documentReference.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                @Override
                public void onSuccess(DocumentSnapshot documentSnapshot) {
                    if (documentSnapshot.exists()) {
                        //Remove Likes
                        documentReference.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                //region LIKE COUNT
                                String Root = context.getString(R.string.FIRESTORE_ROOT_POSTS) + "/" + PostID + "/" + context.getString(R.string.FIRESTORE_ROOT_POSTS_LIKES);
                                FirebaseFirestore.getInstance().collection(Root).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                    @Override
                                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                        try {
                                            if (task.isSuccessful()) {
                                                int tCount = task.getResult().size();
                                                imageView.setImageResource(R.drawable.icon_new_like_dim);
                                                LikeCount(textView, tCount);
                                                imageView.clearAnimation();
                                            }
                                        } catch (Exception ex) {
                                            Log.e(TAG, ex.toString());
                                        }
                                    }
                                });
                                //endregion
                                MainBox.setEnabled(true);
                            }
                        })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        imageView.clearAnimation();
                                        MainBox.setEnabled(true);
                                    }
                                });
                    }
                    else {
                        //Add Likes
                        Timestamp likeTimeStamp = new Timestamp(Calendar.getInstance().getTime());
                        Map<String, Object> map = new HashMap<>();
                        map.put(context.getString(R.string.F_POSTS_COMMENTS_LIKES_UID), UID);
                        map.put(context.getString(R.string.F_POSTS_COMMENTS_LIKES_DATE), likeTimeStamp);

                        FirebaseUtils.FirestroreSetData(Root, map,
                                new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {

                                        //region Server Function Calling
                                        String uid = FirebaseUtils.firebaseAuth.getCurrentUser().getUid();
                                        try{
                                            String shortTitle = postDescription;
                                            if(shortTitle.length() > 30){
                                                shortTitle = postDescription.substring(0,30);
                                            }

                                            Map<String, Object> postMap = new HashMap<>();
                                            postMap.put("postid", PostID);
                                            postMap.put("uid", uid);
                                            postMap.put("description", postDescription);
                                            postMap.put("displayname", FirebaseUtils.firebaseAuth.getCurrentUser().getDisplayName());
                                            postMap.put("date", postDate);
                                            postMap.put("notificationtype", "postlike");
                                            postMap.put("shortTitle", shortTitle);

                                            FirebaseUtils.RunServerFunction("SendNotification", postMap,
                                                    new Continuation<HttpsCallableResult, String>() {
                                                        @Override
                                                        public String then(@NonNull Task<HttpsCallableResult> task) {
                                                            return String.valueOf(task.getResult().getData());
                                                        }
                                                    }, new OnFailureListener() {
                                                        @Override
                                                        public void onFailure(@NonNull Exception e) {
                                                            Log.e("ERROR", e.toString());
                                                        }
                                                    });
                                        }
                                        catch (Exception ex){
                                            Log.e(TAG, ex.toString());
                                        }
                                        //endregion

                                        //region LIKE COUNT
                                        String Root = context.getString(R.string.FIRESTORE_ROOT_POSTS) + "/" + PostID + "/" + context.getString(R.string.FIRESTORE_ROOT_POSTS_LIKES);
                                        FirebaseFirestore.getInstance().collection(Root).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                            @Override
                                            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                                try {
                                                    if (task.isSuccessful()) {
                                                        int tCount = task.getResult().size();
                                                        imageView.setImageResource(R.drawable.icon_new_like);
                                                        LikeCount(textView, tCount);
                                                        imageView.clearAnimation();
                                                    }
                                                } catch (Exception ex) {
                                                    Log.e(TAG, ex.toString());
                                                }
                                            }
                                        });
                                        //endregion

                                        MainBox.setEnabled(true);
                                    }
                                },
                                new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        imageView.clearAnimation();
                                        MainBox.setEnabled(true);
                                    }
                                });
                    }
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Log.e(TAG, e.toString());
                    imageView.clearAnimation();
                }
            });
        }
        catch (Exception ex){
            MainBox.setEnabled(true);
            Log.e(TAG, ex.toString());
        }
    }

    private void LikeState(String PostID,String UID, final ImageView imageView){
        try {
            final String Root = context.getString(R.string.FIRESTORE_ROOT_POSTS) + "/" + PostID + "/" + context.getString(R.string.FIRESTORE_ROOT_POSTS_LIKES) + "/" + UID;
            final DocumentReference documentReference = FirebaseFirestore.getInstance().document(Root);
            documentReference.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                @Override
                public void onSuccess(DocumentSnapshot documentSnapshot) {
                    if (documentSnapshot.exists()) {
                        imageView.setImageResource(R.drawable.icon_new_like);
                    }
                    else {
                        imageView.setImageResource(R.drawable.icon_new_like_dim);
                    }
                }
            });
        }
        catch (Exception ex){
            Log.e(TAG, ex.toString());
        }
    }

    private void LikeCount(TextView textView, int totalCount){
        String tCount = Utils.Numberformat(totalCount);
        textView.setText(tCount);
    }

    private void PersonalDetails(final ImageView profileImage, final TextView fullName,final String UID){
        //Search For UID in database
        String Root = context.getString(R.string.FIRESTORE_ROOT_USERDETAILS) + "/" + UID;
        FirebaseUtils.FirestoreGetData(Root,
                new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        if (documentSnapshot.exists()) {
                            try {
                                String UserProfileLink = "";
                                try {UserProfileLink = documentSnapshot.getString(context.getString(R.string.F_USERDETAILS_PROFILEURL));} catch (Exception ex) {Log.e(TAG, ex.toString());}
                                ColorDrawable colorDrawableInfoPlaceholder = new ColorDrawable(ContextCompat.getColor(context, R.color.colorDrawableInfoPlaceholder));
                                ColorDrawable colorDrawableErrorPlaceholder = new ColorDrawable(ContextCompat.getColor(context, R.color.colorDrawableErrorPlaceholder));
                                Utils.SetImageViaUri((Activity) context, UID, UserProfileLink, profileImage, false, false, colorDrawableInfoPlaceholder, colorDrawableErrorPlaceholder, null);

                                profileImage.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        Utils.OpenProfile((Activity)context, UID);
                                    }
                                });

                                fullName.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        Utils.OpenProfile((Activity)context, UID);
                                    }
                                });
                            }
                            catch (Exception ex) {Log.e(TAG, ex.toString());}
                            String UserFullName = "";  try {  UserFullName = documentSnapshot.getString(context.getString(R.string.F_USERDETAILS_USERFULLNAME));} catch (Exception ex) {Log.e(TAG, ex.toString());}
                            try{fullName.setText(UserFullName);}catch (Exception ex){Log.e(TAG, ex.toString());}
                        }
                    }
                }, new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e(TAG, e.toString());
                    }
                });
    }

    @Override
    public int getItemCount() {
        return mList.size();
    }

    @Override
    public int getItemViewType(int position) {
        return position;
    }

    class CommentHolder extends RecyclerView.ViewHolder{
        CircleImageView imageUser;
        TextView userComment, commentTime;
        public CommentHolder(View v){
            super(v);
            imageUser = v.findViewById(R.id.imageCommentRecyclerUser);
            userComment = v.findViewById(R.id.textCommentComment);
            commentTime = v.findViewById(R.id.textCommentTime);
        }
    }

    class Simple_ViewHolder extends RecyclerView.ViewHolder{
        CircleImageView ProfileImage;
        TextView textPostDate, textPostDescription, UserFullName, PostLikeText, PostCommentText, textTimeAgo;
        ImageView imageLike;
        View buttonLike, buttonComment, optionMenu;
        Simple_ViewHolder(View itemView) {
            super(itemView);
            ProfileImage = itemView.findViewById(R.id.ProfileImage);
            textPostDate = itemView.findViewById(R.id.textPostDate);
            textPostDescription = itemView.findViewById(R.id.textPostDescription);
            UserFullName = itemView.findViewById(R.id.UserFullName);
            imageLike = itemView.findViewById(R.id.imageLike);
            buttonLike = itemView.findViewById(R.id.layoutLike);
            PostLikeText = itemView.findViewById(R.id.PostLikeText);
            buttonComment = itemView.findViewById(R.id.layoutComment);
            PostCommentText = itemView.findViewById(R.id.PostCommentText);
            textTimeAgo = itemView.findViewById(R.id.textTimeAgo);
            optionMenu = itemView.findViewById(R.id.optionMenu);
        }
    }

    class BookRefrence_ViewHolder extends RecyclerView.ViewHolder{
        Typeface robotoLight = Typeface.createFromAsset(itemView.getContext().getAssets(), "fonts/roboto_light.ttf");

        CircleImageView ProfileImage;
        TextView textPostDate, textPostDescription, UserFullName, PostLikeText, PostCommentText, textTimeAgo, bookTitleBook, bookDescriptionBook;
        ImageView imageLike, bookImage;
        View buttonLike, buttonComment, optionMenu, holderBookReference;
        BookRefrence_ViewHolder(View itemView) {
            super(itemView);
            ProfileImage = itemView.findViewById(R.id.ProfileImage);
            textPostDate = itemView.findViewById(R.id.textPostDate);
            textPostDescription = itemView.findViewById(R.id.textPostDescription);
            UserFullName = itemView.findViewById(R.id.UserFullName);
            imageLike = itemView.findViewById(R.id.imageLike);
            buttonLike = itemView.findViewById(R.id.layoutLike);
            PostLikeText = itemView.findViewById(R.id.PostLikeText);
            PostCommentText = itemView.findViewById(R.id.PostCommentText);
            buttonComment = itemView.findViewById(R.id.layoutComment);
            textTimeAgo = itemView.findViewById(R.id.textTimeAgo);
            bookImage = itemView.findViewById(R.id.bookImage);
            bookTitleBook = itemView.findViewById(R.id.bookTitleBook);

            bookDescriptionBook = itemView.findViewById(R.id.bookDescriptionBook);
            bookDescriptionBook.setTypeface(robotoLight);

            optionMenu = itemView.findViewById(R.id.optionMenu);
            holderBookReference = itemView.findViewById(R.id.holderBookReference);
        }
    }
}

class AdapterComment_FBookDetails extends RecyclerView.Adapter<AdapterComment_FBookDetails.holder>{

    private static final String TAG = "AdapterComment_FBookDet";
    private List<Comment> mList;
    private Context context;

    AdapterComment_FBookDetails(Context context, List<Comment> mList) {
        this.context = context;
        this.mList = mList;
    }

    @NonNull
    @Override
    public holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context.getApplicationContext()).inflate(R.layout.recycler_item_comment,parent,false);
        return new holder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final holder holder, final int position) {
        try {
            //Load Data
            String Root = context.getString(R.string.FIRESTORE_ROOT_USERDETAILS) + "/" + mList.get(position).getUid();
            FirebaseUtils.FirestoreGetData(Root, new OnSuccessListener<DocumentSnapshot>() {
                @Override
                public void onSuccess(DocumentSnapshot documentSnapshot) {
                    if (documentSnapshot.exists()) {
                        try {
                            String UserFullName, SourceString;
                            UserFullName = documentSnapshot.get(context.getString(R.string.F_USERDETAILS_USERFULLNAME)).toString();
                            String ProfileUrl =""; try{ ProfileUrl = documentSnapshot.get(context.getString(R.string.F_USERDETAILS_PROFILEURL)).toString();} catch (Exception ex){Log.e(TAG, ex.toString());}
                            SourceString = "<font color='#000000'><b>" + UserFullName + "</b></font> " + mList.get(holder.getAdapterPosition()).getComment();
                            holder.userComment.setText(Html.fromHtml(SourceString));

                            ColorDrawable colorDrawableInfoPlaceholder = new ColorDrawable(ContextCompat.getColor(context, R.color.colorDrawableInfoPlaceholder));
                            ColorDrawable colorDrawableErrorPlaceholder = new ColorDrawable(ContextCompat.getColor(context, R.color.colorDrawableErrorPlaceholder));
                            Utils.SetImageViaUri((Activity) context, ProfileUrl, holder.imageUser, false, false,colorDrawableInfoPlaceholder, colorDrawableErrorPlaceholder,null);





                        } catch (Exception ex) {
                            Log.e("ERROR", ex.toString());
                        }
                    }
                }
            }, new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Log.e(TAG, e.toString());
                }
            });

            //Add Time
            String timeAgo = TimeAgo.using(new Date(mList.get(position).getDate()).getTime());
            holder.commentTime.setText(timeAgo);

            final String UID = mList.get(position).getUid();
            holder.userComment.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    Log.e(TAG, "--------------->");
                    //Utils.OpenProfile((Activity)context, UID);
                }
            });

        }
        catch (Exception ex){
            Log.e(TAG, ex.toString());
        }
    }

    @Override
    public int getItemCount() {
        return mList.size();
    }

    class holder extends RecyclerView.ViewHolder{
        CircleImageView imageUser;
        TextView userComment, commentTime;
        View commentView;
        public holder(View v){
            super(v);
            imageUser = v.findViewById(R.id.imageCommentRecyclerUser);
            userComment = v.findViewById(R.id.textCommentComment);
            commentTime = v.findViewById(R.id.textCommentTime);
            commentView = v.findViewById(R.id.commentView);
        }
    }
}
