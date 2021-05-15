package demoGame.ui

import com.jme3.app.{Application, SimpleApplication}
import com.jme3.app.state.{AbstractAppState, AppStateManager, BaseAppState}
import com.jme3.input.KeyInput
import com.jme3.input.controls.{ActionListener, KeyTrigger}
import de.lessvoid.nifty.Nifty
import de.lessvoid.nifty.builder.ElementBuilder.{Align, VAlign}
import de.lessvoid.nifty.builder.{ImageBuilder, LayerBuilder, PanelBuilder, ScreenBuilder, TextBuilder}
import de.lessvoid.nifty.controls.button.builder.ButtonBuilder
import de.lessvoid.nifty.elements.render.TextRenderer
import de.lessvoid.nifty.screen.{Screen, ScreenController}
import de.lessvoid.nifty.tools.Color
import demoGame.gameplay.{CreatureControl, GameLevelAppState}
import demoGame.ui.UiAppState.{gameScreenId, shopScreenId}
import org.slf4j.LoggerFactory

import java.awt.event.{KeyEvent, KeyListener}
import scala.jdk.CollectionConverters.CollectionHasAsScala

object UiAppState {
  val gameScreenId = "gameScreenId"
  val shopScreenId = "shopScreenId"
}
class UiAppState(
                  gameLevelAppState: GameLevelAppState
                ) extends BaseAppState {
  val log = LoggerFactory.getLogger(this.getClass)

  var nifty: Nifty = _

  var gameScreen: GameUiScreen = _

  var shopScreen: ShopUiScreen = _



  override def initialize(app: Application): Unit = {
    app.getInputManager.addMapping("shopToggle", new KeyTrigger(KeyInput.KEY_TAB))
    app.getInputManager.addListener(new ActionListener {
      override def onAction(name: String, isPressed: Boolean, tpf: Float): Unit = {

        if(!isPressed && name == "shopToggle"){
          log.info("Toggling screen ")
          if(canShowShopScreen && nifty.getCurrentScreen != null && nifty.getCurrentScreen.getScreenId == gameScreenId) toShopScreen()
          else toGameScreen()
        }
      }
    }, "shopToggle")
    import com.jme3.niftygui.NiftyJmeDisplay
    import de.lessvoid.nifty.Nifty
    val niftyDisplay = NiftyJmeDisplay.newNiftyJmeDisplay(app.getAssetManager, app.getInputManager, app.getAudioRenderer, app.getGuiViewPort)
    nifty = niftyDisplay.getNifty
    app.getGuiViewPort.addProcessor(niftyDisplay)

    nifty.loadStyleFile("nifty-default-styles.xml")
    nifty.loadControlFile("nifty-default-controls.xml")


    gameScreen = new GameUiScreen(nifty, this, gameLevelAppState)
    shopScreen = new ShopUiScreen(nifty, this, gameLevelAppState)

  }

  def canShowShopScreen:Boolean = {
    shopScreen != null && shopScreen.shop.nonEmpty && shopScreen.buyer.nonEmpty
  }

  def toShopScreen():Unit = {
    log.info(s"Going to shop screen..")
    nifty.gotoScreen(shopScreenId)

  }

  def toGameScreen():Unit = {
    log.info(s"Going to game screen..")
    nifty.gotoScreen(gameScreenId)
    println(nifty.getAllScreensName.asScala.mkString(" "))
  }


  override def update(tpf: Float): Unit = {
    if (gameLevelAppState.playerCharacter != null) {
      shopScreen.buyer = Some(gameLevelAppState.playerCharacter.getControl(classOf[CreatureControl]))
      gameLevelAppState.shops.find(_.buyers.contains(gameLevelAppState.playerCharacter.getControl(classOf[CreatureControl]))) match {
        case Some(shop) =>
          shopScreen.shop = Some(shop)
        case None =>
          shopScreen.shop = None
      }
    }
    if(!canShowShopScreen && nifty.getCurrentScreen  != null && nifty.getCurrentScreen.getScreenId == shopScreenId)
      toGameScreen()

    gameScreen.update(tpf)
    shopScreen.update(tpf)
  }


  override def cleanup(app: Application): Unit = {}
  override def onEnable(): Unit = {}
  override def onDisable(): Unit = {}


}
