package demoGame.ui

import com.jme3.app.{Application, SimpleApplication}
import com.jme3.app.state.{AbstractAppState, AppStateManager, BaseAppState}
import de.lessvoid.nifty.Nifty
import de.lessvoid.nifty.builder.ElementBuilder.{Align, VAlign}
import de.lessvoid.nifty.builder.{ImageBuilder, LayerBuilder, PanelBuilder, ScreenBuilder, TextBuilder}
import de.lessvoid.nifty.controls.button.builder.ButtonBuilder
import de.lessvoid.nifty.elements.render.TextRenderer
import de.lessvoid.nifty.screen.{Screen, ScreenController}
import de.lessvoid.nifty.tools.Color
import demoGame.gameplay.{CreatureControl, GameLevelAppState}
import demoGame.ui.UiAppState.{gameScreenId, shopScreenId}

import java.awt.event.{KeyEvent, KeyListener}
object UiAppState{
  val gameScreenId = "gameScreenId"
  val shopScreenId = "shopScreenId"
}
class UiAppState(
                  gameLevelAppState: GameLevelAppState
                ) extends BaseAppState with ScreenController {
  var nifty:Nifty = _

  var gameScreen:GameUiScreen = _

  var shopScreen:ShopUiScreen = _

  override def initialize(app: Application): Unit = {

    import com.jme3.niftygui.NiftyJmeDisplay
    import de.lessvoid.nifty.Nifty
    val niftyDisplay = NiftyJmeDisplay.newNiftyJmeDisplay(app.getAssetManager, app.getInputManager, app.getAudioRenderer, app.getGuiViewPort)
    nifty = niftyDisplay.getNifty
    app.getGuiViewPort.addProcessor(niftyDisplay)

    nifty.loadStyleFile("nifty-default-styles.xml")
    nifty.loadControlFile("nifty-default-controls.xml")


    gameScreen = new GameUiScreen(nifty, gameLevelAppState)
    shopScreen = new ShopUiScreen(nifty, null, null)
//    nifty.gotoScreen(gameScreenId)
    nifty.gotoScreen(shopScreenId)
  }

  def addButton = {
    val  screen = nifty.getScreen(gameScreenId)
    val layer = screen.findElementById("statsPanel");


    val button = new TextBuilder {
      color(new Color(1, .7f, .4f, 1f))
      //            backgroundColor("#00FF0000")
      alignLeft()
      font("Interface/Fonts/Default.fnt")
      id("goldText2")
      text("GOAAAAALD:")
      width("*")
    }



    /** nifty is a field */
    button.build(layer)
    layer.layoutElements();
    screen.layoutLayers()
    //    button.build(nifty, screen, layer);

    /** desperate try */
    val buttonElement = screen.findElementById("goldText2");
    buttonElement.show();


  }




  override def update(tpf: Float): Unit = {
    gameScreen.update(tpf)
  }



  override def cleanup(app: Application): Unit = {}
  override def onEnable(): Unit = {}
  override def onDisable(): Unit = {}


  override def bind(nifty: Nifty, screen: Screen): Unit = {
  }
  override def onStartScreen(): Unit = {}
  override def onEndScreen(): Unit = {}
}
