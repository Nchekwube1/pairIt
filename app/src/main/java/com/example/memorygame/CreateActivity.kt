package com.example.memorygame

import android.app.Activity
import android.content.ClipData
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.widget.Adapter
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.memorygame.models.BoardSize
import com.example.memorygame.models.EXTRA_BOARD_SIZE
import com.example.memorygame.models.isPermissionGranted
import com.example.memorygame.models.requestPermission

class CreateActivity : AppCompatActivity() {
    companion object{
        private  const val PICK_PHOTO_CODE = 1827765
        private const val READ_PHOTOS_PERMISSION = android.Manifest.permission.READ_EXTERNAL_STORAGE
        private const val READ_PHOTOS_CODE = 763
        private const val TAG = "CreateActivity"

    }

 private lateinit var rvCreateGame:RecyclerView
 private lateinit var etCreateGame: EditText
 private lateinit var btnCreateGame : Button

private lateinit var adapter:ImagePickerAdapter
    private  lateinit var boardSize:BoardSize
    private  var numImagesReq = -1
private val chosenImageUris = mutableListOf<Uri>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create)
        rvCreateGame = findViewById(R.id.rv_create_game)
        etCreateGame = findViewById(R.id.et_custom_name)
        btnCreateGame = findViewById(R.id.btn_create_custom)


        supportActionBar?.setDisplayHomeAsUpEnabled(true)
         boardSize = intent.getSerializableExtra(EXTRA_BOARD_SIZE) as BoardSize
        numImagesReq = boardSize.getNumPairs()
        supportActionBar?.title = "Choose images  (0/$numImagesReq)"

        rvCreateGame.setHasFixedSize(true)
        rvCreateGame.layoutManager = GridLayoutManager(this, boardSize.getWidth())
  adapter = ImagePickerAdapter(this, chosenImageUris,boardSize,
  object : ImagePickerAdapter.ImageClickListener{
      override  fun onPlaceHolderClicked(){
       if(isPermissionGranted(this@CreateActivity,READ_PHOTOS_PERMISSION)){
           launchIntentForPhotos()
       }else{
           requestPermission(this@CreateActivity, READ_PHOTOS_PERMISSION, READ_PHOTOS_CODE)
       }
      }
  }
      )
        rvCreateGame.adapter =adapter
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if(requestCode == READ_PHOTOS_CODE){
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                launchIntentForPhotos()
            }else{
                Toast.makeText(this,"You need to enable permissions", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun launchIntentForPhotos() {
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE,true)
        startActivityForResult(Intent.createChooser(intent, "Choose images"),PICK_PHOTO_CODE)
    }


    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if(item.itemId == android.R.id.home){
            finish()
            return  true
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if(requestCode !=PICK_PHOTO_CODE    || resultCode != Activity.RESULT_OK || data == null  ) {
            Toast.makeText(this,"Did not get data back from activity, flow likely canceled", Toast.LENGTH_LONG).show()
    return
        }else{
            val selectedUri : Uri? = data.data
            val clipData : ClipData? = data.clipData
            
            if(clipData != null){
                Log.i(TAG, "Clip data result ${clipData.itemCount}: $clipData")
                for (i in 0 until  clipData.itemCount){
                    var clipItem = clipData.getItemAt(i)
                    if(chosenImageUris.size < numImagesReq){
                        chosenImageUris.add(clipItem.uri)
                    }
                }
            }else if(selectedUri != null){
                Log.i(TAG, "selected data result $selectedUri")

                chosenImageUris.add(selectedUri)

            }

            adapter.notifyDataSetChanged()
            supportActionBar?.title = "Choose images ${chosenImageUris.size} / $numImagesReq"
        }
    }
}