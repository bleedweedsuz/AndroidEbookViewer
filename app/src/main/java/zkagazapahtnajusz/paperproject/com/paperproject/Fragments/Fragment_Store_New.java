package zkagazapahtnajusz.paperproject.com.paperproject.Fragments;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.darwindeveloper.horizontalscrollmenulibrary.custom_views.HorizontalScrollMenuView;
import com.darwindeveloper.horizontalscrollmenulibrary.extras.MenuItem;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import zkagazapahtnajusz.paperproject.com.paperproject.Activity_BookDetails;
import zkagazapahtnajusz.paperproject.com.paperproject.Model.BookItems;
import zkagazapahtnajusz.paperproject.com.paperproject.R;
import zkagazapahtnajusz.paperproject.com.paperproject.Utilities.FirebaseUtils;
import zkagazapahtnajusz.paperproject.com.paperproject.Utilities.NetworkManagerUtils;
import zkagazapahtnajusz.paperproject.com.paperproject.Utilities.Utils;
import zkagazapahtnajusz.paperproject.com.paperproject.Utilities.WordsCapitalizer;

import static zkagazapahtnajusz.paperproject.com.paperproject.Activity_Home.GRID_ITEM2;
import static zkagazapahtnajusz.paperproject.com.paperproject.Activity_Home.LIST_ITEM2;
import static zkagazapahtnajusz.paperproject.com.paperproject.Activity_Home.isSwitchView2;

public class Fragment_Store_New extends Fragment implements SwipeRefreshLayout.OnRefreshListener{
    String TAG = "FRAGMENT STORE";

    RecyclerView myRecyclerView;
    RecyclerStore recyclerAdapter;
    List<BookItems> bookItems = new ArrayList<>();
    SwipeRefreshLayout swipeRefreshLayout;
    HorizontalScrollMenuView horizontalScrollMenuView;
    ListenerRegistration listenerRegistration_firstPage, listenerRegistration_loadMore;

    enum FilterMode { RECENT, TOPLIKE, TOPVIEWED, TOPREVIEWED, GENRE, AUTHOR, ANY}
    FilterMode filterMode;
    View progressBarWrapper;
    View NoDataFound;

    int queryLimit;
    private LinearLayoutManager layoutManager;
    private int pastVisiblesItems, visibleItemCount, totalItemCount;
    private boolean loading = true;
    private String OptionSelectionString = null;

    private Set<String> tempAuthor = new HashSet<>();
    private ArrayList<String> AuthorList = new ArrayList<>();

    private Set<String> genreSet = new HashSet<>();

    private ArrayList<String> PriceList = new ArrayList<>();
    private Boolean isFirstPageFirstLoad = false;
    private DocumentSnapshot lastVisible;
    private boolean genreArrayFilter = false;

