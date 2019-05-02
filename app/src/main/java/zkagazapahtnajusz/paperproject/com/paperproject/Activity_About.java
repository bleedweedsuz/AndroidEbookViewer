package zkagazapahtnajusz.paperproject.com.paperproject;

import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.widget.TextView;

public class Activity_About extends AppCompatActivity {
    private static final String TAG = "Activity_About";
    Toolbar toolbar;
    View MainTemplate, CreditsView;

    TextView versionTxt;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity__credits);
        toolbar = findViewById(R.id.toolBar);
        setSupportActionBar(toolbar);
        if(getSupportActionBar() != null){
            getSupportActionBar().setDisplayShowTitleEnabled(false);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }
        versionTxt = findViewById(R.id.version);
        CreditsView = findViewById(R.id.CreditsView);
        CreditsView.startAnimation(AnimationUtils.loadAnimation(this, R.anim.animation_credits));

        try {
            PackageInfo pInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
            String version = "Version " + pInfo.versionName;
            versionTxt.setText(version);
        }
        catch (PackageManager.NameNotFoundException e) {
            Log.e(TAG, e.toString());
        }

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId() == android.R.id.home){
            finish();
        }
        return super.onOptionsItemSelected(item);
    }
}
