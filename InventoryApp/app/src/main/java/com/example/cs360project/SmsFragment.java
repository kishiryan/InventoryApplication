package com.example.cs360project;

import android.Manifest;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import android.telephony.SmsManager;

public class SmsFragment extends Fragment {

    private TextView txtSmsStatus;
    private Button btnSmsPermission;
    private LinearLayout lowInventoryAlerts;
    private DatabaseHelper databaseHelper;

    private final ActivityResultLauncher<String[]> requestPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestMultiplePermissions(), result -> {
                boolean allGranted = true;
                for (Boolean granted : result.values()) {
                    if (!granted) {
                        allGranted = false;
                        break;
                    }
                }
                if (allGranted) {
                    Toast.makeText(requireContext(), "SMS permissions granted ✓", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(requireContext(), "SMS permissions denied", Toast.LENGTH_SHORT).show();
                }
                updateSmsStatus(allGranted);
            });

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_sms, container, false);

        txtSmsStatus = view.findViewById(R.id.txtSmsStatus);
        btnSmsPermission = view.findViewById(R.id.btnSmsPermission);
        lowInventoryAlerts = view.findViewById(R.id.lowInventoryAlerts);
        databaseHelper = new DatabaseHelper(requireContext());

        // check current permission state on load
        checkSmsPermission();

        // load low inventory items
        loadLowInventoryAlerts();

        btnSmsPermission.setOnClickListener(v -> {
            if (!allPermissionsGranted()) {
                // show toast so user knows request is being made
                Toast.makeText(requireContext(), "Requesting SMS permissions...", Toast.LENGTH_SHORT).show();
                requestPermissionLauncher.launch(new String[]{
                        Manifest.permission.SEND_SMS,
                        Manifest.permission.RECEIVE_SMS,
                        Manifest.permission.READ_SMS
                });
            } else {
                Toast.makeText(requireContext(), "SMS permissions already granted ✓", Toast.LENGTH_SHORT).show();
                updateSmsStatus(true);
            }
        });

        // send alerts
        Button btnSendAlerts = view.findViewById(R.id.btnSendAlerts);
        btnSendAlerts.setOnClickListener(v -> {
            if (!allPermissionsGranted()) {
                Toast.makeText(requireContext(), "SMS permission not granted", Toast.LENGTH_SHORT).show();
                return;
            }
            sendLowInventoryAlerts();
        });

        return view;
    }

    // check all sms permissions
    private void checkSmsPermission() {
        updateSmsStatus(allPermissionsGranted());
    }

    private boolean allPermissionsGranted() {
        return ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.SEND_SMS) == PackageManager.PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.RECEIVE_SMS) == PackageManager.PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.READ_SMS) == PackageManager.PERMISSION_GRANTED;
    }

    private void sendLowInventoryAlerts() {
        Cursor cursor = databaseHelper.getLowInventoryItems();

        if (cursor.getCount() == 0) {
            Toast.makeText(requireContext(), "No low inventory items to alert", Toast.LENGTH_SHORT).show();
            return;
        }

        // replace with a real phone number for production
        String phoneNumber = "3333";

        int sentCount = 0;
        while (cursor.moveToNext()) {
            String name = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.col_item_name));
            int qty = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.col_quantity));
            int reorder = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.col_reorder_level));

            String message = name + " is low! Qty: " + qty + " (Reorder @: " + reorder + ")";
            SmsManager.getDefault().sendTextMessage(phoneNumber, null, message, null, null);
            sentCount++;
        }
        cursor.close();

        Toast.makeText(requireContext(), sentCount + " SMS alert(s) sent", Toast.LENGTH_SHORT).show();
    }

    private void updateSmsStatus(boolean granted) {
        if (granted) {
            txtSmsStatus.setText(R.string.sms_alerts_granted);
            btnSmsPermission.setEnabled(false);
            btnSmsPermission.setText(R.string.sms_permission_enabled);
        } else {
            txtSmsStatus.setText(R.string.sms_alerts_not_granted);
            btnSmsPermission.setEnabled(true);
            btnSmsPermission.setText(R.string.enable_sms_permission);
        }
    }

    // load low inventory items into the alerts layout
    private void loadLowInventoryAlerts() {
        lowInventoryAlerts.removeAllViews();
        Cursor cursor = databaseHelper.getLowInventoryItems();

        if (cursor.getCount() == 0) {
            TextView noAlerts = new TextView(requireContext());
            noAlerts.setText(R.string.noLowInventoryAlerts);
            noAlerts.setPadding(12, 12, 12, 12);
            lowInventoryAlerts.addView(noAlerts);
        } else {
            while (cursor.moveToNext()) {
                String name = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.col_item_name));
                int qty = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.col_quantity));
                int reorder = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.col_reorder_level));

                TextView alertTile = new TextView(requireContext());
                alertTile.setText(name + " is low (Qty: " + qty + " | Reorder @: " + reorder + ")");
                alertTile.setPadding(24, 24, 24, 24);
                alertTile.setBackgroundColor(0xFFEEEEEE);

                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT);
                params.setMargins(0, 0, 0, 16);
                alertTile.setLayoutParams(params);

                lowInventoryAlerts.addView(alertTile);
            }
        }
        cursor.close();
    }
}