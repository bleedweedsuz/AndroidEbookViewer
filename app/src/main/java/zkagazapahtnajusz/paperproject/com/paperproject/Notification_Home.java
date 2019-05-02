package zkagazapahtnajusz.paperproject.com.paperproject;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.github.marlonlom.utilities.timeago.TimeAgo;
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
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;
import zkagazapahtnajusz.paperproject.com.paperproject.Utilities.FirebaseUtils;
import zkagazapahtnajusz.paperproject.com.paperproject.Utilities.NetworkManagerUtils;
import zkagazapahtnajusz.paperproject.com.paperproject.Utilities.Utils;

public class Notification_Home extends AppCompatActivity {
    private static final String TAG = "Notification_Home";
    ViewPager viewPager;
    TabLayout tabLayout;
    Toolbar toolbar;
    Notification_ViewPagerAdapter adapter;
    Notification_All notification_all;
    Notification_People notification_people;
    Notification_Books notification_books;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notification__home);
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if(getSupportActionBar() != null){
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }

        viewPager = findViewById(R.id.viewPager);
        tabLayout = findViewById(R.id.tabLayout);

        adapter = new Notification_ViewPagerAdapter(getSupportFragmentManager());

        notification_all = new Notification_All();
        adapter.addFragment(notification_all);

        notification_people = new Notification_People();
        adapter.addFragment(notification_people);

        notification_books = new Notification_Books();
        adapter.addFragment(notification_books);


        viewPager.setAdapter(adapter);
        tabLayout.setupWithViewPager(viewPager);
    }

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(R.anim.slide_in_left,R.anim.slide_out_right);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId() == android.R.id.home){
            finish();
        }
        return super.onOptionsItemSelected(item);
    }

    public static class Notification_ViewPagerAdapter extends FragmentPagerAdapter{
        List<Fragment> fragmentList = new ArrayList<>();
        String tabTitles[] = new String[] {"All", "People", "Books"};

        public Notification_ViewPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            return fragmentList.get(position);
        }

        @Override
        public int getCount() {
            return fragmentList.size();
        }

        public void addFragment(Fragment fragment){
            fragmentList.add(fragment);
        }

        @Nullable
        @Override
        public CharSequence getPageTitle(int position) {
            return tabTitles[position];
        }
    }

    public static class Notification_All extends Fragment implements SwipeRefreshLayout.OnRefreshListener{
        RecyclerView recyclerView;
        LinearLayoutManager layoutManager;
        NotificationTextAdapter adapter;
        List<NotificationTextItem> notificationTextItemList = new ArrayList<>();
        FirebaseFirestore firebaseFirestore;
        ListenerRegistration listenerRegistration;
        SwipeRefreshLayout swipeRefreshLayout;
        boolean isDataLoaded;
        View NoDataFoundView;

        @Nullable
        @Override
        public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
            View view = inflater.inflate(R.layout.notification_home_list_fragments, container, false);
            init(view);
            return view;
        }

        void init(View view){
            try{
                firebaseFirestore = FirebaseFirestore.getInstance();
                swipeRefreshLayout = view.findViewById(R.id.swipeRefresh);
                swipeRefreshLayout.setOnRefreshListener(this);
                recyclerView = view.findViewById(R.id.recyclerView);
                NoDataFoundView = view.findViewById(R.id.NoDataFoundView);
                layoutManager = new LinearLayoutManager(getActivity());
                layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
                recyclerView.setLayoutManager(layoutManager);
                adapter = new NotificationTextAdapter(getActivity(), notificationTextItemList);
                recyclerView.setAdapter(adapter);
                LoadData();
            }
            catch (Exception ex){
                Log.e(TAG, ex.toString());
            }
        }

        public void LoadData(){
            try{
                //if(listenerRegistration !=null){listenerRegistration.remove();listenerRegistration = null;}
                notificationTextItemList.clear();

                NoDataFoundView.setVisibility(View.GONE);
                isDataLoaded = false;
                //region no internet connection show
                NetworkManagerUtils.LoadServerData(getContext());
                //endregion
                String UID = FirebaseUtils.firebaseAuth.getCurrentUser().getUid();
                String Root = getString(R.string.FIRESTORE_ROOT_USERDETAILS) + "/" + UID + "/" + getString(R.string.FIRESTORE_ROOT_NOTIFICATIONS);
                Query firstQuery = firebaseFirestore.collection(Root).orderBy(getString(R.string.F_NOTIFICATIONS_POSTDATE), Query.Direction.DESCENDING);
                listenerRegistration = firstQuery.addSnapshotListener(getActivity(), new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(QuerySnapshot queryDocumentSnapshots, FirebaseFirestoreException e) {
                        if(queryDocumentSnapshots != null && !queryDocumentSnapshots.isEmpty()){
                            for(DocumentChange doc: queryDocumentSnapshots.getDocumentChanges()){

                                if(doc.getType() == DocumentChange.Type.ADDED){
                                    //Add To Lists
                                    String ID = doc.getDocument().getId();
                                    String pTYpe = doc.getDocument().getString(getString(R.string.F_NOTIFICATIONS_PTYPE));
                                    String isRead = doc.getDocument().getString(getString(R.string.F_NOTIFICATIONS_READ));
                                    String postDate = doc.getDocument().get(getString(R.string.F_NOTIFICATIONS_POSTDATE)).toString();
                                    String postID = doc.getDocument().getString(getString(R.string.F_NOTIFICATIONS_POSTID));
                                    String description = doc.getDocument().getString(getString(R.string.F_NOTIFICATIONS_DESCRIPTION));
                                    String UID = doc.getDocument().getString(getString(R.string.F_NOTIFICATIONS_UID));
                                    String bookid = doc.getDocument().getString(getString(R.string.F_NOTIFICATIONS_BOOKID));

                                    if(!isDataLoaded){
                                        notificationTextItemList.add(new NotificationTextItem(ID, pTYpe, isRead, postDate, postID, description, UID, bookid));
                                        adapter.notifyItemInserted(notificationTextItemList.size() - 1);
                                    }
                                    else{
                                        notificationTextItemList.add(0, new NotificationTextItem(ID, pTYpe, isRead, postDate, postID, description, UID, bookid));
                                        adapter.notifyItemInserted(0);
                                        recyclerView.scrollToPosition(0);
                                    }
                                }
                                else if(doc.getType() == DocumentChange.Type.MODIFIED){
                                    String ID = doc.getDocument().getId();
                                    for(int i=0;i<notificationTextItemList.size();i++){
                                        if(notificationTextItemList.get(i).ID.equals(ID)){
                                            notificationTextItemList.get(i).isRead = "true";
                                            adapter.notifyItemChanged(i);
                                            break;
                                        }
                                    }
                                }
                            }

                            if(notificationTextItemList.size() >0){NoDataFoundView.setVisibility(View.GONE);}else{NoDataFoundView.setVisibility(View.VISIBLE);}

                            if(!isDataLoaded){
                                isDataLoaded = true;
                            }
                        }
                        else{
                            if(e != null){Log.e(TAG, e.getMessage());}
                            notificationTextItemList.clear();
                            adapter.notifyDataSetChanged();
                            NoDataFoundView.setVisibility(View.VISIBLE);
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

        @Override
        public void onRefresh() {
            notificationTextItemList.clear();
            if(listenerRegistration != null) { listenerRegistration.remove(); listenerRegistration = null;}
            LoadData();
        }
    }

    public static class Notification_People extends Fragment implements SwipeRefreshLayout.OnRefreshListener{
        RecyclerView recyclerView;
        LinearLayoutManager layoutManager;
        NotificationTextAdapter adapter;
        List<NotificationTextItem> notificationTextItemList = new ArrayList<>();
        FirebaseFirestore firebaseFirestore;
        ListenerRegistration listenerRegistration;
        SwipeRefreshLayout swipeRefreshLayout;
        boolean isDataLoaded;

        View NoDataFoundView;
        @Nullable
        @Override
        public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
            View view = inflater.inflate(R.layout.notification_home_list_fragments, container, false);
            init(view);
            return view;
        }

        void init(View view){
            try{
                firebaseFirestore = FirebaseFirestore.getInstance();

                swipeRefreshLayout = view.findViewById(R.id.swipeRefresh);
                swipeRefreshLayout.setOnRefreshListener(this);
                NoDataFoundView = view.findViewById(R.id.NoDataFoundView);
                recyclerView = view.findViewById(R.id.recyclerView);

                layoutManager = new LinearLayoutManager(getActivity());
                layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
                recyclerView.setLayoutManager(layoutManager);

                adapter = new NotificationTextAdapter(getActivity(), notificationTextItemList);
                recyclerView.setAdapter(adapter);

                LoadData();
            }
            catch (Exception ex){
                Log.e(TAG, ex.toString());
            }
        }

        public void LoadData(){
            try{
                if(listenerRegistration !=null){listenerRegistration.remove();listenerRegistration = null;}
                notificationTextItemList.clear();

                NoDataFoundView.setVisibility(View.GONE);
                isDataLoaded = false;
                //region no internet connection show
                NetworkManagerUtils.LoadServerData(getContext());
                //endregion
                String UID = FirebaseUtils.firebaseAuth.getCurrentUser().getUid();
                String Root = getString(R.string.FIRESTORE_ROOT_USERDETAILS) + "/" + UID + "/" + getString(R.string.FIRESTORE_ROOT_NOTIFICATIONS);
                Query firstQuery = firebaseFirestore.collection(Root).orderBy(getString(R.string.F_NOTIFICATIONS_POSTDATE), Query.Direction.DESCENDING);
                listenerRegistration = firstQuery.addSnapshotListener(getActivity(), new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(QuerySnapshot queryDocumentSnapshots, FirebaseFirestoreException e) {
                        if(queryDocumentSnapshots != null && !queryDocumentSnapshots.isEmpty()){
                            for(DocumentChange doc: queryDocumentSnapshots.getDocumentChanges()){
                                if(doc.getType() == DocumentChange.Type.ADDED){
                                    //Add To Lists
                                    String pTYpe = doc.getDocument().getString(getString(R.string.F_NOTIFICATIONS_PTYPE));
                                    if(pTYpe.equals("post_user") || pTYpe.equals("post_like_user") || pTYpe.equals("post_comment_user")) {
                                        String ID = doc.getDocument().getId();
                                        String isRead = doc.getDocument().getString(getString(R.string.F_NOTIFICATIONS_READ));
                                        String postDate = doc.getDocument().get(getString(R.string.F_NOTIFICATIONS_POSTDATE)).toString();
                                        String postID = doc.getDocument().getString(getString(R.string.F_NOTIFICATIONS_POSTID));
                                        String UID = doc.getDocument().getString(getString(R.string.F_NOTIFICATIONS_UID));
                                        String description = doc.getDocument().getString(getString(R.string.F_NOTIFICATIONS_DESCRIPTION));

                                        if (!isDataLoaded) {
                                            notificationTextItemList.add(new NotificationTextItem(ID, pTYpe, isRead, postDate, postID, description, UID, null));
                                            adapter.notifyItemInserted(notificationTextItemList.size() - 1);
                                        } else {
                                            notificationTextItemList.add(0, new NotificationTextItem(ID, pTYpe, isRead, postDate, postID, description, UID, null));
                                            adapter.notifyItemInserted(0);
                                            recyclerView.scrollToPosition(0);
                                        }
                                    }
                                }
                                else if(doc.getType() == DocumentChange.Type.MODIFIED){
                                    String ID = doc.getDocument().getId();
                                    for(int i=0;i<notificationTextItemList.size();i++){
                                        if(notificationTextItemList.get(i).ID.equals(ID)){
                                            notificationTextItemList.get(i).isRead = "true";
                                            adapter.notifyItemChanged(i);
                                            break;
                                        }
                                    }
                                }
                            }

                            if(notificationTextItemList.size() >0){NoDataFoundView.setVisibility(View.GONE);}else{NoDataFoundView.setVisibility(View.VISIBLE);}

                            if(!isDataLoaded){
                                isDataLoaded = true;
                            }
                        }
                        else{
                            if(e != null){Log.e(TAG, e.getMessage());}
                            notificationTextItemList.clear();
                            adapter.notifyDataSetChanged();
                            NoDataFoundView.setVisibility(View.VISIBLE);
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

        @Override
        public void onRefresh() {
            notificationTextItemList.clear();
            if(listenerRegistration != null) { listenerRegistration.remove(); listenerRegistration = null;}
            LoadData();
        }
    }

    public static class Notification_Books extends Fragment implements SwipeRefreshLayout.OnRefreshListener{
        RecyclerView recyclerView;
        LinearLayoutManager layoutManager;
        NotificationTextAdapter adapter;
        List<NotificationTextItem> notificationTextItemList = new ArrayList<>();
        FirebaseFirestore firebaseFirestore;
        ListenerRegistration listenerRegistration;
        SwipeRefreshLayout swipeRefreshLayout;
        boolean isDataLoaded;

        View NoDataFoundView;
        @Nullable
        @Override
        public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
            View view = inflater.inflate(R.layout.notification_home_list_fragments, container, false);
            init(view);
            return view;
        }

        void init(View view){
            try{
                firebaseFirestore = FirebaseFirestore.getInstance();
                NoDataFoundView = view.findViewById(R.id.NoDataFoundView);
                swipeRefreshLayout = view.findViewById(R.id.swipeRefresh);
                swipeRefreshLayout.setOnRefreshListener(this);

                recyclerView = view.findViewById(R.id.recyclerView);

                layoutManager = new LinearLayoutManager(getActivity());
                layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
                recyclerView.setLayoutManager(layoutManager);

                adapter = new NotificationTextAdapter(getActivity(), notificationTextItemList);
                recyclerView.setAdapter(adapter);

                LoadData();
            }
            catch (Exception ex){
                Log.e(TAG, ex.toString());
            }
        }

        public void LoadData(){
            try{
                if(listenerRegistration !=null){listenerRegistration.remove();listenerRegistration = null;}
                notificationTextItemList.clear();

                NoDataFoundView.setVisibility(View.GONE);
                isDataLoaded = false;
                //region no internet connection show
                NetworkManagerUtils.LoadServerData(getContext());
                //endregion
                String UID = FirebaseUtils.firebaseAuth.getCurrentUser().getUid();
                String Root = getString(R.string.FIRESTORE_ROOT_USERDETAILS) + "/" + UID + "/" + getString(R.string.FIRESTORE_ROOT_NOTIFICATIONS);
                Query firstQuery = firebaseFirestore.collection(Root).orderBy(getString(R.string.F_NOTIFICATIONS_POSTDATE), Query.Direction.DESCENDING);
                listenerRegistration = firstQuery.addSnapshotListener(getActivity(), new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(QuerySnapshot queryDocumentSnapshots, FirebaseFirestoreException e) {
                        if(queryDocumentSnapshots != null && !queryDocumentSnapshots.isEmpty()){
                            for(DocumentChange doc: queryDocumentSnapshots.getDocumentChanges()){
                                if(doc.getType() == DocumentChange.Type.ADDED){
                                    //Add To Lists
                                    String pTYpe = doc.getDocument().getString(getString(R.string.F_NOTIFICATIONS_PTYPE));
                                    if(pTYpe.equals("post_book") || pTYpe.equals("post_like_book") || pTYpe.equals("post_comment_book")) {
                                        String ID = doc.getDocument().getId();
                                        String isRead = doc.getDocument().getString(getString(R.string.F_NOTIFICATIONS_READ));
                                        String postDate = doc.getDocument().get(getString(R.string.F_NOTIFICATIONS_POSTDATE)).toString();
                                        String postID = doc.getDocument().getString(getString(R.string.F_NOTIFICATIONS_POSTID));
                                        String UID = doc.getDocument().getString(getString(R.string.F_NOTIFICATIONS_UID));
                                        String description = doc.getDocument().getString(getString(R.string.F_NOTIFICATIONS_DESCRIPTION));
                                        String bookid = doc.getDocument().getString(getString(R.string.F_NOTIFICATIONS_BOOKID));

                                        if (!isDataLoaded) {
                                            notificationTextItemList.add(new NotificationTextItem(ID, pTYpe, isRead, postDate, postID, description, UID, bookid));
                                            adapter.notifyItemInserted(notificationTextItemList.size() - 1);
                                        } else {
                                            notificationTextItemList.add(0, new NotificationTextItem(ID, pTYpe, isRead, postDate, postID, description, UID, bookid));
                                            adapter.notifyItemInserted(0);
                                            recyclerView.scrollToPosition(0);
                                        }
                                    }
                                }
                                else if(doc.getType() == DocumentChange.Type.MODIFIED){
                                    String ID = doc.getDocument().getId();
                                    for(int i=0;i<notificationTextItemList.size();i++){
                                        if(notificationTextItemList.get(i).ID.equals(ID)){
                                            notificationTextItemList.get(i).isRead = "true";
                                            adapter.notifyItemChanged(i);
                                            break;
                                        }
                                    }
                                }
                            }
                            if(notificationTextItemList.size() >0){NoDataFoundView.setVisibility(View.GONE);}else{NoDataFoundView.setVisibility(View.VISIBLE);}

                            if(!isDataLoaded){
                                isDataLoaded = true;
                            }
                        }
                        else{
                            if(e != null){Log.e(TAG, e.getMessage());}
                            notificationTextItemList.clear();
                            adapter.notifyDataSetChanged();
                            NoDataFoundView.setVisibility(View.VISIBLE);
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

        @Override
        public void onRefresh() {
            notificationTextItemList.clear();
            if(listenerRegistration != null) { listenerRegistration.remove(); listenerRegistration = null;}
            LoadData();
        }
    }

    static class NotificationTextItem{
        String ID;
        String pType;
        String isRead;
        String postDate;
        String postID;
        String description;
        String UID;
        String bookID;
        NotificationTextItem(String ID, String pType, String isRead, String postDate, String postID, String description, String UID, String bookID) {
            this.ID = ID;
            this.pType = pType;
            this.isRead = isRead;
            this.postDate = postDate;
            this.postID = postID;
            this.description = description;
            this.UID = UID;
            this.bookID = bookID;
        }

        public String getID() {
            return ID;
        }

        public void setID(String ID) {
            this.ID = ID;
        }

        public String getpType() {
            return pType;
        }

        public void setpType(String pType) {
            this.pType = pType;
        }

        public String getIsRead() {
            return isRead;
        }

        public void setIsRead(String isRead) {
            this.isRead = isRead;
        }

        public String getPostDate() {
            return postDate;
        }

        public void setPostDate(String postDate) {
            this.postDate = postDate;
        }

        public String getPostID() {
            return postID;
        }

        public void setPostID(String postID) {
            this.postID = postID;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }

        public String getUID() {
            return UID;
        }

        public void setUID(String UID) {
            this.UID = UID;
        }

        public String getBookID() {
            return bookID;
        }

        public void setBookID(String bookID) {
            this.bookID = bookID;
        }
    }

    public static class NotificationTextAdapter extends RecyclerView.Adapter<NotificationTextAdapter.Holder>{
        Context context;
        List<NotificationTextItem> notificationTextItemList;

        NotificationTextAdapter(Context context, List<NotificationTextItem> notificationTextItemList) {
            this.context = context;
            this.notificationTextItemList = notificationTextItemList;
        }

        @NonNull
        @Override
        public NotificationTextAdapter.Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(context).inflate(R.layout.notification_home_list_item, parent, false);
            return new Holder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull final NotificationTextAdapter.Holder holder, int position) {
            try {
                String Title = "";
                String Description = notificationTextItemList.get(position).description.trim();
                //Code = 1
                if (notificationTextItemList.get(position).pType.equals("post_user")) {
                    Title = "New Post by %s.";
                }
                else if (notificationTextItemList.get(position).pType.equals("post_like_user")) {
                    Title = "%s liked post.";
                }
                else if (notificationTextItemList.get(position).pType.equals("post_comment_user")) {
                    Title = "%s commented on post.";
                }
                //Code = 2
                else if (notificationTextItemList.get(position).pType.equals("post_book")) {
                    Title = "New Book Post Share by %s.";
                }
                else if (notificationTextItemList.get(position).pType.equals("post_like_book")) {
                    Title = "%s liked book post share.";
                }
                else if (notificationTextItemList.get(position).pType.equals("post_comment_book")) {
                    Title = "%s commented on on book share.";
                }
                holder.textCommentComment.setText("..");

                if(!Description.equals("")) {
                    if(Description.length() > 200){
                        Title += "<br/><i>\"" + Description.substring(0,199) + "..\"</i>";
                    }
                    else{
                        Title += "<br/><i>\"" + Description + "\"</i>";
                    }
                }

                PersonalDetails(holder.imageCommentRecyclerUser, holder.textCommentComment, Title, notificationTextItemList.get(position).UID);

                //region Time Ago System
                {
                    try {
                        String postDate = notificationTextItemList.get(position).postDate;
                        String timeAgo = TimeAgo.using(new Date(postDate).getTime());
                        holder.textCommentTime.setText(timeAgo);
                    } catch (Exception ex) {
                        Log.e(TAG, ex.toString());
                    }
                }
                //endregion

                holder.view.setOnLongClickListener(new View.OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View v) {
                        showPopUp(notificationTextItemList.get(holder.getAdapterPosition()).ID);
                        return true;
                    }
                });

                holder.view.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        openActivity(notificationTextItemList.get(holder.getAdapterPosition()));
                    }
                });

                if(notificationTextItemList.get(position).isRead.equals("false")){
                    holder.view.setBackgroundColor(context.getResources().getColor(R.color.NotificationNotRead));
                }
                else{
                    holder.view.setBackgroundColor(context.getResources().getColor(R.color.NotificationRead));
                }
            }
            catch (Exception ex){
                Log.e(TAG, ex.toString());
            }
        }

        void openActivity(NotificationTextItem notificationTextItem){
            String UID = notificationTextItem.UID;
            String code = notificationTextItem.pType;
            String ID = notificationTextItem.ID;
            String postID = notificationTextItem.postID;
            String C_UID = FirebaseUtils.firebaseAuth.getCurrentUser().getUid();

            MarkAsRead(C_UID, ID, false);

            //New Post //Post Like
            if(code.equals("post_user") || code.equals("post_like_user")){
                Intent i = new Intent(context, Activity_Comment.class);
                i.putExtra("ID", postID);
                //Open Post
                i.putExtra("commentFrom", Activity_Comment.CommentFrom.Post.toString());
                i.putExtra("isFirstScroll", false);
                context.startActivity(i);

            }
            else if(code.equals("post_book")){
                Intent i = new Intent(context, Activity_Comment.class);
                i.putExtra("ID", postID);
                i.putExtra("commentFrom", Activity_Comment.CommentFrom.Post.toString());
                i.putExtra("isFirstScroll", false);
                context.startActivity(i);

            }
            //Post Comment //Book Comment
            else if(code.equals("post_comment_user") ){
                Intent i = new Intent(context, Activity_Comment.class);
                i.putExtra("ID", postID);
                i.putExtra("commentFrom", Activity_Comment.CommentFrom.Post.toString());
                i.putExtra("isFirstScroll", true);
                context.startActivity(i);

            }
            //no book ref required
            else if(code.equals("post_like_book")){
                Intent intent = new Intent(context, Activity_BookDetails.class);
                intent.putExtra("BookID", notificationTextItem.bookID);
                context.startActivity(intent);
            }
            else if(code.equals("post_comment_book")){
                Intent i = new Intent(context, Activity_Comment.class);
                i.putExtra("ID", postID);
                i.putExtra("ID", notificationTextItem.bookID);
                i.putExtra("commentFrom", Activity_Comment.CommentFrom.Book.toString());
                i.putExtra("isBookDetailsComment", true);
                context.startActivity(i);

            }
        }

        void showPopUp(final String ID){
            try{
                final String UID = FirebaseUtils.firebaseAuth.getCurrentUser().getUid();
                CharSequence menuItems[] = new CharSequence[] {"Mark As Read"};
                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                builder.setTitle(" Options");
                builder.setItems(menuItems, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if(which == 0){
                           MarkAsRead(UID, ID, true);
                        }
                    }
                });
                builder.create().show();
            }
            catch (Exception ex){
                Log.e(TAG, ex.toString());
            }
        }

        void MarkAsRead(String UID, String ID, final boolean isDialog){
            //region Mark As Read
            if(isDialog){Utils.SetProgressDialogIndeterminate(context, "processing..");}
            String Root = context.getString(R.string.FIRESTORE_ROOT_USERDETAILS) + "/" + UID + "/" + context.getString(R.string.FIRESTORE_ROOT_NOTIFICATIONS) + "/" + ID;
            Map<String, Object> map = new HashMap<>();
            map.put(context.getString(R.string.F_NOTIFICATIONS_READ), "true");
            FirebaseUtils.FirestroreSetData(Root, map, new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void aVoid) {if(isDialog){Utils.UnSetProgressDialogIndeterminate();Toast.makeText(context, "Marked as read", Toast.LENGTH_SHORT).show();}}
            }, new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {if(isDialog){Utils.UnSetProgressDialogIndeterminate();Toast.makeText(context, e.getMessage(), Toast.LENGTH_SHORT).show();}}
            });
            //endregion
        }

        @Override
        public int getItemCount() {
            return notificationTextItemList.size();
        }

        void PersonalDetails(final ImageView profileImage, final TextView tView, final String formatData, final String UID){
            //Search For UID in database
            String Root = context.getString(R.string.FIRESTORE_ROOT_USERDETAILS) + "/" + UID;
            FirebaseUtils.FirestoreGetData(Root, new OnSuccessListener<DocumentSnapshot>() {
                @Override
                public void onSuccess(DocumentSnapshot documentSnapshot) {
                    if (documentSnapshot.exists()) {
                        try {String UserProfileLink = "";try {UserProfileLink = documentSnapshot.getString(context.getString(R.string.F_USERDETAILS_PROFILEURL));} catch (Exception ex) {Log.e(TAG, ex.toString());}ColorDrawable colorDrawableInfoPlaceholder = new ColorDrawable(ContextCompat.getColor(context, R.color.colorDrawableInfoPlaceholder));ColorDrawable colorDrawableErrorPlaceholder = new ColorDrawable(ContextCompat.getColor(context, R.color.colorDrawableErrorPlaceholder));Utils.SetImageViaUri((Activity) context, UID, UserProfileLink, profileImage, false, false, colorDrawableInfoPlaceholder, colorDrawableErrorPlaceholder, null);} catch (Exception ex) {Log.e(TAG, ex.toString());}
                        String UserFullName = "";
                        try { UserFullName = documentSnapshot.getString(context.getString(R.string.F_USERDETAILS_USERFULLNAME));} catch (Exception ex) {Log.e(TAG, ex.toString());}
                        try{ String data = String.format(formatData, "<b>"+ UserFullName + "</b>");tView.setText(Html.fromHtml(data));}catch (Exception ex){Log.e(TAG, ex.toString());}
                    }
                }
            }, new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Log.e(TAG, e.toString());
                }
            });
        }

        static class Holder extends RecyclerView.ViewHolder{
            View view;
            TextView textCommentComment, textCommentTime;
            CircleImageView imageCommentRecyclerUser;
            Holder(View itemView) {
                super(itemView);
                textCommentComment = itemView.findViewById(R.id.textCommentComment);
                textCommentTime = itemView.findViewById(R.id.textCommentTime);
                imageCommentRecyclerUser = itemView.findViewById(R.id.imageCommentRecyclerUser);
                view = itemView.findViewById(R.id.view);
            }
        }
    }
}


