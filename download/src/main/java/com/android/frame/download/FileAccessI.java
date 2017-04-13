package com.android.frame.download;

import com.android.hcframe.HcUtil;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.io.Serializable;

/**
 * Created by ncll on 2016/7/6.
 */
public class FileAccessI implements Serializable {

    RandomAccessFile randomAccessFile;
    long nPos;


    public FileAccessI() throws IOException {
        this("", 0);
    }

    public FileAccessI(String sName, long nPos) throws IOException {
        randomAccessFile = new RandomAccessFile(sName, "r");//创建一个随机访问文件类，可读写模式
        this.nPos = nPos;
        randomAccessFile.seek(nPos);
    }

    public synchronized int write(byte[] b, int nStart, int nLen) {
        int n = -1;
        try {
            randomAccessFile.write(b, nStart, nLen);
            n = nLen;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return n;
    }

    //每次读取102400字节
    public synchronized Detail getContent(long nStart) {
        Detail detail = new Detail();
        detail.b = new byte[102400];
        try {
            randomAccessFile.seek(nStart);
            detail.length = randomAccessFile.read(detail.b);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return detail;
    }


    public class Detail {

        public byte[] b;
        public int length;
    }

    //获取文件长度
    public long getFileLength() {
        Long length = 0l;
        try {
            length = randomAccessFile.length();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return length;
    }

    long position;

    public InputStream read() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        byte[] bytes = new byte[DownloadUtil.FILE_CHUNK_SIZE];
        InputStream inputStream;
        int byteread;
        if ((byteread = randomAccessFile.read(bytes)) != -1) {
            position = byteread;
            baos.write(bytes, 0, byteread);
        }
        inputStream = new ByteArrayInputStream(baos.toByteArray());
        baos.close();
        return inputStream;
    }

    public long getPosition() {
        return position;
    }

}