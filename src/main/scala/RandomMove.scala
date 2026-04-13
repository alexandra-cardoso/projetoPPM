import scala.util.Random
object RandomMove {
    
    def randomMove(lstOpenCoords: List[Coord2D], rand: MyRandom): (Coord2D, MyRandom) = {
        // Garanto que a lista tem elementos 
        require(lstOpenCoords.nonEmpty, "A lista de coordenadas livres não pode estar vazia")

        // 2. Gerar um índice aleatório entre 0 e o tamanho da lista - 1
        val (index, nextRand) = rand.nextInt(lstOpenCoords.length)

       
        // para garantir que devolvemos um MyRandom
        val nextMyRandom = nextRand match {
            case mr: MyRandom => mr
            case _ => MyRandom(0L) 
        }
        val chosenCoord = lstOpenCoords(index)// a coordenada escolhida vai ser a no index escolhido aleatóriamente
        
        (chosenCoord, nextMyRandom)
    }

}
