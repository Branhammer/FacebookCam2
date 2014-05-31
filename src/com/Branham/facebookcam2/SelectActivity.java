package com.Branham.facebookcam2;

import java.io.File;	
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.GridView;
import android.widget.Spinner;
import com.facebook.HttpMethod;
import com.facebook.Request;
import com.facebook.Response;
import com.facebook.Session;
import com.facebook.SessionState;
import com.facebook.UiLifecycleHelper;
import com.facebook.android.Facebook;
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

		private Session.StatusCallback callback = new Session.StatusCallback() {
			
			@Override
			public void call(Session session, SessionState state, Exception exception) {
				// TODO Auto-generated method stub
				
			}
		};
		private UiLifecycleHelper uiHelper;
		private Spinner albums;
		private ArrayList<Album> albumList;
		private ArrayList<String> albumNames;
		private ArrayAdapter adapter;
		private File[] files;
		private Bitmap[] uploadPics;
	
	
		@Override
		protected void onCreate(Bundle savedInstanceState){
			super.onCreate(savedInstanceState);
			setContentView(R.layout.select_activity);
			getActionBar().setDisplayHomeAsUpEnabled(true);
			
			// Get permission to read album titles
			LoginButton authButton = (LoginButton) findViewById(R.id.authButton);
			authButton.setReadPermissions(Arrays.asList("public_profile", "user_photos"));
			
			// uiHelper is for keeping the Login button updated
			uiHelper = new UiLifecycleHelper(this,callback);
			uiHelper.onCreate(savedInstanceState);
			
			/*The Pictures*/
			
			GridView pictureGrid = (GridView) findViewById(R.id.view_gridview);
			ImageAdapter myImageAdapter = new ImageAdapter(this);
			pictureGrid.setAdapter(myImageAdapter);
		
			String ExternalStorageDirectoryPath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).getPath();
			String path = ExternalStorageDirectoryPath + "/FacebookCam2";
			File targetDir = new File(path);
			
			/*	Code used to see if the path worked
			Log.d("Files",Environment.getExternalStorageState());
			Log.d("Files",targetDir.exists()+"");
			Log.d("Files",targetDir.isDirectory()+"");
			Log.d("Files",targetDir.listFiles()+"");
			*/
			
			files = targetDir.listFiles();
			for(int i=0; i<files.length; i++){
				File file = files[i];
				myImageAdapter.add(file.getAbsolutePath());
			}
			
			/*The Login*/
			
			albums = (Spinner)findViewById(R.id.spinner_album);
			albums.setVisibility(View.GONE);						// Hide spinner while not logged in
			
			
			/*The Upload*/
			
			Button upload = (Button) findViewById(R.id.button_upload);
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
							getPicBitmaps();
							if(uploadPics.length > 0){
								for(int i=0; i < uploadPics.length; i++){
									Bundle params = new Bundle();
									params.putParcelable("source", uploadPics[i]);
									params.putString("message", "");				// For a description WORKS... maybe add a text box
									params.putBoolean("no_story", true);
									Log.d("Upload", params.toString());
									Request upload = makeUploadRequest(params);
									upload.executeAsync();
									
									/*Log.d("Upload", test.toString());
									Log.d("Upload", test.getGraphPath());
									Log.d("Upload", test.getRestMethod());
									Log.d("Upload", test.getParameters().toString());*/
									
								}
							}
							
						}
					}
			);
		}
		
		public Request makeUploadRequest(Bundle params){
			return new Request(
					Session.getActiveSession(),
					getUploadPath(),
					params,
					HttpMethod.POST,
					new Request.Callback() {
						
						@Override
						public void onCompleted(Response response) {
							Log.d("Upload", response.toString());
							Log.d("Upload", "Upload for a file Completed");
							
							// Delete photos from phone
							delLocalPics();
							
						}
					});
		}
		
		public void delLocalPics(){
			
			for(int i=0; i<files.length; i++){
				if(files[i].delete()){
					Log.d("Delete", "Success");
				}else{
					Log.d("Delete", "Problems");
				}
			}
			
		}
		
		public void getPicBitmaps(){
			uploadPics = new Bitmap[files.length];
			for(int i = 0; i < files.length; i++){
				uploadPics[i] = BitmapFactory.decodeFile(files[i].getPath());
				Log.d("Bitmap", uploadPics[i].toString());
			}
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
				Log.d("Session", "Logged out...");
			}
			if(state.isOpened()){  
				Log.d("Session", "Logged in...");
				
				Log.d("Session", "Access token: " + session.getAccessToken());
		
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
		
		// Add Array to arrayAdapter and set the spinner visible
		public void updateSpinner(){
			adapter = new ArrayAdapter<String>(this,android.R.layout.simple_spinner_item, albumNames);
			albums.setAdapter(adapter);
			albums.setVisibility(View.VISIBLE);
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
