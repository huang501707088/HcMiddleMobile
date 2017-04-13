package com.android.hcframe.im;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.NinePatch;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.style.BackgroundColorSpan;
import android.text.style.ForegroundColorSpan;
import android.text.style.ImageSpan;
import android.widget.RemoteViews;

import com.android.hcframe.HcApplication;
import com.android.hcframe.HcConfig;
import com.android.hcframe.HcLog;
import com.android.hcframe.HcUtil;
import com.android.hcframe.container.data.ContainerConfig;
import com.android.hcframe.container.data.ViewInfo;
import com.android.hcframe.im.data.AppMessageInfo;
import com.android.hcframe.im.data.ChatGroupMessageInfo;
import com.android.hcframe.im.data.ChatImageOtherInfo;
import com.android.hcframe.im.data.ChatMessageInfo;
import com.android.hcframe.im.data.ChatOperatorDatabase;
import com.android.hcframe.im.data.ChatTextOtherInfo;
import com.android.hcframe.im.data.ChatVoiceOtherInfo;
import com.android.hcframe.im.data.IMSettings;
import com.android.hcframe.menu.MenuInfo;
import com.android.hcframe.push.HcPushManager;
import com.android.hcframe.push.PushInfo;
import com.android.hcframe.sql.OperateDatabase;
import com.android.hcframe.sql.SettingHelper;
import com.android.hcframe.sys.SystemMessage;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.nostra13.universalimageloader.utils.StorageUtils;

import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.util.Base64;
import org.json.JSONObject;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.jivesoftware.smack.packet.Message.Type.groupchat;

/**
 * @Company 浙 江 鸿 程 计 算 机 系 统 有 限 公 司
 * @URL http://www.zjhcsoft.com
 * @Address 杭州滨江区伟业路1号
 * @Email jinjr@zjhcsoft.com
 * Created by jrjin on 16-9-11 14:56.
 */
public final class IMUtil {

    private static final String TAG = "IMUtil";

    private static String[] mWeeks = null;
    private static Calendar mCalendar;

    private static int DAY = 24 * 60 * 60 * 1000;

//    public static final String IM_SERVER = "10.80.7.187";//"10.80.7.32";
//
//    public static final int IM_PORT = 5222;

    public static final String CONFERENCE = "conference";

    private static final int IMAGE_MAX_WIDTH = (int) (140 * HcUtil.getScreenDensity());
    private static final int IMAGE_MAX_HEIGHT = (int) (140 * HcUtil.getScreenDensity());

    private static List<String> mSmileys = new ArrayList<String>();

    private static String mServerName;

    private static int mServerPort = -1;

    private static NotificationManager mManager;

    private final static int NOTIFY_ID = 0x1100;

    private static Notification mNotification;

    private static PendingIntent mIntent;

    /** 自定义的状态栏的view */
    private static RemoteViews mRemoteViews;

    private static Notification.Builder mBuilder; // SDK >= 11  3.0以后

    public static final String NETDISK_DIRECTORY = "IM";

    static  {
        for (int i = 0; i < 90; i++) {
            mSmileys.add("smiley_" + i);
        }
    }

    /** 聊天的图片
     *  key: 聊天记录的ID,不是mChatId,是mMessageId */
    private static Map<String, Bitmap> mImageCache = new WeakHashMap<String, Bitmap>();

    /**
     * 照片背景的Bitmap
     * key: resId
     */
    private static Map<Integer, Bitmap> mBgCache = new WeakHashMap<Integer, Bitmap>();


    public static String getDayOfWeek(Context context, long time) {
        if (mWeeks == null) {
            mWeeks = context.getResources().getStringArray(R.array.main_weeks);
        }

        if (mCalendar == null) {
            mCalendar = Calendar.getInstance();
        }
        mCalendar.setTimeInMillis(time);
        return mWeeks[mCalendar.get(Calendar.DAY_OF_WEEK)];
    }

    /**
     * 获取今天是星期几
     * @param todayTime
     * @return {@code SUNDAY},
     * {@code MONDAY}, {@code TUESDAY}, {@code WEDNESDAY},
     * {@code THURSDAY}, {@code FRIDAY}, and
     * {@code SATURDAY}.
     *
     * @see Calendar#SUNDAY
     * @see Calendar#MONDAY
     * @see Calendar#TUESDAY
     * @see Calendar#WEDNESDAY
     * @see Calendar#THURSDAY
     * @see Calendar#FRIDAY
     * @see Calendar#SATURDAY
     */
    public static int getTodayOfWeek(long todayTime) {
        if (mCalendar == null) {
            mCalendar = Calendar.getInstance();
        }
        mCalendar.setTimeInMillis(todayTime);
        return mCalendar.get(Calendar.DAY_OF_WEEK);
    }

