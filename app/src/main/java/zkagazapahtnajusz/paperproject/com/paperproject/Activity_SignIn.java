package zkagazapahtnajusz.paperproject.com.paperproject;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.util.Base64;
import android.util.Log;
import android.util.Patterns;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookAuthorizationException;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.internal.CallbackManagerImpl;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FacebookAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.iid.FirebaseInstanceId;
import com.rilixtech.CountryCodePicker;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

import zkagazapahtnajusz.paperproject.com.paperproject.Utilities.FirebaseUtils;
import zkagazapahtnajusz.paperproject.com.paperproject.Utilities.PermissionUtils;
import zkagazapahtnajusz.paperproject.com.paperproject.Utilities.Utils;

/**
 * Created by Zw4R-TSTUD10 2018/4/1 [2018, April 1]
 */

public class Activity_SignIn extends AppCompatActivity {
    //Tag
    private String TAG = "Activity Sign In";

    int WaitTimeForLogin = 10000;
    private int counterlogin = 0;
    private int maxLogin = 5;

    //Google Sign In Variables
    private static final int Google_RC_SIGNIN = 4901;
    private static int Facebook_RC_SIGNIN = -1;

    TextView textEmail, textPassword, textsignin;
    ImageView showHidePassword;
    EditText editSignInEmailPhone, editSignInPassword;
    boolean hideIcon = true;

    private CallbackManager facebookCallbackManager;

    GoogleApiClient googleApiClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Utils.DEVICE_CHECK(this);//DEVICE CHECK
        new PermissionUtils(this).CheckPermission();//Check Permission
        FirebaseInitialize();//FireBase App Initialize & Instance

        setContentView(R.layout.activity__sign_in);//Setting Layout
        SetUI();//Set UI

