package fi.joonaun.helsinkitour.ui.search.bottomsheet

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import fi.joonaun.helsinkitour.R
import fi.joonaun.helsinkitour.databinding.SingleImageBinding
import fi.joonaun.helsinkitour.network.Image

class ImageRecyclerViewAdapter : RecyclerView.Adapter<ImageRecyclerViewAdapter.ViewHolder>() {

    private val imageList: MutableList<Image> = mutableListOf()

    fun addImages(list: List<Image>) {
        val size = imageList.size
        imageList.addAll(list)
        notifyItemRangeChanged(size, list.size)
    }

    class ViewHolder private constructor(val binding: SingleImageBinding) :
        RecyclerView.ViewHolder(binding.root) {
        companion object {
            fun from(parent: ViewGroup): ViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val binding = SingleImageBinding.inflate(layoutInflater)
                return ViewHolder(binding)
            }
        }

        fun bind(image: Image) {
            binding.image = image
            binding.executePendingBindings()
        }
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder {
        return ViewHolder.from(parent)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val img = imageList[position]
        holder.bind(img)
    }

    override fun getItemCount(): Int = imageList.size
}