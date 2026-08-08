package com.halashasneen.nova.ui.create

import android.content.ContentValues
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.google.android.material.chip.Chip
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.halashasneen.nova.QRProApplication
import com.halashasneen.nova.R
import com.halashasneen.nova.data.model.CornerShape
import com.halashasneen.nova.data.model.DotShape
import com.halashasneen.nova.data.model.QRCustomization
import com.halashasneen.nova.data.model.QRType
import com.halashasneen.nova.databinding.FragmentCreateBinding
import com.halashasneen.nova.ui.ViewModelFactory
import com.halashasneen.nova.util.toast
import java.io.File
import java.io.FileOutputStream

class CreateFragment : Fragment() {

    private var _binding: FragmentCreateBinding? = null
    private val binding get() = _binding!!

    private val viewModel: CreateViewModel by viewModels {
        ViewModelFactory(requireActivity().application as QRProApplication)
    }

    private var selectedType: QRType = QRType.WEBSITE
    private val fieldInputs = mutableMapOf<String, TextInputEditText>()
    private var selectedColor: Int = Color.BLACK
    private var lastGeneratedBitmap: Bitmap? = null
    private var lastGeneratedContent: String = ""

    private val presetColors = intArrayOf(
        Color.parseColor("#000000"),
        Color.parseColor("#6C5CE7"),
        Color.parseColor("#00D2B4"),
        Color.parseColor("#FF6B9D"),
        Color.parseColor("#2ECC71"),
        Color.parseColor("#E74C3C")
    )

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCreateBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupTypeChips()
        setupColorSwatches()
        renderFieldsFor(selectedType)

        binding.switchFrame.setOnCheckedChangeListener { _, isChecked ->
            binding.frameTextLayout.visibility = if (isChecked) View.VISIBLE else View.GONE
        }

