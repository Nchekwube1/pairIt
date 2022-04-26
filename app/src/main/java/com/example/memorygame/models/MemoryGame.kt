package com.example.memorygame.models

class MemoryGame(boardSize:BoardSize) {
  val cards: List<MemoryCard>
    val numPairs = 0

init {
    val chosenImages = DEFAULT_ICONS.shuffled().take(boardSize.getNumPairs())
    val randomisedList  = (chosenImages +chosenImages).shuffled()
    cards=    randomisedList.map {
        MemoryCard(it)
    }
}
}