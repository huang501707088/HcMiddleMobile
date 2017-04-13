package com.android.hcframe.ebook;

import android.os.Environment;

public class Params {
	public final static String USERID = "110";
	public static String TITLE = "title";
	public final static int MSG_OK = 0;
	public final static int MSG_ERROR = -1;
	public static String MSG = "msg";
	public static final String READONLY = "0";//只读
	public static final String EDIT = "1";//可编辑
	public static final String FROM_READ = "0";//来源于只读
	public static final String FROM_EDIT = "1";//来源于编辑
	public static final String FROM_WEB = "2";//来源于web
	public final static String COPYRIGHT = "SxD/phFsuhBWZSmMVtSjKZmm/c/3zSMrkV2Bbj5tznTbalOoU/SoNEHp4A/HvseYNfrq0k/tYu9aiVnc45auDzHJ9ymw8oH48HggOjYvytC/M6iAKDk2CYDvnU/qVpSow1Lyf/gCcnnf/q/UnniWd073/v12IkrJrHbDJVKS5FJwgmOThQp8xrADhCx0m3Vm4qE3BpMNjVZfdPNXM5iqz53ypMrnEAwwOzZZo9SKIPMuiv2lLV+jXsHRW/NsP++8vq04KBDw/udBaW0tiVD4eX+fp1t4Z1bvN6r5dJYGQzBDBVzCuEF2kzvFyN13lwGf/FVKwAIpWNNINn5h/pqNg2pz6EtSWBWc8nNFKZOtJR3bKKY1jBInFPtwonbBivMLmUM5O23DX/hk5qFeybmnOnz6nJbuvVI7AN8ok9vFBwmgvIjq2wPLlRWrgXxzyRenBDgtWtF8gXeygZH6AMf67lOGDCZOU830usJDIcG344o=";

	public static String FILEINFO = "fileinfo";
	
//	public final static String localPath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/com.zjhcsoft.android.sign/sign.pdf";

	public final static String localPath = Environment.getExternalStorageDirectory() + "/sign.pdf";
}
