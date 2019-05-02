package zkagazapahtnajusz.paperproject.com.paperproject.ReaderEngine;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.NumberPicker;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import java.io.FileOutputStream;
import java.util.Locale;

import zkagazapahtnajusz.paperproject.com.paperproject.R;
import zkagazapahtnajusz.paperproject.com.paperproject.Utilities.Utils;

public class Reader_EPUB_BottomMenu_Maker extends Fragment {
    private static final String TAG = "Fragment_BottomMenu_Mak";

    WebView webView;
    public enum BottomMenuType { PAGE, TEXT, DISPLAY, BRIGHTNESS, SNAPSHOT}
    private BottomMenuType bottomMenuType;

    TextView pageCounter, gotoTitle;
    SeekBar pageJumpSeekBar; boolean isSeekBarUsing =false;
    View jumpTo;
    ImageView gotoIcon;
    Reader_EPUB reader_epub;

    //Display
    int currentDisplayVal=0;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        if(bottomMenuType == BottomMenuType.PAGE) {
            View view = inflater.inflate(R.layout.fragment_bottom_menu_page, container, false);
            initPage(view);
            return view;
        }
        else if(bottomMenuType == BottomMenuType.TEXT) {
            View view = inflater.inflate(R.layout.fragment_bottom_menu_text, container, false);
            initText(view);
            return view;
        }
        else if(bottomMenuType == BottomMenuType.DISPLAY) {
            View view = inflater.inflate(R.layout.fragment_bottom_menu_display, container, false);
            initDisplay(view);
            return view;
        }
        else if(bottomMenuType == BottomMenuType.BRIGHTNESS) {
            View view = inflater.inflate(R.layout.fragment_bottom_menu_brightness, container, false);
            return view;
        }
        else if(bottomMenuType == BottomMenuType.SNAPSHOT) {
            View view = inflater.inflate(R.layout.fragment_bottom_menu_snapshot, container, false);
            initSnapshot(view);
            return view;
        }
        else{
            Log.e(TAG, "NULL NO BOTTOM MENU TYPE");
            return null;
        }
    }

    public void setBottomMenuType(BottomMenuType bottomMenuType){
        this.bottomMenuType = bottomMenuType;
    }

    public WebView getWebView() {
        return webView;
    }

    public void setWebView(WebView webView) {
        this.webView = webView;
    }

    private void initPage(View view){
        try{
            int currentPageInt = Reader_EPUB.currentPageStatic;
            pageCounter = view.findViewById(R.id.pageCounter);
            pageJumpSeekBar = view.findViewById(R.id.pageJumpSeekBar);
            jumpTo = view.findViewById(R.id.jumpTo);

            gotoTitle = view.findViewById(R.id.gotoTitle);
            gotoIcon = view.findViewById(R.id.gotoIcon);

            if(Reader_EPUB.isPageGenerate) {
                pageJumpSeekBar.setEnabled(true);
                jumpTo.setEnabled(true);
                gotoIcon.setImageResource(R.drawable.icon_goto);
                gotoTitle.setTextColor(Color.BLACK);

                String currentRawPageStatic = String.format(Locale.US, "%.2f", Double.valueOf(Reader_EPUB.currentRawPageStatic));
                String CP = currentRawPageStatic + "/100 %";
                pageCounter.setText(CP);
                pageJumpSeekBar.setProgress(currentPageInt);
            }
            else{
                gotoIcon.setImageResource(R.drawable.icon_goto_dim);
                pageJumpSeekBar.setEnabled(false);
                jumpTo.setEnabled(false);
            }

            //SeekBar
            pageJumpSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                @Override
                public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                    String CP = progress + "/100 %";
                    pageCounter.setText(CP);
                }

                @Override
                public void onStartTrackingTouch(SeekBar seekBar) {
                    isSeekBarUsing =true;
                }

                @Override
                public void onStopTrackingTouch(SeekBar seekBar) {
                    isSeekBarUsing =false;
                    if(Reader_EPUB.isPageGenerate){
                        Float buffer = seekBar.getProgress() * 1.0f;
                        Float current = Utils.map(buffer,0.0f,100.0f,50.0f,100.0f);
                        Float currentPage = current / 100;
                        webView.loadUrl("javascript:BookPagination(" + currentPage + ")");
                    }
                }
            });

            //JumpTo View
            jumpTo.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //region Jump To Code
                    int currentPage = Reader_EPUB.currentPageStatic;
                    View view = LayoutInflater.from(getActivity()).inflate(R.layout.spinner_dialog_custom,null);
                    final NumberPicker numberPicker = view.findViewById(R.id.numberPicker);
                    numberPicker.setMinValue(0);
                    numberPicker.setMaxValue(100);
                    numberPicker.setValue(currentPage);

                    AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                    builder.setView(view);
                    builder.setTitle("Go To Page");
                    builder.setPositiveButton("Done", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            Float buffer = numberPicker.getValue() * 1.0f;
                            Float current = Utils.map(buffer,0.0f,100.0f,50.0f,100.0f);
                            Float currentPage = current / 100;
                            webView.loadUrl("javascript:BookPagination(" + currentPage + ")");

                        }
                    });
                    builder.setOnDismissListener(new DialogInterface.OnDismissListener() {
                        @Override
                        public void onDismiss(DialogInterface dialog) {
                            Utils.EnableFullScreen(getActivity());
                        }
                    });
                    builder.create().show();
                    //endregion
                }
            });
        }
        catch (Exception ex){
            Log.e(TAG, ex.getMessage());
        }
    }

    public void initPageAfterLoaded(){
        try {
            pageCounter.setEnabled(true);
            pageJumpSeekBar.setEnabled(true);
            jumpTo.setEnabled(true);

            gotoTitle.setTextColor(Color.BLACK);
            gotoIcon.setImageResource(R.drawable.icon_goto);

            int currentPageInt = Reader_EPUB.currentPageStatic;
            String currentRawPageStatic = String.format(Locale.US, "%.2f", Double.valueOf(Reader_EPUB.currentRawPageStatic));
            String CP = currentRawPageStatic + "/100 %";
            pageCounter.setText(CP);
            pageJumpSeekBar.setProgress(currentPageInt);
        }
        catch (Exception ex){
            Log.e(TAG, ex.toString());
        }
    }

    public void initPageChanges(){
        try{
            if(Reader_EPUB.isPageGenerate) {
                int currentPageInt = Reader_EPUB.currentPageStatic;
                String currentRawPageStatic = String.format(Locale.US, "%.2f", Double.valueOf(Reader_EPUB.currentRawPageStatic));
                String CP = currentRawPageStatic + "/100 %";
                pageCounter.setText(CP);
                if(!isSeekBarUsing) {
                    pageJumpSeekBar.setProgress(currentPageInt);
                }
            }
        }
        catch (Exception ex){
            Log.e(TAG, ex.toString());
        }
    }

    private void initText(View view){
        final TextView progressText = view.findViewById(R.id.fontTextView);
        SeekBar seekBarFontSize = view.findViewById(R.id.SeekBar_FontSize);
        View MoreView = view.findViewById(R.id.MoreView);

        final int minValue = 14;

        //Init Font Size First 16px;
        seekBarFontSize.setProgress(2);
        seekBarFontSize.setMax(6);

        seekBarFontSize.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if(fromUser){
                    //Change Font Size
                    String value = (progress + minValue) + "px";
                    webView.loadUrl("javascript:ChangeFontSize('" + value +"')");
                    progressText.setText("Font Size " + value);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        MoreView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showEPUBTextMore();
            }
        });
    }

    private void initDisplay(View view){
        try {
            final TabLayout tabLayout = view.findViewById(R.id.tabBottomMenuDisplay);
            tabLayout.addTab(tabLayout.newTab());
            tabLayout.addTab(tabLayout.newTab());
            tabLayout.addTab(tabLayout.newTab());
            tabLayout.addTab(tabLayout.newTab());
            tabLayout.addTab(tabLayout.newTab());

            TabLayout.Tab Day = tabLayout.getTabAt(0);
            Day.setCustomView(R.layout.tab_day);

            TabLayout.Tab Night = tabLayout.getTabAt(1);
            Night.setCustomView(R.layout.tab_night);

            TabLayout.Tab Sepia = tabLayout.getTabAt(2);
            Sepia.setCustomView(R.layout.tab_sepia);

            TabLayout.Tab Blue = tabLayout.getTabAt(3);
            Blue.setCustomView(R.layout.tab_blue);

            TabLayout.Tab Crumble = tabLayout.getTabAt(4);
            Crumble.setCustomView(R.layout.tab_crumble);

            tabLayout.getTabAt(currentDisplayVal).select();

            tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
                @Override
                public void onTabSelected(TabLayout.Tab tab) {
                    if(tab.getPosition() == 1 || tab.getPosition() == 3){
                        tabLayout.setSelectedTabIndicatorColor(Color.WHITE);
                    }
                    else{
                        tabLayout.setSelectedTabIndicatorColor(Color.BLACK);
                    }
                    //Set background color in WebView
                    initDisplaySelected(tab.getPosition());
                }
                @Override
                public void onTabUnselected(TabLayout.Tab tab) {

                }
                @Override
                public void onTabReselected(TabLayout.Tab tab) {

                }
            });
        }
        catch (Exception ex){
            Log.e(TAG,  ex.toString());
        }
    }

    public void setDisplay(int index){
        currentDisplayVal = index;
    }

    public void initBrightness(){
        try{
            View view = getView();
            if(!Utils.checkSystemWrite(getActivity())){
                Utils.openSystemWriteIntent(getActivity());
            }
            final int currentBrightnessValue = Utils.getScreenBrightness(getActivity());
            final SeekBar SeekBar_Brightness= view.findViewById(R.id.SeekBar_Brightness);
            final TextView ProgressText = view.findViewById(R.id.BrightnessText);

            //region Check Box
            final CheckBox checkBox = view.findViewById(R.id.Auto_Manual_Check);
            checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView,final boolean isChecked) {
                    Utils.setScreenBrightnessMode(getActivity(), isChecked);
                    final int tempCurrentBrightnessValue = Utils.getScreenBrightness(getActivity());
                    SeekBar_Brightness.post(new Runnable() {
                        @Override
                        public void run() {
                            if(!isChecked) {if(!Utils.checkSystemWrite(getActivity())){ checkBox.setChecked(true); Utils.openSystemWriteIntent(getActivity()); }}

                            SeekBar_Brightness.setProgress(tempCurrentBrightnessValue);
                            SeekBar_Brightness.setEnabled(!isChecked);
                            String pText = "Brightness " + (Utils.map(tempCurrentBrightnessValue, 0, 255, 1, 100)) + " %";
                            ProgressText.setText(pText);
                        }
                    });
                }
            });
            //endregion

            checkBox.setChecked(Utils.getScreenBrightnessMode(getActivity()));

            //region Progress Bar Text
            String pText = "Brightness " + (Utils.map(currentBrightnessValue, 0, 255, 1, 100)) + " %";
            ProgressText.setText(pText);
            //endregion

            //region SeekBar
            SeekBar_Brightness.setProgress(4);
            SeekBar_Brightness.setMax(251);
            SeekBar_Brightness.post(new Runnable() {
                @Override
                public void run() {SeekBar_Brightness.setProgress(currentBrightnessValue);}
            });

            final int offset = 4;

            SeekBar_Brightness.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                @Override
                public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                    if(fromUser) {
                        int brightnessValue = offset + progress;
                        Utils.setScreenBrightness(getActivity(), brightnessValue);
                        String pText = "Brightness " + (Utils.map(brightnessValue, 0, 255, 0, 100)) + " %";
                        ProgressText.setText(pText);
                    }
                }

                @Override
                public void onStartTrackingTouch(SeekBar seekBar) {

                }

                @Override
                public void onStopTrackingTouch(SeekBar seekBar) {

                }
            });
            //endregion
        }
        catch (Exception ex){
            Log.e(TAG, ex.toString());
        }
    }

    private void initSnapshot(View view){
        try{
            View snapShot = view.findViewById(R.id.snapCamera);
            ImageView snapShotIcon = view.findViewById(R.id.snapIcon);
            snapShotIcon.animate().rotation(360).setDuration(500).start();

            snapShot.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    try{
                        String filename = "snapshot_" +Utils.UUIDToken() + ".jpg";
                        String rootDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).getAbsolutePath() + "/" + filename;
                        Bitmap bitmap = Utils.getBitmapFromView(webView);
                        FileOutputStream fileOutputStream = new FileOutputStream(rootDir);
                        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fileOutputStream);
                        fileOutputStream.flush();
                        fileOutputStream.close();

                        Toast.makeText(getActivity(), "Save in \"" + rootDir +"\"", Toast.LENGTH_SHORT).show();
                    }
                    catch (Exception ex){
                        Log.e(TAG, ex.toString());
                    }
                }
            });
        }
        catch (Exception ex){
            Log.e(TAG, ex.toString());
        }
    }

    //Private Methods
    private void initDisplaySelected(int position){
        if(position == 0){//Day
            webView.loadUrl("javascript:DisplayMode('1');");
            webView.setBackgroundColor(Color.WHITE);
            Reader_EPUB.CurrentDisplayMode = "1";
        }
        else if(position == 1){//Night
            webView.loadUrl("javascript:DisplayMode('2');");
            webView.setBackgroundColor(Color.BLACK);
            Reader_EPUB.CurrentDisplayMode = "2";
        }
        else if(position == 2){//Sepia
            webView.loadUrl("javascript:DisplayMode('3');");
            webView.setBackgroundColor(Color.parseColor("#f9f0d6"));
            Reader_EPUB.CurrentDisplayMode = "3";
        }
        else if(position == 3){//Blue
            webView.loadUrl("javascript:DisplayMode('4');");
            webView.setBackgroundColor(Color.parseColor("#17406d"));
            Reader_EPUB.CurrentDisplayMode = "4";
        }
        else if(position == 4){//Crumble
            webView.loadUrl("javascript:DisplayMode('5');");
            webView.setBackgroundColor(Color.parseColor("#dcdad7"));
            Reader_EPUB.CurrentDisplayMode = "5";
        }
        if(Reader_EPUB.isCurrentPageBookmark){if(Reader_EPUB.CurrentDisplayMode.equals("2") || Reader_EPUB.CurrentDisplayMode.equals("4")){Reader_EPUB.img_bookmark.setImageResource(R.drawable.icon_bookmark_white);}else{Reader_EPUB.img_bookmark.setImageResource(R.drawable.icon_bookmark);}}else{Reader_EPUB.img_bookmark.setImageResource(R.drawable._transparent);}

    }

    private void showEPUBTextMore(){
        try {
            View mainLayout = getLayoutInflater().inflate(R.layout.fragment_epub_text_more, null);
            final TextView FF_Roboto, FF_karla, FF_Lora, FF_Rubik, FF_Ubuntu, FF_Nunito;
            final TextView M_Normal, M_Wide, M_Narrow;

            FF_Roboto = mainLayout.findViewById(R.id.FF_Roboto);
            FF_karla = mainLayout.findViewById(R.id.FF_karla);
            FF_Lora = mainLayout.findViewById(R.id.FF_Lora);
            FF_Rubik = mainLayout.findViewById(R.id.FF_Rubik);
            FF_Ubuntu = mainLayout.findViewById(R.id.FF_Ubuntu);
            FF_Nunito = mainLayout.findViewById(R.id.FF_Nunito);

            M_Normal = mainLayout.findViewById(R.id.M_Normal);
            M_Wide = mainLayout.findViewById(R.id.M_Wide);
            M_Narrow = mainLayout.findViewById(R.id.M_Narrow);

            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setView(mainLayout);
            builder.setPositiveButton("Close", null);
            builder.setOnDismissListener(new DialogInterface.OnDismissListener() {
                @Override
                public void onDismiss(DialogInterface dialog) {
                    Utils.EnableFullScreen(getActivity());
                }
            });
            AlertDialog alertDialog = builder.create();

            setListenerFF(alertDialog, FF_Roboto, FF_karla, FF_Lora, FF_Rubik, FF_Ubuntu, FF_Nunito);
            setListenerM(alertDialog, M_Normal, M_Wide, M_Narrow);

            alertDialog.show();
        }
        catch (Exception ex){
            Log.e(TAG, ex.toString());
        }
    }

    private void setListenerFF(final AlertDialog alertDialog, TextView FF_Roboto, TextView FF_karla, TextView FF_Lora, TextView FF_Rubik, TextView FF_Ubuntu, TextView FF_Nunito){
        FF_Roboto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                webView.loadUrl("javascript:ChangeFont('1');");
                alertDialog.dismiss();
            }
        });

        FF_karla.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                webView.loadUrl("javascript:ChangeFont('2');");
                alertDialog.dismiss();
            }
        });

        FF_Lora.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                webView.loadUrl("javascript:ChangeFont('3');");
                alertDialog.dismiss();
            }
        });

        FF_Rubik.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                webView.loadUrl("javascript:ChangeFont('4');");
                alertDialog.dismiss();
            }
        });

        FF_Ubuntu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                webView.loadUrl("javascript:ChangeFont('5');");
                alertDialog.dismiss();
            }
        });

        FF_Nunito.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                webView.loadUrl("javascript:ChangeFont('6');");
                alertDialog.dismiss();
            }
        });
    }

    private void setListenerM(final AlertDialog alertDialog, TextView M_Normal, TextView M_Wide, TextView M_Narrow){
        M_Normal.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(Reader_EPUB.CurrentMarginType.equals("1")){
                    alertDialog.dismiss();
                    return;
                }

                Reader_EPUB.CurrentMarginType = "1";
                //webView.loadUrl("javascript:ChangeMargin('1');");

                if(reader_epub != null){ reader_epub.ReloadHtml(); }

                alertDialog.dismiss();
            }
        });

        M_Wide.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(Reader_EPUB.CurrentMarginType.equals("2")){
                    alertDialog.dismiss();
                    return;
                }
                Reader_EPUB.CurrentMarginType = "2";
                //webView.loadUrl("javascript:ChangeMargin('2');");
                if(reader_epub != null){ reader_epub.ReloadHtml(); }
                alertDialog.dismiss();
            }
        });

        M_Narrow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(Reader_EPUB.CurrentMarginType.equals("3")){
                    alertDialog.dismiss();
                    return;
                }

                Reader_EPUB.CurrentMarginType = "3";
                //webView.loadUrl("javascript:ChangeMargin('3');");
                if(reader_epub != null){ reader_epub.ReloadHtml(); }
                alertDialog.dismiss();
            }
        });
    }
}
