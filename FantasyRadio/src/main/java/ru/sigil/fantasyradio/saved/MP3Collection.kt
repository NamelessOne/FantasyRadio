package ru.sigil.fantasyradio.saved

import android.content.Context
import ru.sigil.bassplayerlib.ITracksCollection
import android.database.sqlite.SQLiteDatabase
import javax.inject.Singleton
import ru.sigil.bassplayerlib.ITrack
import android.content.ContentValues
import android.database.Cursor
import ru.sigil.fantasyradio.saved.DbConstants.ARTIST
import ru.sigil.fantasyradio.saved.DbConstants.DIRECTORY
import ru.sigil.fantasyradio.saved.DbConstants.TIME
import ru.sigil.fantasyradio.saved.DbConstants.TITLE
import javax.inject.Inject

/**
 * Created by namelessone
 * on 02.12.18.
 */
@Singleton
class MP3Collection @Inject constructor (context: Context): ITracksCollection {
    private var mDatabase: SQLiteDatabase? = null
    private val saveSync = Any()

    init {
        mDatabase = context.openOrCreateDatabase("MP3Base", 0, null)
        mDatabase?.execSQL("CREATE TABLE IF NOT EXISTS MP3ENTITYES (_id INTEGER PRIMARY KEY AUTOINCREMENT,"
                        + TITLE + " VARCHAR(300),"
                        + ARTIST + " VARCHAR(300),"
                        + TIME + " VARCHAR(300)," + "DIRECTORY VARCHAR(300))")
    }

    override fun remove(mp3entity: ITrack) {
        synchronized(saveSync) {
            try {
                mDatabase?.delete("MP3ENTITYES",
                        DIRECTORY + " = '" + mp3entity.directory + "'", null)
            } catch (e: Exception) {
                e.printStackTrace()
            }

        }
    }

    override fun add(mp3entity: ITrack) {
        synchronized(saveSync) {
            val cv = ContentValues()
            cv.put(TITLE, mp3entity.title)
            cv.put(ARTIST, mp3entity.artist)
            cv.put(TIME, mp3entity.time)
            cv.put(DIRECTORY, mp3entity.directory)
            mDatabase?.insert("MP3ENTITYES", null, cv)
        }
    }

    fun getCursor(): Cursor? {
        // Make the query.
        try {
            return mDatabase?.query("MP3ENTITYES", null, null, null, null, null, "_id DESC")
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return null
    }

    override fun getNext(entity: ITrack?): MP3Entity? {
        // Make the query.
        if (entity == null)
            return null
        synchronized(saveSync) {
            try {
                val managedCursor = mDatabase?.query("MP3ENTITYES", null, "_id < (SELECT _id FROM MP3ENTITYES WHERE "
                        + DIRECTORY + " = '" + entity.directory + "')", null, null, null, "_id DESC")
                if (managedCursor != null) {
                    if (managedCursor.moveToFirst()) {
                        return MP3Entity(managedCursor.getString(
                                managedCursor.getColumnIndex(ARTIST)),
                                managedCursor.getString(managedCursor.getColumnIndex(TITLE)),
                                managedCursor.getString(managedCursor.getColumnIndex(DIRECTORY)),
                                managedCursor.getString(managedCursor.getColumnIndex(TIME)))
                    }
                }
                managedCursor!!.close()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        return null
    }
}