package com.example.carinacunha.inventoryappv2;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.example.carinacunha.inventoryappv2.BookStoreDatabase.DatabaseContract;
import com.example.carinacunha.inventoryappv2.BookStoreDatabase.DatabaseContract.BookEntry;

public class BookCursorAdapter extends CursorAdapter {

    /**
     * This is the recommended constructor
     *
     * @param context This is the context
     * @param c       This is the cursor
     */
    public BookCursorAdapter(Context context, Cursor c) {
        super(context, c, 0);
    }

    /**
     * This will make a new view that holds the data pointed by the cursor
     *
     * @param context this is the context
     * @param cursor  This is the cursor that has the data
     * @param parent  This is the parent of the new view
     * @return the new view
     */
    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        // Inflate a list item view using the layout specified in list_item.xml
        return LayoutInflater.from(context).inflate(R.layout.list_item, parent, false);
    }

    /**
     * This is a method that will connect the view to the data from the cursor
     * Selling a book will decrease its quantity
     *
     * @param view    This is the existing view
     * @param context This is the context
     * @param cursor  This is the cursor which has the data.
     */
    @Override
    public void bindView(View view, final Context context, Cursor cursor) {
        TextView book_name = view.findViewById(R.id.book_name);
        TextView book_price = view.findViewById(R.id.book_price);
        TextView books_in_stock = view.findViewById(R.id.books_in_stock);
        ImageButton sell_book = view.findViewById(R.id.sell_book);

        int indexBookName = cursor.getColumnIndex(BookEntry.COLUMN_NAME);
        int indexBookPrice = cursor.getColumnIndex(BookEntry.COLUMN_PRICE);
        int indexBookQuantity = cursor.getColumnIndex(BookEntry.COLUMN_QUANTITY);

        String bookName = cursor.getString(indexBookName);
        String bookPrice = cursor.getString(indexBookPrice);
        String bookQuantity = cursor.getString(indexBookQuantity);

        String bookPriceDisplay = "Price: " + bookPrice + " €";
        String bookQuantityDisplay = "In stock: " + bookQuantity;

        book_name.setText(bookName);
        book_price.setText(bookPriceDisplay);
        books_in_stock.setText(bookQuantityDisplay);

        final int quantityColumnIndex = cursor.getColumnIndex(BookEntry.COLUMN_QUANTITY);
        String currentQuantity = cursor.getString(quantityColumnIndex);
        final int quantityIntCurrent = Integer.valueOf(currentQuantity);
        final int productId = cursor.getInt(cursor.getColumnIndex(DatabaseContract.BookEntry._ID));
        sell_book.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                if (quantityIntCurrent > 0) {
                    int newQuantity = quantityIntCurrent - 1;
                    Uri quantityUri = ContentUris.withAppendedId(BookEntry.CONTENT_URI, productId);
                    ContentValues values = new ContentValues();
                    values.put(DatabaseContract.BookEntry.COLUMN_QUANTITY, newQuantity);
                    context.getContentResolver().update(quantityUri, values, null, null);
                } else {
                    Toast.makeText(context, "This book is out of stock!", Toast.LENGTH_SHORT).show();
                }
            }
        });

    }

}
