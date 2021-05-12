package demoGame.gameplay

import com.jme3.bullet.collision.shapes.SphereCollisionShape
import com.jme3.bullet.control.GhostControl
import com.jme3.math.Vector3f
import com.jme3.renderer.{RenderManager, ViewPort}
import com.jme3.scene.Node
import com.jme3.scene.control.AbstractControl
import demoGame.JmeImplicitsFHelper
import demoGame.graphics.particles.ParticleUtils


class ManaDomeControl(pos: Vector3f, power01: Float)(implicit level: GameLevelAppState) extends AbstractControl {

  val maxRegenPerSec = 50
  val minRegenPerSec = 10
  val regenPerSec: Int = ((maxRegenPerSec - minRegenPerSec) * power01) toInt
  val regen1Every: Float = 1f / regenPerSec

  val node = new Node
  node.setLocalTranslation(pos)
  node.addControl(this)
  level.levelNode.attachChild(node)

  val ghost = new GhostControl(new SphereCollisionShape(1 + power01 * 5f))
  node.addControl(ghost)
  level.physicSpace.add(ghost)

  val shapeEmitter = ParticleUtils.makeManaDome(1 + power01 * 5f + 1f)(level.app)
  node.attachChild(shapeEmitter)

  val regenEmitter = ParticleUtils.makeManaDomeInside(1 + power01 * 5f)(level.app)
  node.attachChild(regenEmitter)

  node.updateModelBound()


  var time = 0f

  override def controlUpdate(tpf: Float): Unit = {
    time += tpf
    val regen = time / regen1Every
    time = time % regen1Every
    if(regen >= 1f){
      shapeEmitter.emitParticles(regen.toInt)
     JmeImplicitsFHelper.overlappingCreatures(ghost).foreach{
       c =>
         if(!c.isFullMana) {
           c.regenMana(regen.toInt)
           val toEmit = math.min(10, regen * 5)
           regenEmitter.emitParticles(regen.toInt)
         }
     }
    }
  }

  override def controlRender(rm: RenderManager, vp: ViewPort): Unit = {}
}
