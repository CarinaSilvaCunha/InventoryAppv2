package com.example.carinacunha.inventoryappv2.BookStoreDatabase;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import com.example.carinacunha.inventoryappv2.BookStoreDatabase.DatabaseContract.BookEntry;

public class BookStoreSupplier extends ContentProvider {

    public static final String LOG_TAG = BookStoreSupplier.class.getSimpleName();
    private static final int BOOKS = 100;
    private static final int BOOK_ID = 101;
    private static final UriMatcher bookStoreUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

    // this will is a static initializer
    static {
        bookStoreUriMatcher.addURI(DatabaseContract.AUTHORITY, DatabaseContract.BOOKS_PATH, BOOKS);
        bookStoreUriMatcher.addURI(DatabaseContract.AUTHORITY, DatabaseContract.BOOKS_PATH + "/#", BOOK_ID);
    }

    private DatabaseHelper BookStoreHelper;

    @Override
    public boolean onCreate() {
        BookStoreHelper = new DatabaseHelper(getContext());
        return true;
    }

    /**
     * this will get the database and the cursor will hold the query result
     * match the URI to a specific code
     */
    @Override
    public Cursor query(@NonNull Uri uri, @Nullable String[] projection, @Nullable String selection, @Nullable String[] selectionArgs, @Nullable String sortOrder) {
        SQLiteDatabase database = BookStoreHelper.getReadableDatabase();
        Cursor bookStoreCursor;

        int match = bookStoreUriMatcher.match(uri);
        switch (match) {
            case BOOKS:
                bookStoreCursor = database.query(BookEntry.TABLE_NAME, projection, selection, selectionArgs, null, null, sortOrder);
                break;
            case BOOK_ID:
                selection = BookEntry._ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                bookStoreCursor = database.query(BookEntry.TABLE_NAME, projection, selection, selectionArgs, null, null, sortOrder);
                break;
            default:
                throw new IllegalArgumentException("Sorry, cannot query URI " + uri);
        }
        if (getContext() != null) {
            bookStoreCursor.setNotificationUri(getContext().getContentResolver(), uri);
        }
        return bookStoreCursor;
    }

    @Override
    public Uri insert(@Nullable Uri uri, @Nullable ContentValues contentValues) {
        final int match = bookStoreUriMatcher.match(uri);
        switch (match) {
            case BOOKS:
                if (contentValues != null) {
                    return insertBook(uri, contentValues);
                }
            default:
                throw new IllegalArgumentException("Book insertion is not supported for " + uri);
        }
    }

    @Override
    public String getType(@Nullable Uri uri) {
        final int match = bookStoreUriMatcher.match(uri);
        switch (match) {
            case BOOKS:
                return BookEntry.LIST;
            case BOOK_ID:
                return BookEntry.ITEM;
            default:
                throw new IllegalStateException("Sorry! Unknown URI " + uri + " with the match " + match);
        }
    }

    /**
     * This method will insert the book into the database with the selected values. Also returns the new content URI
     * Starts by checking if the name is not null, the genre, the price and quantity
     * If the ID is -1, the insertion fails.
     */
    private Uri insertBook(Uri uri, ContentValues values) {
        String bookName = values.getAsString(BookEntry.COLUMN_NAME);
        if (bookName == null) {
            throw new IllegalArgumentException("Oops! The book requires a name!");
        }

        Integer bookGenre = values.getAsInteger(BookEntry.COLUMN_GENRE);
        if (bookGenre == null) {
            throw new IllegalArgumentException("Oops! The book requires a Genre!");
        }

        Integer bookPrice = values.getAsInteger(BookEntry.COLUMN_PRICE);
        if (bookPrice != null && bookPrice < 0) {
            throw new IllegalArgumentException("Oops! The Price has to be greater than 0!");
        }

        Integer bookQuantity = values.getAsInteger(DatabaseContract.BookEntry.COLUMN_QUANTITY);
        if (bookQuantity != null && bookQuantity < 0) {
            throw new IllegalArgumentException("Oops! Please check the quantity!");
        }

        SQLiteDatabase bookStoreDatabase = BookStoreHelper.getWritableDatabase();
        long id = bookStoreDatabase.insert(BookEntry.TABLE_NAME, null, values);
        if (id == -1) {
            Log.e(LOG_TAG, "\"Oops! Failed to insert book " + uri);
            return null;
        }
        if (getContext() != null) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return ContentUris.withAppendedId(uri, id);
    }

