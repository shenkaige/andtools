package com.phodev.andtools.nio.test;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

import com.phodev.andtools.nio.IoHandler;
import com.phodev.andtools.nio.IoHandler.OP;
import com.phodev.andtools.nio.IoSession;
import com.phodev.andtools.nio.NioSocketConnetor;

public class ConnectorTest {

	public static void main(String[] args) {
		final NioSocketConnetor connetor = new NioSocketConnetor(null);
		connetor.setIoHandler(new IoHandlerImpl());
		try {
			connetor.startService();
		} catch (IOException e2) {
			e2.printStackTrace();
		}
		try {
			connetor.conenct("127.0.0.1", Server.port);
			System.out.println("post request-----------");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	static class IoHandlerImpl implements IoHandler {

		@Override
		public void sessionCreate(IoSession session) {
			System.out.println("session create");
		}

		@Override
		public OP doRead(IoSession session) {
			System.out.println("do Read");
			SocketChannel sc = session.getChannel();
			ByteBuffer dst = ByteBuffer.allocate(1024);
			try {
				int readCount = sc.read(dst);
				System.out.println(new String(dst.array()) + "  read count:"
						+ readCount);
				if (readCount == -1) {
					return OP.NONE_FINISH;
				}
			} catch (IOException e) {
				e.printStackTrace();
				return OP.NONE_FINISH;
			}
			return OP.NONE_FINISH;
		}

		@Override
		public OP doWrite(IoSession session) {
			try {
				session.getChannel().write(
						ByteBuffer.wrap("Hello jim".getBytes()));
			} catch (IOException e) {
				e.printStackTrace();
			}
			return OP.READ;
		}

	}
}
