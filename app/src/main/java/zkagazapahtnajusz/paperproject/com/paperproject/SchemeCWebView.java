package zkagazapahtnajusz.paperproject.com.paperproject;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import zkagazapahtnajusz.paperproject.com.paperproject.Utilities.Utils;

public class SchemeCWebView extends AppCompatActivity {

    private static final String TAG = "SchemeCWebView";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scheme_cweb_view);

        if(savedInstanceState == null){
            String data = getIntent().getDataString().replace("schemecwebview://","");
            if(data.equals("1")){
                Utils.OpenWebView(this,"Term Of Service",getString(R.string.HTML_PAGES_TERMS_CONDITIONS), false);
            }
            else if(data.equals("2")){
                Utils.OpenWebView(this,"Privacy And Policy",getString(R.string.HTML_PAGES_PRIVACY_POLICY), false);
            }
        }
        finish();
    }
}
