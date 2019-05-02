package zkagazapahtnajusz.paperproject.com.paperproject.Utilities;

import android.content.Context;
import android.support.annotation.NonNull;
import android.util.Log;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.functions.HttpsCallableResult;
import com.google.firebase.messaging.FirebaseMessaging;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import zkagazapahtnajusz.paperproject.com.paperproject.Model.Post;

public class FirebaseFunctionsUtils {
    private static final String TAG = "FirebaseFunction";
    public enum NotificationType {postnew, postcomment, postlike, booklike, bookcomment, followrequest}

    public void FunctionPostNew(String PostID, String UID, String Description, String DisplayName, NotificationType notificationType, Post.PostType postType){
        try{
            Map<String, Object> postMap = new HashMap<>();
            postMap.put("uid", UID);
            postMap.put("postid", PostID);
            postMap.put("description", Description);
            postMap.put("displayname", DisplayName);
            postMap.put("date", Calendar.getInstance().getTime().toString());
            postMap.put("posttype", postType.toString());
            postMap.put("notificationtype", notificationType.toString());

            FirebaseUtils.RunServerFunction("SendNotification", postMap,
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
        }
        catch (Exception ex){
            Log.e(TAG, ex.toString());
        }
    }

    public static void SubscribeTopic(Context context, String UID, String followid){
        try{

            //FirebaseMessaging firebaseMessaging = FirebaseMessaging.getInstance();
            //firebaseMessaging.subscribeToTopic(UID + "_postnew");
            //firebaseMessaging.subscribeToTopic(UID + "_postcomment");
            //firebaseMessaging.subscribeToTopic(UID + "_postlike");
            //firebaseMessaging.subscribeToTopic(UID + "_booklike");
            //firebaseMessaging.subscribeToTopic(UID + "_bookcomment");
            //firebaseMessaging.subscribeToTopic(UID + "_followrequest");

            /*
            String Root = context.getString(R.string.FIRESTORE_ROOT_USERDETAILS) + "/" + UID + "/" + context.getString(R.string.FIRESTORE_ROOT_FOLLOWING) + "/" + followid;
            Map<String, Object> map = new HashMap<>();
            map.put(context.getString(R.string.F_USERDETAILS_FOLLOWING_ACHIEVE), 1);
            FirebaseUtils.FirestroreSetData(Root, map,
                    new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            Log.i(TAG, "ACHIVE FOLLOWING");
                        }
                    }, new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Log.e(TAG, e.toString());
                        }
                    });
            */
        }
        catch (Exception ex){
            Log.e(TAG, ex.toString());
        }
    }

    public static void UnSubscribeTopic_User(String UID){
        try{
            FirebaseMessaging firebaseMessaging = FirebaseMessaging.getInstance();
            firebaseMessaging.unsubscribeFromTopic(UID + "_postnew");
            firebaseMessaging.unsubscribeFromTopic(UID + "_postcomment");
            firebaseMessaging.unsubscribeFromTopic(UID + "_postlike");

            //firebaseMessaging.unsubscribeFromTopic(UID + "_followrequest");
        }
        catch (Exception ex){
            Log.e(TAG, ex.toString());
        }
    }

    public static void UnSubscribeTopic_Book(String BookID){
        try{
            FirebaseMessaging firebaseMessaging = FirebaseMessaging.getInstance();
            firebaseMessaging.unsubscribeFromTopic(BookID + "_booklike");
            firebaseMessaging.unsubscribeFromTopic(BookID + "_bookcomment");
        }
        catch (Exception ex){
            Log.e(TAG, ex.toString());
        }
    }
}
