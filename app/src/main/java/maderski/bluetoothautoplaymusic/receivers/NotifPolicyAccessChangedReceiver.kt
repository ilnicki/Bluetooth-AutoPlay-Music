package maderski.bluetoothautoplaymusic.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import maderski.bluetoothautoplaymusic.controls.RingerControl
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

/**
 * Created by Jason on 2/12/17.
 */

class NotifPolicyAccessChangedReceiver : BroadcastReceiver(), KoinComponent {
    private val ringerControl: RingerControl by inject()

    override fun onReceive(context: Context, intent: Intent?) {
        if (intent != null) {
            if (intent.action != null) {
                Log.d(TAG, "ACTION: ${intent.action}")
                ringerControl.saveCurrentRingerSetting()
                ringerControl.soundsOFF()
                context.applicationContext.unregisterReceiver(this)
            }
        }
    }

    companion object {
        private const val TAG = "NotifPolicyAccessChange"
    }
}
