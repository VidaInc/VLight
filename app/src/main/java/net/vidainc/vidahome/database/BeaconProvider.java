package net.vidainc.vidahome.database;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.graphics.Point;
import android.net.Uri;

import net.vidainc.vidahome.models.BeaconData;
import net.vidainc.vidahome.models.Room;

import org.altbeacon.beacon.Identifier;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Aaron on 24/03/2015.
 */
public class BeaconProvider extends ContentProvider {

    private static final int ROOMS = 100;
    private static final int BEACONS = 200;
    private static final int BEACONS_WITH_ROOM = 201;
    // The URI Matcher used by this content provider.
    private static final UriMatcher sUriMatcher = buildUriMatcher();
    private static final SQLiteQueryBuilder sBeaconsByRoomQueryBuilder;
    private static final String sRoomNameSelection =
            BeaconContract.RoomEntry.TABLE_NAME +
                    "." + BeaconContract.RoomEntry.COLUMN_ROOM_NAME + " = ? ";
    private static final String sBeaconNameSelection =
            BeaconContract.BeaconEntry.TABLE_NAME +
                    "." + BeaconContract.BeaconEntry.COLUMN_MAC_ADDRESS + " = ? ";
    public static final String[] ROOM_PROJECTION = {
            BeaconContract.RoomEntry.COLUMN_ROOM_NAME,
            BeaconContract.RoomEntry.COLUMN_ROOM_X,
            BeaconContract.RoomEntry.COLUMN_ROOM_Y,
            BeaconContract.RoomEntry.COLUMN_ROOM_DRAWABLE_ID
    };
    public static final String[] BEACON_PROJECTION = {
            BeaconContract.BeaconEntry.COLUMN_MAC_ADDRESS,
            BeaconContract.BeaconEntry.COLUMN_BLUETOOTH_NAME,
            BeaconContract.BeaconEntry.COLUMN_LAST_KNOWN_DISTANCE,
            BeaconContract.BeaconEntry.COLUMN_SERVICE_UUID,
            BeaconContract.BeaconEntry.COLUMN_LAST_KNOWN_RSSI,
            BeaconContract.BeaconEntry.COLUMN_PROXIMITY_UUID,
            BeaconContract.BeaconEntry.COLUMN_MAJOR_UUID,
            BeaconContract.BeaconEntry.COLUMN_MINOR_UUID,
            BeaconContract.BeaconEntry.COLUMN_ROOM_KEY

    };

    static {
        sBeaconsByRoomQueryBuilder = new SQLiteQueryBuilder();
        sBeaconsByRoomQueryBuilder.setTables(
                BeaconContract.BeaconEntry.TABLE_NAME + " INNER JOIN " +
                        BeaconContract.RoomEntry.TABLE_NAME +
                        " ON " + BeaconContract.BeaconEntry.TABLE_NAME +
                        "." + BeaconContract.BeaconEntry.COLUMN_ROOM_KEY +
                        " = " + BeaconContract.RoomEntry.TABLE_NAME +
                        "." + BeaconContract.RoomEntry.COLUMN_ROOM_NAME);
    }

    private BeaconDbHelper mOpenHelper;

    private static UriMatcher buildUriMatcher() {
        // I know what you're thinking.  Why create a UriMatcher when you can use regular
        // expressions instead?  Because you're not crazy, that's why.

        // All paths added to the UriMatcher have a corresponding code to return when a match is
        // found.  The code passed into the constructor represents the code to return for the root
        // URI.  It's common to use NO_MATCH as the code for this case.
        final UriMatcher matcher = new UriMatcher(UriMatcher.NO_MATCH);
        final String authority = BeaconContract.CONTENT_AUTHORITY;

        // For each type of URI you want to add, create a corresponding code.
        matcher.addURI(authority, BeaconContract.PATH_ROOMS, ROOMS);
        matcher.addURI(authority, BeaconContract.PATH_BEACONS, BEACONS);
        matcher.addURI(authority, BeaconContract.PATH_BEACONS + "/*", BEACONS_WITH_ROOM);
        return matcher;
    }

