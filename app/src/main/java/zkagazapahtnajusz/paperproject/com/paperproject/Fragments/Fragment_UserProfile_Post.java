package zkagazapahtnajusz.paperproject.com.paperproject.Fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import zkagazapahtnajusz.paperproject.com.paperproject.Model.Post;
import zkagazapahtnajusz.paperproject.com.paperproject.Model.PostBuffer;
import zkagazapahtnajusz.paperproject.com.paperproject.R;
import zkagazapahtnajusz.paperproject.com.paperproject.Utilities.FirebaseUtils;
import zkagazapahtnajusz.paperproject.com.paperproject.Utilities.NetworkManagerUtils;
import zkagazapahtnajusz.paperproject.com.paperproject.Utilities.Utils;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Zw4R-TSTUD10 2018/4/1 [2018, April 1]
 */

public class Fragment_UserProfile_Post extends Fragment implements SwipeRefreshLayout.OnRefreshListener {
    String TAG = "Fragment UserProfile Activity";
    FirebaseFirestore firebaseFirestore;
    RecyclerView myRecyclerView;
    public List<PostBuffer> postBuffers = new ArrayList<>();
    ActivityAdapter activityAdapter;
    SwipeRefreshLayout swipeRefreshLayout;

    int queryLimit;
    //Scroller
    DocumentSnapshot lastVisible;
    LinearLayoutManager layoutManager;
    private Boolean isFirstPageFirstLoad = false;
    private boolean loading = true;
    int pastVisiblesItems, visibleItemCount, totalItemCount;
    View NoDataFound;
    ProgressBar progressBar;
    public String UserID = null;
    ListenerRegistration listenerRegistration_firstPage, listenerRegistration_loadMore;
    public static boolean needToReload = false;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        Utils.DEVICE_CHECK(getActivity());//DEVICE CHECK
        queryLimit = Utils.CURRENT_GRID_COUNT;
        View view = inflater.inflate(R.layout.fragment_userprofile_activity,container,false);

        myRecyclerView = view.findViewById(R.id.recyclerFragmentActivity);
        NoDataFound = view.findViewById(R.id.NoDataFoundView);
        progressBar = view.findViewById(R.id.progressBar);
        activityAdapter = new ActivityAdapter(this.getActivity(), postBuffers, myRecyclerView, ActivityAdapter.SkinFor.UserProfilePost);
        layoutManager = new LinearLayoutManager(getActivity());
        myRecyclerView.setLayoutManager(layoutManager);
        myRecyclerView.setAdapter(activityAdapter);
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

