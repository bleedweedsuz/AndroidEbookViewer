package zkagazapahtnajusz.paperproject.com.paperproject;

import android.animation.AnimatorSet;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.functions.HttpsCallableResult;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;
import zkagazapahtnajusz.paperproject.com.paperproject.Fragments.Fragment_Activity;
import zkagazapahtnajusz.paperproject.com.paperproject.Fragments.Fragment_UserProfile_Post;
import zkagazapahtnajusz.paperproject.com.paperproject.Model.BookItems;
import zkagazapahtnajusz.paperproject.com.paperproject.Model.Post;
import zkagazapahtnajusz.paperproject.com.paperproject.Model.SpinnerData;
import zkagazapahtnajusz.paperproject.com.paperproject.Utilities.FirebaseUtils;
import zkagazapahtnajusz.paperproject.com.paperproject.Utilities.Utils;

public class Activity_UserPost extends AppCompatActivity implements View.OnFocusChangeListener{
    private static final String TAG = "ACTIVITY USER POST";

    CircleImageView profileImage;
    TextView UserFullName;
    private EditText NewPostBox;
    Toolbar toolbar;
    private Spinner spinner;
    View buttonSharePost;
    private ArrayList<SpinnerData> mList = new ArrayList<>();
    ProgressBar progressPost;
    String BookShareID = "";
    boolean isBookRefrence = false;
    boolean bAnimation = true;
    boolean isEdit = false;

