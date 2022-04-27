package com.example.memorygame.models

import android.graphics.Bitmap

object BitmapScaler {

    fun scaleToFitHeight(b:Bitmap, height:Int):Bitmap{
  val factor:Float = height/b.height.toFloat()

        return  Bitmap.createScaledBitmap(b, (b.width * factor).toInt(),height,true)
    }

    fun scaleToFitWidth(b:Bitmap, width:Int):Bitmap{
        val factor:Float = width/b.height.toFloat()

        return  Bitmap.createScaledBitmap(b, width ,(b.height * factor).toInt(),true)

    }
}
