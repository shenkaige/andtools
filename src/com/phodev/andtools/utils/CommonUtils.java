package com.phodev.andtools.utils;

import android.content.Context;
import android.widget.Toast;

public class CommonUtils {
	private CommonUtils() {
	}

	public static void toast(Context context, String msg) {
		if (context != null || msg != null) {
			Toast.makeText(context, msg, Toast.LENGTH_SHORT).show();
		}
	}
}