    public static String getChatDate(Context context, long currentTime) {
        if (mCalendar == null) {
            mCalendar = Calendar.getInstance();
        }
        mCalendar.setTimeInMillis(System.currentTimeMillis()); // 确保是最新的时间
        int year = mCalendar.get(Calendar.YEAR);
        int month = mCalendar.get(Calendar.MONTH);
        int day = mCalendar.get(Calendar.DAY_OF_MONTH);
        int week = mCalendar.get(Calendar.DAY_OF_WEEK);
        // 设置今天的最初时间
        mCalendar.set(Calendar.YEAR, year);
        mCalendar.set(Calendar.MONTH, month);
        mCalendar.set(Calendar.DAY_OF_MONTH, day);
        mCalendar.set(Calendar.HOUR_OF_DAY, 0);
        mCalendar.set(Calendar.MINUTE, 0);
        mCalendar.set(Calendar.SECOND, 0);
        mCalendar.set(Calendar.MILLISECOND, 0);

        long dayFirst = mCalendar.getTimeInMillis();
        long dayEnd = dayFirst + DAY - 1;

//        mCalendar.set(Calendar.YEAR, year);
//        mCalendar.set(Calendar.MONTH, month);
//        mCalendar.set(Calendar.DAY_OF_MONTH, day - 1);
//        mCalendar.set(Calendar.HOUR_OF_DAY, 0);
//        mCalendar.set(Calendar.MINUTE, 0);
//        mCalendar.set(Calendar.SECOND, 0);
//        mCalendar.set(Calendar.MILLISECOND, 0);

        long yesterdayFirst = dayFirst - DAY;
        long yesterdayEnd = dayFirst - 1;

        HcLog.D("IMUtil #getChatDate dayFirst = "+dayFirst + " currentTime = "+currentTime);

        if (currentTime >= dayFirst && currentTime <= dayEnd) {
            // 说明是今天
            return HcUtil.getDate("HH:mm", currentTime);
        } else if (currentTime >= yesterdayFirst && currentTime <= yesterdayEnd) {
            if (mWeeks == null) {
                mWeeks = context.getResources().getStringArray(R.array.main_weeks);
            }
//            return context.getResources().getString(R.string.im_yesterday_time, HcUtil.getDate("HH:mm", currentTime));
            return mWeeks[8] + " " + HcUtil.getDate("HH:mm", currentTime);
        } else {

            switch (week) {
                case Calendar.SUNDAY: // 可以显示周一到周五
                    if (currentTime >= dayFirst - DAY * 6) {
                        return getDayOfWeek(context, currentTime) + " " + HcUtil.getDate("HH:mm", currentTime);
                    } else {
                        return HcUtil.getDate("MM月dd日 HH:mm", currentTime);
                    }

                case Calendar.MONDAY: // 不可能显示星期
                case Calendar.TUESDAY: // 不可能显示星期
                    return HcUtil.getDate("MM月dd日 HH:mm", currentTime);

                case Calendar.WEDNESDAY: // 可以显示周一
                    if (currentTime >= dayFirst - DAY * 2) {
                        return getDayOfWeek(context, currentTime) + " " + HcUtil.getDate("HH:mm", currentTime);
                    } else {
                        return HcUtil.getDate("MM月dd日 HH:mm", currentTime);
                    }

                case Calendar.THURSDAY: // 可以显示周一,周二
                    if (currentTime >= dayFirst - DAY * 3) {
                        return getDayOfWeek(context, currentTime) + " " + HcUtil.getDate("HH:mm", currentTime);
                    } else {
                        return HcUtil.getDate("MM月dd日 HH:mm", currentTime);
                    }

                case Calendar.FRIDAY: // 可以显示周一到周三
                    if (currentTime >= dayFirst - DAY * 4) {
                        return getDayOfWeek(context, currentTime) + " " + HcUtil.getDate("HH:mm", currentTime);
                    } else {
                        return HcUtil.getDate("MM月dd日 HH:mm", currentTime);
                    }

                case Calendar.SATURDAY: // 可以显示周一到周四
                    if (currentTime >= dayFirst - DAY * 5) {
                        return getDayOfWeek(context, currentTime) + " " + HcUtil.getDate("HH:mm", currentTime);
                    } else {
                        return HcUtil.getDate("MM月dd日 HH:mm", currentTime);
                    }

                default:
                    return HcUtil.getDate("MM月dd日 HH:mm", currentTime);
            }

        }
    }

    /**
     * 得到一个SpanableString对象，通过传入的字符串,并进行正则判断
     *
     * @param context
     * @param str
     * @return
     */
    public static SpannableString getExpressionString(Context context, String str, int smileyHeight) {
        SpannableString spannableString = new SpannableString(str);
        // 正则表达式比配字符串里是否含有表情，如： 我好[开心]啊
        String zhengze = "\\[[^\\]]+\\]";
        // 通过传入的正则表达式来生成一个pattern
        Pattern sinaPatten = Pattern.compile(zhengze, Pattern.CASE_INSENSITIVE);
        try {
            dealExpression(context, spannableString, sinaPatten, 0, smileyHeight);
        } catch (Exception e) {
            HcLog.D("IMUtil #getExpressionString dealExpression " + e.getMessage());
        }
        return spannableString;
    }

    /**
     * 对SpanableString进行正则判断，如果符合要求，则以表情图片代替
     * <p>SpanableString的flag说明：它是用来标识在 Span 范围内的文本前后输入新的字符时是否把它们也应用这个效果.</p>
     * <p>分别有 Spanned.SPAN_EXCLUSIVE_EXCLUSIVE(前后都不包括)</p>
     * <p>Spanned.SPAN_INCLUSIVE_EXCLUSIVE(前面包括，后面不包括)</p>
     * <p>Spanned.SPAN_EXCLUSIVE_INCLUSIVE(前面不包括，后面包括)</p>
     * <p>Spanned.SPAN_INCLUSIVE_INCLUSIVE(前后都包括)</p>
     * <p>BackgroundColorSpan 背景色;
     * ClickableSpan 文本可点击，有点击事件;
     * ForegroundColorSpan 文本颜色（前景色）;
     * MaskFilterSpan 修饰效果，如模糊(BlurMaskFilter)、浮雕(EmbossMaskFilter);
     * MetricAffectingSpan 父类，一般不用;
     * RasterizerSpan 光栅效果;
     * StrikethroughSpan 删除线（中划线）;
     * SuggestionSpan 相当于占位符;
     * UnderlineSpan 下划线;
     * AbsoluteSizeSpan;
     * DynamicDrawableSpan 设置图片，基于文本基线或底部对齐;
     * ImageSpan 图片;
     * RelativeSizeSpan 相对大小（文本字体）;
     * ReplacementSpan 父类，一般不用;
     * ScaleXSpan 基于x轴缩放;
     * StyleSpan 字体样式：粗体、斜体等;
     * SubscriptSpan 下标（数学公式会用到）;
     * SuperscriptSpan 上标（数学公式会用到）;
     * TextAppearanceSpan 文本外貌（包括字体、大小、样式和颜色）;
     * TypefaceSpan 文本字体;
     * URLSpan 文本超链接</p>
     * @param context
     * @param spannableString
     * @param patten
     * @param start
     * @throws Exception
     */
    private static void dealExpression(Context context,
                                SpannableString spannableString, Pattern patten, int start, int smileyHeight)
            throws Exception {
        Matcher matcher = patten.matcher(spannableString);
        while (matcher.find()) {
            String key = matcher.group();
            // 返回第一个字符的索引的文本匹配整个正则表达式,ture 则继续递归
            if (matcher.start() < start) {
                continue;
            }
            key = key.substring(1, key.length() - 1);
            String value = null;
            if (mSmileys.contains(key))
                value = key;//emojiMap.get(key);
            if (TextUtils.isEmpty(value)) {
                continue;
            }
            int resId = context.getResources().getIdentifier(value, "drawable",
                    context.getPackageName());
            // 通过上面匹配得到的字符串来生成图片资源id
            // Field field=R.drawable.class.getDeclaredField(value);
            // int resId=Integer.parseInt(field.get(null).toString());
            if (resId != 0) {
                Bitmap bitmap = BitmapFactory.decodeResource(
                        context.getResources(), resId);
                if (bitmap == null) continue;

                Bitmap scanBitmap = Bitmap.createScaledBitmap(bitmap, HcUtil.dip2px(context,smileyHeight),  HcUtil.dip2px(context,smileyHeight), true);
                bitmap.recycle();
                bitmap = null;
                // 通过图片资源id来得到bitmap，用一个ImageSpan来包装
                ImageSpan imageSpan = new ImageSpan(context, scanBitmap);
                // 计算该图片名字的长度，也就是要替换的字符串的长度
                int end = matcher.start() + key.length() + 2;
                // 将该图片替换字符串中规定的位置中
                spannableString.setSpan(imageSpan, matcher.start(), end,
                        Spannable.SPAN_INCLUSIVE_EXCLUSIVE);
                if (end < spannableString.length()) {
                    // 如果整个字符串还未验证完，则继续。。
                    dealExpression(context, spannableString, patten, end, smileyHeight);
                }
                break;
            }
        }
    }

