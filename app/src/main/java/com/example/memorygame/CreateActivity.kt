package com.example.memorygame

import android.app.Activity
import android.app.AlertDialog
import android.content.ClipData
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.text.Editable
import android.text.InputFilter
import android.text.TextWatcher
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.widget.*
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.memorygame.models.*
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import java.io.ByteArrayOutputStream

class CreateActivity : AppCompatActivity() {
            companion object {
                        private const val PICK_PHOTO_CODE = 1827765
                        private const val READ_PHOTOS_PERMISSION =
                                    android.Manifest.permission.READ_EXTERNAL_STORAGE
                        private const val READ_PHOTOS_CODE = 763
                        private const val TAG = "CreateActivity"
                        private const val MAX_GAME_NAME_LENGTH = 14
                        private const val MIN_GAME_NAME_LENGTH = 3

            }

            private lateinit var rvCreateGame: RecyclerView
            private lateinit var etCreateGame: EditText
            private lateinit var btnCreateGame: Button
            private lateinit var pbCreateGame:ProgressBar

            private lateinit var adapter: ImagePickerAdapter
            private lateinit var boardSize: BoardSize
            private var numImagesReq = -1
            private val chosenImageUris = mutableListOf<Uri>()
            private val storage = Firebase.storage
            private val db = Firebase.firestore

            override fun onCreate(savedInstanceState: Bundle?) {
                        super.onCreate(savedInstanceState)
                        setContentView(R.layout.activity_create)
                        rvCreateGame = findViewById(R.id.rv_create_game)
                        etCreateGame = findViewById(R.id.et_custom_name)
                        btnCreateGame = findViewById(R.id.btn_create_custom)
                        pbCreateGame = findViewById(R.id.pb_upload_game)


                        supportActionBar?.setDisplayHomeAsUpEnabled(true)
                        boardSize = intent.getSerializableExtra(EXTRA_BOARD_SIZE) as BoardSize
                        numImagesReq = boardSize.getNumPairs()
                        supportActionBar?.title = "Choose images  (0/$numImagesReq)"

                        rvCreateGame.setHasFixedSize(true)
                        rvCreateGame.layoutManager = GridLayoutManager(this, boardSize.getWidth())
                        adapter = ImagePickerAdapter(this, chosenImageUris, boardSize,
                                    object : ImagePickerAdapter.ImageClickListener {
                                                override fun onPlaceHolderClicked() {
                                                            if (isPermissionGranted(
                                                                                    this@CreateActivity,
                                                                                    READ_PHOTOS_PERMISSION
                                                                        )
                                                            ) {
                                                                        launchIntentForPhotos()
                                                            } else {
                                                                        requestPermission(
                                                                                    this@CreateActivity,
                                                                                    READ_PHOTOS_PERMISSION,
                                                                                    READ_PHOTOS_CODE
                                                                        )
                                                            }
                                                }
                                    }
                        )
                        rvCreateGame.adapter = adapter
                        etCreateGame.filters =
                                    arrayOf(InputFilter.LengthFilter(MAX_GAME_NAME_LENGTH))
                        etCreateGame.addTextChangedListener(object : TextWatcher {
                                    override fun beforeTextChanged(
                                                p0: CharSequence?,
                                                p1: Int,
                                                p2: Int,
                                                p3: Int
                                    ) {
                                    }

                                    override fun onTextChanged(
                                                p0: CharSequence?,
                                                p1: Int,
                                                p2: Int,
                                                p3: Int
                                    ) {
                                    }

                                    override fun afterTextChanged(p0: Editable?) {
                                                btnCreateGame.isEnabled = shouldEnableSaveButton()
                                    }

                        })
                        btnCreateGame.setOnClickListener {
                                    saveDataToFirebase()
                        }
            }

            private fun saveDataToFirebase() {
                        val customGameName = etCreateGame.text.toString()
                        btnCreateGame.isEnabled = false

                        db.collection("games").document(customGameName).get().addOnSuccessListener {
                                    document ->
                                    if(document != null && document.data != null ){
                                                AlertDialog.Builder(this)
                                                            .setTitle("Name already exists")
                                                            .setPositiveButton("OK", null)
                                                            .show()
                                    }else{
                                                handleImageUploading(customGameName)
 
                                    }
                        }.addOnFailureListener{exception->
                                    btnCreateGame.isEnabled = true
                                    Toast.makeText(this, "An error occurred while saving memory game $exception", Toast.LENGTH_LONG).show()
                        }


            }

