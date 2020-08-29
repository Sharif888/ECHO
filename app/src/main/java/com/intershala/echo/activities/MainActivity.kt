package com.intershala.echo.activities

import android.app.Notification
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.PersistableBundle
import android.support.v7.app.ActionBarDrawerToggle
import android.support.v4.widget.DrawerLayout
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.DefaultItemAnimator
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.Toolbar
import com.intershala.echo.R
import com.intershala.echo.adapters.NavigationDrawerAdapter
import com.intershala.echo.fragments.MainScreenFragment
import com.intershala.echo.fragments.SongPlayingFragment
import java.lang.Exception

class MainActivity : AppCompatActivity(){
    object Statified {
        var drawerLayout:DrawerLayout?=null
        var notificationManager : NotificationManager? =null
    }
    var trackNotificationBuilder: Notification? = null
    var navigationDrawerIconsList:ArrayList<String> = arrayListOf()
    var images_for_navdrawer = intArrayOf(R.drawable.navigation_allsongs,R.drawable.navigation_favorites,
        R.drawable.navigation_settings,R.drawable.navigation_aboutus)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val toolbar=findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)


        navigationDrawerIconsList.add("All songs")
        navigationDrawerIconsList.add("Favorites")
        navigationDrawerIconsList.add("Settings")
        navigationDrawerIconsList.add("About us")

        val mainScreenFragment = MainScreenFragment()
        this.supportFragmentManager
            .beginTransaction()
            .add(R.id.details_fragment, mainScreenFragment, "RecyclerScreenFragment")
            .commit()


        Statified.drawerLayout = findViewById(R.id.drawer_layout) as DrawerLayout
        val toggle = ActionBarDrawerToggle(this@MainActivity, Statified.drawerLayout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close)
        Statified.drawerLayout?.setDrawerListener(toggle)
        toggle.syncState()

        //navigation drawer code
        val _navigationAdapter = NavigationDrawerAdapter(navigationDrawerIconsList, images_for_navdrawer, this@MainActivity)
        _navigationAdapter.notifyDataSetChanged()
        val navigation_drawer_recycler = findViewById(R.id.navigation_recycler_view) as RecyclerView
        navigation_drawer_recycler.layoutManager = LinearLayoutManager(this@MainActivity)
        navigation_drawer_recycler.itemAnimator = DefaultItemAnimator()
        navigation_drawer_recycler.adapter = _navigationAdapter
        navigation_drawer_recycler.setHasFixedSize(true)
        val intent = Intent(this@MainActivity,MainActivity::class.java)
        val pIntent = PendingIntent.getActivity(this@MainActivity, System.currentTimeMillis().toInt(),intent,0)
         trackNotificationBuilder = Notification.Builder(this)
             .setContentTitle("A track is playing in the background")
             .setSmallIcon(R.drawable.echo_logo)
             .setContentIntent(pIntent)
             .setOngoing(true)
             .setAutoCancel(true)
             .build()
        Statified.notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager


    }

    override fun onStart() {
        super.onStart()
        try {
            Statified.notificationManager?.cancel(1978)
        }catch (e:Exception)
        {
            e.printStackTrace()
        }

    }

    override fun onStop() {
        super.onStop()
        try {
            if(SongPlayingFragment.Statified.mediaPlayer?.isPlaying as Boolean)
                Statified.notificationManager?.notify(1978,trackNotificationBuilder)

        }catch (e:Exception)
        {
            e.printStackTrace()
        }

    }

    override fun onResume() {
        super.onResume()
        try {
            Statified.notificationManager?.cancel(1978)
        }catch (e:Exception)
        {
            e.printStackTrace()
        }

    }
}