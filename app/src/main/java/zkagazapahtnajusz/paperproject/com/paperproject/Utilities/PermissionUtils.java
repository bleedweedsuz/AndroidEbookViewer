package zkagazapahtnajusz.paperproject.com.paperproject.Utilities;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.support.v4.app.ActivityCompat;

public class PermissionUtils {
    private static String TAG = "PERMISSION UTILS";
    private Activity activity;
    private int Permission_All = 100;

    public PermissionUtils(Activity activity){
        this.activity = activity;
    }
    public void CheckPermission(){
        String[] Permissions = {Manifest.permission.WRITE_SETTINGS, Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.SEND_SMS, Manifest.permission.INTERNET, Manifest.permission.ACCESS_NETWORK_STATE, Manifest.permission.ACCESS_WIFI_STATE };
        if(!hasPermissions(activity, Permissions)){
            ActivityCompat.requestPermissions(activity, Permissions, Permission_All);
        }
    }
    private boolean hasPermissions(Context context, String... permissions){
        if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.M && context!=null && permissions!=null){
            for(String permission: permissions){
                if(ActivityCompat.checkSelfPermission(context, permission)!= PackageManager.PERMISSION_GRANTED){
                    return  false;
                }
            }
        }
        return true;
    }
}
