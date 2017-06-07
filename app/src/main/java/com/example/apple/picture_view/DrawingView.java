package com.example.apple.picture_view;


import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PointF;
import android.graphics.Rect;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import java.util.ArrayList;
import java.util.List;

public class DrawingView extends View{

	private List<CustomBitmap> _bitmaps;
	private Context _context;
	private CustomBitmap _curCustomBitmap;


	private Matrix currentMatrix = new Matrix();

	/**
	 * 模式 NONE：无 DRAG：拖拽. ZOOM:缩放
	 *
	 *
	 */
	private enum MODE {
		NONE, DRAG, ZOOM

	};
	private MODE mode = MODE.NONE;

	public DrawingView(Context context) {
		super(context);
		this._context = context;
		_bitmaps = new ArrayList<>();
	}

	public void addBitmap(CustomBitmap bitmap){
		_bitmaps.add(bitmap);
	}

	public List<CustomBitmap> getViews(){
		return _bitmaps;
	}

	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		Paint paint = new Paint();
		paint.setStyle(Paint.Style.FILL);
		paint.setAntiAlias(true);
		for(CustomBitmap bitmap:_bitmaps){
			canvas.drawBitmap(bitmap.getBitmap(), bitmap.matrix, paint);
		}
	}

	/**
	 * 计算两点之间的距离
	 *
	 * @param event
	 * @return
	 */
	public float distance(MotionEvent event) {
		float dx = event.getX(1) - event.getX(0);
		float dy = event.getY(1) - event.getY(0);
		return (float) Math.sqrt(dx * dx + dy * dy);
	}

	/**
	 * 计算两点之间的中间点
	 *
	 * @param event
	 * @return
	 */
	public PointF mid(MotionEvent event) {
		float midX = (event.getX(1) + event.getX(0)) / 2;
		float midY = (event.getY(1) + event.getY(0)) / 2;
		return new PointF(midX, midY);
	}

	public float rotation(MotionEvent event) {
		double delta_x = (event.getX(0) - event.getX(1));
		double delta_y = (event.getY(0) - event.getY(1));
		double radians = Math.atan2(delta_y, delta_x);
		return (float) Math.toDegrees(radians);

	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		switch (event.getAction() & MotionEvent.ACTION_MASK) {
			case MotionEvent.ACTION_DOWN:// 手指压下屏幕
				mode = MODE.DRAG;
				if(_curCustomBitmap == null && _bitmaps.size() > 0){
					_curCustomBitmap = _bitmaps.get(_bitmaps.size()-1);
				}
				boolean isChanged = false;//当前操作bitmap是否改变
				for(CustomBitmap bitmap:_bitmaps){
					float[] values = new float[9];
					bitmap.matrix.getValues(values);
					float globalX = values[Matrix.MTRANS_X];
					float globalY = values[Matrix.MTRANS_Y];
					float width = values[Matrix.MSCALE_X]*bitmap.getBitmap().getWidth();
					float height = values[Matrix.MSCALE_Y]*bitmap.getBitmap().getWidth();
					Log.e("tag", "globalX: " + globalX + " ,globalY: " + globalY + " ,t: " + width + " ,b: " + height);

					Rect rect = new Rect((int)globalX, (int)globalY, (int)(globalX+width), (int)(globalY+height));
					Log.e("tag", "l: " + rect.left + " ,r: " + rect.right + " ,t: " + rect.top + " ,b: " + rect.bottom);
					if(rect.contains((int)event.getX(), (int)event.getY())){
						_curCustomBitmap = bitmap;
						isChanged = true;
					}
				}
				//切换操作对象，只要把这个对象添加到栈底就行
				if(isChanged){
					_bitmaps.remove(_curCustomBitmap);
					_bitmaps.add(_curCustomBitmap);
				}
				currentMatrix.set(_curCustomBitmap.matrix);// 记录ImageView当前的移动位置
				_curCustomBitmap.matrix.set(currentMatrix);
				_curCustomBitmap.startPoint.set(event.getX(), event.getY());
				postInvalidate();
				break;

			case MotionEvent.ACTION_POINTER_DOWN:// 当屏幕上还有触点（手指），再有一个手指压下屏幕
				mode = MODE.ZOOM;
				_curCustomBitmap.oldRotation = rotation(event);
				_curCustomBitmap.startDis = distance(event);
				if (_curCustomBitmap.startDis > 10f) {
					_curCustomBitmap.midPoint = mid(event);
					currentMatrix.set(_curCustomBitmap.matrix);// 记录ImageView当前的缩放倍数
				}
				break;

			case MotionEvent.ACTION_MOVE:// 手指在屏幕移动，该 事件会不断地触发
				if (mode == MODE.DRAG) {
					float dx = event.getX() - _curCustomBitmap.startPoint.x;// 得到在x轴的移动距离
					float dy = event.getY() - _curCustomBitmap.startPoint.y;// 得到在y轴的移动距离
					_curCustomBitmap.matrix.set(currentMatrix);// 在没有进行移动之前的位置基础上进行移动
					_curCustomBitmap.matrix.postTranslate(dx, dy);
				} else if (mode == MODE.ZOOM) {// 缩放与旋转
					float endDis = distance(event);// 结束距离
					_curCustomBitmap.rotation = rotation(event) - _curCustomBitmap.oldRotation;
					if (endDis > 10f) {
						float scale = endDis / _curCustomBitmap.startDis;// 得到缩放倍数
						_curCustomBitmap.matrix.set(currentMatrix);
						_curCustomBitmap.matrix.postScale(scale, scale, _curCustomBitmap.midPoint.x, _curCustomBitmap.midPoint.y);
						_curCustomBitmap.matrix.postRotate(_curCustomBitmap.rotation, _curCustomBitmap.midPoint.x, _curCustomBitmap.midPoint.y);
					}
				}
				break;

			case MotionEvent.ACTION_UP:// 手指离开屏
				break;
			case MotionEvent.ACTION_POINTER_UP:// 有手指离开屏幕,但屏幕还有触点（手指）
				mode = MODE.NONE;
				break;
		}
		invalidate();
		return true;
	}


}
