package zkagazapahtnajusz.paperproject.com.paperproject.Fragments;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.functions.HttpsCallableResult;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import zkagazapahtnajusz.paperproject.com.paperproject.Activity_Friend_Profile;
import zkagazapahtnajusz.paperproject.com.paperproject.R;
import zkagazapahtnajusz.paperproject.com.paperproject.Utilities.FirebaseUtils;
import zkagazapahtnajusz.paperproject.com.paperproject.Utilities.Utils;

/**
 * Created by Zw4R-TSTUD10 2018/4/1 [2018, April 1]
 */

public class Fragment_UserProfile_Request extends Fragment implements SwipeRefreshLayout.OnRefreshListener {
    String TAG = "Fragment UserRequest Activity";
    FirebaseFirestore firebaseFirestore;
    RecyclerView myRecyclerView;
    AdapterRecycler recyclerAdapter;
    LinearLayoutManager layoutManager;
    SwipeRefreshLayout swipeRefreshLayout;
    List<RequestItem> requestItems = new ArrayList<>();
    Boolean isFirstPageFirstLoad = false;
    DocumentSnapshot lastVisible;
    int queryLimit;
    View NoDataFoundView;
    boolean loading = true;
    int pastVisiblesItems, visibleItemCount, totalItemCount;
    ListenerRegistration listenerRegistration_firstPage, listenerRegistration_loadMore;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        Utils.DEVICE_CHECK(getActivity());//DEVICE CHECK
        queryLimit = Utils.CURRENT_GRID_COUNT;
        View view = inflater.inflate(R.layout.fragment_userprofile_request, container,false);
        myRecyclerView = view.findViewById(R.id.recyclerView);
        swipeRefreshLayout = view.findViewById(R.id.swipeRefresh);
        NoDataFoundView = view.findViewById(R.id.NoDataFoundView);

