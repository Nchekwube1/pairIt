package com.example.memorygame

import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.MenuItem
import android.widget.Button
import android.widget.EditText
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.memorygame.models.BoardSize
import com.example.memorygame.models.EXTRA_BOARD_SIZE

class CreateActivity : AppCompatActivity() {
 private lateinit var rvCreateGame:RecyclerView
 private lateinit var etCreateGame: EditText
 private lateinit var btnCreateGame : Button


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
  rvCreateGame.adapter = ImagePickerAdapter(this, chosenImageUris,boardSize)
    }


    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if(item.itemId == android.R.id.home){
            finish()
            return  true
        }
        return super.onOptionsItemSelected(item)
    }
}