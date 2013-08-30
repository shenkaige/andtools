package com.phodev.andtools.download;

import java.io.File;

import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

/**
 * 下载文件描述信息
 * 
 * @author skg
 */
public class DownloadFile implements Parcelable {
	public final static int status_download_complete = 1;
	public final static int status_download_loading = 2;
	public final static int status_download_paused = 3;
	public final static int status_download_error = 4;
	private String sourceUrl;
	private String fileName;
	private int status = status_download_paused;// default
	private long fileSize;
	private long loadedSize;

	public DownloadFile() {
	}

	private DownloadFile(Parcel in) {
		readFromParcel(in);
		if (Constants.DEBUG) {
			Log.d("DownloadFile", "new DownloadFile with parcel :" + hashCode());
		}
	}

	public void readFromParcel(Parcel in) {
		sourceUrl = in.readString();
		fileName = in.readString();
		status = in.readInt();
		fileSize = in.readLong();
		loadedSize = in.readLong();
	}

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeString(sourceUrl);
		dest.writeString(fileName);
		dest.writeInt(status);
		dest.writeLong(fileSize);
		dest.writeLong(loadedSize);
		if (Constants.DEBUG) {
			Log.d("DownloadFile", "writeToParcel  :" + hashCode());
		}
	}

	public static final Parcelable.Creator<DownloadFile> CREATOR = new Parcelable.Creator<DownloadFile>() {
		public DownloadFile createFromParcel(Parcel in) {
			return new DownloadFile(in);
		}

		public DownloadFile[] newArray(int size) {
			return new DownloadFile[size];
		}
	};

	public String getSourceUrl() {
		return sourceUrl;
	}

	public void setSourceUrl(String sourceUrl) {
		this.sourceUrl = sourceUrl;
	}

	public String getFileName() {
		return fileName;
	}

	public void setFileName(String fileName) {
		if (this.fileName != null && fileName != null
				&& this.fileName.equals(fileName)) {
			return;
		}
		this.fileName = fileName;
		file = null;
	}

	private File file;

	public File getFile() {
		if (fileName == null) {
			file = null;
			return null;
		}
		if (file != null) {
			return file;
		}
		file = Utils.createDownloadOutFile(fileName);
		return file;
	}

	public int getStatus() {
		return status;
	}

	public void setStatus(int status) {
		this.status = status;
	}

	public long getFileSize() {
		return fileSize;
	}

	public void setFileSize(long fileSize) {
		this.fileSize = fileSize;
	}

	public long getLoadedSize() {
		return loadedSize;
	}

	public void setLoadedSize(long loadedSize) {
		this.loadedSize = loadedSize;
	}

	@Override
	public String toString() {
		return "DownloadFile [sourceUrl=" + sourceUrl + ", fileName="
				+ fileName + ", status=" + status + ", fileSize=" + fileSize
				+ ", loadedSize=" + loadedSize + ", file=" + file + "]";
	}

}
