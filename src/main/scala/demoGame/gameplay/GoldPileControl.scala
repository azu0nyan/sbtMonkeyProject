package demoGame.gameplay

import com.jme3.bullet.collision.shapes.SphereCollisionShape
import com.jme3.bullet.control.{BetterCharacterControl, GhostControl}
import com.jme3.renderer.{RenderManager, ViewPort}
import com.jme3.scene.Spatial
import com.jme3.scene.control.AbstractControl
import com.jme3.scene.shape.Sphere
import demoGame.JmeImplicits3FHelper.overlappingCreatures
import org.slf4j.LoggerFactory

import java.util.logging.Logger
import scala.jdk.CollectionConverters.ListHasAsScala

class GoldPileControl(sp: Spatial, amount: Int)(implicit level: GameLevelAppState) extends AbstractControl {

  val log = LoggerFactory.getLogger(classOf[GoldPileControl].getName)

  sp.addControl(this)
  val ghost = new GhostControl(new SphereCollisionShape(.5f))
  sp.addControl(ghost)
  level.bulletAppState.getPhysicsSpace.add(ghost)


  override def controlUpdate(tpf: Float): Unit = {
    overlappingCreatures(ghost)
      .headOption.foreach { cr =>
      cr.addGold(amount)
      log.info(s"${cr.info.name} looted ${amount} gold")
      sp.removeFromParent()
      level.physicSpace.remove(ghost)
    }
  }

  override def controlRender(rm: RenderManager, vp: ViewPort): Unit = {}
}
