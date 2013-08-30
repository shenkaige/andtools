package com.phodev.andtools.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;

import com.phodev.andtools.R;
import com.phodev.andtools.common.CommonParam;

/**
 * ImageMarqueeView 类似于TextView的Marquee特性
 * 
 * @author skg
 * 
 */
public class ImageMarqueeView extends ImageView {
	private final String TAG = "ImageMarqueeView";
	private final boolean DEBUG = CommonParam.DEBUG;
	private Drawable mDrawable;
	private int mDrawableWidth;
	private int mDrawableHeight;
	private Orientation orientation = Orientation.To_Header;
	private int defLeft;// 默认显示的位置
	private int defTop;// 默认显示的位置
	private float curLeft;// 当前显示的位置
	private float curTop;// 当前显示的位置
	private int headerCoord;// 头部坐标
	private int centerCoord;// 中间坐标
	private int footCoord;// 尾部坐标
	//
	/**
	 * Speed定义：每个STEP_TIME_INTERVAL时间内移动的距离，可以理解为per step dist
	 */
	private final float MIN_SPEED = 0.2f;// 每STEP_TIME_INTERVAL移动的距离
	private final float MAX_SPEED = 2.8f;// 每STEP_TIME_INTERVAL移动的距离
	private final int STEP_TIME_INTERVAL = 30;// 1s=1000ms,每秒24-30帧动画才连贯
	private final int EXPECT_LIFECYCLE_TIME = 5 * 1000;
	/**
	 * 根据每步移动的时候，跟希望整个生命周期的时间算出一共要移动多少不，这个结果只是希望的结果，<br>
	 * 具体会根据min_speed，max_speed有调整
	 */
	private final int EXPECT_STEP_COUNTS = EXPECT_LIFECYCLE_TIME
			/ STEP_TIME_INTERVAL;
	private final int LIFE_START_PAUSE_TIME = 1500;// 开始滚动时候的停顿时间
	private final int LIFE_FINISH_PAUSE_TIME = 2000;// 完成滚动任务后在到触发一个生命周期的停顿时间
	private float curSpeed = 0f;// 每步移动的距离
	private int curStepTimeInterval = STEP_TIME_INTERVAL;
	//
	private final int EMPTY_LIFE_CYCLE_INTERVAL = EXPECT_LIFECYCLE_TIME;// 如果图片是空，那么等待多长时间算是一个lifecycle
	private final int MESSAGE_MARQUEE_MOVE = 0x10001;
	private final int MESSAGE_MARQUEE_EMPTY_WAIT = 0x10002;
	private final int MESSAGE_MARQUEE_START_WAIT = 0x10003;// Action开始滚动时候的等待
	private final int MESSAGE_MARQUEE_FINISH_WAIT = 0x10004;// Action开始结束一个生命周期的的间隔
	private float fitScale;
	private boolean isLeftRightMarquee = true;// 左右滚动
	private boolean mAutoStart = true;// 自动管理滚动
	//
	private MarqueeState curState = MarqueeState.Stoped;
	private MarqueeState frezonSavedStateRecord = curState;// 自动保存的保存状态
	private MarqueeState tempSavedStateRecord = null;

	public ImageMarqueeView(Context context) {
		super(context);
	}

	public ImageMarqueeView(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public ImageMarqueeView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		TypedArray a = context.obtainStyledAttributes(attrs,
				R.styleable.ImageMarqueeView, defStyle, 0);
		Drawable d = a.getDrawable(R.styleable.ImageMarqueeView_src);
		updateDrawable(d);
		a.recycle();
	}

	public void setImageResource(int resId) {
		setImageDrawable(getResources().getDrawable(resId));
	}

	/**
	 * setImageURI
	 */
	public void setImageURI(Uri uri) {
		throw new RuntimeException(TAG
				+ ":not support method setImageURI(Uri uri)");
	}

	public void setImageBitmap(Bitmap bm) {
		if (bm == null) {
			setImageDrawable(null);
		} else {
			setImageDrawable(new BitmapDrawable(bm));
		}
	}

