package zkagazapahtnajusz.paperproject.com.paperproject;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.TextView;
import android.widget.Toast;

import com.chaos.view.PinView;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.iid.FirebaseInstanceId;

import java.util.concurrent.TimeUnit;

import zkagazapahtnajusz.paperproject.com.paperproject.Utilities.FirebaseUtils;
import zkagazapahtnajusz.paperproject.com.paperproject.Utilities.Utils;

/**
 * Created by Zw4R-TSTUD10 2018/4/1 [2018, April 1]
 */

public class Activity_Phone_Verification extends AppCompatActivity {
    private static final String TAG = "Activity_Phone_Verifica";

    Toolbar toolbar;
    String VerificationCode;
    String PhoneNo;
    PinView pinView;
    TextView textVerifyPhone;
    PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallbacks;
    PhoneAuthProvider.ForceResendingToken forceResendingToken;
    PhoneAuthProvider phoneAuth;
    FirebaseAuth firebaseAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity__phone__verification);
        toolbar = findViewById(R.id.toolBar);

        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Phone Verification");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        pinView = findViewById(R.id.pinView);
        PhoneNo = getIntent().getExtras().getString("PhoneNo");
        textVerifyPhone = findViewById(R.id.textVerifiedPhone);
        textVerifyPhone.setText(PhoneNo);

        phoneAuth = PhoneAuthProvider.getInstance();
        firebaseAuth = FirebaseUtils.firebaseAuth;

        pinView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if(actionId == EditorInfo.IME_ACTION_DONE){
                    VerifyCode_Click(null);
                    return true;
                }
                return false;
            }
        });

        //Add State
        SetPhoneVerificationState();

        //Send Verification
        SendVerificationCode(PhoneNo);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId() == android.R.id.home){
            finish();
        }
        return super.onOptionsItemSelected(item);
    }

    private void SendVerificationCode(String phoneNo) {
        try{
            Utils.SetProgressDialogIndeterminate(this, "connecting..");
            phoneAuth.verifyPhoneNumber(phoneNo, 60, TimeUnit.SECONDS,this, mCallbacks);
        }
        catch (Exception ex){
            Log.e(TAG, ex.toString());
        }
    }

    private void SetPhoneVerificationState(){
        mCallbacks = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            @Override
            public void onVerificationCompleted(PhoneAuthCredential credential) {
                //signInWithPhoneAuthCredential(credential);
                Utils.UnSetProgressDialogIndeterminate();
            }

            @Override
            public void onVerificationFailed(FirebaseException e) {
                Utils.UnSetProgressDialogIndeterminate();
                Toast.makeText(Activity_Phone_Verification.this, e.getMessage(), Toast.LENGTH_LONG).show();
                finish();
            }

            @Override
            public void onCodeSent(String verificationId, PhoneAuthProvider.ForceResendingToken token) {
                VerificationCode = verificationId;
                forceResendingToken = token;
                Utils.UnSetProgressDialogIndeterminate();
            }
        };
    }

    public void VerifyCode_Click(View view){
        try{
            if(pinView.getText().toString().trim().length() < 6){Toast.makeText(Activity_Phone_Verification.this, "Please enter the correct pin format.", Toast.LENGTH_SHORT).show();return;}

            Utils.hideSoftKeyboard(this);
            Utils.SetProgressDialogIndeterminate(this, "processing...");
            String verifyCode = pinView.getText().toString().trim();

            PhoneAuthCredential credential = PhoneAuthProvider.getCredential(VerificationCode, verifyCode);
            FirebaseUtils.SignInWithPhone(this, FirebaseAuth.getInstance(), credential, "", new FirebaseUtils.SignInFinished_Phone() {
                @Override
                public void Finished(boolean isSuccessful, FirebaseUser user) {
                    if(isSuccessful){
                        RedirectHomePage(user);
                    }
                    else{
                        Utils.UnSetProgressDialogIndeterminate();
                        Toast.makeText(Activity_Phone_Verification.this, "Verification Code Does'nt Match", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }
        catch (Exception ex){
            Utils.UnSetProgressDialogIndeterminate();
            Log.e(TAG, ex.toString());
        }
    }

    public void ResendVerifyCode_Click(View view) {
        try{
            Utils.SetProgressDialogIndeterminate(this, "processing..");
            phoneAuth.verifyPhoneNumber(PhoneNo, 60, TimeUnit.SECONDS,this, mCallbacks, forceResendingToken);
        }
        catch (Exception ex){
            Log.e("Error",ex.toString());
        }
    }

    private void signInWithPhoneAuthCredential(PhoneAuthCredential credential) {
        firebaseAuth.signInWithCredential(credential)
            .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    if (task.isSuccessful()) {
                        FirebaseUser user = task.getResult().getUser();
                        RedirectHomePage(user);
                    }
                }
            });
    }

    private void RedirectHomePage(FirebaseUser firebaseUser){
        //redirect
        //region Sign in success Redirect To HomePage
        try {
            if (firebaseUser != null) {
                //Get Display Name From Google
                //String fullName = firebaseUser.getDisplayName();
                //Update Database For User
                //FirebaseUtils.UpdateUserDetailsInDatabase(Activity_Phone_Verification.this, firebaseUser.getUid(), fullName);

                //Redirect to Home Page
                FirebaseUtils.sendTokenToServer(FirebaseInstanceId.getInstance().getToken(), Activity_Phone_Verification.this);
                Intent intent = new Intent(Activity_Phone_Verification.this, Activity_Home.class);
                startActivity(intent);

                finish();
            }
            Utils.UnSetProgressDialogIndeterminate();
        }
        catch (Exception ex){
            Log.e(TAG, ex.toString());
        }
        //endregion
    }
}
