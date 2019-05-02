package zkagazapahtnajusz.paperproject.com.paperproject.Fragments;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.suke.widget.SwitchButton;

import java.util.HashMap;
import java.util.Map;

import zkagazapahtnajusz.paperproject.com.paperproject.Activity_About;
import zkagazapahtnajusz.paperproject.com.paperproject.Activity_Dictionary;
import zkagazapahtnajusz.paperproject.com.paperproject.Activity_LibrarySetting;
import zkagazapahtnajusz.paperproject.com.paperproject.Activity_PushNotifications;
import zkagazapahtnajusz.paperproject.com.paperproject.Activity_ReportProblem;
import zkagazapahtnajusz.paperproject.com.paperproject.Activity_SignIn;
import zkagazapahtnajusz.paperproject.com.paperproject.Activity_StoreSettings;
import zkagazapahtnajusz.paperproject.com.paperproject.Activity_UserProfile;
import zkagazapahtnajusz.paperproject.com.paperproject.Activity_PrivacyAccount_Setting;
import zkagazapahtnajusz.paperproject.com.paperproject.Admin.AdminUtils;
import zkagazapahtnajusz.paperproject.com.paperproject.R;

import de.hdodenhof.circleimageview.CircleImageView;
import zkagazapahtnajusz.paperproject.com.paperproject.Utilities.FirebaseUtils;
import zkagazapahtnajusz.paperproject.com.paperproject.Utilities.NetworkManagerUtils;
import zkagazapahtnajusz.paperproject.com.paperproject.Utilities.SharedPreferenceUtils;
import zkagazapahtnajusz.paperproject.com.paperproject.Utilities.Utils;

/**
 * Created by Zw4R-TSTUD10 2018/4/1 [2018, April 1]
 */

public class Fragment_Menu extends Fragment implements View.OnClickListener, SwitchButton.OnCheckedChangeListener{
    private static String TAG = "FRAGMENT MENU";

