package maderski.bluetoothautoplaymusic;

import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

import java.util.Set;

/**
 * Created by Jason on 1/5/16.
 */
public class BluetoothReceiver extends BroadcastReceiver {
    public final static String TAG = "BluetoothReceiver";

    private ScreenONLock screenONLock = new ScreenONLock();
    private RingerControl ringerControl = new RingerControl();

    public void onReceive(Context context, Intent intent)
    {
        Log.d(TAG, "Bluetooth Intent Received");
        //Toast.makeText(context, "Bluetooth Intent Received", Toast.LENGTH_SHORT).show();

        final BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);

        String action = intent.getAction();
        Set<String> BTDeviceList = BAPMPreferences.getBTDevices(context);

        if (BluetoothDevice.ACTION_ACL_CONNECTED.equalsIgnoreCase(action))
        {
            String btDevice = device.getName();

            Log.d(TAG, "Connected to " + btDevice);
            Toast.makeText(context, "Connected to: " + btDevice, Toast.LENGTH_SHORT).show();

            if(BTDeviceList.contains(btDevice)) {
                Log.i(btDevice, " found");
                VariableStore.btDevice = btDevice;
                BTConnectPhoneDoStuff(context);

            }
        }

        if (BluetoothDevice.ACTION_ACL_DISCONNECTED.equalsIgnoreCase(action))
        {
            String btDevice = device.getName();
            
            Log.d(TAG, "Disconnected from " + btDevice);
            Toast.makeText(context, "Disconnected from: " + btDevice, Toast.LENGTH_SHORT).show();

            if(BTDeviceList.contains(btDevice)) {
                Log.i(btDevice, " found");

                BTDisconnectPhoneDoStuff(context);
            }
        }

    }

    private void BTConnectPhoneDoStuff(Context context){
        boolean screenON = BAPMPreferences.getKeepScreenON(context);
        boolean priorityMode = BAPMPreferences.getPriorityMode(context);
        boolean volumeMAX = BAPMPreferences.getMaxVolume(context);
        boolean unlockScreen = BAPMPreferences.getUnlockScreen(context);
        boolean launchMusicPlayer = BAPMPreferences.getLaunchMusicPlayer(context);

        Notification.BAPMMessage(context);

        if(screenON){
            screenONLock.enableWakeLock(context);
        }

        if(priorityMode){
            ringerControl.soundsOFF(context);
        }

        if(volumeMAX){
            ringerControl.volumeMAX(context);
        }

        if(unlockScreen){
            launchMainActivity(context);
        }

        LaunchApp.launchSelectedMusicPlayer(context);

        if(!launchMusicPlayer){
            LaunchApp.delayLaunchMaps(context, 2);
        }

    }

    private void BTDisconnectPhoneDoStuff(Context context){
        boolean screenON = BAPMPreferences.getKeepScreenON(context);
        boolean priorityMode = BAPMPreferences.getPriorityMode(context);

        Notification.removeBAPMMessage(context);

        if(screenON){
            screenONLock.releaseWakeLock(context);
        }

        if(priorityMode){
            ringerControl.soundsON(context);
        }
    }

    private void launchMainActivity(Context context){
        Intent i = new Intent(context, MainActivity.class);
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(i);
    }

}
