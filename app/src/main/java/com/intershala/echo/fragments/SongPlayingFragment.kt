package com.intershala.echo.fragments


import android.app.Activity
import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.media.AudioManager
import android.media.MediaPlayer
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.support.v4.app.Fragment
import android.support.v4.content.ContextCompat
import android.view.*
import android.widget.ImageButton
import android.widget.SeekBar
import android.widget.TextView
import android.widget.Toast
import com.cleveroad.audiovisualization.AudioVisualization
import com.cleveroad.audiovisualization.DbmHandler
import com.cleveroad.audiovisualization.GLAudioVisualizationView
import com.internshala.echo.fragments.FavoriteFragment
import com.intershala.echo.CurrentSongHelper
import com.intershala.echo.R
import com.intershala.echo.Songs
import com.intershala.echo.databases.EchoDatabase
import com.intershala.echo.fragments.SongPlayingFragment.Statified.mediaPlayer
import com.intershala.echo.fragments.SongPlayingFragment.Statified.seekbar
import java.util.*
import java.util.concurrent.TimeUnit

import kotlin.Exception
import kotlin.collections.ArrayList


// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 *
 */
class SongPlayingFragment : Fragment() {
    var mAccelaration: Float = 0f
    var mAccelarationCurrent : Float = 0f
    var mAccelarationLast : Float = 0f
    object Statified {
        var MY_PREFS_NAME = "ShakeFeature"

        var myActivity: Activity? = null
        var mediaPlayer: MediaPlayer? = null
        var startTimeText: TextView? = null
        var endTimeText: TextView? = null
        var songTitleView: TextView? = null
        var songArtistView: TextView? = null
        var playPauseImageButton: ImageButton? = null
        var previousImageButton: ImageButton? = null
        var nextImageButton: ImageButton? = null
        var loopImageButton: ImageButton? = null
        var shuffleImageButton: ImageButton? = null
        var seekbar: SeekBar? = null
        var currentPosition: Int = 0
        var fetchSongs: ArrayList<Songs>? = null
        var audioVisulization: AudioVisualization? = null
        var glView: GLAudioVisualizationView? = null
        var favoriteContent: EchoDatabase? = null
        var currentSongHelper: CurrentSongHelper? = null
        var fab: ImageButton? = null
        var mSensorManager: SensorManager? = null
        var mSensorListener: SensorEventListener? = null

        var updateSongTime = object : Runnable {
            override fun run() {

                val getCurrent = mediaPlayer?.currentPosition
                startTimeText?.setText(
                    String.format(
                        "%d:%d",
                        TimeUnit.MILLISECONDS.toMinutes(getCurrent?.toLong() as Long),
                        TimeUnit.MILLISECONDS.toSeconds(getCurrent?.toLong() as Long)
                                - TimeUnit.MILLISECONDS.toSeconds(TimeUnit.MILLISECONDS.toMinutes(getCurrent?.toLong() as Long))
                    )
                )
                Handler().postDelayed(this, 1000)
            }

        }

    }


    object Staticated {
        var MY_PREFS_SHUFFLE = "Shuffle Feauture"
        var MY_PREFS_LOOP = "Loop Feature"

