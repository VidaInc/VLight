package net.vidainc.vidahome.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import static net.vidainc.vidahome.database.BeaconContract.RoomEntry;
import static net.vidainc.vidahome.database.BeaconContract.BeaconEntry;


/**
 * Created by Aaron on 24/03/2015.
 */
public class BeaconDbHelper extends SQLiteOpenHelper {

    public static final String DATABASE_NAME = "beacon_data.db";
    // If you change the database schema, you must increment the database version.
    private static final int DATABASE_VERSION = 1;

    public BeaconDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onConfigure(SQLiteDatabase db) {
        super.onConfigure(db);
        db.setForeignKeyConstraintsEnabled(true);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        final String SQL_CREATE_ROOM_TABLE = "CREATE TABLE " + RoomEntry.TABLE_NAME + " (" +
                RoomEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +

                RoomEntry.COLUMN_ROOM_NAME + " TEXT, " +
                RoomEntry.COLUMN_ROOM_X + " INTEGER NOT NULL, " +
                RoomEntry.COLUMN_ROOM_Y + " INTEGER NOT NULL, " +
                RoomEntry.COLUMN_ROOM_DRAWABLE_ID + " INTEGER, " +

                // To assure the application has just one room entry per name,
                // it's created a UNIQUE constraint with REPLACE strategy
                "UNIQUE (" + RoomEntry.COLUMN_ROOM_NAME + ") ON CONFLICT REPLACE);";

        final String SQL_CREATE_BEACON_TABLE = "CREATE TABLE " + BeaconEntry.TABLE_NAME +
                " (" + BeaconEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +

                BeaconEntry.COLUMN_MAC_ADDRESS + " TEXT NOT NULL, " +
                BeaconEntry.COLUMN_BLUETOOTH_NAME + " TEXT," +
                BeaconEntry.COLUMN_LAST_KNOWN_DISTANCE + " REAL," +
                BeaconEntry.COLUMN_LAST_KNOWN_RSSI + " INTEGER," +
                BeaconEntry.COLUMN_SERVICE_UUID + " INTEGER," +
                BeaconEntry.COLUMN_PROXIMITY_UUID + " INTEGER," +
                BeaconEntry.COLUMN_MAJOR_UUID + " INTEGER," +
                BeaconEntry.COLUMN_MINOR_UUID + " INTEGER," +
                BeaconEntry.COLUMN_ROOM_KEY + " TEXT," +

                " FOREIGN KEY (" + BeaconEntry.COLUMN_ROOM_KEY + ") REFERENCES " +
                RoomEntry.TABLE_NAME + " (" + RoomEntry.COLUMN_ROOM_NAME + ") " +
                RoomEntry.TABLE_NAME + " (" + RoomEntry.COLUMN_ROOM_NAME + ") " +
                "ON UPDATE CASCADE ON DELETE CASCADE, " +
                "UNIQUE (" + BeaconEntry.COLUMN_MAC_ADDRESS + ") ON CONFLICT IGNORE);";


        sqLiteDatabase.execSQL(SQL_CREATE_ROOM_TABLE);
        sqLiteDatabase.execSQL(SQL_CREATE_BEACON_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int oldVersion, int newVersion) {
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + RoomEntry.TABLE_NAME);
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + BeaconEntry.TABLE_NAME);
        onCreate(sqLiteDatabase);
    }
}
