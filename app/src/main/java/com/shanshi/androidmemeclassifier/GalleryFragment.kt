package com.shanshi.androidmemeclassifier

import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.documentfile.provider.DocumentFile
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import com.shanshi.androidmemeclassifier.databinding.FragmentGalleryBinding
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class GalleryFragment : Fragment() {

    private var _binding: FragmentGalleryBinding? = null
    private val binding get() = _binding!!

    private lateinit var executor: ExecutorService
    private val mainHandler = Handler(Looper.getMainLooper())

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentGalleryBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val treeUri = Uri.parse(requireArguments().getString(ARG_TREE_URI))
        val appContext = requireContext().applicationContext
        executor = Executors.newFixedThreadPool(THREAD_COUNT)

        val adapter = ImageGridAdapter(appContext.contentResolver, executor, mainHandler) { uri ->
            findNavController().navigate(
                R.id.action_GalleryFragment_to_FullImageFragment,
                Bundle().apply { putString(FullImageFragment.ARG_IMAGE_URI, uri.toString()) }
            )
        }
        binding.recyclerImages.layoutManager = GridLayoutManager(requireContext(), SPAN_COUNT)
        binding.recyclerImages.adapter = adapter
        binding.progressLoading.visibility = View.VISIBLE
        binding.textEmpty.visibility = View.GONE

        executor.execute {
            val imageUris = DocumentFile.fromTreeUri(appContext, treeUri)
                ?.listFiles()
                ?.filter { it.isFile && it.type?.startsWith("image/") == true }
                ?.sortedBy { it.name.orEmpty() }
                ?.map { it.uri }
                .orEmpty()
            mainHandler.post {
                val currentBinding = _binding ?: return@post
                currentBinding.progressLoading.visibility = View.GONE
                if (imageUris.isEmpty()) {
                    currentBinding.textEmpty.visibility = View.VISIBLE
                } else {
                    adapter.submitList(imageUris)
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
        const val ARG_TREE_URI = "treeUri"
        private const val SPAN_COUNT = 5
        private const val THREAD_COUNT = 3
    }
}
