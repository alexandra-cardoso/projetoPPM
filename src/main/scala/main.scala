object main extends App {
    println("--- TESTE DE INTEGRAÇÃO (T1, T2, T3 e T4) ---")

    // 1. Inicializar o tabuleiro e o gerador de números aleatórios
    val inicialGame = Game.initBoard()
    val boardWithoutMiddle = inicialGame.board - (3, 3) - (3, 4)
    val lstOpenCoords = List((3, 3), (3, 4))
    val rand = MyRandom(123L) // Semente fixa para ser fácil de prever os testes

    println("\n[Teste T4] - Tabuleiro Inicial (Antes da jogada do computador):")
    Game.render(boardWithoutMiddle)

    // 2. Criar a instância da tua classe T3
    val computador = new T3()

    // 3. Fazer a jogada! 
    // O T3 vai chamar o T1 (RandomMove.randomMove) e o T2 (Logic.play)
    val jogadaComputador = computador.playRandomly(
        boardWithoutMiddle,
        rand,
        Stone.Black, // Vamos assumir que são as Pretas a jogar
        lstOpenCoords,
        RandomMove.randomMove // É aqui que passas o teu T1 como argumento!
    )

    // O playRandomly devolve uma tupla com 4 valores. Vamos extraí-los:
    val novoBoardOpt = jogadaComputador._1
    val novoRandom = jogadaComputador._2
    val novaListaVazios = jogadaComputador._3
    val coordenadaDestinoOpt = jogadaComputador._4

    // 4. Verificar se a jogada funcionou (Teste T2 e T4)
    novoBoardOpt match {
        case Some(board) =>
            println(s"\n[Teste T1 e T3] - Sucesso! O computador saltou para a coordenada: ${coordenadaDestinoOpt.get}")
            println(s"[Teste T2] - A nova lista de espaços vazios é: $novaListaVazios")

            println("\n[Teste T4] - Tabuleiro Após a Jogada:")
            Game.render(board)

        case None =>
            println("\n[Teste T2 e T3] - O computador não conseguiu encontrar uma jogada válida.")
            println("Destino tentado foi: " + coordenadaDestinoOpt)
    }
}