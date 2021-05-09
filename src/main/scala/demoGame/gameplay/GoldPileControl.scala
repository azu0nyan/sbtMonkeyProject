package demoGame.gameplay

import com.jme3.bullet.collision.shapes.SphereCollisionShape
import com.jme3.bullet.control.{BetterCharacterControl, GhostControl}
import com.jme3.material.Material
import com.jme3.math.{ColorRGBA, Vector3f}
import com.jme3.renderer.{RenderManager, ViewPort}
import com.jme3.scene.Spatial
import com.jme3.scene.control.AbstractControl
import com.jme3.scene.shape.Sphere
import demoGame.JmeImplicitsFHelper.overlappingCreatures
import demoGame.MakerUtils
import org.slf4j.LoggerFactory

import java.util.logging.Logger
import scala.jdk.CollectionConverters.ListHasAsScala

class GoldPileControl(pos:Vector3f, amount: Int)(implicit level: GameLevelAppState) extends AbstractControl {

   val goldMaterial: Material = {
    val goldColor = new ColorRGBA(1f, .8f, .0f, 1f)
    val mat = MakerUtils.makeShaded(goldColor)(level.app)
    mat.setColor("GlowColor", goldColor)
    mat.setColor("Specular", goldColor)
    mat.setFloat("Shininess", 64f)
    mat.setColor("Ambient", goldColor.mult(0.4f).add(ColorRGBA.White.mult(0.1f)))

    mat
  }


  val size = math.pow(amount, 1 / 3f).toFloat * .1f
  val sp = MakerUtils.makeBox(pos, new Vector3f(size, size, size), "gold", goldMaterial, Some(level.levelNode))(level.app)


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
