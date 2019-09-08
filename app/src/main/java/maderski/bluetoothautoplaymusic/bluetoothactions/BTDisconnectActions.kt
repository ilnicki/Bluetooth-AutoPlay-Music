package maderski.bluetoothautoplaymusic.bluetoothactions

import android.content.Context
import android.media.AudioManager
import android.os.Handler
import android.util.Log
import maderski.bluetoothautoplaymusic.controls.RingerControl
import maderski.bluetoothautoplaymusic.controls.VolumeControl
import maderski.bluetoothautoplaymusic.controls.WifiControl
import maderski.bluetoothautoplaymusic.controls.mediaplayer.MediaPlayerControlManager
import maderski.bluetoothautoplaymusic.helpers.LaunchAppHelper
import maderski.bluetoothautoplaymusic.helpers.LaunchAppHelper.DirectionLocation
import maderski.bluetoothautoplaymusic.helpers.PackageHelper.MapApps.WAZE
import maderski.bluetoothautoplaymusic.helpers.TimeHelper
import maderski.bluetoothautoplaymusic.notification.BAPMNotification
import maderski.bluetoothautoplaymusic.services.BTDisconnectService
import maderski.bluetoothautoplaymusic.services.ServiceManager
import maderski.bluetoothautoplaymusic.services.WakeLockService
import maderski.bluetoothautoplaymusic.sharedprefs.BAPMDataPreferences
import maderski.bluetoothautoplaymusic.sharedprefs.BAPMPreferences
import maderski.bluetoothautoplaymusic.utils.serviceManager
import org.koin.core.KoinComponent
import org.koin.core.inject

/**
 * Created by Jason on 6/3/17.
 */

class BTDisconnectActions(private val context: Context): KoinComponent {
    private val preferences: BAPMPreferences by inject()
    private val dataPreferences: BAPMDataPreferences by inject()
    private val mBAPMNotification: BAPMNotification by inject()
    private val mVolumeControl: VolumeControl by inject()
    private val launchAppHelper: LaunchAppHelper by inject()
    private val ringerControl: RingerControl by inject()
    private val mediaPlayerControlManager: MediaPlayerControlManager by inject()
    private val serviceManager: ServiceManager by inject()

    //Removes mNotification and if set releases wakelock, puts the ringer back to normal,
    //pauses the music
    fun actionsOnBTDisconnect() {
        removeBAPMNotification()
        pauseMusic()
        turnOffPriorityMode(ringerControl)
        sendAppToBackground(launchAppHelper)
        closeWaze(launchAppHelper)
        setWifiOn(launchAppHelper)
        stopKeepingScreenOn()

        setVolumeBack(ringerControl)

        stopService()
    }

    private fun stopService() {
        dataPreferences.setRanActionsOnBtConnect(false)
        Handler().postDelayed({ serviceManager.stopService(BTDisconnectService::class.java, BTDisconnectService.TAG) }, 5000)
    }

    private fun removeBAPMNotification() {
        val canShowNotification = preferences.getShowNotification()

        if (canShowNotification) {
            mBAPMNotification.removeBAPMMessage()
        }
    }

    private fun pauseMusic() {
        val playMusic = preferences.getAutoPlayMusic()
        if (playMusic) {
            mediaPlayerControlManager.pause()
        }
    }

    private fun sendAppToBackground(launchAppHelper: LaunchAppHelper) {
        val sendToBackground = preferences.getSendToBackground()
        if (sendToBackground) {
            launchAppHelper.sendEverythingToBackground(context)
        }
    }

    private fun turnOffPriorityMode(ringerControl: RingerControl) {

        val priorityMode = preferences.getPriorityMode()
        if (priorityMode) {
            val currentRinger = dataPreferences.getCurrentRingerSet()
            try {
                when (currentRinger) {
                    AudioManager.RINGER_MODE_SILENT -> Log.d(TAG, "Phone is on Silent")
                    AudioManager.RINGER_MODE_VIBRATE -> ringerControl.vibrateOnly()
                    AudioManager.RINGER_MODE_NORMAL -> ringerControl.soundsON()
                }
            } catch (e: Exception) {
                Log.e(TAG, e.message)
            }

        }
    }

    private fun closeWaze(launchAppHelper: LaunchAppHelper) {
        val closeWaze = (preferences.getCloseWazeOnDisconnect()
                && launchAppHelper.isAbleToLaunch(WAZE.packageName)
                && preferences.getMapsChoice() == WAZE.packageName)
        if (closeWaze) {
            launchAppHelper.closeWazeOnDisconnect()
        }
    }

    private fun setWifiOn(launchAppHelper: LaunchAppHelper) {
        val isWifiOffDevice = dataPreferences.getIsTurnOffWifiDevice()
        if (isWifiOffDevice) {
            val eveningStartTime = preferences.getEveningStartTime()
            val eveningEndTime = preferences.getEveningEndTime()

            val current24hrTime = TimeHelper.current24hrTime

            val timeHelperEvening = TimeHelper(eveningStartTime, eveningEndTime, current24hrTime)
            val canLaunch = timeHelperEvening.isWithinTimeSpan
            val directionLocation = if (canLaunch) DirectionLocation.HOME else DirectionLocation.WORK

            val canChangeWifiState = preferences.getWifiUseMapTimeSpans().not() || canLaunch && launchAppHelper.canLaunchOnThisDay(directionLocation)
            if (canChangeWifiState && !WifiControl.isWifiON(context)) {
                WifiControl.wifiON(context, true)
            }
            dataPreferences.setIsTurnOffWifiDevice(false)
        }
    }

    private fun stopKeepingScreenOn() {
        val screenON = preferences.getKeepScreenON()
        if (screenON) {
            serviceManager.stopService(WakeLockService::class.java, WakeLockService.TAG)
        }
    }

    private fun setVolumeBack(ringerControl: RingerControl) {
        val volumeMAX = preferences.getMaxVolume()
        val setOriginalVolume = preferences.getRestoreNotificationVolume()

        if (volumeMAX && setOriginalVolume) {
            mVolumeControl.setToOriginalVolume(ringerControl)
        }
    }

    companion object {
        private const val TAG = "BTDisconnectActions"
    }
}
