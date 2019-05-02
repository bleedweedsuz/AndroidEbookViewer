package zkagazapahtnajusz.paperproject.com.paperproject;

import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.TabLayout;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.functions.HttpsCallableResult;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import javax.annotation.Nullable;

import de.hdodenhof.circleimageview.CircleImageView;
import zkagazapahtnajusz.paperproject.com.paperproject.Fragments.Fragment_FriendProfile_Details;
import zkagazapahtnajusz.paperproject.com.paperproject.Fragments.Fragment_UserProfile_Post;
import zkagazapahtnajusz.paperproject.com.paperproject.Fragments.Fragment_UserProfile_Purchase;
import zkagazapahtnajusz.paperproject.com.paperproject.Utilities.FirebaseFunctionsUtils;
import zkagazapahtnajusz.paperproject.com.paperproject.Utilities.FirebaseUtils;
import zkagazapahtnajusz.paperproject.com.paperproject.Utilities.Utils;

public class Activity_Friend_Profile extends AppCompatActivity implements AppBarLayout.OnOffsetChangedListener{

    private static String TAG = "ACTIVITY FRIEND PROFILE";
    private static final int ALPHA_ANIMATIONS_DURATION = 300;
    private static final float PERCENTAGE_TO_SHOW_TITLE_AT_TOOLBAR  = 0.9f;

    private String FRIEND_UID, FOLLOW_ID;
    private boolean mIsTheTitleVisible = false;
    ImageView profileBackground;
    AppBarLayout appBar;
    CollapsingToolbarLayout collapsingToolbarLayout;
    TextView textProfileUserName,textToolbarUserName, textEmail;
    LinearLayout linearLayoutTitle;
    ViewPager viewPager;
    TabLayout tabLayout;
    ViewPagerAdapterFriendProfile adapter;
    Toolbar toolbar;
    CircleImageView profileImage;

    //Follow Button
    View buttonFollow;
    TextView textFollow;
    ImageView imageFollow;

    TextView textProfileFollowers, textProfilePost, textProfileFollowing;

