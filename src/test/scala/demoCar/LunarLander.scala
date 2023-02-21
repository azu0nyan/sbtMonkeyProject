package demoCar

import com.jme3.bullet.collision.shapes.{BoxCollisionShape, CompoundCollisionShape, CylinderCollisionShape}
import com.jme3.bullet.control.RigidBodyControl
import com.jme3.bullet.joints.{HingeJoint, Point2PointJoint, SixDofJoint}
import com.jme3.effect.{ParticleEmitter, ParticleMesh}
import com.jme3.input.controls.{ActionListener, AnalogListener, KeyTrigger}
import com.jme3.input.{ChaseCamera, KeyInput}
import com.jme3.material.Material
import com.jme3.math
import com.jme3.math.{ColorRGBA, Quaternion, Transform, Vector3f}
import com.jme3.renderer.{RenderManager, ViewPort}
import com.jme3.scene.Node
import com.jme3.scene.control.AbstractControl
import demoGame.MakerUtils


object LunarLander {
  def main(args: Array[String]): Unit = {
    val app = new LunarLander
    app.setShowSettings(false)
    app.start()
  }
}

class LunarLander extends AppOps {

  var ship: Ship = _
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
    ship = new Ship()
    initInput(ship)

    flyCam.setEnabled(false)

    val chaseCam = new ChaseCamera(cam, ship.shipNode, inputManager)
    chaseCam.setInvertVerticalAxis(true)
//    bulletAppState.setDebugEnabled(true)

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

