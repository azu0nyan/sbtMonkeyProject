package demoCar

import com.jme3.bullet.collision.shapes.{BoxCollisionShape, CompoundCollisionShape, CylinderCollisionShape}
import com.jme3.bullet.control.RigidBodyControl
import com.jme3.bullet.joints.HingeJoint
import com.jme3.input.{ChaseCamera, KeyInput}
import com.jme3.input.controls.{ActionListener, AnalogListener, KeyTrigger}
import com.jme3.math.{ColorRGBA, Matrix3f, Quaternion, Transform, Vector3f}
import com.jme3.renderer.{RenderManager, ViewPort}
import com.jme3.scene.control.AbstractControl
import com.jme3.scene.control.CameraControl.ControlDirection
import com.jme3.scene.{CameraNode, Node}
import demoGame.MakerUtils


object CarApplication {
  def main(args: Array[String]): Unit = {
    val app = new CarApplication
    app.setShowSettings(false)
    app.start()
  }
}

class CarApplication extends AppOps {

  import Vector3fImplicits._

  var car: Car = _
  var showDebug: Boolean = true

  override def simpleInitApp(): Unit = {
    flyCam.setMoveSpeed(40)

    val sun = addSun()
    addAmbient()
    addShadows(sun)
    addSSAO()
    addSkyBox()
    initBullet()

    loadEnvir()
    car = new Car()
    initInput(car)

    flyCam.setEnabled(false)

    val chaseCam = new ChaseCamera(cam, car.carNode, inputManager)
    chaseCam.setInvertVerticalAxis(true)
    bulletAppState.setDebugEnabled(true)

    //    val camNode = new CameraNode("Cam node", cam)
    //    camNode.setControlDir(ControlDirection.SpatialToCamera)
    //    camNode.setLocalTranslation(new Vector3f(0, 5, -5))
    //    camNode.lookAt(car.getLocalTranslation, Vector3f.UNIT_Y)


  }


  def loadEnvir(): Unit = {
    //    makeTerrain()
    val floor = makeBox(new Vector3f(0f, -15f, 0f), new Vector3f(100f, 0.1f, 100f), "eath",
      makeShadedTextured("checker.png", "/Textures/asphalt/TexturesCom_Asphalt_Base4_2x2_1K_normal.png"),
      Some(rootNode))
    makeRigid(floor, 0)


  }

  def initInput(c: Car): Unit = {
    val list = new AnalogListener {
      override def onAnalog(name: String, value: Float, tpf: Float): Unit = {
        //println(s"$name $value $tpf")
        name match {
          case "L" =>
            c.wheelRotation = math.max(-1, c.wheelRotation - c.wheelRotationRate * value)
          case "R" =>
            c.wheelRotation = math.min(1, c.wheelRotation + c.wheelRotationRate * value)
          case "F" =>
          //            c.gas = math.min(1, c.gas + c.gasRate * value)
          //            c.gas = math.min(1, c.gas + c.gasRate * value)
          case "B" =>
          //            c.gas = math.max(-1, c.gas - c.gasRate * value)
        }
      }
    }
    val list2 = new ActionListener {
      var fP = false
      var bP = false

      override def onAction(name: String, isPressed: Boolean, tpf: Float): Unit = {
        name match {
          case "STRAIGHT_WHEEL" => c.wheelRotation = 0f
          case "F" => fP = isPressed
          case "B" => bP = isPressed
        }
        (fP, bP) match {
          case (true, true) | (false, false) => car.gas = 0
          case (true, false) => car.gas = 1
          case (false, true) => car.gas = -1

        }
      }
    }

    inputManager.addListener(list, "L", "R")
    inputManager.addListener(list2, "F", "B", "STRAIGHT_WHEEL")
    inputManager.addMapping("STRAIGHT_WHEEL", new KeyTrigger(KeyInput.KEY_X))
    inputManager.addMapping("L", new KeyTrigger(KeyInput.KEY_A))
    inputManager.addMapping("R", new KeyTrigger(KeyInput.KEY_D))
    inputManager.addMapping("F", new KeyTrigger(KeyInput.KEY_W))
    inputManager.addMapping("B", new KeyTrigger(KeyInput.KEY_S))
  }


