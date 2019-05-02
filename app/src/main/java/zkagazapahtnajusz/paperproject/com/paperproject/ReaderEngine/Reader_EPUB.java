package zkagazapahtnajusz.paperproject.com.paperproject.ReaderEngine;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.app.SearchManager;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.graphics.Color;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.util.Log;
import android.view.ActionMode;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.webkit.ConsoleMessage;
import android.webkit.JavascriptInterface;
import android.webkit.JsResult;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import zkagazapahtnajusz.paperproject.com.paperproject.Activity_UserPost;
import zkagazapahtnajusz.paperproject.com.paperproject.Model.BookItems;
import zkagazapahtnajusz.paperproject.com.paperproject.Model.CustomWebView;
import zkagazapahtnajusz.paperproject.com.paperproject.R;
import zkagazapahtnajusz.paperproject.com.paperproject.Utilities.DBSqliteHelper;
import zkagazapahtnajusz.paperproject.com.paperproject.Utilities.FirebaseUtils;
import zkagazapahtnajusz.paperproject.com.paperproject.Utilities.NetworkManagerUtils;
import zkagazapahtnajusz.paperproject.com.paperproject.Utilities.SharedPreferenceUtils;
import zkagazapahtnajusz.paperproject.com.paperproject.Utilities.Utils;

public class Reader_EPUB extends AppCompatActivity {
    private static final String TAG = "Reader_EPUB";
    public static Integer currentPageStatic = 0;
    public static Float currentRawPageStatic = 0.0f;
    public static boolean isPageGenerate = false;

    boolean isWebConsole = false;
    View bottomView;
    ImageView bookmarkImageView, bookmarkImageView2;

    public static ImageView img_bookmark;

    CustomWebView webView;
    Toolbar toolbar, toolbar2;
    TextView toolbarTitle;
    String bookid, path, title, pKey;
    boolean isMenuShow = true;
    public static boolean isCurrentPageBookmark;
    String currentPageBookmarkCFI = "";
    Reader_EPUB_BottomMenu reader_epub_bottomMenu;

    public ArrayList<TOCItem> tocItems = new ArrayList<>();
    public ArrayList<BookmarkItem> bookmarkItems = new ArrayList<>();
    public ArrayList<HighlightItem> highlightItems = new ArrayList<>();
    public ArrayList<NoteItem> noteItems = new ArrayList<>();

    public String H_Color =  null, N_Color = null;

    public String tempFileLocation = null;
    public static boolean readerFlag = true;
    public boolean isSelecting = false;
    public boolean isHighlightLoaded = false;
    public boolean isBookmarkLoaded = false;
    public boolean isBookStateLoaded = false;
    public boolean isNoteLoaded = false;
    public String CurrentSelectText, CurrentSelectCFIRange;
    public static String CurrentCFI = "";
    public BookItems.StorageType storageType;

    public enum LastInsideElementSelect {HIGHLIGHT, NOTE, NOTHING}
    public boolean insideElementSelectState = false;

    public RecyclerView searchRecyclerView = null;
    public List<SearchItems> searchItemsList = new ArrayList<>();
    public SearchItemsAdapter searchItemsAdapter;
    public ProgressBar searchProgressBar;
    public AlertDialog searchAlertDialog;
    public String searchQueryText = null;
    EditText searchQuery;
    public View searchOption, nextButton, previousButton, searchListButton;
    public ImageView searchOptionIcon, nextButtonIcon, previousButtonIcon, searchListButtonIcon;
    public int currentSearchSelect;
    TextView totalFound;
    boolean isSearchNextAvailable;
    public boolean searchOptionToggle = false;
    public boolean searchViewToggle = false;
    public String lastSelectSearchCfi = "";
    BookItems.BookFlag bookFlag = BookItems.BookFlag.Nothing;
    private boolean isFreeLocalFile = false;

    public static String CurrentMarginType = "1";
    public static String CurrentDisplayMode = "1";

    public String CurrentPath;
    public long CurrentFileSize;
    int slideAnimationD = 200;
    boolean isLinkClick = false;
    boolean isContextMenuOpen = false;

