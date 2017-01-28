package maderski.bluetoothautoplaymusic.Services;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import maderski.bluetoothautoplaymusic.BuildConfig;
import maderski.bluetoothautoplaymusic.Receivers.BTStateChangedReceiver;
import maderski.bluetoothautoplaymusic.Receivers.BluetoothReceiver;
import maderski.bluetoothautoplaymusic.Receivers.CustomReceiver;
import maderski.bluetoothautoplaymusic.Receivers.PowerReceiver;
import maderski.bluetoothautoplaymusic.ScreenONLock;
import maderski.bluetoothautoplaymusic.SharedPrefs.BAPMDataPreferences;
import maderski.bluetoothautoplaymusic.SharedPrefs.BAPMPreferences;

/**
 * Created by Jason on 1/5/16.
 */
public class BAPMService extends Service {
    //Start the Bluetooth receiver as a service
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if(BuildConfig.DEBUG) {
            Log.i("BAPMService: ", "started");
            Toast.makeText(this, "BAPMService started", Toast.LENGTH_LONG).show();
        }

        // Register receivers
        new BluetoothReceiver().onReceive(this, intent);
        new BTStateChangedReceiver().onReceive(this, intent);
        new CustomReceiver().onReceive(this, intent);
        new PowerReceiver().onReceive(this, intent);

        // Rehold WakeLock due to Service Restart
        reHoldWakeLock();

        return Service.START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) {
        //TODO for communication return IBinder implementation
        return null;
    }

    private void reHoldWakeLock(){
        boolean shouldKeepScreenOn = BAPMPreferences.getKeepScreenON(this);

        if(shouldKeepScreenOn) {
            AudioManager audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
            boolean ranBAPM = BAPMDataPreferences.getRanActionsOnBtConnect(this);
            boolean isConnectedToBT = audioManager.isBluetoothA2dpOn();

            if (ranBAPM && isConnectedToBT) {
                ScreenONLock screenONLock = ScreenONLock.getInstance();
                screenONLock.releaseWakeLock();
                screenONLock.enableWakeLock(this);
            }
        }
    }
}