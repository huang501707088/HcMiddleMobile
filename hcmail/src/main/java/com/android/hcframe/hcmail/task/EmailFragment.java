package com.android.hcframe.hcmail.task;

import android.app.Fragment;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.android.email.MessageListContext;
import com.android.emailcommon.provider.Account;
import com.android.emailcommon.provider.Mailbox;
import com.android.hcframe.HcDialog;
import com.android.hcframe.HcLog;
import com.android.hcframe.HcUtil;
import com.android.hcframe.TopBarView;
import com.android.hcframe.email.R;
import com.android.hcframe.hcmail.EmailUtils;
import com.android.hcframe.hcmail.MessagesAdapter;
import com.android.hcframe.pull.PullToRefreshBase;
import com.android.hcframe.pull.PullToRefreshListView;
import com.android.hcframe.view.dialog.AlertDialog;
import com.android.hcframe.view.toast.NoDataView;
import com.android.hcmail.DeleteSelectPicPopupWindow;
import com.android.hcmail.HcmailViewActivity;
import com.android.hcmail.HcmailWriteActivity;

import java.util.ArrayList;
import java.util.List;

/**
 * @Company 浙 江 鸿 程 计 算 机 系 统 有 限 公 司
 * @URL http://www.zjhcsoft.com
 * @Address 杭州滨江区伟业路1号
 * @Email jinjr@zjhcsoft.com
 * Created by jrjin on 17-3-23 10:48.
 */

public class EmailFragment extends Fragment implements MessageListTaskContract.View {

    private static final String TAG = "EmailFragment";

    /** Argument name(s) */
    private static final String ARG_LIST_CONTEXT = "listContext";

    private MessageListTaskContract.Presenter mPresenter;

    private TopBarView mTopBar;

    private PullToRefreshListView mListView;

    private TextView mCompose;

    private LinearLayout mEditTopParent;

    private TextView mCanel;

    private TextView mEditTitle;

    private TextView mSelected;

    private RelativeLayout mEditBottomParent;

    private TextView mDelete;

    private LinearLayout mEditBottom;

    private TextView mClear;

    private TextView mMove;

    private MessagesAdapter mAdapter;

    private int mMailboxType = -1;

    private boolean mEditable;

    private String mDefaultEditTitle;

    private NoDataView mNoDataView;

    private DeleteSelectPicPopupWindow mPopWindow;

