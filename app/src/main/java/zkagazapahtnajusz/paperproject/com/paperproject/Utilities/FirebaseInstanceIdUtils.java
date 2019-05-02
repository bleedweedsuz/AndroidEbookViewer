package zkagazapahtnajusz.paperproject.com.paperproject.Utilities;

import android.app.Activity;
import android.util.Log;

import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.FirebaseInstanceIdService;

public class FirebaseInstanceIdUtils extends FirebaseInstanceIdService {
    private static final String TAG = "FirebaseInstanceIdUtils";

    @Override
    public void onTokenRefresh() {
        try {
            String token = FirebaseInstanceId.getInstance().getToken();
            FirebaseUtils.sendTokenToServer(token, (Activity) getApplicationContext());
        }
        catch (Exception ex){
            Log.e(TAG, ex.toString());
        }
    }
}
