package zkagazapahtnajusz.paperproject.com.paperproject.ReaderEngine;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;

import zkagazapahtnajusz.paperproject.com.paperproject.Activity_UserPost;
import zkagazapahtnajusz.paperproject.com.paperproject.Model.BookItems;
import zkagazapahtnajusz.paperproject.com.paperproject.Model.CustomWebView;
import zkagazapahtnajusz.paperproject.com.paperproject.R;
import zkagazapahtnajusz.paperproject.com.paperproject.Utilities.DBSqliteHelper;
import zkagazapahtnajusz.paperproject.com.paperproject.Utilities.NetworkManagerUtils;
import zkagazapahtnajusz.paperproject.com.paperproject.Utilities.Utils;

/**
 * Created by Zw4R-TSTUD10 2018/4/1 [2018, April 1]
 */

public class Reader_EPUB_Contents_Fragment_Notes extends Fragment {
    private static final String TAG = "Reader_EPUB_Contents_Fr";
    RecyclerView recyclerView;
    LinearLayoutManager layoutManager;
    Note_Adapter adapter;
    List<NoteItem> noteItems;
    CustomWebView webview;
    View NoDataFoundView;
    String BookID;
    BookItems.StorageType storageType;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.activity_reader_epub_contents_fragment, container, false);
        initView(view);
        return view;
    }

    public void setNoteItems(List<NoteItem> noteItems) {
        this.noteItems = noteItems;
    }

    public void setWebview(CustomWebView webview) {
        this.webview = webview;
    }

    public String getBookID() {
        return BookID;
    }

    public void setBookID(String bookID) {
        BookID = bookID;
    }

    public BookItems.StorageType getStorageType() {
        return storageType;
    }

    public void setStorageType(BookItems.StorageType storageType) {
        this.storageType = storageType;
    }

    private void initView(View view){
        try{
            recyclerView = view.findViewById(R.id.recyclerView);
            NoDataFoundView = view.findViewById(R.id.NoDataFoundView);
            layoutManager = new LinearLayoutManager(getActivity());
            layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
            recyclerView.setLayoutManager(layoutManager);
            adapter = new Note_Adapter(getActivity(), getStorageType(), getBookID(), noteItems, webview, NoDataFoundView);
            adapter.notifyDataSetChanged();
            recyclerView.setAdapter(adapter);

            ((TextView)NoDataFoundView.findViewById(R.id.title)).setText("No Note found");
            ((TextView)NoDataFoundView.findViewById(R.id.titleDataNotFound)).setText("you can add it from note context menu");
            if(noteItems.size() <= 0){
                NoDataFoundView.setVisibility(View.VISIBLE);
            }
        }
        catch (Exception ex){
            Log.e(TAG, ex.getMessage());
        }
    }
}

class Note_Adapter extends RecyclerView.Adapter<Note_Adapter.Holder>{
    private static final String TAG = "Note_Adapter";
    private Activity activity;
    private List<NoteItem> noteItems;
    private CustomWebView webview;
    View NoDataFoundView;
    String BookID;
    BookItems.StorageType storageType;

    Note_Adapter(Activity activity, BookItems.StorageType storageType, String BookID,  List<NoteItem> noteItems, CustomWebView webview, View NoDataFoundView){
        this.activity = activity;
        this.storageType = storageType;
        this.BookID = BookID;
        this.noteItems = noteItems;
        this.webview = webview;
        this.NoDataFoundView = NoDataFoundView;
    }

