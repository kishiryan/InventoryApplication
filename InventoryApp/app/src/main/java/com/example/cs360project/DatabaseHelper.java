package com.example.cs360project;

import android.content.ContentValues;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.content.Context;
import android.database.Cursor;
import android.Manifest;
import android.content.pm.PackageManager;
import android.telephony.SmsManager;
import androidx.core.content.ContextCompat;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String database_name = "inventory_db";
    private static final int database_version = 2;

    // user info
    public static final String table_users = "users";
    public static final String col_username = "username";
    public static final String col_password = "password";

    // inventory info
    public static final String table_inventory = "inventory";
    public static final String col_id = "id";
    public static final String col_item_name = "item_name";
    public static final String col_quantity = "quantity";
    public static final String col_reorder_level = "reorder_level";

    // constructor to initialize the database
    public DatabaseHelper(Context context) {
        super(context, database_name, null, database_version);
    }

    // create tables
    @Override
    public void onCreate(SQLiteDatabase db) {
        // users
        db.execSQL("CREATE TABLE " + table_users + " (" +
                col_username + " TEXT PRIMARY KEY, " +
                col_password + " TEXT NOT NULL)");

        // inventory
        db.execSQL("CREATE TABLE " + table_inventory + " (" +
                col_id + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                col_item_name + " TEXT NOT NULL UNIQUE, " + // added UNIQUE to prevent duplicate items
                col_quantity + " INTEGER NOT NULL, " +
                col_reorder_level + " INTEGER NOT NULL DEFAULT 0)");
    }

    // on upgrade required method for Android
    @Override
    public void onUpgrade(SQLiteDatabase db, int preVersion, int newVersion) {
        if (preVersion < 2) {
            db.execSQL("ALTER TABLE " + table_inventory +
                    " ADD COLUMN " + col_reorder_level + " INTEGER NOT NULL DEFAULT 0");
        }
        if (preVersion < 3) {
            db.execSQL("DROP TABLE IF EXISTS " + table_inventory);
            db.execSQL("CREATE TABLE " + table_inventory + " (" +
                    col_id + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    col_item_name + " TEXT NOT NULL UNIQUE, " +
                    col_quantity + " INTEGER NOT NULL, " +
                    col_reorder_level + " INTEGER NOT NULL DEFAULT 0)");
        }
    }

    // register user method
    public boolean registerUser(String username, String password) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(col_username, username);
        values.put(col_password, password);
        long result = db.insert(table_users, null, values);
        return result != -1;
    }

    // login user method
    public boolean loginUser(String username, String password) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(table_users, null,
                col_username + "=? AND " + col_password + "=?",
                new String[]{username, password}, null, null, null);
        boolean exists = cursor.getCount() > 0;
        cursor.close();
        return exists;
    }

    // add item with reorder level
    public boolean addItem(Context context, String name, int quantity, int reorderLevel) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(col_item_name, name);
        values.put(col_quantity, quantity);
        values.put(col_reorder_level, reorderLevel);
        long result = db.insertWithOnConflict(table_inventory, null, values, SQLiteDatabase.CONFLICT_IGNORE);

        // auto send sms alert if quantity is at or below reorder level on add
        if (result != -1 && quantity <= reorderLevel) {
            String phoneNumber = "3333";
            String message = name + " is low! Qty: " + quantity + " (Reorder @: " + reorderLevel + ")";
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.SEND_SMS)
                    == PackageManager.PERMISSION_GRANTED) {
                SmsManager.getDefault().sendTextMessage(phoneNumber, null, message, null, null);
            }
        }

        return result != -1;
    }

    // delete item
    public boolean deleteItem(int id) {
        SQLiteDatabase db = this.getWritableDatabase();
        int result = db.delete(table_inventory, col_id + "=?",
                new String[]{String.valueOf(id)});
        return result > 0;
    }

    // update item with reorder level
    public boolean updateItem(Context context, int id, String name, int quantity, int reorderLevel) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(col_item_name, name);
        values.put(col_quantity, quantity);
        values.put(col_reorder_level, reorderLevel);
        int rows = db.update(table_inventory, values, col_id + "=?",
                new String[]{String.valueOf(id)});

        // auto send sms alert if quantity is at or below reorder level
        if (rows > 0 && quantity <= reorderLevel) {
            String phoneNumber = "3333";
            String message = name + " is low! Qty: " + quantity + " (Reorder @: " + reorderLevel + ")";
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.SEND_SMS)
                    == PackageManager.PERMISSION_GRANTED) {
                SmsManager.getDefault().sendTextMessage(phoneNumber, null, message, null, null);
            }
        }

        return rows > 0;
    }

    // get all items
    public Cursor getAllItems() {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery("SELECT * FROM " + table_inventory, null);
    }

    // get items at or below reorder level
    public Cursor getLowInventoryItems() {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery("SELECT * FROM " + table_inventory +
                " WHERE " + col_quantity + " <= " + col_reorder_level, null);
    }

    // get item id by name
    public int getItemIdByName(String name) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(table_inventory, new String[]{col_id},
                col_item_name + "=?", new String[]{name},
                null, null, null);
        if (cursor.moveToFirst()) {
            int id = cursor.getInt(cursor.getColumnIndexOrThrow(col_id));
            cursor.close();
            return id;
        }
        cursor.close();
        return -1;
    }
}
