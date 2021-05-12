package demoGame.ui

import de.lessvoid.nifty.Nifty
import de.lessvoid.nifty.builder.{ImageBuilder, LayerBuilder, PanelBuilder, PopupBuilder, ScreenBuilder, TextBuilder}
import de.lessvoid.nifty.controls.button.builder.ButtonBuilder
import de.lessvoid.nifty.screen.{Screen, ScreenController}
import de.lessvoid.nifty.tools.Color
import demoGame.gameplay.{CreatureControl, GameLevelAppState}
import demoGame.gameplay.shop.ShopControl
import demoGame.gameplay.shop.ShopLot.ShopLot
import demoGame.ui.UiAppState.{gameScreenId, shopScreenId}


class ShopUiScreen(nifty: Nifty,
                   shop: Option[ShopControl],
                   buyer: CreatureControl) extends ScreenController {
  val shopLotsIdPanelPrefix: String = "shopLotPanelId"
  val shopLotsListId: String = "shopLotPanel"


  val screen = new ScreenBuilder(shopScreenId, ShopUiScreen.this) {
    layer(new LayerBuilder() {
      id("mainLayer")
      backgroundColor(new Color(0f, 0f, 0f, 0f))
      childLayoutCenter()
      panel(new PanelBuilder() {
        id("shopMainPanel")
        alignCenter()
        valignCenter()
        backgroundColor(new Color(0f, 0f, 0f, .6f))
        height("80%")
        width("80%")
        childLayoutVertical()
        panel(new PanelBuilder() {
          id(shopLotsListId)
          height("80%")
          width("100%")
        })
        panel(new PanelBuilder() {
          height("20%")
          width("100%")
          backgroundColor(new Color(0f, 0f, 0f, .8f))
          childLayoutCenter()
          control(new ButtonBuilder("closeButton", "CLOSE") {
            backgroundColor(new Color(1f, 0f, 0f, 1f))
            width("30%")
            height("50%")
          })
        })


      })
    })
  }.build(nifty)

  nifty.addScreen(gameScreenId, screen)
  nifty.gotoScreen(gameScreenId)


  var displayedLots: Seq[ShopLot] = Seq()

  def lotId(lot: ShopLot): String = shopLotsIdPanelPrefix + lot.toString

  def makeLot(lot: ShopLot, id: Int) = {
    val parent = screen.findElementById(shopLotsListId)
    val myId = lotId(lot)
    val lotPanel = new PanelBuilder() {
      childLayoutHorizontal()
      width("100%")
      height("10%")
      text(new TextBuilder() {
        width("10%")
        height("100%")
        text("Q")
      })
      text(new TextBuilder() {
        width("80%")
        height("100%")
        text(lot.itemDescription)
      })
      image(new ImageBuilder(){
        width("10%")
        height("100%")
        filename(lot.shopImage)
      })
    }.build(parent)

  }

  def update(tpf: Float): Unit = {
    for ((lot, id) <- shop.map(_.availableLots(buyer).zipWithIndex).getOrElse(Seq())) {

    }
  }

  override def bind(nifty: Nifty, screen: Screen): Unit = {}
  override def onStartScreen(): Unit = {}
  override def onEndScreen(): Unit = {}
}
