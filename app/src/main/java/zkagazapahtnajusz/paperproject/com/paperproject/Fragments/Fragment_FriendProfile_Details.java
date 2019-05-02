package zkagazapahtnajusz.paperproject.com.paperproject.Fragments;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentSnapshot;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import zkagazapahtnajusz.paperproject.com.paperproject.R;
import zkagazapahtnajusz.paperproject.com.paperproject.Utilities.FirebaseUtils;

public class Fragment_FriendProfile_Details extends Fragment implements SwipeRefreshLayout.OnRefreshListener {
    private static final String TAG = "Fragment_FriendProfile_";
    SwipeRefreshLayout refreshLayout;
    AdapterFriendProfileDetails adapter;
    List<FriendProfileDetails_Item> mList = new ArrayList<>();
    public String UserID = null;
    RecyclerView recyclerView;

    View NoDataFound;
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_friendprofile_details, container, false);
        refreshLayout = view.findViewById(R.id.swipeRefresh);
        recyclerView = view.findViewById(R.id.recyclerView);
        NoDataFound = view.findViewById(R.id.NoDataFoundView);
        refreshLayout.setOnRefreshListener(this);
        adapter = new AdapterFriendProfileDetails(this.getContext(), mList);
        recyclerView.setLayoutManager(new LinearLayoutManager(this.getContext()));
        recyclerView.setAdapter(adapter);
        loadUserDetails();
        return view;
    }

    @Override
    public void onRefresh() {
        mList.clear();
        loadUserDetails();
        refreshLayout.setRefreshing(false);
    }

    public void loadUserDetails(){
        try{
            String Root = getString(R.string.FIRESTORE_ROOT_USERDETAILS) + "/" + UserID ;
            FirebaseUtils.FirestoreGetData(Root,
                new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        if(documentSnapshot.exists()){
                            String FullName = "data not set"; try{ FullName = documentSnapshot.getString(getString(R.string.F_USERDETAILS_USERFULLNAME));} catch (Exception ex){ Log.e(TAG, "1" + ex.toString());}
                            String Email = "data not set"; try{ Email = documentSnapshot.getString(getString(R.string.F_USERDETAILS_EMAIl));} catch (Exception ex){ Log.e(TAG,"2" + ex.toString());}
                            String Phone = "data not set"; try{ Phone = documentSnapshot.getString(getString(R.string.F_USERDETAILS_PHONE));} catch (Exception ex){ Log.e(TAG, "3" +ex.toString());}
                            String Gender = "data not set"; try{ Gender = documentSnapshot.getString(getString(R.string.F_USERDETAILS_GENDER));} catch (Exception ex){ Log.e(TAG, "4" +ex.toString());}
                            String Birthday = "data not set";
                            try{
                                Birthday = documentSnapshot.get(getString(R.string.F_USERDETAILS_BIRTHDAY)).toString();
                                Birthday = new SimpleDateFormat("MMM dd, yyyy", Locale.US).format(new Date(Birthday));
                            } catch (Exception ex){
                                Log.e(TAG, "5" +ex.toString());}
                            String Bio = "data not set"; try{ Bio = documentSnapshot.getString(getString(R.string.F_USERDETAILS_BIO));} catch (Exception ex){ Log.e(TAG, "6" +ex.toString());}

                            final String f_temp = FullName;
                            final String f_email = Email;
                            final String f_phone = Phone;
                            final String f_gender = Gender;
                            final String f_birthday = Birthday;
                            final String f_bio = Bio;

                            String account = getString(R.string.FIRESTORE_ROOT_USERDETAILS) + "/" + UserID + "/" + getString(R.string.AccountPrivacySetting) + "/" + UserID;
                            FirebaseUtils.FirestoreGetData(account, new OnSuccessListener<DocumentSnapshot>() {
                                @Override
                                public void onSuccess(DocumentSnapshot documentSnapshot) {
                                    boolean accountprivacybio = false;
                                    boolean accountprivacybirthday = false;
                                    boolean accountprivacyemail = false;
                                    boolean accountprivacygender = false;
                                    boolean accountprivacyphone = false;

                                    if (documentSnapshot.exists()) {
                                        try {
                                            accountprivacybio = documentSnapshot.getBoolean(getString(R.string.AccountPrivacy_Bio));
                                        } catch (Exception ex) {
                                            Log.e(TAG, "7" +ex.toString());
                                        }
                                        try {
                                            accountprivacybirthday = documentSnapshot.getBoolean(getString(R.string.AccountPrivacy_Birthday));
                                        } catch (Exception ex) {
                                            Log.e(TAG, "8" +ex.toString());
                                        }
                                        try {
                                            accountprivacyemail = documentSnapshot.getBoolean(getString(R.string.AccountPrivacy_Email));
                                        } catch (Exception ex) {
                                            Log.e(TAG, "9" +ex.toString());
                                        }
                                        try {
                                            accountprivacygender = documentSnapshot.getBoolean(getString(R.string.AccountPrivacy_Gender));
                                        } catch (Exception ex) {
                                            Log.e(TAG, "10" +ex.toString());
                                        }
                                        try {
                                            accountprivacyphone = documentSnapshot.getBoolean(getString(R.string.AccountPrivacy_Phone));
                                        } catch (Exception ex) {
                                            Log.e(TAG,"11" +ex.toString());
                                        }
                                    }

                                    mList.add(new FriendProfileDetails_Item("Full Name", f_temp));

                                    if (accountprivacyemail) {
                                        mList.add(new FriendProfileDetails_Item("Email", f_email));
                                    } else {
                                        mList.add(new FriendProfileDetails_Item("Email", "this is set as private"));
                                    }

                                    if (accountprivacygender) {
                                        mList.add(new FriendProfileDetails_Item("Gender", f_gender));
                                    } else {
                                        mList.add(new FriendProfileDetails_Item("Gender", "this is set as private"));
                                    }

                                    if (accountprivacybirthday) {
                                        mList.add(new FriendProfileDetails_Item("Birthday", f_birthday));
                                    } else {
                                        mList.add(new FriendProfileDetails_Item("Birthday", "this is set as private"));
                                    }

                                    if (accountprivacyphone) {
                                        mList.add(new FriendProfileDetails_Item("Phone Number", f_phone));
                                    } else {
                                        mList.add(new FriendProfileDetails_Item("Phone Number", "this is set as private"));
                                    }

                                    if (accountprivacybio) {
                                        mList.add(new FriendProfileDetails_Item("Bio", f_bio));
                                    } else {
                                        mList.add(new FriendProfileDetails_Item("Bio", "this is set as private"));
                                    }

                                    adapter.notifyDataSetChanged();

                                }
                            }, new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    NoDataFound.setVisibility(View.VISIBLE);
                                }
                            });
                        }
                        else{
                            NoDataFound.setVisibility(View.VISIBLE);
                        }
                    }
                }, new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        NoDataFound.setVisibility(View.VISIBLE);
                    }
                });
        }
        catch (Exception ex){
            Log.e(TAG,"1" + ex.toString());
        }
    }
}

class FriendProfileDetails_Item{
    private String title;
    private String body;

    public FriendProfileDetails_Item(String title, String body) {
        this.title = title;
        this.body = body;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }
}

class AdapterFriendProfileDetails extends RecyclerView.Adapter<AdapterFriendProfileDetails.Holder>{
    private String TAG = "ACTIVITY COMMENT R View";
    private List<FriendProfileDetails_Item> mList;
    private Context context;

    public AdapterFriendProfileDetails(Context context, List<FriendProfileDetails_Item> mList) {
        this.context = context;
        this.mList = mList;
    }

    @NonNull
    @Override
    public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context.getApplicationContext()).inflate(R.layout.fragment_friendprofile_details_items,parent,false);
        return new Holder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final Holder holder, int position) {
        holder.title.setText(mList.get(position).getTitle());
        holder.body.setText(mList.get(position).getBody());
    }

    @Override
    public int getItemCount() {
        return mList.size();
    }

    class Holder extends RecyclerView.ViewHolder{
        TextView title, body;
        public Holder(View v){
            super(v);
            title = v.findViewById(R.id.titleMain);
            body = v.findViewById(R.id.bodyMain);
        }
    }
}