    RelativeLayout statusBarSpace;

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reader_epub);
        statusBarSpace = findViewById(R.id.statusBarSpace);
        bottomView = findViewById(R.id.bottomItems);
        toolbar = findViewById(R.id.toolBar);
        toolbar2 = findViewById(R.id.toolBar2);
        toolbarTitle = findViewById(R.id.toolbarTitle);
        bookmarkImageView = findViewById(R.id.bookmarkImageView);
        bookmarkImageView2 = findViewById(R.id.bookmarkImageView2);
        img_bookmark = bookmarkImageView2;
        searchOption = findViewById(R.id.SearchOption);
        searchOptionIcon = findViewById(R.id.searchOptionIcon);
        searchProgressBar = findViewById(R.id.progressBar);
        totalFound = findViewById(R.id.totalFound);
        webView = findViewById(R.id.webView);
        webView.getSettings().setJavaScriptEnabled(true);
        webView.getSettings().setAllowFileAccess(true);
        webView.getSettings().setAllowUniversalAccessFromFileURLs(true);
        nextButton = findViewById(R.id.NextButton);
        previousButton= findViewById(R.id.PreviousButton);
        searchListButton= findViewById(R.id.ListButton);
        searchQuery = findViewById(R.id.searchQueryText);
        nextButtonIcon = nextButton.findViewById(R.id.icon_next);nextButtonIcon.setImageResource(R.drawable.icon_next_unselect);
        previousButtonIcon = previousButton.findViewById(R.id.icon_prev); previousButtonIcon.setImageResource(R.drawable.icon_previous_unselect);
        searchListButtonIcon = searchListButton.findViewById(R.id.searchIconList); searchListButtonIcon.setImageResource(R.drawable.icon_list_unselect);

        //if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {webView.setWebContentsDebuggingEnabled(true);}

        WebChromeClient webChromeClient = new WebChromeClient(){
            @Override
            public boolean onJsAlert(WebView view, String url, String message, JsResult result) {
                return super.onJsAlert(view, url, message, result);
            }

            @Override
            public boolean onConsoleMessage(ConsoleMessage consoleMessage) {
                if(!isWebConsole){
                    return true;
                }
                return super.onConsoleMessage(consoleMessage);
            }
        };
        webView.setWebChromeClient(webChromeClient);
        webView.setCustomOnTapChangeListener(new CustomWebView.CustomOnTapChangeListener() {
            @Override
            public void onTap() {
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        TapEvent();
                    }
                }, 100);
            }
        });

        getWindow().getDecorView().setOnSystemUiVisibilityChangeListener(new View.OnSystemUiVisibilityChangeListener() {
            @Override
            public void onSystemUiVisibilityChange(int visibility) { if(visibility == 0){Utils.DisableFullScreen(Reader_EPUB.this);}}
        });

        webView.setCustomOnSwipeListener(new CustomWebView.CustomOnSwipeListener() {
            @Override
            public void onSwipeLeft() {
                String swipeOrientation = SharedPreferenceUtils.Setting_GetData_Str(Reader_EPUB.this , getString(R.string.EPUB_SETTING_SWIPEORIENTATION), "Horizontal");
                if(swipeOrientation.equals("Horizontal")) {
                    SwipeTrigger("L");
                }
            }

            @Override
            public void onSwipeRight() {
                String swipeOrientation = SharedPreferenceUtils.Setting_GetData_Str(Reader_EPUB.this , getString(R.string.EPUB_SETTING_SWIPEORIENTATION), "Horizontal");
                if(swipeOrientation.equals("Horizontal")) {
                    SwipeTrigger("R");
                }
            }

            @Override
            public void onSwipeUp() {
                String swipeOrientation = SharedPreferenceUtils.Setting_GetData_Str(Reader_EPUB.this , getString(R.string.EPUB_SETTING_SWIPEORIENTATION), "Horizontal");
                if(swipeOrientation.equals("Vertical")) {
                    SwipeTrigger("L");
                }
            }

            @Override
            public void onSwipeDown() {
                String swipeOrientation = SharedPreferenceUtils.Setting_GetData_Str(Reader_EPUB.this , getString(R.string.EPUB_SETTING_SWIPEORIENTATION), "Horizontal");
                if(swipeOrientation.equals("Vertical")) {
                    SwipeTrigger("R");
                }
            }
        });
        webView.setCurling(SharedPreferenceUtils.Setting_GetData(this ,this.getString(R.string.EPUB_SETTING_PAGECURLANIMATION), false));

        //Add Fragments
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

        reader_epub_bottomMenu = new Reader_EPUB_BottomMenu();
        reader_epub_bottomMenu.setReader_epub(this);
        reader_epub_bottomMenu.setWebView(webView);

        fragmentTransaction.add(R.id.bottomItems, reader_epub_bottomMenu);
        fragmentTransaction.commit();

        bookid = getIntent().getExtras().getString("bookid");
        path = getIntent().getExtras().getString("path");
        title = getIntent().getExtras().getString("title");
        pKey = getIntent().getExtras().getString("pKey");
        if(pKey.equals("")){bookFlag = BookItems.BookFlag.Free;}else {bookFlag = BookItems.BookFlag.Store;}

        storageType = BookItems.StorageType.valueOf(getIntent().getExtras().getString("storageType"));
        toolbarTitle.setText(title);
        toolbarTitle.setSelected(true);

        //region Search Option
        searchOption.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                searchViewToggle = true;
                ToggleSearchView();
            }
        });
        //endregion

        searchQuery.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if(hasFocus){
                    getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
                    //int BH = Utils.getStatusBarHeight(Reader_EPUB.this);
                    //int currHeight = Utils.px2Dp(85, getResources()) + BH;
                    //statusBarSpace.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, BH));
                    //toolbar2.setLayoutParams(new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, currHeight));
                }
                else{
                    //statusBarSpace.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 1));
                    //int currHeight = Utils.px2Dp(85, getResources());
                    //toolbar2.setLayoutParams(new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, currHeight));
                    //getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
                }
            }
        });

        searchQuery.setOnEditorActionListener(new EditText.OnEditorActionListener(){
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) { if(actionId == EditorInfo.IME_ACTION_DONE || actionId == EditorInfo.IME_ACTION_SEARCH){ Search_onClick(null);return true; }return false;}
        });
    }

    private void TapEvent(){
        if(isContextMenuOpen){
            isContextMenuOpen = false;
            return;
        }
        if(isLinkClick){
            isLinkClick = false;
            return;
        }
        if(!Utils.isFullScreen){
            Utils.EnableFullScreen(Reader_EPUB.this);
            return;
        }
        if(actionMode != null) {
            return;
        }
        if(searchOptionToggle){
            ToggleOnlySearchView();
            return;
        }
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {if(!insideElementSelectState){ToggleMenu();}insideElementSelectState = false;}
        }, 100);
    }

    @Override
    protected void onResume() {
        super.onResume();

        //Set Display Mode According To BookID
        LoadCurrentDisplayMode();

        if (!isHighlightLoaded) {isHighlightLoaded = true;LoadHighlightData();}
        if (!isBookmarkLoaded) {isBookmarkLoaded = true;LoadBookmarkData();}
        if (!isBookStateLoaded) {isBookStateLoaded = true;LoadBookstateData();}
        if (!isNoteLoaded) {isNoteLoaded = true;LoadNoteData();}

        if (readerFlag) {
            currentPageStatic = 0;
            isPageGenerate = false;
            if(storageType == BookItems.StorageType.CLOUD) {
                if(!pKey.equals("")){
                    //Server Encrypted File
                    Utils.UnCompressedFile(path, pKey, new Utils.ArchiveFinished() {
                        @Override
                        public void finished(String saveRoot, long fileSize) {
                            tempFileLocation = saveRoot;
                            LoadHtml(tempFileLocation, fileSize);
                        }
                    });
                }
                else{
                    //Server NonEncrypted File
                    if(!isFreeLocalFile) {
                        LoadLocal();
                    }
                }
            }
            else if(storageType == BookItems.StorageType.LOCAL){
                if(!isFreeLocalFile) {
                    LoadLocal();
                }
            }
        }
        readerFlag = true;

        Utils.EnableFullScreen(this);

        LoadWebViewAccelerationSystem();
    }

    @Override
    protected void onStop() {
        super.onStop();
        //Save Current Display Mode With Book ID
        SetCurrentDisplayMode();
    }

    private void LoadWebViewAccelerationSystem(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            // chromium, enable hardware acceleration
            webView.setLayerType(View.LAYER_TYPE_HARDWARE, null);
        } else {
            // older android version, disable hardware acceleration
            webView.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
        }
    }

    private void LoadLocal(){
        isFreeLocalFile = true;
        String target = path;
        File file = new File(target);
        long fileSize = file.length();
        LoadHtml(target, fileSize);
    }

    public void onBack_onClick(View view){
        finish();
    }

    public void onBack_onClick2(View view){
        searchOptionToggle = true;
        ToggleSearchView();
    }

    public void ToggleOnlySearchView(){
        float density = getResources().getDisplayMetrics().density;

        if(searchViewToggle){
            searchViewToggle = false;

            int px = (int)(0 * density);int py = (int)(-85 * density);
            toolbar2.setY((float)px);
            ObjectAnimator transAnimation= ObjectAnimator.ofFloat(toolbar2, "y",py);
            transAnimation.setDuration(80);//set duration
            transAnimation.start();
            transAnimation.addListener(new Animator.AnimatorListener() {
                @Override
                public void onAnimationStart(Animator animation) {

                }

                @Override
                public void onAnimationEnd(Animator animation) {
                    toolbar2.setVisibility(View.GONE);
                }

                @Override
                public void onAnimationCancel(Animator animation) {

                }

                @Override
                public void onAnimationRepeat(Animator animation) {

                }
            });
        }
        else {
            searchViewToggle = true;
            toolbar2.setVisibility(View.VISIBLE);
            int px = (int)(-85 * density);int py = (int)(0 * density);toolbar2.setY((float)px);
            ObjectAnimator transAnimation= ObjectAnimator.ofFloat(toolbar2, "y",py);
            transAnimation.setDuration(80);//set duration
            transAnimation.start();
        }
    }

    private void ToggleSearchView(){
        float density = getResources().getDisplayMetrics().density;
        final int px = (int)(54 * density);
        final int py = (int)(148 * density);
        if(!searchOptionToggle) {
            //Open Search
            slideDown(bottomView, null);
            slideUp2(toolbar, new Animator.AnimatorListener() {
                @Override
                public void onAnimationStart(Animator animation) {

                }

                @Override
                public void onAnimationEnd(Animator animation) {
                    searchOptionToggle = true;
                    //Visible toolbar2
                    toolbar2.setVisibility(View.VISIBLE);
                }

                @Override
                public void onAnimationCancel(Animator animation) {

                }

                @Override
                public void onAnimationRepeat(Animator animation) {

                }
            });
            //searchOption.setBackgroundColor(Color.BLACK);
        }
        else{
            //Close Search
            slideDown2(toolbar, null);
            slideUp(bottomView, null);
            searchOptionToggle = false;
            toolbar2.setVisibility(View.GONE);
            searchOptionIcon.setImageResource(R.drawable.search_icon);
            if(!lastSelectSearchCfi.equals("")){
                webView.loadUrl("javascript:RemoveSearchCfi('"+lastSelectSearchCfi+"')");
            }
        }
    }

    public void LoadHighlightData(){
        SQLiteDatabase sqLiteDatabase = null;
        try{
            String query = "select * from highlight where bookid = ? and UID = ?";
            SQLiteOpenHelper helper = new DBSqliteHelper(this, getString(R.string.SQLITE_DATABASE), null, getResources().getInteger(R.integer.SQLITE_DATABSE_VERSION));
            sqLiteDatabase =helper.getWritableDatabase();
            Cursor cursor = sqLiteDatabase.rawQuery(query, new String[] {bookid, FirebaseUtils.firebaseAuth.getCurrentUser().getUid()});
            for(cursor.moveToLast(); !cursor.isBeforeFirst();cursor.moveToPrevious()){
                String title =  cursor.getString(cursor.getColumnIndex("title"));
                String cfi =  cursor.getString(cursor.getColumnIndex("cfi"));
                String date =  cursor.getString(cursor.getColumnIndex("highlightdate"));
                highlightItems.add(new HighlightItem(cfi, title, date));
            }
            cursor.close();
        }
        catch (Exception ex){
            Log.e(TAG, ex.toString());
        }
        finally {
            if(sqLiteDatabase !=null){sqLiteDatabase.close();}
        }
    }

    public void LoadBookmarkData(){
        SQLiteDatabase sqLiteDatabase = null;
        try{
            String query = "select * from bookmark where bookid = ? and UID = ?";
            SQLiteOpenHelper helper = new DBSqliteHelper(this, getString(R.string.SQLITE_DATABASE), null, getResources().getInteger(R.integer.SQLITE_DATABSE_VERSION));
            sqLiteDatabase =helper.getWritableDatabase();
            Cursor cursor = sqLiteDatabase.rawQuery(query, new String[] {bookid, FirebaseUtils.firebaseAuth.getCurrentUser().getUid()});
            for(cursor.moveToLast(); !cursor.isBeforeFirst();cursor.moveToPrevious()){
                String title =  cursor.getString(cursor.getColumnIndex("title"));
                String cfi =  cursor.getString(cursor.getColumnIndex("cfi"));
                bookmarkItems.add(new BookmarkItem(title, cfi));
            }
            cursor.close();
        }
        catch (Exception ex){
            Log.e(TAG, ex.toString());
        }
        finally {
            if(sqLiteDatabase !=null){sqLiteDatabase.close();}
        }
    }

    public void LoadBookstateData(){
        SQLiteDatabase sqLiteDatabase = null;
        try{
            String query = "select * from bookstate where bookid = ? and UID = ?";
            SQLiteOpenHelper helper = new DBSqliteHelper(this, getString(R.string.SQLITE_DATABASE), null, getResources().getInteger(R.integer.SQLITE_DATABSE_VERSION));
            sqLiteDatabase =helper.getWritableDatabase();
            Cursor cursor = sqLiteDatabase.rawQuery(query, new String[] {bookid, FirebaseUtils.firebaseAuth.getCurrentUser().getUid()});
            for(cursor.moveToLast(); !cursor.isBeforeFirst();cursor.moveToPrevious()){
                CurrentCFI =  cursor.getString(cursor.getColumnIndex("cfi"));
            }
            cursor.close();
        }
        catch (Exception ex){
            Log.e(TAG, ex.toString());
        }
        finally {
            if(sqLiteDatabase !=null){sqLiteDatabase.close();}
        }
    }

    public void LoadNoteData(){
        SQLiteDatabase sqLiteDatabase = null;
        try{
            String query = "select * from note where bookid = ? and UID = ?";
            SQLiteOpenHelper helper = new DBSqliteHelper(this, getString(R.string.SQLITE_DATABASE), null, getResources().getInteger(R.integer.SQLITE_DATABSE_VERSION));
            sqLiteDatabase =helper.getWritableDatabase();
            Cursor cursor = sqLiteDatabase.rawQuery(query, new String[] {bookid, FirebaseUtils.firebaseAuth.getCurrentUser().getUid()});
            for(cursor.moveToLast(); !cursor.isBeforeFirst();cursor.moveToPrevious()){
                String ID = cursor.getString(cursor.getColumnIndex("ID"));
                String cfi =  cursor.getString(cursor.getColumnIndex("cfi"));
                String title =  cursor.getString(cursor.getColumnIndex("title"));
                String content =  cursor.getString(cursor.getColumnIndex("content"));
                String notedate =  cursor.getString(cursor.getColumnIndex("notedate"));
                noteItems.add(new NoteItem(ID, cfi, title, content, notedate));
            }
            cursor.close();
        }
        catch (Exception ex){
            Log.e(TAG, ex.toString());
        }
        finally {
            if(sqLiteDatabase !=null){sqLiteDatabase.close();}
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if(storageType == BookItems.StorageType.CLOUD) {
            if (readerFlag) {
                Utils.RemoveTempFile(tempFileLocation);
                currentPageStatic = 0;
                isPageGenerate = false;
            }
        }
        else if(storageType == BookItems.StorageType.LOCAL){
            //..

        }
        //Save To Database
        SetBookStateDataToDatabase();
    }

    public void LoadHtml(String path, long fileSize){
        CurrentPath = path;
        CurrentFileSize = fileSize;

        Utils.SetProgressDialogIndeterminate(this, "loading..");
        AddJavaScriptInterface();
        path = "file://" + path;
        int totalCharacter = (int)(fileSize / 1000);
        webView.loadUrl(getString(R.string.EPUB_ENGINE_ROOT) + "?path=" + path + "&totalCharacter=" + totalCharacter + "&currentMarginVal=" + CurrentMarginType);
        webView.clearCache(true);
    }

    public void ReloadHtml(){
        LoadHtml(CurrentPath, CurrentFileSize);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId() == android.R.id.home){
            finish();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        finish();
        super.onBackPressed();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        boolean volumeKey = SharedPreferenceUtils.Setting_GetData(this ,getString(R.string.EPUB_SETTING_VOLUMEKEYS), true);
        if(keyCode == KeyEvent.KEYCODE_VOLUME_DOWN){
            if(volumeKey) {
                return true;
            }
        }
        else if(keyCode == KeyEvent.KEYCODE_VOLUME_UP){
            if(volumeKey) {
                return true;
            }
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        boolean volumeKey = SharedPreferenceUtils.Setting_GetData(this ,getString(R.string.EPUB_SETTING_VOLUMEKEYS), true);
        if(keyCode == KeyEvent.KEYCODE_VOLUME_DOWN){
            if(volumeKey) {
                SwipeTrigger("R");
                return true;
            }
        }
        else if(keyCode == KeyEvent.KEYCODE_VOLUME_UP){
            if(volumeKey) {
                SwipeTrigger("L");
                return true;
            }
        }
        else if(keyCode == KeyEvent.KEYCODE_BACK){
            finish();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    public void Search_onClick(View view){
        try{
            RadioButton currentChapterRadio = findViewById(R.id.currentChapter);
            RadioButton entireBookRadio = findViewById(R.id.entireBook);

            searchQueryText = searchQuery.getText().toString().trim();
            if(searchQueryText.equals("")){Toast.makeText(Reader_EPUB.this, "enter search string", Toast.LENGTH_SHORT).show();return;}

            //reset search item lists
            isSearchNextAvailable = false;
            searchItemsList.clear();
            searchProgressBar.setVisibility(View.VISIBLE);
            totalFound.setVisibility(View.VISIBLE);
            Utils.hideSoftKeyboard(searchQuery);
            nextButtonIcon.setImageResource(R.drawable.icon_next_unselect);
            previousButtonIcon.setImageResource(R.drawable.icon_previous_unselect);
            searchListButtonIcon.setImageResource(R.drawable.icon_list_unselect);
            currentSearchSelect = -1;

            //region N_P Button
            nextButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(isSearchNextAvailable){
                        SearchListNext();
                    }
                }
            });
            previousButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(isSearchNextAvailable){
                        SearchListPrevious();
                    }
                }
            });
            //endregion

            if(currentChapterRadio.isChecked()){
                webView.loadUrl("javascript:SearchInBook_Chapter('"+searchQueryText+"')");
            }
            else if(entireBookRadio.isChecked()){
                webView.loadUrl("javascript:SearchInBook_EntireBook('"+searchQueryText+"')");
            }
        }
        catch (Exception ex){
            Log.e(TAG, ex.toString());
        }
    }

    public void SearchList_onClick(View view){
        try{
            if(isSearchNextAvailable) {
                View searchViewContainer = getLayoutInflater().inflate(R.layout.reader_epub_search, null, false);
                searchRecyclerView = searchViewContainer.findViewById(R.id.searchItemRecycler);

                AlertDialog.Builder searchBuilder = new AlertDialog.Builder(this);
                searchBuilder.setView(searchViewContainer);
                searchBuilder.setTitle("Searched Lists");
                searchBuilder.setPositiveButton("OK", null);
                searchAlertDialog = searchBuilder.create();

                //init values
                searchRecyclerView.setLayoutManager(new LinearLayoutManager(Reader_EPUB.this));
                searchItemsAdapter = new SearchItemsAdapter(Reader_EPUB.this, searchItemsList, webView, searchAlertDialog);
                searchRecyclerView.setAdapter(searchItemsAdapter);
                searchAlertDialog.show();
            }
        }
        catch (Exception ex){
            Log.e(TAG, ex.toString());
        }
    }

    public void TOC_onClick(View v){
        try {
            readerFlag = false;
            Reader_EPUB_Contents.BookID = bookid;
            Reader_EPUB_Contents.storageType = storageType;
            Reader_EPUB_Contents.tocItems = tocItems;
            Reader_EPUB_Contents.bookmarkItems = bookmarkItems;
            Reader_EPUB_Contents.highlightItems = highlightItems;
            Reader_EPUB_Contents.noteItems = noteItems;
            Reader_EPUB_Contents.webview = webView;

            Intent intent = new Intent(this, Reader_EPUB_Contents.class);
            startActivity(intent);
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
        }
        catch (Exception ex){
            Log.e(TAG, ex.getMessage());
        }
    }

    public void Bookmark_onClick(View view){
        try{
            if(!isCurrentPageBookmark){
                bookmarkImageView.setImageResource(R.drawable.icon_bookmark);
                if(CurrentDisplayMode.equals("2") || CurrentDisplayMode.equals("4")){
                    bookmarkImageView2.setImageResource(R.drawable.icon_bookmark_white);
                }
                else{
                    bookmarkImageView2.setImageResource(R.drawable.icon_bookmark);
                }
                isCurrentPageBookmark = true;
                webView.loadUrl("javascript:BookmarkPage();");
            }
            else{
                //remove cfi from lists
                removeBookmarkFromList(currentPageBookmarkCFI);
                //Remove Items From Bookmark
                RemoveBookmarkDataFromDatabase(currentPageBookmarkCFI);

                isCurrentPageBookmark = false;
                bookmarkImageView.setImageResource(R.drawable.icon_bookmark_unselect);
                bookmarkImageView2.setImageResource(R.drawable._transparent);
            }
        }
        catch (Exception ex){
            Log.e(TAG, ex.getMessage());
        }
    }

    public void ePubPageRelocated(final String cfi){
        try{
            CurrentCFI = cfi;
            new Handler(Looper.getMainLooper()).post(new Runnable() {
                @Override
                public void run() {
                    if(isCFIExistsInBookmarkLists(cfi)){
                        bookmarkImageView.setImageResource(R.drawable.icon_bookmark);
                        if(CurrentDisplayMode.equals("2") || CurrentDisplayMode.equals("4")){
                            bookmarkImageView2.setImageResource(R.drawable.icon_bookmark_white);
                        }
                        else{
                            bookmarkImageView2.setImageResource(R.drawable.icon_bookmark);
                        }
                        isCurrentPageBookmark = true;
                        currentPageBookmarkCFI = cfi;
                    }
                    else{
                        isCurrentPageBookmark = false;
                        bookmarkImageView.setImageResource(R.drawable.icon_bookmark_unselect);
                        bookmarkImageView2.setImageResource(R.drawable._transparent);
                        currentPageBookmarkCFI = cfi;
                    }
                }
            });
        }
        catch (Exception ex){
            Log.e(TAG, ex.getMessage());
        }
    }

    private void ToggleMenu(){
        isMenuShow = !isMenuShow;
        if(isMenuShow){
            slideUp(bottomView, null);
            slideDown2(toolbar, null);
        }
        else{
            slideDown(bottomView, null);
            slideUp2(toolbar, null);
        }
    }

    //region Slide Animation View
    public void slideUp(View view, Animator.AnimatorListener animatorListener){
        ObjectAnimator objectAnimator = ObjectAnimator.ofFloat(view, "translationY",view.getHeight(),0);
        objectAnimator.setDuration(slideAnimationD);
        if(animatorListener != null){
            objectAnimator.addListener(animatorListener);
        }
        objectAnimator.start();
    }

    public void slideDown(final View view, Animator.AnimatorListener animatorListener){
        ObjectAnimator objectAnimator = ObjectAnimator.ofFloat(view, "translationY",0,view.getHeight());
        objectAnimator.setDuration(slideAnimationD);
        if(animatorListener != null){
            objectAnimator.addListener(animatorListener);
        }
        objectAnimator.start();
    }

    public void slideUp2(final View view, Animator.AnimatorListener animatorListener){
        ObjectAnimator objectAnimator = ObjectAnimator.ofFloat(view, "translationY",0, -view.getHeight());
        objectAnimator.setDuration(slideAnimationD);
        if(animatorListener != null){
            objectAnimator.addListener(animatorListener);
        }
        objectAnimator.start();

    }

    public void slideDown2(final View view, Animator.AnimatorListener animatorListener){
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

    private void SwipeTrigger(String T){
        if(!isSelecting) {
            if (T.equals("L")) {
                webView.loadUrl("javascript:SwipeLeft();");
            } else if (T.equals("R")) {
                webView.loadUrl("javascript:SwipeRight();");
            }
        }
    }

    @SuppressLint("JavascriptInterface")
    private void AddJavaScriptInterface(){
        JavaScriptInterface javaScriptInterface = new JavaScriptInterface();
        webView.addJavascriptInterface(javaScriptInterface, "JavaScriptInterface");
    }

    public class JavaScriptInterface{
        @JavascriptInterface
        public void FinishLoading(){
            //Add addHighlight
            new Handler(getMainLooper()).post(new Runnable() {
                @Override
                public void run() {
                    try {
                        Utils.EnableFullScreen(Reader_EPUB.this);

                        //Just Setting For Display Mode
                        webView.loadUrl("javascript:DisplayMode('"+ CurrentDisplayMode +"');");
                        reader_epub_bottomMenu.display.setDisplay(Integer.valueOf(CurrentDisplayMode) -1);


                        //Hide Top & Bottom Bars With Animations
                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {if(!insideElementSelectState){ToggleMenu();}insideElementSelectState = false;}
                        }, 1000);


                        //Load Highlight and NoteColor
                        LoadHighlightAndNoteColor(false);

                        //Register Book Pagination
                        webView.loadUrl("javascript:RegisterBookPagination()");

                        //Add Highlight
                        for (HighlightItem h : highlightItems) {
                            webView.loadUrl("javascript:AddHighlightCFI('" +  h.getCfi() +"','"+ H_Color +"')");
                        }

                        //Add Note
                        for (NoteItem n : noteItems) {
                            webView.loadUrl("javascript:AddNoteCFI('" +  n.getCfi() +"','"+ N_Color +"')");
                        }

                        //LoadBook State
                        webView.loadUrl("javascript:SetBookState('" + CurrentCFI + "')");

                        Utils.UnSetProgressDialogIndeterminate();
                    }
                    catch (Exception ex){
                        Log.e(TAG, ex.toString());
                    }
                }
            });
        }

        @JavascriptInterface
        public void RegisterBookPaginationFinished(String currentPage) {
            try{
                //Enable Pagination
                Float buffer = Float.valueOf(currentPage) * 100;
                Float current = Utils.map(buffer, 50.0f, 100.0f, 0.0f, 100.0f);
                currentPageStatic = Math.round(current);
                currentRawPageStatic = current;
                isPageGenerate = true;
                new Handler(getMainLooper()).post(new Runnable() {
                    @Override
                    public void run() {
                        reader_epub_bottomMenu.page.initPageAfterLoaded();
                    }
                });
            }
            catch (Exception ex){
                Log.e(TAG + " 1", ex.toString());
            }
        }

        @JavascriptInterface
        public void AddTOCData(String Title, String href){
            try {
                TOCItem tocItem = new TOCItem(Title.trim(), href.trim());
                tocItems.add(tocItem);
            }
            catch (Exception ex){
                Log.e(TAG + " 2", ex.toString());
            }
        }

        @JavascriptInterface
        public void AddBookmarkData(String href){
            try {
                String Label;
                String currentTime = new SimpleDateFormat("MMM dd, yyyy 'at' h:m:s a", Locale.US).format(Calendar.getInstance().getTime());
                Label = title + ", Chapter " + href.replace(".xml","").replace(".xhtml","").replace(".html","") + " (" + currentTime + ")";

                if(tocItems.size() > 0){
                    //Label = tocItems.get(Integer.valueOf(TOCIndex)).getTitle() + " (" + currentTime + ")";
                    for(int i=0;i< tocItems.size();i++){
                        if(tocItems.get(i).getHref().equals(href))
                        {
                            Label = tocItems.get(i).getTitle() + " (" + currentTime + ")";
                            break;
                        }
                    }
                }
                bookmarkItems.add(new BookmarkItem(Label, currentPageBookmarkCFI));
                SetBookmarkDataToDatabase(Label, currentPageBookmarkCFI, currentTime);
            }
            catch (Exception ex){
                Log.e(TAG + " 3", ex.toString());
            }
        }

        @JavascriptInterface
        public void PageRelocated(String cfi, String currentPage){
            try {
                ePubPageRelocated(cfi);
                Float buffer = Float.valueOf(currentPage) * 100;
                Float current = Utils.map(buffer, 50.0f, 100.0f, 0.0f, 100.0f);
                currentPageStatic = Math.round(current);
                currentRawPageStatic = current;
                new Handler(getMainLooper()).post(new Runnable() {
                    @Override
                    public void run() {
                        reader_epub_bottomMenu.page.initPageChanges();
                    }
                });
            }
            catch (Exception ex){
                Log.e(TAG + " 4", ex.toString());
            }
        }

        @JavascriptInterface
        public void SelectTextEvent(String H_cfirange, String H_contents){
            try {
                CurrentSelectText = H_contents.replaceAll("\n", " ");
                CurrentSelectText =CurrentSelectText.replaceAll("\\s{2,}", " ").trim();
                CurrentSelectCFIRange = H_cfirange;
            }
            catch (Exception ex){
                Log.e(TAG + " 5", ex.toString());
            }
        }

        @JavascriptInterface
        public void GetHighlightData(String cfi, String content){
            try {
                //Check if CFI Exists in lists
                for(int i=0;i<highlightItems.size();i++){if(highlightItems.get(i).getCfi().equals(cfi)){Toast.makeText(Reader_EPUB.this, "Already Exists Highlight", Toast.LENGTH_SHORT).show(); return;}}

                String buffer = content.replaceAll("\n", " ");
                buffer = buffer.replaceAll("\\s{2,}", " ").trim();
                String currentTime = new SimpleDateFormat("MMM dd, yyyy 'at' h:m:s a", Locale.US).format(Calendar.getInstance().getTime());
                highlightItems.add(new HighlightItem(cfi, buffer, currentTime));
                //Save Data To SQ-LITE
                SetHightlightDataToDatabase(buffer, cfi, currentTime);
            }
            catch (Exception ex){
                Log.e(TAG + " 6", ex.toString());
            }
        }

        @JavascriptInterface
        public void GetDictionaryData(String content){
            try {
                String buffer = content.replaceAll("\n", " ");
                buffer = buffer.replaceAll("\\s{2,}", " ").trim();

                Utils.OpenDictionary(Reader_EPUB.this, buffer);
            }
            catch (Exception ex){
                Log.e(TAG + " 7", ex.toString());
            }
        }

        @JavascriptInterface
        public void GetNoteData(String cfi, String note, String content){
            try {
                //Check If Note Exists
                for(int i=0;i<noteItems.size();i++){if(noteItems.get(i).getCfi().equals(cfi)){Toast.makeText(Reader_EPUB.this, "Already Exists Note", Toast.LENGTH_SHORT).show(); return;}}

                String ID = Utils.UUIDToken();
                String title = content.replaceAll("\n", " ");
                title = title.replaceAll("\\s{2,}", " ").trim();

                String currentTime = new SimpleDateFormat("MMM dd, yyyy 'at' h:m:s a", Locale.US).format(Calendar.getInstance().getTime());
                noteItems.add(new NoteItem(ID, cfi, title, note, currentTime));
                SetNoteDataToDatabase(ID, title, note, cfi, currentTime);
            }
            catch (Exception ex){
                Log.e(TAG + " 8", ex.toString());
            }
        }

        @JavascriptInterface
        public void AnnotateEvent_Click(String cfiRange, String type){
            insideElementSelectState = true;
            if(type.equals("highlight")){
                OpenAnnotatorMenu(cfiRange, LastInsideElementSelect.HIGHLIGHT);
            }
            else if(type.equals("note")){
                OpenAnnotatorMenu(cfiRange, LastInsideElementSelect.NOTE);
            }
        }

        @JavascriptInterface
        public void FindSearchedItem(String searchQueryText,String title, String cfi){
            title = title.replace(searchQueryText, "<B>" + searchQueryText + "</B>");
            title = title.replace(searchQueryText.toUpperCase(), "<B>" + searchQueryText.toUpperCase() + "</B>");
            searchItemsList.add(new SearchItems(title, cfi));
            new Handler(getMainLooper()).post(new Runnable() {
                @Override
                public void run() {
                    totalFound.setText(String.valueOf(searchItemsList.size()));
                }
            });

        }

        @JavascriptInterface
        public void FinishedSearch(){
            new Handler(getMainLooper()).post(new Runnable() {
                @Override
                public void run() {
                    if(searchProgressBar !=null){searchProgressBar.setVisibility(View.INVISIBLE);}
                    if(searchItemsList.size() <=0){ Toast.makeText(Reader_EPUB.this, "no string found", Toast.LENGTH_SHORT).show();}
                    else{
                        //Select First cfi
                        new Handler(getMainLooper()).post(new Runnable() {
                            @Override
                            public void run() {
                                //enable next
                                isSearchNextAvailable = true;
                                nextButtonIcon.setImageResource(R.drawable.icon_next);
                                previousButtonIcon.setImageResource(R.drawable.icon_previous);
                                searchListButtonIcon.setImageResource(R.drawable.icon_list);
                                //Select First from list
                                SelectSearchStringCfi(0);// Initial First
                            }
                        });
                    }
                }
            });
        }

        @JavascriptInterface
        public void LinkClicked(){
            isLinkClick = true;
        }

        @JavascriptInterface
        public void DisplayChangedInit(String val){
            webView.setBackgroundColor(Color.parseColor(val));
        }

        @JavascriptInterface
        public void Log(String val){
            Log.e("WV:LOG:", val);
        }

        @JavascriptInterface
        public void AlertDialog(String data){
            AlertDialog.Builder builder = new AlertDialog.Builder(Reader_EPUB.this);
            builder.setTitle("Error");
            builder.setMessage(data);
            builder.setPositiveButton("Ok", null);
            builder.create().show();
        }

    }

    private boolean isCFIExistsInBookmarkLists(String cfi){
        boolean isBookmark =false;
        for(BookmarkItem bookmarkItem : bookmarkItems){
            if(bookmarkItem.getCfi().equals(cfi)){
                //Change bookmark icon to clean
                isBookmark = true;
                break;
            }
        }
        return isBookmark;
    }

    private void removeBookmarkFromList(String cfi){
        for(int i=0;i< bookmarkItems.size();i++){
            if(bookmarkItems.get(i).getCfi().equals(cfi)){
                bookmarkItems.remove(i);
                break;
            }
        }
    }

    public void SelectSearchStringCfi(int currentIndex){
        if(currentIndex >=0){
            currentSearchSelect = currentIndex;
            String buffer = (currentSearchSelect+1) + "/" + String.valueOf(searchItemsList.size());
            totalFound.setText(buffer);

            //Add CFI
            String currentSelectCFI = searchItemsList.get(currentSearchSelect).cfi;
            webView.loadUrl("javascript:GotToSearchCfi('"+currentSelectCFI+"', '"+lastSelectSearchCfi+"')");
            lastSelectSearchCfi = currentSelectCFI;
        }
    }

    public void SearchListNext(){
        int index = currentSearchSelect + 1;
        if(index < searchItemsList.size()){
            SelectSearchStringCfi(index);
        }
        else{
            Toast.makeText(this, "no next text available", Toast.LENGTH_SHORT).show();
        }
    }

    public void SearchListPrevious(){
        int index = currentSearchSelect - 1;
        if(index >=0){
            SelectSearchStringCfi(index);
        }
        else{
            Toast.makeText(this, "no previous text available", Toast.LENGTH_SHORT).show();
        }
    }

    //region Context Menu For WebView
    ActionMode actionMode= null;
    @Override
    public void onActionModeStarted(ActionMode mode) {
        super.onActionModeStarted(mode);
        if (actionMode == null) {
            if(!insideElementSelectState){
                isContextMenuOpen = true;
                //region Text Select
                try {
                    webView.setTextSelection(true);
                    if(webView.isCurling()) {
                        webView.textSelectionChange();//Invoking curling system
                    }
                    isSelecting = true;
                    actionMode = mode;
                    Menu menu = mode.getMenu();
                    MenuItem copyItem = null;
                    MenuItem selectItem = null;
                    MenuItem wikipediaItem = null;
                    MenuItem webSearch = null;
                    for (int i = 0; i < menu.size(); i++) {
                        String menuName = menu.getItem(i).getTitle().toString().toLowerCase();
                        if (menuName.equals("copy")) {
                            copyItem = menu.getItem(i);
                        }
                        else if (menuName.equals("select all")) {
                            selectItem = menu.getItem(i);
                        }
                        else if (menuName.equals("web search")) {
                            webSearch = menu.getItem(i);
                        }
                        else if (menuName.equals("search wikipedia")) {
                            wikipediaItem = menu.getItem(i);
                        }
                    }
                    //Copy all items from Current Menu
                    menu.clear();

                    mode.getMenuInflater().inflate(R.menu.web_contextmenu, menu);
                    setHighlightMenuItemListener(menu.findItem(R.id.highlight));
                    //setDictionaryMenuItemListener(menu.findItem(R.id.dictionary));
                    setNoteMenuItemListener(menu.findItem(R.id.note));
                    setShareMenuItemListener(menu.findItem(R.id.share));

                    //Adding Items To Context Menu
                    if (copyItem != null) {
                        menu.add(copyItem.getGroupId(), copyItem.getItemId(), copyItem.getOrder(), copyItem.getTitle());
                    }
                    if (selectItem != null) {
                        menu.add(selectItem.getGroupId(), selectItem.getItemId(), selectItem.getOrder(), selectItem.getTitle());
                    }
                    if (webSearch != null) {
                        menu.add(webSearch.getGroupId(), webSearch.getItemId(), webSearch.getOrder(), webSearch.getTitle());
                    }
                    if (wikipediaItem != null) {
                        //menu.add(wikipediaItem.getGroupId(), wikipediaItem.getItemId(), wikipediaItem.getOrder(), wikipediaItem.getTitle());
                    }
                }
                catch (Exception ex){
                    Log.e(TAG, ex.toString());
                }
                //endregion
            }
            else{
                actionMode.getMenu().clear();
            }
        }
    }

    private void setHighlightMenuItemListener(MenuItem menuItem){
        menuItem.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                webView.loadUrl("javascript:SetHighlight('"+ H_Color +"');");
                if(actionMode !=null) {actionMode.finish();}
                return true;
            }
        });
    }

    private void setDictionaryMenuItemListener(MenuItem menuItem){
        menuItem.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                webView.loadUrl("javascript:SetDictionary();");
                if(actionMode !=null) {actionMode.finish();}
                return true;
            }
        });
    }

    private void setNoteMenuItemListener(MenuItem menuItem){
        menuItem.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                //Show PromptText box
                Utils.SetInputDialogBox(Reader_EPUB.this, 4, "Add Note", "Note", "", 0, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        EditText inputText = ((AlertDialog)dialog).findViewById(R.id.inputEditText);
                        String data = inputText.getText().toString();
                        webView.loadUrl("javascript:SetNote('" + data + "','"+ N_Color +"');");
                    }
                });
                if(actionMode !=null) {actionMode.finish();}
                return true;
            }
        });
    }

    private void setShareMenuItemListener(MenuItem menuItem){
        menuItem.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                //region no internet toast
                if(!NetworkManagerUtils.LoadServerData(Reader_EPUB.this)){
                    Toast.makeText(Reader_EPUB.this, "No Network Connection", Toast.LENGTH_SHORT).show();
                }
                else{
                    readerFlag = false;
                    Intent i = new Intent(Reader_EPUB.this, Activity_UserPost.class);
                    i.putExtra("ShareText", CurrentSelectText);
                    i.putExtra("BookShareID", bookid);
                    i.putExtra("storeType", storageType.toString());
                    startActivity(i);
                }
                if(actionMode !=null) {actionMode.finish();}
                return true;
            }
        });
    }

    private void setWebSearchMenuItemListener(MenuItem menuItem){  //Currently not working
        menuItem.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                Intent intent = new Intent(Intent.ACTION_WEB_SEARCH);
                intent.putExtra(SearchManager.QUERY, CurrentSelectText); // query contains search string
                startActivity(intent);
                return true;
            }
        });
    }

    @Override
    public void onActionModeFinished(ActionMode mode) {
        super.onActionModeFinished(mode);
        actionMode = null;
        isSelecting =false;
        webView.setTextSelection(false);
    }
    //endregion

    public void SetCurrentDisplayMode(){
        SQLiteDatabase sqLiteDatabase = null;
        try{
            SQLiteOpenHelper helper = new DBSqliteHelper(this, getString(R.string.SQLITE_DATABASE), null, getResources().getInteger(R.integer.SQLITE_DATABSE_VERSION));
            sqLiteDatabase = helper.getWritableDatabase();

            boolean isDataExists = false;

            String query = "select bookid from currentdisplaymode where bookid=?";
            Cursor cursor = sqLiteDatabase.rawQuery(query, new String[] {bookid});
            if(cursor.getCount() >0){isDataExists = true;}
            cursor.close();

            //check data exists or not
            if(isDataExists){
                ContentValues contentValues = new ContentValues();
                contentValues.put("displaymode", CurrentDisplayMode);
                sqLiteDatabase.update("currentdisplaymode", contentValues, "bookid=?", new String[] { bookid});
            }
            else{
                ContentValues contentValues = new ContentValues();
                contentValues.put("bookid", bookid);
                contentValues.put("displaymode", CurrentDisplayMode);
                sqLiteDatabase.insert("currentdisplaymode", null,contentValues);
            }
        }
        catch (Exception ex){
            Log.e(TAG, ex.toString());
        }
        finally {
            if(sqLiteDatabase !=null){sqLiteDatabase.close();}
        }
    }

    public void LoadCurrentDisplayMode(){
        SQLiteDatabase sqLiteDatabase = null;
        try{
            String query = "select * from currentdisplaymode where bookid = ?";
            SQLiteOpenHelper helper = new DBSqliteHelper(this, getString(R.string.SQLITE_DATABASE), null, getResources().getInteger(R.integer.SQLITE_DATABSE_VERSION));
            sqLiteDatabase =helper.getWritableDatabase();
            Cursor cursor = sqLiteDatabase.rawQuery(query, new String[] {bookid});
            if(cursor.getCount() >0){cursor.moveToFirst();CurrentDisplayMode = cursor.getString(cursor.getColumnIndex("displaymode"));}
            cursor.close();
        }
        catch (Exception ex){
            Log.e(TAG, ex.toString());
        }
        finally {
            if(sqLiteDatabase !=null){sqLiteDatabase.close();}
        }
    }

    public void SetHightlightDataToDatabase(String title, String cfi, String currentTime){
        SQLiteDatabase sqLiteDatabase = null;
        try{
            SQLiteOpenHelper helper = new DBSqliteHelper(this, getString(R.string.SQLITE_DATABASE), null, getResources().getInteger(R.integer.SQLITE_DATABSE_VERSION));
            sqLiteDatabase = helper.getWritableDatabase();

            ContentValues contentValues = new ContentValues();
            contentValues.put("UID", FirebaseUtils.firebaseAuth.getCurrentUser().getUid());
            contentValues.put("bookid", bookid);
            contentValues.put("title", title);
            contentValues.put("cfi", cfi);
            contentValues.put("highlightdate", currentTime);
            sqLiteDatabase.insert("highlight", null,contentValues);
        }
        catch (Exception ex){
            Log.e(TAG, ex.toString());
        }
        finally {
            if(sqLiteDatabase !=null){sqLiteDatabase.close();}
        }
    }

    public void SetNoteDataToDatabase(String ID, String title, String content, String cfi, String currentTime){
        SQLiteDatabase sqLiteDatabase = null;
        try{
            SQLiteOpenHelper helper = new DBSqliteHelper(this, getString(R.string.SQLITE_DATABASE), null, getResources().getInteger(R.integer.SQLITE_DATABSE_VERSION));
            sqLiteDatabase = helper.getWritableDatabase();
            ContentValues contentValues = new ContentValues();
            contentValues.put("ID", ID);
            contentValues.put("UID", FirebaseUtils.firebaseAuth.getCurrentUser().getUid());
            contentValues.put("bookid", bookid);
            contentValues.put("cfi", cfi);
            contentValues.put("title", title);
            contentValues.put("content", content);
            contentValues.put("notedate", currentTime);
            sqLiteDatabase.insert("note", null,contentValues);
        }
        catch (Exception ex){
            Log.e(TAG, ex.toString());
        }
        finally {
            if(sqLiteDatabase !=null){sqLiteDatabase.close();}
        }
    }

    public void SetBookmarkDataToDatabase(String title, String cfi, String date){
        SQLiteDatabase sqLiteDatabase = null;
        try{
            SQLiteOpenHelper helper = new DBSqliteHelper(this, getString(R.string.SQLITE_DATABASE), null, getResources().getInteger(R.integer.SQLITE_DATABSE_VERSION));
            sqLiteDatabase = helper.getWritableDatabase();

            ContentValues contentValues = new ContentValues();
            contentValues.put("UID", FirebaseUtils.firebaseAuth.getCurrentUser().getUid());
            contentValues.put("bookid", bookid);
            contentValues.put("title", title);
            contentValues.put("cfi", cfi);
            contentValues.put("bookmarkdate", date);
            sqLiteDatabase.insert("bookmark", null,contentValues);
        }
        catch (Exception ex){
            Log.e(TAG, ex.toString());
        }
        finally {
            if(sqLiteDatabase !=null){sqLiteDatabase.close();}
        }
    }

    public void RemoveBookmarkDataFromDatabase(String CFI){
        SQLiteDatabase sqLiteDatabase = null;
        try{
            SQLiteOpenHelper helper = new DBSqliteHelper(this, getString(R.string.SQLITE_DATABASE), null, getResources().getInteger(R.integer.SQLITE_DATABSE_VERSION));
            sqLiteDatabase = helper.getWritableDatabase();
            sqLiteDatabase.execSQL("delete from bookmark where cfi LIKE ?", new String[] {CFI});
            sqLiteDatabase.close();
            Toast.makeText(this, "Bookmark Removed", Toast.LENGTH_SHORT).show();
        }
        catch (Exception ex){
            Log.e(TAG, ex.toString());
        }
        finally {
            if(sqLiteDatabase !=null){sqLiteDatabase.close();}
        }
    }

    public void SetBookStateDataToDatabase(){
        SQLiteDatabase sqLiteDatabase = null;
        try{
            SQLiteOpenHelper helper = new DBSqliteHelper(this, getString(R.string.SQLITE_DATABASE), null, getResources().getInteger(R.integer.SQLITE_DATABSE_VERSION));
            sqLiteDatabase = helper.getWritableDatabase();

            //Delete BookID from database First
            sqLiteDatabase.execSQL("delete from bookstate where bookid = ?", new String[]{bookid});

            //Add It To System
            ContentValues contentValues = new ContentValues();
            contentValues.put("UID", FirebaseUtils.firebaseAuth.getCurrentUser().getUid());
            contentValues.put("bookid", bookid);
            contentValues.put("cfi", CurrentCFI);
            sqLiteDatabase.insert("bookstate", null,contentValues);
        }
        catch (Exception ex){
            Log.e(TAG, ex.toString());
        }
        finally {
            if(sqLiteDatabase !=null){sqLiteDatabase.close();}
        }
    }

    public void LoadHighlightAndNoteColor(boolean isReloadData){
        try{
            H_Color = SharedPreferenceUtils.Setting_GetData_Str(this ,getString(R.string.EPUB_SETTING_HIGHLIGHTCOLOR), "#6bdc87");
            N_Color = SharedPreferenceUtils.Setting_GetData_Str(this ,getString(R.string.EPUB_SETTING_NOTECOLOR), "#ffba2f");

            if(isReloadData){
                //Add Highlight
                for(HighlightItem h : highlightItems){
                    webView.loadUrl("javascript:RemoveHighlight('" +  h.getCfi() +"')");
                    webView.loadUrl("javascript:AddHighlightCFI('" +  h.getCfi() +"','"+ H_Color +"')");
                }

                //Add Note
                for(NoteItem n: noteItems){
                    webView.loadUrl("javascript:RemoveNote('" +  n.getCfi() +"')");
                    webView.loadUrl("javascript:AddNoteCFI('" +  n.getCfi() +"','"+ N_Color +"')");
                }
            }
        }
        catch (Exception ex){
            Log.e(TAG, ex.toString());
        }
    }

    public void OpenAnnotatorMenu(final String cfiRange, LastInsideElementSelect lastInsideElementSelect){
        if(lastInsideElementSelect == LastInsideElementSelect.HIGHLIGHT){
            //region HIGHLIGHT
            CharSequence menuItems[] = new CharSequence[] {"Remove Highlight","Share Highlight"};
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Highlight Options");
            builder.setItems(menuItems, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    if(which == 0){
                        //region Remove Highlight
                        AlertDialog.Builder builder = new AlertDialog.Builder(Reader_EPUB.this);
                        builder.setMessage("Are you sure want to remove this highlight?");
                        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                SQLiteDatabase sqLiteDatabase = null;
                                try{
                                    SQLiteOpenHelper helper = new DBSqliteHelper(Reader_EPUB.this, Reader_EPUB.this.getString(R.string.SQLITE_DATABASE), null, Reader_EPUB.this.getResources().getInteger(R.integer.SQLITE_DATABSE_VERSION));
                                    sqLiteDatabase =helper.getWritableDatabase();
                                    sqLiteDatabase.execSQL("delete from highlight where cfi = ? and UID = ?", new String[]{ cfiRange, FirebaseUtils.firebaseAuth.getCurrentUser().getUid()});

                                    //Remove Highlight WebView
                                    new Handler(getMainLooper()).post(new Runnable() {
                                        @Override
                                        public void run() { Reader_EPUB.this.webView.loadUrl("javascript:RemoveHighlight('"+ cfiRange +"')");}
                                    });

                                    //Remove highlight from list
                                    for(int i=0;i<highlightItems.size();i++){if(highlightItems.get(i).getCfi().equals(cfiRange)){highlightItems.remove(i);break;}}
                                }
                                catch (Exception ex){
                                    Log.e(TAG, ex.toString());
                                }
                                finally {
                                    if(sqLiteDatabase !=null){sqLiteDatabase.close();}
                                }
                            }
                        });
                        builder.setNegativeButton("Cancel", null);
                        builder.create().show();
                        //endregion
                    }
                    else if(which == 1){
                        //region Share Text
                        String shareText = "";
                        for(int i=0; i < highlightItems.size(); i++){
                            if(highlightItems.get(i).getCfi().equals(cfiRange)){
                                shareText = highlightItems.get(i).getContents();
                                break;
                            }
                        }
                        if(!shareText.equals("")){
                            if(!NetworkManagerUtils.LoadServerData(Reader_EPUB.this)){
                                Toast.makeText(Reader_EPUB.this, "No Network Connection", Toast.LENGTH_SHORT).show();
                            }
                            else{
                                Intent i = new Intent(Reader_EPUB.this, Activity_UserPost.class);
                                i.putExtra("ShareText", shareText);
                                i.putExtra("BookShareID", bookid);
                                i.putExtra("storeType", storageType.toString());
                                startActivity(i);
                            }
                        }
                        else{
                            Toast.makeText(Reader_EPUB.this, "Error: No Content Found In List", Toast.LENGTH_SHORT).show();
                        }
                        //endregion
                    }
                }
            });
            builder.create().show();
            //endregion
        }
        else if(lastInsideElementSelect == LastInsideElementSelect.NOTE){
            //region NOTE
            CharSequence menuItems[] = new CharSequence[] {"Remove Note", "Share Note", "Show Note" , "Edit Note"};
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Note Options");
            builder.setItems(menuItems, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    if(which == 0){
                        //region Remove Note
                        AlertDialog.Builder builder = new AlertDialog.Builder(Reader_EPUB.this);
                        builder.setMessage("Are you sure want to remove this note?");
                        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                SQLiteDatabase sqLiteDatabase = null;
                                try{
                                    SQLiteOpenHelper helper = new DBSqliteHelper(Reader_EPUB.this, Reader_EPUB.this.getString(R.string.SQLITE_DATABASE), null, Reader_EPUB.this.getResources().getInteger(R.integer.SQLITE_DATABSE_VERSION));
                                    sqLiteDatabase =helper.getWritableDatabase();
                                    sqLiteDatabase.execSQL("delete from note where cfi = ? and UID = ?", new String[]{ cfiRange, FirebaseUtils.firebaseAuth.getCurrentUser().getUid()});

                                    //Remove note WebView
                                    new Handler(getMainLooper()).post(new Runnable() {
                                        @Override
                                        public void run() { Reader_EPUB.this.webView.loadUrl("javascript:RemoveNote('"+ cfiRange +"')");}
                                    });

                                    //Remove note from list
                                    for(int i=0;i<noteItems.size();i++){if(noteItems.get(i).getCfi().equals(cfiRange)){noteItems.remove(i);break;}}
                                }
                                catch (Exception ex){
                                    Log.e(TAG, ex.toString());
                                }
                                finally {
                                    if(sqLiteDatabase !=null){sqLiteDatabase.close();}
                                }
                            }
                        });
                        builder.setNegativeButton("Cancel", null);
                        builder.create().show();
                        //endregion
                    }
                    else if(which == 1){
                        //region Share Text
                        String shareText = "";
                        for(int i=0;i<noteItems.size();i++){if(noteItems.get(i).getCfi().equals(cfiRange)){shareText = noteItems.get(i).getContent();break;}}
                        if(!shareText.equals("")){
                            if(!NetworkManagerUtils.LoadServerData(Reader_EPUB.this)){
                                Toast.makeText(Reader_EPUB.this, "No Network Connection", Toast.LENGTH_SHORT).show();
                            }
                            else{
                                Intent i = new Intent(Reader_EPUB.this, Activity_UserPost.class);
                                i.putExtra("ShareText", shareText);
                                i.putExtra("BookShareID", bookid);
                                i.putExtra("storeType", storageType.toString());
                                startActivity(i);
                            }
                        }
                        else{
                            Toast.makeText(Reader_EPUB.this, "Error: No Content Found In List", Toast.LENGTH_SHORT).show();
                        }
                        //endregion
                    }
                    else if(which == 2){
                        //region Show Note
                        String note = "";
                        for(int i=0;i<noteItems.size();i++){if(noteItems.get(i).getCfi().equals(cfiRange)){note = noteItems.get(i).getContent(); break;}}
                        AlertDialog.Builder builder = new AlertDialog.Builder(Reader_EPUB.this);
                        builder.setTitle("Note");
                        builder.setMessage(note);
                        builder.setPositiveButton("Ok", null);
                        builder.create().show();
                        //endregion
                    }
                    else if(which == 3){
                        //region Edit Note
                        int index = -1;
                        String ID = "";
                        String note = "";
                        for(int i=0;i<noteItems.size();i++){if(noteItems.get(i).getCfi().equals(cfiRange)){note = noteItems.get(i).getContent(); ID = noteItems.get(i).getID(); index =i; break;}}
                        if(index == -1){Toast.makeText(Reader_EPUB.this, "Error: Getting Text", Toast.LENGTH_SHORT).show(); return;}

                        final int bufferIndex = index;
                        final String bufferID = ID;
                        Utils.SetInputDialogBox(Reader_EPUB.this, 4, "Edit Note", "Note", note, 0, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                EditText inputText = ((android.support.v7.app.AlertDialog)dialog).findViewById(R.id.inputEditText);
                                String data = inputText.getText().toString();

                                SQLiteDatabase sqLiteDatabase = null;
                                try{
                                    SQLiteOpenHelper helper = new DBSqliteHelper(Reader_EPUB.this, Reader_EPUB.this.getString(R.string.SQLITE_DATABASE), null, Reader_EPUB.this.getResources().getInteger(R.integer.SQLITE_DATABSE_VERSION));
                                    sqLiteDatabase =helper.getWritableDatabase();
                                    sqLiteDatabase.execSQL("update note set content = ? where ID=?", new String[]{ data, bufferID});
                                    noteItems.get(bufferIndex).setContent(data);
                                }
                                catch (Exception ex){
                                    Log.e(TAG, ex.toString());
                                }
                                finally {
                                    if(sqLiteDatabase !=null){sqLiteDatabase.close();}
                                }
                            }
                        });
                        //endregion
                    }
                }
            });
            builder.create().show();
            //endregion
        }
    }
}

