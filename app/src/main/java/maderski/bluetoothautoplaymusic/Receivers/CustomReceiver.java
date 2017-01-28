package maderski.bluetoothautoplaymusic.Receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.util.Log;

import maderski.bluetoothautoplaymusic.SharedPrefs.BAPMDataPreferences;
import maderski.bluetoothautoplaymusic.BluetoothActions;
import maderski.bluetoothautoplaymusic.BuildConfig;
import maderski.bluetoothautoplaymusic.Notification;
import maderski.bluetoothautoplaymusic.ScreenONLock;
import maderski.bluetoothautoplaymusic.VolumeControl;

/**
 * Created by Jason on 7/28/16.
 */
public class CustomReceiver extends BroadcastReceiver {
    public static final String TAG = CustomReceiver.class.getName();
    private static final String ACTION_POWER_LAUNCH = "maderski.bluetoothautoplaymusic.pluggedinlaunch";
    private static final String ACTION_OFF_TELE_LAUNCH = "maderski.bluetoothautoplaymusic.offtelephonelaunch";
    private static final String ACTION_IS_SELECTED = "maderski.bluetoothautoplaymusic.isselected";

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = "None";
        if (intent != null) {
            if (intent.getAction() != null) {
                action = intent.getAction();
                if(!action.equalsIgnoreCase("ACTION_IS_SELECTED")) {
                    AudioManager audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
                    BluetoothActions bluetoothActions = new BluetoothActions(context, audioManager);
                    performAction(action, bluetoothActions);
                }
            }

            updateIsSelected(context, intent, action);
        }
    }

    private void performAction(String action, BluetoothActions bluetoothActions){
        switch (action) {
            case ACTION_POWER_LAUNCH:
                if(BuildConfig.DEBUG)
                    Log.i(TAG, "POWER_LAUNCH");

                bluetoothActions.OnBTConnect();
                break;
            case ACTION_OFF_TELE_LAUNCH:
                if(BuildConfig.DEBUG)
                    Log.i(TAG, "OFF_TELE_LAUNCH");
                //Calling actionsOnBTConnect cause onBTConnect already ran
                bluetoothActions.actionsOnBTConnect();
                break;
        }
    }

    private void updateIsSelected(Context context, Intent intent, String action){
        if(action.equalsIgnoreCase(ACTION_IS_SELECTED)) {
            boolean isSelected = intent.getBooleanExtra("isSelected", false);
            BAPMDataPreferences.setIsSelected(context, isSelected);
            if (BuildConfig.DEBUG)
                Log.i(TAG, "IS_SELECTED: " + Boolean.toString(isSelected));
        }
    }
}