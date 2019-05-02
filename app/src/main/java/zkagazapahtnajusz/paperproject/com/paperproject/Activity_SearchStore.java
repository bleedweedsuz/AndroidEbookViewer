package zkagazapahtnajusz.paperproject.com.paperproject;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.Animation;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import me.gujun.android.taggroup.TagGroup;
import zkagazapahtnajusz.paperproject.com.paperproject.Model.BookItems;
import zkagazapahtnajusz.paperproject.com.paperproject.Utilities.FirebaseUtils;
import zkagazapahtnajusz.paperproject.com.paperproject.Utilities.SlideAnimation;
import zkagazapahtnajusz.paperproject.com.paperproject.Utilities.Utils;

public class Activity_SearchStore extends AppCompatActivity implements SwipeRefreshLayout.OnRefreshListener{

    private static final String TAG = "Activity_SearchStore";

    RecyclerView mRecyclerView;

    ImageView searchFilter, backButton;
    Toolbar mToolbar;
    Spinner spinnerGenre, spinnerAuthor, spinnerDisplay;
    SearchView searchView;
    boolean toggle;

    int queryLimit = 10;

    List<BookItems> buffer_bookItemsList = new ArrayList<>();
    List<BookItems> bookItemsList = new ArrayList<>();

    Book_Store_Item_AdapterRecycler book_store_item_adapterRecycler;

    View NoDataFound;

    LinearLayoutManager layoutManager;

    SwipeRefreshLayout swipeRefreshLayout;

