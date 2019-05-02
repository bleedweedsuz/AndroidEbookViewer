package zkagazapahtnajusz.paperproject.com.paperproject.Fragments;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import zkagazapahtnajusz.paperproject.com.paperproject.Activity_BookDetails;
import zkagazapahtnajusz.paperproject.com.paperproject.Model.BookItems;
import zkagazapahtnajusz.paperproject.com.paperproject.R;
import zkagazapahtnajusz.paperproject.com.paperproject.Utilities.FirebaseUtils;
import zkagazapahtnajusz.paperproject.com.paperproject.Utilities.Utils;

/**
 * Created by Zw4R-TSTUD10 2018/4/1 [2018, April 1]
 */

public class Fragment_UserProfile_Purchase extends Fragment implements SwipeRefreshLayout.OnRefreshListener{
    private static final String TAG = "Fragment_UserProfile_Pu";
    FirebaseFirestore firebaseFirestore;
    List<BookItemWrapper> purchasedItems = new ArrayList<>();
    RecyclerView myRecyclerView;
    RecyclerAdapterFragmentUserProfilePurchase  recyclerAdapter;
    SwipeRefreshLayout swipeRefreshLayout;
    DocumentSnapshot lastVisible;
    LinearLayoutManager layoutManager;
    private Boolean isFirstPageFirstLoad = false;
    private boolean loading = true;
    int pastVisiblesItems, visibleItemCount, totalItemCount;
    int queryLimit;
    public String UserID = null;
    public boolean isFriendProfile = false;
    View NoDataFound;
    ListenerRegistration listenerRegistration_firstPage, listenerRegistration_loadMore;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        Utils.DEVICE_CHECK(getActivity());
        queryLimit = Utils.CURRENT_GRID_COUNT;

        View view = inflater.inflate(R.layout.fragment_userprofile_purchase,container,false);
        myRecyclerView = view.findViewById(R.id.recyclerView);
        recyclerAdapter = new RecyclerAdapterFragmentUserProfilePurchase(getActivity(), purchasedItems, isFriendProfile);
        layoutManager = new LinearLayoutManager(getActivity());

        NoDataFound = view.findViewById(R.id.NoDataFoundView);

        myRecyclerView.setLayoutManager(layoutManager);

        myRecyclerView.setAdapter(recyclerAdapter);

        swipeRefreshLayout = view.findViewById(R.id.swipeRefresh);
        swipeRefreshLayout.setOnRefreshListener(this);

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

