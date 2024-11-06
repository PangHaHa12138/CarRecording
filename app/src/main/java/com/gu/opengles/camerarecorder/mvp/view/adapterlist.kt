package com.gu.opengles.camerarecorder.mvp.view

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.ImageView
import android.widget.TextView
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.gu.opengles.camerarecorder.R
import com.gu.opengles.camerarecorder.mvp.model.repo.VideoDetail


class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    val name: TextView = itemView.findViewById(R.id.nameTv)
    val sizeTv: TextView = itemView.findViewById(R.id.videoSizeTv)
    val iv: ImageView = itemView.findViewById(R.id.iv)
    val checkIv: CheckBox = itemView.findViewById(R.id.checkedIv)
    val lockIv: ImageView = itemView.findViewById(R.id.lockIv)
}

class VideoListAdapter(context: Context, private val data: MutableList<VideoDetail>) :
    RecyclerView.Adapter<MyViewHolder>() {
    private var inflater: LayoutInflater? = LayoutInflater.from(context)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val holder = MyViewHolder(inflater!!.inflate(R.layout.video_list_item, parent, false))

        holder.itemView.setOnClickListener {
            val pos = holder.adapterPosition
            data[pos].selected = !data[pos].selected
            holder.checkIv.isChecked = data[pos].selected
            notifyItemChanged(pos)
        }
        return holder
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        holder.apply {
            val item = data[position]
            name.text = if (item.recording) "RECORDING" else item.name
            sizeTv.text = if (item.recording) "..." else item.fileSize
            if (item.recording) iv.setImageResource(R.drawable.picture_unknown)
            else iv.setImageBitmap(item.bitmap)
            checkIv.isChecked = item.selected
            lockIv.isVisible = item.locked
        }
    }

    override fun getItemCount(): Int {
        return data.size
    }

    fun release() {
        inflater = null
    }
}