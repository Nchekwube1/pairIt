package com.example.memorygame

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import android.widget.Toast
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.memorygame.models.BoardSize
import com.example.memorygame.models.DEFAULT_ICONS
import com.example.memorygame.models.MemoryCard
import com.example.memorygame.models.MemoryGame
import com.google.android.material.snackbar.Snackbar

class MainActivity : AppCompatActivity() {

    private var boardSize:BoardSize = BoardSize.EASY
  private  lateinit var memoryGame:MemoryGame
  private  lateinit var adapter:MemoryBoardAdapter
private  lateinit var  clRoot:ConstraintLayout
private  lateinit var movesTV:TextView
    private  lateinit var pairsTV:TextView
private var moves:Int = 0
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
         movesTV = findViewById<TextView>(R.id.tv_moves)
         pairsTV = findViewById<TextView>(R.id.tv_pairs)
        val itemsRV = findViewById<RecyclerView>(R.id.rv_card_items)
   clRoot = findViewById(R.id.clRoot)

         memoryGame = MemoryGame(boardSize)

        itemsRV.layoutManager = GridLayoutManager(this, boardSize.getWidth())
       adapter= MemoryBoardAdapter(this,boardSize, memoryGame.cards,
         object :MemoryBoardAdapter.CardClickedListener{
             override fun onCardClicked(position: Int) {
                 updateGameOnFlip(position)
                 moves++
                 movesTV.text = "Moves: $moves"
             }
         }
            )
        itemsRV.adapter = adapter

        itemsRV.setHasFixedSize(true)
    }

    private fun updateGameOnFlip(position: Int) {
        if(memoryGame.hasWonGame()){
            Snackbar.make(clRoot,"Game Won Already", Snackbar.LENGTH_LONG).show()
            return
        }
        if(memoryGame.isCardFaceUp(position)){

            Snackbar.make(clRoot,"Invalid Move", Snackbar.LENGTH_LONG).show()

            return
        }
        memoryGame.flipCard(position)
        pairsTV.text = "Pairs: ${memoryGame.numPairs} / ${boardSize.getNumPairs()}"

        adapter.notifyDataSetChanged()
    }

}