    ListenerRegistration GenreAuthorListener;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_store,container,false);

        Utils.DEVICE_CHECK(getActivity());//DEVICE CHECK
        queryLimit = Utils.CURRENT_GRID_COUNT;
        progressBarWrapper = view.findViewById(R.id.ProgressWrapper);
        NoDataFound = view.findViewById(R.id.NoDataFoundView);
        swipeRefreshLayout = view.findViewById(R.id.swipeRefresh); swipeRefreshLayout.setOnRefreshListener(this);

        myRecyclerView = view.findViewById(R.id.recyclerView_Store);

        horizontalScrollMenuView = view.findViewById(R.id.horizontalScrollMenuView);
        horizontalScrollMenuView.addItem("New Books", R.drawable.icon_recent); //0
        horizontalScrollMenuView.addItem("Most Like", R.drawable.icon_likes_new);//1
        horizontalScrollMenuView.addItem("Most Read", R.drawable.library_icon);//2
        horizontalScrollMenuView.addItem("Most Reviewed", R.drawable.icon_comment_new);//3
        horizontalScrollMenuView.addItem("Genre", R.drawable.icon_genre);//4
        horizontalScrollMenuView.addItem("Author", R.drawable.icon_author);//5
        //horizontalScrollMenuView.addItem("Price", R.drawable.icon_barcode);//6
        horizontalScrollMenuView.showItems();
        horizontalScrollMenuView.setItemSelected(0);

        filterMode = FilterMode.RECENT;

        //region H Menu Click
        horizontalScrollMenuView.setOnHSMenuClickListener(new HorizontalScrollMenuView.OnHSMenuClickListener() {
            @Override
            public void onHSMClick(MenuItem menuItem, final int position) {
                if(position <= 3) {
                    if (filterMode.ordinal() != position) {
                        horizontalScrollMenuView.setItemSelected(position);
                        filterMode = FilterMode.values()[position];
                        onRefresh();
                    }
                }
                else if(position == 4){//Genre
                    filterMode = FilterMode.values()[position];

                    final String[] GenreList = genreSet.toArray(new String[genreSet.size()]);
                    Arrays.sort(GenreList); //Sort
                    ArrayAdapter<String> dataAdapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_list_item_1, GenreList);
                    //Open Dialog
                    AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                    builder.setTitle("Genre List");
                    builder.setAdapter(dataAdapter, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            horizontalScrollMenuView.setItemSelected(position);
                            OptionSelectionString =  GenreList[which];
                            onRefresh();
                        }
                    });
                    builder.setPositiveButton("Cancel", null);
                    AlertDialog dialog = builder.create();
                    dialog.show();
                }
                else if(position == 5){//Author
                    filterMode = FilterMode.values()[position];
                    Collections.sort(AuthorList);//Sort

                    ArrayAdapter<String> dataAdapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_list_item_1, AuthorList);

                    //open dialog
                    AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                    builder.setTitle("Author List");
                    builder.setAdapter(dataAdapter, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            horizontalScrollMenuView.setItemSelected(position);
                            OptionSelectionString =  AuthorList.get(which);
                            onRefresh();
                        }
                    });
                    builder.setPositiveButton("Cancel", null);
                    AlertDialog dialog = builder.create();
                    dialog.show();
                }
                else if(position == 6){//Free Pre Any
                    filterMode = FilterMode.values()[position];
                    ArrayAdapter<String> dataAdapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_list_item_1, PriceList);

                    //open dialog
                    AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                    builder.setTitle("Price List");
                    builder.setAdapter(dataAdapter, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            horizontalScrollMenuView.setItemSelected(position);
                            OptionSelectionString =  PriceList.get(which);
                            onRefresh();
                        }
                    });
                    builder.setPositiveButton("Cancel", null);
                    AlertDialog dialog = builder.create();
                    dialog.show();
                }
            }
        });
        //endregion

        recyclerAdapter = new RecyclerStore(this.getContext(), bookItems);
        layoutManager = new LinearLayoutManager(getContext());

        myRecyclerView.setLayoutManager(layoutManager);
        myRecyclerView.setAdapter(recyclerAdapter);

        myRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
            }

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                if(dy > 0) //check for scroll down
                {
                    visibleItemCount = layoutManager.getChildCount();
                    totalItemCount = layoutManager.getItemCount();
                    pastVisiblesItems = layoutManager.findFirstVisibleItemPosition();

                    //Log.e(TAG, "VIC:" + visibleItemCount + " -->:"+(visibleItemCount + pastVisiblesItems) + ">=" + totalItemCount + " PVI:" + pastVisiblesItems + "-" + dx + ":" +dx);

                    if (loading && ((visibleItemCount + pastVisiblesItems) >= totalItemCount))
                    {
                        loading = false;
                        LoadMore();
                    }
                }
            }
        });
        LoadGenreAndAuthorLists();
        FirstPage(false);
        return view;
    }

    public void changeRecyclerToGrid(){
        int spanCount = Utils.CURRENT_GRID_COUNT/2;
        layoutManager = new GridLayoutManager(getActivity(),spanCount);
        myRecyclerView.setLayoutManager(layoutManager);
    }

    public void changerRecyclerToSingle(){
        layoutManager = new LinearLayoutManager(getActivity());
        myRecyclerView.setLayoutManager(layoutManager);
    }

    public boolean getItemViewTypeFromRecyclerView(){
        try {
            return recyclerAdapter.toggleItemViewType();
        }
        catch (Exception ex){
            return true;
        }
    }

    private void LoadGenreAndAuthorLists(){
        try{
            genreSet.clear();

            tempAuthor.clear();
            AuthorList.clear();

            String Root = getString(R.string.FIRESTORE_ROOT_BOOKS);
            GenreAuthorListener = FirebaseFirestore.getInstance().collection(Root).addSnapshotListener(new EventListener<QuerySnapshot>() {
                @Override
                public void onEvent(QuerySnapshot queryDocumentSnapshots, FirebaseFirestoreException e) {
                    try {
                        for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                            //Author
                            String authorStr = doc.getString(getString(R.string.F_BOOKS_AUTHOR));
                            authorStr = WordsCapitalizer.capitalizeEveryWord(authorStr);
                            tempAuthor.add(authorStr);

                            //Genre Lists
                            ArrayList data = (ArrayList)doc.get(getString(R.string.F_BOOKS_GENRE));
                            for(Object obj: data){
                                genreSet.add((String)obj);
                            }
                        }
                        AuthorList = new ArrayList<>(tempAuthor);
                        GenreAuthorListener.remove();
                    }
                    catch (Exception ex){
                        Log.e(TAG, ex.toString());
                    }
                }
            });

        }
        catch (Exception ex){
            Log.e(TAG, ex.toString());
        }
    }

    private void FirstPage(final boolean isShowLoading){
        try{
            NetworkManagerUtils.LoadServerData(getContext());

            PriceList.add("Any");
            PriceList.add("Free");
            PriceList.add("Paid");

            if(isShowLoading){Utils.SetProgressDialogIndeterminateWithOutCancel(getActivity(), "loading..");}

            String temp_root = getString(R.string.FIRESTORE_ROOT_BOOKS);
            CollectionReference temp_collectionrefrence = FirebaseFirestore.getInstance().collection(temp_root);
            Query temp_query = null;

            //region Filter Settings
            if(filterMode == FilterMode.RECENT){
                temp_query = temp_collectionrefrence.whereEqualTo(getString(R.string.F_BOOKS_STATUS), BookItems.Status.Public.toString()).orderBy(getString(R.string.F_BOOKS_DATE), Query.Direction.DESCENDING).limit(queryLimit);
            }
            //else if(filterMode == FilterMode.TOPDOWNLOAD){
            //    temp_query = temp_collectionrefrence.whereEqualTo(getString(R.string.F_BOOKS_STATUS), BookItems.Status.Public.toString()).orderBy(getString(R.string.F_BOOKS_COUNTERDOWNLOAD), Query.Direction.DESCENDING).limit(queryLimit);
            //}
            else if(filterMode == FilterMode.TOPLIKE){
                temp_query = temp_collectionrefrence.whereEqualTo(getString(R.string.F_BOOKS_STATUS), BookItems.Status.Public.toString()).orderBy(getString(R.string.F_BOOKS_COUNTERLIKE), Query.Direction.DESCENDING).limit(queryLimit);
            }
            else if(filterMode == FilterMode.TOPVIEWED){
                temp_query = temp_collectionrefrence.whereEqualTo(getString(R.string.F_BOOKS_STATUS), BookItems.Status.Public.toString()).orderBy(getString(R.string.F_BOOKS_COUNTERVIEW), Query.Direction.DESCENDING).limit(queryLimit);
            }
            else if(filterMode == FilterMode.TOPREVIEWED){
                temp_query = temp_collectionrefrence.whereEqualTo(getString(R.string.F_BOOKS_STATUS), BookItems.Status.Public.toString()).orderBy(getString(R.string.F_BOOKS_COUNTERCOMMENT), Query.Direction.DESCENDING).limit(queryLimit);
            }
            else if(filterMode == FilterMode.GENRE){
                genreArrayFilter = true;
                temp_query = temp_collectionrefrence.whereEqualTo(getString(R.string.F_BOOKS_STATUS), BookItems.Status.Public.toString()).orderBy(getString(R.string.F_BOOKS_TITLE), Query.Direction.ASCENDING).limit(queryLimit);
            }
            else if(filterMode == FilterMode.AUTHOR){
                temp_query = temp_collectionrefrence.whereEqualTo(getString(R.string.F_BOOKS_STATUS), BookItems.Status.Public.toString()).whereEqualTo(getString(R.string.F_BOOKS_AUTHOR),OptionSelectionString).orderBy(getString(R.string.F_BOOKS_TITLE), Query.Direction.ASCENDING).limit(queryLimit);
            }
            else if(filterMode == FilterMode.ANY){
                if(OptionSelectionString.equals("Free")){
                    temp_query = temp_collectionrefrence.whereEqualTo(getString(R.string.F_BOOKS_STATUS), BookItems.Status.Public.toString()).whereEqualTo(getString(R.string.F_BOOKS_BOOKFLAG),OptionSelectionString).orderBy(getString(R.string.F_BOOKS_TITLE), Query.Direction.ASCENDING).limit(queryLimit);
                }
                else if(OptionSelectionString.equals("Paid")){
                    String bufferStr = "Store";
                    temp_query = temp_collectionrefrence.whereEqualTo(getString(R.string.F_BOOKS_STATUS), BookItems.Status.Public.toString()).whereEqualTo(getString(R.string.F_BOOKS_BOOKFLAG), bufferStr).orderBy(getString(R.string.F_BOOKS_PRICE), Query.Direction.ASCENDING).limit(queryLimit);                    //temp_query = temp_collectionrefrence.whereEqualTo(getString(R.string.F_BOOKS_STATUS), BookItems.Status.Public.toString()).whereEqualTo(getString(R.string.F_BOOKS_BOOKFLAG),OptionSelectionString).orderBy(getString(R.string.F_BOOKS_PRICE), Query.Direction.ASCENDING).limit(queryLimit);
                }
                else{
                    temp_query = temp_collectionrefrence.whereEqualTo(getString(R.string.F_BOOKS_STATUS), BookItems.Status.Public.toString()).orderBy(getString(R.string.F_BOOKS_TITLE), Query.Direction.ASCENDING).limit(queryLimit);
                }
            }
            //endregion

            //region temp query
            if(temp_query != null){
                listenerRegistration_firstPage = FirebaseUtils.FirestoreGetAllData(temp_query, getActivity(), new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(QuerySnapshot queryDocumentSnapshots, FirebaseFirestoreException e) {
                        if(queryDocumentSnapshots != null && !queryDocumentSnapshots.isEmpty()){
                            if(!isFirstPageFirstLoad){
                                lastVisible = queryDocumentSnapshots.getDocuments().get(queryDocumentSnapshots.size() -1);
                            }
                            for(DocumentChange doc : queryDocumentSnapshots.getDocumentChanges()){
                                //region ADDED
                                if(doc.getType() == DocumentChange.Type.ADDED){
                                    NoDataFound.setVisibility(View.GONE);
                                    BookItems bookItem = new BookItems(doc.getDocument().getString(getString(R.string.F_BOOKS_PUBLICKEY)),doc.getDocument().getString(getString(R.string.F_BOOKS_IMAGEURL)),doc.getDocument().getString(getString(R.string.F_BOOKS_BOOKID)),doc.getDocument().getString(getString(R.string.F_BOOKS_TITLE)),doc.getDocument().getString(getString(R.string.F_BOOKS_AUTHOR)).trim(),doc.getDocument().getString(getString(R.string.F_BOOKS_DESCRIPTION)),Double.valueOf(doc.getDocument().get(getString(R.string.F_BOOKS_PRICE)).toString()),doc.getDocument().getString(getString(R.string.F_BOOKS_CURRENCYSTR)),(ArrayList) doc.getDocument().get(getString(R.string.F_BOOKS_GENRE)),BookItems.BookFlag.valueOf(doc.getDocument().getString(getString(R.string.F_BOOKS_BOOKFLAG))),BookItems.FileType.EPUB,BookItems.StorageType.CLOUD);
                                    boolean isNeedToAdd = true;
                                    if(genreArrayFilter){
                                        boolean isContainData = false;for(Object obj : bookItem.Genre){if(obj.toString().equals(OptionSelectionString)){ isContainData = true;break;}}if(!isContainData) {isNeedToAdd = false;}
                                    }
                                    if(isNeedToAdd) {
                                        if (!isFirstPageFirstLoad) {
                                            //Add To List from beginning
                                            bookItems.add(bookItem);
                                            recyclerAdapter.notifyItemInserted(bookItems.size() - 1);
                                        } else {
                                            //Add To Second index i.e. => 1
                                            bookItems.add(0, bookItem);
                                            recyclerAdapter.notifyItemInserted(1);
                                        }
                                    }

                                }
                                //endregion
                            }
                            if(bookItems.size() < queryLimit){LoadMore();}
                            isFirstPageFirstLoad = true;
                        }
                        else{
                            if(e != null){ Log.e(TAG, e.toString());}
                            bookItems.clear();
                            recyclerAdapter.notifyDataSetChanged();
                            isFirstPageFirstLoad = false;
                            NoDataFound.setVisibility(View.VISIBLE);
                            Utils.UnSetProgressDialogIndeterminate();
                            Log.e(TAG,"Data Not Found");
                        }
                        swipeRefreshLayout.setRefreshing(false);
                        if(isShowLoading){Utils.UnSetProgressDialogIndeterminate();}
                    }
                });
            }
            //endregion
        }
        catch (Exception ex){
            swipeRefreshLayout.setRefreshing(false);
            if(isShowLoading){Utils.UnSetProgressDialogIndeterminate();}
            Log.e(TAG, ex.toString());
        }
    }

    private void LoadMore(){
        try{
            progressBarWrapper.setVisibility(View.VISIBLE);
            String temp_root = getString(R.string.FIRESTORE_ROOT_BOOKS);
            CollectionReference temp_collectionrefrence = FirebaseFirestore.getInstance().collection(temp_root);
            Query temp_query = null;

            //region Filter Settings
            if(filterMode == FilterMode.RECENT){
                temp_query = temp_collectionrefrence.whereEqualTo(getString(R.string.F_BOOKS_STATUS), BookItems.Status.Public.toString()).orderBy(getString(R.string.F_BOOKS_DATE), Query.Direction.DESCENDING).startAfter(lastVisible).limit(queryLimit);
            }
            //else if(filterMode == FilterMode.TOPDOWNLOAD){
            // temp_query = temp_collectionrefrence.whereEqualTo(getString(R.string.F_BOOKS_STATUS), BookItems.Status.Public.toString()).orderBy(getString(R.string.F_BOOKS_COUNTERDOWNLOAD), Query.Direction.DESCENDING).startAfter(lastVisible).limit(queryLimit);
            //}
            else if(filterMode == FilterMode.TOPLIKE){
                temp_query = temp_collectionrefrence.whereEqualTo(getString(R.string.F_BOOKS_STATUS), BookItems.Status.Public.toString()).orderBy(getString(R.string.F_BOOKS_COUNTERLIKE), Query.Direction.DESCENDING).startAfter(lastVisible).limit(queryLimit);
            }
            else if(filterMode == FilterMode.TOPVIEWED){
                temp_query = temp_collectionrefrence.whereEqualTo(getString(R.string.F_BOOKS_STATUS), BookItems.Status.Public.toString()).orderBy(getString(R.string.F_BOOKS_COUNTERVIEW), Query.Direction.DESCENDING).startAfter(lastVisible).limit(queryLimit);
            }
            else if(filterMode == FilterMode.TOPREVIEWED){
                temp_query = temp_collectionrefrence.whereEqualTo(getString(R.string.F_BOOKS_STATUS), BookItems.Status.Public.toString()).orderBy(getString(R.string.F_BOOKS_COUNTERCOMMENT), Query.Direction.DESCENDING).startAfter(lastVisible).limit(queryLimit);
            }
            else if(filterMode == FilterMode.GENRE){
                temp_query = temp_collectionrefrence.whereEqualTo(getString(R.string.F_BOOKS_STATUS), BookItems.Status.Public.toString()).orderBy(getString(R.string.F_BOOKS_TITLE), Query.Direction.ASCENDING).startAfter(lastVisible).limit(queryLimit);
            }
            else if(filterMode == FilterMode.AUTHOR){
                temp_query = temp_collectionrefrence.whereEqualTo(getString(R.string.F_BOOKS_STATUS), BookItems.Status.Public.toString()).whereEqualTo(getString(R.string.F_BOOKS_AUTHOR),OptionSelectionString).orderBy(getString(R.string.F_BOOKS_TITLE), Query.Direction.ASCENDING).startAfter(lastVisible).limit(queryLimit);
            }
            else if(filterMode == FilterMode.ANY){
                if(OptionSelectionString.equals("Free")){
                    temp_query = temp_collectionrefrence.whereEqualTo(getString(R.string.F_BOOKS_STATUS), BookItems.Status.Public.toString()).whereEqualTo(getString(R.string.F_BOOKS_BOOKFLAG),OptionSelectionString).orderBy(getString(R.string.F_BOOKS_TITLE), Query.Direction.ASCENDING).startAfter(lastVisible).limit(queryLimit);
                }
                else if(OptionSelectionString.equals("Paid")){
                    String bufferStr = "Store";
                    temp_query = temp_collectionrefrence.whereEqualTo(getString(R.string.F_BOOKS_STATUS), BookItems.Status.Public.toString()).whereEqualTo(getString(R.string.F_BOOKS_BOOKFLAG), bufferStr).orderBy(getString(R.string.F_BOOKS_PRICE), Query.Direction.ASCENDING).startAfter(lastVisible).limit(queryLimit);
                }
                else{
                    temp_query = temp_collectionrefrence.whereEqualTo(getString(R.string.F_BOOKS_STATUS), BookItems.Status.Public.toString()).orderBy(getString(R.string.F_BOOKS_TITLE), Query.Direction.ASCENDING).startAfter(lastVisible).limit(queryLimit);
                }
            }
            //endregion

            //region temp query
            if(temp_query != null){
                listenerRegistration_loadMore = FirebaseUtils.FirestoreGetAllData(temp_query, getActivity(), new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(QuerySnapshot queryDocumentSnapshots, FirebaseFirestoreException e) {
                        if(queryDocumentSnapshots != null && !queryDocumentSnapshots.isEmpty()){
                            lastVisible = queryDocumentSnapshots.getDocuments().get(queryDocumentSnapshots.size() -1);
                            for(DocumentChange doc : queryDocumentSnapshots.getDocumentChanges()){
                                //region ADDED
                                if(doc.getType() == DocumentChange.Type.ADDED){
                                    BookItems bookItem = new BookItems(doc.getDocument().getString(getString(R.string.F_BOOKS_PUBLICKEY)),doc.getDocument().getString(getString(R.string.F_BOOKS_IMAGEURL)),doc.getDocument().getString(getString(R.string.F_BOOKS_BOOKID)),doc.getDocument().getString(getString(R.string.F_BOOKS_TITLE)),doc.getDocument().getString(getString(R.string.F_BOOKS_AUTHOR)).trim(),doc.getDocument().getString(getString(R.string.F_BOOKS_DESCRIPTION)),Double.valueOf(doc.getDocument().get(getString(R.string.F_BOOKS_PRICE)).toString()),doc.getDocument().getString(getString(R.string.F_BOOKS_CURRENCYSTR)),(ArrayList) doc.getDocument().get(getString(R.string.F_BOOKS_GENRE)),BookItems.BookFlag.valueOf(doc.getDocument().getString(getString(R.string.F_BOOKS_BOOKFLAG))),BookItems.FileType.EPUB,BookItems.StorageType.CLOUD);
                                    boolean isNeedToAdd = true; if(genreArrayFilter){if(!bookItem.Genre.contains(OptionSelectionString)){isNeedToAdd = false;}}
                                    if(isNeedToAdd) {
                                        bookItems.add(bookItem);
                                        recyclerAdapter.notifyItemInserted(bookItems.size() - 1);
                                    }
                                }
                                //endregion
                            }
                        }
                        else{
                            if(e != null){ Log.e(TAG, e.toString());}
                            Log.e(TAG,"NO DATA FOUND -  MODE 1");
                        }

                        if(bookItems.size() <=0){
                            NoDataFound.setVisibility(View.VISIBLE);
                        }

                        listenerRegistration_loadMore.remove();
                        listenerRegistration_loadMore = null;
                        progressBarWrapper.setVisibility(View.GONE);
                        loading = true;
                    }
                });
            }
            //endregion
        }
        catch (Exception ex){
            progressBarWrapper.setVisibility(View.INVISIBLE);
            Log.e(TAG, ex.toString());
        }
    }

    @Override
    public void onRefresh() {
        if(listenerRegistration_firstPage != null){ listenerRegistration_firstPage.remove(); listenerRegistration_firstPage = null;}
        if(listenerRegistration_loadMore != null){ listenerRegistration_loadMore.remove(); listenerRegistration_loadMore = null;}
        loading = true;
        genreArrayFilter = false;
        isFirstPageFirstLoad = false;
        bookItems.clear();
        PriceList.clear();
        recyclerAdapter.notifyDataSetChanged();
        FirstPage(true);
    }
}

