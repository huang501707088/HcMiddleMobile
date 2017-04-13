package com.android.hcframe.doc;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.database.Cursor;
import android.net.Uri;
import android.os.Handler;
import android.text.method.PasswordTransformationMethod;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import com.android.hcframe.AbstractPage;
import com.android.hcframe.push.HcAppState;
import com.artifex.mupdfdemo.AsyncTask;
import com.artifex.mupdfdemo.MuPDFAlert;
import com.artifex.mupdfdemo.MuPDFCore;
import com.artifex.mupdfdemo.MuPDFPageAdapter;
import com.artifex.mupdfdemo.MuPDFReaderView;
import com.artifex.mupdfdemo.MuPDFView;

import java.io.File;
import java.io.InputStream;
import java.util.Observable;
import java.util.concurrent.Executor;

public class ShowPdfView extends AbstractPage {

	private String mFileName;

	private String mTitle;

	private Uri uri;

	private String action;

	private RelativeLayout show_pdf_parent;

	private AlertDialog.Builder mAlertBuilder;

	private MuPDFCore core;

	private String filePath = null;

	private EditText mPasswordView;

	private MuPDFReaderView mDocView;

	private SeekBar mPageSlider;

	private int mPageSliderRes;

	private TextView mPageNumberView;

	private TextView mInfoView;

	private boolean mButtonsVisible;

	private TextView mFilenameView;

	private AsyncTask<Void, Void, MuPDFAlert> mAlertTask;

	private View mButtonsView;

	private AlertDialog mAlertDialog;

	private boolean mAlertsActive = false;

	private TopBarMode mTopBarMode = TopBarMode.Main;

	private float mScale;

	enum TopBarMode {
		Main, Search, Annot, Delete, More, Accept
	};

	enum Hit {
		Nothing, Widget, Annotation
	};

	public static final int ShowButton = 10000;

	public static final int HideButton = 10001;

	private Handler mHandler;

	protected ShowPdfView(Activity context, ViewGroup group) {
		super(context, group);
	}

	protected ShowPdfView(Activity context, ViewGroup group, String mFileName,
			String mTitle, String action, Uri uri, float scale, Handler handler) {
		super(context, group);

		this.mFileName = mFileName;

		this.mTitle = mTitle;

		this.action = action;

		this.uri = uri;

		mScale = scale;

		mHandler = handler;

	}

	@Override
	public void update(Observable observable, Object data) {

	}

	@Override
	public void onClick(View view) {

	}

