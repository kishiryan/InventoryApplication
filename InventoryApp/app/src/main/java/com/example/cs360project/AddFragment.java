package com.example.cs360project;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.fragment.app.Fragment;

public class AddFragment extends Fragment {

    private EditText etItemName, etItemQty, etReorderLevel;
    private Button btnAddItem;
    private DatabaseHelper databaseHelper;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_add, container, false);

        etItemName = view.findViewById(R.id.etItemName);
        etItemQty = view.findViewById(R.id.etItemQty);
        etReorderLevel = view.findViewById(R.id.etReorderLevel);
        btnAddItem = view.findViewById(R.id.btnAddItem);
        databaseHelper = new DatabaseHelper(requireContext());

        btnAddItem.setOnClickListener(v -> {
            String name = etItemName.getText().toString().trim();
            String qtyStr = etItemQty.getText().toString().trim();
            String reorderStr = etReorderLevel.getText().toString().trim();

            // validate fields are not empty
            if (name.isEmpty() || qtyStr.isEmpty() || reorderStr.isEmpty()) {
                Toast.makeText(requireContext(), "Please fill in all fields", Toast.LENGTH_SHORT).show();
                return;
            }

            int quantity = Integer.parseInt(qtyStr);
            int reorderLevel = Integer.parseInt(reorderStr);

            boolean success = databaseHelper.addItem(requireContext(), name, quantity, reorderLevel);

            if (success) {
                Toast.makeText(requireContext(), name + " added successfully", Toast.LENGTH_SHORT).show();
                etItemName.setText("");
                etItemQty.setText("");
                etReorderLevel.setText("");
            } else {
                Toast.makeText(requireContext(), "\"" + name + "\" already exists", Toast.LENGTH_SHORT).show();
            }
        });

        return view;
    }
}