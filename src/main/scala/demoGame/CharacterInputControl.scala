package demoGame

import com.jme3.app.SimpleApplication
import com.jme3.bullet.control.BetterCharacterControl
import com.jme3.input.KeyInput
import com.jme3.input.controls.{ActionListener, KeyTrigger}
import com.jme3.math.Vector3f
import com.jme3.renderer.{RenderManager, ViewPort}
import com.jme3.scene.control.AbstractControl
import JmeImplicits3FHelper._
import demoGame.gameplay.actions.Fireball.Fireball
import demoGame.gameplay.actions.GeometricExplosion.GeometricExplosion
import demoGame.gameplay.{CreatureControl, CreatureMovement, CreatureMovementControl}

class CharacterInputControl(creatureMovement:CreatureMovement, creatureControl:CreatureControl)(implicit app:SimpleApplication) extends AbstractControl with ActionListener{
  var isLeft:Boolean = false
  var isRight:Boolean = false
  var isForward:Boolean = false
  var isBackward:Boolean = false


  app.getInputManager.addMapping("chLeft", new KeyTrigger(KeyInput.KEY_A))
  app.getInputManager.addMapping("chRight", new KeyTrigger(KeyInput.KEY_D))
  app.getInputManager.addMapping("chForward", new KeyTrigger(KeyInput.KEY_W))
  app.getInputManager.addMapping("chBackward", new KeyTrigger(KeyInput.KEY_S))
  app.getInputManager.addMapping("chJump", new KeyTrigger(KeyInput.KEY_SPACE))
  app.getInputManager.addMapping("chCast1", new KeyTrigger(KeyInput.KEY_E))
  app.getInputManager.addMapping("chCast2", new KeyTrigger(KeyInput.KEY_Q))

  app.getInputManager.addListener(this, "chCast1","chCast2", "chLeft", "chRight", "chForward", "chBackward", "chJump")

  def cameraDirFlattened:Vector3f = {
    val camDir = app.getCamera.getDirection.clone()
    camDir.y = 0;
    camDir
  }

  override def controlUpdate(tpf: Float): Unit = {
    val camDir = cameraDirFlattened
    val camLeft = app.getCamera.getLeft.clone()
    camLeft.y = 0
    camDir.negateLocal()
    camDir.normalizeLocal()
    camLeft.normalizeLocal()

    val dir:Vector3f = new Vector3f()
    if(isLeft) dir += camLeft
    if(isRight) dir -= camLeft
    if(isForward) dir -= camDir
    if(isBackward) dir += camDir

    creatureMovement.setMoveDirection(dir.normalize)
    creatureMovement.setSightDirection(camDir.negate())
  }

  override def controlRender(rm: RenderManager, vp: ViewPort): Unit = {}
  override def onAction(name: String, isPressed: Boolean, tpf: Float): Unit = {
    name match {
      case "chLeft" => isLeft = isPressed
      case "chRight" => isRight = isPressed
      case "chForward" => isForward = isPressed
      case "chBackward" => isBackward = isPressed
      case "chCast1" if isPressed =>
        creatureControl.doAction(new GeometricExplosion(cameraDirFlattened, creatureControl))
      case "chCast2" if isPressed =>
        creatureControl.doAction(new Fireball(cameraDirFlattened, creatureControl))
      case "chJump" if isPressed =>
        creatureMovement.jumpNow()
      case _ =>
    }
  }
}
