package com.android.hcframe.ebook;

import com.android.frame.download.DownloadUtil;
import com.android.hcframe.HcLog;

import java.io.File;
import java.io.FileInputStream;
import java.math.BigInteger;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Created by ncll on 2016/12/2.
 */

public class EbookUtil {
    private static final String TAG = "EbookUtil";

    public static final String NETDISK_DIRECTORY = "Handwritten";

    private static MessageDigest messagedigest = null;

    /**
     * 获取单个文件的MD5值！
     *
     * @param file
     * @return
     */

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

            if (size > DownloadUtil.FILE_CHUNK_SIZE) {
                int len;
                int fileSize = 0;
                while ((len = digestInputStream.read(buffer, 0, buffer.length)) != -1) {
                    fileSize += len;
                    if (fileSize + buffer.length >= DownloadUtil.FILE_CHUNK_SIZE) {
                        digestInputStream.read(buffer, 0, DownloadUtil.FILE_CHUNK_SIZE - fileSize);
                        break;
                    } else if (fileSize == DownloadUtil.FILE_CHUNK_SIZE) {
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
