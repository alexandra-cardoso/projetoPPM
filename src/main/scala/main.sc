object Main extends App {
    // 1. Inicialização (T2) [cite: 59, 61]
    // Criamos o jogo e definimos o centro dinamicamente
    val rows = 8
    val cols = 8
    val jogoBase = Game.initBoard(rows, cols)

    // No Kōnane, removemos duas pedras adjacentes do centro [cite: 45]
    val mR = rows / 2
    val mC = cols / 2
    val h1 = (mR - 1, mC - 1) // (3,3)
    val h2 = (mR - 1, mC)     // (3,4)

    val boardInicial = jogoBase.board - h1 - h2
    val vaziosIniciais = List(h1, h2)
    val estadoAtual = jogoBase.copy(board = boardInicial)

    // 2. T4 - Mostrar o tabuleiro inicial [cite: 68]
    println("--- TABULEIRO INICIAL (T2 & T4) ---")
    Game.render(estadoAtual)

    // 3. T1 & T3 - Jogada Aleatória do Computador [cite: 55, 62]
    val rand = MyRandom(42L)
    println(s"\n[IA] Vez das ${estadoAtual.currentPlayer}...")

    // Chamada da função de ordem superior (T3) que usa a T1 [cite: 62, 63]
    val (novoBoardOpt, _, novaListaVazios, destino) = Logic.playRandomly(
        estadoAtual.board,
        rand,
        estadoAtual.currentPlayer,
        vaziosIniciais,
        Logic.randomMove // Passamos a função da T1 aqui [cite: 55]
    )

    // 4. Resultado (T2)
    novoBoardOpt match {
        case Some(nb) =>
            println(s"Computador saltou para: ${destino.get}")
            Game.render(estadoAtual.copy(board = nb))
            println(s"Novas coordenadas livres: $novaListaVazios")
        case None =>
            println("O computador não encontrou jogadas válidas.")
    }
}