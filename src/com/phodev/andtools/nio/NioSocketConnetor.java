package com.phodev.andtools.nio;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;

public class NioSocketConnetor extends AbstractNioService {

	public NioSocketConnetor(ExecutorService executors) {
		super(executors);
	}

	private boolean isRunning = Boolean.FALSE;

	@Override
	public boolean isRunning() {
		return isRunning;
	}

	@Override
	protected void configAndStartService(Selector selector) throws IOException {
		new Thread() {
			@Override
			public void run() {
				doSelector();
			}

		}.start();
	}

	private void doSelector() {
		while (true) {
			if (!connectTaskDeque.isEmpty()) {
				SocketChannel sc = null;
				while ((sc = connectTaskDeque.poll()) != null) {
					System.out.println("connect[4]select wakeup");
					try {
						if (sc != null) {
							sc.register(selector, SelectionKey.OP_CONNECT);
						}
						sc = null;
					} catch (ClosedChannelException e) {
						e.printStackTrace();
					}
					System.out.println("connect[5]register OP_CONNECT");
					System.out.println("-----------------------------");
				}
			}
			int selectCount;
			System.out.println("waitting select opreation...");
			try {
				selectCount = selector.select();
			} catch (IOException e) {
				e.printStackTrace();
				// TODO
				break;
			}
			System.out.println("select count:" + selectCount);
			//
			if (selectCount <= 0) {
				continue;
			}
			Set<SelectionKey> keyList = selector.selectedKeys();
			Iterator<SelectionKey> iter = keyList.iterator();
			while (iter.hasNext()) {
				SelectionKey key = iter.next();
				processSelectedKey(key);
			}
			keyList.clear();
		}
	}

	@Override
	protected IoSession newIoSession(SelectionKey key) {
		return new IoSessionImpl(getIoHandler());
	}

	private LinkedBlockingQueue<SocketChannel> connectTaskDeque = new LinkedBlockingQueue<SocketChannel>();

	public void conenct(String host, int port) throws IOException {
		System.out.println("connect[1]start");
		SocketChannel socketChannel = SocketChannel.open();
		socketChannel.socket().setReceiveBufferSize(65535);
		socketChannel.configureBlocking(false);
		System.out.println("connect[2]configed");
		socketChannel.connect(new InetSocketAddress(host, port));
		System.out.println("connect[3]start connect...");
		connectTaskDeque.add(socketChannel);
		selector.wakeup();
	}
}