        fun onSongComplete() {
            if (Statified.currentSongHelper?.isShuffle as Boolean) {
                playNext("PlayNextLikeNormalShuffle")
                Statified.currentSongHelper?.isPlaying = true
            } else {
                if (Statified.currentSongHelper?.isLoop as Boolean) {
                    Statified.currentSongHelper?.isPlaying = true
                    var nextSong = Statified.fetchSongs?.get(Statified.currentPosition)
                    Statified.currentSongHelper?.songTitle = nextSong?.songTitle
                    Statified.currentSongHelper?.songPath = nextSong?.songData
                    Statified.currentSongHelper?.currentPosition = Statified.currentPosition
                    Statified.currentSongHelper?.songId = nextSong?.songID as Long
                    updateTextViews(
                        Statified.currentSongHelper?.songTitle as String,
                        Statified.currentSongHelper?.songArtist as String
                    )

                    Statified.mediaPlayer?.reset()
                    try {
                        Statified.mediaPlayer?.setDataSource(
                            Statified.myActivity,
                            Uri.parse(Statified.currentSongHelper?.songPath)
                        )
                        Statified.mediaPlayer?.prepare()
                        Statified.mediaPlayer?.start()
                        processInformation(Statified.mediaPlayer as MediaPlayer)


                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                } else {
                    playNext("PlayNextNormal")
                    Statified.currentSongHelper?.isPlaying = true
                }
            }
            if (Statified.favoriteContent?.checkifIdExists(Statified.currentSongHelper?.songId?.toInt() as Int) as Boolean) {
                Statified.fab?.setImageDrawable(
                    ContextCompat.getDrawable(
                        Statified.myActivity!!,
                        R.drawable.favorite_on
                    )
                )
            } else {
                Statified.fab?.setImageDrawable(
                    ContextCompat.getDrawable(
                        Statified.myActivity!!,
                        R.drawable.favorite_off
                    )
                )

            }

        }

        fun updateTextViews(songTitle: String, songArtist: String) {
            Statified.songTitleView?.setText(songTitle)
            Statified.songArtistView?.setText(songArtist)
        }

        fun processInformation(mediaPlayer: MediaPlayer) {
            val finalTime = mediaPlayer.duration
            val startTime = mediaPlayer.currentPosition
            Statified.seekbar?.setProgress(startTime)
            Statified.seekbar?.max = finalTime
            Statified.startTimeText?.setText(
                String.format(
                    "%d:%d",
                    TimeUnit.MILLISECONDS.toMinutes(startTime?.toLong() as Long),
                    TimeUnit.MILLISECONDS.toSeconds(startTime?.toLong() as Long)
                            - TimeUnit.MILLISECONDS.toSeconds(TimeUnit.MILLISECONDS.toMinutes(startTime?.toLong() as Long))
                )
            )
            Statified.endTimeText?.setText(
                String.format(
                    "%d:%d",
                    TimeUnit.MILLISECONDS.toMinutes(finalTime?.toLong() as Long),
                    TimeUnit.MILLISECONDS.toSeconds(finalTime?.toLong() as Long)
                            - TimeUnit.MILLISECONDS.toSeconds(TimeUnit.MILLISECONDS.toMinutes(finalTime?.toLong() as Long))
                )
            )
            Statified.seekbar?.setProgress(startTime)
            Handler().postDelayed(Statified.updateSongTime, 1000)

        }

        fun playNext(check: String) {
            if (check.equals("PlayNextNormal", true)) {
                Statified.currentPosition = Statified.currentPosition + 1

            } else if (check.equals("PlayNextLikeNormalShuffle", true)) {
                var randomObject = Random()
                var randomPosition = randomObject.nextInt(Statified.fetchSongs?.size?.plus(1) as Int)
                Statified.currentPosition = randomPosition


            }
            if (Statified.currentPosition == Statified.fetchSongs?.size) {
                Statified.currentPosition = 0
            }
            Statified.currentSongHelper?.isLoop = false
            var nextSong = Statified.fetchSongs?.get(Statified.currentPosition)
            Statified.currentSongHelper?.songTitle = nextSong?.songTitle
            Statified.currentSongHelper?.songPath = nextSong?.songData
            Statified.currentSongHelper?.currentPosition = Statified.currentPosition
            Statified.currentSongHelper?.songId = nextSong?.songID as Long

            updateTextViews(
                Statified.currentSongHelper?.songTitle as String,
                Statified.currentSongHelper?.songArtist as String
            )

            Statified.mediaPlayer?.reset()
            try {
                Statified.mediaPlayer?.setDataSource(
                    Statified.myActivity,
                    Uri.parse(Statified.currentSongHelper?.songPath)
                )
                Statified.mediaPlayer?.prepare()
                Statified.mediaPlayer?.start()
                Statified.playPauseImageButton?.setBackgroundResource(R.drawable.pause_icon)
                processInformation(Statified.mediaPlayer as MediaPlayer)


            } catch (e: Exception) {
                e.printStackTrace()
            }
            if (Statified.favoriteContent?.checkifIdExists(Statified.currentSongHelper?.songId?.toInt() as Int) as Boolean) {
                Statified.fab?.setImageDrawable(
                    ContextCompat.getDrawable(
                        Statified.myActivity!!,
                        R.drawable.favorite_on
                    )
                )
            } else {
                Statified.fab?.setImageDrawable(
                    ContextCompat.getDrawable(
                        Statified.myActivity!!,
                        R.drawable.favorite_off
                    )
                )

            }

        }
    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        var view = inflater!!.inflate(R.layout.fragment_song_playing, container, false)
        setHasOptionsMenu(true)
        Statified.seekbar = view?.findViewById(R.id.seekBar)
        Statified.startTimeText = view?.findViewById(R.id.startTime)
        Statified.endTimeText = view?.findViewById(R.id.endTime)
        Statified.playPauseImageButton = view?.findViewById(R.id.playPauseButton)
        Statified.nextImageButton = view?.findViewById(R.id.nextButton)
        Statified.previousImageButton = view?.findViewById(R.id.previousButton)
        Statified.loopImageButton = view?.findViewById(R.id.loopButton)
        Statified.shuffleImageButton = view?.findViewById(R.id.shuffleButton)
        Statified.songArtistView = view?.findViewById(R.id.songArtist)
        Statified.songTitleView = view?.findViewById(R.id.songTitle)
        Statified.glView = view?.findViewById(R.id.visualizer_view)
        Statified.fab = view?.findViewById(R.id.favoriteIcon)
        Statified.fab?.alpha = 0.8f
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Statified.audioVisulization = Statified.glView as AudioVisualization
    }

