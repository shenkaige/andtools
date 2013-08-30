package com.phodev.andtools.download;

import java.io.File;
import java.net.HttpURLConnection;
import java.net.ProtocolException;
import java.util.LinkedHashMap;
import java.util.Map;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.NetworkInfo.State;
import android.os.Environment;
import android.util.Log;

public class Utils {
	private Utils() {
	}

	public static void configBlockHeader(HttpURLConnection conn,
			String referer, long startPos, long endPos) {
		if (conn == null) {
			return;
		}
		configCommonHeader(conn, referer);
		conn.setRequestProperty("Range", "bytes=" + startPos + "-" + endPos);
		// conn.setRequestProperty("Accept-Encoding", "gzip,deflate");
	}

	public static void configCommonHeader(HttpURLConnection conn, String referer) {
		if (conn == null) {
			return;
		}
		conn.setConnectTimeout(Constants.TIME_OUT);
		try {
			conn.setRequestMethod("GET");
		} catch (ProtocolException e) {
			e.printStackTrace();
		}
		conn.setRequestProperty("Accept", Constants.Accept);
		conn.setRequestProperty("Accept-Language", Constants.Accept_Language);
		if (referer != null) {
			conn.setRequestProperty("Referer", referer);
		}
		conn.setRequestProperty("Charset", "UTF-8");
		conn.setRequestProperty("User-Agent", Constants.User_Agent);
		conn.setRequestProperty("Connection", "Keep-Alive");
	}

	public static Map<String, String> getResponseHeader(HttpURLConnection http) {
		Map<String, String> header = new LinkedHashMap<String, String>();
		for (int i = 0;; i++) {
			String mine = http.getHeaderField(i);
			if (mine == null)
				break;
			header.put(http.getHeaderFieldKey(i), mine);
		}
		return header;
	}

	public static void debugPrintResponseHeader(HttpURLConnection http,
			String tag) {
		if (Constants.DEBUG) {
			Map<String, String> header = getResponseHeader(http);
			for (Map.Entry<String, String> e : header.entrySet()) {
				Log.d(tag, "key-value:" + e.getKey() + ":" + e.getValue());
			}
		}
	}

	public static File getDownloadDir() {
		String dirPath = Environment.getExternalStorageDirectory()
				.getAbsolutePath()
				+ File.separator
				+ Constants.relative_download_path;
		File dir = new File(dirPath);
		if (!dir.exists()) {
			dir.mkdirs();
		}
		return dir;
	}

	public static File createDownloadOutFile(String fileName) {
		if (fileName == null || fileName.length() <= 0) {
			return null;
		}
		return new File(getDownloadDir().getAbsolutePath() + File.separator
				+ fileName);
	}

	public static String createDownloadOutFilePath(String fileName) {
		if (fileName == null || fileName.length() <= 0) {
			return null;
		}
		return getDownloadDir().getAbsolutePath() + File.separator + fileName;
	}

	//
	// public static File createFile(String fileName, boolean tryCreate) {
	// String fileFullPath = makeAbsolutePath(fileName);
	// if (fileFullPath == null) {
	// return null;
	// }
	// File file = new File(makeAbsolutePath(fileName));
	// if (tryCreate && !file.exists()) {
	// try {
	// file.createNewFile();
	// } catch (IOException e) {
	// e.printStackTrace();
	// }
	// }
	// return file;
	// }
	//
	public static boolean isNetworkAvailable(Context context) {
		ConnectivityManager cm = (ConnectivityManager) context
				.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo info = cm.getActiveNetworkInfo();
		return (info != null && info.isConnected());
	}

	public static boolean isWifiNetworkAvailable(Context context) {
		ConnectivityManager cm = (ConnectivityManager) context
				.getSystemService(Context.CONNECTIVITY_SERVICE);
		State wifi = cm.getNetworkInfo(ConnectivityManager.TYPE_WIFI)
				.getState();
		return (wifi == State.CONNECTED);
	}

	public static boolean isSDCardMounted() {
		if (Environment.getExternalStorageState().equals(
				Environment.MEDIA_MOUNTED)) {
			return true;
		} else {
			return false;
		}
	}
}
