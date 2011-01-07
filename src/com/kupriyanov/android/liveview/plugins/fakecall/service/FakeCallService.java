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

package com.kupriyanov.android.liveview.plugins.fakecall.service;

import java.util.ArrayList;

import com.kupriyanov.android.liveview.plugins.fakecall.Preferences;
import com.kupriyanov.android.liveview.plugins.fakecall.R;
import com.kupriyanov.android.liveview.plugins.fakecall.Setup;
import com.kupriyanov.android.liveview.plugins.fakecall.ui.FakeCallActivity;
//import com.kupriyanov.android.media.SoundManager;
import com.sonyericsson.extras.liveview.plugins.AbstractPluginService;
import com.sonyericsson.extras.liveview.plugins.PluginConstants;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.os.SystemClock;
import android.os.Vibrator;
import android.util.Log;

/**
 * Hello World service.
 * 
 * Will send a notification to LiveView every time a button is pressed on the
 * device.
 */
public class FakeCallService extends AbstractPluginService {
	// private static final int PLAY_RING_ONCE = 1;
	// private static final int STOP_RING = 3;

	private static final int VIBRATE_LENGTH = 1000; // ms
	private static final int PAUSE_LENGTH = 1000; // ms

	public static final int MSG_REGISTER_CLIENT = 1;
	public static final int MSG_UNREGISTER_CLIENT = 2;
	public static final int MSG_STOPPING_RINGING = 3;
	public static final int MSG_STOPPED_RINGING = 4;
	

	// Our handler.
	private Handler mHandler = null;

	// // Counter
	// private int mCounter = 1;

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

	private Context mContext = null;

	private Ringtone mRingtone;
	private VibratorThread mVibratorThread;
	private boolean mContinueVibrating;
	private boolean mContinueRinging;

	Vibrator mVibrator = null;
	private int mLastVolume;
	private AudioManager mAudioManager;
	private RingToneThread mRingToneThread;
	private int mVolume;
	private int mLastRingerMode;

	final Messenger mMessenger = new Messenger(new IncomingHandler()); // Target
																								// we
																								// publish
																								// for
																								// clients
																								// to
																								// send
																								// messages
																								// to
																								// IncomingHandler.

	ArrayList<Messenger> mClients = new ArrayList<Messenger>(); // Keeps track of
																					// all current
																					// registered
																					// clients.

	private static boolean isRunning = false;

	@Override
	public IBinder onBind(Intent intent) {
		return mMessenger.getBinder();
	}

