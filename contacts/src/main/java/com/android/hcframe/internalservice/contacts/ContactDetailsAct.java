package com.android.hcframe.internalservice.contacts;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.android.hcframe.HcBaseActivity;
import com.android.hcframe.HcConfig;
import com.android.hcframe.HcLog;
import com.android.hcframe.HcUtil;
import com.android.hcframe.TopBarView;
import com.android.hcframe.command.CommandControl;
import com.android.hcframe.contacts.data.EmployeeInfo;
import com.android.hcframe.push.HcAppState;
import com.android.hcframe.share.ShareActivity;
import com.android.hcframe.view.scroll.HcScrollView;
import com.nostra13.universalimageloader.core.ImageLoader;

import java.util.HashMap;
import java.util.Map;

public class ContactDetailsAct extends HcBaseActivity implements
		OnClickListener {

	private static final String TAG = "ContactDetailsAct";

	private TopBarView mTopBarView;

	private TextView contact_details_name;

	private TextView contact_details_departname;

	private ImageView mHeader;

	/** pop */
	private RelativeLayout mPopView;

	private TextView contact_save_lly;

	private TextView mShareBtn;

	/** 员工信息 */
	private TextView plane_tel_value; // 座机号

	private TextView extension_tel_value; // 座机分号
	private LinearLayout mExtensionTelParent;
	private View mExtensionTelDivider;

	private TextView mobile_tel_value; // 移动电话

	private ImageView details_send_msg;

	private TextView virtual_tel_value; // 虚拟网短号

	private TextView spare_tel_value;  //备用电话

	private TextView email_value; // 邮箱

	private TextView email_spare_value; // 备用邮箱

	private EmployeeInfo emp;

	/** 发送消息 */
	private TextView mSendBtn;

	private boolean mShowPopwindow;

	/** 是否为IM模块 */
	private boolean mIMModule;

	public static final String SEND_MSG = "com.android.hcframe.internalservice.contacts.SEND_MSG";

	private HcScrollView mScrollView;

	private boolean mTransparent = true;

	@Override
	protected void onCreate(Bundle bundle) {
		// TODO Auto-generated method stub
		super.onCreate(bundle);
		/**
		 * @author jrjin
		 * @date 2015-10-22 上午11:00:40
		 * */
		Intent intent = getIntent();
		if (intent == null || intent.getExtras() == null) {
			HcAppState.getInstance().removeActivity(this);
			finish();
			return;
		}
		emp = intent.getParcelableExtra("emp");
		mIMModule = intent.getBooleanExtra("im", false);
		String userId = intent.getStringExtra("userId");
		if (null == emp) {
			if (userId != null) {
				emp = (EmployeeInfo) ContactsOperateDatabase.getContact(this, userId);
				if (emp == null) {
					HcAppState.getInstance().removeActivity(this);
					finish();
					return;
				}
			} else {
				HcAppState.getInstance().removeActivity(this);
				finish();
				return;
			}

		}
		setContentView(R.layout.activity_contact_details);

		ininView();

		initData();

	}

	private void initData() {
		HcLog.D(TAG + " intent = " + getIntent() + " emp = " + emp);
		mTopBarView.setMenuSrc(R.drawable.contact_menu_icon);

		if (emp != null) {
			contact_details_name.setText(emp.getName());

			contact_details_departname.setText(emp.getParentName());
			String uri = HcUtil.getScheme() + "/terminalServer/szf/getEmpIcon?user_id=" + emp.getUserId() +
					"&clientId="+ HcConfig.getConfig().getClientId() +"&token=-1";
			ImageLoader.getInstance().displayImage(uri, mHeader, HcUtil.getAccountImageOptions());

			plane_tel_value.setText(emp.getFixedPhone());

//			if (TextUtils.isEmpty(emp.getExtensionNumber())) {
//				findViewById(R.id.contacts_detail_phone_divider).setVisibility(View.INVISIBLE);
//			} else {
//				extension_tel_value.setText(emp.getExtensionNumber());
//			}
			if (TextUtils.isEmpty(emp.getExtensionNumber())) {
				mExtensionTelParent.setVisibility(View.GONE);
				mExtensionTelDivider.setVisibility(View.GONE);
			} else {
				extension_tel_value.setText(emp.getExtensionNumber());
			}


			// 手机号码处理...
			String phone = emp.getMobilePhone();
			if (TextUtils.isEmpty(phone)) {
				phone = "";
			}
			HcLog.D(TAG + " #initData phone = "+phone + " Visibility = " +emp.getPhoneVisibility());
			mobile_tel_value.setText((!HcConfig.getConfig().hasContactsSecrecy() && emp.getPhoneVisibility() == true) ? phone
				: phone.length() > 3 ? phone.substring(0, 3) + "********" : phone);


			// 备用手机处理...
			String standPhone = emp.getStandbyPhone();
			if (TextUtils.isEmpty(standPhone)) {
				standPhone = "";
			}
			HcLog.D(TAG + " #initData standPhone = "+standPhone + " Visibility = " +emp.getPhoneVisibility());
			spare_tel_value.setText((!HcConfig.getConfig().hasContactsSecrecy() && emp.getPhoneVisibility() == true) ? standPhone
					: standPhone.length() > 3 ? standPhone.substring(0, 3) + "********" : standPhone);

			// 电子邮箱处理...
			String email = emp.getEmail();
			if (TextUtils.isEmpty(email)) {
				email = "";
			} else {
				if (HcConfig.getConfig().hasContactsSecrecy()) {
					if (email.contains("@")) { // 如果最前面是@符号,则不做任何处理.
						if (email.endsWith("@")) { // 最后一个为@符号
							email = "***";
						} else if (email.startsWith("@")) {
							;
						} else {
							String[] emails = email.split("@");
							// 这里肯定长度肯定大于1
							email = "***@" + emails[1];
						}

					} else {
						email = "***";
					}
				}

			}
			email_value.setText(email);

			// 备用邮箱处理...
			String standEmail = emp.getStandEmail();
			if (TextUtils.isEmpty(standEmail)) {
				standEmail = "";
			} else {
				if (HcConfig.getConfig().hasContactsSecrecy()) {
					if (standEmail.contains("@")) { // 如果最前面是@符号,则不做任何处理.
						if (standEmail.endsWith("@")) { // 最后一个为@符号
							standEmail = "***";
						} else if (standEmail.startsWith("@")) {
							;
						} else {
							String[] emails = standEmail.split("@");
							// 这里肯定长度肯定大于1
							standEmail = "***@" + emails[1];
						}

					} else {
						standEmail = "***";
					}
				}

			}
			email_spare_value.setText(standEmail);


			virtual_tel_value.setText(emp.getVirtualNetNumber());

		}
	}

	private String shareInfo() {
		StringBuffer shareInfo = new StringBuffer();
		if (!HcUtil.isEmpty(emp.getName())) {
			shareInfo.append("姓名：" + emp.getName());
		}
		if (!HcUtil.isEmpty(emp.getParentName())) {
			shareInfo.append("\n部门：" + emp.getParentName());
		}
		if (!HcUtil.isEmpty(emp.getFixedPhone())) {
			shareInfo.append("\n座机电话：" + emp.getFixedPhone());
		}
		if (!HcUtil.isEmpty(emp.getExtensionNumber())) {
			shareInfo.append("\n分机号：" + emp.getExtensionNumber());
		}
		if (!HcUtil.isEmpty(emp.getMobilePhone())) {
			shareInfo.append("\n移动电话：" + emp.getMobilePhone());
		}
		if (!HcUtil.isEmpty(emp.getVirtualNetNumber())) {
			shareInfo.append("\n虚拟短号：" + emp.getVirtualNetNumber());
		}
		if (!HcUtil.isEmpty(emp.getStandbyPhone())) {
			shareInfo.append("\n备用电话：" + emp.getStandbyPhone());
		}
		if (!HcUtil.isEmpty(emp.getEmail())) {
			shareInfo.append("\n电子邮箱：" + emp.getEmail());
		}
		if (!HcUtil.isEmpty(emp.getStandEmail())) {
			shareInfo.append("\n备用电子邮箱：" + emp.getStandEmail());
		}

		// shareInfo.delete(0, 1);
		return shareInfo.toString();
	}

	private void ininView() {
		mTopBarView = (TopBarView) findViewById(R.id.contacts_detail_top_bar);

		mScrollView = (HcScrollView) findViewById(R.id.contact_details_scrollview);

		contact_details_name = (TextView) findViewById(R.id.contact_details_name);

		contact_details_departname = (TextView) findViewById(R.id.contact_details_departname);

		mHeader = (ImageView) findViewById(R.id.contacts_detail_icon);


		plane_tel_value = (TextView) findViewById(R.id.plane_tel_value);

		extension_tel_value = (TextView) findViewById(R.id.extension_tel_value);
		mExtensionTelDivider = findViewById(R.id.extension_tel_divider);
		mExtensionTelParent = (LinearLayout) findViewById(R.id.extension_tel_parent);

		mobile_tel_value = (TextView) findViewById(R.id.mobile_tel_value);

		details_send_msg = (ImageView) findViewById(R.id.details_send_msg);

		virtual_tel_value = (TextView) findViewById(R.id.virtual_tel_value);

		spare_tel_value = (TextView) findViewById(R.id.spare_tel_value);

		email_value = (TextView) findViewById(R.id.email_value);

		email_spare_value = (TextView) findViewById(R.id.email_spare_value);


		mPopView = (RelativeLayout) findViewById(R.id.contacts_detail_pop_view);
		contact_save_lly = (TextView) findViewById(R.id.contact_save_lly);
		mShareBtn = (TextView) findViewById(R.id.contacts_detail_share);

		mSendBtn = (TextView) findViewById(R.id.contacts_detail_send_msg_btn);

		mTopBarView.setMenuListener(this);

		mobile_tel_value.setOnClickListener(this);
		spare_tel_value.setOnClickListener(this);
		virtual_tel_value.setOnClickListener(this);
		plane_tel_value.setOnClickListener(this);
		details_send_msg.setOnClickListener(this);

		mPopView.setOnClickListener(this);
		contact_save_lly.setOnClickListener(this);
		mShareBtn.setOnClickListener(this);

		mSendBtn.setOnClickListener(this);

		email_value.setOnClickListener(this);
		email_spare_value.setOnClickListener(this);

		if (HcConfig.getConfig().hasContactsSecrecy()) {
			mTopBarView.setMenuBtnVisiable(View.GONE);
		}

		if (mIMModule) {
			mSendBtn.setVisibility(View.VISIBLE);
		}

		mScrollView.setOnScrollChangedListener(new HcScrollView.OnScrollChangedListener() {
			@Override
			public void onScrollChanged(int x, int y, int oldX, int oldY) {
				HcLog.D(TAG + " #onScrollChanged x = "+x + " y = "+y + " oldX = "+oldX + " oldY ="+oldY);
				if (y <= 0) {
					if (!mTransparent) {
						mTransparent = !mTransparent;
						mTopBarView.setBackgroundColor(Color.TRANSPARENT);
					}

				} else {
					if (mTransparent) {
						mTransparent = !mTransparent;
						mTopBarView.setBackgroundColor(getResources().getColor(R.color.contact_topbar_bg));
					}

				}
			}
		});
	}

	@Override
	public void onClick(View view) {
		int viewId = view.getId();
		if (viewId == R.id.contacts_detail_share) {
			mPopView.setVisibility(View.GONE);
			mShowPopwindow = !mShowPopwindow;
			showShareActivity();
		} else if (viewId == R.id.contact_save_lly) {
			mPopView.setVisibility(View.GONE);
			mShowPopwindow = !mShowPopwindow;
			emp.saveContacts(this);
		} else if (viewId == R.id.details_send_msg) {
			emp.sendMessage(this, "");
		} else if (viewId == R.id.mobile_tel_value) {
			if (!HcUtil.isEmpty(emp.getMobilePhone())) {
				HcUtil.dial(this, emp.getMobilePhone());
			}
		} else if (viewId == R.id.spare_tel_value) {
			if (!HcUtil.isEmpty(emp.getStandbyPhone())) {
				HcUtil.dial(this, emp.getStandbyPhone());
			}
		} else if (viewId == R.id.plane_tel_value) {
			if (!HcUtil.isEmpty(emp.getFixedPhone())) {
				HcUtil.dial(this, emp.getFixedPhone());
			}
		} else if (viewId == R.id.virtual_tel_value) {
			if (!HcUtil.isEmpty(emp.getVirtualNetNumber())) {
				HcUtil.dial(this, emp.getVirtualNetNumber());
			}
		} else if (viewId == R.id.topbar_menu_btn) {
			if (mShowPopwindow) {
				mPopView.setVisibility(View.GONE);
			} else {
				mPopView.setVisibility(View.VISIBLE);
			}
			mShowPopwindow = !mShowPopwindow;
		} else if (viewId == R.id.contacts_detail_pop_view) {
			// do nothing
			if (mShowPopwindow) {
				mPopView.setVisibility(View.GONE);
				mShowPopwindow = !mShowPopwindow;
			}
		} else if (viewId == R.id.contacts_detail_send_msg_btn) {
			if (mIMModule) {
				Intent intent = new Intent(SEND_MSG);
				intent.setPackage(getPackageName());
				intent.setClassName(this, "com.android.hcframe.im.ChatActivity");
				intent.putExtra("emp", emp);
				startActivity(intent);
				setResult(Activity.RESULT_OK);
				finish();
			}
		} else if (viewId == R.id.email_value) {
			if (!TextUtils.isEmpty(email_value.getText())) {
//				sendEmail(email_value.getText().toString());
				writeLocalEmail(emp.getName(), email_value.getText().toString());
			}
		} else if (viewId == R.id.email_spare_value) {
			if (!TextUtils.isEmpty(email_spare_value.getText())) {
//				sendEmail(email_spare_value.getText().toString());
				writeLocalEmail(emp.getName(), email_spare_value.getText().toString());
			}
		}

	}

	private void sendEmail(String to) {
		Intent data = new Intent(Intent.ACTION_SENDTO);
		data.addCategory(Intent.CATEGORY_DEFAULT);
		data.setData(Uri.parse(to));
		data.putExtra(Intent.EXTRA_SUBJECT, "这是标题");
		data.putExtra(Intent.EXTRA_TEXT, "这是内容");
		ComponentName name = data.resolveActivity(getPackageManager());
		if (name != null) {
//			data.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			startActivity(Intent.createChooser(data, "请选择邮件类应用"));
		} else {
			data = new Intent(Intent.ACTION_SEND);
			data.addCategory(Intent.CATEGORY_DEFAULT);
			data.setData(Uri.parse(to));
			data.putExtra(Intent.EXTRA_SUBJECT, "这是标题");
			data.putExtra(Intent.EXTRA_TEXT, "这是内容");
			name = data.resolveActivity(getPackageManager());
			if (name != null) {
//				data.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

				startActivity(data/*Intent.createChooser(data, "请选择邮件类应用")*/);
			} else {
				HcUtil.showToast(this, "没有系统邮箱!");
			}

		}

	}

	private void jumpContact(String name, String tel) {
		Intent it = new Intent(Intent.ACTION_INSERT, Uri.withAppendedPath(
				Uri.parse("content://com.android.contacts"), "contacts"));
		it.setType("vnd.android.cursor.dir/person");
		// it.setType("vnd.android.cursor.dir/contact");
		// it.setType("vnd.android.cursor.dir/raw_contact");
		// 联系人姓名
		it.putExtra(android.provider.ContactsContract.Intents.Insert.NAME, "张三");
		// 公司
		it.putExtra(android.provider.ContactsContract.Intents.Insert.COMPANY,
				"北京XXXXXX公司");
		// email
		it.putExtra(android.provider.ContactsContract.Intents.Insert.EMAIL,
				"123456@qq.com");
		// 手机号码
		it.putExtra(android.provider.ContactsContract.Intents.Insert.PHONE,
				"010-1234567");
		// 单位电话
		it.putExtra(
				android.provider.ContactsContract.Intents.Insert.SECONDARY_PHONE,
				"18600001111");
		// 住宅电话
		it.putExtra(
				android.provider.ContactsContract.Intents.Insert.TERTIARY_PHONE,
				"010-7654321");
		// 备注信息
		it.putExtra(android.provider.ContactsContract.Intents.Insert.JOB_TITLE,
				"名片");
		startActivity(it);
	}

	private void showShareActivity() {
		Intent intent = new Intent(this, ShareActivity.class);
		intent.putExtra(ShareActivity.SHARE_KEY_TYPE, ShareActivity.SHARE_TEXT);
		intent.putExtra(ShareActivity.SHARE_KEY_CONTENT, shareInfo());
		startActivity(intent);
		overridePendingTransition(R.anim.slide_in_from_bottom, 0);
	}
	

	private void writeLocalEmail(String name, String address) {
		if (HcConfig.getConfig().assertModule(HcConfig.Module.MAIL)) {
			Map<String, String> data = new HashMap<String, String>();
			data.put("name", name);
			data.put("address", address);
			CommandControl.getInstance().sendCommand(this, CommandControl.FLAG_CONTACTS_WRITE_EMAIL, data);
		}
	}
}