        if(UserID == null) {
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
            //region no internet connection show
            NetworkManagerUtils.LoadServerData(getContext());
            //endregion
            String UID = UserID;
            String Root = getString(R.string.FIRESTORE_ROOT_USERDETAILS) + "/" + UID + "/" + getString(R.string.FIRESTORE_ROOT_POSTS);
            Query firstQuery = firebaseFirestore.collection(Root).whereEqualTo(getString(R.string.F_POSTS_POST_UID), UID).orderBy(getString(R.string.F_POSTS_POST_DATE), Query.Direction.DESCENDING).limit(queryLimit);
            listenerRegistration_firstPage = firstQuery.addSnapshotListener(getActivity(), new EventListener<QuerySnapshot>() {
                @Override
                public void onEvent(QuerySnapshot queryDocumentSnapshots, FirebaseFirestoreException e) {
                    if(queryDocumentSnapshots !=null && !queryDocumentSnapshots.isEmpty()){
                        if(!isFirstPageFirstLoad){
                            lastVisible = queryDocumentSnapshots.getDocuments().get(queryDocumentSnapshots.size() -1);
                        }
                        for(DocumentChange doc: queryDocumentSnapshots.getDocumentChanges()){
                            switch (doc.getType()) {
                                case ADDED:
                                {
                                    NoDataFound.setVisibility(View.GONE);
                                    PostBuffer postBuffer = new PostBuffer(doc.getDocument().getString(getString(R.string.F_POSTS_POSTID)), Post.PostType.valueOf(doc.getDocument().getString(getString(R.string.F_POSTS_POST_TYPE))));
                                    if (!isFirstPageFirstLoad) {
                                        //Add To List from beginning
                                        postBuffers.add(postBuffer);
                                        activityAdapter.notifyItemInserted(postBuffers.size() - 1);
                                    } else {
                                        //Add To Second index i.e. => 1
                                        postBuffers.add(1, postBuffer);
                                        activityAdapter.notifyItemInserted(1);
                                    }
                                }
                                break;
                                case MODIFIED:
                                {
                                    Log.e(TAG, "FRAGMENT ACTIVITY MODIFIED...");
                                    //String postID = doc.getDocument().getString(getString(R.string.F_POSTS_POSTID));
                                    //activityAdapter.updateItemFromPostID(postID);
                                }
                                break;
                                case REMOVED:
                                {
                                    String postID = doc.getDocument().getString(getString(R.string.F_POSTS_POSTID));
                                    activityAdapter.removeItemFromPostID(postID);
                                }
                                break;
                            }
                        }
                        isFirstPageFirstLoad = true;
                    }
                    else{
                        if(postBuffers.size() >= 2){
                            postBuffers.clear();
                        }
                        isFirstPageFirstLoad = false;
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
            progressBar.setVisibility(View.VISIBLE);
            String UID = UserID;
            String Root = getString(R.string.FIRESTORE_ROOT_USERDETAILS) + "/" + UID + "/" + getString(R.string.FIRESTORE_ROOT_POSTS);
            Query firstQuery = firebaseFirestore.collection(Root).whereEqualTo(getString(R.string.F_POSTS_POST_UID), UID).orderBy(getString(R.string.F_POSTS_POST_DATE), Query.Direction.DESCENDING).startAfter(lastVisible).limit(queryLimit);
            listenerRegistration_loadMore = firstQuery.addSnapshotListener(getActivity(), new EventListener<QuerySnapshot>() {
                @Override
                public void onEvent(QuerySnapshot queryDocumentSnapshots, FirebaseFirestoreException e) {
                    if(!queryDocumentSnapshots.isEmpty()) {
                        lastVisible = queryDocumentSnapshots.getDocuments().get(queryDocumentSnapshots.size() -1);
                        for (DocumentChange doc : queryDocumentSnapshots.getDocumentChanges()) {
                            if(doc.getType() == DocumentChange.Type.ADDED) {
                                try {
                                    PostBuffer postBuffer = new PostBuffer(doc.getDocument().getString(getString(R.string.F_POSTS_POSTID)), Post.PostType.valueOf(doc.getDocument().getString(getString(R.string.F_POSTS_POST_TYPE))));
                                    postBuffers.add(postBuffer);
                                    activityAdapter.notifyItemChanged(postBuffers.size() - 1);
                                }
                                catch (Exception ex){
                                    Log.e(TAG, ex.toString());
                                }
                            }
                        }
                    }
                    else{
                        //Do Data Found
                        Log.e(TAG,"NO DATA FOUND -  MODE 1");
                    }
                    listenerRegistration_loadMore.remove();
                    listenerRegistration_loadMore = null;
                    progressBar.setVisibility(View.INVISIBLE);
                    loading = true;
                }
            });
        }
        catch (Exception ex){
            progressBar.setVisibility(View.INVISIBLE);
            Log.e(TAG, ex.toString());
        }
    }

    @Override
    public void onRefresh() {
        if(listenerRegistration_firstPage !=null){listenerRegistration_firstPage.remove(); listenerRegistration_firstPage=null;}
        if(listenerRegistration_loadMore !=null){listenerRegistration_loadMore.remove(); listenerRegistration_loadMore = null;}
        loading = true;
        isFirstPageFirstLoad = false;
        postBuffers.clear();

        activityAdapter = new ActivityAdapter(this.getActivity(), postBuffers, myRecyclerView, ActivityAdapter.SkinFor.UserProfilePost);
        layoutManager = new LinearLayoutManager(getActivity());
        myRecyclerView.setLayoutManager(layoutManager);
        myRecyclerView.setAdapter(activityAdapter);

        LoadPortalAndPages();
    }

    @Override
    public void onResume() {
        super.onResume();
        if(needToReload) {
            needToReload = false;
            onRefresh();
        }
    }
}