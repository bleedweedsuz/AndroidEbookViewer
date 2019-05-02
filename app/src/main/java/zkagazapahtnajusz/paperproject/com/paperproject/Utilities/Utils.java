package zkagazapahtnajusz.paperproject.com.paperproject.Utilities;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Environment;
import android.os.PowerManager;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.design.widget.AppBarLayout;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.text.Html;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.request.RequestListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.UploadTask;
import com.lelloman.identicon.classic.ClassicIdenticonDrawable;
import com.thefinestartist.finestwebview.FinestWebView;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.NavigableMap;
import java.util.Random;
import java.util.TreeMap;
import java.util.UUID;

import ir.mahdi.mzip.zip.ZipArchive;
import zkagazapahtnajusz.paperproject.com.paperproject.Activity_BookDetails;
import zkagazapahtnajusz.paperproject.com.paperproject.Activity_Dictionary;
import zkagazapahtnajusz.paperproject.com.paperproject.Activity_Friend_Profile;
import zkagazapahtnajusz.paperproject.com.paperproject.Activity_Home;
import zkagazapahtnajusz.paperproject.com.paperproject.Activity_SignIn;
import zkagazapahtnajusz.paperproject.com.paperproject.Activity_UserProfile;
import zkagazapahtnajusz.paperproject.com.paperproject.Admin.AdminUtils;
import zkagazapahtnajusz.paperproject.com.paperproject.Model.BookItems;
import zkagazapahtnajusz.paperproject.com.paperproject.Model.ProgressDialogInterface;
import zkagazapahtnajusz.paperproject.com.paperproject.R;
import zkagazapahtnajusz.paperproject.com.paperproject.ReaderEngine.Reader_EPUB;
import zkagazapahtnajusz.paperproject.com.paperproject.ReaderEngine.Reader_PDF;
import zkagazapahtnajusz.paperproject.com.paperproject.ReaderEngine.Reader_TXT;

/**
 * Created by Zw4R-TSTUD10 2018/4/1 [2018, April 1]
 */

public class Utils {
    private static String TAG = "UTILS";
    public enum Device {PHONE, TABLET}
    public static Device device = Device.PHONE;
    public static boolean isFullScreen = false;
    private static int PHONE_COUNT = 4;
    private static int TABLET_COUNT = 10;
    public static int CURRENT_GRID_COUNT;
    private static AlertDialog progressDialog;
    public static ImageView ProfileImage = null;
    static PowerManager.WakeLock wakeLock;
    public static BookItems currentBookItemsFromOpen;

