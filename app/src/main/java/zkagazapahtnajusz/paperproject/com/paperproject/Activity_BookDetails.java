package zkagazapahtnajusz.paperproject.com.paperproject;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.design.widget.AppBarLayout;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.NestedScrollView;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.ScaleAnimation;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.functions.HttpsCallableResult;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.OnProgressListener;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import me.gujun.android.taggroup.TagGroup;
import zkagazapahtnajusz.paperproject.com.paperproject.Fragments.Fragment_Library;
import zkagazapahtnajusz.paperproject.com.paperproject.Model.BookItems;
import zkagazapahtnajusz.paperproject.com.paperproject.Model.ProgressDialogInterface;
import zkagazapahtnajusz.paperproject.com.paperproject.Utilities.FirebaseUtils;
import zkagazapahtnajusz.paperproject.com.paperproject.Utilities.Utils;

/**
 * Created by Zw4R-TSTUD10 2018/4/1 [2018, April 1]
 */

public class Activity_BookDetails extends AppCompatActivity implements AppBarLayout.OnOffsetChangedListener{
    private static final String TAG = "Activity_BookDetails";
    private static final float PERCENTAGE_TO_SHOW_TITLE_AT_TOOLBAR = 0.8f;
    private static final float PERCENTAGE_TO_HIDE_TITLE_DETAILS = 0.15f;
    private static final int ALPHA_ANIMATIONS_DURATION = 300;

    public enum LibraryState { ADDED, DELETE}
    int SimilarLimit = 3;

    TextView textBookName, textAuthor, textViews, textDownload, textLikes, textComments, textDescription,toolBarTitle, textPrice, textPurchaseFree, textInsideBookFollow;
    ImageView imageBackground, imageMain, imageIcon, imageIconFollow, iconLike;
    RecyclerView recyclerView;
    SimilarStoriesAdapter mAdapter;
    List<BookItems> tempStories;
    View relativeHolder;
    AppBarLayout appBar;
    Toolbar toolbar;
    TagGroup tagGroup;
    NestedScrollView nestedScrollView;
    BookItems.BookFlag bookFlag;
    private boolean mIsTheTitleVisible = false;
    private boolean mIsTheTitleContainerVisible = true;
    String BOOKID, price, BookName, Author, tName, pKey;
    View likeView, commentView, followView;
    View NoDataFound;
    boolean sync = false;

    String InComingType = null;

