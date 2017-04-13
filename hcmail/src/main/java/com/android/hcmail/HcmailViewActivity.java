package com.android.hcmail;

import android.app.Activity;
import android.app.DownloadManager;
import android.content.ActivityNotFoundException;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.database.ContentObserver;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.os.Parcelable;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.text.format.DateUtils;
import android.util.Log;
import android.util.Patterns;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.android.email.AttachmentInfo;
import com.android.email.Controller;
import com.android.email.ControllerResultUiThreadWrapper;
import com.android.email.Preferences;
import com.android.email.Throttle;
import com.android.email.activity.MessageViewFragmentBase;
import com.android.email.activity.RecentMailboxManager;
import com.android.email.mail.internet.EmailHtmlUtil;
import com.android.email.service.AttachmentDownloadService;
import com.android.email.view.RigidWebView;
import com.android.emailcommon.Logging;
import com.android.emailcommon.mail.Address;
import com.android.emailcommon.mail.MessagingException;
import com.android.emailcommon.provider.Account;
import com.android.emailcommon.provider.EmailContent;
import com.android.emailcommon.provider.Mailbox;
import com.android.emailcommon.utility.AttachmentUtilities;
import com.android.emailcommon.utility.EmailAsyncTask;
import com.android.emailcommon.utility.Utility;
import com.android.hcframe.HcBaseActivity;
import com.android.hcframe.HcDialog;
import com.android.hcframe.HcLog;
import com.android.hcframe.HcUtil;
import com.android.hcframe.TopBarView;
import com.android.hcframe.email.R;
import com.android.hcframe.hcmail.EmailUtils;

import org.apache.commons.io.IOUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by zhujiabin on 2017/3/20.
 */

public class HcmailViewActivity extends HcBaseActivity implements View.OnClickListener {

    private static final String TAG = "HcmailViewActivity";

    private static final String MESSAGE_ID = "messageId";

    private TopBarView mViewTopbar;
    private TextView mViewSend;
    private TextView mViewTo;
    private LinearLayout mCcParent;
    private LinearLayout mBccParent;
    private TextView mCc; // 抄送
    private TextView mBcc; // 秘送
    private TextView mViewDate;
    private TextView mMessageSubject;
    private ImageView mAttachmentBtn;

    // content
    private WebView mMessageContentView;
    private View mAttachmentsScroll;
    private LinearLayout mAttachments;

    /**
     * 底部按钮
     */
    private ImageView mViewMessageDelete;
    private View mEditParent;
    private ImageView mViewMessagePen;
    private TextView mViewReply;
    private TextView mViewReplyTo;

    private long mMessageId = EmailContent.Message.NO_MESSAGE; // 邮件ID

    private ControllerResultUiThreadWrapper<ControllerResults> mControllerCallback;

    /**
     * URI of message to open.
     */
    private Uri mFileEmailUri;

    private final EmailAsyncTask.Tracker mTaskTracker = new EmailAsyncTask.Tracker();
    //accountId和messageId
    private long mAccountId = Account.NO_ACCOUNT;
    //
    private EmailContent.Message mMessage;
    private MessageObserver mMessageObserver;

    // contains the HTML body. Is used by LoadAttachmentTask to display inline images.
    // is null most of the time, is used transiently to pass info to LoadAttachementTask
    private String mHtmlTextRaw;


    // Regex that matches start of img tag. '<(?i)img\s+'.
    private static final Pattern IMG_TAG_START_REGEX = Pattern.compile("<(?i)img\\s+");
    // Regex that matches Web URL protocol part as case insensitive.
    private static final Pattern WEB_URL_PROTOCOL = Pattern.compile("(?i)http|https://");