    override fun onAttach(context: Context?) {
        super.onAttach(context)
        Statified.myActivity = context as Activity
    }

    override fun onAttach(activity: Activity?) {
        super.onAttach(activity)
        Statified.myActivity = activity
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        Statified.favoriteContent = EchoDatabase(Statified.myActivity)
        Statified.currentSongHelper = CurrentSongHelper()
        Statified.currentSongHelper?.isPlaying = true
        Statified.currentSongHelper?.isLoop = false
        Statified.currentSongHelper?.isShuffle = false

        var path: String? = null
        var _songTitle: String? = null
        var songId: Long = 0
        var _songArtist: String? = null

        try {
            path = arguments?.getString("path")
            _songArtist = arguments?.getString("songArtist")
            _songTitle = arguments?.getString("songTitle")
            songId = arguments?.getInt("songId")!!.toLong()

            Statified.currentPosition = arguments!!.getInt("songPosition")
            Statified.fetchSongs = arguments?.getParcelableArrayList("songData")
            Statified.currentSongHelper?.songPath = path
            Statified.currentSongHelper?.songId = songId
            Statified.currentSongHelper?.songTitle = _songTitle
            Statified.currentSongHelper?.songArtist = _songArtist
            Statified.currentSongHelper?.currentPosition = Statified.currentPosition
            Staticated.updateTextViews(
                Statified.currentSongHelper?.songTitle as String,
                Statified.currentSongHelper?.songArtist as String
            )


        } catch (e: Exception) {
            e.printStackTrace()
        }
        var fromFavBottomBar = arguments?.get("FavBottomBar") as? String
        if (fromFavBottomBar != null) {
            Statified.mediaPlayer = FavoriteFragment.Statified.mediaPlayer
        } else {

            Statified.mediaPlayer = MediaPlayer()
            Statified.mediaPlayer?.setAudioStreamType(AudioManager.STREAM_MUSIC)

            try {
                Statified.mediaPlayer?.setDataSource(Statified.myActivity, Uri.parse(path))
                Statified.mediaPlayer?.prepare()
            } catch (e: Exception) {
                e.printStackTrace()
            }
            Statified.mediaPlayer?.start()
        }
        Staticated.processInformation(Statified.mediaPlayer as MediaPlayer)
        if (Statified.currentSongHelper?.isPlaying as Boolean) {
            Statified.playPauseImageButton?.setBackgroundResource(R.drawable.pause_icon)
        } else {
            Statified.playPauseImageButton?.setBackgroundResource(R.drawable.play_icon)
        }
        Statified.mediaPlayer?.setOnCompletionListener {
            Staticated.onSongComplete()
        }
        clickHandler()
        var visualizationHandler = DbmHandler.Factory.newVisualizerHandler(Statified.myActivity as Context, 0)
        Statified.audioVisulization?.linkTo(visualizationHandler)
        var prefsForShuffle =
            Statified.myActivity?.getSharedPreferences(Staticated.MY_PREFS_SHUFFLE, Context.MODE_PRIVATE)
        var isShuffleAllowed = prefsForShuffle?.getBoolean("feature", false)
        if (isShuffleAllowed as Boolean) {
            Statified.currentSongHelper?.isShuffle = true
            Statified.currentSongHelper?.isLoop = false
            Statified.shuffleImageButton?.setBackgroundResource(R.drawable.shuffle_icon)
            Statified.loopImageButton?.setBackgroundResource(R.drawable.shuffle_white_icon)
        } else {
            Statified.currentSongHelper?.isShuffle = false
            Statified.shuffleImageButton?.setBackgroundResource(R.drawable.shuffle_white_icon)
        }
        var prefsForLoop = Statified.myActivity?.getSharedPreferences(Staticated.MY_PREFS_LOOP, Context.MODE_PRIVATE)
        var isLoopAllowed = prefsForLoop?.getBoolean("feature", false)
        if (isLoopAllowed as Boolean) {
            Statified.currentSongHelper?.isShuffle = false
            Statified.currentSongHelper?.isLoop = true
            Statified.shuffleImageButton?.setBackgroundResource(R.drawable.shuffle_white_icon)
            Statified.loopImageButton?.setBackgroundResource(R.drawable.loop_icon)
        } else {
            Statified.currentSongHelper?.isLoop = false
            Statified.shuffleImageButton?.setBackgroundResource(R.drawable.shuffle_icon
            )
        }
        //Causing the crash
        if (Statified.favoriteContent?.checkifIdExists(Statified.currentSongHelper?.songId?.toInt() as Int) as Boolean) {
            Statified.fab?.setImageDrawable(ContextCompat.getDrawable(Statified.myActivity!!, R.drawable.favorite_on))
        } else {
            Statified.fab?.setImageDrawable(ContextCompat.getDrawable(Statified.myActivity!!, R.drawable.favorite_off))

        }
    }