class TOCItem{
    private String title;
    private String href;

    TOCItem(String title, String href) {
        this.title = title;
        this.href = href;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getHref() {
        return href;
    }

    public void setHref(String href) {
        this.href = href;
    }
}

class BookmarkItem{
    private String title;
    private String cfi;

    BookmarkItem(String title, String cfi) {
        this.title = title;
        this.cfi = cfi;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getCfi() {
        return cfi;
    }

    public void setCfi(String cfi) {
        this.cfi = cfi;
    }
}

class HighlightItem{
    private String cfi;
    private String contents;
    private String highlightDate;

    HighlightItem(String cfi, String contents, String highlightDate) {
        this.cfi = cfi;
        this.contents = contents;
        this.highlightDate = highlightDate;
    }

    public String getCfi() {
        return cfi;
    }

    public void setCfi(String cfi) {
        this.cfi = cfi;
    }

    public String getContents() {
        return contents;
    }

    public void setContents(String contents) {
        this.contents = contents;
    }

    public String getHighlightDate() {
        return highlightDate;
    }

    public void setHighlightDate(String highlightDate) {
        this.highlightDate = highlightDate;
    }
}

class NoteItem{
    private String ID;
    private String cfi;
    private String title;
    private String content;
    private String notedate;

    public NoteItem(String ID, String cfi, String title, String content, String notedate) {
        this.ID = ID;
        this.cfi = cfi;
        this.title = title;
        this.content = content;
        this.notedate = notedate;
    }

