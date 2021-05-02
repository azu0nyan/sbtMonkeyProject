package demoGame

import com.jme3.app.SimpleApplication
import com.jme3.bullet.control.BetterCharacterControl
import com.jme3.input.KeyInput
import com.jme3.input.controls.{ActionListener, KeyTrigger}
import com.jme3.math.Vector3f
import com.jme3.renderer.{RenderManager, ViewPort}
import com.jme3.scene.control.AbstractControl

import JmeImplicits3FHelper._

class CharacterInputControl(character:BetterCharacterControl)(implicit app:SimpleApplication) extends AbstractControl with ActionListener{
  var isLeft:Boolean = false
  var isRight:Boolean = false
  var isForward:Boolean = false
  var isBackward:Boolean = false
  var speed:Float = 25f

  app.getInputManager.addMapping("chLeft", new KeyTrigger(KeyInput.KEY_A))
  app.getInputManager.addMapping("chRight", new KeyTrigger(KeyInput.KEY_D))
  app.getInputManager.addMapping("chForward", new KeyTrigger(KeyInput.KEY_W))
  app.getInputManager.addMapping("chBackward", new KeyTrigger(KeyInput.KEY_S))
  app.getInputManager.addMapping("chJump", new KeyTrigger(KeyInput.KEY_SPACE))

  app.getInputManager.addListener(this, "chLeft", "chRight", "chForward", "chBackward", "chJump")
  override def controlUpdate(tpf: Float): Unit = {
    val camDir = app.getCamera.getDirection.clone()
    val camLeft = app.getCamera.getLeft.clone()
    camDir.y = 0
    camLeft.y = 0
    camDir.negateLocal()
    camDir.normalizeLocal()
    camLeft.normalizeLocal()

    val dir:Vector3f = new Vector3f()
    if(isLeft) dir += camLeft
    if(isRight) dir -= camLeft
    if(isForward) dir -= camDir
    if(isBackward) dir += camDir

    character.setWalkDirection(dir.normalize.mult(speed))
    character.setViewDirection(dir.normalize())
  }

  override def controlRender(rm: RenderManager, vp: ViewPort): Unit = {}
  override def onAction(name: String, isPressed: Boolean, tpf: Float): Unit = {
    name match {
      case "chLeft" => isLeft = isPressed
      case "chRight" => isRight = isPressed
      case "chForward" => isForward = isPressed
      case "chBackward" => isBackward = isPressed
      case "chJump" if isPressed =>
        character.jump()
      case _ =>
    }
  }
}
