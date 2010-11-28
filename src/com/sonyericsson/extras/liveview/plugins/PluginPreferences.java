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

package com.sonyericsson.extras.liveview.plugins;

import com.kupriyanov.android.liveview.plugins.fakecall.Preferences;
import com.kupriyanov.android.liveview.plugins.fakecall.R;
import com.kupriyanov.android.liveview.plugins.fakecall.Setup;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.util.Log;

/**
 * Implements PreferenceActivity and sets the project preferences to the shared
 * preferences of the current user session.
 */
public class PluginPreferences extends PreferenceActivity {

	final int RESULT_PICK_RING = 2;

	SharedPreferences mSharedPreferences = null;

	Context mContext = null;

	private Ringtone mRingtone;

	private Preference mPreference;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		addPreferencesFromResource(getResources().getIdentifier("preferences", "xml", getPackageName()));

		if (mContext == null) {
			mContext = getApplicationContext();
		}

		mPreference = (Preference) findPreference(Preferences.PREFERENCE_RING_URI);

		if (mSharedPreferences == null) {
			mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(mContext);
		}

		mPreference.setOnPreferenceClickListener(new OnPreferenceClickListener() {

			@Override
			public boolean onPreferenceClick(Preference preference) {

				String uri = mSharedPreferences.getString(Preferences.PREFERENCE_RING_URI, null);

				if (Setup.LOG_ON)
					Log.d(Setup.LOG_TAG, "got ringtone uri:" + uri);

				Intent intent = new Intent(RingtoneManager.ACTION_RINGTONE_PICKER);
				intent.putExtra(RingtoneManager.EXTRA_RINGTONE_TYPE, RingtoneManager.TYPE_RINGTONE);
				intent.putExtra(RingtoneManager.EXTRA_RINGTONE_TITLE, getString(R.string.prefs_ringtone_title));

				if (uri != null) {
					intent.putExtra(RingtoneManager.EXTRA_RINGTONE_EXISTING_URI, Uri.parse(uri));
				} else {
					
//					final Uri mCustomRingtoneUri = RingtoneManager.getActualDefaultRingtoneUri(mContext,
//							RingtoneManager.TYPE_RINGTONE);
//
//					intent.putExtra(RingtoneManager.EXTRA_RINGTONE_EXISTING_URI, (Uri) mCustomRingtoneUri);
					intent.putExtra(RingtoneManager.EXTRA_RINGTONE_EXISTING_URI, "");

				}

				startActivityForResult(intent, RESULT_PICK_RING);

				return false;
			}
		});

	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);

		switch (requestCode) {
		case RESULT_PICK_RING:
			if (resultCode == RESULT_OK) {
				Uri uri = data.getParcelableExtra(RingtoneManager.EXTRA_RINGTONE_PICKED_URI);

				if (uri != null) {
					if (Setup.LOG_ON)
						Log.d(Setup.LOG_TAG, "saving ringtone:" + uri.toString());
					Editor editor = mSharedPreferences.edit();
					editor.putString(Preferences.PREFERENCE_RING_URI, uri.toString());
					editor.commit();

					mRingtone = RingtoneManager.getRingtone(mContext, uri);
					mPreference.setSummary(mRingtone.getTitle(mContext));

				}else{
					Editor editor = mSharedPreferences.edit();
					editor.putString(Preferences.PREFERENCE_RING_URI, null);
					editor.commit();

					mPreference.setSummary("none");
				}
			}

			break;

		default:
			break;
		}

	}
}