    public String getID() {
        return ID;
    }

    public void setID(String ID) {
        this.ID = ID;
    }

    public String getCfi() {
        return cfi;
    }

    public void setCfi(String cfi) {
        this.cfi = cfi;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getNotedate() {
        return notedate;
    }

    public void setNotedate(String notedate) {
        this.notedate = notedate;
    }
}

class SearchItems{
    String title;
    String cfi;

    public SearchItems(String title, String cfi) {
        this.title = title;
        this.cfi = cfi;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getCfi() {
        return cfi;
    }

    public void setCfi(String cfi) {
        this.cfi = cfi;
    }
}

class SearchItemsAdapter extends RecyclerView.Adapter<SearchItemsAdapter.Holder>{
    Reader_EPUB reader_epub;
    private List<SearchItems> searchItemsList;
    private CustomWebView webview;
    private AlertDialog searchAlertDialog;

    SearchItemsAdapter(Reader_EPUB reader_epub, List<SearchItems> searchItemsList, CustomWebView webview, AlertDialog searchAlertDialog){
        this.reader_epub = reader_epub;
        this.searchItemsList = searchItemsList;
        this.webview = webview;
        this.searchAlertDialog = searchAlertDialog;
    }

    @NonNull
    @Override
    public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(reader_epub).inflate(R.layout.activity_reader_epub_contents_fragment_text, parent, false);
        return new Holder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final Holder holder, int position) {
        holder.title.setText(Html.fromHtml(searchItemsList.get(position).getTitle()));
        holder.view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //WebView GoTo
                if(webview !=null) {
                    reader_epub.SelectSearchStringCfi(holder.getAdapterPosition());
                    searchAlertDialog.dismiss();
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return searchItemsList.size();
    }

    static class Holder extends RecyclerView.ViewHolder{
        View view;
        TextView title;
        Holder(View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.titleView);
            view = itemView.findViewById(R.id.view);
        }
    }
}