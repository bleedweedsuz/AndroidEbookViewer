package zkagazapahtnajusz.paperproject.com.paperproject;

import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.Timestamp;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import zkagazapahtnajusz.paperproject.com.paperproject.Utilities.FirebaseUtils;
import zkagazapahtnajusz.paperproject.com.paperproject.Utilities.Utils;

public class Activity_ReportProblem extends AppCompatActivity {
    private static final String TAG = "Activity_ReportProblem";

    Toolbar toolbar;
    EditText title, description;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity__report_problem);
        toolbar = findViewById(R.id.toolBar);
        setSupportActionBar(toolbar);
        if(getSupportActionBar() != null){
            getSupportActionBar().setDisplayShowTitleEnabled(false);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }

        title = findViewById(R.id._titledata);
        description = findViewById(R.id._descriptionData);
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
            Utils.SetProgressDialogIndeterminate(this, "submitting..");

            String TITLEDATA = title.getText().toString().trim();
            String DESCRIPTIONDATA = description.getText().toString().trim();

            if(TITLEDATA.length() <=0){
                Toast.makeText(Activity_ReportProblem.this, "no title entered.", Toast.LENGTH_SHORT).show();
                Utils.UnSetProgressDialogIndeterminate();
                return;
            }
            else if(DESCRIPTIONDATA.length() <=0){
                Toast.makeText(Activity_ReportProblem.this, "no description entered.", Toast.LENGTH_SHORT).show();
                Utils.UnSetProgressDialogIndeterminate();
                return;
            }

            String Root = getString(R.string.FIRESTORAGE_ROOT_REPORTS) + "/" + Utils.UUIDToken();
            Timestamp timeStamp = new Timestamp(Calendar.getInstance().getTime());
            Map<String, Object> map = new HashMap<>();
            map.put(getString(R.string.F_REPORTS_TITLE),TITLEDATA);
            map.put(getString(R.string.F_REPORTS_DESCRIPTION), DESCRIPTIONDATA);
            map.put(getString(R.string.F_REPORTS_STATUS), "New");
            map.put(getString(R.string.F_REPORTS_DATE), timeStamp);
            map.put(getString(R.string.F_REPORTS_USERID), FirebaseUtils.firebaseAuth.getCurrentUser().getUid());

            FirebaseUtils.FirestroreSetData(Root, map, new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void aVoid) {
                    Utils.UnSetProgressDialogIndeterminate();
                    Toast.makeText(Activity_ReportProblem.this, "Thank you for your report.", Toast.LENGTH_SHORT).show();
                    finish();
                }
            }, new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Utils.UnSetProgressDialogIndeterminate();
                    Toast.makeText(Activity_ReportProblem.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        }
        catch (Exception ex){
            Log.e(TAG, ex.toString());
        }
    }

    public void Clean_onClick(View view){
        try{
            title.setText("");
            description.setText("");
        }
        catch (Exception ex){
            Log.e(TAG, ex.toString());
        }
    }
}
