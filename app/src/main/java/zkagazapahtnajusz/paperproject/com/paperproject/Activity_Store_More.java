package zkagazapahtnajusz.paperproject.com.paperproject;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
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
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

import me.gujun.android.taggroup.TagGroup;
import zkagazapahtnajusz.paperproject.com.paperproject.Model.BookItems;
import zkagazapahtnajusz.paperproject.com.paperproject.Utilities.FirebaseUtils;
import zkagazapahtnajusz.paperproject.com.paperproject.Utilities.Utils;

public class Activity_Store_More extends AppCompatActivity implements SwipeRefreshLayout.OnRefreshListener{
    private static final String TAG = "Activity_Store_More";
    
    SwipeRefreshLayout swipeRefreshLayout;
    RecyclerView mRecyclerView;
    View NoDataFoundView;
    TextView titleDataNotFound;
    Activity_Store_More_AdapterRecycler adapter;
    List<BookItems> bookItemsList = new ArrayList<>();
    Toolbar toolbar;
    LinearLayoutManager layoutManager;

    private boolean loading = true;
    int pastVisiblesItems, visibleItemCount, totalItemCount;

    int queryLimit;

    String Suffix = null;
    private FirebaseFirestore firebaseFirestore;
    DocumentSnapshot lastVisible;
    boolean isFirstPageFirstLoad = false;

