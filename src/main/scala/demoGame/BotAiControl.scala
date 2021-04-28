package demoGame

import com.jme3.renderer.{RenderManager, ViewPort}
import com.jme3.scene.Spatial
import com.jme3.scene.control.AbstractControl

class BotAiControl(control:NavigationControl, toFollow:Spatial) extends AbstractControl{
  val maxDistance = 10f


  override def controlUpdate(tpf: Float): Unit = {
    if(control.getMoveTo.distance(toFollow.getLocalTranslation) > maxDistance){
      control.setMoveTo(toFollow.getLocalTranslation)
    }
  }
  override def controlRender(rm: RenderManager, vp: ViewPort): Unit = {}
}
