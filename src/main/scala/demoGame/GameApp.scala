package demoGame

import com.jme3.app.SimpleApplication
import com.jme3.system.AppSettings
import JmeImplicits3FHelper._
import demoGame.gameplay.GameLevelAppState
import demoGame.graphics.GraphicsUtils
import demoGame.ui.UiAppState
import org.slf4j.LoggerFactory


object GameApp {

  val log = LoggerFactory.getLogger(classOf[GameApp])

  def main(args: Array[String]): Unit = {
    log.info(s"Redirecting java logs to logback...")
    import org.slf4j.bridge.SLF4JBridgeHandler
    SLF4JBridgeHandler.removeHandlersForRootLogger()
    SLF4JBridgeHandler.install()
    log.trace(s"Redirecred...")

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

  override def simpleUpdate(tpf: Float): Unit = {
  }


}
