package com.phodev.andtools.wifianalyse;

import android.net.wifi.ScanResult;

public class WifiDrawEntity {
	/** The network name. */
	public String SSID;
	/** The address of the access point. */
	public String BSSID;
	/**
	 * Describes the authentication, key management, and encryption schemes
	 * supported by the access point.
	 */
	public String capabilities;
	/**
	 * The detected signal level in dBm. At least those are the units used by
	 * the TI driver.
	 */
	public int curLevel;

	public int levelBeforLastChnaged;
	/**
	 * The frequency in MHz of the channel over which the client is
	 * communicating with the access point.
	 */
	public int frequency;
	/**
	 * draw color
	 */
	public int drawColor = -1;
	//
	public boolean drawIsLight = Boolean.FALSE;// 是否要高亮
	//
	private float drawLineWitdh = -1;

	public final static float def_draw_line_width = 4f;

	public float getDrawLineWidth() {
		if (drawLineWitdh < 0) {
			return def_draw_line_width;
		}
		return drawLineWitdh;
	}

	public void setDrawLineWidth(float width) {
		drawLineWitdh = width;
	}

	public WifiDrawEntity() {

	}

	public WifiDrawEntity(ScanResult sr) {
		reset(sr);
	}

	public void reset(ScanResult sr) {
		this.SSID = sr.SSID;
		this.BSSID = sr.BSSID;
		this.capabilities = sr.capabilities;
		this.curLevel = sr.level;
		this.frequency = sr.frequency;
	}

	public void reset(WifiDrawEntity sr) {
		this.SSID = sr.SSID;
		this.BSSID = sr.BSSID;
		this.capabilities = sr.capabilities;
		this.curLevel = sr.curLevel;
		this.frequency = sr.frequency;
	}
	@Override
	public String toString() {
		return "WifiEntity [SSID=" + SSID + ", BSSID=" + BSSID
				+ ", capabilities=" + capabilities + ", level=" + curLevel
				+ ", frequency=" + frequency + "]";
	}

}