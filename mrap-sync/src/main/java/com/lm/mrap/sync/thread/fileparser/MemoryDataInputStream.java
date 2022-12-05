package com.lm.mrap.sync.thread.fileparser;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufInputStream;
import org.apache.hadoop.fs.PositionedReadable;
import org.apache.hadoop.fs.Seekable;

import java.io.IOException;

/**
 * @author liming
 * @version 1.0
 * @description: TODO
 * @date 2022/11/15 下午5:28
 */
public class MemoryDataInputStream extends ByteBufInputStream implements Seekable, PositionedReadable {

    private final ByteBuf buf;

    public MemoryDataInputStream(ByteBuf buf) {
        super(buf);
        this.buf = buf;
    }

    public MemoryDataInputStream(ByteBuf buf, int length) {
        super(buf, length);
        this.buf = buf;
    }

    public MemoryDataInputStream(ByteBuf buf, boolean releaseOnClose) {
        super(buf, releaseOnClose);
        this.buf = buf;
    }

    public MemoryDataInputStream(ByteBuf buf, int length, boolean releaseOnClose) {
        super(buf, length, releaseOnClose);
        this.buf = buf;
    }

    @Override
    public int read(long position, byte[] buffer, int offset, int length) throws IOException {

        int bufferSize = buf.writerIndex();

        if (position >= bufferSize) {
            throw new IOException("MemoryDataInputStream读取越界");
        }

        int readable = bufferSize - (int) position;

        int readLength = Math.min(readable, length);

        buf.getBytes((int) position, buffer, offset, length);

        return readLength;
    }

    @Override
    public void readFully(long position, byte[] buffer, int offset, int length) throws IOException {
        read(position, buffer, offset, length);
    }

    @Override
    public void readFully(long position, byte[] buffer) throws IOException {
        read(position, buffer, 0, buffer.length);
    }

    @Override
    public void seek(long pos) throws IOException {

        if (pos > buf.writerIndex()) {
            throw new IOException("设置文件内容指针越界");
        }

        buf.readerIndex((int) pos);
    }

    @Override
    public long getPos() throws IOException {
        return buf.readerIndex();
    }

    @Override
    public boolean seekToNewSource(long targetPos) throws IOException {
        return false;
    }
}
