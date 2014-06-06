package com.Branham.featherweightcamera;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.NavUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;

import com.facebook.HttpMethod;
import com.facebook.Request;
import com.facebook.Response;
import com.facebook.Session;
import com.facebook.SessionState;
import com.facebook.UiLifecycleHelper;
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
 * 	http://stackoverflow.com/questions/18257451/how-to-fix-noclassdeffounderror-com-facebook-android-facebook
 * 	http://stackoverflow.com/questions/19976026/parsing-json-data-in-java-from-facebook-graph-response
 * 
 */

public class SelectActivity extends Activity{

		private UiLifecycleHelper uiHelper;
		private Spinner albums;
		private TextView select;
		private ArrayList<Album> albumList;
		private ArrayList<String> albumNames;
		private ArrayAdapter<String> adapter;
		private File[] files;
		private String ExternalStorageDirectoryPath;
		private String AppStoragePath;
		private ProgressDialog uploading;
		private Session.StatusCallback callback = new Session.StatusCallback() {
			@Override
			public void call(Session session, SessionState state, Exception exception) {
				// TODO Auto-generated method stub
				
			}
		};
	
		
		@Override
		protected void onCreate(Bundle savedInstanceState){
			super.onCreate(savedInstanceState);
			requestWindowFeature(Window.FEATURE_NO_TITLE);
	        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
			setContentView(R.layout.select_activity);
			//getActionBar().setDisplayHomeAsUpEnabled(true);
			uploading = new ProgressDialog(this);
			uploading.setMessage("Uploading...");
			uploading.setProgressStyle(1);
			uploading.setCancelable(false);
			uploading.setCanceledOnTouchOutside(false);
			
			// Get permission to read album titles
			LoginButton authButton = (LoginButton) findViewById(R.id.authButton);
			authButton.setReadPermissions(Arrays.asList("public_profile", "user_photos"));
			
			// uiHelper is for keeping the Login button updated
			uiHelper = new UiLifecycleHelper(this,callback);
			uiHelper.onCreate(savedInstanceState);
			
			/*The Pictures*/
			
			updatePicsView();
			
			/*The Login*/
			// The login button is handled by the facebook api
			// Check out onSessionStateChange()
			
			albums = (Spinner)findViewById(R.id.spinner_album);
			albums.setVisibility(View.GONE);						// Hide spinner while not logged in
			select = (TextView)findViewById(R.id.text_select);
			select.setVisibility(View.GONE);
			
			
			/*Save Locally*/
			
			ImageButton saveLoc = (ImageButton) findViewById(R.id.button_saveLocal);
			saveLoc.setOnClickListener(
					new View.OnClickListener() {
						
						@Override
						public void onClick(View v) {
							moveFiles();
						}
					});
			
			/*The Upload*/
			
			ImageButton upload = (ImageButton) findViewById(R.id.button_upload);
			upload.setOnClickListener(
					new View.OnClickListener() {
						
						@Override
						public void onClick(View v) {
							
							// Make sure we have permission to upload
							if(Session.isPublishPermission("publish_actions")){
								Log.d("Permission", "Permission");
							}else{
								Log.d("Permission", "Getting Permission");
								Session session = Session.getActiveSession();
								session.requestNewPublishPermissions(askPermissions());
							}
							
							// Upload photos
							uploading.setMax(files.length);
							uploading.show();
							uploadExecute();
						}
					}
			);
		}
		
		@Override
		public boolean onKeyDown(int keyCode, KeyEvent event)  {
		    
			switch(keyCode){
				case KeyEvent.KEYCODE_BACK:
					Log.d("Buttons", "Back button pushed");
					NavUtils.navigateUpFromSameTask(this);
					return true;
				/*case KeyEvent.KEYCODE_HOME:
					Log.d("Buttons", "Home button pushed");
					Intent intent = new Intent(Intent.ACTION_MAIN);
					intent.addCategory(Intent.CATEGORY_HOME);
					intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
					startActivity(intent);*/
			}

		    return super.onKeyDown(keyCode, event);
		}
		
		public void uploadExecute(){
			File tempDir = new File(AppStoragePath);
			if(tempDir.listFiles().length > 0){
				File[] tempArray = tempDir.listFiles();
				String firstPath = tempArray[0].getAbsolutePath();
				Bitmap pic = BitmapFactory.decodeFile(firstPath);
				Log.d("Path", firstPath);
				Log.d("Bitmap", pic.toString());
				Bundle params = new Bundle();
				params.putParcelable("source", pic);
				params.putString("message", "");				// For a description WORKS... maybe add a text box for user input
				params.putBoolean("no_story", true);			// Publish to wall?
				Request upload = makeUploadRequest(params, firstPath);
				upload.executeAsync();
			}else{
				uploading.dismiss();
			}
		}
		