    public static EmailFragment newInstance(MessageListContext context, int mailboxType) {
        Bundle arguments = new Bundle();
        arguments.putInt(EmailActivity.EXTRA_MAILBOX_TYPE, mailboxType);
        arguments.putParcelable(ARG_LIST_CONTEXT, context);
        EmailFragment fragment = new EmailFragment();
        fragment.setArguments(arguments);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.email_fragment_layout, container, false);
        mTopBar = (TopBarView) view.findViewById(R.id.email_fragment_top_bar);
        mListView = (PullToRefreshListView) view.findViewById(R.id.email_fragment_listview);
        mCompose = (TextView) view.findViewById(R.id.email_fragment_compose_mail);
        mEditTopParent = (LinearLayout) view.findViewById(R.id.email_fragment_edit_top_parent);
        mCanel = (TextView) view.findViewById(R.id.email_fragment_edit_top_left);
        mEditTitle = (TextView) view.findViewById(R.id.email_fragment_edit_top_title);
        mSelected = (TextView) view.findViewById(R.id.email_fragment_edit_top_right);
        mEditBottomParent = (RelativeLayout) view.findViewById(R.id.email_fragment_edit_bottom_parent);
        mDelete = (TextView) view.findViewById(R.id.email_fragment_edit_bottom_delete);
        mEditBottom = (LinearLayout) view.findViewById(R.id.email_fragment_edit_bottom);
        mClear = (TextView) view.findViewById(R.id.email_fragment_edit_bottom_clear);
        mMove = (TextView) view.findViewById(R.id.email_fragment_edit_bottom_move);
        mNoDataView = (NoDataView) view.findViewById(R.id.email_fragment_no_data);
        initListeners();
        return view;
    }

    private void initListeners() {
        mTopBar.setMenuListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mPresenter.openEditMode(mMailboxType);
            }
        });
        mListView.setOnRefreshBothListener(new PullToRefreshBase.OnRefreshBothListener<ListView>() {
            @Override
            public void onPullDownToRefresh(PullToRefreshBase<ListView> refreshView) {
                mPresenter.loadMessageList(mMailboxType, true, 0);
            }

            @Override
            public void onPullUpToRefresh(PullToRefreshBase<ListView> refreshView) {
                mPresenter.loadMessageList(mMailboxType, false, 0);
            }
        });
        mCompose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mMailboxType == Mailbox.TYPE_TRASH) {
                    AlertDialog.showUntitledUneditableDialog(getActivity(), "删除后将无法找回,确认继续执行删除吗?", new AlertDialog.OnClickListener() {
                        @Override
                        public void onClick(int which, String content) {
                            if (which == DialogInterface.BUTTON_POSITIVE) {
//                                mPresenter.closeEditMode();
                                mPresenter.clearMessages(mAdapter.getAllMessageIds());
                            }
                        }
                    });
                } else {
                    mPresenter.openTaskComposeMail();
                }

            }
        });
        mCanel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mPresenter.closeEditMode();
            }
        });
        mSelected.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                mPresenter.selectAllMessages();
                if (mListView.isRefreshing()) return;
                mAdapter.updateAllSelected();
                int size = mAdapter.getSelectedSize();
                if (size > 0) {
                    mSelected.setText("全不选");
                    mEditTitle.setText(mDefaultEditTitle + "(" + size + ")");
                } else {
                    mSelected.setText("全选");
                    mEditTitle.setText(mDefaultEditTitle);
                }
            }
        });
        mDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mAdapter.getSelectedSize() == 0) {
                    HcUtil.showToast(getActivity(), "未选中邮件!");
                    return;
                }
                List<Long> messageIds = new ArrayList<Long>(mAdapter.getSelectedSet());
                mPresenter.closeEditMode();
                mPresenter.deleteMessages(messageIds);
            }
        });
        mClear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mAdapter.getSelectedSize() == 0) {
                    HcUtil.showToast(getActivity(), "未选中邮件!");
                    return;
                }
                AlertDialog.showUntitledUneditableDialog(getActivity(), "删除后将无法找回,确认继续执行删除吗?", new AlertDialog.OnClickListener() {
                    @Override
                    public void onClick(int which, String content) {
                        if (which == DialogInterface.BUTTON_POSITIVE) {
                            List<Long> messageIds = new ArrayList<Long>(mAdapter.getSelectedSet());
                            mPresenter.closeEditMode();
                            mPresenter.clearMessages(messageIds);
                        }
                    }
                });
            }
        });
        mMove.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mAdapter.getSelectedSize() == 0) {
                    HcUtil.showToast(getActivity(), "未选中邮件!");
                    return;
                }
