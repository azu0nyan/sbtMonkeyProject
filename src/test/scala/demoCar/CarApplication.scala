package demoCar

import com.jme3.bullet.collision.shapes.{BoxCollisionShape, CompoundCollisionShape}
import com.jme3.bullet.control.RigidBodyControl
import com.jme3.math.{ColorRGBA, Transform, Vector3f}
import com.jme3.scene.Node
import Vector3fImplicits.Vector3fImplicits


object CarApplication {
  def main(args: Array[String]): Unit = {
    val app = new CarApplication
    app.setShowSettings(false)
    app.start()
  }
}
class CarApplication extends AppOps {


  override def simpleInitApp(): Unit = {
    flyCam.setMoveSpeed(100)

    val sun = addSun()
    addAmbient()
    addShadows(sun)
    addSSAO()
    addSkyBox()
    initBullet()

    loadEnvir()

  }

  def loadEnvir(): Unit = {
//    makeTerrain()
    val floor = makeBox(new Vector3f(0f, -15f, 0f), new Vector3f(100f, 0.1f, 100f), "eath", makeUnshaded(ColorRGBA.Brown), Some(rootNode))
    makeRigid(floor, 0)

    loadCar()
  }

  def makeWheel(carNode:Node, offset: Vector3f, radius: Float, width: Float): Unit = {
      val shape = makeCylinder(Transform.IDENTITY.setTranslation(offset), "wheel", makeShaded(ColorRGBA.Gray), Some(carNode), radius, width)

  }
  def loadCar(): Unit = {
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
    
    val backAxisSize = new Vector3f(0.1f, 0.1f, 1f)
    val backAxisLocation = new Vector3f(-1.9f, 0f, 0f)
    val backAxis = makeBox(backAxisLocation, backAxisSize, "centerAxis", makeShaded(ColorRGBA.White))
    val backAxisCollision = new BoxCollisionShape(backAxisSize)
    centerShape.addChildShape(backAxisCollision, backAxisLocation)

    carNode.attachChild(centerAxis)
    carNode.attachChild(frontAxis)
    carNode.attachChild(backAxis)

    val centerBody = new RigidBodyControl(centerShape, 100f)
    centerBody.setCcdMotionThreshold(0.01f)
    carNode.addControl(centerBody)

    bulletAppState.getPhysicsSpace.add(carNode)
    rootNode.attachChild(carNode)
  }

  override def simpleUpdate(tpf: Float): Unit = {

  }
}
