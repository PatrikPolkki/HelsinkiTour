package fi.joonaun.helsinkitour.ui.search

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.squareup.picasso.Picasso
import fi.joonaun.helsinkitour.R
import fi.joonaun.helsinkitour.network.Activity
import fi.joonaun.helsinkitour.network.Helsinki

class SearchRecyclerViewAdapter : RecyclerView.Adapter<SearchRecyclerViewAdapter.ViewHolder>() {

    private val results: MutableList<Helsinki> = mutableListOf()

    fun addItem(item: Helsinki) {
        results.add(item)
        notifyItemInserted(results.size)
    }

    fun addItems(items: List<Helsinki>) {
        val startPos = results.size
        results.addAll(items)
        notifyItemRangeInserted(startPos, items.size)
    }

    fun clearItems() {
        val size = results.size
        results.clear()
        notifyItemRangeRemoved(0, size)
    }

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
            txtDescription.text = item.description.intro ?: item.description.body
        }

        val image = item.description.images?.firstOrNull()
        if (image != null) {
            Picasso.get().load(image.url)
                .fit()
                .placeholder(R.drawable.ic_baseline_downloading_24)
                .error(R.drawable.ic_baseline_error_outline_24)
                .into(holder.img)
        } else {
            holder.img.setImageDrawable(
                ContextCompat.getDrawable(holder.itemView.context,
                    R.drawable.ic_baseline_error_outline_24)
            )
        }
    }

    override fun getItemCount(): Int = results.size
}