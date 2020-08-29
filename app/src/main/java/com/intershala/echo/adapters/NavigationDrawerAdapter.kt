package com.intershala.echo.adapters

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import com.internshala.echo.fragments.FavoriteFragment
import com.intershala.echo.R
import com.intershala.echo.activities.MainActivity
import com.intershala.echo.fragments.AboutUsFragment
import com.intershala.echo.fragments.MainScreenFragment
import com.intershala.echo.fragments.SettingFragment

class NavigationDrawerAdapter(_contentList: ArrayList<String>,_getImages:IntArray,_context:Context)
    :RecyclerView.Adapter<NavigationDrawerAdapter.NavViewHolder>() {
    var contentList:ArrayList<String>?=null
    var getImages:IntArray?=null
    var mContext:Context?= null
            init{
                this.contentList=_contentList
                this.getImages=_getImages
                this.mContext=_context
            }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NavViewHolder {
     var itemView=LayoutInflater.from(parent?.context)
             .inflate(R.layout.row_custom_navigation_drawer,parent,false)
        var returnThis=NavViewHolder(itemView)
        return  returnThis
    }

    override fun getItemCount(): Int {
        return contentList?.size as Int

    }

    override fun onBindViewHolder(holder: NavViewHolder, position: Int) {
   holder?.icon_GET?.setBackgroundResource(getImages?.get(position) as Int)
        holder?.text_GET?.setText(contentList?.get(position))
        holder?.content_Holder?.setOnClickListener({
            if(position==0)
            {
                val mainScreenFragment=MainScreenFragment()
                (mContext as MainActivity).supportFragmentManager
                    .beginTransaction()
                    .replace(R.id.details_fragment,mainScreenFragment)
                    .commit()
            }
            else if(position==1){
                val favoriteFragment= FavoriteFragment()
                (mContext as MainActivity).supportFragmentManager
                    .beginTransaction()
                    .replace(R.id.details_fragment,favoriteFragment)
                    .commit()
            }
            else if(position==2){
                val settingFragment=SettingFragment()
                (mContext as MainActivity).supportFragmentManager
                    .beginTransaction()
                    .replace(R.id.details_fragment,settingFragment)
                    .commit()
            }
            else{
                val aboutUsFragment =AboutUsFragment()
                (mContext as MainActivity).supportFragmentManager
                    .beginTransaction()
                    .replace(R.id.details_fragment,aboutUsFragment)
                    .commit()
            }
        })

    }

    class NavViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var icon_GET:ImageView?=null
        var text_GET:TextView?=null
        var content_Holder:RelativeLayout?=null
        init {
            icon_GET=itemView.findViewById(R.id.icon_navdrawer)
            text_GET=itemView.findViewById(R.id.text_navdrawer)
            content_Holder=itemView.findViewById(R.id.navdrawer_item_content_holder)
        }

    }
}