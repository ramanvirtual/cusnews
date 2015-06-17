package com.cusnews.app.activities;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.TypedArray;
import android.os.Build;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceActivity;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.view.ViewGroup.MarginLayoutParams;

import com.chopping.application.LL;
import com.chopping.bus.ApplicationConfigurationDownloadedEvent;
import com.chopping.bus.ApplicationConfigurationLoadingIgnoredEvent;
import com.chopping.exceptions.CanNotOpenOrFindAppPropertiesException;
import com.chopping.exceptions.InvalidAppPropertiesException;
import com.cusnews.R;
import com.cusnews.gcm.RegistrationIntentService;
import com.cusnews.gcm.SubscribeIntentService;
import com.cusnews.gcm.UnregistrationIntentService;
import com.cusnews.gcm.UnsubscribeIntentService;
import com.cusnews.utils.Prefs;

import de.greenrobot.event.EventBus;


/**
 * Setting .
 */
public final class SettingActivity extends PreferenceActivity implements Preference.OnPreferenceChangeListener {

	/**
	 * The "ActionBar".
	 */
	private Toolbar mToolbar;

	/**
	 * Progress indicator.
	 */
	private ProgressDialog mPb;

	private BroadcastReceiver mRegistrationBroadcastReceiver;
	private BroadcastReceiver mUnregistrationBroadcastReceiver;
	private BroadcastReceiver mSubscribeBroadcastReceiver;
	private BroadcastReceiver mUnsubscribeBroadcastReceiver;
	//------------------------------------------------
	//Subscribes, event-handlers
	//------------------------------------------------

	/**
	 * Handler for {@link com.chopping.bus.ApplicationConfigurationDownloadedEvent}
	 *
	 * @param e
	 * 		Event {@link  com.chopping.bus.ApplicationConfigurationDownloadedEvent}.
	 */
	public void onEvent(ApplicationConfigurationDownloadedEvent e) {
		onAppConfigLoaded();
	}

	/**
	 * Handler for {@link com.chopping.bus.ApplicationConfigurationLoadingIgnoredEvent}.
	 *
	 * @param e
	 * 		Event {@link com.chopping.bus.ApplicationConfigurationLoadingIgnoredEvent}.
	 */
	public void onEvent(ApplicationConfigurationLoadingIgnoredEvent e) {
		LL.i("Ignored a change to load application's configuration.");
		onAppConfigIgnored();
	}

	//------------------------------------------------

	/**
	 * Show an instance of SettingsActivity.
	 *
	 * @param context
	 * 		A context object.
	 */
	public static void showInstance(Activity context) {
		Intent intent = new Intent(context, SettingActivity.class);
		intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
		context.startActivity(intent);
	}


	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Prefs prefs = Prefs.getInstance();
		//		if(getResources().getBoolean(R.bool.landscape)) {
		//			setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
		//		}
		addPreferencesFromResource(R.xml.settings);

