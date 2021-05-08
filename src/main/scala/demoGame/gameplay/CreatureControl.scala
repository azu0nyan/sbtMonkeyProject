package demoGame.gameplay

import com.jme3.bullet.control.BetterCharacterControl
import com.jme3.renderer.{RenderManager, ViewPort}
import com.jme3.scene.control.AbstractControl
import demoGame.{CharacterInputControl, Navigation, NavigationControl}
import demoGame.gameplay.CreatureInfo.CreatureInfo
import demoGame.gameplay.CreatureState.{ChannelingAction, CreatureAction, CreatureState, Normal, Stunned}


trait Creature

class CreatureControl(initialInfo: CreatureInfo)(implicit val level: GameLevelAppState) extends AbstractControl {


  def receiveDamage(dmg: Int): Unit = {
    val dmgReceived = math.min(info.hp,math.max(dmg, 0))
    info.hp -= dmgReceived
    if(info.hp <= 0) death()
  }

  implicit val cr: CreatureControl = this

  val info: CreatureInfo = initialInfo

  lazy val movement: CreatureMovement = getSpatial.getControl(classOf[CreatureMovementControl])
  lazy val nav: NavigationControl = getSpatial.getControl(classOf[NavigationControl])


  def death(): Unit = {
    getSpatial.removeFromParent()
    level.physicSpace.remove(movement.asInstanceOf[CreatureMovementControl])
  }

  def state: CreatureState = _state
  private var _state: CreatureState = Normal()


  def setState(newState: CreatureState): Unit = {
    _state match {
      case Normal() =>
      case Stunned(timeLeft) =>
      case ChannelingAction(timeLeft, action) => action.onActionInterrupted
    }
    newState match {
      case Normal() => movement.allowMovement()
      case CreatureState.Stunned(timeLeft) => movement.forbidMovement()
      case ChannelingAction(timeLeft, action) => movement.forbidMovement()
    }
    _state = newState
  }
  def name:String = info.name

  def setSpeed(speed: Float) = {
    movement.setSpeed(speed)
  }

  def addGold(gold: Int): CreatureControl = {
    info.gold += gold
    this
  }

  def stun(time: Float): Unit = {
    setState(Stunned(time))
  }

  def doAction(act: CreatureAction): Unit = {
    if (act.canAct) {
      val newState = act.onActionStarts
      newState.foreach(st => setState(st))
    }
  }

  override def controlUpdate(tpf: Float): Unit = {
    _state match {
      case Normal() =>
      case Stunned(timeLeft) if timeLeft - tpf <= 0 =>
        _state = Normal()
        movement.allowMovement()
      case Stunned(timeLeft) =>
        _state = Stunned(timeLeft - tpf)
      case ChannelingAction(timeLeft, action) if timeLeft - tpf > 0 =>
        action.onActionContinues(tpf)
        _state = ChannelingAction(timeLeft - tpf, action)
      case ChannelingAction(timeLeft, action) =>
        action.onActionContinues(timeLeft)
        val actionOnEnd = action.onActionEnds(this)
        if (actionOnEnd.isEmpty) {
          _state = Normal()
          movement.allowMovement()
        } else doAction(actionOnEnd.get)

    }
  }

  override def controlRender(rm: RenderManager, vp: ViewPort): Unit = {}
}
