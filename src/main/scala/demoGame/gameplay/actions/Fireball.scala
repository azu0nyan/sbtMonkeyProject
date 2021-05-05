package demoGame.gameplay.actions

import com.jme3.bullet.collision.shapes.SphereCollisionShape
import com.jme3.bullet.control.GhostControl
import com.jme3.effect.ParticleEmitter
import com.jme3.math.Vector3f
import com.jme3.renderer.{RenderManager, ViewPort}
import com.jme3.scene.Node
import com.jme3.scene.control.AbstractControl
import demoGame.JmeImplicits3FHelper._
import demoGame.gameplay.{CreatureControl, CreatureState, GameLevelAppState}
import demoGame.gameplay.CreatureState.{ChannelingAction, CreatureAction, CreatureState, Normal}
import demoGame.graphics.ParticleUtils

import java.util.logging.Logger

object Fireball {
  val logger = Logger.getLogger("fireball")
  class Fireball(val dir: Vector3f, caster: CreatureControl) extends CreatureAction {
    var p: ParticleEmitter = _
    var time: Float = 0

    override def canAct(implicit creature: CreatureControl): Boolean = {
      creature.state == Normal()
    }

    /** Функция должна обновить стейт на ChannelingAction чтобы были вызваны Ends, Continues, Interrupted */
    override def onActionStarts(implicit creature: CreatureControl): Option[CreatureState] = {
      logger.info(s"Casting fireball  ...")
      p = ParticleUtils.makeFireball()(creature.level.app)
      creature.level.levelNode.attachChild(p)
      p.setLocalTranslation(creature.getSpatial.getLocalTranslation + dir.normalize() * 1.5f)
      Some(ChannelingAction(.5f, this))
    }

    override def onActionInterrupted(implicit creature: CreatureControl): Unit = {
      logger.info(s"Casting fireball interrupted ...")
      p.removeFromParent()
    }

    override def onActionContinues(tpf: Float)(implicit creature: CreatureControl): Unit = {
      logger.info(s"Casting fireball tick ${creature.state}...")
      time += tpf

    }

    override def onActionEnds(implicit creature: CreatureControl): Option[CreatureAction] = {
      logger.info(s"Casting fireball cast finished ...")
      new LaunchFireball(dir, p, caster)
      None
    }
  }

  class LaunchFireball(dir: Vector3f, p: ParticleEmitter, caster: CreatureControl, speed:Float = 20f, acceleration:Float = 50f, maxSpeed: Float = 100f ) extends AbstractControl {
    p.addControl(this)
    val ghost = new GhostControl(new SphereCollisionShape(.5f))
    p.addControl(ghost)
    caster.level.bulletAppState.getPhysicsSpace.add(ghost)

    var curSpeed:Float = speed

    override def controlUpdate(tpf: Float): Unit = {
      curSpeed += math.min(maxSpeed, acceleration * tpf)
      p.setLocalTranslation(p.getLocalTranslation + dir.normalize() * tpf * curSpeed )
      overlappingCreatures(ghost).find(p => p != caster).foreach { cc =>
        cc.info.hp -= 10
        p.removeFromParent()
      }
      //
    }

    override def controlRender(rm: RenderManager, vp: ViewPort): Unit = {}
  }
}
