package com.example.android.inventory;

import android.Manifest;
import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.android.inventory.data.InventoryContract;

import java.io.ByteArrayOutputStream;

/**
 * Created by meets on 1/12/2018.
 */

public class EditorActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {
    EditText mQuantityTextView;
    EditText mProductNameTextView;
    EditText mPriceTextView;
    EditText mPhoneTextView;
    Button mDeleteButtonView;
    Button mOrderButtonView;
    ImageView mImageView;
    long quantity;
    Uri mCurrentInventoryUri;
    String picturePath = null;
    private boolean addingItem = false;
    private static int RESULT_LOAD_IMAGE = 1;
    private boolean mInventoryHasChanged = false;
    int readPermission;
    int writePermission;
    private static final int PERMISSION_REQUEST_READ_STORAGE = 0;
    private boolean makeImageMandatory = false;
    Button mDecreaseQtyBtnView;
    Button mIncreaseQtyBtnView;
    String sPhoneNumber;

    // OnTouchListener that listens for any user touches on a View, implying that they are modifying
    // the view, and we change the mInventoryHasChanged boolean to true.

    private View.OnTouchListener mTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            mInventoryHasChanged = true;
            return false;
        }
    };
    private static final int EXISTING_INVENTORY_LOADER = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editor);
        Intent intent = getIntent();
        mCurrentInventoryUri = intent.getData();
        mDeleteButtonView = (Button) findViewById(R.id.delete_btn);
        mOrderButtonView = (Button) findViewById(R.id.order_btn);
        mPhoneTextView = (EditText)findViewById(R.id.phone_edit_text);
        if (mCurrentInventoryUri != null) {
            setTitle(R.string.editor_activity_title_edit_Inventory);
            getSupportLoaderManager().initLoader(EXISTING_INVENTORY_LOADER, null, this);
        } else {
            setTitle(R.string.editor_activity_title_new_inventory);
            addingItem = true;
            mOrderButtonView.setVisibility(View.GONE);
            mDeleteButtonView.setVisibility(View.GONE);
            // Invalidate the options menu, so the "Delete" menu option can be hidden.
            // (It doesn't make sense to delete an item that hasn't been created yet.)
            invalidateOptionsMenu();
        }
        readPermission = ContextCompat.checkSelfPermission(this,
                Manifest.permission.READ_EXTERNAL_STORAGE);
        writePermission = ContextCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE);
        if (!(readPermission == PERMISSION_REQUEST_READ_STORAGE)) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                    PERMISSION_REQUEST_READ_STORAGE);
        } else {
            makeImageMandatory = true;
        }

        mQuantityTextView = (EditText) findViewById(R.id.quantity_edit_text);
        mProductNameTextView = (EditText) findViewById(R.id.product_name);
        mPriceTextView = (EditText) findViewById(R.id.price_edit_text);
        mImageView = (ImageView) findViewById(R.id.image_view);
        mDecreaseQtyBtnView = (Button) findViewById(R.id.dec_qty_btn);
        mIncreaseQtyBtnView = (Button) findViewById(R.id.inc_qty_btn);
        mPhoneTextView = (EditText)findViewById(R.id.phone_edit_text);

        //Touch Listeners
        mQuantityTextView.setOnTouchListener(mTouchListener);
        mProductNameTextView.setOnTouchListener(mTouchListener);
        mPriceTextView.setOnTouchListener(mTouchListener);
        mImageView.setOnTouchListener(mTouchListener);
        mDecreaseQtyBtnView.setOnTouchListener(mTouchListener);
        mDeleteButtonView.setOnTouchListener(mTouchListener);
        mIncreaseQtyBtnView.setOnTouchListener(mTouchListener);
        mPhoneTextView.setOnTouchListener(mTouchListener);

        mIncreaseQtyBtnView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String qty_string = mQuantityTextView.getText().toString();
                long qty_long = 0;
                if (!(qty_string.isEmpty() || qty_string == null)) {
                    qty_long = Long.parseLong(qty_string);
                    if (qty_long >= 10000) {
                        quantity = 10000;
                        displayQuantity(quantity);
                        displayToastMessage("Qty cannot be greater than 10000. Setting quantity to 10000");
                    } else if (qty_long >= 0 || qty_long <= 999) {
                        increment();
                    }
                } else {
                    quantity = 0;
                    increment();
                }
            }
        });


        mDecreaseQtyBtnView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String qty_string = mQuantityTextView.getText().toString();
                if (!(qty_string.isEmpty() || qty_string == null)) {

                    quantity = Long.parseLong(qty_string);
                    if (quantity >= 1) {
                        decrement();
                    } else {
                        quantity = 0;
                        displayQuantity(quantity);
                        displayToastMessage("Qty cannot be less than 0");
                    }
                } else {
                    quantity = 0;
                    displayQuantity(quantity);
                }
            }
        });

        mDeleteButtonView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                deleteitem();
            }
        });
        mOrderButtonView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Intent.ACTION_DIAL);
                intent.setData(Uri.parse("tel:"+sPhoneNumber));
                startActivity(intent);
            }
        });

        mImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (makeImageMandatory) {
                    Intent i = new Intent(
                            Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                    startActivityForResult(i, RESULT_LOAD_IMAGE);
                } else {
                    displayToastMessage(getString(R.string.permission_denied_message));
                }
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case PERMISSION_REQUEST_READ_STORAGE: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    makeImageMandatory = true;
                } else {
                    makeImageMandatory = false;
                }
            }
            // other 'case' lines to check for other
            // permissions this app might request.
        }
    }

    /**
     * Method to complete the image pick from gallery
     *
     * @param requestCode
     * @param resultCode
     * @param data
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RESULT_LOAD_IMAGE && resultCode == RESULT_OK && null != data) {
            Uri selectedImage = data.getData();
            String[] filePathColumn = {MediaStore.Images.Media.DATA};
            Cursor cursor = getContentResolver().query(selectedImage,
                    filePathColumn, null, null, null);
            cursor.moveToFirst();

            int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
            picturePath = cursor.getString(columnIndex);
            cursor.close();
            Bitmap bitmap = BitmapFactory.decodeFile(picturePath);
            mImageView.setImageBitmap(bitmap);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu options from the res/menu/menu_editor.xml file.
        // This adds menu items to the app bar.
        getMenuInflater().inflate(R.menu.menu_editor, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_save:
                if (validations()) {
                    saveInventory();
                    finish();
                }
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * Method to validate the inputs
     *
     * @return
     */
    public boolean validations() {
        boolean result = false;
        long quantity_long;
        long price_long;
        String nameString = mProductNameTextView.getText().toString().trim();
        String quantityString = mQuantityTextView.getText().toString().trim();
        String priceString = mPriceTextView.getText().toString().trim();
        String phoneString = mPhoneTextView.getText().toString().trim();
        if (nameString.isEmpty() || nameString == null || quantityString.isEmpty() || quantityString == null || priceString.isEmpty() || priceString == null || isImageNotPresent() ||phoneString.isEmpty() || phoneString == null) {
            displayToastMessage(getString(R.string.all_fields_mandatory_msg));
            return result;
        } else {
            quantity_long = Long.parseLong(quantityString);
            price_long = Long.parseLong(priceString);
            if (quantity_long > 10000) {
                displayToastMessage(getString(R.string.quantity_error_msg));
                mQuantityTextView.setText("");
                return result;
            } else if (price_long > 100000) {
                displayToastMessage(getString(R.string.prices_error_msg));
                mPriceTextView.setText("");
                return result;
            } else if(!(phoneString.length() == 10)){
                displayToastMessage(getString(R.string.phone_error_msg));
                return result;
            } else {
                result = true;
                return result;
            }
        }
    }

    /**
     * Method to validate if image is present or not
     *
     * @return
     */
    public boolean isImageNotPresent() {
        if (addingItem) {
            if (picturePath == null) {
                if (makeImageMandatory) {
                    return true;
                    //true creates toast
                } else {
                    return false;
                }
            } else {
                return false;
            }
        } else {
            return false;
        }
    }

    /**
     * This method is called when the - button is clicked.
     */
    public void decrement() {
        quantity = quantity - 1;
        if (quantity <= 0) {
            displayToastMessage(getString(R.string.min_quantity_error));
            quantity = 0;
        }
        displayQuantity(quantity);
    }

    /**
     * This method is called when the - button is clicked.
     */
    public void increment() {
        quantity = quantity + 1;
        displayQuantity(quantity);
    }

    /**
     * This method displays the given quantity value on the screen.
     */
    private void displayQuantity(long number) {

        mQuantityTextView.setText(String.valueOf(number));
    }

    /**
     * Method to show Toast messages
     *
     * @param message
     */
    public void displayToastMessage(String message) {
        Toast toast = Toast.makeText(this, message, Toast.LENGTH_LONG);
        toast.setGravity(Gravity.BOTTOM, 0, 0);
        toast.show();
    }

    /**
     * Method to Save Inventory to DB
     */
    public void saveInventory() {
        ContentValues values = new ContentValues();
        String nameString = mProductNameTextView.getText().toString().trim();
        String quantityString = mQuantityTextView.getText().toString().trim();
        String priceString = mPriceTextView.getText().toString().trim();
        String phoneString = mPhoneTextView.getText().toString().trim();
        Bitmap bitmap = null;
        if (!(picturePath == null)) {
            bitmap = BitmapFactory.decodeFile(picturePath);
        } else {
            BitmapDrawable drawable = (BitmapDrawable) mImageView.getDrawable();
            bitmap = drawable.getBitmap();
        }
        byte[] imageData = getBitmapAsByteArray(bitmap);
        int quantityInt;
        int priceInt;
        long phoneLong;
        if (!(nameString.isEmpty() && priceString.isEmpty() && quantityString.isEmpty() && phoneString.isEmpty())) {

            if (quantityString.isEmpty()) {
                quantityInt = 0;
            } else {
                quantityInt = Integer.parseInt(quantityString);
            }
            if (priceString.isEmpty()) {
                priceInt = 0;
            } else {
                priceInt = Integer.parseInt(priceString);
            }
            values.put(InventoryContract.InventoryEntry.COLUMN_PRODUCT_NAME, nameString);
            values.put(InventoryContract.InventoryEntry.COLUMN_QUANTITY, quantityInt);
            values.put(InventoryContract.InventoryEntry.COLUMN_PRICE, priceInt);
            values.put(InventoryContract.InventoryEntry.COLUMN_IMAGE, imageData);
            values.put(InventoryContract.InventoryEntry.COLUMN_PHONE, phoneString);
            Uri uri = null;
            int changedRowID = 0;
            if (mCurrentInventoryUri != null) {
                changedRowID = updateInDb(mCurrentInventoryUri, values);
                if (changedRowID != 0) {
                    Toast.makeText(this, getString(R.string.editor_insert_item_successful),
                            Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, getString(R.string.editor_insert_item_failed),
                            Toast.LENGTH_SHORT).show();
                }
            } else {
                uri = getContentResolver().insert(InventoryContract.InventoryEntry.CONTENT_URI, values);
                if (uri == null) {
                    // If the new content URI is null, then there was an error with insertion.
                    Toast.makeText(this, getString(R.string.editor_insert_item_failed),
                            Toast.LENGTH_SHORT).show();
                } else {
                    // Otherwise, the insertion was successful and we can display a toast.
                    Toast.makeText(this, getString(R.string.editor_insert_item_successful),
                            Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    public static byte[] getBitmapAsByteArray(Bitmap bitmap) {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 50, outputStream);
        return outputStream.toByteArray();

    }

    public int updateInDb(Uri uri, ContentValues values) {
        return getContentResolver().update(uri, values, null, null);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        String[] projection = {
                InventoryContract.InventoryEntry._ID,
                InventoryContract.InventoryEntry.COLUMN_PRODUCT_NAME,
                InventoryContract.InventoryEntry.COLUMN_QUANTITY,
                InventoryContract.InventoryEntry.COLUMN_PRICE,
                InventoryContract.InventoryEntry.COLUMN_IMAGE,
                InventoryContract.InventoryEntry.COLUMN_PHONE};
        return new CursorLoader(this, mCurrentInventoryUri, projection, null, null, null);

    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        // Bail early if the cursor is null or there is less than 1 row in the cursor
        if (cursor == null || cursor.getCount() < 1) {
            return;
        }
        if (cursor.moveToFirst()) {
            mProductNameTextView.setText(cursor.getString(cursor.getColumnIndex(InventoryContract.InventoryEntry.COLUMN_PRODUCT_NAME)));
            int quantity = cursor.getInt(cursor.getColumnIndex(InventoryContract.InventoryEntry.COLUMN_QUANTITY));
            long price = cursor.getInt(cursor.getColumnIndex(InventoryContract.InventoryEntry.COLUMN_PRICE));
            mQuantityTextView.setText(Integer.toString(quantity));
            mPriceTextView.setText(Long.toString(price));
            mPhoneTextView.setText(cursor.getString(cursor.getColumnIndex(InventoryContract.InventoryEntry.COLUMN_PHONE)));
            sPhoneNumber = mPhoneTextView.getText().toString();
            byte[] image = null;
            try {
                image = cursor.getBlob(cursor.getColumnIndex(InventoryContract.InventoryEntry.COLUMN_IMAGE));
            } catch (Exception e) {
                Log.d("cursor problem,", cursor.getBlob(cursor.getColumnIndex(InventoryContract.InventoryEntry.COLUMN_IMAGE)).toString());
                e.printStackTrace();
            }
            Bitmap bmp = BitmapFactory.decodeByteArray(image, 0, image.length);
            mImageView.setImageBitmap(bmp);
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mProductNameTextView.setText("");
        mQuantityTextView.setText("");
        mPriceTextView.setText("");
        mPhoneTextView.setText("");
    }

    @Override
    public void onBackPressed() {
        // If the Item hasn't changed, continue with handling back button press
        if (!mInventoryHasChanged) {
            super.onBackPressed();
            return;
        }
        // Otherwise if there are unsaved changes, setup a dialog to warn the user.
        // Create a click listener to handle the user confirming that changes should be discarded.
        DialogInterface.OnClickListener discardButtonClickListener =
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        // User clicked "Discard" button, close the current activity.
                        finish();
                    }
                };

        // Show dialog that there are unsaved changes
        showUnsavedChangesDialog(discardButtonClickListener);
    }

    private void showUnsavedChangesDialog(
            DialogInterface.OnClickListener discardButtonClickListener) {
        // Create an AlertDialog.Builder and set the message, and click listeners
        // for the positive and negative buttons on the dialog.
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.unsaved_changes_dialog_msg);
        builder.setPositiveButton(R.string.discard, discardButtonClickListener);
        builder.setNegativeButton(R.string.keep_editing, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Keep editing" button, so dismiss the dialog
                // and continue editing the item.
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });

        // Create and show the AlertDialog
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    /**
     * Perform the deletion of the Item in the database.
     */
    private void deleteitem() {
        if (mCurrentInventoryUri != null) {
            DialogInterface.OnClickListener yesButtonClickListener =
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            int delID = getContentResolver().delete(mCurrentInventoryUri, null, null);
                            if (delID != 0) {
                                displayToastMessage(getString(R.string.editor_delete_item_successful));
                            } else {
                                displayToastMessage(getString(R.string.editor_delete_item_failed));
                            }
                            // User clicked "Yes" button, Delete and close the current activity.
                            finish();
                        }
                    };
            // Show dialog that there are unsaved changes
            showConfirmationDeleteDialog(yesButtonClickListener);
        }
    }

    /**
     * Method for Showing confirmation on Deleting an Item
     *
     * @param yesButtonClickListener
     */
    private void showConfirmationDeleteDialog(
            DialogInterface.OnClickListener yesButtonClickListener) {
        // Create an AlertDialog.Builder and set the message, and click listeners
        // for the positive and negative buttons on the dialog.
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.are_you_sure);
        builder.setPositiveButton(R.string.yes, yesButtonClickListener);
        builder.setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Keep editing" button, so dismiss the dialog
                // and continue editing the Inventory.
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });

        // Create and show the AlertDialog
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }
}