	public void setImageDrawable(Drawable drawable) {
		/**
		 * <pre>
		 * 如果需要让View支持Animation，就必须给Drawable设置一个合适的bound，
		 * 小于等于View宽高，在这里直接调用ImageView的super.setImageDrawable(drawable);
		 * 让父类的方法ImageView.configureBounds();计算一个有效的Bounds
		 * 如果不需要支持Animation的话，则不需要调用super.setImageDrawable(drawable);
		 * </pre>
		 */
		updateDrawable(drawable);
		/* 如果不调用super.setImageDrawable(drawable);那么下面的方面必须调用 */
		// requestLayout();
		// invalidate();
		super.setImageDrawable(drawable);

	}

	private int mRemedyBackgroundResource;
	private Drawable mRemedyBackground;

	/**
	 * @see #setRemedyBackgroud(Drawable)
	 * 
	 * @param resId
	 */
	public void setRemedyBackgroud(int resid) {
		if (resid != 0 && resid == mRemedyBackgroundResource) {
			return;
		}
		if (resid != 0) {
			setRemedyBackgroud(getResources().getDrawable(resid));
			mRemedyBackgroundResource = resid;
		}
	}

	/**
	 * 如果没有设置ImageDrawable或者是null，那么会自动切换到设置的默认图片，并完成完整的生命周期
	 * 
	 * <pre>
	 * 主意：该方法会和View的Background冲突，如果想使用View自己的BG，就不要使用该方法
	 * </pre>
	 * 
	 * @param d
	 */
	public void setRemedyBackgroud(Drawable d) {
		if (mRemedyBackground == d) {
			return;
		}
		mRemedyBackground = d;
		if (mDrawable == null) {// 没有可用的滚动图片资源
			setBackgroundDrawable(mRemedyBackground);
		} else {
			setBackgroundDrawable(null);
		}
	}

	/**
	 * 移除替代的BG
	 */
	private void checkRemoveRemedyBG() {
		if (mRemedyBackground != null && getBackground() == mRemedyBackground) {
			setBackgroundDrawable(null);
		}
	}

	/**
	 * 没有可滚动的图片，只是默认BG {@link #setRemedyBackgroud(Drawable)}
	 */
	private void checkUseRemedyBG() {
		if (mRemedyBackground != null && getBackground() != mRemedyBackground) {
			setBackgroundDrawable(mRemedyBackground);
		}
	}

	@Override
	protected boolean verifyDrawable(Drawable dr) {
		return mDrawable == dr || super.verifyDrawable(dr);
	}

	@Override
	public void invalidateDrawable(Drawable dr) {
		if (dr == mDrawable) {
			invalidate();
		} else {
			super.invalidateDrawable(dr);
		}
	}

	@Override
	protected void onDraw(Canvas canvas) {
		if (mDrawable != null) {
			int c_count = canvas.save();
			canvas.translate(curLeft, curTop);
			canvas.scale(fitScale, fitScale, 0, 0);
			mDrawable.draw(canvas);
			canvas.restoreToCount(c_count);
		}
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		int w;
		int h;
		if (mDrawable == null) {
			// If no drawable, its intrinsic size is 0.
			mDrawableWidth = -1;
			mDrawableHeight = -1;
			w = h = 0;
		} else {
			w = mDrawableWidth;
			h = mDrawableHeight;
			if (w <= 0)
				w = 1;
			if (h <= 0)
				h = 1;
		}
		int pleft = getPaddingLeft();
		int pright = getPaddingRight();
		int ptop = getPaddingTop();
		int pbottom = getPaddingBottom();

		int widthSize;
		int heightSize;

		w += pleft + pright;
		h += ptop + pbottom;

		w = Math.max(w, getSuggestedMinimumWidth());
		h = Math.max(h, getSuggestedMinimumHeight());

		widthSize = resolveSize(w, widthMeasureSpec);
		heightSize = resolveSize(h, heightMeasureSpec);

		setMeasuredDimension(widthSize, heightSize);
	}

	private boolean isRecycleClear = false;

