package com.Branham.facebookcam2;

import java.io.File;

import android.app.Activity;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.widget.GridView;
import android.widget.Toast;

/**
 * 
 * @author stevenbranham
 *
 * References used: 
 * 	http://android-er.blogspot.com/2012/07/gridview-loading-photos-from-sd-card.html
 * 	
 */


public class SelectActivity extends Activity{

		@Override
		protected void onCreate(Bundle savedInstanceState){
			super.onCreate(savedInstanceState);
			setContentView(R.layout.select_activity);
			
			GridView pictureGrid = (GridView) findViewById(R.id.view_gridview);
			ImageAdapter myImageAdapter = new ImageAdapter(this);
			pictureGrid.setAdapter(myImageAdapter);
		
			String ExternalStorageDirectoryPath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).getPath();
			String path = ExternalStorageDirectoryPath + "/FacebookCam2";
			Log.d("This is the path", path);
			Toast.makeText(getApplicationContext(), path, Toast.LENGTH_LONG).show();
			File targetDir = new File(path);
			Log.d("Files",Environment.getExternalStorageState());
			Log.d("Files",targetDir.exists()+"");
			Log.d("Files",targetDir.isDirectory()+"");
			Log.d("Files",targetDir.listFiles()+"");
			File[] files = targetDir.listFiles();
			for(int i=0; i<files.length; i++){
				File file = files[i];
				Log.d("SelectActivity", file.toString());
				myImageAdapter.add(file.getAbsolutePath());
			}
			
		}
}
