package com.phodev.andtools.drag;

import android.content.Context;
import android.util.Log;

import com.phodev.andtools.common.CommonParam;

/**
 * 拖动的配置
 * 
 * @author skg
 * 
 */
public class DragConfig {
	private DragConfig() {
	}

	public static boolean DEBUG = CommonParam.DEBUG;
	public static final int CELL_MOVE_ANIMATION_DURATION = 500;
	// 在移动跨页的时候需要等待确认的最小时间
	public static final int MOVE_PAGE_CONFIRM_TIME = 800;
	// 如果距离左右其中一边的绝对距离小于这个尺寸，则开始检查跨页
	private static int move_page_min_boundary_def = 30;// dp
	public static int CELL_STATUS = CellModel.CELL_STATUS_NORMAL;
	public static final int CELL_EDIT_ALPHA = 180;
	public static final int GRID_DEFAULT_COLUMNS = 3;

	// need config file
	private static int cell_content_margging = 7;// dp

	public static void debugCellLayout(Object cellLayout, String tag, String msg) {
		if (cellLayout != null && cellLayout instanceof CellLayout) {
			CellLayout cll = (CellLayout) cellLayout;
			Log.d(tag, "drag_debug->msg:" + msg);
		} else {
			Log.d(tag, "drag_debug->msg:" + msg + "->" + cellLayout);
		}
	}

	private static boolean inited = false;

	public static void initConfig(Context context) {
		if (inited || context == null) {
			return;
		}
		inited = true;
		float density = context.getResources().getDisplayMetrics().density;
		move_page_min_boundary_def = change(density, move_page_min_boundary_def);
		cell_content_margging = change(density, cell_content_margging);

	}

	public static int getCellContentMargging() {
		return cell_content_margging;
	}

	public static int getMovePageMinBoundary() {
		return move_page_min_boundary_def;
	}

	private static int change(float density, int value) {
		return (int) (value * density + 0.5f);
	}
}