    // contains the HTML content as set in WebView.
    private String mHtmlTextWebView;
    private Controller mController;
    private ListView mAttaContainer;
    private List<HcmailAtta> mAttaList;
    private HcmailAttaAdapter mAdapter;
    //    private String text;
    private boolean isViewToFlag = false;
    private Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            Bundle bundle = msg.getData();
            mAttaList = bundle.getParcelableArrayList("list");
            HcLog.D("mAttaList.size()=" + mAttaList.size());
            // 附件信息展示
            mAdapter = new HcmailAttaAdapter(HcmailViewActivity.this, mAttaList, false);
            mAttaContainer.setAdapter(mAdapter);
            super.handleMessage(msg);
        }
    };

    private String mSubject;

    private String fromFriendly;
    private String fromAddress;
    private String friendlyTo;
    private String friendlyCc;
    private String friendlyBcc;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent intent = getIntent();
        if (intent.getExtras() == null) {
            HcUtil.showToast(this, "没有邮件!");
            finish();
            return;
        }
        mMessageId = intent.getExtras().getLong("messageId", EmailContent.Message.NO_MESSAGE);
        if (mMessageId == EmailContent.Message.NO_MESSAGE) {
            HcUtil.showToast(this, "没有邮件!");
            finish();
            return;
        }
        setContentView(R.layout.hcmail_message_view_fragment);
        mController = Controller.getInstance(this);
        mControllerCallback = new ControllerResultUiThreadWrapper<ControllerResults>(
                new Handler(), new ControllerResults());
        mMessageObserver = new MessageObserver(new Handler(), this);
        initView();
        initData();
    }

    private void initView() {
        cleanupDetachedViews();
        mViewTopbar = (TopBarView) findViewById(R.id.hcmail_view_top_bar);
        mViewSend = (TextView) findViewById(R.id.send);
        mViewTo = (TextView) findViewById(R.id.to);
        mCc = (TextView) findViewById(R.id.email_message_view_cc);
        mBcc = (TextView) findViewById(R.id.email_message_view_bcc);
        mCcParent = (LinearLayout) findViewById(R.id.email_message_view_cc_parent);
        mBccParent = (LinearLayout) findViewById(R.id.email_message_view_bcc_parent);
        mViewDate = (TextView) findViewById(R.id.first_date);
        mMessageSubject = (TextView) findViewById(R.id.email_message_view_subject);
        mAttachmentBtn = (ImageView) findViewById(R.id.email_message_view_attachment);

        // content
        mMessageContentView = (WebView) findViewById(R.id.message_content);
        mAttachmentsScroll = findViewById(R.id.attachments_scroll);
        mAttaContainer = (ListView) findViewById(R.id.attachment_container_list);
        mAttachments = (LinearLayout) findViewById(R.id.attachments);

        // bottom
        mViewMessageDelete = (ImageView) findViewById(R.id.message_delete);
        mViewMessagePen = (ImageView) findViewById(R.id.message_pen);
        mViewReply = (TextView) findViewById(R.id.reply);
        mViewReplyTo = (TextView) findViewById(R.id.reply_to);
        mEditParent = findViewById(R.id.email_message_view_edit_parent);

        mViewMessageDelete.setOnClickListener(this);
        mViewMessagePen.setOnClickListener(this);
        mViewReply.setOnClickListener(this);
        mViewReplyTo.setOnClickListener(this);
        mViewTo.setOnClickListener(this);
        mEditParent.setOnClickListener(this);
        mAttachmentBtn.setOnClickListener(this);

        WebSettings webSettings = mMessageContentView.getSettings();
        boolean supportMultiTouch = getPackageManager()
                .hasSystemFeature(PackageManager.FEATURE_TOUCHSCREEN_MULTITOUCH);
        webSettings.setDisplayZoomControls(!supportMultiTouch);
        webSettings.setSupportZoom(true);
        webSettings.setBuiltInZoomControls(true);
        mMessageContentView.setWebViewClient(new CustomWebViewClient());
    }

    private void initData() {
        mViewTopbar.setReturnBtnIcon(R.drawable.hcmail_back);
        mController.addResultCallback(mControllerCallback);

        new LoadMessageTask(true).executeParallel();
    }


    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        return super.onKeyDown(keyCode, event);
    }

    @Override
    protected void onDestroy() {
        mController.removeResultCallback(mControllerCallback);
        cancelAllTasks();
        cleanupDetachedViews();
        super.onDestroy();
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.message_delete) {
            //点击删除按钮
            mController.deleteMessage(mMessageId);
            finish();
        } else if (id == R.id.message_pen) {
            //点击钢笔按钮
        } else if (id == R.id.reply) {
            //点击转发按钮
            Intent intent = new Intent();
            intent.setClass(this, HcmailWriteActivity.class);
            intent.setAction(HcmailWriteActivity.ACTION_FORWARD);
            intent.putExtra("messageId", String.valueOf(mMessageId));
            intent.putExtra("subject", mSubject);
            startActivity(intent);
        } else if (id == R.id.reply_to) {
            //点击回复按钮
            Intent intent = new Intent();
            intent.setClass(this, HcmailWriteActivity.class);
            intent.setAction(HcmailWriteActivity.ACTION_REPLY_TO);
            intent.putExtra("messageId", String.valueOf(mMessageId));
            intent.putExtra("messageTo", mMessage.mTo);
            intent.putExtra("messageCc", mMessage.mCc);
            intent.putExtra("mTo", friendlyTo);
            intent.putExtra("mCc", friendlyCc);
            intent.putExtra("fFri", fromFriendly);
            intent.putExtra("fAdd", fromAddress);
            startActivity(intent);
        } else if (id == R.id.to) {
            if (isViewToFlag) {
                mViewTo.setMaxLines(3);
                mViewTo.requestLayout();
                isViewToFlag = false;
            } else {
                mViewTo.setMaxLines(Integer.MAX_VALUE);
                mViewTo.requestLayout();
                isViewToFlag = true;
            }
        } else if (id == R.id.email_message_view_attachment) {
            if (mAttachmentsScroll.getVisibility() == View.VISIBLE) {
                mAttachmentsScroll.setVisibility(View.GONE);
            } else {
                mAttachmentsScroll.setVisibility(View.VISIBLE);
            }
        }
    }

    /**
     * Async task for loading a single message outside of the UI thread
     */
    private class LoadMessageTask extends EmailAsyncTask<Void, Void, EmailContent.Message> {

        private final boolean mOkToFetch;
        private Mailbox mMailbox;

        /**
         * Special constructor to cache some local info
         */
        public LoadMessageTask(boolean okToFetch) {
            super(mTaskTracker);
            mOkToFetch = okToFetch;
        }

        @Override
        protected EmailContent.Message doInBackground(Void... params) {
            EmailContent.Message message = null;
            message = openMessageSync(HcmailViewActivity.this);
            if (message != null) {
                HcLog.D(EmailUtils.DEBUG, TAG + "$LoadMessageTask#doInBackground message.mMailboxKey=" + message.mMailboxKey);
                mMailbox = Mailbox.restoreMailboxWithId(HcmailViewActivity.this, message.mMailboxKey);
                HcLog.D(EmailUtils.DEBUG, TAG + "$LoadMessageTask#doInBackgroundmMailbox=" + mMailbox);
                if (mMailbox == null) {
                    message = null; // mailbox removed??
                }
            }
            return message;
        }

        /**
         * NOTE See the comment on the super method.  It's called on a worker thread.
         */
        protected EmailContent.Message openMessageSync(Activity activity) {
            return EmailContent.Message.restoreMessageWithId(activity, mMessageId);
        }

        @Override
        protected void onSuccess(EmailContent.Message message) {
            if (message == null) {
                //通知用户邮件信息为空
                HcUtil.showToast(HcmailViewActivity.this, "邮件不存在!");
                finish();
                return;
            }
            mMessageId = message.mId;
            reloadUiFromMessage(message, mOkToFetch);
            RecentMailboxManager.getInstance(HcmailViewActivity.this).touch(mAccountId, message.mMailboxKey);
        }
    }

    /**
     * Reload the UI from a provider cursor.  {@link MessageViewFragmentBase.LoadMessageTask#onSuccess} calls it.
     * <p>
     * Update the header views, and start loading the body.
     *
     * @param message   A copy of the message loaded from the database
     * @param okToFetch If true, and message is not fully loaded, it's OK to fetch from
     *                  the network.  Use false to prevent looping here.
     */
    protected void reloadUiFromMessage(EmailContent.Message message, boolean okToFetch) {
        mMessage = message;
        mAccountId = message.mAccountKey;
        mSubject = message.mSubject;
        mMessageSubject.setText(mSubject);
        //注册监听者
        mMessageObserver.register(ContentUris.withAppendedId(EmailContent.Message.CONTENT_URI, mMessage.mId));

        final Address from = Address.unpackFirst(mMessage.mFrom);
        if (from != null) {
            fromFriendly = from.toFriendly();
            fromAddress = from.getAddress();
            HcLog.D(EmailUtils.DEBUG, TAG + "#reloadUiFromMessage fromFriendly = " + fromFriendly + " fromAddress = " + fromAddress);
            mViewSend.setText(fromFriendly);
        } else {
            mViewSend.setText(" ");
        }

        mViewDate.setText("发送日期: " + DateUtils.getRelativeTimeSpanString(this, mMessage.mTimeStamp)
                .toString());

        // To/Cc/Bcc
        final Resources res = getResources();
        final SpannableStringBuilder ssb = new SpannableStringBuilder();
        friendlyTo = Address.toFriendly(Address.unpack(mMessage.mTo));
        friendlyCc = Address.toFriendly(Address.unpack(mMessage.mCc));
        friendlyBcc = Address.toFriendly(Address.unpack(mMessage.mBcc));

        if (!TextUtils.isEmpty(friendlyTo)) {
            mViewTo.setText(friendlyTo);
        }
        if (!TextUtils.isEmpty(friendlyCc)) {
            mCcParent.setVisibility(View.VISIBLE);
            mCc.setText(friendlyCc);
        }
        if (!TextUtils.isEmpty(friendlyBcc)) {
            mBccParent.setVisibility(View.VISIBLE);
            mBcc.setText(friendlyCc);
        }

        // Handle partially-loaded email, as follows:
        // 1. Check value of message.mFlagLoaded
        // 2. If != LOADED, ask controller to load it
        // 3. Controller callback (after loaded) should trigger LoadBodyTask & LoadAttachmentsTask
        // 4. Else start the loader tasks right away (message already loaded)
        if (okToFetch && message.mFlagLoaded != EmailContent.Message.FLAG_LOADED_COMPLETE) {
            mControllerCallback.getWrappee().setWaitForLoadMessageId(message.mId);
            mController.loadMessageForView(message.mId);
        } else {
            Address[] fromList = Address.unpack(mMessage.mFrom);
            boolean autoShowImages = false;
            for (Address sender : fromList) {
                String email = sender.getAddress();
                if (shouldShowImagesFor(email)) {
                    autoShowImages = true;
                    break;
                }
            }
            mControllerCallback.getWrappee().setWaitForLoadMessageId(EmailContent.Message.NO_MESSAGE);
            // Ask for body
            new LoadBodyTask(message.mId, autoShowImages).executeParallel();
        }

//        new LoadBodyTask(message.mId, true).executeParallel();
    }

    /**
     * Class to detect update on the current message (e.g. toggle star).  When it gets content
     * change notifications, it kicks {@link MessageViewFragmentBase.ReloadMessageTask}.
     */
    public class MessageObserver extends ContentObserver implements Runnable {
        private final Throttle mThrottle;
        private final ContentResolver mContentResolver;

        private boolean mRegistered;

        public MessageObserver(Handler handler, Context context) {
            super(handler);
            mContentResolver = context.getContentResolver();
            mThrottle = new Throttle("MessageObserver", this, handler);
        }

        public void unregister() {
            if (!mRegistered) {
                return;
            }
            mThrottle.cancelScheduledCallback();
            mContentResolver.unregisterContentObserver(this);
            mRegistered = false;
        }

        public void register(Uri notifyUri) {
            unregister();
            mContentResolver.registerContentObserver(notifyUri, true, this);
            mRegistered = true;
        }

        @Override
        public boolean deliverSelfNotifications() {
            return true;
        }

        @Override
        public void onChange(boolean selfChange) {
            if (mRegistered) {
                mThrottle.onEvent();
            }
        }

        /**
         * This method is delay-called by {@link Throttle} on the UI thread.
         */
        @Override
        public void run() {
            // This method is delay-called, so need to make sure if it's still registered.
//            if (mRegistered) {
//                new ReloadMessageTask().cancelPreviousAndExecuteParallel();
//            }
        }
    }
    /**
     * Kicked by {@link MessageViewFragmentBase.MessageObserver}.  Reload the message and update the views.
     */
