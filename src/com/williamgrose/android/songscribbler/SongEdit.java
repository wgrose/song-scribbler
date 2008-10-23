/*
 * Copyright (C) 2008 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.williamgrose.android.songscribbler;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;

public class SongEdit extends Activity {

    private EditText mTitleText;
    private EditText mBodyText;
    private int scrollspeed;
    private Long mRowId;
    private SongScribblerDbAdapter mDbHelper;
    
    
    private static final int ACTIVITY_VIEW=2;
    private static final int SAVE_ID = Menu.FIRST;
    private static final int VIEW_ID = Menu.FIRST+1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mDbHelper = new SongScribblerDbAdapter(this);
        mDbHelper.open();
        setContentView(R.layout.song_edit);


        mTitleText = (EditText) findViewById(R.id.title);
        mBodyText = (EditText) findViewById(R.id.body);
        
        mRowId = savedInstanceState != null ? savedInstanceState.getLong(SongScribblerDbAdapter.KEY_ROWID)
                                                                        : null;
        if (mRowId == null) {
                Bundle extras = getIntent().getExtras();
                mRowId = extras != null ? extras.getLong(SongScribblerDbAdapter.KEY_ROWID)
                                                                : null;
        }

        populateFields();

    }

    private void populateFields() {
        if (mRowId != null) {
            Cursor song = mDbHelper.fetchSong(mRowId);
            startManagingCursor(song);
            mTitleText.setText(song.getString(
                    song.getColumnIndexOrThrow(SongScribblerDbAdapter.KEY_TITLE)));
            mBodyText.setText(song.getString(
                    song.getColumnIndexOrThrow(SongScribblerDbAdapter.KEY_BODY)));
            scrollspeed = song.getInt(
                    song.getColumnIndexOrThrow(SongScribblerDbAdapter.KEY_SCROLLSPEED));
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putLong(SongScribblerDbAdapter.KEY_ROWID, mRowId);
    }

    @Override
    protected void onPause() {
        super.onPause();
        saveState();
    }

    @Override
    protected void onResume() {
        super.onResume();
        populateFields();
    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        menu.add(0, SAVE_ID, 0, R.string.menu_save);
        menu.add(0, VIEW_ID, 0,  R.string.menu_view);
        return true;
    }
    
    @Override
    public boolean onMenuItemSelected(int featureId, MenuItem item) {
        switch(item.getItemId()) {
        case SAVE_ID:
            setResult(RESULT_OK);
            finish();
            return true;
        case VIEW_ID:
            viewSong();
            return true;
        }
        return super.onMenuItemSelected(featureId, item);
    }
    
    private void viewSong() {
    	saveState();
        Intent i = new Intent(this, SongView.class);
        i.putExtra(SongScribblerDbAdapter.KEY_ROWID, mRowId);
        startActivityForResult(i, ACTIVITY_VIEW);
    }

    private void saveState() {
        String title = mTitleText.getText().toString();
        String body = mBodyText.getText().toString();

        if (mRowId == null) {
            long id = mDbHelper.createSong(title, body, SongScribblerDbAdapter.DEFAULT_SCROLLSPEED);
            if (id > 0) {
                mRowId = id;
            }
        } else {
            mDbHelper.updateSong(mRowId, title, body, scrollspeed);
        }
    }

}