    @NonNull
    @Override
    public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(activity).inflate(R.layout.activity_reader_epub_contents_fragment_text, parent, false);
        return new Holder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final Holder holder, int position) {
        String bufferTitle = noteItems.get(position).getTitle();
        String bufferContent = noteItems.get(position).getContent();
        String editNoteDate = noteItems.get(position).getNotedate();
        final String cfiData = noteItems.get(position).getCfi();

        final String bufferContentTemp = bufferContent;

        if(bufferTitle.length() > 30){ bufferTitle = bufferTitle.substring(0, 30);}
        if(bufferContent.length() > 30){ bufferContent = bufferContent.substring(0, 30);}

        String str = (position + 1) + ". <B>\"" + bufferTitle + "\"..</B> " + bufferContent + "..  (" + editNoteDate + ")";
        holder.title.setText(Html.fromHtml(str));

        holder.view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //WebView GoTo
                try {
                    if (webview != null) {
                        webview.loadUrl("javascript:GoToNotePage('" + cfiData + "')");
                        activity.finish();
                    }
                }
                catch (Exception ex){
                    Log.e(TAG, ex.getMessage());
                }
            }
        });

        //region onLongClick
        holder.view.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                OpenNotePopUp(holder, bufferContentTemp, cfiData);
                return true;
            }
        });
        //endregion
    }

    private void OpenNotePopUp(final Holder holder, final String contentData, final String cfiRange){
        try{
            CharSequence menuItems[] = new CharSequence[] {"Remove Note", "Share Note", "Show Note" , "Edit Note"};
            AlertDialog.Builder builder = new AlertDialog.Builder(activity);
            builder.setTitle("Note Options");
            builder.setItems(menuItems, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    if(which == 0){
                        //region Remove Note
                        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
                        builder.setMessage("Are you sure want to remove this note?");
                        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                SQLiteDatabase sqLiteDatabase = null;
                                try{
                                    SQLiteOpenHelper helper = new DBSqliteHelper(activity, activity.getString(R.string.SQLITE_DATABASE), null,activity.getResources().getInteger(R.integer.SQLITE_DATABSE_VERSION));
                                    sqLiteDatabase =helper.getWritableDatabase();
                                    sqLiteDatabase.execSQL("delete from note where ID=?", new String[]{ noteItems.get(holder.getAdapterPosition()).getID()});

                                    //Remove note WebView
                                    new Handler(activity.getMainLooper()).post(new Runnable() {
                                        @Override
                                        public void run() {
                                            webview.loadUrl("javascript:RemoveNote('"+ cfiRange +"')");
                                        }
                                    });

                                    //Remove note from list
                                    noteItems.remove(holder.getAdapterPosition());
                                    //notifyItemRemoved(holder.getAdapterPosition());
                                    notifyDataSetChanged();

                                    if(noteItems.size() <=0){
                                        NoDataFoundView.setVisibility(View.VISIBLE);
                                    }
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
                        //region Share Note
                        if(!contentData.equals("")){
                            if(!NetworkManagerUtils.LoadServerData(activity)){
                                Toast.makeText(activity, "No Network Connection", Toast.LENGTH_SHORT).show();
                            }
                            else{
                                Intent i = new Intent(activity, Activity_UserPost.class);
                                i.putExtra("ShareText", contentData);
                                i.putExtra("BookShareID", BookID);
                                i.putExtra("storeType", storageType.toString());
                                activity.startActivity(i);
                            }
                        }
                        else{
                            Toast.makeText(activity, "Error: No Content Found In List", Toast.LENGTH_SHORT).show();
                        }
                        //endregion
                    }
                    else if(which ==2){
                        //region Show Note
                        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
                        builder.setTitle("Note");
                        builder.setMessage(contentData);
                        builder.setPositiveButton("Ok", null);
                        builder.create().show();
                        //endregion
                    }
                    else if(which == 3){
                        //region Show PromptText box
                        Utils.SetInputDialogBox(activity, 4, "Edit Note", "Note", contentData, 0, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                EditText inputText = ((AlertDialog)dialog).findViewById(R.id.inputEditText);
                                String data = inputText.getText().toString();
                                SQLiteDatabase sqLiteDatabase = null;
                                try{
                                    SQLiteOpenHelper helper = new DBSqliteHelper(activity, activity.getString(R.string.SQLITE_DATABASE), null, activity.getResources().getInteger(R.integer.SQLITE_DATABSE_VERSION));
                                    sqLiteDatabase =helper.getWritableDatabase();
                                    sqLiteDatabase.execSQL("update note set content = ? where ID=?", new String[]{ data, noteItems.get(holder.getAdapterPosition()).getID()});

                                    noteItems.get(holder.getAdapterPosition()).setContent(data);
                                    notifyItemChanged(holder.getAdapterPosition());
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
        }
        catch (Exception ex){
            Log.e(TAG, ex.toString());
        }
    }

    @Override
    public int getItemCount() {
        return noteItems.size();
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