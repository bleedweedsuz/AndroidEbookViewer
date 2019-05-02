package zkagazapahtnajusz.paperproject.com.paperproject.Fragments;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
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
import zkagazapahtnajusz.paperproject.com.paperproject.Activity_Comment;
import zkagazapahtnajusz.paperproject.com.paperproject.Activity_UserPost;
import zkagazapahtnajusz.paperproject.com.paperproject.Activity_UserProfile_Setting;
import zkagazapahtnajusz.paperproject.com.paperproject.Model.Post;
import zkagazapahtnajusz.paperproject.com.paperproject.Model.PostBuffer;
import zkagazapahtnajusz.paperproject.com.paperproject.R;
import zkagazapahtnajusz.paperproject.com.paperproject.Utilities.FirebaseUtils;
import zkagazapahtnajusz.paperproject.com.paperproject.Utilities.NetworkManagerUtils;
import zkagazapahtnajusz.paperproject.com.paperproject.Utilities.Utils;

/**
 * Created by Zw4R-TSTUD10 2018/4/1 [2018, April 1]
 */

public class Fragment_Activity extends Fragment implements SwipeRefreshLayout.OnRefreshListener{

    String TAG = "FRAGMENT ACTIVITY";
    SwipeRefreshLayout swipeRefreshLayout;
    int queryLimit;
    public List<PostBuffer> postBuffers = new ArrayList<>();
    RecyclerView myRecyclerView;
    public ActivityAdapter activityAdapter;
    FirebaseFirestore firebaseFirestore;
    //Scroller
    DocumentSnapshot lastVisible;
    private Boolean isFirstPageFirstLoad = false;
    LinearLayoutManager layoutManager;
    //public static boolean isReLoadData = false;
    private boolean loading = true;
    int pastVisiblesItems, visibleItemCount, totalItemCount;
    View NoDataFound;
    ProgressBar progressBar;
    public static TextView counterTextView = null;
    public static boolean isReLoadData = false;
    ListenerRegistration listenerRegistration_firstPage, listenerRegistration_loadMore;

