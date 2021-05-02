package demoGame.graphics

import com.jme3.app.SimpleApplication
import com.jme3.material.Material
import com.jme3.math.{ColorRGBA, FastMath}
import com.jme3.renderer.{RenderManager, ViewPort}
import com.jme3.scene.control.AbstractControl

class SetColorFromTime(
                        mat:Material,
                        paramName:String = "Color",
                        timeFunc:Float => Float =  FastMath.sin,
                        color0:ColorRGBA,
                        color1:ColorRGBA
                      )(implicit app: SimpleApplication) extends AbstractControl {
  var timeTotal = 0f

  override def controlUpdate(tpf: Float): Unit = {
    timeTotal += tpf
    val param =timeFunc(timeTotal)
    val color = new ColorRGBA().interpolateLocal(color0, color1, param)
    mat.setColor(paramName, color)
  }

  override def controlRender(rm: RenderManager, vp: ViewPort): Unit = {
  }
}