    /**
     * 将文件转成base64 字符串
     * @param path 文件路径
     * @return
     * @throws Exception
     */

    public static String encodeBase64File(String path) {
        String data = Base64.encodeFromFile(path);
        if (data == null)
            data = "";
        return data;

//        FileInputStream in = null;
//        try {
//            File file = new File(path);
//            in = new FileInputStream(file);
//            byte[] data = new byte[in.available()];
//            in.read(data);
//            Base64.encodeBytes(data);
//        } catch (Exception e) {
//
//        } finally {
//            if (in != null) {
//                try {
//                    in.close();
//                } catch(Exception e) {
//
//                }
//
//            }
//        }
//
//        return "";
    }

    /**
     * 将base64字符解码保存文件
     * @param base64Code
     * @return 文件存储的路径
     */

    public static String decoderBase64File(String base64Code, String fileDir) {
        String filePath = null;
        byte[] data = Base64.decode(base64Code);
        if (data != null) {
            File file = new File(HcApplication.getAppDownloadPath(), IM_DIR);
            if (!file.exists())
                file.mkdir();
            file = new File(file.getAbsolutePath(), fileDir);
            if (!file.exists())
                file.mkdir();
            file = new File(file, System.currentTimeMillis() + ".aac");
            FileOutputStream os = null;
            try {
                os = new FileOutputStream(file);
                os.write(data);
                os.flush();
                filePath = file.getAbsolutePath();
            } catch(Exception e) {

            } finally {
                if (os != null) {
                    try {
                        os.close();
                    } catch(Exception e) {

                    }
                }

            }
        }

        return filePath;

    }

    private static final String IM_DIR = "im";

    public static Bitmap getRoundCornerImage(Bitmap bg, Bitmap src, String messageId) {
        int width = src.getWidth();
        int height = src.getHeight();
        HcLog.D("IMUtil #getRoundCornerImage before scale width = "+width + " height = "+height);
        float scaleW = IMAGE_MAX_WIDTH / (float) width;
        float scaleH = IMAGE_MAX_HEIGHT / (float) height;
        if (scaleW < 1.0f) { // 说明超过了最大宽度
            if (scaleH < scaleW) { // 说明超过了最大高度,而且高度>宽度
                width = (int) (width * scaleH);
                height = IMAGE_MAX_HEIGHT;
            } else {
                width = IMAGE_MAX_WIDTH;
                height = (int) (height * scaleW);
            }
        } else if (scaleH < 1.0f) { // 这里scaleH肯定 < scaleW
            width = (int) (width * scaleH);
            height = IMAGE_MAX_HEIGHT;
        }
        HcLog.D("IMUtil #getRoundCornerImage after scale width = "+width + " height = "+height);
        Bitmap roundConcerImage = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(roundConcerImage);
        Paint paint = new Paint();
        Rect rect = new Rect(0, 0, width, height);
        Rect rectF = new Rect(0, 0, src.getWidth(), src.getHeight());
        paint.setAntiAlias(true);
        NinePatch patch = new NinePatch(bg, bg.getNinePatchChunk(), null);
        patch.draw(canvas, rect);
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
        canvas.drawBitmap(src, rectF, rect, paint);
//        bg.recycle(); // 存放在缓存里了
        src.recycle();
        mImageCache.put(messageId, roundConcerImage);
        return roundConcerImage;
    }

    public static Bitmap getRoundCornerImage(Context context, int bgResId, Bitmap src, String messageId) {

        Bitmap bg = mBgCache.get(bgResId);
        if (bg == null) {
            bg = BitmapFactory.decodeResource(context.getResources(), bgResId);
            mBgCache.put(bgResId, bg);
        }


        return getRoundCornerImage(bg, src, messageId);
    }

    private static DisplayImageOptions mOptions;

    public static DisplayImageOptions getImageOptions() {
        if (mOptions == null) {
            mOptions = new DisplayImageOptions.Builder()
                    .imageScaleType(ImageScaleType.EXACTLY)
                    .cacheInMemory(false)
                    .cacheOnDisk(false)
                    .considerExifParams(true)
                    .bitmapConfig(Bitmap.Config.ARGB_8888).build();
        }

        return mOptions;
    }

    private static String mImDir;

    /**
     * 判断缩略图是否存在
     * @param filePath 原文件的绝对路径
     * @param fileDir 聊天对象的文件夹
     * @return  缩略图的文件
     */
    public static File fileExist(String filePath, String fileDir) {
        if (mImDir == null) {
            File cacheDir = StorageUtils.getCacheDirectory(HcApplication.getContext());
            File im = new File(cacheDir, IM_DIR);
            if (!im.exists()) {
                im.mkdir();
            }
            mImDir = im.getAbsolutePath();
        }
        File image = new File(mImDir, fileDir);
        if (!image.exists()) {
            image.mkdir();
        }
        image = new File(image, "image");
        if (!image.exists()) {
            image.mkdir();
        }
        image = new File(image, HcUtil.getMD5String(filePath) + "." + getExtByPath(filePath));
        return image;
    }

