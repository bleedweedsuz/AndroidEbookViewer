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
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.NumberPicker;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.github.barteksc.pdfviewer.PDFView;

import java.io.FileOutputStream;

import zkagazapahtnajusz.paperproject.com.paperproject.R;
import zkagazapahtnajusz.paperproject.com.paperproject.Utilities.Utils;

public class Reader_PDF_BottomMenu_Maker extends Fragment {

    private static final String TAG = "Fragment_BottomMenu_Mak";

    PDFView pdfView;
    PDFView.Configurator configurator;
    public enum BottomMenuType { PAGE, DISPLAY, BRIGHTNESS, SNAPSHOT}
    private BottomMenuType bottomMenuType;
    int pageCount=0;

    private SeekBar page_jumpToSeekBar;
    private View page_jumpTo;
    private TextView page_Counter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        if(bottomMenuType == BottomMenuType.PAGE) {
            View view = inflater.inflate(R.layout.fragment_bottom_menu_page, container, false);
            page_jumpToSeekBar = view.findViewById(R.id.pageJumpSeekBar);
            page_jumpTo = view.findViewById(R.id.jumpTo);
            page_Counter = view.findViewById(R.id.pageCounter);

            TextView textView = view.findViewById(R.id.gotoTitle);
            textView.setEnabled(true);
            textView.setTextColor(Color.BLACK);
            ((ImageView)view.findViewById(R.id.gotoIcon)).setImageResource(R.drawable.icon_goto);
            return view;
        }
        else if(bottomMenuType == BottomMenuType.DISPLAY) {
            View view = inflater.inflate(R.layout.fragment_bottom_menu_display, container, false);
            initDisplay(view);
            return view;
        }
        else if(bottomMenuType == BottomMenuType.BRIGHTNESS) {
            View view = inflater.inflate(R.layout.fragment_bottom_menu_brightness, container, false);
            //initBrightness(view);
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

    public void setPdfView(PDFView pdfView){
        this.pdfView = pdfView;
    }

    public void initConfig(PDFView.Configurator configurator){
        this.configurator = configurator;
    }

    public void initPage(){
        pageCount = pdfView.getPageCount();
        String counterStr = "Page 1/" + pageCount;
        page_Counter.setText(counterStr);
        page_jumpToSeekBar.setMax(pageCount);
        page_jumpToSeekBar.setProgress(1);

        //region SeekBar
        page_jumpToSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if(fromUser){
                    String counterStr = "Page " + progress + "/" + pageCount;
                    page_Counter.setText(counterStr);
                    pdfView.jumpTo(progress, false);
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

        //region Jump to
        page_jumpTo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                int currentPage = pdfView.getCurrentPage();
                View view = LayoutInflater.from(getActivity()).inflate(R.layout.spinner_dialog_custom,null);
                final NumberPicker numberPicker = view.findViewById(R.id.numberPicker);
                numberPicker.setMinValue(1);
                numberPicker.setMaxValue(pageCount);
                numberPicker.setValue(currentPage + 1);

                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                builder.setView(view);
                builder.setTitle("Go To Page");
                builder.setPositiveButton("Done", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        page_jumpToSeekBar.setProgress(numberPicker.getValue());
                        pdfView.jumpTo(numberPicker.getValue() - 1, false);
                    }
                });
                builder.create().show();
            }
        });
        //endregion
    }

    public void setPageValue(int page){
        String counterStr = "Page " + (page + 1) + "/" + pageCount;
        page_Counter.setText(counterStr);
        page_jumpToSeekBar.setProgress(page+1);
    }

    private void initBrightness(View view){
        try{
            if(!Utils.checkSystemWrite(getContext())){ Utils.openSystemWriteIntent(getContext()); }
            final int currentBrightnessValue = Utils.getScreenBrightness(getActivity());
            final SeekBar SeekBar_Brightness= view.findViewById(R.id.SeekBar_Brightness);
            final TextView ProgressText = view.findViewById(R.id.BrightnessText);

            //region Check Box
            final CheckBox checkBox = view.findViewById(R.id.Auto_Manual_Check);
            checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView,final boolean isChecked) {
                    Utils.setScreenBrightnessMode(getContext(), isChecked);
                    final int tempCurrentBrightnessValue = Utils.getScreenBrightness(getActivity());
                    SeekBar_Brightness.post(new Runnable() {
                        @Override
                        public void run() {
                            if(!isChecked) {if(!Utils.checkSystemWrite(getContext())){ checkBox.setChecked(true); Utils.openSystemWriteIntent(getContext()); }}

                            SeekBar_Brightness.setProgress(tempCurrentBrightnessValue);
                            SeekBar_Brightness.setEnabled(!isChecked);
                            String pText = "Brightness " + (Utils.map(tempCurrentBrightnessValue, 0, 255, 1, 100)) + " %";
                            ProgressText.setText(pText);
                        }
                    });
                }
            });
            //endregion

            checkBox.setChecked(Utils.getScreenBrightnessMode(getContext()));

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
                        Utils.setScreenBrightness(getContext(), brightnessValue);
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

            tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
                @Override
                public void onTabSelected(TabLayout.Tab tab) {
                    if(tab.getPosition() == 1){
                        tabLayout.setSelectedTabIndicatorColor(Color.WHITE);
                    }else{
                        tabLayout.setSelectedTabIndicatorColor(Color.BLACK);
                    }
                    Reader_PDF.colorSelectIndex = tab.getPosition();
                    if(pdfView.getScrollY() >=0){
                        //just some trick for redraw image
                        pdfView.setScrollX(1);
                        pdfView.setScrollX(0);
                    }
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
                        Bitmap bitmap = Utils.getBitmapFromView(pdfView);
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

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if(isVisibleToUser){
            if(bottomMenuType == BottomMenuType.BRIGHTNESS){
                initBrightness(getView());
            }
        }
    }
}
