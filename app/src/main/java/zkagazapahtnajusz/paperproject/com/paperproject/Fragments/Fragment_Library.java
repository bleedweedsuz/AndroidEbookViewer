package zkagazapahtnajusz.paperproject.com.paperproject.Fragments;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.view.animation.LayoutAnimationController;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import zkagazapahtnajusz.paperproject.com.paperproject.Activity_BookDetails;
import zkagazapahtnajusz.paperproject.com.paperproject.Activity_Home;
import zkagazapahtnajusz.paperproject.com.paperproject.Model.BookItems;
import zkagazapahtnajusz.paperproject.com.paperproject.Model.BookItemWrapper;
import zkagazapahtnajusz.paperproject.com.paperproject.R;
import zkagazapahtnajusz.paperproject.com.paperproject.Utilities.FirebaseUtils;
import zkagazapahtnajusz.paperproject.com.paperproject.Utilities.SharedPreferenceUtils;
import zkagazapahtnajusz.paperproject.com.paperproject.Utilities.Utils;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static zkagazapahtnajusz.paperproject.com.paperproject.Activity_Home.GRID_ITEM;
import static zkagazapahtnajusz.paperproject.com.paperproject.Activity_Home.LIST_ITEM;
import static zkagazapahtnajusz.paperproject.com.paperproject.Activity_Home.isSwitchView;

/**
 * Created by Zw4R-TSTUD10 2018/4/1 [2018, April 1]
 */

public class Fragment_Library extends Fragment implements SwipeRefreshLayout.OnRefreshListener{
    private String TAG = "FRAGMENT LIBRARY";

