package com.phodev.andtools.nio.test;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.ByteBuffer;

import com.phodev.andtools.nio.IoHandler;
import com.phodev.andtools.nio.IoHandler.OP;
import com.phodev.andtools.nio.IoSession;

public class Client {
	static byte[] obj = ByteBuffer.allocate(50).array();

	public static void main(String[] args) {
		Socket socket = new Socket();
		log("create client socket");
		try {
			socket.connect(new InetSocketAddress(Server.port));
			log("is connected:" + socket.isConnected());
			OutputStream os = socket.getOutputStream();
			long time;
			while (true) {
				time = System.currentTimeMillis();
				os.write(obj);
				try {
					Thread.sleep(10000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				log("write use time:" + (System.currentTimeMillis() - time));
			}
			// for (char i = 'a'; i < 'z'; i++) {
			// os.write(i);
			// log("client sleep write :" + i);
			// // try {
			// // Thread.sleep(1000 * 10);
			// // } catch (InterruptedException e) {
			// // e.printStackTrace();
			// // }
			// }
			// os.close();
			// socket.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static class Handler implements IoHandler {

		@Override
		public void sessionCreate(IoSession session) {
			log("sessionCreate");
		}

		@Override
		public OP doRead(IoSession session) {
			log("doRead");
			return OP.READ;
		}

		@Override
		public OP doWrite(IoSession session) {
			log("doWriter");
			return OP.WRITER;
		}

	}

	public static void log(String msg) {
		System.err.println("client:" + msg);
	}
}
