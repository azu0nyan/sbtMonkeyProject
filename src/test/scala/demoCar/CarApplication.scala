package demoCar

import com.jme3.bullet.collision.shapes.{BoxCollisionShape, CompoundCollisionShape, CylinderCollisionShape}
import com.jme3.bullet.control.RigidBodyControl
import com.jme3.bullet.joints.HingeJoint
import com.jme3.input.{ChaseCamera, KeyInput}
import com.jme3.input.controls.{AnalogListener, KeyTrigger}
import com.jme3.math.{ColorRGBA, Matrix3f, Quaternion, Transform, Vector3f}
import com.jme3.renderer.{RenderManager, ViewPort}
import com.jme3.scene.control.AbstractControl
import com.jme3.scene.control.CameraControl.ControlDirection
import com.jme3.scene.{CameraNode, Node}


object CarApplication {
  def main(args: Array[String]): Unit = {
    val app = new CarApplication
    app.setShowSettings(false)
    app.start()
  }
}

class CarApplication extends AppOps {

  var car: Car = _
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
    val floor = makeBox(new Vector3f(0f, -15f, 0f), new Vector3f(100f, 0.1f, 100f), "eath", makeUnshaded(ColorRGBA.Brown), Some(rootNode))
    makeRigid(floor, 0)


  }

  def initInput(c:Car): Unit = {
    val list = new AnalogListener {
      override def onAnalog(name: String, value: Float, tpf: Float): Unit = {
        //println(s"$name $value $tpf")
        name match {
          case "L" =>
            c.wheelRotation = math.max(-1, c.wheelRotation - c.wheelRotationRate * value)
          case "R" =>
            c.wheelRotation = math.min(1, c.wheelRotation + c.wheelRotationRate * value)
          case "F" =>
            c.gas = math.min(1, c.gas + c.gasRate * value)
          case "B" =>
            c.gas = math.max(-1, c.gas - c.gasRate * value)
        }
      }
    }

    inputManager.addListener(list, "L", "R", "F", "B")
    inputManager.addMapping("L", new KeyTrigger(KeyInput.KEY_A))
    inputManager.addMapping("R", new KeyTrigger(KeyInput.KEY_D))
    inputManager.addMapping("F", new KeyTrigger(KeyInput.KEY_W))
    inputManager.addMapping("B", new KeyTrigger(KeyInput.KEY_S))
  }


  class Wheel(val carNode: Node, val carRigidBodyControl: RigidBodyControl, val offset: Vector3f, val radius: Float, val width: Float,
              val minRotation: Float = 0f, val maxRotation:Float = 0f,
              val minPower: Float = 0f, val maxPower: Float = 0f,
              val suppressRate: Float = 1f) {

    //    val shape = makeCylinder(Transform.IDENTITY.setTranslation(offset), "wheel", makeShaded(ColorRGBA.Gray), Some(carNode), radius, width)
    //    carShape.addChildShape(cShape, offset)

    val geom = makeCylinder(Transform.IDENTITY.setTranslation(offset), "wheel", makeShaded(ColorRGBA.Gray), Some(rootNode), radius, width)
    val cShape = new CylinderCollisionShape(new Vector3f(radius, radius, width / 2f))
    val pControl = new RigidBodyControl(cShape, 10f)

    geom.addControl(pControl)
    bulletAppState.getPhysicsSpace.add(pControl)

    val joint = new HingeJoint(carRigidBodyControl, pControl, offset, new Vector3f(0, 0, 0), Vector3f.UNIT_Y, Vector3f.UNIT_Y)
    bulletAppState.getPhysicsSpace.add(joint)


    def applyWheelRotation(wheelRotation: Float): Unit = {
      //      println(s"wheel rotation $wheelRotation")
      val rot = (minRotation + maxRotation) / 2f + (maxRotation - minRotation) * wheelRotation / 2f
//      println(s"$rot")
      joint.setLimit(rot, rot)
      //      carShape.removeChildShape(cShape)
      //      carShape.addChildShape(cShape, offset, new Quaternion(Array(0f, rot, 0f)).toRotationMatrix)
    }

    def getAxis: Vector3f = {
      val axes = Array.fill(3)(new Vector3f())
      pControl.getPhysicsRotation.toAxes(axes)
      axes(1).normalize()
    }

    def getDirection: Vector3f = {
      val axes = Array.fill(3)(new Vector3f())
      pControl.getPhysicsRotation.toAxes(axes)
      axes(0).normalize()
    }

  }

  class Car() extends AbstractControl{

    val motorStrength = 1000f

    val wheelRotationRate: Float = 2f
    val gasRate:Float = 5f

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

    val backAxisSize = new Vector3f(0.2f, 0.2f, 1.5f)
    val backAxisLocation = new Vector3f(-1.9f, 0f, 0f)
    val backAxis = makeBox(backAxisLocation, backAxisSize, "centerAxis", makeShaded(ColorRGBA.White))
    val backAxisCollision = new BoxCollisionShape(backAxisSize)
    centerShape.addChildShape(backAxisCollision, backAxisLocation)

    carNode.attachChild(centerAxis)
    carNode.attachChild(frontAxis)
    carNode.attachChild(backAxis)

    val carBodyControl = new RigidBodyControl(centerShape, 100f)
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
      new Wheel(carNode, carBodyControl, flWheelPos, 0.3f, 0.1f, minRotation = -Math.PI.toFloat / 2, maxRotation =  Math.PI.toFloat / 2),
      new Wheel(carNode, carBodyControl, frWheelPos, 0.3f, 0.1f, minRotation = -Math.PI.toFloat / 2, maxRotation = Math.PI.toFloat / 2),
      new Wheel(carNode, carBodyControl, blWheelPos, 0.6f, 0.2f, minPower = -.5f, maxPower = .5f),
      new Wheel(carNode, carBodyControl, brWheelPos, 0.6f, 0.2f, minPower = -.5f, maxPower = .5f),
    )
    val Seq(flWheel, frWheel, blWheel, brWheel) = wheels


    bulletAppState.getPhysicsSpace.add(carNode)
    carNode.addControl(this)
    rootNode.attachChild(carNode)



    override def controlUpdate(tpf: Float): Unit = {
//        centerBody.get

      val vel = carBodyControl.getLinearVelocity * (140f)



      val axes = Array.fill[Vector3f](3)(new Vector3f())
      carNode.getLocalRotation.toAxes(axes)

//      carBodyControl.setLinearVelocity(Vector3f.ZERO)
      var newVel = Vector3f.ZERO
      for(w <- car.wheels){
        val velToConvert = w.getDirection ** vel / wheels.size
        val velToSuppress = w.getAxis ** vel / wheels.size

        newVel = newVel + w.getDirection * velToConvert //+ velToSuppress  //w- velToSuppress * w.suppressRate * tpf
      }
//      carBodyControl.setLinearVelocity(newVel)

      carBodyControl.clearForces()
      for(w <- car.wheels){
        w.applyWheelRotation(car.wheelRotation)

        val motorPowerPercentage =  ((w.minPower + w.maxPower) / 2f + (w.maxPower - w.minPower) * car.gas / 2f)
        carBodyControl.applyForce(axes(0) * motorPowerPercentage * car.motorStrength, new Vector3f(0, 0, 0))
      }





//      println(s"gas ${car.gas} ${axes(0)} $tpf ")
//

    }

    override def controlRender(rm: RenderManager, vp: ViewPort): Unit = {

    }

  }

  override def simpleUpdate(tpf: Float): Unit = {

  }
}
