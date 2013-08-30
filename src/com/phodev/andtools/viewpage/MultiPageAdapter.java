package com.phodev.andtools.viewpage;

import android.support.v4.view.PagerAdapter;
import android.view.View;
import android.view.ViewGroup;

import com.phodev.andtools.viewpage.MultiPageView.OnFetchSizeListener;

/**
 * 多页面Adapter
 * 
 * @author skg
 * 
 */
public abstract class MultiPageAdapter implements OnFetchSizeListener {
	private PagerAdapter pagerAdapter;

	public abstract int getCount();

	protected void setDataSetObserverMapping(PagerAdapter pagerAdapter) {
		this.pagerAdapter = pagerAdapter;
	}

	public void notifyDataSetChanged() {
		if (pagerAdapter != null) {
			pagerAdapter.notifyDataSetChanged();
		}
	}

	public int getItemViewType(int position) {
		return 0;
	}

	public int getViewTypeCount() {
		return 1;
	}

	public abstract View getPageView(int position, View convertView,
			ViewGroup parent);

	/**
	 * 在页面生成好准备提交显示的时候询问是否需要再次包装
	 * 
	 * @param needWrappage
	 * @param pageIndex
	 * @return
	 */
	protected View onWrapPage(View needWrappage, int pageIndex) {
		return needWrappage;
	}

	protected void onPageRecyle(View page) {

	}

	/**
	 * 在页面被销毁的时候询问是否有包转过的需要卸载包转，否则可能会重用的时候报错
	 * 
	 * @param wrapedPage
	 * @param pageIndex
	 */
	protected View onUnwrapPage(View wrapedPage, int pageIndex) {
		return wrapedPage;
	}

	@Override
	public void onFetchSize(int pageWidth, int pageHeight) {

	}

}
