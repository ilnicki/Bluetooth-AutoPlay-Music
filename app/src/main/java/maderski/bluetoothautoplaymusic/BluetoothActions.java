package maderski.bluetoothautoplaymusic;

import android.app.KeyguardManager;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.os.Build;
import android.util.Log;
import android.widget.Toast;

import maderski.bluetoothautoplaymusic.Helpers.PermissionHelper;
import maderski.bluetoothautoplaymusic.Receivers.NotifPolicyAccessChangedReceiver;
import maderski.bluetoothautoplaymusic.SharedPrefs.BAPMDataPreferences;
import maderski.bluetoothautoplaymusic.SharedPrefs.BAPMPreferences;

/**
 * Created by Jason on 2/22/16.
 */
public class BluetoothActions {

    private static final String TAG = BluetoothActions.class.getName();

    private ScreenONLock screenONLock;
    private Context context;
    private Notification notification;
    private VolumeControl volumeControl;
    private PlayMusic mPlayMusic;

    public BluetoothActions(Context context){
        this.context = context;
        this.screenONLock = ScreenONLock.getInstance();
        this.notification = new Notification();
        this.volumeControl = new VolumeControl(context);
        this.mPlayMusic = new PlayMusic(context);
    }

    public void OnBTConnect(){
        boolean waitTillOffPhone = BAPMPreferences.getWaitTillOffPhone(context);

        if(waitTillOffPhone){
            Telephone telephone = new Telephone(context);
            if(Power.isPluggedIn(context)){
                if(telephone.isOnCall()) {
                    Log.d(TAG, "ON a call");
                    //Run CheckIfOnPhone
                    telephone.CheckIfOnPhone(volumeControl);
                }else{
                    Log.d(TAG, "NOT on a call");
                    actionsOnBTConnect();
                }
            }else{
                if(telephone.isOnCall()) {
                    notification.launchBAPM(context);
                }else{
                    actionsOnBTConnect();
                }
            }
        }else{
            actionsOnBTConnect();
        }
    }

