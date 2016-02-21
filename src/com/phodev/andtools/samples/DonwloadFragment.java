package com.phodev.andtools.samples;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.RemoteException;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import com.phodev.andtools.R;
import com.phodev.andtools.common.SimpleDesc;
import com.phodev.andtools.download.DownloadFile;
import com.phodev.andtools.download.DownloadService;
import com.phodev.andtools.download.ICallback;
import com.phodev.andtools.download.IDownloadManager;
import com.phodev.andtools.samples.inner.InnerFragment;
import com.phodev.andtools.utils.CommonUtils;

@SimpleDesc(title = "Download")
public class DonwloadFragment extends InnerFragment implements OnClickListener {
	private final static String TAG = "DonwloadFragment";

	@Override
	protected int obtainInitLayoutResId() {
		return R.layout.fragment_download;
	}

	private IDownloadManager downloadManager;
	private boolean isServiceBind = Boolean.FALSE;
	//
	private EditText urlBox;
	private View loadBtn;
	private ListView listview;
	private TextView stateTv;

	@Override
	protected void initWidget() {
		listview = (ListView) findViewById(R.id.listview);
		urlBox = (EditText) findViewById(R.id.download_url);
		loadBtn = findViewById(R.id.btn_start_download);
		loadBtn.setOnClickListener(this);
		//
		if (!isServiceBind || downloadManager == null) {
			Intent intent = new Intent(getActivity(), DownloadService.class);
			getActivity().bindService(intent, conn, Context.BIND_AUTO_CREATE);
		}
		//
		String durl = "http://zanasi.chem.unisa.it/download/C.pdf";
		urlBox.setText(durl);
		stateTv = (TextView) findViewById(R.id.download_state);
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		getActivity().unbindService(conn);
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.btn_start_download:
			if (startLoad(urlBox.getText().toString())) {
				urlBox.setText(null);
			}
			break;
		}
	}

	private boolean startLoad(String url) {
		if (url == null || url.length() <= 0) {
			CommonUtils.toast(getActivity(), "invalid url");
			return false;
		}
		if (!isServiceBind || downloadManager == null) {
			CommonUtils.toast(getActivity(), "download service is unbind");
			return false;
		}
		try {
			downloadManager.start(url);
		} catch (RemoteException e) {
			e.printStackTrace();
			CommonUtils.toast(getActivity(), "start failed:" + e.toString());
			return false;
		}
		return true;
	}

	IDownloadManager manager;
	private ServiceConnection conn = new ServiceConnection() {

		@Override
		public void onServiceDisconnected(ComponentName name) {
			isServiceBind = Boolean.FALSE;
			downloadManager = null;
		}

		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {
			isServiceBind = Boolean.TRUE;
			downloadManager = IDownloadManager.Stub.asInterface(service);
			try {
				downloadManager.registerICallback(iCallback);
			} catch (RemoteException e) {
				e.printStackTrace();
			}
		}
	};

	private ICallback iCallback = new ICallback.Stub() {
		@Override
		public void onProgress(String url, int total, int loaded, int speed) throws RemoteException {
			sendState("------Loaded : " + (loaded / 1024) + "/" + (total / 1024) + "kb" + "\n        Speed : "
					+ speed / 1024 + "kb/s");
		}

		@Override
		public void onDownloadSuccess(String url) throws RemoteException {
			sendState("DownloadSuccess");
		}

		@Override
		public void onDownloadFileStatusChanged(DownloadFile file) throws RemoteException {
			sendState("onDownloadFileStatusChanged,status:" + file.getStatus());
		}

		@Override
		public void onDownloadFileAdd(DownloadFile file) throws RemoteException {
			sendState("Download task created,connecting...");
		}

		@Override
		public void onDownloadFailed(String url, int curStatus, int errorCode) throws RemoteException {
			sendState("DownloadFailed,Status:" + curStatus + ",ErrorCode:" + errorCode);
		}

		@Override
		public void onDownloadFileRemove(DownloadFile file, boolean removedFromDisk) throws RemoteException {
			// TODO Auto-generated method stub

		}

	};

	private void sendState(String msgStr) {
		Message msg = handler.obtainMessage(what_download_state);
		msg.obj = msgStr;
		msg.sendToTarget();
	}

	private final static int what_download_state = 100;
	Handler handler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			if (msg.what == what_download_state) {
				stateTv.setText(msg.obj + "\n" + stateTv.getText().toString());
			}
		}

	};

	void log(String msg) {
		Log.d(TAG, "" + msg);
	}
}
