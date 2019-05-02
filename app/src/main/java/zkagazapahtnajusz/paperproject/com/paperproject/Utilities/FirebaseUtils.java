package zkagazapahtnajusz.paperproject.com.paperproject.Utilities;

import android.app.Activity;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.SetOptions;
import com.google.firebase.firestore.Transaction;
import com.google.firebase.functions.FirebaseFunctions;
import com.google.firebase.functions.HttpsCallableResult;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import zkagazapahtnajusz.paperproject.com.paperproject.R;

/**
 * Created by Zw4R-TSTUD10 2018/4/1 [2018, April 1]
 */

public class FirebaseUtils {

    private static String TAG = "Firebase Utils";

    public static FirebaseAuth firebaseAuth;

    public static void SendVerificationEmail(FirebaseUser firebaseUser){
        firebaseUser.sendEmailVerification()
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if(task.isSuccessful()){
                            Log.i("INFO","SAVE");
                        }
                        else{
                            Log.e("ERROR","ERROR");
                        }
                    }
                });
    }

    public interface SignInFinished_Phone{
        void Finished(boolean isSuccessful, FirebaseUser user);
    }

    public static void SignInWithPhone(final Activity activity, FirebaseAuth firebaseAuth, PhoneAuthCredential phoneAuthCredential, final String FullName, final SignInFinished_Phone signInFinished_phone) {
        firebaseAuth.signInWithCredential(phoneAuthCredential).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if(task.isSuccessful()){
                    FirebaseUser user = task.getResult().getUser();
                    String fName = FullName;
                    if(fName.equals("")){
                        fName = user.getUid().substring(0, 5);
                    }

                    //Update Database For User
                    FirebaseUtils.UpdateUserDetailsInDatabase(activity, user.getUid(), fName);

                    //Add Display Data In System
                    FirebaseUtils.SetAuthDisplayName(FirebaseUtils.firebaseAuth, fName);

                    //Set Display Name
                    user.updateProfile(new UserProfileChangeRequest.Builder().setDisplayName(fName).build())
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if(task.isSuccessful()){
                                Log.i("INFO", "Profile Display Name Update.");
                            }
                        }
                    });

                    Log.i("Info", "Sign In :) " + user.getUid());

                    signInFinished_phone.Finished(true, user);
                }
                else{
                    Log.e("Error", task.getException().toString());
                    signInFinished_phone.Finished(false, null);
                }
            }
        });
    }

    public static void SetAuthDisplayName(FirebaseAuth firebaseAuth, String name){
        FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();
        if(firebaseUser != null){
            UserProfileChangeRequest userProfileChangeRequest = new UserProfileChangeRequest.Builder()
                    .setDisplayName(name)
                    .build();
            firebaseUser.updateProfile(userProfileChangeRequest)
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if(task.isSuccessful()){
                                Log.i("INFO", "Profile Display Name Update.");
                            }
                        }
                    });
        }
    }

    public static void UpdateUserDetailsInDatabase(Activity activity, String UID, String FullName){
        try{
            String Root = activity.getString(R.string.FIRESTORE_ROOT_USERDETAILS) + "/" + UID;
            Map<String, Object> map = new HashMap<>();
            map.put(activity.getString(R.string.F_USERDETAILS_UID), UID);
            map.put(activity.getString(R.string.F_USERDETAILS_USERFULLNAME), FullName);
            map.put(activity.getString(R.string.F_USERDETAILS_JOINDATE), new Timestamp(Calendar.getInstance().getTime()));
            FirestroreSetData(
                    Root,
                    map,
                    new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            Log.i(TAG, "User Details Updated");
                        }
                    },
                    new OnFailureListener() {
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

    public static void uploadProfileImage(final Activity activity, Bitmap OriginalImage){
        try {
            //Upload Image To Fire-Store
            String path = activity.getString(R.string.FIRESTORAGE_ROOT_USERIMAGES) + "/" + activity.getString(R.string.FIRESTORAGE_ROOT_PROFILE_SUFFIX) + FirebaseUtils.firebaseAuth.getCurrentUser().getUid();
            final StorageReference storageReference = FirebaseStorage.getInstance().getReference().child(path);

            //Convert Image To ByteArray
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            OriginalImage.compress(Bitmap.CompressFormat.JPEG, activity.getResources().getInteger(R.integer.FIRESTORAGE_UPLOAD_QUALITY_IMAGE) , byteArrayOutputStream);
            byte[] data = byteArrayOutputStream.toByteArray();
            //region RUN UPLOAD IMAGE
            UploadTask uploadTask = storageReference.putBytes(data);

            uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                @Override
                public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                    if (!task.isSuccessful()) {
                        throw task.getException();
                    }
                    // Continue with the task to get the download URL
                    return storageReference.getDownloadUrl();
                }
            }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                @Override
                public void onComplete(@NonNull Task<Uri> task) {
                    if(task.isSuccessful()){
                        //region Save To Database
                        Uri downloadUrl = task.getResult();
                        String Root = activity.getString(R.string.FIRESTORE_ROOT_USERDETAILS) + "/" + FirebaseUtils.firebaseAuth.getCurrentUser().getUid();
                        Map<String, Object> map = new HashMap<>();
                        map.put(activity.getString(R.string.F_USERDETAILS_PROFILEURL), downloadUrl.toString());

                        FirebaseUtils.FirestroreSetData(Root, map, new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                Log.i(TAG, "SAVE PROFILE IMAGE TO DATABASE");
                            }
                        }, new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Log.e(TAG, e.getMessage());
                            }
                        });
                        //endregion
                    }
                    else{
                        Log.e(TAG, "Task Does not Complete");
                    }
                }
            });


            //region OLD SYS
            /*
            uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    //region Save To Database
                    Uri downloadUrl = taskSnapshot.getDownloadUrl();
                    String Root = activity.getString(R.string.FIRESTORE_ROOT_USERDETAILS) + "/" + FirebaseUtils.firebaseAuth.getCurrentUser().getUid();
                    Map<String, Object> map = new HashMap<>();
                    map.put(activity.getString(R.string.F_USERDETAILS_PROFILEURL), downloadUrl.toString());

                    FirebaseUtils.FirestroreSetData(Root, map, new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            Log.i(TAG, "SAVE PROFILE IMAGE TO DATABASE");
                        }
                    }, new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Log.e(TAG, e.getMessage());
                        }
                    });
                    //endregion
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Log.e(TAG, e.getMessage() );
                }
            });
            */
            //endregion

            //endregion
        }
        catch (Exception ex){
            Log.e(TAG, ex.toString());
        }
    }

    public static void uploadFile(final Activity activity, String ext, String fileID, String filePath, OnSuccessListener<UploadTask.TaskSnapshot> onSuccessListener){
        try{
            //Upload Image To Fire-Store
            String path = activity.getString(R.string.FIRESTORAGE_LOCAL_DRIVE_BOOK) + "/" + fileID + ext;
            StorageReference storageReference = FirebaseStorage.getInstance().getReference().child(path);

            File bookFile = new File(filePath);

            Uri uri = Uri.fromFile(bookFile);
            UploadTask uploadTask = storageReference.putFile(uri);
            uploadTask.addOnSuccessListener(onSuccessListener)
                    .addOnFailureListener(new OnFailureListener() {
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

    public static void uploadBackgroundImage(final Activity activity, Bitmap OriginalImage){
        try {
            //Upload Image To Fire-Store
            String path = activity.getString(R.string.FIRESTORAGE_ROOT_USERIMAGES) + "/" + activity.getString(R.string.FIRESTORAGE_ROOT_PROFILE_BACKGROUND_SUFFIX) + FirebaseUtils.firebaseAuth.getCurrentUser().getUid();
            final StorageReference storageReference = FirebaseStorage.getInstance().getReference().child(path);

            //Convert Image To ByteArray
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            OriginalImage.compress(Bitmap.CompressFormat.JPEG, activity.getResources().getInteger(R.integer.FIRESTORAGE_UPLOAD_QUALITY_IMAGE) , byteArrayOutputStream);
            byte[] data = byteArrayOutputStream.toByteArray();
            //region RUN UPLOAD IMAGE
            UploadTask uploadTask = storageReference.putBytes(data);

            uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                @Override
                public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                    if (!task.isSuccessful()) {
                        throw task.getException();
                    }
                    // Continue with the task to get the download URL
                    return storageReference.getDownloadUrl();
                }
            }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                @Override
                public void onComplete(@NonNull Task<Uri> task) {
                    if(task.isSuccessful()){
                        //region Save To Database
                        Uri downloadUrl = task.getResult();
                        String Root = activity.getString(R.string.FIRESTORE_ROOT_USERDETAILS) + "/" + FirebaseUtils.firebaseAuth.getCurrentUser().getUid();
                        Map<String, Object> map = new HashMap<>();
                        map.put(activity.getString(R.string.F_USERDETAILS_BACKGROUNDURL), downloadUrl.toString());

                        FirebaseUtils.FirestroreSetData(Root, map, new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                Log.i(TAG, "SAVE BACKGROUND PROFILE IMAGE TO DATABASE");
                            }
                        }, new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Log.e(TAG, e.toString());
                            }
                        });
                        //endregion
                    }
                    else{
                        Log.e(TAG, "Task Does not Complete");
                    }
                }
            });


            //region OLD SYS
            /*
            uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Log.e(TAG, e.toString());
                }
            });
            */
            //endregion
            //endregion
        }
        catch (Exception ex){
            Log.e(TAG, ex.toString());
        }
    }

    public static void SetBookToLocalDrive(String bookUrl, String extension,String BOOKID, OnProgressListener<FileDownloadTask.TaskSnapshot> progressListener, OnCompleteListener<FileDownloadTask.TaskSnapshot> completeListener, OnFailureListener failureListener){
        try {
            String FilePathServer = bookUrl + "/" + BOOKID + extension;
            FirebaseStorage storage = FirebaseStorage.getInstance();
            StorageReference reference = storage.getReference().child(FilePathServer);

            //Save Image To Folder
            String root = Environment.getExternalStorageDirectory().getAbsolutePath() + "/.ppstore/";
            String fileStr = BOOKID + extension;

            File localFile = FileHelper.SaveFile(root, fileStr); //new File(root, fileStr);
            reference.
            getFile(localFile).
            addOnProgressListener(progressListener).
            addOnCompleteListener(completeListener).
            addOnFailureListener(failureListener);
        }
        catch (Exception ex){
            Log.e(TAG, ex.toString());
        }
    }

    public static void SetEmail(FirebaseAuth firebaseAuth, String email){
        FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();
        if(firebaseUser != null){

        }
    }

    public static void SetNewUserDetails(FirebaseAuth firebaseAuth, String RootPath){
        try{
            String DocPath = RootPath + "/" + firebaseAuth.getCurrentUser().getUid();
            DocumentReference DR = FirebaseFirestore.getInstance().document(DocPath);
            //User Details
            String UserID = firebaseAuth.getCurrentUser().getUid(), About = "about me", Gender = "male", Backgroundurl = "fuck";
            //Set Map
            Map<String, Object> dataToSave = new HashMap<String, Object>();
            dataToSave.put("uid", UserID);
            dataToSave.put("about", About);
            dataToSave.put("gender", Gender);
            dataToSave.put("backgroundurl",Backgroundurl);

            //Set User Details in firestore
            DR.set(dataToSave).addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void aVoid) {
                    Log.i(TAG, "Data Save");
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Log.e(TAG, "Data Error: " + e.toString());
                }
            });
        }
        catch (Exception ex){
            Log.e(TAG, ex.toString());
        }
    }

    public static void FirestroreSetData(String RootPath, Map<String, Object> dataToSave, OnSuccessListener<Void> onSuccessListener, OnFailureListener onFailureListener){
        try{
            DocumentReference DR = FirebaseFirestore.getInstance().document(RootPath);
            DR.set(dataToSave, SetOptions.merge()).addOnSuccessListener(onSuccessListener).addOnFailureListener(onFailureListener);
        }
        catch (Exception ex){
            Log.e(TAG, ex.toString());
        }
    }

    public static void FirestoreGetData(String RootPath, OnSuccessListener<DocumentSnapshot> onSuccessListener, OnFailureListener onFailureListener){
        try{
            DocumentReference DR = FirebaseFirestore.getInstance().document(RootPath);
            DR.get().addOnSuccessListener(onSuccessListener).addOnFailureListener(onFailureListener);
        }
        catch (Exception ex){
            Log.e(TAG, ex.toString());
        }
    }

    public static ListenerRegistration FirestoreGetAllData(Query query, Activity activity, EventListener<QuerySnapshot> querySnapshotEventListener){
        try{
            return query.addSnapshotListener(activity, querySnapshotEventListener);
        }
        catch (Exception ex){
            Log.e(TAG, ex.toString());
            return null;
        }
    }

    public static void BookCounter(String Root, final String CounterStrLink, final int value){
        try{
            FirebaseFirestore firebaseFirestore = FirebaseFirestore.getInstance();
            final DocumentReference documentReference = firebaseFirestore.document(Root);
            firebaseFirestore.runTransaction(new Transaction.Function<Void>() {
                @Nullable
                @Override
                public Void apply(@NonNull Transaction transaction) throws FirebaseFirestoreException {
                    DocumentSnapshot snapshot = transaction.get(documentReference);
                    Integer counter = Integer.valueOf(snapshot.get(CounterStrLink).toString());
                    counter += value; //Increment or Decrement By
                    transaction.update(documentReference, CounterStrLink, counter);
                    //success
                    return null;
                }
            }).addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void aVoid) {
                    Log.e(TAG, "Transaction success!");
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Log.e(TAG, "Transaction failure.", e);
                }
            });
        }
        catch (Exception ex){
            Log.e(TAG, ex.toString());
        }
    }

    public static void FirestoreGetAllSnapshot(CollectionReference collectionReference, EventListener<QuerySnapshot> eventListener){
        try{
            collectionReference.addSnapshotListener(eventListener);
        }
        catch (Exception ex)
        {
            Log.e(TAG, ex.toString());
        }

    }

    public static void FirestoreDeleteData(String RootPath, OnSuccessListener<Void> onSuccessListener, OnFailureListener onFailureListener){
        try{
            DocumentReference DR = FirebaseFirestore.getInstance().document(RootPath);
            DR.delete().addOnSuccessListener(onSuccessListener).addOnFailureListener(onFailureListener);
        }
        catch (Exception ex){
            Log.e(TAG, ex.toString());
        }
    }

    public static void sendTokenToServer(String token, Activity activity){
        try{
            String ID = FirebaseUtils.firebaseAuth.getCurrentUser().getUid();
            String email = FirebaseUtils.firebaseAuth.getCurrentUser().getEmail();
            String Root =activity.getString(R.string.FIRESTORE_ROOT_USERDETAILS) + "/" + ID;
            Map<String, Object> map = new HashMap<>();
            map.put(activity.getString(R.string.F_USERDETAILS_TOKEN), token);
            map.put(activity.getString(R.string.F_USERDETAILS_EMAIl), email);//Set email

            FirebaseUtils.FirestroreSetData(Root, map, new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            Log.i(TAG, "CODE: TR - Ok");
                        }
                    }
                    , new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Log.e(TAG, "ERROR:"+ e.toString());
                        }
                    });

        }
        catch (Exception ex){
            Log.e(TAG, ex.toString());
        }
    }

    public static void RunServerFunction(String FunctionName, Map<String, Object> map, Continuation<HttpsCallableResult, String> continuation, OnFailureListener onFailureListener){
        try{
            FirebaseFunctions.getInstance().getHttpsCallable(FunctionName).call(map).continueWith(continuation).addOnFailureListener(onFailureListener);
        }
        catch (Exception ex){
            Log.e(TAG, ex.toString());
        }
    }
}