    TextView toolbarTitle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity__store__more);

        Utils.DEVICE_CHECK(this);//DEVICE CHECK
        queryLimit = Utils.CURRENT_GRID_COUNT;

        NoDataFoundView = findViewById(R.id.NoDataFoundView);
        titleDataNotFound = findViewById(R.id.titleDataNotFound);

        toolbarTitle = findViewById(R.id.toolbarTitle);

        swipeRefreshLayout = findViewById(R.id.swipeRefresh);
        swipeRefreshLayout.setOnRefreshListener(this);

        toolbar = findViewById(R.id.toolbarSearchPage);
        mRecyclerView = findViewById(R.id.recyclerSearchPage);

        setSupportActionBar(toolbar);
        if(getSupportActionBar() != null){
            getSupportActionBar().setDisplayShowTitleEnabled(false);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }

        Bundle bundle = getIntent().getExtras();
        if(bundle != null){
            Suffix = bundle.getString("Suffix");
            String buffer_title = "Books Start With \"" + Suffix + "\"";
            toolbarTitle.setText(buffer_title);
        }

        Log.i("---->", Suffix);

        //Setting RecyclerView
        layoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(layoutManager);

        //Adding list here
        adapter = new Activity_Store_More_AdapterRecycler(this, bookItemsList);
        mRecyclerView.setAdapter(adapter);

        mRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
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

                    if (loading && ((visibleItemCount + pastVisiblesItems) >= totalItemCount))
                    {
                        loading = false;
                        //Load More
                        LoadMore();
                    }
                }
            }
        });

        //Load Page
        LoadPages();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if(item.getItemId() == android.R.id.home){
            finish();
        }

        return super.onOptionsItemSelected(item);
    }

    public void LoadPages(){//Activate this First
        try {
            firebaseFirestore = FirebaseFirestore.getInstance();
            FirstPage();
        }
        catch (Exception ex){
            Log.e(TAG, ex.toString());
        }
    }

    private void FirstPage(){
        try{
            NoDataFoundView.setVisibility(View.INVISIBLE);
            String temp_root = getString(R.string.FIRESTORE_ROOT_BOOKS);
            CollectionReference collectionReference = FirebaseFirestore.getInstance().collection(temp_root);
            Query firstQuery = collectionReference.whereEqualTo(getString(R.string.F_BOOKS_SUFFIX), Suffix).limit(queryLimit);
            FirebaseUtils.FirestoreGetAllData(firstQuery, this, new EventListener<QuerySnapshot>() {
                @Override
                public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {
                    if(queryDocumentSnapshots !=null && !queryDocumentSnapshots.isEmpty()) {
                        if(!isFirstPageFirstLoad){
                            lastVisible = queryDocumentSnapshots.getDocuments().get(queryDocumentSnapshots.size() -1);
                        }
                        for (DocumentChange doc : queryDocumentSnapshots.getDocumentChanges()) {
                            if(doc.getType() == DocumentChange.Type.ADDED) {
                                try {
                                    BookItems bookItems = new BookItems(
                                            doc.getDocument().getString(getString(R.string.F_BOOKS_PUBLICKEY)),
                                            doc.getDocument().getString(getString(R.string.F_BOOKS_IMAGEURL)),
                                            doc.getDocument().getString(getString(R.string.F_BOOKS_BOOKID)),
                                            doc.getDocument().getString(getString(R.string.F_BOOKS_TITLE)),
                                            doc.getDocument().getString(getString(R.string.F_BOOKS_AUTHOR)),
                                            doc.getDocument().getString(getString(R.string.F_BOOKS_DESCRIPTION)),
                                            Double.valueOf(doc.getDocument().get(getString(R.string.F_BOOKS_PRICE)).toString()),
                                            doc.getDocument().getString(getString(R.string.F_BOOKS_CURRENCYSTR)),
                                            (ArrayList)doc.getDocument().get(getString(R.string.F_BOOKS_GENRE)),
                                            BookItems.BookFlag.valueOf(doc.getDocument().getString(getString(R.string.F_BOOKS_BOOKFLAG))),
                                            BookItems.FileType.EPUB,
                                            BookItems.StorageType.CLOUD
                                    );

                                    if(!isFirstPageFirstLoad){
                                        bookItemsList.add(bookItems);
                                        adapter.notifyItemChanged(bookItemsList.size() - 1);
                                    }
                                    else{
                                        bookItemsList.add(0, bookItems);
                                        adapter.notifyItemChanged(0);
                                    }
                                }
                                catch (Exception ex){
                                    Log.e(TAG, ex.toString());
                                }
                            }
                        }
                        isFirstPageFirstLoad = true;
                    }
                    else{
                        //Do Data Found
                        titleDataNotFound.setText("No Book Found In Database");
                        NoDataFoundView.setVisibility(View.VISIBLE);
                        Log.e(TAG, "NO DATA FOUND -  MODE SEARCH FRIENDS");
                    }
                    swipeRefreshLayout.setRefreshing(false);
                }
            });
        }
        catch (Exception ex){
            swipeRefreshLayout.setRefreshing(false);
            Log.e(TAG, ex.toString());
        }
    }

    private void LoadMore(){
        try{
            NoDataFoundView.setVisibility(View.INVISIBLE);
            String temp_root = getString(R.string.FIRESTORE_ROOT_BOOKS);
            CollectionReference collectionReference = FirebaseFirestore.getInstance().collection(temp_root);
            Query firstQuery = collectionReference.whereEqualTo(getString(R.string.F_BOOKS_SUFFIX), Suffix).startAfter(lastVisible).limit(queryLimit);
            FirebaseUtils.FirestoreGetAllData(firstQuery, this, new EventListener<QuerySnapshot>() {
                @Override
                public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {
                    if(queryDocumentSnapshots !=null && !queryDocumentSnapshots.isEmpty()) {
                        lastVisible = queryDocumentSnapshots.getDocuments().get(queryDocumentSnapshots.size() -1);
                        for (DocumentChange doc : queryDocumentSnapshots.getDocumentChanges()) {
                            if(doc.getType() == DocumentChange.Type.ADDED) {
                                try {
                                    BookItems bookItems = new BookItems(
                                            doc.getDocument().getString(getString(R.string.F_BOOKS_PUBLICKEY)),
                                            doc.getDocument().getString(getString(R.string.F_BOOKS_IMAGEURL)),
                                            doc.getDocument().getString(getString(R.string.F_BOOKS_BOOKID)),
                                            doc.getDocument().getString(getString(R.string.F_BOOKS_TITLE)),
                                            doc.getDocument().getString(getString(R.string.F_BOOKS_AUTHOR)),
                                            doc.getDocument().getString(getString(R.string.F_BOOKS_DESCRIPTION)),
                                            Double.valueOf(doc.getDocument().get(getString(R.string.F_BOOKS_PRICE)).toString()),
                                            doc.getDocument().getString(getString(R.string.F_BOOKS_CURRENCYSTR)),
                                            (ArrayList)doc.getDocument().get(getString(R.string.F_BOOKS_GENRE)),
                                            BookItems.BookFlag.valueOf(doc.getDocument().getString(getString(R.string.F_BOOKS_BOOKFLAG))),
                                            BookItems.FileType.EPUB,
                                            BookItems.StorageType.CLOUD
                                    );
                                    bookItemsList.add(bookItems);
                                    adapter.notifyItemChanged(bookItemsList.size() - 1);
                                }
                                catch (Exception ex){
                                    Log.e(TAG, ex.toString());
                                }
                            }
                        }
                    }
                }
            });
        }
        catch (Exception ex){
            Log.e(TAG, ex.toString());
        }
    }

    @Override
    public void onRefresh() {
        isFirstPageFirstLoad = false;
        bookItemsList.clear();
        adapter.notifyDataSetChanged();
        LoadPages();
    }
}

