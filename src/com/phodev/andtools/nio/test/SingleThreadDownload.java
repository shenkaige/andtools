package com.phodev.andtools.nio.test;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.SocketChannel;

import com.phodev.andtools.nio.IoHandler;
import com.phodev.andtools.nio.IoHandler.OP;
import com.phodev.andtools.nio.IoSession;
import com.phodev.andtools.nio.NioSocketConnetor;

public class SingleThreadDownload {
	public static void main(String[] args) throws IOException {
		final NioSocketConnetor connector = new NioSocketConnetor(null);
		connector.setIoHandler(new DownloadIoHandler());
		connector.startService();
		//
		try {
			Thread.sleep(2000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		//
		String filePath = "www.oschina.net";
		connector.conenct(filePath, 80);
		connector.conenct(filePath, 80);
		connector.conenct(filePath, 80);
		connector.conenct(filePath, 80);
		connector.conenct(filePath, 80);
		connector.conenct(filePath, 80);
		connector.conenct(filePath, 80);
		connector.conenct(filePath, 80);
		connector.conenct(filePath, 80);
		connector.conenct(filePath, 80);
		connector.conenct(filePath, 80);
		connector.conenct(filePath, 80);
		connector.conenct(filePath, 80);
		connector.conenct(filePath, 80);
		connector.conenct(filePath, 80);

	}

	static class DownloadIoHandler implements IoHandler {

		@Override
		public void sessionCreate(IoSession session) {
			File file = new File("/home/sky/testNioDownload/"
					+ System.nanoTime());
			if (!file.exists()) {
				try {
					file.createNewFile();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			FileOutputStream fos = null;
			FileChannel fileChannel = null;
			try {
				fos = new FileOutputStream(file);
				fileChannel = fos.getChannel();
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			}
			session.putAttribute("file", file);
			session.putAttribute("fos", fos);
			session.putAttribute("fileChannel", fileChannel);
			session.putAttribute("buffer", ByteBuffer.allocate(1024 * 10));
		}

		@Override
		public OP doRead(IoSession session) {
			FileChannel fc = (FileChannel) session.getAttribute("fileChannel");
			ByteBuffer buffer = (ByteBuffer) session.getAttribute("buffer");
			//
			buffer.clear();
			//
			SocketChannel sc = session.getChannel();
			int rc = -1;
			try {
				rc = sc.read(buffer);
				buffer.flip();
				fc.write(buffer);
			} catch (IOException e) {
				e.printStackTrace();
			}
			System.out.println("do read<---");
			if (rc == -1) {
				return OP.NONE_FINISH;
			}
			return OP.READ;
		}

		@Override
		public OP doWrite(IoSession session) {
			String header = "GET / HTTP/1.1"
					+ "Host: www.oschina.net"
					+ "Connection: keep-alive"
					+ "Cache-Control: max-age=0"
					+ "Accept: text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8"
					+ "User-Agent: Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/27.0.1453.110 Safari/537.36"
					+ "Referer: http://www.oschina.net/question/12_131873"
					+ "Accept-Encoding: gzip,deflate,sdch"
					+ "Accept-Language: en-US,en;q=0.8";

			try {
				session.getChannel().write(
						ByteBuffer.wrap(header.getBytes("utf-8")));
			} catch (IOException e) {
				e.printStackTrace();
			}
			System.out.println("do write --->");
			return OP.READ;
		}

	}
}