            private fun handleImageUploading(gameName: String) {
                        pbCreateGame.visibility = View.VISIBLE
                        var didEncounterErr = false
                        val uploadedImageUrls: MutableList<String> = mutableListOf<String>()
                        Toast.makeText(this, "Saving data to firebase", Toast.LENGTH_LONG).show()
                        for ((index, photoUri) in chosenImageUris.withIndex()) {
                                    val imageByteArray = getImageByteArray(photoUri)
                                    val filePath =
                                                "images/$gameName/${System.currentTimeMillis()}-${index}.jpg"
                                    val photoReference = storage.reference.child(filePath)
                                    photoReference.putBytes(imageByteArray)
                                                .continueWithTask { photoUploadTask ->
                                                            Toast.makeText(
                                                                        this,
                                                                        "Uploaded bytes: ${photoUploadTask.result?.bytesTransferred}",
                                                                        Toast.LENGTH_LONG
                                                            ).show()
                                                            photoReference.downloadUrl
                                                }.addOnCompleteListener { downloadUrlTask ->
                                                            if (!downloadUrlTask.isSuccessful) {
                                                                        Toast.makeText(
                                                                                    this,
                                                                                    "An exception occurred with firebase",
                                                                                    Toast.LENGTH_LONG
                                                                        ).show()
                                                                        didEncounterErr = true
                                                                        return@addOnCompleteListener
                                                            }
                                                            if (didEncounterErr) {
                                                                        pbCreateGame.visibility = View.GONE
                                                                        return@addOnCompleteListener
                                                            }

                                                            val downloadUrl =
                                                                        downloadUrlTask.result.toString()
                                                            uploadedImageUrls.add(downloadUrl)
                                                            pbCreateGame.progress = uploadedImageUrls.size * 100/ chosenImageUris.size
                                                            Toast.makeText(
                                                                        this,
                                                                        "Upload finished",
                                                                        Toast.LENGTH_LONG
                                                            ).show()
                                                            if (uploadedImageUrls.size == chosenImageUris.size) {
                                                                        handleAllImagesUploaded(
                                                                                    gameName,
                                                                                    uploadedImageUrls
                                                                        )
                                                            }
                                                }
                        }

            }

            private fun handleAllImagesUploaded(gameName: String, imageUrls: MutableList<String>) {
                        db.collection("games").document(gameName)
                                    .set(mapOf("images" to imageUrls))
                                    .addOnCompleteListener{
                                                gameCreationTask ->
                                                if(!gameCreationTask.isSuccessful){
                                                            Toast.makeText(this, "An error occurred while saving $gameName to firestore", Toast.LENGTH_LONG).show()
                                                            return@addOnCompleteListener
                                                }
                                                pbCreateGame.visibility = View.GONE
                                                Toast.makeText(this, "successfully created game $gameName", Toast.LENGTH_LONG).show()
                                                AlertDialog.Builder(this)
                                                            .setTitle("Upload complete!, Lets play your game $gameName")
                                                            .setPositiveButton("OK"){
                                                                        _,_ ->
                                                                        val resultData = Intent()
                                                                        resultData.putExtra(EXTRA_GAME_NAME, gameName)
                                                                        setResult(Activity.RESULT_OK, resultData)
                                                                        finish()
                                                            }.show()

                                    }
            }

            private fun getImageByteArray(photoUri: Uri): ByteArray {
                        val originalBitMap = if (Build.VERSION.SDK_INT == Build.VERSION_CODES.P) {
                                    val source =
                                                ImageDecoder.createSource(contentResolver, photoUri)
                                    ImageDecoder.decodeBitmap(source)
                        } else {
                                    MediaStore.Images.Media.getBitmap(contentResolver, photoUri)
                        }
                        val scaledBitmap = BitmapScaler.scaleToFitHeight(originalBitMap, 250)
                        val byteOutputStream = ByteArrayOutputStream()
                        scaledBitmap.compress(Bitmap.CompressFormat.JPEG, 60, byteOutputStream)
                        return byteOutputStream.toByteArray()
            }

            override fun onRequestPermissionsResult(
                        requestCode: Int,
                        permissions: Array<out String>,
                        grantResults: IntArray
            ) {
                        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
                        if (requestCode == READ_PHOTOS_CODE) {
                                    if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                                                launchIntentForPhotos()
                                    } else {
                                                Toast.makeText(
                                                            this,
                                                            "You need to enable permissions",
                                                            Toast.LENGTH_LONG
                                                ).show()
                                    }
                        }
            }

            private fun launchIntentForPhotos() {
                        val intent = Intent(Intent.ACTION_PICK)
                        intent.type = "image/*"
                        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
                        startActivityForResult(
                                    Intent.createChooser(intent, "Choose images"),
                                    PICK_PHOTO_CODE
                        )
            }


            override fun onOptionsItemSelected(item: MenuItem): Boolean {
                        if (item.itemId == android.R.id.home) {
                                    finish()
                                    return true
                        }
                        return super.onOptionsItemSelected(item)
            }

            override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {


                        super.onActivityResult(requestCode, resultCode, data)

                        if (requestCode != PICK_PHOTO_CODE || resultCode != Activity.RESULT_OK || data == null) {
                                    Toast.makeText(
                                                this,
                                                "Did not get data back from activity, flow likely canceled",
                                                Toast.LENGTH_LONG
                                    ).show()
                                    return
                        } else {
                                    val selectedUri: Uri? = data.data
                                    val clipData: ClipData? = data.clipData

                                    if (clipData != null) {
                                                Log.i(
                                                            TAG,
                                                            "Clip data result ${clipData.itemCount}: $clipData"
                                                )
                                                for (i in 0 until clipData.itemCount) {
                                                            var clipItem = clipData.getItemAt(i)
                                                            if (chosenImageUris.size < numImagesReq) {
                                                                        chosenImageUris.add(clipItem.uri)
                                                            }
                                                }
                                    } else if (selectedUri != null) {
                                                Log.i(TAG, "selected data result $selectedUri")

                                                chosenImageUris.add(selectedUri)

                                    }

                                    adapter.notifyDataSetChanged()
                                    supportActionBar?.title =
                                                "Choose images ${chosenImageUris.size} / $numImagesReq"

                                    btnCreateGame.isEnabled = shouldEnableSaveButton()
                        }
            }

            private fun shouldEnableSaveButton(): Boolean {
                        if (chosenImageUris.size == numImagesReq && (etCreateGame.text.isNotBlank() && etCreateGame.text.length > MIN_GAME_NAME_LENGTH)) {
                                    return true
                        }
                        return false
            }


}