package android.mrunal.com.todoapp;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.util.ArrayList;

/**
 * Created by mrunal.upadhyay on 7/9/17.
 */

public class TodoItemsDatabaseHelper extends SQLiteOpenHelper {

    private static final String TAG = "TodoItemsDatabaseHelper";

    // Database Info
    private static final String DATABASE_NAME = "todoItemsDatabase";
    private static final int DATABASE_VERSION = 1;
    // Table Names
    private static final String TABLE_TODO_ITEMS = "todoItems";
    // Table Columns
    private static final String KEY_TODO_ITEMS_ID = "id";
    private static final String KEY_TODO_ITEMS_TEXT = "text";
    private static TodoItemsDatabaseHelper sInstance;

    /**
     * Constructor should be private to prevent direct instantiation.
     * Make a call to the static method "getInstance()" instead.
     */
    private TodoItemsDatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    public static synchronized TodoItemsDatabaseHelper getInstance(Context context) {
        // Use the application context, which will ensure that you
        // don't accidentally leak an Activity's context.
        // See this article for more information: http://bit.ly/6LRzfx
        if (sInstance == null) {
            sInstance = new TodoItemsDatabaseHelper(context.getApplicationContext());
        }
        return sInstance;
    }

    // Called when the database connection is being configured.
    // Configure database settings for things like foreign key support, write-ahead logging, etc.
    @Override
    public void onConfigure(SQLiteDatabase db) {
        super.onConfigure(db);
        db.setForeignKeyConstraintsEnabled(true);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        String CREATE_TODO_ITEMS_TABLE = "CREATE TABLE " + TABLE_TODO_ITEMS +
                "(" +
                KEY_TODO_ITEMS_ID + " INTEGER PRIMARY KEY," + // Define a primary key
                KEY_TODO_ITEMS_TEXT + " TEXT" +
                ")";

        sqLiteDatabase.execSQL(CREATE_TODO_ITEMS_TABLE);

    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int oldVersion, int newVersion) {
        if (oldVersion != newVersion) {
            // Simplest implementation is to drop all old tables and recreate them
            sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + TABLE_TODO_ITEMS);
            onCreate(sqLiteDatabase);
        }
    }

    // Insert or update an item in the database
    // Since SQLite doesn't support "upsert" we need to fall back on an attempt to UPDATE (in case the
    // user already exists) optionally followed by an INSERT (in case the item does not already exist).
    // Unfortunately, there is a bug with the insertOnConflict method
    // (https://code.google.com/p/android/issues/detail?id=13045) so we need to fall back to the more
    // verbose option of querying for the user's primary key if we did an update.
    public void addOrUpdateItem(String oldText, String newText) {
        // The database connection is cached so it's not expensive to call getWriteableDatabase() multiple times.
        SQLiteDatabase db = getWritableDatabase();

        db.beginTransaction();
        try {
            ContentValues values = new ContentValues();
            values.put(KEY_TODO_ITEMS_TEXT, newText);

            // First try to update the item in case the item already exists in the database
            // This assumes items are unique
            int rows = db.update(TABLE_TODO_ITEMS, values, KEY_TODO_ITEMS_TEXT + "= ?", new String[]{oldText});
            // Check if update succeeded
            if (rows != 1) {
                // item with this text did not already exist, so insert new item
                db.insertOrThrow(TABLE_TODO_ITEMS, null, values);
            }
            db.setTransactionSuccessful();
        } catch (Exception e) {
            Log.d(TAG, "Error while trying to add or update item");
        } finally {
            db.endTransaction();
        }
    }

    public ArrayList<String> getAllItems() {
        ArrayList<String> items = new ArrayList<>();
        String ITEMS_SELECT_QUERY =
                String.format("SELECT * FROM %s",
                        TABLE_TODO_ITEMS);

        // "getReadableDatabase()" and "getWriteableDatabase()" return the same object (except under low
        // disk space scenarios)
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.rawQuery(ITEMS_SELECT_QUERY, null);
        try {
            if (cursor.moveToFirst()) {
                do {
                    items.add(cursor.getString(cursor.getColumnIndex(KEY_TODO_ITEMS_TEXT)));
                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            Log.d(TAG, "Error while trying to get items from database");
        } finally {
            if (cursor != null && !cursor.isClosed()) {
                cursor.close();
            }
        }
        return items;
    }

    public void deleteItem(String text) {
        SQLiteDatabase db = getWritableDatabase();
        db.beginTransaction();
        try {
            db.delete(TABLE_TODO_ITEMS, KEY_TODO_ITEMS_TEXT + "= ?", new String[]{text});
            db.setTransactionSuccessful();
        } catch (Exception e) {
            Log.d(TAG, "Error while trying to delete the item");
        } finally {
            db.endTransaction();
        }
    }
}
