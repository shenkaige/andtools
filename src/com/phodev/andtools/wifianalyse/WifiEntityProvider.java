package com.phodev.andtools.wifianalyse;

import java.util.List;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Handler;
import android.os.Handler.Callback;
import android.os.Message;
import android.util.Log;

public class WifiEntityProvider {
	private static final String TAG = "WifiEntityProvider";
	private static final boolean DEBUG = true;
	private WifiManager mWifiManager;
	private Context context;

	public WifiEntityProvider(Context context) {
		this.context = context;
		mWifiManager = (WifiManager) context
				.getSystemService(Context.WIFI_SERVICE);
	}

	// -------------------------------------------------
	private final long min_scan_interval = 1500L;
	private long lastScanTime;
	private BroadcastReceiver mReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			final String ac = intent.getAction();
			if (ac.equals(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION)) {
				Message msg = handler.obtainMessage();
				msg.obj = mWifiManager.getScanResults();
				handler.sendMessage(msg);
			}
		}
	};

	private Callback handlerCallback = new Callback() {

		@SuppressWarnings("unchecked")
		@Override
		public boolean handleMessage(Message msg) {
			List<ScanResult> scanResultList = null;
			if (msg != null && msg.obj != null) {
				try {
					scanResultList = (List<ScanResult>) msg.obj;
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			perScanFinish(scanResultList);
			return true;
		}
	};
	private Handler handler = new Handler(handlerCallback);
	//
	private Runnable delayScanRunnable = new Runnable() {
		@Override
		public void run() {
			if (isRunning) {
				mWifiManager.startScan();
			}
		}
	};

	private void perScanFinish(List<ScanResult> scanResults) {
		if (mWifiEntityHolder != null) {
			mWifiEntityHolder.onWifiEntitysUpdate(scanResults);
		}
		//
		long cur_time = System.currentTimeMillis();
		long interval = cur_time - lastScanTime;
		if (interval < min_scan_interval) {
			if (isRunning) {
				handler.postDelayed(delayScanRunnable, min_scan_interval
						- interval);
				if (DEBUG) {
					log("bad scan interval:" + interval + ",append interval:"
							+ (min_scan_interval - interval));
				}
			}
		} else {
			lastScanTime = cur_time;
			if (isRunning) {
				mWifiManager.startScan();
			}
			if (DEBUG) {
				log("scan interval:" + interval);
			}
		}
	}

	private boolean isRunning = false;

	public void start() {
		isRunning = true;
		IntentFilter filter = new IntentFilter();
		filter.addAction(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION);
		context.registerReceiver(mReceiver, filter);
		mWifiManager.startScan();
	}

	public void stop() {
		isRunning = false;
		handler.removeCallbacks(delayScanRunnable);
		context.unregisterReceiver(mReceiver);
	}

	public interface WifiEntityHolder {
		public void onWifiEntitysUpdate(List<ScanResult> results);
	}

	private WifiEntityHolder mWifiEntityHolder;

	public void setWifiEntityHolder(WifiEntityHolder holder) {
		mWifiEntityHolder = holder;
	}

	void log(String msg) {
		Log.d(TAG, "" + msg);
	}

}
