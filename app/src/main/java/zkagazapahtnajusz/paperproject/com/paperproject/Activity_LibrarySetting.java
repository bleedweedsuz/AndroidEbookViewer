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

public class Activity_LibrarySetting extends AppCompatActivity implements SwitchButton.OnCheckedChangeListener{
    private static final String TAG = "Activity_LibrarySetting";

    Toolbar toolbar;
    SwitchButton LocalDriveSwitch, DocSwitch, ExcelSwitch, PowerPointSwitch, PdfSwitch, TxtSwitch, DocumentSwitch, DownloadSwitch;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity__library_setting);
        toolbar = findViewById(R.id.toolBar);

        setSupportActionBar(toolbar);
        if(getSupportActionBar() != null){
            getSupportActionBar().setDisplayShowTitleEnabled(false);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }
        LocalDriveSwitch = findViewById(R.id.LocalDriveSwitch);
        DocSwitch = findViewById(R.id.DocSwitch);
        ExcelSwitch = findViewById(R.id.ExcelSwitch);
        PowerPointSwitch = findViewById(R.id.PowerPointSwitch);
        PdfSwitch = findViewById(R.id.PdfSwitch);
        TxtSwitch = findViewById(R.id.TxtSwitch);
        DocumentSwitch = findViewById(R.id.DocumentSwitch);
        DownloadSwitch = findViewById(R.id.DownloadSwitch);

        loadSharedPref();

        LocalDriveSwitch.setOnCheckedChangeListener(this);
        DocSwitch.setOnCheckedChangeListener(this);
        ExcelSwitch.setOnCheckedChangeListener(this);
        PowerPointSwitch.setOnCheckedChangeListener(this);
        TxtSwitch.setOnCheckedChangeListener(this);
        PdfSwitch.setOnCheckedChangeListener(this);
        DocumentSwitch.setOnCheckedChangeListener(this);
        DownloadSwitch.setOnCheckedChangeListener(this);
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
        if(view.getId() == R.id.LocalDriveSwitch){
            Map<String, String> map = new HashMap<>();
            map.put(getString(R.string.LibrarySetting_ShowLocalDrive), isChecked + "");
            SharedPreferenceUtils.UpdateData(this, map);
            if(isChecked) {
                Toast.makeText(this, "show local drive set", Toast.LENGTH_SHORT).show();
            }
            else{
                Toast.makeText(this, "do not show local drive set", Toast.LENGTH_SHORT).show();
            }

            DocumentSwitch.setChecked(isChecked);
            DownloadSwitch.setChecked(isChecked);
        }
        else if(view.getId() == R.id.DocumentSwitch){
            Map<String, String> map = new HashMap<>();
            map.put(getString(R.string.LibrarySetting_ShowLocalDrive_Document), isChecked + "");
            SharedPreferenceUtils.UpdateData(this, map);
            if(isChecked) {
                Toast.makeText(this, "search in document", Toast.LENGTH_SHORT).show();
            }
            else{
                Toast.makeText(this, "do not search in document", Toast.LENGTH_SHORT).show();
            }
        }
        else if(view.getId() == R.id.DownloadSwitch){
            Map<String, String> map = new HashMap<>();
            map.put(getString(R.string.LibrarySetting_ShowLocalDrive_Download), isChecked + "");
            SharedPreferenceUtils.UpdateData(this, map);
            if(isChecked) {
                Toast.makeText(this, "search in download", Toast.LENGTH_SHORT).show();
            }
            else{
                Toast.makeText(this, "do not search search in download", Toast.LENGTH_SHORT).show();
            }
        }
        else if(view.getId() == R.id.DocSwitch){
            Map<String, String> map = new HashMap<>();
            map.put(getString(R.string.LibrarySetting_FilterFileExtension_DOC), isChecked + "");
            SharedPreferenceUtils.UpdateData(this, map);
            if(isChecked) {
                Toast.makeText(this, "search doc in local drive", Toast.LENGTH_SHORT).show();
            }
            else{
                Toast.makeText(this, "do not search doc local drive", Toast.LENGTH_SHORT).show();
            }
        }
        else if(view.getId() == R.id.ExcelSwitch){
            Map<String, String> map = new HashMap<>();
            map.put(getString(R.string.LibrarySetting_FilterFileExtension_XLS), isChecked + "");
            SharedPreferenceUtils.UpdateData(this, map);
            if(isChecked) {
                Toast.makeText(this, "search xls in local drive", Toast.LENGTH_SHORT).show();
            }
            else{
                Toast.makeText(this, "do not search xls local drive", Toast.LENGTH_SHORT).show();
            }
        }
        else if(view.getId() == R.id.PowerPointSwitch){
            Map<String, String> map = new HashMap<>();
            map.put(getString(R.string.LibrarySetting_FilterFileExtension_PPT), isChecked + "");
            SharedPreferenceUtils.UpdateData(this, map);
            if(isChecked) {
                Toast.makeText(this, "search ppt in local drive", Toast.LENGTH_SHORT).show();
            }
            else{
                Toast.makeText(this, "do not search ppt local drive", Toast.LENGTH_SHORT).show();
            }
        }
        else if(view.getId() == R.id.PdfSwitch){
            Map<String, String> map = new HashMap<>();
            map.put(getString(R.string.LibrarySetting_FilterFileExtension_PDF), isChecked + "");
            SharedPreferenceUtils.UpdateData(this, map);
            if(isChecked) {
                Toast.makeText(this, "search pdf in local drive", Toast.LENGTH_SHORT).show();
            }
            else{
                Toast.makeText(this, "do not search pdf local drive", Toast.LENGTH_SHORT).show();
            }
        }
        else if(view.getId() == R.id.TxtSwitch){
            Map<String, String> map = new HashMap<>();
            map.put(getString(R.string.LibrarySetting_FilterFileExtension_TXT), isChecked + "");
            SharedPreferenceUtils.UpdateData(this, map);
            if(isChecked) {
                Toast.makeText(this, "search txt file in local drive", Toast.LENGTH_SHORT).show();
            }
            else{
                Toast.makeText(this, "do not search txt file in local drive", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void loadSharedPref(){
        try{
            LocalDriveSwitch.setChecked(SharedPreferenceUtils.Setting_GetData(this, getString(R.string.LibrarySetting_ShowLocalDrive), true));

            DocSwitch.setChecked(SharedPreferenceUtils.Setting_GetData(this, getString(R.string.LibrarySetting_FilterFileExtension_DOC), false));
            PowerPointSwitch.setChecked(SharedPreferenceUtils.Setting_GetData(this, getString(R.string.LibrarySetting_FilterFileExtension_PPT), false));
            ExcelSwitch.setChecked(SharedPreferenceUtils.Setting_GetData(this, getString(R.string.LibrarySetting_FilterFileExtension_XLS), false));

            PdfSwitch.setChecked(SharedPreferenceUtils.Setting_GetData(this, getString(R.string.LibrarySetting_FilterFileExtension_PDF), true));
            TxtSwitch.setChecked(SharedPreferenceUtils.Setting_GetData(this, getString(R.string.LibrarySetting_FilterFileExtension_TXT), true));

            DocumentSwitch.setChecked(SharedPreferenceUtils.Setting_GetData(this, getString(R.string.LibrarySetting_ShowLocalDrive_Document), true));
            DownloadSwitch.setChecked(SharedPreferenceUtils.Setting_GetData(this, getString(R.string.LibrarySetting_ShowLocalDrive_Download), true));
        }
        catch (Exception ex){
            Log.e(TAG, ex.toString());
        }
    }
}
