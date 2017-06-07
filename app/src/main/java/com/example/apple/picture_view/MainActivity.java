package com.example.apple.picture_view;

import android.app.Activity;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.RequiresApi;
import android.util.Log;import org.json.JSONArray;
import org.json.JSONException;

import java.util.List;

public class MainActivity extends Activity{
	private DrawingView mView;
	
	@RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		mView = new DrawingView(this);
		super.onCreate(savedInstanceState);
		setContentView(mView);

		mView.setBackground(getResources().getDrawable(R.drawable.ww));
		Bitmap bmp1 = BitmapFactory.decodeResource(getResources(),R.drawable.clothes);
		Bitmap bmp2 = BitmapFactory.decodeResource(getResources(),R.drawable.skirt);
		CustomBitmap customBitmap1 = new CustomBitmap(bmp1);
		CustomBitmap customBitmap2 = new CustomBitmap(bmp2);
		customBitmap1.setId(1);
		customBitmap2.setId(2);
		if (getSavedMatrix(1) != null){
			Log.e("tag", "matrix 1 is not null");
			customBitmap1.setMatrix(getSavedMatrix(1));

		}
		if (getSavedMatrix(2) != null){
			Log.e("tag", "matrix 2 is not null");
			customBitmap2.setMatrix(getSavedMatrix(2));
		}
		mView.addBitmap(customBitmap1);
		mView.addBitmap(customBitmap2);
	}

	private void saveMatrix(CustomBitmap customBitmap){
		Log.e("tag", "save matrix" + customBitmap.getId());
		SharedPreferences.Editor editor = getSharedPreferences("matrix", 1).edit();
		Matrix matrix = customBitmap.matrix;
		float[] values = new float[9];
		matrix.getValues(values);
		JSONArray array = new JSONArray();
		for (float value:values){
			try {
				array.put(value);
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
		editor.putString(String.valueOf(customBitmap.getId()), array.toString());
		editor.commit();
		Log.e("tag", "save matrix id:" + customBitmap.getId() + "---------"+values[Matrix.MPERSP_0] + " , " + values[Matrix.MPERSP_1] + " , " +
				values[Matrix.MPERSP_2] + " , " + values[Matrix.MSCALE_X] + " , " +
				values[Matrix.MSCALE_Y] + " , " + values[Matrix.MSKEW_X] + " , " +
						values[Matrix.MSKEW_Y] + " , " +values[Matrix.MTRANS_X] + " , " +
						values[Matrix.MTRANS_Y]);
	}

	//��ȡmatrix
	private Matrix getSavedMatrix(int id){
		SharedPreferences sp = getSharedPreferences("matrix", 1);
		String result = sp.getString(String.valueOf(id), null);
		if (result != null){
			float[] values = new float[9];
			Matrix matrix = new Matrix();
				try {
					JSONArray array = new JSONArray(result);
					for (int i = 0; i < array.length(); i++) {
						values[i] = Float.valueOf(String.valueOf(array.getDouble(i)));
					}
					matrix.setValues(values);
				} catch (JSONException e) {
					e.printStackTrace();
				}
			Log.e("tag", "get matrix id:" + id + "---------"+values[Matrix.MPERSP_0] + " , " + values[Matrix.MPERSP_1] + " , " +
					values[Matrix.MPERSP_2] + " , " + values[Matrix.MSCALE_X] + " , " +
					values[Matrix.MSCALE_Y] + " , " + values[Matrix.MSKEW_X] + " , " +
					values[Matrix.MSKEW_Y] + " , " +values[Matrix.MTRANS_X] + " , " +
					values[Matrix.MTRANS_Y]);

			return matrix ;
		}
		return null;
	}

	@Override
	public void finish() {
		List<CustomBitmap> list = mView.getViews();
		for (CustomBitmap customBitmap:list){
			saveMatrix(customBitmap);
		}
		super.finish();
	}
}
