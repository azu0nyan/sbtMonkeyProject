package demoGame.gameplay

import com.jme3.app.SimpleApplication
import com.jme3.bullet.{PhysicsSpace, PhysicsTickListener}
import com.jme3.bullet.control.BetterCharacterControl
import com.jme3.bullet.objects.PhysicsRigidBody
import com.jme3.math.{FastMath, MathUtils, Quaternion, Vector2f, Vector3f}
import com.jme3.renderer.{RenderManager, ViewPort}
import com.jme3.scene.control.AbstractControl
import demoGame.JmeImplicits3FHelper._
import demoGame.{CharacterInputControl, MyMathUtils, NavigationControl}

trait CreatureMovement{
  def setSightDirection(vector3f: Vector3f): Unit
  def maxSpeed:Float
  def setSpeed(sp:Float):Unit
  def forbidMovement():Unit
  def allowMovement():Unit
  def jumpNow():Unit
  def setMoveDirection(dirWithCel:Vector3f):Unit

  def controlledRigidBody:PhysicsRigidBody
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
  val rotationAngleTolerance = .01f
  var turnRate = FastMath.PI
  private var targetSightDirection:Vector3f = getViewDirection

  def maxSpeed: Float = _maxSpeed

  def setSpeed(sp: Float): Unit = {
    _maxSpeed = sp
  }

  def forbidMovement(): Unit = {
    movementEnabled = false
    setWalkDirection(Vector3f.ZERO)
    controlledRigidBody.setLinearVelocity(Vector3f.ZERO)
  }

  def allowMovement(): Unit = {
    movementEnabled = true
    setWalkDirection(Vector3f.ZERO)
  }

  override def prePhysicsTick(space: PhysicsSpace, tpf: Float): Unit = {
    if(movementEnabled) {
      super.prePhysicsTick(space, tpf)
    }
  }

  override def update(tpf: Float): Unit = {
    val target = targetSightDirection.planeProjection
    val current = getViewDirection.planeProjection
    val toRotateAngle = MyMathUtils.directedSmallestAngle(target, current)//target.angleBetween(current)
    if(math.abs(toRotateAngle) > rotationAngleTolerance){
      val sign = math.signum(toRotateAngle)
      val a = sign * math.min(toRotateAngle * sign, turnRate * tpf)
      val q = new Quaternion().fromAngleAxis(a, Vector3f.UNIT_Y)
      val dir = q.mult(getViewDirection)
      setViewDirection(dir)
    }
    super.update(tpf)
  }

  override def jumpNow(): Unit = if(movementEnabled) jump()

  override def setMoveDirection(dirWithCel: Vector3f): Unit = {
    if(movementEnabled) {
      val vec = if (dirWithCel.lengthSquared() <= 1f) dirWithCel * _maxSpeed else dirWithCel.normalize() * _maxSpeed
      setWalkDirection(vec)
    }
  }
  override def setSightDirection(vector3f: Vector3f): Unit = {
    targetSightDirection = vector3f.clone()
    targetSightDirection.normalizeLocal()
//    setViewDirection(vector3f)
  }

 override def controlledRigidBody:PhysicsRigidBody = rigidBody
}



