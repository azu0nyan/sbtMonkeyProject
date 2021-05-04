package demoGame.gameplay

object CreatureState {

  sealed trait CreatureState
  case class Normal() extends CreatureState
  case class Stunned(timeLeft:Float) extends CreatureState
  case class ChannelingAction(timeLeft:Float, action:CreatureAction) extends CreatureState

  trait  CreatureAction{
    def canAct(implicit creature: CreatureControl):Boolean
    /**Функция должна обновить стейт на ChannelingAction чтобы были вызваны Ends, Continues, Interrupted */
    def onActionStarts(implicit creature: CreatureControl):Option[CreatureState]
    def onActionContinues(tpf:Float)(implicit creature: CreatureControl):Unit = {}
    def onActionEnds(implicit creature: CreatureControl):Option[CreatureAction] = None
    def onActionInterrupted(implicit creature: CreatureControl):Unit = {}

  }


}