    /**
     * 获取文件的扩展名
     * @param filePath
     * @return 文件的扩展名
     */
    private static String getExtByPath(String filePath) {
        int position = filePath.lastIndexOf('.');
        String ext = filePath.substring(position + 1, filePath.length());
        HcLog.D("IMUtil#getExtByPath filePath ="+filePath + " position = "+position + " ext = "+ext);
        return ext;
    }

    /**
     * 保存图片
     * @param image 保存到的目的文件
     * @param src 需要保存的文件
     */
    public static Bitmap saveImage(File image, Bitmap src) {
        int width = src.getWidth();
        int height = src.getHeight();
        HcLog.D("IMUtil #saveImage before scale width = "+width + " height = "+height);
        float scaleW = IMAGE_MAX_WIDTH / (float) width;
        float scaleH = IMAGE_MAX_HEIGHT / (float) height;
        if (scaleW < 1.0f) { // 说明超过了最大宽度
            if (scaleH < scaleW) { // 说明超过了最大高度,而且高度>宽度
                width = (int) (width * scaleH);
                height = IMAGE_MAX_HEIGHT;
            } else {
                width = IMAGE_MAX_WIDTH;
                height = (int) (height * scaleW);
            }
        } else if (scaleH < 1.0f) { // 这里scaleH肯定 < scaleW
            width = (int) (width * scaleH);
            height = IMAGE_MAX_HEIGHT;
        }

        Bitmap scaleSrc = Bitmap.createScaledBitmap(src, width, height, true);
        HcLog.D("IMUtil #saveImage after scale width = "+width + " height = "+height);
        BufferedOutputStream os = null;
        try {
            os = new BufferedOutputStream(new FileOutputStream(image));
            scaleSrc.compress(Bitmap.CompressFormat.JPEG, 100, os);
            os.flush();
        } catch(Exception e) {
            HcLog.D(TAG + " #saveImage error = "+e);
        } finally {
            if (scaleSrc != null && scaleSrc != src && !src.isRecycled()) {
                src.recycle();
                src = null;
            }
            if (os != null) {
                try {
                    os.close();
                } catch(Exception e) {

                }
            }

        }

        return scaleSrc;
    }

    /**
     * 将base64字符解码保存文件
     * @param base64Code
     * @param fileDir 聊天的对象的文件夹
     * @param fileName 文件名字
     * @return 被保存文件的路径
     */

    public static String decoderBase64ImageFile(String base64Code, String fileDir, String fileName) {

        File image = fileExist(fileName, fileDir);
        if (!image.exists()) {
            byte[] data = Base64.decode(base64Code);
            FileOutputStream os = null;
            try {
                os = new FileOutputStream(image);
                os.write(data);
                os.flush();
            } catch(Exception e) {

            } finally {
                if (os != null) {
                    try {
                        os.close();
                    } catch(Exception e) {

                    }
                }

            }
        }
        return image.getAbsolutePath();

    }

    /**
     *
     * @param context
     * @param message 接收到的消息
     * @param callback 消息的接收回调
     */
    public static void parseMessage(Context context, Message message, OnReceiverCallback callback) {
        parseMessage(context, message, null, null, null, callback);
    }


    /**
     *
     * @param context
     * @param message 接收到的消息
     * @param name 单聊时为当前界面聊天对象的名字,不一定为当前message的发送者;群聊时为当前界面群的名字;不在聊天界面为空
     * @param userId 单聊时为聊天对象的userId;群聊时为群的jid;不在聊天界面为空
     * @param callback 消息的接收回调
     * @param title 消息列表项的标题
     */
    public static void parseMessage(Context context, Message message, String name, String title, String userId, OnReceiverCallback callback) {
        Message.Type messageType = message.getType();
        String from = message.getFrom();
        if (TextUtils.isEmpty(from)) { // 出错,应该不会出错.
            HcLog.D(TAG + "#parseMessage  error 没有发送方！！！！！");
        } else {
            String mediaType = message.getMediaType();
            // 普通文本,可能是资源名
            String content = message.getBody(); // 显示的内容
            String attachment = null; // 语音或者图片附件
            String fromUserId = null; // 单聊或者群聊的消息发送方
            String chatId = null; //
            String receiver = null;
            int type = 1;
            int duration = 0;
            boolean add = true;
            HcLog.D(TAG + "#parseMessage before sub from = "+from);
            switch (messageType) {
                case normal: // 系统消息

                    notifySystemMessage(context, content, callback);

                    return;
                case chat:
                    from = from.substring(0, from.indexOf("@")); // 只用userId
                    fromUserId = from;
                    chatId = HcUtil.getMD5String(SettingHelper.getUserId(context) + fromUserId);
                    break;
                case groupchat: // 群组的from不需要更改,用全jid
                    if (from.contains("/"))
                        from = from.substring(0, from.indexOf("/"));
                    fromUserId = message.getUserId();
                    chatId = from;
                    HcLog.D(TAG + "#parseMessage fromUserId = "+fromUserId);
                    if (SettingHelper.getUserId(context).equals(fromUserId)) {
                        HcLog.D(TAG + "#parseMessage 群聊 接收到了自己的群聊消息！！！！！");
                        return;
                    }
                    receiver = message.getReceiver();
                    /** 这里放到下面处理了
                    if (title == null) {
                        // 根据jid先去群组数据库里面查找
                        title = ChatOperatorDatabase.getTitleByJid(context, from);
                        if (title == null)
                            title = from.substring(0, from.indexOf("@"));
                    }*/
                    break;

                default:
                    return;
            }

            HcLog.D(TAG + "#parseMessage after sub from = "+from);

            if (!TextUtils.isEmpty(mediaType)) {
                HcLog.D(TAG + "#parseMessage mediaType = "+mediaType);
                type = Integer.valueOf(mediaType);
                attachment = message.getAttachment();

                if (type == 3) {
                    duration = Integer.valueOf(message.getDuration());
                }

            }

            HcLog.D(TAG + " #parseMessage message content = "+content + " type = "+type + " duration = "+duration + " userId = "+userId);
            if (callback != null && callback instanceof ChatActivity) {
                add = from.equals(userId); // 判断在聊天界面是否需要显示消息
                if (!add) { // 说明不是当前界面的聊天对象发的
                    title = null;
                    name = null;
                }
            } else { // 理论上这里的name和title都为空的,为了保险还是重新设置下
                title = null;
                name = null;
            }

            if (name == null) {
                name = ChatOperatorDatabase.getNameByUserId(context, fromUserId);
            }

            if (title == null) {
                if (messageType == groupchat) {
                    // 根据jid先去群组数据库里面查找
                    title = ChatOperatorDatabase.getTitleByJid(context, from);
                    if (title == null) // 这里会有误差,但不能避免.
                        title = from.substring(0, from.indexOf("@")); // 群的名字
                } else {
                    title = name;
                }

            }

            HcLog.D(TAG + "#parseMessage name = "+name + " fromUserId ="+fromUserId + " add = "+add + " type = "+type + " attachment = "+attachment
                    + " duration = "+duration + " chatId ="+chatId + " messageType ="+mediaType + " callback = "+callback + " receiver = "+receiver);
            setReceiverData(context, name, title, fromUserId, content, add, type, attachment, duration, chatId, messageType, callback, receiver);

        }
    }