    override fun onPause() {
        Statified.audioVisulization?.onPause()
        super.onPause()

        Statified.mSensorManager?.unregisterListener(Statified.mSensorListener)

    }

    override fun onResume() {
        super.onResume()
        Statified.audioVisulization?.onResume()
        Statified.mSensorManager?.registerListener(
            Statified.mSensorListener,
            Statified.mSensorManager?.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
            SensorManager.SENSOR_DELAY_NORMAL
        )

    }

    override fun onDestroyView() {
        Statified.audioVisulization?.release()
        super.onDestroyView()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Statified.mSensorManager = Statified.myActivity?.getSystemService(Context.SENSOR_SERVICE) as SensorManager
        mAccelaration = 0.0f
        mAccelarationCurrent = SensorManager.GRAVITY_EARTH
        mAccelarationLast = SensorManager.GRAVITY_EARTH
        bindShakeListener()
    }

    override fun onCreateOptionsMenu(menu: Menu?, inflater: MenuInflater?) {
        menu?.clear()
        inflater?.inflate(R.menu.song_playing_menu,menu)
        super.onCreateOptionsMenu(menu, inflater)


    }

    override fun onPrepareOptionsMenu(menu: Menu?) {

        super.onPrepareOptionsMenu(menu)
        var item: MenuItem? = menu?.findItem(R.id.action_redirect)
        item?.isVisible = true
        var item2: MenuItem? = menu?.findItem(R.id.action_sort)
        item2?.isVisible = false
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when(item?.itemId){
            R.id.action_redirect -> {
                Statified.myActivity?.onBackPressed()
                return false
            }
        }
        return false
    }


