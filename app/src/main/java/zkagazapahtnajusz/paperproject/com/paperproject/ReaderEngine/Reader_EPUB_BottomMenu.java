package zkagazapahtnajusz.paperproject.com.paperproject.ReaderEngine;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.Toast;

import com.suke.widget.SwitchButton;

import java.util.HashMap;
import java.util.Map;

import zkagazapahtnajusz.paperproject.com.paperproject.Activity_Dictionary;
import zkagazapahtnajusz.paperproject.com.paperproject.Model.CustomWebView;
import zkagazapahtnajusz.paperproject.com.paperproject.R;
import zkagazapahtnajusz.paperproject.com.paperproject.Utilities.SharedPreferenceUtils;
import zkagazapahtnajusz.paperproject.com.paperproject.Utilities.Utils;
import zkagazapahtnajusz.paperproject.com.paperproject.ViewPagerAdapterBottomMenu;

public class Reader_EPUB_BottomMenu extends Fragment {
    private static final String TAG = "Reader_PDF_BottomMenu";

    private ViewPager viewPager;
    private TabLayout tabLayout;
    private ViewPagerAdapterBottomMenu adapter;
    private RelativeLayout bottomMenuSettingLayout;
    private ImageView settingImage;

    private CustomWebView webView;

    private Reader_EPUB reader_epub = null;
    public Reader_EPUB_BottomMenu_Maker page, text, display, brightness, snap;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view =  getLayoutInflater().inflate(R.layout.activity__bottom_menu,container,false);
        viewPager = view.findViewById(R.id.viewPagerBottomMenu);
        tabLayout = view.findViewById(R.id.tabLayoutBottomMenu);
        bottomMenuSettingLayout = view.findViewById(R.id.bottomMenuSettingLayout);
        settingImage = view.findViewById(R.id.settingImage);
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        adapter = new ViewPagerAdapterBottomMenu(getActivity().getSupportFragmentManager());

        page = new Reader_EPUB_BottomMenu_Maker();
        page.setBottomMenuType(Reader_EPUB_BottomMenu_Maker.BottomMenuType.PAGE);
        page.setWebView(webView);
        adapter.addFragment(page);

        text = new Reader_EPUB_BottomMenu_Maker();
        text.setBottomMenuType(Reader_EPUB_BottomMenu_Maker.BottomMenuType.TEXT);
        text.reader_epub = reader_epub;
        text.setWebView(webView);

        adapter.addFragment(text);

        display = new Reader_EPUB_BottomMenu_Maker();
        display.setBottomMenuType(Reader_EPUB_BottomMenu_Maker.BottomMenuType.DISPLAY);
        display.setWebView(webView);
        adapter.addFragment(display);

        brightness = new Reader_EPUB_BottomMenu_Maker();
        brightness.setBottomMenuType(Reader_EPUB_BottomMenu_Maker.BottomMenuType.BRIGHTNESS);
        brightness.setWebView(webView);
        adapter.addFragment(brightness);

        snap = new Reader_EPUB_BottomMenu_Maker();
        snap.setBottomMenuType(Reader_EPUB_BottomMenu_Maker.BottomMenuType.SNAPSHOT);
        snap.setWebView(webView);
        adapter.addFragment(snap);

        viewPager.setAdapter(adapter);
        tabLayout.setupWithViewPager(viewPager);

        TabLayout.Tab tabCall = tabLayout.getTabAt(0);
        if (tabCall != null) {
            tabCall.setIcon(R.drawable.tab_page);
        }

        TabLayout.Tab tabCall2 = tabLayout.getTabAt(1);
        if (tabCall2 != null) {
            tabCall2.setIcon(R.drawable.tab_text);
        }

        TabLayout.Tab tabCall3 = tabLayout.getTabAt(2);
        if (tabCall3 != null) {
            tabCall3.setIcon(R.drawable.tab_display);
        }

        TabLayout.Tab tabCall4 = tabLayout.getTabAt(3);
        if (tabCall4 != null) {
            tabCall4.setIcon(R.drawable.tab_brightness);
        }