        binding.btnGenerate.setOnClickListener { onGenerateClicked() }
        binding.btnSavePng.setOnClickListener { saveCurrentBitmapToGallery() }
        binding.btnSharePreview.setOnClickListener { shareCurrentBitmap() }
    }

    private fun setupTypeChips() {
        val types = listOf(
            QRType.WEBSITE to R.string.create_type_website,
            QRType.TEXT to R.string.create_type_text,
            QRType.PHONE to R.string.create_type_phone,
            QRType.EMAIL to R.string.create_type_email,
            QRType.SMS to R.string.create_type_sms,
            QRType.WIFI to R.string.create_type_wifi,
            QRType.CONTACT to R.string.create_type_contact,
            QRType.LOCATION to R.string.create_type_location,
            QRType.WHATSAPP to R.string.create_type_whatsapp,
            QRType.TELEGRAM to R.string.create_type_telegram,
            QRType.INSTAGRAM to R.string.create_type_instagram,
            QRType.FACEBOOK to R.string.create_type_facebook,
            QRType.YOUTUBE to R.string.create_type_youtube
        )

        types.forEachIndexed { index, (type, labelRes) ->
            val chip = Chip(requireContext()).apply {
                text = getString(labelRes)
                isCheckable = true
                isChecked = index == 0
                setOnClickListener {
                    selectedType = type
                    renderFieldsFor(type)
                    hidePreview()
                }
            }
            binding.typeChipGroup.addView(chip)
        }
    }

    private fun setupColorSwatches() {
        binding.colorSwatchContainer.removeAllViews()
        presetColors.forEachIndexed { index, color ->
            val swatch = View(requireContext()).apply {
                val size = (36 * resources.displayMetrics.density).toInt()
                layoutParams = ViewGroup.MarginLayoutParams(size, size).apply {
                    marginEnd = (8 * resources.displayMetrics.density).toInt()
                }
                setBackgroundColor(color)
                background = android.graphics.drawable.GradientDrawable().apply {
                    shape = android.graphics.drawable.GradientDrawable.OVAL
                    setColor(color)
                    if (index == 0) setStroke(3, Color.LTGRAY)
                }
                setOnClickListener { selectedColor = color }
            }
            binding.colorSwatchContainer.addView(swatch)
        }
        selectedColor = presetColors[0]
    }

    /** يعرض فقط الحقول اللازمة للنوع المحدد، ويخفي كل الحقول الأخرى */
    private fun renderFieldsFor(type: QRType) {
        binding.fieldsContainer.removeAllViews()
        fieldInputs.clear()

        when (type) {
            QRType.WEBSITE, QRType.FACEBOOK, QRType.YOUTUBE ->
                addField("url", getString(R.string.create_type_website))

            QRType.TEXT ->
                addField("text", getString(R.string.create_type_text))

            QRType.PHONE ->
                addField("phone", getString(R.string.create_type_phone))

            QRType.EMAIL -> {
                addField("email", getString(R.string.create_type_email))
                addField("subject", "Subject")
                addField("body", "Message")
            }

            QRType.SMS -> {
                addField("phone", getString(R.string.create_type_phone))
                addField("message", "Message")
            }

            QRType.WIFI -> {
                addField("ssid", "Wi-Fi SSID")
                addField("password", "Password")
            }

            QRType.CONTACT -> {
                addField("name", "Name")
                addField("phone", getString(R.string.create_type_phone))
                addField("email", getString(R.string.create_type_email))
                addField("company", "Company")
            }

            QRType.LOCATION -> {
                addField("lat", "Latitude")
                addField("lng", "Longitude")
            }

            QRType.WHATSAPP -> {
                addField("phone", getString(R.string.create_type_phone))
                addField("message", "Message")
            }

            QRType.TELEGRAM, QRType.INSTAGRAM ->
                addField("username", "Username")

            QRType.UNKNOWN -> addField("text", getString(R.string.create_type_text))
        }
    }

    private fun addField(key: String, hintText: String) {
        val inflater = LayoutInflater.from(requireContext())
        val layout = inflater.inflate(
            R.layout.item_text_field, binding.fieldsContainer, false
        ) as TextInputLayout
        layout.hint = hintText
        val editText = layout.editText as TextInputEditText
        fieldInputs[key] = editText
        binding.fieldsContainer.addView(layout)
    }

    private fun onGenerateClicked() {
        val fields = fieldInputs.mapValues { it.value.text?.toString().orEmpty() }
        val content = viewModel.buildContent(selectedType, fields)

        if (content.isBlank() || fields.values.all { it.isBlank() }) {
            requireContext().toast(getString(R.string.dialog_cancel))
            return
        }

        val customization = QRCustomization(
            foregroundColor = selectedColor,
            backgroundColor = Color.WHITE,
            dotShape = when (binding.dotShapeToggle.checkedButtonId) {
                R.id.dotRounded -> DotShape.ROUNDED
                R.id.dotCircle -> DotShape.CIRCLE
                else -> DotShape.SQUARE
            },
            cornerShape = when (binding.cornerShapeToggle.checkedButtonId) {
                R.id.cornerRounded -> CornerShape.ROUNDED
                R.id.cornerDots -> CornerShape.DOTS
                else -> CornerShape.SQUARE
            },
            hasFrame = binding.switchFrame.isChecked,
            frameText = binding.etFrameText.text?.toString()
        )

        val bitmap = QRCodeGenerator.generate(content, sizePx = 800, customization = customization)
        lastGeneratedBitmap = bitmap
        lastGeneratedContent = content
        binding.ivQrPreview.setImageBitmap(bitmap)
        binding.previewContainer.visibility = View.VISIBLE

        viewModel.saveGeneratedToHistory(content, selectedType, content.take(40))
    }

    private fun hidePreview() {
        binding.previewContainer.visibility = View.GONE
        lastGeneratedBitmap = null
    }

    private fun saveCurrentBitmapToGallery() {
        val bitmap = lastGeneratedBitmap ?: return
        try {
            val fileName = "QRPro_${System.currentTimeMillis()}.png"
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val values = ContentValues().apply {
                    put(MediaStore.Images.Media.DISPLAY_NAME, fileName)
                    put(MediaStore.Images.Media.MIME_TYPE, "image/png")
                    put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/QRPro")
                }
                val uri = requireContext().contentResolver.insert(
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values
                )
                uri?.let {
                    requireContext().contentResolver.openOutputStream(it)?.use { stream ->
                        bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
                    }
                }
            } else {
                @Suppress("DEPRECATION")
                val picturesDir = android.os.Environment.getExternalStoragePublicDirectory(
                    android.os.Environment.DIRECTORY_PICTURES
                )
                val qrDir = File(picturesDir, "QRPro").apply { mkdirs() }
                val file = File(qrDir, fileName)
                FileOutputStream(file).use { bitmap.compress(Bitmap.CompressFormat.PNG, 100, it) }
            }
            requireContext().toast(getString(R.string.action_save))
        } catch (e: Exception) {
            requireContext().toast(getString(R.string.dialog_cancel))
        }
    }

    private fun shareCurrentBitmap() {
        val bitmap = lastGeneratedBitmap ?: return
        try {
            val cacheFile = File(requireContext().cacheDir, "qr_share_temp.png")
            FileOutputStream(cacheFile).use { bitmap.compress(Bitmap.CompressFormat.PNG, 100, it) }
            val uri = FileProvider.getUriForFile(
                requireContext(),
                "${requireContext().packageName}.fileprovider",
                cacheFile
            )
            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "image/png"
                putExtra(Intent.EXTRA_STREAM, uri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            startActivity(Intent.createChooser(intent, null))
        } catch (e: Exception) {
            requireContext().toast(getString(R.string.dialog_cancel))
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
