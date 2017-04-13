package com.android.hcframe.netdisc.util;

import com.android.frame.download.DownloadUtil;
import com.android.frame.download.FileColumn;
import com.android.frame.download.HcDownloadService;
import com.android.hcframe.HcApplication;
import com.android.hcframe.HcConfig;
import com.android.hcframe.HcLog;
import com.android.hcframe.HcUtil;
import com.android.hcframe.netdisc.sql.OperateDatabase;

import java.io.File;
import java.io.FileInputStream;
import java.math.BigInteger;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * @Company 浙 江 鸿 程 计 算 机 系 统 有 限 公 司
 * @URL http://www.zjhcsoft.com
 * @Address 杭州滨江区伟业路1号
 * @Email jinjr@zjhcsoft.com
 * Created by jrjin on 16-7-27 16:25.
 */
public class NetdiscUtil {

    private static final String TAG = "NetdiscUtil";
    public static final String BASE_IP = HcConfig.getConfig().getServerName(HcConfig.Module.CLOUD_DISK)
            + ":" + HcConfig.getConfig().getServerPort(HcConfig.Module.CLOUD_DISK);// "http://mobile.zjhcsoft.com:16201";//"http://mobile.zjhcsoft.com:16201";//"http://10.80.7.182:9080";//"http://mobile.zjhcsoft.com:16201";//"http://10.80.7.225:8080";//"http://172.24.4.62:8001";

    public static final String BASE_SERVER = "/clouddiskM-webapp/api/clouddisk/";

    public static final String BASE_URL = BASE_IP + BASE_SERVER;

//    public static final String BASE_URL = HcConfig.getConfig().getServerName(HcConfig.Module.CLOUD_DISK)
//            + ":" + HcConfig.getConfig().getServerPort(HcConfig.Module.CLOUD_DISK)
//            + "/clouddiskM-webapp/api/clouddisk/";

    public static final int THREAD_MAX_SIZE = 5; //最大下载上传个数

    public static final String UPDIRID = "updirId";

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

    /**
     * 获取文件信息，并存入数据库
     *
     * @param path
     * @param updirId
     */
    public static FileColumn getFileInfo(String path, String updirId, String MD5) {
        File file = new File(path);
        if (!file.exists()) {
            return null;
        }
        if (file.length() == 0) {
            return null;
        }
        long length = file.length();
        String fileName = file.getName();
        int position = fileName.lastIndexOf(".");
        String name = fileName.substring(0, position);
        String ext = fileName.substring(position + 1, fileName.length());
        if (name != null && !"".equals(name)) {
            FileColumn fileColumn = new FileColumn();
            fileColumn.setName(name);
            fileColumn.setExt(ext);
            String fileid = HcUtil.getMD5String(name + ext + file.length() + path);
            fileColumn.setFileid(fileid);
            fileColumn.setPath(path);
//            String md5;
//            md5 = NetdiscUtil.getFileMD5(file, length);
            fileColumn.setMd5(MD5);
            fileColumn.setPosition(0);
            fileColumn.setState("2");
            fileColumn.setFileSize(length + "");
            fileColumn.setUpdirid(updirId);
            fileColumn.setUpOrDown(0);
            fileColumn.setUrl(HcConfig.getConfig().getServerName(HcConfig.Module.CLOUD_DISK) + ":" + HcConfig.getConfig().getServerPort(HcConfig.Module.CLOUD_DISK) + "/clouddiskM-webapp/api/clouddisk/");
            fileColumn.setLevel(1);
            fileColumn.setSource(HcDownloadService.NETDISC_SOURCE);
            //如果文件大小小于5M直接上传不需要分片，如果大于5M将文件分片
            if (DownloadUtil.FILE_CHUNK_SIZE >= length) {
                fileColumn.setAll_slice(-1);
                fileColumn.setSlice(1);
                fileColumn.setType("0");
            } else {
//                path = path + "/HcMobile/upload/" + HcUtil.getDateFile();
//                exist(path);
                int file_num = (int) ((length + (DownloadUtil.FILE_CHUNK_SIZE - 1)) / DownloadUtil.FILE_CHUNK_SIZE);
                fileColumn.setAll_slice(file_num);
                fileColumn.setSlice(1);
                fileColumn.setType("1");
            }
            boolean b = OperateDatabase.insertUpload(fileColumn, HcApplication.getContext());
            if (b) {
                return fileColumn;
            } else {
                return null;
            }

        } else {
            return null;
        }
    }

}
