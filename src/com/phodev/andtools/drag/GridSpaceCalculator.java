package com.phodev.andtools.drag;

public class GridSpaceCalculator {
	private int mFitItemWith;
	private int mFitRows;
	private int mMaxW;
	private int mMaxH;
	private int mGridViewW;
	private int mGridViewH;
	// Horizontal Vertical Max Ratio
	private final float max_v_h_space_ratio = 1.8f;

	//
	boolean useHorizontalPadding = false;
	boolean useVerticlePadding = false;

	public void setBound(int maxW, int maxH, int maxColumns, int hopeItemSpace,
			boolean useHorizontalPadding, boolean useVerticlePadding) {
		this.useHorizontalPadding = useHorizontalPadding;
		this.useVerticlePadding = useVerticlePadding;
		mMaxW = maxW;
		mMaxH = maxH;
		if (maxColumns <= 0) {
			throw new RuntimeException("maxColumns must > 0.maxColumns="
					+ maxColumns);
		}
		if (maxW <= 0 || maxH <= 0) {
			throw new RuntimeException("maxW and maxH must all > 0. maxW="
					+ maxW + " maxH=" + maxH);
		}
		int fitItemSize;
		int rawColumnW = maxW / maxColumns;
		int hopeRows = maxH / rawColumnW;
		int fitRows = getFitRows(rawColumnW, hopeRows, maxH);
		int rawRowHeight = 0;
		if (fitRows > 0) {
			rawRowHeight = maxH / fitRows;
		}
		if (rawRowHeight > rawColumnW) {
			fitItemSize = rawColumnW;
		} else {
			fitItemSize = rawRowHeight;
		}
		mHorizontalSpace = getSpace(maxW, fitItemSize, maxColumns,
				useHorizontalPadding);
		mVerticleSpace = getSpace(maxH, fitItemSize, fitRows,
				useVerticlePadding);
		if (mHorizontalSpace < hopeItemSpace || mVerticleSpace < hopeItemSpace) {
			fitItemSize -= hopeItemSpace;
			mHorizontalSpace = getSpace(maxW, fitItemSize, maxColumns,
					useHorizontalPadding);
			mVerticleSpace = getSpace(maxH, fitItemSize, fitRows,
					useVerticlePadding);
		}
		float ratio = 0;
		if (mHorizontalSpace > mVerticleSpace) {
			ratio = mHorizontalSpace / (float) mVerticleSpace;
		} else {
			ratio = mVerticleSpace / (float) mHorizontalSpace;
		}
		if (ratio > max_v_h_space_ratio && ratio > 0) {
			// 需要对V H Space做调整
			if (mHorizontalSpace > mVerticleSpace) {
				mHorizontalSpace = (int) (mHorizontalSpace / ratio);
			} else {
				mVerticleSpace = (int) (mVerticleSpace / ratio);
			}
		}
		//
		mFitItemWith = fitItemSize;
		mFitRows = fitRows;
		//
		mGridViewH = calculateGridHeight(fitRows);
		mGridViewW = calculateGridWidth(maxColumns);
		mCastoffHeight = mMaxH - mGridViewH;
		mCastoffWidth = mMaxW - mGridViewW;
	}

	private int getSpace(int totalSize, int itemSize, int count,
			boolean usePadding) {
		int spaceCount = count;
		if (usePadding) {
			spaceCount++;
		} else {
			spaceCount--;
		}
		if (count <= 0 || spaceCount <= 0) {
			return 0;
		}
		return (totalSize - itemSize * count) / spaceCount;
	}

	/**
	 * 获取GridView的高度
	 * 
	 * @return
	 */
	public int calculateGridHeight(int rows) {
		if (rows <= 0) {
			rows = 1;
		}
		if (useVerticlePadding) {
			return getColumnWidth() * rows + (rows + 1) * mVerticleSpace;
		} else {
			return getColumnWidth() * rows + (rows - 1) * mVerticleSpace;
		}
	}

	/**
	 * 获取GridView的宽度
	 * 
	 * @return
	 */
	public int calculateGridWidth(int columns) {
		if (columns <= 0) {
			columns = 1;
		}
		if (useHorizontalPadding) {
			return getColumnWidth() * columns + (columns + 1) * mVerticleSpace;
		} else {
			return getColumnWidth() * columns + (columns - 1) * mVerticleSpace;
		}
	}

	/**
	 * 获取ColumnWidth
	 * 
	 * @return
	 */
	public int getColumnWidth() {
		return mFitItemWith;
	}

	private int mHorizontalSpace;
	private int mVerticleSpace;

	public int getItemHorizontalSpace() {
		return mHorizontalSpace;
	}

	public int getItemVerticleSpace() {
		return mVerticleSpace;
	}

	public int getFitRows() {
		return mFitRows;
	}

	private int mCastoffWidth;
	private int mCastoffHeight;

	/**
	 * 获取通过计算之后，并没有被使用的剩余的宽度
	 * 
	 * @return
	 */
	public int getCastoffWidth() {
		return mCastoffWidth;
	}

	/**
	 * 获取通过计算之后，并没有被使用的剩余的高度
	 * 
	 * @return
	 */
	public int getCastoffHeight() {
		return mCastoffHeight;
	}

	/**
	 * 递归计算合适的行数
	 * 
	 * @param referW
	 * @param hopeRows
	 * @param maxH
	 * @return
	 */
	private int getFitRows(int referW, int hopeRows, int maxH) {
		int rows = hopeRows;
		while (true) {
			if (maxH - rows * referW > (referW / 2)) {
				// 加一行
				rows++;
			} else {
				return rows;
			}
		}
	}
}