    @Nullable
    @Override
    public View onCreateView(@Nullable LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        Utils.DEVICE_CHECK(getActivity());
        queryLimit = Utils.CURRENT_GRID_COUNT;

        View view = inflater.inflate(R.layout.fragment_activity,container,false);
        myRecyclerView = view.findViewById(R.id.recyclerFragmentActivity);
        NoDataFound = view.findViewById(R.id.NoDataFoundView);
        progressBar = view.findViewById(R.id.progressBar);

        activityAdapter = new ActivityAdapter(this.getActivity(), postBuffers, myRecyclerView, ActivityAdapter.SkinFor.FragmentActivity);
        layoutManager = new LinearLayoutManager(getActivity());
        myRecyclerView.setLayoutManager(layoutManager);
        myRecyclerView.setAdapter(activityAdapter);

        swipeRefreshLayout = view.findViewById(R.id.swipeRefresh);
        swipeRefreshLayout.setOnRefreshListener(this);

        myRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
            }

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                if(dy > 0) //check for scroll down
                {
                    visibleItemCount = layoutManager.getChildCount();
                    totalItemCount = layoutManager.getItemCount();
                    pastVisiblesItems = layoutManager.findFirstVisibleItemPosition();

                    if (loading && ((visibleItemCount + pastVisiblesItems) >= totalItemCount))
                    {
                        loading = false;
                        LoadMore();
                    }
                }
            }
        });
        LoadPortalAndPages();
        return view;
    }

    public void LoadPortalAndPages(){
        try {
            firebaseFirestore = FirebaseFirestore.getInstance();
            AddNewPostPortal();
            FirstPage();
        }
        catch (Exception ex){
            Log.e(TAG, ex.toString());
        }
    }

    private void AddNewPostPortal(){
        postBuffers.add(new PostBuffer(Post.PostType.NewPost));
        activityAdapter.notifyDataSetChanged();
    }

    private void FirstPage(){
        try{
            //region no internet connection show
            NetworkManagerUtils.LoadServerData(getContext());
            //endregion
            String UID = FirebaseUtils.firebaseAuth.getCurrentUser().getUid();
            String Root = getString(R.string.FIRESTORE_ROOT_USERDETAILS) + "/" + UID + "/" + getString(R.string.FIRESTORE_ROOT_POSTS);
            Query firstQuery = firebaseFirestore.collection(Root).orderBy(getString(R.string.F_POSTS_POST_DATE), Query.Direction.DESCENDING).limit(queryLimit);
            listenerRegistration_firstPage = firstQuery.addSnapshotListener(getActivity(), new EventListener<QuerySnapshot>() {
                @Override
                public void onEvent(QuerySnapshot queryDocumentSnapshots, FirebaseFirestoreException e) {
                    if(!queryDocumentSnapshots.isEmpty()){
                        if(!isFirstPageFirstLoad){
                            lastVisible = queryDocumentSnapshots.getDocuments().get(queryDocumentSnapshots.size() -1);
                        }
                        for(DocumentChange doc: queryDocumentSnapshots.getDocumentChanges()){
                            switch (doc.getType()) {
                                case ADDED:
                                    {
                                        NoDataFound.setVisibility(View.GONE);
                                        PostBuffer postBuffer = new PostBuffer(doc.getDocument().getString(getString(R.string.F_POSTS_POSTID)), Post.PostType.valueOf(doc.getDocument().getString(getString(R.string.F_POSTS_POST_TYPE))));
                                        if (!isFirstPageFirstLoad) {
                                            //Add To List from beginning
                                            postBuffers.add(postBuffer);
                                            activityAdapter.notifyItemInserted(postBuffers.size() - 1);
                                        } else {
                                            //Add To Second index i.e. => 1
                                            postBuffers.add(1, postBuffer);
                                            activityAdapter.notifyItemInserted(1);
                                        }
                                    }
                                    break;
                                case MODIFIED:
                                    {
                                        Log.e(TAG, "FRAGMENT ACTIVITY MODIFIED...");
                                    }
                                    break;
                                case REMOVED:
                                    {
                                        String postID = doc.getDocument().getString(getString(R.string.F_POSTS_POSTID));
                                        activityAdapter.removeItemFromPostID(postID);
                                    }
                                    break;
                            }
                        }
                        isFirstPageFirstLoad = true;
                    }
                    else{
                        if(postBuffers.size() >= 2){
                            postBuffers.clear();
                            AddNewPostPortal();
                        }
                        isFirstPageFirstLoad = false;
                        NoDataFound.setVisibility(View.VISIBLE);
                    }
                    swipeRefreshLayout.setRefreshing(false);
                }
            });
        }
        catch (Exception ex){
            swipeRefreshLayout.setRefreshing(false);
            Log.e(TAG, ex.toString());
        }
    }

    private void LoadMore(){
        try{
            progressBar.setVisibility(View.VISIBLE);
            String UID = FirebaseUtils.firebaseAuth.getCurrentUser().getUid();
            String Root = getString(R.string.FIRESTORE_ROOT_USERDETAILS) + "/" + UID + "/" + getString(R.string.FIRESTORE_ROOT_POSTS);
            Query firstQuery = firebaseFirestore.collection(Root).orderBy(getString(R.string.F_POSTS_POST_DATE), Query.Direction.DESCENDING).startAfter(lastVisible).limit(queryLimit);
            listenerRegistration_loadMore = firstQuery.addSnapshotListener(getActivity(), new EventListener<QuerySnapshot>() {
                @Override
                public void onEvent(QuerySnapshot queryDocumentSnapshots, FirebaseFirestoreException e) {
                    if(!queryDocumentSnapshots.isEmpty()) {
                        lastVisible = queryDocumentSnapshots.getDocuments().get(queryDocumentSnapshots.size() -1);
                        for (DocumentChange doc : queryDocumentSnapshots.getDocumentChanges()) {
                            if(doc.getType() == DocumentChange.Type.ADDED) {
                                try {
                                    PostBuffer postBuffer = new PostBuffer(doc.getDocument().getString(getString(R.string.F_POSTS_POSTID)), Post.PostType.valueOf(doc.getDocument().getString(getString(R.string.F_POSTS_POST_TYPE))));
                                    postBuffers.add(postBuffer);
                                    activityAdapter.notifyItemInserted(postBuffers.size() - 1);
                                }
                                catch (Exception ex){
                                    Log.e(TAG, ex.toString());
                                }
                            }
                        }
                    }
                    else{
                        //Do Data Found
                        Log.e(TAG,"NO DATA FOUND -  MODE 1");
                    }
                    listenerRegistration_loadMore.remove();
                    listenerRegistration_loadMore = null;
                    progressBar.setVisibility(View.INVISIBLE);
                    loading = true;
                }
            });
        }
        catch (Exception ex){
            progressBar.setVisibility(View.INVISIBLE);
            Log.e(TAG, ex.toString());
        }
    }

    @Override
    public void onRefresh() {
        if(listenerRegistration_firstPage !=null){listenerRegistration_firstPage.remove(); listenerRegistration_firstPage=null;}
        if(listenerRegistration_loadMore !=null){listenerRegistration_loadMore.remove(); listenerRegistration_loadMore = null;}
        loading = true;
        isFirstPageFirstLoad = false;
        postBuffers.clear();
        LoadPortalAndPages();
    }

    @Override
    public void onResume() {
        super.onResume();
        if(Activity_UserProfile_Setting.isProfileImageChange_Activity){
            Activity_UserProfile_Setting.isProfileImageChange_Activity = false;
            onRefresh();
        }
        if(isReLoadData){
            isReLoadData =false;
            onRefresh();
        }
    }
}

class ActivityAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>{
    private static final String TAG = "ActivityAdapter";
    public enum SkinFor {FragmentActivity, UserProfilePost}
    private Context context;
    private List<PostBuffer> postBuffers;
    private RecyclerView recyclerView;
    private SkinFor skinFor;

    ActivityAdapter(Context context, List<PostBuffer> postBuffers, RecyclerView recyclerView, SkinFor skinFor) {
        this.context = context;
        this.postBuffers = postBuffers;
        this.recyclerView = recyclerView;
        this.skinFor = skinFor;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if(viewType == 0){
            //Log.e(TAG, "NEW POST");
            View view  = LayoutInflater.from(context).inflate(R.layout.fragment_activity_recycler_newpost, parent,false);
            return new NewPost_ViewHolder(view);
        }
        else if(viewType == 1){
            //Log.e(TAG, "SIMPLE POST");
            View view  = LayoutInflater.from(context).inflate(R.layout.fragment_activity_recycler_simple, parent,false);
            return new Simple_ViewHolder(view);
        }
        else if(viewType == 2){
            //Log.e(TAG, "BOOK REFERENCE");
            View view  = LayoutInflater.from(context).inflate(R.layout.fragment_activity_recycler_bookreference, parent,false);
            return new BookRefrence_ViewHolder(view);
        }
        return null;
    }

