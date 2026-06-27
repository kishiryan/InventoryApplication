package com.example.cs360project;

import android.content.res.ColorStateList;
import android.database.Cursor;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.GridLayout;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.fragment.app.Fragment;

public class DatabaseDisplayFragment extends Fragment {

    private GridLayout gridInventory;
    private DatabaseHelper databaseHelper;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_database_display, container, false);

        gridInventory = view.findViewById(R.id.gridInventory);
        databaseHelper = new DatabaseHelper(requireContext());

        // custom column count variable to screen width (ex. desktop, tablet, phone)
        DisplayMetrics metrics = getResources().getDisplayMetrics();
        float screenWidthDp = metrics.widthPixels / metrics.density;

        if (screenWidthDp >= 840) {
            gridInventory.setColumnCount(4);
        } else if (screenWidthDp >= 600) {
            gridInventory.setColumnCount(3);
        } else {
            gridInventory.setColumnCount(2);
        }

        // navigate to add item fragment
        Button btnNavToAddItem = view.findViewById(R.id.btn_navToAddItem);
        btnNavToAddItem.setOnClickListener(v ->
                requireActivity().getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.fragmentContainer, new AddFragment())
                        .addToBackStack(null)
                        .commit()
        );

        // load inventory tiles
        loadInventoryTiles();

        return view;
    }

    private void loadInventoryTiles() {
        gridInventory.removeAllViews();
        Cursor cursor = databaseHelper.getAllItems();

        if (cursor.getCount() == 0) {
            TextView empty = new TextView(requireContext());
            empty.setText(R.string.noInventoryItemsFound);
            empty.setPadding(12, 12, 12, 12);
            gridInventory.addView(empty);
        } else {
            while (cursor.moveToNext()) {
                int id = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.col_id));
                String name = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.col_item_name));
                int qty = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.col_quantity));
                int reorder = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.col_reorder_level));

                // outer card container
                LinearLayout card = new LinearLayout(requireContext());
                card.setOrientation(LinearLayout.VERTICAL);
                card.setPadding(24, 24, 24, 16);
                card.setBackgroundColor(qty <= reorder ? 0xFFFFCDD2 : 0xFFE8F5E9);

                GridLayout.LayoutParams cardParams = new GridLayout.LayoutParams();
                cardParams.width = 0;
                cardParams.columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f);
                cardParams.setMargins(8, 8, 8, 8);
                card.setLayoutParams(cardParams);

                // item info text
                TextView tileText = new TextView(requireContext());
                tileText.setText(name + "\nQty: " + qty + "\nReorder @: " + reorder);
                tileText.setTextSize(14f);
                tileText.setPadding(0, 0, 0, 12);
                card.addView(tileText);

                // delete button
                Button btnDelete = new Button(requireContext());
                btnDelete.setText(R.string.delete);
                btnDelete.setTextSize(12f);
                btnDelete.setBackgroundTintList(ColorStateList.valueOf(0xFFD32F2F));
                btnDelete.setTextColor(0xFFFFFFFF);
                LinearLayout.LayoutParams btnParams = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT);
                btnDelete.setLayoutParams(btnParams);
                card.addView(btnDelete);

                // click tile text to navigate to edit fragment
                tileText.setOnClickListener(v -> {
                    Bundle args = new Bundle();
                    args.putInt("item_id", id);
                    args.putString("item_name", name);
                    args.putInt("item_qty", qty);
                    args.putInt("item_reorder", reorder);

                    EditFragment editFragment = new EditFragment();
                    editFragment.setArguments(args);

                    requireActivity().getSupportFragmentManager()
                            .beginTransaction()
                            .replace(R.id.fragmentContainer, editFragment)
                            .addToBackStack(null)
                            .commit();
                });

                // delete button click
                btnDelete.setOnClickListener(v -> {
                    boolean success = databaseHelper.deleteItem(id);
                    if (success) {
                        Toast.makeText(requireContext(), name + " deleted", Toast.LENGTH_SHORT).show();
                        loadInventoryTiles();
                    } else {
                        Toast.makeText(requireContext(), "Failed to delete " + name, Toast.LENGTH_SHORT).show();
                    }
                });

                gridInventory.addView(card);
            }
        }
        cursor.close();
    }
}
