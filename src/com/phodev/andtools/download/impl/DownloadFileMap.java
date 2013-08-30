package com.phodev.andtools.download.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.phodev.andtools.download.DownloadFile;

/**
 * 下载记录在内存中的管理器
 * 
 * @author skg
 * 
 */
public class DownloadFileMap {
	private List<DownloadFile> loadingFiles = new ArrayList<DownloadFile>();
	private List<DownloadFile> loadCompleteFiles = new ArrayList<DownloadFile>();
	private Map<String, DownloadFile> allFiles = new ConcurrentHashMap<String, DownloadFile>();

	/**
	 * 更新数据源
	 * 
	 * @param loading
	 * @param loadComplete
	 */
	public void update(List<DownloadFile> loading,
			List<DownloadFile> loadComplete) {
		if (loading != loadingFiles) {
			loadingFiles.clear();
			if (loading != null && !loading.isEmpty()) {
				loadingFiles.addAll(loading);
			}
		}
		if (loadComplete != loadCompleteFiles) {
			loadCompleteFiles.clear();
			if (loadComplete != null && !loadComplete.isEmpty()) {
				loadCompleteFiles.addAll(loadComplete);
			}
		}
		refreshAllFilesMaping();
	}

	public void refreshAllFilesMaping() {
		allFiles.clear();
		append(loadingFiles, allFiles);
		append(loadCompleteFiles, allFiles);
	}

	private void append(List<DownloadFile> source,
			Map<String, DownloadFile> target) {
		if (source == null || target == null) {
			return;
		}
		int len = source.size();
		for (int i = 0; i < len; i++) {
			DownloadFile f = source.get(i);
			if (f != null) {
				String key = f.getSourceUrl();
				if (key != null) {
					target.put(key, f);
				}
			}
		}
	}

	/**
	 * 添加一个记录
	 * 
	 * @param file
	 */
	public void addFile(DownloadFile file) {
		if (file == null) {
			return;
		}
		String key = file.getSourceUrl();
		allFiles.put(key, file);
		switch (file.getStatus()) {
		case DownloadFile.status_download_complete:
			loadCompleteFiles.add(file);
			break;
		// case DownloadFile.status_download_loading:
		default:// 其他情况都添加到loading
			loadingFiles.add(file);
			break;
		}
	}

	/**
	 * 移除一个记录
	 */
	public DownloadFile removeFile(String sourceUrl) {
		if (sourceUrl == null) {
			return null;
		}
		DownloadFile file = allFiles.remove(sourceUrl);
		if (file == null) {
			return null;
		}
		switch (file.getStatus()) {
		case DownloadFile.status_download_complete:
			if (!loadCompleteFiles.remove(file)) {
				loadingFiles.remove(file);
			}
			break;
		// case DownloadFile.status_download_loading:
		default:// 其他情況都从loading总尝试删除
			if (!loadingFiles.remove(file)) {
				loadCompleteFiles.remove(file);
			}
			break;
		}
		return file;
	}

	public void onFileStatusChanged(DownloadFile file) {
		if (file == null) {
			return;
		}
		switch (file.getStatus()) {
		case DownloadFile.status_download_complete:
			loadingFiles.remove(file);
			if (!loadCompleteFiles.contains(file)) {
				loadCompleteFiles.add(file);
			}
			break;
		default:
			loadCompleteFiles.remove(file);
			if (!loadingFiles.contains(file)) {
				loadingFiles.add(file);
			}
			break;
		}
	}

	public DownloadFile get(String sourceUrl) {
		return allFiles.get(sourceUrl);
	}

	public List<DownloadFile> getLoadingFiles() {
		return loadingFiles;
	}

	public List<DownloadFile> getLoadCompleteFiles() {
		return loadCompleteFiles;
	}
}
