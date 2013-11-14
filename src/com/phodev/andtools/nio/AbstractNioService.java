package com.phodev.andtools.nio;

import java.io.IOException;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.concurrent.ExecutorService;

import com.phodev.andtools.nio.IoHandler.OP;

/**
 * NIO Service
 * 
 * @author sky
 * 
 */
public abstract class AbstractNioService {
	protected Selector selector;
	protected ExecutorService executors;
	protected IoHandler ioHandler;

	public AbstractNioService(ExecutorService executors) {
		this.executors = executors;
	}

	public void setIoHandler(IoHandler handler) {
		this.ioHandler = handler;
	}

	public IoHandler getIoHandler() {
		return ioHandler;
	}

	public final boolean startService() throws IOException {
		if (isRunning()) {
			return true;
		}
		selector = Selector.open();
		configAndStartService(selector);
		return isRunning();
	}

	public abstract boolean isRunning();

	protected abstract void configAndStartService(Selector selector)
			throws IOException;

	protected void processSelectedKey(SelectionKey key) {
		// TODO cache Handler runner
		// KeyProcessor processor = new KeyProcessor();
		// processor.setSelectionKey(key);
		// submitTask(processor);
		//
		// SingleThread
		IoProcessor processor = new IoProcessor();
		processor.setSelectionKey(key);
		processor.run();
	}

	protected void submitTask(Runnable task) {
		executors.execute(task);
	}

	protected abstract IoSession newIoSession(SelectionKey key);

	public static final String TAG_IoHandlerRunner = "IoHandlerRunner";

	class IoProcessor implements Runnable {
		private SelectionKey selectionKey;

		public void setSelectionKey(SelectionKey key) {
			this.selectionKey = key;
		}

		@Override
		public void run() {
			if (selectionKey == null) {
				return;
			}
			if (!selectionKey.isValid()) {
				// TODO
				return;
			}
			if (selectionKey.isValid() && selectionKey.isAcceptable()) {
				doAccept(selectionKey);
			}
			if (selectionKey.isValid() && selectionKey.isConnectable()) {
				doConnect(selectionKey);
			}
			if (selectionKey.isValid() && selectionKey.isReadable()) {
				doRead(selectionKey);
			}
			if (selectionKey.isValid() && selectionKey.isWritable()) {
				doWrite(selectionKey);
			}
			selectionKey = null;
		}

	}

	/**
	 * 处理Accept
	 * 
	 * @param key
	 * @throws IOException
	 */
	protected void doAccept(SelectionKey key) {
		ServerSocketChannel ssc = (ServerSocketChannel) key.channel();
		IoSession session = newIoSession(key);// on session create
		IoHandler handler = session.getHandler();
		if (handler != null) {
			handler.sessionCreate(session);
		}
		//
		SocketChannel channel = null;
		try {
			channel = ssc.accept();
			session.configSocketChannel(channel);
			channel.configureBlocking(false);
			channel.register(selector, SelectionKey.OP_READ, session);
		} catch (IOException e) {
			e.printStackTrace();
			// TODO accept Failed
		}
	}

	/**
	 * 处理连接
	 * 
	 * @param key
	 */
	protected void doConnect(SelectionKey key) {
		SocketChannel channel = (SocketChannel) key.channel();
		IoSession session = newIoSession(key);// on session create
		IoHandler handler = session.getHandler();
		if (handler != null) {
			handler.sessionCreate(session);
		}
		//
		try {
			session.configSocketChannel(channel);
			if (channel.finishConnect()) {
				channel.register(selector, SelectionKey.OP_WRITE, session);
			} else {
				channel.register(selector, SelectionKey.OP_CONNECT, session);
			}
		} catch (IOException e) {
			e.printStackTrace();
			// TODO accept Failed
		}
	}

	protected void doRead(SelectionKey key) {
		SocketChannel sc = (SocketChannel) key.channel();
		IoSession session = (IoSession) key.attachment();
		IoHandler handler = session.getHandler();
		if (handler == null) {
			// TODO
			return;
		}
		OP op = handler.doRead(session);
		doRegisterReadWrite(sc, op, session, key);
	}

	protected void doWrite(SelectionKey key) {
		SocketChannel sc = (SocketChannel) key.channel();
		IoSession session = (IoSession) key.attachment();
		IoHandler handler = session.getHandler();
		if (handler == null) {
			// TODO
			return;
		}
		OP op = handler.doWrite(session);
		doRegisterReadWrite(sc, op, session, key);
	}

	private void doRegisterReadWrite(SocketChannel sc, OP op,
			IoSession session, SelectionKey oldKey) {
		try {
			switch (op) {
			case READ:
				sc.register(selector, SelectionKey.OP_READ, session);
				break;
			case WRITER:
				sc.register(selector, SelectionKey.OP_WRITE, session);
				break;
			case READ_WRITE:
				sc.register(selector, SelectionKey.OP_WRITE
						| SelectionKey.OP_WRITE, session);
				break;
			case NONE_FINISH:
			default:
				oldKey.cancel();
			}
		} catch (ClosedChannelException e) {
			e.printStackTrace();
			// TODO
		}
	}

}
