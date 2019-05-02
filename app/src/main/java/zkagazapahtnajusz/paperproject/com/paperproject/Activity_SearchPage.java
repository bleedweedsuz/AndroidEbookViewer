package zkagazapahtnajusz.paperproject.com.paperproject;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import zkagazapahtnajusz.paperproject.com.paperproject.Model.BookItems;
import zkagazapahtnajusz.paperproject.com.paperproject.Model.SearchedItems;
import zkagazapahtnajusz.paperproject.com.paperproject.Utilities.FirebaseUtils;
import zkagazapahtnajusz.paperproject.com.paperproject.Utilities.Utils;

/**
 * Created by Zw4R-TSTUD10 2018/4/1 [2018, April 1]
 */

public class Activity_SearchPage extends AppCompatActivity implements SwipeRefreshLayout.OnRefreshListener{
    private static final String TAG = "ACTIVITY SEARCH PAGE";

    public enum SearchType {LIBRARY, FOLLOWER, FOLLOWING, SEARCHFRIENDS};
    private SearchType searchType;

    int queryLimit = 10;
    RecyclerView mRecyclerView;
    Toolbar toolbar;
    SearchView searchView;
    String tag;
    AdapterRecycler user_adapter;
    Book_Library_Item_AdapterRecycler book_library_item_adapterRecycler;

    List<SearchedItems> userLists  = new ArrayList<>();
    List<SearchedItems> user_bufferList  = new ArrayList<>();

    List<BookItems> buffer_bookItemsList = new ArrayList<>();
    List<BookItems> bookItemsList = new ArrayList<>();

    //Library Data
    int libraryTotalBooks = 0;
    int libraryBookAddedInBuffer = 0;

