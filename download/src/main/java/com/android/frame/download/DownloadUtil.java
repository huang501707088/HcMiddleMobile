package com.android.frame.download;

import com.android.hcframe.HcConfig;
import com.android.hcframe.HcLog;

import java.io.File;
import java.io.FileInputStream;
import java.math.BigInteger;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Created by ncll on 2016/10/19.
 */
public class DownloadUtil {
    private static final String TAG = "DownloadUtil";
    public static final int FILE_CHUNK_SIZE = 1 * 1024 * 1024; // 5M
    private static MessageDigest messagedigest = null;
    public static final String BASE_IP = HcConfig.getConfig().getServerName(HcConfig.Module.CLOUD_DISK)
            + ":" + HcConfig.getConfig().getServerPort(HcConfig.Module.CLOUD_DISK);// "http://mobile.zjhcsoft.com:16201";//"http://mobile.zjhcsoft.com:16201";//"http://10.80.7.182:9080";//"http://mobile.zjhcsoft.com:16201";//"http://10.80.7.225:8080";//"http://172.24.4.62:8001";

    public static final String BASE_SERVER = "/clouddiskM-webapp/api/clouddisk/";

    public static final String BASE_URL = BASE_IP + BASE_SERVER;
    public static String getFileMD5(File file, long size) {
        if (!file.isFile()) {
            return "";
        }
        if (messagedigest == null) {
            try {
                messagedigest = MessageDigest.getInstance("MD5");
            } catch (NoSuchAlgorithmException nsaex) {

                nsaex.printStackTrace();
            }
        } else {
            messagedigest.reset();
        }

        FileInputStream in = null;
        DigestInputStream digestInputStream = null;
        byte buffer[] = new byte[1024 * 32];
        try {
            in = new FileInputStream(file);
            digestInputStream = new DigestInputStream(in, messagedigest);

            if (size > FILE_CHUNK_SIZE) {
                int len;
                int fileSize = 0;
                while ((len = digestInputStream.read(buffer, 0, buffer.length)) != -1) {
                    fileSize += len;
                    if (fileSize + buffer.length >= FILE_CHUNK_SIZE) {
                        digestInputStream.read(buffer, 0, FILE_CHUNK_SIZE - fileSize);
                        break;
                    } else if (fileSize == FILE_CHUNK_SIZE) {
                        break;
                    }
                }
            } else {
                while (digestInputStream.read(buffer) > 0) ;
            }

        } catch (Exception e) {
            HcLog.D(TAG + " #getFileMD5 Exception e =" + e);
            return "";
        } finally {
            if (digestInputStream != null) {
                try {
                    digestInputStream.close();
                } catch (Exception e) {

                }
            }

            if (in != null) {
                try {
                    in.close();
                } catch (Exception e) {

                }
            }
        }
        HcLog.D(TAG + " #getFileMD5 messagedigest size = " + messagedigest.getDigestLength());
        BigInteger bigInt = new BigInteger(1, messagedigest.digest());
        return bigInt.toString(16);
    }
}
