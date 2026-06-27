package com.example.cs360project;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.fragment.app.Fragment;

public class EditFragment extends Fragment {

    private EditText etExistingItemName, etNewItemName, etNewQuantity, etNewReorderLevel;
    private Button btnSaveChanges, btnDeleteItem;
    private DatabaseHelper databaseHelper;
    private int itemId = -1;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_edit, container, false);

        etExistingItemName = view.findViewById(R.id.etExistingItemName);
        etNewItemName = view.findViewById(R.id.etNewItemName);
        etNewQuantity = view.findViewById(R.id.etNewQuantity);
        etNewReorderLevel = view.findViewById(R.id.etNewReorderLevel);
        btnSaveChanges = view.findViewById(R.id.btnSaveChanges);
        databaseHelper = new DatabaseHelper(requireContext());

        // autopopulate fields if navigated from a tile
        Bundle args = getArguments();
        if (args != null) {
            itemId = args.getInt("item_id", -1);
            String name = args.getString("item_name", "");
            int qty = args.getInt("item_qty", 0);
            int reorder = args.getInt("item_reorder", 0);

            etExistingItemName.setText(name);
            etExistingItemName.setEnabled(false); // lock existing name field
            etNewItemName.setText(name);
            etNewQuantity.setText(String.valueOf(qty));
            etNewReorderLevel.setText(String.valueOf(reorder));
        }

        // save changes
        btnSaveChanges.setOnClickListener(v -> {
            String newName = etNewItemName.getText().toString().trim();
            String newQtyStr = etNewQuantity.getText().toString().trim();
            String newReorderStr = etNewReorderLevel.getText().toString().trim();

            if (newName.isEmpty() || newQtyStr.isEmpty() || newReorderStr.isEmpty()) {
                Toast.makeText(requireContext(), "Please fill in all fields", Toast.LENGTH_SHORT).show();
                return;
            }

            if (itemId == -1) {
                String existingName = etExistingItemName.getText().toString().trim();
                itemId = databaseHelper.getItemIdByName(existingName);
            }

            if (itemId == -1) {
                Toast.makeText(requireContext(), "Item not found", Toast.LENGTH_SHORT).show();
                return;
            }

            int newQty = Integer.parseInt(newQtyStr);
            int newReorder = Integer.parseInt(newReorderStr);

            boolean success = databaseHelper.updateItem(requireContext(), itemId, newName, newQty, newReorder);

            if (success) {
                Toast.makeText(requireContext(), newName + " updated successfully", Toast.LENGTH_SHORT).show();
                navigateBackToDatabase();
            } else {
                Toast.makeText(requireContext(), "Failed to update item", Toast.LENGTH_SHORT).show();
            }
        });

        return view;
    }

    private void navigateBackToDatabase() {
        requireActivity().getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragmentContainer, new DatabaseDisplayFragment())
                .addToBackStack(null)
                .commit();
    }
}