		public Request makeUploadRequest(Bundle params, final String delPath){
			return new Request(
					Session.getActiveSession(),
					getUploadPath(),
					params,
					HttpMethod.POST,
					new Request.Callback() {
						
						@Override
						public void onCompleted(Response response) {
							// Log.d("Upload", response.toString());
							Log.d("Upload", "Upload for a file Completed");
							
							// Delete photo from phone
							delAppPic(delPath);
							uploading.incrementProgressBy(1);
							uploadExecute();
						}
					});
		}
		
		public boolean delAppPic(String path){
			if(new File(path).delete()){
				Log.d("Delete", "Success");
				updatePicsView();
				return true;
			}else{
				Log.d("Delete", "Problems");
				return false;
			}
		}
		
		public void updatePicsView(){
			GridView pictureGrid = (GridView) findViewById(R.id.view_gridview);
			ImageAdapter myImageAdapter = new ImageAdapter(this);
			pictureGrid.setAdapter(myImageAdapter);
			
			ExternalStorageDirectoryPath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).getPath();
			AppStoragePath = ExternalStorageDirectoryPath + "/FacebookCam2";
			File targetDir = new File(AppStoragePath);
			files = targetDir.listFiles();
			
			for(int i=0; i<files.length; i++){
				File file = files[i];
				myImageAdapter.add(file.getAbsolutePath());
			}
		}
		
		public void moveFiles(){
			for(int i=0 ; i < files.length; i++){
				File file = files[i];
				String path = Environment.getExternalStorageDirectory().getPath() + "/DCIM" + file.getAbsolutePath().replace(AppStoragePath, "");
				Log.d("move files", "Path: " + path);
				if(file.renameTo(new File(path))){
					Log.d("move files", "This pic should have moved");
				}else{
					Log.d("move files", "This pic failed to move");
				}
			}
			updatePicsView();
		}
		
		public String getUploadPath(){
			String path = "";
			for(int i = 0; i < albumList.size(); i++){
				if(albums.getSelectedItem().toString().equalsIgnoreCase(albumList.get(i).getName())){
					path = ("/" + albumList.get(i).getId() + "/photos").replaceAll(" ", "%20");
				}
			}
			return path;
		}
		
		public Session.NewPermissionsRequest askPermissions(){
			Session.NewPermissionsRequest newPermissionsRequest = new Session.NewPermissionsRequest(this, "publish_actions");
			return newPermissionsRequest;
		}
		
		@Override
		public void onActivityResult(int requestCode, int resultCode, Intent data){
			super.onActivityResult(requestCode, resultCode, data);
			Session.getActiveSession().onActivityResult(this, requestCode, resultCode, data);
		}
		
		private void onSessionStateChange (Session session, SessionState state, Exception exception){
			if (state.isClosed()){
				
			}
			if(state.isOpened()){  
				new Request(session, "/me/albums", null, HttpMethod.GET, new Request.Callback() {
					
					@Override
					public void onCompleted(Response response) {
						try{
							JSONObject json = new JSONObject(response.getRawResponse());
							JSONArray jarray = json.getJSONArray("data");
							albumList = new ArrayList<Album>();
							albumNames = new ArrayList<String>();
							Log.d("JSON", "Starting JSON loop");
							for(int i = 0; i < jarray.length(); i++){
								JSONObject oneAlbum = jarray.getJSONObject(i);
								if(oneAlbum.getBoolean("can_upload")==true){
									String name = oneAlbum.getString("name");
									albumNames.add(name);
									Album newAlbum = new Album(name, oneAlbum.getString("id"));
									albumList.add(newAlbum);
								}
							}
							
							updateSpinner();
						
						}catch(JSONException e){
							Log.d("JSON", e.getMessage());
						}
					}
				}).executeAsync();
				
			}
		}
		
		public void updateSpinner(){
			// Adds Array to arrayAdapter and sets the spinner visible
			adapter = new ArrayAdapter<String>(this,android.R.layout.simple_spinner_item, albumNames);
			albums.setAdapter(adapter);
			albums.setVisibility(View.VISIBLE);
			select.setVisibility(View.VISIBLE);
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
		
		@Override
		public boolean onOptionsItemSelected(MenuItem item){
			switch(item.getItemId()){
			case android.R.id.home:
				NavUtils.navigateUpFromSameTask(this);
				return true;
			}
			return super.onOptionsItemSelected(item);
		}
		
}