                    if (loading && ((visibleItemCount + pastVisiblesItems) >= totalItemCount))
                    {
                        loading = false;
                        LoadMore();
                    }
                }
            }
        });
        if(UserID == null){
            UserID = FirebaseUtils.firebaseAuth.getCurrentUser().getUid();
        }
        LoadPortalAndPages();
        return view;
    }

    public void LoadPortalAndPages(){
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
            String UID = UserID;
            String Root = getString(R.string.FIRESTORE_ROOT_USERDETAILS) + "/" + UID + "/" +getString(R.string.FIRESTORE_ROOT_LIBRARY);
            Query query;
            if(isFriendProfile){
                query = FirebaseFirestore.getInstance().collection(Root).whereEqualTo(getString(R.string.F_USERDETAILS_LIBRARY_PRIVACY), "public").orderBy(getString(R.string.F_USERDETAILS_LIBRARY_DATE), Query.Direction.DESCENDING).limit(queryLimit);
            }
            else{
                query = FirebaseFirestore.getInstance().collection(Root).orderBy(getString(R.string.F_USERDETAILS_LIBRARY_DATE), Query.Direction.DESCENDING).limit(queryLimit);
            }
            listenerRegistration_firstPage = FirebaseUtils.FirestoreGetAllData(query, getActivity(), new EventListener<QuerySnapshot>() {
                @Override
                public void onEvent(QuerySnapshot queryDocumentSnapshots, FirebaseFirestoreException e) {
                    if(queryDocumentSnapshots !=null  && !queryDocumentSnapshots.isEmpty()){
                        NoDataFound.setVisibility(View.INVISIBLE);
                        if(!isFirstPageFirstLoad){
                            lastVisible = queryDocumentSnapshots.getDocuments().get(queryDocumentSnapshots.size() -1);
                        }
                        for(DocumentChange doc : queryDocumentSnapshots.getDocumentChanges()) {
                            if(doc.getType() == DocumentChange.Type.ADDED) {
                                try{
                                    //Search in server
                                    final String LState = doc.getDocument().getString(getString(R.string.F_USERDETAILS_LIBRARY_STATE));
                                    final String privacy = doc.getDocument().getString(getString(R.string.F_USERDETAILS_LIBRARY_PRIVACY));

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
                                                BookItemWrapper bItemWrappter = new BookItemWrapper(bookItems, Activity_BookDetails.LibraryState.valueOf(LState), privacy, BOOKID);
                                                purchasedItems.add(bItemWrappter);
                                                recyclerAdapter.notifyItemInserted(purchasedItems.size() -1);
                                            }
                                        }
                                    }, new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            //Data not found in server
                                            Log.e(TAG, e.toString());
                                        }
                                    });
                                }
                                catch (Exception ex){
                                    Log.e(TAG, ex.toString());
                                }
                            }
                        }
                        isFirstPageFirstLoad = true;
                    }
                    else{
                        if(e != null){Log.e(TAG, e.toString());}
                        NoDataFound.setVisibility(View.VISIBLE);
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
            String UID = UserID;
            String Root = getString(R.string.FIRESTORE_ROOT_USERDETAILS) + "/" + UID + "/" +getString(R.string.FIRESTORE_ROOT_LIBRARY);
            Query query;
            if(isFriendProfile){
                query = FirebaseFirestore.getInstance().collection(Root).whereEqualTo(getString(R.string.F_USERDETAILS_LIBRARY_PRIVACY), "public").orderBy(getString(R.string.F_USERDETAILS_LIBRARY_DATE), Query.Direction.DESCENDING).startAfter(lastVisible).limit(queryLimit);
            }
            else{
                query = FirebaseFirestore.getInstance().collection(Root).orderBy(getString(R.string.F_USERDETAILS_LIBRARY_DATE), Query.Direction.DESCENDING).startAfter(lastVisible).limit(queryLimit);
            }

            listenerRegistration_loadMore = FirebaseUtils.FirestoreGetAllData(query, getActivity(), new EventListener<QuerySnapshot>() {
                @Override
                public void onEvent(QuerySnapshot queryDocumentSnapshots, FirebaseFirestoreException e) {
                    if(queryDocumentSnapshots !=null  && !queryDocumentSnapshots.isEmpty()){
                        lastVisible = queryDocumentSnapshots.getDocuments().get(queryDocumentSnapshots.size() -1);
                        for(DocumentChange doc : queryDocumentSnapshots.getDocumentChanges()) {
                            if(doc.getType() == DocumentChange.Type.ADDED) {
                                try{
                                    //region Search in server
                                    final String LState = doc.getDocument().getString(getString(R.string.F_USERDETAILS_LIBRARY_STATE));
                                    final String privacy = doc.getDocument().getString(getString(R.string.F_USERDETAILS_LIBRARY_PRIVACY));
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
                                                BookItemWrapper bItemWrappter = new BookItemWrapper(bookItems, Activity_BookDetails.LibraryState.valueOf(LState), privacy, BOOKID);
                                                purchasedItems.add(bItemWrappter);
                                                recyclerAdapter.notifyItemChanged(purchasedItems.size() -1);
                                            }
                                        }
                                    }, new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            //Data not found in server
                                            Log.e(TAG, e.toString());
                                        }
                                    });
                                    //endregion
                                }
                                catch (Exception ex){
                                    Log.e(TAG, ex.toString());
                                }
                            }
                        }
                        loading = true;
                        listenerRegistration_loadMore.remove();
                        listenerRegistration_loadMore = null;
                    }
                    else{
                        if(e != null){Log.e(TAG, e.toString());}
                    }
                }
            });
        }
        catch (Exception ex){
            swipeRefreshLayout.setRefreshing(false);
            Log.e(TAG, ex.toString());
        }
    }

    @Override
    public void onRefresh() {
        purchasedItems.clear();
        isFirstPageFirstLoad = false;
        loading = true;
        if(listenerRegistration_loadMore != null){listenerRegistration_loadMore.remove(); listenerRegistration_loadMore = null;}
        if(listenerRegistration_firstPage != null){listenerRegistration_firstPage.remove(); listenerRegistration_firstPage =null;}
        FirstPage();
    }

    public class BookItemWrapper{
        private BookItems bookItems;
        private Activity_BookDetails.LibraryState libraryState;
        private String privacy;
        private String bookID;

        BookItemWrapper(BookItems bookItems, Activity_BookDetails.LibraryState libraryState, String privacy, String bookID) {
            this.bookItems = bookItems;
            this.libraryState = libraryState;
            this.privacy = privacy;
            this.bookID = bookID;
        }

        BookItems getBookItems() {
            return bookItems;
        }

        public void setBookItems(BookItems bookItems) {
            this.bookItems = bookItems;
        }

        Activity_BookDetails.LibraryState getLibraryState() {
            return libraryState;
        }

        public void setLibraryState(Activity_BookDetails.LibraryState libraryState) {
            this.libraryState = libraryState;
        }

        public String getPrivacy() {
            return privacy;
        }

        public void setPrivacy(String privacy) {
            this.privacy = privacy;
        }

        String getBookID() {
            return bookID;
        }

        public void setBookID(String bookID) {
            this.bookID = bookID;
        }
    }
}

