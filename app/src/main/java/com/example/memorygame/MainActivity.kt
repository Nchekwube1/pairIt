package com.example.memorygame

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.memorygame.models.BoardSize
import com.example.memorygame.models.DEFAULT_ICONS
import com.example.memorygame.models.MemoryCard
import com.example.memorygame.models.MemoryGame

class MainActivity : AppCompatActivity() {

    private var boardSize:BoardSize = BoardSize.EASY
  private  lateinit var memoryGame:MemoryGame
  private  lateinit var adapter:MemoryBoardAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val movesTV = findViewById<TextView>(R.id.tv_moves)
        val pairsTV = findViewById<TextView>(R.id.tv_pairs)
        val itemsRV = findViewById<RecyclerView>(R.id.rv_card_items)


         memoryGame = MemoryGame(boardSize)

        itemsRV.layoutManager = GridLayoutManager(this, boardSize.getWidth())
       adapter= MemoryBoardAdapter(this,boardSize, memoryGame.cards,
         object :MemoryBoardAdapter.CardClickedListener{
             override fun onCardClicked(position: Int) {
                 updateGameOnFlip(position)
             }
         }
            )
        itemsRV.adapter = adapter

        itemsRV.setHasFixedSize(true)
    }

    private fun updateGameOnFlip(position: Int) {
        memoryGame.flipCard(position)
        adapter.notifyDataSetChanged()
    }

}