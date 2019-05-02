package zkagazapahtnajusz.paperproject.com.paperproject.ReaderEngine;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.DialogInterface;
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
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.Toast;

import com.github.barteksc.pdfviewer.PDFView;
import com.suke.widget.SwitchButton;

import java.util.HashMap;
import java.util.Map;

import zkagazapahtnajusz.paperproject.com.paperproject.R;
import zkagazapahtnajusz.paperproject.com.paperproject.Utilities.SharedPreferenceUtils;
import zkagazapahtnajusz.paperproject.com.paperproject.Utilities.Utils;
import zkagazapahtnajusz.paperproject.com.paperproject.ViewPagerAdapterBottomMenu;

public class Reader_PDF_BottomMenu extends Fragment {
    private static final String TAG = "Reader_PDF_BottomMenu";

    ViewPager viewPager;
    TabLayout tabLayout;
    ViewPagerAdapterBottomMenu adapter;
    RelativeLayout bottomMenuSettingLayout;
    ImageView settingImage;

    PDFView pdfView;
    PDFView.Configurator configurator;

    private Reader_PDF reader_pdf = null;
    public Reader_PDF_BottomMenu_Maker page, display,  brightness, snapshot;

    public void setReader_pdf(Reader_PDF reader_pdf) {
        this.reader_pdf = reader_pdf;
    }

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

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        adapter = new ViewPagerAdapterBottomMenu(getActivity().getSupportFragmentManager());

        page = new Reader_PDF_BottomMenu_Maker();
        page.setBottomMenuType(Reader_PDF_BottomMenu_Maker.BottomMenuType.PAGE);
        page.setPdfView(pdfView);
        page.initConfig(configurator);
        adapter.addFragment(page);

        display = new Reader_PDF_BottomMenu_Maker();
        display.setBottomMenuType(Reader_PDF_BottomMenu_Maker.BottomMenuType.DISPLAY);
        display.setPdfView(pdfView);
        display.initConfig(configurator);
        adapter.addFragment(display);

        brightness = new Reader_PDF_BottomMenu_Maker();
        brightness.setBottomMenuType(Reader_PDF_BottomMenu_Maker.BottomMenuType.BRIGHTNESS);
        brightness.setPdfView(pdfView);
        brightness.initConfig(configurator);
        adapter.addFragment(brightness);

        snapshot = new Reader_PDF_BottomMenu_Maker();
        snapshot.setBottomMenuType(Reader_PDF_BottomMenu_Maker.BottomMenuType.SNAPSHOT);
        snapshot.setPdfView(pdfView);
        snapshot.initConfig(configurator);
        adapter.addFragment(snapshot);

        viewPager.setAdapter(adapter);
        tabLayout.setupWithViewPager(viewPager);

        TabLayout.Tab tabCall = tabLayout.getTabAt(0);
        if (tabCall != null) {
            tabCall.setIcon(R.drawable.tab_page);
        }

        TabLayout.Tab tabCall2 = tabLayout.getTabAt(1);
        if (tabCall2 != null) {
            tabCall2.setIcon(R.drawable.tab_display);
        }

        TabLayout.Tab tabCall3 = tabLayout.getTabAt(2);
        if (tabCall3 != null) {
            tabCall3.setIcon(R.drawable.tab_brightness);
        }

