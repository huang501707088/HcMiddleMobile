package com.android.hcframe.hcmail.task;

import android.database.Cursor;

import com.android.hcframe.mvp.BasePresenter;
import com.android.hcframe.mvp.BaseView;
import com.android.emailcommon.provider.Mailbox;

import java.util.List;

/**
 * @Company 浙 江 鸿 程 计 算 机 系 统 有 限 公 司
 * @URL http://www.zjhcsoft.com
 * @Address 杭州滨江区伟业路1号
 * @Email jinjr@zjhcsoft.com
 * Created by jrjin on 17-3-22 16:32.
 */

public interface MessageListTaskContract {

    interface View extends BaseView<Presenter> {

        /**
         * 设置页面的标题
         * @param title 标题
         */
        void setTitle(String title);

        /**
         * 显示邮件列表
         * @param c
         */
        void showMessageList(Cursor c);

        /**
         * 显示详情页
         */
        void showTaskDetails(long messageId);

        /**
         * 显示写邮件页
         */
        void showTaskComposeMail();

        /**
         * 显示编辑页
         * @param mailboxType 邮箱类型: {@link Mailbox#TYPE_INBOX}; {@link Mailbox#TYPE_DRAFTS};
         *                    {@link Mailbox#TYPE_OUTBOX}; {@link Mailbox#TYPE_SENT};
         *                    {@link Mailbox#TYPE_TRASH}
         */
        void showEditView(int mailboxType);

        /**
         * 隐藏编辑页
         */
        void hideEditView();


        /**
         * 设置编辑页的标题和按钮
         * @param title 标题
         * @param btn 按钮文字(全选/全不选)
         */
        void setEditView(String title, String btn);

        /**
         * 显示加载对话框
         * @param description
         */
        void showLoadingDialog(String description);

        /**
         * 隐藏加载对话框
         */
        void hideLoadingDialog();

        /**
         * Called when the specified mailbox does not exist.
         */
        void onMailboxNotFound(boolean firstLoad);

        void updateMessagesList(Cursor c);

        void setListAdapter(boolean firstLoad);

    }

    interface Presenter extends BasePresenter {

        /**
         * 加载邮件列表
         * @param mailboxType 邮箱类型: {@link Mailbox#TYPE_INBOX}; {@link Mailbox#TYPE_DRAFTS};
         *                    {@link Mailbox#TYPE_OUTBOX}; {@link Mailbox#TYPE_SENT};
         *                    {@link Mailbox#TYPE_TRASH}
         * @param refresh 是否是刷新操作,true:刷新操作;false:获取更多操作.
         * @param messageId 邮件的起始Id
         */
        void loadMessageList(int mailboxType, boolean refresh, long messageId);

        /**
         * 打开详情页的操作
         * @param messageId 邮件的id
         */
        void openTaskDetails(long messageId);

        /**
         * 写邮件
         */
        void openTaskComposeMail();

        /**
         * 进入编辑模式
         * @param mailboxType 邮箱类型: {@link Mailbox#TYPE_INBOX}; {@link Mailbox#TYPE_DRAFTS};
         *                    {@link Mailbox#TYPE_OUTBOX}; {@link Mailbox#TYPE_SENT};
         *                    {@link Mailbox#TYPE_TRASH}
         */
        void openEditMode(int mailboxType);

        /**
         * 退出编辑模式
         */
        void closeEditMode();

        /**
         * 删除选中的邮件
         * @param messageIds 邮件的ids
         */
        void deleteMessages(List<Long> messageIds);

        /**
         * 删除选中的邮件
         * @param messageIds 邮件的ids
         */
        void deleteMessages(long[] messageIds);

        /**
         * 彻底删除已删除邮箱中选中的邮件
         * @param messageIds 邮件的ids
         */
        void clearMessages(List<Long> messageIds);

        /**
         * 彻底删除已删除邮箱中选中的邮件
         * @param messageIds 邮件的ids
         */
        void clearMessages(long[] messageIds);

        /**
         * 选中或者不选中邮件
         * @param messageId 邮件Id
         */
        void selectMessage(long messageId);

        /**
         * 选中或者不选中全部邮件
         */
        void selectAllMessages();

        /**
         * 打开移动操作的选择界面
         */
        void openPopWindow();

        /**
         * 把已删除的邮件移动到指定的邮箱中
         * @param mailboxId 邮箱类型: {@link Mailbox#TYPE_INBOX}; {@link Mailbox#TYPE_DRAFTS};
         *                    {@link Mailbox#TYPE_OUTBOX}; {@link Mailbox#TYPE_SENT}.
         * @param messageIds 邮件ids
         */
        void moveTo(long mailboxId, List<Long> messageIds);

        void moveTo(long mailboxId, long[] messageIds);

        void onDestory();
    }
}
