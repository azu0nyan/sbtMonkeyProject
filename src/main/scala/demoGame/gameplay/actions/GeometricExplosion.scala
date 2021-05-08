package demoGame.gameplay.actions

import com.jme3.bullet.collision.shapes.SphereCollisionShape
import com.jme3.bullet.control.GhostControl
import com.jme3.effect.ParticleEmitter
import com.jme3.math.{ColorRGBA, Vector2f, Vector3f}
import com.jme3.renderer.{RenderManager, ViewPort}
import com.jme3.scene.Node
import com.jme3.scene.control.AbstractControl
import demoGame.JmeImplicits3FHelper
import demoGame.JmeImplicits3FHelper.{V3Helper, boundingRadius}
import demoGame.gameplay.{CreatureControl, CreatureMovementControl}
import demoGame.gameplay.CreatureState.{ChannelingAction, CreatureAction, CreatureState, Normal}
import demoGame.gameplay.actions.Fireball.LaunchFireball
import demoGame.graphics.particles.ParticleUtils
import org.slf4j.LoggerFactory

object GeometricExplosion {
  val logger = LoggerFactory.getLogger(classOf[GeometricExplosion].getName)

  class GeometricExplosion(val dir: Vector3f, caster: CreatureControl) extends CreatureAction {
    var n: Node = _
    var p: ParticleEmitter = _
    var time: Float = 0
    var minDamage = 10
    var maxDamage = 50
    var minDamageDist = 5f
    var maxDist = 30f


    override def canAct(implicit creature: CreatureControl): Boolean = {
      creature.state == Normal()
    }

    /** Функция должна обновить стейт на ChannelingAction чтобы были вызваны Ends, Continues, Interrupted */
    override def onActionStarts(implicit creature: CreatureControl): Option[CreatureState] = {
      logger.info(s"Casting geometric explosion  ...")
      n = new Node("geometric explosion ")
      p = ParticleUtils.makeGeometricExplosion(0f)(creature.level.app)
      n.attachChild(p)
      creature.level.levelNode.attachChild(n)
      n.setLocalTranslation(creature.getSpatial.getLocalTranslation)
      p.emitAllParticles()
      Some(ChannelingAction(.5f, this))
    }

    override def onActionInterrupted(implicit creature: CreatureControl): Unit = {
      logger.info(s"Casting geometric explosion  interrupted ...")
      p.removeFromParent()
    }

    override def onActionContinues(tpf: Float)(implicit creature: CreatureControl): Unit = {
      logger.info(s"Casting geometric explosion  tick ${creature.state}...")
      time += tpf

    }

    override def onActionEnds(implicit creature: CreatureControl): Option[CreatureAction] = {
      logger.info(s"Casting geometric explosion  cast finished ...")
      p.removeFromParent()
      val ghost = new GhostControl(new SphereCollisionShape(30))
      n.addControl(ghost)
      creature.level.physicSpace.add(ghost)
      JmeImplicits3FHelper.overlappingCreatures(ghost).filter(_ != caster).foreach{ target =>
        val pos = target.getSpatial.getLocalTranslation
        val dist = pos.distance(n.getLocalTranslation)
        val dmg = if(dist > maxDist) 0
        else if(dist < minDamageDist) minDamage
        else {
          val percentage = (dist - minDamageDist) / (maxDist - minDamageDist)
          percentage * (maxDamage - minDamage)
        }.toInt
        logger.info(s"dealing damage $dmg")
        if(dmg > 0){
          target.receiveDamage(dmg)
          target.stun(.3f)
          val body = target.movement.asInstanceOf[CreatureMovementControl].controlledRigidBody
          val dir_ = pos - caster.getSpatial.getLocalTranslation
          dir_.normalizeLocal()
          body.applyImpulse(dir_.mult(body.getMass * 30f * math.sqrt(maxDamage / dmg).toFloat), Vector3f.ZERO)
        }
      }
      creature.level.physicSpace.remove(ghost)
      n.removeControl(ghost)
      new GeometricExplosionControl(n, caster)
      None
    }
  }

  class GeometricExplosionControl(n:Node, caster: CreatureControl) extends AbstractControl {
    val p = ParticleUtils.makeGeometricExplosion(100f)(caster.level.app)
    p.setStartColor(new ColorRGBA(1f, 1f, 1f, 1f))
    p.setEndColor(new ColorRGBA(.5f, 0f, 1f, 1f))
    n.attachChild(p)
    p.emitAllParticles()

    n.addControl(this)

    override def controlUpdate(tpf: Float): Unit = {
      if(p.getNumVisibleParticles == 0){
        n.removeFromParent()
      }
    }

    override def controlRender(rm: RenderManager, vp: ViewPort): Unit = {

    }
  }
}
