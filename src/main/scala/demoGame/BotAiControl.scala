package demoGame

import com.jme3.math.Vector3f
import com.jme3.renderer.{RenderManager, ViewPort}
import com.jme3.scene.Spatial
import com.jme3.scene.control.AbstractControl
import demoGame.gameplay.{CreatureControl, GameLevelAppState}

class BotAiControl(control:NavigationControl, creatureControl: CreatureControl, val initialPos:Vector3f)(implicit level:GameLevelAppState) extends AbstractControl{
  val maxDistance = 2f

  val followDistance = 60f



  override def controlUpdate(tpf: Float): Unit = {
    val playerPos = level.playerCharacter.getLocalTranslation
    val pos = getSpatial.getLocalTranslation
    val dist = pos.distance(playerPos)
    if(dist < followDistance){
      goToLocation(playerPos)
    } else {
      goToLocation(initialPos, false)
    }
  }

  def goToLocation(location:Vector3f, force:Boolean = true) = {
        if(force || control.getMoveTo.distance(location) > maxDistance){
          control.setMoveTo(location)
        }
  }
  override def controlRender(rm: RenderManager, vp: ViewPort): Unit = {}
}
