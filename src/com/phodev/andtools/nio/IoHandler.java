package com.phodev.andtools.nio;

public interface IoHandler {

	public enum OP {
		/** 继续读 */
		READ,
		/** 继续写 */
		WRITER,
		/** 继续读写 */
		READ_WRITE,
		/** 终止 */
		NONE_FINISH;
	}

	public void sessionCreate(IoSession session);

	public OP doRead(IoSession session);

	public OP doWrite(IoSession session);

}
