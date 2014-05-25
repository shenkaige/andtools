package com.phodev.andtools.wifianalyse;

import android.content.Context;

public class WifiChannels {
	public final static ChannelList GHz_2400 = new ChannelList(//
			new Channel(1, 2412), //
			new Channel(2, 2417), //
			new Channel(3, 2422), //
			new Channel(4, 2427), //
			new Channel(5, 2432), //
			new Channel(6, 2437), //
			new Channel(7, 2442), //
			new Channel(8, 2447), //
			new Channel(9, 2452), //
			new Channel(10, 2457), //
			new Channel(11, 2462), //
			new Channel(12, 2467), //
			new Channel(13, 2472), //
			new Channel(14, 2484));
	public final static ChannelList GHz_4000 = new ChannelList(//
			new Channel(183, 4915),//
			new Channel(184, 4920),//
			new Channel(185, 4925),//
			new Channel(187, 4935),//
			new Channel(188, 4940),//
			new Channel(189, 4945),//
			new Channel(192, 4960),//
			new Channel(196, 4980));
	public final static ChannelList GHz_5000 = new ChannelList(//
			new Channel(7, 5035),//
			new Channel(8, 5040),//
			new Channel(9, 5045),//
			new Channel(11, 5055),//
			new Channel(12, 5060),//
			new Channel(16, 5080),//
			new Channel(34, 5170),//
			new Channel(36, 5180),//
			new Channel(38, 5190),//
			new Channel(40, 5200),//
			new Channel(42, 5210),//
			new Channel(44, 5220),//
			new Channel(46, 5230),//
			new Channel(48, 5240),//
			new Channel(52, 5260),//
			new Channel(56, 5280),//
			new Channel(60, 5300),//
			new Channel(64, 5320),//
			new Channel(100, 5500),//
			new Channel(104, 5520),//
			new Channel(108, 5540),//
			new Channel(112, 5560),//
			new Channel(116, 5580),//
			new Channel(120, 5600),//
			new Channel(124, 5620),//
			new Channel(128, 5640),//
			new Channel(132, 5660),//
			new Channel(136, 5680),//
			new Channel(140, 5700),//
			new Channel(149, 5745),//
			new Channel(153, 5765),//
			new Channel(157, 5785),//
			new Channel(161, 5805),//
			new Channel(165, 5825));

	public static final class ChannelList {
		private final Channel[] channels;

		public ChannelList(Channel... channels) {
			this.channels = channels;
		}

		public int getSize() {
			return channels.length;
		}

		public Channel get(int i) {
			if (i < 0 || i >= channels.length) {
				return null;
			}
			return channels[i];
		}
	}

	public static final class ReadOnlyList<T> {
		private final T[] ts;

		public ReadOnlyList(T... ts) {
			this.ts = ts;
		}

		public int getSize() {
			return ts.length;
		}

		public T get(int i) {
			if (i < 0 || i >= ts.length) {
				return null;
			}
			return ts[i];
		}
	}

	public final static class Channel {
		public final int channel;
		public final int hz;

		public Channel(int channel, int hz) {
			this.channel = channel;
			this.hz = hz;
		}
	}

	public static String getSignalStrengthName(Context context) {
		// return context.getString(0);
		return "信号强度 [dBm]";
	}

	public static String getSignalName(Context context) {
		// return context.getString(0);
		return "Wi-fi 信道";
	}

	public static final ReadOnlyList<Integer> CHANNEL_SIGNAL = new ReadOnlyList<Integer>(
			-30, -40, -50, -60, -70, -80, -90);

}
