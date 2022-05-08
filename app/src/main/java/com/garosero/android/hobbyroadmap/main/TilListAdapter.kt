package com.garosero.android.hobbyroadmap.main

import android.content.Intent
import android.os.Build
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.recyclerview.widget.RecyclerView
import com.garosero.android.hobbyroadmap.data.TilItem
import com.garosero.android.hobbyroadmap.databinding.RecyclerTilListBinding
import com.garosero.android.hobbyroadmap.til.TilActivity

/**
 * Adapter for the [RecyclerView] in [TilListFragment].
 */

@RequiresApi(Build.VERSION_CODES.O)
class TilListAdapter(var dataSet: MutableList<TilItem>) :
    RecyclerView.Adapter<TilListAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = RecyclerTilListBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int = dataSet.size
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(dataSet[position])
    }

    inner class ViewHolder(private val binding : RecyclerTilListBinding)
        : RecyclerView.ViewHolder(binding.root){

        fun bind(item: TilItem){
            binding.apply {
                // todo : item
                tvTitle.text = item.content
                layout.setOnClickListener {
                    val intent = Intent(itemView.context, TilActivity::class.java)
                    itemView.context.startActivity(intent)
                }
            }
        }
    }

    // submit data
    fun submitData(dataSet: MutableList<TilItem>){
        this.dataSet = dataSet
        notifyDataSetChanged()
    }
}
