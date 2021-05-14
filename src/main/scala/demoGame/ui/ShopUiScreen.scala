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
import org.slf4j.LoggerFactory

import scala.jdk.CollectionConverters.ListHasAsScala


class ShopUiScreen(nifty: Nifty,
                   uiAppState: UiAppState,
                   level: GameLevelAppState,
                   var shop: Option[ShopControl] = None,
                   var buyer: Option[CreatureControl] = None) extends ScreenController {
  val log = LoggerFactory.getLogger(this.getClass)

  val shopLotsIdPanelPrefix: String = "shopLotPanelId"
  val shopLotsListId: String = "shopLotPanel"
  val goldTextId:String ="goldTextId"


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
          alignCenter()
          valignCenter()
          childLayoutVertical()
          height("80%")
          width("100%")
        })
        panel(new PanelBuilder() {
          height("20%")
          width("100%")
          backgroundColor(new Color(0f, 0f, 0f, .8f))
          childLayoutHorizontal()
          text(new TextBuilder(){
            id(goldTextId)
            height("30%")
            width("30%")
            text("GOLD : ")
            color(new Color(1, .7f, .4f, 1f))
            font("Interface/Fonts/Default.fnt")
          })

          control(new ButtonBuilder("closeButton", "CLOSE") {
            backgroundColor(new Color(1f, 0f, 0f, 1f))
            width("30%")
            height("50%")
            interactOnClick("closeShop()")
          })
        })
      })
    })
  }.build(nifty)

  nifty.addScreen(shopScreenId, screen)
  nifty.gotoScreen(shopScreenId)


  var displayedLots: Seq[ShopLot] = Seq()
  def closeShop(): Unit = {
    uiAppState.toGameScreen()
  }
  def lotId(lot: ShopLot): String = shopLotsIdPanelPrefix + lot.toString


  def addLot(lot: ShopLot, index: Int) = {
    val myId = lotId(lot)
    log.info(s"Adding lot $lot with id $myId")
    val parent = screen.findElementById(shopLotsListId)
    val lotPanel = new PanelBuilder() {
      id(myId)
      alignCenter()
      //      backgroundColor(1f)
      childLayoutHorizontal()
      width("100%")
      height("10%")
      control(new ButtonBuilder(s"buyButton$index", "BUY") {
        //backgroundColor(new Color(1f, 0f, 0f, 1f))
        width("10%")
        height("100%")
        interactOnClick(s"buy($index)")
      })
      text(new TextBuilder() {
        font("Interface/Fonts/Default.fnt")
        width("10%")
        height("100%")
        text(buyer.map(lot.price(_).toString).getOrElse(""))
      })
      text(new TextBuilder() {
        font("Interface/Fonts/Default.fnt")
        width("70%")
        height("100%")
        text(lot.itemDescription)
      })
      if (lot.shopImage != "")
        image(new ImageBuilder() {
          width("10%")
          height("100%")
          filename(lot.shopImage)
        })
      else
        text(new TextBuilder() {
          font("Interface/Fonts/Default.fnt")
          log.warn(s"Lot ${lot.itemDescription} NO image")
          width("10%")
          height("100%")
          text("NO IMAGE")
        })
    }
    lotPanel.build(parent)

    parent.layoutElements();
    screen.layoutLayers()

    log.info(parent.getChildren.asScala.mkString(" \n"))
  }

  def updateLots(lots: Seq[ShopLot]) = {
    for (lot <- displayedLots) screen.findElementById(lotId(lot)).markForRemoval()
    for ((lot, id) <- lots.zipWithIndex) addLot(lot, id)
    displayedLots = lots
  }

  def updateGoldText() = {
    if(buyer.nonEmpty) {
      val gold = buyer.get.info.gold
      UiElementOps.setText(goldTextId, shopScreenId, nifty, s"GOLD : $gold")
    }
  }


  def buy(idStr: String): Unit = {
    for (id <- idStr.toIntOption;
         s <- shop;
         b <- buyer if displayedLots.indices.contains(id)) {
      s.buy(b, displayedLots(id))
      updateLots(s.availableLots(b))
    }

  }


  def update(tpf: Float): Unit = {
    updateGoldText()
    //    if(buyer.nonEmpty && shop.nonEmpty){
    //      log.info(shop.map(_.availableLots(buyer.get).zipWithIndex).toString)
    //    }
    for (b <- buyer;
         s <- shop
         ) {
      if (displayedLots != s.availableLots(b)) {
        updateLots(s.availableLots(b))
      }
      //      if (!displayedLots.contains(lot)) addLot(lot, id)
    }
  }

  override def bind(nifty: Nifty, screen: Screen): Unit = {
    log.info(s"Bind")
  }
  override def onStartScreen(): Unit = {
    log.info(s"Start")
    if (level.chaseCamera != null)
      level.chaseCamera.setDragToRotate(true)

  }
  override def onEndScreen(): Unit = {
    log.info(s"End")
    if (level.chaseCamera != null)
      level.chaseCamera.setDragToRotate(false)
  }


}