    String edit_bookID, edit_postID, edit_text, edit_privacy;
    BookItems.StorageType storageType = null;
    String sF  = "";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_post);
        progressPost = findViewById(R.id.progressPostTextCheck);
        toolbar = findViewById(R.id.toolBarUserPost);
        buttonSharePost = findViewById(R.id.buttonSharePost);
        spinner = findViewById(R.id.spinner);
        NewPostBox = findViewById(R.id.NewPostBox);
        NewPostBox.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                float progress = s.length();
                progressPost.setProgress((int) progress);
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        profileImage = findViewById(R.id.profileImage);
        UserFullName = findViewById(R.id.UserFullName);
        setSupportActionBar(toolbar);
        if(getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        //Adding Images to Spinner
        mList.add(new SpinnerData("Public   ",R.drawable.icon_public));
        mList.add(new SpinnerData("Followers  ",R.drawable.icon_follower));
        mList.add(new SpinnerData("Only me  ",R.drawable.icon_privacy));

        SpinnerAdapter adapter = new SpinnerAdapter(this,R.layout.custom_spinner,R.id.spinnerText,mList);
        spinner.setAdapter(adapter);

        //Load Profile Picture
        LoadProfileImage();

        //Load Text From Share Function
        LoadShareData();

        //Set User Full Name
        String fullname = FirebaseUtils.firebaseAuth.getCurrentUser().getDisplayName();
        if(fullname != null) {
            UserFullName.setText(fullname.toString());
        }

        CheckIsEditMode();
    }

    private void CheckIsEditMode(){
        try {
            if (getIntent().getExtras().containsKey("isEdit")) {
                isEdit = getIntent().getExtras().getBoolean("isEdit");
                sF = getIntent().getExtras().getString("SkinFor");
                String[] editContents = getIntent().getExtras().getStringArray("editContents");//postid, bookid, privacy, text
                edit_postID = editContents[0];
                edit_bookID = editContents[1];//can be blank here
                edit_privacy = editContents[2];
                edit_text = editContents[3];

                if(!edit_bookID.equals("")){
                    isBookRefrence = true;
                    BookShareID = edit_bookID;
                    LoadShareBook();
                }
                ((TextView)findViewById(R.id.postText)).setText("Edit Post");
                if(edit_privacy.equals("Public")){
                    spinner.setSelection(0);
                }
                else if(edit_privacy.equals("Followers")){
                    spinner.setSelection(1);
                }
                else if(edit_privacy.equals("Only me")){
                    spinner.setSelection(2);
                }
                NewPostBox.setText(edit_text);

            }
        }
        catch (Exception ex){
            Log.e(TAG, ex.toString());
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
                                    Activity_UserPost.this,
                                    UserProfileLink,
                                    profileImage,
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

    private void LoadShareData(){
        try {
            Bundle bundle = getIntent().getExtras();
            if (bundle.containsKey("ShareText")) {
                String ShareText = bundle.getString("ShareText").trim();
                BookShareID = bundle.getString("BookShareID");
                storageType = BookItems.StorageType.valueOf(bundle.getString("storeType"));

                Log.e(TAG, storageType.toString());

                isBookRefrence = true;
                if(ShareText.length() >=498){
                    ShareText = ShareText.substring(0, 496) + "..";
                }

                NewPostBox.setText(ShareText);

                //Load Share Book
                if(storageType == BookItems.StorageType.CLOUD) {
                    LoadShareBook();
                }
                else {
                    isBookRefrence = false;
                    BookShareID = "";
                }
            }
        }
        catch (Exception ex){
            Log.e(TAG, ex.getMessage());
        }
    }

    private void LoadShareBook(){
        try{
            String Root = getString(R.string.FIRESTORE_ROOT_BOOKS) + "/" + BookShareID;
            FirebaseUtils.FirestoreGetData(Root, new OnSuccessListener<DocumentSnapshot>() {
                @Override
                public void onSuccess(DocumentSnapshot documentSnapshot) {
                    if(documentSnapshot.exists()) {
                        final RelativeLayout refrenceBookContainer = findViewById(R.id.refrenceBook);
                        refrenceBookContainer.setVisibility(View.VISIBLE);
                        View view = LayoutInflater.from(Activity_UserPost.this).inflate(R.layout.activity_userpost_book_refrence, null, false);

                        //region Set Image Data
                        ImageView imageView = view.findViewById(R.id.recyclerItemImage);
                        String imageURL = documentSnapshot.getString(getString(R.string.F_BOOKS_IMAGEURL));
                        ColorDrawable colorDrawableInfoPlaceholder = new ColorDrawable(ContextCompat.getColor(Activity_UserPost.this, R.color.colorDrawableInfoPlaceholder));
                        ColorDrawable colorDrawableErrorPlaceholder = new ColorDrawable(ContextCompat.getColor(Activity_UserPost.this, R.color.colorDrawableErrorPlaceholder));
                        Utils.SetImageViaUri(Activity_UserPost.this,imageURL,imageView,false,false,colorDrawableInfoPlaceholder,colorDrawableErrorPlaceholder,null);
                        //endregion

                        //region SetText
                        TextView tName = view.findViewById(R.id.recyclerItemName);
                        TextView tDescription = view.findViewById(R.id.recyclerItemDescription);

                        tName.setText(documentSnapshot.getString(getString(R.string.F_BOOKS_TITLE)));
                        tDescription.setText(documentSnapshot.getString(getString(R.string.F_BOOKS_DESCRIPTION)));
                        //endregion
                        refrenceBookContainer.addView(view);

                        //ChangeAnimation view
                        changeAnimation(refrenceBookContainer);

                        //OnClick Animation
                        refrenceBookContainer.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {changeAnimation(refrenceBookContainer);}
                        });

                    }
                }
            }, null);
        }
        catch (Exception ex){
            Log.e(TAG, ex.getMessage());
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if(item.getItemId() == android.R.id.home){
            finish();
        }

        return super.onOptionsItemSelected(item);
    }

    public void Post_onClick(View view){
        if(!isEdit){
            Post(Utils.UUIDToken(), false);
        }
        else{
            Post(edit_postID, true);
        }
    }

    public void Post(String PostID, boolean editedState){
        try{
            Utils.SetProgressDialogIndeterminate(this, "processing");
            //final String PostID = Utils.UUIDToken();
            final String UID = FirebaseUtils.firebaseAuth.getCurrentUser().getUid();
            final Timestamp postTimeStamp = new Timestamp(Calendar.getInstance().getTime());
            final String postDescription = NewPostBox.getText().toString().trim();
            final String privacy = mList.get(spinner.getSelectedItemPosition()).getText().trim();
            final String postType;
            if(!isBookRefrence){
                postType = Post.PostType.Simple.toString();
            }
            else{
                postType = Post.PostType.BookReference.toString();
            }
            final String postBookrefrence = BookShareID;

            if(postType.equals("")){Toast.makeText(this, "Post is empty", Toast.LENGTH_SHORT).show();return;}
            // First Part
            //region Set Data To Posts [User Post]
            {
                String Root = getString(R.string.FIRESTORE_ROOT_USERDETAILS) + "/" + UID + "/" + getString(R.string.FIRESTORE_ROOT_POSTS) + "/" + PostID;
                Map<String, Object> map = new HashMap<>();
                map.put(getString(R.string.F_POSTS_POSTID), PostID);
                map.put(getString(R.string.F_POSTS_POST_UID), UID);
                if(!editedState) {
                    map.put(getString(R.string.F_POSTS_POST_DATE), postTimeStamp);
                }
                map.put(getString(R.string.F_POSTS_POST_TYPE), postType);
                map.put(getString(R.string.F_POSTS_POST_POSTEDITED), editedState);
                FirebaseUtils.FirestroreSetData(
                        Root,
                        map,
                        new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                Log.i(TAG, "POST SUCCESS Part 1");
                            }
                        },
                        new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Utils.UnSetProgressDialogIndeterminate();
                                Log.e(TAG, e.toString());
                            }
                        });
            }
            //endregion

            //Second Part
            //region Set Data To Posts [MAIN POST]
            {
                String Root = getString(R.string.FIRESTORE_ROOT_POSTS) + "/" + PostID;
                Map<String, Object> map = new HashMap<>();
                map.put(getString(R.string.F_POSTS_POSTID), PostID);
                map.put(getString(R.string.F_POSTS_POST_UID), UID);
                map.put(getString(R.string.F_POSTS_POST_TYPE), postType);
                map.put(getString(R.string.F_POSTS_POST_DESCRIPTION), postDescription);
                map.put(getString(R.string.F_POSTS_POST_BOOK_REFRENCE), postBookrefrence);
                map.put(getString(R.string.F_POSTS_POST_PRIVACY), privacy);
                if(!editedState) {
                    map.put(getString(R.string.F_POSTS_POST_DATE), postTimeStamp);
                }
                map.put(getString(R.string.F_POSTS_POST_POSTEDITED), editedState);
                FirebaseUtils.FirestroreSetData(
                        Root,
                        map,
                        new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                Utils.UnSetProgressDialogIndeterminate();
                                finish();
                                if(sF.equals("UserProfilePost")){
                                    Fragment_UserProfile_Post.needToReload = true;
                                }
                                Fragment_Activity.isReLoadData = true;
                                Toast.makeText(Activity_UserPost.this, "New Post Added", Toast.LENGTH_SHORT).show();
                            }
                        },
                        new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Utils.UnSetProgressDialogIndeterminate();
                                Log.e(TAG, e.toString());
                            }
                        });
            }
            //endregion

            //Sharing Policy
            if(postType.equals("Only me")){return;}
            if(isEdit){return;}

            //Third Part//
            //region Call Server Function
            Map<String, Object> postMap = new HashMap<>();
            postMap.put("postid", PostID);
            postMap.put("uid", UID);
            postMap.put("description", postDescription);
            postMap.put("displayname", FirebaseUtils.firebaseAuth.getCurrentUser().getDisplayName());
            if(!editedState) {postMap.put("date", postTimeStamp.toDate().toString());}
            postMap.put("posttype", postType);
            postMap.put("bookid", BookShareID);
            postMap.put("notificationtype", "postnew");
            postMap.put("postedited", editedState);

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
        catch (Exception ex){
            Log.e(TAG, ex.toString());
        }
    }

    @Override
    public void onFocusChange(View v, boolean hasFocus) {
        if(NewPostBox.getText().toString().trim().length() <=0){
            if(NewPostBox.requestFocus()){
                getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
            }
        }
    }

    public void changeAnimation(final View view){
        if(bAnimation){
            bAnimation = false;
            int currentHeight = Utils.px2Dp(85,getResources());
            int newHeight = Utils.px2Dp(20,getResources());
            ValueAnimator slideAnimator = ValueAnimator.ofInt(currentHeight, newHeight).setDuration(300);

            slideAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    Integer value = (Integer) animation.getAnimatedValue();
                    view.getLayoutParams().height = value.intValue();
                    view.requestLayout();
                }
            });

            AnimatorSet set = new AnimatorSet();
            set.play(slideAnimator);
            set.setInterpolator(new AccelerateDecelerateInterpolator());
            set.start();
        }
        else{
            bAnimation = true;
            int currentHeight = Utils.px2Dp(0,getResources());
            int newHeight = Utils.px2Dp(85,getResources());
            ValueAnimator slideAnimator = ValueAnimator.ofInt(currentHeight, newHeight).setDuration(300);

            slideAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    Integer value = (Integer) animation.getAnimatedValue();
                    view.getLayoutParams().height = value.intValue();
                    view.requestLayout();
                }
            });

            AnimatorSet set = new AnimatorSet();
            set.play(slideAnimator);
            set.setInterpolator(new AccelerateDecelerateInterpolator());
            set.start();
        }
    }
}

class SpinnerAdapter extends ArrayAdapter<SpinnerData>{
    int groupId;
    Context mContext;
    ArrayList<SpinnerData> mList;
    LayoutInflater inflater;

    public SpinnerAdapter(Context context, int groupId, int id, ArrayList<SpinnerData> mList){
        super(context,id,mList);
        this.mList = mList;
        inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        this.groupId = groupId;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        View itemView = inflater.inflate(groupId, parent, false);
        ImageView imageView = itemView.findViewById(R.id.spinnerImage);
        imageView.setImageResource(mList.get(position).getImage());
        TextView textView = itemView.findViewById(R.id.spinnerText);
        textView.setText(mList.get(position).getText());
        return itemView;
    }

    @Override
    public View getDropDownView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        return getView(position,convertView,parent);
    }
}