    FirebaseFirestore firebaseFirestore;
    SwipeRefreshLayout swipeRefreshLayout;
    LinearLayoutManager layoutManager;
    View NoDataFound;
    TextView titleDataNotFound;
    int totalPos = 0;
    ListenerRegistration listenerRegistration;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_search_page);

        NoDataFound = findViewById(R.id.NoDataFoundView);
        titleDataNotFound = findViewById(R.id.titleDataNotFound);

        swipeRefreshLayout = findViewById(R.id.swipeRefresh);
        swipeRefreshLayout.setOnRefreshListener(this);

        toolbar = findViewById(R.id.toolbarSearchPage);
        searchView = findViewById(R.id.searchView);
        mRecyclerView = findViewById(R.id.recyclerSearchPage);
        setSupportActionBar(toolbar);
        if(getSupportActionBar() != null){
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }

        Bundle bundle = getIntent().getExtras();
        if(bundle != null){
            searchType = SearchType.valueOf(bundle.getString("SearchType"));
            tag = bundle.getString("tag");
            searchView.setQueryHint(bundle.getString("queryhint"));
            if(bundle.getBoolean("isKeyboardFocus")){
                searchView.setIconified(true);
                searchView.onActionViewExpanded();
                searchView.clearFocus();
            }
        }

        //Setting RecyclerView
        layoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(layoutManager);

        //Load All Data
        LoadAllData();

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                SearchQuery(query);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                SearchQuery(newText);
                return true;
            }
        });
    }

    private void LoadAllData(){
        try {
            Utils.SetProgressDialogIndeterminate(this, "Loading");

            //Library
            bookItemsList.clear();
            buffer_bookItemsList.clear();
            libraryTotalBooks = 0;
            libraryBookAddedInBuffer = 0;

            //Users
            userLists.clear();
            user_bufferList.clear();

            NoDataFound.setVisibility(View.VISIBLE);
            ((ImageView)NoDataFound.findViewById(R.id.icon)).setImageResource(R.drawable.search_icon);
            ((TextView)NoDataFound.findViewById(R.id.title)).setText("Search Your Data");
            ((TextView)NoDataFound.findViewById(R.id.titleDataNotFound)).setText("Use suffix to search data in search bar");

            firebaseFirestore = FirebaseFirestore.getInstance();
            if (searchType == SearchType.LIBRARY) {
                book_library_item_adapterRecycler = new Book_Library_Item_AdapterRecycler(this, bookItemsList);
                mRecyclerView.setAdapter(book_library_item_adapterRecycler);

                //region LIBRARY
                String UID = FirebaseUtils.firebaseAuth.getCurrentUser().getUid();
                String Root = getString(R.string.FIRESTORE_ROOT_USERDETAILS) + "/" + UID + "/" + getString(R.string.FIRESTORE_ROOT_LIBRARY);
                CollectionReference collection = FirebaseFirestore.getInstance().collection(Root);
                Query query = collection.whereEqualTo(getString(R.string.F_USERDETAILS_LIBRARY_STATE), Activity_BookDetails.LibraryState.ADDED.toString());
                listenerRegistration = FirebaseUtils.FirestoreGetAllData(query, this, new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(QuerySnapshot queryDocumentSnapshots, FirebaseFirestoreException e) {
                        if(!queryDocumentSnapshots.isEmpty()){
                            libraryTotalBooks = queryDocumentSnapshots.size();
                            for(DocumentChange doc : queryDocumentSnapshots.getDocumentChanges()){
                                if(doc.getType() == DocumentChange.Type.ADDED){
                                    String BOOKID = doc.getDocument().getString(getString(R.string.F_USERDETAILS_LIBRARY_BOOKID));
                                    String _Root = getString(R.string.FIRESTORE_ROOT_BOOKS) + "/" + BOOKID;

                                    FirebaseUtils.FirestoreGetData(_Root, new OnSuccessListener<DocumentSnapshot>() {
                                        @Override
                                        public void onSuccess(DocumentSnapshot documentSnapshot) {
                                            if(documentSnapshot.exists()){
                                                BookItems bookItems = new BookItems(
                                                        documentSnapshot.getString(getString(R.string.F_BOOKS_PUBLICKEY)),
                                                        documentSnapshot.getString(getString(R.string.F_BOOKS_IMAGEURL)),
                                                        documentSnapshot.getString(getString(R.string.F_BOOKS_BOOKID)),
                                                        documentSnapshot.getString(getString(R.string.F_BOOKS_TITLE)).trim(),
                                                        documentSnapshot.getString(getString(R.string.F_BOOKS_AUTHOR)),
                                                        documentSnapshot.getString(getString(R.string.F_BOOKS_DESCRIPTION)),
                                                        Double.valueOf(documentSnapshot.get(getString(R.string.F_BOOKS_PRICE)).toString()),
                                                        documentSnapshot.getString(getString(R.string.F_BOOKS_CURRENCYSTR)),
                                                        (ArrayList) documentSnapshot.get(getString(R.string.F_BOOKS_GENRE)),
                                                        BookItems.BookFlag.valueOf(documentSnapshot.getString(getString(R.string.F_BOOKS_BOOKFLAG))),
                                                        BookItems.FileType.EPUB,
                                                        BookItems.StorageType.CLOUD
                                                );
                                                buffer_bookItemsList.add(bookItems);
                                                libraryBookAddedInBuffer++;
                                                CollectLibraryBooks(libraryBookAddedInBuffer);
                                            }
                                        }
                                    }, null);
                                }
                            }
                        }
                        else{
                            Utils.UnSetProgressDialogIndeterminate();
                            NoDataFound.setVisibility(View.VISIBLE);
                            ((ImageView)NoDataFound.findViewById(R.id.icon)).setImageResource(R.drawable.exclamation_icon);
                            ((TextView)NoDataFound.findViewById(R.id.title)).setText("No Data Found");
                            ((TextView)NoDataFound.findViewById(R.id.titleDataNotFound)).setText("Data does not in library \"you can add it from store\"");
                        }
                    }
                });
                //endregion
            }
            else if(searchType == SearchType.SEARCHFRIENDS){
                //Adding list here
                user_adapter = new AdapterRecycler(Activity_SearchPage.this, userLists, searchType);
                mRecyclerView.setAdapter(user_adapter);

                //region SEARCH FRIENDS
                Query query = firebaseFirestore.collection(getString(R.string.FIRESTORE_ROOT_USERDETAILS)).orderBy(getString(R.string.F_USERDETAILS_USERFULLNAME), Query.Direction.ASCENDING);
                listenerRegistration = FirebaseUtils.FirestoreGetAllData(query, this, new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(QuerySnapshot queryDocumentSnapshots, FirebaseFirestoreException e) {
                        if(queryDocumentSnapshots != null && !queryDocumentSnapshots.isEmpty()){
                            for(DocumentChange doc : queryDocumentSnapshots.getDocumentChanges()){
                                if(doc.getType() == DocumentChange.Type.ADDED){
                                    String ID = ""; try{ ID = doc.getDocument().getString(getString(R.string.F_USERDETAILS_UID));} catch (NullPointerException ex){Log.e (TAG, ex.toString());}
                                    if(ID.equals(FirebaseUtils.firebaseAuth.getCurrentUser().getUid())){continue;}
                                    String FullName = ""; try{ FullName = doc.getDocument().getString(getString(R.string.F_USERDETAILS_USERFULLNAME)).trim();} catch (NullPointerException ex){Log.e (TAG, ex.toString());}
                                    String ImageLink = ""; try{ ImageLink = doc.getDocument().getString(getString(R.string.F_USERDETAILS_PROFILEURL));} catch (NullPointerException ex){Log.e (TAG, ex.toString());}
                                    SearchedItems bufferItems = new SearchedItems(ID, ImageLink, FullName, "View Details");
                                    user_bufferList.add(bufferItems);
                                }
                            }
                        }
                        //UnSet Progress Dialog
                        Utils.UnSetProgressDialogIndeterminate();
                        //SearchQuery("");
                    }
                });
                //endregion
            }
            else if(searchType == SearchType.FOLLOWER){
                //Adding list here
                user_adapter = new AdapterRecycler(Activity_SearchPage.this, userLists, searchType);
                mRecyclerView.setAdapter(user_adapter);

                //region FOLLOWER
                String UID = FirebaseUtils.firebaseAuth.getCurrentUser().getUid();
                String Root = getString(R.string.FIRESTORE_ROOT_USERDETAILS) + "/" + UID + "/" + getString(R.string.FIRESTORE_ROOT_FOLLOWER);
                CollectionReference collectionReference = firebaseFirestore.collection(Root);
                Query query = collectionReference.whereEqualTo(getString(R.string.F_USERDETAILS_FOLLOWER_STATE), Activity_Friend_Profile.FollowState.ACCEPT.toString());
                query.orderBy(getString(R.string.F_USERDETAILS_FOLLOWER_DATE), Query.Direction.DESCENDING);

                listenerRegistration = FirebaseUtils.FirestoreGetAllData(query, this, new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(QuerySnapshot queryDocumentSnapshots, FirebaseFirestoreException e) {
                        if(queryDocumentSnapshots != null && !queryDocumentSnapshots.isEmpty()){
                            totalPos = queryDocumentSnapshots.size();
                            int count = 0;
                            for(DocumentChange doc : queryDocumentSnapshots.getDocumentChanges()){
                                if(doc.getType() == DocumentChange.Type.ADDED){
                                    count++;
                                    String ID = doc.getDocument().getString(getString(R.string.F_USERDETAILS_UID));
                                    //Get Profile Data User
                                    GetProfileDataByID(ID, count);
                                }
                            }
                        }
                        Utils.UnSetProgressDialogIndeterminate();
                    }
                });
                //endregion
            }
            else if(searchType == SearchType.FOLLOWING){
                //Adding list here
                user_adapter = new AdapterRecycler(Activity_SearchPage.this, userLists, searchType);
                mRecyclerView.setAdapter(user_adapter);

                //region FOLLOWING
                String UID = FirebaseUtils.firebaseAuth.getCurrentUser().getUid();
                String Root = getString(R.string.FIRESTORE_ROOT_USERDETAILS) + "/" + UID + "/" + getString(R.string.FIRESTORE_ROOT_FOLLOWING);
                CollectionReference collectionReference = firebaseFirestore.collection(Root);
                Query query = collectionReference.whereEqualTo(getString(R.string.F_USERDETAILS_FOLLOWER_STATE), Activity_Friend_Profile.FollowState.ACCEPT.toString());
                query.orderBy(getString(R.string.F_USERDETAILS_FOLLOWER_DATE), Query.Direction.DESCENDING);

                listenerRegistration = FirebaseUtils.FirestoreGetAllData(query, this, new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(QuerySnapshot queryDocumentSnapshots, FirebaseFirestoreException e) {
                        if(queryDocumentSnapshots != null && !queryDocumentSnapshots.isEmpty()){
                            totalPos = queryDocumentSnapshots.size();
                            int count = 0;
                            for(DocumentChange doc : queryDocumentSnapshots.getDocumentChanges()){
                                if(doc.getType() == DocumentChange.Type.ADDED){
                                    count++;
                                    String ID = doc.getDocument().getString(getString(R.string.F_USERDETAILS_UID));
                                    //Get Profile Data User
                                    GetProfileDataByID(ID, count);
                                }
                            }
                        }
                        Utils.UnSetProgressDialogIndeterminate();
                    }
                });
                //endregion
            }
            else{
                //..
            }
            swipeRefreshLayout.setRefreshing(false);
        }
        catch (Exception ex){
            swipeRefreshLayout.setRefreshing(false);
            Utils.UnSetProgressDialogIndeterminate();
            Log.e(TAG, ex.toString());
        }
    }

    private void GetProfileDataByID(final String ID, final int count){
        String Root = getString(R.string.FIRESTORE_ROOT_USERDETAILS) + "/" + ID;
        FirebaseUtils.FirestoreGetData(Root, new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                if(documentSnapshot.exists()){
                    String FN = documentSnapshot.getString(getString(R.string.F_USERDETAILS_USERFULLNAME));
                    String IL = documentSnapshot.getString(getString(R.string.F_USERDETAILS_PROFILEURL));

                    SearchedItems bufferItems = new SearchedItems(ID, IL, FN, "View Details");
                    user_bufferList.add(bufferItems);
                    checkFItems(count);
                }
            }
        }, null);
    }

    private void checkFItems(int currentPos){
        if(currentPos >= totalPos){
            Utils.UnSetProgressDialogIndeterminate();
            SearchQuery("");
        }
    }

    private void CollectLibraryBooks(int count){
        if(count >= libraryTotalBooks){
            Utils.UnSetProgressDialogIndeterminate();
            //SearchQuery("");
        }
    }

    @Override
    public void onRefresh() {
        totalPos = 0;
        if(listenerRegistration != null) {listenerRegistration.remove(); listenerRegistration = null;}
        searchView.setQuery("", false);
        LoadAllData();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if(item.getItemId() == android.R.id.home){
            finish();
        }

        return super.onOptionsItemSelected(item);
    }

    private void SearchQuery(String query){
        try{
            if (searchType == SearchType.LIBRARY) {
                //region LIBRARY STORE
                bookItemsList.clear();
                int count =0, max = queryLimit;

                if(!query.equals("")) {
                    for (BookItems bookItems : buffer_bookItemsList) {
                        if (count < max && bookItems.Name.matches("(?i)(" + query + ").*")) {
                            bookItemsList.add(bookItems);
                            count++;
                        }
                    }
                }
                book_library_item_adapterRecycler.notifyDataSetChanged();


                if(count <=0){
                    //NoDataFound.setVisibility(View.VISIBLE);
                    //((TextView)NoDataFound.findViewById(R.id.title)).setText("No Data Found");
                    //((TextView)NoDataFound.findViewById(R.id.titleDataNotFound)).setText("Please use other query string to find books");

                    if(query.equals("")) {
                        NoDataFound.setVisibility(View.VISIBLE);
                        ((ImageView)NoDataFound.findViewById(R.id.icon)).setImageResource(R.drawable.search_icon);
                        ((TextView)NoDataFound.findViewById(R.id.title)).setText("Search Your Data");
                        ((TextView)NoDataFound.findViewById(R.id.titleDataNotFound)).setText("Use suffix to search data in search bar");
                    }
                    else{
                        ((ImageView)NoDataFound.findViewById(R.id.icon)).setImageResource(R.drawable.search_icon);
                        NoDataFound.setVisibility(View.VISIBLE);
                        ((TextView) NoDataFound.findViewById(R.id.title)).setText("No Data Found");
                        ((TextView) NoDataFound.findViewById(R.id.titleDataNotFound)).setText("Please use other query string to find user");
                    }
                }
                else{
                    NoDataFound.setVisibility(View.INVISIBLE);
                }
                //endregion
            }
            else if(searchType == SearchType.SEARCHFRIENDS || searchType == SearchType.FOLLOWER || searchType == SearchType.FOLLOWING){
                //region USERS SEARCH
                userLists.clear();
                int count = 0, max = queryLimit;
                if(!query.equals("")) {
                    for (SearchedItems items : user_bufferList) {
                        if (count < max && items.getTitlestr().matches("(?i)(" + query + ").*")) {
                            userLists.add(items);
                            count++;
                        }
                    }
                }
                user_adapter.notifyDataSetChanged();
                if(count <=0){
                    if(query.equals("")) {
                        NoDataFound.setVisibility(View.VISIBLE);
                        ((ImageView)NoDataFound.findViewById(R.id.icon)).setImageResource(R.drawable.search_icon);
                        ((TextView)NoDataFound.findViewById(R.id.title)).setText("Search Your Data");
                        ((TextView)NoDataFound.findViewById(R.id.titleDataNotFound)).setText("Use suffix to search data in search bar");
                    }
                    else{
                        ((ImageView)NoDataFound.findViewById(R.id.icon)).setImageResource(R.drawable.search_icon);
                        NoDataFound.setVisibility(View.VISIBLE);
                        ((TextView) NoDataFound.findViewById(R.id.title)).setText("No Data Found");
                        ((TextView) NoDataFound.findViewById(R.id.titleDataNotFound)).setText("Please use other query string to find user");
                    }
                }
                else{
                    NoDataFound.setVisibility(View.INVISIBLE);
                }
                //endregion
            }
            else{
                //..
            }
        }
        catch (Exception ex){
            Log.e(TAG, ex.toString());
        }
    }
}

