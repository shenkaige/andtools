package com.phodev.andtools.drag;

import android.content.Context;
import android.graphics.Canvas;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import com.phodev.andtools.R;

/**
 * 统一Cell的行为
 * 
 * @author skg
 * 
 */
public class CellLayout extends FrameLayout {
	private int mPageIndex;
	private int mPositionInPage;
	private int mGloablePosition;
	private CellModel cellModel;
	private View mContentView;
	private View closeBtn;

	public CellLayout(Context context) {
		super(context);
		LayoutInflater.from(context).inflate(R.layout.templet_cell_layout,
				this, true);
		closeBtn = findViewById(R.id.cell_layout_remove);
		closeBtn.setVisibility(View.GONE);
		closeBtn.setOnClickListener(mOnClickListener);
		setChildrenDrawingOrderEnabled(true);
	}

	@Override
	protected int getChildDrawingOrder(int childCount, int i) {
		int index = indexOfChild(closeBtn);
		if (index == -1)
			return i;
		else if (i == childCount - 1)
			return index;
		else if (i >= index)
			return i + 1;
		return i;
	}

	public int getPageIndex() {
		return mPageIndex;
	}

	public void setPageIndex(int pageIndex) {
		mPageIndex = pageIndex;
	}

	public int getPositionInPage() {
		return mPositionInPage;
	}

	public void setPositionInPage(int positionInPage) {
		mPositionInPage = positionInPage;
	}

	public int getGloablePosition() {
		return mGloablePosition;
	}

	public void setGloablePosition(int gloablePosition) {
		mGloablePosition = gloablePosition;
	}

	public CellModel getCellModel() {
		return cellModel;
	}

	public void setCellModel(CellModel cellModel) {
		this.cellModel = cellModel;
	}

	public View getContentView() {
		return mContentView;
	}

	public void setContentView(View contentView) {
		if (mContentView == contentView) {
			return;
		} else if (contentView == null) {
			mContentView = null;
			removeView(mContentView);
		} else {
			removeView(mContentView);
			mContentView = contentView;
			addView(contentView);
			ViewGroup.LayoutParams lp = mContentView.getLayoutParams();
			MarginLayoutParams mlp;
			if (lp instanceof MarginLayoutParams) {
				mlp = (MarginLayoutParams) lp;
			} else {
				mlp = generateDefaultLayoutParams();
				mContentView.setLayoutParams(mlp);
			}
			int cellMarging = DragConfig.getCellContentMargging();
			mlp.leftMargin = mlp.topMargin = mlp.rightMargin = mlp.bottomMargin = cellMarging;
		}
	}

	private View mTagView;

	/**
	 * 添加一个标签View
	 * 
	 * @param view
	 */
	public void setTagView(View view) {
		if (view == null) {
			return;
		}
		if (view == mTagView) {
			return;
		}
		removeTagView();
		mTagView = view;
		addView(mTagView);
	}

	public View getTagView() {
		return mTagView;
	}

	public View removeTagView() {
		if (mTagView == null) {
			return null;
		}
		View view = mTagView;
		mTagView = null;
		removeView(view);
		return view;
	}

	private OnClickListener mOnClickListener = new OnClickListener() {
		@Override
		public void onClick(View v) {
			if (mCellActionListener != null) {
				mCellActionListener.onCellRequestRemove(CellLayout.this);
			}
		}
	};

	private CellActionListener mCellActionListener;

	public void setCellActionListener(CellActionListener listener) {
		mCellActionListener = listener;
	}

	public interface CellActionListener {
		public void onCellRequestRemove(CellLayout cell);
	}

	public void onAttach(ViewGroup parent) {
		if (mOnAttachChangedListener != null) {
			mOnAttachChangedListener.onAttachChanged(this, parent, true);
		}
	}

	public void onDetach(ViewGroup parent) {
		if (mOnAttachChangedListener != null) {
			mOnAttachChangedListener.onAttachChanged(this, parent, false);
		}
	}

	public interface OnAttachChangedListener {
		/**
		 * @param status
		 *            true 是Attach false detach
		 */
		public void onAttachChanged(CellLayout cell, ViewGroup parent,
				boolean status);
	}

	private OnAttachChangedListener mOnAttachChangedListener;

	public void setOnAttachChangedListener(OnAttachChangedListener listener) {
		mOnAttachChangedListener = listener;
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		int childCount = getChildCount();
		for (int i = 0; i < childCount; i++) {
			View v = getChildAt(i);
			if (v != closeBtn && v != mTagView) {
				ViewGroup.LayoutParams lp = v.getLayoutParams();
				if (lp != null) {
					lp.height = LayoutParams.MATCH_PARENT;
					lp.width = LayoutParams.MATCH_PARENT;
				}
			}
		}
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
	}

	@Override
	protected void dispatchDraw(Canvas canvas) {
		dispatchDraw(canvas,
				DragConfig.CELL_STATUS == CellModel.CELL_STATUS_EDIT);
	}

	public void drawDraggingFace(Canvas canvas) {
		dispatchDraw(canvas, false);
	}

	private void dispatchDraw(Canvas canvas, boolean isEditModel) {
		if (isEditModel) {
			if (cellModel != null && cellModel.isDeletable()) {
				if (closeBtn.getVisibility() != View.VISIBLE) {
					closeBtn.setVisibility(View.VISIBLE);
				}
			}
			int c_count = canvas.saveLayerAlpha(0, 0, getWidth(), getHeight(),
					DragConfig.CELL_EDIT_ALPHA,
					Canvas.HAS_ALPHA_LAYER_SAVE_FLAG);
			super.dispatchDraw(canvas);
			canvas.restoreToCount(c_count);
		} else {
			if (closeBtn.getVisibility() != View.GONE) {
				closeBtn.setVisibility(View.GONE);
			}
			super.dispatchDraw(canvas);
		}
	}
}
