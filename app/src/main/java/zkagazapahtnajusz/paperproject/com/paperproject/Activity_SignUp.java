package zkagazapahtnajusz.paperproject.com.paperproject;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.Html;
import android.text.InputType;
import android.text.TextWatcher;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.AuthResult;
import com.ybs.passwordstrengthmeter.PasswordStrength;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import zkagazapahtnajusz.paperproject.com.paperproject.Utilities.FirebaseUtils;
import zkagazapahtnajusz.paperproject.com.paperproject.Utilities.Utils;

/**
 * Created by Zw4R-TSTUD10 2018/4/1 [2018, April 1]
 */

public class Activity_SignUp extends AppCompatActivity {

    private static final String TAG = "Activity_SignUp";
    //End Fire-base Variables
    TextView textSignUpFullName, textSignUpEmail;
    //CountryCodePicker ccp;
    EditText editSignUpPassword;
    ImageView showHidePassword;
    boolean hideIcon = true;

    //Sign Up For Phone & Email Variables
    EditText FullName, PhoneNo, Password, Email;
    TextView textSignUpPassword, agrees_;

    ProgressBar passwordProgress;
    private boolean isPasswordOk = true;


    TextView _specialcharacter, _numericcharacter, _uppercasecharacter, _lowercasecharacter, _greaterthaneightcharacter;

    Pattern letterL = Pattern.compile("[a-z]");
    Pattern letterU = Pattern.compile("[A-Z]");
    Pattern digit = Pattern.compile("[0-9]");
    Pattern special = Pattern.compile ("[!@#$%&*()_+=|<>?{}\\[\\]~-]");

