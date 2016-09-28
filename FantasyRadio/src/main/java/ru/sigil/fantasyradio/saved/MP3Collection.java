package ru.sigil.fantasyradio.saved;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class MP3Collection {
    private Context context;
    private List<MP3Entity> mp3entityes = new ArrayList<>();
    public SQLiteDatabase database;
    private final Object saveSync = new Object();

    public MP3Collection(Context c) {
        setContext(c);
    }

    public boolean add(MP3Entity mp3entity) {
        getMp3entityes().add(mp3entity);
        this.addToBase(mp3entity);
        return true;
    }

    public void add(int location, MP3Entity mp3entity) {
        getMp3entityes().add(location, mp3entity);
        this.addToBase(mp3entity);
    }

    public MP3Entity get(int location) {
        return getMp3entityes().get(location);
    }

    boolean remove(Object object) {
        getMp3entityes().remove(object);
        removeFromBase((MP3Entity) object);
        return true;
    }

    public int size() {
        return getMp3entityes().size();
    }

    private void removeFromBase(MP3Entity mp3entity) {
        synchronized (saveSync) {
            SQLiteDatabase mDatabase = getContext().openOrCreateDatabase(
                    "MP3Base", 0, null);
            try {
                mDatabase.delete("MP3ENTITYES",
                        "DIRECTORY = '" + mp3entity.getDirectory() + "'", null);
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                mDatabase.close();
            }
        }
    }

    private void addToBase(MP3Entity mp3entity) {
        synchronized (saveSync) {
            SQLiteDatabase mDatabase = getContext().openOrCreateDatabase(
                    "MP3Base", 0, null);
            ContentValues cv = new ContentValues();
            mDatabase
                    .execSQL("CREATE TABLE IF NOT EXISTS MP3ENTITYES (_id INTEGER PRIMARY KEY AUTOINCREMENT,"
                            + "TITLE VARCHAR(300),"
                            + "ARTIST VARCHAR(300),"
                            + "TIME VARCHAR(300)," + "DIRECTORY VARCHAR(300))");
            cv.put("TITLE", mp3entity.getTitle());
            cv.put("ARTIST", mp3entity.getArtist());
            cv.put("TIME", mp3entity.getTime());
            cv.put("DIRECTORY", mp3entity.getDirectory());
            mDatabase.insert("MP3ENTITYES", null, cv);
            mDatabase.close();
        }
    }

    private void Save() {
        synchronized (saveSync) {
            SQLiteDatabase mDatabase = getContext().openOrCreateDatabase(
                    "MP3Base", 0, null);
            ContentValues cv = new ContentValues();
            try {
                mDatabase.execSQL("DROP TABLE IF EXISTS MP3ENTITYES");
            } catch (Exception e) {
                e.printStackTrace();
            }
            mDatabase
                    .execSQL("CREATE TABLE IF NOT EXISTS MP3ENTITYES (_id INTEGER PRIMARY KEY AUTOINCREMENT,"
                            + "TITLE VARCHAR(300),"
                            + "ARTIST VARCHAR(300),"
                            + "TIME VARCHAR(300)," + "DIRECTORY VARCHAR(300))");
            Iterator<MP3Entity> iter = getMp3entityes().iterator();
            //noinspection WhileLoopReplaceableByForEach
            while (iter.hasNext()) {
                MP3Entity Mp3Entity = iter.next();
                cv.put("TITLE", Mp3Entity.getTitle());
                cv.put("ARTIST", Mp3Entity.getArtist());
                cv.put("TIME", Mp3Entity.getTime());
                cv.put("DIRECTORY", Mp3Entity.getDirectory());
                mDatabase.insert("MP3ENTITYES", null, cv);
            }
            mDatabase.close();
        }
    }

    public void Load() {
        SQLiteDatabase mDatabase = getContext().openOrCreateDatabase("MP3Base",
                0, null);
        getMp3entityes().clear();
        // Make the query.
        try {
            Cursor managedCursor = mDatabase.query("MP3ENTITYES", null, null,
                    null, null, null, null);
            for (managedCursor.moveToFirst(); !managedCursor.isAfterLast(); managedCursor
                    .moveToNext()) {
                MP3Entity Mp3Entity = new MP3Entity();
                Mp3Entity.setTitle(managedCursor.getString(managedCursor
                        .getColumnIndex("TITLE")));
                Mp3Entity.setArtist(managedCursor.getString(managedCursor
                        .getColumnIndex("ARTIST")));
                Mp3Entity.setTime(managedCursor.getString(managedCursor
                        .getColumnIndex("TIME")));
                Mp3Entity.setDirectory(managedCursor.getString(managedCursor
                        .getColumnIndex("DIRECTORY")));
                getMp3entityes().add(Mp3Entity);
            }
            managedCursor.close();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            mDatabase.close();
        }
    }

    public String[] getTitles() {
        String[] res = new String[getMp3entityes().toArray().length];
        MP3Entity mp3Entity;
        for (int i = 0; i < getMp3entityes().toArray().length; i++) {
            mp3Entity = (MP3Entity) getMp3entityes().toArray()[i];
            res[i] = mp3Entity.getTitle();
        }
        return res;
    }

    public String[] getArtists() {
        String[] res = new String[getMp3entityes().toArray().length];
        MP3Entity mp3Entity;
        for (int i = 0; i < getMp3entityes().toArray().length; i++) {
            mp3Entity = (MP3Entity) getMp3entityes().toArray()[i];
            res[i] = mp3Entity.getArtist();
        }
        return res;
    }

    public String[] getTimes() {
        String[] res = new String[getMp3entityes().toArray().length];
        MP3Entity mp3Entity;
        for (int i = 0; i < getMp3entityes().toArray().length; i++) {
            mp3Entity = (MP3Entity) getMp3entityes().toArray()[i];
            res[i] = mp3Entity.getTime();
        }
        return res;
    }

    public String[] getDirectories() {
        String[] res = new String[getMp3entityes().toArray().length];
        MP3Entity mp3Entity;
        for (int i = 0; i < getMp3entityes().toArray().length; i++) {
            mp3Entity = (MP3Entity) getMp3entityes().toArray()[i];
            res[i] = mp3Entity.getDirectory();
        }
        return res;
    }

    void setContext(Context context) {
        this.context = context;
    }

    Context getContext() {
        return context;
    }

    List<MP3Entity> getMp3entityes() {
        return mp3entityes;
    }

    public void removeEntityByDirectory(String directory) {
        try {
            Iterator<MP3Entity> iter = mp3entityes.iterator();
            MP3Entity del = new MP3Entity();
            while (iter.hasNext()) {
                MP3Entity e = iter.next();
                if (e.getDirectory().equals(directory))
                    del = e;
            }
            remove(del);
        } catch (NullPointerException e) {
            e.printStackTrace();
        }
    }
}
