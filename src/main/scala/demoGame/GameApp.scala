package demoGame

import com.jme3.app.SimpleApplication
import com.jme3.bullet.BulletAppState
import com.jme3.math.{ColorRGBA, FastMath, Matrix3f, Quaternion, Ray, Vector3f}
import com.jme3.system.AppSettings
import Vector3FHelper._
import com.jme3.collision.CollisionResults
import com.jme3.input.{ChaseCamera, KeyInput}
import com.jme3.input.controls.KeyTrigger
import com.jme3.scene.{Geometry, Mesh, Node, Spatial}

import java.io.File
import javax.imageio.ImageIO

object GameApp {
  def main(args: Array[String]): Unit = {
    val app = new GameApp()
    val setting = new AppSettings(true)
    setting.setWidth(1280)
    setting.setHeight(720)
    app.setSettings(setting)
    app.setShowSettings(false)
    app.start() // start the game
  }
}

class GameApp extends SimpleApplication {
  implicit val app: SimpleApplication = this
  implicit var bulletAppState: BulletAppState = _


  override def simpleInitApp(): Unit = {
    //    flyCam.setMoveSpeed(100)


    bulletAppState = new BulletAppState()
    stateManager.attach(bulletAppState)
    val sun = GraphicsUtils.addSun()
    GraphicsUtils.addAmbient()
    GraphicsUtils.addShadows(sun)
    GraphicsUtils.addSSAO()
    GraphicsUtils.addSkyBox()
    initMap()
    val char = initCharacter()


//    cam.setLocation(new Vector3f(0f, 100f, 0f))
    //    cam.lookAt(0f, new Vector3f(0, 1, 0))
    flyCam.setEnabled(false)
    val chaseCam = new ChaseCamera(cam, char, inputManager)
    chaseCam.setInvertVerticalAxis(true)
    chaseCam.setDragToRotate(true)

    chaseCam.setLookAtOffset(new Vector3f(0, 3, 0))

//    import com.jme3.input.CameraInput
//    inputManager.deleteMapping(CameraInput.CHASECAM_TOGGLEROTATE)
//    chaseCam.setMinVerticalRotation(-FastMath.PI / 2)
//    chaseCam.setDefaultVerticalRotation(-FastMath.PI / 2)
//    chaseCam.setDefaultHorizontalRotation(-FastMath.PI / 2)

//    chaseCam.setRo
    //    bulletAppState.setDebugEnabled(true)
  }


  override def simpleUpdate(tpf: Float): Unit = {
    //    val rot = new Quaternion().fromAngleAxis(math.random().toFloat, new Vector3f(math.random().toFloat, math.random().toFloat, math.random().toFloat))
    //    println(rot)
    //    gg.setLocalRotation(rot)
    //    gg.setLocalScale(math.random().toFloat * 10)
  }


  var gg: Geometry = _
  def initCharacter(): Spatial = {
    val r = new Ray(new Vector3f(0, 100, 0), new Vector3f(0, -1, 0))
    val res = new CollisionResults
    rootNode.collideWith(r, res)
    val pos = if (res.size() >= 1) {
      res.getClosestCollision.getContactPoint + new Vector3f(0f, .75f, 0f)
    } else new Vector3f(0f, 2f, 0f)

    val g = MakerUtils.makeCylinder(new Vector3f(0f, .75f, 0f), .5f, 1.5f, "player", MakerUtils.makeShaded(ColorRGBA.Pink))
    val rot = new Quaternion().fromAngleAxis(FastMath.HALF_PI, new Vector3f(1, 0, 0))
    g.setLocalRotation(rot)

    val dummyParent = new Node()
    rootNode.attachChild(dummyParent)
    g.removeFromParent()
    dummyParent.attachChild(g)
    val cc = MakerUtils.makeCharacterControl(dummyParent)
    //    cc.setWalkDirection(new Vector3f(1f, 0f, 1f))
    cc.setJumpForce(new Vector3f(0, 50, 0))
    cc.warp(pos)

    dummyParent.addControl(new CharacterInputControl(cc))
    dummyParent

  }

  def initMap() = {
    val mapSize = 100f

    val mapImg = ImageIO.read(getClass.getClassLoader.getResource("maps/lvl1.png"))
    val (msx, msy) = (mapImg.getWidth, mapImg.getHeight)
    val blockSize = mapSize / msx
    val map: IndexedSeq[IndexedSeq[Int]] =
      for (i <- 0 until msx) yield for (j <- 0 until msy) yield mapImg.getRGB(i, j)
    val floor = MakerUtils.makeBox(new Vector3f(0f, -0.1f, 0f), new Vector3f(mapSize / 2, 0.1f, mapSize / 2), "floor", MakerUtils.makeShaded(ColorRGBA.Gray))
    MakerUtils.makeRigid(floor, 0)

    val angle = new Vector3f(-mapSize / 2f, blockSize / 2f, -mapSize / 2f) + new Vector3f(blockSize / 2f, 0f, blockSize / 2f)

    for (i <- 0 until msx; j <- 0 until msy) {
      if ((0xFF000000 & map(i)(j)) != 0) {
        val pos = angle + new Vector3f(blockSize, 0, blockSize) * new Vector3f(i.toFloat, 0f, j.toFloat)
        val c = ColorUtils.colorRGBAFromInt(map(i)(j))
        val b = MakerUtils.makeBox(pos, blockSize / 2f, "wall", MakerUtils.makeShaded(c))
        MakerUtils.makeRigid(b, 0f)
      }

    }
  }


}
