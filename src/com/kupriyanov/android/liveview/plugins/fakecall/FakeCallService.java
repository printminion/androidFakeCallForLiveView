/*
 * Copyright (c) 2010 Sony Ericsson
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * 
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package com.kupriyanov.android.liveview.plugins.fakecall;

import com.kupriyanov.android.media.SoundManager;
import com.sonyericsson.extras.liveview.plugins.AbstractPluginService;
import com.sonyericsson.extras.liveview.plugins.PluginConstants;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Handler;
import android.os.IBinder;
import android.os.Vibrator;
import android.util.Log;

/**
 * Hello World service.
 * 
 * Will send a notification to LiveView every time a button is pressed on the
 * device.
 */
public class FakeCallService extends AbstractPluginService {

	// Our handler.
	private Handler mHandler = null;

	// Counter
	private int mCounter = 1;

	// Is loop running?
	private boolean mWorkerRunning = false;

	// Preferences - update interval
	private static final String UPDATE_INTERVAL = "updateInterval";

	// private long mUpdateInterval = 60000;
	private long mUpdateInterval = 10000; // set 10 sec for update

	// private Worker mRingThread;
	// private Handler mRingHandler;

	// Uri for the ringtone.
	Uri mCustomRingtoneUri;

	@Override
	public void onStart(Intent intent, int startId) {
		super.onStart(intent, startId);
		Log.d(Setup.LOG_TAG, "Enter FakeCallService.onStart.");
		// Create handler.
		if (mHandler == null) {
			mHandler = new Handler();
		}
	}

	@Override
	public void onCreate() {
		super.onCreate();
		Log.d(Setup.LOG_TAG, "Enter FakeCallService.onCreate.");
		// ...
		// Do plugin specifics.
		// ...

		// Create, Initialise and then load the Sound manager
		SoundManager.getInstance();
		SoundManager.initSounds(this);
		SoundManager.loadSounds();

	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		Log.d(Setup.LOG_TAG, "Enter FakeCallService.onDestroy.");
		// ...
		// Do plugin specifics.
		// ...
	}

	/**
	 * Plugin is just sending notifications.
	 */
	protected boolean isSandboxPlugin() {
		Log.d(Setup.LOG_TAG, "Enter FakeCallService.isSandboxPlugin.");
		return false;
	}

	/**
	 * Must be implemented. Starts plugin work, if any.
	 */
	protected void startWork() {
		Log.d(Setup.LOG_TAG, "Enter FakeCallService.startWork.");
		// Check if plugin is enabled.

		// if (!mWorkerRunning &&
		// mSharedPreferences.getBoolean(PluginConstants.PREFERENCES_PLUGIN_ENABLED,
		// false)) {

		if (!mWorkerRunning) {
			mWorkerRunning = true;

			scheduleTimer();
		}
	}

	/**
	 * Must be implemented. Stops plugin work, if any.
	 */
	protected void stopWork() {
		Log.d(Setup.LOG_TAG, "Enter FakeCallService.stopWork.");
		mHandler.removeCallbacks(mAnnouncer);
		mWorkerRunning = false;
	}

	/**
	 * Must be implemented.
	 * 
	 * PluginService has done connection and registering to the LiveView Service.
	 * 
	 * If needed, do additional actions here, e.g. starting any worker that is
	 * needed.
	 */
	protected void onServiceConnectedExtended(ComponentName className, IBinder service) {
		Log.d(Setup.LOG_TAG, "Enter FakeCallService.onServiceConnectedExtended.");
	}

	/**
	 * Must be implemented.
	 * 
	 * PluginService has done disconnection from LiveView and service has been
	 * stopped.
	 * 
	 * Do any additional actions here.
	 */
	protected void onServiceDisconnectedExtended(ComponentName className) {
		Log.d(Setup.LOG_TAG, "Enter FakeCallService.onServiceDisconnectedExtended.");
	}

	/**
	 * Must be implemented.
	 * 
	 * PluginService has checked if plugin has been enabled/disabled.
	 * 
	 * The shared preferences has been changed. Take actions needed.
	 */
	protected void onSharedPreferenceChangedExtended(SharedPreferences pref, String key) {
		Log.d(Setup.LOG_TAG, "Enter FakeCallService.onSharedPreferenceChangedExtended.");
		if (key.equals(UPDATE_INTERVAL)) {
			long value = Long.parseLong(pref.getString("updateInterval", "60"));
			mUpdateInterval = value * 1000;

			Log.d(PluginConstants.LOG_TAG, "Preferences changed - update interval: " + mUpdateInterval);
		}
	}

	protected void startPlugin() {
		Log.d(PluginConstants.LOG_TAG, "startPlugin");
		// Check if plugin is enabled.
		if (mSharedPreferences.getBoolean(PluginConstants.PREFERENCES_PLUGIN_ENABLED, false)) {
			// Do.
		}

		startWork();
	}

