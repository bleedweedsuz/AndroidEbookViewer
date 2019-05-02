package zkagazapahtnajusz.paperproject.com.paperproject.ReaderEngine;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

import zkagazapahtnajusz.paperproject.com.paperproject.Model.CustomWebView;
import zkagazapahtnajusz.paperproject.com.paperproject.R;
import zkagazapahtnajusz.paperproject.com.paperproject.Utilities.DBSqliteHelper;
import zkagazapahtnajusz.paperproject.com.paperproject.Utilities.FirebaseUtils;

/**
 * Created by Zw4R-TSTUD10 2018/4/1 [2018, April 1]
 */

public class Reader_EPUB_Contents_Fragment_Bookmark extends Fragment {

    private static final String TAG = "Reader_EPUB_Contents_Fr";
    RecyclerView recyclerView;
    LinearLayoutManager layoutManager;
    Bookmark_Adapter adapter;
    List<BookmarkItem> bookmarkItems;
    CustomWebView webview;
    View NoDataFoundView;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.activity_reader_epub_contents_fragment, container, false);
        initView(view);
        return view;
    }

    public void setBookmarkItems(List<BookmarkItem> bookmarkItems) {
        this.bookmarkItems = bookmarkItems;
    }

    public void setWebview(CustomWebView webview) {
        this.webview = webview;
    }

    private void initView(View view){
        try{
            recyclerView = view.findViewById(R.id.recyclerView);
            NoDataFoundView = view.findViewById(R.id.NoDataFoundView);
            layoutManager = new LinearLayoutManager(getActivity());
            layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
            recyclerView.setLayoutManager(layoutManager);
            adapter = new Bookmark_Adapter(getActivity(), bookmarkItems, webview, NoDataFoundView);
            adapter.notifyDataSetChanged();
            recyclerView.setAdapter(adapter);

            ((TextView)NoDataFoundView.findViewById(R.id.title)).setText("No Bookmark found");
            ((TextView)NoDataFoundView.findViewById(R.id.titleDataNotFound)).setText("you can add it from bookmark context menu");
            if(bookmarkItems.size() <=0){
                NoDataFoundView.setVisibility(View.VISIBLE);
            }
        }
        catch (Exception ex){
            Log.e(TAG, ex.getMessage());
        }
    }
}

class Bookmark_Adapter extends RecyclerView.Adapter<Bookmark_Adapter.Holder>{
    private static final String TAG = "TOC_Adapter";
    private Activity activity;
    private Context context;
    private List<BookmarkItem> bookmarkItems;
    private CustomWebView webview;
    private View NoDataFoundView;

    Bookmark_Adapter(Activity activity, List<BookmarkItem> bookmarkItems, CustomWebView webview, View NoDataFoundView){
        this.activity = activity;
        this.context = activity.getApplicationContext();
        this.bookmarkItems = bookmarkItems;
        this.webview = webview;
        this.NoDataFoundView = NoDataFoundView;
    }

    @NonNull
    @Override
    public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.activity_reader_epub_contents_fragment_text, parent, false);
        return new Holder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final Holder holder, int position) {
        String title = (position+1) + ". " + bookmarkItems.get(position).getTitle();holder.title.setText(title);

        //region onClick
        holder.view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            //WebView GoTo
            try {
                String CFI = bookmarkItems.get(holder.getAdapterPosition()).getCfi();
                if (webview != null) {
                    webview.loadUrl("javascript:GoToBookMarkPage('" + CFI + "')");
                    activity.finish();
                }
            }
            catch (Exception ex){
                Log.e(TAG, ex.getMessage());
            }
            }
        });
        //endregion

        //region onLongClick
        holder.view.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(activity);
                builder.setMessage("Are you sure want to remove this bookmark?");
                builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        SQLiteDatabase sqLiteDatabase = null;
                        try{
                            SQLiteOpenHelper helper = new DBSqliteHelper(context, context.getString(R.string.SQLITE_DATABASE), null, context.getResources().getInteger(R.integer.SQLITE_DATABSE_VERSION));
                            sqLiteDatabase =helper.getWritableDatabase();
                            sqLiteDatabase.execSQL("delete from bookmark where cfi = ? and UID = ?", new String[]{ bookmarkItems.get(holder.getAdapterPosition()).getCfi(), FirebaseUtils.firebaseAuth.getCurrentUser().getUid()});

                            bookmarkItems.remove(holder.getAdapterPosition());
                            notifyDataSetChanged();
                        }
                        catch (Exception ex){
                            Log.e(TAG, ex.toString());
                        }
                        finally {
                            if(sqLiteDatabase !=null){sqLiteDatabase.close();}
                        }

                        //No Data Found View
                        if(bookmarkItems.size() <= 0){ NoDataFoundView.setVisibility(View.VISIBLE);}
                    }
                });
                builder.setNegativeButton("Cancel", null);
                builder.create().show();
                return false;
            }
        });
        //endregion
    }

    @Override
    public int getItemCount() {
        return bookmarkItems.size();
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