package com.kupriyanov.android.media;

import java.util.HashMap;

import com.kupriyanov.android.liveview.plugins.fakecall.R;

import android.content.Context;
import android.media.AudioManager;
import android.media.SoundPool;

public class SoundManager {

	static private SoundManager _instance;
	private static SoundPool mSoundPool;
	private static HashMap<Integer, Integer> mSoundPoolMap;
	private static AudioManager mAudioManager;
	private static Context mContext;

	private SoundManager() {
	}

	/**
	 * Requests the instance of the Sound Manager and creates it if it does not
	 * exist.
	 * 
	 * @return Returns the single instance of the SoundManager
	 */
	static synchronized public SoundManager getInstance() {
		if (_instance == null)
			_instance = new SoundManager();
		return _instance;
	}

	/**
	 * Initialises the storage for the sounds
	 * 
	 * @param theContext
	 *           The Application context
	 */
	public static void initSounds(Context theContext) {
		mContext = theContext;
		mSoundPool = new SoundPool(4, AudioManager.STREAM_MUSIC, 0);
		mSoundPoolMap = new HashMap<Integer, Integer>();
		mAudioManager = (AudioManager) mContext.getSystemService(Context.AUDIO_SERVICE);
	}

	/**
	 * Add a new Sound to the SoundPool
	 * 
	 * @param Index
	 *           - The Sound Index for Retrieval
	 * @param SoundID
	 *           - The Android ID for the Sound asset.
	 */
	public static void addSound(int Index, int SoundID) {
		mSoundPoolMap.put(Index, mSoundPool.load(mContext, SoundID, 1));
	}

	/**
	 * Loads the various sound assets Currently hardcoded but could easily be
	 * changed to be flexible.
	 */
	public static void loadSounds() {
		mSoundPoolMap.put(1, mSoundPool.load(mContext, R.raw.laugh_4, 1));

		// mSoundPoolMap.put(1, mSoundPool.load(mContext, R.raw.telephone_ring_1,
		// 1));
		// mSoundPoolMap.put(2, mSoundPool.load(mContext, R.raw.telephone_ring_3,
		// 1));
		// mSoundPoolMap.put(3, mSoundPool.load(mContext,
		// R.raw.cell_phone_vibrate_1, 1));
		// mSoundPoolMap.put(5, mSoundPool.load(mContext, R.raw.snoring_5, 1));

	}

	/**
	 * Plays a Sound
	 * 
	 * @param index
	 *           - The Index of the Sound to be played
	 * @param speed
	 *           - The Speed to play not, not currently used but included for
	 *           compatibility
	 */
	public static void playSound(int index, int repeat, float speed) {
		float streamVolume = mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC);

		// mAudioManager.setVibrateSetting(AudioManager.VIBRATE_TYPE_RINGER,
		// AudioManager.VIBRATE_SETTING_ON);

		streamVolume = streamVolume / mAudioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);

		// stopSound(index);

		mSoundPool.play(mSoundPoolMap.get(index), streamVolume, streamVolume, 1, repeat, speed);
	}

	/**
	 * Stop a Sound
	 * 
	 * @param index
	 *           - index of the sound to be stopped
	 */
	public static void stopSound(int index) {
		mSoundPool.stop(mSoundPoolMap.get(index));
	}

	public static void cleanup() {
		mSoundPool.release();
		mSoundPool = null;
		mSoundPoolMap.clear();
		mAudioManager.unloadSoundEffects();
		_instance = null;

	}

}