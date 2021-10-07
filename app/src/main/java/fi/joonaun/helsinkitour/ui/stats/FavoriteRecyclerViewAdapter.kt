package fi.joonaun.helsinkitour.ui.stats

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import fi.joonaun.helsinkitour.database.Favorite
import fi.joonaun.helsinkitour.databinding.SingleFavoriteItemBinding
import fi.joonaun.helsinkitour.databinding.SingleSearchItemBinding
import fi.joonaun.helsinkitour.network.Helsinki
import fi.joonaun.helsinkitour.ui.search.CellClickListener
import fi.joonaun.helsinkitour.utils.HelsinkiType

class FavoriteRecyclerViewAdapter(private val cellClickListener: CellClickListener) :
    RecyclerView.Adapter<FavoriteRecyclerViewAdapter.ViewHolder>() {

    private val results: MutableList<Favorite> = mutableListOf()

    fun addItems(items: List<Favorite>) {
        val startPos = results.size
        results.addAll(items)
        notifyItemRangeInserted(startPos, items.size)
    }

    fun clearItems() {
        val size = results.size
        results.clear()
        notifyItemRangeRemoved(0, size)
    }

    class ViewHolder(private val binding: SingleFavoriteItemBinding) :
        RecyclerView.ViewHolder(binding.root) {
        companion object {
            fun from(parent: ViewGroup): ViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val binding = SingleFavoriteItemBinding.inflate(layoutInflater, parent, false)
                return ViewHolder(binding)
            }
        }

        fun bind(item: Favorite) {
            binding.favoriteItem = item
            binding.executePendingBindings()
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder.from(parent)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = results[position]
        holder.bind(item)

        val type = when(item.type) {
            HelsinkiType.EVENT -> 1
            HelsinkiType.PLACE -> 2
            HelsinkiType.ACTIVITY -> 3
        }

        holder.itemView.setOnClickListener {
            cellClickListener.onCellClickListener(item.id, type)
        }
    }

    override fun getItemCount(): Int = results.size
}