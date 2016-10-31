package ru.sigil.fantasyradio.saved;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

public class MP3Collection {
    public static final String ARTIST = "ARTIST";
    public static final String TITLE = "TITLE";
    public static final String TIME = "TIME";
    public static final String DIRECTORY = "DIRECTORY";

    private Context context;
    private final Object saveSync = new Object();
    private SQLiteDatabase mDatabase;

    public MP3Collection(Context c) {
        context = c;
        mDatabase = context.openOrCreateDatabase(
                "MP3Base", 0, null);
    }

    public void removeFromBase(MP3Entity mp3entity) {
        synchronized (saveSync) {
            try {
                mDatabase.delete("MP3ENTITYES",
                        DIRECTORY + " = '" + mp3entity.getDirectory() + "'", null);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void addToBase(MP3Entity mp3entity) {
        synchronized (saveSync) {
            ContentValues cv = new ContentValues();
            mDatabase
                    .execSQL("CREATE TABLE IF NOT EXISTS MP3ENTITYES (_id INTEGER PRIMARY KEY AUTOINCREMENT,"
                            + TITLE + " VARCHAR(300),"
                            + ARTIST + " VARCHAR(300),"
                            + TIME + " VARCHAR(300)," + "DIRECTORY VARCHAR(300))");
            cv.put(TITLE, mp3entity.getTitle());
            cv.put(ARTIST, mp3entity.getArtist());
            cv.put(TIME, mp3entity.getTime());
            cv.put(DIRECTORY, mp3entity.getDirectory());
            mDatabase.insert("MP3ENTITYES", null, cv);
        }
    }

    public Cursor getCursor() {
        // Make the query.
        try {
            return mDatabase.query("MP3ENTITYES", null, null,
                    null, null, null, "_id DESC");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public MP3Entity getNext(MP3Entity entity) {
        // Make the query.
        if (entity == null)
            return null;
        synchronized (saveSync) {
            try {
                Cursor managedCursor = mDatabase.query("MP3ENTITYES", null, "_id < (SELECT _id FROM MP3ENTITYES WHERE "
                        + DIRECTORY + " = '" + entity.getDirectory() + "')", null, null, null, "_id DESC");
                if (managedCursor != null) {
                    if (managedCursor.moveToFirst()) {
                        MP3Entity mp3Entity = new MP3Entity();
                        mp3Entity.setTitle(managedCursor.getString(managedCursor
                                .getColumnIndex(TITLE)));
                        mp3Entity.setArtist(managedCursor.getString(managedCursor
                                .getColumnIndex(ARTIST)));
                        mp3Entity.setTime(managedCursor.getString(managedCursor
                                .getColumnIndex(TIME)));
                        mp3Entity.setDirectory(managedCursor.getString(managedCursor
                                .getColumnIndex(DIRECTORY)));
                        return mp3Entity;
                    }
                }
                managedCursor.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return null;
    }
}
