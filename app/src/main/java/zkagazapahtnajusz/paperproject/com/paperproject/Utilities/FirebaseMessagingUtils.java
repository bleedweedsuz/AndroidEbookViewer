package zkagazapahtnajusz.paperproject.com.paperproject.Utilities;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import zkagazapahtnajusz.paperproject.com.paperproject.Activity_SignIn;
import zkagazapahtnajusz.paperproject.com.paperproject.R;

public class FirebaseMessagingUtils extends FirebaseMessagingService {
    private static final String TAG = "FirebaseMessagingUtils";
    private String UID = null;
    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        try{
            FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
            try{UID = firebaseAuth.getCurrentUser().getUid();}catch (Exception ex){Log.e(TAG, ex.toString());}
            if(firebaseAuth != null && UID != null){
                String NotificationType = remoteMessage.getData().get("type");
                if(NotificationType.equals(getString(R.string.Notification_postnew))){//Post New
                    Notification_postnew(remoteMessage);
                }
                else if(NotificationType.equals(getString(R.string.Notification_postcomment))){//Post Comment
                    Notification_postcomment(remoteMessage);
                }
                else if(NotificationType.equals(getString(R.string.Notification_postlike))){//Post Like
                    Notification_postlike(remoteMessage);
                }
                else if(NotificationType.equals(getString(R.string.Notification_booklike))){//Book Like
                    Notification_booklike(remoteMessage);
                }
                else if(NotificationType.equals(getString(R.string.Notification_bookcomment))){//Book Comment
                    Notification_bookcomment(remoteMessage);
                }
                else if(NotificationType.equals(getString(R.string.Notification_followrequest))){//Follow Request
                    Notification_followrequest(remoteMessage);
                }
                else if(NotificationType.equals(getString(R.string.Notification_request_replied))){
                    String TUID = remoteMessage.getData().get("tuid");
                    String followid = remoteMessage.getData().get("followid");
                    FirebaseFunctionsUtils.SubscribeTopic(getApplicationContext(), TUID, followid);
                }
            }
            else{
                Log.e(TAG, "No AUTH OR USER FOUND");
            }
        }
        catch (Exception ex){
            Log.e(TAG , ex.toString());
        }
    }

    @Override
    public void onDeletedMessages() {
        super.onDeletedMessages();
    }

    private void Notification_postnew(RemoteMessage remoteMessage){
        try{
            if(SharedPreferenceUtils.Setting_GetData(getApplicationContext(),getString(R.string.Notification_postnew), true, UID)) {
                String NotificationTitle = remoteMessage.getData().get("t");
                String NotificationBody = remoteMessage.getData().get("b");
                String POSTID = remoteMessage.getData().get("pid");

                Intent intent = new Intent(this, Activity_SignIn.class);
                intent.putExtra("FLAG", "POST_NEW_ADDED");
                intent.putExtra("POSTID", POSTID);
                SetNotification(intent, NotificationTitle, NotificationBody, R.drawable.icon_activity_select, "0x0001", "New Post Added");
            }
        }
        catch (Exception ex){
            Log.e(TAG, ex.toString());
        }
    }

    private void Notification_postcomment(RemoteMessage remoteMessage){
        try{
            if(SharedPreferenceUtils.Setting_GetData(getApplicationContext(),getString(R.string.Notification_postcomment), true, UID)) {
                String NotificationTitle = remoteMessage.getData().get("t");
                String NotificationBody = remoteMessage.getData().get("b");
                String POSTID = remoteMessage.getData().get("pid");

                Intent intent = new Intent(this, Activity_SignIn.class);
                intent.putExtra("FLAG", "POST_COMMENT");
                intent.putExtra("POSTID", POSTID);
                SetNotification(intent, NotificationTitle, NotificationBody, R.drawable.comment, "0x0002", "New Post Comment");
            }
        }
        catch (NullPointerException ex){
            Log.e(TAG, ex.toString());
        }
        catch (Exception ex){
            Log.e(TAG, ex.toString());
        }
    }

    private void Notification_postlike(RemoteMessage remoteMessage){
        try{
            if(SharedPreferenceUtils.Setting_GetData(getApplicationContext(),getString(R.string.Notification_postlike), true, UID)) {
                String NotificationTitle = remoteMessage.getData().get("t");
                String NotificationBody = remoteMessage.getData().get("b");
                String POSTID = remoteMessage.getData().get("pid");

                Intent intent = new Intent(this, Activity_SignIn.class);
                intent.putExtra("FLAG", "POST_LIKE");
                intent.putExtra("POSTID", POSTID);
                SetNotification(intent, NotificationTitle, NotificationBody, R.drawable.icon_likes_new,  "0x0003", "New Post Like");
            }
        }
        catch (Exception ex){
            Log.e(TAG, ex.toString());
        }
    }

    private void Notification_booklike(RemoteMessage remoteMessage){
        try{
            if(SharedPreferenceUtils.Setting_GetData(getApplicationContext(),getString(R.string.Notification_booklike), true, UID)) {
                String uid = remoteMessage.getData().get("UID");
                if(!UID.equals(uid)) {
                    String NotificationTitle = remoteMessage.getData().get("t");
                    String NotificationBody = remoteMessage.getData().get("b");

                    Intent intent = new Intent(this, Activity_SignIn.class);
                    intent.putExtra("FLAG", "BOOKDETAILS_LIKE");
                    intent.putExtra("BOOKID", remoteMessage.getData().get("bid"));
                    SetNotification(intent, NotificationTitle, NotificationBody, R.drawable.icon_likes_new,  "0x0004", "New Book Like");
                }
                else{
                    Log.e(TAG, "Error: I Got The Message");
                }
            }
        }
        catch (Exception ex){
            Log.e(TAG, ex.toString());
        }
    }

    private void Notification_bookcomment(RemoteMessage remoteMessage){
        try{
            if(SharedPreferenceUtils.Setting_GetData(getApplicationContext(),getString(R.string.Notification_bookcomment), true, UID)) {
                String uid = remoteMessage.getData().get("UID");

                if(!UID.equals(uid)) {
                    String NotificationTitle = remoteMessage.getData().get("t");
                    String NotificationBody = remoteMessage.getData().get("b");

                    Intent intent = new Intent(this, Activity_SignIn.class);
                    intent.putExtra("FLAG", "BOOKDETAILS_COMMENT");
                    intent.putExtra("BOOKID", remoteMessage.getData().get("bid"));

                    SetNotification(intent, NotificationTitle, NotificationBody, R.drawable.comment ,  "0x0005", "New Book Comment");
                }
                else{
                    Log.e(TAG, "Error: I Got The Message");
                }
            }
        }
        catch (Exception ex){
            Log.e(TAG, ex.toString());
        }
    }

    private void Notification_followrequest(RemoteMessage remoteMessage){
        try{
            String UID = remoteMessage.getData().get("ID");
            if(SharedPreferenceUtils.Setting_GetData(getApplicationContext(), getString(R.string.Notification_followrequest), true, UID)) {
                String NotificationTitle = remoteMessage.getData().get("t");
                String NotificationBody = remoteMessage.getData().get("b");

                Intent intent = new Intent(this, Activity_SignIn.class);
                intent.putExtra("FLAG", "FOLLOWREQUEST");
                SetNotification(intent, NotificationTitle, NotificationBody, R.drawable.icon_follow, "0x0006","Follow Request");
            }
        }
        catch (Exception ex){
            Log.e(TAG, ex.toString());
        }
    }

    private void SetNotification(Intent intent,String NotificationTitle, String NotificationBody, int appIcon, String CHANNEL_ID, String ChannelName){
        try{
            int id = Utils.randInt(1,1001);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

            NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this, CHANNEL_ID)
                                                    .setSmallIcon(R.drawable.logo_small)
                                                    .setContentTitle(NotificationTitle)
                                                    .setContentText(NotificationBody)
                                                    .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                                                    .setContentIntent(pendingIntent)
                                                    .setAutoCancel(true);

            NotificationManager notificationManager =  (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                NotificationChannel channel = new NotificationChannel(CHANNEL_ID, ChannelName, NotificationManager.IMPORTANCE_DEFAULT);
                channel.setDescription(ChannelName + " System");
                notificationManager.createNotificationChannel(channel);
            }
            notificationManager.notify(id, mBuilder.build());
        }
        catch (NullPointerException ex){
            Log.e(TAG, ex.toString());
        }
        catch (Exception ex){
            Log.e(TAG, ex.toString());
        }
    }
}