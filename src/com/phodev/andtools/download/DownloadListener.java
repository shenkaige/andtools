package com.phodev.andtools.download;

/**
 * 下载任务监听器
 * 
 * @author skg
 * 
 */
public interface DownloadListener extends ICallback {
	/**
	 * 下载任务状态发生改变
	 * 
	 * @param file
	 */
	public void onDownloadFileStatusChanged(DownloadFile file);

	/**
	 * 添加新的下载任务
	 * 
	 * @param file
	 */
	public void onDownloadFileAdd(DownloadFile file);

	/**
	 * 下载任务被移除
	 * 
	 * @param file
	 */
	public void onDownloadFileRemove(DownloadFile file);

	/**
	 * 下载任务进度的变化(单位都是byte)
	 * 
	 * @param url
	 * @param total
	 *            文件总大小
	 * @param loaded
	 *            已经加载到的大小
	 * @param speed
	 *            当前的速度
	 */
	public void onProgress(String url, long total, long loaded, int speed);

	/**
	 * 下载成功
	 * 
	 * @param url
	 */
	public void onDownloadSuccess(String url);

	/**
	 * 下载失败
	 * 
	 * @param url
	 * @param curStatus
	 * @param errorCode
	 */
	public void onDownloadFailed(String url, int curStatus, int errorCode);
}
