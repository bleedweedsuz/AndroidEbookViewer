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

public class Reader_EPUB_Contents_Fragment_Highlights extends Fragment {
    private static final String TAG = "Reader_EPUB_Contents_Fr";
    
    RecyclerView recyclerView;
    LinearLayoutManager layoutManager;
    Highlight_Adapter adapter;
    List<HighlightItem> highlightItems;
    CustomWebView webview;
    View NoDataFoundView;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.activity_reader_epub_contents_fragment, container, false);
        initView(view);
        return view;
    }

    public void setHighlightItems(List<HighlightItem> highlightItems) {
        this.highlightItems = highlightItems;
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
            adapter = new Highlight_Adapter(getActivity(), highlightItems, webview, NoDataFoundView);
            adapter.notifyDataSetChanged();
            recyclerView.setAdapter(adapter);

            ((TextView)NoDataFoundView.findViewById(R.id.title)).setText("No Highlight found");
            ((TextView)NoDataFoundView.findViewById(R.id.titleDataNotFound)).setText("you can add it from highlight context menu");
            if(highlightItems.size() <=0){
                NoDataFoundView.setVisibility(View.VISIBLE);
            }
        }
        catch (Exception ex){
            Log.e(TAG, ex.getMessage());
        }
    }
}

class Highlight_Adapter extends RecyclerView.Adapter<Highlight_Adapter.Holder>{
    private static final String TAG = "TOC_Adapter";
    private Activity activity;
    private Context context;
    private List<HighlightItem> highlightItems;
    private CustomWebView webview;
    private View NoDataFoundView;

    Highlight_Adapter(Activity activity, List<HighlightItem> highlightItems, CustomWebView webview, View NoDataFoundView){
        this.activity = activity;
        this.context = activity.getApplicationContext();
        this.highlightItems = highlightItems;
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
        String contentData = highlightItems.get(position).getContents();

        if(contentData.length() > 60){
            contentData = contentData.substring(0,60) + "..";
        }
        String title = (position + 1) + ". " + contentData + " (" + highlightItems.get(position).getHighlightDate() +")";
        holder.title.setText(title);

        holder.view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            //WebView GoTo
            try {String CFI = highlightItems.get(holder.getAdapterPosition()).getCfi();if (webview != null) {webview.loadUrl("javascript:GoToBookMarkPage('" + CFI + "')");activity.finish();}}
            catch (Exception ex){Log.e(TAG, ex.getMessage());}
            }
        });

        //region onLongClick
        holder.view.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(activity);
                builder.setMessage("Are you sure want to remove this highlight?");
                builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        SQLiteDatabase sqLiteDatabase = null;
                        try{
                            SQLiteOpenHelper helper = new DBSqliteHelper(context, context.getString(R.string.SQLITE_DATABASE), null, context.getResources().getInteger(R.integer.SQLITE_DATABSE_VERSION));
                            sqLiteDatabase =helper.getWritableDatabase();
                            sqLiteDatabase.execSQL("delete from highlight where cfi = ? and UID = ?", new String[]{ highlightItems.get(holder.getAdapterPosition()).getCfi(), FirebaseUtils.firebaseAuth.getCurrentUser().getUid()});

                            //Remove Highlight WebView
                            webview.loadUrl("javascript:RemoveHighlight('"+ highlightItems.get(holder.getAdapterPosition()).getCfi() +"')");

                            highlightItems.remove(holder.getAdapterPosition());
                            notifyDataSetChanged();
                        }
                        catch (Exception ex){
                            Log.e(TAG, ex.toString());
                        }
                        finally {
                            if(sqLiteDatabase !=null){sqLiteDatabase.close();}
                        }

                        //No Data Found View
                        if(highlightItems.size() <= 0){ NoDataFoundView.setVisibility(View.VISIBLE);}
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
        return highlightItems.size();
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