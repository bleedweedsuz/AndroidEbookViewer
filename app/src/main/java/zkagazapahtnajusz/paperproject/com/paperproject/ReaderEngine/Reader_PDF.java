package zkagazapahtnajusz.paperproject.com.paperproject.ReaderEngine;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.github.barteksc.pdfviewer.PDFView;
import com.github.barteksc.pdfviewer.listener.OnDrawListener;
import com.github.barteksc.pdfviewer.listener.OnErrorListener;
import com.github.barteksc.pdfviewer.listener.OnLoadCompleteListener;
import com.github.barteksc.pdfviewer.listener.OnPageChangeListener;
import com.github.barteksc.pdfviewer.listener.OnRenderListener;
import com.github.barteksc.pdfviewer.listener.OnTapListener;
import com.github.barteksc.pdfviewer.util.FitPolicy;

import zkagazapahtnajusz.paperproject.com.paperproject.R;
import zkagazapahtnajusz.paperproject.com.paperproject.Utilities.SharedPreferenceUtils;
import zkagazapahtnajusz.paperproject.com.paperproject.Utilities.Utils;

public class Reader_PDF extends AppCompatActivity {

    private static final String TAG = "Reader_PDF";
    Toolbar toolbar;
    View bottomView, reloadPDF;
    PDFView pdfView;
    String title, path;
    boolean isMenuShow = true;
    TextView toolbarTitle;
    Reader_PDF_BottomMenu reader_pdf_bottomMenu;

    public static int colorSelectIndex = 0;