    /**
     *
     * @param name 发送方的名字
     * @param userId 发送方的userId,不一定是当前聊天的对象的userId,群聊则为全jid
     * @param content 发送的内容
     * @param add 是否需要添加到当前列表,true,添加;不是在聊天界面的话都为true
     * @param type 消息内容类型 1.文本;2.图片;3.语音;4.文件
     * @param attachment 语音或者图片的Base64编码文件
     * @param duration 语音的播放时间
     * @param chatId 消息保存在数据库里ID
     * @param messageType 消息类型
     * @param callback
     * @param title 消息列表项的标题
     * @param receiver 群聊@的对象
     * @see ChatMessageInfo
     * @see AppMessageInfo
     */
    private static void setReceiverData(Context context, String name, String title, String userId, String content, boolean add, int type, String attachment, int duration,
                                        String chatId, Message.Type messageType, OnReceiverCallback callback, String receiver) {
        HcLog.D(TAG + " #setReceiverData name = "+name + " userId = "+userId + " content = "+content + " add = "+add + " type = "+type);
        int size = ChatOperatorDatabase.getChatCount(context, chatId); //聊天对象的消息条数

        long date = System.currentTimeMillis();
        ChatMessageInfo info = null;
        switch (type) {
            case 1:
                info = new ChatTextOtherInfo();
                break;
            case 2:
                info = new ChatImageOtherInfo();
                info.setFilePath(IMUtil.decoderBase64ImageFile(attachment, userId, content));
                content = "[图片]";
                break;
            case 3:
                content = "[语音]";
                info = new ChatVoiceOtherInfo();
                info.setDuration(duration);
                info.setReaded(false);
                info.setFilePath(IMUtil.decoderBase64File(attachment, userId));
                break;
            case 4:
                content = "[文件]";
                break;

            default:
                info = new ChatTextOtherInfo();
                break;
        }
        info.setName(name);
        info.setUserId(userId);
        info.setChatId(chatId);
        info.setContent(content);
        info.setDate("" + date);
        info.setReceiver(receiver);
        if (size > 0) {
            ChatMessageInfo last = ChatOperatorDatabase.getLastMessage(context, chatId); // 最后一条消息
            if (last != null) {
                int num = Integer.valueOf(last.getMessageId()) + 1;
                info.setMessageId(num + "");
                long oldDate = Long.valueOf(last.getDate());
                if (oldDate + 5 * 60 * 1000 > date) { // 未超过5分钟
                    info.setShowDate(false);
                } else {
                    info.setShowDate(true);
                }
            } else {
                HcLog.D(TAG + " #setReceiverData 出错！！！！！ size > 0 但是数据为null ");
                info.setMessageId("" + 0);
                info.setShowDate(true);
            }

        } else {
            info.setMessageId("" + 0);
            info.setShowDate(true);
        }


        ChatOperatorDatabase.insertChatMessage(context, info);

        // 更新主页的消息
        AppMessageInfo appInfo = new AppMessageInfo();
        appInfo.setDate("" + date);
        appInfo.setContent(content);
        appInfo.setId(chatId);
        appInfo.setTitle(title);
        boolean hasReceiver = false;
        if (messageType == Message.Type.chat) {
            appInfo.setType(3);
            appInfo.setIconUri(userId);
        } else if (messageType == groupchat) {
            appInfo.setContent(name + ": " +content);
            appInfo.setType(2);
            appInfo.setIconUri("drawable://" + R.drawable.im_chat_group_icon);
            // 群的@处理放到下面处理
        } else {
            appInfo.setIconUri(userId);
            appInfo.setType(3);
        }
//        ChatOperatorDatabase.updateOrinsertAppMessage(context, appInfo);
        HcLog.D(TAG + " #setReceiverData end name = "+name + " userId = "+userId + " content = "+content + " add = "+add + " type = "+type);
        boolean notify = true;
        if (!add) { // 肯定在聊天界面,但接收到其他人的或者群的信息
            appInfo.setCount(1);
            // 这里要判断要是是群聊的话是否禁用了通知
            if (messageType == groupchat) {
                ChatGroupMessageInfo groupInfo = ChatOperatorDatabase.getChatGroupInfo(context, chatId);
                if (groupInfo != null) {
                    if (groupInfo.isNoticed()) { // 消息免打扰
                        notify = false;
                    }
                }
            }


            // 通知聊天界面更新角标数,这里到时可以移动到下面去
            if(callback != null) {
                callback.onReceiver(null, appInfo);
            }

        } else {
            if (callback == null || !(callback instanceof ChatActivity)) {
                appInfo.setCount(1);
                // 这里要判断要是是群聊的话是否禁用了通知
                if (messageType == groupchat) {
                    ChatGroupMessageInfo groupInfo = ChatOperatorDatabase.getChatGroupInfo(context, chatId);
                    if (groupInfo != null) {
                        if (groupInfo.isNoticed()) { // 消息免打扰
                            notify = false;
                        }
                    }
                }
            } else {
                notify = false;
            }
        }
        ChatOperatorDatabase.updateOrinsertAppMessage(context, appInfo);

        if (notify && !TextUtils.isEmpty(receiver)) {
            String currentUser = SettingHelper.getUserId(context);
            String[] userIds = receiver.split(";");
            for (String id : userIds) {
                if (id.equals(currentUser)) {
                    IMSettings.setIMReceiverGroup(context, chatId);
                    hasReceiver = true;
                    break;
                }
            }

        }


        if (add && callback != null) {

            callback.onReceiver(info, appInfo);

        }

        // notify放在存储的后面,确保状态栏上点击有数据
        if (notify) {
            notification(context, appInfo, hasReceiver);
        }

    }