    static TextView counterTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bookdetails);

        textBookName = findViewById(R.id.textFragmentLibraryTitle);
        textAuthor = findViewById(R.id.textFragmentLibraryAuthorName);
        textViews = findViewById(R.id.textFragmentLibraryViews);
        textDownload = findViewById(R.id.textFragmentLibraryDownload);
        textLikes = findViewById(R.id.textFragmentLibraryLikes);
        textComments = findViewById(R.id.textComments);
        textDescription = findViewById(R.id.textFragmentLibraryDescription);
        textPrice = findViewById(R.id.textFragmentLibraryPrice);
        imageMain = findViewById(R.id.imageFragmentLibraryImage);
        textPurchaseFree = findViewById(R.id.textPurchaseFree);
        imageBackground = findViewById(R.id.imageFragmentLibraryInsidePdfBackground);
        toolBarTitle = findViewById(R.id.textIoolBarInsideFragmentLibraryTitle);
        relativeHolder = findViewById(R.id.linearCheck);
        appBar = findViewById(R.id.appbarInsideFragmentLibrary);
        toolbar = findViewById(R.id.toolbarFragmentInsideLibrary);
        recyclerView = findViewById(R.id.recyclerViewInsideFragmentHorizontal);
        tagGroup = findViewById(R.id.tag_group);
        imageIcon = findViewById(R.id.imageIcon);
        nestedScrollView = findViewById(R.id.nestedScrollView);
        imageIconFollow = findViewById(R.id.imageIconFollow);
        iconLike = findViewById(R.id.iconLike);
        likeView = findViewById(R.id.buttonLike);
        commentView = findViewById(R.id.buttonComment);
        followView = findViewById(R.id.buttonInsideBookFollow);
        textInsideBookFollow = findViewById(R.id.textInsideBookFollow);
        NoDataFound = findViewById(R.id.NoDataFoundView);
        setSupportActionBar(toolbar);
        if(getSupportActionBar() != null){
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }
        toolBarTitle.setVisibility(View.INVISIBLE);
        appBar.addOnOffsetChangedListener(this);

        Utils.SetProgressDialogIndeterminate(this, "loading..");

        Bundle extras = getIntent().getExtras();
        if(extras != null) {
            InComingType = extras.getString("InComingType");
            BOOKID = extras.getString("BookID");

            if(extras.containsKey("sync")){sync = extras.getBoolean("sync");}

            SearchBookInServer();
        }
    }

    //region Search Book In Server
    private void SearchBookInServer(){
        try{
            String Root = getString(R.string.FIRESTORE_ROOT_BOOKS) + "/" + BOOKID;
            FirebaseUtils.FirestoreGetData(Root,
                    new OnSuccessListener<DocumentSnapshot>() {
                        @Override
                        public void onSuccess(DocumentSnapshot documentSnapshot) {
                            if(documentSnapshot.exists()){
                                pKey= documentSnapshot.getString(getString(R.string.F_BOOKS_PUBLICKEY));
                                BookName = documentSnapshot.getString(getString(R.string.F_BOOKS_TITLE));
                                Author = documentSnapshot.getString(getString(R.string.F_BOOKS_AUTHOR));
                                String description = documentSnapshot.getString(getString(R.string.F_BOOKS_DESCRIPTION));

                                String c = documentSnapshot.getString(getString(R.string.F_BOOKS_COUNTRY));
                                String l = documentSnapshot.getString(getString(R.string.F_BOOKS_LANGUAGE));
                                String p = documentSnapshot.getString(getString(R.string.F_BOOKS_PUBLISHER));
                                String pd = ""; try{ pd= Utils.ConvertDBDateToStr(documentSnapshot.get(getString(R.string.F_BOOKS_PUBLICATIONDATE)).toString());} catch (Exception ex){ Log.e(TAG, ex.toString());}
                                String pb = documentSnapshot.getString(getString(R.string.F_BOOKS_PRECEDEDBY));
                                String fb = documentSnapshot.getString(getString(R.string.F_BOOKS_FOLLOWEDBY));

                                String DescriptionBuilder = description;
                                if(!c.equals("")){DescriptionBuilder += "<br><br><B> Country: </B>" + c + "<br><br>";}
                                if(!l.equals("")){DescriptionBuilder += "<B> Language: </B>" + l + "<br><br>";}
                                if(!p.equals("")){DescriptionBuilder += "<B> Publisher: </B>" + p + "<br><br>";}
                                if(!pd.equals("")){DescriptionBuilder += "<B> Publication Date: </B>" + pd + "<br><br>";}
                                if(!pb.equals("")){DescriptionBuilder += "<B> Preceded By: </B>" + pb + "<br><br>";}
                                if(!fb.equals("")){DescriptionBuilder += "<B> Followed By: </B>" + fb;}

                                //DescriptionBuilder += description;

                                price = documentSnapshot.getString(getString(R.string.F_BOOKS_CURRENCYSTR)) + " " + documentSnapshot.get(getString(R.string.F_BOOKS_PRICE));

                                String imageUrl = ""; try{imageUrl = documentSnapshot.getString(getString(R.string.F_BOOKS_IMAGEURL));} catch (Exception ex){ Log.e(TAG, ex.toString());}
                                String imageUrl2 = ""; try{imageUrl2 = documentSnapshot.getString(getString(R.string.F_BOOKS_IMAGEURLBACK));} catch (Exception ex){ Log.e(TAG, ex.toString());}
                                ColorDrawable colorDrawableInfoPlaceholder = new ColorDrawable(ContextCompat.getColor(Activity_BookDetails.this, R.color.colorDrawableInfoPlaceholder));
                                ColorDrawable colorDrawableErrorPlaceholder = new ColorDrawable(ContextCompat.getColor(Activity_BookDetails.this, R.color.colorDrawableErrorPlaceholder));

                                Utils.SetImageViaUri(Activity_BookDetails.this, imageUrl, imageMain,false,false, colorDrawableInfoPlaceholder, colorDrawableErrorPlaceholder,null);
                                Utils.SetImageViaUri(Activity_BookDetails.this, imageUrl2, imageBackground,true,false, colorDrawableInfoPlaceholder, colorDrawableErrorPlaceholder,null);

                                bookFlag = BookItems.BookFlag.valueOf(documentSnapshot.getString(getString(R.string.F_BOOKS_BOOKFLAG)));

                                ArrayList GenreAList = (ArrayList) documentSnapshot.get(getString(R.string.F_BOOKS_GENRE));
                                textBookName.setText(BookName);
                                textAuthor.setText(Author);
                                textDescription.setText(Html.fromHtml(DescriptionBuilder));
                                toolBarTitle.setText(BookName);
                                toolBarTitle.setSelected(true);
                                String[] Genre = new String[GenreAList.size()];
                                for(int i=0;i<GenreAList.size();i++){Genre[i] = GenreAList.get(i).toString();}
                                tagGroup.setTags(Genre);
                                tName = BookName;

                                LoadMainFunctions();
                            }
                            else{
                                Utils.UnSetProgressDialogIndeterminate();
                                Toast.makeText(Activity_BookDetails.this, "Oops!! book does not exists in server", Toast.LENGTH_SHORT).show();
                                Activity_BookDetails.this.finish();
                            }
                        }
                    },
                    new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Utils.UnSetProgressDialogIndeterminate();
                            Toast.makeText(Activity_BookDetails.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                            Activity_BookDetails.this.finish();
                        }
                });

        }
        catch (Exception ex){
            Utils.UnSetProgressDialogIndeterminate();
            Log.e(TAG, ex.toString());
        }
    }
    //endregion

    //region Load Main Functions
    private void LoadMainFunctions(){
        //Adding Temp Values
        tempStories = new ArrayList<>();
        mAdapter = new SimilarStoriesAdapter(this,tempStories);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this,LinearLayoutManager.HORIZONTAL,false);
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.setAdapter(mAdapter);

        //Check Book In Library
        if(sync){
            bookFlag = BookItems.BookFlag.Redownload;
            UpdateButtonView();
            Utils.UnSetProgressDialogIndeterminate();
        }
        else {
            CheckBookInLibrary();
        }
        //Views & Downloads
        AddViewNumber();
        GetViewDownloadNumber();

        //Likes
        GetLikeData();

        //Comments
        GetComments();

        //Follow
        GetFollows();

        //Similar Stories
        SimilarStories();
    }
    //endregion

    //region Check Book In Library
    private void CheckBookInLibrary(){
        try{
            String UID = FirebaseUtils.firebaseAuth.getCurrentUser().getUid();
            String Root = getString(R.string.FIRESTORE_ROOT_USERDETAILS) + "/" + UID + "/" + getString(R.string.FIRESTORE_ROOT_LIBRARY) + "/" + BOOKID;
            FirebaseUtils.FirestoreGetData(Root,
                    new OnSuccessListener<DocumentSnapshot>() {
                        @Override
                        public void onSuccess(DocumentSnapshot documentSnapshot) {
                            if(documentSnapshot.exists()){
                                String state = documentSnapshot.getString(getString(R.string.F_USERDETAILS_LIBRARY_STATE));
                                LibraryState libraryState = LibraryState.valueOf(state);
                                if(libraryState == LibraryState.ADDED){
                                    //Read
                                    bookFlag = BookItems.BookFlag.Purchased;
                                }
                                else{
                                    //Add To library
                                    bookFlag = BookItems.BookFlag.Redownload;
                                }
                            }
                            UpdateButtonView();
                            Utils.UnSetProgressDialogIndeterminate();
                        }
                    }, new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            //Error
                            UpdateButtonView();
                            Utils.UnSetProgressDialogIndeterminate();
                            Log.e(TAG, e.toString());
                        }
                    });
        }
        catch (Exception ex){
            UpdateButtonView();
            Utils.UnSetProgressDialogIndeterminate();
            Log.e(TAG, ex.toString());
        }
    }
    //endregion

    //region Views & Download
    private void AddViewNumber(){
        try{
            String UID = FirebaseUtils.firebaseAuth.getCurrentUser().getUid();
            String Root = getString(R.string.FIRESTORE_ROOT_BOOKS) + "/" + BOOKID + "/" + getString(R.string.FIRESTORE_ROOT_BOOKS_VIEWS) + "/" + UID;
            Map<String, Object> map = new HashMap<>();
            map.put(getString(R.string.F_VIEWS_UID), UID);
            FirebaseUtils.FirestroreSetData(Root, map, new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void aVoid) {
                    String CounterRoot = getString(R.string.FIRESTORE_ROOT_BOOKS) + "/" + BOOKID;
                    FirebaseUtils.BookCounter(CounterRoot, getString(R.string.F_BOOKS_COUNTERVIEW), 1);
                    Log.i(TAG, "New Views User Added");
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

    private void GetViewDownloadNumber(){
        try{
            String Root = getString(R.string.FIRESTORE_ROOT_BOOKS) + "/" + BOOKID;
            FirebaseUtils.FirestoreGetData(Root, new OnSuccessListener<DocumentSnapshot>() {
                @Override
                public void onSuccess(DocumentSnapshot documentSnapshot) {
                    if(documentSnapshot.exists()){
                        String TotalViews = String.valueOf(documentSnapshot.get(getString(R.string.F_BOOKS_COUNTERVIEW)));
                        TotalViews = Utils.Numberformat(Long.valueOf(TotalViews));
                        textViews.setText(TotalViews);

                        String TotalDownload = String.valueOf(documentSnapshot.get(getString(R.string.F_BOOKS_COUNTERDOWNLOAD)));
                        TotalDownload = Utils.Numberformat(Long.valueOf(TotalDownload));
                        textDownload.setText(TotalDownload);
                    }
                }
            }, null);
        }
        catch (Exception ex){
            Log.e(TAG, ex.toString());
        }
    }
    //endregion

    //region Likes
    private void ToggleLike(){
        try{
            likeView.setEnabled(false);
            //Search Like data
            final String UID = FirebaseUtils.firebaseAuth.getCurrentUser().getUid();
            final String Root = getString(R.string.FIRESTORE_ROOT_BOOKS) + "/" + BOOKID + "/" + getString(R.string.FIRESTORE_ROOT_BOOKS_LIKES) + "/" + UID;
            FirebaseUtils.FirestoreGetData(Root, new OnSuccessListener<DocumentSnapshot>() {
                @Override
                public void onSuccess(DocumentSnapshot documentSnapshot) {
                    if (documentSnapshot.exists()) {//Found
                        //region Delete From System
                        {
                            FirebaseUtils.FirestoreDeleteData(Root, new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    //Delete from system
                                    likeView.setEnabled(true);
                                    iconLike.setImageResource(R.drawable.icon_new_like_dim);
                                    String CounterRoot = getString(R.string.FIRESTORE_ROOT_BOOKS) + "/" + BOOKID;
                                    FirebaseUtils.BookCounter(CounterRoot, getString(R.string.F_BOOKS_COUNTERLIKE), -1);
                                }
                            }, new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    likeView.setEnabled(true);
                                    Log.e(TAG, e.toString());
                                }
                            });
                        }
                        //endregion
                    } else {
                        //region Add In System
                        {
                            Timestamp postTimeStamp = new Timestamp(Calendar.getInstance().getTime());
                            //region Call Server Function
                            Map<String, Object> postMap = new HashMap<>();
                            postMap.put("uid", UID);
                            postMap.put("bookid", BOOKID);
                            postMap.put("description", tName);
                            postMap.put("displayname", FirebaseUtils.firebaseAuth.getCurrentUser().getDisplayName());
                            postMap.put("notificationtype", "booklike");
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


                        {
                            Map<String, Object> map = new HashMap<>();
                            map.put(getString(R.string.F_BOOKS_LIKES_UID), UID);
                            FirebaseUtils.FirestroreSetData(Root, map, new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    likeView.setEnabled(true);
                                    iconLike.setImageResource(R.drawable.icon_new_like);

                                    String CounterRoot = getString(R.string.FIRESTORE_ROOT_BOOKS) + "/" + BOOKID;
                                    FirebaseUtils.BookCounter(CounterRoot, Activity_BookDetails.this.getString(R.string.F_BOOKS_COUNTERLIKE), 1);
                                    Log.i(TAG, "NEW LIKE ADDED IN SYSTEM");
                                }
                            }, new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    likeView.setEnabled(true);
                                    Log.e(TAG, e.toString());
                                }
                            });
                        }

                        //endregion
                    }
                }
            }, new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    likeView.setEnabled(true);
                }
            });
        }
        catch (Exception ex){
            Log.e(TAG, ex.toString());
        }
    }

    private void GetLikeData(){
        try{
            String Root = getString(R.string.FIRESTORE_ROOT_BOOKS) + "/" + BOOKID + "/" + getString(R.string.FIRESTORE_ROOT_BOOKS_LIKES);
            CollectionReference collectionReference = FirebaseFirestore.getInstance().collection(Root);
            FirebaseUtils.FirestoreGetAllData(collectionReference, this, new EventListener<QuerySnapshot>() {
                @Override
                public void onEvent(QuerySnapshot queryDocumentSnapshots, FirebaseFirestoreException e) {
                    int tLikesValue = 0;
                    if(!queryDocumentSnapshots.isEmpty()){
                        tLikesValue = queryDocumentSnapshots.size();
                    }
                    textLikes.setText(Utils.Numberformat(tLikesValue));
                }
            });
        }
        catch (Exception ex){
            Log.e(TAG, ex.toString());
        }

        try{
            String UID = FirebaseUtils.firebaseAuth.getCurrentUser().getUid();
            String Root = getString(R.string.FIRESTORE_ROOT_BOOKS) + "/" + BOOKID + "/" + getString(R.string.FIRESTORE_ROOT_BOOKS_LIKES) + "/" + UID;
            FirebaseUtils.FirestoreGetData(Root, new OnSuccessListener<DocumentSnapshot>() {
                @Override
                public void onSuccess(DocumentSnapshot documentSnapshot) {
                    if(documentSnapshot.exists()){
                        iconLike.setImageResource(R.drawable.icon_new_like);
                    }
                    else{
                        iconLike.setImageResource(R.drawable.icon_new_like_dim);
                    }
                }
            }, null);
        }
        catch (Exception ex){
            Log.e(TAG, ex.toString());
        }

    }
    //endregion

    //region Comments
    private void ShowCommentPage(){
        try{
            Intent i = new Intent(this, Activity_Comment.class);
            i.putExtra("ID", BOOKID);
            i.putExtra("commentFrom", Activity_Comment.CommentFrom.Book.toString());
            i.putExtra("isBookDetailsComment", true);
            startActivity(i);
        }
        catch (Exception ex){
            Log.e("TAG", ex.toString());
        }
    }

    private void GetComments(){
        try{
            String Root = getString(R.string.FIRESTORE_ROOT_BOOKS) + "/" + BOOKID + "/" + getString(R.string.FIRESTORE_ROOT_BOOKS_COMMENTS);
            CollectionReference collectionReference = FirebaseFirestore.getInstance().collection(Root);
            FirebaseUtils.FirestoreGetAllData(collectionReference, this, new EventListener<QuerySnapshot>() {
                @Override
                public void onEvent(QuerySnapshot queryDocumentSnapshots, FirebaseFirestoreException e) {
                    if(!queryDocumentSnapshots.isEmpty()){
                        String TotalViews = String.valueOf(queryDocumentSnapshots.getDocumentChanges().size());
                        TotalViews = Utils.Numberformat(Long.valueOf(TotalViews));
                        textComments.setText(TotalViews);
                    }
                }
            });
        }
        catch (Exception ex){
            Log.e(TAG, ex.toString());
        }
    }
    //endregion Comments

    //region Follow Book
    private void ToggleFollow(){
        try{
            followView.setEnabled(false);
            //Create Topic
            final String Topic_A = BOOKID + "_booklike";
            final String Topic_B = BOOKID + "_bookcomment";
            //Search Like data
            final String UID = FirebaseUtils.firebaseAuth.getCurrentUser().getUid();
            final String Root = getString(R.string.FIRESTORE_ROOT_BOOKS) + "/" + BOOKID + "/" + getString(R.string.FIRESTORE_ROOT_BOOKS_FOLLOWER) + "/" + UID;
            FirebaseUtils.FirestoreGetData(Root, new OnSuccessListener<DocumentSnapshot>() {
                @Override
                public void onSuccess(DocumentSnapshot documentSnapshot) {
                    if (documentSnapshot.exists()) {//Found
                        //UnSubscribe
                        FirebaseMessaging.getInstance().unsubscribeFromTopic(Topic_A);
                        FirebaseMessaging.getInstance().unsubscribeFromTopic(Topic_B);

                        //region Delete From System
                        FirebaseUtils.FirestoreDeleteData(Root, new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                followView.setEnabled(true);
                                imageIconFollow.setImageResource(R.drawable.icon_follow);
                                textInsideBookFollow.setText("Follow");
                                //Delete from system
                                Log.i(TAG, "DELETE FROM SYSTEM -- FOLLOW");
                            }
                        }, new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                followView.setEnabled(true);
                                Log.e(TAG, e.toString());
                            }
                        });
                        //endregion
                    } else {
                        //Subscribe
                        FirebaseMessaging.getInstance().subscribeToTopic(Topic_A);
                        FirebaseMessaging.getInstance().subscribeToTopic(Topic_B);

                        //region Add In System
                        Timestamp timestamp = new Timestamp(Calendar.getInstance().getTime());
                        Map<String, Object> map = new HashMap<>();
                        map.put(getString(R.string.F_BOOKS_FOLLOWER_UID), UID);
                        map.put(getString(R.string.F_BOOKS_FOLLOWER_DATE), timestamp);
                        FirebaseUtils.FirestroreSetData(Root, map, new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                followView.setEnabled(true);
                                imageIconFollow.setImageResource(R.drawable.icon_followed_black);
                                textInsideBookFollow.setText("Following");
                                Log.i(TAG, "NEW FOLLOW ADDED IN SYSTEM");
                            }
                        }, new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                followView.setEnabled(true);
                                Log.e(TAG, e.toString());
                            }
                        });
                        //endregion
                    }
                }
            }, new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    followView.setEnabled(true);
                }
            });
        }
        catch (Exception ex){
            Log.e(TAG, ex.toString());
        }
    }

    private void GetFollows(){
        try{
            String UID = FirebaseUtils.firebaseAuth.getCurrentUser().getUid();
            String Root = getString(R.string.FIRESTORE_ROOT_BOOKS) + "/" + BOOKID + "/" + getString(R.string.FIRESTORE_ROOT_BOOKS_FOLLOWER) + "/" + UID;
            FirebaseUtils.FirestoreGetData(Root, new OnSuccessListener<DocumentSnapshot>() {
                @Override
                public void onSuccess(DocumentSnapshot documentSnapshot) {
                    if(documentSnapshot.exists()){
                        imageIconFollow.setImageResource(R.drawable.icon_followed_black);
                        textInsideBookFollow.setText("Following");
                    }
                    else{
                        imageIconFollow.setImageResource(R.drawable.icon_follow);
                        textInsideBookFollow.setText("Follow");
                    }
                }
            }, new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    imageIconFollow.setImageResource(R.drawable.icon_follow);
                    textInsideBookFollow.setText("Follow");
                    Log.e(TAG, e.toString());
                }
            });
        }
        catch (Exception ex){
            Log.e(TAG, ex.toString());
        }
    }
    //endregion

    //region Similar Stories
    private void SimilarStories(){
        try{
            NoDataFound.setVisibility(View.INVISIBLE);
            String Root = getString(R.string.FIRESTORE_ROOT_BOOKS);
            CollectionReference collectionReference = FirebaseFirestore.getInstance().collection(Root);

            Task task1 = collectionReference.whereLessThan(getString(R.string.F_BOOKS_BOOKID), BOOKID).limit(SimilarLimit).get();
            Task task2 = collectionReference.whereGreaterThan(getString(R.string.F_BOOKS_BOOKID), BOOKID).limit(SimilarLimit).get();

            Task<List<QuerySnapshot>> allTasks = Tasks.whenAllSuccess(task1, task2);
            allTasks.addOnSuccessListener(new OnSuccessListener<List<QuerySnapshot>>() {
                @Override
                public void onSuccess(List<QuerySnapshot> querySnapshots) {
                    if(!querySnapshots.isEmpty()){
                        for(QuerySnapshot queryDocumentSnapshots: querySnapshots) {
                            for (QueryDocumentSnapshot documentSnapshot : queryDocumentSnapshots) {
                                //Adding Data
                                BookItems bookItems = new BookItems(
                                        documentSnapshot.getString(getString(R.string.F_BOOKS_PUBLICKEY)),
                                        documentSnapshot.getString(getString(R.string.F_BOOKS_IMAGEURL)),
                                        documentSnapshot.getString(getString(R.string.F_BOOKS_BOOKID)),
                                        documentSnapshot.getString(getString(R.string.F_BOOKS_TITLE)),
                                        documentSnapshot.getString(getString(R.string.F_BOOKS_AUTHOR)),
                                        documentSnapshot.getString(getString(R.string.F_BOOKS_DESCRIPTION)),
                                        Double.valueOf(documentSnapshot.get(getString(R.string.F_BOOKS_PRICE)).toString()),
                                        documentSnapshot.getString(getString(R.string.F_BOOKS_CURRENCYSTR)),
                                        (ArrayList)documentSnapshot.get(getString(R.string.F_BOOKS_GENRE)),
                                        BookItems.BookFlag.valueOf(documentSnapshot.getString(getString(R.string.F_BOOKS_BOOKFLAG))),
                                        BookItems.FileType.EPUB,
                                        BookItems.StorageType.CLOUD
                                );
                                tempStories.add(bookItems);
                            }
                        }
                        mAdapter.notifyDataSetChanged();
                    }
                    else{
                        //No data found
                        NoDataFound.setVisibility(View.VISIBLE);
                    }
                }
            }).addOnFailureListener(new OnFailureListener() {
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
    //endregion

    public void buttonView_onClick(View v){
        if(bookFlag == BookItems.BookFlag.Free || bookFlag == BookItems.BookFlag.Redownload){
            //Add To Library
            AddToLibrary();
        }
        else if(bookFlag == BookItems.BookFlag.Purchased){
            //Open Book View Portal
            OpenBookViewPortal();
        }
        else if(bookFlag == BookItems.BookFlag.Store){
            //Open Purchase Portal
            OpenPurchasePortal();
        }
    }

    public void buttonLike_onClick(View v){
        ToggleLike();
    }

    public void buttonComment_onClick(View view){
        counterTextView = textComments;
        ShowCommentPage();
    }

    public void buttonFollower_onClick(View view){
        ToggleFollow();
    }

    //Add To Library
    private void AddToLibrary(){
        try{
            final String extension;
            if(pKey.equals("")){extension = ".epub";}else{extension = ".zip";}
            //Download Book From Server
            Utils.SetProgressDialog(this, "downloading book", new ProgressDialogInterface() {
                @Override
                public void callback(final ProgressDialog progressDialog) {
                    //download book begins here
                   FirebaseUtils.SetBookToLocalDrive(
                           getString(R.string.FIRESTORAGE_LOCAL_DRIVE_BOOK),
                           extension,
                           BOOKID,      //"Metamorphosis-jackson.epub",
                           new OnProgressListener<FileDownloadTask.TaskSnapshot>() {
                               @Override
                               public void onProgress(FileDownloadTask.TaskSnapshot taskSnapshot) {
                                   double tempProgress = (100.0 * taskSnapshot.getBytesTransferred()) / taskSnapshot.getTotalByteCount();
                                   int progress = (int) tempProgress;
                                   progressDialog.setProgress(progress);
                               }
                           },
                           new OnCompleteListener<FileDownloadTask.TaskSnapshot>() {
                               @Override
                               public void onComplete(@NonNull Task<FileDownloadTask.TaskSnapshot> task) {
                                   //Complete
                                   if (task.isComplete()) {
                                       progressDialog.setMessage("processing..");
                                       //Save To Server
                                       String UID = FirebaseUtils.firebaseAuth.getCurrentUser().getUid();
                                       String Root = getString(R.string.FIRESTORE_ROOT_USERDETAILS) + "/" + UID + "/"  + getString(R.string.FIRESTORE_ROOT_LIBRARY) + "/" + BOOKID;

                                       Timestamp timestamp = new Timestamp(Calendar.getInstance().getTime());
                                       Map<String, Object> map = new HashMap<>();
                                       map.put(getString(R.string.F_USERDETAILS_LIBRARY_BOOKID), BOOKID);
                                       map.put(getString(R.string.F_USERDETAILS_LIBRARY_DATE), timestamp);
                                       map.put(getString(R.string.F_USERDETAILS_LIBRARY_STATE), LibraryState.ADDED.toString());
                                       map.put(getString(R.string.F_USERDETAILS_LIBRARY_PRIVACY), "public");
                                       FirebaseUtils.FirestroreSetData(
                                               Root,
                                               map,
                                               new OnSuccessListener<Void>() {
                                                   @Override
                                                   public void onSuccess(Void aVoid) {
                                                       progressDialog.dismiss();
                                                       new Handler(Activity_BookDetails.this.getMainLooper()).post(new Runnable() {
                                                           @Override
                                                           public void run() {
                                                               Fragment_Library.isNeedToReload = true;
                                                               bookFlag = BookItems.BookFlag.Purchased;
                                                               UpdateButtonView();
                                                           }
                                                       });
                                                   }
                                               }, new OnFailureListener() {
                                                   @Override
                                                   public void onFailure(@NonNull Exception e) {
                                                       Toast.makeText(Activity_BookDetails.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                                                   }
                                               });

                                       String CounterRoot = getString(R.string.FIRESTORE_ROOT_BOOKS) + "/" + BOOKID;
                                       FirebaseUtils.BookCounter(CounterRoot, getString(R.string.F_BOOKS_COUNTERDOWNLOAD), 1);
                                   }
                               }
                           }, new OnFailureListener() {
                               @Override
                               public void onFailure(@NonNull Exception e) {
                                   Toast.makeText(Activity_BookDetails.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                               }
                           });
                }
            }, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    bookFlag = BookItems.BookFlag.Free;
                    UpdateButtonView();
                    Log.i(TAG, "CANCEL DOWNLOADING BOOKS");
                }
            });
        }
        catch (Exception ex){
            Log.e(TAG, ex.toString());
        }
    }

    //Open Purchase Portal
    private void OpenPurchasePortal(){
        Toast.makeText(this, "Book Purchase Portal Is In Construction Phase", Toast.LENGTH_SHORT).show();
    }

    //Open Book View Portal
    private void OpenBookViewPortal(){
        try{
            BookItems bookItems = new BookItems(pKey,"", BOOKID, BookName, Author,"",0.00,"", null, null, BookItems.FileType.EPUB, BookItems.StorageType.CLOUD);
            Utils.OpenBook(this, bookItems);
        }
        catch (Exception ex){
            Log.e(TAG, ex.toString());
        }
    }

    private void UpdateButtonView(){
        if(bookFlag == BookItems.BookFlag.Free){
            textPurchaseFree.setText("Add To Library");
            imageIcon.setImageResource(R.drawable.icon_download);
            textPrice.setVisibility(View.GONE);
        }
        else if(bookFlag == BookItems.BookFlag.Redownload){
            if(sync){
                textPurchaseFree.setText("Sync To Library");
            }
            else{
                textPurchaseFree.setText("Re-download To Library");
            }

            imageIcon.setImageResource(R.drawable.icon_download);
            textPrice.setVisibility(View.GONE);
        }
        else if(bookFlag == BookItems.BookFlag.Store){
            textPurchaseFree.setText("Buy");
            imageIcon.setImageResource(R.drawable.icon_pack);
            textPrice.setText(price);
        }
        else if(bookFlag == BookItems.BookFlag.Purchased){
            textPurchaseFree.setText("Read Now");
            imageIcon.setImageResource(R.drawable.icon_book);
            textPrice.setVisibility(View.GONE);
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
    public void onOffsetChanged(AppBarLayout appBarLayout, int verticalOffset) {
        int maxScroll = appBarLayout.getTotalScrollRange();
        float percentage = (float) Math.abs(verticalOffset) / (float) maxScroll;
        handleAlphaOnTitle(percentage);
        handleToolbarTitleVisibility(percentage);
    }

    private void handleToolbarTitleVisibility(float percentage) {
        try {
            if (percentage >= PERCENTAGE_TO_SHOW_TITLE_AT_TOOLBAR) {
                if (!mIsTheTitleVisible) {
                    toolBarTitle.setText(tName);
                    Drawable upArrow = getResources().getDrawable(R.drawable.abc_ic_ab_back_material);
                    upArrow.setColorFilter((Color.BLACK), PorterDuff.Mode.SRC_ATOP);
                    getSupportActionBar().setHomeAsUpIndicator(upArrow);
                    startAlphaAnimation(toolBarTitle, ALPHA_ANIMATIONS_DURATION, View.VISIBLE);
                    mIsTheTitleVisible = true;
                }
            } else {

                if (mIsTheTitleVisible) {
                    Drawable upArrow = getResources().getDrawable(R.drawable.abc_ic_ab_back_material);
                    upArrow.setColorFilter((Color.WHITE), PorterDuff.Mode.SRC_ATOP);
                    getSupportActionBar().setHomeAsUpIndicator(upArrow);
                    startAlphaAnimation(toolBarTitle, ALPHA_ANIMATIONS_DURATION, View.INVISIBLE);
                    mIsTheTitleVisible = false;
                }
            }
        }
        catch (Exception ex){
            Log.e(TAG, ex.toString());
        }
    }

    private void handleAlphaOnTitle(float percentage) {
        if (percentage >= PERCENTAGE_TO_HIDE_TITLE_DETAILS) {
            if(mIsTheTitleContainerVisible) {
                startAlphaAnimation(relativeHolder, ALPHA_ANIMATIONS_DURATION, View.INVISIBLE);
                mIsTheTitleContainerVisible = false;
            }

        } else {

            if (!mIsTheTitleContainerVisible) {
                startAlphaAnimation(relativeHolder, ALPHA_ANIMATIONS_DURATION, View.VISIBLE);
                mIsTheTitleContainerVisible = true;
            }
        }
    }

    public static void startAlphaAnimation (View v, long duration, int visibility) {
        AlphaAnimation alphaAnimation = (visibility == View.VISIBLE)
                ? new AlphaAnimation(0f, 1f)
                : new AlphaAnimation(1f, 0f);

        alphaAnimation.setDuration(duration);
        alphaAnimation.setFillAfter(true);
        v.startAnimation(alphaAnimation);

        ScaleAnimation scaleAnimation = (visibility == View.VISIBLE)
                ?new ScaleAnimation(0,1,0,1, Animation.RELATIVE_TO_SELF,0.5f,Animation.RELATIVE_TO_SELF, 0.5f)
                :new ScaleAnimation(1,0,1,0,Animation.RELATIVE_TO_SELF,0.5f,Animation.RELATIVE_TO_SELF, 0.5f);
        scaleAnimation.setDuration(duration);
        scaleAnimation.setFillAfter(true);
        v.startAnimation(scaleAnimation);

    }
}

class SimilarStoriesAdapter extends RecyclerView.Adapter<SimilarStoriesAdapter.holderSimilarStories>{
    private String TAG = "Similar Stories Adapter";
    private Context mContext;
    private List<BookItems> tempStories;


    public SimilarStoriesAdapter(Context mContext, List<BookItems> tempStories) {
        this.mContext = mContext;
        this.tempStories = tempStories;
    }

    @NonNull
    @Override
    public holderSimilarStories onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.recycler_single_horozontal,parent,false);
        return new holderSimilarStories(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final holderSimilarStories holder, int position) {
        holder.mTextTitle.setText(tempStories.get(position).Name);

        ColorDrawable colorDrawableInfoPlaceholder = new ColorDrawable(ContextCompat.getColor(mContext, R.color.colorDrawableInfoPlaceholder));
        ColorDrawable colorDrawableErrorPlaceholder = new ColorDrawable(ContextCompat.getColor(mContext, R.color.colorDrawableErrorPlaceholder));
        Utils.SetImageViaUri(
                (Activity) mContext,
                tempStories.get(position).imageURL,
                holder.tImage,
                false,
                false,
                colorDrawableInfoPlaceholder,
                colorDrawableErrorPlaceholder,
                null
        );
        holder.view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(mContext, Activity_BookDetails.class);
                intent.putExtra("BookID", tempStories.get(holder.getAdapterPosition()).BOOKID);
                mContext.startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return tempStories.size();
    }

    class holderSimilarStories extends RecyclerView.ViewHolder{
        View view;
        ImageView tImage;
        TextView mTextTitle;
        public holderSimilarStories(View v){
            super(v);
            view = v.findViewById(R.id.selectBook);
            tImage = v.findViewById(R.id.imageRecyclerHorizontal);
            mTextTitle = v.findViewById(R.id.textRecyclerHorizontal);
        }

    }
}