	/**
	 * 更新 Drawable对象
	 * 
	 * @param d
	 */
	private void updateDrawable(Drawable d) {
		final MarqueeState currentState = getCurrentState();
		if (d != mDrawable) {
			clearAllRecordAndState();
			// Drawable发生改变的时候需要重新计算属性
			needRecalculateOnLayout = true;
			if (mDrawable != null) {
				mDrawable.setCallback(null);
				unscheduleDrawable(mDrawable);
			}
			mDrawable = d;
			if (d != null) {
				d.setCallback(this);
				mDrawableWidth = d.getIntrinsicWidth();
				mDrawableHeight = d.getIntrinsicHeight();
				// 必须设置一个小于，等于View尺寸的Bounds，才能支持Animation
				// d.setBounds(0, 0, mDrawableWidth, mDrawableHeight);
			}
		}
		if (d == null) {// 没有可滚动的图片
			checkUseRemedyBG();
		} else {
			checkRemoveRemedyBG();
			if (isRunning(currentState)) {
				// 重新启动Marquee让新图片资源显示并启动滚动
				stopMarquee();
				startMarquee();
			}
		}
		if (listener != null) {
			listener.onUpdateDrawable(this, mDrawable, isRecycleClear);
		}
	}

	private int fitDrawableW;
	private int fitDrawableH;
	private boolean needRecalculateOnLayout = true;

	@Override
	protected void onLayout(boolean changed, int left, int top, int right,
			int bottom) {
		if (!needRecalculateOnLayout && !changed) {
			// 如果既不需要RecalculateOnLayout，onLayout的chagned也没chagned，就直接返回
			return;
		}
		// 判断是否需要Marquee
		int viewW = getMeasuredWidth();
		int viewH = getMeasuredHeight();
		fitScale = calculateFitScale(mDrawableWidth, mDrawableHeight, viewW,
				viewH);
		fitDrawableW = (int) (fitScale * mDrawableWidth);
		fitDrawableH = (int) (fitScale * mDrawableHeight);
		//
		defLeft = (viewW - fitDrawableW) / 2;
		defTop = (viewH - fitDrawableH) / 2;
		curLeft = defLeft;
		curTop = defTop;
		//
		if (Math.abs(defLeft) > Math.abs(defTop)) {
			isLeftRightMarquee = true;
			headerCoord = 0;
			centerCoord = defLeft;
			footCoord = viewW - fitDrawableW;
			calculateMarqueeSpeed(viewW, fitDrawableW);
		} else {
			isLeftRightMarquee = false;
			headerCoord = 0;
			centerCoord = defTop;
			footCoord = viewH - fitDrawableH;
			calculateMarqueeSpeed(viewH, fitDrawableH);
		}
		orientation = Orientation.To_Header;
		onFitScaleChanged(fitScale);
		// 如果处于Empty_running状态的时候，突然设置了一个可以滚动的图片，这个时候可以激活滚动
		if (isRunning(curState)) {
			startMarquee();
		}
		needRecalculateOnLayout = false;
	}

	/**
	 * 计算每步距离
	 * 
	 * @param boxLength
	 * @param contentLength
	 * @return
	 */
	private void calculateMarqueeSpeed(int boxLength, int contentLength) {
		curStepTimeInterval = STEP_TIME_INTERVAL;
		float speed = 0;
		if (boxLength <= 0 || contentLength <= 0 || contentLength <= boxLength) {
			// 不需要滚动//
			speed = 0;
			if (DEBUG) {
				log("null speed:" + speed);
			}
		} else {
			float distOfMarquee = contentLength - boxLength;
			speed = distOfMarquee / EXPECT_STEP_COUNTS;
			if (speed > MAX_SPEED) {
				speed = MAX_SPEED;
				if (DEBUG) {
					log("use max_speed-->speed:" + speed
							+ " EXPECT_STEP_COUNTS:" + EXPECT_STEP_COUNTS
							+ " real step counts:"
							+ (int) (distOfMarquee / speed));
				}
			} else if (speed < MIN_SPEED) {
				speed = MIN_SPEED;
				if (DEBUG) {
					log("use min_speed-->speed:" + speed
							+ " EXPECT_STEP_COUNTS:" + EXPECT_STEP_COUNTS
							+ " real step counts:"
							+ (int) (distOfMarquee / speed));
				}
			}
			if (speed > 0) {
				if (curStepTimeInterval <= 0) {
					curStepTimeInterval = STEP_TIME_INTERVAL;
				}
				float mT = distOfMarquee / speed * STEP_TIME_INTERVAL;
				if (mT < EXPECT_LIFECYCLE_TIME) {
					curStepTimeInterval = (int) (EXPECT_LIFECYCLE_TIME / (distOfMarquee / speed));
				}
			}
		}
		curSpeed = speed;
		if (DEBUG) {
			log("calculate new info curSpeed:" + curSpeed
					+ " curStepTimeInterval:" + curStepTimeInterval);
		}
	}

