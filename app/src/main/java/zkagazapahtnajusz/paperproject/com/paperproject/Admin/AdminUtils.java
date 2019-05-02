package zkagazapahtnajusz.paperproject.com.paperproject.Admin;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.ScrollView;
import android.widget.TextView;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;

import zkagazapahtnajusz.paperproject.com.paperproject.R;
import zkagazapahtnajusz.paperproject.com.paperproject.Utilities.FirebaseFunctionsUtils;
import zkagazapahtnajusz.paperproject.com.paperproject.Utilities.FirebaseUtils;
import zkagazapahtnajusz.paperproject.com.paperproject.Utilities.Utils;

public class AdminUtils extends AppCompatActivity {
    private static final String TAG = "AdminUtils";
    TextView consoleView;
    Toolbar toolbar;
    ScrollView scrollView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_utils);
        consoleView = findViewById(R.id.consoleView);
        toolbar = findViewById(R.id.toolBar);
        setSupportActionBar(toolbar);
        if(getSupportActionBar() != null){
            getSupportActionBar().setDisplayShowTitleEnabled(false);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }

        scrollView = findViewById(R.id.scrollview);
        scrollView.fullScroll(View.FOCUS_DOWN);
    }

    public void AddBooks(View view){
        consoleView.append("Adding books...\n");
        Utils.GenerateRandombookAndAdd(this, false, false, this);
    }

    public void AddNepaliBooks(View view){
        consoleView.append("Adding Nepali books...\n");
        for(int i=0;i<5;i++) {
            Utils.GenerateRandombookAndAdd(this, true, false, this);
        }
    }

    public void AddPrivateBook(View view){
        consoleView.append("Adding books...\n");
        Utils.GenerateRandombookAndAdd(this, false, true, this);
    }

    public void AddPrivateNepaliBook(View view){
        consoleView.append("Adding Nepali books...\n");
        Utils.GenerateRandombookAndAdd(this, true, true, this);
    }

    public void UnSubscribe_Users(View view){
        try{
            consoleView.append("UnSubscribing Users...\n");
            String Root= getString(R.string.FIRESTORE_ROOT_USERDETAILS);
            CollectionReference collectionReference = FirebaseFirestore.getInstance().collection(Root);

            FirebaseUtils.FirestoreGetAllData(collectionReference, this, new EventListener<QuerySnapshot>() {
                @Override
                public void onEvent(QuerySnapshot queryDocumentSnapshots, FirebaseFirestoreException e) {
                    if(!queryDocumentSnapshots.isEmpty()){
                        int count =0;
                        String data = "";
                        for(DocumentChange doc : queryDocumentSnapshots.getDocumentChanges()){
                            if(doc.getType() == DocumentChange.Type.ADDED){
                                String UID = doc.getDocument().getString(getString(R.string.F_USERDETAILS_UID));
                                FirebaseFunctionsUtils.UnSubscribeTopic_User(UID);
                                count++;
                                data += "   UnSubscribing Users " + UID + "  >> " + count + "\n";
                            }
                        }
                        AddToConsole(data);
                    }
                }
            });
        }
        catch (Exception ex){
            consoleView.append(ex.toString());
            Log.e(TAG, ex.toString());
        }
    }

    public void UnSubscribe_Books(View view){
        try{
            consoleView.append("UnSubscribing Books...\n");
            String Root= getString(R.string.FIRESTORE_ROOT_BOOKS);
            CollectionReference collectionReference = FirebaseFirestore.getInstance().collection(Root);
            FirebaseUtils.FirestoreGetAllData(collectionReference, this, new EventListener<QuerySnapshot>() {
                @Override
                public void onEvent(QuerySnapshot queryDocumentSnapshots, FirebaseFirestoreException e) {
                    if(!queryDocumentSnapshots.isEmpty()){
                        int count =0;
                        String data = "";
                        for(DocumentChange doc : queryDocumentSnapshots.getDocumentChanges()){
                            if(doc.getType() == DocumentChange.Type.ADDED){
                                String BookID = doc.getDocument().getString(getString(R.string.F_BOOKS_BOOKID));
                                FirebaseFunctionsUtils.UnSubscribeTopic_Book(BookID);
                                count++;
                                data += "   UnSubscribing Books " + BookID + "  >> " + count + "\n";
                            }
                        }
                        AddToConsole(data);
                    }
                }
            });
        }
        catch (Exception ex){
            consoleView.append(ex.toString());
            Log.e(TAG, ex.toString());
        }
    }

    public void AddToConsole(String data){
        consoleView.append(data);
        scrollView.fullScroll(View.FOCUS_DOWN);
    }
}
