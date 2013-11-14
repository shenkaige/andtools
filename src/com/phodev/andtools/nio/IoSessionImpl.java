package com.phodev.andtools.nio;

import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.util.concurrent.ConcurrentHashMap;

public class IoSessionImpl implements IoSession {
	private SelectionKey mSelectionKey;
	private IoHandler mIoHandler;
	private SocketChannel mSocketChannel;
	private ConcurrentHashMap<Object, Object> mAttrs = new ConcurrentHashMap<Object, Object>();

	public IoSessionImpl(IoHandler ioHandler) {
		this.mIoHandler = ioHandler;
	}

	@Override
	public Object getAttribute() {
		return mAttrs.get("");
	}

	@Override
	public Object getAttribute(String key) {
		return mAttrs.get(key);
	}

	@Override
	public void putAttribute(Object attr) {
		mAttrs.put("", attr);
	}

	@Override
	public void putAttribute(String key, Object attr) {
		mAttrs.put(key, attr);
	}

	@Override
	public IoHandler getHandler() {
		return mIoHandler;
	}

	@Override
	public boolean isReadable() {
		return mSelectionKey.isReadable();
	}

	@Override
	public boolean isWriteable() {
		return mSelectionKey.isWritable();
	}

	@Override
	public SocketChannel getChannel() {
		return mSocketChannel;
	}

	@Override
	public void configSocketChannel(SocketChannel channel) {
		mSocketChannel = channel;
	}

}
