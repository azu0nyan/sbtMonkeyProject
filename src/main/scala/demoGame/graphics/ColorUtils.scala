package demoGame.graphics

import com.jme3.math.ColorRGBA

import java.awt.Color

object ColorUtils {
  def colorRGBAFromInt(color: Int): ColorRGBA = {
    val c = new Color(color)
    new ColorRGBA(c.getRed / 256f, c.getGreen / 256f, c.getBlue / 256f, c.getAlpha / 256f)
  }
}