    public static int CONTAINER_HEIGHT_MODIFY = 82;
    public static int IMAGE_HEIGHT_MODIFY = 130;
    RecyclerView myRecyclerView;
    RecyclerAdapterOuter recyclerAdapter;
    List<BookItemWrapper> bookItemWrappers = new ArrayList<>();
    List<BookItemWrapper> tempBookItemWrappers = new ArrayList<>();
    SwipeRefreshLayout swipeRefreshLayout;
    int threadCount = 0;
    LinearLayoutManager linearLayoutManager;
    View progressBar;
    boolean searchInLibrary = true;
    boolean isSearverDatataFound =false, isLocalDataFound = false;
    int totalServerData =0;
    ListenerRegistration listenerRegistration;
    public static boolean isNeedToReload = false;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_library,container,false);
        myRecyclerView = view.findViewById(R.id.recyclerView_Library);
        swipeRefreshLayout = view.findViewById(R.id.swipeRefresh);
        progressBar = view.findViewById(R.id.ProgressWrapper);
        swipeRefreshLayout.setOnRefreshListener(this);

        linearLayoutManager = new LinearLayoutManager(getActivity());

        recyclerAdapter = new RecyclerAdapterOuter(getActivity(), bookItemWrappers);
        myRecyclerView.setLayoutManager(linearLayoutManager);

        if(!Activity_Home.runOnce){
            LayoutAnimationController controller = AnimationUtils.loadLayoutAnimation(getActivity(),R.anim.layout_fall_down);
            myRecyclerView.setLayoutAnimation(controller);
            Activity_Home.runOnce = true;
        }
        myRecyclerView.setAdapter(recyclerAdapter);

        progressBar.setVisibility(View.VISIBLE);
        LoadAllBooksFromServer();
        return view;
    }

    public void changeRecyclerToGrid(){
        recyclerAdapter.changeRecyclerToGrid();
    }

    public void changerRecyclerToSingle(){
        recyclerAdapter.changerRecyclerToSingle();
    }

    public boolean getItemViewTypeFromRecyclerView(){
        return recyclerAdapter.getItemViewTypeFromRecyclerView();
    }

    public void LoadAllBooksFromServer(){
        try{
            isSearverDatataFound =false;
            isLocalDataFound =false;
            tempBookItemWrappers.clear();
            bookItemWrappers.clear();
            threadCount =0;
            totalServerData =0;

            final BookItemWrapper buffer = new BookItemWrapper();
            buffer.Tag = "Cloud Library";
            buffer.isDataLoad = false;
            buffer.insideData = new ArrayList<>();
            tempBookItemWrappers.add(buffer);

            String UID = FirebaseUtils.firebaseAuth.getCurrentUser().getUid();
            String Root = getString(R.string.FIRESTORE_ROOT_USERDETAILS) + "/" + UID + "/" + getString(R.string.FIRESTORE_ROOT_LIBRARY);
            Query query = FirebaseFirestore.getInstance().collection(Root).whereEqualTo(getString(R.string.F_USERDETAILS_LIBRARY_STATE), Activity_BookDetails.LibraryState.ADDED.toString()).orderBy(getString(R.string.F_USERDETAILS_LIBRARY_DATE), Query.Direction.DESCENDING);
            listenerRegistration = FirebaseUtils.FirestoreGetAllData(query, getActivity(), new EventListener<QuerySnapshot>() {
                @Override
                public void onEvent(QuerySnapshot queryDocumentSnapshots, FirebaseFirestoreException e) {
                    if(queryDocumentSnapshots != null && !queryDocumentSnapshots.isEmpty()){
                        totalServerData = queryDocumentSnapshots.size();
                        for(DocumentChange doc : queryDocumentSnapshots.getDocumentChanges()){
                            if(doc.getType() == DocumentChange.Type.ADDED){
                                //Get BOOK ID
                                final String BOOKID = doc.getDocument().getString(getString(R.string.F_USERDETAILS_LIBRARY_BOOKID));
                                String _Root = getString(R.string.FIRESTORE_ROOT_BOOKS) + "/" + BOOKID;
                                FirebaseUtils.FirestoreGetData(_Root, new OnSuccessListener<DocumentSnapshot>() {
                                    @Override
                                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                                        if (documentSnapshot.exists()) {
                                            BookItems bookItems = new BookItems(
                                                    documentSnapshot.getString(getString(R.string.F_BOOKS_PUBLICKEY)),
                                                    documentSnapshot.getString(getString(R.string.F_BOOKS_IMAGEURL)),
                                                    documentSnapshot.getString(getString(R.string.F_BOOKS_BOOKID)),
                                                    documentSnapshot.getString(getString(R.string.F_BOOKS_TITLE)),
                                                    documentSnapshot.getString(getString(R.string.F_BOOKS_AUTHOR)),
                                                    documentSnapshot.getString(getString(R.string.F_BOOKS_DESCRIPTION)),
                                                    Double.valueOf(documentSnapshot.get(getString(R.string.F_BOOKS_PRICE)).toString()),
                                                    documentSnapshot.getString(getString(R.string.F_BOOKS_CURRENCYSTR)),
                                                    (ArrayList) documentSnapshot.get(getString(R.string.F_BOOKS_GENRE)),
                                                    BookItems.BookFlag.valueOf(documentSnapshot.getString(getString(R.string.F_BOOKS_BOOKFLAG))),
                                                    BookItems.FileType.EPUB,
                                                    BookItems.StorageType.CLOUD
                                            );

                                            String uDate_ = documentSnapshot.get(getString(R.string.F_USERDETAILS_LIBRARY_DATE)).toString();
                                            long uDate = Date.parse(uDate_);
                                            bookItems.sn = uDate;
                                            bookItems.uDate = uDate_;

                                            buffer.insideData.add(bookItems);
                                            threadCount++;
                                            CheckThreadCount(threadCount);
                                        }
                                    }
                                }, new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        //Data not found in server
                                        SearchInLocalDirectory();
                                    }
                                });
                            }
                        }
                    }
                    else{
                        if(e !=null){Log.e(TAG, e.toString());}
                        //Data not found in server
                        SearchInLocalDirectory();
                    }
                }
            });
        }
        catch (Exception ex){
            progressBar.setVisibility(View.INVISIBLE);
            swipeRefreshLayout.setRefreshing(false);
            SearchInLocalDirectory();
        }
    }

    private void CheckThreadCount(int count){
        try {
            int totalThread = totalServerData;
            if (count >= totalThread) {
                //for(int i=0; i<tempBookItemWrappers.get(0).insideData.size();i++){
                //    Log.e(TAG, "BEFORE SORT:" + i + ". ---->" + tempBookItemWrappers.get(0).insideData.get(i).sn + "::::" + tempBookItemWrappers.get(0).insideData.get(i).uDate);
                //}

                Collections.sort(tempBookItemWrappers.get(0).insideData);

                //for(int i=0; i<tempBookItemWrappers.get(0).insideData.size();i++){
                //    Log.e(TAG, "AFTER SORT:" + i + ". ---->" + tempBookItemWrappers.get(0).insideData.get(i).sn + "::::" + tempBookItemWrappers.get(0).insideData.get(i).uDate);
                //}

                tempBookItemWrappers.get(0).isDataLoad = true; //Library Data Added
                if (searchInLibrary) {
                    SearchInLocalDirectory();
                } else {
                    ReOrganizeData();
                }
            }
        }
        catch (Exception ex){
            Log.e(TAG, ex.toString());
        }
    }

    private void ReOrganizeData(){
        swipeRefreshLayout.setRefreshing(false);
        progressBar.setVisibility(View.INVISIBLE);

        bookItemWrappers = tempBookItemWrappers;
        recyclerAdapter = new RecyclerAdapterOuter(getContext(), bookItemWrappers);
        myRecyclerView.setAdapter(recyclerAdapter);
    }

    private void SearchInLocalDirectory(){
        try{
            new Thread(new Runnable() {
                @Override
                public void run() {
                    BookItemWrapper buffer = new BookItemWrapper();
                    buffer.Tag = "Local Drive";
                    buffer.insideData = new ArrayList<>();

                    if(SharedPreferenceUtils.Setting_GetData(getContext(), getString(R.string.LibrarySetting_ShowLocalDrive), true)) {
                        ArrayList<File> files = Utils.SearchFileInLocalDirectory(Fragment_Library.this.getContext());
                        for (File f : files) {
                            //Add Data In System
                            buffer.isDataLoad = true;
                            String Ext = f.getAbsolutePath().substring(f.getAbsolutePath().lastIndexOf(".")).toUpperCase();

                            BookItems.FileType fileType = BookItems.FileType.Nothing;
                            if (Ext.equals(".EPUB")) {
                                fileType = BookItems.FileType.EPUB;
                            }
                            else if(SharedPreferenceUtils.Setting_GetData(Fragment_Library.this.getContext(), getString(R.string.LibrarySetting_FilterFileExtension_PDF), true) && Ext.equals(".PDF")){
                                fileType = BookItems.FileType.PDF;
                            }
                            else if(SharedPreferenceUtils.Setting_GetData(Fragment_Library.this.getContext(), getString(R.string.LibrarySetting_FilterFileExtension_DOC), false) && Ext.equals(".DOC")){
                                fileType = BookItems.FileType.DOC;
                            }
                            else if(SharedPreferenceUtils.Setting_GetData(Fragment_Library.this.getContext(), getString(R.string.LibrarySetting_FilterFileExtension_PPT), false) && (Ext.equals(".PPT") || Ext.equals(".PPTX"))){
                                fileType = BookItems.FileType.PPT;
                            }
                            else if(SharedPreferenceUtils.Setting_GetData(Fragment_Library.this.getContext(), getString(R.string.LibrarySetting_FilterFileExtension_XLS), false) && (Ext.equals(".XLS") || Ext.equals(".XLSX"))){
                                fileType = BookItems.FileType.XLS;
                            }
                            else if(SharedPreferenceUtils.Setting_GetData(Fragment_Library.this.getContext(), getString(R.string.LibrarySetting_FilterFileExtension_TXT), true) && Ext.equals(".TXT")){
                                fileType = BookItems.FileType.TXT;
                            }

                            if(fileType != BookItems.FileType.Nothing) {
                                BookItems bookItems = new BookItems(
                                        "",
                                        "",
                                        f.getAbsolutePath(),
                                        f.getName(),
                                        "",
                                        f.getAbsolutePath(),
                                        0.0,
                                        "",
                                        null,
                                        BookItems.BookFlag.Purchased,
                                        fileType,
                                        BookItems.StorageType.LOCAL
                                );
                                bookItems.Ext = Ext;
                                bookItems.LocalFileURL = f.getAbsolutePath();
                                buffer.insideData.add(bookItems);
                            }
                        }
                        tempBookItemWrappers.add(buffer);
                    }
                    try {
                        new Handler(getContext().getMainLooper()).post(new Runnable() {
                            @Override
                            public void run() {
                                ReOrganizeData();
                            }
                        });
                    }
                    catch (Exception ex){
                        Log.e(TAG, ex.toString());
                    }
                }
            }).start();
        }
        catch (Exception ex){
            Log.e(TAG, ex.toString());
        }
    }

    @Override
    public void onRefresh() {
        if(listenerRegistration != null){listenerRegistration.remove();listenerRegistration = null;}
        linearLayoutManager = new LinearLayoutManager(getActivity());
        myRecyclerView.setLayoutManager(linearLayoutManager);
        LoadAllBooksFromServer();
    }

    @Override
    public void onResume() {
        super.onResume();
        if(isNeedToReload){
            isNeedToReload = false;
            onRefresh();
        }
    }
}

