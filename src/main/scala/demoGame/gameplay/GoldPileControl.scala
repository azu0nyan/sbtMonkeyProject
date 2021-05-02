package demoGame.gameplay

import com.jme3.bullet.collision.shapes.SphereCollisionShape
import com.jme3.bullet.control.{BetterCharacterControl, GhostControl}
import com.jme3.renderer.{RenderManager, ViewPort}
import com.jme3.scene.Spatial
import com.jme3.scene.control.AbstractControl
import com.jme3.scene.shape.Sphere

import scala.jdk.CollectionConverters.ListHasAsScala

class GoldPileControl(sp: Spatial, amount: Int)(implicit level: GameLevelAppState) extends AbstractControl {
  sp.addControl(this)
  val ghost = new GhostControl(new SphereCollisionShape(.5f))
  sp.addControl(ghost)
  level.bulletAppState.getPhysicsSpace.add(ghost)

  override def controlUpdate(tpf: Float): Unit = {
        ghost.getOverlappingObjects.asScala
          .filter(o  => o.getUserObject != null && o.getUserObject.isInstanceOf[Spatial])
          .map(o => o.getUserObject.asInstanceOf[Spatial])
          .find(sp => sp.getControl(classOf[CreatureControl]) != null)
          .foreach{ looter =>
            val cr = looter.getControl(classOf[CreatureControl])
            cr.addGold(amount)
            println(s"${cr.info.name} looted ${amount} gold")
            sp.removeFromParent()
            level.bulletAppState.getPhysicsSpace.remove(ghost)
          }
  }

  override def controlRender(rm: RenderManager, vp: ViewPort): Unit = {}
}
