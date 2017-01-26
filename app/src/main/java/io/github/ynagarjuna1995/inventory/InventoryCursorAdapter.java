package io.github.ynagarjuna1995.inventory;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CursorAdapter;
import android.widget.TextView;
import android.widget.Toast;

import io.github.ynagarjuna1995.inventory.data.InventoryContract;

/**
 * {@link InventoryCursorAdapter} is an adapter for a list or grid view
 * that uses a {@link Cursor} of inventory data as its data source. This adapter knows
 * how to create list items for each row of product data in the {@link Cursor}.
 */
public class InventoryCursorAdapter extends CursorAdapter {

    Cursor mCursor;
    Context mContext;

    int rowsAffected;
    int newQuantity;

    /**
     * Constructs a new {@link InventoryCursorAdapter}.
     *
     * @param context The context
     * @param c       The cursor from which to get the data.
     */
    public InventoryCursorAdapter(Context context, Cursor c) {
        super(context, c, 0 /* flags */);
    }


    /**
     * Makes a new blank list item view. No data is set (or bound) to the views yet.
     *
     * @param context app context
     * @param cursor  The cursor from which to get the data. The cursor is already
     *                moved to the correct position.
     * @param parent  The parent to which the new view is attached to
     * @return the newly created list item view.
     */
    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        // Inflate a list item view using the layout specified in list_item.xml
        return LayoutInflater.from(context).inflate(R.layout.list_item, parent, false);
    }

    /**
     * This method binds the product data (in the current row pointed to by cursor) to the given
     * list item layout. For github, the name for the current product can be set on the name TextView
     * in the list item layout.
     *
     * @param view    Existing view, returned earlier by newView() method
     * @param context app context
     * @param cursor  The cursor from which to get the data. The cursor is already moved to the
     *                correct row.
     */
    @Override
    public void bindView(View view, final Context context, Cursor cursor) {
        mContext = context;
        mCursor = cursor;
        // Find individual views that we want to modify in the list item layout
        TextView nameTextView = (TextView) view.findViewById(R.id.name);
        TextView priceTextView = (TextView) view.findViewById(R.id.price);
        final TextView quantityTextView = (TextView) view.findViewById(R.id.quantity);

        // Find the columns of product attributes that we're interested in
        int nameColumnIndex = cursor.getColumnIndex(InventoryContract.InventoryEntry.COLUMN_PRODUCT_NAME);
        int priceColumnIndex = cursor.getColumnIndex(InventoryContract.InventoryEntry.COLUMN_PRODUCT_PRICE);
        int quantityColumnIndex = cursor.getColumnIndex(InventoryContract.InventoryEntry.COLUMN_PRODUCT_QUANTITY);


        // Find the columns of attributes that we're interested in
        int itemIdIndex = cursor.getColumnIndex(InventoryContract.InventoryEntry._ID);

        // Read the attributes from the Cursor for the current product
        final int itemId = cursor.getInt(itemIdIndex);

        // Read the product attributes from the Cursor for the current product
        String productName = cursor.getString(nameColumnIndex);
        Integer productPrice = cursor.getInt(priceColumnIndex);
        Integer productQuantity = cursor.getInt(quantityColumnIndex);

        // If the product name is empty string or null, then use some default text
        // If the product name is empty string or null, then use some default text
        // that says "Unnamed product", so the TextView isn't blank.
        if (TextUtils.isEmpty(productName)) {
            productName = context.getString(R.string.unknown_product);
        }

        // Update the TextViews with the attributes for the current product
        nameTextView.setText(productName);
        priceTextView.setText(String.valueOf(productPrice));
        quantityTextView.setText(String.valueOf(productQuantity));

        //Decrease the product quantity by one when it's sold
        Button sellButton = (Button) view.findViewById(R.id.button_sell);
        sellButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //On click, update the current product quantity
                rowsAffected = saleOfAProduct(itemId, quantityTextView);
                if (rowsAffected != 0) {
                    quantityTextView.setText(String.valueOf(newQuantity));
                } else {
                    Toast.makeText(context, "Update not possible", Toast.LENGTH_SHORT).show();

                }
            }
        });
    }

    public int saleOfAProduct(int rowId, TextView qTextView) {
        mCursor.moveToPosition(rowId);
        int oldQuantity = Integer.parseInt(qTextView.getText().toString());

        if (oldQuantity > 0) {
            newQuantity = oldQuantity - 1;

            ContentValues values = new ContentValues();
            values.put(InventoryContract.InventoryEntry.COLUMN_PRODUCT_QUANTITY, String.valueOf(newQuantity));
            Uri currentProductUri = ContentUris.withAppendedId(InventoryContract.InventoryEntry.CONTENT_URI, rowId);

            rowsAffected = mContext.getContentResolver().update(currentProductUri, values, null, null);
        }
        return rowsAffected;
    }
}