class RecyclerAdapterOuter extends RecyclerView.Adapter<RecyclerAdapterOuter.HolderOuter>{
    private Context mContext;
    private List<BookItemWrapper> tempTags;
    private RecyclerView mRecyclerViewInner;
    private RecyclerAdapterInner recyclerAdapterInner;

    private List<RecyclerView> listRecyclerView = new ArrayList<>();
    private List<RecyclerAdapterInner> listRecyclerAdapter = new ArrayList<>();

    public RecyclerAdapterOuter(Context mContext, List<BookItemWrapper> tempTags ) {
        this.mContext = mContext;
        this.tempTags = tempTags;
    }

    @NonNull
    @Override
    public HolderOuter onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view, NoDataFound;
        TextView titleDataNotFound;
        view = LayoutInflater.from(mContext).inflate(R.layout.recycler_item_outer, parent, false);

        mRecyclerViewInner = view.findViewById(R.id.recyclerViewInner);
        NoDataFound = view.findViewById(R.id.NoDataFoundView);
        titleDataNotFound = view.findViewById(R.id.titleDataNotFound);

        if(!tempTags.get(viewType).isDataLoad){
            NoDataFound.setVisibility(View.VISIBLE);
            if(tempTags.get(viewType).Tag.equals("Cloud Library")) {
                titleDataNotFound.setText("No books found in cloud library, you can add it from store.");
            }
            else if(tempTags.get(viewType).Tag.equals("Local Drive")) {
                titleDataNotFound.setText("No files found in local drive. [for more info about search file please use setting section]");
            }
        }
        return new HolderOuter(view);
    }

    @Override
    public int getItemViewType(int position) {
        return position;
    }

    @Override
    public void onBindViewHolder(@NonNull HolderOuter holder, int position) {
        holder.tTitles.setText(tempTags.get(position).Tag);
        recyclerAdapterInner = new RecyclerAdapterInner(mContext,tempTags.get(position).insideData);

        listRecyclerView.add(mRecyclerViewInner);

        if(isSwitchView){
            mRecyclerViewInner.setLayoutManager(new LinearLayoutManager(mContext));

        }else{
            int spanCount = Utils.CURRENT_GRID_COUNT/2;
            mRecyclerViewInner.setLayoutManager(new GridLayoutManager(mContext,spanCount));
        }
        listRecyclerAdapter.add(recyclerAdapterInner);
        mRecyclerViewInner.setAdapter(recyclerAdapterInner);

    }

    @Override
    public int getItemCount() {
        return tempTags.size();
    }

    class HolderOuter extends RecyclerView.ViewHolder{

        TextView tTitles;
        RecyclerView tRecycler;
        public HolderOuter(View view){
            super(view);
            tTitles = view.findViewById(R.id.recyclerOuter_TagTitle);
            tRecycler = view.findViewById(R.id.recyclerViewInner);
        }
    }

    public void changeRecyclerToGrid(){
        for (int i = 0; i < listRecyclerView.size(); i++){
            int spanCount = Utils.CURRENT_GRID_COUNT/2;
            listRecyclerView.get(i).setLayoutManager(new GridLayoutManager(mContext,spanCount));
            listRecyclerAdapter.get(i).notifyDataSetChanged();
        }
    }

    public void changerRecyclerToSingle(){
        for (int i = 0; i < listRecyclerView.size(); i++){
            listRecyclerView.get(i).setLayoutManager(new LinearLayoutManager(mContext));
            listRecyclerAdapter.get(i).notifyDataSetChanged();
        }
    }

    public boolean getItemViewTypeFromRecyclerView(){
        return recyclerAdapterInner.toggleItemViewType();
    }
}

