package com.example.memorygame

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.example.memorygame.models.BoardSize
import kotlin.math.min

class MemoryBoardAdapter(private val context: Context, private val boardSize: BoardSize)
    : RecyclerView.Adapter<MemoryBoardAdapter.ViewHolder>()
{

    inner class  ViewHolder(viewHolder: View): RecyclerView.ViewHolder(viewHolder){
        private val imageBtn = viewHolder.findViewById<ImageButton>(R.id.ib_single_image)

        fun bind(position: Int) {
            imageBtn.setOnClickListener{
                Log.i(TAG, "Clicked on item position: $position")

            }
        }

    }
    companion object{
private  const val  MARGIN_SIZE = 10
        private  const val  TAG =  "MemoryBoardAdapter"

    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {


        val cardWidth = parent.width/boardSize.getWidth() - (2* MARGIN_SIZE)
        val cardHeight = parent.height / boardSize.getHeight() - (2* MARGIN_SIZE)
        val cardSidesLength = min(cardHeight,cardWidth)
     val view = LayoutInflater.from(context).inflate(
         R.layout.custom_layout,
         parent,
         false
     )
val layoutParams = view.findViewById<CardView>(R.id.cv_custom_layout).layoutParams as ViewGroup.MarginLayoutParams
        layoutParams.width = cardSidesLength
        layoutParams.height = cardSidesLength
layoutParams.setMargins(MARGIN_SIZE, MARGIN_SIZE,MARGIN_SIZE,MARGIN_SIZE)
        return  ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
       holder.bind(position)
    }

    override fun getItemCount() = boardSize.numCards

}