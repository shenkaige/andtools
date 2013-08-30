package com.phodev.andtools.widget;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.Interpolator;

import com.phodev.andtools.R;
import com.phodev.andtools.common.CommonParam;

/**
 * 圆盘
 * 
 * @author skg
 * 
 */
public class RoundDiscView extends View {
	private static final String TAG = "RoundDiscView";
	private float canvasCenterX;// 画布中心坐标
	private float canvasCenterY;// 画布中心坐标
	private int discWidth;// 圆盘宽度
	private int discHeight;// 圆盘高度
	private float discLeft;// 圆盘的Left
	private float discTop;// 圆盘的Top
	private float pointerLeft;// 针的Left
	private float pointerTop;// 针的Top
	private float pointerHatLeft;// 针盖的Left
	private float pointerHatTop;// 针盖的Top
	private float discCentreY;// 圆盘的圆心坐标
	private float discCentreX;// 圆盘的圆心坐标

	private int discRadius;// 半径
	private int discInnerRradius;// 内圆

	private Context mContext;
	private GestureDetector gesture;
	private Paint defPaint;

	/*-------------Bitmap start----------------*/
	private Bitmap b_discBg;// 圆盘图片//从初始化到结束
	private Bitmap b_pointer;// 指针图片//从初始化到结束
	private Bitmap b_pointerHat;// 指针的盖子//从初始化到结束
	/*--------------Bitmap end---------------*/
	private final int Click_Threshold_Value = 15;// 决定是不是Click的阀值
	private final static int max_fling_degrees_speed = 150;// 每秒的最大角速度

