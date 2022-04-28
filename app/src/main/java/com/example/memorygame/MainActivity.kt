package com.example.memorygame

import android.app.Activity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.RadioGroup
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.memorygame.models.*
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class MainActivity : AppCompatActivity() {

            companion object {
                        private const val CREATE_REQUEST_CODE = 14607
                        private const val TAG = "com.example.memorygame"
            }

            private var boardSize: BoardSize = BoardSize.EASY
            private lateinit var memoryGame: MemoryGame
            private lateinit var adapter: MemoryBoardAdapter
            private lateinit var clRoot: ConstraintLayout
            private lateinit var movesTV: TextView
            private lateinit var pairsTV: TextView
            private lateinit var itemsRV: RecyclerView
            private val db = Firebase.firestore
            private var gameName : String? = null
            private var clicks: Int = 0
            private var customGameImages : List<String>? = null

            private var moves: Int = 0
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
                        menuInflater.inflate(R.menu.main_menu, menu)
                        return true
            }

            override fun onOptionsItemSelected(item: MenuItem): Boolean {
                        when (item.itemId) {
                                    R.id.mi_refresh -> {
                                                if (moves > 0 && !memoryGame.hasWonGame()) {
                                                            showAlertDialog(
                                                                        "Quit current game?",
                                                                        null,
                                                                        View.OnClickListener {
                                                                                    setupBoard()
                                                                        })
                                                } else {
                                                            setupBoard()
                                                }

                                                return true
                                    }

                                    R.id.mi_new_size -> {

                                                showNewSizeDialog()
                                                return true

                                    }

                                    R.id.mi_custom -> {
                                                showCreationDialog()
                                    }
                        }
                        return super.onOptionsItemSelected(item)
            }

            override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
                        if(requestCode == CREATE_REQUEST_CODE && resultCode == Activity.RESULT_OK){
                                    val customGameName = data?.getStringExtra(EXTRA_GAME_NAME)
                                    if(customGameName == null){
                                                Toast.makeText(this,"Custom name returned null",
                                                            Toast.LENGTH_LONG).show()
                                                return
                                    }
                                    downloadGame(customGameName)

                        }

                        super.onActivityResult(requestCode, resultCode, data)

            }

            private fun downloadGame(customGameName: String) {
                        db.collection("games").document(customGameName).get().addOnSuccessListener { document ->
                                val userImageList =     document.toObject(UserImageList::class.java)
                                    if(userImageList?.images == null){
                                                Log.e(TAG, "Invalid custom game name ")
                                                Snackbar.make(clRoot,"Sorry we couldn't find any game with that name: $gameName",Snackbar.LENGTH_LONG).show()
                                                return@addOnSuccessListener
                                    }
                                    val numCards = userImageList.images.size * 2
                                    boardSize = BoardSize.getByValue(numCards)
                                    gameName = customGameName
                                    customGameImages = userImageList.images
                        }.addOnFailureListener{exception ->
                                    Log.e(TAG, "Exception when retrieving game:  ",exception)
                        }


            }

            private fun showCreationDialog() {
                        val sizeView = LayoutInflater.from(this).inflate(R.layout.show_size, null)
                        val radioGroupSize: RadioGroup =
                                    sizeView.findViewById<RadioGroup>(R.id.rg_choose_size)

                        showAlertDialog("Create custom  board", sizeView, View.OnClickListener {
                                    val desiredBoardSize =
                                                when (radioGroupSize.checkedRadioButtonId) {
                                                            R.id.rb_easy -> BoardSize.EASY
                                                            R.id.rb_medium -> BoardSize.MEDIUM
                                                            else -> BoardSize.HARD
                                                }
                                    val intent: Intent = Intent(this, CreateActivity::class.java)
                                    intent.putExtra(EXTRA_BOARD_SIZE, desiredBoardSize)
                                    startActivityForResult(intent, CREATE_REQUEST_CODE)

                        })
            }

            private fun showNewSizeDialog() {
                        val sizeView = LayoutInflater.from(this).inflate(R.layout.show_size, null)
                        val radioGroupSize: RadioGroup =
                                    sizeView.findViewById<RadioGroup>(R.id.rg_choose_size)
                        when (boardSize) {
                                    BoardSize.EASY -> radioGroupSize.check(R.id.rb_easy)
                                    BoardSize.MEDIUM -> radioGroupSize.check(R.id.rb_medium)
                                    BoardSize.HARD -> radioGroupSize.check(R.id.rb_hard)

                        }
                        showAlertDialog("Choose new size", sizeView, View.OnClickListener {
                                    boardSize = when (radioGroupSize.checkedRadioButtonId) {
                                                R.id.rb_easy -> BoardSize.EASY
                                                R.id.rb_medium -> BoardSize.MEDIUM
                                                else -> BoardSize.HARD
                                    }
                                    setupBoard()
                        })
            }

            private fun showAlertDialog(
                        title: String,
                        view: View?,
                        positiveClickListener: View.OnClickListener
            ) {
                        AlertDialog.Builder(this)
                                    .setTitle(title).setView(view)
                                    .setNegativeButton("Cancel", null)
                                    .setPositiveButton("Ok") { _, _ ->
                                                positiveClickListener.onClick(null)

                                    }.show()
            }

            private fun updateGameOnFlip(position: Int) {
                        if (memoryGame.hasWonGame()) {
                                    Snackbar.make(clRoot, "Game Won Already", Snackbar.LENGTH_LONG)
                                                .show()
                                    return
                        }
                        if (memoryGame.isCardFaceUp(position)) {

                                    Snackbar.make(clRoot, "Invalid Move", Snackbar.LENGTH_LONG)
                                                .show()

                                    return
                        }
                        if (memoryGame.flipCard(position)) {
                                    if (memoryGame.hasWonGame()) {
                                                Snackbar.make(
                                                            clRoot,
                                                            "Game Won Congratulations!!!",
                                                            Snackbar.LENGTH_LONG
                                                ).show()

                                    }
                        }
                        pairsTV.text = "Pairs: ${memoryGame.numPairs} / ${boardSize.getNumPairs()}"

                        adapter.notifyDataSetChanged()
            }

            private fun setupBoard() {
                        memoryGame = MemoryGame(boardSize,customGameImages)

                        itemsRV.layoutManager = GridLayoutManager(this, boardSize.getWidth())
                        adapter = MemoryBoardAdapter(this, boardSize, memoryGame.cards,
                                    object : MemoryBoardAdapter.CardClickedListener {
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