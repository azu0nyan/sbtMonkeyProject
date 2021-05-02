package demoGame.ui

import com.jme3.app.{Application, SimpleApplication}
import com.jme3.app.state.{AbstractAppState, AppStateManager, BaseAppState}
import de.lessvoid.nifty.Nifty
import de.lessvoid.nifty.builder.{ImageBuilder, LayerBuilder, PanelBuilder, ScreenBuilder, TextBuilder}
import de.lessvoid.nifty.elements.render.TextRenderer
import de.lessvoid.nifty.screen.{Screen, ScreenController}
import de.lessvoid.nifty.tools.Color
import demoGame.gameplay.{CreatureControl, GameLevelAppState}

class UiAppState(
                  gameLevelAppState: GameLevelAppState
                ) extends BaseAppState with ScreenController {
  var nifty:Nifty = _
  override def initialize(app: Application): Unit = {
    val guiNode = app.asInstanceOf[SimpleApplication].getGuiNode

    import com.jme3.niftygui.NiftyJmeDisplay
    import de.lessvoid.nifty.Nifty
    val niftyDisplay = NiftyJmeDisplay.newNiftyJmeDisplay(app.getAssetManager, app.getInputManager, app.getAudioRenderer, app.getGuiViewPort)
    /** Create a new NiftyGUI object */
     nifty = niftyDisplay.getNifty
    import de.lessvoid.nifty.builder.ScreenBuilder


    val screen = new ScreenBuilder("game", UiAppState.this) {
      layer(new LayerBuilder {
        backgroundColor(new Color(1, 1, 1, 0f))
        childLayoutCenter()
        panel(new PanelBuilder() {
          id("mainPanel")
          alignLeft()
          valignTop()
          height("10%")
          width("20%")
//          backgroundColor("#f0FF0000")
          backgroundColor(new Color(0, 0, 0, .2f))
          childLayoutVertical()
          text(new TextBuilder {
            color(new Color(1, .4f, .4f, 1f))
//            backgroundColor("#00FF0000")
            font("Interface/Fonts/Default.fnt")
            id("goldText")
            text("GOLD:")
            width("*")
          })
        })
      })
    }.build(nifty)

    nifty.addScreen("game", screen)

    nifty.gotoScreen("game")
    app.getGuiViewPort.addProcessor(niftyDisplay)


  }

  override def update(tpf: Float): Unit = {
    val el = nifty.getScreen("game").findElementById("goldText")
    if(el != null){
      val gold = gameLevelAppState.playerCharacter.getControl(classOf[CreatureControl]).info.gold
      el.getRenderer(classOf[TextRenderer]).setText(s"GOLD $gold")
    }
  }

  override def cleanup(app: Application): Unit = {}
  override def onEnable(): Unit = {}
  override def onDisable(): Unit = {}


  override def bind(nifty: Nifty, screen: Screen): Unit = {
  }
  override def onStartScreen(): Unit = {}
  override def onEndScreen(): Unit = {}
}
