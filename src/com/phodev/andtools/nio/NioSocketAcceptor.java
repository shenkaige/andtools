package com.phodev.andtools.nio;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.ExecutorService;

public class NioSocketAcceptor extends AbstractNioService {
	private ServerSocketChannel serverSocketChannel;
	private boolean isRunning = Boolean.FALSE;
	private int server_port;

	public NioSocketAcceptor(ExecutorService executor, int port) {
		super(executor);
		this.server_port = port;
	}

	@Override
	public boolean isRunning() {
		return isRunning;
	}

	@Override
	protected void configAndStartService(Selector selector) throws IOException {
		serverSocketChannel = ServerSocketChannel.open();
		serverSocketChannel.socket().bind(new InetSocketAddress(server_port));
		serverSocketChannel.configureBlocking(false);
		serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);
		//
		isRunning = true;
		accept();
	}

	@Override
	protected IoSession newIoSession(SelectionKey key) {
		return new IoSessionImpl(getIoHandler());
	}

	private void accept() {
		while (true) {
			int selectCount;
			try {
				selectCount = selector.select();
			} catch (IOException e) {
				e.printStackTrace();
				// TODO
				break;
			}
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
}
