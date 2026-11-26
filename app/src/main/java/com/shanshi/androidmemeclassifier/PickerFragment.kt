package com.shanshi.androidmemeclassifier

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.shanshi.androidmemeclassifier.databinding.FragmentPickerBinding

class PickerFragment : Fragment() {

    private var _binding: FragmentPickerBinding? = null
    private val binding get() = _binding!!

    private val openDocumentTree =
        registerForActivityResult(ActivityResultContracts.OpenDocumentTree()) { uri ->
            if (uri != null) {
                try {
                    requireContext().contentResolver.takePersistableUriPermission(
                        uri, Intent.FLAG_GRANT_READ_URI_PERMISSION
                    )
                } catch (ignored: SecurityException) {
                }
                findNavController().navigate(
                    R.id.action_PickerFragment_to_GalleryFragment,
                    Bundle().apply { putString(GalleryFragment.ARG_TREE_URI, uri.toString()) }
                )
            }
        }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPickerBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.buttonPickDirectory.setOnClickListener {
            openDocumentTree.launch(null)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
