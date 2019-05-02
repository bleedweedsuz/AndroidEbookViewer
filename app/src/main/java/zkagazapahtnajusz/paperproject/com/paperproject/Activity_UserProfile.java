package zkagazapahtnajusz.paperproject.com.paperproject;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.TabLayout;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.rahimlis.badgedtablayout.BadgedTabLayout;

import de.hdodenhof.circleimageview.CircleImageView;
import zkagazapahtnajusz.paperproject.com.paperproject.Fragments.Fragment_UserProfile_Post;
import zkagazapahtnajusz.paperproject.com.paperproject.Fragments.Fragment_UserProfile_Purchase;
import zkagazapahtnajusz.paperproject.com.paperproject.Fragments.Fragment_UserProfile_Request;
import zkagazapahtnajusz.paperproject.com.paperproject.Utilities.FirebaseUtils;
import zkagazapahtnajusz.paperproject.com.paperproject.Utilities.Utils;

/**
 * Created by Zw4R-TSTUD10 2018/4/1 [2018, April 1]
 */

public class Activity_UserProfile extends AppCompatActivity implements AppBarLayout.OnOffsetChangedListener{

    private static String TAG = "ACTIVITY USER PROFILE";
    private static final int ALPHA_ANIMATIONS_DURATION = 300;
    private static final float PERCENTAGE_TO_SHOW_TITLE_AT_TOOLBAR  = 0.9f;
    private boolean mIsTheTitleVisible = false;

    ImageView profileBackground,profileSetting, profileAddFriend;
    AppBarLayout appBar;
    CollapsingToolbarLayout collapsingToolbarLayout;
    TextView textProfileUserName,textToolbarUserName, textEmail;
    LinearLayout linearLayoutTitle;
    ViewPager viewPager;
    BadgedTabLayout tabLayout;
    ViewPagerAdapterUserProfile adapter;
    Toolbar toolbar;
    CircleImageView profileImage;
    TextView textProfileFollowers, textProfilePost, textProfileFollowing;

