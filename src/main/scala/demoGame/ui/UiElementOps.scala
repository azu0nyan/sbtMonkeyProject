package demoGame.ui

import de.lessvoid.nifty.Nifty
import de.lessvoid.nifty.builder.{ControlBuilder, ElementBuilder, ImageBuilder, TextBuilder}
import de.lessvoid.nifty.elements.render.TextRenderer
import de.lessvoid.nifty.tools.{Color, SizeValue}

object UiElementOps {


  val minPbWidth = 32

  def makeProgressBar(progressBarId:String,
                      progressBarTextId:String,
                      outerFilename:String,
                      innerFilename:String):ControlBuilder = {
    new ControlBuilder(s"${progressBarId}Control") {

      alignCenter()
      valignCenter()
      width("200px")
      height("32px")
      backgroundColor(new Color(1f, 1f, 1f, 1f))
      image(new ImageBuilder() {
        filename(outerFilename)
        imageMode("resize:15,2,15,15,15,2,15,2,15,2,15,15")
        childLayoutAbsolute()
        image(new ImageBuilder() {
          id(progressBarId)
          x("0")
          y("0")
          filename(innerFilename)
          width("32px")
          height("100%")
          imageMode("resize:15,2,15,15,15,2,15,2,15,2,15,15")

        })
        text(new TextBuilder {
          id(progressBarTextId)
          x("50%")
          y("20%")
          color(new Color(1, 1f, 1f, 1f))
          //            backgroundColor("#00FF0000")
          font("Interface/Fonts/Default.fnt")
          text("")
          width("*")
        })
      })
    }
  }

  def setProgress(progressBarId:String, screen:String, nifty: Nifty, progress:Float ) :Unit = {
    val pb = nifty.getScreen(screen).findElementById(progressBarId)
    if(pb != null){
      val  pixelWidth:Int = (minPbWidth + (pb.getParent.getWidth - minPbWidth) * progress).toInt
      pb.setConstraintWidth(new SizeValue(s"${pixelWidth}px"));
      pb.getParent.layoutElements();
    }
  }

  def setText(textId:String,  screen:String, nifty: Nifty, text:String ) :Unit = {
    val el = nifty.getScreen(screen).findElementById(textId)
    if(el != null){
      el.getRenderer(classOf[TextRenderer]).setText(text)
    }
  }

}
