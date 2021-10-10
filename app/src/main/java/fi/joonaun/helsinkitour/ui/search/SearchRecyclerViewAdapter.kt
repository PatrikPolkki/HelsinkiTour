package fi.joonaun.helsinkitour.ui.search

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import fi.joonaun.helsinkitour.databinding.SingleSearchItemBinding
import fi.joonaun.helsinkitour.network.Helsinki

class SearchRecyclerViewAdapter(private val cellClickListener: CellClickListener) :
    RecyclerView.Adapter<SearchRecyclerViewAdapter.ViewHolder>() {

    /**
     * List for adapter items
     */
    private val results: MutableList<Helsinki> = mutableListOf()

    /**
     * Add items to [results] list and notifies adapter of changes
     */
    fun addItems(items: List<Helsinki>) {
        val startPos = results.size
        results.addAll(items)
        notifyItemRangeInserted(startPos, items.size)
    }

    /**
     * Clears [results] list and notifies adapter of changes
     */
    fun clearItems() {
        val size = results.size
        results.clear()
        notifyItemRangeRemoved(0, size)
    }

    class ViewHolder(private val binding: SingleSearchItemBinding) :
        RecyclerView.ViewHolder(binding.root) {
        companion object {
            /**
             * Fills layout with [SingleSearchItemBinding]
             */
            fun from(parent: ViewGroup): ViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val binding = SingleSearchItemBinding.inflate(layoutInflater, parent, false)
                return ViewHolder(binding)
            }
        }

        /**
         * Binds [item] to binding
         */
        fun bind(item: Helsinki) {
            binding.helsinkiItem = item
            binding.executePendingBindings()
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder.from(parent)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = results[position]
        holder.bind(item)

        holder.itemView.setOnClickListener {
            cellClickListener.onCellClickListener(item.id, item.sourceType.id)
        }
    }

    override fun getItemCount(): Int = results.size
}