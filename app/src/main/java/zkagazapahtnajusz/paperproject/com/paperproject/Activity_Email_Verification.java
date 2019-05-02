package zkagazapahtnajusz.paperproject.com.paperproject;

import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;

import zkagazapahtnajusz.paperproject.com.paperproject.Utilities.FirebaseUtils;
import zkagazapahtnajusz.paperproject.com.paperproject.Utilities.Utils;

/**
 * Created by Zw4R-TSTUD10 2018/4/1 [2018, April 1]
 */

public class Activity_Email_Verification extends AppCompatActivity {
    TextView userEmail, resendEmail;
    View buttonVerified;

    String EmailStr, PasswordStr;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity__email__verification);
        userEmail = findViewById(R.id.verificationEmail);
        buttonVerified = findViewById(R.id.buttonVerified);
        resendEmail = findViewById(R.id.textResendEmail);

        //Change userEmail text here
        userEmail.setText(getIntent().getStringExtra("Email"));
        EmailStr = getIntent().getStringExtra("Email");
        PasswordStr = getIntent().getStringExtra("Password");
    }

    public void OnVerified_Click(View view){
        finish();
    }

    public void OnResendEmail_Click(View view){
        AuthenticateAndResendEmail(EmailStr, PasswordStr);
    }

    public void AuthenticateAndResendEmail(String Email, String Password){
        Utils.SetProgressDialogIndeterminate(this, "Resending verification");
        AuthCredential authCredential = EmailAuthProvider.getCredential(Email,Password);
        FirebaseAuth.getInstance().signInWithCredential(authCredential).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if(task.isSuccessful()) {
                    FirebaseUtils.SendVerificationEmail(FirebaseAuth.getInstance().getCurrentUser());
                    FirebaseAuth.getInstance().signOut();
                    Utils.UnSetProgressDialogIndeterminate();
                    Toast.makeText(Activity_Email_Verification.this,
                            "Email Verification Sent",
                             Toast.LENGTH_SHORT)
                            .show();
                }
            }
        });
    }
}