  class Wheel(val carNode: Node, val carRigidBodyControl: RigidBodyControl, val offset: Vector3f, val radius: Float, val width: Float,
              val minRotation: Float = 0f, val maxRotation: Float = 0f,
              val minPower: Float = 0f, val maxPower: Float = 0f,
              val gripForce: Float = 500f,
              val dampingForce: Float = 5f,
              val wheelMass: Float = 10f) extends AbstractControl {

    //    val shape = makeCylinder(Transform.IDENTITY.setTranslation(offset), "wheel", makeShaded(ColorRGBA.Gray), Some(carNode), radius, width)
    //    carShape.addChildShape(cShape, offset)

    val wheelNode = new Node("wheel node")
    wheelNode.setLocalTranslation(offset)

    val geom = makeCylinder(Transform.IDENTITY, "wheel", makeShaded(ColorRGBA.Gray), Some(wheelNode), radius, width)
    val cShape = new CylinderCollisionShape(new Vector3f(radius, radius, width / 2f))
    val pControl = new RigidBodyControl(cShape, wheelMass)
    pControl.setFriction(0.0f)

    wheelNode.addControl(pControl)
    wheelNode.addControl(this)
    carNode.attachChild(wheelNode)
    bulletAppState.getPhysicsSpace.add(pControl)

    //connect to car
    val joint = new HingeJoint(carRigidBodyControl, pControl, offset, new Vector3f(0, 0, 0), Vector3f.UNIT_Y, Vector3f.UNIT_Y)
    bulletAppState.getPhysicsSpace.add(joint)

    var debugArrowX = MakerUtils.makeArrow(Vector3f.ZERO, new Vector3f(1f, 0f, 0f), "debug arrow x", makeUnshaded(ColorRGBA.Red), Some(wheelNode))(CarApplication.this)
    var debugArrowY = MakerUtils.makeArrow(Vector3f.ZERO, new Vector3f(0f, 1f, 0f), "debug arrow y", makeUnshaded(ColorRGBA.Green), Some(wheelNode))(CarApplication.this)
    var debugArrowZ = MakerUtils.makeArrow(Vector3f.ZERO, new Vector3f(0f, 0f, 1f), "debug arrow z", makeUnshaded(ColorRGBA.Blue), Some(wheelNode))(CarApplication.this)

    def applyWheelRotation(wheelRotation: Float): Unit = {
      //      println(s"wheel rotation $wheelRotation")
      val rot = (minRotation + maxRotation) / 2f + (maxRotation - minRotation) * wheelRotation / 2f
      //      println(s"$rot")
      joint.setLimit(rot, rot)
      //      carShape.removeChildShape(cShape)
      //      carShape.addChildShape(cShape, offset, new Quaternion(Array(0f, rot, 0f)).toRotationMatrix)
    }

    override def controlUpdate(tpf: Float): Unit = {
      //      debugArrowX.removeFromParent()
      //      debugArrowY.removeFromParent()
      //      debugArrowZ.removeFromParent()
      //      println(getDirection)
      //      debugArrowX = MakerUtils.makeArrow(offset, offset + getDirection, "debug arrow x", makeUnshaded(ColorRGBA.Red), Some(carNode))(CarApplication.this)
      //      debugArrowY = MakerUtils.makeArrow(offset, offset +  getAxis, "debug arrow x", makeUnshaded(ColorRGBA.Green), Some(carNode))(CarApplication.this)
      //      debugArrowZ = MakerUtils.makeArrow(offset, offset + getUp, "debug arrow x", makeUnshaded(ColorRGBA.Blue), Some(carNode))(CarApplication.this)


    }

    override def controlRender(rm: RenderManager, vp: ViewPort): Unit = {

    }

    def getUp: Vector3f = {
      val axes = Array.fill(3)(new Vector3f())
      pControl.getPhysicsRotation.toAxes(axes)
      axes(1).normalize()
    }

    def getAxis: Vector3f = {
      val axes = Array.fill(3)(new Vector3f())
      pControl.getPhysicsRotation.toAxes(axes)
      axes(2).normalize()
    }

    def getDirection: Vector3f = {
      val axes = Array.fill(3)(new Vector3f())
      pControl.getPhysicsRotation.toAxes(axes)
      axes(0).normalize()
    }

  }