	class IncomingHandler extends Handler { // Handler of incoming messages from
		// clients.
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case MSG_REGISTER_CLIENT:
				mClients.add(msg.replyTo);
				break;
			case MSG_UNREGISTER_CLIENT:
				mClients.remove(msg.replyTo);
				break;
			case MSG_STOPPING_RINGING:
				if (isRinging() || isVibrating()) {
					stopRing();
					sendMessageToUI(MSG_STOPPED_RINGING);
				}
				break;
			default:
				super.handleMessage(msg);
			}
		}
	}

	
	private void sendMessageToUI(int messageToSend) {
      for (int i=mClients.size()-1; i>=0; i--) {
          try {
              // Send data as an Integer
              mClients.get(i).send(Message.obtain(null, messageToSend, messageToSend, 0));

              //Send data as a String
              Bundle b = new Bundle();
              b.putString("str1", "ab" + messageToSend + "cd");
              Message msg = Message.obtain(null, messageToSend);
              msg.setData(b);
              mClients.get(i).send(msg);

          } catch (RemoteException e) {
              // The client is dead. Remove it from the list; we are going through the list from back to front so this is safe to do inside the loop.
              mClients.remove(i);
          }
      }
  }

	


	@Override
	public void onCreate() {
		super.onCreate();
		Log.d(Setup.LOG_TAG, "Enter FakeCallService.onCreate.");
		// ...
		// Do plugin specifics.
		// ...
		isRunning = true;

		if (mContext == null) {
			mContext = getApplicationContext();
		}

		// Create, Initialise and then load the Sound manager
		// SoundManager.getInstance();
		// SoundManager.initSounds(this);
		// SoundManager.loadSounds();

	}
	
	
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
	public void onDestroy() {
		super.onDestroy();
		Log.d(Setup.LOG_TAG, "Enter FakeCallService.onDestroy.");
		isRunning = false;
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

		Log.d(PluginConstants.LOG_TAG, "openInPhone: sound_nr:" + mSharedPreferences.getString("sound_nr", "2"));

		mAudioManager = (AudioManager) mContext.getSystemService(Context.AUDIO_SERVICE);

		if (Setup.LOG_ON)
			Log.d(Setup.LOG_TAG,
					"override_nosound:" + mSharedPreferences.getString(Preferences.PREFERENCE_OVERRIDE_SOUND, "50"));

		/*
		 * ger Ringtone Uri
		 */
		if (shouldRing()) {
			if (mCustomRingtoneUri == null) {
				String uri = mSharedPreferences.getString(Preferences.PREFERENCE_RING_URI, null);

				if (uri != null && uri.length() != 0) {
					mCustomRingtoneUri = Uri.parse(uri);
				}
			}

			if (Setup.LOG_ON)
				Log.d(Setup.LOG_TAG, "got ringtone:" + mCustomRingtoneUri.toString());

			/*
			 * get ringtone
			 */
			if (mRingtone == null) {

				mRingtone = RingtoneManager.getRingtone(getApplicationContext(), mCustomRingtoneUri);

			} else {

				if (isRinging()) {
					stopRing();
					return;
				}

				mRingtone = RingtoneManager.getRingtone(getApplicationContext(), mCustomRingtoneUri);
			}

			/*
			 * set max volume
			 */

			mVolume = Integer.parseInt(mSharedPreferences.getString(Preferences.PREFERENCE_OVERRIDE_SOUND, "50"));

			if (mVolume > 0) {

				if (!isRinging()) {

					/*
					 * show Ring Activity
					 */

					Intent fakeCallActivity = new Intent(getApplicationContext(), FakeCallActivity.class);
					fakeCallActivity.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
					startActivity(fakeCallActivity);

					final int maxVolume = mAudioManager.getStreamMaxVolume(AudioManager.STREAM_RING);
					final Double volumeToset = (double) maxVolume / 100 * (double) mVolume;

					mLastVolume = mAudioManager.getStreamVolume(AudioManager.STREAM_RING);
					mLastRingerMode = mAudioManager.getRingerMode();

					mAudioManager.setRingerMode(AudioManager.RINGER_MODE_NORMAL);

					mAudioManager.setStreamVolume(AudioManager.STREAM_RING, volumeToset.intValue(),
							AudioManager.FLAG_PLAY_SOUND);
					if (Setup.LOG_ON)
						log("override_nosound_VOLUME[" + mLastVolume + "]:" + volumeToset.intValue());

				}

			} else {
				if (mAudioManager.getStreamVolume(AudioManager.STREAM_RING) == 0) {
					if (Setup.LOG_ON)
						Log.d(Setup.LOG_TAG, "skipping ring because volume is zero");
				}

			}
		}

		if (isRinging() || isVibrating()) {
			stopRing();
		} else {

			if (shouldRing() && mRingToneThread == null) {
				mContinueRinging = true;

				mRingToneThread = new RingToneThread();
				if (Setup.LOG_ON)
					Log.d(Setup.LOG_TAG, "- starting RingToneThread...");
				mRingToneThread.start();
			}

			if (shouldVibrate() && mVibratorThread == null) {
				if (mVibrator == null) {
					mVibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
				}

				mContinueVibrating = true;
				mVibratorThread = new VibratorThread();
				if (Setup.LOG_ON)
					Log.d(Setup.LOG_TAG, "- starting vibrator...");
				mVibratorThread.start();
			}
		}

	}

	private boolean shouldRing() {
		final String uri = mSharedPreferences.getString(Preferences.PREFERENCE_RING_URI, null);
		final int mVolume = Integer.parseInt(mSharedPreferences.getString(Preferences.PREFERENCE_OVERRIDE_SOUND, "50"));

		if (mVolume == 0) {
			return false;
		}

		if (uri == null) {
			return false;
		} else {
			if (uri.length() > 0) {
				return true;
			}
		}

		return false;
	}

	private boolean isRinging() {
		synchronized (this) {
			return (mRingToneThread != null);
		}
	}

	/**
	 * @return true if we're vibrating in response to an incoming call
	 * @see isVibrating
	 * @see isRinging
	 */
	private boolean isVibrating() {
		synchronized (this) {
			return (mVibratorThread != null);
		}
	}

	private boolean shouldVibrate() {
		return mSharedPreferences.getBoolean("vibrate_on", false);
	}

	private class VibratorThread extends Thread {
		public void run() {
			while (mContinueVibrating) {
				mVibrator.vibrate(VIBRATE_LENGTH);
				SystemClock.sleep(VIBRATE_LENGTH + PAUSE_LENGTH);
			}
		}
	}

	private class RingToneThread extends Thread {
		public void run() {
			while (mContinueRinging) {
				if (mRingtone != null) {
					if (!mRingtone.isPlaying()) {
						mRingtone.play();
					}
				}
				SystemClock.sleep(VIBRATE_LENGTH + PAUSE_LENGTH);
			}
		}
	}

	void stopRing() {
		synchronized (this) {
			if (Setup.LOG_ON)
				log("stopRing()...");

			// try {
			// mPowerManager.setAttentionLight(false, 0x00000000);
			// } catch (RemoteException ex) {
			// // the other end of this binder call is in the system process.
			// }

			mAudioManager.setStreamVolume(AudioManager.STREAM_RING, mLastVolume, AudioManager.FLAG_PLAY_SOUND);
			mAudioManager.setRingerMode(mLastRingerMode);

			if (mRingToneThread != null) {

				if (mRingtone != null) {
					if (mRingtone.isPlaying()) {
						mRingtone.stop();
					}
					mRingtone = null;
					mCustomRingtoneUri = null;
				}

				if (Setup.LOG_ON)
					log("- stopRing: cleaning up ringtone thread...");
				mContinueRinging = false;
				mRingToneThread = null;
			}

			if (mVibratorThread != null) {
				if (Setup.LOG_ON)
					log("- stopRing: cleaning up vibrator thread...");
				mContinueVibrating = false;
				mVibratorThread = null;
			}
			// Also immediately cancel any vibration in progress.
			if (mVibrator != null) {
				mVibrator.cancel();
			}
		}
	}

	public static boolean isRunning() {
		return isRunning;
	}

	private void log(String string) {
		Log.d(Setup.LOG_TAG, string);
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
				sendAnnounce(getString(R.string.message_title), getString(R.string.message_text));

			} catch (Exception re) {
				Log.e(PluginConstants.LOG_TAG, "Failed to send image to LiveView.", re);
			}

			// scheduleTimer();
		}

	};

}