    @Override
    public void onBindViewHolder(@NonNull final RecyclerView.ViewHolder holder, int position) {
        if(holder.getItemViewType() == 0){
            //region New Post BindView
            final NewPost_ViewHolder newPost_viewHolder = (NewPost_ViewHolder) holder;
            String UID = FirebaseUtils.firebaseAuth.getCurrentUser().getUid();
            String Root = context.getString(R.string.FIRESTORE_ROOT_USERDETAILS) + "/" + UID;
            FirebaseUtils.FirestoreGetData(Root, new OnSuccessListener<DocumentSnapshot>() {
                @Override
                public void onSuccess(DocumentSnapshot documentSnapshot) {
                    if (documentSnapshot.exists()) {
                        try {
                            String UserProfileLink = ""; try{ UserProfileLink = documentSnapshot.getString(context.getString(R.string.F_USERDETAILS_PROFILEURL));} catch (Exception ex){Log.e(TAG, ex.toString());}
                            ColorDrawable colorDrawableInfoPlaceholder = new ColorDrawable(ContextCompat.getColor(context, R.color.colorDrawableInfoPlaceholder));
                            ColorDrawable colorDrawableErrorPlaceholder = new ColorDrawable(ContextCompat.getColor(context, R.color.colorDrawableErrorPlaceholder));
                            Utils.SetImageViaUri((Activity) context, UserProfileLink, newPost_viewHolder.ProfileImage, false,false, colorDrawableInfoPlaceholder, colorDrawableErrorPlaceholder,null);
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
            //endregion
        }
        else if(holder.getItemViewType() == 1){
            //region Simple Post
            final String CurrentUID = FirebaseUtils.firebaseAuth.getCurrentUser().getUid();
            final Simple_ViewHolder simple_viewHolder = (Simple_ViewHolder) holder;
            //region Find post from server
            final String POSTID = postBuffers.get(position).getPOSTID();
            String Root = context.getString(R.string.FIRESTORE_ROOT_POSTS) + "/" + POSTID;
            FirebaseUtils.FirestoreGetData(Root, new OnSuccessListener<DocumentSnapshot>() {
                @Override
                public void onSuccess(DocumentSnapshot documentSnapshot) {
                    if (documentSnapshot != null && documentSnapshot.exists()) {
                        try {
                            //Post Found Full Details
                            final String UID = documentSnapshot.getString(context.getString(R.string.F_POSTS_POST_UID));
                            final String postDate = documentSnapshot.get(context.getString(R.string.F_POSTS_POST_DATE)).toString();
                            final String postDescription = documentSnapshot.getString(context.getString(R.string.F_POSTS_POST_DESCRIPTION));
                            String privacy = documentSnapshot.getString(context.getString(R.string.F_POSTS_POST_PRIVACY));
                            String edited = documentSnapshot.getBoolean(context.getString(R.string.F_POSTS_POST_POSTEDITED))? " ( Edited )": "";

                            //POST
                            final Post post = new Post(UID, null, postDate, postDescription, edited, POSTID, privacy, Post.PostType.Simple);

                            //Full System Logic
                            //region OptionMenu
                            if(CurrentUID.equals(UID)) {
                                String[] editContent = {POSTID, "", privacy, postDescription}; //postid, bookid, privacy, text
                                OptionMenu(simple_viewHolder.optionMenu, POSTID, simple_viewHolder.getAdapterPosition(), editContent);
                            }
                            else{
                                simple_viewHolder.optionMenu.setVisibility(View.GONE);
                            }
                            //endregion

                            //region Likes Counter
                            {
                                String Root = context.getString(R.string.FIRESTORE_ROOT_POSTS) + "/" + POSTID + "/" + context.getString(R.string.FIRESTORE_ROOT_POSTS_LIKES);
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
                                String Root = context.getString(R.string.FIRESTORE_ROOT_POSTS) + "/" + POSTID + "/" + context.getString(R.string.FIRESTORE_ROOT_POSTS_COMMENTS);
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
                                pDate = new SimpleDateFormat("MMM dd, yyyy 'at' h:m a", Locale.US).format(new Date(postDate));
                                pDate += edited;
                                simple_viewHolder.textPostDate.setText(pDate);
                            } catch (Exception ex) {
                                Log.e(TAG, ex.toString());
                            }
                            //endregion

                            //region Post Description
                            try {
                                simple_viewHolder.textPostDescription.setText(postDescription);
                            } catch (Exception ex) {
                                Log.e("ERROR: PD", ex.toString());
                            }
                            //endregion

                            //region Load Personal Details
                            PersonalDetails(simple_viewHolder.ProfileImage, simple_viewHolder.UserFullName, UID);
                            //endregion

                            //region Like State
                            LikeState(POSTID, UID, simple_viewHolder.imageLike);
                            //endregion

                            //region Like Button Click
                            simple_viewHolder.buttonLike.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    //for Reverting Image Back
                                    LikePost(POSTID, UID, simple_viewHolder.imageLike, simple_viewHolder.getAdapterPosition(), simple_viewHolder.PostLikeText, simple_viewHolder.buttonLike, postDate, postDescription);
                                }
                            });
                            //endregion

                            //region Comment Button Click
                            simple_viewHolder.buttonComment.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    Fragment_Activity.counterTextView = simple_viewHolder.PostCommentText;
                                    ShowComment(post);
                                }
                            });
                            //endregion

                            //region Time Ago System
                            {
                                try {
                                    //tempPost.get(position).postDate;
                                    String timeAgo = TimeAgo.using(new Date(postDate).getTime());
                                    simple_viewHolder.textTimeAgo.setText(timeAgo);
                                } catch (Exception ex) {
                                    Log.e(TAG, ex.toString());
                                }
                            }
                            //endregion
                        }
                        catch (Exception ex){
                            Log.e("ERROR SIMPLE: ", ex.toString());
                        }
                    }
                }
            }, null);
            //endregion
            //endregion
        }
        else if(holder.getItemViewType() == 2){
            //region BookRefrence
                try {
                    final String CurrentUID = FirebaseUtils.firebaseAuth.getCurrentUser().getUid();
                    final BookRefrence_ViewHolder bookRefrence_viewHolder = (BookRefrence_ViewHolder) holder;
                    final String POSTID = postBuffers.get(position).getPOSTID();
                    String Root = context.getString(R.string.FIRESTORE_ROOT_POSTS) + "/" + POSTID;
                    FirebaseUtils.FirestoreGetData(Root, new OnSuccessListener<DocumentSnapshot>() {
                        @Override
                        public void onSuccess(DocumentSnapshot documentSnapshot) {
                            if (documentSnapshot != null && documentSnapshot.exists()) {
                                try {
                                    //Post Found Full Details
                                    final String UID = documentSnapshot.getString(context.getString(R.string.F_POSTS_POST_UID));
                                    final String postDate = documentSnapshot.get(context.getString(R.string.F_POSTS_POST_DATE)).toString();
                                    final String postDescription = documentSnapshot.getString(context.getString(R.string.F_POSTS_POST_DESCRIPTION));
                                    final String BookID = documentSnapshot.getString(context.getString(R.string.F_POSTS_POST_BOOK_REFRENCE));
                                    String privacy = documentSnapshot.getString(context.getString(R.string.F_POSTS_POST_PRIVACY));
                                    String edited = documentSnapshot.getBoolean(context.getString(R.string.F_POSTS_POST_POSTEDITED)) ? " ( Edited )" : "";
                                    //Full System Logic


                                    //POST
                                    final Post post = new Post(UID, new Post.PostBookReference(BookID), postDate, postDescription, edited, POSTID, privacy, Post.PostType.BookReference);

                                    //region OptionMenu
                                    if (CurrentUID.equals(UID)) {
                                        String[] editContent = {POSTID, BookID, privacy, postDescription}; //postid, bookid, privacy, text
                                        OptionMenu(bookRefrence_viewHolder.optionMenu, POSTID, bookRefrence_viewHolder.getAdapterPosition(), editContent);
                                    } else {
                                        bookRefrence_viewHolder.optionMenu.setVisibility(View.GONE);
                                    }
                                    //endregion

                                    //region Likes Counter
                                    {
                                        String Root = context.getString(R.string.FIRESTORE_ROOT_POSTS) + "/" + POSTID + "/" + context.getString(R.string.FIRESTORE_ROOT_POSTS_LIKES);
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
                                        String Root = context.getString(R.string.FIRESTORE_ROOT_POSTS) + "/" + POSTID + "/" + context.getString(R.string.FIRESTORE_ROOT_POSTS_COMMENTS);
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
                                        pDate = new SimpleDateFormat("MMM dd, yyyy 'at' h:m a").format(new Date(postDate));
                                        pDate += edited;
                                        bookRefrence_viewHolder.textPostDate.setText(pDate);
                                    } catch (Exception ex) {
                                        Log.e(TAG, ex.toString());
                                    }
                                    //endregion

                                    //region Post Description
                                    try {
                                        bookRefrence_viewHolder.textPostDescription.setText(postDescription);
                                    } catch (Exception ex) {
                                        Log.e("ERROR: PD", ex.toString());
                                    }
                                    //endregion

                                    //region Load Personal Details
                                    PersonalDetails(bookRefrence_viewHolder.ProfileImage, bookRefrence_viewHolder.UserFullName, UID);
                                    //endregion

                                    //region Like State
                                    LikeState(POSTID, UID, bookRefrence_viewHolder.imageLike);
                                    //endregion

                                    //region Like Button Click
                                    bookRefrence_viewHolder.buttonLike.setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View v) {
                                            //for Reverting Image Back
                                            LikePost(POSTID, UID, bookRefrence_viewHolder.imageLike, bookRefrence_viewHolder.getAdapterPosition(), bookRefrence_viewHolder.PostLikeText, bookRefrence_viewHolder.buttonLike, postDate, postDescription);
                                        }
                                    });
                                    //endregion

                                    //region Comment Button Click
                                    bookRefrence_viewHolder.buttonComment.setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View v) {
                                            Fragment_Activity.counterTextView = bookRefrence_viewHolder.PostCommentText;
                                            ShowComment(post);
                                        }
                                    });
                                    //endregion

                                    //region Time Ago System
                                    {
                                        try {
                                            //tempPost.get(position).postDate;
                                            String timeAgo = TimeAgo.using(new Date(postDate).getTime());
                                            bookRefrence_viewHolder.textTimeAgo.setText(timeAgo);
                                        } catch (Exception ex) {
                                            Log.e(TAG, ex.toString());
                                        }
                                    }
                                    //endregion

                                    //region Book Details
                                    {
                                        String Root = context.getString(R.string.FIRESTORE_ROOT_BOOKS) + "/" + BookID;
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
                                            Utils.GoToBookDetails(context, BookID);
                                        }
                                    });
                                    //endregion
                                } catch (Exception ex) {
                                    Log.e("ERROR", ex.toString());
                                }
                            }
                            }
                        }, null);
                }
                catch (Exception ex){
                    Log.e(TAG, ex.toString());
                }
            //endregion
        }
    }

    private void OptionMenu(final View view, final String postID, final int position, final String[] editContent){
        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final PopupMenu popupMenu = new PopupMenu(context, view);
                popupMenu.inflate(R.menu.activity_mypost);
                popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        if(item.getItemId() == R.id.edit){
                            //send to post to edit
                            if(!NetworkManagerUtils.LoadServerData(context)){
                                return true;
                            }
                            Intent i = new Intent(context, Activity_UserPost.class);
                            i.putExtra("isEdit", true);
                            i.putExtra("editContents", editContent);
                            i.putExtra("SkinFor", skinFor.toString());
                            context.startActivity(i);
                            return true;
                        }
                        else if(item.getItemId() == R.id.delete){
                            if(!NetworkManagerUtils.LoadServerData(context)){
                                return true;
                            }
                            //delete this system
                            new AlertDialog.Builder(context).
                                    setTitle("Info").
                                    setMessage("Are you sure want to delete this post?").
                                    setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            if(!NetworkManagerUtils.LoadServerData(context)){
                                                return;
                                            }
                                            Utils.SetProgressDialogIndeterminate(context, "deleting post");
                                            String UID = FirebaseUtils.firebaseAuth.getCurrentUser().getUid();
                                            postBuffers.remove(position);
                                            notifyItemRemoved(position);

                                            //region Other Function
                                            {
                                                String Root = context.getString(R.string.FIRESTORE_ROOT_POSTS) + "/" + postID;
                                                FirebaseUtils.FirestoreDeleteData(
                                                        Root,
                                                        new OnSuccessListener<Void>() {
                                                            @Override
                                                            public void onSuccess(Void aVoid) {
                                                                Utils.UnSetProgressDialogIndeterminate();
                                                            }
                                                        },
                                                        new OnFailureListener() {
                                                            @Override
                                                            public void onFailure(@NonNull Exception e) {
                                                                Utils.UnSetProgressDialogIndeterminate();
                                                                Toast.makeText(context, e.getMessage(), Toast.LENGTH_SHORT).show();
                                                            }
                                                        }
                                                );
                                            }

                                            {
                                                String Root = context.getString(R.string.FIRESTORE_ROOT_USERDETAILS) + "/" + UID + "/" + context.getString(R.string.FIRESTORE_ROOT_POSTS) + "/" + postID;
                                                FirebaseUtils.FirestoreDeleteData(
                                                        Root,
                                                        new OnSuccessListener<Void>() {
                                                            @Override
                                                            public void onSuccess(Void aVoid) {
                                                                Utils.UnSetProgressDialogIndeterminate();
                                                            }
                                                        },
                                                        new OnFailureListener() {
                                                            @Override
                                                            public void onFailure(@NonNull Exception e) {
                                                                Utils.UnSetProgressDialogIndeterminate();
                                                                Toast.makeText(context, e.getMessage(), Toast.LENGTH_SHORT).show();
                                                            }
                                                        }
                                                );
                                            }

                                            {
                                                Map<String, Object> postMap = new HashMap<>();
                                                postMap.put("POSTID", postID);
                                                postMap.put("USERID", UID);
                                                FirebaseUtils.RunServerFunction("DeletePostFromSystem", postMap,
                                                        new Continuation<HttpsCallableResult, String>() {
                                                            @Override
                                                            public String then(@NonNull Task<HttpsCallableResult> task) throws Exception {
                                                                return String.valueOf(task.getResult().getData());
                                                            }
                                                        }, new OnFailureListener() {
                                                            @Override
                                                            public void onFailure(@NonNull Exception e) {
                                                                Log.e("ERROR", e.toString());
                                                            }
                                                        });
                                            }
                                        }
                                    }).
                                    setNegativeButton("No", null).
                                    create().show();
                            return true;
                        }
                        return false;
                    }
                });
                popupMenu.show();
            }
        });
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

    private void PersonalDetails(final ImageView profileImage, final TextView fullName,final String UID){
        //Search For UID in database
        String Root = context.getString(R.string.FIRESTORE_ROOT_USERDETAILS) + "/" + UID;
        FirebaseUtils.FirestoreGetData(Root, new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                if (documentSnapshot.exists()) {
                    try {
                        String UserProfileLink = "";try {UserProfileLink = documentSnapshot.getString(context.getString(R.string.F_USERDETAILS_PROFILEURL));} catch (Exception ex) {Log.e(TAG, ex.toString());}
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

                    }catch (Exception ex) {Log.e(TAG, ex.toString());}
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

    private void ShowComment(Post post){
        try{
            Intent i = new Intent(context, Activity_Comment.class);
            i.putExtra("ID", post.getPostId());
            i.putExtra("commentFrom", Activity_Comment.CommentFrom.Post.toString());
            context.startActivity(i);
        }
        catch (Exception ex){
            Log.e("TAG", ex.toString());
        }
    }

    @Override
    public int getItemCount() {
        return postBuffers.size();
    }

    @Override
    public int getItemViewType(int position) {
        if(postBuffers.get(position).getPOSTTYPE() == Post.PostType.NewPost){
            return 0;
        }
        else if(postBuffers.get(position).getPOSTTYPE() == Post.PostType.Simple){
            return 1;
        }
        else if(postBuffers.get(position).getPOSTTYPE() == Post.PostType.BookReference){
            return 2;
        }
        return -1;
    }

    void removeItemFromPostID(String postID){
        for(int i=1; i<postBuffers.size(); i++){if(postBuffers.get(i).getPOSTID().equals(postID)){postBuffers.remove(i);notifyItemRemoved(i);break;}}
    }

    class NewPost_ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
        CircleImageView ProfileImage;
        View NewPostContainer;
        NewPost_ViewHolder(View itemView) {
            super(itemView);
            ProfileImage = itemView.findViewById(R.id.ProfileImage);
            NewPostContainer = itemView.findViewById(R.id.NewPostContainer);
            NewPostContainer.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            if(v.getId() == R.id.NewPostContainer){
                if(!NetworkManagerUtils.LoadServerData(context)){
                    return;
                }
                Intent i = new Intent(context, Activity_UserPost.class);
                context.startActivity(i);
            }
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
            optionMenu = itemView.findViewById(R.id.optionMenu);
            holderBookReference = itemView.findViewById(R.id.holderBookReference);
        }
    }
}
