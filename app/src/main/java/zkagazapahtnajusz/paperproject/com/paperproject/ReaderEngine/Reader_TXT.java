package zkagazapahtnajusz.paperproject.com.paperproject.ReaderEngine;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;

import zkagazapahtnajusz.paperproject.com.paperproject.Model.CustomScrollView;
import zkagazapahtnajusz.paperproject.com.paperproject.R;
import zkagazapahtnajusz.paperproject.com.paperproject.Utilities.Utils;

public class Reader_TXT extends AppCompatActivity {

    private static final String TAG = "Reader_TXT";
    Toolbar toolbar;
    String title, path;
    TextView toolbarTitle, txtview;
    CustomScrollView scrollView;
    View gotoTop;

    boolean isTopBar = true;
    boolean isScroll = false;
    int slideAnimationD = 200;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reader_txt);

        toolbar = findViewById(R.id.toolBar);
        toolbarTitle = findViewById(R.id.toolbarTitle);
        txtview = findViewById(R.id.txtview);

        scrollView = findViewById(R.id.scrollview);
        gotoTop = findViewById(R.id.gotoTop);

        setSupportActionBar(toolbar);
        if(getSupportActionBar() != null){
            getSupportActionBar().setDisplayShowTitleEnabled(false);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }

        path = getIntent().getExtras().getString("path");
        title = getIntent().getExtras().getString("title");

        toolbarTitle.setText(title);
        toolbarTitle.setSelected(true);

        scrollView.setCustomOnScrollChangeListener(new CustomScrollView.CustomOnScrollChangeListener() {
            @Override
            public void onScrollUp() {
                checkScrolling();
            }

            @Override
            public void onScrollDown() {
                checkScrolling();
            }
        });

        scrollView.setCustomOnTapChangeListener(new CustomScrollView.CustomOnTapChangeListener() {
            @Override
            public void onTap() {
                toggleBar();
            }
        });

        loadTxt();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId() == android.R.id.home){
            finish();
        }
        return super.onOptionsItemSelected(item);
    }

    private void checkScrolling(){
        if(scrollView.getScrollY() > 20){
            isScroll = true;
            gotoTop.setVisibility(View.VISIBLE);
            gotoTop.animate().alpha(1).setDuration(500).setListener(new Animator.AnimatorListener() {
                @Override
                public void onAnimationStart(Animator animation) {

                }

                @Override
                public void onAnimationEnd(Animator animation) {
                    gotoTop.setVisibility(View.VISIBLE);
                }

                @Override
                public void onAnimationCancel(Animator animation) {

                }

                @Override
                public void onAnimationRepeat(Animator animation) {

                }
            }).start();
        }
        else{
            isScroll =false;
            gotoTop.animate().alpha(0.0f).setDuration(500).setListener(new Animator.AnimatorListener() {
                @Override
                public void onAnimationStart(Animator animation) {

                }

                @Override
                public void onAnimationEnd(Animator animation) {
                    gotoTop.setVisibility(View.INVISIBLE);
                }

                @Override
                public void onAnimationCancel(Animator animation) {

                }

                @Override
                public void onAnimationRepeat(Animator animation) {

                }
            }).start();
        }
    }

    private void loadTxt(){
        try{
            Utils.SetProgressDialogIndeterminate(this, "loading..");
            txtview.setText("");
            //region load txt data inside textview
            new Handler().post(new Runnable() {
                @Override
                public void run() {
                    try {
                        File file = new File(path);
                        long fileSize = file.length() / (1024 * 1024);
                        if(fileSize < 4) {
                            BufferedReader br = new BufferedReader(new FileReader(file));
                            String line;
                            while ((line = br.readLine()) != null) {
                                final String bufferLine = line + "\n";
                                //Load in Main loop
                                new Handler(Looper.getMainLooper()).post(new Runnable() {
                                    @Override
                                    public void run() {
                                        txtview.append(bufferLine);

                                    }
                                });
                            }
                            br.close();
                        }
                        else{
                            new Handler(Looper.getMainLooper()).post(new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(Reader_TXT.this,"Error Loading File, file size is greater than 4 mb", Toast.LENGTH_SHORT).show();
                                }
                            });
                        }
                        new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                scrollView.fullScroll(ScrollView.FOCUS_UP);
                                //scrollView.smoothScrollTo(0,0);
                                Utils.UnSetProgressDialogIndeterminate();
                            }
                        }, 1000);
                    }
                    catch (Exception ex){
                        new Handler(Looper.getMainLooper()).post(new Runnable() {
                            @Override
                            public void run() {
                                Utils.UnSetProgressDialogIndeterminate();
                            }
                        });
                        Log.e(TAG, ex.toString());
                    }
                }
            });
            //endregion
        }
        catch (Exception ex){
            Utils.UnSetProgressDialogIndeterminate();
            Log.e(TAG, ex.toString());
            txtview.setText(ex.getMessage().toString());
        }
    }

    public void reloadTxt(View view){
        loadTxt();
    }

    public void toggleBar(){
        if(isTopBar){
            slideUp2(toolbar);
            isTopBar  =false;
        }
        else{
            slideDown2(toolbar);
            isTopBar =true;
        }
    }

    //region Slide Animation View
    public void slideUp2(final View view){
        ObjectAnimator objectAnimator = ObjectAnimator.ofFloat(view, "translationY",0, -view.getHeight());
        objectAnimator.setDuration(slideAnimationD);
        objectAnimator.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {

            }

            @Override
            public void onAnimationEnd(Animator animation) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    view.setElevation(0);
                }
            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });
        objectAnimator.start();

    }

    public void slideDown2(final View view){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            view.setElevation(10);
        }
        ObjectAnimator objectAnimator = ObjectAnimator.ofFloat(view, "translationY", -view.getHeight(), 0);
        objectAnimator.setDuration(slideAnimationD);
        objectAnimator.start();
    }
    //endregion

    public void gotoTop_onClick(View view){
        try{
            scrollView.fullScroll(ScrollView.FOCUS_UP);
        }
        catch (Exception ex){
            Log.e(TAG, ex.toString());
        }
    }
}
