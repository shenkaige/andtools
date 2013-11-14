package com.phodev.andtools.nio.test;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

import com.phodev.andtools.nio.IoHandler;
import com.phodev.andtools.nio.IoHandler.OP;
import com.phodev.andtools.nio.IoSession;
import com.phodev.andtools.nio.NioSocketAcceptor;

public class Server {

	public static final int port = 6666;

	public static void main(String[] args) throws IOException {
		NioSocketAcceptor nsa = new NioSocketAcceptor(null, port);
		nsa.setIoHandler(new Handler());
		nsa.startService();
	}

	public static class Handler implements IoHandler {

		@Override
		public void sessionCreate(IoSession session) {
			log("sessionCreate");
		}

		@Override
		public OP doRead(IoSession session) {
			SocketChannel sc = session.getChannel();
			log("doRead:" + session);
			//
			ByteBuffer buffer = ByteBuffer.allocate(1024);
			try {
				// try {
				// Thread.sleep(10000);
				// } catch (InterruptedException e) {
				// e.printStackTrace();
				// }
				int readCount = sc.read(buffer);

				log("read size:" + readCount / 1024 / 1024 + "mb,read count:"
						+ readCount);
				if (readCount != -1) {
					buffer.flip();
					// if (buffer.hasRemaining()) {
					// log("read result:" + new String(buffer.array()));
					// }
					buffer.clear();
					return OP.WRITER;
				}
			} catch (IOException e) {
				e.printStackTrace();
				log("read error");
			}
			return OP.WRITER;
		}

		@Override
		public OP doWrite(IoSession session) {
			log("doWriter");
			try {
				session.getChannel().write(
						ByteBuffer.wrap("Hello jim from server".getBytes()));
			} catch (IOException e) {
				e.printStackTrace();
			}
			return OP.NONE_FINISH;
		}

	}

	public static void log(String msg) {
		System.out.println("server:" + msg);
	}
}
