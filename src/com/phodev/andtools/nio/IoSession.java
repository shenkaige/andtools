package com.phodev.andtools.nio;

import java.nio.channels.SocketChannel;

public interface IoSession {

	public Object getAttribute();

	public Object getAttribute(String key);

	public void putAttribute(Object attr);

	public void putAttribute(String key, Object attr);

	public IoHandler getHandler();

	public SocketChannel getChannel();

	void configSocketChannel(SocketChannel channel);

	public boolean isReadable();

	public boolean isWriteable();

}
