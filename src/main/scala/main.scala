object main extends App {
    println("--- TESTE DE INTEGRAÇÃO (Organização Final) ---")

    // 1. Inicializar o tabuleiro e o gerador de números aleatórios
    val inicialGame = Game.initBoard()
    val boardWithoutMiddle = inicialGame.board - (3, 3) - (3, 4)
    val lstOpenCoords = List((3, 3), (3, 4))
    val rand = MyRandom(123L) // Semente fixa para ser fácil de prever os testes

    println("\n[Teste T4] - Tabuleiro Inicial (Antes da jogada do computador):")
    Game.render(boardWithoutMiddle)

    // 2. Fazer a jogada! 
    // Como o T3 deixou de ser uma classe instanciável e passou para o Logic,
    // já não precisamos de fazer "new T3()". Chamamos os métodos diretamente!
    val jogadaComputador = Logic.playRandomly(
        boardWithoutMiddle,
        rand,
        Stone.Black, // Vamos assumir que são as Pretas a jogar
        lstOpenCoords,
        Logic.randomMove // O método randomMove agora também vive no Logic!
    )

    // O playRandomly devolve uma tupla com 4 valores. Vamos extraí-los:
    val novoBoardOpt = jogadaComputador._1
    val novoRandom = jogadaComputador._2
    val novaListaVazios = jogadaComputador._3
    val coordenadaDestinoOpt = jogadaComputador._4

    // 3. Verificar se a jogada funcionou (Teste T2 e T4)
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