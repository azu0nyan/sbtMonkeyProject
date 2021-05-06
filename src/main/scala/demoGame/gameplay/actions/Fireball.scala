package demoGame.gameplay.actions

import com.jme3.bullet.collision.shapes.SphereCollisionShape
import com.jme3.bullet.control.GhostControl
import com.jme3.effect.ParticleEmitter
import com.jme3.math.Vector3f
import com.jme3.renderer.{RenderManager, ViewPort}
import com.jme3.scene.{Node, Spatial}
import com.jme3.scene.control.AbstractControl
import demoGame.JmeImplicits3FHelper._
import demoGame.gameplay.{CreatureControl, CreatureState, GameLevelAppState}
import demoGame.gameplay.CreatureState.{ChannelingAction, CreatureAction, CreatureState, Normal}
import demoGame.graphics.particles.ParticleUtils

import java.util.logging.Logger

object Fireball {
  val logger = Logger.getLogger("fireball")

  class Fireball(val dir: Vector3f, caster: CreatureControl) extends CreatureAction {
    var n: Node = _
    var p: ParticleEmitter = _
    var time: Float = 0

    override def canAct(implicit creature: CreatureControl): Boolean = {
      creature.state == Normal()
    }

    /** Функция должна обновить стейт на ChannelingAction чтобы были вызваны Ends, Continues, Interrupted */
    override def onActionStarts(implicit creature: CreatureControl): Option[CreatureState] = {
      logger.info(s"Casting fireball  ...")
      n = new Node("fireball node")
      p = ParticleUtils.makeFireball()(creature.level.app)
      n.attachChild(p)
      creature.level.levelNode.attachChild(n)
      n.setLocalTranslation(creature.getSpatial.getLocalTranslation + dir.normalize() * 1.5f)
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
      new LaunchFireball(dir, n, p, caster)
      None
    }
  }

  class LaunchFireball(dir: Vector3f, spellNode: Node, fireball: ParticleEmitter, caster: CreatureControl, speed: Float = 20f, acceleration: Float = 50f, maxSpeed: Float = 100f, distance: Float = 100f) extends AbstractControl {
    spellNode.addControl(this)

    val ghost = new GhostControl(new SphereCollisionShape(.5f))
    spellNode.addControl(ghost)
    caster.level.bulletAppState.getPhysicsSpace.add(ghost)

    var curSpeed: Float = speed
    val start: Vector3f = spellNode.getLocalTranslation.clone()

    val flyState = 0
    val bangState = 1
    var state:Int = flyState
    var bang:ParticleEmitter = _
    override def controlUpdate(tpf: Float): Unit = state match {
      case `flyState` =>
        overlappingCreatures(ghost).find(p => p != caster).foreach { cc =>
          cc.receiveDamage(50)
//          spellNode.removeFromParent()
          state = bangState
          bang = ParticleUtils.makeFireExplosion()(caster.level.app)
          fireball.removeFromParent()
          spellNode.attachChild(bang)
          bang.emitAllParticles()
        }
        if(state == flyState){
          overlappingSpatials(ghost).find(sp => sp.getName == "wall").foreach { sp =>
            state = bangState
            bang = ParticleUtils.makeFireExplosion()(caster.level.app)
            fireball.removeFromParent()
            spellNode.attachChild(bang)
            bang.emitAllParticles()
          }
        }

        curSpeed += math.min(maxSpeed, acceleration * tpf)
        spellNode.setLocalTranslation(spellNode.getLocalTranslation + dir.normalize() * tpf * curSpeed)
        if (spellNode.getLocalTranslation.distance(start) > distance) {
          spellNode.removeFromParent()
        }
      case `bangState` =>
        if(bang.getNumVisibleParticles == 0) spellNode.removeFromParent()
      //
    }

    override def controlRender(rm: RenderManager, vp: ViewPort): Unit = {}
  }
}
