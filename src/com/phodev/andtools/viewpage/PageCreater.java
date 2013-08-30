package com.phodev.andtools.viewpage;

import android.view.View;
import android.view.ViewGroup;

/**
 * 分页页面View创建接口
 * 
 * @author skg
 * 
 * @param <DataType>
 */
public interface PageCreater<DataType> {

	/**
	 * 初始化PageCreater,提供以下信息一遍合理穿件Page的尺寸
	 * 
	 * @param maxW
	 * @param maxH
	 * @param perPageCount
	 */
	public void configPage(int maxW, int maxH, int perPageCount);

	/**
	 * 获取页面的容量,建议在{@code #configPage(int, int, int)}之后获取
	 * 
	 * @return
	 */
	public int getPageCapacity();

	/**
	 * 生成页面
	 * 
	 * @param pageIndex
	 *            第几页
	 * @param convertView
	 *            可复用的view
	 * @param parent
	 * @param paging
	 *            页面数据提供者
	 * @return
	 */
	public View createrPage(int pageIndex, View convertView, ViewGroup parent,
			PageDataProvider<DataType> paging);

	public void onDataChanged(PageDataProvider<DataType> paging);

	public void onPageRecycle(View page);
}
