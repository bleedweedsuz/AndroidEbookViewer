package zkagazapahtnajusz.paperproject.com.paperproject;

import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import de.hdodenhof.circleimageview.CircleImageView;
import zkagazapahtnajusz.paperproject.com.paperproject.Fragments.*;
import zkagazapahtnajusz.paperproject.com.paperproject.Model.BookItems;
import zkagazapahtnajusz.paperproject.com.paperproject.Utilities.FirebaseUtils;
import zkagazapahtnajusz.paperproject.com.paperproject.Utilities.NetworkManagerUtils;
import zkagazapahtnajusz.paperproject.com.paperproject.Utilities.PermissionUtils;
import zkagazapahtnajusz.paperproject.com.paperproject.Utilities.Utils;

/**
 * Created by Zw4R-TSTUD10 2018/4/1 [2018, April 1]
 */

public class Activity_Home extends AppCompatActivity {
    private static final String TAG = "Activity_Home";
    Toolbar toolbar;
    ViewPager viewPager;
    TabLayout tabLayout;
    TextView toolbarTitle;
    ImageView recyclerViewChanger, notificationIcon;
    View imageRelativeLayout, mainHolderMain;
    ViewPagerAdapter adapter;
    public static boolean runOnce;
    public static boolean isSwitchView = false;
    public static int LIST_ITEM = 1;
    public static int GRID_ITEM = 0;
    public static boolean isSwitchView2 = true;
    public static int LIST_ITEM2 = 1;
    public static int GRID_ITEM2 = 0;
    int flag = 0;
    TabLayout.Tab currentTab;
    boolean doubleBackToExitPressedOnce = false;
    public static BookItems currentBookItems = null;
    FirebaseFirestore firebaseFirestore;
    ListenerRegistration listenerRegistration;
    public static boolean isActivityTab = false;
    public CircleImageView profilePicture;
    View notificationListView, searchSelectionView, profilePicView, requestIndicatorView;
    ImageView searchOptionIcon;
    ListenerRegistration listenerRegistration_RequestCounter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Utils.CheckUserLogin(this);

        setContentView(R.layout.activity__home);
        new PermissionUtils(this).CheckPermission();

        toolbar = findViewById(R.id.activityHomeToolBar);
        toolbarTitle = findViewById(R.id.toolbarTitle);
        imageRelativeLayout = findViewById(R.id.recyclerImageChangerMain);
        recyclerViewChanger = findViewById(R.id.recyclerImageChanger);
        mainHolderMain = findViewById(R.id.mainHolderMain);
        notificationIcon = findViewById(R.id.NotificationIcon);
        profilePicture = findViewById(R.id.profilePicture);

