package com.phodev.andtools.viewpage;

import java.util.List;
import java.util.Map;

/**
 * 分页页面数据提供者
 * 
 * @author skg
 * 
 * @param <Data>
 */
public interface PageDataProvider<Data> {

	/**
	 * 获取数据的其实位置
	 * 
	 * @param pageIndex
	 * @return
	 */
	public int getDataStart(int pageIndex);

	/**
	 * 获取数据的技术位置
	 * 
	 * @param pageIndex
	 * @return
	 */
	public int getDataEnd(int pageIndex);

	/**
	 * globalPosition是在整个数据中的位置
	 * 
	 * @param globalPosition
	 * @return
	 */
	public Data getData(int globalPosition);

	/**
	 * 获取数据
	 * 
	 * @param pageIndex
	 *            第几页的数据
	 * @param positionInPage
	 *            第pageIndex页中的第positionInPage几个数据
	 * @return
	 */
	public Data getData(int pageIndex, int positionInPage);

	/**
	 * 获取所有数据
	 * 
	 * @return
	 */
	public List<Data> getAllData();

	/**
	 * 拷贝指定的数据
	 * 
	 * @param array
	 * @param start
	 * @param end
	 * @return
	 */
	public int copyData(List<Data> array, int start, int end);

	/**
	 * 拷贝指定的数据
	 * 
	 * @param array
	 * @param pageIndex
	 * @return
	 */
	public int copyPageData(List<Data> array, int pageIndex);

	/**
	 * 获取总更的数据长度
	 * 
	 * @return
	 */
	public int getTotalDataSize();

	/**
	 * 获取一页内显示的Item数量
	 * 
	 * @return
	 */
	public int getDefaultPerPageItemCount();

	/**
	 * 获取指定页面内显示的数量
	 * 
	 * @param pageIndex
	 * @return
	 */
	public int getPageItemCount(int pageIndex);

	/**
	 * 获取总更的页面数量
	 * 
	 * @return
	 */
	public int getPageCount();

	/**
	 * 指定特殊页面位置需要的item数量,优先级高于{@code #getPageItemCount()}
	 * 
	 * <pre>
	 * Map:
	 * key 1:value 5-->第一页需要最多5个
	 * key 10:value 2-->第10页最多2个
	 * </pre>
	 * 
	 * @param refer
	 * @return
	 */
	public void setSpecificPerPageItemCount(Map<Integer, Integer> refer);

	/**
	 * 设置Header Page的item的count数量优先级高于{@code #getPageItemCount()}
	 * 
	 * @param count
	 */
	public void setFirstPageSpecificItemCount(int count);
}
