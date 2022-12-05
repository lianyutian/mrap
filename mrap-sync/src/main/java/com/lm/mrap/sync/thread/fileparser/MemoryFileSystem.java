package com.lm.mrap.sync.thread.fileparser;

import io.netty.buffer.ByteBuf;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.*;
import org.apache.hadoop.fs.permission.FsPermission;
import org.apache.hadoop.util.Progressable;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URI;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author liming
 * @version 1.0
 * @description: TODO
 * @date 2022/11/15 下午4:09
 */
public class MemoryFileSystem extends FileSystem {

    private static final String MEMORY_PROTOCOL = "memory";

    private static final String MEMORY_HOST = "NativeHost";

    private final ConcurrentHashMap<String, ByteBuf> MEMORY_CACHE = new ConcurrentHashMap<>();

    public MemoryFileSystem() {
        setConf(new Configuration());
    }

    @Override
    public URI getUri() {
        return URI.create(MEMORY_PROTOCOL + "://" + MEMORY_HOST);
    }

    @Override
    public FSDataInputStream open(Path path, int bufferSize) throws IOException {

        String pathStr = path.toString();
        if (!MEMORY_CACHE.containsKey(pathStr)) {
            throw new IOException(pathStr + "不存在");
        }
        return new FSDataInputStream(new MemoryDataInputStream(MEMORY_CACHE.get(pathStr).duplicate()));
    }

    @Override
    public FSDataOutputStream create(Path f, FsPermission permission, boolean overwrite, int bufferSize, short replication, long blockSize, Progressable progress) throws IOException {
        throw  new IOException("MemoryFileSystem不支持create操作");
    }

    @Override
    public FSDataOutputStream append(Path f, int bufferSize, Progressable progress) throws IOException {
        throw  new IOException("MemoryFileSystem不支持append操作");
    }

    @Override
    public boolean rename(Path src, Path dst) throws IOException {
        throw  new IOException("MemoryFileSystem不支持rename操作");
    }

    @Override
    public boolean delete(Path path, boolean recursive) throws IOException {

        String pathStr = path.toString();
        if (!MEMORY_CACHE.containsKey(pathStr)) {
            throw new IOException(pathStr + " 不在内存中");
        }

        MEMORY_CACHE.remove(pathStr);

        return true;
    }

    @Override
    public FileStatus[] listStatus(Path f) throws FileNotFoundException, IOException {
        return new FileStatus[0];
    }

    @Override
    public void setWorkingDirectory(Path newDir) {
    }

    @Override
    public Path getWorkingDirectory() {
        return null;
    }

    @Override
    public boolean mkdirs(Path f, FsPermission permission) throws IOException {
        return false;
    }

    @Override
    public FileStatus getFileStatus(Path path) throws IOException {
        return new FileStatus(
                MEMORY_CACHE.get(path.toString()).writerIndex(),
                false,
                3,
                64 * 1024 * 1024,
                System.currentTimeMillis(),
                path
        );
    }

    @Override
    public void close() throws IOException {
        super.close();
        MEMORY_CACHE.clear();
    }

    public void put(Path path, ByteBuf byteBuf) {
        MEMORY_CACHE.put(path.toString(), byteBuf);
    }

    public void remove(Path path) {
        MEMORY_CACHE.remove(path.toString());
    }


}