	@Override
	protected void onWindowVisibilityChanged(int visibility) {
		super.onWindowVisibilityChanged(visibility);
		if (visibility == View.VISIBLE) {// 恢复状态
			restoreState(frezonSavedStateRecord);
			if (isNeedCheckStartOnVisible) {
				isNeedCheckStartOnVisible = false;
				startMarquee();
			} else if (mAutoStart && !isStarted()) {
				startMarquee();
			}
			if (DEBUG) {
				log("onVisibilityChanged VISIBLE");
			}
		} else {// 保存状态
			frezonSaveState();
			if (DEBUG) {
				log("onVisibilityChanged unVISIBLE");
			}
		}
	}

	private boolean pauseOnPressed = true;

	/**
	 * 设置是否在被touch down的时候暂停，在touch up的时候start(前提是start able)
	 * 
	 * @param enable
	 */
	public void setPauseOnPressed(boolean enable) {
		pauseOnPressed = enable;
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		if (pauseOnPressed) {
			switch (event.getAction()) {
			case MotionEvent.ACTION_DOWN:
				stopMarquee();
				if (DEBUG) {
					log("============onTouch ACTION_DOWN stopMarquee()");
				}
				break;
			case MotionEvent.ACTION_UP:
				startMarquee(false);
				if (DEBUG) {
					log("=============onTouch ACTION_UP startMarquee()");
				}
				break;
			}
		}
		return super.onTouchEvent(event);
	}

	/**
	 * 重置滚动的位置
	 */
	public void resetScrollLocation() {
		orientation = Orientation.To_Header;
		curLeft = defLeft;
		curTop = defTop;
		invalidate();
	}

	public enum Orientation {
		To_Header(0), To_Center(1), To_Foot(2);
		final int value;

		Orientation(int v) {
			value = v;
		}

		public int getValue() {
			return value;
		}
	}

	enum MarqueeState {
		Running,
		/**
		 * 没有可滚动的图片，只是在等待完成一个生命周期
		 */
		Empty_running, Paused, Stoped, One_lifecyle_start_waiting, One_liefcyle_end_waiting,
		/**
		 * view不可见，或者没有被天添加到Window中，之前的运行状态以及被保存，在可见情况下会自动恢复
		 */
		Frozen;
	}

	public boolean startMarquee() {
		return startMarquee(true);
	}

	private boolean isNeedCheckStartOnVisible = false;

	public boolean startMarquee(boolean hopeDelayStart) {
		if (!mMarqueeEnable) {
			return false;
		}
		if (getWindowVisibility() != View.VISIBLE) {
			isNeedCheckStartOnVisible = true;
			return false;
		}
		if (tempSavedStateRecord != null) {
			tempSavedStateRecord = null;
			if (isRunning(tempSavedStateRecord)) {
				// 恢复状态
				restoreState(getRunnableState());
			}
			if (isRunning(curState)) {
				return true;
			}
		}
		if (mDrawable == null || curSpeed <= 0) {
			if (curState == MarqueeState.Empty_running
					&& handler.hasMessages(MESSAGE_MARQUEE_EMPTY_WAIT)) {
				return true;
			}
			// 不滚动图片，空闲等待到指定时间间隔，循环完成生命周期的回调
			updateCurrentState(MarqueeState.Empty_running);
			sendCommandEmptyMarqueeInterval();
		} else {
			if (hopeDelayStart && isAddIntervaleWhenStartAndFinish) {
				if (curState == MarqueeState.One_lifecyle_start_waiting
						&& handler.hasMessages(MESSAGE_MARQUEE_START_WAIT)) {
					return true;
				}
				updateCurrentState(MarqueeState.One_lifecyle_start_waiting);
				sendCommandLifeBeforStartWait();
			} else {
				if (curState == MarqueeState.Running
						&& handler.hasMessages(MESSAGE_MARQUEE_MOVE)) {
					return true;
				}
				updateCurrentState(MarqueeState.Running);
				sendCommandMoveStep();
			}
		}
		return true;
	}

