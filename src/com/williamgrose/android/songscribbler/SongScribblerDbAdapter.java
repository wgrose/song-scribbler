/*
 * Copyright (C) 2008 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package com.williamgrose.android.songscribbler;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

/**
 * Simple notes database access helper class. Defines the basic CRUD operations
 * for the notepad example, and gives the ability to list all notes as well as
 * retrieve or modify a specific note.
 *
 * This has been improved from the first version of this tutorial through the
 * addition of better error handling and also using returning a Cursor instead
 * of using a collection of inner classes (which is less scalable and not
 * recommended).
 */
public class SongScribblerDbAdapter {

    public static final String KEY_TITLE = "title";
    public static final String KEY_BODY = "body";
    public static final String KEY_SCROLLSPEED = "scrollspeed";
    public static final String KEY_ROWID = "_id";
    public static final int DEFAULT_SCROLLSPEED = 2;
    

    private static final String TAG = "SongScribblerDbAdapter";
    private DatabaseHelper mDbHelper;
    private SQLiteDatabase mDb;

    /**
     * Database creation sql statement
     */
    private static final String DATABASE_CREATE =
            "create table songs (_id integer primary key autoincrement, "
                    + KEY_TITLE+" text not null, "+KEY_BODY+" text not null,"
                    + KEY_SCROLLSPEED+" integer not null);";

    private static final String DATABASE_NAME = "song_scribbler";
    private static final String DATABASE_TABLE = "songs";
    private static final int DATABASE_VERSION = 2;

    private final Context mCtx;

    private static class DatabaseHelper extends SQLiteOpenHelper {

        DatabaseHelper(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {

            db.execSQL(DATABASE_CREATE);
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            Log.w(TAG, "Upgrading database from version " + oldVersion + " to "
                    + newVersion + ", which will destroy all old data");
            db.execSQL("DROP TABLE IF EXISTS " + DATABASE_TABLE);
            onCreate(db);
        }
    }

    /**
     * Constructor - takes the context to allow the database to be
     * opened/created
     *
     * @param ctx the Context within which to work
     */
    public SongScribblerDbAdapter(Context ctx) {
        this.mCtx = ctx;
    }

    /**
     * Open the songs database. If it cannot be opened, try to create a new
     * instance of the database. If it cannot be created, throw an exception to
     * signal the failure
     *
     * @return this (self reference, allowing this to be chained in an
     *         initialization call)
     * @throws SQLException if the database could be neither opened or created
     */
    public SongScribblerDbAdapter open() throws SQLException {
        mDbHelper = new DatabaseHelper(mCtx);
        mDb = mDbHelper.getWritableDatabase();
        return this;
    }

    public void close() {
        mDbHelper.close();
    }


    /**
     * Create a new song using the title and body provided. If the song is
     * successfully created return the new rowId for that song, otherwise return
     * a -1 to indicate failure.
     *
     * @param title the title of the song
     * @param body the body of the song
     * @return rowId or -1 if failed
     */
    public long createSong(String title, String body, int scrollspeed  ) {
        ContentValues initialValues = new ContentValues();
        initialValues.put(KEY_TITLE, title);
        initialValues.put(KEY_BODY, body);
        initialValues.put(KEY_SCROLLSPEED, scrollspeed);

        return mDb.insert(DATABASE_TABLE, null, initialValues);
    }

    /**
     * Delete the song with the given rowId
     *
     * @param rowId id of song to delete
     * @return true if deleted, false otherwise
     */
    public boolean deleteSong(long rowId) {

        return mDb.delete(DATABASE_TABLE, KEY_ROWID + "=" + rowId, null) > 0;
    }

    /**
     * Return a Cursor over the list of all songs in the database
     *
     * @return Cursor over all songs
     */
    public Cursor fetchAllSongs() {

        return mDb.query(DATABASE_TABLE, new String[] {KEY_ROWID, KEY_TITLE,
                KEY_BODY, KEY_SCROLLSPEED}, null, null, null, null, null);
    }

    /**
     * Return a Cursor positioned at the song that matches the given rowId
     *
     * @param rowId id of song to retrieve
     * @return Cursor positioned to matching song, if found
     * @throws SQLException if song could not be found/retrieved
     */
    public Cursor fetchSong(long rowId) throws SQLException {

        Cursor mCursor =

                mDb.query(true, DATABASE_TABLE, new String[] {KEY_ROWID,
                        KEY_TITLE, KEY_BODY,KEY_SCROLLSPEED}, KEY_ROWID + "=" + rowId, null,
                        null, null, null, null);
        if (mCursor != null) {
            mCursor.moveToFirst();
        }
        return mCursor;

    }

    /**
     * Update the song using the details provided. The song to be updated is
     * specified using the rowId, and it is altered to use the title and body
     * values passed in
     *
     * @param rowId id of song to update
     * @param title value to set song title to
     * @param body value to set song body to
     * @return true if the song was successfully updated, false otherwise
     */
    public boolean updateSong(long rowId, String title, String body, int scrollspeed) {
        ContentValues args = new ContentValues();
        args.put(KEY_TITLE, title);
        args.put(KEY_BODY, body);
        args.put(KEY_SCROLLSPEED, scrollspeed);

        return mDb.update(DATABASE_TABLE, args, KEY_ROWID + "=" + rowId, null) > 0;
    }
}
