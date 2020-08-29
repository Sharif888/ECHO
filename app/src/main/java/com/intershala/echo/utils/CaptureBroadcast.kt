package com.intershala.echo.utils

import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.telephony.TelephonyManager
import com.intershala.echo.R
import com.intershala.echo.activities.MainActivity
import com.intershala.echo.fragments.SongPlayingFragment

class CaptureBroadcast : BroadcastReceiver(){
    override fun onReceive(context: Context?, intent: Intent?) {
        if(intent?.action == Intent.ACTION_NEW_OUTGOING_CALL)
        {
            try {
                MainActivity.Statified.notificationManager?.cancel(1978)
            }catch (e: java.lang.Exception)
            {
                e.printStackTrace()
            }

            if(SongPlayingFragment.Statified.mediaPlayer?.isPlaying as Boolean)
            {
                try {
                    SongPlayingFragment.Statified.mediaPlayer?.pause()
                    SongPlayingFragment.Statified.playPauseImageButton?.setBackgroundResource(R.drawable.play_icon)
                }catch (e: Exception)
                {
                    e.printStackTrace()
                }


            }else{
                val tm: TelephonyManager = context?.getSystemService(Context.TELEPHONY_SERVICE)
                        as TelephonyManager
                when(tm?.callState){
                    TelephonyManager.CALL_STATE_RINGING ->

                    {
                        try {
                            MainActivity.Statified.notificationManager?.cancel(1978)
                        }catch (e: java.lang.Exception)
                        {
                            e.printStackTrace()
                        }

                        try {
                            SongPlayingFragment.Statified.mediaPlayer?.pause()
                            SongPlayingFragment.Statified.playPauseImageButton?.setBackgroundResource(R.drawable.play_icon)
                        }catch (e: Exception)
                        {
                            e.printStackTrace()
                        }
                    }
                    else ->{

                    }
                }

            }
        }
    }

}