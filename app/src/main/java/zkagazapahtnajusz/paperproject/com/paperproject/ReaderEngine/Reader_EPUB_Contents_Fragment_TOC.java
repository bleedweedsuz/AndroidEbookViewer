package zkagazapahtnajusz.paperproject.com.paperproject.ReaderEngine;

import android.app.Activity;
import android.content.Context;
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

/**
 * Created by Zw4R-TSTUD10 2018/4/1 [2018, April 1]
 */

public class Reader_EPUB_Contents_Fragment_TOC extends Fragment {
    private static final String TAG = "Reader_EPUB_Contents_Fr";
    RecyclerView recyclerView;
    LinearLayoutManager layoutManager;
    TOC_Adapter adapter;
    List<TOCItem> tocItems;
    CustomWebView webview;
    View NoDataFoundView;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.activity_reader_epub_contents_fragment, container, false);
        initView(view);
        return view;
    }

    public void setTocItemList(List<TOCItem> tocItems) {
        this.tocItems = tocItems;
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

            adapter = new TOC_Adapter(getActivity(), tocItems, webview);
            adapter.notifyDataSetChanged();

            recyclerView.setAdapter(adapter);

            if(tocItems.size() <=0){
                NoDataFoundView.setVisibility(View.VISIBLE);
                ((TextView)NoDataFoundView.findViewById(R.id.title)).setText("No Table Of Content found");
                ((TextView)NoDataFoundView.findViewById(R.id.titleDataNotFound)).setText("Error: no table of content added in file.");
            }

        }
        catch (Exception ex){
            Log.e(TAG, ex.getMessage());
        }
    }
}

class TOC_Adapter extends RecyclerView.Adapter<TOC_Adapter.Holder>{
    private static final String TAG = "TOC_Adapter";
    private Activity activity;
    private Context context;
    private List<TOCItem> tocItems;
    private CustomWebView webview;

    TOC_Adapter(Activity activity, List<TOCItem> tocItems, CustomWebView webview){
        this.activity = activity;
        this.context = activity.getApplicationContext();
        this.tocItems = tocItems;
        this.webview = webview;
    }

    @NonNull
    @Override
    public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.activity_reader_epub_contents_fragment_text, parent, false);
        return new Holder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final Holder holder, int position) {
        String title = (position + 1) + ". " + tocItems.get(position).getTitle();
        holder.title.setText(title);

        holder.view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //WebView GoTo
                String URL = tocItems.get(holder.getAdapterPosition()).getHref();
                if(webview !=null) {
                    webview.loadUrl("javascript:GotToTitle('" + URL + "')");
                    activity.finish();
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return tocItems.size();
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