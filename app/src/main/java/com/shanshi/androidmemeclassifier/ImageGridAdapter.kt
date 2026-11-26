package com.shanshi.androidmemeclassifier

import android.content.ContentResolver
import android.graphics.Bitmap
import android.net.Uri
import android.os.Handler
import android.util.LruCache
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.shanshi.androidmemeclassifier.databinding.ItemImageBinding
import java.util.concurrent.ExecutorService

class ImageGridAdapter(
    private val resolver: ContentResolver,
    private val executor: ExecutorService,
    private val mainHandler: Handler,
    private val onImageClick: (Uri) -> Unit
) : RecyclerView.Adapter<ImageGridAdapter.ImageViewHolder>() {

    private val uris = mutableListOf<Uri>()

    private val cache = object : LruCache<String, Bitmap>((Runtime.getRuntime().maxMemory() / 1024L / 8L).toInt()) {
        override fun sizeOf(key: String, value: Bitmap): Int = value.byteCount / 1024
    }

    class ImageViewHolder(val binding: ItemImageBinding) : RecyclerView.ViewHolder(binding.root)

    fun submitList(items: List<Uri>) {
        uris.clear()
        uris.addAll(items)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ImageViewHolder {
        return ImageViewHolder(ItemImageBinding.inflate(LayoutInflater.from(parent.context), parent, false))
    }

    override fun getItemCount(): Int = uris.size

    override fun onBindViewHolder(holder: ImageViewHolder, position: Int) {
        val uri = uris[position]
        val imageView = holder.binding.imageThumbnail
        imageView.tag = uri
        imageView.setImageDrawable(null)
        holder.itemView.setOnClickListener { onImageClick(uri) }
        val cached = cache.get(uri.toString())
        if (cached != null) {
            imageView.setImageBitmap(cached)
            return
        }
        executor.execute {
            val bitmap = BitmapUtils.decodeSampled(resolver, uri, THUMBNAIL_SIZE, THUMBNAIL_SIZE) ?: return@execute
            cache.put(uri.toString(), bitmap)
            mainHandler.post {
                if (imageView.tag == uri) {
                    imageView.setImageBitmap(bitmap)
                }
            }
        }
    }

    companion object {
        private const val THUMBNAIL_SIZE = 256
    }
}
