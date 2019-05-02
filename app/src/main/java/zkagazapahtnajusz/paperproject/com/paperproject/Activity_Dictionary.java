package zkagazapahtnajusz.paperproject.com.paperproject;

import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentSnapshot;

import org.json.JSONObject;

import java.util.Iterator;

import zkagazapahtnajusz.paperproject.com.paperproject.Utilities.DBSqliteHelper;
import zkagazapahtnajusz.paperproject.com.paperproject.Utilities.FirebaseUtils;
import zkagazapahtnajusz.paperproject.com.paperproject.Utilities.Utils;

public class Activity_Dictionary extends AppCompatActivity{
    private static final String TAG = "Activity_Dictionary";
    Toolbar toolbar;
    TextView title;
    StringRequest stringRequest;
    RequestQueue requestQueue;
    boolean isDownloading = false;
    ParsingJsonData parsingJsonData;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity__dictionary);
        toolbar = findViewById(R.id.toolBar);
        title = findViewById(R.id.updateDictionaryText);
        setSupportActionBar(toolbar);
        if(getSupportActionBar() != null){
            getSupportActionBar().setDisplayShowTitleEnabled(false);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId() == android.R.id.home){
            finish();
        }
        return super.onOptionsItemSelected(item);
    }

    public void updateDictionary(View view){
        try{
            if(isDownloading){
                Toast.makeText(this, "Download is already running", Toast.LENGTH_SHORT).show();
                return;
            }

            new AlertDialog.Builder(this)
                    .setTitle("Alert!")
                    .setMessage("Are you sure want to update dictionary database? \n\n*this will remove all old data (this will take few minutes depending on your internet connection)")
                    .setNegativeButton(android.R.string.no, null)
                    .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface arg0, int arg1) {
                            startDownloadingPackage();
                        }
                    }).create().show();
        }
        catch (Exception ex){
            Log.e(TAG, ex.getMessage());
        }
    }



    @Override
    public void onBackPressed() {
        if(isDownloading) {
            new AlertDialog.Builder(this)
                    .setMessage("Download is still running. Still want to exit?")
                    .setNegativeButton(android.R.string.no, null)
                    .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface arg0, int arg1) {
                            Activity_Dictionary.super.onBackPressed();
                        }
                    }).create().show();
        }
        else{
            super.onBackPressed();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if(requestQueue != null){
            requestQueue.cancelAll(TAG);
        }

        if(parsingJsonData !=null){
            parsingJsonData.cancel(true);
        }

    }

    private void startDownloadingPackage(){
        isDownloading = true;
        String Root = "dictionary/dictionary";
        FirebaseUtils.FirestoreGetData(Root, new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                if(documentSnapshot.exists()){
                    Utils.SetProgressDialogIndeterminate(Activity_Dictionary.this, "downloading it will take few minutes", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            if(requestQueue != null){
                                requestQueue.cancelAll(TAG);
                            }
                        }
                    });

                    final String jsonDownload = documentSnapshot.getString("download");
                    //Http Request -->
                    requestQueue = Volley.newRequestQueue(Activity_Dictionary.this);
                    stringRequest = new StringRequest(
                            Request.Method.GET,
                            jsonDownload,
                            new Response.Listener<String>() {
                                @Override
                                public void onResponse(String response) {
                                    if(response != null && !response.equals("")){
                                        Utils.UnSetProgressDialogIndeterminate();
                                        updateDictionaryDatabase(response, title);
                                    }
                                }
                            },
                            new Response.ErrorListener() {
                                @Override
                                public void onErrorResponse(VolleyError error) {
                                    Utils.UnSetProgressDialogIndeterminate();
                                    isDownloading = false;
                                    Toast.makeText(Activity_Dictionary.this, error.getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            }
                    );
                    stringRequest.setTag(TAG);
                    requestQueue.add(stringRequest);
                }
            }
        }, new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Utils.UnSetProgressDialogIndeterminate();
                isDownloading = false;
                Log.e(TAG, e.getMessage());
            }
        });
    }

    private void updateDictionaryDatabase(final String jsonString, TextView textView){
        try {
            parsingJsonData = new ParsingJsonData(jsonString, this, textView, new ParsingJsonFinished() {
                @Override
                public void Finished() {
                    isDownloading = false;
                }
            });
            parsingJsonData.execute();
        }
        catch (Exception ex){
            Log.e(TAG, ex.toString());
        }
    }
}

interface ParsingJsonFinished{
    void Finished();
}

class ParsingJsonData extends AsyncTask<String, Integer, String> {
    private static final String TAG = "ParsingJsonData";
    private String jsonString;
    private Context context;
    private TextView title;
    private ParsingJsonFinished parsingJsonFinished;

    ParsingJsonData(String jsonString, Context context, TextView title, ParsingJsonFinished parsingJsonFinished) {
        this.jsonString = jsonString;
        this.context = context;
        this.title = title;
        this.parsingJsonFinished = parsingJsonFinished;
    }

    @Override
    protected String doInBackground(String... strings) {
        //Json string parsing
        //Clear Database
        SQLiteOpenHelper helper = new DBSqliteHelper(context, context.getString(R.string.SQLITE_DATABASE), null, context.getResources().getInteger(R.integer.SQLITE_DATABSE_VERSION));
        SQLiteDatabase sqLiteDatabase = helper.getWritableDatabase();

        //CLEAR ALL FROM DATAbASE
        sqLiteDatabase.execSQL("delete from dictionary");
        try {
            //JSONArray jsonArray = new JSONArray(jsonString);

            JSONObject jsonObject = new JSONObject(jsonString);
            int total = jsonObject.length();
            int i=0;
            for (Iterator<?> iterator = jsonObject.keys(); iterator.hasNext();) {
                if(isCancelled()){break;}
                String Title = (String) iterator.next();
                String Description = jsonObject.getString(Title);

                //Insert into database
                ContentValues contentValues = new ContentValues();
                contentValues.put("title", Title);
                contentValues.put("description", Description);

                //Execute SQL
                sqLiteDatabase.insert("dictionary", null, contentValues);

                int count = i + 1;
                publishProgress(total, count);

                i++;
            }
            parsingJsonFinished.Finished();
        }
        catch (Exception ex){
            Log.e(TAG, ex.getMessage());
        }
        return null;
    }

    @Override
    protected void onProgressUpdate(Integer... values) {
        super.onProgressUpdate(values);
        int total = values[0], count = values[1];

        String _buffer ="updating " + count + "/" + total + "\nwait for few minutes while updating";
        title.setText(_buffer);
        if(count >= total){_buffer = "Dictionary Data updated "+ total; title.setText(_buffer);}
    }
}
