package zkagazapahtnajusz.paperproject.com.paperproject;

import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.suke.widget.SwitchButton;

import java.util.HashMap;
import java.util.Map;

import zkagazapahtnajusz.paperproject.com.paperproject.Utilities.FirebaseUtils;
import zkagazapahtnajusz.paperproject.com.paperproject.Utilities.Utils;

public class Activity_PrivacyAccount_Setting extends AppCompatActivity implements SwitchButton.OnCheckedChangeListener{
    private static final String TAG = "Activity_PrivacyAccount";
    Toolbar toolbar;
    SwitchButton emailSwitch, phoneSwitch, genderSwitch, birthdaySwitch, bioSwitch;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_privacyaccount_setting);
        toolbar = findViewById(R.id.toolBar);
        setSupportActionBar(toolbar);
        if(getSupportActionBar() != null){
            getSupportActionBar().setDisplayShowTitleEnabled(false);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }

        emailSwitch = findViewById(R.id.emailSwitch);
        phoneSwitch = findViewById(R.id.phoneSwitch);
        genderSwitch = findViewById(R.id.genderSwitch);
        birthdaySwitch = findViewById(R.id.birthdaySwitch);
        bioSwitch = findViewById(R.id.bioSwitch);

        LoadSettings();
    }

    private void SetListener(){
        emailSwitch.setOnCheckedChangeListener(this);
        phoneSwitch.setOnCheckedChangeListener(this);
        genderSwitch.setOnCheckedChangeListener(this);
        birthdaySwitch.setOnCheckedChangeListener(this);
        bioSwitch.setOnCheckedChangeListener(this);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId() == android.R.id.home){
            finish();
        }
        return super.onOptionsItemSelected(item);
    }

    private void LoadSettings(){
        try{
            Utils.SetProgressDialogIndeterminate(this, "loading");
            String UID = FirebaseUtils.firebaseAuth.getCurrentUser().getUid();
            String Root = getString(R.string.FIRESTORE_ROOT_USERDETAILS) + "/" + UID + "/" + getString(R.string.AccountPrivacySetting) + "/" + UID;
            FirebaseUtils.FirestoreGetData(Root, new OnSuccessListener<DocumentSnapshot>() {
                @Override
                public void onSuccess(DocumentSnapshot documentSnapshot) {
                    if(documentSnapshot.exists()){
                        boolean emailSwitchState =false; try{emailSwitchState = documentSnapshot.getBoolean(getString(R.string.AccountPrivacy_Email));} catch ( Exception ex){Log.e(TAG, ex.toString());}
                        boolean phoneSwitchState =false; try{ phoneSwitchState= documentSnapshot.getBoolean(getString(R.string.AccountPrivacy_Phone));} catch ( Exception ex){Log.e(TAG, ex.toString());}
                        boolean genderSwitchState =false; try{ genderSwitchState= documentSnapshot.getBoolean(getString(R.string.AccountPrivacy_Gender));} catch ( Exception ex){Log.e(TAG, ex.toString());}
                        boolean birthdaySwitchState =false; try{ birthdaySwitchState = documentSnapshot.getBoolean(getString(R.string.AccountPrivacy_Birthday));} catch ( Exception ex){Log.e(TAG, ex.toString());}
                        boolean bioSwitchState =false; try{ bioSwitchState= documentSnapshot.getBoolean(getString(R.string.AccountPrivacy_Bio));} catch ( Exception ex){Log.e(TAG, ex.toString());}

                        emailSwitch.setChecked(emailSwitchState);
                        phoneSwitch.setChecked(phoneSwitchState);
                        genderSwitch.setChecked(genderSwitchState);
                        birthdaySwitch.setChecked(birthdaySwitchState);
                        bioSwitch.setChecked(bioSwitchState);
                    }
                    new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            Utils.UnSetProgressDialogIndeterminate();
                            SetListener();
                        }
                    }, 500);
                }
            }, new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Log.e(TAG, e.toString());
                    Toast.makeText(Activity_PrivacyAccount_Setting.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                    Utils.UnSetProgressDialogIndeterminate();
                    SetListener();
                }
            });
        }
        catch (Exception ex){
            SetListener();
            Log.e(TAG, ex.toString());
        }
    }

    @Override
    public void onCheckedChanged(SwitchButton view, boolean isChecked) {
        if(view.getId() == R.id.emailSwitch){
            Map<String , Object> map = new HashMap<>();
            map.put(getString(R.string.AccountPrivacy_Email), isChecked);
            setSettings(map);
        }
        else if(view.getId() == R.id.phoneSwitch){
            Map<String , Object> map = new HashMap<>();
            map.put(getString(R.string.AccountPrivacy_Phone), isChecked);
            setSettings(map);
        }
        else if(view.getId() == R.id.genderSwitch){
            Map<String , Object> map = new HashMap<>();
            map.put(getString(R.string.AccountPrivacy_Gender), isChecked);
            setSettings(map);
        }
        else if(view.getId() == R.id.birthdaySwitch){
            Map<String , Object> map = new HashMap<>();
            map.put(getString(R.string.AccountPrivacy_Birthday), isChecked);
            setSettings(map);
        }
        else if(view.getId() == R.id.bioSwitch){
            Map<String , Object> map = new HashMap<>();
            map.put(getString(R.string.AccountPrivacy_Bio), isChecked);
            setSettings(map);
        }

        Log.i(TAG, "<--------------11");
    }

    private void setSettings(Map<String, Object> map){
        try{
            Utils.SetProgressDialogIndeterminate(this, "processing");
            String UID = FirebaseUtils.firebaseAuth.getCurrentUser().getUid();
            String Root = getString(R.string.FIRESTORE_ROOT_USERDETAILS) + "/" + UID + "/" + getString(R.string.AccountPrivacySetting) + "/" + UID;
            FirebaseUtils.FirestroreSetData(Root, map,
                    new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            Log.i(TAG, "Data loaded...");
                            Utils.UnSetProgressDialogIndeterminate();
                        }
                    }, new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Utils.UnSetProgressDialogIndeterminate();
                        }
                    });
        }
        catch (Exception ex){
            Utils.UnSetProgressDialogIndeterminate();
            Log.e(TAG, ex.toString());
        }
    }
}
