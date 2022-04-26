package com.example.memorygame

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.TextView
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.memorygame.models.BoardSize
import com.example.memorygame.models.DEFAULT_ICONS

class MainActivity : AppCompatActivity() {

    private var boardSize:BoardSize = BoardSize.HARD

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val movesTV = findViewById<TextView>(R.id.tv_moves)
        val pairsTV = findViewById<TextView>(R.id.tv_pairs)
        val itemsRV = findViewById<RecyclerView>(R.id.rv_card_items)
      val chosenImages = DEFAULT_ICONS.shuffled().take(boardSize.getNumPairs())
        val randomisedList  = (chosenImages +chosenImages).shuffled()
        itemsRV.layoutManager = GridLayoutManager(this, boardSize.getWidth())
        itemsRV.adapter = MemoryBoardAdapter(this,boardSize, randomisedList)
        itemsRV.setHasFixedSize(true)
    }
}