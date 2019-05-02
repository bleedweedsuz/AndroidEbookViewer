package zkagazapahtnajusz.paperproject.com.paperproject.Utilities;

import android.util.Log;
import java.io.File;

public class FileHelper {

    private static final String TAG = "File Helper";

    public static File SaveFile(String _dir, String fileStr){
        try {
            File file = new File(_dir);
            if (!file.exists()) {
                file.mkdirs();
            }

            File _bufferFile = new File(_dir , fileStr);
            if(_bufferFile.exists()){
                _bufferFile.delete();
            }
            Log.i(TAG, "SAVE TO FILE:" + _bufferFile.getAbsolutePath());
            return _bufferFile;
        }
        catch (Exception ex){
            Log.e(TAG, ex.toString());
            return null;
        }
    }
}