class RecyclerStore extends RecyclerView.Adapter<RecyclerStore.holderStore>{
    private String TAG = "RECYCLER INNER";
    private Context mContext;
    private List<BookItems> insideTags;

    RecyclerStore(Context mContext, List<BookItems> insideTags) {
        this.mContext = mContext;
        this.insideTags = insideTags;
    }

    @NonNull
    @Override
    public holderStore onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view;
        if(viewType == LIST_ITEM2){
            view = LayoutInflater.from(mContext).inflate(R.layout.recycler_item_inner_list_store, parent, false);
        }else{
            view = LayoutInflater.from(mContext).inflate(R.layout.recycler_item_inner_grid_store, parent, false);
        }
        return new holderStore(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final holderStore holder, int position) {
        //region SET IMAGE
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
        //endregion

        //region Set Name
        if (holder.tName != null) {
            try{
                holder.tName.setText(insideTags.get(position).Name);
            }
            catch (Exception ex){
                Log.e(TAG, ex.toString());
            }
        }
        //endregion

        //region Adding TAG
        /*
        if(holder.mTagGroup != null) {
            try{
                String UID = FirebaseUtils.firebaseAuth.getCurrentUser().getUid();
                String Root = mContext.getString(R.string.FIRESTORE_ROOT_USERDETAILS) + "/" + UID + "/" + mContext.getString(R.string.FIRESTORE_ROOT_LIBRARY) + "/" + insideTags.get(position).BOOKID;
                FirebaseUtils.FirestoreGetData(Root, new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        try {
                            if (documentSnapshot != null && documentSnapshot.exists()) {
                                insideTags.get(holder.getAdapterPosition()).bookFlag = BookItems.BookFlag.Purchased;
                                UpdateBookFlag(holder.getAdapterPosition(), holder);
                            } else {
                                UpdateBookFlag(holder.getAdapterPosition(), holder);
                            }
                        }
                        catch (Exception ex){
                            Log.e(TAG, ex.toString());
                        }
                    }
                }, new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        try{
                            UpdateBookFlag(holder.getAdapterPosition(), holder);
                        }
                        catch (Exception ex){
                            Log.e(TAG, ex.toString());
                        }
                    }
                });
            }
            catch (Exception ex){
                Log.e(TAG,ex.toString());
            }
        }
        */
        //endregion

        //region Adding Author
        if(holder.author != null) {
            String authordata = "by <font color='#333333'>" + insideTags.get(position).Author + "</font>";
            holder.author.setText(Html.fromHtml(authordata));
        }
        //endregion

        //region LIKE
        if (holder.tLikes != null) {
            try{
                String BOOKID = insideTags.get(position).BOOKID;
                String Root = mContext.getString(R.string.FIRESTORE_ROOT_BOOKS) + "/" + BOOKID + "/" + mContext.getString(R.string.FIRESTORE_ROOT_BOOKS_LIKES);
                CollectionReference collectionReference = FirebaseFirestore.getInstance().collection(Root);
                FirebaseUtils.FirestoreGetAllData(collectionReference, (Activity) mContext, new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(QuerySnapshot queryDocumentSnapshots, FirebaseFirestoreException e) {
                        try{
                            int tLikesValue = 0;
                            if(!queryDocumentSnapshots.isEmpty()){
                                tLikesValue = queryDocumentSnapshots.size();
                            }
                            holder.tLikes.setText(Utils.Numberformat(tLikesValue));
                        }
                        catch (Exception ex){
                            Log.e(TAG, ex.toString());
                        }
                    }
                });
            }
            catch (Exception ex){
                Log.e(TAG, ex.toString());
            }
        }
        //endregion

