/*
 * Copyright (C) 2008 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License")savedInstanceState;
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

import android.app.ListActivity;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;

public class SongScribbler extends ListActivity {
    private static final int ACTIVITY_CREATE=0;
    private static final int ACTIVITY_EDIT=1;
    

    private static final int INSERT_ID = Menu.FIRST;
    private static final int DELETE_ID = Menu.FIRST + 1;

    private SongScribblerDbAdapter mDbHelper;

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.songs_list);
        mDbHelper = new SongScribblerDbAdapter(this);
        mDbHelper.open();
        fillData();
    }

    private void fillData() {
        Cursor songsCursor = mDbHelper.fetchAllSongs();
        startManagingCursor(songsCursor);

        // Create an array to specify the fields we want to display in the list (only TITLE)
        String[] from = new String[]{SongScribblerDbAdapter.KEY_TITLE};

        // and an array of the fields we want to bind those fields to (in this case just text1)
        int[] to = new int[]{R.id.textRow};

        // Now create a simple cursor adapter and set it to display
        SimpleCursorAdapter songs =
                    new SimpleCursorAdapter(this, R.layout.songs_row, songsCursor, from, to);
        setListAdapter(songs);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        menu.add(0, INSERT_ID, 0, R.string.menu_insert);
        menu.add(0, DELETE_ID, 0,  R.string.menu_delete);
        return true;
    }

    @Override
    public boolean onMenuItemSelected(int featureId, MenuItem item) {
        switch(item.getItemId()) {
        case INSERT_ID:
            createSong();
            return true;
        case DELETE_ID:
            mDbHelper.deleteSong(getListView().getSelectedItemId());
            fillData();
            return true;
        }

        return super.onMenuItemSelected(featureId, item);
    }

    private void createSong() {
        Intent i = new Intent(this, SongEdit.class);
        startActivityForResult(i, ACTIVITY_CREATE);
    }   
    

    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);
        Intent i = new Intent(this, SongEdit.class);
        i.putExtra(SongScribblerDbAdapter.KEY_ROWID, id);
        startActivityForResult(i, ACTIVITY_EDIT);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode,
                                    Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);
        fillData();
    }
}
