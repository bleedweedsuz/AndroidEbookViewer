package zkagazapahtnajusz.paperproject.com.paperproject;

import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;

import zkagazapahtnajusz.paperproject.com.paperproject.Utilities.FirebaseUtils;
import zkagazapahtnajusz.paperproject.com.paperproject.Utilities.Utils;

public class Activity_ForgotPassword extends AppCompatActivity {

    private static final String TAG = "Activity_ForgotPassword";
    Toolbar toolbar;

    EditText EmailBox;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity__forgot_password);
        toolbar = findViewById(R.id.toolBar);
        setSupportActionBar(toolbar);
        if(getSupportActionBar() != null){
            getSupportActionBar().setDisplayShowTitleEnabled(false);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }
        EmailBox = findViewById(R.id.email);
        EmailBox.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if(actionId == EditorInfo.IME_ACTION_DONE){
                    Submit_onClick(null);
                    return true;
                }
                return false;
            }
        });

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId() == android.R.id.home){
            finish();
        }
        return super.onOptionsItemSelected(item);
    }

    public void Submit_onClick(View view){
        try{
            EmailBox.clearFocus();
            Utils.SetProgressDialogIndeterminate(this, "sending email..");
            String Email = EmailBox.getText().toString().trim();
            FirebaseUtils.firebaseAuth.sendPasswordResetEmail(Email).
                    addOnCompleteListener(
                            new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if(task.isSuccessful()){
                                        Toast.makeText(Activity_ForgotPassword.this, "Reset Password email is send to your email address.", Toast.LENGTH_SHORT).show();
                                        Activity_ForgotPassword.this.finish();
                                    }
                                    Utils.UnSetProgressDialogIndeterminate();
                                }
                            })
                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Utils.UnSetProgressDialogIndeterminate();
                                    Toast.makeText(Activity_ForgotPassword.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            });
        }
        catch (Exception ex){
            Utils.UnSetProgressDialogIndeterminate();
            Log.e(TAG, ex.toString());
        }
    }
}