package com.app.dementiaguard.Fragment

import android.app.Activity
import android.app.DatePickerDialog
import android.app.ProgressDialog
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.text.TextUtils
import android.util.Base64
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.view.marginBottom
import androidx.fragment.app.Fragment
import com.app.dementiaguard.R
import com.app.dementiaguard.Utils.FormCompletionManager
import com.app.dementiaguard.Api.ImageExtractionApiService
import com.app.dementiaguard.Api.RetrofitClient
import com.app.dementiaguard.Model.CreateImageRequest
import com.app.dementiaguard.Model.CreateImageResponse
import com.app.dementiaguard.Model.ImageExtractionRequest
import com.app.dementiaguard.Model.ImageExtractionResponse
import com.google.android.material.button.MaterialButton
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.ByteArrayOutputStream
import java.io.InputStream
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class ImagesFragment : Fragment() {
    
    private lateinit var etImageTitle: EditText
    private lateinit var etDescription: EditText
    private lateinit var etDate: EditText
    private lateinit var etWhere: EditText
    private lateinit var imagePreview: ImageView
    private lateinit var btnSelectImage: Button
    private lateinit var btnSubmit: MaterialButton
    
    private lateinit var tvImageTitle: TextView
    private lateinit var tvDescription: TextView
    private lateinit var tvWho: TextView
    private lateinit var tvWhen: TextView
    private lateinit var tvWhere: TextView
    private lateinit var objectsContainer: LinearLayout
    
    private var selectedImageUri: Uri? = null
    private val PICK_IMAGE_REQUEST = 1
    private val apiService = RetrofitClient.instance.create(ImageExtractionApiService::class.java)
    private lateinit var progressDialog: ProgressDialog
    private val objectEditTexts = mutableMapOf<String, EditText>()
    private var base64Image = ""
    private val calendar = Calendar.getInstance()
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    private lateinit var formCompletionManager: FormCompletionManager
    
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_images, container, false)
        
        etImageTitle = view.findViewById(R.id.etImageTitle)
        etDescription = view.findViewById(R.id.etDescription)
        etDate = view.findViewById(R.id.etDate)
        etWhere = view.findViewById(R.id.etWhere)
        imagePreview = view.findViewById(R.id.imagePreview)
        btnSelectImage = view.findViewById(R.id.btnSelectImage)
        btnSubmit = view.findViewById(R.id.btnSubmit)
        
        tvImageTitle = view.findViewById(R.id.tvImageTitle)
        tvDescription = view.findViewById(R.id.tvDescription)
        tvWho = view.findViewById(R.id.tvWho)
        tvWhen = view.findViewById(R.id.tvWhen)
        tvWhere = view.findViewById(R.id.tvWhere)
        objectsContainer = view.findViewById(R.id.objectsContainer)
        
        formCompletionManager = FormCompletionManager.getInstance(requireContext())
        
        progressDialog = ProgressDialog(context)
        progressDialog.setMessage("Processing...")
        progressDialog.setCancelable(false)
        
        setupDatePicker()
        
        btnSelectImage.setOnClickListener {
            openGallery()
        }
        
        btnSubmit.setOnClickListener {
            if (validateInputs()) {
                uploadImageData()
            }
        }
        
        return view
    }
    
    private fun setupDatePicker() {
        val dateSetListener = DatePickerDialog.OnDateSetListener { _, year, month, dayOfMonth ->
            calendar.set(Calendar.YEAR, year)
            calendar.set(Calendar.MONTH, month)
            calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)
            updateDateInView()
        }
        
        etDate.setOnClickListener {
            context?.let {
                DatePickerDialog(
                    it,
                    dateSetListener,
                    calendar.get(Calendar.YEAR),
                    calendar.get(Calendar.MONTH),
                    calendar.get(Calendar.DAY_OF_MONTH)
                ).show()
            }
        }
        
        etDate.isFocusable = false
        etDate.isClickable = true
    }
    
    private fun updateDateInView() {
        etDate.setText(dateFormat.format(calendar.time))
    }
    
    private fun openGallery() {
        try {
            val galleryIntent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            startActivityForResult(galleryIntent, PICK_IMAGE_REQUEST)
        } catch (e: Exception) {
            Log.e("ImagesFragment", "Error opening gallery: ${e.message}")
            Toast.makeText(context, "Error opening gallery", Toast.LENGTH_SHORT).show()
        }
    }
    
    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK && data != null) {
            try {
                selectedImageUri = data.data
                Log.d("ImagesFragment", "Selected image URI: $selectedImageUri")
                
                if (selectedImageUri != null) {
                    imagePreview.setImageURI(null)
                    imagePreview.setImageURI(selectedImageUri)
                    
                    clearObjectFields()
                    extractImageData()
                    
                    Toast.makeText(context, "Image selected successfully", Toast.LENGTH_SHORT).show()
                } else {
                    Log.e("ImagesFragment", "Selected URI is null")
                }
            } catch (e: Exception) {
                Log.e("ImagesFragment", "Error setting image: ${e.message}")
                e.printStackTrace()
                Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        } else {
            Log.d("ImagesFragment", "Image selection cancelled or failed")
        }
    }
    
    private fun clearObjectFields() {
        objectsContainer.removeAllViews()
        objectEditTexts.clear()
    }
    
    private fun extractImageData() {
        progressDialog.show()
        
        try {
            base64Image = encodeImageToBase64()
            if (base64Image.isNotEmpty()) {
                val request = ImageExtractionRequest(base64Image)
                
                apiService.extractImageData(request).enqueue(object : Callback<ImageExtractionResponse> {
                    override fun onResponse(
                        call: Call<ImageExtractionResponse>,
                        response: Response<ImageExtractionResponse>
                    ) {
                        progressDialog.dismiss()
                        
                        if (response.isSuccessful && response.body() != null) {
                            val extractionResponse = response.body()!!
                            updateLabelsWithExtractionData(extractionResponse)
                            createDynamicObjectFields(extractionResponse.objects)
                        } else {
                            Toast.makeText(
                                context,
                                "Failed to analyze image: ${response.code()}",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                    
                    override fun onFailure(call: Call<ImageExtractionResponse>, t: Throwable) {
                        progressDialog.dismiss()
                        Toast.makeText(
                            context,
                            "Network error: ${t.message}",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                })
            } else {
                progressDialog.dismiss()
                Toast.makeText(context, "Failed to encode image", Toast.LENGTH_SHORT).show()
            }
        } catch (e: Exception) {
            progressDialog.dismiss()
            Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }
    
    private fun encodeImageToBase64(): String {
        try {
            val inputStream = context?.contentResolver?.openInputStream(selectedImageUri!!)
            val bitmap = BitmapFactory.decodeStream(inputStream)
            inputStream?.close()
            
            val outputStream = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.JPEG, 70, outputStream)
            val byteArray = outputStream.toByteArray()
            
            return Base64.encodeToString(byteArray, Base64.DEFAULT)
        } catch (e: Exception) {
            Log.e("ImagesFragment", "Error encoding image: ${e.message}")
            return ""
        }
    }
    
    private fun updateLabelsWithExtractionData(data: ImageExtractionResponse) {
        tvImageTitle.text = data.event_title
        tvDescription.text = data.description
        tvWho.text = data.context_who
        tvWhen.text = data.context_when
        tvWhere.text = data.context_where
        
        try {
            val extractedDate = data.context_when
            if (extractedDate.isNotEmpty()) {
                val parsedDate = parseExtractedDate(extractedDate)
                if (parsedDate != null) {
                    calendar.time = parsedDate
                    updateDateInView()
                }
            }
        } catch (e: Exception) {
            Log.e("ImagesFragment", "Error parsing date: ${e.message}")
        }
    }
    
    private fun parseExtractedDate(dateStr: String): Date? {
        val possibleFormats = listOf(
            SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()),
            SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()),
            SimpleDateFormat("MM/dd/yyyy", Locale.getDefault()),
            SimpleDateFormat("MMMM d, yyyy", Locale.getDefault())
        )
        
        for (format in possibleFormats) {
            try {
                return format.parse(dateStr)
            } catch (e: Exception) {
                continue
            }
        }
        
        return null
    }
    
    private fun createDynamicObjectFields(objects: List<String>) {
        objectsContainer.removeAllViews()
        objectEditTexts.clear()
        
        if (objects.isEmpty()) {
            return
        }
        
        for (objectName in objects) {
            val objectLayout = LinearLayout(context)
            objectLayout.orientation = LinearLayout.VERTICAL
            objectLayout.layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            objectLayout.setPadding(0, 8, 0, 8)
            
            val objectLabel = TextView(context)
            objectLabel.text = "About $objectName:"
            objectLabel.setTextColor(ContextCompat.getColor(requireContext(), R.color.secondary))
            objectLabel.textSize = 15f
            objectLabel.setPadding(0,0,0,10)
            
            val objectEditText = EditText(context)
            objectEditText.hint = "Enter details about $objectName"
            objectEditText.setBackgroundColor(ContextCompat.getColor(requireContext(), android.R.color.white))
            objectEditText.setPadding(36, 36, 36, 36)
            objectEditText.setTextColor(ContextCompat.getColor(requireContext(), R.color.secondary))
            objectEditText.textSize = 14f
            
            objectEditTexts[objectName] = objectEditText
            
            objectLayout.addView(objectLabel)
            objectLayout.addView(objectEditText)
            objectsContainer.addView(objectLayout)
        }
    }
    
    private fun validateInputs(): Boolean {
        var isValid = true
        
        if (TextUtils.isEmpty(etImageTitle.text)) {
            etImageTitle.error = "Please enter image title"
            isValid = false
        }
        
        if (TextUtils.isEmpty(etDescription.text)) {
            etDescription.error = "Please enter description"
            isValid = false
        }
        
        if (TextUtils.isEmpty(etDate.text)) {
            etDate.error = "Please select a date"
            isValid = false
        }
        
        if (TextUtils.isEmpty(etWhere.text)) {
            etWhere.error = "Please enter where the image was taken"
            isValid = false
        }
        
        if (selectedImageUri == null) {
            Toast.makeText(context, "Please select an image", Toast.LENGTH_SHORT).show()
            isValid = false
        }
        
        return isValid
    }
    
    private fun uploadImageData() {
        progressDialog.setMessage("Uploading image...")
        progressDialog.show()
        
        val objectNames = objectEditTexts.keys.toList()
        
        val createImageRequest = CreateImageRequest(
            user_id = 1,
            image_base64 = base64Image,
            context_who = objectNames,
            context_where = etWhere.text.toString(),
            context_when = etDate.text.toString(),
            event_title = etImageTitle.text.toString(),
            description = etDescription.text.toString()
        )
        
        apiService.createImage(createImageRequest).enqueue(object : Callback<CreateImageResponse> {
            override fun onResponse(
                call: Call<CreateImageResponse>,
                response: Response<CreateImageResponse>
            ) {
                progressDialog.dismiss()
                
                if (response.isSuccessful && response.body() != null) {
                    val createResponse = response.body()!!
                    saveImageData(createResponse.image_id)
                    
                    formCompletionManager.markImagesFormCompleted()
                    
                    Toast.makeText(
                        context,
                        "Image uploaded successfully",
                        Toast.LENGTH_SHORT
                    ).show()
                    activity?.supportFragmentManager?.popBackStack()
                } else {
                    Toast.makeText(
                        context,
                        "Failed to upload image: ${response.code()}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
            
            override fun onFailure(call: Call<CreateImageResponse>, t: Throwable) {
                progressDialog.dismiss()
                Toast.makeText(
                    context,
                    "Network error: ${t.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
        })
    }
    
    private fun saveImageData(imageId: String) {
        val sharedPreferences = activity?.getSharedPreferences("ImageStories", 0)
        val editor = sharedPreferences?.edit()
        
        val imageCount = sharedPreferences?.getInt("imageCount", 0) ?: 0
        val newImageCount = imageCount + 1
        
        editor?.putString("imageTitle_$newImageCount", etImageTitle.text.toString())
        editor?.putString("imageDescription_$newImageCount", etDescription.text.toString())
        
        val objectDetails = StringBuilder()
        for ((objectName, editText) in objectEditTexts) {
            objectDetails.append("$objectName: ${editText.text}\n")
        }
        editor?.putString("imageWho_$newImageCount", objectDetails.toString())
        
        editor?.putString("imageWhen_$newImageCount", etDate.text.toString())
        editor?.putString("imageWhere_$newImageCount", etWhere.text.toString())
        editor?.putString("imageUri_$newImageCount", selectedImageUri.toString())
        editor?.putString("imageId_$newImageCount", imageId)
        editor?.putInt("imageCount", newImageCount)
        
        editor?.apply()
    }
}