		mToolbar = (Toolbar) getLayoutInflater().inflate(R.layout.toolbar, null, false);
		addContentView(mToolbar, new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
		mToolbar.setTitle(R.string.action_settings);
		mToolbar.setTitleTextColor(getResources().getColor(R.color.common_white));
		mToolbar.setNavigationIcon(R.drawable.ic_arrow_back_white_24dp);
		mToolbar.setNavigationOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				backPressed();
			}
		});
		setTitle(R.string.action_settings);

		//Listeners for register and unregister PUSH.
		mRegistrationBroadcastReceiver = new BroadcastReceiver() {
			@Override
			public void onReceive(Context context, Intent intent) {
				if(!TextUtils.isEmpty(Prefs.getInstance().getPushToken())) {
					dismissPb();
				}
			}
		};

		mUnregistrationBroadcastReceiver = new BroadcastReceiver() {
			@Override
			public void onReceive(Context context, Intent intent) {
				if(!TextUtils.isEmpty(Prefs.getInstance().getPushToken())) {
					dismissPb();
				}
			}
		};
		mSubscribeBroadcastReceiver = new BroadcastReceiver() {
			@Override
			public void onReceive(Context context, Intent intent) {
				if(!TextUtils.isEmpty(Prefs.getInstance().getPushToken())) {
					dismissPb();
				}
			}
		};

		mUnsubscribeBroadcastReceiver = new BroadcastReceiver() {
			@Override
			public void onReceive(Context context, Intent intent) {
				if(!TextUtils.isEmpty(Prefs.getInstance().getPushToken())) {
					dismissPb();
				}
			}
		};


		//Feeds-language selection.
		ListPreference sort = (ListPreference) findPreference(Prefs.KEY_LANG_VALUE);
		String value = prefs.getLanguageValue();
		sort.setValue(value);
		int pos = Integer.valueOf(value);
		String[] arr = getResources().getStringArray(R.array.lang_types);
		sort.setSummary(arr[pos]);
		sort.setOnPreferenceChangeListener(this);

		//Show-Image-Mode or not.
		CheckBoxPreference showImages = (CheckBoxPreference) findPreference(Prefs.KEY_SHOW_IMAGES);
		showImages.setOnPreferenceChangeListener(this);

		//Push.
		CheckBoxPreference pushOnOff = (CheckBoxPreference) findPreference(Prefs.KEY_PUSH_ON_OFF);
		pushOnOff.setOnPreferenceChangeListener(this);
		Preference selection =  findPreference(Prefs.KEY_PUSH_TOPICS_SELECTIONS);
		selection.setOnPreferenceClickListener(new OnPreferenceClickListener() {
			@Override
			public boolean onPreferenceClick(Preference preference) {
				TopicListActivity.showInstance(SettingActivity.this);
				return true;
			}
		});


		((MarginLayoutParams) findViewById(android.R.id.list).getLayoutParams()).topMargin = getActionBarHeight(this);
	}


	/**
	 * Get height of {@link android.support.v7.app.ActionBar}.
	 *
	 * @param activity
	 * 		{@link Activity} that hosts an  {@link android.support.v7.app.ActionBar}.
	 *
	 * @return Height of bar.
	 */
	public static int getActionBarHeight(Activity activity) {
		int[] abSzAttr;
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
			abSzAttr = new int[] { android.R.attr.actionBarSize };
		} else {
			abSzAttr = new int[] { R.attr.actionBarSize };
		}
		TypedArray a = activity.obtainStyledAttributes(abSzAttr);
		return a.getDimensionPixelSize(0, -1);
	}


	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			backPressed();
			break;
		}
		return super.onOptionsItemSelected(item);
	}


	@Override
	public boolean onPreferenceChange(Preference preference, Object newValue) {
		if (preference.getKey().equals(Prefs.KEY_LANG_VALUE)) {
			int pos = Integer.valueOf(newValue.toString());
			String[] arr = getResources().getStringArray(R.array.lang_types);
			preference.setSummary(arr[pos]);

			switch (pos) {
			case 0:
				Prefs.getInstance().setLanguage("en");
				break;
			case 1:
				Prefs.getInstance().setLanguage("de");
				break;
			case 2:
				Prefs.getInstance().setLanguage("zh");
				break;
			}
			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			builder.setTitle(R.string.application_name).setMessage(R.string.lbl_app_reload).setNegativeButton(
					R.string.btn_no, null).setPositiveButton(R.string.btn_yes, new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					ActivityCompat.finishAffinity(SettingActivity.this);
				}
			});
			builder.create().show();
		}

		if (preference.getKey().equals(Prefs.KEY_SHOW_IMAGES)) {
			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			builder.setTitle(R.string.application_name).setMessage(R.string.lbl_app_reload).setNegativeButton(
					R.string.btn_no, null).setPositiveButton(R.string.btn_yes, new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							ActivityCompat.finishAffinity(SettingActivity.this);
						}
					});
			builder.create().show();
		}


		//Push
		String lang = Prefs.getInstance().getLanguage();
		if (preference.getKey().equals(Prefs.KEY_PUSH_ON_OFF)) {
			if (Boolean.valueOf(newValue.toString())) {
				mPb = ProgressDialog.show(this, null, getString(R.string.lbl_registering));
				mPb.setCancelable(true);
				Intent intent = new Intent(this, RegistrationIntentService.class);
				startService(intent);
			} else {
				mPb = ProgressDialog.show(this, null, getString(R.string.lbl_unregistered));
				mPb.setCancelable(true);
				Intent intent = new Intent(this, UnregistrationIntentService.class);
				startService(intent);
			}
		}


		//if (preference.getKey().equals(Prefs.KEY_PUSH_TOPICS_SELECTIONS)) {
