package demoGame.gameplay

import com.jme3.app.SimpleApplication
import com.jme3.bullet.{PhysicsSpace, PhysicsTickListener}
import com.jme3.bullet.control.BetterCharacterControl
import com.jme3.math.Vector3f
import com.jme3.renderer.{RenderManager, ViewPort}
import com.jme3.scene.control.AbstractControl
import demoGame.JmeImplicits3FHelper._
import demoGame.{CharacterInputControl, NavigationControl}

trait CreatureMovement{
  def setSightDirection(vector3f: Vector3f): Unit
  def maxSpeed:Float
  def setSpeed(sp:Float):Unit
  def forbidMovement():Unit
  def allowMovement():Unit
  def jumpNow():Unit
  def setMoveDirection(dirWithCel:Vector3f):Unit
}

/**
 * Позволяет включать/выключать перемещение персонажа вне зависимости от того игрок это или нет
 */
class CreatureMovementControl(radius: Float,
                              height: Float,
                              mass: Float,
                              private var _maxSpeed: Float,
                              private var movementEnabled: Boolean = true,
                             ) extends BetterCharacterControl(radius, height, mass)  with CreatureMovement {

  def maxSpeed: Float = _maxSpeed

  def setSpeed(sp: Float): Unit = {
    _maxSpeed = sp
  }

  def forbidMovement(): Unit = {
    movementEnabled = false
    setWalkDirection(Vector3f.ZERO)
  }

  def allowMovement(): Unit = {
    movementEnabled = true
  }

  override def jumpNow(): Unit = if(movementEnabled) jump()

  override def setMoveDirection(dirWithCel: Vector3f): Unit = {
    if(movementEnabled) {
      val vec = if (dirWithCel.lengthSquared() <= 1f) dirWithCel * _maxSpeed else dirWithCel.normalize() * _maxSpeed
      setWalkDirection(vec)
    }
  }
  override def setSightDirection(vector3f: Vector3f): Unit = {
    setViewDirection(vector3f)
  }
}



