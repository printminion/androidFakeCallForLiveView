package com.kupriyanov.android.liveview.plugins.fakecall.ui;

import com.kupriyanov.android.liveview.plugins.fakecall.R;
import com.kupriyanov.android.liveview.plugins.fakecall.service.FakeCallService;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;

public class FakeCallActivity extends Activity {

	public static final String TAG = "FakeCallActivity";
	// Button btnStart, btnStop, btnBind, btnUnbind, btnUpby1, btnUpby10;
	TextView textStatus;// , textIntValue, textStrValue;
	Messenger mService = null;
	boolean mIsBound;
	final Messenger mMessenger = new Messenger(new IncomingHandler());

	@Override
	protected void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
		requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
		setContentView(R.layout.activity_main);

		textStatus = (TextView) findViewById(R.id.tvStatus);

		CheckIfServiceIsRunning();

	}

	private void CheckIfServiceIsRunning() {
		// If the service is running when the activity starts, we want to
		// automatically bind to it.
		if (FakeCallService.isRunning()) {
			doBindService();
		}
	}

	void doBindService() {
		bindService(new Intent(this, FakeCallService.class), mConnection, Context.BIND_AUTO_CREATE);
		mIsBound = true;
		textStatus.setText("Binding.");
	}

	public void stopRinging(View v) {
		if (mIsBound) {
			// If we have received the service, and hence registered with it, then
			// now is the time to unregister.
			if (mService != null) {
				try {
					Message msg = Message.obtain(null, FakeCallService.MSG_STOPPING_RINGING);
					msg.replyTo = mMessenger;
					mService.send(msg);
				} catch (RemoteException e) {
					// There is nothing special we need to do if the service has
					// crashed.
				}
			}
			textStatus.setText("stop ringing");
		}
	}

	private ServiceConnection mConnection = new ServiceConnection() {
		public void onServiceConnected(ComponentName className, IBinder service) {
			mService = new Messenger(service);
			textStatus.setText("Attached.");
			try {
				Message msg = Message.obtain(null, FakeCallService.MSG_REGISTER_CLIENT);
				msg.replyTo = mMessenger;
				mService.send(msg);
			} catch (RemoteException e) {
				// In this case the service has crashed before we could even do
				// anything with it
			}
		}

		public void onServiceDisconnected(ComponentName className) {
			// This is called when the connection with the service has been
			// unexpectedly disconnected - process crashed.
			mService = null;
			textStatus.setText("Disconnected.");
		}
	};

	class IncomingHandler extends Handler {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case FakeCallService.MSG_STOPPED_RINGING:
				textStatus.setText("Stopped ringing");
				finish();
				break;
			// case FakeCallService.MSG_SET_STRING_VALUE:
			// String str1 = msg.getData().getString("str1");
			// textStrValue.setText("Str Message: " + str1);
			// break;
			default:
				Log.d(TAG, "handleMessage" + msg);
				super.handleMessage(msg);
			}
		}
	}

	void doUnbindService() {
		if (mIsBound) {
			// If we have received the service, and hence registered with it, then
			// now is the time to unregister.
			if (mService != null) {
				try {
					Message msg = Message.obtain(null, FakeCallService.MSG_UNREGISTER_CLIENT);
					msg.replyTo = mMessenger;
					mService.send(msg);
				} catch (RemoteException e) {
					// There is nothing special we need to do if the service has
					// crashed.
				}
			}
			// Detach our existing connection.
			unbindService(mConnection);
			mIsBound = false;
			textStatus.setText("Unbinding.");
		}
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		try {
			doUnbindService();
		} catch (Throwable t) {
			Log.e("MainActivity", "Failed to unbind from the service", t);
		}
	}
}
