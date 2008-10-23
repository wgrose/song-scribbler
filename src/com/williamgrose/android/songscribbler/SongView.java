package com.williamgrose.android.songscribbler;


import android.app.Activity;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Layout;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;

public class SongView extends Activity {
	class SongThread extends Thread {
		

		/** Time to sleep before invoking next message **/
		private int mUpdateInterval = 1000;

        /** Message handler used by thread to interact with TextView */
        private Handler mHandler;
        
        public Boolean mRun = true;
        
		
        public SongThread(int updateInterval, Handler handler) {
        	mUpdateInterval = updateInterval;
        	mHandler = handler;
        	
        }
        
        @Override
        public void run() {
        	Log.i(this.getClass().getName(), "Running Thread");
        	while(true && mRun){
        		doSleep();
        		//Log.i(this.getClass().getName(), "Invoking Tick");
        		tick();
        	}
        }
        
        public void setRunning(boolean b) {
            mRun = b;
            if(b){
            	stop();
            }else{
            	start();
            }
        }

        
        /** Broadcasts empty message for the handler to receive
         * notifications of the updates **/
        private void tick(){
            Message msg = mHandler.obtainMessage();
            mHandler.sendMessage(msg);
        }
        
        private void doSleep(){
            try {
                sleep(mUpdateInterval);
            } catch (InterruptedException e) {
            }
        }
	}
	
    private TextView mBodyText;
    private Long mRowId;
    private String title;
    private String body;
    private int mScrollspeed;
    private SongScribblerDbAdapter mDbHelper;
    private SongThread thread;
    private int MAX_SPEED = 10;
    
    private static final int START_ID = Menu.FIRST;
    private static final int STOP_ID = Menu.FIRST+1;
    private static final int RESET_ID = Menu.FIRST+2;
    
    
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mDbHelper = new SongScribblerDbAdapter(this);
        mDbHelper.open();
        setContentView(R.layout.song_view);

        mBodyText = (TextView) findViewById(R.id.scroll_body);

        Spinner scrollSpeedSpinner = (Spinner) findViewById(R.id.scroll_speed);
        
        ArrayAdapter<CharSequence> speedListAdapter = new ArrayAdapter<CharSequence>(
                      this, android.R.layout.simple_spinner_dropdown_item);
        
        for (int i = 0; i < MAX_SPEED; i++)
        	speedListAdapter.add(Integer.toString(i));
        
        speedListAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        scrollSpeedSpinner.setAdapter(speedListAdapter);
        scrollSpeedSpinner.setOnItemSelectedListener(scrollSpinnerListener);

        
        mRowId = savedInstanceState != null ? savedInstanceState.getLong(SongScribblerDbAdapter.KEY_ROWID)
                                                                        : null;
        if (mRowId == null) {
                Bundle extras = getIntent().getExtras();
                mRowId = extras != null ? extras.getLong(SongScribblerDbAdapter.KEY_ROWID)
                                                                : null;
        }

        populateFields();

    }
    
    private Spinner.OnItemSelectedListener scrollSpinnerListener 
    	= new Spinner.OnItemSelectedListener() {
    	@SuppressWarnings("unchecked")
        public void onItemSelected(AdapterView parent, View v, int position, long id) {
        	li("Changed Speed: " + parent.getSelectedItem().toString());
        	setSpeed(Integer.parseInt((parent.getSelectedItem().toString())));
        }
    	@SuppressWarnings("unchecked")
        public void onNothingSelected(AdapterView arg0) {}
    };
    
    private void setSpeed(int speed){
    	mScrollspeed = speed;
    	saveState();
    }
    
    private void autoScroll(){
    	Layout layout = mBodyText.getLayout();
    	/*li("getExtendedPaddingBottom: THeight: " + mBodyText.getHeight() + 
    			" LHeight: " + layout.getHeight() + 
    			" Scroll Y:" + mBodyText.getScrollY());*/
    	if(layout.getHeight()-mBodyText.getScrollY()>mBodyText.getHeight()){
    		mBodyText.scrollTo(0, mBodyText.getScrollY()+mScrollspeed);
    	}else{
    		li("Scrolled to End");
    		stopThread();
    	}
    }
    
    private void startScrolling(){
        startThread();
    }
    
    private void stopScrolling(){
    	stopThread();
    }
    
    private void resetScrolling(){
    	stopThread();
    	mBodyText.scrollTo(0, 0);
    }    

    private void populateFields() {
        if (mRowId != null) {
            Cursor song = mDbHelper.fetchSong(mRowId);
            startManagingCursor(song);
            title = song.getString(
                    song.getColumnIndexOrThrow(SongScribblerDbAdapter.KEY_TITLE));
            body = song.getString(
                    song.getColumnIndexOrThrow(SongScribblerDbAdapter.KEY_BODY));
            mScrollspeed = song.getInt(
                    song.getColumnIndexOrThrow(SongScribblerDbAdapter.KEY_SCROLLSPEED));
            
            mBodyText.setText(body);
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putLong(SongScribblerDbAdapter.KEY_ROWID, mRowId);
    }
    
    private void stopThread(){
    	if(thread != null){
    		thread.setRunning(false);
    		//thread.stop();
    		//thread = null;
    	}
    }
    
    private void startThread(){
    	if(thread == null){
	        thread = new SongThread(200, new Handler() {
	            @Override
	            public void handleMessage(Message m) {
	                autoScroll();
	            }
	        });
    	}
        thread.setRunning(true);
    	//if(!thread.isAlive()){
    	//	thread.start();
    	//}
    }

    @Override
    protected void onPause() {
        super.onPause();
        saveState();
        li("Killing thread...");
        stopThread();
    }
    
    @Override
    protected void onResume() {
        super.onResume();
        populateFields();
        startThread();
    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        menu.add(0, START_ID, 0, R.string.menu_scroll_start);
        menu.add(0, STOP_ID, 0,  R.string.menu_scroll_stop);
        menu.add(0, RESET_ID, 0,  R.string.menu_scroll_reset);
        return true;
    }
    
    @Override
    public boolean onMenuItemSelected(int featureId, MenuItem item) {
        switch(item.getItemId()) {
        case START_ID:
        	startScrolling();
        	return true;
        case STOP_ID:
        	stopScrolling();
        	return true;
        case RESET_ID:
        	resetScrolling();
        	return true;
        }
        return super.onMenuItemSelected(featureId, item);
    }
    
    private void saveState() {
    	mDbHelper.updateSong(mRowId, title, body,mScrollspeed);
    }
    
    private void li(String mesg){
		Log.i(this.getClass().getName(), mesg);
    }


}
