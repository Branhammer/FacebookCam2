package com.Branham.facebookcam2;

import java.io.IOException;

import android.content.Context;
import android.hardware.Camera;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

public class PreviewFBC2 extends SurfaceView implements SurfaceHolder.Callback {
	private SurfaceHolder mHolder;
	private Camera mCamera;
	public String TAG = "PreviewFBC2";

	public PreviewFBC2 (Context context, Camera camera) {
		super(context);
		mCamera = camera;
		
		// Install a SurfaceHolder.Callback so we get notified when the
        // underlying surface is created and destroyed.
        mHolder = getHolder();
        mHolder.addCallback(this);
	}

	@Override
	public void surfaceChanged(SurfaceHolder arg0, int arg1, int arg2, int arg3) {
		
		/** If your preview can change or rotate, take care of those events here.
        Make sure to stop the preview before resizing or reformatting it.**/

        if (mHolder.getSurface() == null){
          // Preview surface does not exist
          return;
        }

        // Stop preview before making changes
        try {
            mCamera.stopPreview();
        } catch (Exception e){
          // ignore: tried to stop a non-existent preview
        }

        /** set preview size and make any resize, rotate or
         reformatting changes here **/

        // start preview with new settings
        try {
            mCamera.setPreviewDisplay(mHolder);
            mCamera.startPreview();

        } catch (Exception e){
            Log.d(TAG, "Error starting camera preview: " + e.getMessage());
        }
		
	}

	@Override
	public void surfaceCreated(SurfaceHolder Holder) {
		try {
			mCamera.setPreviewDisplay(Holder);
			mCamera.startPreview();
		}catch (IOException e){
			Log.d(TAG, "Error setting camera preview: " + e.getMessage());
		}
	}

	@Override
	public void surfaceDestroyed(SurfaceHolder arg0) {
		// Release the Camera somewhere!!!
	}
}