class RecyclerAdapterInner extends RecyclerView.Adapter<RecyclerAdapterInner.HolderInner>{
    private static final String TAG = "RecyclerAdapterInner";
    private Context mContext;
    private List<BookItems> insideTags;

    RecyclerAdapterInner(Context mContext,  List<BookItems> insideTag) {
        this.mContext = mContext;
        this.insideTags = insideTag;
    }

    @NonNull
    @Override
    public HolderInner onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view;
        if(viewType ==  LIST_ITEM){
            view = LayoutInflater.from(mContext).inflate(R.layout.recycler_item_inner_list_library, parent, false);
        }else{
            view = LayoutInflater.from(mContext).inflate(R.layout.recycler_item_inner_grid_library, parent, false);
        }
         return new HolderInner(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final HolderInner holder, int position) {

        if(holder.tName != null){
            holder.tName.setText(insideTags.get(position).Name);
        }
        if(holder.tDescription != null){
            holder.tDescription.setText(insideTags.get(position).Description);
        }

        if(insideTags.get(position).storageType != BookItems.StorageType.CLOUD){
            //region Local Part

            try {holder.tName.setMaxLines(2);holder.tName.setEllipsize(TextUtils.TruncateAt.END);}catch (Exception ex){Log.e(TAG, ex.toString());}

            if (holder.tImage != null) {
                if(insideTags.get(position).fileType == BookItems.FileType.PDF) {
                    Utils.SetImageViaRes((Activity)mContext, R.drawable.icon_pdf, holder.tImage);
                }
                else if(insideTags.get(position).fileType == BookItems.FileType.EPUB) {
                    Utils.SetImageViaRes((Activity)mContext, R.drawable.icon_epub, holder.tImage);
                }
                else if(insideTags.get(position).fileType == BookItems.FileType.DOC) {
                    Utils.SetImageViaRes((Activity)mContext, R.drawable.icon_word, holder.tImage);
                }
                else if(insideTags.get(position).fileType == BookItems.FileType.PPT) {
                    Utils.SetImageViaRes((Activity)mContext, R.drawable.icon_powerpoint, holder.tImage);
                }
                else if(insideTags.get(position).fileType == BookItems.FileType.XLS) {
                    Utils.SetImageViaRes((Activity)mContext, R.drawable.icon_excel, holder.tImage);
                }
                else if(insideTags.get(position).fileType == BookItems.FileType.TXT) {
                    Utils.SetImageViaRes((Activity)mContext, R.drawable.icon_txt, holder.tImage);
                }
                holder.tImage.setScaleType(ImageView.ScaleType.FIT_XY);
            }

            if(!isSwitchView){
                int height = Utils.px2Dp(Fragment_Library.IMAGE_HEIGHT_MODIFY, mContext.getResources());
                holder.tImage.getLayoutParams().height = height;
            }
            else {
                int height = Utils.px2Dp(Fragment_Library.CONTAINER_HEIGHT_MODIFY, mContext.getResources());

                holder.textContainer.getLayoutParams().height = height;
                holder.imageContainer.getLayoutParams().height = height;

                int width = Utils.px2Dp(72, mContext.getResources());
                holder.tImage.getLayoutParams().width = width;

                RelativeLayout.LayoutParams layout = new RelativeLayout.LayoutParams(holder.tImage.getLayoutParams());
                layout.setMargins(2, 0, 0, 0);//left,right,top,bottom
                holder.tImage.setLayoutParams(layout);
            }

            holder.textOptionMenu.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    PopupMenu popupMenu = new PopupMenu(mContext,holder.textOptionMenu);
                    popupMenu.inflate(R.menu.library_menu_local);
                    popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                        @Override
                        public boolean onMenuItemClick(MenuItem item) {
                            switch (item.getItemId()){
                                case R.id.menuInfo:
                                    String Message = "File Name: " + insideTags.get(holder.getAdapterPosition()).Name + "\n\n" +
                                                    "Format: " + insideTags.get(holder.getAdapterPosition()).fileType.toString() + "\n\n" +
                                                    "Location: " + insideTags.get(holder.getAdapterPosition()).LocalFileURL;

                                    new AlertDialog.Builder(mContext).setTitle("Book Info").setMessage(Message).setPositiveButton("Ok", null).create().show();
                                    break;
                                default:
                                    break;
                            }
                            return false;
                        }
                    });
                    popupMenu.show();
                }
            });
            //endregion
         }
        else {
            //region Cloud Part

            //region Adding Author
            if(holder.author != null) {
                if(insideTags.get(position).Author.equals("")){
                    holder.author.setVisibility(View.GONE);
                }
                else {
                    String authordata = "by <font color='#333333'>" + insideTags.get(position).Author + "</font>";
                    holder.author.setText(Html.fromHtml(authordata));
                }
            }
            //endregion

            if (holder.tImage != null) {
                ColorDrawable colorDrawableInfoPlaceholder = new ColorDrawable(ContextCompat.getColor(mContext, R.color.colorDrawableInfoPlaceholder));
                ColorDrawable colorDrawableErrorPlaceholder = new ColorDrawable(ContextCompat.getColor(mContext, R.color.colorDrawableErrorPlaceholder));

                Utils.SetImageViaUri(
                        (Activity)mContext,
                        insideTags.get(position).imageURL,
                        holder.tImage,
                        false,
                        false,
                        colorDrawableInfoPlaceholder,
                        colorDrawableErrorPlaceholder,
                        null
                );
            }

            holder.textOptionMenu.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    PopupMenu popupMenu = new PopupMenu(mContext,holder.textOptionMenu);
                    popupMenu.inflate(R.menu.library_menu_cloud);

                    popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                        @Override
                        public boolean onMenuItemClick(MenuItem item) {
                            switch (item.getItemId()){
                                case R.id.menuView:
                                    ReadBook(insideTags.get(holder.getAdapterPosition()));
                                    break;
                                case R.id.menuInfo:
                                    //GoToBookDetails(holder);
                                    Utils.GoToBookDetails(mContext, insideTags.get(holder.getAdapterPosition()).BOOKID);
                                    break;
                                case R.id.menuDelete:
                                    new AlertDialog.Builder(mContext).
                                            setTitle("Delete").
                                            setMessage("Are you sure want to remove this from library? \n(*note you can re-download it from your purchase section.)").
                                            setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialog, int which) {
                                                    DeleteCloudBook(insideTags.get(holder.getAdapterPosition()).BOOKID);
                                                    insideTags.remove(holder.getAdapterPosition());
                                                    RecyclerAdapterInner.this.notifyItemRemoved(holder.getAdapterPosition());
                                                }
                                            }).
                                            setNegativeButton("No", null).
                                            create().show();
                                    break;
                                default:
                                    break;
                            }
                            return false;
                        }
                    });
                    popupMenu.show();

                }
            });
            //endregion
        }

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
            ReadBook(insideTags.get(holder.getAdapterPosition()));
            }
        });
    }

    private void ReadBook(BookItems bookItems){
        Utils.OpenBook(mContext, bookItems);
    }

    private void DeleteCloudBook(final String BookID){
        try{
            String UID = FirebaseUtils.firebaseAuth.getCurrentUser().getUid();
            String Root = mContext.getString(R.string.FIRESTORE_ROOT_USERDETAILS) + "/" + UID + "/" + mContext.getString(R.string.FIRESTORE_ROOT_LIBRARY) + "/" + BookID;
            Map<String, Object> map = new HashMap<>();
            map.put(mContext.getString(R.string.F_USERDETAILS_LIBRARY_STATE), Activity_BookDetails.LibraryState.DELETE.toString());
            FirebaseUtils.FirestroreSetData(Root, map,
                    new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            Utils.LocalFileRemove(BookID);
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
    public int getItemCount() {
        return insideTags.size();
    }

    class HolderInner extends RecyclerView.ViewHolder{

        ImageView tImage;
        TextView tName, tDescription ,textOptionMenu, author;
        View textContainer, imageContainer;
        HolderInner(View view){
            super(view);
            author = view.findViewById(R.id.author);
            tImage = view.findViewById(R.id.recyclerItemImage);
            tName = view.findViewById(R.id.recyclerItemName);
            tDescription = view.findViewById(R.id.recyclerItemDescription);
            textOptionMenu = view.findViewById(R.id.optionMenu);
            textContainer = view.findViewById(R.id.textContainer);
            imageContainer = view.findViewById(R.id.imageContainer);
        }
    }

    @Override
    public int getItemViewType(int position) {
        if (isSwitchView){
            return LIST_ITEM;
        }else{
            return GRID_ITEM;
        }
    }

    boolean toggleItemViewType(){
        isSwitchView = !isSwitchView;
        return isSwitchView;
    }
}