        requestIndicatorView = findViewById(R.id.requestIndicatorView);
        searchOptionIcon = findViewById(R.id.searchOptionIcon);
        notificationListView = findViewById(R.id.NotificationLists);
        searchSelectionView = findViewById(R.id.buttonSearch);
        profilePicView = findViewById(R.id.recyclerProfilePic);

        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            toolbarTitle.setLetterSpacing(.04f);
        }

        viewPager = findViewById(R.id.viewPager);

        adapter = new ViewPagerAdapter(getSupportFragmentManager());
        adapter.addFragment(new Fragment_Library());
        adapter.addFragment(new Fragment_Store_New());
        adapter.addFragment(new Fragment_Activity());
        adapter.addFragment(new Fragment_Menu());

        viewPager.setAdapter(adapter);
        //Cause of It Glitch in Scroll stopped
        viewPager.setOffscreenPageLimit(4);
        tabLayout = findViewById(R.id.tabLayout);
        tabLayout.setupWithViewPager(viewPager);

        TabLayout.Tab tabCall = tabLayout.getTabAt(0);tabCall.setIcon(R.drawable.tab_library);
        TabLayout.Tab tabCall2 = tabLayout.getTabAt(1);tabCall2.setIcon(R.drawable.tab_store);
        TabLayout.Tab tabCall3 = tabLayout.getTabAt(2);tabCall3.setIcon(R.drawable.tab_activity);
        TabLayout.Tab tabCall4 = tabLayout.getTabAt(3);tabCall4.setIcon(R.drawable.tab_drawer);

        SetToolBarMenus(0);
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                //Flag Setter
                currentTab = tab;
                if(tab.getPosition() == 0){flag =0;}else if(tab.getPosition() == 1){flag = 1;}
                if(tab.getPosition() == 0){
                    //Library
                    if(!isSwitchView){recyclerViewChanger.setImageResource(R.drawable.single);}else {recyclerViewChanger.setImageResource(R.drawable.grid);}
                    changeTitleText("Library");
                    SetToolBarMenus(0);
                }
                else if(tab.getPosition() == 1){
                    //Store
                    if(!isSwitchView2){recyclerViewChanger.setImageResource(R.drawable.single);}else{recyclerViewChanger.setImageResource(R.drawable.grid);}
                    changeTitleText("Store");
                    SetToolBarMenus(1);
                }
                else if(tab.getPosition() == 2){
                    //Activity
                    changeTitleText("Activity");
                    SetToolBarMenus(2);
                }
                else if(tab.getPosition() == 3){
                    //Menu
                    changeTitleText("Menu");
                    SetToolBarMenus(3);
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });

        //region ERROR
        /*
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(final TabLayout.Tab tab) {
                currentTab = tab;
                if(tab.getPosition() == 0){
                    if(!isSwitchView){
                        recyclerViewChanger.setImageResource(R.drawable.single);
                    }else {
                        recyclerViewChanger.setImageResource(R.drawable.grid);
                    }
                    changeTitleText("Library");
                    int[] location = new int[2];
                    mainHolderMain.getLocationOnScreen(location);

                    //converting pixel to dp
                    float marginLeft = location[0]/density;

                    if(marginLeft == 66){
                        ObjectAnimator transAnimation= ObjectAnimator.ofFloat(mainHolderMain, "x",0);
                        transAnimation.setDuration(80);//set duration
                        transAnimation.start();
                    }else if(marginLeft == 116){
                        ObjectAnimator transAnimation= ObjectAnimator.ofFloat(mainHolderMain, "x",0);
                        transAnimation.setDuration(80);//set duration
                        transAnimation.start();
                    }
                }
                else if(tab.getPosition() == 1){
                    if(!isSwitchView2){
                        recyclerViewChanger.setImageResource(R.drawable.single);
                    }else{
                        recyclerViewChanger.setImageResource(R.drawable.grid);
                    }
                    changeTitleText("Store");
                    int[] location = new int[2];
                    mainHolderMain.getLocationOnScreen(location);
                    float marginLeft = location[0]/density;

                    if(marginLeft == 66){
                        ObjectAnimator transAnimation= ObjectAnimator.ofFloat(mainHolderMain, "x",0);
                        transAnimation.setDuration(80);//set duration
                        transAnimation.start();
                    }else if(marginLeft == 116){
                        ObjectAnimator transAnimation= ObjectAnimator.ofFloat(mainHolderMain, "x",0);
                        transAnimation.setDuration(80);//set duration
                        transAnimation.start();
                    }
                }
                else if(tab.getPosition() == 2){
                    changeTitleText("Activity");
                    int[] location = new int[2];
                    mainHolderMain.getLocationOnScreen(location);
                    //converting pixel to dp
                    float marginLeft = location[0]/density;
                    float menuMargin = 116 * density;
                    float hideToggleMargin = 50 * density;
                    if(marginLeft == 16) {
                        ObjectAnimator transAnimation= ObjectAnimator.ofFloat(mainHolderMain, "x",0, hideToggleMargin);
                        transAnimation.setDuration(80);//set duration
                        transAnimation.start();
                    }else if(marginLeft == 116){
                        ObjectAnimator transAnimation= ObjectAnimator.ofFloat(mainHolderMain, "x",menuMargin, hideToggleMargin);
                        transAnimation.setDuration(80);//set duration
                        transAnimation.start();
                    }
                }

                else if(tab.getPosition() == 3){
                    changeTitleText("Menu");
                    int[] location = new int[2];
                    mainHolderMain.getLocationOnScreen(location);
                    //converting pixel to dp
                    float marginLeft = location[0]/density;
                    float activityMargin = 66 * density;
                    float hideToggleMargin = 100 * density;
                    if(marginLeft == 16) {
                        ObjectAnimator transAnimation= ObjectAnimator.ofFloat(mainHolderMain, "x",0, hideToggleMargin);
                        transAnimation.setDuration(80);//set duration
                        transAnimation.start();
                    }else if(marginLeft == 66){
                        ObjectAnimator transAnimation= ObjectAnimator.ofFloat(mainHolderMain, "x",activityMargin, hideToggleMargin);
                        transAnimation.setDuration(80);//set duration
                        transAnimation.start();
                    }
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });
        */
        //endregion
    }

    @Override
    protected void onResume() {
        super.onResume();
        RequestCounter();
        checkForNotificationState();
        if(isActivityTab){isActivityTab = false;tabLayout.getTabAt(2).select();}
        LoadPersonalDetailsProfilePic();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if(listenerRegistration_RequestCounter != null){listenerRegistration_RequestCounter.remove();listenerRegistration_RequestCounter = null;}
    }

    public void onActivityMainClick(View v){
        if(v.getId() == R.id.buttonSearch){
            //region Button Search
            if(currentTab == null || currentTab.getPosition() == 0){
                //Library
                Intent i = new Intent(Activity_Home.this,Activity_SearchPage.class);
                i.putExtra("SearchType", Activity_SearchPage.SearchType.LIBRARY.toString());
                i.putExtra("tag","Library");
                i.putExtra("queryhint","Search in Library");
                i.putExtra("isKeyboardFocus",true);
                startActivity(i);
            }
            else if(currentTab.getPosition() == 1){
                //Store
                Intent i = new Intent(Activity_Home.this,Activity_SearchStore.class);
                startActivity(i);
            }
            else if(currentTab.getPosition() == 2){
                //Search Users
                Intent i = new Intent(this,Activity_SearchPage.class);
                i.putExtra("SearchType", Activity_SearchPage.SearchType.SEARCHFRIENDS.toString());
                i.putExtra("tag","Friend");
                i.putExtra("queryhint","Search Friend");
                i.putExtra("isKeyboardFocus",true);
                startActivity(i);
            }
            //endregion
        }
        else if(v.getId() == R.id.buttonRecentlyViewedActivityhome){
            //region Recently View Activity Home
            if(currentBookItems != null){
                Utils.OpenBook(this, currentBookItems);
            }
            else{
                BookItems bookItems = Utils.GetCurrentBookFromSqlite(this);
                if(bookItems != null){
                    Utils.OpenBook(this, bookItems);
                }
                else{
                    Toast.makeText(this, "No book in current list", Toast.LENGTH_SHORT).show();
                }
            }
            //endregion
        }
    }

    public void onClickRecyclerLayoutChange(View v){
        if(v.getId() == R.id.recyclerImageChangerMain){
            try {
                if (flag == 0) { //Library
                    Fragment fragment = getSupportFragmentManager().findFragmentByTag("android:switcher:" + R.id.viewPager + ":" + viewPager.getCurrentItem());
                    if (fragment == adapter.getItem(0)) {
                        boolean isSwitched = ((Fragment_Library) fragment).getItemViewTypeFromRecyclerView();
                        if (!isSwitched) {
                            ((Fragment_Library) fragment).changeRecyclerToGrid();
                            recyclerViewChanger.setImageResource(R.drawable.single);
                        } else {
                            ((Fragment_Library) fragment).changerRecyclerToSingle();
                            recyclerViewChanger.setImageResource(R.drawable.grid);

                        }
                    }
                } else if (flag == 1) { //Store
                    Fragment fragment = getSupportFragmentManager().findFragmentByTag("android:switcher:" + R.id.viewPager + ":" + viewPager.getCurrentItem());
                    if (fragment == adapter.getItem(1)) {
                        boolean isSwitched2 = ((Fragment_Store_New) fragment).getItemViewTypeFromRecyclerView();
                        if (!isSwitched2) {
                            ((Fragment_Store_New) fragment).changeRecyclerToGrid();
                            recyclerViewChanger.setImageResource(R.drawable.single);
                        } else {
                            ((Fragment_Store_New) fragment).changerRecyclerToSingle();
                            recyclerViewChanger.setImageResource(R.drawable.grid);

                        }
                    }
                }
            }
            catch (Exception ex){
                Log.e(TAG, ex.toString());
            }

        }

    }

    public void changeTitleText(String title){
        toolbarTitle.setText(title);
    }

    public void openNotificationMain(View view){
        startActivity(new Intent(this, Notification_Home.class));
        overridePendingTransition(R.anim.slide_in_right,R.anim.slide_out_left);
    }

    public void checkForNotificationState(){
        try{
            if(listenerRegistration != null){listenerRegistration.remove();listenerRegistration = null;}
            firebaseFirestore = FirebaseFirestore.getInstance();

            //region no internet connection show
            NetworkManagerUtils.LoadServerData(this);
            //endregion

            String UID = FirebaseUtils.firebaseAuth.getCurrentUser().getUid();
            String Root = getString(R.string.FIRESTORE_ROOT_USERDETAILS) + "/" + UID + "/" + getString(R.string.FIRESTORE_ROOT_NOTIFICATIONS);
            Query firstQuery = firebaseFirestore.collection(Root).whereEqualTo(getString(R.string.F_NOTIFICATIONS_READ), "false").orderBy(getString(R.string.F_NOTIFICATIONS_POSTDATE), Query.Direction.DESCENDING);
            listenerRegistration = firstQuery.addSnapshotListener(this, new EventListener<QuerySnapshot>() {
                @Override
                public void onEvent(QuerySnapshot queryDocumentSnapshots, FirebaseFirestoreException e) {
                    if(queryDocumentSnapshots != null && !queryDocumentSnapshots.isEmpty()){
                        boolean isDataFound = false;
                        for(DocumentChange doc: queryDocumentSnapshots.getDocumentChanges()){
                            if(doc.getType() == DocumentChange.Type.ADDED){
                                isDataFound = true;
                            }
                        }
                        if(isDataFound){
                            notificationIcon.setImageResource(R.drawable.notification_2);
                        }
                        else{
                            notificationIcon.setImageResource(R.drawable.notification_1);
                        }
                    }
                    else{
                        if(e != null){Log.e(TAG, e.getMessage());}
                        notificationIcon.setImageResource(R.drawable.notification_1);
                    }
                }
            });
        }
        catch (Exception ex){
            Log.e(TAG, ex.toString());
        }
    }

    public void openProfile(View view){
        Intent intent = new Intent(this, Activity_UserProfile.class);
        startActivity(intent);
    }

    @Override
    public void onBackPressed() {
        if (doubleBackToExitPressedOnce) {
            super.onBackPressed();
            return;
        }
        this.doubleBackToExitPressedOnce = true;
        Toast.makeText(this, "please click back again to exit", Toast.LENGTH_SHORT).show();
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                doubleBackToExitPressedOnce=false;
            }
        }, 2000);
    }

    private void LoadPersonalDetailsProfilePic(){
        //Search For UID in database
        final String UID = FirebaseUtils.firebaseAuth.getCurrentUser().getUid();
        String Root = getString(R.string.FIRESTORE_ROOT_USERDETAILS) + "/" + UID;
        FirebaseUtils.FirestoreGetData(Root, new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                if (documentSnapshot.exists()) {
                    try {
                        String UserProfileLink = "";try {UserProfileLink = documentSnapshot.getString(getString(R.string.F_USERDETAILS_PROFILEURL));} catch (Exception ex) {Log.e(TAG, ex.toString());}
                        ColorDrawable colorDrawableInfoPlaceholder = new ColorDrawable(ContextCompat.getColor(Activity_Home.this, R.color.colorDrawableInfoPlaceholder));
                        ColorDrawable colorDrawableErrorPlaceholder = new ColorDrawable(ContextCompat.getColor(Activity_Home.this, R.color.colorDrawableErrorPlaceholder));
                        Utils.SetImageViaUri(Activity_Home.this, UID, UserProfileLink, profilePicture, false, false, colorDrawableInfoPlaceholder, colorDrawableErrorPlaceholder, null);
                    }
                    catch (Exception ex) {
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

    private void SetToolBarMenus(int pos){
        if(pos == 0){
            //Library
            //Toolbar Options
            notificationListView.setVisibility(View.GONE); //Notification List
            searchSelectionView.setVisibility(View.VISIBLE); //Search Selection
            profilePicView.setVisibility(View.GONE); //Profile Pic
            imageRelativeLayout.setVisibility(View.VISIBLE); //Grid List
            searchOptionIcon.setImageResource(R.drawable.search_icon);
        }
        else if(pos == 1){
            //Store
            //Toolbar Options
            notificationListView.setVisibility(View.GONE); //Notification List
            searchSelectionView.setVisibility(View.VISIBLE); //Search Selection
            profilePicView.setVisibility(View.GONE); //Profile Pic
            imageRelativeLayout.setVisibility(View.VISIBLE); //Grid List
            searchOptionIcon.setImageResource(R.drawable.search_icon);
        }
        else if(pos == 2){
            //Activity
            //Toolbar Options
            notificationListView.setVisibility(View.VISIBLE); //Notification List
            searchSelectionView.setVisibility(View.VISIBLE); //Search Selection
            profilePicView.setVisibility(View.VISIBLE); //Profile Pic
            imageRelativeLayout.setVisibility(View.GONE); //Grid List
            searchOptionIcon.setImageResource(R.drawable.add_people);
        }
        else if(pos == 3){
            //Menu
            //Toolbar Options
            notificationListView.setVisibility(View.GONE); //Notification List
            searchSelectionView.setVisibility(View.GONE); //Search Selection
            profilePicView.setVisibility(View.GONE); //Profile Pic
            imageRelativeLayout.setVisibility(View.GONE); //Grid List
            searchOptionIcon.setImageResource(R.drawable.search_icon);
        }
    }

    private void RequestCounter(){
        try{
            requestIndicatorView.setVisibility(View.GONE);
            String MYID = FirebaseUtils.firebaseAuth.getCurrentUser().getUid();
            String Root = getString(R.string.FIRESTORE_ROOT_USERDETAILS) + "/" + MYID + "/" +getString(R.string.FIRESTORE_ROOT_FOLLOWER);
            CollectionReference collectionReference = FirebaseFirestore.getInstance().collection(Root);
            Query query = collectionReference.whereEqualTo(getString(R.string.F_USERDETAILS_FOLLOWER_STATE), Activity_Friend_Profile.FollowState.REQUEST.toString());
            listenerRegistration_RequestCounter = FirebaseUtils.FirestoreGetAllData(query, this, new EventListener<QuerySnapshot>() {
                @Override
                public void onEvent(QuerySnapshot queryDocumentSnapshots, FirebaseFirestoreException e) {
                    if(!queryDocumentSnapshots.isEmpty()){
                        int size = 0;
                        for(DocumentChange doc : queryDocumentSnapshots.getDocumentChanges()) {
                            if(doc.getType() == DocumentChange.Type.ADDED) {size += 1;}
                        }
                        if(size >0){
                            //-- Show Badge-->
                            Log.e(TAG, "SHOW BADGE");
                            requestIndicatorView.setVisibility(View.VISIBLE);
                        }
                        else{
                            //-- Don't Show Badge-->
                            Log.e(TAG, "DONT SHOW BADGE");
                            requestIndicatorView.setVisibility(View.GONE);
                        }
                    }
                }
            });
        }
        catch (Exception ex){
            Log.e(TAG, ex.toString());
        }
    }
}