        TabLayout.Tab tabCall4 = tabLayout.getTabAt(3);
        if (tabCall4 != null) {
            tabCall4.setIcon(R.drawable.tab_camera);
        }

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
            public void onClick(View view) {
                showPDFSettings();
            }
        });
    }

    private void showPDFSettings(){
        try {
            View view = getLayoutInflater().inflate(R.layout.fragment_pdf_settings, null);

            final SwitchButton Antialiasing_Switch, Double_Switch, AutoSpacing_Switch, Scrollable_Switch;
            final Spinner Orientation_Spinner, PageFitPolicy_Spinner;

            Antialiasing_Switch = view.findViewById(R.id.Antialiasing_Switch);
            Double_Switch = view.findViewById(R.id.Double_Switch);
            AutoSpacing_Switch = view.findViewById(R.id.AutoSpacing_Switch);
            Scrollable_Switch = view.findViewById(R.id.Scrollable_Switch);

            Orientation_Spinner = view.findViewById(R.id.Orientation_Spinner);
            PageFitPolicy_Spinner = view.findViewById(R.id.PageFitPolicy_Spinner);

            //Load Settings
            loadSettings(Antialiasing_Switch, Double_Switch, AutoSpacing_Switch, Scrollable_Switch, Orientation_Spinner, PageFitPolicy_Spinner);

            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setView(view);
            builder.setOnDismissListener(new DialogInterface.OnDismissListener() {
                @Override
                public void onDismiss(DialogInterface dialog) {
                    Utils.EnableFullScreen(getActivity());
                }
            });
            builder.setPositiveButton("Save", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {try {Map<String, String> map = new HashMap<>();map.put(getActivity().getString(R.string.PDF_SETTING_ANTIALIASING), String.valueOf(Antialiasing_Switch.isChecked()));map.put(getActivity().getString(R.string.PDF_SETTING_DOUBLETAP), String.valueOf(Double_Switch.isChecked()));map.put(getActivity().getString(R.string.PDF_SETTING_AUTOSPACING), String.valueOf(AutoSpacing_Switch.isChecked()));map.put(getActivity().getString(R.string.PDF_SETTING_SCROLLABLE), String.valueOf(Scrollable_Switch.isChecked()));map.put(getActivity().getString(R.string.PDF_SETTING_ORIENTATION), Orientation_Spinner.getSelectedItem().toString());map.put(getActivity().getString(R.string.PDF_SETTING_PAGEFITPOLICY), PageFitPolicy_Spinner.getSelectedItem().toString());SharedPreferenceUtils.UpdateData(getActivity(), map);Toast.makeText(getActivity(), "Save in config", Toast.LENGTH_SHORT).show();int currentPage = pdfView.getCurrentPage();reader_pdf.loadPdf(currentPage); } catch (Exception ex){Log.e(TAG, ex.toString());} }
            });
            builder.setNegativeButton("Cancel", null);
            builder.setNeutralButton("Default", null);

            AlertDialog alertDialog = builder.create();
            alertDialog.setOnShowListener(new DialogInterface.OnShowListener() {
                @Override
                public void onShow(DialogInterface dialog) {
                    Button negativeButton = ((AlertDialog)dialog).getButton(AlertDialog.BUTTON_NEUTRAL);
                    negativeButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {Antialiasing_Switch.setChecked(true);Double_Switch.setChecked(true);AutoSpacing_Switch.setChecked(false);Scrollable_Switch.setChecked(true);Utils.selectSpinnerItemByValue(Orientation_Spinner, "Horizontal");Utils.selectSpinnerItemByValue(PageFitPolicy_Spinner, "Width");}
                    });
                }
            });
            alertDialog.show();
        }
        catch (Exception ex){
            Log.e(TAG, ex.toString());
        }
    }

    private void loadSettings(SwitchButton Antialiasing_Switch, SwitchButton Double_Switch, SwitchButton AutoSpacing_Switch, SwitchButton Scrollable_Switch, Spinner Orientation_Spinner, Spinner PageFitPolicy_Spinner){
        try {
            Antialiasing_Switch.setChecked(SharedPreferenceUtils.Setting_GetData(getContext(), getActivity().getString(R.string.PDF_SETTING_ANTIALIASING), true));
            Double_Switch.setChecked(SharedPreferenceUtils.Setting_GetData(getContext(), getActivity().getString(R.string.PDF_SETTING_DOUBLETAP), true));
            AutoSpacing_Switch.setChecked(SharedPreferenceUtils.Setting_GetData(getContext(), getActivity().getString(R.string.PDF_SETTING_AUTOSPACING), false));
            Scrollable_Switch.setChecked(SharedPreferenceUtils.Setting_GetData(getContext(), getActivity().getString(R.string.PDF_SETTING_SCROLLABLE), true));
            Utils.selectSpinnerItemByValue(Orientation_Spinner, SharedPreferenceUtils.Setting_GetData_Str(getContext(), getActivity().getString(R.string.PDF_SETTING_ORIENTATION) ,"Horizontal"));
            Utils.selectSpinnerItemByValue(PageFitPolicy_Spinner, SharedPreferenceUtils.Setting_GetData_Str(getContext(), getActivity().getString(R.string.PDF_SETTING_PAGEFITPOLICY), "Width"));
        }
        catch (Exception ex){
            Log.e(TAG, ex.toString());
        }
    }

    public void setPdfView(PDFView pdfView){
        this.pdfView = pdfView;
    }

    public void initConfig(PDFView.Configurator configurator) {
        this.configurator = configurator;
    }
}
