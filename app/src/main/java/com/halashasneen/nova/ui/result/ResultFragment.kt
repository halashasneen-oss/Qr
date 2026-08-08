package com.halashasneen.nova.ui.result

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.halashasneen.nova.QRProApplication
import com.halashasneen.nova.R
import com.halashasneen.nova.data.model.QRHistoryItem
import com.halashasneen.nova.data.model.QRType
import com.halashasneen.nova.databinding.FragmentResultBinding
import com.halashasneen.nova.ui.ViewModelFactory
import com.halashasneen.nova.util.copyToClipboard
import com.halashasneen.nova.util.shareText
import com.halashasneen.nova.util.toast

class ResultFragment : Fragment() {

    private var _binding: FragmentResultBinding? = null
    private val binding get() = _binding!!

    private val viewModel: ResultViewModel by viewModels {
        ViewModelFactory(requireActivity().application as QRProApplication)
    }

    private var currentItemId: String = ""
    private var currentIsFavorite: Boolean = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentResultBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val rawContent = arguments?.getString("rawContent") ?: ""
        val isGenerated = arguments?.getBoolean("isGenerated") ?: false

        binding.btnBack.setOnClickListener { findNavController().popBackStack() }

        val parsed = viewModel.parse(rawContent)

        binding.tvTypeValue.text = typeLabel(parsed.type)
        binding.tvContentValue.text = rawContent

        if (parsed.siteName != null) {
            binding.rowSiteName.visibility = View.VISIBLE
            binding.tvSiteNameValue.text = parsed.siteName
        } else {
            binding.rowSiteName.visibility = View.GONE
        }

        if (parsed.isSecure != null) {
            binding.securityBadge.visibility = View.VISIBLE
            if (parsed.isSecure) {
                binding.securityBadge.setBackgroundResource(R.drawable.bg_https_badge)
                binding.tvSecurityText.text = getString(R.string.result_secure)
                binding.tvSecurityText.setTextColor(resources.getColor(R.color.secure_https, null))
            } else {
                binding.securityBadge.setBackgroundResource(R.drawable.bg_http_badge)
                binding.tvSecurityText.text = getString(R.string.result_insecure)
                binding.tvSecurityText.setTextColor(resources.getColor(R.color.insecure_http, null))
            }
        } else {
            binding.securityBadge.visibility = View.GONE
        }

        setupQuickActions(parsed.type, rawContent)

        binding.btnCopy.setOnClickListener {
            requireContext().copyToClipboard("qr_content", rawContent)
            requireContext().toast(getString(R.string.copied_toast))
        }
        binding.btnShare.setOnClickListener {
            requireContext().shareText(rawContent)
        }
        binding.btnFavorite.setOnClickListener {
            currentIsFavorite = !currentIsFavorite
            updateFavoriteIcon()
            if (currentItemId.isNotEmpty()) viewModel.toggleFavorite(currentItemId)
        }

        // حفظ تلقائي في السجل عند فتح شاشة النتيجة لأول مرة فقط (حسب إعداد المستخدم)،
        // مع تجنب التكرار إذا أعيد إنشاء الشاشة (مثل تدوير الجهاز)
        currentItemId = savedInstanceState?.getString(KEY_ITEM_ID)
            ?: java.util.UUID.randomUUID().toString()
        if (savedInstanceState == null) {
            val historyItem = QRHistoryItem(
                id = currentItemId,
                rawContent = rawContent,
                type = parsed.type,
                displayTitle = parsed.displayTitle,
                isGenerated = isGenerated,
                isSecure = parsed.isSecure
            )
            viewModel.saveToHistoryIfEnabled(historyItem)
        }
    }

    private fun updateFavoriteIcon() {
        binding.btnFavorite.setImageResource(
            if (currentIsFavorite) R.drawable.ic_favorite_filled else R.drawable.ic_favorite_border
        )
    }

    private fun typeLabel(type: QRType): String = when (type) {
        QRType.WEBSITE -> getString(R.string.create_type_website)
        QRType.TEXT -> getString(R.string.create_type_text)
        QRType.PHONE -> getString(R.string.create_type_phone)
        QRType.EMAIL -> getString(R.string.create_type_email)
        QRType.SMS -> getString(R.string.create_type_sms)
        QRType.WIFI -> getString(R.string.create_type_wifi)
        QRType.CONTACT -> getString(R.string.create_type_contact)
        QRType.LOCATION -> getString(R.string.create_type_location)
        QRType.WHATSAPP -> getString(R.string.create_type_whatsapp)
        QRType.TELEGRAM -> getString(R.string.create_type_telegram)
        QRType.INSTAGRAM -> getString(R.string.create_type_instagram)
        QRType.FACEBOOK -> getString(R.string.create_type_facebook)
        QRType.YOUTUBE -> getString(R.string.create_type_youtube)
        QRType.UNKNOWN -> getString(R.string.create_type_text)
    }

    /** الإجراءات السريعة تتغير حسب نوع المحتوى، دون فتح الرابط تلقائيًا أبدًا */
    private fun setupQuickActions(type: QRType, raw: String) {
        binding.quickActionsContainer.removeAllViews()

        when (type) {
            QRType.WEBSITE, QRType.WHATSAPP, QRType.TELEGRAM,
            QRType.INSTAGRAM, QRType.FACEBOOK, QRType.YOUTUBE -> {
                addQuickAction(getString(R.string.action_open)) {
                    openUri(raw)
                }
            }
            QRType.PHONE -> {
                val number = raw.removePrefix("tel:")
                addQuickAction(getString(R.string.action_call)) {
                    openUri("tel:$number")
                }
            }
            QRType.EMAIL -> {
                val email = raw.removePrefix("mailto:").substringBefore("?")
                addQuickAction(getString(R.string.action_send_email)) {
                    openUri("mailto:$email")
                }
            }
            QRType.LOCATION -> {
                addQuickAction(getString(R.string.action_open_maps)) {
                    openUri(raw)
                }
            }
            else -> { /* لا حاجة لإجراء سريع إضافي لباقي الأنواع (Text, SMS, WiFi, Contact) */ }
        }
    }

    private fun addQuickAction(label: String, onClick: () -> Unit) {
        val button = com.google.android.material.button.MaterialButton(requireContext()).apply {
            text = label
            setOnClickListener { onClick() }
        }
        binding.quickActionsContainer.addView(button)
    }

    private fun openUri(uri: String) {
        try {
            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(uri)))
        } catch (e: Exception) {
            requireContext().toast(getString(R.string.dialog_cancel))
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString(KEY_ITEM_ID, currentItemId)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        private const val KEY_ITEM_ID = "current_item_id"
    }
}
