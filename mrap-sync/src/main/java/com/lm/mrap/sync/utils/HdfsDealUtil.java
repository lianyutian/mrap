package com.lm.mrap.sync.utils;

import com.lm.mrap.common.utils.StringUtil;
import com.lm.mrap.logger.Logger;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileStatus;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.LocatedFileStatus;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.fs.RemoteIterator;
import org.apache.hadoop.fs.permission.FsAction;
import org.apache.hadoop.fs.permission.FsPermission;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URI;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

/**
 * @author liming
 * @version 1.0
 * @description: HDFS工具类
 * @date 2022/10/20 上午11:07
 */
public class HdfsDealUtil {
    private FileSystem hdfsFileSystem;

    private List<Path> listPath = new ArrayList<>();

    public HdfsDealUtil(Configuration configuration) throws IOException {
        hdfsFileSystem = FileSystem.get(configuration);
    }

    public BufferedReader getBufferReader(String filePath) throws IOException {
        return getBufferReader(path(filePath));
    }

    public BufferedReader getBufferReader(Path path) throws IOException {
        return getBufferReader(hdfsFileSystem.open(path));
    }

    public BufferedReader getBufferReader(InputStream inputStream) {
        return new BufferedReader(getReader(inputStream));
    }

    private Reader getReader(InputStream inputStream) {
        return new InputStreamReader(inputStream);
    }

    public ByteBuffer getByteBuffer(Path path) throws IOException {
        InputStream inputStream = getFileInputStream(path);

        int inputStreamSize = inputStream.available();

        ByteArrayOutputStream swapStream = new ByteArrayOutputStream();
        byte[] buff = new byte[inputStreamSize];

        int len = 0;

        while ((len = inputStream.read(buff, 0, inputStreamSize)) > 0) {
            swapStream.write(buff, 0, len);
            String str = "\n";
            byte[] bytes = str.getBytes();
            swapStream.write(bytes);
        }

        byte[] hdfsFileByteArray = swapStream.toByteArray();
        return ByteBuffer.wrap(hdfsFileByteArray);
    }

    public InputStream getFileInputStream(Path path) throws IOException {
        return hdfsFileSystem.open(path);
    }

    public InputStream getFileInputStream(String filePath) throws IOException {
        return getFileInputStream(path(filePath));
    }

    /**
     * 判断文件是否存在
     *
     * @param filePath 文件路径
     * @return true 存在
     * @throws IOException IOException
     */
    public boolean exists(String filePath) throws IOException {
        return hdfsFileSystem.exists(path(filePath));
    }

    public FileStatus getFileStatus(String filePath) throws IOException {
        return hdfsFileSystem.getFileStatus(path(filePath));
    }

    public FsPermission getPermission(String filePath) throws IOException {
        return getFileStatus(filePath).getPermission();
    }

    public FsAction getOtherFsAction(String filePath) throws IOException {
        return getPermission(filePath).getOtherAction();
    }

    public Boolean isHasOtherAllPersion(String filePath) throws IOException {
        FsAction fsAction = getOtherFsAction(filePath);
        return fsAction == FsAction.ALL;
    }

    public Path path(String filePath) {
        return new Path(filePath);
    }

    /**
     * 获取目录下的所有文件
     *
     * @param dirPath 目录路径
     * @return 文件路径集合
     * @throws IOException IOException
     */
    public List<String> getPaths(String dirPath) throws IOException {

        List<String> pathList = new ArrayList<>();
        RemoteIterator<LocatedFileStatus> fileStatusRemoteIterator;

        if (exists(dirPath)) {
            fileStatusRemoteIterator = hdfsFileSystem.listFiles(path(dirPath), true);
        } else {
            return pathList;
        }

        while (fileStatusRemoteIterator.hasNext()) {

            LocatedFileStatus locatedFileStatus = fileStatusRemoteIterator.next();

            Path path = locatedFileStatus.getPath();

            if (!"_SUCCESS".equals(path.getName())) {
                pathList.add(path.toString());
            }
        }

        return pathList;
    }

    /**
     * 不递归删除文件
     *
     * @param filePath 文件路径
     * @return 删除是否成功
     * @throws IOException IOException
     */
    public boolean noRecursiveDeleteFile(String filePath) throws IOException {
        return hdfsFileSystem.delete(path(filePath), false);
    }

    public URI getUri() {
        return hdfsFileSystem.getUri();
    }

    public void close() {

        try {
            hdfsFileSystem.close();
        } catch (IOException e) {

            Logger.error(
                    "关闭 hdfsFileSystem 时出现异常",
                    StringUtil.exToString(e)
            );
        }
    }
}
