package demoGame

import com.jme3.app.SimpleApplication
import com.jme3.bullet.BulletAppState
import com.jme3.math.{ColorRGBA, Vector3f}
import com.jme3.system.AppSettings
import Vector3FHelper._

import java.io.File
import javax.imageio.ImageIO

object GameApp {
  def main(args: Array[String]): Unit = {
    val app = new GameApp()
    app.setShowSettings(false)
    app.start() // start the game
  }
}

class GameApp extends SimpleApplication {
  implicit val app: SimpleApplication = this
  implicit var bulletAppState: BulletAppState = _


  override def simpleInitApp(): Unit = {
    bulletAppState = new BulletAppState()
    stateManager.attach(bulletAppState)
    val sun = GraphicsUtils.addSun()
    GraphicsUtils.addAmbient()
    GraphicsUtils.addShadows(sun)
    GraphicsUtils.addSSAO()
    GraphicsUtils.addSkyBox()
    initMap()
  }


  override def simpleUpdate(tpf: Float): Unit = {

  }


  val mapSize = 100f

  val mapImg = ImageIO.read(getClass().getClassLoader().getResource("maps/lvl1.png"))
  val (msx, msy) = (mapImg.getWidth, mapImg.getHeight)
  val blockSize = mapSize / msx
  val map: IndexedSeq[IndexedSeq[Int]] =
    for (i <- 0 until msx) yield for (j <- 0 until msy) yield mapImg.getRGB(i, j)

  def initMap() = {
    val floor = MakerUtils.makeBox(0f, new Vector3f(mapSize / 2, 0, mapSize / 2), "floor", MakerUtils.makeShaded(ColorRGBA.Gray))
    MakerUtils.makeRigid(floor, 0)

    val angle = new Vector3f(mapSize / 2f, 0, mapSize / 2f) + new Vector3f(blockSize / 2f, 0f, blockSize / 2f)

    for (i <- 0 until msx; j <- 0 until msy) {
      if((0xFF000000 & map(i)(j)) != 0) {
        val pos = angle + new Vector3f(blockSize, 0, blockSize) * new Vector3f(i.toFloat, 0f, j.toFloat)
        val c = ColorUtils.colorRGBAFromInt(map(i)(j))
        MakerUtils.makeBox(pos, blockSize, "wall", MakerUtils.makeShaded(c))
      }

    }
  }


}
