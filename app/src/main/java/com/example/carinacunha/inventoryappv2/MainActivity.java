package com.example.carinacunha.inventoryappv2;

import android.app.LoaderManager;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.example.carinacunha.inventoryappv2.BookStoreDatabase.DatabaseContract;
import com.example.carinacunha.inventoryappv2.BookStoreDatabase.DatabaseContract.BookEntry;

public class MainActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final int BOOK_LOADER = 0;
    BookCursorAdapter bookAdapter;

    /**
     * Here we'll create the main view, where we can see the existing books
     * We'll be able to click the floating button to add more
     * or click the item to edit it
     * we'll be able to add it to the cart (which removes an unit)
     * or select an option to create dummy data or delete all
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_activity);

        FloatingActionButton floatingActionButton = findViewById(R.id.floating_action_button);
        floatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent onFloatingButtonClick = new Intent(MainActivity.this, BookStoreEditor.class);
                startActivity(onFloatingButtonClick);
            }
        });
        // Set the book list view
        ListView book_list = findViewById(R.id.book_list);
        // set the default view for empty lists
        View default_view = findViewById(R.id.default_view);
        book_list.setEmptyView(default_view);
        // set the adapter to create a list for each row
        bookAdapter = new BookCursorAdapter(this, null);
        book_list.setAdapter(bookAdapter);

        // set the onclicklistener for the item
        book_list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                Intent bookClickIntent = new Intent(MainActivity.this, BookStoreEditor.class);
                Uri currentBookURI = ContentUris.withAppendedId(BookEntry.CONTENT_URI, id);
                bookClickIntent.setData(currentBookURI);
                startActivity(bookClickIntent);
            }
        });
        getLoaderManager().initLoader(BOOK_LOADER, null, this);
    }

    /**
     * This deletes all books
     */
    private void deleteAllBooks() {
        int booksDeleted = getContentResolver().delete(BookEntry.CONTENT_URI, null, null);
        Log.v("MainActivity", booksDeleted + getString(R.string.all_deleted));
    }

    /**
     * Inflates the menu layout
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    /**
     * This allows the user to select an item from the app bar menu
     * The options will be : dummy data insertion to simplify the process
     * And Delete all books, so it's easier to clear the database for testing
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.insert_dummy_data:
                insertDummyBook();
                return true;
            case R.id.delete_all_books:
                deleteAllBooks();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * This will instantiate and return a new Loader for the given ID
     * the loader executes on a background thread
     */
    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        String[] projection = {
                BookEntry._ID,
                BookEntry.COLUMN_NAME,
                BookEntry.COLUMN_PRICE,
                BookEntry.COLUMN_QUANTITY};
        return new CursorLoader(this, BookEntry.CONTENT_URI, projection,null,null,null);
    }

    /**
     * This will update the adapter with the updated book information
     */
    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        bookAdapter.swapCursor(data);
    }

    /**
     * This is called when a previously created loader is reset.
     */
    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        bookAdapter.swapCursor(null);
    }

    /**
     *  Insert a new dummy book
     *  This is for quick content addition
     */
    private void insertDummyBook() {
        ContentValues values = new ContentValues();
        values.put(BookEntry.COLUMN_NAME, getString(R.string.dummy_book_name));
        values.put(BookEntry.COLUMN_GENRE, BookEntry.GENRE_SELF_HELP);
        values.put(BookEntry.COLUMN_PRICE, getResources().getInteger(R.integer.dummy_book_price));
        values.put(BookEntry.COLUMN_QUANTITY, getResources().getInteger(R.integer.dummy_book_quantity));
        values.put(BookEntry.COLUMN_SUPPLIER, getString(R.string.dummy_book_supplier));
        values.put(DatabaseContract.BookEntry.COLUMN_SUPPLIER_PHONE, getString(R.string.dummy_supplier_phone_number));
        values.put(DatabaseContract.BookEntry.COLUMN_SUPPLIER_EMAIL, getString(R.string.dummy_book_supplier_email));
        getContentResolver().insert(BookEntry.CONTENT_URI, values);
    }

}
