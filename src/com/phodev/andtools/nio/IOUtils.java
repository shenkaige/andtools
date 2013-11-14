package com.phodev.andtools.nio;

import java.io.Closeable;
import java.io.IOException;

public class IOUtils {
	private IOUtils() {
	}

	public static void close(Closeable entity) {
		if (entity != null) {
			try {
				entity.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
}