  class Car() extends AbstractControl {

    val motorStrength = 1000f
    val carBodyMass = 100f

    val wheelRotationRate: Float = 2f
    val gasRate: Float = 5f

    var wheelRotation: Float = 0f
    var gas: Float = 0f

    val carNode = new Node("car")
    carNode.setLocalTranslation(0f, -5f, 0f)

    val centerShape = new CompoundCollisionShape

    val centerAxisSize = new Vector3f(2.0f, 0.2f, 0.2f)
    val centerAxisLocation = new Vector3f(0f, 0f, 0f)
    val centerAxis = makeBox(centerAxisLocation, centerAxisSize, "centerAxis", makeShaded(ColorRGBA.White))
    val centerAxisCollision = new BoxCollisionShape(centerAxisSize)
    centerShape.addChildShape(centerAxisCollision, centerAxisLocation)


    val frontAxisSize = new Vector3f(0.1f, 0.1f, 1f)
    val frontAxisLocation = new Vector3f(1.9f, 0f, 0f)
    val frontAxis = makeBox(frontAxisLocation, frontAxisSize, "centerAxis", makeShaded(ColorRGBA.White))
    val frontAxisCollision = new BoxCollisionShape(frontAxisSize)
    centerShape.addChildShape(frontAxisCollision, frontAxisLocation)

    val backAxisSize = new Vector3f(0.2f, 0.2f, 1f)
    val backAxisLocation = new Vector3f(-1.9f, 0f, 0f)
    val backAxis = makeBox(backAxisLocation, backAxisSize, "centerAxis", makeShaded(ColorRGBA.White))
    val backAxisCollision = new BoxCollisionShape(backAxisSize)
    centerShape.addChildShape(backAxisCollision, backAxisLocation)

    carNode.attachChild(centerAxis)
    carNode.attachChild(frontAxis)
    carNode.attachChild(backAxis)


    val carBodyControl = new RigidBodyControl(centerShape, carBodyMass)
    carBodyControl.setCcdMotionThreshold(0.01f)
    carBodyControl.setLinearSleepingThreshold(0f)
    carBodyControl.setAngularSleepingThreshold(0f)
    carNode.addControl(carBodyControl)

    import Vector3fImplicits._

    val flWheelPos = frontAxisLocation + new Vector3f(0f, 0f, frontAxisSize.z + 0.2f)
    val frWheelPos = frontAxisLocation + new Vector3f(0f, 0f, -frontAxisSize.z - 0.2f)
    val blWheelPos = backAxisLocation + new Vector3f(0f, 0f, backAxisSize.z + 0.3f)
    val brWheelPos = backAxisLocation + new Vector3f(0f, 0f, -backAxisSize.z - 0.3f)
    val wheels = Seq(
      new Wheel(carNode, carBodyControl, flWheelPos, 0.3f, 0.1f, minRotation = -Math.PI.toFloat / 2, maxRotation = Math.PI.toFloat / 2),
      new Wheel(carNode, carBodyControl, frWheelPos, 0.3f, 0.1f, minRotation = -Math.PI.toFloat / 2, maxRotation = Math.PI.toFloat / 2),
      new Wheel(carNode, carBodyControl, blWheelPos, 0.6f, 0.2f, minPower = -.5f, maxPower = .5f),
      new Wheel(carNode, carBodyControl, brWheelPos, 0.6f, 0.2f, minPower = -.5f, maxPower = .5f),
    )
    val Seq(flWheel, frWheel, blWheel, brWheel) = wheels


    bulletAppState.getPhysicsSpace.add(carNode)
    carNode.addControl(this)
    rootNode.attachChild(carNode)

