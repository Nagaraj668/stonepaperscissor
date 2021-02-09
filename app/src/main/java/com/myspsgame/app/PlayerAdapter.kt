package com.myspsgame.app

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class PlayerAdapter(
    var context: Context?, var itemClickListener: ItemClickListener,
    var players: MutableList<OnlinePlayer>
) : RecyclerView.Adapter<PlayerAdapter.ViewHolder>() {

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        lateinit var playerNameTextView: TextView
        lateinit var playButton: Button

        fun bind() {
            playerNameTextView = itemView.findViewById(R.id.player_name)
            playButton = itemView.findViewById(R.id.play_btn)
            itemView.setOnClickListener {
                itemClickListener.onItemClick(adapterPosition)
            }

            playButton.setOnClickListener {
                itemClickListener.onItemClick(adapterPosition)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(context).inflate(
            R.layout.item_player, parent,
            false
        )
        val holder = ViewHolder(view)
        holder.bind()
        return holder
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val player = players[position]
        holder.playerNameTextView.text = player.playerName
    }

    override fun getItemCount(): Int {
        return players.size
    }
}
