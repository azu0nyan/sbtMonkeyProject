package demoGame.gameplay

import com.jme3.renderer.{RenderManager, ViewPort}
import com.jme3.scene.control.AbstractControl
import demoGame.{CharacterInputControl, Navigation, NavigationControl}
import demoGame.gameplay.CreatureInfo.CreatureInfo
import demoGame.gameplay.CreatureState.{ContinuousState, CreatureState, InContinuousState, Normal, Stunned}
import demoGame.gameplay.spells.CreatureSpell
import demoGame.gameplay.spells.CreatureSpell.CreatureSpell
import demoGame.gameplay.spells.GeometricBall.GeometricBall
import org.slf4j.LoggerFactory


trait Creature

class CreatureControl(initialInfo: CreatureInfo)(implicit val level: GameLevelAppState) extends AbstractControl {



  val logger = LoggerFactory.getLogger(classOf[CreatureControl].getName)

  implicit val cr: CreatureControl = this

  val info: CreatureInfo = initialInfo

  lazy val movement: CreatureMovement = getSpatial.getControl(classOf[CreatureMovementControl])
  lazy val nav: NavigationControl = getSpatial.getControl(classOf[NavigationControl])

  var spells:Seq[CreatureSpell] = Seq()

  def death(): Unit = {
    logger.info(s"Creature ${name} dead")
    getSpatial.removeFromParent()
    level.physicSpace.remove(movement.asInstanceOf[CreatureMovementControl])
    if(info.gold > 0) new GoldPileControl(getSpatial.getLocalTranslation, info.gold)
  }

  def state: CreatureState = _state
  private var _state: CreatureState = Normal()


  def setState(newState: CreatureState): Unit = {
    _state match {
      case Normal() =>
      case Stunned(timeLeft) =>
      case InContinuousState(timeLeft, action) => action.onStateInterrupted()
    }
    newState match {
      case Normal() => movement.allowMovement()
      case CreatureState.Stunned(timeLeft) => movement.forbidMovement()
      case InContinuousState(timeLeft, action) => movement.forbidMovement()
    }
    _state = newState
  }
  def name:String = info.name

  def setSpeed(speed: Float) :Unit = {
    movement.setSpeed(speed)
  }

  def addGold(gold: Int): CreatureControl = {
    info.gold += gold
    this
  }

  def gold:Int  = info.gold


  def spendGold(gold:Int):CreatureControl = {
    info.gold -= gold
    this
  }

  def stun(time: Float): Unit = {
    setState(Stunned(time))
  }


  def castSpell(sp:CreatureSpell):Unit = {
    if(spells.contains(sp))sp.cast()
    else logger.error(s"Creature ${name} trying to cast spell that not owned by him")
  }

  def doAction(act: ContinuousState): Unit = {
      val newState = act.onStateStarts()
      newState.foreach(st => setState(st))
  }

  def spendMana(manaCost: Int):Unit = {
    val manaSpent = math.min(info.mana, math.max(manaCost, 0))
    info.mana -= manaSpent
  }

  def regenMana(mana:Int):Unit = {
    val manaRegent = math.min(info.maxMana - info.mana, math.max(mana, 0))
    info.mana += manaRegent
  }

  def isFullMana:Boolean = info.mana == info.maxMana

  def receiveDamage(dmg: Int): Unit = {
    val dmgReceived = math.min(info.hp,math.max(dmg, 0))
    info.hp -= dmgReceived
    if(info.hp <= 0) death()
  }


  override def controlUpdate(tpf: Float): Unit = {
    _state match {
      case Normal() =>
      case Stunned(timeLeft) if timeLeft - tpf <= 0 =>
        _state = Normal()
        movement.allowMovement()
      case Stunned(timeLeft) =>
        _state = Stunned(timeLeft - tpf)
      case InContinuousState(timeLeft, action) if timeLeft - tpf > 0 =>
        action.onStateContinues(tpf)
        _state = InContinuousState(timeLeft - tpf, action)
      case InContinuousState(timeLeft, action) =>
        action.onStateContinues(timeLeft)
        val actionOnEnd = action.onStateEnds()
        if (actionOnEnd.isEmpty) {
          _state = Normal()
          movement.allowMovement()
        } else doAction(actionOnEnd.get)

    }
  }

  override def controlRender(rm: RenderManager, vp: ViewPort): Unit = {}
}
