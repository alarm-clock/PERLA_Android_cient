package com.example.jmb_bms

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.jmb_bms.model.Affirmation

class MainMenuCustomAdapter( private val dataSet: List<Affirmation>): RecyclerView.Adapter<MainMenuCustomAdapter.ViewHolder>() {

    var onItemClick: ((Affirmation) -> Unit)? = null
    var tuple : List<Affirmation> = dataSet
    inner class ViewHolder( view: View) : RecyclerView.ViewHolder(view)
    {
        val textView: TextView
        val imageView: ImageView

        init {
            textView = view.findViewById(R.id.textView)
            imageView = view.findViewById(R.id.imageView)
            view.setOnClickListener {
                onItemClick?.invoke( tuple[bindingAdapterPosition])
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.main_menu_row_item , parent , false )
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.textView.setText(dataSet[position].stringId)
        holder.imageView.setImageResource(dataSet[position].imageId)
    }

    override fun getItemCount() = dataSet.size
}