package com.android.hcframe.hcmail.task;

import android.app.LoaderManager;
import android.content.Context;
import android.content.Loader;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;

import com.android.email.Controller;
import com.android.email.ControllerResultUiThreadWrapper;
import com.android.email.MessageListContext;
import com.android.email.RefreshManager;
import com.android.email.RefreshManager.Listener;
import com.android.emailcommon.mail.MessagingException;
import com.android.emailcommon.provider.Account;
import com.android.emailcommon.provider.Mailbox;
import com.android.hcframe.HcLog;
import com.android.hcframe.hcmail.EmailUtils;
import com.android.hcframe.hcmail.data.LoaderProvider;

import java.util.ArrayList;
import java.util.List;

/**
 * @Company 浙 江 鸿 程 计 算 机 系 统 有 限 公 司
 * @URL http://www.zjhcsoft.com
 * @Address 杭州滨江区伟业路1号
 * @Email jinjr@zjhcsoft.com
 * Created by jrjin on 17-3-23 10:48.
 */

public class EmailPresenter implements MessageListTaskContract.Presenter {

    private static final String TAG = "EmailPresenter";

    private LoaderManager mLoaderManager;

    private MessageListTaskContract.View mTasksView;

    private long mAccountId;

    private long mMailboxId;

    private int mMailboxType;

    // Controller access
    private Controller mController;
    private RefreshManager mRefreshManager;
    private final RefreshListener mRefreshListener = new RefreshListener();

    private static final int LOADER_ID_MESSAGES_LOADER = 1;

    /**
     * The context describing the contents to be shown in the list.
     * Do not use directly; instead, use the getters such as {@link MessageListContext#getAccountId()}.
     * <p><em>NOTE:</em> Although we cannot force these to be immutable using Java language
     * constructs, this <em>must</em> be considered immutable.
     */
    private MessageListContext mListContext;

    private boolean mIsFirstLoad;

    private LoaderProvider mLoaderProvider;

    private Account mAccount;
    private Mailbox mMailbox;
    private boolean mIsRefreshable;
    private int mCountTotalAccounts;

    private final Controller.Result mControllerResult;

    public EmailPresenter(@NonNull Context context, @NonNull LoaderManager loaderManager,
                          @NonNull MessageListTaskContract.View view, MessageListContext listContext) {
        mController = Controller.getInstance(context);
        mRefreshManager = RefreshManager.getInstance(context);
        mLoaderManager = loaderManager;
        mTasksView = view;
        mListContext = listContext;
        mAccountId = mListContext.getAccountId();
        mMailboxId = mListContext.getMailboxId();
        mMailboxType = Mailbox.getMailboxType(context, mMailboxId);
        mLoaderProvider = new LoaderProvider(context);
        mControllerResult = new ControllerResultUiThreadWrapper<ControllerResult>(new Handler(), new ControllerResult());
        mController.addResultCallback(mControllerResult);
        mTasksView.setPresenter(this);
        HcLog.D(EmailUtils.DEBUG, TAG + " #EmailPresenter accountId = "+mAccountId + " mailboxId = "+mMailboxId + " mailboxType = "+mMailboxType);
    }

    @Override
    public void clearMessages(List<Long> messageIds) {
        int size = messageIds.size();
        long[] newMessageIds = new long[size];
        for (int i = 0; i < size; i++) {
            newMessageIds[i] = messageIds.get(i);
        }
        clearMessages(newMessageIds);
    }

    @Override
    public void loadMessageList(int mailboxType, boolean refresh, long messageId) {
        if (refresh) {
            mRefreshManager.refreshMessageList(mAccountId, mMailboxId, true);
        } else {
            mRefreshManager.loadMoreMessages(mAccountId, mMailboxId);
        }

    }

    @Override
    public void openTaskDetails(long messageId) {
        mTasksView.showTaskDetails(messageId);
    }

    @Override
    public void openTaskComposeMail() {
        mTasksView.showTaskComposeMail();
    }

    @Override
    public void openEditMode(int mailboxType) {
        mTasksView.showEditView(mMailboxType);
        mTasksView.setEditView(getTitle(), "全选");
    }

    @Override
    public void closeEditMode() {
        mTasksView.hideEditView();
    }

    @Override
    public void deleteMessages(List<Long> messageIds) {
        int size = messageIds.size();
        long[] newMessageIds = new long[size];
        for (int i = 0; i < size; i++) {
        	newMessageIds[i] = messageIds.get(i);
        }
        deleteMessages(newMessageIds);

    }

    @Override
    public void deleteMessages(long[] messageIds) {
        mController.deleteMessages(messageIds);
    }

    @Override
    public void clearMessages(long[] messageIds) {
        mController.deleteMessages(messageIds);
    }

    @Override
    public void selectMessage(long messageId) {

    }

    @Override
    public void selectAllMessages() {

    }

    @Override
    public void openPopWindow() {

    }

    @Override
    public void moveTo(long mailboxId, List<Long> messageIds) {
        int size = messageIds.size();
        long[] newMessageIds = new long[size];
        for (int i = 0; i < size; i++) {
            newMessageIds[i] = messageIds.get(i);
        }
        moveTo(mailboxId, newMessageIds);
    }

    @Override
    public void moveTo(long mailboxId, long[] messageIds) {
        mController.moveMessages(messageIds, mailboxId);
    }

    @Override
    public void start() {
        mTasksView.setTitle(getTitle());
        mTasksView.showLoadingDialog("正在获取邮件...");
        mRefreshManager.registerListener(mRefreshListener);
        mLoaderManager.initLoader(LOADER_ID_MESSAGES_LOADER, null, LOADER_CALLBACKS);
    }

