package demoGame.gameplay

import com.jme3.renderer.{RenderManager, ViewPort}
import com.jme3.scene.control.AbstractControl
import demoGame.gameplay.CreatureInfo.CreatureInfo

class CreatureControl(initialInfo:CreatureInfo) extends AbstractControl{
  val info:CreatureInfo = initialInfo

  def addGold(gold:Int):CreatureControl = {
    info.gold += gold
    this
  }

  override def controlUpdate(tpf: Float): Unit = {

  }
  override def controlRender(rm: RenderManager, vp: ViewPort): Unit = {}
}