    public static int px2Dp(int pxl, Resources resources) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, pxl, resources.getDisplayMetrics());
    }

    public static void SetProgressDialogIndeterminate(final Context context, String message) {
        try {
            LayoutInflater layoutInflater = ((Activity) context).getLayoutInflater();
            View view = layoutInflater.inflate(R.layout.progress_dialog_layout, null);

            TextView title = view.findViewById(R.id.title);
            title.setText(message);

            AlertDialog.Builder builder = new AlertDialog.Builder(context);
            builder.setView(view);
            builder.setCancelable(true);
            builder.setPositiveButton("Cancel", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    ((Activity) context).finish();
                    dialog.dismiss();
                }
            });
            progressDialog = builder.create();
            progressDialog.setOnShowListener(new DialogInterface.OnShowListener() {
                @Override
                public void onShow(DialogInterface dialog) {
                    progressDialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(context.getResources().getColor(R.color.progressDialogPositiveTextColor));
                }
            });
            progressDialog.show();
        } catch (Exception ex) {
            Log.e(TAG, ex.toString());
        }
    }

    public static void SetProgressDialogIndeterminate(final Context context, String message, DialogInterface.OnClickListener onClickListener) {
        try {
            LayoutInflater layoutInflater = ((Activity) context).getLayoutInflater();
            View view = layoutInflater.inflate(R.layout.progress_dialog_layout, null);

            TextView title = view.findViewById(R.id.title);
            title.setText(message);

            AlertDialog.Builder builder = new AlertDialog.Builder(context);
            builder.setView(view);
            builder.setCancelable(false);
            builder.setPositiveButton("Cancel", onClickListener);
            progressDialog = builder.create();
            progressDialog.setOnShowListener(new DialogInterface.OnShowListener() {
                @Override
                public void onShow(DialogInterface dialog) {
                    progressDialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(context.getResources().getColor(R.color.progressDialogPositiveTextColor));
                }
            });
            progressDialog.show();
        } catch (Exception ex) {
            Log.e(TAG, ex.toString());
        }
    }

    public static void SetProgressDialogIndeterminateWithOutCancel(final Context context, String message) {
        try {
            LayoutInflater layoutInflater = ((Activity) context).getLayoutInflater();
            View view = layoutInflater.inflate(R.layout.progress_dialog_layout_2, null);

            TextView title = view.findViewById(R.id.title);
            title.setText(message);

            AlertDialog.Builder builder = new AlertDialog.Builder(context);
            builder.setView(view);
            builder.setCancelable(false);

            progressDialog = builder.create();
            progressDialog.setOnShowListener(new DialogInterface.OnShowListener() {
                @Override
                public void onShow(DialogInterface dialog) {
                    progressDialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(context.getResources().getColor(R.color.progressDialogPositiveTextColor));
                }
            });
            progressDialog.show();
        } catch (Exception ex) {
            Log.e(TAG, ex.toString());
        }
    }

    public static void SetProgressDialog(Context context, String message, ProgressDialogInterface progressDialogInterface, DialogInterface.OnClickListener cancelButton) {
        final ProgressDialog progressDialog = new ProgressDialog(context);
        progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        progressDialog.setMessage(message);
        progressDialog.setMax(100);
        progressDialog.setCancelable(true);
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.setButton(DialogInterface.BUTTON_POSITIVE, "CANCEL", cancelButton);
        progressDialog.show();
        progressDialogInterface.callback(progressDialog);
    }

    public static void UnSetProgressDialogIndeterminate() {
        if (progressDialog != null) {
            progressDialog.dismiss();
        }
    }

    private static Bitmap drawableToBitmap(Drawable drawable, int widthPixels, int heightPixels) {
        Bitmap src = Bitmap.createBitmap(widthPixels, heightPixels, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(src);
        drawable.setBounds(0, 0, widthPixels, heightPixels);
        drawable.draw(canvas);
        return src;
    }

    public static void SetImageViaRes(Activity activity, int photoUri, ImageView target) {
        GlideApp.with(activity).load(photoUri).placeholder(R.drawable.imageplaceholder_info).error(R.drawable.imageplaceholder_error).fitCenter().into(target);
    }

    public static void SetImageViaUri(Activity activity, String photoUri, ImageView target, boolean isShaded, boolean isCropCenter, ColorDrawable placeHolder, ColorDrawable errorPlaceHolder, RequestListener<Drawable> requestListener) {
        try {
            if (photoUri != null && !photoUri.equals("")) {
                Uri serverImageUri = Uri.parse(photoUri);
                if (requestListener != null) {
                    if (!isCropCenter) {
                        GlideApp.with(activity).load(serverImageUri).listener(requestListener).placeholder(placeHolder).error(errorPlaceHolder).fitCenter().into(target);
                    } else {
                        GlideApp.with(activity).load(serverImageUri).listener(requestListener).placeholder(placeHolder).error(errorPlaceHolder).centerCrop().into(target);
                    }
                } else {
                    if (!isCropCenter) {
                        GlideApp.with(activity).load(serverImageUri).placeholder(placeHolder).error(errorPlaceHolder).fitCenter().into(target);
                    } else {
                        GlideApp.with(activity).load(serverImageUri).placeholder(placeHolder).error(errorPlaceHolder).centerCrop().into(target);
                    }
                }
                if (isShaded) {
                    target.setColorFilter(Color.rgb(160, 160, 160), android.graphics.PorterDuff.Mode.MULTIPLY);
                }
            } else {
                GetClassicIdenticonDrawable(target, isShaded);
            }
        } catch (Exception ex) {
            Log.e(TAG, ex.toString());
        }
    }

    private static void GetClassicIdenticonDrawable(ImageView imageView, boolean isShaded) {
        int WIDTH = 800, HEIGHT = 800;
        String HASH_STRING = FirebaseUtils.firebaseAuth.getCurrentUser().getUid() + "" + FirebaseUtils.firebaseAuth.getCurrentUser().getUid();
        int HASH_INTEGER = HASH_STRING.hashCode();
        Drawable drawable = new ClassicIdenticonDrawable(WIDTH, HEIGHT, HASH_INTEGER);
        imageView.setImageBitmap(Utils.drawableToBitmap(drawable, WIDTH, HEIGHT));
        imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
        if (isShaded) {
            imageView.setColorFilter(Color.rgb(160, 160, 160), android.graphics.PorterDuff.Mode.MULTIPLY);
        }
    }

    public static void SetImageViaUri(Activity activity, String UID, String photoUri, ImageView imageTarget, boolean isShaded, boolean isCropCenter, ColorDrawable placeHolder, ColorDrawable errorPlaceHolder, RequestListener<Drawable> requestListener) {
        if (photoUri != null && !photoUri.equals("")) {
            Uri serverImageUri = Uri.parse(photoUri);
            if (requestListener != null) {
                if (!isCropCenter) {
                    GlideApp.with(activity).load(serverImageUri).listener(requestListener).placeholder(placeHolder).error(errorPlaceHolder).fitCenter().into(imageTarget);
                } else {
                    GlideApp.with(activity).load(serverImageUri).listener(requestListener).placeholder(placeHolder).error(errorPlaceHolder).centerCrop().into(imageTarget);
                }
            } else {
                if (!isCropCenter) {
                    GlideApp.with(activity).load(serverImageUri).placeholder(placeHolder).error(errorPlaceHolder).fitCenter().into(imageTarget);
                } else {
                    GlideApp.with(activity).load(serverImageUri).placeholder(placeHolder).error(errorPlaceHolder).centerCrop().into(imageTarget);
                }
            }
            if (isShaded) {
                imageTarget.setColorFilter(Color.rgb(160, 160, 160), android.graphics.PorterDuff.Mode.MULTIPLY);
            }
        } else {
            GetClassicIdenticonDrawable(imageTarget, UID, isShaded);
        }
    }

    private static void GetClassicIdenticonDrawable(ImageView imageView, String UID, boolean isShaded) {
        int WIDTH = 800, HEIGHT = 800;
        String HASH_STRING = UID + UID;
        int HASH_INTEGER = HASH_STRING.hashCode();
        Drawable drawable = new ClassicIdenticonDrawable(WIDTH, HEIGHT, HASH_INTEGER);
        imageView.setImageBitmap(Utils.drawableToBitmap(drawable, WIDTH, HEIGHT));
        imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);

        if (isShaded) {
            imageView.setColorFilter(Color.rgb(160, 160, 160), android.graphics.PorterDuff.Mode.MULTIPLY);
        }
    }

    public static void SetInputDialogBox(Activity activity, int multiLine, String mainTitle, String title, String text, int inputType, DialogInterface.OnClickListener onClickListener) {
        try {
            //set layout inflater
            LayoutInflater layoutInflater = LayoutInflater.from(activity);
            View promotView = layoutInflater.inflate(R.layout.custom_input_dialog, null);
            //Set Title
            TextView textView = promotView.findViewById(R.id.titleText);
            textView.setText(title);

            EditText editText = promotView.findViewById(R.id.inputEditText);
            if (multiLine > 1) {
                editText.setSingleLine(false);
                editText.setLines(multiLine);
                editText.setGravity(Gravity.TOP);
            } else {
                editText.setInputType(inputType);
                editText.setSingleLine(true);
            }
            editText.setText(text);

            //Set Dialog
            AlertDialog.Builder builder = new AlertDialog.Builder(activity);
            if (title.length() > 0) {
                builder.setTitle(mainTitle);
            }
            builder.setView(promotView);
            builder.setPositiveButton("Done", onClickListener);
            builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            });
            builder.create().show();
        } catch (Exception ex) {
            Log.e(TAG, ex.toString());
        }
    }

    public static void SetDialogBox(Activity activity, String mainTitle, String text, DialogInterface.OnClickListener onClickListener) {
        try {
            //set layout inflater
            LayoutInflater layoutInflater = LayoutInflater.from(activity);
            View promotView = layoutInflater.inflate(R.layout.custom_dialog, null);
            //Set data
            TextView textView = promotView.findViewById(R.id.dialogdata);
            textView.setText(Html.fromHtml(text));

            //Set Dialog
            AlertDialog.Builder builder = new AlertDialog.Builder(activity);
            builder.setTitle(mainTitle);
            builder.setView(promotView);
            builder.setPositiveButton("Done", onClickListener);
            builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            });
            builder.create().show();
        } catch (Exception ex) {
            Log.e(TAG, ex.toString());
        }
    }

    public static void SetPhoneDialogBox(Activity activity, String mainTitle, String buttonTitle, int layoutView, DialogInterface.OnClickListener onClickListener) {
        try {
            //set layout inflater
            LayoutInflater layoutInflater = LayoutInflater.from(activity);
            View view = layoutInflater.inflate(layoutView, null);

            //Set Dialog
            AlertDialog.Builder builder = new AlertDialog.Builder(activity);
            builder.setTitle(mainTitle);
            builder.setView(view);
            builder.setPositiveButton(buttonTitle, onClickListener);
            builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            });
            builder.create().show();
        } catch (Exception ex) {
            Log.e(TAG, ex.toString());
        }
    }


    public interface SetPasswordInputDialogBox_interfacecallback {
        void updatePassword(AlertDialog alertDialog);
    }

    public static void SetPasswordInputDialogBox(Activity activity, String mainTitle, final SetPasswordInputDialogBox_interfacecallback setPasswordInputDialogBox_interfacecallback) {
        try {
            //set layout inflater
            LayoutInflater layoutInflater = LayoutInflater.from(activity);
            View view = layoutInflater.inflate(R.layout.custom_password_input_dialog, null);

            //Set Dialog
            final AlertDialog.Builder builder = new AlertDialog.Builder(activity)
                    .setTitle(mainTitle)
                    .setView(view)
                    .setPositiveButton("Done", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            setPasswordInputDialogBox_interfacecallback.updatePassword((AlertDialog) dialog);
                        }
                    })
                    .setNegativeButton("Cancel", null);

            final AlertDialog alertDialog = builder.create();
            //Look for Action Done
            ((EditText) view.findViewById(R.id.inputrepasswordEditText)).setOnEditorActionListener(new EditText.OnEditorActionListener() {
                @Override
                public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                    if (actionId == EditorInfo.IME_ACTION_DONE) {
                        setPasswordInputDialogBox_interfacecallback.updatePassword(alertDialog);
                        alertDialog.dismiss();
                        return true;
                    }
                    return false;
                }
            });
            alertDialog.show();
        } catch (Exception ex) {
            Log.e(TAG, ex.toString());
        }
    }

    public static void SetSingleChoiceInputDialogBox(Activity activity, String mainTitle, CharSequence[] picker, String SelectedCharsequence, DialogInterface.OnClickListener onClickListener) {
        try {
            int index = 0;
            for (int i = 0; i < picker.length; i++) {
                if (picker[i].toString().equals(SelectedCharsequence)) {
                    index = i;
                    break;
                }
            }
            //Set Dialog
            AlertDialog.Builder builder = new AlertDialog.Builder(activity)
                    .setTitle(mainTitle)
                    .setSingleChoiceItems(picker, index, onClickListener);
            builder.create().show();
        } catch (Exception ex) {
            Log.e(TAG, ex.toString());
        }
    }

    public static String UUIDToken() {
        String _token = "";
        try {
            long msb = System.currentTimeMillis();
            long lsb = System.currentTimeMillis();
            UUID uuid = new UUID(msb, lsb);
            _token = uuid.toString();
        } catch (Exception ex) {
            Log.e(TAG, ex.toString());
        }
        return _token;
    }

    private static final NavigableMap<Long, String> suffixes = new TreeMap<>();

    static {
        suffixes.put(1_000L, "k");
        suffixes.put(1_000_000L, "M");
        suffixes.put(1_000_000_000L, "G");
        suffixes.put(1_000_000_000_000L, "T");
        suffixes.put(1_000_000_000_000_000L, "P");
        suffixes.put(1_000_000_000_000_000_000L, "E");
    }

    public static String Numberformat(long value) {
        //Long.MIN_VALUE == -Long.MIN_VALUE so we need an adjustment here
        if (value == Long.MIN_VALUE) return Numberformat(Long.MIN_VALUE + 1);
        if (value < 0) return "-" + Numberformat(-value);
        if (value < 1000) return Long.toString(value); //deal with easy case

        Map.Entry<Long, String> e = suffixes.floorEntry(value);
        Long divideBy = e.getKey();
        String suffix = e.getValue();

        long truncated = value / (divideBy / 10); //the number part of the output times 10
        boolean hasDecimal = truncated < 100 && (truncated / 10d) != (truncated / 10);
        return hasDecimal ? (truncated / 10d) + suffix : (truncated / 10) + suffix;
    }

    public static void DEVICE_CHECK(Activity activity) {
        try {
            DisplayMetrics metrics = new DisplayMetrics();
            activity.getWindowManager().getDefaultDisplay().getMetrics(metrics);

            float yInches = metrics.heightPixels / metrics.ydpi;
            float xInches = metrics.widthPixels / metrics.xdpi;
            double diagonalInches = Math.sqrt(xInches * xInches + yInches * yInches);

            if (diagonalInches >= 6.5) {
                // 6.5inch device or bigger
                CURRENT_GRID_COUNT = TABLET_COUNT;
            } else {
                // smaller device
                CURRENT_GRID_COUNT = PHONE_COUNT;
            }
        } catch (Exception ex) {
            Log.e(TAG, ex.toString());
        }
    }

    public static Bitmap ConvertImageViewToBitmap(ImageView imageView) {
        if (imageView != null) {
            try {
                return ((BitmapDrawable) imageView.getDrawable()).getBitmap();
            } catch (NullPointerException ex) {
                Log.e("ERROR", ex.toString());
            }
        }
        return null;
    }

    public static void GenerateRandombookAndAdd(final Activity activity, final boolean isNepali, final boolean isPrivate, final AdminUtils adminUtils) {
        try {
            Utils.SetProgressDialogIndeterminate(activity, "loading..");
            final String UUID = UUIDToken();
            final int currentIndex = randInt(0, 6);

            final String[] bookName = {"lorelei sutton a howl in the night", "Moby Dick", "Childrens Literatur", "Indexing For EDS and auth 3d", "Israelsailing", "My Media Lite", "Quiz Binding"};
            String[] bookLocation;
            String[] passKey = {"lorelei-sutton-a-howl-in-the-night", "moby-dick", "childrens-literature", "indexing-for-eds-and-auths-3md", "israelsailing", "mymedia_lite", "quiz-bindings"};

            final BookItems.BookFlag[] bookFlagLists = {BookItems.BookFlag.Free, BookItems.BookFlag.Store};
            final BookItems.BookFlag bookFlag = bookFlagLists[randInt(0, 1)];

            final String pKey;
            String extension;

            if (bookFlag == BookItems.BookFlag.Store) {
                bookLocation = new String[]{
                        "/storage/emulated/0/epub/lorelei-sutton-a-howl-in-the-night.zip",
                        "/storage/emulated/0/epub/moby-dick.zip",
                        "/storage/emulated/0/epub/childrens-literature.zip",
                        "/storage/emulated/0/epub/indexing-for-eds-and-auths-3md.zip",
                        "/storage/emulated/0/epub/israelsailing.zip",
                        "/storage/emulated/0/epub/mymedia_lite.zip",
                        "/storage/emulated/0/epub/quiz-bindings.zip"};
                pKey = passKey[currentIndex];
                extension = ".zip";
            } else if (bookFlag == BookItems.BookFlag.Free) {
                bookLocation = new String[]{
                        "/storage/emulated/0/epub/lorelei-sutton-a-howl-in-the-night.epub",
                        "/storage/emulated/0/epub/moby-dick.epub",
                        "/storage/emulated/0/epub/childrens-literature.epub",
                        "/storage/emulated/0/epub/indexing-for-eds-and-auths-3md.epub",
                        "/storage/emulated/0/epub/israelsailing.epub",
                        "/storage/emulated/0/epub/mymedia_lite.epub",
                        "/storage/emulated/0/epub/quiz-bindings.epub"};
                pKey = "";
                extension = ".epub";
            } else {
                //do nothing..
                return;
            }

            final String bookL = bookLocation[currentIndex];

            FirebaseUtils.uploadFile(activity, extension, UUID, bookL, new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    try {
                        String[] genreList = {"Science fiction", "Satire", "Drama", "Action and Adventure", "Romance", "Mystery", "Horror", "Self help", "Health", "Guide", "Travel", "Childrens", "Religion, Spirituality & New Age", "Science", "History", "Math", "Anthology", "Poetry", "Encyclopedias", "Dictionaries", "Comics", "Art", "Cookbooks", "Diaries", "Journals", "Prayer books", "Series", "Trilogy", "Biographies", "Autobiographies", "Fantasy"};

                        //region Image List
                        String[] imageList = {
                                "https://firebasestorage.googleapis.com/v0/b/projectpaper-468fc.appspot.com/o/bookthumblin%2Faa.jpg?alt=media&token=dcdb2a49-b739-4992-add8-6b4490fe40a0",
                                "https://firebasestorage.googleapis.com/v0/b/projectpaper-468fc.appspot.com/o/bookthumblin%2Faaa.jpg?alt=media&token=135dc8ce-396c-4a2b-b757-57de0c453275",
                                "https://firebasestorage.googleapis.com/v0/b/projectpaper-468fc.appspot.com/o/bookthumblin%2Fbb.jpg?alt=media&token=d7601259-68c9-47f1-b17c-f35a72b9cd63",
                                "https://firebasestorage.googleapis.com/v0/b/projectpaper-468fc.appspot.com/o/bookthumblin%2Fbbb.jpg?alt=media&token=13290c17-5a13-48c1-a8ec-07a236fb3bee",
                                "https://firebasestorage.googleapis.com/v0/b/projectpaper-468fc.appspot.com/o/bookthumblin%2Fcc.jpg?alt=media&token=c827ad37-4a07-4a48-b15b-18604638920d",
                                "https://firebasestorage.googleapis.com/v0/b/projectpaper-468fc.appspot.com/o/bookthumblin%2Fccc.jpg?alt=media&token=8b048813-2210-4ee7-ba07-86c4fdf1aade",
                                "https://firebasestorage.googleapis.com/v0/b/projectpaper-468fc.appspot.com/o/bookthumblin%2Fddd.jpg?alt=media&token=542be633-1388-4a20-a92c-26600ef81d98",
                                "https://firebasestorage.googleapis.com/v0/b/projectpaper-468fc.appspot.com/o/bookthumblin%2Ffff.jpg?alt=media&token=7d92d1e5-2121-4216-94fe-226c99bb90cc",
                                "https://firebasestorage.googleapis.com/v0/b/projectpaper-468fc.appspot.com/o/bookthumblin%2Fggg.jpg?alt=media&token=11cd9e3f-578d-4782-9878-7f932f24fd48"
                        };
                        //endregion

                        String currencyStr = "Rs";
                        String Root = activity.getString(R.string.FIRESTORE_ROOT_BOOKS) + "/" + UUID;
                        String ImageUrl = imageList[randInt(0, imageList.length - 1)];
                        final String BookTitle = bookName[currentIndex];
                        final String Suffix = BookTitle.substring(0, 1).toUpperCase();
                        Map<String, Object> map = new HashMap<>();
                        map.put(activity.getString(R.string.F_BOOKS_BOOKID), UUID);
                        map.put(activity.getString(R.string.F_BOOKS_TITLE), BookTitle); //SET TITLE
                        map.put(activity.getString(R.string.F_BOOKS_DESCRIPTION), randGetStr(randInt(0, 10), randInt(200, 400), isNepali)); //SET DESCRIPTION
                        map.put(activity.getString(R.string.F_BOOKS_AUTHOR), randGetStr(randInt(0, 10), randInt(20, 30), isNepali).trim()); //SET AUTHOR
                        List<String> genre = new ArrayList<>();
                        RandomNumber randomNumber = new RandomNumber(genreList.length - 1);
                        for (int i = 0; i < 4; i++) {
                            genre.add(genreList[randomNumber.nextRnd()]);
                        }
                        map.put(activity.getString(R.string.F_BOOKS_GENRE), genre);
                        map.put(activity.getString(R.string.F_BOOKS_SUFFIX), Suffix);
                        map.put(activity.getString(R.string.F_BOOKS_PRICE), randInt(100, 1000));
                        map.put(activity.getString(R.string.F_BOOKS_CURRENCYSTR), currencyStr);
                        map.put(activity.getString(R.string.F_BOOKS_BOOKFLAG), bookFlag.toString());
                        map.put(activity.getString(R.string.F_BOOKS_FILETYPE), BookItems.FileType.EPUB.toString());
                        map.put(activity.getString(R.string.F_BOOKS_IMAGEURL), ImageUrl);
                        map.put(activity.getString(R.string.F_BOOKS_DATE), new Timestamp(Calendar.getInstance().getTime()));
                        map.put(activity.getString(R.string.F_BOOKS_COUNTERDOWNLOAD), 0);
                        map.put(activity.getString(R.string.F_BOOKS_COUNTERLIKE), 0);
                        map.put(activity.getString(R.string.F_BOOKS_COUNTERVIEW), 0);
                        map.put(activity.getString(R.string.F_BOOKS_COUNTERCOMMENT), 0);
                        map.put(activity.getString(R.string.F_BOOKS_PUBLICKEY), pKey);


                        if (isPrivate) {
                            map.put(activity.getString(R.string.F_BOOKS_STATUS), BookItems.Status.Private.toString());
                        } else {
                            map.put(activity.getString(R.string.F_BOOKS_STATUS), BookItems.Status.Public.toString());
                        }
                        FirebaseUtils.FirestroreSetData(Root, map, new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                adminUtils.AddToConsole("Added New Book:" + BookTitle + " - " + bookFlag.toString());
                            }
                        }, new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Log.e(TAG, "ERROR: NO BOOK ADDED");
                            }
                        });

                        Utils.UnSetProgressDialogIndeterminate();
                    } catch (Exception ex) {
                        Toast.makeText(activity, "ERROR: " + ex.toString(), Toast.LENGTH_SHORT).show();
                        Utils.UnSetProgressDialogIndeterminate();
                        Log.e(TAG, ex.toString());
                    }
                }
            });
        } catch (Exception ex) {
            Utils.UnSetProgressDialogIndeterminate();
            Log.e(TAG, ex.toString());
        }
    }

    private static void CheckSuffix(final Activity activity, final String suffix) {
        try {
            final String Root = activity.getString(R.string.FIRESTORE_ROOT_BOOKSUFFIX);
            CollectionReference db = FirebaseFirestore.getInstance().collection(Root);
            Query query = db.whereEqualTo(activity.getString(R.string.F_BOOKSUFFIX_ID), suffix);

            FirebaseUtils.FirestoreGetAllData(query, activity, new EventListener<QuerySnapshot>() {
                @Override
                public void onEvent(QuerySnapshot queryDocumentSnapshots, FirebaseFirestoreException e) {
                    if (queryDocumentSnapshots.isEmpty()) {
                        //not exists
                        //set suffix in system
                        String temp_root = Root + "/" + suffix;
                        Map<String, Object> map = new HashMap<>();
                        map.put(activity.getString(R.string.F_BOOKSUFFIX_ID), suffix);
                        FirebaseUtils.FirestroreSetData(temp_root, map, new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                Log.i(TAG, "SUFFIX SET--------------->");
                            }
                        }, null);
                    } else {
                        Log.i(TAG, "SUFFIX ALREADY SET<<<<<<<<->");
                    }
                }
            });
        } catch (Exception ex) {
            Log.e(TAG, ex.toString());
        }
    }

    public static int randInt(int min, int max) {
        Random rand = new Random();
        return rand.nextInt((max - min) + 1) + min;
    }

    private static String randGetStr(int start, int end, boolean isNepali) {
        if (isNepali) {
            String src = "\"कुल\" कोलमको तल त्यस विकिपीडियामा लेखिएका सबै पृष्ठहरुको संख्या छ, लेख (प्रत्येक विकिको आधिकारिक लेख गणना) र अलेख (उपयोगकर्ता पृष्ठ, चित्रहरू, वार्ता पृष्ठ, \"परियोजना\" पृष्ठ, श्रेणीहरू, र साँचाहरू)।\"गहिराई\" कोलम ((सम्पादन/लेख) × (अलेख/सम्पादन) × (१ − स्टब-अनुपात)) यस कुराको सांकेतक छ कि कुनै विकिपीडिया कति उत्तमता छ, जसले यो दर्शाउँछ कि त्यसका लेख कति पल्ट अध्यावधिक गरिन्छन्। ध्यान रहोस् यसको अर्थ शैक्षणिक उत्तमतासित छैन (स्पष्ट कुरा के छ भने गणितीय रूपले कुनै पनि प्रकारले नाप्न सकिंदैन), तर विकिपीडियाली उत्तमतासित छ, अर्थात सहयोगको गहिराई-एक सांकेतक जुन विकिपीडियाको लागि धेरै प्रासंगिक छ। १ ०० ००० लेखहरूबाट कम विकिपीडियाहरूको ३०० भन्दा बढी गहिराइ अप्रासंगिक रूपले खारेज गरिन सक्छ ।";
            return src.substring(start, end);
        } else {
            String src = "Lorem ipsum dolor sit amet, consectetur adipiscing elit. Maecenas gravida est et rutrum eleifend. Maecenas congue mollis consequat. Sed pharetra sem turpis, nec eleifend erat varius eget. Integer fringilla cursus quam, vitae consectetur sapien vestibulum sit amet. Cras euismod nisl risus, sed posuere lacus placerat at. Maecenas elementum lectus et ligula cursus, eu laoreet velit commodo. Aenean lacinia tellus eget ligula vestibulum accumsan. Vestibulum a erat augue. Integer vehicula gravida magna in congue. Sed quis eros quis libero aliquam consequat sed id sapien. Sed maximus odio vel semper tincidunt. Curabitur velit tortor, cursus vel convallis ut, lacinia quis ligula. Morbi tristique libero in dolor consectetur, nec ultrices neque imperdiet. Proin in ipsum blandit lectus viverra condimentum. Etiam quis gravida leo. sce eget molestie augue, at consequat metus. Aenean gravida lorem diam, nec pulvinar enim dictum ac. Maecenas mi nisl, pharetra non malesuada sit amet, elementum ac tellus. Class aptent taciti sociosqu ad litora torquent per conubia nostra, per inceptos himenaeos. Curabitur odio enim, fringilla ut pretium sed, pharetra ut arcu. Suspendisse malesuada aliquam pellentesque. Cras eu lacus quis nibh pretium laoreet a rutrum enim. Morbi congue laoreet nisi, vel iaculis erat pulvinar nec. Fusce metus lectus, finibus dictum erat non, rutrum iaculis lorem. Sed ornare, nisi quis gravida viverra, mi lacus porttitor velit, vel consectetur odio nunc in libero. Nunc ut tortor elit. Donec hendrerit quam in enim ullamcorper dignissim. Integer interdum magna nec augue porttitor scelerisque. Pellentesque facilisis elementum tortor, pellentesque semper velit dignissim non. Nunc interdum blandit purus id pulvinar. Donec vel interdum velit, nec pharetra erat. Curabitur viverra viverra ex et fringilla. Sed eu elementum purus, nec finibus felis. Vestibulum et augue vel tortor dictum pellentesque. Etiam euismod ex rutrum nisi hendrerit mollis. Quisque hendrerit ipsum a viverra vestibulum. Suspendisse posuere, risus id rhoncus bibendum, elit nisl fermentum risus, id tempus magna orci non libero. Proin rhoncus libero justo, eget lacinia ex elementum quis. Donec feugiat ultricies dolor, in varius lacus tincidunt facilisis. Orci varius natoque penatibus et magnis dis parturient montes, nascetur ridiculus mus. Phasellus arcu mauris, egestas vel enim et, consectetur iaculis augue. Vestibulum nisl quam, suscipit sed nisl eu, mollis finibus urna. Vivamus tempor euismod justo ac eleifend. In hac habitasse platea dictumst. Vestibulum non lectus nec nunc dictum faucibus vel porttitor enim. Praesent elit orci, ultrices malesuada tristique sit amet, luctus id nisl. Duis malesuada ornare ipsum, eget vulputate magna dignissim eu. Donec fermentum facilisis urna, scelerisque vestibulum dolor aliquam eget. Phasellus eu imperdiet risus. Etiam venenatis finibus enim condimentum porttitor. Cras elementum dui ex, bibendum varius odio bibendum ac Nunc rutrum, risus vitae fringilla volutpat, orci tellus interdum metus, molestie ultrices massa tellus nec erat. Donec accumsan hendrerit velit eget pulvinar. Etiam euismod massa in dolor pharetra, id bibendum sapien lobortis. In hac habitasse platea dictumst. Aliquam at lorem massa. Nulla accumsan lectus at erat tristique, lobortis egestas felis tristique. Aenean consectetur dolor quis laoreet aliquet. Suspendisse placerat lacus sit amet volutpat placerat. Donec molestie dapibus nibh, quis convallis ante lacinia quis.";
            return src.substring(start, end);
        }

    }

    public static class RandomNumber {
        private final Random random = new Random();
        private final int range;
        private int previous;

        RandomNumber(int range) {
            this.range = range;
        }

        int nextRnd() {
            if (previous == 0) return previous = random.nextInt(range) + 1;
            final int rnd = random.nextInt(range - 1) + 1;
            return previous = (rnd < previous ? rnd : rnd + 1);
        }
    }

    public static ArrayList<File> SearchFileInLocalDirectory(Context context) {
        try {
            ArrayList<File> fileArrayList = new ArrayList<>();

            //region documents
            if (SharedPreferenceUtils.Setting_GetData(context, context.getString(R.string.LibrarySetting_ShowLocalDrive_Document), true)) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                    File rootDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS);
                    if (rootDir.exists()) {
                        for (File file : rootDir.listFiles()) {
                            if (file.isFile() && (file.getName().endsWith(".epub") || file.getName().endsWith(".EPUB") || file.getName().endsWith(".doc") || file.getName().endsWith(".DOC") || file.getName().endsWith(".xls") || file.getName().endsWith(".XLS") || file.getName().endsWith(".xlsx") || file.getName().endsWith(".XLSX") || file.getName().endsWith(".ppt") || file.getName().endsWith(".PPT") || file.getName().endsWith(".pptx") || file.getName().endsWith(".PPTX") || file.getName().endsWith(".pdf") || file.getName().endsWith(".PDF") || file.getName().endsWith(".txt") || file.getName().endsWith(".TXT"))) {
                                //Add File
                                fileArrayList.add(file);

                            }
                        }
                    }
                }
            }
            //endregion

            //region downloads
            if (SharedPreferenceUtils.Setting_GetData(context, context.getString(R.string.LibrarySetting_ShowLocalDrive_Download), true)) {
                File rootDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
                if (rootDir.exists()) {
                    for (File file : rootDir.listFiles()) {
                        if (file.isFile() && (file.getName().endsWith(".epub") || file.getName().endsWith(".EPUB") || file.getName().endsWith(".doc") || file.getName().endsWith(".DOC") || file.getName().endsWith(".xls") || file.getName().endsWith(".XLS") || file.getName().endsWith(".xlsx") || file.getName().endsWith(".XLSX") || file.getName().endsWith(".ppt") || file.getName().endsWith(".PPT") || file.getName().endsWith(".pptx") || file.getName().endsWith(".PPTX") || file.getName().endsWith(".pdf") || file.getName().endsWith(".PDF") || file.getName().endsWith(".txt") || file.getName().endsWith(".TXT"))) {
                            //Add File
                            fileArrayList.add(file);
                        }
                    }
                }
            }
            //endregion

            return fileArrayList;
        } catch (Exception ex) {
            Log.e(TAG, ex.toString());
            return null;
        }
    }

    public static void LocalFileRemove(String BookID) {
        try {
            String fileLocal = Environment.getExternalStorageDirectory().getAbsolutePath() + "/PPstore/0H49FK9dS3drKuo0mUErZ8ozwndMa7DV/" + BookID;
            File file = new File(fileLocal);
            if (file.delete()) {
                Log.i(TAG, "File Deleted");
            } else {
                Log.i(TAG, "ERROR DELETING FILE >>" + fileLocal);
            }
        } catch (Exception ex) {
            Log.e(TAG, ex.toString());
        }
    }

    public static void CheckUserLogin(Activity activity) {
        if (FirebaseUtils.firebaseAuth != null && FirebaseUtils.firebaseAuth.getCurrentUser() == null) {
            Intent i = new Intent(activity.getApplicationContext(), Activity_SignIn.class);
            activity.startActivity(i);
            activity.finish();
        }
    }

    public static Bitmap getBitmapFromView(View view) {
        Bitmap returnedBitmap = Bitmap.createBitmap(view.getWidth(), view.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(returnedBitmap);
        Drawable bgDrawable = view.getBackground();
        if (bgDrawable != null)
            bgDrawable.draw(canvas);
        else
            canvas.drawColor(Color.WHITE);
        view.draw(canvas);
        return returnedBitmap;
    }

    public static void selectSpinnerItemByValue(Spinner spnr, String value) {
        try {
            spnr.setSelection(((ArrayAdapter<String>) spnr.getAdapter()).getPosition(value));
        } catch (Exception ex) {
            Log.e(TAG, ex.toString());
        }
    }

    public static void OrderAscending(ArrayList<String> data) {
        Collections.sort(data, new Comparator<String>() {
            @Override
            public int compare(String s1, String s2) {
                return s1.compareToIgnoreCase(s2);
            }
        });
    }

    public static int getScreenBrightness(Context context) {
        int brightnessValue = 0;
        try {
            brightnessValue = Settings.System.getInt(context.getContentResolver(), Settings.System.SCREEN_BRIGHTNESS);
        } catch (Exception ex) {
            Toast.makeText(context, ex.getMessage(), Toast.LENGTH_SHORT).show();
            Log.e(TAG, ex.getMessage());
        }
        return brightnessValue;
    }

    public static boolean getScreenBrightnessMode(Context context) {
        try {
            if (Settings.System.getInt(context.getContentResolver(), Settings.System.SCREEN_BRIGHTNESS_MODE) == 1) {
                return true;
            } else {
                return false;
            }
        } catch (Exception ex) {
            return false;
        }
    }

    public static boolean checkSystemWrite(final Context context) {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (Settings.System.canWrite(context)) {
                    return true;
                } else {
                    return false;
                }
            } else {
                if (ContextCompat.checkSelfPermission(context, android.Manifest.permission.WRITE_SETTINGS) == PackageManager.PERMISSION_GRANTED) {
                    return true;
                } else {
                    return false;
                }
            }
        } catch (Exception ex) {
            Log.e(TAG, ex.toString());
            return false;
        }
    }

    public static void openSystemWriteIntent(final Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            AlertDialog.Builder builder = new AlertDialog.Builder(context);
            builder.setTitle("Permission Denied");
            builder.setMessage("Changing brightness is disable! you can turn it on manually.");
            builder.setPositiveButton("Proceed", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    Intent intent = new Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS, Uri.parse("package:" + context.getPackageName()));
                    context.startActivity(intent);
                }
            });
            builder.setNegativeButton("Cancel", null);
            builder.create().show();
        } else {
            AlertDialog.Builder builder = new AlertDialog.Builder(context);
            builder.setTitle("System Setting");
            builder.setMessage("Changing brightness is disable! you can turn it on manually.");
            builder.setPositiveButton("Proceed", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    new PermissionUtils((Activity) context).CheckPermission();
                }
            });
            builder.setNegativeButton("Cancel", null);
            builder.create().show();
        }
    }

    public static void setScreenBrightness(final Context context, int brightnessValue) {
        try {
            if (checkSystemWrite(context)) {
                if (brightnessValue >= 0 && brightnessValue <= 255) {
                    Settings.System.putInt(context.getContentResolver(), Settings.System.SCREEN_BRIGHTNESS, brightnessValue);
                }
            } else {
                Log.e(TAG, "ERROR: NO CHECK SYSTEM WRITE");
            }
        } catch (Exception ex) {
            Log.e(TAG, ex.getMessage());
        }
    }

    public static void setScreenBrightnessMode(Context context, boolean val) {
        try {
            if (val) {
                Settings.System.putInt(context.getContentResolver(), Settings.System.SCREEN_BRIGHTNESS_MODE, 1);
            } else {
                Settings.System.putInt(context.getContentResolver(), Settings.System.SCREEN_BRIGHTNESS_MODE, 0);
            }
        } catch (Exception ex) {
            Log.e(TAG, ex.toString());
        }
    }

    public static int map(int x, int in_min, int in_max, int out_min, int out_max) {
        return (x - in_min) * (out_max - out_min) / (in_max - in_min) + out_min;
    }

    public static Float map(Float x, Float in_min, Float in_max, Float out_min, Float out_max) {
        return (x - in_min) * (out_max - out_min) / (in_max - in_min) + out_min;
    }

    public static void OpenBook(Context context, BookItems bookItems) {
        try {
            if (bookItems.fileType == BookItems.FileType.EPUB) {
                if (bookItems.storageType == BookItems.StorageType.CLOUD) {
                    //region Open Server file
                    String path;
                    if (bookItems.pKey.equals("")) {
                        path = Environment.getExternalStorageDirectory().getAbsolutePath() + "/.ppstore/" + bookItems.BOOKID + ".epub";
                    } else {
                        path = Environment.getExternalStorageDirectory().getAbsolutePath() + "/.ppstore/" + bookItems.BOOKID + ".zip";
                    }

                    File file = new File(path);
                    if (file.exists()) {
                        Activity_Home.currentBookItems = null;//bookItems;
                        SaveBookToSqlite(context, bookItems); //save to sqlite

                        Intent i = new Intent(context, Reader_EPUB.class);
                        i.putExtra("pKey", bookItems.pKey);
                        i.putExtra("path", path);
                        i.putExtra("title", bookItems.Name);
                        i.putExtra("bookid", bookItems.BOOKID);
                        i.putExtra("storageType", bookItems.storageType.toString());
                        context.startActivity(i);
                    } else {
                        //Open Book Details
                        Toast.makeText(context, "Looks like you are using other device please sync this book to your device.", Toast.LENGTH_LONG).show();

                        Intent intent = new Intent(context, Activity_BookDetails.class);
                        intent.putExtra("BookID", bookItems.BOOKID);
                        intent.putExtra("sync", true);
                        context.startActivity(intent);
                    }
                    //endregion
                } else if (bookItems.storageType == BookItems.StorageType.LOCAL) {
                    //region Open local file
                    String path = bookItems.LocalFileURL;
                    File file = new File(path);
                    if (file.exists()) {
                        Activity_Home.currentBookItems = null;//bookItems;
                        SaveBookToSqlite(context, bookItems); //save to sqlite

                        Intent i = new Intent(context, Reader_EPUB.class);
                        i.putExtra("pKey", bookItems.pKey);
                        i.putExtra("path", path);
                        i.putExtra("title", bookItems.Name);
                        i.putExtra("bookid", bookItems.BOOKID);
                        i.putExtra("storageType", bookItems.storageType.toString());
                        context.startActivity(i);
                    } else {
                        //Book does not exists
                        Toast.makeText(context, "Book does not exists [" + path + "].", Toast.LENGTH_SHORT).show();
                    }
                    //endregion
                }
            } else if (bookItems.fileType == BookItems.FileType.PDF) {
                Activity_Home.currentBookItems = null;//bookItems;
                SaveBookToSqlite(context, bookItems); //save to sqlite

                //PDF
                Intent i = new Intent(context, Reader_PDF.class);
                i.putExtra("path", bookItems.LocalFileURL);
                i.putExtra("title", bookItems.Name);
                context.startActivity(i);
            } else if (bookItems.fileType == BookItems.FileType.TXT) {
                Activity_Home.currentBookItems = null;//bookItems;
                SaveBookToSqlite(context, bookItems); //save to sqlite

                Intent i = new Intent(context, Reader_TXT.class);
                i.putExtra("path", bookItems.LocalFileURL);
                i.putExtra("title", bookItems.Name);
                context.startActivity(i);
            } else if (bookItems.fileType == BookItems.FileType.XLS || bookItems.fileType == BookItems.FileType.DOC || bookItems.fileType == BookItems.FileType.PPT) {
                Activity_Home.currentBookItems = null;//bookItems;
                SaveBookToSqlite(context, bookItems); //save to sqlite

                CheckPackage(context, bookItems.LocalFileURL, bookItems.fileType);
            } else {
                Activity_Home.currentBookItems = null;
                Toast.makeText(context, "Error file type", Toast.LENGTH_SHORT).show();
            }

        } catch (Exception ex) {
            Log.e(TAG, ex.getMessage());
        }
    }

    private static void CheckPackage(Context context, String filePathAbsolute, BookItems.FileType fileType) {
        boolean isPackageAvailable;
        if (fileType == BookItems.FileType.DOC) {
            //region Word
            String MicrosoftWordPackageName = "com.microsoft.office.word";
            try {
                PackageManager packageManager = context.getPackageManager();
                packageManager.getPackageInfo(MicrosoftWordPackageName, PackageManager.GET_ACTIVITIES);
                isPackageAvailable = true;
            } catch (PackageManager.NameNotFoundException ex) {
                Log.e(TAG, ex.toString());
                Intent goToMarket = new Intent(Intent.ACTION_VIEW).setData(Uri.parse("market://details?id=" + MicrosoftWordPackageName));
                context.startActivity(goToMarket);
                isPackageAvailable = false;
            }
            //endregion

            //region Open Word
            if (isPackageAvailable) {
                try {
                    String filePath = "file://" + filePathAbsolute;
                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("ms-word:ofv|u|" + filePath));
                    context.startActivity(intent);
                } catch (Exception ex) {
                    Log.e(TAG, ex.toString());
                }
            }
            //endregion
        } else if (fileType == BookItems.FileType.XLS) {
            //region Excel
            String MicrosoftExcelPackageName = "com.microsoft.office.excel";
            try {
                PackageManager packageManager = context.getPackageManager();
                packageManager.getPackageInfo(MicrosoftExcelPackageName, PackageManager.GET_ACTIVITIES);
                isPackageAvailable = true;
            } catch (PackageManager.NameNotFoundException ex) {
                Log.e(TAG, ex.toString());
                Intent goToMarket = new Intent(Intent.ACTION_VIEW).setData(Uri.parse("market://details?id=" + MicrosoftExcelPackageName));
                context.startActivity(goToMarket);
                isPackageAvailable = false;
            }
            //endregion

            //region Open Excel
            if (isPackageAvailable) {
                try {
                    String filePath = "file://" + filePathAbsolute;
                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("ms-excel:ofv|u|" + filePath));
                    context.startActivity(intent);
                } catch (Exception ex) {
                    Log.e(TAG, ex.toString());
                }
            }
            //endregion
        } else if (fileType == BookItems.FileType.PPT) {
            //region Power Point
            String MicrosoftPowerPointPackageName = "com.microsoft.office.powerpoint";
            try {
                PackageManager packageManager = context.getPackageManager();
                packageManager.getPackageInfo(MicrosoftPowerPointPackageName, PackageManager.GET_ACTIVITIES);
                isPackageAvailable = true;
            } catch (PackageManager.NameNotFoundException ex) {
                Log.e(TAG, ex.toString());
                Intent goToMarket = new Intent(Intent.ACTION_VIEW).setData(Uri.parse("market://details?id=" + MicrosoftPowerPointPackageName));
                context.startActivity(goToMarket);
                isPackageAvailable = false;
            }
            //endregion

            //region Open Power Point
            if (isPackageAvailable) {
                try {
                    String filePath = "file://" + filePathAbsolute;
                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("ms-powerpoint:ofv|u|" + filePath));
                    context.startActivity(intent);
                } catch (Exception ex) {
                    Log.e(TAG, ex.toString());
                }
            }
            //endregion
        }
    }

    public interface ArchiveFinished {
        void finished(String saveRoot, long fileSize);
    }

    public static void UnCompressedFile(String target, String pass, ArchiveFinished finished) {
        try {
            String FileID = Utils.UUIDToken() + "_";
            File file = new File(target);
            String rootPath = file.getParent();
            //String fileName = file.getName();
            String savePath = rootPath + "/." + FileID + "/";
            ZipArchive.unzip(target, savePath, pass);

            //Get Current Directory and find epub file
            File dir = new File(savePath);
            String CurrentFilePath = "";
            long CurrentFileSize = 0;
            try {
                if (dir.exists()) {
                    File epubFile = dir.listFiles()[0];
                    CurrentFilePath = epubFile.getAbsolutePath();
                    CurrentFileSize = epubFile.length();
                }
            } catch (Exception ex) {
                Log.e(TAG, ex.toString());
            }
            finished.finished(CurrentFilePath, CurrentFileSize);
        } catch (Exception ex) {
            Log.e(TAG, ex.toString());
            finished.finished(null, 0);
        }
    }

    public static void CompressedFile(String target, String pass, ArchiveFinished finished) {
        try {
            //File file = new File(target);
            String rootPath = "";//file.getParent();
            String savePath = "";//rootPath;
            ZipArchive.zip(target, savePath, pass);
            finished.finished(savePath, 0);
        } catch (Exception ex) {
            Log.e(TAG, ex.toString());
            finished.finished(null, 0);
        }
    }

    public static boolean RemoveTempFile(String tempFile) {
        try {
            File tempDir = new File(tempFile);
            if (tempDir.isDirectory()) {
                for (File child : tempDir.listFiles()) {
                    RemoveTempFile(child.getAbsolutePath());
                }
            }
            return tempDir.exists() && tempDir.delete();
        } catch (Exception ex) {
            Log.e(TAG, ex.toString());
            return false;
        }
    }

    public static void OpenDictionary(final Context context, String needle) {
        try {
            View view = LayoutInflater.from(context).inflate(R.layout.dictionary_viewer_template, null);
            ((TextView) view.findViewById(R.id.title)).setText(Html.fromHtml("<b>Search</b> \"" + needle + "\""));
            final TextView Description = view.findViewById(R.id.description);
            AlertDialog.Builder builder = new AlertDialog.Builder(context);
            builder.setView(view);
            builder.setPositiveButton("Ok", null);
            builder.setNeutralButton("Update Dictionary", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    context.startActivity(new Intent(context, Activity_Dictionary.class));
                }
            });
            builder.create().show();

            SQLiteDatabase sqLiteDatabase = null;
            try {
                String _bufferDescription = "Data Not Found";
                String query = "select * from dictionary where title LIKE ?";
                SQLiteOpenHelper helper = new DBSqliteHelper(context, context.getString(R.string.SQLITE_DATABASE), null, context.getResources().getInteger(R.integer.SQLITE_DATABSE_VERSION));
                sqLiteDatabase = helper.getWritableDatabase();
                Cursor cursor = sqLiteDatabase.rawQuery(query, new String[]{needle});
                for (cursor.moveToLast(); !cursor.isBeforeFirst(); cursor.moveToPrevious()) {
                    _bufferDescription = "<B>Define: </B>" + cursor.getString(cursor.getColumnIndex("description"));
                }
                cursor.close();

                final String descr = _bufferDescription;
                ((Activity)context).runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            Description.setText(Html.fromHtml(descr));
                        }
                        catch (Exception ex){
                            Log.e(TAG, ex.toString());
                        }
                    }
                });

            } catch (Exception ex) {
                Log.e(TAG, ex.getMessage());
            } finally {
                if (sqLiteDatabase != null) {
                    sqLiteDatabase.close();
                }
            }

        } catch (Exception ex) {
            Log.e(TAG, ex.getMessage());
        }
    }

    public static void hideSoftKeyboard(Activity activity) {
        if (activity != null) {
            InputMethodManager inputManager = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
            if (activity.getCurrentFocus() != null && inputManager != null) {
                inputManager.hideSoftInputFromWindow(activity.getCurrentFocus().getWindowToken(), 0);
                inputManager.hideSoftInputFromInputMethod(activity.getCurrentFocus().getWindowToken(), 0);
            }
        }
    }

    public static void hideSoftKeyboard(View view) {
        if (view != null) {
            InputMethodManager inputManager = (InputMethodManager) view.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
            if (inputManager != null) {
                inputManager.hideSoftInputFromWindow(view.getWindowToken(), 0);
            }
        }
    }

    public static void GoToBookDetails(Context context, String BOOKID) {
        try {
            Intent intent = new Intent(context, Activity_BookDetails.class);
            intent.putExtra("BookID", BOOKID);
            context.startActivity(intent);
        } catch (Exception ex) {
            Log.e("ERROR", ex.toString());
        }
    }

    public static void SaveBookToSqlite(Context context, BookItems bookItems) {
        SQLiteDatabase sqLiteDatabase = null;
        try {
            SQLiteOpenHelper helper = new DBSqliteHelper(context, context.getString(R.string.SQLITE_DATABASE), null, context.getResources().getInteger(R.integer.SQLITE_DATABSE_VERSION));
            sqLiteDatabase = helper.getWritableDatabase();

            //just delete it and add it
            sqLiteDatabase.execSQL("delete from currentbook where ID=1");

            String localurl = bookItems.LocalFileURL;
            if (localurl == null) {
                localurl = "";
            }

            //Add new one to system
            ContentValues contentValues = new ContentValues();
            contentValues.put("ID", 1);
            contentValues.put("bookid", bookItems.BOOKID);
            contentValues.put("name", bookItems.Name);
            contentValues.put("filetype", bookItems.fileType.toString());
            contentValues.put("storagetype", bookItems.storageType.toString());
            contentValues.put("localfilreurl", localurl);
            contentValues.put("pkey", bookItems.pKey);
            sqLiteDatabase.insert("currentbook", null, contentValues);
        } catch (Exception ex) {
            Log.e(TAG, ex.toString());
        } finally {
            if (sqLiteDatabase != null) {
                sqLiteDatabase.close();
            }
        }
    }

    public static BookItems GetCurrentBookFromSqlite(Context context) {
        BookItems bookItems = null;
        SQLiteDatabase sqLiteDatabase = null;
        try {
            SQLiteOpenHelper helper = new DBSqliteHelper(context, context.getString(R.string.SQLITE_DATABASE), null, context.getResources().getInteger(R.integer.SQLITE_DATABSE_VERSION));
            sqLiteDatabase = helper.getWritableDatabase();
            String query = "select * from currentbook where ID=1";
            Cursor cursor = sqLiteDatabase.rawQuery(query, null);
            for (cursor.moveToLast(); !cursor.isBeforeFirst(); cursor.moveToPrevious()) {
                bookItems = new BookItems(
                        cursor.getString(cursor.getColumnIndex("pkey")),
                        "",
                        cursor.getString(cursor.getColumnIndex("bookid")),
                        cursor.getString(cursor.getColumnIndex("name")),
                        "",
                        "",
                        0.0,
                        "",
                        null,
                        null,
                        BookItems.FileType.valueOf(cursor.getString(cursor.getColumnIndex("filetype"))),
                        BookItems.StorageType.valueOf(cursor.getString(cursor.getColumnIndex("storagetype")))
                );
                bookItems.LocalFileURL = cursor.getString(cursor.getColumnIndex("localfilreurl"));
            }
            cursor.close();
            Log.e(TAG, "GET BOOK FROM SQLITE");
        } catch (Exception ex) {
            Log.e(TAG, ex.toString());
        } finally {
            if (sqLiteDatabase != null) {
                sqLiteDatabase.close();
            }
        }
        return bookItems;
    }

    public static void EnableFullScreen(Activity activity) {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.KITKAT) {
            isFullScreen = true;
            View decorView = activity.getWindow().getDecorView();
            int flag = View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_FULLSCREEN | View.SYSTEM_UI_FLAG_IMMERSIVE;
            decorView.setSystemUiVisibility(flag);
        }
        EnableBrightnessLock(activity);
    }

    public static void DisableFullScreen(Activity activity) {
        isFullScreen = false;
        View decorView = activity.getWindow().getDecorView();
        decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);

        DisableBrightnessLock(activity);
    }

    static void EnableBrightnessLock(Activity activity){
        try {
            activity.getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
            //String lockTag = "paper_lock_tag";
            //PowerManager powerManager = (PowerManager) activity.getSystemService(Context.POWER_SERVICE);
            //wakeLock = powerManager.newWakeLock(PowerManager.FULL_WAKE_LOCK, lockTag);
            //wakeLock.acquire();
        }
        catch (Exception ex){
            Log.e(TAG, ex.toString());
        }
    }

    static void DisableBrightnessLock(Activity activity){
        //wakeLock.release();
    }

    public static String ConvertDBDateToStr(String date){
        String pDate;
        pDate = new SimpleDateFormat("MMM dd, yyyy 'at' h:m a", Locale.US).format(new Date(date));
        return pDate;
    }

    public static int getStatusBarHeight(Activity activity) {
        int result = 0;
        int resourceId = activity.getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            result = activity.getResources().getDimensionPixelSize(resourceId);
        }
        return result;
    }

    public static void OpenWebView(Context context, String Title, String url, boolean noCheckConnection){
        //region no internet toast
        if(noCheckConnection) {
            if (!NetworkManagerUtils.LoadServerData(context)) {
                return;
            }
        }
        //endregion

        new FinestWebView.Builder(context)
                .webViewJavaScriptEnabled(true)
                .titleDefault(Title)
                .updateTitleFromHtml(false)
                .toolbarScrollFlags(AppBarLayout.LayoutParams.SCROLL_FLAG_SCROLL | AppBarLayout.LayoutParams.SCROLL_FLAG_ENTER_ALWAYS)
                .gradientDivider(false)
                .showDivider(false)
                .showUrl(false)
                .toolbarColorRes(R.color.colWhite)
                .iconDefaultColorRes(R.color.colorAccent)
                .iconDisabledColorRes(R.color.Color_Aqua)
                .iconPressedColorRes(R.color.Color_Blue)
                .progressBarColorRes(R.color.colorAccent)
                .backPressToClose(false)
                .titleFont("roboto_bold.ttf")
                .menuTextFont("roboto.ttf")
                .showIconMenu(false)
                .setCustomAnimations(R.anim.activity_open_enter, R.anim.activity_open_exit, R.anim.activity_close_enter, R.anim.activity_close_exit)
                .show(url);
    }

    public static String getFileNameFromPath(String path){
        return new File(path).getName();
    }

    public static void OpenProfile(Activity activity, String UID){
        try{
            String currentUID = FirebaseUtils.firebaseAuth.getCurrentUser().getUid();
            if(currentUID.equals(UID)){
                //Open My Profile
                Intent intent = new Intent(activity, Activity_UserProfile.class);
                activity.startActivity(intent);
            }
            else{
                //Open Friend Profile
                Intent i = new Intent(activity, Activity_Friend_Profile.class);
                i.putExtra("UID", UID);
                activity.startActivity(i);
            }
        }
        catch (Exception ex){
            Log.e(TAG, ex.toString());
        }
    }

    //region GET REAL PATH FROM URI PROTOTYPE
    /*
    public static String getRealPathFromURI(Context context, Uri contentUri) {
        Cursor cursor = null;
        try {
            String[] proj = { MediaStore.Images.Media.DATA };
            cursor = context.getContentResolver().query(contentUri,  proj, null, null, null);
            int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            cursor.moveToFirst();
            return cursor.getString(column_index);
        }
        catch (Exception e) {
            Log.e(TAG, "getRealPathFromURI Exception : " + e.toString());
            return null;
        }
        finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }
    */
    //endregion
}
