package zkagazapahtnajusz.paperproject.com.paperproject;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.widget.Toast;

import com.suke.widget.SwitchButton;

import java.util.HashMap;
import java.util.Map;

import zkagazapahtnajusz.paperproject.com.paperproject.Utilities.SharedPreferenceUtils;
import zkagazapahtnajusz.paperproject.com.paperproject.Utilities.Utils;

public class Activity_PushNotifications extends AppCompatActivity implements SwitchButton.OnCheckedChangeListener{
    private static final String TAG = "Activity_PushNotificati";
    Toolbar toolbar;
    SwitchButton PostAddSwitch, PostCommentSwitch, PostLikeSwitch, BookCommentSwitch, BookLikeSwitch, FollowRequestSwitch;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity__push_notifications);
        toolbar = findViewById(R.id.toolBar);

        setSupportActionBar(toolbar);
        if(getSupportActionBar() != null){
            getSupportActionBar().setDisplayShowTitleEnabled(false);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }
        PostAddSwitch = findViewById(R.id.PostAddSwitch);
        PostCommentSwitch = findViewById(R.id.PostCommentSwitch);
        PostLikeSwitch = findViewById(R.id.PostLikeSwitch);
        BookCommentSwitch  = findViewById(R.id.BookCommentSwitch);
        BookLikeSwitch = findViewById(R.id.BookLikeSwitch);
        FollowRequestSwitch = findViewById(R.id.FollowRequestSwitch);

        //Load definition From Server
        loadNotificationSetting();

        PostAddSwitch.setOnCheckedChangeListener(Activity_PushNotifications.this);
        PostCommentSwitch.setOnCheckedChangeListener(Activity_PushNotifications.this);
        PostLikeSwitch.setOnCheckedChangeListener(Activity_PushNotifications.this);
        BookCommentSwitch.setOnCheckedChangeListener(Activity_PushNotifications.this);
        BookLikeSwitch.setOnCheckedChangeListener(Activity_PushNotifications.this);
        FollowRequestSwitch.setOnCheckedChangeListener(Activity_PushNotifications.this);
    }

    private void loadNotificationSetting(){
        try{
            PostAddSwitch.setChecked(SharedPreferenceUtils.Setting_GetData(this, getString(R.string.Notification_postnew), true));
            PostCommentSwitch.setChecked(SharedPreferenceUtils.Setting_GetData(this, getString(R.string.Notification_postcomment), true));
            PostLikeSwitch.setChecked(SharedPreferenceUtils.Setting_GetData(this, getString(R.string.Notification_postlike), true));
            BookCommentSwitch.setChecked(SharedPreferenceUtils.Setting_GetData(this, getString(R.string.Notification_bookcomment), true));
            BookLikeSwitch.setChecked(SharedPreferenceUtils.Setting_GetData(this, getString(R.string.Notification_booklike), true));
            FollowRequestSwitch.setChecked(SharedPreferenceUtils.Setting_GetData(this, getString(R.string.Notification_followrequest), true));
        }
        catch (Exception ex){
            Log.e(TAG, ex.toString());
            Utils.UnSetProgressDialogIndeterminate();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId() == android.R.id.home){
            finish();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onCheckedChanged(SwitchButton view, boolean isChecked) {
        try {
            if (view.getId() == R.id.PostAddSwitch) {
                Map<String, String> map = new HashMap<>();
                map.put(getString(R.string.Notification_postnew), isChecked + "");
                String msg = "New Post Added, Notification";
                if (isChecked) {
                    msg += " Enable";
                } else {
                    msg += " Disable";
                }
                SetSettingData(map, msg);
            } else if (view.getId() == R.id.PostCommentSwitch) {
                Map<String, String> map = new HashMap<>();
                map.put(getString(R.string.Notification_postcomment), isChecked + "");
                String msg = "New Post Added, Notification";
                if (isChecked) {
                    msg += " Enable";
                } else {
                    msg += " Disable";
                }
                SetSettingData(map, msg);
            } else if (view.getId() == R.id.PostLikeSwitch) {
                Map<String, String> map = new HashMap<>();
                map.put(getString(R.string.Notification_postlike), isChecked + "");
                String msg = "New Post Like Added, Notification";
                if (isChecked) {
                    msg += " Enable";
                } else {
                    msg += " Disable";
                }
                SetSettingData(map, msg);
            } else if (view.getId() == R.id.BookCommentSwitch) {
                Map<String, String> map = new HashMap<>();
                map.put(getString(R.string.Notification_bookcomment), isChecked + "");
                String msg = "New Book Comment Added, Notification";
                if (isChecked) {
                    msg += " Enable";
                } else {
                    msg += " Disable";
                }
                SetSettingData(map, msg);
            } else if (view.getId() == R.id.BookLikeSwitch) {
                Map<String, String> map = new HashMap<>();
                map.put(getString(R.string.Notification_booklike), isChecked + "");
                String msg = "New Book Like Added, Notification";
                if (isChecked) {
                    msg += " Enable";
                } else {
                    msg += " Disable";
                }
                SetSettingData(map, msg);
            } else if (view.getId() == R.id.FollowRequestSwitch) {
                Map<String, String> map = new HashMap<>();
                map.put(getString(R.string.Notification_followrequest), isChecked + "");
                String msg = "New Follow Request Added, Notification";
                if (isChecked) {
                    msg += " Enable";
                } else {
                    msg += " Disable";
                }
                SetSettingData(map, msg);
            }
        }
        catch (Exception ex){
            Log.e(TAG, ex.toString());
        }
    }

    private void SetSettingData(Map<String, String> map, final String message){
        try {
            SharedPreferenceUtils.UpdateData(this, map);
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
        }
        catch (Exception ex){
            Log.e(TAG, ex.toString());
        }
    }
}
