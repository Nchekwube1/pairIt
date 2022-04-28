package com.example.memorygame.models

class MemoryGame(val boardSize: BoardSize, customImages: List<String>?) {
  val cards: List<MemoryCard>
    var numPairs = 0
  private var indexOfSelectedCard: Int? = null
init {
    if(customImages == null){
        val chosenImages = DEFAULT_ICONS.shuffled().take(boardSize.getNumPairs())
        val randomisedList  = (chosenImages +chosenImages).shuffled()
        cards=    randomisedList.map {
            MemoryCard(it)
        }
    }else{
        val randomisedImages = (customImages + customImages).shuffled()
        cards=    randomisedImages.map {
            MemoryCard(it.hashCode(),it)
        }
    }

}

    fun hasWonGame(): Boolean {
        return  numPairs == boardSize.getNumPairs()
    }

    fun flipCard(position:Int):Boolean{
        var foundMatch = false
        val card = cards[position]

        if(indexOfSelectedCard == null){
            restoreCards()
            indexOfSelectedCard = position
        }else{
            foundMatch  = checkForMatch(indexOfSelectedCard!!,position)
            indexOfSelectedCard = null

        }


        card .isFaceUp = !card.isFaceUp

        return  foundMatch
    }

    private fun checkForMatch(index1:Int, index2:Int) :Boolean{
        val checker = cards[index1].identifier == cards[index2].identifier
        cards[index1].isMatched = checker
        cards[index2].isMatched = checker
        if(checker) numPairs++
        return  checker
    }

    private fun restoreCards() {
        for (card in cards){
            if(!card.isMatched){
                card.isFaceUp = false

            }
        }
    }

    fun isCardFaceUp(position: Int): Boolean {
        return  cards[position].isFaceUp

    }


}