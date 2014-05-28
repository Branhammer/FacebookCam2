package com.Branham.facebookcam2;

import java.io.File;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.GridView;
import android.widget.Spinner;
import android.widget.Toast;

import com.facebook.Request;
import com.facebook.Response;
import com.facebook.Session;
import com.facebook.SessionState;
import com.facebook.UiLifecycleHelper;
import com.facebook.model.GraphUser;
import com.facebook.widget.LoginButton;

/**
 * 
 * @author stevenbranham
 *
 * References used: 
 * 	http://android-er.blogspot.com/2012/07/gridview-loading-photos-from-sd-card.html
 * 	http://stackoverflow.com/questions/20714058/file-exists-and-is-directory-but-listfiles-returns-null
 * 	http://stackoverflow.com/questions/1016896/how-to-get-screen-dimensions	
 * 	http://stackoverflow.com/questions/6814575/image-adapter-adding-imageviews-dynamically-to-gridview-without-cursor
 * 
 */


public class SelectActivity extends Activity{

		private Session.StatusCallback callback = new Session.StatusCallback() {
			
			@Override
			public void call(Session session, SessionState state, Exception exception) {
				// TODO Auto-generated method stub
				
			}
		};
		private UiLifecycleHelper uiHelper;
		private Spinner albums;
	
	
		@Override
		protected void onCreate(Bundle savedInstanceState){
			super.onCreate(savedInstanceState);
			setContentView(R.layout.select_activity);
			LoginButton authButton = (LoginButton) findViewById(R.id.authButton);
			
			
			uiHelper = new UiLifecycleHelper(this,callback);
			uiHelper.onCreate(savedInstanceState);
			
			/*The Pictures*/
			
			GridView pictureGrid = (GridView) findViewById(R.id.view_gridview);
			ImageAdapter myImageAdapter = new ImageAdapter(this);
			pictureGrid.setAdapter(myImageAdapter);
		
			String ExternalStorageDirectoryPath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).getPath();
			String path = ExternalStorageDirectoryPath + "/FacebookCam2";
			
			/*Log.d("This is the path", path);
			Toast.makeText(getApplicationContext(), path, Toast.LENGTH_LONG).show();*/
			
			File targetDir = new File(path);
			
			/*Log.d("Files",Environment.getExternalStorageState());
			Log.d("Files",targetDir.exists()+"");
			Log.d("Files",targetDir.isDirectory()+"");
			Log.d("Files",targetDir.listFiles()+"");*/
			
			File[] files = targetDir.listFiles();
			for(int i=0; i<files.length; i++){
				File file = files[i];
				Log.d("SelectActivity", file.toString());
				myImageAdapter.add(file.getAbsolutePath());
			}
			
			/*The Login*/
			
			albums = (Spinner)findViewById(R.id.spinner_album);
			albums.setVisibility(View.GONE);
			
		}
		
		@Override
		public void onActivityResult(int requestCode, int resultCode, Intent data){
			super.onActivityResult(requestCode, resultCode, data);
			Session.getActiveSession().onActivityResult(this, requestCode, resultCode, data);
		}
		
		private void onSessionStateChange (Session session, SessionState state, Exception exception){
			if (state.isClosed()){
				Log.d("Session", "Logged out...");
			}
			if(state.isOpened()){
				Log.d("Session", "Logged in...");
				albums.setVisibility(View.VISIBLE);
			}
		}
		
		@Override
		public void onResume(){
			super.onResume();
			
			Session session = Session.getActiveSession();
			if(session != null && (session.isOpened() || session.isClosed())){
				onSessionStateChange(session,session.getState(), null);
			}
			
			uiHelper.onResume();
		}
		
		@Override
		public void onPause(){
			super.onPause();
			uiHelper.onPause();
		}
		
		@Override
		public void onDestroy(){
			super.onDestroy();
			uiHelper.onDestroy();
		}
		
		@Override
		public void onSaveInstanceState(Bundle outState){
			super.onSaveInstanceState(outState);
			uiHelper.onSaveInstanceState(outState);
		}
		
	
}