class Activity_Store_More_AdapterRecycler extends RecyclerView.Adapter<Activity_Store_More_AdapterRecycler.Holder>{
    private static final String TAG = "Activity_SM_AR";
    private Context context;
    private List<BookItems> bookItemsList;

    public Activity_Store_More_AdapterRecycler(Context context, List<BookItems> bookItemsList) {
        this.context = context;
        this.bookItemsList = bookItemsList;
    }

    @NonNull
    @Override
    public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.recycler_item_inner_list_store, parent, false);
        return new Holder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final Holder holder, final int position) {
        try{
            //region SET IMAGE
            if (holder.tImage != null) {
                ColorDrawable colorDrawableInfoPlaceholder = new ColorDrawable(ContextCompat.getColor(context, R.color.colorDrawableInfoPlaceholder));
                ColorDrawable colorDrawableErrorPlaceholder = new ColorDrawable(ContextCompat.getColor(context, R.color.colorDrawableErrorPlaceholder));

                Utils.SetImageViaUri(
                        (Activity)context,
                        bookItemsList.get(position).imageURL,
                        holder.tImage,
                        false,
                        true,
                        colorDrawableInfoPlaceholder,
                        colorDrawableErrorPlaceholder,
                        null
                );
            }
            //endregion

            //region Set Name
            if (holder.tName != null) {
                try{
                    holder.tName.setText(bookItemsList.get(position).Name);
                }
                catch (Exception ex){
                    Log.e(TAG, ex.toString());
                }
            }
            //endregion

            //region Adding TAG
            if(holder.mTagGroup != null) {
                try{
                    String UID = FirebaseUtils.firebaseAuth.getCurrentUser().getUid();
                    String Root = context.getString(R.string.FIRESTORE_ROOT_USERDETAILS) + "/" + UID + "/" + context.getString(R.string.FIRESTORE_ROOT_LIBRARY) + "/" + bookItemsList.get(position).BOOKID;
                    FirebaseUtils.FirestoreGetData(Root, new OnSuccessListener<DocumentSnapshot>() {
                        @Override
                        public void onSuccess(DocumentSnapshot documentSnapshot) {
                            try {
                                if (documentSnapshot != null && documentSnapshot.exists()) {
                                    bookItemsList.get(holder.getAdapterPosition()).bookFlag = BookItems.BookFlag.Purchased;
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
                                bookItemsList.get(holder.getAdapterPosition()).bookFlag = BookItems.BookFlag.Nothing;
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
            //endregion

            //region LIKE
            if (holder.tLikes != null) {
                try{
                    String BOOKID = bookItemsList.get(position).BOOKID;
                    String Root = context.getString(R.string.FIRESTORE_ROOT_BOOKS) + "/" + BOOKID + "/" + context.getString(R.string.FIRESTORE_ROOT_BOOKS_LIKES);
                    CollectionReference collectionReference = FirebaseFirestore.getInstance().collection(Root);
                    FirebaseUtils.FirestoreGetAllData(collectionReference, (Activity) context, new EventListener<QuerySnapshot>() {
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

            //region VIEWS
            if (holder.tViews != null) {
                try{
                    String BOOKID = bookItemsList.get(position).BOOKID;
                    String Root = context.getString(R.string.FIRESTORE_ROOT_BOOKS) + "/" + BOOKID + "/" + context.getString(R.string.FIRESTORE_ROOT_BOOKS_VIEWS);
                    CollectionReference collectionReference = FirebaseFirestore.getInstance().collection(Root);
                    FirebaseUtils.FirestoreGetAllData(collectionReference, (Activity) context, new EventListener<QuerySnapshot>() {
                        @Override
                        public void onEvent(QuerySnapshot queryDocumentSnapshots, FirebaseFirestoreException e) {
                            try {
                                if (!queryDocumentSnapshots.isEmpty()) {
                                    String TotalViews = String.valueOf(queryDocumentSnapshots.getDocumentChanges().size());
                                    TotalViews = Utils.Numberformat(Long.valueOf(TotalViews));
                                    holder.tViews.setText(TotalViews);
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

            //region COMMENTS
            if (holder.tComments != null) {
                try{
                    String BOOKID = bookItemsList.get(position).BOOKID;
                    String Root = context.getString(R.string.FIRESTORE_ROOT_BOOKS) + "/" + BOOKID + "/" + context.getString(R.string.FIRESTORE_ROOT_BOOKS_COMMENTS);
                    CollectionReference collectionReference = FirebaseFirestore.getInstance().collection(Root);
                    FirebaseUtils.FirestoreGetAllData(collectionReference, (Activity)context, new EventListener<QuerySnapshot>() {
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
                    holder.tDescription.setText(bookItemsList.get(position).Description);
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
                        Intent intent = new Intent(context, Activity_BookDetails.class);
                        intent.putExtra("BookID", bookItemsList.get(holder.getAdapterPosition()).BOOKID);
                        context.startActivity(intent);
                    }
                    catch (Exception ex){
                        Log.e(TAG,ex.toString());
                    }
                }
            });
            //endregion
        }
        catch (Exception ex){
            Log.e(TAG, ex.toString());
        }
    }

    private void UpdateBookFlag(int position, Holder holder){
        try {
            String tagText;
            if (bookItemsList.get(position).bookFlag == BookItems.BookFlag.Free) {
                tagText = "Free";
                String[] tagValue = {tagText};
                holder.mTagGroup.setTags(tagValue);
            } else if (bookItemsList.get(position).bookFlag == BookItems.BookFlag.Store) {
                tagText = "Buy " + bookItemsList.get(position).CurrencyStr + " " + bookItemsList.get(position).Price;
                String[] tagValue = {tagText};
                holder.mTagGroup.setTags(tagValue);
            } else if (bookItemsList.get(position).bookFlag == BookItems.BookFlag.Purchased) {
                tagText = "Purchased";
                String[] tagValue = {tagText};
                holder.mTagGroup.setTags(tagValue);
            } else if (bookItemsList.get(position).bookFlag == BookItems.BookFlag.Nothing) {
                tagText = "Error";
                String[] tagValue = {tagText};
                holder.mTagGroup.setTags(tagValue);
            }
        }
        catch (ArrayIndexOutOfBoundsException ex){
            Log.e(TAG, ex.getMessage());
        }
    }

    @Override
    public int getItemCount() {
        return bookItemsList.size();
    }

    class Holder extends RecyclerView.ViewHolder{
        ImageView tImage;
        TextView tName, tViews, tComments, tDescription, tLikes;
        TagGroup mTagGroup;
        public Holder(View v){
            super(v);
            tImage = v.findViewById(R.id.recyclerItemImage);
            tName = v.findViewById(R.id.recyclerItemName);
            tViews = v.findViewById(R.id.recyclerItemViews);
            tComments = v.findViewById(R.id.recyclerItemComments);
            tDescription = v.findViewById(R.id.recyclerItemDescription);
            tLikes = v.findViewById(R.id.recyclerItemLikes);
            mTagGroup = v.findViewById(R.id.tag_group);
        }
    }
}


