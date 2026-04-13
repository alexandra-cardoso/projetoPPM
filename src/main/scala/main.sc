import scala.collection.parallel.immutable.ParMap

object Main extends App {

    println("=== TESTE DE INTEGRAÇÃO KŌNANE (T1, T2, T3, T4) ===\n")

    // 1. T2 (Parte 1) - Inicialização Dinâmica
    // Criamos um tabuleiro 8x8. O initBoard já deve calcular o centro.
    val gameInicial = Game.initBoard(8, 8)

    // Calculamos o centro para remover as peças (Figura 1 do enunciado)
    val midR = gameInicial.rows / 2
    val midC = gameInicial.cols / 2
    val h1 = (midR - 1, midC - 1) // (3,3)
    val h2 = (midR - 1, midC)     // (3,4)

    // Criamos o estado com os primeiros buracos
    val boardComBuracos = gameInicial.board - h1 - h2
    val listaVaziosInicial = List(h1, h2)
    val estadoPronto = gameInicial.copy(board = boardComBuracos)

    // 2. T4 - Visualização do Tabuleiro (Render)
    println("[T4] - Estado Inicial do Tabuleiro (Regra das casas centrais):")
    Game.render(estadoPronto)

    // 3. Configuração para Teste de IA (T1 e T3)
    val semente = MyRandom(42L) // Semente fixa para resultados reproduzíveis
    val jogadorAtual = Stone.Black

    println(s"\n[T3] - O Computador ($jogadorAtual) vai tentar uma jogada aleatória...")

    // Chamada da T3: Função de ordem superior que usa a T1 (randomMove)
    // O playRandomly devolve (Option[Board], MyRandom, List[Coord2D], Option[Coord2D])
    val (novoBoardOpt, novoRand, novaListaVazios, destinoEscolhido) = Logic.playRandomly(
        estadoPronto.board,
        semente,
        jogadorAtual,
        listaVaziosInicial,
        Logic.randomMove // Passagem da função T1 como argumento
    )

    // 4. Verificação de Resultados (T2 e T3)
    novoBoardOpt match {
        case Some(tabuleiroPosJogada) =>
            val destino = destinoEscolhido.getOrElse((-1, -1))
            println(s"\n[SUCESSO] O computador escolheu o destino: $destino")
            println(s"[T2] - A nova lista de espaços vazios agora tem ${novaListaVazios.size} elementos.")

            // Criamos o novo estado para mostrar o resultado
            val estadoFinal = estadoPronto.copy(
                board = tabuleiroPosJogada,
                message = "Jogada efetuada com sucesso!"
            )

            println("\n[T4] - Tabuleiro após a jogada do computador:")
            Game.render(estadoFinal)

        case None =>
            println("\n[FALHA] O computador não encontrou jogadas válidas.")
            println(s"Lista de vazios analisada: $listaVaziosInicial")
    }

    println("\n=== FIM DO TESTE DE INTEGRAÇÃO ===")
}