    String InComingType = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity__user_profile);
        profileBackground = findViewById(R.id.profileBackground);

        textProfileFollowers = findViewById(R.id.textProfileFollowers);
        textProfilePost = findViewById(R.id.textProfilePost);
        textProfileFollowing = findViewById(R.id.textProfileFollowing);

        appBar = findViewById(R.id.appbar);
        collapsingToolbarLayout = findViewById(R.id.collapsing_toolbar);
        textProfileUserName = findViewById(R.id.textProfileUserName);
        textToolbarUserName = findViewById(R.id.textToolbarUserName);
        textEmail = findViewById(R.id.textProfilEmail);
        linearLayoutTitle = findViewById(R.id.linearLayoutTitle);
        toolbar = findViewById(R.id.toolbar);
        profileSetting = findViewById(R.id.buttonProfileSetting);
        profileAddFriend = findViewById(R.id.buttonProfileAddFriend);
        profileImage = findViewById(R.id.profileImage);

        setSupportActionBar(toolbar);
        if(getSupportActionBar() != null){
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);

            Drawable upArrow = ContextCompat.getDrawable(this,R.drawable.abc_ic_ab_back_material);
            upArrow.setColorFilter((Color.WHITE), PorterDuff.Mode.SRC_ATOP);
            getSupportActionBar().setHomeAsUpIndicator(upArrow);
        }

        profileSetting.setColorFilter((Color.WHITE), PorterDuff.Mode.SRC_ATOP);
        profileAddFriend.setColorFilter((Color.WHITE), PorterDuff.Mode.SRC_ATOP);


        textToolbarUserName.setVisibility(View.INVISIBLE);
        appBar.addOnOffsetChangedListener(this);

        viewPager = findViewById(R.id.viewPagerUserProfile);
        adapter = new ViewPagerAdapterUserProfile(getSupportFragmentManager());

        //Post Fragment
        adapter.addFragmentUserProfile(new Fragment_UserProfile_Post());
        adapter.addFragmentUserProfile(new Fragment_UserProfile_Purchase());
        adapter.addFragmentUserProfile(new Fragment_UserProfile_Request());

        viewPager.setAdapter(adapter);
        viewPager.setOffscreenPageLimit(3);

        tabLayout = findViewById(R.id.tabLayoutUserProfile);
        tabLayout.setupWithViewPager(viewPager);
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                if(tab.getPosition() == 2){
                    SetTabBadgeCounter(2, null);
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });

        SetTabBadgeCounter(1, null); //Not Include Index = 1

        Bundle bundle = getIntent().getExtras();
        if(bundle !=null && !bundle.isEmpty() && bundle.containsKey("InComingType")) {
            InComingType = bundle.getString("InComingType");
            tabLayout.getTabAt(2).select();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        //Load Firebase UserDetails
        LoadUserDetails();
    }

    //Loading User Details
    private void LoadUserDetails(){
        if(FirebaseUtils.firebaseAuth != null){
            try {
                String FullName = FirebaseUtils.firebaseAuth.getCurrentUser().getDisplayName();
                String Email = FirebaseUtils.firebaseAuth.getCurrentUser().getEmail();
                if (FullName != null) {
                    textProfileUserName.setText(FullName);
                    textToolbarUserName.setText(FullName);
                }
                if (Email != null) {
                    textEmail.setText(Email);
                }

                //Profile Image
                LoadProfileImages();

                //Request Counter
                RequestCounter();

                //Load Followers Counter
                FollowerCounter();

                //Load Post Counter
                PostCounter();

                //Load Following Counter
                FollowingCounter();
            }
            catch (Exception ex){
                Log.e(TAG, ex.toString());
            }
        }
    }

    private void LoadProfileImages() {
        try {
            String UID = FirebaseUtils.firebaseAuth.getCurrentUser().getUid();
            String Root = getString(R.string.FIRESTORE_ROOT_USERDETAILS) + "/" + UID;
            FirebaseUtils.FirestoreGetData(Root, new OnSuccessListener<DocumentSnapshot>() {
                @Override
                public void onSuccess(DocumentSnapshot documentSnapshot) {
                    if (documentSnapshot.exists()) {
                        try {
                            String UserProfileLink =""; try { UserProfileLink= documentSnapshot.getString(getString(R.string.F_USERDETAILS_PROFILEURL));}catch (Exception ex){Log.e(TAG, ex.toString());}
                            String UserBackgroundProfileLink =""; try { UserBackgroundProfileLink= documentSnapshot.getString(getString(R.string.F_USERDETAILS_BACKGROUNDURL));}catch (Exception ex){Log.e(TAG, ex.toString());}

                            ColorDrawable colorDrawableInfoPlaceholder = new ColorDrawable(ContextCompat.getColor(getApplicationContext(), R.color.colorDrawableInfoPlaceholder));
                            ColorDrawable colorDrawableErrorPlaceholder = new ColorDrawable(ContextCompat.getColor(getApplicationContext(), R.color.colorDrawableErrorPlaceholder));

                            Utils.SetImageViaUri(Activity_UserProfile.this, UserProfileLink, profileImage, false, false, colorDrawableInfoPlaceholder, colorDrawableErrorPlaceholder, null);
                            Utils.SetImageViaUri(Activity_UserProfile.this, UserBackgroundProfileLink, profileBackground, true,true, colorDrawableInfoPlaceholder, colorDrawableErrorPlaceholder,null);
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

    private void PostCounter(){
        try{
            String UID = FirebaseUtils.firebaseAuth.getCurrentUser().getUid();
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
            Log.e(TAG, ex.toString());
        }
    }

    private void FollowerCounter(){
        try{
            String MYID = FirebaseUtils.firebaseAuth.getCurrentUser().getUid();
            final String Root = getString(R.string.FIRESTORE_ROOT_USERDETAILS) + "/" + MYID + "/" +getString(R.string.FIRESTORE_ROOT_FOLLOWER);
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
            Log.e(TAG, ex.toString());
        }
    }

    private void FollowingCounter(){
        try{
            String MYID = FirebaseUtils.firebaseAuth.getCurrentUser().getUid();
            final String Root = getString(R.string.FIRESTORE_ROOT_USERDETAILS) + "/" + MYID + "/" +getString(R.string.FIRESTORE_ROOT_FOLLOWING);
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
            Log.e(TAG, ex.toString());
        }
    }

    private void RequestCounter(){
        try{
            String MYID = FirebaseUtils.firebaseAuth.getCurrentUser().getUid();
            String Root = getString(R.string.FIRESTORE_ROOT_USERDETAILS) + "/" + MYID + "/" +getString(R.string.FIRESTORE_ROOT_FOLLOWER);
            CollectionReference collectionReference = FirebaseFirestore.getInstance().collection(Root);
            Query query = collectionReference.whereEqualTo(getString(R.string.F_USERDETAILS_FOLLOWER_STATE), Activity_Friend_Profile.FollowState.REQUEST.toString());

            FirebaseUtils.FirestoreGetAllData(query, this, new EventListener<QuerySnapshot>() {
                @Override
                public void onEvent(QuerySnapshot queryDocumentSnapshots, FirebaseFirestoreException e) {
                    if(!queryDocumentSnapshots.isEmpty()){
                        int size = 0;
                        for(DocumentChange doc : queryDocumentSnapshots.getDocumentChanges()) {
                            if(doc.getType() == DocumentChange.Type.ADDED) {
                                if(tabLayout.getSelectedTabPosition() != 2) {
                                    size += 1;
                                }
                            }
                        }
                        if(size >0){
                            SetTabBadgeCounter(2, String.valueOf(size));
                        }
                        else{
                            SetTabBadgeCounter(2, null);
                        }
                    }
                }
            });
        }
        catch (Exception ex){
            Log.e(TAG, ex.toString());
        }
    }

    public void UserFollowers_OnClick(View view){
        Intent i = new Intent(this,Activity_SearchPage.class);
        i.putExtra("SearchType", Activity_SearchPage.SearchType.FOLLOWER.toString());
        i.putExtra("tag","Followers");
        i.putExtra("queryhint","Search Followers");
        i.putExtra("isKeyboardFocus",true);
        startActivity(i);
    }

    public void UserFollowing_OnClick(View view){
        Intent i = new Intent(this,Activity_SearchPage.class);
        i.putExtra("SearchType", Activity_SearchPage.SearchType.FOLLOWING.toString());
        i.putExtra("tag","Following");
        i.putExtra("queryhint","Search Following");
        i.putExtra("isKeyboardFocus",true);
        startActivity(i);
    }

    public void ProfileSetting_OnClick(View view){
        Intent i = new Intent(this,Activity_UserProfile_Setting.class);
        startActivity(i);
    }

    public void ProfileAddFriend_OnClick(View view){
        Intent i = new Intent(this,Activity_SearchPage.class);
        i.putExtra("SearchType", Activity_SearchPage.SearchType.SEARCHFRIENDS.toString());
        i.putExtra("tag","Friend");
        i.putExtra("queryhint","Search User");
        i.putExtra("isKeyboardFocus",true);
        startActivity(i);
    }

    public void Post_OnClick(View view){
        //Just Select Tab
        tabLayout.getTabAt(0).select();
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

       // handleAlphaOnTitle(percentage);
        handleToolbarTitleVisibility(percentage);
    }

    private void handleToolbarTitleVisibility(float percentage) {
        if (percentage >= PERCENTAGE_TO_SHOW_TITLE_AT_TOOLBAR) {

            if(!mIsTheTitleVisible) {

                Drawable upArrow = ContextCompat.getDrawable(this,R.drawable.abc_ic_ab_back_material);
                upArrow.setColorFilter((Color.BLACK), PorterDuff.Mode.SRC_ATOP);
                getSupportActionBar().setHomeAsUpIndicator(upArrow);

                profileSetting.setColorFilter((Color.BLACK), PorterDuff.Mode.SRC_ATOP);
                profileAddFriend.setColorFilter((Color.BLACK), PorterDuff.Mode.SRC_ATOP);

                startAlphaAnimation(textToolbarUserName, ALPHA_ANIMATIONS_DURATION, View.VISIBLE);
                mIsTheTitleVisible = true;
            }

        } else {

            if (mIsTheTitleVisible) {

                Drawable upArrow = ContextCompat.getDrawable(this,R.drawable.abc_ic_ab_back_material);
                upArrow.setColorFilter((Color.WHITE), PorterDuff.Mode.SRC_ATOP);
                getSupportActionBar().setHomeAsUpIndicator(upArrow);

                profileSetting.setColorFilter((Color.WHITE), PorterDuff.Mode.SRC_ATOP);
                profileAddFriend.setColorFilter((Color.WHITE), PorterDuff.Mode.SRC_ATOP);

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

    private void SetTabBadgeCounter(int tablayoutIndex, String counter){
        if(tabLayout != null){
            tabLayout.setBadgeText(tablayoutIndex, counter);
        }
    }
}