    CircleImageView profileImage;
    LinearLayout buttonToActivityUserProfile;
    View LogOut, PushNotification, LibrarySetting, StoreSettings, DictionarySetting, AdminLogin, AccountPrivacy, PrivacyAndPolicy, TermOfService, Community, OpenSourceLibrary, Credits, Help, ReportAProblem;
    TextView UserFullName;
    SwitchButton wifiSwitch, appupdateSwitch;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_menu, container, false);
        InitializeView(view);
        return view;
    }

    private void InitializeView(View view){
        profileImage = view.findViewById(R.id.userImageOnFrag);
        buttonToActivityUserProfile = view.findViewById(R.id.buttonToActivityUserProfile);

        AdminLogin = view.findViewById(R.id.AdminLogin); AdminLogin.setOnClickListener(this);
        PushNotification = view.findViewById(R.id.PushNotification); PushNotification.setOnClickListener(this);
        LibrarySetting = view.findViewById(R.id.LibrarySetting); LibrarySetting.setOnClickListener(this);
        StoreSettings = view.findViewById(R.id.StoreSettings); StoreSettings.setOnClickListener(this);
        AccountPrivacy = view.findViewById(R.id.AccountPrivacy); AccountPrivacy.setOnClickListener(this);
        DictionarySetting = view.findViewById(R.id.DictionarySetting); DictionarySetting.setOnClickListener(this);

        Help = view.findViewById(R.id.Help); Help.setOnClickListener(this);
        ReportAProblem = view.findViewById(R.id.ReportAProblem); ReportAProblem.setOnClickListener(this);

        PrivacyAndPolicy = view.findViewById(R.id.PrivacyAndPolicy); PrivacyAndPolicy.setOnClickListener(this);
        TermOfService = view.findViewById(R.id.TermOfService); TermOfService.setOnClickListener(this);
        Community = view.findViewById(R.id.Community); Community.setOnClickListener(this);
        OpenSourceLibrary = view.findViewById(R.id.OpenSourceLibrary); OpenSourceLibrary.setOnClickListener(this);
        Credits = view.findViewById(R.id.Credits); Credits.setOnClickListener(this);

        LogOut = view.findViewById(R.id.LogOut); LogOut.setOnClickListener(this);

        UserFullName = view.findViewById(R.id.UserFullName);


        wifiSwitch = view.findViewById(R.id.wifiSwitch);
        appupdateSwitch = view.findViewById(R.id.appupdateSwitch);

        loadSharedPref();

        wifiSwitch.setOnCheckedChangeListener(this);
        appupdateSwitch.setOnCheckedChangeListener(this);
    }

    private void LoadProfile(){
        try {
            if(FirebaseUtils.firebaseAuth != null &&  FirebaseUtils.firebaseAuth.getCurrentUser().getDisplayName() != null) {
                String fullname = FirebaseUtils.firebaseAuth.getCurrentUser().getDisplayName();
                UserFullName.setText(fullname);
            }

            String UID = FirebaseUtils.firebaseAuth.getCurrentUser().getUid();
            String Root = getString(R.string.FIRESTORE_ROOT_USERDETAILS) + "/" + UID;
            FirebaseUtils.FirestoreGetData(Root, new OnSuccessListener<DocumentSnapshot>() {
                @Override
                public void onSuccess(DocumentSnapshot documentSnapshot) {
                    if (documentSnapshot.exists()) {
                        try {
                            String UserProfileLink = "";  try{ UserProfileLink = documentSnapshot.getString(getString(R.string.F_USERDETAILS_PROFILEURL));}catch (NullPointerException ex){Log.i(TAG , ex.toString());}

                            ColorDrawable colorDrawableInfoPlaceholder = new ColorDrawable(ContextCompat.getColor(getContext(), R.color.colorDrawableInfoPlaceholder));
                            ColorDrawable colorDrawableErrorPlaceholder = new ColorDrawable(ContextCompat.getColor(getContext(), R.color.colorDrawableErrorPlaceholder));
                            Utils.SetImageViaUri(
                                    getActivity(),
                                    UserProfileLink,
                                    profileImage,
                                    false,
                                    false,
                                    colorDrawableInfoPlaceholder,
                                    colorDrawableErrorPlaceholder,
                                    null
                            );
                        } catch (Exception ex) {
                            Log.e(TAG, ex.toString());
                        }
                    }
                }
            }, new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Log.e(TAG, e.toString());
                }
            });
        }
        catch (Exception ex){
            Log.e(TAG, ex.toString());
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        LoadProfile();
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        buttonToActivityUserProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getActivity(), Activity_UserProfile.class);
                getActivity().startActivity(intent);
            }
        });
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.AdminLogin: Admin_onClick(); break;
            case R.id.AccountPrivacy: AccountPrivacy_onClick(); break;
            case R.id.PushNotification: PushNotification_onClick(); break;
            case R.id.LibrarySetting: LibrarySetting_onClick(); break;
            case R.id.DictionarySetting: Dictionary_onClick(); break;
            case R.id.StoreSettings: StoreSettings_onClick(); break;

            case R.id.Help: Help_onClick(); break;
            case R.id.ReportAProblem: ReportAProblem_onClick(); break;

            case R.id.PrivacyAndPolicy: PrivacyAndPolicy_onClick(); break;
            case R.id.TermOfService: TermOfService_onClick(); break;
            case R.id.Community: Community_onClick(); break;
            case R.id.OpenSourceLibrary: OpenSourceLibrary_onClick(); break;
            case R.id.Credits: OpenCredit_onClick(); break;

            case R.id.LogOut: LogOut_onClick(); break;
            default:break;
        }
    }

    @Override
    public void onCheckedChanged(SwitchButton view, boolean isChecked) {
        if(view.getId() == R.id.wifiSwitch){
            Map<String, String> map = new HashMap<>();
            map.put(getString(R.string.UseWifiOnlySetting), isChecked + "");
            SharedPreferenceUtils.UpdateData(this.getContext(), map);
        }
        else if(view.getId() == R.id.appupdateSwitch){
            Map<String, String> map = new HashMap<>();
            map.put(getString(R.string.AppUpdatesSetting), isChecked + "");
            SharedPreferenceUtils.UpdateData(this.getContext(), map);
        }
    }

    private void loadSharedPref(){
        try{
            wifiSwitch.setChecked(SharedPreferenceUtils.Setting_GetData(this.getContext(), getString(R.string.UseWifiOnlySetting), true));
            appupdateSwitch.setChecked(SharedPreferenceUtils.Setting_GetData(this.getContext(), getString(R.string.AppUpdatesSetting), true));
        }
        catch (Exception ex){
            Log.e(TAG, ex.toString());
        }
    }

    private void LogOut_onClick(){
        if(FirebaseUtils.firebaseAuth.getInstance().getCurrentUser() != null){
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setTitle("Logout");
            builder.setMessage("Are you sure want to logout?");
            builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    FirebaseUtils.firebaseAuth.signOut();
                    Intent intent = new Intent(getActivity(), Activity_SignIn.class);
                    startActivity(intent);
                    getActivity().finish();
                }
            });
            builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    //Nothing
                }
            });
            builder.create().show();
        }
        else{
            Toast.makeText(getActivity(), "Error: No User Selected", Toast.LENGTH_SHORT).show();
        }
    }

    private void PushNotification_onClick(){
        Intent i = new Intent(getActivity(), Activity_PushNotifications.class);
        startActivity(i);
    }

    private void LibrarySetting_onClick(){
        Intent i = new Intent(getActivity(), Activity_LibrarySetting.class);
        startActivity(i);
    }

    private void StoreSettings_onClick(){
        Intent i = new Intent(getActivity(), Activity_StoreSettings.class);
        startActivity(i);
    }

    private void AccountPrivacy_onClick(){
        //region no internet toast
        if(!NetworkManagerUtils.LoadServerData(getContext())){
            return;
        }
        //endregion

        Intent i = new Intent(getActivity(), Activity_PrivacyAccount_Setting.class);
        startActivity(i);
    }

    private void Dictionary_onClick(){
        startActivity(new Intent(getActivity(), Activity_Dictionary.class));
    }

    public void Admin_onClick(){
        //region Open Admin
        RelativeLayout relativeLayout = new RelativeLayout(getActivity());
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        layoutParams.setMargins(30,30,30,30);
        relativeLayout.setLayoutParams(layoutParams);

        final EditText input = new EditText(getActivity());
        input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        input.setHint("Enter Password");
        input.setTextSize(14);
        input.setLayoutParams(layoutParams);

        relativeLayout.addView(input);

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("Admin Login");
        builder.setView(relativeLayout);
        builder.setPositiveButton("Submit", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String password = input.getText().toString().trim();
                //password ="pure";
                if(password.equals("pure")) {
                    startActivity(new Intent(getActivity(), AdminUtils.class));
                }
                else{
                    Toast.makeText(getActivity(), "Error: Login Password", Toast.LENGTH_SHORT).show();
                }
            }
        });
        builder.create().show();
        //endregion
    }

    private void ReportAProblem_onClick(){
        startActivity(new Intent(getActivity(), Activity_ReportProblem.class));
    }

    //region Web View
    private void Help_onClick(){
        Utils.OpenWebView(getContext(),"Help", "https://projectpaper-468fc.firebaseapp.com/help.html", true);
    }

    private void PrivacyAndPolicy_onClick(){
        Utils.OpenWebView(getContext(),"Privacy And Policy", getString(R.string.HTML_PAGES_PRIVACY_POLICY), true);
    }

    private void TermOfService_onClick(){
        Utils.OpenWebView(getContext(),"Term Of Service",getString(R.string.HTML_PAGES_TERMS_CONDITIONS), true);
    }

    private void Community_onClick(){
        Utils.OpenWebView(getContext(),"Community","https://projectpaper-468fc.firebaseapp.com/community.html", true);
    }

    private void OpenSourceLibrary_onClick(){
        Utils.OpenWebView(getContext(),"Open Source Library", "https://projectpaper-468fc.firebaseapp.com/opensourcelibrary.html", true);
    }


    //endregion

    private void OpenCredit_onClick(){
        startActivity(new Intent(getActivity(), Activity_About.class));
    }
}