    ArrayList<String> genreList = new ArrayList<>();
    ArrayList<String> authorList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_store);
        searchFilter = findViewById(R.id.searchFilter);
        mToolbar = findViewById(R.id.toolbarSearchFilter);
        spinnerGenre = findViewById(R.id.spinnerGenre);
        spinnerAuthor = findViewById(R.id.spinnerAuthor);
        spinnerDisplay = findViewById(R.id.spinnerDisplay);
        backButton = findViewById(R.id.imageBack);
        searchView = findViewById(R.id.searchView);

        searchView.setIconified(true);
        searchView.onActionViewExpanded();
        searchView.clearFocus();

        mRecyclerView = findViewById(R.id.recyclerSearchPage);

        NoDataFound = findViewById(R.id.NoDataFoundView);

        setSupportActionBar(mToolbar);


        layoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(layoutManager);

        swipeRefreshLayout = findViewById(R.id.swipeRefresh);
        swipeRefreshLayout.setOnRefreshListener(this);

        //region SearchFilter Animation
        searchFilter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                float density = getResources().getDisplayMetrics().density;
                int px = (int)(54 * density);
                int py = (int)(168 * density);
                if(!toggle) {
                    Animation animation = new SlideAnimation(mToolbar, px, py);
                    animation.setInterpolator(new AccelerateInterpolator());
                    animation.setDuration(300);
                    mToolbar.setAnimation(animation);
                    mToolbar.startAnimation(animation);
                    toggle = true;
                    searchFilter.setImageResource(R.drawable.icon_filter_dark);
                }else{
                    Animation animation = new SlideAnimation(mToolbar, py, px);
                    animation.setInterpolator(new AccelerateInterpolator());
                    animation.setDuration(300);
                    mToolbar.setAnimation(animation);
                    mToolbar.startAnimation(animation);
                    toggle = false;
                    searchFilter.setImageResource(R.drawable.icon_filter);
                }
            }
        });
        //endregion

        //Custom Back Button
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

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


        //Spinner Display
        ArrayList<String> displayList = new ArrayList<>();
        displayList.add("Any");
        displayList.add("Free");
        displayList.add("Paid");

        SpinnerArrayAdapter spinnerDisplayAdapter = new SpinnerArrayAdapter(this, displayList);
        spinnerDisplay.setAdapter(spinnerDisplayAdapter);
    }

    private void LoadAllData(){
        try{
            Utils.SetProgressDialogIndeterminate(this, "loading..");

            buffer_bookItemsList.clear();
            bookItemsList.clear();

            genreList.clear(); genreList.add("Genre");
            authorList.clear(); authorList.add("Author");


            NoDataFound.setVisibility(View.VISIBLE);
            ((ImageView)NoDataFound.findViewById(R.id.icon)).setImageResource(R.drawable.search_icon);
            ((TextView)NoDataFound.findViewById(R.id.title)).setText("Search Your Data");
            ((TextView)NoDataFound.findViewById(R.id.titleDataNotFound)).setText("Use suffix to search data in search bar");


            book_store_item_adapterRecycler = new Book_Store_Item_AdapterRecycler(this, bookItemsList);
            mRecyclerView.setAdapter(book_store_item_adapterRecycler);

            FirebaseFirestore firebaseFirestore = FirebaseFirestore.getInstance();
            //Load All Data
            String Root = getString(R.string.FIRESTORE_ROOT_BOOKS);
            CollectionReference collectionReference = firebaseFirestore.collection(Root);
            Query query = collectionReference.whereEqualTo(getString(R.string.F_BOOKS_STATUS), BookItems.Status.Public.toString());

            FirebaseUtils.FirestoreGetAllData(query, this, new EventListener<QuerySnapshot>() {
                @Override
                public void onEvent(QuerySnapshot queryDocumentSnapshots, FirebaseFirestoreException e) {
                    if(!queryDocumentSnapshots.isEmpty()){
                        for(DocumentChange doc : queryDocumentSnapshots.getDocumentChanges()){
                            if(doc.getType() == DocumentChange.Type.ADDED){
                                ArrayList buffer_genre = (ArrayList) doc.getDocument().get(getString(R.string.F_BOOKS_GENRE));
                                String buffer_author = doc.getDocument().getString(getString(R.string.F_BOOKS_AUTHOR));

                                BookItems bookItems = new BookItems(
                                        doc.getDocument().getString(getString(R.string.F_BOOKS_PUBLICKEY)),
                                        doc.getDocument().getString(getString(R.string.F_BOOKS_IMAGEURL)),
                                        doc.getDocument().getString(getString(R.string.F_BOOKS_BOOKID)),
                                        doc.getDocument().getString(getString(R.string.F_BOOKS_TITLE)).trim(),
                                        buffer_author,
                                        doc.getDocument().getString(getString(R.string.F_BOOKS_DESCRIPTION)),
                                        Double.valueOf(doc.getDocument().get(getString(R.string.F_BOOKS_PRICE)).toString()),
                                        doc.getDocument().getString(getString(R.string.F_BOOKS_CURRENCYSTR)),
                                        buffer_genre,
                                        BookItems.BookFlag.valueOf(doc.getDocument().getString(getString(R.string.F_BOOKS_BOOKFLAG))),
                                        BookItems.FileType.EPUB,
                                        BookItems.StorageType.CLOUD
                                );

                                for(int i=0;i<buffer_genre.size();i++) {
                                    genreList.add(buffer_genre.get(i).toString());
                                }
                                authorList.add(buffer_author);
                                buffer_bookItemsList.add(bookItems);
                            }
                        }
                    }

                    //Add Adapter Here
                    SpinnerArrayAdapter spinnerAuthorAdapter = new SpinnerArrayAdapter(Activity_SearchStore.this, authorList);
                    spinnerAuthor.setAdapter(spinnerAuthorAdapter);

                    //Add Adapter Here
                    genreList = removeDuplicates(genreList);
                    SpinnerArrayAdapter spinnerGenreAdapter = new SpinnerArrayAdapter(Activity_SearchStore.this, genreList);
                    spinnerGenre.setAdapter(spinnerGenreAdapter);

                    Utils.UnSetProgressDialogIndeterminate();
                    swipeRefreshLayout.setRefreshing(false);
                    //SearchQuery("");
                }
            });
        }
        catch (Exception ex){
            Utils.UnSetProgressDialogIndeterminate();
            Log.e(TAG, ex.toString());
        }
    }

    private ArrayList<String> removeDuplicates(ArrayList<String> list) {
        ArrayList<String> result = new ArrayList<>();
        HashSet<String> set = new HashSet<>();
        for (String item : list) {
            if (!set.contains(item)) {
                result.add(item);
                set.add(item);
            }
        }
        return result;
    }

    @Override
    public void onRefresh() {
        searchView.setQuery("", false);
        LoadAllData();
    }

    private void SearchQuery(String query){
        try{
            if(toggle){
                WithFilter(query);
            }
            else{
                NoFilter(query);
            }
        }
        catch (Exception ex){
            Log.e(TAG, ex.toString());
        }
    }

    private void NoFilter(String query){
        try{
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
            book_store_item_adapterRecycler.notifyDataSetChanged();
            if(count <=0){
                //NoDataFound.setVisibility(View.VISIBLE);
                //((TextView)NoDataFound.findViewById(R.id.title)).setText("No Data Found");
                //((TextView)NoDataFound.findViewById(R.id.titleDataNotFound)).setText("Please use other query string to find books \" \ndon't forge to use filter\"");

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
                    ((TextView)NoDataFound.findViewById(R.id.titleDataNotFound)).setText("Please use other query string to find books \" \ndon't forge to use filter\"");
                }


            }
            else{
                NoDataFound.setVisibility(View.INVISIBLE);
            }
        }
        catch (Exception ex){
            Log.e(TAG, ex.toString());
        }
    }

    private void WithFilter(String query){
        try{
            bookItemsList.clear();
            int count =0, max = queryLimit;
            if(!query.equals("")) {
                for (BookItems bookItems : buffer_bookItemsList) {
                    if (count < max && bookItems.Name.matches("(?i)(" + query + ").*")) {
                        //No Filter
                        if (spinnerAuthor.getSelectedItemPosition() == 0 && spinnerGenre.getSelectedItemPosition() == 0 && spinnerDisplay.getSelectedItemPosition() == 0) {
                            bookItemsList.add(bookItems);
                            count++;
                        } else {
                            String Genre = spinnerGenre.getSelectedItem().toString();
                            String Author = spinnerAuthor.getSelectedItem().toString();

                            boolean isGenre = false, isAuthor = false;
                            BookItems.BookFlag bookFlag = BookItems.BookFlag.Nothing;
                            //Filter Genre
                            if (spinnerGenre.getSelectedItemPosition() > 0) {
                                isGenre = true;
                            }
                            //Filter Author
                            if (spinnerAuthor.getSelectedItemPosition() > 0) {
                                isAuthor = true;
                            }
                            //Filter Display
                            if (spinnerDisplay.getSelectedItemPosition() == 1) {
                                bookFlag = BookItems.BookFlag.Free;
                            } else if (spinnerDisplay.getSelectedItemPosition() == 2) {
                                bookFlag = BookItems.BookFlag.Store;
                            }

                            //region Genre
                            if (isGenre && !isAuthor) {
                                //Only Genre
                                if (bookFlag == BookItems.BookFlag.Nothing) {
                                    if (bookItems.Genre.contains(Genre)) {
                                        bookItemsList.add(bookItems);
                                        count++;
                                        Log.i(TAG, Genre + "------->>ADDED T1_1");
                                    }
                                } else {
                                    //Genre With Book Flag
                                    if (bookItems.Genre.contains(Genre) && bookItems.bookFlag == bookFlag) {
                                        bookItemsList.add(bookItems);
                                        count++;
                                        Log.i(TAG, Genre + "------->>ADDED T1_2");
                                    }
                                }
                            }
                            //endregion

                            //region Author
                            else if (!isGenre && isAuthor) {
                                //Only Author
                                if (bookFlag == BookItems.BookFlag.Nothing) {
                                    if (bookItems.Author.equals(Author)) {
                                        bookItemsList.add(bookItems);
                                        count++;
                                        Log.i(TAG, Author + "------->>ADDED T2_1");
                                    }
                                } else {
                                    //Author With BookFlag
                                    if (bookItems.Author.equals(Author) && bookItems.bookFlag == bookFlag) {
                                        bookItemsList.add(bookItems);
                                        count++;
                                        Log.i(TAG, Author + "------->>ADDED T2_2");
                                    }
                                }
                            }
                            //endregion

                            //region Genre & Author
                            else if (isGenre && isAuthor) {
                                //Both Genre & Author
                                if (bookFlag == BookItems.BookFlag.Nothing) {
                                    if (bookItems.Genre.contains(Genre) && bookItems.Author.equals(Author)) {
                                        bookItemsList.add(bookItems);
                                        count++;
                                        Log.i(TAG, Genre + "---ADDED----" + Author + "---> T3_1");
                                    }
                                } else {
                                    //Genre & Author & Book Flag
                                    if (bookItems.Genre.contains(Genre) && bookItems.Author.equals(Author) && bookItems.bookFlag == bookFlag) {
                                        bookItemsList.add(bookItems);
                                        count++;
                                        Log.i(TAG, Genre + "---ADDED----" + Author + "---> T3_2");
                                    }
                                }
                            }
                            //endregion

                            //region No Genre & Author
                            else {
                                if (bookItems.bookFlag == bookFlag) {
                                    bookItemsList.add(bookItems);
                                    count++;
                                    Log.i(TAG, Genre + "---ADDED----" + Author + "---" + bookItems.BOOKID + "----" + bookItems.bookFlag.toString() + "---> T4_1");
                                }
                            }
                            //endregion
                        }
                    }
                }
            }
            book_store_item_adapterRecycler.notifyDataSetChanged();
            if(count <=0){
                //NoDataFound.setVisibility(View.VISIBLE);
                //((TextView)NoDataFound.findViewById(R.id.title)).setText("No Data Found");
                //((TextView)NoDataFound.findViewById(R.id.titleDataNotFound)).setText("Please use other query string to find books \" \ndon't forge to use filter\"");

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
                    ((TextView)NoDataFound.findViewById(R.id.titleDataNotFound)).setText("Please use other query string to find books \" \ndon't forge to use filter\"");
                }
            }
            else{
                NoDataFound.setVisibility(View.INVISIBLE);
            }
        }
        catch (Exception ex){
            Log.e(TAG, ex.toString());
        }
    }

    public void SearchButton(View view){
        SearchQuery(searchView.getQuery().toString().trim());
    }
}