	/**
	 * 获取当前可以运行的状态
	 * 
	 * @return
	 */
	private MarqueeState getRunnableState() {
		if (mDrawable == null || curSpeed <= 0) {
			return MarqueeState.Empty_running;
		} else {
			if (isAddIntervaleWhenStartAndFinish) {
				return MarqueeState.One_lifecyle_start_waiting;
			} else {
				return MarqueeState.Running;
			}
		}
	}

	public boolean stopMarquee() {
		tempSavedStateRecord = getCurrentState();
		removeAllMessage();
		updateCurrentState(MarqueeState.Stoped);
		return true;
	}

	public boolean pauseMarqquee() {
		tempSavedStateRecord = getCurrentState();
		removeAllMessage();
		updateCurrentState(MarqueeState.Paused);
		return true;
	}

	private boolean mMarqueeEnable = true;

	/**
	 * 设置是否可以滚动
	 * 
	 * @param enable
	 *            是false的时候会检查当前是否是Running的状态，如果是会stopMarquee
	 */
	public void setMarqueeEnable(boolean enable) {
		mMarqueeEnable = enable;
		if (!mMarqueeEnable) {
			stopMarquee();
		}
	}

	public boolean getMarqueeEnable() {
		return mMarqueeEnable;
	}

	/**
	 * 在view不可见的时候，保存状态，停止滚动
	 */
	private void frezonSaveState() {
		if (curState == MarqueeState.Frozen) {// 已经保持了
			return;
		}
		frezonSavedStateRecord = curState;
		if (isRunning(curState)) {
			stopMarquee();
		} else {
			removeAllMessage();
		}
		updateCurrentState(MarqueeState.Frozen);
	}

	/**
	 * 尝试恢复之前保存的状态
	 */
	private void restoreState(MarqueeState state) {
		MarqueeState restoredState = state;
		switch (state) {
		case Running:
			sendCommandMoveStep();
			break;
		case Empty_running:
			sendCommandEmptyMarqueeInterval();
			break;
		case Paused:
		case Stoped:
			break;
		case One_lifecyle_start_waiting:
			sendCommandLifeBeforStartWait();
			break;
		case One_liefcyle_end_waiting:
			sendCommandLifeBeforEndWait();
			break;
		}
		updateCurrentState(restoredState);
	}

	/**
	 * 删除所有滚动的信息，重新使用，设置的属性不会改变比如autostart...
	 */
	public void recycle() {
		isRecycleClear = true;
		setImageDrawable(null);
		clearAllRecordAndState();
		isRecycleClear = false;
	}

	public void clearAllRecordAndState() {
		needRecalculateOnLayout = true;
		curSpeed = 0;
		stopMarquee();
		resetScrollLocation();
		removeAllMessage();
		frezonSavedStateRecord = MarqueeState.Stoped;
		tempSavedStateRecord = null;
		hasLifeCircleFinishToInvoke = false;
		isWaitingForFinishLifecycleInvoke = false;
	}

	public MarqueeState getCurrentState() {
		return curState;
	}

	private void updateCurrentState(MarqueeState state) {
		curState = state;
	}

	/**
	 * 移动一步
	 */
	private void moveStep() {
		if (isLeftRightMarquee) {// 左右移动
			curLeft = calculateNewCoord(curLeft);
		} else {// 上下移动
			curTop = calculateNewCoord(curTop);
		}
		invalidate();
		if (hasLifeCircleFinishToInvoke) {// 回调
			hasLifeCircleFinishToInvoke = false;
			if (isWaitingForFinishLifecycleInvoke
					&& handler.hasMessages(MESSAGE_MARQUEE_FINISH_WAIT)) {// 命令已经发出去了，正在等待执行,则继续等待
				if (DEBUG) {
					log("warning: I don't want to see this msg,"
							+ "because this is mean that may has some problems."
							+ "isWaitingForFinishLifecycleInvoke:"
							+ isWaitingForFinishLifecycleInvoke
							+ " hasLifeCircleFinishToInvoke:"
							+ hasLifeCircleFinishToInvoke
							+ " isAddIntervaleWhenStartAndFinish:"
							+ isAddIntervaleWhenStartAndFinish
							+ " isMarqueeAble:" + getMarqueeEnable());
				}
				return;
			} else {
				if (isAddIntervaleWhenStartAndFinish) {
					sendCommandLifeBeforEndWait();
				} else {
					perLifecycle();
					sendCommandMoveStep();
				}
			}
		} else {// 正常滚动
			sendCommandMoveStep();
		}
	}

