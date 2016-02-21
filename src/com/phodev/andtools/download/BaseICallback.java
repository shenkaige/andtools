package com.phodev.andtools.download;

import android.os.RemoteException;

public abstract class BaseICallback extends ICallback.Stub {

	@Override
	public void onDownloadFileAdd(DownloadFile file) throws RemoteException {
	}

	@Override
	public void onDownloadFileRemove(DownloadFile file, boolean removedFromDisk) throws RemoteException {
	}

	@Override
	public void onDownloadSuccess(String url) throws RemoteException {
	}

	@Override
	public void onDownloadFailed(String url, int curStatus, int errorCode) throws RemoteException {
	}

}
