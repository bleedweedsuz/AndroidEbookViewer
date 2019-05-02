package zkagazapahtnajusz.paperproject.com.paperproject.Utilities;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DBSqliteHelper extends SQLiteOpenHelper {
    public DBSqliteHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE dictionary (sn INTEGER PRIMARY KEY AUTOINCREMENT, title TEXT NOT NULL, description TEXT NOT NULL);");

        db.execSQL("CREATE TABLE highlight (sn INTEGER PRIMARY KEY AUTOINCREMENT, UID TEXT NOT NULL, bookid TEXT NOT NULL, title TEXT NOT NULL, cfi TEXT NOT NULL, highlightdate TEXT NOT NULL);");
        db.execSQL("CREATE TABLE bookmark (sn INTEGER PRIMARY KEY AUTOINCREMENT, UID TEXT NOT NULL, bookid TEXT NOT NULL, title TEXT NOT NULL, cfi TEXT NOT NULL, bookmarkdate TEXT NOT NULL);");
        db.execSQL("CREATE TABLE bookstate (sn INTEGER PRIMARY KEY AUTOINCREMENT, UID TEXT NOT NULL, bookid TEXT NOT NULL, cfi TEXT NOT NULL);");
        db.execSQL("CREATE TABLE note (sn INTEGER PRIMARY KEY AUTOINCREMENT, ID TEXT NOT NULL, UID TEXT NOT NULL, bookid TEXT NOT NULL, cfi TEXT NOT NULL, title TEXT NOT NULL, content TEXT NOT NULL, notedate TEXT NOT NULL);");

        db.execSQL("CREATE TABLE currentbook (ID INTEGER NOT NULL, bookid TEXT NOT NULL,name TEXT NOT NULL, filetype TEXT NOT NULL, storagetype TEXT NOT NULL, localfilreurl TEXT NOT NULL, pkey TEXT NOT NULL);");

        db.execSQL("CREATE TABLE currentdisplaymode (bookid TEXT NOT NULL,displaymode TEXT NOT NULL);");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        //..
        onCreate(db);
    }
}