	/**
	 * 根据当前运动的方向，计算下一步的位置
	 * 
	 * @param cur
	 * @return
	 */
	private float calculateNewCoord(float cur) {
		float newCoord = cur;
		switch (orientation) {
		case To_Header:// 滚动到头部显示
			if (cur < headerCoord) {
				newCoord = cur + curSpeed;
				if (newCoord > headerCoord) {
					newCoord = headerCoord;
					orientation = Orientation.To_Foot;
				}
			} else {
				// 修改方向
				orientation = Orientation.To_Foot;
				newCoord = headerCoord;
			}
			break;
		case To_Foot:// 滚动到尾部显示
			if (cur > footCoord) {
				newCoord = cur - curSpeed;
				if (newCoord < footCoord) {
					newCoord = footCoord;
					orientation = Orientation.To_Center;
				}
			} else {// 修改方向
				orientation = Orientation.To_Center;
				newCoord = footCoord;
			}
			break;
		case To_Center:// 滚动到中间显示
			if (cur >= centerCoord) {// 修改方向
				hasLifeCircleFinishToInvoke = true;
				orientation = Orientation.To_Header;
			} else {
				newCoord = cur + curSpeed;
			}
			break;
		}
		return newCoord;
	}

	private boolean hasLifeCircleFinishToInvoke = false;
	private boolean isWaitingForFinishLifecycleInvoke = false;

	private boolean perLifecycle() {
		if (listener != null) {
			listener.onFinishOneCycle(this);
		}
		hasLifeCircleFinishToInvoke = false;
		isWaitingForFinishLifecycleInvoke = false;
		return false;
	}

	/**
	 * 在开始滚动和完成一个生命周期的时候时候，是否添加指定的时间间隔
	 */
	private boolean isAddIntervaleWhenStartAndFinish = true;

	/**
	 * 是否需要在start和finish one lifecycle的时候需要添加指定的停顿时间
	 * 
	 * @param state
	 */
	public void setAddIntervaleWhenStartAndFinish(boolean state) {
		isAddIntervaleWhenStartAndFinish = state;
	}

	/**
	 * 添加一个空等待的延时Handler的MSG等时间到了算是一个EmptyMarquee的生命周期
	 */
	private void sendCommandEmptyMarqueeInterval() {
		removeAllMessage();
		handler.sendEmptyMessageDelayed(MESSAGE_MARQUEE_EMPTY_WAIT,
				EMPTY_LIFE_CYCLE_INTERVAL);
	}

	/**
	 * 在开始滚动的时候，先等待指定的时间，只在第一次滚动有效
	 */
	private void sendCommandLifeBeforStartWait() {
		handler.removeMessages(MESSAGE_MARQUEE_START_WAIT);
		handler.sendEmptyMessageDelayed(MESSAGE_MARQUEE_START_WAIT,
				LIFE_START_PAUSE_TIME);
	}

	/**
	 * 在一个生命周期完成之前，即完成一个生命周期后在等待指定的时候在继续完成回调
	 */
	private void sendCommandLifeBeforEndWait() {
		handler.removeMessages(MESSAGE_MARQUEE_FINISH_WAIT);
		handler.sendEmptyMessageDelayed(MESSAGE_MARQUEE_FINISH_WAIT,
				LIFE_FINISH_PAUSE_TIME);
		isWaitingForFinishLifecycleInvoke = true;
	}

	/**
	 * 移除Handler的msg
	 */
	private void removeCommandLifeBeforEndWait() {
		handler.removeMessages(MESSAGE_MARQUEE_FINISH_WAIT);
		hasLifeCircleFinishToInvoke = false;
		isWaitingForFinishLifecycleInvoke = false;
	}

	private void sendCommandMoveStep() {
		handler.removeMessages(MESSAGE_MARQUEE_MOVE);
		handler.sendEmptyMessageDelayed(MESSAGE_MARQUEE_MOVE,
				curStepTimeInterval);
	}