class AdapterRecycler extends RecyclerView.Adapter<AdapterRecycler.holder>{
    private static final String TAG = "Adapter Recycler";
    private Activity activity;
    private List<SearchedItems> mList;
    private Activity_SearchPage.SearchType searchType;

    public AdapterRecycler(Activity activity, List<SearchedItems> mList, Activity_SearchPage.SearchType searchType) {
        this.activity = activity;
        this.mList = mList;
        this.searchType = searchType;
    }

    @NonNull
    @Override
    public holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(activity.getBaseContext()).inflate(R.layout.layout_search_item,parent,false);
        return new holder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final holder holder, final int position) {
        try{
            if(searchType == Activity_SearchPage.SearchType.SEARCHFRIENDS){
                //region Search Friends
                SearchFriends_CV(holder, position);
                //endregion
            }
            else if(searchType == Activity_SearchPage.SearchType.FOLLOWER){
                //region Search Followers
                Follower_CV(holder, position);
                //endregion
            }
            else if(searchType == Activity_SearchPage.SearchType.FOLLOWING){
                //region Search Followers
                Following_CV(holder, position);
                //endregion
            }
        }
        catch (Exception ex){
            Log.e(TAG, ex.toString());
        }
    }

    private void SearchFriends_CV (final holder holder, final int position){
        try {
            //Set User Image
            ColorDrawable colorDrawableInfoPlaceholder = new ColorDrawable(ContextCompat.getColor(activity, R.color.colorDrawableInfoPlaceholder));
            ColorDrawable colorDrawableErrorPlaceholder = new ColorDrawable(ContextCompat.getColor(activity, R.color.colorDrawableErrorPlaceholder));
            Utils.SetImageViaUri(
                    activity,
                    mList.get(position).getId(),
                    mList.get(position).getImagelinkstr(),
                    holder.imageview,
                    false,
                    false,
                    colorDrawableInfoPlaceholder,
                    colorDrawableErrorPlaceholder,
                    null
            );

            //Set Searched Title
            holder.title.setText(mList.get(position).getTitlestr());

            //Inside Button Name
            holder.buttontitle.setText(mList.get(position).getButtonstr());

            //Follower/Following button click
            holder.button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent i = new Intent(activity, Activity_Friend_Profile.class);
                    i.putExtra("UID", mList.get(position).getId());
                    activity.startActivity(i);
                }
            });
        }
        catch (Exception ex){
            Log.e(TAG, ex.toString());
        }
    }

    private void Follower_CV (final holder holder, final int position){

        try {
            //Set Name, Image
            //Set User Image
            ColorDrawable colorDrawableInfoPlaceholder = new ColorDrawable(ContextCompat.getColor(activity, R.color.colorDrawableInfoPlaceholder));
            ColorDrawable colorDrawableErrorPlaceholder = new ColorDrawable(ContextCompat.getColor(activity, R.color.colorDrawableErrorPlaceholder));
            Utils.SetImageViaUri(
                    activity,
                    mList.get(position).getId(),
                    mList.get(position).getImagelinkstr(),
                    holder.imageview,
                    false,
                    false,
                    colorDrawableInfoPlaceholder,
                    colorDrawableErrorPlaceholder,
                    null
            );

            //Set Searched Title
            holder.title.setText(mList.get(position).getTitlestr());

            holder.buttontitle.setText(mList.get(position).getButtonstr());

            //Follower/Following button click
            holder.button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent i = new Intent(activity, Activity_Friend_Profile.class);
                    i.putExtra("UID", mList.get(position).getId());
                    activity.startActivity(i);
                }
            });
        }
        catch (Exception ex){
            Log.e(TAG, ex.toString());
        }
    }

    private void Following_CV (final holder holder, final int position){
        try {
            //Set User Image
            ColorDrawable colorDrawableInfoPlaceholder = new ColorDrawable(ContextCompat.getColor(activity, R.color.colorDrawableInfoPlaceholder));
            ColorDrawable colorDrawableErrorPlaceholder = new ColorDrawable(ContextCompat.getColor(activity, R.color.colorDrawableErrorPlaceholder));
            Utils.SetImageViaUri(
                    activity,
                    mList.get(position).getId(),
                    mList.get(position).getImagelinkstr(),
                    holder.imageview,
                    false,
                    false,
                    colorDrawableInfoPlaceholder,
                    colorDrawableErrorPlaceholder,
                    null
            );

            //Set Searched Title
            holder.title.setText(mList.get(position).getTitlestr());

            holder.buttontitle.setText(mList.get(position).getButtonstr());

            //Follower/Following button click
            holder.button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent i = new Intent(activity, Activity_Friend_Profile.class);
                    i.putExtra("UID", mList.get(position).getId());
                    activity.startActivity(i);
                }
            });
        }
        catch (Exception ex){
            Log.e(TAG, ex.toString());
        }
    }

    @Override
    public int getItemCount() {
        return mList.size();
    }

    class holder extends RecyclerView.ViewHolder{

        View button;
        TextView buttontitle, title;
        ImageView imageview;

        public holder(View v){
            super(v);
            button = v.findViewById(R.id.button);
            buttontitle = v.findViewById(R.id.buttontitle);
            imageview = v.findViewById(R.id.searchedimage);
            title = v.findViewById(R.id.searchedtitle);
        }
    }
}