  def initInput(c: Ship): Unit = {
    val list = new AnalogListener {
      override def onAnalog(name: String, value: Float, tpf: Float): Unit = {
        //println(s"$name $value $tpf")
        name match {
          case "L" =>
//            c.wheelRotation = math.max(-1, c.wheelRotation - c.wheelRotationRate * value)
          case "R" =>
//            c.wheelRotation = math.min(1, c.wheelRotation + c.wheelRotationRate * value)
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
          case "STRAIGHT_WHEEL" =>
//            c.wheelRotation = 0f
          case "F" => fP = isPressed
          case "B" => bP = isPressed
        }
        (fP, bP) match {
          case (true, true) | (false, false) => ship.gas = 0
          case (true, false) => ship.gas = 1
          case (false, true) => ship.gas = -1

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


  class Engine(val shipNode: Node, val shipRigidBodyControl: RigidBodyControl,
               val offset: Vector3f, thrustDirection: Vector3f, val radius: Float, val width: Float,
               val minPower: Float = 0f, val maxPower: Float = 0f,
               val engineMass: Float = 10f, thrustControl: () => Float) extends AbstractControl {

    //    val shape = makeCylinder(Transform.IDENTITY.setTranslation(offset), "wheel", makeShaded(ColorRGBA.Gray), Some(carNode), radius, width)
    //    carShape.addChildShape(cShape, offset)

    val engineNode = new Node("engine node")
    engineNode.setLocalTranslation(offset)
//    engineNode.setLocalRotation(Quaternion.IDENTITY.fromAngleAxis(0f, thrustDirection)) // twodo
    engineNode.setLocalRotation(new Quaternion().lookAt(thrustDirection, new Vector3f(1f, 0f, 0f))) // twodo

    val geom = makeCylinder(Transform.IDENTITY, "engine", makeShaded(ColorRGBA.Gray), Some(engineNode), radius, width)
    val cShape = new CylinderCollisionShape(new Vector3f(radius, radius, width / 2f))


   val fire = new ParticleEmitter("Emitter", ParticleMesh.Type.Triangle, 30)
    val mat_red = new Material(assetManager, "Common/MatDefs/Misc/Particle.j3md")
//    mat_red.setTexture("Texture", assetManager.loadTexture("Effects/Explosion/flame.png"))
    mat_red.setTexture("Texture", assetManager.loadTexture("part.png"))
    fire.setMaterial(mat_red)
    fire.setImagesX(1)
    fire.setImagesY(1) // 2x2 texture animation
    fire.setEndColor(new ColorRGBA(1f, 0f, 0f, 1f)) // red
    fire.setStartColor(new ColorRGBA(1f, 1f, 0f, 0.5f)) // yellow

    fire.getParticleInfluencer.setInitialVelocity(new Vector3f(0, 2, 0))
    fire.setStartSize(1.5f)
    fire.setEndSize(0.1f)
    fire.setGravity(0, 0, 0)

    fire.setLowLife(1f)
    fire.setHighLife(3f)
    fire.getParticleInfluencer.setVelocityVariation(0.3f)
    engineNode.attachChild(fire)




    engineNode.addControl(this)
    shipNode.attachChild(engineNode)


    override def controlUpdate(tpf: Float): Unit = {
      import demoCar.Vector3fImplicits.Vector3fImplicits
      val a = Array.fill(3)(new Vector3f())
      shipNode.getLocalRotation.toAxes(a)
      val localThrust = a(1)
      val gasPower = thrustControl()
      //val localThrust = engineNode.localToWorld(new Vector3f(0f, 1f, 0f), new Vector3f()).normalizeLocal()
      val imp = localThrust * tpf * (minPower  +  gasPower * (maxPower - minPower))
      println(gasPower + " " + imp)
      shipRigidBodyControl.applyImpulse(imp, offset)


     // fire.setGravity(localThrust)
      fire.setEnabled(gasPower != 0)

    }

    override def controlRender(rm: RenderManager, vp: ViewPort): Unit = {

    }



  }

  class Ship() extends AbstractControl {

    val bodyMass = 100f

    //    val wheelRotationRate: Float = 2f
    val gasRate: Float = 5f

    //    var wheelRotation: Float = 0f
    var gas: Float = 0f

    val shipNode = new Node("ship")
    shipNode.setLocalTranslation(0f, -5f, 0f)

    val centerShape = new CompoundCollisionShape

    val centerAxisSize = new Vector3f(2.0f, 0.8f, 1.0f)
    val centerAxisLocation = new Vector3f(0f, 0f, 0f)
    val centerAxis = makeBox(centerAxisLocation, centerAxisSize, "centerAxis", makeShaded(ColorRGBA.White))
    val centerAxisCollision = new BoxCollisionShape(centerAxisSize)
    centerShape.addChildShape(centerAxisCollision, centerAxisLocation)


    val frontAxisSize = new Vector3f(0.6f, 0.6f, 1.3f)
    val frontAxisLocation = new Vector3f(1.9f, -0.4f, 0f)
    val frontAxis = makeBox(frontAxisLocation, frontAxisSize, "centerAxis", makeShaded(ColorRGBA.White))
    val frontAxisCollision = new BoxCollisionShape(frontAxisSize)
    centerShape.addChildShape(frontAxisCollision, frontAxisLocation)

    val backAxisSize = new Vector3f(0.7f, 0.7f, 1.5f)
    val backAxisLocation = new Vector3f(-1.9f, -0.4f, 0f)
    val backAxis = makeBox(backAxisLocation, backAxisSize, "centerAxis", makeShaded(ColorRGBA.White))
    val backAxisCollision = new BoxCollisionShape(backAxisSize)
    centerShape.addChildShape(backAxisCollision, backAxisLocation)

    shipNode.attachChild(centerAxis)
    shipNode.attachChild(frontAxis)
    shipNode.attachChild(backAxis)


    val carBodyControl = new RigidBodyControl(centerShape, bodyMass)
    carBodyControl.setCcdMotionThreshold(0.01f)
    carBodyControl.setLinearSleepingThreshold(0f)
    carBodyControl.setAngularSleepingThreshold(0f)
    shipNode.addControl(carBodyControl)

    import Vector3fImplicits._

    val flEnginePos = frontAxisLocation + new Vector3f(0f, -frontAxisSize.y - 0.2f , frontAxisSize.z - 0.6f)
    val frEnginePos = frontAxisLocation + new Vector3f(0f,-frontAxisSize.y - 0.2f, -frontAxisSize.z + 0.6f)
    val blEnginePos = backAxisLocation + new Vector3f(0f, -frontAxisSize.y - 0.2f, backAxisSize.z - 0.6f)
    val brEnginePos = backAxisLocation + new Vector3f(0f, -frontAxisSize.y - 0.2f, -backAxisSize.z + 0.6f)

    val bottomEnginesThrustVector = new Vector3f(0, 1f, 0f)

    val engines = Seq(
      new Engine(shipNode, carBodyControl, flEnginePos, bottomEnginesThrustVector, 0.3f, 0.9f, 0f, 1000f, 10f, () => gas),
      new Engine(shipNode, carBodyControl, frEnginePos, bottomEnginesThrustVector, 0.3f, 0.9f, 0f, 1000f, 10f, () => gas),
      new Engine(shipNode, carBodyControl, blEnginePos, bottomEnginesThrustVector, 0.3f, 0.9f, 0f, 1000f, 10f, () => gas),
      new Engine(shipNode, carBodyControl, brEnginePos, bottomEnginesThrustVector, 0.3f, 0.9f, 0f, 1000f, 10f, () => gas),
      new Engine(shipNode, carBodyControl, new Vector3f(0f, 1f, 0f), bottomEnginesThrustVector.negate(), 0.3f, 0.9f, 0f, 0f, 10f, () => gas),
    )
    val Seq(flWheel, frWheel, blWheel, brWheel, _) = engines


    bulletAppState.getPhysicsSpace.add(shipNode)
    shipNode.addControl(this)
    rootNode.attachChild(shipNode)

    var debugVelocity = MakerUtils.makeArrow(Vector3f.ZERO, new Vector3f(0f, 0f, 0f), "debug arrow vel", makeUnshaded(ColorRGBA.Gray), Some(shipNode))(LunarLander.this)


    // END CONSTRUCTOR
    // END CONSTRUCTOR
    // END CONSTRUCTOR
    def massTotal: Float = bodyMass + engines.map(_.engineMass).sum


    var t = 0

    override def controlUpdate(tpf: Float): Unit = {
      t = t + 1
//      if (t < 10) {
//        ship.carBodyControl.setLinearVelocity(Vector3f.ZERO)
//        t = t + 1
//      }

      debugVelocity.removeFromParent()
      debugVelocity = MakerUtils.makeArrow(shipNode.getLocalTranslation, shipNode.getLocalTranslation + carBodyControl.getLinearVelocity,
        "debug arrow vel", makeUnshaded(ColorRGBA.Gray), Some(shipNode.getParent))(LunarLander.this)


    }

    override def controlRender(rm: RenderManager, vp: ViewPort): Unit = {

    }

  }

  override def simpleUpdate(tpf: Float): Unit = {

  }
}
