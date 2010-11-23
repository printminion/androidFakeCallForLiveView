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

package com.sonyericsson.extras.liveview.plugins.template;

import com.sonyericsson.extras.liveview.plugins.AbstractPluginService;
import com.sonyericsson.extras.liveview.plugins.PluginConstants;

import android.content.ComponentName;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.util.Log;

public class TemplateService extends AbstractPluginService {
	
	@Override
	public void onStart(Intent intent, int startId) {
		super.onStart(intent, startId);

		// ... 
		// Do plugin specifics.
		// ...
	}
	
	@Override
	public void onCreate() {
		super.onCreate();
		
		// ... 
		// Do plugin specifics.
		// ...
	}
	
	@Override
	public void onDestroy() {
		super.onDestroy();
		
		// ... 
		// Do plugin specifics.
		// ...
	}
	
	/**
	 * Plugin is just sending notifications.
	 */
	protected boolean isSandboxPlugin() {
	    return false;
	}
	
	/**
	 * Must be implemented. Starts plugin work, if any.
	 */
	protected void startWork() {
		
		// Check if plugin is enabled.
		if(mSharedPreferences.getBoolean(PluginConstants.PREFERENCES_PLUGIN_ENABLED, false)) {
			// Do stuff.
		}
		
	}
	
	/**
	 * Must be implemented. Stops plugin work, if any.
	 */
	protected void stopWork() {
		
	}
	
	/**
	 * Must be implemented.
	 * 
	 * PluginService has done connection and registering to the LiveView Service. 
	 * 
	 * If needed, do additional actions here, e.g. 
	 * starting any worker that is needed.
	 */
	protected void onServiceConnectedExtended(ComponentName className, IBinder service) {
		
	}
	
	/**
	 * Must be implemented.
	 * 
	 * PluginService has done disconnection from LiveView and service has been stopped. 
	 * 
	 * Do any additional actions here.
	 */
	protected void onServiceDisconnectedExtended(ComponentName className) {
		
	}

	/**
	 * Must be implemented.
	 * 
	 * PluginService has checked if plugin has been enabled/disabled.
	 * 
	 * The shared preferences has been changed. Take actions needed. 
	 */	
	protected void onSharedPreferenceChangedExtended(SharedPreferences prefs, String key) {
		
	}

	/**
	 * This method is called by the LiveView application to start the plugin.
	 * For sandbox plugins, this means when the user has pressed the action button to start the plugin.
	 */
	protected void startPlugin() {
		Log.d(PluginConstants.LOG_TAG, "startPlugin");
		
		// Check if plugin is enabled.
		if(mSharedPreferences.getBoolean(PluginConstants.PREFERENCES_PLUGIN_ENABLED, false)) {
			// Do.
		}
		
	}

	/**
     * This method is called by the LiveView application to stop the plugin.
     * For sandbox plugins, this means when the user has long-pressed the action button to stop the plugin.
     */
	protected void stopPlugin() {
		Log.d(PluginConstants.LOG_TAG, "stopPlugin");
		stopWork();
	}

	/**
     * Sandbox mode only. When a user presses any buttons on the LiveView device, this method will be called.
     */
	protected void button(String buttonType, boolean doublepress, boolean longpress) {
		Log.d(PluginConstants.LOG_TAG, "button - type " + buttonType + ", doublepress " + doublepress + ", longpress " + longpress);
	}

	/**
     * Called by the LiveView application to indicate the capabilites of the LiveView device.
     */
	protected void displayCaps(int displayWidthPx, int displayHeigthPx) {
	    Log.d(PluginConstants.LOG_TAG, "displayCaps - width " + displayWidthPx + ", height " + displayHeigthPx);
	}

	/**
     * Called by the LiveView application when the plugin has been kicked out by the framework.
     */
	protected void onUnregistered() {
		Log.d(PluginConstants.LOG_TAG, "onUnregistered");
		stopWork();
	}

	/**
     * When a user presses the "open in phone" button on the LiveView device, this method is called.
     * You could e.g. open a browser and go to a specific URL, or open the music player.
     */
	protected void openInPhone(String openInPhoneAction) {
		Log.d(PluginConstants.LOG_TAG, "openInPhone: " + openInPhoneAction);
	}
	
	/**
     * Sandbox mode only. Called by the LiveView application when the screen mode has changed.
     * 0 = screen is off, 1 = screen is on
     */
    protected void screenMode(int mode) {
        Log.d(PluginConstants.LOG_TAG, "screenMode: screen is now " + ((mode == 0) ? "OFF" : "ON"));
    }
			
}