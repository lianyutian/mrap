package com.lm.mrap.sync.thread.fileparser;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import org.apache.hadoop.fs.FSDataInputStream;
import org.apache.hadoop.fs.Path;
import org.junit.Test;

import java.io.IOException;


/**
 * @author liming
 * @version 1.0
 * @description: TODO
 * @date 2022/11/17 上午9:54
 */
public class MemoryFileSystemTest {
    @Test
    public void testMemoryFileSystem() throws IOException {
        MemoryFileSystem memoryFileSystem = new MemoryFileSystem();

        Path path = new Path("memory://NativeHost/test");

        String content = "adabasdi asdofiawef alsdifjbioa lsdiafwe";

        ByteBuf contentBytes = Unpooled.wrappedBuffer(content.getBytes());

        memoryFileSystem.put(path, contentBytes);

        FSDataInputStream fsDataInputStream = memoryFileSystem.open(path, 100);

        int size = fsDataInputStream.available();

        System.out.println(size + " size");

        int count = 0;

        while (count < size) {

            char c = (char) fsDataInputStream.read();
            System.out.println(c);
            count++;
       }

    }
}