    fun clickHandler() {
        seekbar?.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {
            }

            override fun onStopTrackingTouch(seekBarget: SeekBar?) {
             seekbar?.setProgress(seekbar?.getProgress() as Int)
                mediaPlayer?.seekTo(seekbar?.getProgress() as Int)
            }
        })
        Statified.fab?.setOnClickListener({
            if (Statified.favoriteContent?.checkifIdExists(Statified.currentSongHelper?.songId?.toInt() as Int) as Boolean) {
                Statified.fab?.setImageDrawable(
                    ContextCompat.getDrawable(
                        Statified.myActivity!!,
                        R.drawable.favorite_off
                    )
                )
                Statified.favoriteContent?.deleteFavourite(Statified.currentSongHelper?.songId?.toInt() as Int)
                Toast.makeText(Statified.myActivity, "Removed From Favourites", Toast.LENGTH_SHORT).show()
            } else {
                Statified.fab?.setImageDrawable(
                    ContextCompat.getDrawable(
                        Statified.myActivity!!,
                        R.drawable.favorite_on
                    )
                )
                Statified.favoriteContent?.storeasFavourite(
                    Statified.currentSongHelper?.songId?.toInt(),
                    Statified.currentSongHelper?.songArtist,
                    Statified.currentSongHelper?.songTitle,
                    Statified.currentSongHelper?.songPath
                )
                Toast.makeText(Statified.myActivity, "Added to Favorite", Toast.LENGTH_SHORT).show()

            }
        })
        Statified.shuffleImageButton?.setOnClickListener({
            var editorShuffle =
                Statified.myActivity?.getSharedPreferences(Staticated.MY_PREFS_SHUFFLE, Context.MODE_PRIVATE)?.edit()
            var editorLoop =
                Statified.myActivity?.getSharedPreferences(Staticated.MY_PREFS_LOOP, Context.MODE_PRIVATE)?.edit()

            if (Statified.currentSongHelper?.isShuffle as Boolean) {
                Statified.shuffleImageButton?.setBackgroundResource(R.drawable.shuffle_white_icon)
                Statified.currentSongHelper?.isShuffle = false
                editorShuffle?.putBoolean("feature", false)
                editorShuffle?.apply()
            } else {
                Statified.currentSongHelper?.isShuffle = true
                Statified.currentSongHelper?.isLoop = false
                Statified.shuffleImageButton?.setBackgroundResource(R.drawable.shuffle_icon)
                Statified.loopImageButton?.setBackgroundResource(R.drawable.loop_white_icon)
                editorShuffle?.putBoolean("feature", true)
                editorShuffle?.apply()
                editorLoop?.putBoolean("feature", false)
                editorLoop?.apply()
            }


        })

