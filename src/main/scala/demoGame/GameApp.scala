package demoGame

import com.jme3.app.SimpleApplication
import com.jme3.bullet.BulletAppState
import com.jme3.math.{ColorRGBA, FastMath, Matrix3f, Quaternion, Ray, Vector3f}
import com.jme3.system.AppSettings
import JmeImplicits3FHelper._
import com.jme3.bullet.control.BetterCharacterControl
import com.jme3.collision.CollisionResults
import com.jme3.input.{ChaseCamera, KeyInput}
import com.jme3.input.controls.KeyTrigger
import com.jme3.scene.{Geometry, Mesh, Node, Spatial}
import demoGame.GameApp.getClass
import demoGame.gameplay.GameLevelAppState
import demoGame.graphics.{GraphicsUtils, SetColorFromTime}
import demoGame.ui.UiAppState

import java.io.File
import java.util.logging.{Filter, Level, LogRecord, Logger}
import javax.imageio.ImageIO
import scala.reflect.internal.util.FileUtils
import scala.util.Random

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


  override def simpleInitApp(): Unit = {

    //    flyCam.setMoveSpeed(100)
    flyCam.setEnabled(false)


    //    bulletAppState.setDebugEnabled(true)
    val sun = GraphicsUtils.addSun()
    GraphicsUtils.addAmbient()
    GraphicsUtils.addShadows(sun)
    GraphicsUtils.addSSAO()
    GraphicsUtils.addSkyBox()

    val lvl = new GameLevelAppState()
    stateManager.attach(lvl)
    stateManager.attach(new UiAppState(lvl))
  }

  //todo log in xml
  var off = false
  override def simpleUpdate(tpf: Float): Unit = {
    if (!off) {
      Logger.getLogger(classOf[BetterCharacterControl].getName).setLevel(Level.OFF)
    }
  }


}