    private String getTitle() {
        switch (mMailboxType) {
            case Mailbox.TYPE_INBOX: // 收件箱
                return "收件箱";
            case Mailbox.TYPE_OUTBOX: // 发件箱,存本地
                return "发件箱";
            case Mailbox.TYPE_SENT: // 已发送
                return "已发送";
            case Mailbox.TYPE_DRAFTS: // 草稿
                return "草稿箱";
            case Mailbox.TYPE_TRASH: //  已删除
                return "已删除";

            default:
                mMailboxType = Mailbox.TYPE_INBOX;
                return "收件箱";
        }
    }

    private class RefreshListener implements Listener {
        @Override
        public void onMessagingError(long accountId, long mailboxId, String message) {
            mTasksView.hideLoadingDialog();
        }

        @Override
        public void onRefreshStatusChanged(long accountId, long mailboxId) {
//            mTasksView.hideLoadingDialog();
        }
    }

    /**
     * Loader callbacks for message list.
     */
    private final LoaderManager.LoaderCallbacks<Cursor> LOADER_CALLBACKS =
            new LoaderManager.LoaderCallbacks<Cursor>() {

                @Override
                public Loader<Cursor> onCreateLoader(int id, Bundle args) {
                    final MessageListContext listContext = mListContext;

                    mIsFirstLoad = true;
                    return mLoaderProvider.createLoader(listContext);
                }

                @Override
                public void onLoadFinished(Loader<Cursor> loader, Cursor c) {

                    LoaderProvider.MessagesCursor cursor = (LoaderProvider.MessagesCursor) c;
                    HcLog.D(EmailUtils.DEBUG, TAG + " #onLoadFinished "+ " onLoadFinished(messages) mailboxId=" + mMailboxId + " cursor count = "+cursor.getCount());
                    // Update the list
                    mTasksView.updateMessagesList(cursor);
                    if (cursor.getCount() > 0 || mMailboxType == Mailbox.TYPE_OUTBOX) {
                        mTasksView.hideLoadingDialog();
                    }

                    if (!cursor.mIsFound) {
                        mTasksView.onMailboxNotFound(mIsFirstLoad);
                        return;
                    }

                    // Get the "extras" part.
                    mAccount = cursor.mAccount;
                    mMailbox = cursor.mMailbox;
                    mIsRefreshable = cursor.mIsRefreshable;
                    mCountTotalAccounts = cursor.mCountTotalAccounts;



                    // Various post processing...
                    autoRefreshStaleMailbox();
                    if (!isEmptyAndLoading(cursor)) {
                        mTasksView.setListAdapter(mIsFirstLoad);
                    }
                    mIsFirstLoad = false;

                    HcLog.D(EmailUtils.DEBUG, TAG + " #onLoadFinished "+ " onLoadFinished end!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
                }

                /**
                 * Determines whether or not the list is empty, but we're still potentially loading data.
                 * This represents an ambiguous state where we may not want to show "No messages", since
                 * it may still just be loading.
                 */
                private boolean isEmptyAndLoading(Cursor cursor) {
                    if (mMailbox == null) return false;
                    return cursor.getCount() == 0 && mRefreshManager.isMessageListRefreshing(mMailbox.mId);
                }

                @Override
                public void onLoaderReset(Loader<Cursor> loader) {
                    HcLog.D(TAG + " #onLoaderReset!!!!!!!!!!!!");
                    mTasksView.updateMessagesList(null);
                    mAccount = null;
                    mMailbox = null;
                    mCountTotalAccounts = 0;
                }
            };


    /**
     * Implements a timed refresh of "stale" mailboxes.  This should only happen when
     * multiple conditions are true, including:
     *   Only refreshable mailboxes.
     *   Only when the mailbox is "stale" (currently set to 5 minutes since last refresh)
     * Note we do this even if it's a push account; even on Exchange only inbox can be pushed.
     */
    private void autoRefreshStaleMailbox() {
        if (!mIsRefreshable) {
            // Not refreshable (special box such as drafts, or magic boxes)
            return;
        }
        if (!mRefreshManager.isMailboxStale(mMailboxId)) {
            return;
        }
        onRefresh(true);
    }

    /**
     * Refresh the list.  NOOP for special mailboxes (e.g. combined inbox).
     *
     * Note: Manual refresh is enabled even for push accounts.
     */
    public void onRefresh(boolean userRequest) {
        HcLog.D(EmailUtils.DEBUG, TAG + " #onRefresh userRequest ="+userRequest + " mIsRefreshable = "+mIsRefreshable);
        if (mIsRefreshable) {
            mRefreshManager.refreshMessageList(mAccountId, mMailboxId, userRequest);
        }
    }

    @Override
    public void onDestory() {
        mRefreshManager.unregisterListener(mRefreshListener);
        mController.removeResultCallback(mControllerResult);
    }

    private class ControllerResult extends Controller.Result {
        @Override
        public void updateMailboxCallback(MessagingException result, long accountId, long mailboxId, int progress, int numNewMessages, ArrayList<Long> addedMessages) {
            HcLog.D(EmailUtils.DEBUG, TAG + "$ControllerResult#updateMailboxCallback result = "+result + " accountId = "+accountId + " mailboxId = "+mailboxId + " numNewMessages ="+numNewMessages + " progress = "+progress);
            if (result != null) { // failed
                mTasksView.hideLoadingDialog();
                return;
            }

            if (progress == 100) { // success
                mTasksView.hideLoadingDialog();
            }
        }
    }
}