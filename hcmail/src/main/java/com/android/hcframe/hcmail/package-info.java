/**
 * @author jrjin
 * @time 2015-12-30 下午2:34:33
 * 邮箱
 */
/**
 * 1.{@link com.android.email.MessagingController#processPendingDataChange}
 * 方法里更改mailbox.mType == Mailbox.TYPE_DRAFTS需要远程访问  1468行
 * <p>2.{@link com.android.email.MessagingController#synchronizeMailboxGeneric}
 * 方法里更改mailbox.mType == Mailbox.TYPE_DRAFTS需要远程访问  742行</>
 * <p>3.{@link Mailbox#isRefreshable} TYPE_DRAFTS 需要返回true 479行</p>
 * <p>4.用户 存储规则:accountId为数据库中自增长的主键</p>
 * <p>5.邮箱的ID,mailboxId为数据库中自增长的主键,可以根据用户ID和邮箱类型(mailboxType)获取唯一的邮箱Id;
 * 所以不同用户的mailboxId肯定是唯一的</p>
 * <p>6.{@link com.android.emailcommon.provider.Mailbox#loadsFromServer(java.lang.String)}
 * 方法里更改mType != Mailbox.TYPE_DRAFTS为访问服务器 515行</p>
 * <p>7.{@link com.android.emailcommon.provider.Mailbox#uploadsToServer(android.content.Context)}
 * 方法里mType == TYPE_DRAFTS返回true 527行</p>
 */
package com.android.hcframe.hcmail;