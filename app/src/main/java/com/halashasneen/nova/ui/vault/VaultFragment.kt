package com.halashasneen.nova.ui.vault

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.halashasneen.nova.QRProApplication
import com.halashasneen.nova.R
import com.halashasneen.nova.data.model.QRType
import com.halashasneen.nova.data.model.VaultItem
import com.halashasneen.nova.databinding.DialogAddVaultItemBinding
import com.halashasneen.nova.databinding.DialogSetPinBinding
import com.halashasneen.nova.databinding.FragmentVaultBinding
import com.halashasneen.nova.ui.ViewModelFactory
import com.halashasneen.nova.util.toast

class VaultFragment : Fragment() {

    private var _binding: FragmentVaultBinding? = null
    private val binding get() = _binding!!

    private val viewModel: VaultViewModel by viewModels {
        ViewModelFactory(requireActivity().application as QRProApplication)
    }

    private lateinit var adapter: VaultAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentVaultBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        adapter = VaultAdapter(
            onClick = { item -> onVaultItemClicked(item) },
            onDelete = { item -> confirmDelete(item) }
        )
        binding.recyclerVault.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerVault.adapter = adapter

        binding.btnAddVaultItem.setOnClickListener { showAddItemDialog() }
        binding.btnUnlockPin.setOnClickListener { attemptUnlockWithPin() }
        binding.btnUnlockFingerprint.setOnClickListener { attemptBiometricUnlock() }

        viewModel.items.observe(viewLifecycleOwner) { items ->
            adapter.submitList(items)
            binding.tvVaultEmpty.visibility = if (items.isEmpty()) View.VISIBLE else View.GONE
        }

        refreshLockState()
    }

    private fun refreshLockState() {
        if (viewModel.isUnlocked) {
            showUnlocked()
            return
        }
        if (!viewModel.hasPin()) {
            // أول استخدام: نطلب من المستخدم تعيين كلمة مرور الخزنة
            showSetPinDialog()
        } else {
            showLocked()
            binding.btnUnlockFingerprint.visibility =
                if (viewModel.isBiometricEnabled() && canUseBiometrics()) View.VISIBLE else View.GONE
        }
    }

    private fun showLocked() {
        binding.lockedContainer.visibility = View.VISIBLE
        binding.unlockedContainer.visibility = View.GONE
    }

    private fun showUnlocked() {
        binding.lockedContainer.visibility = View.GONE
        binding.unlockedContainer.visibility = View.VISIBLE
        viewModel.load()
    }

    private fun showSetPinDialog() {
        val dialogBinding = DialogSetPinBinding.inflate(layoutInflater)
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.vault_set_pin_title)
            .setView(dialogBinding.root)
            .setCancelable(false)
            .setPositiveButton(R.string.dialog_confirm) { _, _ ->
                val pin = dialogBinding.etNewPin.text?.toString().orEmpty()
                if (pin.length >= 4) {
                    viewModel.setPin(pin)
                    viewModel.isUnlocked = true
                    showUnlocked()
                } else {
                    requireContext().toast(getString(R.string.vault_unlock_pin))
                    showSetPinDialog()
                }
            }
            .show()
    }

    private fun attemptUnlockWithPin() {
        val pin = binding.etPin.text?.toString().orEmpty()
        if (viewModel.verifyPin(pin)) {
            viewModel.isUnlocked = true
            showUnlocked()
        } else {
            requireContext().toast(getString(R.string.dialog_cancel))
        }
    }

    private fun canUseBiometrics(): Boolean {
        val manager = BiometricManager.from(requireContext())
        return manager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_WEAK) ==
            BiometricManager.BIOMETRIC_SUCCESS
    }

    private fun attemptBiometricUnlock() {
        val executor = ContextCompat.getMainExecutor(requireContext())
        val prompt = BiometricPrompt(
            this, executor,
            object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                    viewModel.isUnlocked = true
                    showUnlocked()
                }
            }
        )
        val info = BiometricPrompt.PromptInfo.Builder()
            .setTitle(getString(R.string.vault_unlock_fingerprint))
            .setNegativeButtonText(getString(R.string.dialog_cancel))
            .build()
        prompt.authenticate(info)
    }

    private fun showAddItemDialog() {
        val dialogBinding = DialogAddVaultItemBinding.inflate(layoutInflater)
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.vault_add_item)
            .setView(dialogBinding.root)
            .setNegativeButton(R.string.dialog_cancel, null)
            .setPositiveButton(R.string.dialog_confirm) { _, _ ->
                val title = dialogBinding.etVaultTitle.text?.toString().orEmpty()
                val content = dialogBinding.etVaultContent.text?.toString().orEmpty()
                val note = dialogBinding.etVaultNote.text?.toString().orEmpty()
                if (title.isNotBlank() && content.isNotBlank()) {
                    viewModel.addItem(title, QRType.TEXT, content, note)
                }
            }
            .show()
    }

    private fun onVaultItemClicked(item: VaultItem) {
        val bundle = Bundle().apply {
            putString("rawContent", item.content)
            putBoolean("isGenerated", false)
        }
        findNavController().navigate(R.id.action_vault_to_result, bundle)
    }

    private fun confirmDelete(item: VaultItem) {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.dialog_delete_confirm_title)
            .setMessage(R.string.dialog_delete_confirm_message)
            .setNegativeButton(R.string.dialog_cancel, null)
            .setPositiveButton(R.string.dialog_confirm) { _, _ -> viewModel.deleteItem(item.id) }
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