class RecyclerAdapterFragmentUserProfilePurchase extends RecyclerView.Adapter<RecyclerAdapterFragmentUserProfilePurchase.HolderFragmentPurchase>{
    private static final String TAG = "RecyclerAdapterFragment";
    private Context mContext;
    private List<Fragment_UserProfile_Purchase.BookItemWrapper> purchasedItems;
    private boolean isFriendProfile;

    RecyclerAdapterFragmentUserProfilePurchase(Context mContext, List<Fragment_UserProfile_Purchase.BookItemWrapper> purchasedItems, boolean isFriendProfile) {
        this.mContext = mContext;
        this.purchasedItems = purchasedItems;
        this.isFriendProfile = isFriendProfile;
    }

    @NonNull
    @Override
    public HolderFragmentPurchase onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.recycler_userprofile_purchase, parent, false);
        return new HolderFragmentPurchase(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final HolderFragmentPurchase holder, int position) {
        if(purchasedItems.get(position).getLibraryState() == Activity_BookDetails.LibraryState.DELETE){
            holder.MainView.setBackgroundColor(mContext.getResources().getColor(R.color.colorLibraryMenuDelete));
            //region DELETE
            holder.textOptionMenu.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    PopupMenu popupMenu = new PopupMenu(mContext, holder.textOptionMenu);
                    popupMenu.inflate(R.menu.post_menu_2);
                    popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                        @Override
                        public boolean onMenuItemClick(MenuItem item) {
                            switch (item.getItemId()){
                                case R.id.menuPostDelete:
                                    //region PDelete
                                    new AlertDialog.Builder(mContext).
                                            setTitle("Delete").
                                            setMessage("Are you sure want to permanently delete this from library? \n(*note you can't re-download this again)").
                                            setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialog, int which) {
                                                    DeleteBookFromLibraryPermanently(purchasedItems.get(holder.getAdapterPosition()).getBookItems().BOOKID);
                                                    Utils.LocalFileRemove(purchasedItems.get(holder.getAdapterPosition()).getBookItems().BOOKID);
                                                    purchasedItems.remove(holder.getAdapterPosition());
                                                    notifyItemRemoved(holder.getAdapterPosition());
                                                    //notifyDataSetChanged();
                                                }
                                            }).
                                            setNegativeButton("No", null).
                                            create().show();
                                    //endregion
                                    break;
                                case R.id.details:
                                    GoToBookDetails(holder);
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
        else{
            holder.MainView.setBackgroundColor(Color.WHITE);
            //region ADDED
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                }
            });
            holder.textOptionMenu.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    PopupMenu popupMenu = new PopupMenu(mContext,holder.textOptionMenu);
                    if(isFriendProfile){popupMenu.inflate(R.menu.post_menu_friends);}
                    else{popupMenu.inflate(R.menu.post_menu);}

                    popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                        @Override
                        public boolean onMenuItemClick(MenuItem item) {
                            switch (item.getItemId()){
                                case R.id.details:
                                    GoToBookDetails(holder);
                                    break;
                                case R.id.privacy:
                                    SetPrivacy(holder.privacyText, holder.privacyIcon, purchasedItems.get(holder.getAdapterPosition()).getBookID());
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

        if (holder.tImage != null) {
            ColorDrawable colorDrawableInfoPlaceholder = new ColorDrawable(ContextCompat.getColor(mContext, R.color.colorDrawableInfoPlaceholder));
            ColorDrawable colorDrawableErrorPlaceholder = new ColorDrawable(ContextCompat.getColor(mContext, R.color.colorDrawableErrorPlaceholder));
            Utils.SetImageViaUri((Activity) mContext,purchasedItems.get(position).getBookItems().imageURL,holder.tImage,false,false,colorDrawableInfoPlaceholder,colorDrawableErrorPlaceholder,null);
        }

        if(holder.tName != null){
            holder.tName.setText(purchasedItems.get(position).getBookItems().Name);
        }

        if(holder.tDescription != null){
            holder.tDescription.setText(purchasedItems.get(position).getBookItems().Description);
        }

        if(holder.privacyText != null && holder.privacyIcon != null){
            try {
                String privacyStr = purchasedItems.get(holder.getAdapterPosition()).getPrivacy();
                holder.privacyText.setText(privacyStr);
                if (privacyStr.equals("private")) {
                    holder.privacyIcon.setImageResource(R.drawable.icon_privacy);
                } else if (privacyStr.equals("public")) {
                    holder.privacyIcon.setImageResource(R.drawable.icon_public);
                }
            }
            catch (Exception ex){
                Log.e(TAG, ex.toString());
            }
        }

        holder.MainView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                GoToBookDetails(holder);
            }
        });
    }

    private void DeleteBookFromLibraryPermanently(String BookID){
        try{
            Utils.SetProgressDialogIndeterminate(mContext, "deleting");
            String UID = FirebaseUtils.firebaseAuth.getCurrentUser().getUid();
            String Root = mContext.getString(R.string.FIRESTORE_ROOT_USERDETAILS) + "/" + UID + "/" + mContext.getString(R.string.FIRESTORE_ROOT_LIBRARY) + "/" + BookID;
            FirebaseUtils.FirestoreDeleteData(Root, new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void aVoid) {
                    Utils.UnSetProgressDialogIndeterminate();
                    Log.i(TAG, "P Delete From Library");
                }
            }, new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Utils.UnSetProgressDialogIndeterminate();
                    Log.e(TAG, e.toString());
                }
            });
        }
        catch (Exception ex){
            Utils.UnSetProgressDialogIndeterminate();
            Log.e(TAG, ex.toString());
        }
    }

    private void GoToBookDetails(HolderFragmentPurchase holder){
        try {

            Intent intent = new Intent(mContext, Activity_BookDetails.class);
            intent.putExtra("BookID", purchasedItems.get(holder.getAdapterPosition()).getBookItems().BOOKID);
            mContext.startActivity(intent);

        }
        catch (Exception ex){
            Log.e("ERROR", ex.toString());
        }
    }

    private void SetPrivacy(final TextView privacyText, final ImageView privacyIcon, final String bookID){
        final CharSequence[] privacyOptions = {"public", "private"};
        final String currentPrivacyOptionString = privacyText.getText().toString().trim();

        Utils.SetSingleChoiceInputDialogBox((Activity) mContext, "Select Privacy", privacyOptions, currentPrivacyOptionString, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Utils.SetProgressDialogIndeterminateWithOutCancel(mContext, "updating..");
                //region privacy update
                String privacyStr = privacyOptions[which].toString();
                privacyText.setText(privacyStr);
                if(privacyStr.equals("private")){
                    privacyIcon.setImageResource(R.drawable.icon_privacy);
                }
                else if(privacyStr.equals("public")){
                    privacyIcon.setImageResource(R.drawable.icon_public);
                }
                try {
                    //Update Privacy
                    {
                        String UID = FirebaseUtils.firebaseAuth.getCurrentUser().getUid();
                        String Root = mContext.getString(R.string.FIRESTORE_ROOT_USERDETAILS) + "/" + UID + "/" + mContext.getString(R.string.FIRESTORE_ROOT_LIBRARY) + "/" + bookID;
                        Map<String, Object> map = new HashMap<>();
                        map.put(mContext.getString(R.string.F_USERDETAILS_LIBRARY_PRIVACY), privacyOptions[which].toString());
                        OnSuccessListener<Void> onSuccessListener = new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                Log.e(TAG, "Privacy Updated");
                                Utils.UnSetProgressDialogIndeterminate();
                            }
                        };
                        OnFailureListener onFailureListener = new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Utils.UnSetProgressDialogIndeterminate();
                                Log.e(TAG, e.toString());
                            }
                        };
                        FirebaseUtils.FirestroreSetData(Root, map, onSuccessListener, onFailureListener);
                    }
                } catch (Exception ex) {
                    Utils.UnSetProgressDialogIndeterminate();
                    Log.e(TAG, ex.toString());
                }
                //endregion
                dialog.dismiss();
            }
        });
    }
    @Override
    public int getItemCount() {
        return purchasedItems.size();
    }

    class HolderFragmentPurchase extends RecyclerView.ViewHolder{
        RelativeLayout MainView;
        ImageView tImage, privacyIcon;
        TextView tName, tDescription ,textOptionMenu, privacyText;

        HolderFragmentPurchase(View v){
            super(v);
            MainView = v.findViewById(R.id.MainView);
            tImage = v.findViewById(R.id.recyclerItemImage);
            tName = v.findViewById(R.id.recyclerItemName);
            tDescription = v.findViewById(R.id.recyclerItemDescription);
            textOptionMenu = v.findViewById(R.id.optionMenu);
            privacyText = v.findViewById(R.id.privacyText);
            privacyIcon = v.findViewById(R.id.privacyIcon);
        }
    }
}