class Book_Library_Item_AdapterRecycler extends RecyclerView.Adapter<Book_Library_Item_AdapterRecycler.Holder>{
    private static final String TAG = "Activity_SM_AR";
    private Context context;
    private List<BookItems> bookItemsList;

    Book_Library_Item_AdapterRecycler(Context context, List<BookItems> bookItemsList) {
        this.context = context;
        this.bookItemsList = bookItemsList;
    }

    @NonNull
    @Override
    public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.recycler_item_inner_list_library, parent, false);
        return new Holder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final Holder holder, int position) {
        if(holder.tName != null){
            holder.tName.setText(bookItemsList.get(position).Name);
        }

        if(holder.tAuthor != null){
            String authorData = "by "+ bookItemsList.get(position).Author;
            holder.tAuthor.setText(authorData);
        }

        if(holder.tDescription != null){
            holder.tDescription.setText(bookItemsList.get(position).Description);
        }

        if (holder.tImage != null) {
            ColorDrawable colorDrawableInfoPlaceholder = new ColorDrawable(ContextCompat.getColor(context, R.color.colorDrawableInfoPlaceholder));
            ColorDrawable colorDrawableErrorPlaceholder = new ColorDrawable(ContextCompat.getColor(context, R.color.colorDrawableErrorPlaceholder));
            Utils.SetImageViaUri((Activity)context, bookItemsList.get(position).imageURL, holder.tImage,false,false, colorDrawableInfoPlaceholder, colorDrawableErrorPlaceholder,null);
        }

        holder.textOptionMenu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //region Option Menu
                PopupMenu popupMenu = new PopupMenu(context, holder.textOptionMenu);
                popupMenu.inflate(R.menu.library_menu_cloud);
                popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        switch (item.getItemId()){
                            case R.id.menuView:
                                Utils.OpenBook(context, bookItemsList.get(holder.getAdapterPosition()));
                                break;
                            case R.id.menuInfo:
                                GoToBookDetails(holder);
                                break;

                            case R.id.menuDelete:
                                new AlertDialog.Builder(context).
                                        setTitle("Delete").
                                        setMessage("Are you sure want to remove this from library? \n(*note you can re-download it from your purchase section.)").
                                        setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                DeleteCloudBook(bookItemsList.get(holder.getAdapterPosition()).BOOKID);
                                                bookItemsList.remove(holder.getAdapterPosition());
                                                Book_Library_Item_AdapterRecycler.this.notifyItemRemoved(holder.getAdapterPosition());
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
                //endregion
            }
        });

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Utils.OpenBook(context, bookItemsList.get(holder.getAdapterPosition()));
            }
        });
    }

    private void DeleteCloudBook(final String BookID){
        try{
            String UID = FirebaseUtils.firebaseAuth.getCurrentUser().getUid();
            String Root = context.getString(R.string.FIRESTORE_ROOT_USERDETAILS) + "/" + UID + "/" + context.getString(R.string.FIRESTORE_ROOT_LIBRARY) + "/" + BookID;
            Map<String, Object> map = new HashMap<>();
            map.put(context.getString(R.string.F_USERDETAILS_LIBRARY_STATE), Activity_BookDetails.LibraryState.DELETE.toString());
            FirebaseUtils.FirestroreSetData(Root, map,
                    new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            Log.i(TAG, "BOOK DELETED " + BookID);
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

    private void GoToBookDetails(Holder holder){
        try {
            Intent intent = new Intent(context, Activity_BookDetails.class);
            intent.putExtra("BookID", bookItemsList.get(holder.getAdapterPosition()).BOOKID);
            context.startActivity(intent);
        }
        catch (Exception ex){
            Log.e("ERROR", ex.toString());
        }
    }

    @Override
    public int getItemCount() {
        return bookItemsList.size();
    }

    class Holder extends RecyclerView.ViewHolder{
        ImageView tImage;
        TextView tName, tAuthor, tDescription, textOptionMenu;
        public Holder(View v){
            super(v);
            tImage = v.findViewById(R.id.recyclerItemImage);
            tName = v.findViewById(R.id.recyclerItemName);
            tAuthor = v.findViewById(R.id.author);
            tDescription = v.findViewById(R.id.recyclerItemDescription);
            textOptionMenu = v.findViewById(R.id.optionMenu);
        }
    }
}