	private final Handler handler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case MESSAGE_MARQUEE_MOVE:
				if (curState == MarqueeState.Running) {
					moveStep();
				}
				break;
			case MESSAGE_MARQUEE_EMPTY_WAIT:
				perLifecycle();
				if (curState == MarqueeState.Empty_running) {
					sendCommandEmptyMarqueeInterval();
				}
				if (DEBUG) {
					log("empty wait finish" + " object hascode:"
							+ ImageMarqueeView.this.hashCode());
				}
				break;
			case MESSAGE_MARQUEE_START_WAIT:
				updateCurrentState(MarqueeState.Running);
				moveStep();
				if (DEBUG) {
					log("life start wait end current state:" + curState
							+ " object hascode:"
							+ ImageMarqueeView.this.hashCode());
				}
				break;
			case MESSAGE_MARQUEE_FINISH_WAIT:
				updateCurrentState(MarqueeState.Running);
				moveStep();
				perLifecycle();
				if (DEBUG) {
					log("one lifecycle wait end and perLiefcycle normally current state:"
							+ curState
							+ " object hascode:"
							+ ImageMarqueeView.this.hashCode());
				}
				break;
			}
		}

	};

	/**
	 * 移除所有的命令
	 */
	private void removeAllMessage() {
		handler.removeMessages(MESSAGE_MARQUEE_MOVE);
		handler.removeMessages(MESSAGE_MARQUEE_EMPTY_WAIT);
		handler.removeMessages(MESSAGE_MARQUEE_START_WAIT);
		removeCommandLifeBeforEndWait();
	}

	/**
	 * 计算缩放比例
	 * 
	 * @param bW
	 * @param bH
	 * @param cW
	 * @param cH
	 * @return
	 */
	public float calculateFitScale(int bW, int bH, int cW, int cH) {
		// float fitScale = 1;
		float sw = 1;
		float sh = 1;
		if (bW > 0) {
			sw = (float) cW / bW;
		}
		if (bH > 0) {
			sh = (float) cH / bH;
		}
		if (bW >= cW && bH >= cH) {
			if (sw > sh) {
				return sw;
			} else {
				return sh;
			}
		} else if (bW <= cW && bH <= cH) {
			if (sw > sh) {
				return sw;
			} else {
				return sh;
			}
		} else if (bW < cW) {
			return sw;
		} else {
			return sh;
		}
	}

	/**
	 * 当前滚动到的x
	 * 
	 * @return
	 */
	protected float getCurrentMarqueeX() {
		return curLeft;
	}

	/**
	 * 当前滚动到的y
	 * 
	 * @return
	 */
	protected float getCurrentMarqueeY() {
		return curTop;
	}

	/**
	 * 图片的缩放缩放比例发生改变
	 * 
	 * @param fitScale
	 */
	protected void onFitScaleChanged(float fitScale) {

	}

	private ImageMarqueeListener listener;

	public interface ImageMarqueeListener {
		/**
		 * 完成一个周期
		 */
		public void onFinishOneCycle(ImageMarqueeView view);

		/**
		 * Bitmap，或者Drawable被设置成了Null
		 */
		public void onUpdateDrawable(ImageMarqueeView view,
				Drawable newDrawable, boolean isRecycleClear);

	}

	public void setListener(ImageMarqueeListener l) {
		listener = l;
	}

	/**
	 * 是否自动启动滚动
	 * 
	 * @param autoStart
	 */
	public void setAutoStart(boolean autoStart) {
		if (mAutoStart == autoStart) {
			return;
		} else {
			if (autoStart) {// 尝试启动
				if (getCurrentState() != MarqueeState.Running)
					startMarquee();
			} else {// 尝试停止
				stopMarquee();
			}
			mAutoStart = autoStart;
		}
	}

	/**
	 * 当前是否已经启动了
	 * 
	 * @return
	 */
	public boolean isStarted() {
		return isRunning(curState);
	}

	public boolean isRunning(MarqueeState state) {
		if (state == MarqueeState.Running
				|| state == MarqueeState.Empty_running
				|| state == MarqueeState.One_liefcyle_end_waiting
				|| state == MarqueeState.One_lifecyle_start_waiting) {
			return true;
		} else {
			return false;
		}
	}

	/**
	 * 是否自动启动滚动
	 * 
	 * @return
	 */
	public boolean isAutoStart() {
		return mAutoStart;
	}

	void log(String msg) {
		Log.d(TAG, "" + msg);
	}

}