//                mPresenter.openPopWindow();
//点击移动到按钮
                mPopWindow = new DeleteSelectPicPopupWindow(getActivity(), mPopItemClick);
                //显示窗口
                mPopWindow.showAtLocation(getActivity().findViewById(R.id.email_fragment_parent), Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL, 0, 0); //设置layout在PopupWindow中显示的位置
            }
        });

        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                long messageId = (Long) view.getTag(MessagesAdapter.KEY_MESSAGE_ID);
                if (mEditable) {
                    mAdapter.updateSelected(messageId);
                    int size = mAdapter.getSelectedSize();
                    if (mAdapter.allSelected()) {
                        mEditTitle.setText(mDefaultEditTitle + "(" + size + ")");
                        mSelected.setText("全不选");
                    } else {
                        mSelected.setText("全选");
                        if (size > 0) {
                            mEditTitle.setText(mDefaultEditTitle + "(" + size + ")");
                        } else {
                            mEditTitle.setText(mDefaultEditTitle);
                        }
                    }
                } else {
                    if (mMailboxType == Mailbox.TYPE_TRASH) {
                        mPresenter.openEditMode(mMailboxType);
                        mAdapter.updateSelected(messageId);
                        int size = mAdapter.getSelectedSize();
                        if (mAdapter.allSelected()) {
                            mEditTitle.setText(mDefaultEditTitle + "(" + size + ")");
                            mSelected.setText("全不选");
                        } else {
                            mSelected.setText("全选");
                            if (size > 0) {
                                mEditTitle.setText(mDefaultEditTitle + "(" + size + ")");
                            } else {
                                mEditTitle.setText(mDefaultEditTitle);
                            }
                        }
                    } else {
                        mPresenter.openTaskDetails(messageId);
                    }

                }
            }
        });
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mPresenter.start();
    }

    @Override
    public void hideEditView() {
        mEditable = false;
        mEditTopParent.setVisibility(View.GONE);
        mEditBottomParent.setVisibility(View.GONE);
        if (mMailboxType == Mailbox.TYPE_OUTBOX) {
            mListView.setMode(PullToRefreshBase.Mode.DISABLED);
        } else {
            mListView.setMode(PullToRefreshBase.Mode.BOTH); // 这里需要更改,最好是设置为上次的模式
        }

        mAdapter.setEditable(false);
        mAdapter.clear();
    }

    @Override
    public void setTitle(String title) {
        mTopBar.setTitle(title);
        initDate();
    }

    @Override
    public void showMessageList(Cursor c) {

    }

    @Override
    public void showTaskDetails(long messageId) {
        Intent intent = new Intent();
        intent.putExtra("messageId", messageId);
        switch (mMailboxType) {
            case Mailbox.TYPE_INBOX:
            case Mailbox.TYPE_SENT:
                intent.setClass(getActivity(), HcmailViewActivity.class);
                startActivity(intent);
                break;
            case Mailbox.TYPE_OUTBOX:
            case Mailbox.TYPE_DRAFTS:
                intent.setClass(getActivity(), HcmailWriteActivity.class);
                startActivity(intent);
                break;

            default:
                break;
        }
    }

    @Override
    public void showTaskComposeMail() {
        //写邮件
        Intent intent = new Intent();
        intent.setClass(getActivity(), HcmailWriteActivity.class);
        startActivity(intent);
        getActivity().finish();
    }

    @Override
    public void showEditView(int mailboxType) {
        mEditable = true;
        mAdapter.setEditable(true);
        switch (mailboxType) {
            case Mailbox.TYPE_INBOX: // 收件箱
            case Mailbox.TYPE_OUTBOX: // 发件箱,存本地
            case Mailbox.TYPE_SENT: // 已发送
            case Mailbox.TYPE_DRAFTS: // 草稿
                mEditTopParent.setVisibility(View.VISIBLE);
                mEditBottomParent.setVisibility(View.VISIBLE);
                mDelete.setVisibility(View.VISIBLE);
                mEditBottom.setVisibility(View.GONE);
                break;
            case Mailbox.TYPE_TRASH: //  已删除
                mEditTopParent.setVisibility(View.VISIBLE);
                mEditBottomParent.setVisibility(View.VISIBLE);
                mDelete.setVisibility(View.GONE);
                mEditBottom.setVisibility(View.VISIBLE);
                break;

            default:

                break;
        }
    }

    @Override
    public void setEditView(String title, String btn) {
        mDefaultEditTitle = title;
        mEditTitle.setText(title);
        mSelected.setText(btn);
        mListView.setMode(PullToRefreshBase.Mode.DISABLED);
    }

    @Override
    public void setPresenter(MessageListTaskContract.Presenter presenter) {
        mPresenter = presenter;
    }

    @Override
    public void hideLoadingDialog() {
        HcDialog.deleteProgressDialog();
        mListView.onRefreshComplete();
    }

    @Override
    public void showLoadingDialog(String description) {
        HcDialog.showProgressDialog(getActivity(), description, true);
    }

    @Override
    public void onMailboxNotFound(boolean firstLoad) {
        // Something bad happened - the account or mailbox we were looking for was deleted.
        // Just restart and let the entry flow find a good default view.
        if (firstLoad) {
            // Only show this if it's the first load (e.g. a shortcut) rather an a return to
            // a mailbox (which might be in a just-deleted account)
            HcUtil.showToast(getActivity(), R.string.toast_mailbox_not_found);
        }

        HcLog.D(EmailUtils.DEBUG, TAG + "#onMailboxNotFound isFirstLoad ="+firstLoad);

        getActivity().finish();
    }

    @Override
    public void updateMessagesList(Cursor c) {
        if (mAdapter != null) {
            mAdapter.swapCursor(c);
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (mMailboxType == -1) {
            mMailboxType = getArguments().getInt(EmailActivity.EXTRA_MAILBOX_TYPE, -1);
        }
        mAdapter = new MessagesAdapter(getActivity(), mMailboxType);
    }

    @Override
    public void setListAdapter(boolean firstLoad) {
        if (firstLoad)
            mListView.setAdapter(mAdapter);
        else {
            if (mListView.getAdatper() == null)
                mListView.setAdapter(mAdapter);
        }
    }

    private void initDate() {
        if (mMailboxType == -1) {
            mMailboxType = getArguments().getInt(EmailActivity.EXTRA_MAILBOX_TYPE, Mailbox.TYPE_INBOX);
        }
        switch (mMailboxType) {
            case Mailbox.TYPE_OUTBOX: // 发件箱,存本地
                mListView.setMode(PullToRefreshBase.Mode.DISABLED);
                break;
            case Mailbox.TYPE_INBOX: // 收件箱
            case Mailbox.TYPE_SENT: // 已发送
            case Mailbox.TYPE_DRAFTS: // 草稿
                mListView.setMode(PullToRefreshBase.Mode.BOTH);
                break;
            case Mailbox.TYPE_TRASH: //  已删除
                mListView.setMode(PullToRefreshBase.Mode.BOTH);
                mTopBar.setMenuBtnVisiable(View.GONE);
                mCompose.setText("清空邮件");
                mCompose.setCompoundDrawablesWithIntrinsicBounds(R.drawable.hcmail_delete_box_img, 0, 0, 0);
                break;

            default:
                mMailboxType = Mailbox.TYPE_INBOX;
                break;
        }

        mListView.setScrollingWhileRefreshingEnabled(false);
        mListView.setEmptyView(mNoDataView);
    }

    //为弹出窗口实现监听类
    private View.OnClickListener mPopItemClick = new View.OnClickListener() {

        public void onClick(View v) {
            mPopWindow.dismiss();
            mPopWindow = null;
            long accountId = ((MessageListContext) getArguments().getParcelable(ARG_LIST_CONTEXT)).getAccountId();
            int i = v.getId();
            List<Long> messageIds = new ArrayList<Long>(mAdapter.getSelectedSet());
            mPresenter.closeEditMode();
            if (i == R.id.hcmail_send_box) {
                //转移到发件箱
                long mailboxId = Mailbox.findMailboxOfType(getActivity(), accountId, Mailbox.TYPE_SENT);
                mPresenter.moveTo(mailboxId, messageIds);
            } else if (i == R.id.hcmail_receive_box) {
                //转移到收件箱
                long mailboxId = Mailbox.findMailboxOfType(getActivity(), accountId, Mailbox.TYPE_INBOX);
                mPresenter.moveTo(mailboxId, messageIds);
            } else if (i == R.id.hcmail_draft_box) {
                //转移到草稿箱
                long mailboxId = Mailbox.findMailboxOfType(getActivity(), accountId, Mailbox.TYPE_DRAFTS);
                mPresenter.moveTo(mailboxId, messageIds);
            }
        }
    };

    @Override
    public void onDestroy() {
        mPresenter.onDestory();
        super.onDestroy();
    }
}
