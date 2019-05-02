package zkagazapahtnajusz.paperproject.com.paperproject.Utilities;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.Map;

import zkagazapahtnajusz.paperproject.com.paperproject.R;

public class SharedPreferenceUtils {

    public static void UpdateData(Context context, Map<String, String> map){
        String refrenceStr = context.getString(R.string.SHARED_KEY) + "_" + FirebaseUtils.firebaseAuth.getCurrentUser().getUid();
        SharedPreferences sharedPreferences = context.getSharedPreferences(refrenceStr, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        for(Map.Entry<String, String> entry : map.entrySet()){
            String K = entry.getKey();
            String V = entry.getValue();
            editor.putString(K, V);
        }
        editor.commit();
    }

    private static String GetData(Context context, String key){
        String refrenceStr = context.getString(R.string.SHARED_KEY) + "_" + FirebaseUtils.firebaseAuth.getCurrentUser().getUid();
        SharedPreferences sharedPreferences = context.getSharedPreferences(refrenceStr, Context.MODE_PRIVATE);
        return sharedPreferences.getString(key, "");
    }

    private static String GetData(Context context, String key, String fixVal){
        String refrenceStr = context.getString(R.string.SHARED_KEY) + "_" + FirebaseUtils.firebaseAuth.getCurrentUser().getUid();
        SharedPreferences sharedPreferences = context.getSharedPreferences(refrenceStr, Context.MODE_PRIVATE);
        return sharedPreferences.getString(key, fixVal);
    }

    private static String GetData(Context context, String key, String fixVal, String UID){
        String refrenceStr = context.getString(R.string.SHARED_KEY) + "_" + UID;
        SharedPreferences sharedPreferences = context.getSharedPreferences(refrenceStr, Context.MODE_PRIVATE);
        return sharedPreferences.getString(key, fixVal);
    }

    public static boolean Setting_GetData(Context context, String key, boolean fixVal, String UID){
        String str;
        str = SharedPreferenceUtils.GetData(context, key, String.valueOf(fixVal), UID);
        if(!str.equals("")){
            if(str.equals("true")){
                return true;
            }
            else{
                return false;
            }
        }
        else{
            return false;
        }
    }

    public static boolean Setting_GetData(Context context, String key, boolean fixVal){
        String str;
        str = SharedPreferenceUtils.GetData(context, key, String.valueOf(fixVal));
        if(!str.equals("")){
            if(str.equals("true")){
                return true;
            }
            else{
                return false;
            }
        }
        else{
            return false;
        }
    }

    public static boolean Setting_GetData(Context context, String key){
        String str;
        str = SharedPreferenceUtils.GetData(context, key);
        if(!str.equals("")){
            if(str.equals("true")){
                return true;
            }
            else{
                return false;
            }
        }
        else{
            return false;
        }
    }

    public static String Setting_GetData_Str(Context context, String key, String fixVal){
        return GetData(context, key, fixVal);
    }
}
