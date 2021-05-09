package demoGame.gameplay

object CreatureState {

  sealed trait CreatureState
  case class Normal() extends CreatureState
  case class Stunned(timeLeft:Float) extends CreatureState
  case class InContinuousState(timeLeft:Float, action:ContinuousState) extends CreatureState

  trait  ContinuousState{
    /**Возвращает новый CreatureState или None, для продолжительных действий вернуть Some(InContinuousState(time, this)) */
    def onStateStarts():Option[CreatureState]
    def onStateContinues(tpf:Float):Unit = {}
    /**Можно вернуть новый CreatureState чтобы получть цепочку последовательных действий*/
    def onStateEnds():Option[ContinuousState] = None
    def onStateInterrupted():Unit = {}

  }




}