	/**
	 * This method is called by the LiveView application to stop the plugin. For
	 * sandbox plugins, this means when the user has long-pressed the action
	 * button to stop the plugin.
	 */
	protected void stopPlugin() {
		Log.d(PluginConstants.LOG_TAG, "stopPlugin");
		stopWork();
	}

	protected void button(String buttonType, boolean doublepress, boolean longpress) {
		Log.d(PluginConstants.LOG_TAG, "button - type " + buttonType + ", doublepress " + doublepress + ", longpress "
				+ longpress);
	}

	/**
	 * Called by the LiveView application to indicate the capabilites of the
	 * LiveView device.
	 */
	protected void displayCaps(int displayWidthPx, int displayHeigthPx) {
		Log.d(PluginConstants.LOG_TAG, "displayCaps - width " + displayWidthPx + ", height " + displayHeigthPx);
	}

	protected void onUnregistered() {
		Log.d(PluginConstants.LOG_TAG, "onUnregistered");
		stopWork();
	}

	protected void openInPhone(String openInPhoneAction) {
		Log.d(PluginConstants.LOG_TAG, "openInPhone: " + openInPhoneAction);

		// // Open in browser.
		// final Uri uri = Uri.parse(openInPhoneAction);
		// final Intent browserIntent = new Intent();
		// browserIntent.setData(uri);
		// browserIntent.setClassName("com.android.browser",
		// "com.android.browser.BrowserActivity");
		// browserIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		// startActivity(browserIntent);

		// mAudioManager = (AudioManager)
		// getApplicationContext().getSystemService(Context.AUDIO_SERVICE);
		// // mAudioManager.setRingerMode(AudioManager.);
		// mAudioManager.setVibrateSetting(AudioManager.VIBRATE_TYPE_RINGER,
		// AudioManager.VIBRATE_SETTING_OFF);
		//
		// // mAudioManager.playSoundEffect(AudioManager.E)

		Log.d(PluginConstants.LOG_TAG, "openInPhone: sound_nr:" + mSharedPreferences.getString("sound_nr", "2"));

		if (!mSharedPreferences.getString("sound_nr", "2").equals("0")) {
			SoundManager.playSound(new Integer(mSharedPreferences.getString("sound_nr", "2")), new Integer(
					mSharedPreferences.getString("sound_repeat_times", "3")), 1);
		}

		// vibrate_on
		if (mSharedPreferences.getBoolean("vibrate_on", false)) {

			Vibrator v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);

			// 1. Vibrate for 1000 milliseconds
			long milliseconds = 1000;
			v.vibrate(milliseconds);
		}

		// // 2. Vibrate in a Pattern with 500ms on, 500ms off for 5 times
		// long[] pattern = { 500, 300 };
		// v.vibrate(pattern, 5);

		// r = RingtoneManager.getRingtone(getApplicationContext(),
		// mCustomRingtoneUri);
		// // PhoneUtils.setAudioMode(mContext, AudioManager.MODE_RINGTONE);
		// r.play();

	}

	protected void screenMode(int mode) {
		Log.d(PluginConstants.LOG_TAG, "screenMode: screen is now " + ((mode == 0) ? "OFF" : "ON"));
	}

	private void sendAnnounce(String header, String body) {
		try {

			// if (mWorkerRunning && (mLiveViewAdapter != null)
			// &&
			// mSharedPreferences.getBoolean(PluginConstants.PREFERENCES_PLUGIN_ENABLED,
			// false)) {

			if (mWorkerRunning && (mLiveViewAdapter != null)) {

				mLiveViewAdapter.sendAnnounce(mPluginId, mMenuIcon, header, body, System.currentTimeMillis(),
						"http://en.wikipedia.org/wiki/Hello_world_program");

				Log.d(PluginConstants.LOG_TAG, "Announce sent to LiveView");
			} else {
				Log.d(PluginConstants.LOG_TAG, "LiveView not reachable");
			}
		} catch (Exception e) {
			Log.e(PluginConstants.LOG_TAG, "Failed to send announce", e);
		}
	}

	/**
	 * Schedules a timer.
	 */
	private void scheduleTimer() {
		if (mWorkerRunning) {
			mHandler.postDelayed(mAnnouncer, mUpdateInterval);
		}
	}

	/**
	 * The runnable used for posting to handler
	 */
	private Runnable mAnnouncer = new Runnable() {

		@Override
		public void run() {
			try {

				// sendAnnounce("Fake call",
				// "Scroll down and push upper left button " + mCounter++);
				sendAnnounce("Fake call", "Scroll down and push upper left button ");

			} catch (Exception re) {
				Log.e(PluginConstants.LOG_TAG, "Failed to send image to LiveView.", re);
			}

			// scheduleTimer();
		}

	};

}