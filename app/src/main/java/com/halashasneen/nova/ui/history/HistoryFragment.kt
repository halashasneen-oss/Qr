package com.halashasneen.nova.ui.history

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.core.content.FileProvider
import androidx.core.view.MenuProvider
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.halashasneen.nova.QRProApplication
import com.halashasneen.nova.R
import com.halashasneen.nova.data.model.QRHistoryItem
import com.halashasneen.nova.databinding.FragmentHistoryBinding
import com.halashasneen.nova.ui.ViewModelFactory
import com.halashasneen.nova.util.toast
import java.io.File

class HistoryFragment : Fragment() {

    private var _binding: FragmentHistoryBinding? = null
    private val binding get() = _binding!!

    private val viewModel: HistoryViewModel by viewModels {
        ViewModelFactory(requireActivity().application as QRProApplication)
    }

    private lateinit var adapter: HistoryAdapter
    private val selectedIds = mutableSetOf<String>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHistoryBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        adapter = HistoryAdapter(
            onClick = { item -> onItemClicked(item) },
            onFavoriteClick = { item -> viewModel.toggleFavorite(item.id) },
            onLongClick = { item -> onItemLongClicked(item) },
            isSelected = { id -> selectedIds.contains(id) }
        )
        binding.recyclerHistory.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerHistory.adapter = adapter

        binding.etSearch.doAfterTextChanged { viewModel.setQuery(it?.toString().orEmpty()) }

        binding.chipAll.setOnClickListener { viewModel.setFavoritesOnly(false) }
        binding.chipFavorites.setOnClickListener { viewModel.setFavoritesOnly(true) }

        viewModel.items.observe(viewLifecycleOwner) { items ->
            adapter.submitList(items)
            binding.tvEmpty.visibility = if (items.isEmpty()) View.VISIBLE else View.GONE
        }

        requireActivity().addMenuProvider(object : MenuProvider {
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                menuInflater.inflate(R.menu.history_selection_menu, menu)
            }

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                return when (menuItem.itemId) {
                    R.id.action_export -> { exportHistory(); true }
                    R.id.action_delete_selected -> { confirmDeleteSelected(); true }
                    R.id.action_clear_all -> { confirmClearAll(); true }
                    else -> false
                }
            }
        }, viewLifecycleOwner, Lifecycle.State.RESUMED)
    }

    private fun onItemClicked(item: QRHistoryItem) {
        if (selectedIds.isNotEmpty()) {
            toggleSelection(item.id)
            return
        }
        val bundle = Bundle().apply {
            putString("rawContent", item.rawContent)
            putBoolean("isGenerated", item.isGenerated)
        }
        findNavController().navigate(R.id.action_history_to_result, bundle)
    }

    private fun onItemLongClicked(item: QRHistoryItem): Boolean {
        toggleSelection(item.id)
        return true
    }

    private fun toggleSelection(id: String) {
        if (selectedIds.contains(id)) selectedIds.remove(id) else selectedIds.add(id)
        adapter.notifyDataSetChanged()
    }

    private fun confirmDeleteSelected() {
        if (selectedIds.isEmpty()) {
            requireContext().toast(getString(R.string.history_empty))
            return
        }
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.dialog_delete_confirm_title)
            .setMessage(R.string.dialog_delete_confirm_message)
            .setNegativeButton(R.string.dialog_cancel, null)
            .setPositiveButton(R.string.dialog_confirm) { _, _ ->
                viewModel.deleteMultiple(selectedIds.toSet())
                selectedIds.clear()
            }
            .show()
    }

    private fun confirmClearAll() {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.dialog_delete_confirm_title)
            .setMessage(R.string.dialog_delete_confirm_message)
            .setNegativeButton(R.string.dialog_cancel, null)
            .setPositiveButton(R.string.dialog_confirm) { _, _ ->
                viewModel.clearAll()
                selectedIds.clear()
            }
            .show()
    }

    private fun exportHistory() {
        val file = File(requireContext().cacheDir, "qrpro_history_export.json")
        viewModel.exportToFile(file) { success ->
            if (!success) {
                requireContext().toast(getString(R.string.dialog_cancel))
                return@exportToFile
            }
            val uri = FileProvider.getUriForFile(
                requireContext(), "${requireContext().packageName}.fileprovider", file
            )
            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "application/json"
                putExtra(Intent.EXTRA_STREAM, uri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            startActivity(Intent.createChooser(intent, null))
        }
    }

    override fun onResume() {
        super.onResume()
        viewModel.load()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
