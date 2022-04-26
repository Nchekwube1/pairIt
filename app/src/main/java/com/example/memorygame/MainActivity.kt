package com.example.memorygame

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.RadioGroup
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.memorygame.models.BoardSize
import com.example.memorygame.models.MemoryGame
import com.google.android.material.snackbar.Snackbar

class MainActivity : AppCompatActivity() {

    private var boardSize:BoardSize = BoardSize.EASY
  private  lateinit var memoryGame:MemoryGame
  private  lateinit var adapter:MemoryBoardAdapter
private  lateinit var  clRoot:ConstraintLayout
private  lateinit var movesTV:TextView
    private  lateinit var pairsTV:TextView
    private lateinit var itemsRV:RecyclerView
    private var clicks:Int = 0

    private var moves:Int = 0
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
         movesTV = findViewById<TextView>(R.id.tv_moves)
         pairsTV = findViewById<TextView>(R.id.tv_pairs)
         itemsRV = findViewById<RecyclerView>(R.id.rv_card_items)
   clRoot = findViewById(R.id.clRoot)
        setupBoard()

    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.main_menu,menu)
        return  true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId){
            R.id.mi_refresh ->{
                if (moves > 0 && !memoryGame.hasWonGame()){
                    showAlertDialog("Quit current game?",null,View.OnClickListener {
                        setupBoard()
                    })
                }else{
                    setupBoard()
                }

                return  true
            }

            R.id.mi_new_size ->{

showNewSizeDialog()
                return  true

            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun showNewSizeDialog() {
        val sizeView = LayoutInflater.from(this).inflate(R.layout.show_size,null)
        val radioGroupSize: RadioGroup = sizeView.findViewById<RadioGroup>(R.id.rg_choose_size)
    when (boardSize){
        BoardSize.EASY ->  radioGroupSize.check(R.id.rb_easy)
        BoardSize.MEDIUM ->  radioGroupSize.check(R.id.rb_medium)
        BoardSize.HARD->  radioGroupSize.check(R.id.rb_hard)

    }
            showAlertDialog("Choose new size",sizeView, View.OnClickListener {
   boardSize = when (radioGroupSize.checkedRadioButtonId){
       R.id.rb_easy -> BoardSize.EASY
       R.id.rb_medium -> BoardSize.MEDIUM
       else ->     BoardSize.HARD
   }
                setupBoard()
        })
    }

    private fun showAlertDialog(title:String, view:View?,positiveClickListener: View.OnClickListener) {
        AlertDialog.Builder(this)
            .setTitle(title).
            setView(view)
            .setNegativeButton("Cancel",null)
            .setPositiveButton("Ok"){_,_ ->
                positiveClickListener.onClick(null)

            }.show()
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
        if (memoryGame.flipCard(position)){
            if(memoryGame.hasWonGame()){
                Snackbar.make(clRoot,"Game Won Congratulations!!!", Snackbar.LENGTH_LONG).show()

            }
        }
        pairsTV.text = "Pairs: ${memoryGame.numPairs} / ${boardSize.getNumPairs()}"

        adapter.notifyDataSetChanged()
    }

    private fun setupBoard(){
        memoryGame = MemoryGame(boardSize)

        itemsRV.layoutManager = GridLayoutManager(this, boardSize.getWidth())
        adapter= MemoryBoardAdapter(this,boardSize, memoryGame.cards,
            object :MemoryBoardAdapter.CardClickedListener{
                override fun onCardClicked(position: Int) {
                    updateGameOnFlip(position)
                    clicks++
                    moves = clicks / 2
                    movesTV.text = "Moves: $moves"
                }
            }
        )
        itemsRV.adapter = adapter

        itemsRV.setHasFixedSize(true)
    }

}