	public RoundDiscView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(context);
	}

	public RoundDiscView(Context context) {
		super(context);
		init(context);
	}

	public void init(Context context) {
		setFocusableInTouchMode(true);
		setLongClickable(true);
		mContext = context;
		gesture = new GestureDetector(getContext(), simpleOnGestureListener);
		gesture.setIsLongpressEnabled(true);
		defPaint = new Paint();
		defPaint.setAntiAlias(true);
		//
		b_discBg = createBitmap(R.drawable.disc_bg);// 圆盘底图也需要旋转
		b_pointer = createBitmap(R.drawable.d_pointer);
		b_pointerHat = createBitmap(R.drawable.d_pointer_hat);
	}

	private float disc_scale = 1;// 圆盘的整体缩放率

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		if (b_discBg == null) {
			super.onMeasure(widthMeasureSpec, heightMeasureSpec);
			return;
		}
		int discSize = b_discBg.getWidth();
		disc_scale = 1;
		int vw = resolveSize(discSize, widthMeasureSpec);
		int vh = resolveSize(discSize, heightMeasureSpec);
		int minVSize = Math.min(vw, vh);
		if (discSize > minVSize) {
			disc_scale = minVSize / (float) discSize;
		}
		setMeasuredDimension(vw, vh);
		configSizeInfo(vw, vh);
	}

	private void configSizeInfo(int viewWidth, int viewHeight) {
		if (b_discBg == null || b_pointer == null || b_pointerHat == null) {
			return;
		}
		discWidth = b_discBg.getWidth();
		discHeight = b_discBg.getHeight();
		discRadius = discWidth / 2;// 半径
		discInnerRradius = b_pointerHat.getWidth() / 2;// 内圆
		canvasCenterX = (float) viewWidth / 2;
		canvasCenterY = (float) viewHeight / 2;

		discTop = (viewHeight - discHeight) / 2;
		discLeft = (viewWidth - discWidth) / 2;

		discCentreY = discHeight / 2 + discTop;
		discCentreX = discWidth / 2 + discLeft;

		pointerLeft = canvasCenterX - b_pointer.getWidth() / 2f;
		pointerTop = canvasCenterY - b_pointer.getHeight();
		pointerHatLeft = canvasCenterX - b_pointerHat.getWidth() / 2f;
		pointerHatTop = canvasCenterY - b_pointerHat.getHeight() / 2f;
	}

	private float anchorX;// Touch的Down事件触发点
	private float anchorY;// Touch的Down事件触发点
	private float downX, downY;// touch down时候的x,y坐标

	@Override
	public boolean dispatchTouchEvent(MotionEvent event) {
		boolean result = false;
		boolean isClick = false;
		boolean isFling = gesture.onTouchEvent(event);
		float r = getDistanceFromDiscCenter(event);
		if (event.getAction() != MotionEvent.ACTION_UP && !inDisckArea(r)) {
			return result;
		}
		boolean haveTaskRunning = mDegreeRotator.isRunning()
				|| mVelocityRotator.isRunning();
		if (!haveTaskRunning)
			isClick = checkTouchUp(event);
		switch (event.getAction()) {
		case MotionEvent.ACTION_DOWN:
			markTouchDown(event);
			markeAnchor(event);
			break;
		case MotionEvent.ACTION_UP:
			// Up的时候，坐标没有变动（在一定的范围内就可以），就认为是Click
			if (!haveTaskRunning && !isFling && !isClick)
				checkReviseDegrees();
			break;
		case MotionEvent.ACTION_MOVE:
			if (!isClick && !inInnerCircleArea(r)) {
				updateDiscDegress(getDegrees(event, anchorX, anchorY,
						discCentreX, discCentreY));
				result = true;
			}
			break;
		}
		if (result == true) {
			return true;
		} else {
			return super.dispatchTouchEvent(event);
		}
	}

	/**
	 * 根据Touch的up事件的x有坐标结算到圆盘中心点的距离
	 * 
	 * @param upPoint
	 * @return
	 */
	private float getDistanceFromDiscCenter(MotionEvent upPoint) {
		float x = upPoint.getX();
		float y = upPoint.getY();
		y = Math.abs(y - discCentreY);
		x = Math.abs(x - discCentreX);
		double r = Math.sqrt(x * x + y * y);
		return (float) r;
	}

	/**
	 * 是否在内圆区域内
	 * 
	 * @param distToCenterPoint
	 * @return
	 */
	private boolean inInnerCircleArea(float distToCenterPoint) {
		if (distToCenterPoint < discInnerRradius)
			return true;
		return false;
	}

	/**
	 * 是否在圆盘区域内
	 * 
	 * @param distToCenterPoint
	 * @return
	 */
	private boolean inDisckArea(float distToCenterPoint) {
		if (distToCenterPoint < discRadius)
			return true;
		return false;
	}

	/**
	 * 标记Touch Down Event
	 * 
	 * @param e
	 */
	private void markTouchDown(MotionEvent e) {
		downX = e.getX();
		downY = e.getY();
	}

	/**
	 * 检查Touch up时候时候可以出发onClick Event
	 * 
	 * @param e
	 * @return
	 */
	private boolean checkTouchUp(MotionEvent e) {
		if (e.getAction() == MotionEvent.ACTION_UP) {
			if (Math.abs(e.getY() - downY) > Click_Threshold_Value
					|| Math.abs(e.getX() - downX) > Click_Threshold_Value)
				return false;
			float dist = getDistanceFromDiscCenter(e);
			boolean isInnerCircleArea = inInnerCircleArea(dist);
			if (mDiscListener != null) {
				if (isInnerCircleArea) {
					mDiscListener.onDiscCenterClick();
				} else {
					mDiscListener.onDiscAngleClick(this, getMappingIndex());
				}
			}
			if (!isInnerCircleArea) {
				innerHandleDiscClick(e);
			}
			return true;
		}
		return false;

	}

	/**
	 * 在Touch Down的时候根据Touch的坐标，标记关联点的角度
	 * 
	 * @param event
	 */
	private void markeAnchor(MotionEvent event) {
		updateAnchor(event.getX(), event.getY());
	}

	/**
	 * 更新锚点
	 * 
	 * @param x
	 * @param y
	 */
	private void updateAnchor(float x, float y) {
		anchorX = x;
		anchorY = y;
	}

	/**
	 * 处理圆盘被点击的事件
	 * 
	 * @param e
	 */
	private void innerHandleDiscClick(MotionEvent e) {
		// preMappingIndex = -1;//
		// 清空历史记录，及时点击的是当前指针指向的index，也依然会触发onMappingChange
		float rX = canvasCenterX;
		float rY = 2;// 大于0 小于canvasCenterY都可以
		float degrees = getAbsRawDegrees(e.getX(), e.getY(), rX, rY,
				canvasCenterX, canvasCenterY);
		if (e.getX() <= canvasCenterX) {
			degrees *= -1;
		}
		// 当一个区域被点击的时候，旋转到该区域
		spinWithDegrees(degrees, speed_for_click_spin);
	}

	/**
	 * 获取圆盘状态角度
	 * 
	 * @return
	 */
	public float getDiscStateDegrees() {
		return discStateDegrees;
	}

	/**
	 * @see #getDegrees(float, float, float, float, float, float)
	 */
	private float getDegrees(MotionEvent end, float anchorX, float anchorY,
			float centerX, float centerY) {
		return getDegrees(end.getX(), end.getY(), anchorX, anchorY, centerX,
				centerY);
	}

	/**
	 * 根据圆心坐标，Anchor坐标，动态给定的当前坐标求出之间的夹角(有正负之分)
	 * 
	 * @param curX
	 * @param curY
	 * @param anchorX
	 * @param anchorY
	 * @param centerX
	 * @param centerY
	 * @return
	 */
	private float getDegrees(float curX, float curY, float anchorX,
			float anchorY, float centerX, float centerY) {
		float degrees = getRawDegrees(curX, curY, anchorX, anchorY, centerX,
				centerY);
		/* 角度算出来以后要更新锚点 anchorY，anchorX */
		updateAnchor(curX, curY);
		return degrees;
	}

	/**
	 * 根据连点，跟圆心的左边，算出夹角,只有正数角度
	 * 
	 * @param curX
	 * @param curY
	 * @param preX
	 * @param preY
	 * @param centerX
	 * @param centerY
	 * @return
	 */
	private float getAbsRawDegrees(float curX, float curY, float preX,
			float preY, float centerX, float centerY) {
		// 根据touch的坐标旋转，不可以超出touch的坐标范围
		double la, lb;// 边a，b
		double a, b, c;
		la = Math.abs(preY - discCentreY);
		lb = Math.abs(preX - discCentreX);
		a = Math.sqrt(lb * lb + la * la);

		la = Math.abs(curY - discCentreY);
		lb = Math.abs(curX - discCentreX);
		b = Math.sqrt(lb * lb + la * la);

		la = Math.abs(curY - preY);
		lb = Math.abs(curX - preX);
		c = Math.sqrt(lb * lb + la * la);

		double cosC = (a * a + b * b - c * c) / (2 * a * b);
		double degrees = Math.acos(cosC) * (180 / Math.PI);

		return (float) degrees;
	}

	/**
	 * 根据连点，跟圆心的左边，算出夹角
	 * 
	 * @param curX
	 * @param curY
	 * @param preX
	 * @param preY
	 * @param centerX
	 * @param centerY
	 * @return
	 */
	private float getRawDegrees(float curX, float curY, float preX, float preY,
			float centerX, float centerY) {
		double dCur = getRadian(curX, curY, centerX, centerY);
		double dPre = getRadian(preX, preY, centerX, centerY);
		return (float) ((dPre - dCur) * 180 / Math.PI);
	}

	/**
	 * 算出一点和边的弧度
	 * 
	 * @param x
	 * @param y
	 * @param centerX
	 * @param centerY
	 * @return
	 */
	private double getRadian(float x, float y, float centerX, float centerY) {
		double radian = 0;
		y -= centerY;
		x -= centerX;
		double delt = Math.abs(y / x);
		if (y > 0 && x > 0) {
			radian = Math.atan(delt);
		} else if (y > 0 && x < 0) {
			radian = Math.PI - Math.atan(delt);
		} else if (y < 0 && x < 0) {
			radian = Math.PI + Math.atan(delt);
		} else if (y < 0 && x > 0) {
			radian = 2 * Math.PI - Math.atan(delt);
		}
		return radian;
	}

	float lastChangedDegrees;

	/**
	 * 刷新转盘的角度,并没有过度的过程
	 * 
	 * @param degrees
	 */
	public void updateDiscDegress(float degreesChanged) {
		discStateDegrees += degreesChanged;
		lastChangedDegrees = degreesChanged;
		invalidate();
	}

	private VelocityRotator mVelocityRotator = new VelocityRotator();

	/**
	 * 根据角度旋转Disk
	 * 
	 * @param degrees
	 */
	public void spinWihtSpeed(float degreesSpeedPerSeconde) {
		mVelocityRotator.stop();
		mVelocityRotator.setupRunning(degreesSpeedPerSeconde);
		mVelocityRotator.start();
	}

	private final static float speed_for_click_spin = 4;
	private static final float speed_for_revise_degrees = 0.2f;
	private DegreeRotator mDegreeRotator = new DegreeRotator();

	public void spinWithDegrees(float degrees, float speed) {
		mDegreeRotator.stop();
		mDegreeRotator.setupRunning(degrees, speed);
		mDegreeRotator.start();
	}

	// 圆盘当前的状态选装的角度
	private float discStateDegrees;

	// ----------------------------------------------draw disc start--------
	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		int c_scale_count = canvas.save();
		canvas.scale(disc_scale, disc_scale, canvasCenterX, canvasCenterY);
		float canvasDegrees = -discStateDegrees;// 旋转画布跟旋转图片是相反的角度所以要取反
		// 清除画布//surface view
		// canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
		int c_rotate_count = canvas.save();
		canvas.rotate(canvasDegrees, canvasCenterX, canvasCenterY);
		canvas.drawBitmap(b_discBg, discLeft, discTop, defPaint);
		canvas.restoreToCount(c_rotate_count);
		// 先画针，再画针盖
		canvas.drawBitmap(b_pointer, pointerLeft, pointerTop, defPaint);
		canvas.drawBitmap(b_pointerHat, pointerHatLeft, pointerHatTop, defPaint);
		canvas.restoreToCount(c_scale_count);

	}

	// ----------------------------------------------draw disc end--------
	/**
	 * 负责处理onFling的滑动
	 */
	SimpleOnGestureListener simpleOnGestureListener = new SimpleOnGestureListener() {
		@Override
		public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX,
				float velocityY) {
			float ab = velocityX * velocityY;
			double c = Math.sqrt(velocityX * velocityX + velocityY * velocityY);
			float degreesV = (float) (((ab / c) / (discRadius * Math.PI)) * 180);
			float taskDegress;
			if (lastChangedDegrees < 0) {
				if (degreesV < 0) {
					taskDegress = degreesV;
				} else {
					taskDegress = -degreesV;
				}
			} else {
				if (degreesV < 0) {
					taskDegress = -degreesV;
				} else {
					taskDegress = degreesV;
				}
			}
			if (Math.abs(taskDegress) > max_fling_degrees_speed) {
				if (taskDegress > 0) {
					taskDegress = max_fling_degrees_speed;
				} else {
					taskDegress = -max_fling_degrees_speed;
				}
			}
			if (CommonParam.DEBUG) {
				log("velocityX:" + velocityX + " velocityY:" + velocityY
						+ " taskDegress:" + taskDegress
						+ " lastChangedDegrees:" + lastChangedDegrees);
			}
			spinWihtSpeed(taskDegress);
			return true;
		}
	};

	/**
	 * 创建指定资源ID的Bitmap
	 * 
	 * @param resId
	 * @return
	 */
	private Bitmap createBitmap(int resId) {
		return BitmapFactory.decodeResource(mContext.getResources(), resId);
	}

	/**
	 * 释放view,释放过后就不能在使用了
	 */
	public void releaseView() {
		releaseBitmap(b_discBg);
		releaseBitmap(b_pointer);
		releaseBitmap(b_pointerHat);
	}

	private void releaseBitmap(Bitmap b) {
		if (b != null && !b.isRecycled()) {
			b.recycle();
		}
	}

	public interface DiscListener {
		/**
		 * 圆盘旋转完毕
		 * 
		 * @param rd
		 * @param selectedIndex
		 *            圆盘旋转后，指针下面对应的索引
		 */
		public void onPointerMappingChanged(RoundDiscView rd, int mappingIndex);

		/**
		 * 角度区域对应被赋予的意义通过degreesIndex来获得
		 * 
		 * @param rd
		 * @param degreesIndex
		 */
		public void onDiscAngleClick(RoundDiscView rd, int degreesIndex);

		public void onDiscCenterClick();

	}

	private DiscListener mDiscListener;// 监听Disc的状态

	/**
	 * 设置圆盘状态监听
	 * 
	 * @param discListener
	 */
	public void setDiscListener(DiscListener discListener) {
		mDiscListener = discListener;
	}

	// 速度旋转器，根据其速度来旋转，并不知道终点在哪里
	final class VelocityRotator implements Runnable {
		static final float disc_max_friction_factor = 0.5f;
		static final float disc_min_friction_factor = 0.06f;
		static final int ANIMATION_FPS = 1000 / 60;
		//
		private float mStartSpeed;
		private float currentSpeed;
		private float total_dist = 0;
		private boolean isRunning = false;

		/**
		 * @param targetView
		 * @param startSpeed
		 *            speed Per Second
		 */
		public void setupRunning(float startSpeed) {
			mStartSpeed = startSpeed / ANIMATION_FPS;
			currentSpeed = mStartSpeed;
		}

		@Override
		public void run() {
			if (Math.abs(currentSpeed) > 0) {
				total_dist += currentSpeed;
				float d_f_f = disc_max_friction_factor
						* (1 - currentSpeed / mStartSpeed);
				if (d_f_f > disc_max_friction_factor) {
					d_f_f = disc_max_friction_factor;
				} else if (d_f_f < disc_min_friction_factor) {
					d_f_f = disc_min_friction_factor;
				}
				float absSpeed = Math.abs(currentSpeed);
				if (d_f_f > absSpeed) {
					d_f_f = absSpeed;
				}
				if (mStartSpeed > 0) {
					currentSpeed -= d_f_f;
				} else {
					currentSpeed += d_f_f;
				}
				if (CommonParam.DEBUG) {
					log("currentSpeed:" + currentSpeed + " d_f_f:" + d_f_f);
				}
				updateDiscDegress(currentSpeed);
				postDelayed(this, ANIMATION_FPS);
			} else {
				mStartSpeed = 0;
				currentSpeed = 0;
				stop();
				onVelocityRotatEnd();
			}
		}

		public boolean isRunning() {
			return isRunning;
		}

		public void stop() {
			removeCallbacks(this);
			isRunning = false;
		}

		public void start() {
			if (isRunning) {
				stop();
			}
			isRunning = true;
			post(this);
		}
	}

	// 旋转指定的角度
	final class DegreeRotator implements Runnable {
		Interpolator interpolator = new DecelerateInterpolator(0.2f);
		float mDegreesTask;
		float mAbsDegreesTask;
		float mSpeed;
		boolean isRunning = false;

		public void setupRunning(float degreesTask, float speed) {
			mDegreesTask = degreesTask;
			mAbsDegreesTask = Math.abs(degreesTask);
			mSpeed = speed;
		}

		@Override
		public void run() {
			if (mAbsDegreesTask > 0) {
				if (mAbsDegreesTask < mSpeed) {
					mSpeed = mAbsDegreesTask;
				}
				mAbsDegreesTask -= mSpeed;
				if (mDegreesTask > 0) {
					updateDiscDegress(mSpeed);
				} else {
					updateDiscDegress(-mSpeed);
				}
				postDelayed(this, VelocityRotator.ANIMATION_FPS);
			} else {
				mDegreesTask = 0;
				mAbsDegreesTask = -1;
				mSpeed = 0;
				stop();
				onDegreeRotatEnd();
			}
		}

		public boolean isRunning() {
			return isRunning;
		}

		public void stop() {
			removeCallbacks(this);
			isRunning = false;
		}

		public void start() {
			if (isRunning) {
				stop();
			}
			isRunning = true;
			post(this);
		}
	}

	/**
	 * 根据Mapping Index 获取角度
	 */
	public int getDegreesByMapping(int index) {
		if (index > 12) {
			index = 12;
		}
		if (index <= 0)
			index = 1;
		int d = (index - 1) * 30;
		return d;
	}

	private float lastCalculateIndexDegrees;
	private int lastCalculateIndexResult = -1;

	public int getMappingIndex() {
		float rootDegrees = getDiscStateDegrees();
		if (lastCalculateIndexResult != -1) {
			if (lastCalculateIndexDegrees == rootDegrees) {
				// 这种情况不需要再次计算
				return lastCalculateIndexResult;
			}
		}
		lastCalculateIndexDegrees = rootDegrees;
		rootDegrees %= 360;
		if (rootDegrees < 0) {
			rootDegrees += 360;
		}
		int i = 1;
		if (rootDegrees >= 345 || rootDegrees <= 15) {
			i = 1;
		} else {
			rootDegrees += 15;
			i = (int) rootDegrees / 30;
			if (rootDegrees % 30 != 0) {
				i++;
			}
		}
		if (i <= 0) {
			i = 1;
		} else if (i > 12) {
			i = 12;
		}
		return i;
	}

	protected void onDegreeRotatEnd() {
		if (!checkReviseDegrees()) {
			checkMappingChanged();
		}
	}

	protected void onVelocityRotatEnd() {
		if (!checkReviseDegrees()) {
			checkMappingChanged();
		}
	}

	private int lastMappingIndex = -1;

	private void checkMappingChanged() {
		if (mDiscListener != null) {
			int index = getMappingIndex();
			if (lastMappingIndex != index) {
				lastMappingIndex = index;
				mDiscListener.onPointerMappingChanged(this, index);
			}
		}
	}

	private static final float DEGREES_REVISE_DEVIATION = 0.3f;

	/**
	 * 修正角度，如果当角度并不需要修正则返回false
	 * 
	 * @return
	 */
	public boolean checkReviseDegrees() {
		// check degree mapping
		int currentMapping = getMappingIndex();
		float targetDegrees = getDegreesByMapping(currentMapping);
		// 修正角度，使指针知道每个小图的正中间
		float currentD = getDiscStateDegrees();
		currentD %= 360;
		if (currentD < 0) {
			currentD += 360;
		}
		float deviation = targetDegrees - currentD;
		if (Math.abs(deviation) < DEGREES_REVISE_DEVIATION) {
			// 不需要修正
			return false;
		}
		if (targetDegrees == 0 && deviation < -15) {
			deviation += 360;
		}
		spinWithDegrees(deviation, speed_for_revise_degrees);
		return true;
	}

	void log(String msg) {
		Log.d(TAG, "" + msg);
	}
}