        GetPackageHashKey();//Get Package Hash Key
    }

    private void CheckFlag(){
        Bundle bundle = getIntent().getExtras();
        if(bundle !=null && !bundle.isEmpty() && bundle.containsKey("FLAG")){
            String FLAG = bundle.getString("FLAG");
            //region Redirect
            if(FLAG.equals("POST_NEW_ADDED") || FLAG.equals("POST_LIKE")){
                Intent i = new Intent(this, Activity_Comment.class);
                i.putExtra("ID", bundle.getString("POSTID"));
                i.putExtra("commentFrom", Activity_Comment.CommentFrom.Post.toString());
                i.putExtra("isFirstScroll", false);
                i.putExtra("InComingType", "DIRECT");
                startActivity(i);
            }
            else if(FLAG.equals("POST_COMMENT")){
                Intent i = new Intent(this, Activity_Comment.class);
                i.putExtra("ID", bundle.getString("POSTID"));
                i.putExtra("commentFrom", Activity_Comment.CommentFrom.Post.toString());
                i.putExtra("isFirstScroll", true);
                i.putExtra("InComingType", "DIRECT");
                startActivity(i);
            }
            else if(FLAG.equals("BOOKDETAILS_LIKE")){
                Intent intent = new Intent(this, Activity_BookDetails.class);
                intent.putExtra("InComingType", "DIRECT");
                intent.putExtra("BookID", bundle.getString("BOOKID"));
                startActivity(intent);
            }
            else if(FLAG.equals("BOOKDETAILS_COMMENT")){
                Intent intent = new Intent(this, Activity_Comment.class);
                intent.putExtra("ID", bundle.getString("BOOKID"));
                intent.putExtra("InComingType", "DIRECT");
                intent.putExtra("commentFrom", Activity_Comment.CommentFrom.Book.toString());
                intent.putExtra("isBookDetailsComment", true);
                startActivity(intent);
            }
            else if(FLAG.equals("FOLLOWREQUEST")){
                Intent intent = new Intent(this, Activity_UserProfile.class);
                intent.putExtra("InComingType", "DIRECT");
                startActivity(intent);
            }
            else if(FLAG.equals("OPENFILE")){
                Utils.OpenBook(this, Utils.currentBookItemsFromOpen);
                Utils.currentBookItemsFromOpen = null;
            }
            //endregion
        }
        else{
            Intent intent = new Intent(Activity_SignIn.this, Activity_Home.class);
            startActivity(intent);
        }
        finish();
    }

    @Override
    protected void onResume() {
        super.onResume();
        CheckAuthState();
    }

    private void CheckAuthState(){
        if(FirebaseAuth.getInstance().getCurrentUser() != null){
            //Check if it is from email /facebook / google / phone
            if(FirebaseUtils.firebaseAuth != null && FirebaseUtils.firebaseAuth.getCurrentUser() != null){
                try{
                    String provider = FirebaseUtils.firebaseAuth.getCurrentUser().getProviders().get(0);
                    if(provider.equals(FacebookAuthProvider.PROVIDER_ID)){
                        FirebaseUtils.sendTokenToServer(FirebaseInstanceId.getInstance().getToken(), this);
                        CheckFlag();//Check Flag If Something Come From Notification
                    }
                    else if(provider.equals(GoogleAuthProvider.PROVIDER_ID)){
                        FirebaseUtils.sendTokenToServer(FirebaseInstanceId.getInstance().getToken(), this);
                        CheckFlag();//Check Flag If Something Come From Notification
                    }
                    else if(provider.equals(PhoneAuthProvider.PROVIDER_ID)){
                        FirebaseUtils.sendTokenToServer(FirebaseInstanceId.getInstance().getToken(), this);
                        CheckFlag();//Check Flag If Something Come From Notification
                    }
                    else if(provider.equals(EmailAuthProvider.PROVIDER_ID)){
                        if(FirebaseAuth.getInstance().getCurrentUser().isEmailVerified()) {
                            FirebaseUtils.sendTokenToServer(FirebaseInstanceId.getInstance().getToken(), this);
                            CheckFlag();//Check Flag If Something Come From Notification
                        }
                        else{
                            try{
                                FirebaseAuth.getInstance().signOut();
                                Toast.makeText(this, "this e-mail is not verified", Toast.LENGTH_SHORT).show();
                            }
                            catch (Exception ex){
                                Log.e(TAG, ex.toString());
                            }
                        }
                    }
                }
                catch (Exception ex){
                    Log.e(TAG, ex.toString());
                }
            }
        }
    }

    private void SetUI(){
        textsignin = findViewById(R.id.signintext);
        textEmail = findViewById(R.id.emailText);
        textPassword = findViewById(R.id.passwordText);
        showHidePassword = findViewById(R.id.editShowHidePassword);

        editSignInEmailPhone = findViewById(R.id.editSignInEmailPhone);
        editSignInPassword = findViewById(R.id.editSignInPassword);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            textEmail.setLetterSpacing(.06f);
            textPassword.setLetterSpacing(.06f);
        }

        //Turning off Visibility of showHidePassword Image
        showHidePassword.setVisibility(View.INVISIBLE);

        //Adding Text listener for EditText editSignInPassword
        editSignInPassword.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if(editSignInPassword.getText().length() > 0){
                    showHidePassword.setVisibility(View.VISIBLE);
                }else {
                    showHidePassword.setVisibility(View.GONE);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        editSignInPassword.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if(actionId == EditorInfo.IME_ACTION_DONE){
                    OnSignInWithEmailAndPhone_Click(v);
                }
                return false;
            }
        });

        //region Show Hide Password
        showHidePassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(hideIcon){
                    showHidePassword.setImageResource(R.drawable.icon_visibility_on);
                    hideIcon = false;
                    editSignInPassword.setInputType(InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
                    Typeface tf =  Typeface.createFromAsset(getAssets(),"fonts/roboto.ttf");
                    editSignInPassword.setTypeface(tf);
                    editSignInPassword.setSelection(editSignInPassword.length());
                }else{
                    showHidePassword.setImageResource(R.drawable.icon_visibility_off);
                    hideIcon = true;
                    editSignInPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                    Typeface tf =  Typeface.createFromAsset(getAssets(),"fonts/roboto.ttf");
                    editSignInPassword.setTypeface(tf);
                    editSignInPassword.setSelection(editSignInPassword.length());
                }

            }
        });
        //endregion
    }

    private void FirebaseInitialize(){
        FirebaseApp.initializeApp(this);
        FirebaseUtils.firebaseAuth = FirebaseAuth.getInstance();
    }

    public void OnPhone_Click(View view){
        try{

            Utils.SetPhoneDialogBox(this, "Phone Login", "Continue", R.layout.fragment_phone, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    Utils.hideSoftKeyboard(Activity_SignIn.this);

                    AlertDialog view = (AlertDialog) dialog;
                    CountryCodePicker codePicker = view.findViewById(R.id.countryCodePicker);
                    EditText phoneEdit = view.findViewById(R.id.editSignUpPhone);
                    String PhoneNo = codePicker.getSelectedCountryCodeWithPlus() + phoneEdit.getText().toString().trim();

                    //Go To Pin Code
                    Intent intent = new Intent(Activity_SignIn.this, Activity_Phone_Verification.class);
                    intent.putExtra("PhoneNo", PhoneNo);
                    startActivity(intent);
                }
            });
        }
        catch (Exception ex){
            Log.e(TAG, ex.toString());
        }
    }

    public void OnSignInWithEmailAndPhone_Click(View view) {
        String InputStr = editSignInEmailPhone.getText().toString().trim();
        String Password = editSignInPassword.getText().toString().trim();
        if(InputStr.equals("")){
            Toast.makeText(this,"Email Not Enter", Toast.LENGTH_SHORT).show();
        }
        else if(Password.equals("")){
            Toast.makeText(this,"Password Not Enter", Toast.LENGTH_SHORT).show();
        }
        else if (Patterns.EMAIL_ADDRESS.matcher(InputStr).matches()) {
            if(counterlogin < maxLogin) {
                EmailSignIn(InputStr, Password);
            }
            else{
                Toast.makeText(this,"wait for a few second to login.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    public void OnSignUpPage_Click(View view){
        Intent intent = new Intent(this,Activity_SignUp.class);
        startActivity(intent);
    }

    public void ForgotPassword_onClick(View view){
        Intent intent = new Intent(this, Activity_ForgotPassword.class);
        startActivity(intent);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == Google_RC_SIGNIN){
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                GoogleSignInAccount googleSignInAccount = task.getResult(ApiException.class);
                FirebaseAuthWithGoogle(googleSignInAccount);
            }
            catch (ApiException ex){
                Utils.UnSetProgressDialogIndeterminate();
                Log.e(TAG, ex.toString());
                Toast.makeText(this, ex.toString(), Toast.LENGTH_SHORT).show();
            }
        }
        else if(requestCode == Facebook_RC_SIGNIN){
            facebookCallbackManager.onActivityResult(requestCode, resultCode, data);
        }
    }

    private void FirebaseAuthWithGoogle(GoogleSignInAccount googleSignInAccount){
        AuthCredential credential = GoogleAuthProvider.getCredential(googleSignInAccount.getIdToken(), null);
        FirebaseUtils.firebaseAuth.signInWithCredential(credential)
        .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {
                    //Sign in success Redirect To HomePage
                    FirebaseUser firebaseUser = task.getResult().getUser();
                    if(firebaseUser !=null) {
                        //Get Display Name From Google
                        String fullName = firebaseUser.getDisplayName();
                        //Update Database For User
                        FirebaseUtils.UpdateUserDetailsInDatabase(Activity_SignIn.this, firebaseUser.getUid(), fullName);

                        //Redirect to Home Page
                        FirebaseUtils.sendTokenToServer(FirebaseInstanceId.getInstance().getToken(), Activity_SignIn.this);
                        Intent intent = new Intent(Activity_SignIn.this, Activity_Home.class);
                        startActivity(intent);
                        finish();
                    }
                    else{
                        Log.e(TAG, "No User Found.");
                    }
                }
                else {
                    Log.e(TAG, "signInWithCredential:failure", task.getException());
                }
                //Dialog Dismiss
                Utils.UnSetProgressDialogIndeterminate();
            }
        });
    }

    public void OnGoogleSignIn_Click(View view){
        try {
            Utils.SetProgressDialogIndeterminate(this, "Google signing in");
            if(googleApiClient != null){
                googleApiClient.stopAutoManage(this);
                googleApiClient.disconnect();
            }
            GoogleSignInOptions googleSignInOptions = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN).requestIdToken(getString(R.string.default_web_client_id)).requestEmail().build();
            googleApiClient = new GoogleApiClient.Builder(getApplicationContext()).enableAutoManage(this, new GoogleApiClient.OnConnectionFailedListener() {
                @Override
                public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
                    Toast.makeText(Activity_SignIn.this, connectionResult.getErrorMessage(), Toast.LENGTH_SHORT).show();
                    Log.e(TAG, connectionResult.getErrorMessage());
                }
            }).addApi(Auth.GOOGLE_SIGN_IN_API, googleSignInOptions).build();

            Intent intent = Auth.GoogleSignInApi.getSignInIntent(googleApiClient);
            startActivityForResult(intent, Google_RC_SIGNIN);
        }
        catch (Exception ex){
            Utils.UnSetProgressDialogIndeterminate();
            Log.e(TAG, ex.toString());
        }
    }

    //Email Sign In
    public void EmailSignIn(String Email, String Password) {
        FirebaseUtils.firebaseAuth = FirebaseAuth.getInstance();
        Utils.SetProgressDialogIndeterminate(this, "Signing in..");
        FirebaseUtils.firebaseAuth.signInWithEmailAndPassword(Email, Password)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if(task.isSuccessful()){
                            FirebaseUser firebaseUser = FirebaseUtils.firebaseAuth.getCurrentUser();
                            if(firebaseUser.isEmailVerified()){
                                //Send Token To Server
                                FirebaseUtils.sendTokenToServer(FirebaseInstanceId.getInstance().getToken(), Activity_SignIn.this);

                                //Verified Email & Redirect Page
                                Intent intent = new Intent(Activity_SignIn.this, Activity_Home.class);
                                startActivity(intent);

                                finish();
                            }
                            else{
                                Toast.makeText(Activity_SignIn.this, "Error: Not Verified User [Please Check Your Email To Verify As User]", Toast.LENGTH_SHORT).show();
                            }
                        }
                        else{
                            counterlogin ++;
                            if(counterlogin >= maxLogin){ StartTimer();}
                            int currentLogin = (maxLogin - counterlogin);
                            String tR = "";
                            if(currentLogin > 0) {
                                tR = " ( " + currentLogin + " times remaining. )";
                            }
                            Toast.makeText(Activity_SignIn.this, task.getException().getMessage() + tR, Toast.LENGTH_SHORT).show();
                        }
                        Utils.UnSetProgressDialogIndeterminate();
                    }
                });
    }

    //Facebook Sign In
    public void OnFacebookSignIn_Click(View view){
        try{
            Utils.SetProgressDialogIndeterminate(this, "logging in");
            facebookCallbackManager = CallbackManager.Factory.create();
            Facebook_RC_SIGNIN = CallbackManagerImpl.RequestCodeOffset.Login.toRequestCode();

            final LoginManager loginManager = LoginManager.getInstance();
            loginManager.logInWithReadPermissions(this, Arrays.asList("email", "public_profile"));
            loginManager.registerCallback(facebookCallbackManager, new FacebookCallback<LoginResult>() {
                @Override
                public void onSuccess(LoginResult loginResult) {
                    AuthCredential credential = FacebookAuthProvider.getCredential(loginResult.getAccessToken().getToken());
                    FirebaseUtils.firebaseAuth.signInWithCredential(credential).
                        addOnCompleteListener(Activity_SignIn.this, new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                //region Sign in success Redirect To HomePage
                                try {
                                    FirebaseUser firebaseUser = task.getResult().getUser();
                                    if (firebaseUser != null) {
                                        //Get Display Name From Google
                                        String fullName = firebaseUser.getDisplayName();
                                        //Update Database For User
                                        FirebaseUtils.UpdateUserDetailsInDatabase(Activity_SignIn.this, firebaseUser.getUid(), fullName);

                                        //Redirect to Home Page
                                        FirebaseUtils.sendTokenToServer(FirebaseInstanceId.getInstance().getToken(), Activity_SignIn.this);
                                        Intent intent = new Intent(Activity_SignIn.this, Activity_Home.class);
                                        startActivity(intent);

                                        finish();
                                    }
                                    Utils.UnSetProgressDialogIndeterminate();
                                }
                                catch (Exception ex){
                                    //Toast.makeText(Activity_SignIn.this, "--->2"+ ex.getMessage(), Toast.LENGTH_SHORT).show();
                                    Log.e(TAG, ex.toString());
                                }
                                //endregion
                            }
                        }).
                        addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Utils.UnSetProgressDialogIndeterminate();
                                Toast.makeText(Activity_SignIn.this, e.getMessage(), Toast.LENGTH_LONG).show();
                            }
                        });
                }

                @Override
                public void onCancel() {
                    Utils.UnSetProgressDialogIndeterminate();
                    Log.e(TAG, "FB-sign in cancel");
                }

                @Override
                public void onError(FacebookException error) {
                    if (error instanceof FacebookAuthorizationException) {
                        if (AccessToken.getCurrentAccessToken() != null) {
                            LoginManager.getInstance().logOut();
                        }
                    }
                    Utils.UnSetProgressDialogIndeterminate();
                    Log.e(TAG , error.toString());
                }
            });
        }
        catch (Exception ex){
            Utils.UnSetProgressDialogIndeterminate();
            Log.e(TAG, ex.toString());
        }
    }

    public void StartTimer(){
        new CountDownTimer(WaitTimeForLogin, 1000){
            @Override
            public void onTick(long millisUntilFinished) {
                String rTime = "remain time "+ (millisUntilFinished/ 1000) + "s";
                textsignin.setText(rTime);
            }

            @Override
            public void onFinish() {
                counterlogin = 0;
                textsignin.setText("Sign In");
            }
        }.start();
    }

    public void GetPackageHashKey(){
        try {
            PackageInfo info = getPackageManager().getPackageInfo(getPackageName(), PackageManager.GET_SIGNATURES);
            for (Signature signature : info.signatures) {
                MessageDigest md = MessageDigest.getInstance("SHA");
                md.update(signature.toByteArray());
                Log.e("K_SUZ_HK", Base64.encodeToString(md.digest(), Base64.DEFAULT));
            }
        } catch (PackageManager.NameNotFoundException e) {
            Log.e("PM:HK:1:", e.toString());

        } catch (NoSuchAlgorithmException e) {
            Log.e("PM:HK:2:", e.toString());
        }
    }
}