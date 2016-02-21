package com.phodev.andtools.download;

import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

/**
 * 下载文件描述信息
 * 
 * @author skg
 */
public class DownloadFile implements Parcelable {
	public final static int STATUS_DOWNLOAD_WAIT = 0;
	public final static int STATUS_DOWNLOAD_COMPLETE = 1;
	public final static int STATUS_DOWNLOAD_LOADING = 2;
	public final static int STATUS_DOWNLOAD_PAUSED = 3;
	public final static int STATUS_DOWNLOAD_ERROR = 4;

	private String sourceUrl;
	private String realUrl;
	private String fileName;
	private int status = STATUS_DOWNLOAD_WAIT;// default
	private int fileSize;
	private int loadedSize;
	private long createtime;
	// apk need
	private String thumbUri;
	private String title;

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
		fileSize = in.readInt();
		loadedSize = in.readInt();
		//
		thumbUri = in.readString();
		realUrl = in.readString();
		title = in.readString();
		createtime = in.readLong();
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
		dest.writeInt(fileSize);
		dest.writeInt(loadedSize);
		//
		dest.writeString(realUrl);
		dest.writeString(thumbUri);
		dest.writeString(title);
		dest.writeLong(createtime);
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
		if (this.fileName != null && fileName != null && this.fileName.equals(fileName)) {
			return;
		}
		this.fileName = fileName;
	}

	public String getFilePath() {
		return Utils.createDownloadOutFilePath(getFileName());
	}

	public int getStatus() {
		return status;
	}

	public void setStatus(int status) {
		this.status = status;
	}

	public int getFileSize() {
		return fileSize;
	}

	public void setFileSize(int fileSize) {
		this.fileSize = fileSize;
	}

	public int getLoadedSize() {
		return loadedSize;
	}

	public void setLoadedSize(int loadedSize) {
		this.loadedSize = loadedSize;
	}

	public String getThumbUri() {
		return thumbUri;
	}

	public void setThumbUri(String thumbUri) {
		this.thumbUri = thumbUri;
	}

	public String getRealUrl() {
		return realUrl;
	}

	public void setRealUrl(String realUrl) {
		this.realUrl = realUrl;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public long getCreatetime() {
		return createtime;
	}

	public void setCreatetime(long createtime) {
		this.createtime = createtime;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((sourceUrl == null) ? 0 : sourceUrl.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		DownloadFile other = (DownloadFile) obj;
		if (sourceUrl == null) {
			if (other.sourceUrl != null)
				return false;
		} else if (!sourceUrl.equals(other.sourceUrl))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "DownloadFile [sourceUrl=" + sourceUrl + ", realUrl=" + realUrl + ", fileName=" + fileName + ", status="
				+ status + ", fileSize=" + fileSize + ", loadedSize=" + loadedSize + ", createtime=" + createtime
				+ ", thumbUri=" + thumbUri + ", title=" + title + "]";
	}

}