        //region VIEWS & DOWNLOAD
        if (holder.tViews != null) {
            try{
                String BOOKID = insideTags.get(position).BOOKID;
                String Root = mContext.getString(R.string.FIRESTORE_ROOT_BOOKS) + "/" + BOOKID;
                FirebaseUtils.FirestoreGetData(Root, new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                    if(documentSnapshot.exists()){
                        String TotalViews = String.valueOf(documentSnapshot.get(mContext.getString(R.string.F_BOOKS_COUNTERVIEW)));
                        TotalViews = Utils.Numberformat(Long.valueOf(TotalViews));
                        holder.tViews.setText(TotalViews);

                        String TotalDownload = String.valueOf(documentSnapshot.get(mContext.getString(R.string.F_BOOKS_COUNTERDOWNLOAD)));
                        TotalDownload = Utils.Numberformat(Long.valueOf(TotalDownload));
                        holder.tDownload.setText(TotalDownload);

                    }
                    }
                }, null);
            }
            catch (Exception ex){
                Log.e(TAG, ex.toString());
            }
        }
        //endregion

        //region COMMENTS
        if (holder.tComments != null) {
            try{
                String BOOKID = insideTags.get(position).BOOKID;
                String Root = mContext.getString(R.string.FIRESTORE_ROOT_BOOKS) + "/" + BOOKID + "/" + mContext.getString(R.string.FIRESTORE_ROOT_BOOKS_COMMENTS);
                CollectionReference collectionReference = FirebaseFirestore.getInstance().collection(Root);
                FirebaseUtils.FirestoreGetAllData(collectionReference, (Activity)mContext, new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(QuerySnapshot queryDocumentSnapshots, FirebaseFirestoreException e) {
                        try {
                            if (!queryDocumentSnapshots.isEmpty()) {
                                String TotalViews = String.valueOf(queryDocumentSnapshots.getDocumentChanges().size());
                                TotalViews = Utils.Numberformat(Long.valueOf(TotalViews));
                                holder.tComments.setText(TotalViews);
                            }
                        }
                        catch (Exception ex){
                            Log.e(TAG,ex.toString());
                        }
                    }
                });
            }
            catch (Exception ex){
                Log.e(TAG, ex.toString());
            }
        }
        //endregion

        //region Description
        if (holder.tDescription != null) {
            try {
                holder.tDescription.setText(insideTags.get(position).Description);
            }
            catch (Exception ex){
                Log.e(TAG,ex.toString());
            }
        }
        //endregion

        //region Set Click
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    //Fragment_Store_New.tagGroup_static_RI = holder.mTagGroup;
                    Intent intent = new Intent(mContext, Activity_BookDetails.class);
                    intent.putExtra("BookID", insideTags.get(holder.getAdapterPosition()).BOOKID);
                    mContext.startActivity(intent);
                }
                catch (Exception ex){
                    Log.e(TAG,ex.toString());
                }
            }
        });
        //endregion
    }

    //region Update Book Flag ERROR
    /*
    private void UpdateBookFlag(int position, holderStore holder){
        try {
            String Author = insideTags.get(position).Author;
            //String tagText = "";
            if (insideTags.get(position).bookFlag == BookItems.BookFlag.Free) {
                //tagText = "Free";
                String[] tagValue = {Author};
                holder.mTagGroup.setTags(tagValue);
            } else if (insideTags.get(position).bookFlag == BookItems.BookFlag.Store) {
                //tagText = "Buy " + insideTags.get(position).CurrencyStr + " " + insideTags.get(position).Price;
                String[] tagValue = {Author};
                holder.mTagGroup.setTags(tagValue);
            } else if (insideTags.get(position).bookFlag == BookItems.BookFlag.Purchased) {
                //tagText = "Purchased";
                String[] tagValue = {Author};
                holder.mTagGroup.setTags(tagValue);
            } else if (insideTags.get(position).bookFlag == BookItems.BookFlag.Nothing) {
                //tagText = "Error";
                String[] tagValue = {Author};
                holder.mTagGroup.setTags(tagValue);
            }
        }
        catch (ArrayIndexOutOfBoundsException ex){
            Log.e(TAG, ex.getMessage());
        }
    }
    */
    //endregion

    @Override
    public int getItemCount() {
        return insideTags.size();
    }

    class holderStore extends RecyclerView.ViewHolder{
        ImageView tImage;
        TextView tName, tViews,tDownload, tComments, tDescription, tLikes, author;
        holderStore(View view){
            super(view);
            tImage = view.findViewById(R.id.recyclerItemImage);
            tName = view.findViewById(R.id.recyclerItemName);
            tViews = view.findViewById(R.id.recyclerItemViews);
            tDownload = view.findViewById(R.id.recyclerItemDownload);
            tComments = view.findViewById(R.id.recyclerItemComments);
            tDescription = view.findViewById(R.id.recyclerItemDescription);
            tLikes = view.findViewById(R.id.recyclerItemLikes);
            author = view.findViewById(R.id.author);
        }
    }

    @Override
    public int getItemViewType(int position) {
        if (isSwitchView2) {
            return LIST_ITEM2;
        } else {
            return GRID_ITEM2;
        }
    }

    boolean toggleItemViewType() {
        isSwitchView2 = !isSwitchView2;
        return isSwitchView2;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }
}
