package com.example.android.inventory;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.support.v4.widget.CursorAdapter;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.android.inventory.data.InventoryContract;

import java.util.zip.Inflater;

/**
 * Created by meets on 1/13/2018.
 */

public class InventoryCursorAdapter extends CursorAdapter {
    Button saleButton;
    Context mcontext;

    public InventoryCursorAdapter(Context context, Cursor c) {
        super(context, c);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        mcontext = context;
        return LayoutInflater.from(context).inflate(R.layout.list_item, parent, false);
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        TextView nameTextView = (TextView) view.findViewById(R.id.product_name);
        TextView quantityTextView = (TextView) view.findViewById(R.id.quantity);
        TextView priceTextView = (TextView) view.findViewById(R.id.price);
        String name = cursor.getString(cursor.getColumnIndex(InventoryContract.InventoryEntry.COLUMN_PRODUCT_NAME));
        String quantity = cursor.getString(cursor.getColumnIndex(InventoryContract.InventoryEntry.COLUMN_QUANTITY));
        String price = cursor.getString(cursor.getColumnIndex(InventoryContract.InventoryEntry.COLUMN_PRICE));
        nameTextView.setText(name);
        quantityTextView.setText(quantity);
        priceTextView.setText(price);
    }

    @Override
    public View getView(final int position, View convertView, final ViewGroup parent) {

        final ViewHolder viewHolder;
        if (convertView == null) {
            convertView = LayoutInflater.from(mContext).inflate(R.layout.list_item, parent, false);
            viewHolder = new ViewHolder();
            viewHolder.productName = (TextView) convertView.findViewById(R.id.product_name);
            viewHolder.quantity = (TextView) convertView.findViewById(R.id.quantity);
            viewHolder.price = (TextView) convertView.findViewById(R.id.price);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }
        final View v = convertView;
        saleButton = (Button) convertView.findViewById(R.id.sale_button);
        saleButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                int num = (int) getItemId(position);
                Uri updateSaleURI = Uri.withAppendedPath(InventoryContract.InventoryEntry.CONTENT_URI, String.valueOf(num));
                String sProductName = viewHolder.productName.getText().toString();
                String sQuantity = viewHolder.quantity.getText().toString().trim();
                String sPrice = viewHolder.price.getText().toString().trim();
                long lQuantity = Long.parseLong(sQuantity);
                if (lQuantity <= 0) {
                    Toast toast = Toast.makeText(mContext, "Qty cannot be less than 0", Toast.LENGTH_LONG);
                    toast.setGravity(Gravity.BOTTOM, 0, 0);
                    toast.show();
                } else {
                    lQuantity = lQuantity - 1;
                }
                int iPrice = Integer.parseInt(sPrice);
                ContentValues contentValues = new ContentValues();
                contentValues.put(InventoryContract.InventoryEntry.COLUMN_PRODUCT_NAME, sProductName);
                contentValues.put(InventoryContract.InventoryEntry.COLUMN_QUANTITY, lQuantity);
                contentValues.put(InventoryContract.InventoryEntry.COLUMN_PRICE, iPrice);
                v.getContext().getContentResolver().update(updateSaleURI, contentValues, null, null);
            }
        });
        return super.getView(position, convertView, parent);
    }

    /**
     * Class with ViewHolder Objects
     */
    private final class ViewHolder {
        public TextView productName;
        public TextView quantity;
        public TextView price;
    }

}
