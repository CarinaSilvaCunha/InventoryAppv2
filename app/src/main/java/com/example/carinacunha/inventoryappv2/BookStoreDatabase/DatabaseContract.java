package com.example.carinacunha.inventoryappv2.BookStoreDatabase;

import android.content.ContentResolver;
import android.net.Uri;
import android.provider.BaseColumns;

public final class DatabaseContract {

    public static final String AUTHORITY = "com.example.carinacunha.inventoryappv2";
    public static final String BOOKS_PATH = "books";
    private static final Uri BASE_URI = Uri.parse("content://" + AUTHORITY);

    /**
     * empty constructor to prevent instantiation
     */
    private DatabaseContract() {
    }

    /**
     * Book entries method
     */
    public static final class BookEntry implements BaseColumns {

        public final static String TABLE_NAME = "books";
        public static final Uri CONTENT_URI = Uri.withAppendedPath(BASE_URI, BOOKS_PATH);

        public final static String _ID = BaseColumns._ID;
        public final static String COLUMN_NAME = "name";
        public final static String COLUMN_GENRE = "genre";
        public static final String COLUMN_QUANTITY = "quantity";
        public static final String COLUMN_PRICE = "price";
        public final static String COLUMN_SUPPLIER = "supplier";
        public final static String COLUMN_SUPPLIER_PHONE = "phone";
        public final static String COLUMN_SUPPLIER_EMAIL = "email";

        public final static int GENRE_SCIENCE_FICTION = 1;
        public final static int GENRE_DRAMA = 2;
        public final static int GENRE_ROMANCE = 3;
        public final static int GENRE_HORROR = 4;
        public final static int GENRE_SELF_HELP = 5;
        public final static int GENRE_TRAVEL = 6;
        public final static int GENRE_NOT_AVAILABLE = 0;

        public static final String LIST = ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + AUTHORITY + "/" + BOOKS_PATH;
        public static final String ITEM = ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + AUTHORITY + "/" + BOOKS_PATH;
    }
}
