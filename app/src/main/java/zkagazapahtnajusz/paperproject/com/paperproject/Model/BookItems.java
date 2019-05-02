package zkagazapahtnajusz.paperproject.com.paperproject.Model;

import android.support.annotation.NonNull;

import java.util.ArrayList;

/**
 * Created by Zw4R-TSTUD10 2018/4/1 [2018, April 1]
 */

public class BookItems implements Comparable {
     public long sn;
     public String BOOKID;
     public enum BookFlag {Free, Store, Purchased, Nothing, Redownload}
     public enum Status {Public, Private}
     public enum FileType {EPUB, DOC, PPT, XLS, PDF, TXT, Nothing}
     public enum StorageType{CLOUD, LOCAL}
     public String Name;
     public String Author;
     public String Description;
     public Double Price;
     public String CurrencyStr;
     public ArrayList Genre;
     public BookFlag bookFlag;
     public FileType fileType;
     public StorageType storageType;
     public String imageURL;
     public String LocalFileURL;
     public String Ext;
     public String pKey;
     public String uDate;

     public BookItems(String pKey,String imageURL, String BOOKID, String name, String Author,  String description, Double price, String currencyStr, ArrayList Genre, BookFlag bookFlag, FileType fileType, StorageType storageType) {
          this.pKey = pKey;
          this.BOOKID = BOOKID;
          this.imageURL = imageURL;
          this.Name = name;
          this.Author = Author;
          this.Description = description;
          this.Price = price;
          this.CurrencyStr = currencyStr;
          this.Genre = Genre;
          this.bookFlag = bookFlag;
          this.fileType = fileType;
          this.storageType = storageType;
     }

     @Override
     public int compareTo(@NonNull Object o) {
          //Ascending Order
          long comparesn = ((BookItems)o).sn;
          return -(int) (this.sn - comparesn);
     }

}