    @Override
    public int update(@NonNull Uri uri, @Nullable ContentValues contentValues, @Nullable String selection, @Nullable String[] selectionArgs) {
        final int match = bookStoreUriMatcher.match(uri);
        switch (match) {
            case BOOKS:
                if (contentValues != null) {
                    return updateBook(uri, contentValues, selection, selectionArgs);
                }
            case BOOK_ID:
                if (contentValues != null) {
                    selection = BookEntry._ID + "=?";
                    selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                    return updateBook(uri, contentValues, selection, selectionArgs);
                }
            default:
                throw new IllegalArgumentException("Sorry! The update is not supported for " + uri);
        }
    }

    /**
     * Updates the database with the content
     */
    private int updateBook(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        if (values.size() == 0) {
            return 0;
        }

        if (values.containsKey(BookEntry.COLUMN_NAME)) {
            String bookName = values.getAsString(BookEntry.COLUMN_NAME);
            if (bookName == null) {
                throw new IllegalArgumentException("The book requires a name!");
            }
        }

        if (values.containsKey(BookEntry.COLUMN_GENRE)) {
            Integer bookGenre = values.getAsInteger(BookEntry.COLUMN_GENRE);
            if (bookGenre == null) {
                throw new IllegalArgumentException("The book requires a genre!");
            }
        }

        if (values.containsKey(BookEntry.COLUMN_PRICE)) {
            Integer bookPrice = values.getAsInteger(BookEntry.COLUMN_PRICE);
            if (bookPrice != null && bookPrice < 0) {
                throw new IllegalArgumentException("The book requires a price!");
            }
        }

        if (values.containsKey(BookEntry.COLUMN_QUANTITY)) {
            Integer bookQuantity = values.getAsInteger(BookEntry.COLUMN_QUANTITY);
            if (bookQuantity != null && bookQuantity < 0) {
                throw new IllegalArgumentException("The book requires a valid bookQuantity!");
            }
        }
        SQLiteDatabase database = BookStoreHelper.getWritableDatabase();
        int booksUpdated = database.update(BookEntry.TABLE_NAME, values, selection, selectionArgs);

        if (booksUpdated != 0) {
            if (getContext() != null) {
                getContext().getContentResolver().notifyChange(uri, null);
            }
        }
        return booksUpdated;
    }

    /**
     * This deletes one or more books
     * Get the database, see how many books were deleted
     * Delete all the rows that match the bookSelection or delete a single row by the ID
     */
    @Override
    public int delete(@NonNull Uri uri, @Nullable String selection, @Nullable String[] selectionArgs) {
        SQLiteDatabase bookStoreDatabase = BookStoreHelper.getWritableDatabase();
        int booksDeleted;
        final int match = bookStoreUriMatcher.match(uri);

        switch (match) {
            case BOOKS:
                booksDeleted = bookStoreDatabase.delete(BookEntry.TABLE_NAME, selection, selectionArgs);
                break;
            case BOOK_ID:
                selection = BookEntry._ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                booksDeleted = bookStoreDatabase.delete(BookEntry.TABLE_NAME, selection, selectionArgs);
                break;
            default:
                throw new IllegalArgumentException("This deletion is not supported for " + uri);
        }

        if (booksDeleted != 0) {
            if (getContext() != null) {
                getContext().getContentResolver().notifyChange(uri, null);
            }
        }
        return booksDeleted;
    }


}
