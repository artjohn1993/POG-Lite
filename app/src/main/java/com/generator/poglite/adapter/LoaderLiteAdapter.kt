package com.generator.poglite.adapter

import android.text.Layout
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.generator.poglite.model.LoadingUrl
import com.generator.poglite.R

class LoaderLiteAdapter(var data: MutableList<LoadingUrl.Data>) :
    RecyclerView.Adapter<LoaderLiteAdapter.LoaderHolder>() {


    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): LoaderHolder {
        var inflater = LayoutInflater.from(parent.context)
        var layout = inflater.inflate(R.layout.loader_lite_view, parent, false)

        return LoaderHolder(layout)
    }

    override fun onBindViewHolder(holder: LoaderLiteAdapter.LoaderHolder, position: Int) {
        holder.url.text = data[position].url
        holder.total.text = "${data[position].loadedWordpress}/${data[position].totalWordpress} posts"
    }

    override fun getItemCount(): Int {
        return data.count()
    }

    class LoaderHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var url: TextView = itemView.findViewById(R.id.urlTxt)
        var total: TextView = itemView.findViewById(R.id.totalLoadedTxt)
    }

}