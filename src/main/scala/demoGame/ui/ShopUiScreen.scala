package demoGame.ui

import de.lessvoid.nifty.Nifty
import de.lessvoid.nifty.builder.{LayerBuilder, PanelBuilder, PopupBuilder, ScreenBuilder}
import de.lessvoid.nifty.controls.button.builder.ButtonBuilder
import de.lessvoid.nifty.screen.{Screen, ScreenController}
import de.lessvoid.nifty.tools.Color
import demoGame.gameplay.{CreatureControl, GameLevelAppState}
import demoGame.gameplay.shop.ShopControl
import demoGame.ui.UiAppState.{gameScreenId, shopScreenId}



class ShopUiScreen(nifty: Nifty,
                   shop: ShopControl,
                   buyer: CreatureControl) extends ScreenController{
  var shopLotsIdPanelPrefix:String = "shopLotPanelId"


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
        panel(new PanelBuilder(){
          height("80%")
          width("100%")
        })
        panel(new PanelBuilder(){
          height("20%")
          width("100%")
          backgroundColor(new Color(0f, 0f, 0f, .8f))
          childLayoutCenter()
          control(new ButtonBuilder("closeButton", "CLOSE"){
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

  override def bind(nifty: Nifty, screen: Screen): Unit = {}
  override def onStartScreen(): Unit = {}
  override def onEndScreen(): Unit = {}
}