    public enum FollowState { REQUEST, ACCEPT, NOTHING}
    FollowState followState = FollowState.NOTHING;

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity__friend__profile);
        profileBackground = findViewById(R.id.profileBackground);
        appBar = findViewById(R.id.appbar);
        collapsingToolbarLayout = findViewById(R.id.collapsing_toolbar);

        textProfileUserName = findViewById(R.id.textProfileUserName);
        textToolbarUserName = findViewById(R.id.textToolbarUserName);

        textProfileFollowers = findViewById(R.id.textProfileFollowers);
        textProfilePost = findViewById(R.id.textProfilePost);
        textProfileFollowing = findViewById(R.id.textProfileFollowing);

        linearLayoutTitle = findViewById(R.id.linearLayoutTitle);
        toolbar = findViewById(R.id.toolbar);
        profileImage = findViewById(R.id.profileImage);

        buttonFollow = findViewById(R.id.buttonFriendProfileFollow);
        textFollow = findViewById(R.id.textFriendProfileFollow);
        imageFollow = findViewById(R.id.imageFriendProfileFollow);
        setSupportActionBar(toolbar);

        if(getSupportActionBar() != null){
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);

            Drawable upArrow = ContextCompat.getDrawable(this,R.drawable.abc_ic_ab_back_material);
            upArrow.setColorFilter((Color.WHITE), PorterDuff.Mode.SRC_ATOP);
            getSupportActionBar().setHomeAsUpIndicator(upArrow);
        }

        //GET EXTRA DATA
        FRIEND_UID = getIntent().getExtras().getString("UID");



        textToolbarUserName.setVisibility(View.INVISIBLE);
        appBar.addOnOffsetChangedListener(this);


        viewPager = findViewById(R.id.viewPagerUserProfile);

        adapter = new ViewPagerAdapterFriendProfile(getSupportFragmentManager());

        //Post Fragment
        Fragment_UserProfile_Post fragment_userProfile_post = new Fragment_UserProfile_Post();
        fragment_userProfile_post.UserID= FRIEND_UID;
        adapter.addFragmentUserProfile(fragment_userProfile_post);

        Fragment_UserProfile_Purchase fragment_userProfile_purchase = new Fragment_UserProfile_Purchase();
        fragment_userProfile_purchase.UserID = FRIEND_UID;
        fragment_userProfile_purchase.isFriendProfile = true;
        adapter.addFragmentUserProfile(fragment_userProfile_purchase);


        Fragment_FriendProfile_Details fragment_friendProfile_details = new Fragment_FriendProfile_Details();
        fragment_friendProfile_details.UserID = FRIEND_UID;
        adapter.addFragmentUserProfile(fragment_friendProfile_details);


        viewPager.setAdapter(adapter);
        viewPager.setOffscreenPageLimit(3);
        tabLayout = findViewById(R.id.tabLayoutUserProfile);
        tabLayout.setupWithViewPager(viewPager);
        LoadUserDetails();
    }

    private void LoadUserDetails(){
        try{
            Utils.SetProgressDialogIndeterminate(this, "loading..");
            //region Load CheckFollowState
            CheckFollowState();
            //endregion

            //region Load Main User Info
            String Root = getString(R.string.FIRESTORE_ROOT_USERDETAILS) + "/" + FRIEND_UID;
            FirebaseUtils.FirestoreGetData(
                    Root,
                    new OnSuccessListener<DocumentSnapshot>() {
                        @Override
                        public void onSuccess(DocumentSnapshot documentSnapshot) {
                            if(documentSnapshot.exists()){
                                String FullName = ""; try{ FullName = documentSnapshot.getString(getString(R.string.F_USERDETAILS_USERFULLNAME));} catch (NullPointerException ex){Log.e (TAG, ex.toString());}
                                String ProfileImageLink = ""; try{ ProfileImageLink = documentSnapshot.getString(getString(R.string.F_USERDETAILS_PROFILEURL));} catch (NullPointerException ex){Log.e (TAG, ex.toString());}
                                String ProfileBackgroundLink = ""; try{ ProfileBackgroundLink = documentSnapshot.getString(getString(R.string.F_USERDETAILS_BACKGROUNDURL));} catch (NullPointerException ex){Log.e (TAG, ex.toString());}

                                textProfileUserName.setText(FullName);
                                textToolbarUserName.setText(FullName);
                                //Profile Picture
                                {
                                    ColorDrawable colorDrawableInfoPlaceholder = new ColorDrawable(ContextCompat.getColor(Activity_Friend_Profile.this, R.color.colorDrawableInfoPlaceholder));
                                    ColorDrawable colorDrawableErrorPlaceholder = new ColorDrawable(ContextCompat.getColor(Activity_Friend_Profile.this, R.color.colorDrawableErrorPlaceholder));
                                    Utils.SetImageViaUri(
                                            Activity_Friend_Profile.this,
                                            FRIEND_UID,
                                            ProfileImageLink,
                                            profileImage,
                                            false,
                                            false,
                                            colorDrawableInfoPlaceholder,
                                            colorDrawableErrorPlaceholder,
                                            null
                                    );
                                }
                                {
                                    ColorDrawable colorDrawableInfoPlaceholder = new ColorDrawable(ContextCompat.getColor(Activity_Friend_Profile.this, R.color.colorDrawableInfoPlaceholder));
                                    ColorDrawable colorDrawableErrorPlaceholder = new ColorDrawable(ContextCompat.getColor(Activity_Friend_Profile.this, R.color.colorDrawableErrorPlaceholder));
                                    Utils.SetImageViaUri(
                                            Activity_Friend_Profile.this,
                                            FRIEND_UID,
                                            ProfileBackgroundLink,
                                            profileBackground,
                                            true,
                                            true,
                                            colorDrawableInfoPlaceholder,
                                            colorDrawableErrorPlaceholder,
                                            null
                                    );
                                }
                                Utils.UnSetProgressDialogIndeterminate();
                            }
                        }
                    },
                    new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Utils.UnSetProgressDialogIndeterminate();
                        }
                    });
            //endregion

            //region Load Followers Counter
            FollowerCounter();
            //endregion

            //region Load Post Counter
            PostCounter();
            //endregion

            //region Load Following Counter
            FollowingCounter();
            //endregion

        }
        catch (Exception ex){
            Utils.UnSetProgressDialogIndeterminate();
            Log.e(TAG, "1" + ex.toString());
        }
    }

    private void PostCounter(){
        try{
            String UID = FRIEND_UID;
            String Root = getString(R.string.FIRESTORE_ROOT_USERDETAILS)  + "/" + UID + "/" + getString(R.string.FIRESTORE_ROOT_POSTS);

            FirebaseFirestore firebaseFirestore = FirebaseFirestore.getInstance();
            Query firstQuery = firebaseFirestore.collection(Root).whereEqualTo(getString(R.string.F_POSTS_POST_UID), UID);
            firstQuery.addSnapshotListener(this, new EventListener<QuerySnapshot>() {
                @Override
                public void onEvent(QuerySnapshot queryDocumentSnapshots, FirebaseFirestoreException e) {
                    if(!queryDocumentSnapshots.isEmpty()){
                        for(DocumentChange doc :  queryDocumentSnapshots.getDocumentChanges()){
                            if(doc.getType() == DocumentChange.Type.ADDED){
                                int size = queryDocumentSnapshots.getDocuments().size();
                                textProfilePost.setText(size + "");
                            }
                        }
                    }
                }
            });
        }
        catch (Exception ex){
            Log.e(TAG, "2" + ex.toString());
        }
    }

    private void FollowerCounter(){
        try{
            String UID = FRIEND_UID;
            final String Root = getString(R.string.FIRESTORE_ROOT_USERDETAILS) + "/" + UID + "/" +getString(R.string.FIRESTORE_ROOT_FOLLOWER);
            CollectionReference collectionReference = FirebaseFirestore.getInstance().collection(Root);
            Query query = collectionReference.whereEqualTo(getString(R.string.F_USERDETAILS_FOLLOWER_STATE), Activity_Friend_Profile.FollowState.ACCEPT.toString());

            FirebaseUtils.FirestoreGetAllData(query, this, new EventListener<QuerySnapshot>() {
                @Override
                public void onEvent(QuerySnapshot queryDocumentSnapshots, FirebaseFirestoreException e) {
                    if(!queryDocumentSnapshots.isEmpty()){
                        int size = 0;
                        for(DocumentChange doc : queryDocumentSnapshots.getDocumentChanges()) {
                            if(doc.getType() == DocumentChange.Type.ADDED) {
                                size += 1;
                            }
                        }
                        textProfileFollowers.setText(String.valueOf(size));
                    }
                }
            });

        }
        catch (Exception ex){
            Log.e(TAG, "3" + ex.toString());
        }
    }

    private void FollowingCounter(){
        try{
            String UID = FRIEND_UID;
            final String Root = getString(R.string.FIRESTORE_ROOT_USERDETAILS) + "/" + UID + "/" +getString(R.string.FIRESTORE_ROOT_FOLLOWING);
            CollectionReference collectionReference = FirebaseFirestore.getInstance().collection(Root);
            Query query = collectionReference.whereEqualTo(getString(R.string.F_USERDETAILS_FOLLOWER_STATE), Activity_Friend_Profile.FollowState.ACCEPT.toString());

            FirebaseUtils.FirestoreGetAllData(query, this, new EventListener<QuerySnapshot>() {
                @Override
                public void onEvent(QuerySnapshot queryDocumentSnapshots, FirebaseFirestoreException e) {
                    if(!queryDocumentSnapshots.isEmpty()){
                        int size = 0;
                        for(DocumentChange doc : queryDocumentSnapshots.getDocumentChanges()) {
                            if(doc.getType() == DocumentChange.Type.ADDED) {
                                size += 1;
                            }
                        }
                        textProfileFollowing.setText(String.valueOf(size));
                    }
                }
            });

        }
        catch (Exception ex){
            Log.e(TAG, "4" + ex.toString());
        }
    }

    private void CheckFollowState(){
        try{
            String MY_ID = FirebaseUtils.firebaseAuth.getCurrentUser().getUid();
            String Root = getString(R.string.FIRESTORE_ROOT_USERDETAILS) + "/" + FRIEND_UID + "/" + getString(R.string.FIRESTORE_ROOT_FOLLOWER);
            CollectionReference requestRefrence = FirebaseFirestore.getInstance().collection(Root);
            final Query query = requestRefrence.whereEqualTo(getString(R.string.F_USERDETAILS_FOLLOWER_UID), MY_ID);
            FirebaseUtils.FirestoreGetAllData(query, this, new EventListener<QuerySnapshot>() {
                @Override
                public void onEvent(QuerySnapshot queryDocumentSnapshots, FirebaseFirestoreException e) {
                    if(queryDocumentSnapshots != null && !queryDocumentSnapshots.isEmpty()){
                        for(DocumentChange doc : queryDocumentSnapshots.getDocumentChanges()){
                            if(doc.getType() == DocumentChange.Type.ADDED){
                                FollowState temp = FollowState.valueOf(doc.getDocument().getString(Activity_Friend_Profile.this.getString(R.string.F_USERDETAILS_FOLLOWER_STATE)));
                                FOLLOW_ID = doc.getDocument().getId();
                                if(temp == FollowState.ACCEPT) {
                                    followState = FollowState.ACCEPT;
                                }
                                else if(temp == FollowState.REQUEST){
                                    followState = FollowState.REQUEST;
                                }
                                UpdateFollowView();
                            }
                            else if(doc.getType() == DocumentChange.Type.MODIFIED){
                                FollowState temp = FollowState.valueOf(doc.getDocument().getString(Activity_Friend_Profile.this.getString(R.string.F_USERDETAILS_FOLLOWER_STATE)));
                                if(temp == FollowState.ACCEPT) {
                                    followState = FollowState.ACCEPT;
                                }
                                else if(temp == FollowState.REQUEST){
                                    followState = FollowState.REQUEST;
                                }
                                else{
                                    followState = FollowState.NOTHING;
                                }
                                UpdateFollowView();
                            }
                        }
                    }
                    else{
                        //Nothing
                        followState = FollowState.NOTHING;
                        UpdateFollowView();
                    }
                }
            });
        }
        catch (Exception ex)
        {
            Log.e(TAG, "5" + ex.toString());
        }
    }

    public void ButtonFollow_Click(View view) {
        if(followState == FollowState.NOTHING) {
            SendFollowRequest();
        }
        else if(followState == FollowState.REQUEST){
            //Cancel Following
            CancelRequest();
        }
        else if(followState == FollowState.ACCEPT){
            //Cancel Request
            CancelRequestFull();
        }
    }

    private void SendFollowRequest(){
        try {
            Utils.SetProgressDialogIndeterminate(this, "sending request");
            final String UIID = Utils.UUIDToken();
            String MYID = FirebaseUtils.firebaseAuth.getCurrentUser().getUid();
            String Root = getString(R.string.FIRESTORE_ROOT_USERDETAILS) + "/" + FRIEND_UID  + "/" + getString(R.string.FIRESTORE_ROOT_FOLLOWER) + "/" + UIID;
            Map<String, Object> map = new HashMap<>();
            map.put(getString(R.string.F_USERDETAILS_FOLLOWER_UID), MYID);
            map.put(getString(R.string.F_USERDETAILS_FOLLOWER_STATE), FollowState.REQUEST.toString());
            map.put(getString(R.string.F_USERDETAILS_FOLLOWER_DATE), new Timestamp(Calendar.getInstance().getTime()));
            FirebaseUtils.FirestroreSetData(Root, map,
                    new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            FOLLOW_ID = UIID;
                            followState = FollowState.REQUEST;
                            UpdateFollowView();
                            Utils.UnSetProgressDialogIndeterminate();
                        }
                    }, new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Utils.UnSetProgressDialogIndeterminate();
                            Log.e(TAG, "7" + e.toString());
                        }
                    });

            //region Call Server Function
            Map<String, Object> postMap = new HashMap<>();
            postMap.put("uid", FRIEND_UID);
            postMap.put("description", "");
            postMap.put("displayname", FirebaseUtils.firebaseAuth.getCurrentUser().getDisplayName());
            postMap.put("notificationtype", "followrequest");

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
        catch (Exception ex) {
            Utils.UnSetProgressDialogIndeterminate();
            Log.e(TAG, "6" + ex.toString());
        }
    }

    private void unsubscribeTopic(){
        FirebaseFunctionsUtils.UnSubscribeTopic_User(FRIEND_UID);
    }

    private void UpdateFollowView(){
        if(followState == FollowState.NOTHING){
            imageFollow.setImageResource(R.drawable.icon_follow_white);
            textFollow.setText("Follow");
        }
        else if(followState == FollowState.REQUEST){
            imageFollow.setImageResource(R.drawable.icon_pending_white);
            textFollow.setText("Pending");
        }
        else if(followState == FollowState.ACCEPT){
            imageFollow.setImageResource(R.drawable.icon_followed_white);
            textFollow.setText("Following");
        }
    }

    private void CancelRequest(){
        try{
            new AlertDialog.Builder(this).
                    setTitle("Are you sure want to cancel the request").
                    setPositiveButton("yes", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            //Unsubscribe Topic
                            Utils.SetProgressDialogIndeterminate(Activity_Friend_Profile.this, "canceling");
                            unsubscribeTopic();
                            String Root = Activity_Friend_Profile.this.getString(R.string.FIRESTORE_ROOT_USERDETAILS) + "/" + FRIEND_UID + "/" +  Activity_Friend_Profile.this.getString(R.string.FIRESTORE_ROOT_FOLLOWER) + "/" +  FOLLOW_ID;
                            FirebaseUtils.FirestoreDeleteData(Root,
                                    new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {
                                            Log.i(TAG, "SUCCESS: Delete data");
                                            Utils.UnSetProgressDialogIndeterminate();
                                        }
                                    }, new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            Log.e(TAG, "11" + e.toString());
                                            Utils.UnSetProgressDialogIndeterminate();
                                        }
                                    });
                        }
                    }).
                    setNegativeButton("no", null).
                    create().show();
        }
        catch (Exception ex){
            Log.e(TAG, "8" + ex.toString());
        }
    }

    private void CancelRequestFull(){
        new AlertDialog.Builder(this).
                setTitle("Are you sure want to unfollow this user?").
                setPositiveButton("yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        try{
                            Utils.SetProgressDialogIndeterminate(Activity_Friend_Profile.this, "unfollowing..");
                            //Unsubscribe Topic
                            unsubscribeTopic();

                            String MY_ID = FirebaseUtils.firebaseAuth.getCurrentUser().getUid();
                            String Root = Activity_Friend_Profile.this.getString(R.string.FIRESTORE_ROOT_USERDETAILS) + "/" + MY_ID + "/" +  Activity_Friend_Profile.this.getString(R.string.FIRESTORE_ROOT_FOLLOWING) + "/" +  FOLLOW_ID;
                            FirebaseUtils.FirestoreDeleteData(Root, new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    String Root = Activity_Friend_Profile.this.getString(R.string.FIRESTORE_ROOT_USERDETAILS) + "/" + FRIEND_UID + "/" + Activity_Friend_Profile.this.getString(R.string.FIRESTORE_ROOT_FOLLOWER) + "/" + FOLLOW_ID;
                                    FirebaseUtils.FirestoreDeleteData(Root,
                                            new OnSuccessListener<Void>() {
                                                @Override
                                                public void onSuccess(Void aVoid) {
                                                    followState = FollowState.NOTHING;
                                                    UpdateFollowView();
                                                    Utils.UnSetProgressDialogIndeterminate();
                                                }
                                            }, new OnFailureListener() {
                                                @Override
                                                public void onFailure(@NonNull Exception e) {
                                                    Utils.UnSetProgressDialogIndeterminate();
                                                }
                                            });
                                }
                            }, new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Utils.UnSetProgressDialogIndeterminate();
                                }
                            });

                            //Remove all post from cloud computing system
                            RemovePostFrom(MY_ID, FRIEND_UID);
                        }
                        catch (Exception ex){
                            Utils.UnSetProgressDialogIndeterminate();
                            Log.e(TAG, "9" + ex.toString());
                        }
                    }
                }).
                setNegativeButton("no", null).
                create().show();
    }

    public void Post_OnClick(View view){
        //Just Select Tab
        tabLayout.getTabAt(0).select();
    }

    private void RemovePostFrom(String MYID, String FID){
        try{
            //region Remove All Friend Post From My Database
            final String Root_Post = getString(R.string.FIRESTORE_ROOT_USERDETAILS) + "/" + MYID + "/" + getString(R.string.FIRESTORE_ROOT_POSTS);
            Query postquery = FirebaseFirestore.getInstance().collection(Root_Post).whereEqualTo(getString(R.string.F_POSTS_POST_UID), FID);
            FirebaseUtils.FirestoreGetAllData(
                    postquery,
                    this,
                    new EventListener<QuerySnapshot>() {
                        @Override
                        public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {
                            if(queryDocumentSnapshots != null && !queryDocumentSnapshots.isEmpty()){
                                for(DocumentChange doc : queryDocumentSnapshots.getDocumentChanges()){
                                    if(doc.getType() == DocumentChange.Type.ADDED){
                                        String postid = doc.getDocument().getString(getString(R.string.F_POSTS_POSTID));
                                        //region Remove Post
                                        String Root_P = Root_Post + "/" + postid;
                                        FirebaseUtils.FirestoreDeleteData(Root_P, new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void aVoid) {
                                                Log.e(TAG, "Remove All Posts");
                                            }
                                        }, new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception e) {
                                                Log.e(TAG, e.toString());
                                            }
                                        });
                                        //endregion
                                    }
                                }
                            }
                            else{
                                if(e != null){
                                    Log.e(TAG, e.toString());
                                }
                            }
                        }
                    });
            //endregion

            //region Remove Notification From My Database
            final String Root_Notification = getString(R.string.FIRESTORE_ROOT_USERDETAILS) + "/" + MYID + "/" + getString(R.string.FIRESTORE_ROOT_NOTIFICATIONS);
            Query notification_query = FirebaseFirestore.getInstance().collection(Root_Notification).whereEqualTo(getString(R.string.F_NOTIFICATIONS_UID), FID);
            FirebaseUtils.FirestoreGetAllData(
                    notification_query,
                    this,
                    new EventListener<QuerySnapshot>() {
                        @Override
                        public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {
                            if(queryDocumentSnapshots != null && !queryDocumentSnapshots.isEmpty()){
                                for(DocumentChange doc : queryDocumentSnapshots.getDocumentChanges()){
                                   if(doc.getType() == DocumentChange.Type.ADDED){
                                       doc.getDocument().getReference().delete();
                                   }
                                }
                            }
                            else{
                                if(e != null){
                                    Log.e(TAG, e.toString());
                                }
                            }
                        }
                    });
            //endregion

            //region Remove From Post Server
            /*
            Map<String, Object> map = new HashMap<>();
            map.put("my_uid", MYID);
            map.put("f_uid", FID);
            FirebaseUtils.RunServerFunction("RemovePostFrom", map,
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
            */
                    //endregion
        }
        catch (Exception ex){
            Log.e(TAG, ex.toString());
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if(item.getItemId() == android.R.id.home){
            finish();
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onOffsetChanged(AppBarLayout appBarLayout, int verticalOffset) {
        int maxScroll = appBarLayout.getTotalScrollRange();
        float percentage = (float) Math.abs(verticalOffset) / (float) maxScroll;

        // handleAlphaOnTitle(percentage);
        handleToolbarTitleVisibility(percentage);
    }

    private void handleToolbarTitleVisibility(float percentage) {
        if (percentage >= PERCENTAGE_TO_SHOW_TITLE_AT_TOOLBAR) {

            if(!mIsTheTitleVisible) {

                Drawable upArrow = ContextCompat.getDrawable(this,R.drawable.abc_ic_ab_back_material);
                upArrow.setColorFilter((Color.BLACK), PorterDuff.Mode.SRC_ATOP);
                getSupportActionBar().setHomeAsUpIndicator(upArrow);

                startAlphaAnimation(textToolbarUserName, ALPHA_ANIMATIONS_DURATION, View.VISIBLE);
                mIsTheTitleVisible = true;
            }

        } else {

            if (mIsTheTitleVisible) {

                Drawable upArrow = ContextCompat.getDrawable(this,R.drawable.abc_ic_ab_back_material);
                upArrow.setColorFilter((Color.WHITE), PorterDuff.Mode.SRC_ATOP);
                getSupportActionBar().setHomeAsUpIndicator(upArrow);

                startAlphaAnimation(textToolbarUserName, ALPHA_ANIMATIONS_DURATION, View.INVISIBLE);
                mIsTheTitleVisible = false;
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
    }
}
