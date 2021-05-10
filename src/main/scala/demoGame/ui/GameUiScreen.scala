package demoGame.ui

import com.jme3.bullet.control.CharacterControl
import de.lessvoid.nifty.Nifty
import de.lessvoid.nifty.builder.{EffectBuilder, ImageBuilder, LayerBuilder, PanelBuilder, ScreenBuilder, TextBuilder}
import de.lessvoid.nifty.screen.{Screen, ScreenController}
import de.lessvoid.nifty.tools.{Color, SizeValue}
import demoGame.gameplay.CreatureState.{ContinuousState, Stunned}
import demoGame.gameplay.spells.CreatureSpell.{CreatureSpell, SpellLevel}
import demoGame.gameplay.{CreatureControl, GameLevelAppState}
import demoGame.ui.GameUiScreen.gameScreenId

object GameUiScreen {
  val gameScreenId = "gameScreenId"
}
class GameUiScreen(nifty: Nifty, gameLevelAppState: GameLevelAppState) extends ScreenController {


  val manaPbName = "manaProgressBar"
  val manaPbText = "manaProgressBarText"
  val hpPbName = "hpProgressBar"
  val hpPbText = "hpProgressBarText"
  val spellPanelId = "spellPanel"

  val screen = new ScreenBuilder(gameScreenId, GameUiScreen.this) {
    layer(new LayerBuilder {
      id("mainLayer")
      backgroundColor(new Color(1, 1, 1, 0f))
      childLayoutCenter()
      panel(new PanelBuilder() {
        id("statsPanel")
        alignLeft()
        valignTop()
        height("20%")
        width("20%")
        backgroundColor(new Color(0, 0, 0, .2f))
        childLayoutVertical()
        control(UiElementOps.makeProgressBar(hpPbName, hpPbText, "assets/Interface/hpBorder.png", "assets/Interface/hpInner.png"))
        control(UiElementOps.makeProgressBar(manaPbName, manaPbText, "assets/Interface/manaBorder.png", "assets/Interface/manaInner.png"))
        text(new TextBuilder {
          color(new Color(1, .7f, .4f, 1f))
          font("Interface/Fonts/Default.fnt")
          id("goldText")
          text("GOLD:")
          width("*")
        })
      })
      panel(new PanelBuilder() {
        id(spellPanelId)
        backgroundColor(new Color(0, 0, 0, .2f))
        alignCenter()
        valignBottom()
        height("15%")
        width("50%")
        childLayoutHorizontal()
      })
    })
  }.build(nifty)

  nifty.addScreen(gameScreenId, screen)
  nifty.gotoScreen(gameScreenId)

  var spellButtons: Set[CreatureSpell] = Set()

  def updateSpellButtons(): Unit = {
    val current = player.spells
    val toAdd = current.toSet &~ spellButtons
    val toRemove = spellButtons &~ current.toSet
    for (s <- toAdd) addSpellButton(s)
    for (s <- toRemove) screen.findElementById(spellElementId(s.name)).markForRemoval()
    for (s <- current) {
      if (s.creature.state.isInstanceOf[Stunned]) UiElementOps.setText(spellElementNoManaTextId(s.name), gameScreenId, nifty, "STUN")
      else if (s.manaCost > s.creature.info.mana) UiElementOps.setText(spellElementNoManaTextId(s.name), gameScreenId, nifty, "NO MANA")
      else UiElementOps.setText(spellElementNoManaTextId(s.name), gameScreenId, nifty, "")

      UiElementOps.setText(spellElementManaCostTextId(s.name), gameScreenId, nifty, s.manaCost.toString)
      UiElementOps.setText(spellElementLevelTextId(s.name), gameScreenId, nifty, s"L:${s.level}  ")
    }

  }

  def spellElementId(spellName: String) = s"spellElement${spellName}"
  def spellElementNoManaTextId(spellName: String) = s"spellElementNoManaText${spellName}"
  def spellElementManaCostTextId(spellName: String) = s"spellElementManaCostText${spellName}"
  def spellElementLevelTextId(spellName: String) = s"spellElementLevelText${spellName}"

  def addSpellButton(s: CreatureSpell): Unit = {
    spellButtons += s
    val panel = screen.findElementById(spellPanelId)
    val marginPx = 10
    val heightWidth = panel.getHeight - 2 * marginPx
    val spell = new ImageBuilder() {
      alignCenter()
      margin(s"${marginPx}px")
      id(spellElementId(s.name))
      filename(s.spellImageFile)

      width(s"${heightWidth}px")
      height(s"${heightWidth}px")
      childLayoutOverlay()
      image(new ImageBuilder() {
        filename("/assets/Interface/spellOverlay.png")
        childLayoutVertical()

        text(new TextBuilder {
          id(spellElementManaCostTextId(s.name))
          color(new Color(0, .5f, 1f, 1f))
          textVAlignCenter()
          textHAlignLeft()
          marginLeft(s"${marginPx}px")
          font("Interface/Fonts/Default.fnt")
          text(s.manaCost.toString)
          width("100%")
          height("30%")
          //        onActiveEffect(new EffectBuilder("textSize"){{
          //          effectParameter("endSize", "1.5");
          //        }});
        })

        text(new TextBuilder {
          id(spellElementNoManaTextId(s.name))
          color(new Color(1, 1f, 1f, 1f))
          textVAlignCenter()
          textHAlignCenter()

          font("Interface/Fonts/Default.fnt")
          text("")
          width("100%")
          height("40%")
        })

        text(new TextBuilder {
          id(spellElementLevelTextId(s.name))
          color(new Color(1, .5f, 0f, 1f))
          textVAlignTop()
          textHAlignRight()
          marginRight(s"${marginPx}px")
          font("Interface/Fonts/Default.fnt")
          text(s"L:${s.level}")
          width("100%")
          height("30%")
        })
      })
    }
    spell.build(panel)

    panel.setConstraintWidth(new SizeValue(s"${panel.getHeight * 2}px"))


    panel.layoutElements();
    screen.layoutLayers()
    //    button.build(nifty, screen, layer);

    //    buttonElement.show();


  }


  def player: CreatureControl = {
    val c = gameLevelAppState.playerCharacter
    if (c != null) {
      c.getControl(classOf[CreatureControl])
    } else null
  }

  def update(tpf: Float): Unit = {
    if (player != null) {
      updateManaBar()
      updateGoldText()
      updateHpBar()
      updateSpellButtons()
    }
  }

  def updateGoldText() = {
    val gold = player.info.gold
    UiElementOps.setText("goldText", gameScreenId, nifty, s"GOLD $gold")
  }

  def updateManaBar(): Unit = {
    val manaStr = s"\\#ffffff#${player.info.mana} / ${player.info.maxMana}"
    val manaPercentage = player.info.mana.toFloat / player.info.maxMana.toFloat
    UiElementOps.setProgress(manaPbName, gameScreenId, nifty, manaPercentage)
    UiElementOps.setText(manaPbText, gameScreenId, nifty, manaStr)
  }

  def updateHpBar(): Unit = {
    val hpStr = s"${player.info.hp} / ${player.info.maxHp}"
    val hpPercentage = player.info.hp.toFloat / player.info.maxHp.toFloat
    UiElementOps.setProgress(hpPbName, gameScreenId, nifty, hpPercentage)
    UiElementOps.setText(hpPbText, gameScreenId, nifty, hpStr)
  }


  override def bind(nifty: Nifty, screen: Screen): Unit = {
  }
  override def onStartScreen(): Unit = {}
  override def onEndScreen(): Unit = {}
}
