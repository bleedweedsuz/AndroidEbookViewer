package zkagazapahtnajusz.paperproject.com.paperproject;

import android.app.DatePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.InputType;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FacebookAuthProvider;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.firestore.DocumentSnapshot;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;
import zkagazapahtnajusz.paperproject.com.paperproject.Fragments.Fragment_UserProfile_Post;
import zkagazapahtnajusz.paperproject.com.paperproject.Utilities.FirebaseUtils;
import zkagazapahtnajusz.paperproject.com.paperproject.Utilities.Utils;

/**
 * Created by Zw4R-TSTUD10 2018/4/1 [2018, April 1]
 */

public class Activity_UserProfile_Setting extends AppCompatActivity {

    private String TAG = "Activity User Profile Setting";
    CircleImageView imageProfile;
    ImageView imageBackground;
    Toolbar toolbar;
    TextView Fullname_TextView, Email_TextView, PhoneNo_TextView, Gender_TextView, Birthday_TextView;
    String BIO_STR = "", B_Date_Str = "";
    private boolean cropImage;
    private static final int SELECTED_PICTURE = 11;
    private static final int SELECTED_PICTURE2 = 12;
    //boolean canChangePassword = false;

    public static boolean isProfileImageChange_Activity = false;
    public static boolean isProfileImageChange_Activity_UserPost = false;

    ImageView providerIcon;