    public static String getServerName() {
        if (mServerName == null)
            mServerName = HcConfig.getConfig().getServerName(HcConfig.Module.IM);
        return mServerName;
    }

    public static int getServerPort() {
        if (mServerPort == -1)
            mServerPort = HcConfig.getConfig().getServerPort(HcConfig.Module.IM);
        return mServerPort;
    }

    public static String getMessageDate(Context context, long currentTime) {
        if (mCalendar == null) {
            mCalendar = Calendar.getInstance();
        }
        mCalendar.setTimeInMillis(System.currentTimeMillis()); // 确保是最新的时间
        int year = mCalendar.get(Calendar.YEAR);
        int month = mCalendar.get(Calendar.MONTH);
        int day = mCalendar.get(Calendar.DAY_OF_MONTH);
        int week = mCalendar.get(Calendar.DAY_OF_WEEK);
        // 设置今天的最初时间
        mCalendar.set(Calendar.YEAR, year);
        mCalendar.set(Calendar.MONTH, month);
        mCalendar.set(Calendar.DAY_OF_MONTH, day);
        mCalendar.set(Calendar.HOUR_OF_DAY, 0);
        mCalendar.set(Calendar.MINUTE, 0);
        mCalendar.set(Calendar.SECOND, 0);
        mCalendar.set(Calendar.MILLISECOND, 0);

        long dayFirst = mCalendar.getTimeInMillis();
        long dayEnd = dayFirst + DAY - 1;


        long yesterdayFirst = dayFirst - DAY;
        long yesterdayEnd = dayFirst - 1;

        HcLog.D("IMUtil #getMessageDate dayFirst = "+dayFirst + " currentTime = "+currentTime);

        if (currentTime >= dayFirst && currentTime <= dayEnd) {
            // 说明是今天
            return HcUtil.getDate("HH:mm", currentTime);
        } else if (currentTime >= yesterdayFirst && currentTime <= yesterdayEnd) {
            if (mWeeks == null) {
                mWeeks = context.getResources().getStringArray(R.array.main_weeks);
            }
//            return context.getResources().getString(R.string.im_yesterday_time, HcUtil.getDate("HH:mm", currentTime));
            return mWeeks[8];
        } else {
            return HcUtil.getDate(HcUtil.FORMAT_MONTH, currentTime);
        }
    }