//			if (Boolean.valueOf(newValue.toString())) {
//				mPb = ProgressDialog.show(this, null, getString(R.string.lbl_subscribe));
//				mPb.setCancelable(true);
//				Intent intent = new Intent(this, SubscribeIntentService.class);
//				intent.putExtra(SubscribeIntentService.TOPIC, "global-" + lang );
//				startService(intent);
//			} else {
//				mPb = ProgressDialog.show(this, null, getString(R.string.lbl_unscribe));
//				mPb.setCancelable(true);
//				Intent intent = new Intent(this, UnsubscribeIntentService.class);
//				intent.putExtra(UnsubscribeIntentService.TOPIC, "global-" + lang );
//				startService(intent);
//			}

//		}
		return true;
	}


	@Override
	protected void onResume() {
		EventBus.getDefault().registerSticky(this);
		super.onResume();

		String mightError = null;
		try {
			Prefs.getInstance().downloadApplicationConfiguration();
		} catch (InvalidAppPropertiesException _e) {
			mightError = _e.getMessage();
		} catch (CanNotOpenOrFindAppPropertiesException _e) {
			mightError = _e.getMessage();
		}
		if (mightError != null) {
			new AlertDialog.Builder(this).setTitle(com.chopping.R.string.app_name).setMessage(mightError).setCancelable(
					false).setPositiveButton("Ok", new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					finish();
				}
			}).create().show();
		}


		LocalBroadcastManager.getInstance(this).registerReceiver(mRegistrationBroadcastReceiver,
				new IntentFilter(RegistrationIntentService.REGISTRATION_COMPLETE));
		LocalBroadcastManager.getInstance(this).registerReceiver(mUnregistrationBroadcastReceiver,
				new IntentFilter(UnregistrationIntentService.UNREGISTRATION_COMPLETE));
		LocalBroadcastManager.getInstance(this).registerReceiver(mSubscribeBroadcastReceiver,
				new IntentFilter(SubscribeIntentService.SUBSCRIBE_COMPLETE));
		LocalBroadcastManager.getInstance(this).registerReceiver(mUnsubscribeBroadcastReceiver,
				new IntentFilter(UnsubscribeIntentService.UNSUBSCRIBE_COMPLETE));
	}

	@Override
	protected void onPause() {
		EventBus.getDefault().unregister(this);
		LocalBroadcastManager.getInstance(this).unregisterReceiver(mRegistrationBroadcastReceiver);
		LocalBroadcastManager.getInstance(this).unregisterReceiver(mUnregistrationBroadcastReceiver);
		LocalBroadcastManager.getInstance(this).unregisterReceiver(mSubscribeBroadcastReceiver);
		LocalBroadcastManager.getInstance(this).unregisterReceiver(mUnsubscribeBroadcastReceiver);
		super.onPause();
	}

	/**
	 * Remove the progress indicator.
	 */
	private void dismissPb() {
		if (mPb != null && mPb.isShowing()) {
			mPb.dismiss();
		}
	}

	@Override
	public void onBackPressed() {
		backPressed();
	}

	private void onAppConfigLoaded() {
		dismissPb();
	}

	private void onAppConfigIgnored() {
		dismissPb();
	}

	private void backPressed() {
		dismissPb();
		ActivityCompat.finishAfterTransition(this);
	}
}
