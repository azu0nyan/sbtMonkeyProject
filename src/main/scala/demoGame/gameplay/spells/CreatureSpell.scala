package demoGame.gameplay.spells

import demoGame.gameplay.CreatureControl
import demoGame.gameplay.CreatureState.{ContinuousState, CreatureState, Normal}
import demoGame.gameplay.spells.GeometricBall.GeometricBall
import org.slf4j.LoggerFactory

object CreatureSpell {
  val logger = LoggerFactory.getLogger(classOf[CreatureSpell].getName)

  type SpellLevel = Int

  class CreatureSpell(creature: CreatureControl,
                      var manaCostFunc: SpellLevel => Int,
                      var castActionBuilder: (SpellLevel, CreatureControl) => ContinuousState,
                      var level: Int) {
    def manaCost:Int = manaCostFunc(level)
    def canCast: Boolean = {
      creature.state == Normal() &&
        manaCost <= creature.info.mana
    }

    def cast(): Unit = if (canCast) {
      val castAction = castActionBuilder(level, creature)
      val mana = manaCost
      creature.spendMana(mana)
      //Возвращаем ману если заклинание было прервано
      val castWithRemoveManaAction = new ContinuousState {
        override def onStateStarts(): Option[CreatureState] = castAction.onStateStarts()
        override def onStateContinues(tpf: Float): Unit = castAction.onStateContinues(tpf)
        override def onStateEnds(): Option[ContinuousState] = castAction.onStateEnds()
        override def onStateInterrupted(): Unit = {
          creature.regenMana(mana)
          castAction.onStateInterrupted()
        }
      }
      creature.doAction(castWithRemoveManaAction)
    } else {
      logger.info(s"Cant cast spell..")
    }
  }

}
