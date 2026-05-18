1. ORGANIZAÇÃO DO CÓDIGO
Optámos por organizar o projeto por responsabilidades lógicas em vez de tarefas isoladas:
- Logic.scala: Centraliza a T1 (Random), T2 (Jogada) e T3 (Random).
- Game.scala: Gere o estado do jogo e e o seu "aspeto" (T4).
- Types.scala / Random.scala: Define a estrutura base imutável.

2. ESCOLHAS TÉCNICAS 
- T1 & T3 (Inteligência Aleatória): Implementámos um sistema de "retry" recursivo. Se o computador 
  escolher um buraco inválido, a função 'tentarJogar' (Tail Recursive) filtra essa opção e 
  tenta novamente. Isto garante que o computador nunca "encrava" o jogo.
- T2 (Lógica de Jogo): O método 'play' valida a distância do salto (exatamente 2 casas) e 
  a existência de um inimigo no meio. A atualização da lista de espaços vazios é feita de 
  forma incremental.
- T4 (Visualização): Todo o desenho do tabuleiro (incluindo números das colunas e divisórias) 
  foi feito usando recursividade pura. 

3. ADAPTABILIDADE
O tabuleiro foi implementado para ser de tamanho variável. O utilizador define as dimensões 
no início, e o sistema calcula automaticamente as duas casas centrais para remover, 
respeitando a regra de inicialização do Könane.

4. REQUISITOS CUMPRIDOS
- Implementação total das tarefas T1, T2, T3 e T4.
- Utilização de Imutabilidade, Funções de Ordem Superior e Pattern Matching.
- Suporte para capturas múltiplas (o jogador pode escolher continuar a saltar).

COMO TESTAR:
Executar o ficheiro 'Game.scala' (objeto KönaneGame) para jogar na consola