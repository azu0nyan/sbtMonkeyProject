package demoGame.graphics

import com.jme3.math.ColorRGBA

import java.awt.Color

object ColorUtils {
  def colorRGBAFromInt(color: Int): ColorRGBA = {
    val c = new Color(color, true)
    new ColorRGBA(c.getRed / 255f, c.getGreen / 255f, c.getBlue / 255f, c.getAlpha / 255f)
  }
}
