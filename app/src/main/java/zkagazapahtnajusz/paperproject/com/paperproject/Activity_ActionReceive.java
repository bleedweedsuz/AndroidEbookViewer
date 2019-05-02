package zkagazapahtnajusz.paperproject.com.paperproject;

import android.content.Intent;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import zkagazapahtnajusz.paperproject.com.paperproject.Model.BookItems;
import zkagazapahtnajusz.paperproject.com.paperproject.Utilities.RealPathUtil;
import zkagazapahtnajusz.paperproject.com.paperproject.Utilities.Utils;

public class Activity_ActionReceive extends AppCompatActivity {

    private static final String TAG = "Activity_ActionReceive";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity__action_receive);
        Intent intent = getIntent();
        Uri data = intent.getData();
        try {
            if (intent.getType().equals("application/epub+zip")) {
                String path = RealPathUtil.getRealPath(this, data);
                if(path != null && !path.equals("")){OpenEpub(path);}
            }
            else if (intent.getType().equals("application/pdf")) {
                String path = RealPathUtil.getRealPath(this, data);
                if(path != null && !path.equals("")){OpenPDF(path);}
            }
            else if (intent.getType().equals("text/plain")) {
                String path = RealPathUtil.getRealPath(this, data);
                if(path != null && !path.equals("")){OpenText(path);}
            }

            Intent i = new Intent(this, Activity_SignIn.class);
            i.putExtra("FLAG", "OPENFILE");
            startActivity(i);
            finish();
        }
        catch (Exception ex){
            Toast.makeText(this, ex.getMessage(), Toast.LENGTH_SHORT).show();
            Log.e(TAG, ex.toString());
            finish();
        }
    }

    private void OpenEpub(String path){
        String FileName = Utils.getFileNameFromPath(path);
        BookItems bookItems = new BookItems("","",path,FileName,"",path,0.0,"",null,BookItems.BookFlag.Purchased,BookItems.FileType.EPUB,BookItems.StorageType.LOCAL);
        bookItems.Ext = "EPUB";
        bookItems.LocalFileURL = path;
        Utils.currentBookItemsFromOpen = bookItems;
    }

    private void OpenPDF(String path){
        String FileName = Utils.getFileNameFromPath(path);
        BookItems bookItems = new BookItems("","",path,FileName,"",path,0.0,"",null,BookItems.BookFlag.Purchased,BookItems.FileType.PDF,BookItems.StorageType.LOCAL);
        bookItems.Ext = "PDF";
        bookItems.LocalFileURL = path;
        Utils.currentBookItemsFromOpen = bookItems;
    }

    private void OpenText(String path){
        String FileName = Utils.getFileNameFromPath(path);
        BookItems bookItems = new BookItems("","",path,FileName,"",path,0.0,"",null,BookItems.BookFlag.Purchased,BookItems.FileType.TXT,BookItems.StorageType.LOCAL);
        bookItems.Ext = "TXT";
        bookItems.LocalFileURL = path;
        Utils.currentBookItemsFromOpen = bookItems;
    }
}