        TabLayout.Tab tabCall5 = tabLayout.getTabAt(4);
        if (tabCall5 != null) {
            tabCall5.setIcon(R.drawable.tab_camera);
        }

        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                if(position == 3 && brightness != null){
                    brightness.initBrightness();
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });

        //region Setting Button
        bottomMenuSettingLayout.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                switch (motionEvent.getAction()){
                    case MotionEvent.ACTION_DOWN:
                        settingImage.setImageResource(R.drawable.icon_setting_select);
                        break;
                    case MotionEvent.ACTION_UP:
                        view.performClick();
                        settingImage.setImageResource(R.drawable.icon_setting_unselect);
                        break;
                }
                return true;
            }
        });
        //endregion

        bottomMenuSettingLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {showEPUBSettings();}
        });
    }

    public CustomWebView getWebView() {
        return webView;
    }

    public void setWebView(CustomWebView webView) {
        this.webView = webView;
    }

    public Reader_EPUB getReader_epub() {
        return reader_epub;
    }

    public void setReader_epub(Reader_EPUB reader_epub) {
        this.reader_epub = reader_epub;
    }

    private void showEPUBSettings(){
        try {
            View mainLayout = getLayoutInflater().inflate(R.layout.fragment_epub_settings, null);

            final SwitchButton volumekeySwitch, pageCurlSwitch;
            final Spinner swipeOrientation;
            final View noteColor, highlightColor, clearViewCache, swipeOrientationContainer, updateDictionary;

            volumekeySwitch = mainLayout.findViewById(R.id.volumekeySwitch);
            pageCurlSwitch = mainLayout.findViewById(R.id.pageCurlSwitch);
            swipeOrientation = mainLayout.findViewById(R.id.swipeOrientation);
            noteColor = mainLayout.findViewById(R.id.noteColor);
            highlightColor = mainLayout.findViewById(R.id.highlightColor);
            clearViewCache = mainLayout.findViewById(R.id.clearViewCache);
            updateDictionary = mainLayout.findViewById(R.id.updateDictionary);
            swipeOrientationContainer = mainLayout.findViewById(R.id.swipeOrientationContainer);

            //Load Settings
            loadSetting(volumekeySwitch, pageCurlSwitch, swipeOrientation, noteColor, highlightColor, clearViewCache, swipeOrientationContainer);

            //add listener
            setListenerEPUBSetting(volumekeySwitch, pageCurlSwitch, swipeOrientation, noteColor, highlightColor, clearViewCache, swipeOrientationContainer, updateDictionary);

            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setView(mainLayout);
            builder.setOnDismissListener(new DialogInterface.OnDismissListener() {
                @Override
                public void onDismiss(DialogInterface dialog) {
                    Utils.EnableFullScreen(getActivity());
                }
            });
            builder.setPositiveButton("Save", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    //Save all
                    String NColor = String.format("#%06X", (0xFFFFFF & ((ColorDrawable) (noteColor.findViewById(R.id.noteColorView)).getBackground()).getColor()));
                    String HColor = String.format("#%06X", (0xFFFFFF & ((ColorDrawable) (highlightColor.findViewById(R.id.highlightColorView)).getBackground()).getColor()));
                    String swipeOrientationStr; if(pageCurlSwitch.isChecked()){swipeOrientationStr = "Horizontal";}else{swipeOrientationStr = swipeOrientation.getSelectedItem().toString();}
                    Map<String, String> map = new HashMap<>();
                    map.put(getActivity().getString(R.string.EPUB_SETTING_VOLUMEKEYS), String.valueOf(volumekeySwitch.isChecked()));
                    map.put(getActivity().getString(R.string.EPUB_SETTING_PAGECURLANIMATION), String.valueOf(pageCurlSwitch.isChecked()));
                    map.put(getActivity().getString(R.string.EPUB_SETTING_SWIPEORIENTATION), swipeOrientationStr);
                    map.put(getActivity().getString(R.string.EPUB_SETTING_NOTECOLOR), NColor);
                    map.put(getActivity().getString(R.string.EPUB_SETTING_HIGHLIGHTCOLOR), HColor);
                    SharedPreferenceUtils.UpdateData(getActivity(), map);

                    //Set Curl Effect in system
                    webView.setCurling(pageCurlSwitch.isChecked());

                    //Reload Color System
                    reader_epub.LoadHighlightAndNoteColor(true);
                    Toast.makeText(getActivity(), "New settings made", Toast.LENGTH_SHORT).show();
                }
            });
            builder.setNegativeButton("Close", null);
            AlertDialog alertDialog = builder.create();
            alertDialog.show();
        }
        catch (Exception ex){
            Log.e(TAG, ex.toString());
        }
    }

    private void loadSetting(SwitchButton volumekeySwitch, SwitchButton pageCurlSwitch, Spinner swipeOrientation, final View noteColor, final View highlightColor, View clearViewCache, View swipeOrientationContainer){
        try {
            volumekeySwitch.setChecked(SharedPreferenceUtils.Setting_GetData(getContext() ,getActivity().getString(R.string.EPUB_SETTING_VOLUMEKEYS), true));
            pageCurlSwitch.setChecked(SharedPreferenceUtils.Setting_GetData(getContext() ,getActivity().getString(R.string.EPUB_SETTING_PAGECURLANIMATION), false));
            Utils.selectSpinnerItemByValue(swipeOrientation, SharedPreferenceUtils.Setting_GetData_Str(getContext(), getActivity().getString(R.string.EPUB_SETTING_SWIPEORIENTATION) ,"Horizontal"));
            (highlightColor.findViewById(R.id.highlightColorView)).setBackgroundColor(Color.parseColor(SharedPreferenceUtils.Setting_GetData_Str(getContext() ,getActivity().getString(R.string.EPUB_SETTING_HIGHLIGHTCOLOR), "#6bdc87")));
            (noteColor.findViewById(R.id.noteColorView)).setBackgroundColor(Color.parseColor(SharedPreferenceUtils.Setting_GetData_Str(getContext() ,getActivity().getString(R.string.EPUB_SETTING_NOTECOLOR), "#ffba2f")));

            if(pageCurlSwitch.isChecked()){swipeOrientationContainer.setVisibility(View.GONE);} else{swipeOrientationContainer.setVisibility(View.VISIBLE);}

        }
        catch (Exception ex){
            Log.e(TAG, ex.getMessage());
        }
    }

    private void setListenerEPUBSetting(SwitchButton volumekeySwitch, SwitchButton pageCurlSwitch, final Spinner swipeOrientation, final View noteColor, final View highlightColor, View clearViewCache, final View swipeOrientationContainer, final View updateDictionary){
        try {
            /*
            noteColor.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String color ="#f2c875";
                    if(reader_epub.N_Color != null) {color = reader_epub.N_Color;}

                    new SpectrumDialog.Builder(getContext())
                            .setTitle("Select Note Color")
                            .setColors(R.array.noteColorPallet)
                            .setSelectedColor(Color.parseColor(color))
                            .setDismissOnColorSelected(false)
                            .setOutlineWidth(5)

                            .setOnColorSelectedListener(new SpectrumDialog.OnColorSelectedListener() {
                                @Override
                                public void onColorSelected(boolean positiveResult, int color) {
                                    if(positiveResult){
                                        String hexColor = String.format("#%06X", (0xFFFFFF & color));
                                        (noteColor.findViewById(R.id.noteColorView)).setBackgroundColor(Color.parseColor(hexColor));
                                    }
                                }
                            })
                            .build()
                            .show(getFragmentManager(), "NoteColor");
                }
            });

            highlightColor.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String color ="#ffdb00";
                    if(reader_epub.H_Color != null) {color = reader_epub.H_Color;}

                    new SpectrumDialog.Builder(getContext())
                            .setTitle("Select Highlight Color")
                            .setColors(R.array.highlightColorPallet)
                            .setSelectedColor(Color.parseColor(color))
                            .setDismissOnColorSelected(false)
                            .setOutlineWidth(5)
                            .setOnColorSelectedListener(new SpectrumDialog.OnColorSelectedListener() {
                                @Override
                                public void onColorSelected(boolean positiveResult, int color) {
                                    if(positiveResult){
                                        String hexColor = String.format("#%06X", (0xFFFFFF & color));
                                        (highlightColor.findViewById(R.id.highlightColorView)).setBackgroundColor(Color.parseColor(hexColor));
                                    }
                                }
                            })
                            .build()
                            .show(getFragmentManager(), "HighlightColor");
                }
            });
            */

            pageCurlSwitch.setOnCheckedChangeListener(new SwitchButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(SwitchButton view, boolean isChecked) {
                    if(isChecked){
                        swipeOrientationContainer.setVisibility(View.GONE);
                    }
                    else{
                        swipeOrientationContainer.setVisibility(View.VISIBLE);
                    }
                }
            });

            clearViewCache.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    webView.clearCache(true);
                    webView.clearHistory();
                    Toast.makeText(getContext(), "Cache Cleared", Toast.LENGTH_SHORT).show();
                }
            });

            updateDictionary.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    startActivity(new Intent( getActivity(), Activity_Dictionary.class));
                }
            });

        }
        catch (Exception ex){
            Log.e(TAG, ex.getMessage());
        }
    }
}