    private static void notification(Context context, AppMessageInfo info, boolean hasReceiver) {
        if (mManager == null) {
            mManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        }
        if (mBuilder == null) {
//				Intent intent = new Intent();
//				mIntent = PendingIntent.getBroadcast(context, 0, intent,
//						PendingIntent.FLAG_UPDATE_CURRENT);
            mBuilder = new Notification.Builder(context)
                    .setSmallIcon(context.getResources().getIdentifier("app_icon", "drawable", context.getPackageName()));
//						.setContentIntent(mIntent)
//                        .setTicker("您有一条新的消息!")
//                        .setContentTitle("");

        }

        List<Object> list = splitEmojiString(context, info.getContent());
        int size = list.size();
        Object o;
        if (size > 0) {
            Intent intent = new Intent(/*context, ChatActivity.class*/IMPushReceiver.ACTION_CHAT_MESSAGE);
            intent.setPackage(context.getPackageName());
            intent.putExtra("emp", info);
            intent.putExtra("appId", getIMAppId(context));
            switch (info.getType()) {
                case 1: // 应用模块,系统消息

                    break;
                case 2: // 群聊消息
                    intent.putExtra("group", true);

                    break;
                case 3: // 单聊消息
                    intent.putExtra("group", false);
                    break;

                default:
                    break;
            }
            PendingIntent pIntent = PendingIntent.getBroadcast(context, 0, intent,
						PendingIntent.FLAG_UPDATE_CURRENT);
            mBuilder.setContentIntent(pIntent);
            mBuilder.setAutoCancel(true);
            if (mRemoteViews != null) {
                mRemoteViews.removeAllViews(R.id.im_notification_content_parent); // 不起作用
                mRemoteViews = null;
                mManager.cancel(NOTIFY_ID);
            }


            String content = info.getContent();
            SpannableString spannableString = null;
            if (hasReceiver) {
                String s = "[有人@我] ";
                spannableString = new SpannableString(s + content);
                spannableString.setSpan(new ForegroundColorSpan(Color.RED), 0, s.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            }
            if (size == 1) {
                o = list.get(0);
                if (o instanceof String) { // 说明只有字符串
                    mBuilder.setContent(null);
                    mBuilder.setTicker(hasReceiver ? spannableString : content);
                    mBuilder.setDefaults(Notification.DEFAULT_SOUND);
//                    mBuilder.setSound() // 格式为“file:///mnt/sdcard/Xxx.mp3”
                    mBuilder.setContentTitle(info.getTitle());
                    mBuilder.setContentText(hasReceiver ? spannableString : content);
                    mManager.notify(NOTIFY_ID, mBuilder.getNotification());
                } else { // 说明只有一个表情

                    mBuilder.setTicker(content);
                    mBuilder.setDefaults(Notification.DEFAULT_SOUND);
                    mBuilder.setContent(getRemoteViews(context, list, info.getTitle()));
                    mManager.notify(NOTIFY_ID, mBuilder.getNotification());
                }
            } else { // 说明肯定有一个表情
                mBuilder.setTicker(hasReceiver ? spannableString : content);
                mBuilder.setDefaults(Notification.DEFAULT_SOUND);
                if (hasReceiver) {
                    String s = "[有人@我] ";
                    spannableString = new SpannableString(s);
                    spannableString.setSpan(new ForegroundColorSpan(Color.RED), 0, s.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                    list.add(0, spannableString);
                }
                mBuilder.setContent(getRemoteViews(context, list, info.getTitle()));
                mManager.notify(NOTIFY_ID, mBuilder.getNotification());
            }
        }

    }

    /**
     * 把Emoji的Unicode编码转化成字符串
     * @param unicode Emoji的Unicode编码
     * @return Emoji的字符串
     */
    public static String getEmojiByUnicode(int unicode) {
        return new String(Character.toChars(unicode));
    }


    private static List<Object> splitEmojiString(Context context, String content) {
        List<Object> emojis = new ArrayList<Object>();
        // 正则表达式比配字符串里是否含有表情，如： 我好[开心]啊
        String zhengze = "\\[[^\\]]+\\]";
        // 通过传入的正则表达式来生成一个pattern
        Pattern patten = Pattern.compile(zhengze, Pattern.CASE_INSENSITIVE);

        splitEmojiString(context, content, patten, 0, emojis);
        HcLog.D(TAG + "#splitEmojiString emojis size = "+emojis.size());
        return emojis;
    }

    private static void splitEmojiString(Context context, String content, Pattern patten, int start, List<Object> list) {
        Matcher matcher = patten.matcher(content);
        boolean find = false;
        while (matcher.find()) {
            String key = matcher.group();
            // 返回第一个字符的索引的文本匹配整个正则表达式,ture 则继续递归
            if (matcher.start() < start) {
                continue;
            }
            key = key.substring(1, key.length() - 1); // 已经去除了[]
            String value = null;
            if (mSmileys.contains(key))
                value = key;
            if (TextUtils.isEmpty(value)) {
                continue;
            }
            int resId = context.getResources().getIdentifier(value, "drawable",
                    context.getPackageName());
            // 通过上面匹配得到的字符串来生成图片资源id
            // Field field=R.drawable.class.getDeclaredField(value);
            // int resId=Integer.parseInt(field.get(null).toString());
            if (resId != 0) {
                find = true;
//                Drawable src = context.getResources().getDrawable(resId);
//                if (src == null) {
//                    continue;
//                }
                if (matcher.start() > 0) {
                    String front = content.substring(0, matcher.start());
                    HcLog.D(TAG + " #splitEmojiString front = "+front);
                    list.add(front);
                }
                list.add(resId);
                // 计算该图片名字的长度，也就是要替换的字符串的长度
                int end = matcher.start() + key.length() + 2;
                if (end < content.length()) {
                    content = content.substring(end, content.length());
                    HcLog.D(TAG + " #splitEmojiString content = "+content);
                    splitEmojiString(context, content, patten, 0, list);
                }

                break;
            }
        }

        if (!find) {
            HcLog.D(TAG + "#splitEmojiString not find contnet = "+content);
            list.add(content);
        }
    }

    private static RemoteViews getRemoteViews(Context context, List<Object> list, String title) {
        RemoteViews rv = new RemoteViews(context.getPackageName(), R.layout.im_notification_layout);
        int size = list.size();
        Object o;
        RemoteViews content;
        for (int i = 0; i < size; i++) {
            o = list.get(i);
            if (o instanceof CharSequence) {
                content = new RemoteViews(context.getPackageName(), R.layout.im_notification_item_text);
                content.setTextViewText(R.id.im_notification_text, (CharSequence) o);
                rv.addView(R.id.im_notification_content_parent, content);
            } else if (o instanceof Integer) {
                content = new RemoteViews(context.getPackageName(), R.layout.im_notification_item_image);
                content.setImageViewResource(R.id.im_notification_image, (Integer) o);
                rv.addView(R.id.im_notification_content_parent, content);
            }
        }
        rv.setTextViewText(R.id.im_notification_title, title);
        rv.setImageViewResource(R.id.im_notification_icon, context.getResources().getIdentifier("app_icon", "drawable", context.getPackageName()));
//        rv.setCharSequence(R.id.im_notification_title, "setText", title);
        mRemoteViews = rv;
        return rv;
    }

    public static void clearImageCache() {
        Iterator<Bitmap> iterator = mImageCache.values().iterator();
        if (iterator.hasNext()) {
            iterator.next().recycle();
        }
        mImageCache.clear();
    }

    public static Bitmap getImage(String messageId) {
        return mImageCache.get(messageId);
    }


    private static void notifySystemMessage(Context context, String jsonContent, OnReceiverCallback callback) {
        if (mManager == null) {
            mManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        }
        if (mBuilder == null) {
            mBuilder = new Notification.Builder(context)
                    .setSmallIcon(context.getResources().getIdentifier("app_icon", "drawable", context.getPackageName()));

        }

        String title = null;
        String description = null;
        String content = null;
        try {
            JSONObject object = new JSONObject(jsonContent);
            if (HcUtil.hasValue(object, "title")) {
                title = object.getString("title");
            } else {
                HcLog.D(TAG + " #notifySystemMessage 缺少Title jsonContent = "+jsonContent);
                return;
            }
            if (HcUtil.hasValue(object, "description")) {
                description = object.getString("description");
            } else {
                HcLog.D(TAG + " #notifySystemMessage 缺少description jsonContent = "+jsonContent);
                return;
            }
            if (HcUtil.hasValue(object, "body")) {
                content = object.getJSONObject("body").toString();
            } else {
                HcLog.D(TAG + " #notifySystemMessage 缺少body jsonContent = "+jsonContent);
                return;
            }

            // 保存数据,这里可能需要更改,需要判断是否需要保存消息,有些消息可能不需要保存
            SystemMessage message = new SystemMessage();
            PushInfo info = new PushInfo(content);
            message.setAppId(info.getAppId());
            message.setContentId(info.getContent()); // 存放消息内容
            message.setDate("" + System.currentTimeMillis());
            message.setReaded(false);
            message.setType(info.getType());
            message.setContent(description);
            message.setTitle(title);
            OperateDatabase.insertSysMessage(message, context);

            // 通知更新,在IM的系统消息模块的具体消息列表里面
            HcPushManager.getInstance().notifyUpdateMessage();

            // 增加模块消息,点击系统消息才会显示,二级列表
            AppMessageInfo messageInfo = ChatOperatorDatabase.getAppMessageInfo(context, message.getAppId());
            if (messageInfo == null) {
                AppInfo appInfo = getAppInfo(context, message.getAppId());
                messageInfo = new AppMessageInfo();
                messageInfo.setTitle(title);
                messageInfo.setType(1);
                messageInfo.setId(message.getAppId());
                messageInfo.setIconUri(appInfo != null ? appInfo.mICon : "");
                messageInfo.setCount(1);
                messageInfo.setContent(description);
                messageInfo.setDate(message.getDate());
                ChatOperatorDatabase.insertAppMessage(context, messageInfo);

                // 更新系统消息,用于显示消息列表的第一条.

            } else {
                messageInfo.setCount(messageInfo.getCount() + 1);
                messageInfo.setContent(description);
                messageInfo.setDate(message.getDate());
                ChatOperatorDatabase.updateAppMessage(context, messageInfo);


            }
            // 更新系统消息,用于显示消息列表的第一条.
            messageInfo.setId(AppMessageInfo.SYSTEM_MESSAGE_ID);
            messageInfo.setCount(1);
            messageInfo.setType(4);
            messageInfo.setIconUri("drawable://" + R.drawable.im_system_message_icon);
            ChatOperatorDatabase.updateOrinsertAppMessage(context, messageInfo);

            if (callback != null) {
                callback.onReceiver(null, messageInfo);
            }

            // 通知更新,在IM的系统消息模块列表界面里面
            HcPushManager.getInstance().notifyUpdateMessage();

        } catch(Exception e) {
            HcLog.D(TAG + " #notifySystemMessage error =" + e + " jsonContent = "+jsonContent);
            return;
        }
        Intent intent = new Intent(IMPushReceiver.ACTION_SYSTEM_MESSAGE);
        intent.setPackage(context.getPackageName());
        intent.putExtra("content", content);

        PendingIntent pIntent = PendingIntent.getBroadcast(context, 0, intent,
                PendingIntent.FLAG_UPDATE_CURRENT);
        mBuilder.setContentIntent(pIntent);
        mBuilder.setAutoCancel(true);
        if (mRemoteViews != null) {
            mRemoteViews.removeAllViews(R.id.im_notification_content_parent); // 不起作用
            mRemoteViews = null;
            mManager.cancel(NOTIFY_ID);
        }
        mBuilder.setContent(null);
        mBuilder.setTicker(title + ":" + description);
        mBuilder.setDefaults(Notification.DEFAULT_SOUND);
//                    mBuilder.setSound() // 格式为“file:///mnt/sdcard/Xxx.mp3”
        mBuilder.setContentTitle(title);
        mBuilder.setContentText(description);
        mManager.notify(NOTIFY_ID, mBuilder.getNotification());

    }

    public static class AppInfo {

        public String mName;
        public String mAppId;
        public String mICon;
    }


    public static AppInfo getAppInfo(Context context, String appId) {
        List<MenuInfo> infos = new ArrayList<MenuInfo>(HcConfig.getConfig().getFirstMenus());
        MenuInfo info;
        Iterator<MenuInfo> iterator = infos.iterator();
        AppInfo appInfo = null;
        while (iterator.hasNext()) {
            info = iterator.next();
            if (info.getAppId().equals(appId)) {
                appInfo = new AppInfo();
                appInfo.mAppId = appId;
                appInfo.mName = info.getAppName();
                appInfo.mICon = "drawable://" + context.getResources().getIdentifier(info.getSelectAppIcon().substring(0, info.getSelectAppIcon().lastIndexOf(".")), "drawable", context.getPackageName());
                break;
            } else if (!"com.android.hcframe.container.ContainerMenuPage".equals(info.getClassName())) {
                iterator.remove();
            }

        }

        if (appInfo != null) {
            infos.clear();
        } else {
            iterator = infos.iterator();
            while (iterator.hasNext()) {
                info = iterator.next();
                appInfo = getAppInfoByContainer(context, appId, info.getAppId());
                if (appInfo != null)
                    break;
            }
        }


        return appInfo;
    }

    public static AppInfo getAppInfoByContainer(Context context, String appId, String containerId) {
        AppInfo appInfo = null;
        ViewInfo containerInfo = ContainerConfig.getInstance().getContainerInfo(context, containerId);
        if (containerInfo != null) {
            List<ViewInfo> viewInfos = containerInfo.getViewInfos(); // 模板
            for (ViewInfo viewInfo : viewInfos) {
                List<ViewInfo> apps = viewInfo.getViewInfos(); // 每个模版中的应用列表
                for (ViewInfo app : apps) {
                    if (app.getAppId().equals(appId)) {
                        List<ViewInfo> elements = app.getViewInfos();
                        for(ViewInfo element : elements) {
                            if ("img01".equals(element.getViewId())) {
                                appInfo = new AppInfo();
                                appInfo.mAppId = appId;
                                appInfo.mName = element.getAppName();
                                appInfo.mICon = element.getElementValue();
                                return appInfo;
                            }
                        }

                    } else if ("com.android.hcframe.container.ContainerMenuPage".equals(app.getViewAction())) {
                        appInfo = getAppInfoByContainer(context, appId, app.getAppId());
                        if (appInfo != null)
                            return appInfo;
                    }
                }

            }

        }

        return appInfo;
    }

    public static String getIMAppId(Context context) {
        String appId = IMSettings.getIMAppId(context);
        if (TextUtils.isEmpty(appId)) {
            List<MenuInfo> infos = new ArrayList<MenuInfo>(HcConfig.getConfig().getFirstMenus());
            MenuInfo info;
            Iterator<MenuInfo> iterator = infos.iterator();
            while (iterator.hasNext()) {
                info = iterator.next();
                if (info.getClouded() || !"com.android.hcframe.container.ContainerMenuPage".equals(info.getClassName())) {
                    iterator.remove();
                } else if ("com.android.hcframe.im.IMHomeMenuPage".equals(info.getClassName())) {
                    appId = info.getAppId();
                    break;
                }
            }

            if (!TextUtils.isEmpty(appId)) {
                infos.clear();
                IMSettings.setIMAppId(HcApplication.getContext(), appId);
            } else {
                iterator = infos.iterator();
                while (iterator.hasNext()) {
                    info = iterator.next();
                    ViewInfo containerInfo = ContainerConfig.getInstance().getContainerInfo(HcApplication.getContext(), info.getAppId());
                    if (containerInfo != null) {
                        List<ViewInfo> viewInfos = containerInfo.getViewInfos();
                        for (ViewInfo viewInfo : viewInfos) {
                            List<ViewInfo> apps = viewInfo.getViewInfos(); // 每个模版中的应用列表
                            for (ViewInfo app : apps) {
                                if ("com.android.hcframe.im.IMHomeMenuPage".equals(app.getViewAction())) {
                                    appId = app.getAppId();
                                    IMSettings.setIMAppId(HcApplication.getContext(), appId);
                                    return appId;
                                }
                            }
                        }

                    }
                }
            }

        }
        return appId;
    }

    public static SpannableString getGroupSpannable(Context context, String userId) {
        String spannableString = ChatOperatorDatabase.getNameByUserId(context, userId) + " ";
        return new SpannableString(spannableString);
    }

    public static boolean imEnabled() {
        return HcConfig.getConfig().assertModule(HcConfig.Module.IM);
    }
}
