package com.Branham.featherweightcamera;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.hardware.Camera;
import android.hardware.Camera.PictureCallback;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageButton;

public class MainActivity extends Activity {

	private static Camera mCamera;
	private PreviewFBC2 mPreview;
	private static int MEDIA_TYPE_IMAGE = 1;
	private static String TAG = "MainActivity";
	private Intent done;
	private FrameLayout preview;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
		setContentView(R.layout.activity_main);
		
		done = new Intent(this, SelectActivity.class);
		mCamera = getCameraInstance();
		mCamera.setDisplayOrientation(90);
		mPreview = new PreviewFBC2(this, mCamera);
		preview = (FrameLayout) findViewById(R.id.previewFBC2);
		preview.addView(mPreview);
		
		ImageButton captureButton = (ImageButton) findViewById(R.id.button_capture);
		captureButton.setOnClickListener(
				new View.OnClickListener() {
					
					@Override
					public void onClick(View v) {
						mCamera.takePicture(null, null, mPicture);
					}
				}
		);
		
		ImageButton doneButton = (ImageButton) findViewById(R.id.button_done);
		doneButton.setOnClickListener(
				new View.OnClickListener() {
					
					@Override
					public void onClick(View v) {
						startActivity(done);
					}
				}
		);
		
	}
	
	@Override
	protected void onPause(){
		super.onPause();
		releaseCamera();
	}
	
	private void releaseCamera(){
		if(mCamera != null){
			mPreview.getHolder().removeCallback(mPreview);
			mCamera.release();
			mCamera = null;
		}
	}
	
	
	/**Access Camera**/
	
	public static Camera getCameraInstance(){
		Camera c = null;				// Declare a Camera object c
		try{
			c = Camera.open();			// Try to get a Camera instance
		}catch(Exception e){
										// Camera is unavailable or doesn't exist
		}
		return c; 						// Will return null if unavailable
	}
	
	private PictureCallback mPicture = new PictureCallback(){
		
		@Override
		public void onPictureTaken(byte[] data, Camera camera) {
			
			File pictureFile = getOutputMediaFile(MEDIA_TYPE_IMAGE);
			if (pictureFile == null){
				Log.d(TAG, "Error creating media file, check storage permissions");
				return;
			}
			try{
				FileOutputStream fos = new FileOutputStream(pictureFile);
				fos.write(data);
				fos.close();
			} catch (FileNotFoundException e){
				Log.d(TAG, "File not found: " + e.getMessage());
			} catch (IOException e){
				Log.d(TAG, "Error accessing file: " + e.getMessage());
			}
			if(rotatePic(pictureFile)){
				Log.d("Rotate", "Should have rotated");
			}else{
				Log.d("Rotate", "Failed rotate");
			}
		}
	};
	
	/** Create a file Uri for saving an image or video */
	/*private static Uri getOutputMediaFileUri(int type){
	      return Uri.fromFile(getOutputMediaFile(type));
	}*/

	/** Create a File for saving an image or video */
	private static File getOutputMediaFile(int type){

	    File mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(
	              Environment.DIRECTORY_PICTURES), "FacebookCam2");

	    // Create the storage directory if it does not exist
	    if (! mediaStorageDir.exists()){
	        if (! mediaStorageDir.mkdirs()){
	            Log.d(TAG, "failed to create directory");
	            return null;
	        }
	    }

	    // Create a media file name
	    String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss",Locale.US).format(new Date());
	    File mediaFile;
	    if (type == MEDIA_TYPE_IMAGE){
	        mediaFile = new File(mediaStorageDir.getPath() + File.separator +
	        "IMG_"+ timeStamp + ".jpg");
	    } /*else if(type == MEDIA_TYPE_VIDEO) {
	        mediaFile = new File(mediaStorageDir.getPath() + File.separator +
	        "VID_"+ timeStamp + ".mp4");
	    }*/ else {
	        return null;
	    }
	    mCamera.startPreview();
	    return mediaFile;
	}
	
	public boolean rotatePic(File file){
		String filePath = file.getAbsolutePath();
		Bitmap pic = BitmapFactory.decodeFile(filePath);
		Matrix matrix = new Matrix();
		matrix.postRotate(90);
		pic = Bitmap.createBitmap(pic,0,0,pic.getWidth(),pic.getHeight(),matrix,true);
		file = new File(filePath);
	    FileOutputStream fos;
		try {
			fos = new FileOutputStream(file);
			pic.compress(Bitmap.CompressFormat.PNG, 90, fos);
		    fos.close();  
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e){
			e.printStackTrace();
		}
		return true;
	}
}