        recyclerAdapter = new AdapterRecycler(getActivity(), requestItems);
        layoutManager = new LinearLayoutManager(getActivity());
        myRecyclerView.setLayoutManager(layoutManager);
        myRecyclerView.setAdapter(recyclerAdapter);
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
            NoDataFoundView.setVisibility(View.GONE);
            String UID = FirebaseUtils.firebaseAuth.getCurrentUser().getUid();
            String Root = getString(R.string.FIRESTORE_ROOT_USERDETAILS) + "/" + UID + "/" +getString(R.string.FIRESTORE_ROOT_FOLLOWER);
            Query query = FirebaseFirestore.getInstance().collection(Root).whereEqualTo(getString(R.string.F_USERDETAILS_FOLLOWER_STATE),Activity_Friend_Profile.FollowState.REQUEST.toString()).limit(queryLimit);
            listenerRegistration_firstPage = FirebaseUtils.FirestoreGetAllData(query, getActivity(), new EventListener<QuerySnapshot>() {
                @Override
                public void onEvent(QuerySnapshot queryDocumentSnapshots, FirebaseFirestoreException e) {
                    if(queryDocumentSnapshots !=null  && !queryDocumentSnapshots.isEmpty()){
                        if(!isFirstPageFirstLoad){
                            lastVisible = queryDocumentSnapshots.getDocuments().get(queryDocumentSnapshots.size() -1);
                        }
                        for(DocumentChange doc : queryDocumentSnapshots.getDocumentChanges()) {

                            //region Added
                            if(doc.getType() == DocumentChange.Type.ADDED) {
                                try{
                                    String FID = doc.getDocument().getId();
                                    String UID = doc.getDocument().getString(getString(R.string.F_USERDETAILS_FOLLOWER_UID));
                                    RequestItem requestItem = new RequestItem(FID, UID);

                                    if(!isFirstPageFirstLoad){
                                        requestItems.add(requestItem);
                                        recyclerAdapter.notifyItemChanged(requestItems.size() -1);
                                    }
                                    else{
                                        requestItems.add(0, requestItem);
                                        recyclerAdapter.notifyItemChanged(0);
                                    }
                                    NoDataFoundView.setVisibility(View.GONE);
                                }
                                catch (Exception ex){
                                    Log.e(TAG, ex.toString());
                                }
                            }
                            //endregion

                            //region Removed
                            else if(doc.getType() == DocumentChange.Type.REMOVED){
                                String UID = doc.getDocument().getString(getString(R.string.F_USERDETAILS_FOLLOWER_UID));
                                for(int i=0;i<requestItems.size();i++){
                                    if(requestItems.get(i).getUid().equals(UID)){
                                        requestItems.remove(i);
                                        recyclerAdapter.notifyItemRemoved(i);
                                        break;
                                    }
                                }
                            }
                            //endregion
                        }
                        isFirstPageFirstLoad = true;
                    }
                    else{
                        if(requestItems.size() <=1){requestItems.clear();recyclerAdapter.notifyDataSetChanged();}NoDataFoundView.setVisibility(View.VISIBLE);
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
            String UID = FirebaseUtils.firebaseAuth.getCurrentUser().getUid();
            String Root = getString(R.string.FIRESTORE_ROOT_USERDETAILS) + "/" + UID + "/" +getString(R.string.FIRESTORE_ROOT_FOLLOWER);
            Query query = FirebaseFirestore.getInstance().collection(Root).whereEqualTo(getString(R.string.F_USERDETAILS_FOLLOWER_STATE),Activity_Friend_Profile.FollowState.REQUEST.toString()).startAfter(lastVisible).limit(queryLimit);
            listenerRegistration_loadMore = FirebaseUtils.FirestoreGetAllData(query, getActivity(), new EventListener<QuerySnapshot>() {
                @Override
                public void onEvent(QuerySnapshot queryDocumentSnapshots, FirebaseFirestoreException e) {
                    if(queryDocumentSnapshots !=null  && !queryDocumentSnapshots.isEmpty()){
                        lastVisible = queryDocumentSnapshots.getDocuments().get(queryDocumentSnapshots.size() -1);
                        for(DocumentChange doc : queryDocumentSnapshots.getDocumentChanges()) {
                            if(doc.getType() == DocumentChange.Type.ADDED) {
                                try{
                                    String FID = doc.getDocument().getId();
                                    String UID = doc.getDocument().getString(getString(R.string.F_USERDETAILS_FOLLOWER_UID));
                                    RequestItem requestItem = new RequestItem(FID, UID);
                                    requestItems.add(requestItem);
                                    recyclerAdapter.notifyItemChanged(requestItems.size() -1);
                                }
                                catch (Exception ex){
                                    Log.e(TAG, ex.toString());
                                }
                            }
                        }
                    }
                    else{
                        Log.e(TAG, e.toString());
                    }
                    listenerRegistration_loadMore.remove();
                    listenerRegistration_loadMore = null;
                }
            });
        }
        catch (Exception ex){
            Log.e(TAG, ex.toString());
        }
    }

    @Override
    public void onRefresh() {
        loading = true;
        isFirstPageFirstLoad = false;
        requestItems.clear();
        if(listenerRegistration_firstPage != null){listenerRegistration_firstPage.remove();listenerRegistration_firstPage = null;}
        if(listenerRegistration_loadMore !=null){listenerRegistration_loadMore.remove();listenerRegistration_loadMore = null;}
        FirstPage();
    }
}

class RequestItem {
    private String fid, uid;

    RequestItem(String fid, String uid) {
        this.fid = fid;
        this.uid = uid;
    }

    String getFid() {
        return fid;
    }

    public void setFid(String fid) {
        this.fid = fid;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }
}

class  AdapterRecycler extends RecyclerView.Adapter<AdapterRecycler.holder>{
    private String TAG = "ADAPTER RECYCLER";
    private Activity activity;
    private List<RequestItem> requestItems;

    AdapterRecycler(Activity activity, List<RequestItem> requestItems) {
        this.activity = activity;
        this.requestItems = requestItems;
    }

    @NonNull
    @Override
    public holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(activity.getBaseContext()).inflate(R.layout.layout_request_item, parent,false);
        return new holder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final holder holder, final int position) {
        //region GET FULL NAME AND PROFILE FROM FIRE STORE DATABASE
        String Root = activity.getString(R.string.FIRESTORE_ROOT_USERDETAILS) + "/" + requestItems.get(position).getUid();
        FirebaseUtils.FirestoreGetData(Root, new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                if(documentSnapshot.exists()){
                    String FullName = ""; try{ FullName = documentSnapshot.getString(activity.getString(R.string.F_USERDETAILS_USERFULLNAME)); } catch (Exception ex){Log.e(TAG, ex.toString());}
                    String ProfileUrl = ""; try{ ProfileUrl = documentSnapshot.getString(activity.getString(R.string.F_USERDETAILS_PROFILEURL)); } catch (Exception ex){Log.e(TAG, ex.toString());}
                    holder.UserFullName.setText(FullName);
                    ColorDrawable colorDrawableInfoPlaceholder = new ColorDrawable(ContextCompat.getColor(activity, R.color.colorDrawableInfoPlaceholder));
                    ColorDrawable colorDrawableErrorPlaceholder = new ColorDrawable(ContextCompat.getColor(activity, R.color.colorDrawableErrorPlaceholder));
                    Utils.SetImageViaUri(
                            activity,
                            ProfileUrl,
                            holder.UserImage,
                            false,
                            false,
                            colorDrawableInfoPlaceholder,
                            colorDrawableErrorPlaceholder,
                            null
                    );
                }
            }
        }, null);
        //endregion

