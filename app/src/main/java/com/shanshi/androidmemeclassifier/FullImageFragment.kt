package com.shanshi.androidmemeclassifier

import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.shanshi.androidmemeclassifier.databinding.FragmentFullImageBinding
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class FullImageFragment : Fragment() {

    private var _binding: FragmentFullImageBinding? = null
    private val binding get() = _binding!!

    private lateinit var executor: ExecutorService
    private val mainHandler = Handler(Looper.getMainLooper())

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentFullImageBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val imageUri = Uri.parse(requireArguments().getString(ARG_IMAGE_URI))
        val resolver = requireContext().contentResolver
        val screenWidth = resources.displayMetrics.widthPixels
        val screenHeight = resources.displayMetrics.heightPixels
        executor = Executors.newSingleThreadExecutor()
        executor.execute {
            val bitmap = BitmapUtils.decodeSampled(resolver, imageUri, screenWidth, screenHeight)
            mainHandler.post {
                val currentBinding = _binding ?: return@post
                currentBinding.progressLoading.visibility = View.GONE
                if (bitmap != null) {
                    currentBinding.imageFull.setImageBitmap(bitmap)
                } else {
                    currentBinding.textError.visibility = View.VISIBLE
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        executor.shutdownNow()
        _binding = null
    }

    companion object {
        const val ARG_IMAGE_URI = "imageUri"
    }
}