    var debugVelocity = MakerUtils.makeArrow(Vector3f.ZERO, new Vector3f(0f, 0f, 0f), "debug arrow vel", makeUnshaded(ColorRGBA.Gray), Some(carNode))(CarApplication.this)


    // END CONSTRUCTOR
    // END CONSTRUCTOR
    // END CONSTRUCTOR
    def massTotal: Float = carBodyMass + wheels.map(_.wheelMass).sum


    var t = 0
    override def controlUpdate(tpf: Float): Unit = {
      if(t < 10){
        car.carBodyControl.setLinearVelocity(Vector3f.ZERO)
        t =t +1
      }

      debugVelocity.removeFromParent()
      debugVelocity = MakerUtils.makeArrow(carNode.getLocalTranslation, carNode.getLocalTranslation + carBodyControl.getLinearVelocity,
        "debug arrow vel", makeUnshaded(ColorRGBA.Gray), Some(carNode.getParent))(CarApplication.this)


      //        centerBody.get
      print(s"Car thrust ${car.gas} ")

      val impulse = carBodyControl.getLinearVelocity * carBodyMass / wheels.size
      //      println(carBodyControl.getLinearVelocity)
      carBodyControl.setLinearVelocity(Vector3f.ZERO)
      //carBodyControl.applyImpulse(impulse, Vector3f.ZERO)
      for (w <- car.wheels) {
        val impulseCollinear = w.getDirection ** impulse //brake here
        val impulseOrthogonal = w.getAxis ** impulse
        val impulseUp = w.getUp ** impulse

        val collinearDelta = tpf * w.dampingForce
        val resultingCollinearlImpulse =
          if (impulseCollinear > 0) math.max(0, impulseCollinear - collinearDelta)
          else math.min(0, impulseCollinear + collinearDelta)

        val orthogonalDelta = tpf * w.gripForce
        val resultingOrthogonalImpulse =
          if (impulseOrthogonal > 0) math.max(0, impulseOrthogonal - orthogonalDelta)
          else math.min(0, impulseOrthogonal + orthogonalDelta)

        println(impulseOrthogonal + " " + tpf + " " + orthogonalDelta)

        //        println(carBodyControl.getLinearVelocity)
        carBodyControl.applyImpulse(w.getDirection * resultingCollinearlImpulse, w.offset)
        carBodyControl.applyImpulse(w.getAxis * resultingOrthogonalImpulse, w.offset)
        carBodyControl.applyImpulse(w.getUp * impulseUp, w.offset)
        //        println(carBodyControl.getLinearVelocity)
      }

      /*val vel = carBodyControl.getLinearVelocity
      carBodyControl.setLinearVelocity(Vector3f.ZERO)
      var newVel = Vector3f.ZERO
      for (w <- car.wheels) {
        val velocityCollinear = w.getDirection ** vel / wheels.size
        val velocityOrtogonal = w.getAxis ** vel / wheels.size
        val velocityUp = w.getUp ** vel / wheels.size
        print(f" $velocityOrtogonal%.2f $velocityCollinear%.2f")
        newVel = newVel + w.getUp * velocityUp
        newVel = newVel + w.getDirection * velocityCollinear
        //newVel = newVel + velocityOrtogonal  //w- velocityOrtogonal * w.suppressRate * tpf
      }
      carBodyControl.setLinearVelocity(newVel)
      */

      //carBodyControl.clearForces()
      for (w <- car.wheels) {
        w.applyWheelRotation(car.wheelRotation)

        val motorPowerPercentage = ((w.minPower + w.maxPower) / 2f + (w.maxPower - w.minPower) * car.gas / 2f)
        carBodyControl.applyImpulse(w.getDirection * motorPowerPercentage * car.motorStrength * tpf, w.offset)
      }

      val cVel = carBodyControl.getLinearVelocity
      for(w <- car.wheels){
        w.pControl.setLinearVelocity(cVel)
      }


      println()

    }

    override def controlRender(rm: RenderManager, vp: ViewPort): Unit = {

    }

  }

  override def simpleUpdate(tpf: Float): Unit = {

  }
}