    //Creates notification and if set turns screen ON, puts the phone in priority mode,
    //sets the volume to MAX, dismisses the keyguard, Launches the Music Selected Music
    //Player and Launches Maps
    public void actionsOnBTConnect(){
        synchronized (this) {
            boolean screenON = BAPMPreferences.getKeepScreenON(context);
            boolean priorityMode = BAPMPreferences.getPriorityMode(context);
            boolean volumeMAX = BAPMPreferences.getMaxVolume(context);
            boolean unlockScreen = BAPMPreferences.getUnlockScreen(context);
            boolean launchMusicPlayer = BAPMPreferences.getLaunchMusicPlayer(context);
            boolean launchMaps = BAPMPreferences.getLaunchGoogleMaps(context);
            boolean playMusic = BAPMPreferences.getAutoPlayMusic(context);
            boolean isWifiOffDevice = BAPMDataPreferences.getIsTurnOffWifiDevice(context);

            int checkToPlaySeconds = 5;

            String mapChoice = BAPMPreferences.getMapsChoice(context);

            RingerControl ringerControl = new RingerControl(context);
            LaunchApp launchApp = new LaunchApp();

            notification.BAPMMessage(context, mapChoice);

            if (screenON) {
                //Try to releaseWakeLock() in case for some reason it was not released on disconnect
                if (screenONLock.wakeLockHeld()) {
                    screenONLock.releaseWakeLock();
                }
                screenONLock.enableWakeLock(context);
            }

            if (unlockScreen) {
                boolean isKeyguardLocked = ((KeyguardManager)context.getSystemService(Context.KEYGUARD_SERVICE)).isKeyguardLocked();
                Log.d(TAG, "Is keyguard locked: " + Boolean.toString(isKeyguardLocked));
                if(isKeyguardLocked) {
                    launchApp.launchBAPMActivity(context);
                }
            }

            if(isWifiOffDevice){
                WifiControl.wifiON(context, false);
                checkToPlaySeconds = 10;
            }

            if (playMusic) {
                mPlayMusic.play();
                mPlayMusic.checkIfPlaying(context, checkToPlaySeconds);
            }

            if (volumeMAX) {
                volumeControl.checkSetMAXVol(context, 4);
            }

            if (launchMusicPlayer && !launchMaps) {
                launchApp.musicPlayerLaunch(context, 3);
            }

            if (launchMaps) {
                launchApp.launchMaps(context, 3);
            }

            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                boolean hasDoNotDisturbPerm = PermissionHelper.checkDoNotDisturbPermission(context, 10);
                if (priorityMode && hasDoNotDisturbPerm) {
                    BAPMDataPreferences.setCurrentRingerSet(context, ringerControl.ringerSetting());
                    ringerControl.soundsOFF();
                } else {
                    BroadcastReceiver broadcastReceiver = new NotifPolicyAccessChangedReceiver();
                    IntentFilter intentFilter = new IntentFilter(NotificationManager.ACTION_NOTIFICATION_POLICY_ACCESS_GRANTED_CHANGED);
                    context.getApplicationContext().registerReceiver(broadcastReceiver, intentFilter);
                }
            } else {
                if (priorityMode) {
                    BAPMDataPreferences.setCurrentRingerSet(context, ringerControl.ringerSetting());
                    ringerControl.soundsOFF();
                }
            }

            BAPMDataPreferences.setRanActionsOnBtConnect(context, true);
        }
    }

    //Removes notification and if set releases wakelock, puts the ringer back to normal,
    //pauses the music
    public void actionsOnBTDisconnect(){
        synchronized (this) {
            LaunchApp launchApp = new LaunchApp();
            RingerControl ringerControl = new RingerControl(context);

            boolean screenON = BAPMPreferences.getKeepScreenON(context);
            boolean priorityMode = BAPMPreferences.getPriorityMode(context);
            boolean playMusic = BAPMPreferences.getAutoPlayMusic(context);
            boolean sendToBackground = BAPMPreferences.getSendToBackground(context);
            boolean volumeMAX = BAPMPreferences.getMaxVolume(context);
            boolean closeWaze = BAPMPreferences.getCloseWazeOnDisconnect(context)
                    && launchApp.checkPkgOnPhone(context, PackageTools.PackageName.WAZE)
                    && BAPMPreferences.getMapsChoice(context).equals(PackageTools.PackageName.WAZE);
            boolean isWifiOffDevice = BAPMDataPreferences.getIsTurnOffWifiDevice(context);

            notification.removeBAPMMessage(context);

            if (screenON) {
                screenONLock.releaseWakeLock();
            }

            if (priorityMode) {
                int currentRinger = BAPMDataPreferences.getCurrentRingerSet(context);
                try {
                    switch (currentRinger) {
                        case AudioManager.RINGER_MODE_SILENT:
                            Log.d(TAG, "Phone is on Silent");
                            break;
                        case AudioManager.RINGER_MODE_VIBRATE:
                            ringerControl.vibrateOnly();
                            break;
                        case AudioManager.RINGER_MODE_NORMAL:
                            ringerControl.soundsON();
                            break;
                    }
                } catch (Exception e) {
                    Log.e(TAG, e.getMessage());
                }
            }

            if (playMusic) {
                mPlayMusic.pause();
            }

            if (volumeMAX) {
                volumeControl.setOriginalVolume(context);
            }

            if (sendToBackground) {
                launchApp.sendEverythingToBackground(context);
            }

            if(closeWaze) {
                launchApp.closeWazeOnDisconnect(context);
            }

            if(isWifiOffDevice){
                WifiControl.wifiON(context, true);
                BAPMDataPreferences.setIsTurnOffWifiDevice(context, false);
            }

            BAPMDataPreferences.setRanActionsOnBtConnect(context, false);
        }
    }

    public void actionsBTStateOff(){
        // Pause music
        PlayMusic playMusic = new PlayMusic(context);
        playMusic.pause();

        // Put music volume back to original volume
        volumeControl.setOriginalVolume(context);

        if(BuildConfig.DEBUG)
            Toast.makeText(context, "Music Paused", Toast.LENGTH_SHORT).show();

        actionsOnBTDisconnect();
    }
}
