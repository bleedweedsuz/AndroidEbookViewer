package zkagazapahtnajusz.paperproject.com.paperproject.ReaderEngine;

import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.widget.TextView;

import java.util.ArrayList;

import zkagazapahtnajusz.paperproject.com.paperproject.Model.BookItems;
import zkagazapahtnajusz.paperproject.com.paperproject.Model.CustomWebView;
import zkagazapahtnajusz.paperproject.com.paperproject.R;

public class Reader_EPUB_Contents extends AppCompatActivity {
    private static final String TAG = "Reader_EPUB_Contents";
    Toolbar toolbar;
    TextView toolbarTitle;
    ViewPager viewPager;
    TabLayout tabLayout;
    Reader_EPUB_Contents_ViewPagerAdapter adapter;

    public static ArrayList<TOCItem> tocItems = new ArrayList<>();
    public static ArrayList<BookmarkItem> bookmarkItems = new ArrayList<>();
    public static ArrayList<HighlightItem> highlightItems = new ArrayList<>();
    public static ArrayList<NoteItem> noteItems = new ArrayList<>();
    public static CustomWebView webview;
    public static String BookID;
    public static BookItems.StorageType storageType;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reader_epub_contents);
        toolbar = findViewById(R.id.toolbar                                                                                                                                                 );
        toolbarTitle = findViewById(R.id.toolbarTitle);

        setSupportActionBar(toolbar);
        if(getSupportActionBar() != null){
            getSupportActionBar().setDisplayShowTitleEnabled(false);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }

        viewPager = findViewById(R.id.viewPagerUserProfile);
        adapter = new Reader_EPUB_Contents_ViewPagerAdapter(getSupportFragmentManager());

        //Toc Items
        Reader_EPUB_Contents_Fragment_TOC reader_epub_contents_fragment_toc = new Reader_EPUB_Contents_Fragment_TOC();
        reader_epub_contents_fragment_toc.setTocItemList(tocItems);
        reader_epub_contents_fragment_toc.setWebview(webview);
        adapter.addFragment(reader_epub_contents_fragment_toc);

        //Bookmark
        Reader_EPUB_Contents_Fragment_Bookmark reader_epub_contents_fragment_bookmark = new Reader_EPUB_Contents_Fragment_Bookmark();
        reader_epub_contents_fragment_bookmark.setBookmarkItems(bookmarkItems);
        reader_epub_contents_fragment_bookmark.setWebview(webview);
        adapter.addFragment(reader_epub_contents_fragment_bookmark);

        //Notes
        Reader_EPUB_Contents_Fragment_Notes reader_epub_contents_fragment_notes = new Reader_EPUB_Contents_Fragment_Notes();
        reader_epub_contents_fragment_notes.setBookID(BookID);
        reader_epub_contents_fragment_notes.setStorageType(storageType);
        reader_epub_contents_fragment_notes.setNoteItems(noteItems);
        reader_epub_contents_fragment_notes.setWebview(webview);
        adapter.addFragment(reader_epub_contents_fragment_notes);

        //Highlight
        Reader_EPUB_Contents_Fragment_Highlights reader_epub_contents_fragment_highlights = new Reader_EPUB_Contents_Fragment_Highlights();
        reader_epub_contents_fragment_highlights.setHighlightItems(highlightItems);
        reader_epub_contents_fragment_highlights.setWebview(webview);
        adapter.addFragment(reader_epub_contents_fragment_highlights);

        viewPager.setAdapter(adapter);

        tabLayout = findViewById(R.id.tabLayout);
        tabLayout.setupWithViewPager(viewPager);

        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                toolbarTitle.setText(adapter.getPageTitle(position));
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId() == android.R.id.home){
            finish();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(R.anim.slide_in_left,R.anim.slide_out_right);
    }
}