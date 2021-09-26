package fi.joonaun.helsinkitour.ui.search

import android.graphics.drawable.Drawable
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.progressindicator.CircularProgressIndicator
import com.squareup.picasso.Picasso
import fi.joonaun.helsinkitour.R
import fi.joonaun.helsinkitour.network.Activity

class SearchRecyclerViewAdapter : RecyclerView.Adapter<SearchRecyclerViewAdapter.ViewHolder>() {

    val results: MutableList<Activity> = mutableListOf()

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val img: ImageView
        val txtTitle: TextView
        val txtDescription: TextView

        init {
            view.apply {
                img = findViewById(R.id.imsSingleImg)
                txtTitle = findViewById(R.id.txtSingleTitle)
                txtDescription = findViewById(R.id.txtSingleDescription)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.single_search_item, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = results[position]
        holder.apply {
            txtTitle.text = item.name.en
            txtDescription.text = item.description.intro
        }

        val image = item.description.images.firstOrNull()
        if (image != null) {
            Picasso.get()
                .load(image.url)
                .fit()
                .error(R.drawable.ic_baseline_person_24)
                .into(holder.img)
        }
    }

    override fun getItemCount(): Int = results.size
}