    boolean isEmailChange = true;
    boolean isPhoneChange = true;
    boolean isPasswordChange = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity__user_profile__setting);
        toolbar = findViewById(R.id.toolbarUserProfileSetting);
        imageProfile = findViewById(R.id.accountProfile);
        imageBackground = findViewById(R.id.accountProfileBackground);

        Fullname_TextView = findViewById(R.id.FullName);
        Email_TextView = findViewById(R.id.Email);
        PhoneNo_TextView = findViewById(R.id.PhoneNo);
        Gender_TextView = findViewById(R.id.Gender);
        Birthday_TextView = findViewById(R.id.Birthday);

        providerIcon = findViewById(R.id.providerIcon);

        setSupportActionBar(toolbar);
        if(getSupportActionBar() != null){
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);

            Drawable upArrow = ContextCompat.getDrawable(this,R.drawable.abc_ic_ab_back_material);
            upArrow.setColorFilter((Color.BLACK), PorterDuff.Mode.SRC_ATOP);
            getSupportActionBar().setHomeAsUpIndicator(upArrow);
        }

        //Set Firebase Auth
        LoadUserDetails();
    }

    private void LoadUserDetails(){
        //region Set Email Provider
        if(FirebaseUtils.firebaseAuth != null && FirebaseUtils.firebaseAuth.getCurrentUser() != null){
            try{
                String provider = FirebaseUtils.firebaseAuth.getCurrentUser().getProviders().get(0);
                if(provider.equals(FacebookAuthProvider.PROVIDER_ID)){
                    providerIcon.setImageResource(R.drawable.icon_fb);
                    providerIcon.setColorFilter(R.color.com_facebook_blue);
                    isEmailChange = false;
                    isPhoneChange = true;
                    isPasswordChange = false;
                }
                else if(provider.equals(GoogleAuthProvider.PROVIDER_ID)){
                    providerIcon.setImageResource(R.drawable.icon_google_32);
                    isEmailChange = false;
                    isPhoneChange = true;
                    isPasswordChange = false;
                }
                else if(provider.equals(PhoneAuthProvider.PROVIDER_ID)){
                    providerIcon.setImageResource(R.drawable.icon_phone);
                    providerIcon.setColorFilter(Color.parseColor("#2d9448"));
                    isEmailChange = true;
                    isPhoneChange = false;
                    isPasswordChange = false;

                    //Set Phone No.
                    PhoneNo_TextView.setText(FirebaseUtils.firebaseAuth.getCurrentUser().getPhoneNumber());
                }
                else if(provider.equals(EmailAuthProvider.PROVIDER_ID)){
                    providerIcon.setImageResource(R.drawable.icon_email);
                    isEmailChange = true;
                    isPhoneChange = true;
                    isPasswordChange = true;
                }
            }
            catch (Exception ex){
                Log.e(TAG, "ERROR_1:"+ ex.toString());
            }
        }
        //endregion

        //region Set Full Name
        if(FirebaseUtils.firebaseAuth !=null && FirebaseUtils.firebaseAuth.getCurrentUser().getDisplayName() != null){
            String str = FirebaseUtils.firebaseAuth.getCurrentUser().getDisplayName();
            if(str != ""){Fullname_TextView.setText(str);}
        }
        //endregion

        //region Set Email
        if(FirebaseUtils.firebaseAuth !=null && FirebaseUtils.firebaseAuth.getCurrentUser().getEmail() != null){
            String str = FirebaseUtils.firebaseAuth.getCurrentUser().getEmail();
            if(str != ""){Email_TextView.setText(str);}
        }
        //endregion

        //region Set Gender, Phone No, Birthday, Bio
        {
            if(FirebaseUtils.firebaseAuth !=null && FirebaseUtils.firebaseAuth.getCurrentUser() !=null) {
                String UID = FirebaseUtils.firebaseAuth.getCurrentUser().getUid();
                String Root = getString(R.string.FIRESTORE_ROOT_USERDETAILS) + "/" + UID;
                FirebaseUtils.FirestoreGetData(Root, new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        if(documentSnapshot.exists()){
                            String pNo = "", gengerStr= "";
                            if(isPhoneChange) {
                                try {
                                    pNo = documentSnapshot.getString(getString(R.string.F_USERDETAILS_PHONE));
                                } catch (Exception ex) {
                                    Log.e(TAG, ex.toString());
                                }
                                if(pNo != null && !pNo.trim().equals("")){PhoneNo_TextView.setText(pNo);}
                            }
                            try{ gengerStr = documentSnapshot.getString(getString(R.string.F_USERDETAILS_GENDER)); } catch (Exception ex){Log.e(TAG, ex.toString());}
                            try{ B_Date_Str = documentSnapshot.get(getString(R.string.F_USERDETAILS_BIRTHDAY)).toString(); String fDate = new SimpleDateFormat("MMM dd, yyyy", Locale.US).format(new Date(B_Date_Str));Birthday_TextView.setText(fDate);} catch (Exception ex){Log.e(TAG, ex.toString());}
                            try{ BIO_STR = documentSnapshot.getString(getString(R.string.F_USERDETAILS_BIO)); } catch (Exception ex){Log.e(TAG, ex.toString());}

                            if(gengerStr != null && !gengerStr.trim().equals("")){Gender_TextView.setText(gengerStr);}
                        }
                    }
                }, null);
            }
        }
        //endregion

        //Load Profile Images
        LoadProfileImages();
    }

    private void LoadProfileImages(){
        try {
            String UID = FirebaseUtils.firebaseAuth.getCurrentUser().getUid();
            String Root = getString(R.string.FIRESTORE_ROOT_USERDETAILS) + "/" + UID;
            FirebaseUtils.FirestoreGetData(Root, new OnSuccessListener<DocumentSnapshot>() {
                @Override
                public void onSuccess(DocumentSnapshot documentSnapshot) {
                    if (documentSnapshot.exists()) {
                        try {
                            String UserProfileLink = ""; try{ UserProfileLink = documentSnapshot.getString(getString(R.string.F_USERDETAILS_PROFILEURL));} catch (Exception ex){Log.e(TAG, ex.toString());}
                            String UserBackgroundProfileLink = ""; try { UserBackgroundProfileLink = documentSnapshot.getString(getString(R.string.F_USERDETAILS_BACKGROUNDURL));} catch (Exception ex){Log.e(TAG, ex.toString());}

                            ColorDrawable colorDrawableInfoPlaceholder = new ColorDrawable(ContextCompat.getColor(getApplicationContext(), R.color.colorDrawableInfoPlaceholder));
                            ColorDrawable colorDrawableErrorPlaceholder = new ColorDrawable(ContextCompat.getColor(getApplicationContext(), R.color.colorDrawableErrorPlaceholder));

                            Utils.SetImageViaUri(Activity_UserProfile_Setting.this,UserProfileLink, imageProfile,false,false, colorDrawableInfoPlaceholder, colorDrawableErrorPlaceholder,null);
                            Utils.SetImageViaUri(Activity_UserProfile_Setting.this,UserBackgroundProfileLink, imageBackground,false,false, colorDrawableInfoPlaceholder, colorDrawableErrorPlaceholder,null);
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
    public boolean onOptionsItemSelected(MenuItem item) {

        if(item.getItemId() == android.R.id.home){
            finish();
        }
        return super.onOptionsItemSelected(item);
    }

    public void onUserProfileSettingClick(View v){
        if(v.getId() == R.id.buttonProfilePictureChange){
            ProfilePictureChange();
        }
        else if(v.getId() == R.id.buttonProfileBackgroundPictureChange){
            ProfileBackgroundChange();
        }
    }

    private void ProfilePictureChange(){
        try {
            cropImage = true;
            OpenGallery(SELECTED_PICTURE);
        }
        catch (Exception ex){
            Log.e(TAG, ex.toString());
        }
    }

    private void ProfileBackgroundChange(){
        try {
            cropImage = false;
            OpenGallery(SELECTED_PICTURE2);
        }
        catch (Exception ex){
            Log.e(TAG, ex.toString());
        }
    }

    private void OpenGallery(int requestCode){
        String manufacturer_Type =  android.os.Build.MANUFACTURER;
        if(manufacturer_Type.equalsIgnoreCase("samsung")){
            Intent i = new Intent(Intent.ACTION_GET_CONTENT).setType("image/*");
            startActivityForResult(i, requestCode);
        }
        else{
            Intent i = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            i.setType("image/*");
            startActivityForResult(i, requestCode);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        //region Enter Image Request Mode
        if(requestCode == SELECTED_PICTURE){
            if(resultCode == RESULT_OK){
                Uri uri = data.getData();
                CropImage.activity(uri).setCropShape(CropImageView.CropShape.OVAL).setAspectRatio(2,2)
                        .start(this);
            }
        }
        if(requestCode == SELECTED_PICTURE2){
            if(resultCode == RESULT_OK){
                Uri uri = data.getData();
                CropImage.activity(uri)
                        .setCropShape(CropImageView.CropShape.RECTANGLE)
                        .setAspectRatio(9,6)
                        .setMinCropWindowSize(900,600)
                        .start(this);
            }
        }
        //endregion

        //region After Math Request Code
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            if(cropImage){//Profile Image
                CropImage.ActivityResult result = CropImage.getActivityResult(data);
                if (resultCode == RESULT_OK) {
                    //region UPLOAD PROFILE IMAGE
                    Utils.SetProgressDialogIndeterminate(this, "processing..");
                    final Uri resultUri = result.getUri();
                    imageProfile.setImageURI(resultUri);
                    //region Upload Image
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                final Bitmap orginalBitmap = Glide.with(Activity_UserProfile_Setting.this).asBitmap().load(resultUri).submit(900, 600).get();
                                new Handler(Looper.getMainLooper()).post(new Runnable() {
                                    @Override
                                    public void run() {
                                        isProfileImageChange_Activity = true;
                                        isProfileImageChange_Activity_UserPost = true;
                                        FirebaseUtils.uploadProfileImage(Activity_UserProfile_Setting.this, orginalBitmap);
                                        if(Utils.ProfileImage != null){Utils.ProfileImage.setImageBitmap(orginalBitmap);}
                                        Fragment_UserProfile_Post.needToReload = true;
                                        Log.e(TAG, "IMAGE CHANGE PROFILE");
                                        Utils.UnSetProgressDialogIndeterminate();
                                    }
                                });
                            }
                            catch (Exception ex){
                                Utils.UnSetProgressDialogIndeterminate();
                                Log.e(TAG, ex.toString());
                            }
                        }
                    }).start();
                    //endregion
                }
                else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                    Exception error = result.getError();
                    Log.e(TAG, error.toString());
                }
            }
            else{//Profile Background Image
                CropImage.ActivityResult result = CropImage.getActivityResult(data);
                if (resultCode == RESULT_OK) {
                    Utils.SetProgressDialogIndeterminate(this, "processing..");
                    final Uri resultUri = result.getUri();
                    imageBackground.setImageURI(resultUri);
                    //region Upload Image
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                final Bitmap orginalBitmap = Glide.with(Activity_UserProfile_Setting.this).asBitmap().load(resultUri).submit(900, 600).get();
                                new Handler(Looper.getMainLooper()).post(new Runnable() {
                                    @Override
                                    public void run() {
                                        FirebaseUtils.uploadBackgroundImage(Activity_UserProfile_Setting.this, orginalBitmap);
                                        Utils.UnSetProgressDialogIndeterminate();
                                        Log.e(TAG, "IMAGE BACKGROUND PROFILE");
                                    }
                                });
                            }
                            catch (Exception ex){
                                Utils.UnSetProgressDialogIndeterminate();
                                Log.e(TAG, ex.toString());
                            }
                        }
                    }).start();
                    //endregion
                }
                else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                    Exception error = result.getError();
                    Log.e(TAG, error.toString());
                }
            }
        }
        //endregion

    }

    public void UpdateFullName_Click(View view) {
        try{
            DialogInterface.OnClickListener clickListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Utils.SetProgressDialogIndeterminate(Activity_UserProfile_Setting.this, "updating");
                try {
                    EditText editText = ((AlertDialog) dialog).findViewById(R.id.inputEditText);
                    String FullName = editText.getText().toString().trim();

                    //Firebase Auth
                    if (FirebaseUtils.firebaseAuth.getCurrentUser() != null) {
                        FirebaseUtils.SetAuthDisplayName(FirebaseUtils.firebaseAuth, FullName);
                        //Change UI text too
                        Activity_UserProfile_Setting.this.Fullname_TextView.setText(FullName);
                    }

                    String Root = getString(R.string.FIRESTORE_ROOT_USERDETAILS) + "/" + FirebaseUtils.firebaseAuth.getCurrentUser().getUid();
                    Map<String, Object> map = new HashMap<>();
                    map.put(getString(R.string.F_USERDETAILS_USERFULLNAME), FullName);
                    FirebaseUtils.FirestroreSetData(Root, map,
                            new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    Log.i(TAG, "SAVE: DATA");
                                }
                            },
                            new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Log.e(TAG, "ERROR");
                                }
                            });
                    Toast.makeText(Activity_UserProfile_Setting.this, "Data Update", Toast.LENGTH_SHORT).show();
                } catch (Exception ex) {
                    Log.e("ERROR", ex.toString());
                }
                Utils.UnSetProgressDialogIndeterminate();
            }
        };
        String FullName = Fullname_TextView.getText().toString().trim();
        Utils.SetInputDialogBox(this, 1, "Change your full name", "New full name", FullName, InputType.TYPE_CLASS_TEXT, clickListener);
        }
        catch (Exception ex){
            Log.e(TAG, ex.toString());
        }
    }

    public void UpdateEmail_Click(View view){
        Toast.makeText(this, "you can't change email", Toast.LENGTH_SHORT).show();
    }

    public void UpdatePhoneNo_Click(View view){
        try {
            if(!isPhoneChange){
                Toast.makeText(this, "you can't change phone no", Toast.LENGTH_SHORT).show();
                return;
            }

            DialogInterface.OnClickListener clickListener = new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    Utils.SetProgressDialogIndeterminate(Activity_UserProfile_Setting.this, "updating");
                    //Update Phone No.
                    try {
                        EditText editText = ((AlertDialog) dialog).findViewById(R.id.inputEditText);
                        final String phone = editText.getText().toString().trim();

                        PhoneNo_TextView.setText(phone);

                        String Root = getString(R.string.FIRESTORE_ROOT_USERDETAILS) + "/" + FirebaseUtils.firebaseAuth.getCurrentUser().getUid();
                        Map<String, Object> map = new HashMap<>();
                        map.put(getString(R.string.F_USERDETAILS_PHONE), phone);

                        OnSuccessListener<Void> onSuccessListener = new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                Log.i(TAG, "Data Updated Phone");
                            }
                        };
                        OnFailureListener onFailureListener = new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Log.e(TAG, e.toString());
                            }
                        };

                        FirebaseUtils.FirestroreSetData(Root, map, onSuccessListener, onFailureListener);
                    } catch (Exception ex) {
                        Log.e(TAG, ex.toString());
                    }
                    //
                    Utils.UnSetProgressDialogIndeterminate();
                }
            };


            String PhoneNo = PhoneNo_TextView.getText().toString().trim();
            if(PhoneNo.equals("Set Phone No.")){ PhoneNo = "";}

            Utils.SetInputDialogBox(this, 1, "Change your phoneno", "New phone number", PhoneNo, InputType.TYPE_CLASS_PHONE, clickListener);
        }
        catch (Exception ex){
            Log.e(TAG, ex.toString());
        }
    }

    public void UpdateGender_Click(View view){
        try {
            final CharSequence[] picker = new CharSequence[]{"He", "She", "Zie"};
            DialogInterface.OnClickListener clickListener = new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    Utils.SetProgressDialogIndeterminate(Activity_UserProfile_Setting.this, "updating");
                    try {
                        //Update Gender
                        {
                            String Root = getString(R.string.FIRESTORE_ROOT_USERDETAILS) + "/" + FirebaseUtils.firebaseAuth.getCurrentUser().getUid();
                            Map<String, Object> map = new HashMap<>();
                            map.put(getString(R.string.F_USERDETAILS_GENDER), picker[which].toString());

                            OnSuccessListener<Void> onSuccessListener = new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    Log.i(TAG, "Data Updated Gender");
                                }
                            };
                            OnFailureListener onFailureListener = new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Log.e(TAG, e.toString());
                                }
                            };

                            FirebaseUtils.FirestroreSetData(Root, map, onSuccessListener, onFailureListener);
                            Gender_TextView.setText(picker[which].toString());
                        }
                    } catch (Exception ex) {
                        Log.e(TAG, ex.toString());
                    }
                    Utils.UnSetProgressDialogIndeterminate();
                    dialog.dismiss();
                }
            };
            String SelectedCharSequence = Gender_TextView.getText().toString();
            Utils.SetSingleChoiceInputDialogBox(this, "Update your gender", picker, SelectedCharSequence, clickListener);
        }
        catch (Exception ex){
            Log.e(TAG, ex.toString());
        }
    }

    public void UpdateBirthday_Click(View view){
        //try {
            Calendar c = Calendar.getInstance();
            if(!B_Date_Str.equals("")){
                Date d = new Date(B_Date_Str);
                c.setTime(d);
            }
            DatePickerDialog datePickerDialog = new DatePickerDialog(this, new DatePickerDialog.OnDateSetListener() {
                @Override
                public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                    //Update date
                    Calendar calendar = new GregorianCalendar();
                    calendar.set(year, month, dayOfMonth);
                    Timestamp birthdayTimestamp = new Timestamp(calendar.getTime());
                    B_Date_Str = String.valueOf(birthdayTimestamp.toDate().toString());
                    try {
                        Utils.SetProgressDialogIndeterminate(Activity_UserProfile_Setting.this, "updating");
                        //Update Date
                        {
                            String Root = getString(R.string.FIRESTORE_ROOT_USERDETAILS) + "/" + FirebaseUtils.firebaseAuth.getCurrentUser().getUid();
                            Map<String, Object> map = new HashMap<>();
                            map.put(getString(R.string.F_USERDETAILS_BIRTHDAY), birthdayTimestamp);

                            OnSuccessListener<Void> onSuccessListener = new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    Log.i(TAG, "Data Updated Date");
                                }
                            };
                            OnFailureListener onFailureListener = new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Log.e(TAG, e.toString());
                                }
                            };
                            FirebaseUtils.FirestroreSetData(Root, map, onSuccessListener, onFailureListener);

                            String fDate = new SimpleDateFormat("MMM dd, yyyy", Locale.US).format(calendar.getTime());
                            Activity_UserProfile_Setting.this.Birthday_TextView.setText(fDate);
                            Utils.UnSetProgressDialogIndeterminate();
                        }
                    } catch (Exception ex) {
                        Log.e(TAG, ex.toString());
                    }
                }
            },
                    c.get(Calendar.YEAR),
                    c.get(Calendar.MONTH),
                    c.get(Calendar.DAY_OF_MONTH));
            datePickerDialog.show();
        //}
        //catch (Exception ex){
        //    Log.e(TAG, ex.toString());
        //}
    }

    public void UpdatePassword_Click(View view){
        try {
            if(!isPasswordChange){
                Toast.makeText(this, "you can't change this password.", Toast.LENGTH_SHORT).show();
                return;
            }

            Utils.SetPasswordInputDialogBox(this,
                    "Change your password",
                    new Utils.SetPasswordInputDialogBox_interfacecallback(){
                        @Override
                        public void updatePassword(AlertDialog dialog) {
                            Utils.SetProgressDialogIndeterminate(Activity_UserProfile_Setting.this, "updating");
                            UpdatePassword(dialog);
                        }
                    });
        }
        catch (Exception ex){
            Log.e(TAG, ex.toString());
        }
    }

    private void UpdatePassword(AlertDialog dialog){
        try
        {
            String CurrentPassword = ((EditText) (dialog).findViewById(R.id.inputcurrentpasswordEditText)).getText().toString().trim();
            final String NewPassword = ((EditText) (dialog).findViewById(R.id.inputnewpasswordEditText)).getText().toString().trim();
            String RetypePassword = ((EditText) (dialog).findViewById(R.id.inputrepasswordEditText)).getText().toString().trim();

            if(NewPassword.equals(RetypePassword)){
                final FirebaseUser user = FirebaseUtils.firebaseAuth.getCurrentUser();
                AuthCredential credential = EmailAuthProvider.getCredential(FirebaseUtils.firebaseAuth.getCurrentUser().getEmail(), CurrentPassword);
                user.reauthenticate(credential)
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if(task.isSuccessful()){
                                    user.updatePassword(NewPassword).addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if(task.isSuccessful()){
                                                Toast.makeText(Activity_UserProfile_Setting.this, "New password updated", Toast.LENGTH_SHORT).show();
                                            }
                                        }
                                    });
                                }
                                Utils.UnSetProgressDialogIndeterminate();
                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Utils.UnSetProgressDialogIndeterminate();
                                Toast.makeText(Activity_UserProfile_Setting.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        });
            }
            else{
                Utils.UnSetProgressDialogIndeterminate();
                Toast.makeText(Activity_UserProfile_Setting.this, "Re-type password doesnot match", Toast.LENGTH_SHORT).show();
            }

        }
        catch (Exception ex) {
            Utils.UnSetProgressDialogIndeterminate();
            Toast.makeText(Activity_UserProfile_Setting.this, ex.getMessage(), Toast.LENGTH_SHORT).show();
            Log.e("ERROR", ex.toString());
        }
    }

    public void UpdateBio_Click(View view){
        DialogInterface.OnClickListener clickListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                try{
                    Utils.SetProgressDialogIndeterminate(Activity_UserProfile_Setting.this, "updating");
                    //Update Bio
                    {

                        EditText editText = ((AlertDialog) dialog).findViewById(R.id.inputEditText);
                        final String bioStr = editText.getText().toString().trim();

                        String Root = getString(R.string.FIRESTORE_ROOT_USERDETAILS) + "/" + FirebaseUtils.firebaseAuth.getCurrentUser().getUid();
                        Map<String, Object> map = new HashMap<>();
                        map.put(getString(R.string.F_USERDETAILS_BIO), bioStr);

                        OnSuccessListener<Void> onSuccessListener = new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                Log.i(TAG, "Data Updated Bio");
                                BIO_STR = bioStr;
                            }
                        };
                        OnFailureListener onFailureListener = new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Log.e(TAG, e.toString());
                            }
                        };

                        FirebaseUtils.FirestroreSetData(Root, map, onSuccessListener, onFailureListener);
                        //
                        Utils.UnSetProgressDialogIndeterminate();
                        dialog.dismiss();
                    }
                    //
                    Utils.UnSetProgressDialogIndeterminate();
                }
                catch (Exception ex){
                    Log.e(TAG, ex.toString());
                }
            }
        };
        Utils.SetInputDialogBox(this, 5,"Update your bio", "Update bio", BIO_STR, -1, clickListener);
    }
}
