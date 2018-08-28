package com.example.carinacunha.inventoryappv2.BookStoreDatabase;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.example.carinacunha.inventoryappv2.BookStoreDatabase.DatabaseContract.BookEntry;

/**
 * This is a database helper.
 * This helps managing version management and database creation
 * You can define here: database name and version (which you'll need to increase if you change the db schema)
 */
public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String BOOKSTORE_DB_NAME = "bookstore.db";
    private static final int BOOKSTORE_DB_VERSION = 1;

    /**
     * This is a helper method that is used to create/manage a database
     *
     * @param context is used to open or create the database
     */
    public DatabaseHelper(Context context) {
        super(context, BOOKSTORE_DB_NAME, null, BOOKSTORE_DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase bookStoreDataBase) {
        String createDataBase = "CREATE TABLE " + BookEntry.TABLE_NAME + " ("
                + BookEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + BookEntry.COLUMN_NAME + " TEXT NOT NULL, "
                + BookEntry.COLUMN_GENRE + " INTEGER NOT NULL, "
                + BookEntry.COLUMN_QUANTITY + " INTEGER NOT NULL DEFAULT 0, "
                + BookEntry.COLUMN_PRICE + " TEXT NOT NULL DEFAULT 0, "
                + BookEntry.COLUMN_SUPPLIER + " TEXT NOT NULL, "
                + BookEntry.COLUMN_SUPPLIER_PHONE + " TEXT NOT NULL DEFAULT 0,"
                + DatabaseContract.BookEntry.COLUMN_SUPPLIER_EMAIL + " TEXT );";
        bookStoreDataBase.execSQL(createDataBase);
    }

    /**
     * This method will be called when the database needs to be upgraded.
     * For now, it will be an empty method as this is not needed and the database is still at version 1
     *
     * @param oldVersion is for the old database version
     * @param newVersion is the the new database version
     */
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
    }
}