//    private class ReloadMessageTask extends EmailAsyncTask<Void, Void, EmailContent.Message> {
//        public ReloadMessageTask() {
//            super(mTaskTracker);
//        }
//
//        @Override
//        protected EmailContent.Message doInBackground(Void... params) {
//            Activity activity = HcmailViewActivity.this;
//            if (activity == null) {
//                return null;
//            } else {
//                return reloadMessageSync(activity);
//            }
//        }
//
//        @Override
//        protected void onSuccess(EmailContent.Message message) {
//            if (message == null || message.mMailboxKey != mMessage.mMailboxKey) {
//                // Message deleted or moved.
//                mCallback.onMessageNotExists();
//                return;
//            }
//            mMessage = message;
//            //更新message
////            updateHeaderView(mMessage);
//        }
//    }
//    protected EmailContent.Message reloadMessageSync(Activity activity) {
//        return openMessageSync(activity);
//    }

    /**
     * Async task for loading a single message body outside of the UI thread
     */
    private class LoadBodyTask extends EmailAsyncTask<Void, Void, String[]> {

        private final long mId;
        private boolean mErrorLoadingMessageBody;
        private final boolean mAutoShowPictures;

        /**
         * Special constructor to cache some local info
         */
        public LoadBodyTask(long messageId, boolean autoShowPictures) {
            super(mTaskTracker);
            mId = messageId;
            mAutoShowPictures = autoShowPictures;
        }

        @Override
        protected String[] doInBackground(Void... params) {
            try {
                String text = null;
                String html = EmailContent.Body.restoreBodyHtmlWithMessageId(HcmailViewActivity.this, mId);
                if (html == null) {
                    text = EmailContent.Body.restoreBodyTextWithMessageId(HcmailViewActivity.this, mId);
                }
                return new String[]{text, html};
            } catch (RuntimeException re) {
                // This catches SQLiteException as well as other RTE's we've seen from the
                // database calls, such as IllegalStateException
                HcLog.D(EmailUtils.DEBUG, TAG + "$LoadBodyTask#doInBackground Exception while loading message body error = " + re);
                mErrorLoadingMessageBody = true;
                return null;
            }
        }

        @Override
        protected void onSuccess(String[] results) {
            if (results == null) {
                if (mErrorLoadingMessageBody) {
                    HcUtil.showToast(HcmailViewActivity.this, R.string.error_loading_message_body);
                }
                return;
            }
            reloadUiFromBody(results[0], results[1], mAutoShowPictures);    // text, html
            onMarkMessageAsRead(true);
        }
    }

    /**
     * Reload the body from the provider cursor.  This must only be called from the UI thread.
     *
     * @param bodyText text part
     * @param bodyHtml html part
     *                 <p>
     *                 TODO deal with html vs text and many other issues <- WHAT DOES IT MEAN??
     */
    private void reloadUiFromBody(String bodyText, String bodyHtml, boolean autoShowPictures) {
        String text = null;
        mHtmlTextRaw = null;
        boolean hasImages = false;

        if (bodyHtml == null) {
            text = bodyText;
            /*
             * Convert the plain text to HTML
             */
            StringBuffer sb = new StringBuffer("<html><body>");
            if (text != null) {
                // Escape any inadvertent HTML in the text message
                text = EmailHtmlUtil.escapeCharacterToDisplay(text);
                // Find any embedded URL's and linkify
                Matcher m = Patterns.WEB_URL.matcher(text);
                while (m.find()) {
                    int start = m.start();
                    /*
                     * WEB_URL_PATTERN may match domain part of email address. To detect
                     * this false match, the character just before the matched string
                     * should not be '@'.
                     */
                    if (start == 0 || text.charAt(start - 1) != '@') {
                        String url = m.group();
                        Matcher proto = WEB_URL_PROTOCOL.matcher(url);
                        String link;
                        if (proto.find()) {
                            // This is work around to force URL protocol part be lower case,
                            // because WebView could follow only lower case protocol link.
                            link = proto.group().toLowerCase() + url.substring(proto.end());
                        } else {
                            // Patterns.WEB_URL matches URL without protocol part,
                            // so added default protocol to link.
                            link = "http://" + url;
                        }
                        String href = String.format("<a href=\"%s\">%s</a>", link, url);
                        m.appendReplacement(sb, href);
                    } else {
                        m.appendReplacement(sb, "$0");
                    }
                }
                m.appendTail(sb);
            }
            sb.append("</body></html>");
            text = sb.toString();
        } else {
            text = bodyHtml;
            mHtmlTextRaw = bodyHtml;
            hasImages = IMG_TAG_START_REGEX.matcher(text).find();
        }

        // TODO this is not really accurate.
        // - Images aren't the only network resources.  (e.g. CSS)
        // - If images are attached to the email and small enough, we download them at once,
        //   and won't need network access when they're shown.
        if (hasImages) {
            if (autoShowPictures) {
                blockNetworkLoads(false);
            } else {
//                addTabFlags(TAB_FLAGS_HAS_PICTURES);
            }
        }
        setMessageHtml(text);

        // Ask for attachments after body
        new LoadAttachmentsTask().executeParallel(mMessage.mId);
    }

    private void blockNetworkLoads(boolean block) {
        if (mMessageContentView != null) {
            mMessageContentView.getSettings().setBlockNetworkLoads(block);
        }
    }

    private void setMessageHtml(String html) {
        if (html == null) {
            html = "";
        }
        HcLog.D("html = " + html);
        if (mMessageContentView != null) {
            if (mMessageContentView.getVisibility() != View.VISIBLE)
                mMessageContentView.setVisibility(View.VISIBLE);
            mMessageContentView.loadDataWithBaseURL("email://", html, "text/html", "utf-8", null);
        }
    }

    /**
     * Async task for loading attachments
     * <p>
     * Note:  This really should only be called when the message load is complete - or, we should
     * leave open a listener so the attachments can fill in as they are discovered.  In either case,
     * this implementation is incomplete, as it will fail to refresh properly if the message is
     * partially loaded at this time.
     */
    private class LoadAttachmentsTask extends EmailAsyncTask<Long, Void, EmailContent.Attachment[]> {
        public LoadAttachmentsTask() {
            super(mTaskTracker);
        }

        @Override
        protected EmailContent.Attachment[] doInBackground(Long... messageIds) {
            return EmailContent.Attachment.restoreAttachmentsWithMessageId(HcmailViewActivity.this, messageIds[0]);
        }

        @Override
        protected void onSuccess(EmailContent.Attachment[] attachments) {
            try {
                if (attachments == null) {
                    return;
                }
//                if (mAttachmentsScroll.getVisibility() != View.VISIBLE)
//                    mAttachmentsScroll.setVisibility(View.VISIBLE);
                boolean htmlChanged = false;
                int numDisplayedAttachments = 0;
                for (EmailContent.Attachment attachment : attachments) {
                    if (mHtmlTextRaw != null && attachment.mContentId != null
                            && attachment.mContentUri != null) {
                        // for html body, replace CID for inline images
                        // Regexp which matches ' src="cid:contentId"'.
                        String contentIdRe =
                                "\\s+(?i)src=\"cid(?-i):\\Q" + attachment.mContentId + "\\E\"";
                        String srcContentUri = " src=\"" + attachment.mContentUri + "\"";
                        mHtmlTextRaw = mHtmlTextRaw.replaceAll(contentIdRe, srcContentUri);
                        htmlChanged = true;
                    } else {
                        addAttachment(attachment);
                        numDisplayedAttachments++;
                    }
                }
                HcLog.D(EmailUtils.DEBUG, TAG + "$LoadAttachmentsTask#onSuccess numDisplayedAttachments = " + numDisplayedAttachments);
                if (numDisplayedAttachments > 0) {
                    mAttachmentBtn.setVisibility(View.VISIBLE);
                }
                mHtmlTextWebView = mHtmlTextRaw;
                mHtmlTextRaw = null;
                if (htmlChanged) {
                    setMessageHtml(mHtmlTextWebView);
                }
                /**
                 mAttaList = new ArrayList<>();
                 for (EmailContent.Attachment attachment : attachments) {
                 //将附件显示在附件添加的位置
                 addAttachment(attachment);
                 }
                 Message message = Message.obtain();
                 Bundle bundle = new Bundle();
                 bundle.putParcelableArrayList("list", (ArrayList<? extends Parcelable>) mAttaList);
                 message.setData(bundle);
                 mHandler.sendMessage(message);
                 HcLog.D("mAttaList.size()=" + mAttaList.size());
                 mHtmlTextWebView = mHtmlTextRaw;
                 mHtmlTextRaw = null;
                 if (htmlChanged) {
                 setMessageHtml(mHtmlTextWebView);
                 }*/
            } finally {
                ;
            }
        }
    }

    private void addAttachment(EmailContent.Attachment attachment) {
//        MessageViewFragmentBase.MessageViewAttachmentInfo attachmentInfo = new MessageViewFragmentBase.MessageViewAttachmentInfo(
//                this, attachment, null);
//        HcLog.D("attachmentInfo.mName = " + attachmentInfo.mName);
//        HcmailAtta atta = new HcmailAtta();
//        atta.setAttaName(attachmentInfo.mName);
//        mAttaList.add(atta);
        LayoutInflater inflater = getLayoutInflater();
        View view = inflater.inflate(R.layout.hcmail_atta_list_item, null);
        TextView attachmentName = (TextView) view.findViewById(R.id.hcmail_atta_name);

        final MessageViewAttachmentInfo attachmentInfo = new MessageViewAttachmentInfo(
                this, attachment);

        // Check whether the attachment already exists
        if (Utility.attachmentExists(this, attachment)) {
            attachmentInfo.loaded = true;
        }


        attachmentName.setText(attachmentInfo.mName);
//        if (attachment.mFileName.endsWith(".png") || attachment.mFileName.endsWith(".jpg") ||
//                attachment.mFileName.endsWith(".jpeg")) {
//            view.setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View v) {
//
//                }
//            });
//        }
        view.setTag(attachmentInfo);
        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (attachmentInfo.loaded) {
                    onOpenAttachment(attachmentInfo);
                } else {
                    HcDialog.showProgressDialog(HcmailViewActivity.this, "正在下载附件...");
                    if (!AttachmentDownloadService.isAttachmentQueued(attachmentInfo.mId)) {
                        onLoadAttachment(attachmentInfo);
                    }
                }
            }
        });
        mAttachments.addView(view);
    }


    /////////////// add /////////////////

    /**
     * Controller results listener.  We wrap it with {@link ControllerResultUiThreadWrapper},
     * so all methods are called on the UI thread.
     */
    private class ControllerResults extends Controller.Result {
        private long mWaitForLoadMessageId;

        public void setWaitForLoadMessageId(long messageId) {
            mWaitForLoadMessageId = messageId;
        }

        @Override
        public void loadMessageForViewCallback(MessagingException result, long accountId,
                                               long messageId, int progress) {
            if (messageId != mWaitForLoadMessageId) {
                // We are not waiting for this message to load, so exit quickly
                return;
            }
            if (result == null) {
                switch (progress) {
                    case 0:
                        // Loading from network -- show the progress icon.
                        HcDialog.showProgressDialog(HcmailViewActivity.this, "正在获取数据...");
                        break;
                    case 100:
                        mWaitForLoadMessageId = -1;
                        // reload UI and reload everything else too
                        // pass false to LoadMessageTask to prevent looping here
                        cancelAllTasks();
                        HcDialog.deleteProgressDialog();
                        new LoadMessageTask(false).executeParallel();
                        break;
                    default:
                        // do nothing - we don't have a progress bar at this time
                        break;
                }
            } else {
                mWaitForLoadMessageId = EmailContent.Message.NO_MESSAGE;
                String error = getResources().getString(R.string.status_network_error);
                HcUtil.showToast(HcmailViewActivity.this, error);
            }
        }

        @Override
        public void loadAttachmentCallback(MessagingException result, long accountId,
                                           long messageId, long attachmentId, int progress) {
            if (messageId == mMessageId) {
                if (result == null) {
                    HcDialog.deleteProgressDialog();
                    switch (progress) {
                        case 100:
                            final MessageViewAttachmentInfo attachmentInfo =
                                    findAttachmentInfoFromView(attachmentId);
                            if (attachmentInfo != null) {
                                attachmentInfo.loaded = true;
                                onOpenAttachment(attachmentInfo);
                            }

                            break;
                        default:
                            // do nothing - we don't have a progress bar at this time
                            break;
                    }
                } else {
                    HcUtil.showToast(HcmailViewActivity.this, "附件下载失败!");
                }
            }
        }

    }

    private void cleanupDetachedViews() {
        // WebView cleanup must be done after it leaves the rendering tree, according to
        // its contract
        if (mMessageContentView != null) {
            mMessageContentView.destroy();
            mMessageContentView = null;
        }
    }

    /**
     * Overrides for WebView behaviors.
     */
    private class CustomWebViewClient extends WebViewClient {
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
//            return mCallback.onUrlInMessageClicked(url);
            return super.shouldOverrideUrlLoading(view, url);
        }
    }

    private boolean shouldShowImagesFor(String senderEmail) {
        return Preferences.getPreferences(this).shouldShowImagesFor(senderEmail);
    }

    private void cancelAllTasks() {
        mMessageObserver.unregister();
        mTaskTracker.cancellAllInterrupt();
    }

    /**
     * Set message read/unread.
     */
    public void onMarkMessageAsRead(boolean isRead) {
        if (mMessage == null) return;
        EmailContent.Message message = mMessage;
        if (message.mFlagRead != isRead) {
            message.mFlagRead = isRead;
            mController.setMessageRead(message.mId, isRead);
            if (!isRead) { // Became unread.  We need to close the message.

            }
        }
    }

    private static class MessageViewAttachmentInfo extends AttachmentInfo {


        private static final Map<AttachmentInfo, String> sSavedFileInfos = HcUtil.newHashMap();


        private boolean loaded;

        public MessageViewAttachmentInfo(Context context, EmailContent.Attachment attachment) {
            super(context, attachment);
        }

        /**
         * Create a new attachment info based upon an existing attachment info. Display
         * related fields (such as views and buttons) are copied from old to new.
         */
        private MessageViewAttachmentInfo(Context context, MessageViewAttachmentInfo oldInfo) {
            super(context, oldInfo);
            loaded = oldInfo.loaded;
        }

        /**
         * Determines whether or not this attachment has a saved file in the external storage. That
         * is, the user has at some point clicked "save" for this attachment.
         * <p>
         * Note: this is an approximation and uses an in-memory cache that can get wiped when the
         * process dies, and so is somewhat conservative. Additionally, the user can modify the file
         * after saving, and so the file may not be the same (though this is unlikely).
         */
        public boolean isFileSaved() {
            String path = getSavedPath();
            if (path == null) {
                return false;
            }
            boolean savedFileExists = new File(path).exists();
            if (!savedFileExists) {
                // Purge the cache entry.
                setSavedPath(null);
            }
            return savedFileExists;
        }

        private void setSavedPath(String path) {
            if (path == null) {
                sSavedFileInfos.remove(this);
            } else {
                sSavedFileInfos.put(this, path);
            }
        }

        /**
         * Returns an absolute file path for the given attachment if it has been saved. If one is
         * not found, {@code null} is returned.
         * <p>
         * Clients are expected to validate that the file at the given path is still valid.
         */
        private String getSavedPath() {
            return sSavedFileInfos.get(this);
        }

        @Override
        protected Uri getUriForIntent(Context context, long accountId) {
            // Prefer to act on the saved file for intents.
            String path = getSavedPath();
            return (path != null)
                    ? Uri.parse("file://" + getSavedPath())
                    : super.getUriForIntent(context, accountId);
        }
    }


    private void onOpenAttachment(MessageViewAttachmentInfo info) {
        HcLog.D(EmailUtils.DEBUG, TAG + "#onOpenAttachment mAllowInstall = " + info.mAllowInstall + " mAllowSave = " + info.mAllowSave + " isFileSaved = " + info.isFileSaved());
        if (info.mAllowInstall) {
            // The package installer is unable to install files from a content URI; it must be
            // given a file path. Therefore, we need to save it first in order to proceed
            if (!info.mAllowSave || !Utility.isExternalStorageMounted()) {
                HcUtil.showToast(this, R.string.message_view_status_attachment_not_saved);
                return;
            }

            if (!info.isFileSaved()) {
                if (performAttachmentSave(info) == null) {
                    // Saving failed for some reason - bail.
                    HcUtil.showToast(
                            this, R.string.message_view_status_attachment_not_saved);
                    return;
                }
            }
        }
        try {
            Intent intent = info.getAttachmentIntent(this, mAccountId);
            startActivity(intent);
        } catch (ActivityNotFoundException e) {
            HcUtil.showToast(this, R.string.message_view_display_attachment_toast);
        } catch (Exception e) {
            HcLog.D(EmailUtils.DEBUG, TAG + " #onOpenAttachment error =" + e);
        }
    }

    private File performAttachmentSave(MessageViewAttachmentInfo info) {
        HcLog.D(EmailUtils.DEBUG, TAG + " #performAttachmentSave start !!!!!!!!!!!!!!!!!!!!!!!!!!!!1");
        EmailContent.Attachment attachment = EmailContent.Attachment.restoreAttachmentWithId(this, info.mId);
        Uri attachmentUri = AttachmentUtilities.getAttachmentUri(mAccountId, attachment.mId);

        try {
            File downloads = Environment.getExternalStoragePublicDirectory(
                    Environment.DIRECTORY_DOWNLOADS);
            downloads.mkdirs();
            File file = Utility.createUniqueFile(downloads, attachment.mFileName);
            Uri contentUri = AttachmentUtilities.resolveAttachmentIdToContentUri(
                    getContentResolver(), attachmentUri);
            InputStream in = getContentResolver().openInputStream(contentUri);
            OutputStream out = new FileOutputStream(file);
            IOUtils.copy(in, out);
            out.flush();
            out.close();
            in.close();

            String absolutePath = file.getAbsolutePath();

            // Although the download manager can scan media files, scanning only happens after the
            // user clicks on the item in the Downloads app. So, we run the attachment through
            // the media scanner ourselves so it gets added to gallery / music immediately.
            MediaScannerConnection.scanFile(this, new String[]{absolutePath},
                    null, null);

            DownloadManager dm =
                    (DownloadManager) getSystemService(Context.DOWNLOAD_SERVICE);
            dm.addCompletedDownload(info.mName, info.mName,
                    false /* do not use media scanner */,
                    info.mContentType, absolutePath, info.mSize,
                    true /* show notification */);

            // Cache the stored file information.
            info.setSavedPath(absolutePath);
            HcLog.D(EmailUtils.DEBUG, TAG + " #performAttachmentSave end!!!!!!!!!!!!!!!");
            return file;

        } catch (IOException ioe) {
            // Ignore. Callers will handle it from the return code.
            HcLog.D(EmailUtils.DEBUG, TAG + " #performAttachmentSave error = " + ioe);
        }

        return null;
    }

    private void onLoadAttachment(final MessageViewAttachmentInfo attachment) {

        mController.loadAttachment(attachment.mId, mMessageId, mAccountId);
    }

    private MessageViewAttachmentInfo findAttachmentInfoFromView(long attachmentId) {
        for (int i = 0, count = mAttachments.getChildCount(); i < count; i++) {
            MessageViewAttachmentInfo attachmentInfo =
                    (MessageViewAttachmentInfo) mAttachments.getChildAt(i).getTag();
            if (attachmentInfo.mId == attachmentId) {
                return attachmentInfo;
            }
        }
        return null;
    }
}
