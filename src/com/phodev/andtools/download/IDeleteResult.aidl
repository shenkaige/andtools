package com.phodev.andtools.download;
import com.phodev.andtools.download.DownloadFile;

	interface IDeleteResult {
		/**
		 * on file 
		 * 
		 * @param df
		 * @param result
		 * @param total
		 * @param index
		 */
		void onDeleteResult(in DownloadFile df, boolean success,int total, int index);
	}