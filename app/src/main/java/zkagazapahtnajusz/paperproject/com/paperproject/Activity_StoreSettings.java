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

public class Activity_StoreSettings extends AppCompatActivity implements SwitchButton.OnCheckedChangeListener{
    private static final String TAG = "Activity_StoreSettings";
    
    Toolbar toolbar;
    SwitchButton AscendingSwitch, DescendingSwitch;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity__store_settings);
        toolbar = findViewById(R.id.toolBar);

        setSupportActionBar(toolbar);
        if(getSupportActionBar() != null){
            getSupportActionBar().setDisplayShowTitleEnabled(false);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }

        AscendingSwitch = findViewById(R.id.AscendingSwitch);
        DescendingSwitch = findViewById(R.id.DescendingSwitch);

        loadSharedPref();

        AscendingSwitch.setOnCheckedChangeListener(this);
        DescendingSwitch.setOnCheckedChangeListener(this);
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
        if(view.getId() == R.id.AscendingSwitch){
            Map<String, String> map = new HashMap<>();
            map.put(getString(R.string.StoreSetting_SortMode), isChecked + "");
            SharedPreferenceUtils.UpdateData(this, map);
            if(isChecked) {
                DescendingSwitch.setChecked(false);
                Toast.makeText(this, "sort mode to ascending", Toast.LENGTH_SHORT).show();
            }
            else{
                DescendingSwitch.setChecked(true);
                Toast.makeText(this, "sort mode to descending", Toast.LENGTH_SHORT).show();
            }
        }
        else if(view.getId() == R.id.DescendingSwitch){
            if(isChecked) {
                AscendingSwitch.setChecked(false);
                Toast.makeText(this, "sort mode to descending", Toast.LENGTH_SHORT).show();
            }
            else{
                AscendingSwitch.setChecked(true);
                Toast.makeText(this, "sort mode to ascending", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void loadSharedPref(){
        try{
            boolean flag = SharedPreferenceUtils.Setting_GetData(this, getString(R.string.StoreSetting_SortMode), true);
            if(flag){
                AscendingSwitch.setChecked(true);
                DescendingSwitch.setChecked(false);
            }
            else{
                AscendingSwitch.setChecked(false);
                DescendingSwitch.setChecked(true);
            }
        }
        catch (Exception ex){
            Log.e(TAG, ex.toString());
        }
    }
}
