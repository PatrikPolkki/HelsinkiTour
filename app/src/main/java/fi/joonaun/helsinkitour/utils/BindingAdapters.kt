package fi.joonaun.helsinkitour.utils

import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.databinding.BindingAdapter
import androidx.recyclerview.widget.RecyclerView
import com.squareup.picasso.Picasso
import fi.joonaun.helsinkitour.R
import fi.joonaun.helsinkitour.network.Description
import fi.joonaun.helsinkitour.network.Image
import fi.joonaun.helsinkitour.network.Name
import fi.joonaun.helsinkitour.ui.search.bottomsheet.ImageRecyclerViewAdapter

@BindingAdapter("recyclerViewImages")
fun bindShowImages(view: View, images: List<Image>?) {
    if (images == null || images.isEmpty()) {
        view.visibility = View.GONE
        return
    }

    if (view is RecyclerView) {
        view.adapter = ImageRecyclerViewAdapter()
        val adapter = (view.adapter as ImageRecyclerViewAdapter)
        adapter.addImages(images)
    }
}

@BindingAdapter(value = ["title", "locale"], requireAll = false)
fun bindShowLocaledTitle(view: TextView, name: Name, locale: String?) {
    view.text = if (locale == "fi") name.fi else name.en
}

@BindingAdapter("imageIfExist")
fun bindShowImageOrPlaceholder(view: ImageView, images: List<Image>?) {
    if (images == null || images.isEmpty()) {
        view.setImageDrawable(
            ContextCompat.getDrawable(
                view.context,
                R.drawable.ic_baseline_image_not_supported_24
            )
        )
    } else {
        val img = images.first()
        loadSmallImage(view, img.url)
    }
}

@BindingAdapter("imageUrl")
fun bindShowImage(view: ImageView, url: String) {
    loadSmallImage(view, url)
}

@BindingAdapter("shortDescription")
fun bindShortDescription(view: TextView, desc: Description) {
    view.text = desc.intro ?: desc.body
}

private fun loadSmallImage(view: ImageView, url: String) {
    Picasso.get().load(url)
        .resize(300, 300)
        .centerCrop()
        .placeholder(R.drawable.ic_baseline_downloading_24)
        .error(R.drawable.ic_baseline_error_outline_24)
        .into(view)
}