    int slideAnimationD = 200;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reader_pdf);

        getWindow().getDecorView().setOnSystemUiVisibilityChangeListener(new View.OnSystemUiVisibilityChangeListener() {
            @Override
            public void onSystemUiVisibilityChange(int visibility) {
                if(visibility == 0){
                    Utils.DisableFullScreen(Reader_PDF.this);
                }
            }
        });
        bottomView = findViewById(R.id.bottomItems);
        toolbar = findViewById(R.id.toolBar);

        pdfView = findViewById(R.id.pdfview);

        toolbarTitle = findViewById(R.id.toolbarTitle);

        reloadPDF = findViewById(R.id.reloadPDF);
        reloadPDF.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loadPdf(-1);
            }
        });

        setSupportActionBar(toolbar);
        if(getSupportActionBar() != null){
            getSupportActionBar().setDisplayShowTitleEnabled(false);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }

        //Add Fragments
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

        reader_pdf_bottomMenu = new Reader_PDF_BottomMenu();
        reader_pdf_bottomMenu.setPdfView(pdfView);
        reader_pdf_bottomMenu.setReader_pdf(this);

        fragmentTransaction.add(R.id.bottomItems, reader_pdf_bottomMenu);
        fragmentTransaction.commit();

        path = getIntent().getExtras().getString("path");
        title = getIntent().getExtras().getString("title");

        toolbarTitle.setText(title);
        toolbarTitle.setSelected(true);
        loadPdf(-1);

        //ToggleMenu();
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if(!Utils.isFullScreen){
                    Utils.EnableFullScreen(Reader_PDF.this);
                }
                ToggleMenu();
            }
        }, 2000);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId() == android.R.id.home){
            finish();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume() {
        super.onResume();
        Utils.EnableFullScreen(this);
    }

    public void loadPdf(final int currentPage){
        try {
            Utils.SetProgressDialogIndeterminate(this, "loading..");

            String actualFilePath = "file://" + path;
            final PDFView.Configurator configurator = pdfView.fromUri(Uri.parse(actualFilePath));

            //Add Config To System
            reader_pdf_bottomMenu.initConfig(configurator);

            UpdateConfigurator(this, configurator);

            configurator.enableSwipe(true);
            configurator.pageSnap(true);
            configurator.defaultPage(0);
            configurator.onError(new OnErrorListener() {
                @Override
                public void onError(Throwable t) {
                    Utils.UnSetProgressDialogIndeterminate();
                    Toast.makeText(Reader_PDF.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
            configurator.onLoad(new OnLoadCompleteListener() {
                @Override
                public void loadComplete(int nbPages) {
                    reader_pdf_bottomMenu.page.initPage();
                }
            });

            configurator.onRender(new OnRenderListener() {
                @Override
                public void onInitiallyRendered(int nbPages) {
                    Utils.UnSetProgressDialogIndeterminate();
                    if(currentPage >-1){
                        pdfView.jumpTo(currentPage);
                    }
                }
            });

            //region config onpage change
            configurator.onPageChange(new OnPageChangeListener() {
                @Override
                public void onPageChanged(int page, int pageCount) {
                    reader_pdf_bottomMenu.page.setPageValue(page);
                }
            });
            //endregion

            configurator.onDraw(new OnDrawListener() {
                @Override
                public void onLayerDrawn(Canvas canvas, float pageWidth, float pageHeight, int displayedPage) {
                    if(colorSelectIndex == 0){
                        //White #ffffff
                        canvas.drawColor(Color.parseColor("#ffffff"), PorterDuff.Mode.MULTIPLY);
                        pdfView.setBackgroundColor(Color.parseColor("#1d1d1d"));
                    }
                    else if(colorSelectIndex == 1){
                        //Black #a1a1a1
                        canvas.drawColor(Color.parseColor("#aaaaaa"), PorterDuff.Mode.MULTIPLY);
                        pdfView.setBackgroundColor(Color.parseColor("#303030"));
                    }
                    else if(colorSelectIndex == 2){
                        //sepia #f9f0d6
                        canvas.drawColor(Color.parseColor("#f9f0d6"), PorterDuff.Mode.MULTIPLY);
                        pdfView.setBackgroundColor(Color.parseColor("#f9f0d6"));
                    }
                    else if(colorSelectIndex == 3){
                        //blue #17406d
                        canvas.drawColor(Color.parseColor("#badafd"), PorterDuff.Mode.MULTIPLY);
                        pdfView.setBackgroundColor(Color.parseColor("#badafd"));
                    }
                    else if(colorSelectIndex == 4){
                        //crumble #dcdad7
                        canvas.drawColor(Color.parseColor("#dcdad7"), PorterDuff.Mode.MULTIPLY);
                        pdfView.setBackgroundColor(Color.parseColor("#dcdad7"));
                    }
                }
            });

            configurator.onTap(new OnTapListener() {
                @Override
                public boolean onTap(MotionEvent e) {
                    if(!Utils.isFullScreen){
                        Utils.EnableFullScreen(Reader_PDF.this);
                        return false;
                    }
                    ToggleMenu();
                    return false;
                }
            });
            configurator.load();
        }
        catch (Exception ex){
            Log.e(TAG , ex.toString());
            Utils.UnSetProgressDialogIndeterminate();
            Toast.makeText(Reader_PDF.this, ex.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void ToggleMenu(){
        isMenuShow = !isMenuShow;
        if(isMenuShow){
            slideUp(bottomView);
            slideDown2(toolbar);
        }
        else{
            slideDown(bottomView);
            slideUp2(toolbar);
        }
    }

    public void UpdateConfigurator(Context context, PDFView.Configurator configurator){
        try {
            boolean Antialiasing_State = SharedPreferenceUtils.Setting_GetData(context, context.getString(R.string.PDF_SETTING_ANTIALIASING), true);
            boolean DoubleTap_State = SharedPreferenceUtils.Setting_GetData(context, context.getString(R.string.PDF_SETTING_DOUBLETAP), true);
            boolean AutoSpacing_State = SharedPreferenceUtils.Setting_GetData(context, context.getString(R.string.PDF_SETTING_AUTOSPACING), false);
            String Orientation_State = SharedPreferenceUtils.Setting_GetData_Str(context, context.getString(R.string.PDF_SETTING_ORIENTATION) ,"Horizontal");
            String PageFitPolicy_State = SharedPreferenceUtils.Setting_GetData_Str(context, context. getString(R.string.PDF_SETTING_PAGEFITPOLICY), "Width");

            //Antialiasing
            configurator.enableAntialiasing(Antialiasing_State);

            //Double Tap
            configurator.enableDoubletap(DoubleTap_State);

            //Auto Spacing
            if (AutoSpacing_State) {
                configurator.autoSpacing(true);
                pdfView.setBackgroundColor(Color.WHITE);
            }
            else{
                configurator.autoSpacing(false);
                configurator.spacing(4);
                pdfView.setBackgroundColor(Color.BLACK);
            }

            //Orientation State
            if (Orientation_State.equals("Vertical")) {
                configurator.swipeHorizontal(false);
            } else {
                configurator.swipeHorizontal(true);
            }

            //Page Fit Policy
            configurator.pageFitPolicy(FitPolicy.valueOf(PageFitPolicy_State.toUpperCase()));
        }
        catch (Exception ex){
            Log.e(TAG, ex.toString());
        }
    }

    //region Slide Animation View
    public void slideUp(View view){
        ObjectAnimator objectAnimator = ObjectAnimator.ofFloat(view, "translationY",view.getHeight(),0);
        objectAnimator.setDuration(slideAnimationD);
        objectAnimator.start();
    }

    public void slideDown(final View view){
        ObjectAnimator objectAnimator = ObjectAnimator.ofFloat(view, "translationY",0,view.getHeight());
        objectAnimator.setDuration(slideAnimationD);
        objectAnimator.start();
    }

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
        ObjectAnimator objectAnimator = ObjectAnimator.ofFloat(view, "translationY", -view.getHeight(), 0);
        objectAnimator.setDuration(slideAnimationD);
        objectAnimator.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {

            }

            @Override
            public void onAnimationEnd(Animator animation) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    view.setElevation(10);
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
    //endregion
}