class Book_Store_Item_AdapterRecycler extends RecyclerView.Adapter<Book_Store_Item_AdapterRecycler.Holder>{
    private static final String TAG = "Book_Store_Item_Adapter";
    private Context context;
    private List<BookItems> bookItemsList;

    public Book_Store_Item_AdapterRecycler(Context context, List<BookItems> bookItemsList) {
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
    public void onBindViewHolder(@NonNull final Holder holder, int position) {
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

        //region Set Author Name
        if (holder.tAuthor != null) {
            String authorData = "by "+ bookItemsList.get(position).Author;
            holder.tAuthor.setText(authorData);
        }
        //endregion

        //region Adding TAG
        if(holder.mTagGroup != null) {
            UpdateBookFlag(holder.getAdapterPosition(), holder);
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
        TextView tName, tAuthor, tViews, tComments, tDescription, tLikes;
        View view;
        TagGroup mTagGroup;
        public Holder(View view) {
            super(view);
            tImage = view.findViewById(R.id.recyclerItemImage);
            tName = view.findViewById(R.id.recyclerItemName);
            tAuthor = view.findViewById(R.id.author);
            tViews = view.findViewById(R.id.recyclerItemViews);
            tComments = view.findViewById(R.id.recyclerItemComments);
            tDescription = view.findViewById(R.id.recyclerItemDescription);
            tLikes = view.findViewById(R.id.recyclerItemLikes);
            mTagGroup = view.findViewById(R.id.tag_group);
        }
    }
}

class SpinnerArrayAdapter extends BaseAdapter {
    private Context context;
    private ArrayList<String> list;

    public SpinnerArrayAdapter(Context context, ArrayList<String> list) {
        this.context = context;
        this.list = list;
    }

    @Override
    public int getCount() {
        return list.size();
    }

    @Override
    public Object getItem(int position) {
        return list.get(position);
    }

    @Override
    public long getItemId(int position) {
        return (long) position;
    }

    @Override
    public View getDropDownView(int position, View convertView, ViewGroup parent) {
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);
        params.setMargins(10,10,10,10);

        TextView txt = new TextView(context);
        txt.setLayoutParams(params);
        txt.setPadding(40, 40, 40, 40);
        txt.setTextSize(16);
        txt.setGravity(Gravity.CENTER_VERTICAL);
        txt.setText(list.get(position));
        txt.setTextColor(Color.parseColor("#000000"));
        return  txt;
    }

    public View getView(int position, View view, ViewGroup viewgroup) {
        TextView txt = new TextView(context);
        txt.setTextSize(14);
        txt.setSingleLine(true);
        txt.setGravity(Gravity.CENTER_VERTICAL);
        txt.setText(list.get(position));
        txt.setTextColor(Color.parseColor("#000000"));
        return  txt;
    }
}
