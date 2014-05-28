package com.Branham.facebookcam2;

import java.util.ArrayList;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.view.Display;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.BaseAdapter;
import android.widget.ImageView;

public class ImageAdapter extends BaseAdapter{

	private Context mContext;
	ArrayList<String> itemList = new ArrayList<String>();
	//private Bitmap[] my_photos;
	
	public ImageAdapter(Context c){
		mContext = c;
	}
	
	void add(String path){
		itemList.add(path);
	}
	
	@Override
	public int getCount() {
		/*try{
		getImages();
		return my_photos.length;
		}catch(NullPointerException e){
			return 0;
		}*/
		return itemList.size();
	}

	@Override
	public Object getItem(int position) {
		return position;
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		ImageView imageView;
		WindowManager wm = (WindowManager) mContext.getSystemService(Context.WINDOW_SERVICE);
		Display display = wm.getDefaultDisplay();
		Point size = new Point();
		display.getSize(size);
		int width = size.x/3;
		
		if(convertView == null) {
			imageView = new ImageView(mContext);
			//imageView.setLayoutParams(new GridView.LayoutParams(90,90));
			imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
			imageView.setPadding(0,0,0,0);
		}else{
			imageView = (ImageView) convertView;
		}
		
		Bitmap bm = decodeSampledBitmapFromUri(itemList.get(position),width,width);
		
		imageView.setImageBitmap(bm);
		return imageView;
	}
	
	public Bitmap decodeSampledBitmapFromUri(String path, int reqWidth, int reqHeight){
		Bitmap bm = null;
		final BitmapFactory.Options options = new BitmapFactory.Options();
		options.inJustDecodeBounds = true;
		BitmapFactory.decodeFile(path, options);
		
		options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);
		
		options.inJustDecodeBounds = false;
		bm = BitmapFactory.decodeFile(path, options);
		
		return bm;
	}
	
	public int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight){
		final int height = options.outHeight;
		final int width = options.outWidth;
		int inSampleSize = 1;
		
		if (height > reqHeight || width > reqWidth) {
			if (width > height) {
				inSampleSize = Math.round((float)height / (float) reqHeight);
			}else{
				inSampleSize = Math.round((float)width / (float)reqWidth);
			}
		}
		
		return inSampleSize;
	}
	/*private void getImages(){
		String path = Environment.getExternalStorageDirectory().getPath() + "/pictures/facebookcam2";
		Log.d("here", path);
		File directory = new File(path);
		
		try{
		File[] stored = directory.listFiles();
		my_photos = new Bitmap[stored.length];
		
		for (int i=0; i<stored.length; i++){
			File imgFile = new File(stored[i].toString());
			my_photos[i] = BitmapFactory.decodeFile(imgFile.getAbsolutePath());
		}
		}catch(NullPointerException e){
			
		}
	}*/
	
}
