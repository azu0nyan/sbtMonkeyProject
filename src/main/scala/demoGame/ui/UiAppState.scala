package demoGame.ui

import com.jme3.app.{Application, SimpleApplication}
import com.jme3.app.state.{AbstractAppState, AppStateManager, BaseAppState}
import de.lessvoid.nifty.Nifty
import de.lessvoid.nifty.builder.{ImageBuilder, LayerBuilder, PanelBuilder, ScreenBuilder, TextBuilder}
import de.lessvoid.nifty.elements.render.TextRenderer
import de.lessvoid.nifty.screen.{Screen, ScreenController}
import de.lessvoid.nifty.tools.Color
import demoGame.gameplay.{CreatureControl, GameLevelAppState}

import java.awt.event.{KeyEvent, KeyListener}

class UiAppState(
                  gameLevelAppState: GameLevelAppState
                ) extends BaseAppState with ScreenController {
  var nifty:Nifty = _

  val manaPbName = "manaProgressBar"
  val manaPbText = "manaProgressBarText"
  val hpPbName = "hpProgressBar"
  val hpPbText = "hpProgressBarText"
  override def initialize(app: Application): Unit = {

    import com.jme3.niftygui.NiftyJmeDisplay
    import de.lessvoid.nifty.Nifty
    val niftyDisplay = NiftyJmeDisplay.newNiftyJmeDisplay(app.getAssetManager, app.getInputManager, app.getAudioRenderer, app.getGuiViewPort)
    /** Create a new NiftyGUI object */
     nifty = niftyDisplay.getNifty
    import de.lessvoid.nifty.builder.ScreenBuilder



    val screen = new ScreenBuilder("game", UiAppState.this) {
      controller(UiAppState.this)
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
          control(UiElementOps.makeProgressBar(hpPbName, hpPbText, "assets/Interface/hpBorder.png", "assets/Interface/hpInner.png"))
          control(UiElementOps.makeProgressBar(manaPbName, manaPbText, "assets/Interface/manaBorder.png", "assets/Interface/manaInner.png"))
          text(new TextBuilder {
            color(new Color(1, .7f, .4f, 1f))
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
    updateManaBar()
    updateGoldText()
    updateHpBar()
  }

  def updateGoldText() = {
    val player = gameLevelAppState.playerCharacter.getControl(classOf[CreatureControl])
    val gold = player.info.gold
    UiElementOps.setText("goldText", "game", nifty, s"GOLD $gold")
  }

  def updateManaBar():Unit = {
    val player = gameLevelAppState.playerCharacter.getControl(classOf[CreatureControl])
    val manaStr = s"\\#ffffff#${player.info.mana} / ${player.info.maxMana}"
    val manaPercentage = player.info.mana.toFloat / player.info.maxMana.toFloat
    UiElementOps.setProgress(manaPbName, "game", nifty, manaPercentage)
    UiElementOps.setText(manaPbText, "game", nifty, manaStr)
  }

  def updateHpBar() :Unit = {
    val player = gameLevelAppState.playerCharacter.getControl(classOf[CreatureControl])
    val hpStr = s"${player.info.hp} / ${player.info.maxHp}"
    val hpPercentage = player.info.hp.toFloat / player.info.maxHp.toFloat
    UiElementOps.setProgress(hpPbName, "game", nifty, hpPercentage)
    UiElementOps.setText(hpPbText, "game", nifty, hpStr)
  }

  override def cleanup(app: Application): Unit = {}
  override def onEnable(): Unit = {}
  override def onDisable(): Unit = {}


  override def bind(nifty: Nifty, screen: Screen): Unit = {
  }
  override def onStartScreen(): Unit = {}
  override def onEndScreen(): Unit = {}
}
