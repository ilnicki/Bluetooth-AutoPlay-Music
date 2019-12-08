package maderski.bluetoothautoplaymusic.helpers

import android.bluetooth.BluetoothDevice
import maderski.bluetoothautoplaymusic.helpers.enums.MapApps
import maderski.bluetoothautoplaymusic.sharedprefs.BAPMPreferences

class PreferencesHelper(private val preferences: BAPMPreferences) {
    val mapAppChosen get() = preferences.getMapsChoice()
    val customLocationName get() = preferences.getCustomLocationName()
    val canLaunchDirections get() = preferences.getCanLaunchDirections()
    val canLaunchDrivingMode get() = preferences.getLaunchMapsDrivingMode() &&
            mapAppName == MapApps.MAPS.packageName
    val isLaunchingWithDirections get() = preferences.getCanLaunchDirections()
    val isUsingTimesToLaunch get() = preferences.getUseTimesToLaunchMaps()

    val morningStartTime get() = preferences.getMorningStartTime()
    val morningEndTime get() = preferences.getMorningEndTime()

    val eveningStartTime get() = preferences.getEveningStartTime()
    val eveningEndTime get() = preferences.getEveningEndTime()

    val customStartTime get() = preferences.getCustomStartTime()
    val customEndTime get() = preferences.getCustomEndTime()

    val isUseLaunchTimeEnabled get() = preferences.getUseTimesToLaunchMaps()

    val musicPlayerPkgName get() = preferences.getPkgSelectedMusicPlayer()

    val daysToLaunchHome get() = preferences.getHomeDaysToLaunchMaps() ?: setOf<String>()
    val daysToLaunchWork get() = preferences.getWorkDaysToLaunchMaps() ?: setOf<String>()
    val daysToLaunchCustom get() = preferences.getCustomDaysToLaunchMaps() ?: setOf<String>()

    val waitTillOffPhone get() = preferences.getWaitTillOffPhone()
    val unlockScreen get() = preferences.getUnlockScreen()
    val mapAppName get() = preferences.getMapsChoice()
    val canShowNotification get() = preferences.getShowNotification()
    val keepScreenON get() = preferences.getKeepScreenON()
    val volumeMAX get() = preferences.getMaxVolume()
    val canAutoPlayMusic get() = preferences.getAutoPlayMusic()
    val isLaunchingMusicPlayer get() = preferences.getLaunchMusicPlayer()
    val isLaunchingMaps get() = preferences.getLaunchGoogleMaps()
    val isUsingWifiMapTimeSpans get() = preferences.getWifiUseMapTimeSpans()
    val priorityMode get() = preferences.getPriorityMode()
    val shouldCloseWaze get() = preferences.getCloseWazeOnDisconnect()
    val originalVolume get() = preferences.getRestoreNotificationVolume()
    val headphoneVolume get() = preferences.getHeadphonePreferredVolume()
    val waitTillPowerConnected = preferences.getPowerConnected()

    private val selectedBTDevices get() = preferences.getBTDevices()
    private val selectedHeadphoneDevices get() = preferences.getHeadphoneDevices()

    fun isASelectedBTDevice(bluetoothDevice: BluetoothDevice): Boolean =
            selectedBTDevices.contains(bluetoothDevice.name)

    fun isASelectedHeadphonesBT(bluetoothDevice: BluetoothDevice): Boolean =
            selectedHeadphoneDevices.contains(bluetoothDevice.name)
}