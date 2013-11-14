package com.phodev.andtools.nio;

class IoHandlerTask implements Runnable {
	private IoSession session;

	public IoHandlerTask(IoSession session) {
		this.session = session;
	}

	@Override
	public void run() {
		IoHandler handler = session.getHandler();
		if (session.isReadable()) {
			handler.doRead(session);
		}
		if (session.isWriteable()) {
			handler.doWrite(session);
		}
	}
}