        //region Accept Button
        holder.AcceptBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Utils.SetProgressDialogIndeterminate(activity, "loading");
                final String FOLLOW_ID = requestItems.get(position).getFid(), FRIEND_ID = requestItems.get(position).getUid(), MY_ID = FirebaseUtils.firebaseAuth.getCurrentUser().getUid();

                {//Accept in follower
                    String Root = activity.getString(R.string.FIRESTORE_ROOT_USERDETAILS) + "/" + MY_ID + "/" + activity.getString(R.string.FIRESTORE_ROOT_FOLLOWER) + "/" + FOLLOW_ID;
                    Map<String , Object> map = new HashMap<>();
                    map.put(activity.getString(R.string.F_USERDETAILS_FOLLOWER_STATE), Activity_Friend_Profile.FollowState.ACCEPT.toString());
                    FirebaseUtils.FirestroreSetData(Root, map, new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            Utils.UnSetProgressDialogIndeterminate();
                        }
                    }, null);
                }

                {//Accept in Following
                    String Root = activity.getString(R.string.FIRESTORE_ROOT_USERDETAILS) + "/" + FRIEND_ID + "/" + activity.getString(R.string.FIRESTORE_ROOT_FOLLOWING) + "/" + FOLLOW_ID;
                    Map<String , Object> map = new HashMap<>();
                    map.put(activity.getString(R.string.F_USERDETAILS_FOLLOWING_UID), MY_ID);
                    map.put(activity.getString(R.string.F_USERDETAILS_FOLLOWING_STATE), Activity_Friend_Profile.FollowState.ACCEPT.toString());
                    map.put(activity.getString(R.string.F_USERDETAILS_FOLLOWING_DATE), new Timestamp(Calendar.getInstance().getTime()));
                    map.put(activity.getString(R.string.F_USERDETAILS_FOLLOWING_ACHIEVE), 0);
                    FirebaseUtils.FirestroreSetData(Root, map,
                            new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    //Send Notification To User
                                    //region Notification
                                    Map<String, Object> map = new HashMap<>();
                                    map.put("notificationtype", "followreplied");
                                    map.put("tuid", MY_ID);
                                    map.put("uid", FRIEND_ID);
                                    map.put("followid", FOLLOW_ID);
                                    FirebaseUtils.RunServerFunction("SendNotification", map,
                                            new Continuation<HttpsCallableResult, String>() {
                                                @Override
                                                public String then(@NonNull Task<HttpsCallableResult> task){
                                                    return String.valueOf(task.getResult().getData());
                                                }
                                            }, new OnFailureListener() {
                                                @Override
                                                public void onFailure(@NonNull Exception e) {
                                                    Log.e("ERROR", e.toString());
                                                }
                                            });

                                    Log.i("---------->", "FOLLOW REPLIED");
                                    //endregion
                                    Utils.UnSetProgressDialogIndeterminate();
                                }
                            }, null);
                }
                requestItems.remove(position);
                notifyDataSetChanged();
            }
        });
        //endregion

        //region Decline Button
        holder.DeclineBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new AlertDialog.Builder(activity).setMessage("Are you sure want to decline this request?").setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Utils.SetProgressDialogIndeterminate(activity, "declining request");
                        {//Delete in follower
                            String FOLLOW_ID = requestItems.get(position).getFid(), MY_ID = FirebaseUtils.firebaseAuth.getCurrentUser().getUid();
                            String Root = activity.getString(R.string.FIRESTORE_ROOT_USERDETAILS) + "/" + MY_ID + "/" + activity.getString(R.string.FIRESTORE_ROOT_FOLLOWER) + "/" + FOLLOW_ID;
                            FirebaseUtils.FirestoreDeleteData(Root, new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    Utils.UnSetProgressDialogIndeterminate();
                                }
                            }, new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Utils.UnSetProgressDialogIndeterminate();
                                }
                            });

                        }
                        requestItems.remove(position);
                        notifyDataSetChanged();
                    }
                }).setNegativeButton("No", null).create().show();
            }
        });
        //endregion
    }

    @Override
    public int getItemCount() {
        return requestItems.size();
    }

    class holder extends RecyclerView.ViewHolder{
        TextView UserFullName;
        View AcceptBtn, DeclineBtn;
        ImageView UserImage;
        public holder(View v){
            super(v);
            UserFullName = v.findViewById(R.id.UserFullName);
            AcceptBtn = v.findViewById(R.id.Accept);
            DeclineBtn = v.findViewById(R.id.Decline);
            UserImage = v.findViewById(R.id.userimage);
        }
    }
}
