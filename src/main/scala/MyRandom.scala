trait Random {
  def nextInt(x: Int): (Int, Random)
}
case class MyRandom(seed: Long) extends Random {
  
  def nextInt(x: Int): (Int, Random) = { //devolve um par, um número aleatório inteiro  e um objeto novo random mantém a imutabilidade 
    val newSeed = (seed * 0x5DEECE66DL + 0xBL) &
      0xFFFFFFFFFFFFL
    val nextRandom = MyRandom(newSeed)
    val n = (newSeed >>> 16).toInt % x
    (if(n<0) -n else n, nextRandom) //my random devolve smp um obj MyRandom tmb, para manter o estado, mantendo a imutabilidade
  }
}