    CheckBox checkBox;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity__sign_up);
        textSignUpEmail = findViewById(R.id.textSignUpEmail);
        passwordProgress = findViewById(R.id.passwordProgress);
        textSignUpPassword = findViewById(R.id.textSignUpPassword);

        editSignUpPassword = findViewById(R.id.editSignUpPassword);
        showHidePassword = findViewById(R.id.editShowHidePasswordSignUp);

        agrees_ = findViewById(R.id.agrees_);

        checkBox = findViewById(R.id.checkBox);

        //Init Phone & Email Variables
        FullName = findViewById(R.id.editSignUpFullName);

        Password = editSignUpPassword;
        Email = findViewById(R.id.editSignUpEmail);

        //Initialize FireBase App
        FirebaseApp.initializeApp(this);

        //Turning off Visibility of showHidePassword Image
        showHidePassword.setVisibility(View.INVISIBLE);

        //Set Password indicator view
        _specialcharacter = findViewById(R.id._specialcharacter);
        _numericcharacter = findViewById(R.id._numericcharacter);
        _uppercasecharacter = findViewById(R.id._uppercasecharacter);
        _lowercasecharacter = findViewById(R.id._lowercasecharacter);
        _greaterthaneightcharacter = findViewById(R.id._greaterthaneightcharacter);

        editSignUpPassword.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if(editSignUpPassword.getText().length() > 0){
                    showHidePassword.setVisibility(View.VISIBLE);
                }else {
                    showHidePassword.setVisibility(View.GONE);
                }

                //Update Password String
                //UpdatePasswordStrengthView(s.toString().trim());
            }

            @Override
            public void afterTextChanged(Editable s) {

            }


        });

        showHidePassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(hideIcon){
                    showHidePassword.setImageResource(R.drawable.icon_visibility_on);
                    hideIcon = false;
                    editSignUpPassword.setInputType(InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
                    Typeface tf =  Typeface.createFromAsset(getAssets(),"fonts/roboto.ttf");
                    editSignUpPassword.setTypeface(tf);
                    editSignUpPassword.setSelection(editSignUpPassword.length());
                }else{
                    showHidePassword.setImageResource(R.drawable.icon_visibility_off);
                    hideIcon = true;
                    editSignUpPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                    Typeface tf =  Typeface.createFromAsset(getAssets(),"fonts/roboto.ttf");
                    editSignUpPassword.setTypeface(tf);
                    editSignUpPassword.setSelection(editSignUpPassword.length());
                }
            }
        });

        editSignUpPassword.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if(actionId == EditorInfo.IME_ACTION_DONE){
                    OnSignUpPage_Click(v);
                }
                return false;
            }
        });


        String ag_str = "I agree to the <b><a href='schemecwebview://1'>Terms Of Service</a></b> &amp; <b><a href='schemecwebview://2'>Privacy Policy</a></b>";
        agrees_.setText(Html.fromHtml(ag_str));
        agrees_.setMovementMethod(LinkMovementMethod.getInstance());
    }

    //SignIn Page Click
    public void OnSignInPage_Click(View view){
        finish();
    }

    public void OnSignUpPage_Click(View v){
        try {
            String fullName = FullName.getText().toString().trim();
            String email = Email.getText().toString().trim();
            String password = Password.getText().toString().trim();
            if(fullName.isEmpty()){
                Toast.makeText(this, "Enter Full Name", Toast.LENGTH_SHORT).show();
                return;
            }
            else if(email.isEmpty()){
                Toast.makeText(this, "Enter Your Email", Toast.LENGTH_SHORT).show();
                return;
            }
            else if(password.isEmpty()){
                Toast.makeText(this, "Enter Your Password", Toast.LENGTH_SHORT).show();
                return;
            }

            if(!checkBox.isChecked()){
                Toast.makeText(this, "Please agree to Terms Of Use & Privacy Statement.", Toast.LENGTH_SHORT).show();
                return;
            }

            if(isPasswordOk) {
                SignUp(email, password, fullName);
            }
            else{
                Toast.makeText(this, "password is not strong, please enter strong password.", Toast.LENGTH_SHORT).show();
            }
        }
        catch (Exception ex){
            Log.e(TAG, ex.toString());
        }
    }

    public void SignUp(final String email, final String password, final String fullName){
        try{
            Utils.SetProgressDialogIndeterminate(this, "signing up");
            FirebaseUtils.firebaseAuth.createUserWithEmailAndPassword(email,password)
                    .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if(task.isSuccessful()){
                                //Save To Database
                                //Update Database For User
                                FirebaseUtils.UpdateUserDetailsInDatabase(Activity_SignUp.this, FirebaseUtils.firebaseAuth.getCurrentUser().getUid(), fullName);
                                //Add Display Data In System
                                FirebaseUtils.SetAuthDisplayName(FirebaseUtils.firebaseAuth, fullName);

                                //Email Verification
                                FirebaseUtils.SendVerificationEmail(FirebaseUtils.firebaseAuth.getCurrentUser());

                                //SignOut System
                                //FirebaseUtils.firebaseAuth.signOut();

                                //Redirect To Email Verification Page
                                Intent intent = new Intent(Activity_SignUp.this, Activity_Email_Verification.class);
                                intent.putExtra("Email", email);
                                intent.putExtra("Password", password);
                                startActivity(intent);

                                //Toast For Register Mail
                                Toast.makeText(Activity_SignUp.this, "please check your email to verify", Toast.LENGTH_SHORT).show();
                                finish();
                            }
                            else{
                                //Dismiss Dialog
                                //Email Fail Error
                                try {
                                    Log.e("Error", task.getException().toString());
                                }
                                catch (Exception ex){
                                    Log.e(TAG, ex.toString());
                                }
                            }
                            //Dismiss Dialog
                            Utils.UnSetProgressDialogIndeterminate();
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(Activity_SignUp.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
        }
        catch (Exception ex){
            Log.e(TAG, ex.toString());
        }
    }

    private void UpdatePasswordStrengthView(String password){
        if(password.isEmpty()){
            textSignUpPassword.setText("PASSWORD");
            passwordProgress.setProgress(0);
            passwordProgress.setVisibility(View.INVISIBLE);

            //Clear All View
            _specialcharacter.setBackgroundResource(R.drawable.button_password_indicator_clear);
            _specialcharacter.setTextColor(Color.parseColor("#6e6e6e"));
            _numericcharacter.setBackgroundResource(R.drawable.button_password_indicator_clear);
            _numericcharacter.setTextColor(Color.parseColor("#6e6e6e"));
            _uppercasecharacter.setBackgroundResource(R.drawable.button_password_indicator_clear);
            _uppercasecharacter.setTextColor(Color.parseColor("#6e6e6e"));
            _lowercasecharacter.setBackgroundResource(R.drawable.button_password_indicator_clear);
            _lowercasecharacter.setTextColor(Color.parseColor("#6e6e6e"));
            _greaterthaneightcharacter.setBackgroundResource(R.drawable.button_password_indicator_clear);
            _greaterthaneightcharacter.setTextColor(Color.parseColor("#6e6e6e"));
        }
        else{

            passwordProgress.setVisibility(View.VISIBLE);
            PasswordStrength str = PasswordStrength.calculateStrength(password);
            String strType = str.getText(Activity_SignUp.this).toString();
            int color;
            int val;
            if (strType.equals("Weak")) {
                isPasswordOk = false;
                color = Color.parseColor("#fb1515");
                val = 25;
            } else if (strType.equals("Medium")) {
                isPasswordOk = true;
                color = Color.parseColor("#f34646");
                val = 50;
            } else if (strType.equals("Strong")) {
                isPasswordOk = true;
                color = Color.parseColor("#36802d");
                val = 75;
            } else {
                isPasswordOk = true;
                color = Color.parseColor("#09dc5f");
                val = 100;
            }
            String signUpPasswordStr = "<b>PASSWORD</b> <i>"+ strType +" &nbsp;&nbsp;</i>";
            textSignUpPassword.setText(Html.fromHtml(signUpPasswordStr));
            passwordProgress.getProgressDrawable().setColorFilter(color, android.graphics.PorterDuff.Mode.SRC_IN);
            passwordProgress.setProgress(val);

            //Check if special character exists
            Matcher hasSpecial = special.matcher(password);
            if(hasSpecial.find()){
                _specialcharacter.setTextColor(Color.parseColor("#ffffff"));
                _specialcharacter.setBackgroundResource(R.drawable.button_password_indicator_pass);
            }
            else{
                _specialcharacter.setTextColor(Color.parseColor("#6e6e6e"));
                _specialcharacter.setBackgroundResource(R.drawable.button_password_indicator_clear);
            }

            //Check if numeric character exists
            Matcher hasNumeric = digit.matcher(password);
            if(hasNumeric.find()){
                _numericcharacter.setTextColor(Color.parseColor("#ffffff"));
                _numericcharacter.setBackgroundResource(R.drawable.button_password_indicator_pass);
            }
            else{
                _numericcharacter.setTextColor(Color.parseColor("#6e6e6e"));
                _numericcharacter.setBackgroundResource(R.drawable.button_password_indicator_clear);
            }

            //Check if upper case character exists
            Matcher hasUper = letterU.matcher(password);
            if(hasUper.find()){
                _uppercasecharacter.setTextColor(Color.parseColor("#ffffff"));
                _uppercasecharacter.setBackgroundResource(R.drawable.button_password_indicator_pass);
            }
            else{
                _uppercasecharacter.setTextColor(Color.parseColor("#6e6e6e"));
                _uppercasecharacter.setBackgroundResource(R.drawable.button_password_indicator_clear);
            }

            //Check if lower case character exists
            Matcher hasLower = letterL.matcher(password);
            if(hasLower.find()){
                _lowercasecharacter.setTextColor(Color.parseColor("#ffffff"));
                _lowercasecharacter.setBackgroundResource(R.drawable.button_password_indicator_pass);
            }
            else{
                _lowercasecharacter.setTextColor(Color.parseColor("#6e6e6e"));
                _lowercasecharacter.setBackgroundResource(R.drawable.button_password_indicator_clear);
            }

            //Check if greater than 8 character exists
            if(password.length() >8 ){
                _greaterthaneightcharacter.setTextColor(Color.parseColor("#ffffff"));
                _greaterthaneightcharacter.setBackgroundResource(R.drawable.button_password_indicator_pass);
            }
            else{
                _greaterthaneightcharacter.setTextColor(Color.parseColor("#6e6e6e"));
                _greaterthaneightcharacter.setBackgroundResource(R.drawable.button_password_indicator_clear);
            }
        }
    }

    public void Click_termsofuse(View view){

    }

    public void Click_privacystatement(View view){

    }
}
