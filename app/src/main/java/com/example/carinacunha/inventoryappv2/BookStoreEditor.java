package com.example.carinacunha.inventoryappv2;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.LoaderManager;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.Toast;

import com.example.carinacunha.inventoryappv2.BookStoreDatabase.DatabaseContract;
import com.example.carinacunha.inventoryappv2.BookStoreDatabase.DatabaseContract.BookEntry;

import static com.example.carinacunha.inventoryappv2.R.array;
import static com.example.carinacunha.inventoryappv2.R.id;
import static com.example.carinacunha.inventoryappv2.R.layout;
import static com.example.carinacunha.inventoryappv2.R.string;

public class BookStoreEditor extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {
    private static final int BOOK_LOADER = 0;
    private Uri currentBook;
    private EditText bookName;
    private EditText bookPrice;
    private EditText bookQuantity;
    private EditText bookSupplier;
    private EditText bookSupplierPhone;
    private EditText bookSupplierEmail;
    private Spinner bookGenre;
    private int quantity;
    private int genre = BookEntry.GENRE_NOT_AVAILABLE;
    private boolean hasBookBeenEdited = false;

    /**
     * This will provide info if the view has changed
     */
    private View.OnTouchListener bookEdit = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            hasBookBeenEdited = true;
            return false;
        }
    };

    /**
     * This will get the intent. If the intent has a URI, it's an item edition
     * If not, it's a new item
     */
    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(layout.book_edit);

        Intent intent = getIntent();
        currentBook = intent.getData();

        if (currentBook == null) {
            setTitle(getString(string.new_book));
            invalidateOptionsMenu();
        } else {
            setTitle(getString(string.edit_book));
            getLoaderManager().initLoader(BOOK_LOADER, null, this);
        }

        bookName = findViewById(id.edit_book_name);
        bookPrice = findViewById(id.edit_book_price);
        bookQuantity = findViewById(id.edit_book_quantity);
        bookGenre = findViewById(id.edit_book_genre);
        bookSupplier = findViewById(id.edit_book_supplier);
        bookSupplierPhone = findViewById(id.edit_book_supplier_phone);
        bookSupplierEmail = findViewById(id.edit_book_supplier_email);
        ImageButton increaseBookStock = findViewById(id.increase_stock);
        ImageButton decreaseBookStock = findViewById(id.decrease_stock);

        bookName.setOnTouchListener(bookEdit);
        bookPrice.setOnTouchListener(bookEdit);
        bookQuantity.setOnTouchListener(bookEdit);
        bookSupplier.setOnTouchListener(bookEdit);
        bookSupplierPhone.setOnTouchListener(bookEdit);
        bookSupplierEmail.setOnTouchListener(bookEdit);
        increaseBookStock.setOnTouchListener(bookEdit);
        decreaseBookStock.setOnTouchListener(bookEdit);
        bookGenre.setOnTouchListener(bookEdit);


        // this is the onClickListener for increase quantity
        increaseBookStock.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String quantity = bookQuantity.getText().toString();
                if (TextUtils.isEmpty(quantity)) {
                    Toast.makeText(BookStoreEditor.this, string.edit_quantity, Toast.LENGTH_SHORT).show();
                    return;
                } else {
                    BookStoreEditor.this.quantity = Integer.parseInt(quantity);
                    bookQuantity.setText(String.valueOf(BookStoreEditor.this.quantity + 1));
                }

            }
        });

        //this is the onClickListener for decrease quantity
        decreaseBookStock.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String quantity = bookQuantity.getText().toString();
                if (TextUtils.isEmpty(quantity)) {
                    Toast.makeText(BookStoreEditor.this, string.edit_quantity, Toast.LENGTH_SHORT).show();
                    return;
                } else {
                    BookStoreEditor.this.quantity = Integer.parseInt(quantity);
                    //To validate if quantity is greater than 0
                    if ((BookStoreEditor.this.quantity - 1) >= 0) {
                        bookQuantity.setText(String.valueOf(BookStoreEditor.this.quantity - 1));
                    } else {
                        Toast.makeText(BookStoreEditor.this, string.edit_quantity, Toast.LENGTH_SHORT).show();
                        return;

                    }
                }
            }
        });

        // This is the button to e-mail
        ImageButton emailSupplier = findViewById(id.emailButton);
        emailSupplier.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String supplier = bookSupplier.getText().toString().trim();
                String supplierEmail = bookSupplierEmail.getText().toString().trim();
                String bookName = BookStoreEditor.this.bookName.getText().toString().trim();
                orderBookByEmail(supplierEmail, bookName, supplier);
            }
        });

        // This is the button to call supplier
        ImageButton callSupplier = findViewById(id.phoneButton);
        callSupplier.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String supplierNumber = bookSupplierPhone.getText().toString().trim();
                orderBookByPhone(supplierNumber);
            }
        });

        bookGenreSpinner();

    }


    /**
     * Select the book genre
     */
    private void bookGenreSpinner() {
        ArrayAdapter genreSpinner = ArrayAdapter.createFromResource(this, array.array_book_genres, android.R.layout.simple_spinner_item);
        genreSpinner.setDropDownViewResource(android.R.layout.simple_dropdown_item_1line);
        bookGenre.setAdapter(genreSpinner);

        bookGenre.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selection = (String) parent.getItemAtPosition(position);
                if (!TextUtils.isEmpty(selection)) {
                    if (selection.equals(getString(string.genre_science_fiction))) {
                        genre = DatabaseContract.BookEntry.GENRE_SCIENCE_FICTION;
                    } else if (selection.equals(getString(string.genre_drama))) {
                        genre = DatabaseContract.BookEntry.GENRE_DRAMA;
                    } else if (selection.equals(getString(string.genre_romance))) {
                        genre = BookEntry.GENRE_ROMANCE;
                    } else if (selection.equals(getString(string.genre_horror))) {
                        genre = DatabaseContract.BookEntry.GENRE_HORROR;
                    } else if (selection.equals(getString(string.genre_self_help))) {
                        genre = BookEntry.GENRE_SELF_HELP;
                    } else if (selection.equals(getString(string.genre_travel))) {
                        genre = DatabaseContract.BookEntry.GENRE_TRAVEL;
                    } else {
                        genre = DatabaseContract.BookEntry.GENRE_NOT_AVAILABLE;
                    }
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                genre = BookEntry.GENRE_NOT_AVAILABLE;
            }
        });
    }


    /**
     * Save Book Edition
     * This will read the input, trim and save information
     * Will check if fields are empty or not and give warning message
     * After Review Note: There needs to be validation of the fields to check if they're valid
     * Extra validation steps inserted. Fixed validation if all fields are empty and added an extra validation if there's too many characters
     */
    private boolean saveBook() {
        if (!validateEditTextToString()) {
            return false;
        }
        String saveBookName = bookName.getText().toString().trim();
        String saveBookPrice = bookPrice.getText().toString().trim();
        String saveBookQuantity = bookQuantity.getText().toString().trim();
        String saveBookSupplier = bookSupplier.getText().toString().trim();
        String saveBookPhone = bookSupplierPhone.getText().toString().trim();
        String saveBookEmail = bookSupplierEmail.getText().toString().trim();

        // there's a try catch validation on validateEditTextToString()
        Double bookPriceDouble = Double.parseDouble(saveBookPrice);
        int bookQuantityInt = Integer.parseInt(saveBookQuantity);
        int bookSupplierNumberInt = Integer.parseInt(saveBookPhone);

        ContentValues values = new ContentValues();
        values.put(BookEntry.COLUMN_NAME, saveBookName);
        values.put(BookEntry.COLUMN_GENRE, genre);
        values.put(BookEntry.COLUMN_PRICE, bookPriceDouble);
        values.put(BookEntry.COLUMN_QUANTITY, bookQuantityInt);
        values.put(BookEntry.COLUMN_SUPPLIER, saveBookSupplier);
        values.put(BookEntry.COLUMN_SUPPLIER_PHONE, bookSupplierNumberInt);
        values.put(BookEntry.COLUMN_SUPPLIER_EMAIL, saveBookEmail);

        if (currentBook == null) {
            Uri newBookUri = getContentResolver().insert(BookEntry.CONTENT_URI, values);
            if (newBookUri == null) {
                Toast.makeText(this, getString(string.new_book_failed), Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, getString(string.book_added), Toast.LENGTH_SHORT).show();
            }
        } else {
            int booksAffected = getContentResolver().update(currentBook, values, null, null);
            if (booksAffected == 0) {
                Toast.makeText(this, getString(string.edit_book_fail), Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, getString(string.edit_book_success), Toast.LENGTH_SHORT).show();
            }
            finish();
        }
        return true;
    }

    public boolean validateEditTextToString() {
        String saveBookName = bookName.getText().toString().trim();
        String saveBookPrice = bookPrice.getText().toString().trim();
        String saveBookQuantity = bookQuantity.getText().toString().trim();
        String saveBookSupplier = bookSupplier.getText().toString().trim();
        String saveBookPhone = bookSupplierPhone.getText().toString().trim();
        String saveBookEmail = bookSupplierEmail.getText().toString().trim();

        if (currentBook == null && TextUtils.isEmpty(saveBookName) && TextUtils.isEmpty(saveBookPrice) && saveBookQuantity.equals("0") && TextUtils.isEmpty(saveBookSupplier) &&
                TextUtils.isEmpty(saveBookPhone) && TextUtils.isEmpty(saveBookEmail)) {
            Toast.makeText(this, (getString(string.please_insert_data)), Toast.LENGTH_SHORT).show();
            return false;
        }

        if (TextUtils.isEmpty(saveBookName) || saveBookName.length() > 50) {
            bookName.setError(getString(string.please_check_data));
            return false;
        }

        if (TextUtils.isEmpty(saveBookPrice) || saveBookPrice.length() > 3) {
            bookPrice.setError(getString(string.please_check_data));
            return false;
        }

        if (TextUtils.isEmpty(saveBookQuantity) || saveBookQuantity.length() > 3) {
            bookQuantity.setError(getString(string.please_check_data));
            return false;
        }

        if (TextUtils.isEmpty(saveBookSupplier) || saveBookSupplier.length() > 50) {
            bookSupplier.setError(getString(string.please_check_data));
            return false;
        }

        if (TextUtils.isEmpty(saveBookPhone) || saveBookPhone.length() > 30) {
            bookSupplierPhone.setError(getString(string.please_check_data));
            return false;
        }

        if (TextUtils.isEmpty(saveBookEmail) || saveBookEmail.length() > 50) {
            bookSupplierEmail.setError(getString(string.please_check_data));
            return false;
        }

        try {
            int bookSupplierNumberInt = Integer.parseInt(saveBookPhone);
        } catch (NumberFormatException e) {
            bookSupplierPhone.setError(getString(string.phone_error));
            return false;
        }

        try {
            Double bookPriceDouble = Double.parseDouble(saveBookPrice);
        } catch (NumberFormatException e) {
            bookPrice.setError(getString(string.price_error));
            return false;
        }

        try {
            int bookQuantityInt = Integer.parseInt(saveBookQuantity);
        } catch (NumberFormatException e) {
            bookQuantity.setError(getString(string.quantity_error));
            return false;
        }
        return true;
    }

    /**
     * Creates Options Menu
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_edit, menu);
        return true;
    }

    /**
     * This method is called so we can hide the delete menu item if the book is new
     */
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        if (currentBook == null) {
            MenuItem deleteBook = menu.findItem(id.action_delete);
            deleteBook.setVisible(false);
        }
        return true;
    }

    /**
     * This method is for the menu options
     * We can save the new items, delete or return home
     * If there are unsaved changes, the user should be warned by a click listener
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case id.action_save:
                boolean saved = saveBook();
                if (saved) {
                    finish();
                }
                return true;
            case id.action_delete:
                showDeleteBookConfirmation();
                return true;
            case android.R.id.home:
                if (!hasBookBeenEdited) {
                    NavUtils.navigateUpFromSameTask(BookStoreEditor.this);
                    return true;
                }

                DialogInterface.OnClickListener discardChanges =
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                NavUtils.navigateUpFromSameTask(BookStoreEditor.this);
                            }
                        };
                unsavedChanges(discardChanges);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * This method confirms if the book has been edited when the user clicks back
     * If it has been edited, warn user
     */
    @Override
    public void onBackPressed() {
        if (!hasBookBeenEdited) {
            super.onBackPressed();
            return;
        }
        DialogInterface.OnClickListener discardButtonClickListener =
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        finish();
                    }
                };
        unsavedChanges(discardButtonClickListener);
    }

    /**
     * This loader will execute the query method on a background thread
     */
    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        String[] projection = {
                BookEntry._ID,
                BookEntry.COLUMN_NAME,
                BookEntry.COLUMN_GENRE,
                BookEntry.COLUMN_PRICE,
                BookEntry.COLUMN_QUANTITY,
                BookEntry.COLUMN_SUPPLIER,
                BookEntry.COLUMN_SUPPLIER_PHONE,
                BookEntry.COLUMN_SUPPLIER_EMAIL,};
        return new CursorLoader(this, currentBook, projection, null, null, null);
    }

    /**
     * This will extract the value from the Cursor and update the views with the values from the database
     */
    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        if (cursor == null || cursor.getCount() < 1) {
            return;
        }

        if (cursor.moveToFirst()) {
            int bookNameColumnIndex = cursor.getColumnIndex(BookEntry.COLUMN_NAME);
            int bookGenreColumnIndex = cursor.getColumnIndex(BookEntry.COLUMN_GENRE);
            int bookPriceColumnIndex = cursor.getColumnIndex(BookEntry.COLUMN_PRICE);
            int bookQuantityColumnIndex = cursor.getColumnIndex(BookEntry.COLUMN_QUANTITY);
            int bookSupplierColumnIndex = cursor.getColumnIndex(BookEntry.COLUMN_SUPPLIER);
            int bookPhoneColumnIndex = cursor.getColumnIndex(BookEntry.COLUMN_SUPPLIER_PHONE);
            int bookEmailColumnIndex = cursor.getColumnIndex(BookEntry.COLUMN_SUPPLIER_EMAIL);

            final String name = cursor.getString(bookNameColumnIndex);
            int genre = cursor.getInt(bookGenreColumnIndex);
            double price = cursor.getInt(bookPriceColumnIndex);
            int quantity = cursor.getInt(bookQuantityColumnIndex);
            String supplier = cursor.getString(bookSupplierColumnIndex);
            int phone = cursor.getInt(bookPhoneColumnIndex);
            final String email = cursor.getString(bookEmailColumnIndex);

            bookName.setText(name);
            bookPrice.setText(Double.toString(price));
            bookQuantity.setText(Integer.toString(quantity));
            bookSupplier.setText(supplier);
            bookSupplierPhone.setText(Integer.toString(phone));
            bookSupplierEmail.setText(email);
            switch (genre) {
                case BookEntry.GENRE_SCIENCE_FICTION:
                    bookGenre.setSelection(1);
                    break;
                case BookEntry.GENRE_DRAMA:
                    bookGenre.setSelection(2);
                    break;
                case BookEntry.GENRE_ROMANCE:
                    bookGenre.setSelection(3);
                    break;
                case BookEntry.GENRE_HORROR:
                    bookGenre.setSelection(4);
                    break;
                case BookEntry.GENRE_SELF_HELP:
                    bookGenre.setSelection(5);
                    break;
                case BookEntry.GENRE_TRAVEL:
                    bookGenre.setSelection(6);
                    break;
                default:
                    bookGenre.setSelection(0);
                    break;
            }

        }

    }

    /**
     * Reset Values
     */
    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        bookName.setText("");
        bookPrice.setText("");
        bookQuantity.setText("");
        bookSupplier.setText("");
        bookSupplierPhone.setText("");
        bookSupplierEmail.setText("");
        bookGenre.setSelection(0);
    }

    /**
     * Warn users if there are unsaved changes that will be lost
     * Give them a chance to go back to editing or discard changes
     */
    private void unsavedChanges(
            DialogInterface.OnClickListener discardButtonClickListener) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(string.unsaved_changes);
        builder.setPositiveButton(string.discard, discardButtonClickListener);
        builder.setNegativeButton(string.keep_edit, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    /**
     * Ask the user if they want to delete the book
     */
    private void showDeleteBookConfirmation() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(string.delete_book);
        builder.setPositiveButton(string.delete, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                deleteBook();
            }
        });
        builder.setNegativeButton(string.cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    /**
     * Delete a book from the database
     */
    private void deleteBook() {
        if (currentBook != null) {
            int rowsDeleted = getContentResolver().delete(currentBook, null, null);
            if (rowsDeleted == 0) {
                Toast.makeText(this, getString(string.delete_book_failed), Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, getString(string.delete_book_succes), Toast.LENGTH_SHORT).show();
            }
        }
        finish();
    }

    /**
     * Send a book order by email to supplier
     * Edit: Added permission request in runtime
     */
    private void orderBookByEmail(String emailAddress, String bookName, String supplier) {
        if (!emailAddress.isEmpty()) {
            String subject = getString(string.order) + " " + bookName + " " + getString(string.email_book);
            String emailBody = getString((string.body01)) + " " + supplier + ",";
            emailBody = emailBody + "\n" + getString((string.body02)) + " " + bookName + " " + getString((string.body03));

            Intent sendEmail = new Intent(Intent.ACTION_SENDTO);
            sendEmail.setType("text/plain");
            sendEmail.setData(Uri.parse("mailto:"));
            sendEmail.putExtra(android.content.Intent.EXTRA_EMAIL, new String[]{emailAddress});
            sendEmail.putExtra(Intent.EXTRA_SUBJECT, subject);
            sendEmail.putExtra(Intent.EXTRA_TEXT, emailBody);
            if (sendEmail.resolveActivity(getPackageManager()) != null) {
                startActivity(sendEmail);
            }
        } else {
            Toast.makeText(BookStoreEditor.this, string.email_empty, Toast.LENGTH_SHORT).show();
        }
    }


    /**
     *  Call book supplier
     *  Edit: Added permission request in runtime
     **/
    private void orderBookByPhone(String phoneNumber) {
        if (!phoneNumber.isEmpty()) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
                Intent callSupplier = new Intent(Intent.ACTION_DIAL);
                callSupplier.setData(Uri.parse("tel:" + phoneNumber));
                if (callSupplier.resolveActivity(getPackageManager()) != null) {
                    startActivity(callSupplier);
                }
            }
        } else {
            Toast.makeText(BookStoreEditor.this, string.cell_phone_number, Toast.LENGTH_SHORT).show();
        }
    }
}

