package zkagazapahtnajusz.paperproject.com.paperproject.Utilities;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.util.Log;
import android.widget.Toast;

import zkagazapahtnajusz.paperproject.com.paperproject.R;

public class NetworkManagerUtils {
    private static final String TAG = "NetworkManagerUtils";

    public enum NetworkType { WIFI, MOBILE, NOTHING}

    private static NetworkType NetworkConnectionType(Context context){
        NetworkType networkType = NetworkType.NOTHING;
        try{
            ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo networkInfo = cm.getActiveNetworkInfo();
            if(networkInfo != null && networkInfo.isConnectedOrConnecting()){
                boolean isWifi = networkInfo.getType() == ConnectivityManager.TYPE_WIFI;
                boolean isMobile = networkInfo.getType() == ConnectivityManager.TYPE_MOBILE;

                if(isWifi){
                    networkType = NetworkType.WIFI;
                }
                else if(isMobile){
                    networkType = NetworkType.MOBILE;
                }
                else{
                    networkType = NetworkType.NOTHING;
                }
            }
            return networkType;
        }
        catch (Exception ex){
            Log.e(TAG, ex.toString());
            return networkType;
        }
    }

    private static boolean CheckWifiState(Context context){
        try {
            WifiManager wifi = (WifiManager)context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
            return wifi.isWifiEnabled();
        }
        catch (NullPointerException ex){
            return false;
        }
        catch (Exception ex){
            return  false;
        }
    }

    public static boolean LoadServerData(Context context){
        NetworkType networkType = NetworkConnectionType(context);
        // Only WiFi
        if(SharedPreferenceUtils.Setting_GetData(context, context.getString(R.string.UseWifiOnlySetting), true)){
            if(networkType == NetworkType.WIFI){
                return true;
            }
            else {
                Toast.makeText(context, "No internet connection.", Toast.LENGTH_SHORT).show();
                return false;
            }
        }
        // Mobile & WiFi
        else{
            if(networkType == NetworkType.WIFI || networkType == NetworkType.MOBILE){
                return true;
            }
            else{
                Toast.makeText(context, "No internet connection.", Toast.LENGTH_SHORT).show();
                return false;
            }
        }
    }
}
