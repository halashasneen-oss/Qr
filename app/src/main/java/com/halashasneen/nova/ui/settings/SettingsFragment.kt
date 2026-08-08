package com.halashasneen.nova.ui.settings

import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.halashasneen.nova.QRProApplication
import com.halashasneen.nova.R
import com.halashasneen.nova.databinding.FragmentSettingsBinding
import com.halashasneen.nova.ui.ViewModelFactory
import com.halashasneen.nova.util.toast
import java.io.File

class SettingsFragment : Fragment() {

    private var _binding: FragmentSettingsBinding? = null
    private val binding get() = _binding!!

    private val viewModel: SettingsViewModel by viewModels {
        ViewModelFactory(requireActivity().application as QRProApplication)
    }

    private val createBackupLauncher = registerForActivityResult(
        ActivityResultContracts.CreateDocument("application/json")
    ) { uri -> uri?.let { performBackup(it) } }

    private val openRestoreLauncher = registerForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri -> uri?.let { performRestore(it) } }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSettingsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.btnBack.setOnClickListener { findNavController().popBackStack() }

        try {
            setupSettingsUi()
        } catch (e: Exception) {
            // Defensive: never let a settings-screen error crash the whole app — degrade
            // gracefully instead, since this screen is reachable from the home button.
            requireContext().toast(getString(R.string.dialog_cancel))
        }
    }

    private fun setupSettingsUi() {
        val settings = viewModel.getSettings()

        when (settings.language) {
            "en" -> binding.chipLangEn.isChecked = true
            "fr" -> binding.chipLangFr.isChecked = true
            "es" -> binding.chipLangEs.isChecked = true
            else -> binding.chipLangAr.isChecked = true
        }
        when (settings.darkMode) {
            "light" -> binding.chipModeLight.isChecked = true
            "dark" -> binding.chipModeDark.isChecked = true
            else -> binding.chipModeSystem.isChecked = true
        }

        binding.switchVibration.isChecked = settings.vibrationEnabled
        binding.switchScanSound.isChecked = settings.scanSoundEnabled
        binding.switchAutoCopy.isChecked = settings.autoCopyEnabled
        binding.switchSaveHistory.isChecked = settings.saveHistoryEnabled
        binding.switchPrivacyMode.isChecked = settings.privacyModeEnabled
        binding.switchVaultBiometric.isChecked = settings.vaultBiometricEnabled

        binding.chipLangAr.setOnClickListener { applyLanguage("ar") }
        binding.chipLangEn.setOnClickListener { applyLanguage("en") }
        binding.chipLangFr.setOnClickListener { applyLanguage("fr") }
        binding.chipLangEs.setOnClickListener { applyLanguage("es") }

        binding.chipModeSystem.setOnClickListener { applyDarkMode("system") }
        binding.chipModeLight.setOnClickListener { applyDarkMode("light") }
        binding.chipModeDark.setOnClickListener { applyDarkMode("dark") }

        binding.switchVibration.setOnCheckedChangeListener { _, checked ->
            viewModel.update { it.copy(vibrationEnabled = checked) }
        }
        binding.switchScanSound.setOnCheckedChangeListener { _, checked ->
            viewModel.update { it.copy(scanSoundEnabled = checked) }
        }
        binding.switchAutoCopy.setOnCheckedChangeListener { _, checked ->
            viewModel.update { it.copy(autoCopyEnabled = checked) }
        }
        binding.switchSaveHistory.setOnCheckedChangeListener { _, checked ->
            viewModel.update { it.copy(saveHistoryEnabled = checked) }
        }
        binding.switchPrivacyMode.setOnCheckedChangeListener { _, checked ->
            viewModel.update { it.copy(privacyModeEnabled = checked) }
        }
        binding.switchVaultBiometric.setOnCheckedChangeListener { _, checked ->
            viewModel.update { it.copy(vaultBiometricEnabled = checked) }
        }

        binding.btnBackup.setOnClickListener {
            createBackupLauncher.launch("qrpro_backup_${System.currentTimeMillis()}.json")
        }
        binding.btnRestore.setOnClickListener {
            openRestoreLauncher.launch(arrayOf("application/json"))
        }
    }

    private fun applyLanguage(code: String) {
        viewModel.update { it.copy(language = code) }
        val localeList = LocaleListCompat.forLanguageTags(code)
        AppCompatDelegate.setApplicationLocales(localeList)
    }

    private fun applyDarkMode(mode: String) {
        viewModel.update { it.copy(darkMode = mode) }
        val nightMode = when (mode) {
            "light" -> AppCompatDelegate.MODE_NIGHT_NO
            "dark" -> AppCompatDelegate.MODE_NIGHT_YES
            else -> AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
        }
        AppCompatDelegate.setDefaultNightMode(nightMode)
    }

    private fun performBackup(uri: Uri) {
        val tempFile = File(requireContext().cacheDir, "backup_temp.json")
        viewModel.backupToFile(tempFile) { success ->
            if (!success) {
                requireContext().toast(getString(R.string.dialog_cancel))
                return@backupToFile
            }
            requireContext().contentResolver.openOutputStream(uri)?.use { out ->
                tempFile.inputStream().use { it.copyTo(out) }
            }
            requireContext().toast(getString(R.string.settings_backup))
        }
    }

    private fun performRestore(uri: Uri) {
        val tempFile = File(requireContext().cacheDir, "restore_temp.json")
        requireContext().contentResolver.openInputStream(uri)?.use { input ->
            tempFile.outputStream().use { input.copyTo(it) }
        }
        viewModel.restoreFromFile(tempFile) { success ->
            requireContext().toast(
                getString(if (success) R.string.settings_restore else R.string.dialog_cancel)
            )
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
