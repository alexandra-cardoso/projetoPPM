
    object main extends App {
        println("--- Teste da Tarefa T1 ---")

        // 1. Criar uma semente (seed) aleatória inicial
        val randomInicial = MyRandom(12345L)

        // 2. Simular uma lista de posições livres
        val coordenadasLivres: List[Coord2D] = List((3, 3), (3, 4), (4, 4), (5, 2))

        println(s"Coordenadas livres disponíveis: $coordenadasLivres")

        // 3. Chamar a função randomMove
        val (jogadaEscolhida, proximoRandom) = RandomMove.randomMove(coordenadasLivres, randomInicial)

        println(s"O computador escolheu a coordenada: $jogadaEscolhida")

        // 4. Testar uma segunda vez com o novo estado do Random
        val (jogadaEscolhida2, proximoRandom2) = RandomMove.randomMove(coordenadasLivres, proximoRandom)
        println(s"Na segunda tentativa, com a nova seed, escolheu: $jogadaEscolhida2")
    } 