	@Override
	public void initialized() {

		mAlertBuilder = new AlertDialog.Builder(mContext);

		if (core == null) {
			byte buffer[] = null;
			if (Intent.ACTION_VIEW.equals(action)) {
				filePath = uri.toString();
				if (uri.toString().startsWith("content://")) {
					// Handle view requests from the Transformer Prime's file
					// manager
					// Hopefully other file managers will use this same scheme,
					// if not
					// using explicit paths.
					Cursor cursor = mContext.getContentResolver().query(uri,
							new String[] { "_data" }, null, null, null);
					if (cursor.moveToFirst()) {
						String str = cursor.getString(0);
						String reason = null;
						if (str == null) {
							try {
								InputStream is = mContext.getContentResolver()
										.openInputStream(uri);
								int len = is.available();
								buffer = new byte[len];
								is.read(buffer, 0, len);
								is.close();
							} catch (OutOfMemoryError e) {
								System.out
										.println("Out of memory during buffer reading");
								reason = e.toString();
							} catch (Exception e) {
								reason = e.toString();
							}
							if (reason != null) {
								buffer = null;
								Resources res = mContext.getResources();
								AlertDialog alert = mAlertBuilder.create();
								alert.setButton(AlertDialog.BUTTON_POSITIVE,
										mContext.getString(R.string.dismiss),
										new DialogInterface.OnClickListener() {
											public void onClick(
													DialogInterface dialog,
													int which) {
												HcAppState.getInstance().removeActivity(mContext);
												mContext.finish();
											}
										});
								alert.setCancelable(false);
								alert.show();
								return;
							}
						} else {
							uri = Uri.parse(str);
						}
					}
				}
				if (buffer != null) {
					core = openBuffer(buffer);
				} else {
					core = openFile(Uri.decode(uri.getEncodedPath()));
				}
			}
			if (core != null && core.needsPassword()) {
				requestPassword();
				return;
			}
			if (core != null && core.countPages() == 0) {
				core = null;
			}
		}
		if (core == null) {
			AlertDialog alert = mAlertBuilder.create();
			alert.setTitle(R.string.cannot_open_document);
			alert.setButton(AlertDialog.BUTTON_POSITIVE,
					mContext.getString(R.string.dismiss),
					new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int which) {
							// 删除原先的文件

							File file = new File(filePath);
							if (file != null && file.exists())
								file.delete();
							HcAppState.getInstance().removeActivity(mContext);
							mContext.finish();
						}
					});
			alert.setCancelable(false);
			alert.show();
			return;
		}

		createUI();

		if (core != null) {
			core.startAlerts();
			createAlertWaiter();
		}
	}

	@Override
	public void setContentView() {
		if (mView == null) {
			mView = mInflater.inflate(R.layout.show_pdf_view, null);

			show_pdf_parent = (RelativeLayout) mView
					.findViewById(R.id.show_pdf_parent);
		}
	}

	@Override
	public void onResume() {
		super.onResume();

	}

	@Override
	public void onPause() {
		super.onPause();
		if (mFileName != null && mDocView != null) {
			SharedPreferences prefs = mContext
					.getPreferences(Context.MODE_PRIVATE);
			SharedPreferences.Editor edit = prefs.edit();
			edit.putInt("page" + mFileName, mDocView.getDisplayedViewIndex());
			edit.commit();
		}
	}

	private MuPDFCore openBuffer(byte buffer[]) {
		System.out.println("Trying to open byte buffer");
		try {
			core = new MuPDFCore(mContext, buffer);

		} catch (Exception e) {
			System.out.println(e);
			return null;
		}
		return core;
	}

	private MuPDFCore openFile(String path) {
		int lastSlashPos = path.lastIndexOf('/');
		mFileName = new String(lastSlashPos == -1 ? path
				: path.substring(lastSlashPos + 1));
		System.out.println("Trying to open " + path);
		try {
			core = new MuPDFCore(mContext, path);
		} catch (Exception e) {
			System.out.println(e);
			return null;
		}
		return core;
	}

	public void requestPassword() {
		mPasswordView = new EditText(mContext);
		mPasswordView.setInputType(EditorInfo.TYPE_TEXT_VARIATION_PASSWORD);
		mPasswordView
				.setTransformationMethod(new PasswordTransformationMethod());

		AlertDialog alert = mAlertBuilder.create();
		alert.setTitle(R.string.enter_password);
		alert.setView(mPasswordView);
		alert.setButton(AlertDialog.BUTTON_POSITIVE,
				mContext.getString(R.string.okay),
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
						if (core.authenticatePassword(mPasswordView.getText()
								.toString())) {
							createUI();
						} else {
							requestPassword();
						}
					}
				});
		alert.setButton(AlertDialog.BUTTON_NEGATIVE,
				mContext.getString(R.string.cancel),
				new DialogInterface.OnClickListener() {

					public void onClick(DialogInterface dialog, int which) {
						HcAppState.getInstance().removeActivity(mContext);
						mContext.finish();
					}
				});
		alert.show();
	}

	public void createUI() {
		if (core == null)
			return;

		// Now create the UI.
		// First create the document view
		mDocView = new MuPDFReaderView(mContext, mScale) {
			@Override
			protected void onMoveToChild(int i) {
				if (core == null)
					return;
				mPageNumberView.setText(String.format("%d / %d", i + 1,
						core.countPages()));
				mPageSlider.setMax((core.countPages() - 1) * mPageSliderRes);
				mPageSlider.setProgress(i * mPageSliderRes);
				super.onMoveToChild(i);
			}

			@Override
			protected void onTapMainDocArea() {
				if (!mButtonsVisible) {
					showButtons();
					if (mHandler != null)
						mHandler.sendEmptyMessage(ShowButton);
				} else {
					if (mTopBarMode == TopBarMode.Main)
						hideButtons();
					if (mHandler != null)
						mHandler.sendEmptyMessage(HideButton);
				}
			}

			@Override
			protected void onDocMotion() {
				hideButtons();
				if (mHandler != null)
					mHandler.sendEmptyMessage(HideButton);
			}

			protected void onHit(Hit item) {
				switch (mTopBarMode) {
				case Annot:
					if (item == Hit.Annotation) {
						showButtons();
						if (mHandler != null)
							mHandler.sendEmptyMessage(ShowButton);
						mTopBarMode = TopBarMode.Delete;
						// mTopBarSwitcher.setDisplayedChild(mTopBarMode.ordinal());
					}
					break;
				case Delete:
					mTopBarMode = TopBarMode.Annot;
					// mTopBarSwitcher.setDisplayedChild(mTopBarMode.ordinal());
					// fall through
				default:
					// Not in annotation editing mode, but the pageview will
					// still select and highlight hit annotations, so
					// deselect just in case.
					MuPDFView pageView = (MuPDFView) mDocView
							.getDisplayedView();
					if (pageView != null)
						pageView.deselectAnnotation();
					break;
				}
			}
		};
		mDocView.setAdapter(new MuPDFPageAdapter(mContext, core));

		// Make the buttons overlay, and store all its
		// controls in variables
		makeButtonsView();

		// Set up the page slider
		int smax = Math.max(core.countPages() - 1, 1);
		mPageSliderRes = ((10 + smax - 1) / smax) * 2;

		// Set the file-name text
		mFilenameView.setText(mFileName);

		// Activate the seekbar
		mPageSlider
				.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
					public void onStopTrackingTouch(SeekBar seekBar) {
						mDocView.setDisplayedViewIndex((seekBar.getProgress() + mPageSliderRes / 2)
								/ mPageSliderRes);
					}

					public void onStartTrackingTouch(SeekBar seekBar) {
					}

					public void onProgressChanged(SeekBar seekBar,
							int progress, boolean fromUser) {
						updatePageNumView((progress + mPageSliderRes / 2)
								/ mPageSliderRes);
					}
				});

		// Reenstate last state if it was recorded
		SharedPreferences prefs = mContext.getPreferences(Context.MODE_PRIVATE);
		mDocView.setDisplayedViewIndex(prefs.getInt("page" + mFileName, 0));

		// if (savedInstanceState == null
		// || !savedInstanceState.getBoolean("ButtonsHidden", false))
		// showButtons();

		// Stick the document view and the buttons overlay into a parent view
		// RelativeLayout layout = new RelativeLayout(this);
		// layout.addView(mDocView);
		// layout.addView(mButtonsView);
		// setContentView(layout);

		show_pdf_parent.addView(mDocView);
		show_pdf_parent.addView(mButtonsView);

	}

	private void showButtons() {
		if (core == null)
			return;
		if (!mButtonsVisible) {
			mButtonsVisible = true;
			// Update page number text and slider
			int index = mDocView.getDisplayedViewIndex();
			updatePageNumView(index);
			mPageSlider.setMax((core.countPages() - 1) * mPageSliderRes);
			mPageSlider.setProgress(index * mPageSliderRes);

			Animation anim = null;// new TranslateAnimation(0, 0,
									// -mTopBarSwitcher.getHeight(), 0);
			// anim.setDuration(200);
			// anim.setAnimationListener(new Animation.AnimationListener() {
			// public void onAnimationStart(Animation animation) {
			// mTopBarSwitcher.setVisibility(View.VISIBLE);
			// }
			// public void onAnimationRepeat(Animation animation) {}
			// public void onAnimationEnd(Animation animation) {}
			// });
			// mTopBarSwitcher.startAnimation(anim);

			anim = new TranslateAnimation(0, 0, mPageSlider.getHeight(), 0);
			anim.setDuration(200);
			anim.setAnimationListener(new Animation.AnimationListener() {
				public void onAnimationStart(Animation animation) {
					mPageSlider.setVisibility(View.VISIBLE);
				}

				public void onAnimationRepeat(Animation animation) {
				}

				public void onAnimationEnd(Animation animation) {
					mPageNumberView.setVisibility(View.VISIBLE);
				}
			});
			mPageSlider.startAnimation(anim);
		}
	}

	private void hideButtons() {
		if (mButtonsVisible) {
			mButtonsVisible = false;

			Animation anim = null;// new TranslateAnimation(0, 0, 0,
									// -mTopBarSwitcher.getHeight());
			// anim.setDuration(200);
			// anim.setAnimationListener(new Animation.AnimationListener() {
			// public void onAnimationStart(Animation animation) {}
			// public void onAnimationRepeat(Animation animation) {}
			// public void onAnimationEnd(Animation animation) {
			// mTopBarSwitcher.setVisibility(View.INVISIBLE);
			// }
			// });
			// mTopBarSwitcher.startAnimation(anim);

			anim = new TranslateAnimation(0, 0, 0, mPageSlider.getHeight());
			anim.setDuration(200);
			anim.setAnimationListener(new Animation.AnimationListener() {
				public void onAnimationStart(Animation animation) {
					mPageNumberView.setVisibility(View.INVISIBLE);
				}

				public void onAnimationRepeat(Animation animation) {
				}

				public void onAnimationEnd(Animation animation) {
					mPageSlider.setVisibility(View.INVISIBLE);
				}
			});
			mPageSlider.startAnimation(anim);
		}
	}

	private void makeButtonsView() {
		mButtonsView = mInflater.inflate(R.layout.buttons, null);
		mFilenameView = (TextView) mButtonsView.findViewById(R.id.docNameText);
		mPageSlider = (SeekBar) mButtonsView.findViewById(R.id.pageSlider);
		mPageNumberView = (TextView) mButtonsView.findViewById(R.id.pageNumber);
		mInfoView = (TextView) mButtonsView.findViewById(R.id.info);
		// mTopBarSwitcher =
		// (ViewAnimator)mButtonsView.findViewById(R.id.switcher);

		// mTopBarSwitcher.setVisibility(View.INVISIBLE);
		mPageNumberView.setVisibility(View.INVISIBLE);
		mInfoView.setVisibility(View.INVISIBLE);
		mPageSlider.setVisibility(View.INVISIBLE);
	}

	private void updatePageNumView(int index) {
		if (core == null)
			return;
		mPageNumberView.setText(String.format("%d / %d", index + 1,
				core.countPages()));
	}

	@Override
	public void release() {
		// TODO Auto-generated method stub
		super.release();

		if (core != null) {
			destroyAlertWaiter();
			core.stopAlerts();
		}

		if (core != null)
			core.onDestroy();
		if (mAlertTask != null) {
			mAlertTask.cancel(true);
			mAlertTask = null;
		}
		core = null;
	}

	public void createAlertWaiter() {
		mAlertsActive = true;
		// All mupdf library calls are performed on asynchronous tasks to avoid
		// stalling
		// the UI. Some calls can lead to javascript-invoked requests to display
		// an
		// alert dialog and collect a reply from the user. The task has to be
		// blocked
		// until the user's reply is received. This method creates an
		// asynchronous task,
		// the purpose of which is to wait of these requests and produce the
		// dialog
		// in response, while leaving the core blocked. When the dialog receives
		// the
		// user's response, it is sent to the core via replyToAlert, unblocking
		// it.
		// Another alert-waiting task is then created to pick up the next alert.
		if (mAlertTask != null) {
			mAlertTask.cancel(true);
			mAlertTask = null;
		}
		if (mAlertDialog != null) {
			mAlertDialog.cancel();
			mAlertDialog = null;
		}
		mAlertTask = new AsyncTask<Void, Void, MuPDFAlert>() {

			@Override
			protected MuPDFAlert doInBackground(Void... arg0) {
				if (!mAlertsActive)
					return null;

				return core.waitForAlert();
			}

			@Override
			protected void onPostExecute(final MuPDFAlert result) {
				// core.waitForAlert may return null when shutting down
				if (result == null)
					return;
				final MuPDFAlert.ButtonPressed pressed[] = new MuPDFAlert.ButtonPressed[3];
				for (int i = 0; i < 3; i++)
					pressed[i] = MuPDFAlert.ButtonPressed.None;
				DialogInterface.OnClickListener listener = new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
						mAlertDialog = null;
						if (mAlertsActive) {
							int index = 0;
							switch (which) {
							case AlertDialog.BUTTON1:
								index = 0;
								break;
							case AlertDialog.BUTTON2:
								index = 1;
								break;
							case AlertDialog.BUTTON3:
								index = 2;
								break;
							}
							result.buttonPressed = pressed[index];
							// Send the user's response to the core, so that it
							// can
							// continue processing.
							core.replyToAlert(result);
							// Create another alert-waiter to pick up the next
							// alert.
							createAlertWaiter();
						}
					}
				};
				mAlertDialog = mAlertBuilder.create();
				mAlertDialog.setTitle(result.title);
				mAlertDialog.setMessage(result.message);
				switch (result.iconType) {
				case Error:
					break;
				case Warning:
					break;
				case Question:
					break;
				case Status:
					break;
				}
				switch (result.buttonGroupType) {
				case OkCancel:
					mAlertDialog.setButton(AlertDialog.BUTTON2,
							mContext.getString(R.string.cancel), listener);
					pressed[1] = MuPDFAlert.ButtonPressed.Cancel;
				case Ok:
					mAlertDialog.setButton(AlertDialog.BUTTON1,
							mContext.getString(R.string.okay), listener);
					pressed[0] = MuPDFAlert.ButtonPressed.Ok;
					break;
				case YesNoCancel:
					mAlertDialog.setButton(AlertDialog.BUTTON3,
							mContext.getString(R.string.cancel), listener);
					pressed[2] = MuPDFAlert.ButtonPressed.Cancel;
				case YesNo:
					mAlertDialog.setButton(AlertDialog.BUTTON1,
							mContext.getString(R.string.yes), listener);
					pressed[0] = MuPDFAlert.ButtonPressed.Yes;
					mAlertDialog.setButton(AlertDialog.BUTTON2,
							mContext.getString(R.string.no), listener);
					pressed[1] = MuPDFAlert.ButtonPressed.No;
					break;
				}
				mAlertDialog
						.setOnCancelListener(new DialogInterface.OnCancelListener() {
							public void onCancel(DialogInterface dialog) {
								mAlertDialog = null;
								if (mAlertsActive) {
									result.buttonPressed = MuPDFAlert.ButtonPressed.None;
									core.replyToAlert(result);
									createAlertWaiter();
								}
							}
						});

				mAlertDialog.show();
			}
		};

		mAlertTask.executeOnExecutor(new ThreadPerTaskExecutor());
	}

	public void destroyAlertWaiter() {
		mAlertsActive = false;
		if (mAlertDialog != null) {
			mAlertDialog.cancel();
			mAlertDialog = null;
		}
		if (mAlertTask != null) {
			mAlertTask.cancel(true);
			mAlertTask = null;
		}
	}

	class ThreadPerTaskExecutor implements Executor {
		public void execute(Runnable r) {
			new Thread(r).start();
		}
	}

}