    @Override
    public boolean onCreate() {
        mOpenHelper = new BeaconDbHelper(getContext());
        return true;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        // Here's the switch statement that, given a URI, will determine what kind of request it is,
        // and query the database accordingly.
        Cursor retCursor;
        switch (sUriMatcher.match(uri)) {
            case ROOMS:
                retCursor = mOpenHelper.getReadableDatabase().query(
                        BeaconContract.RoomEntry.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder);
                break;
            case BEACONS:
                retCursor = mOpenHelper.getReadableDatabase().query(
                        BeaconContract.BeaconEntry.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder);
                break;
            case BEACONS_WITH_ROOM:
                retCursor = getBeaconsByRoom(uri, projection, sortOrder);
                break;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        retCursor.setNotificationUri(getContext().getContentResolver(), uri);
        return retCursor;
    }

    @Override
    public String getType(Uri uri) {
        // Use the Uri Matcher to determine what kind of URI this is.
        final int match = sUriMatcher.match(uri);

        switch (match) {
            case ROOMS:
                return BeaconContract.RoomEntry.CONTENT_TYPE;
            case BEACONS:
                return BeaconContract.BeaconEntry.CONTENT_TYPE;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
    }

    @Override
    public synchronized Uri insert(Uri uri, ContentValues values) {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        Uri returnUri;

        switch (match) {
            case ROOMS: {
                long _id = db.insert(BeaconContract.RoomEntry.TABLE_NAME, null, values);
                if (_id > 0)
                    returnUri = BeaconContract.RoomEntry.buildRoomUri(_id);
                else
                    throw new android.database.SQLException("Failed to insert row into " + uri);
                break;
            }
            case BEACONS: {
                long _id = db.insert(BeaconContract.BeaconEntry.TABLE_NAME, null, values);
                if (_id > 0)
                    returnUri = BeaconContract.BeaconEntry.buildBeaconUri(_id);
                else
                    throw new android.database.SQLException("Failed to insert row into " + uri);
                break;
            }
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        getContext().getContentResolver().notifyChange(uri, null);
        return returnUri;
    }

    @Override
    public synchronized int delete(Uri uri, String selection, String[] selectionArgs) {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        int rowsDeleted;
        switch (match) {
            case ROOMS:
                rowsDeleted = db.delete(
                        BeaconContract.RoomEntry.TABLE_NAME, selection, selectionArgs);
                break;
            case BEACONS:
                rowsDeleted = db.delete(
                        BeaconContract.BeaconEntry.TABLE_NAME, selection, selectionArgs);
                break;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        // Because a null deletes all rows
        if (selection == null || rowsDeleted != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return rowsDeleted;
    }

    @Override
    public synchronized int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        int rowsUpgraded;
        switch (match) {
            case ROOMS:
                rowsUpgraded = db.update(
                        BeaconContract.RoomEntry.TABLE_NAME, values, selection, selectionArgs);
                break;
            case BEACONS:
                rowsUpgraded = db.update(
                        BeaconContract.BeaconEntry.TABLE_NAME, values, selection, selectionArgs);
                break;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        if (rowsUpgraded != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return rowsUpgraded;
    }

    private Cursor getBeaconsByRoom(Uri uri, String[] projection, String sortOrder) {
        return sBeaconsByRoomQueryBuilder.query(mOpenHelper.getReadableDatabase(),
                projection,
                sRoomNameSelection,
                new String[]{BeaconContract.BeaconEntry.getRoomNameFromUri(uri)},
                null,
                null,
                sortOrder
        );
    }


    public static synchronized Uri insertRoom(Context context, Room room) {

        ContentValues contentValues = new ContentValues();
        contentValues.put(BeaconContract.RoomEntry.COLUMN_ROOM_NAME, room.getName());
        contentValues.put(BeaconContract.RoomEntry.COLUMN_ROOM_X, room.getLeft());
        contentValues.put(BeaconContract.RoomEntry.COLUMN_ROOM_Y, room.getTop());
        contentValues.put(BeaconContract.RoomEntry.COLUMN_ROOM_DRAWABLE_ID, room.getDrawable());

        try {
            return context.getContentResolver().insert(BeaconContract.RoomEntry.CONTENT_URI, contentValues);
        } catch (SQLException e) {
            context.getContentResolver().update(BeaconContract.RoomEntry.CONTENT_URI,
                    contentValues, BeaconContract.RoomEntry.COLUMN_ROOM_NAME + " =? " +
                            room.getName(), null);
            return BeaconContract.RoomEntry.CONTENT_URI;
        }
    }

    public static synchronized Room getRoom(Context context, String name) {
        Cursor cursor = context.getContentResolver().query(BeaconContract.RoomEntry.CONTENT_URI,
                ROOM_PROJECTION, sRoomNameSelection,
                new String[]{name}, null);
        if (!cursor.moveToFirst()) throw new IllegalArgumentException("Room not found");

        Room room = new Room();
        room.setName(cursor.getString(0));
        room.setCurrent(new Point(cursor.getInt(1), cursor.getInt(2)));
        room.setDrawable(cursor.getInt(3));
        cursor.close();
        return room;
    }

    public static synchronized List<Room> getAllRooms(Context context) {
        ArrayList<Room> rooms = new ArrayList<>();
        Cursor cursor = context.getContentResolver().query(BeaconContract.RoomEntry.CONTENT_URI,
                ROOM_PROJECTION, null,
                null, BeaconContract.RoomEntry.COLUMN_ROOM_NAME + " ASC");
        //if (!cursor.moveToFirst()) throw new IllegalArgumentException("Room not found");

        while (cursor.moveToNext()) {
            Room room = new Room();

            room.setId(cursor.getLong(cursor.getColumnIndex("_id")));
            room.setName(cursor.getString(0));
            room.setCurrent(new Point(cursor.getInt(1), cursor.getInt(2)));
            room.setDrawable(cursor.getInt(3));
            rooms.add(room);
        }
        cursor.close();
        return rooms;
    }

    public static synchronized BeaconData getBeacon(Context context, String macAddress) {
        Cursor cursor = context.getContentResolver().query(BeaconContract.BeaconEntry.CONTENT_URI,
                BEACON_PROJECTION, sBeaconNameSelection, new String[]{macAddress}, null);

        if (!cursor.moveToFirst()) throw new IllegalArgumentException("Beacon not found");

        BeaconData beaconData = new BeaconData();
        beaconData.setBluetoothAddress(cursor.getString(0));
        beaconData.setBluetoothName(cursor.getString(1));
        beaconData.setLastKnownDistance(cursor.getDouble(2));
        beaconData.setServiceUuid(cursor.getInt(3));
        beaconData.setRssi(cursor.getInt(4));
        beaconData.setIdentifier1(Identifier.fromInt(cursor.getInt(5)));
        beaconData.setIdentifier2(Identifier.fromInt(cursor.getInt(6)));
        beaconData.setIdentifier3(Identifier.fromInt(cursor.getInt(7)));
        beaconData.setRoom(getRoom(context, cursor.getString(8)));
        cursor.close();

        return beaconData;
    }

    public static synchronized List<BeaconData> getAllBeacons(Context context) {
        ArrayList<BeaconData> beaconDataList = new ArrayList<>();
        Cursor cursor = context.getContentResolver().query(BeaconContract.BeaconEntry.CONTENT_URI,
                BEACON_PROJECTION, null,
                null, null);
        //if (!cursor.moveToFirst()) throw new IllegalArgumentException("Room not found");

        while (cursor.moveToNext()) {
            BeaconData beaconData = new BeaconData();
            beaconData.setBluetoothAddress(cursor.getString(0));
            beaconData.setBluetoothName(cursor.getString(1));
            beaconData.setLastKnownDistance(cursor.getDouble(2));
            beaconData.setServiceUuid(cursor.getInt(3));
            beaconData.setRssi(cursor.getInt(4));
            beaconData.setIdentifier1(Identifier.fromInt(cursor.getInt(5)));
            beaconData.setIdentifier2(Identifier.fromInt(cursor.getInt(6)));
            beaconData.setIdentifier3(Identifier.fromInt(cursor.getInt(7)));
            beaconData.setRoom(getRoom(context, cursor.getString(8)));
            beaconDataList.add(beaconData);
        }
        cursor.close();
        return beaconDataList;
    }

    public static synchronized List<BeaconData> getAllBeaconsInRoom(Context context, String roomName) {
        ArrayList<BeaconData> beaconDataList = new ArrayList<>();
        Cursor cursor = context.getContentResolver().query(BeaconContract.BeaconEntry
                        .buildBeaconUriWithRoom(roomName),
                BEACON_PROJECTION, null,
                null, null);
        //if (!cursor.moveToFirst()) throw new IllegalArgumentException("Room not found");
        Room room = getRoom(context, roomName);

        while (cursor.moveToNext()) {
            BeaconData beaconData = new BeaconData();
            beaconData.setBluetoothAddress(cursor.getString(0));
            beaconData.setBluetoothName(cursor.getString(1));
            beaconData.setLastKnownDistance(cursor.getDouble(2));
            beaconData.setServiceUuid(cursor.getInt(3));
            beaconData.setRssi(cursor.getInt(4));
            beaconData.setIdentifier1(Identifier.fromInt(cursor.getInt(5)));
            beaconData.setIdentifier2(Identifier.fromInt(cursor.getInt(6)));
            beaconData.setIdentifier3(Identifier.fromInt(cursor.getInt(7)));
            beaconData.setRoom(room);
            beaconDataList.add(beaconData);
        }
        cursor.close();
        return beaconDataList;
    }
}