        Statified.nextImageButton?.setOnClickListener({
            Statified.playPauseImageButton?.setBackgroundResource(R.drawable.pause_icon)
            Statified.currentSongHelper?.isPlaying = true
            if (Statified.currentSongHelper?.isShuffle as Boolean) {
                Staticated.playNext("PlayNextLikeNormalShuffle")
            } else {
                Staticated.playNext("PlayNextNormal")
            }

        })
        Statified.previousImageButton?.setOnClickListener({
            Statified.currentSongHelper?.isPlaying = true
            if (Statified.currentSongHelper?.isPlaying as Boolean) {
                Statified.loopImageButton?.setBackgroundResource(R.drawable.loop_white_icon)
            }
            playPrevious()
        })
        Statified.loopImageButton?.setOnClickListener({
            var editorShuffle =
                Statified.myActivity?.getSharedPreferences(Staticated.MY_PREFS_SHUFFLE, Context.MODE_PRIVATE)?.edit()
            var editorLoop =
                Statified.myActivity?.getSharedPreferences(Staticated.MY_PREFS_LOOP, Context.MODE_PRIVATE)?.edit()

            if (Statified.currentSongHelper?.isLoop as Boolean) {
                Statified.currentSongHelper?.isLoop = false
                Statified.loopImageButton?.setBackgroundResource(R.drawable.loop_white_icon)
                editorLoop?.putBoolean("feature", false)
                editorLoop?.apply()
            } else {

                Statified.currentSongHelper?.isLoop = true
                Statified.currentSongHelper?.isShuffle = false
                Statified.loopImageButton?.setBackgroundResource(R.drawable.loop_icon)
                Statified.shuffleImageButton?.setBackgroundResource(R.drawable.shuffle_white_icon)
                editorShuffle?.putBoolean("feature", false)
                editorShuffle?.apply()
                editorLoop?.putBoolean("feature", true)
                editorLoop?.apply()
            }

        })
        Statified.playPauseImageButton?.setOnClickListener({
            if (Statified.mediaPlayer?.isPlaying as Boolean) {
                Statified.mediaPlayer?.pause()
                Statified.currentSongHelper?.isPlaying = false
                Statified.playPauseImageButton?.setBackgroundResource(R.drawable.play_icon)

            } else {
                Statified.mediaPlayer?.start()

                Statified.currentSongHelper?.isPlaying = true
                Statified.playPauseImageButton?.setBackgroundResource(R.drawable.pause_icon)

            }


        })

    }


    fun playPrevious() {
        Statified.currentPosition = Statified.currentPosition - 1
        if (Statified.currentPosition == -1) {
            Statified.currentPosition = 0
        }
        if (Statified.currentSongHelper?.isPlaying as Boolean) {
            Statified.playPauseImageButton?.setBackgroundResource(R.drawable.pause_icon)
        } else {
            Statified.playPauseImageButton?.setBackgroundResource(R.drawable.play_icon)
        }
        Statified.currentSongHelper?.isLoop = false
        var nextSong = Statified.fetchSongs?.get(Statified.currentPosition)
        Statified.currentSongHelper?.songTitle = nextSong?.songTitle
        Statified.currentSongHelper?.songPath = nextSong?.songData
        Statified.currentSongHelper?.songId = nextSong?.songID as Long

        Statified.currentSongHelper?.currentPosition = Statified.currentPosition
        Statified.mediaPlayer?.reset()
        try {
            Statified.mediaPlayer?.setDataSource(Statified.myActivity, Uri.parse(Statified.currentSongHelper?.songPath))
            Statified.mediaPlayer?.prepare()
            Statified.mediaPlayer?.start()
            Staticated.processInformation(Statified.mediaPlayer as MediaPlayer)


        } catch (e: Exception) {
            e.printStackTrace()
        }

        if (Statified.favoriteContent?.checkifIdExists(Statified.currentSongHelper?.songId?.toInt() as Int) as Boolean) {
            Statified.fab?.setImageDrawable(ContextCompat.getDrawable(Statified.myActivity!!, R.drawable.favorite_on))
        } else {
            Statified.fab?.setImageDrawable(ContextCompat.getDrawable(Statified.myActivity!!, R.drawable.favorite_off))

        }

    }

    fun bindShakeListener() {
        Statified.mSensorListener = object : SensorEventListener {
            override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {

            }

            override fun onSensorChanged(event: SensorEvent) {
                var x = event.values[0]
                var y = event.values[1]
                var z = event.values[2]
                mAccelarationLast = mAccelarationCurrent
                mAccelarationCurrent = Math.sqrt(((x*x + y*y +z*z).toDouble())).toFloat()
                val delta = mAccelarationCurrent - mAccelarationLast
                mAccelaration = mAccelaration * 0.9f+ delta
                if(mAccelaration > 14) {
                    val prefs =
                        Statified.myActivity?.getSharedPreferences(Statified.MY_PREFS_NAME, Context.MODE_PRIVATE)
                    val isAllowed = prefs?.getBoolean("feature", true)
                    if (isAllowed as Boolean)
                    {
                        Staticated.playNext("PlayNextNormal")
                    }
                }
            }
        }
    }
}



