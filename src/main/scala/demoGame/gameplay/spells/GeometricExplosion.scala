package demoGame.gameplay.spells

import com.jme3.bullet.collision.shapes.SphereCollisionShape
import com.jme3.bullet.control.GhostControl
import com.jme3.effect.ParticleEmitter
import com.jme3.math.{ColorRGBA, Vector2f, Vector3f}
import com.jme3.renderer.{RenderManager, ViewPort}
import com.jme3.scene.Node
import com.jme3.scene.control.AbstractControl
import demoGame.JmeImplicitsFHelper
import demoGame.JmeImplicitsFHelper.{V3Helper, boundingRadius}
import demoGame.gameplay.{CreatureControl, CreatureMovementControl}
import demoGame.gameplay.CreatureState.{InContinuousState, ContinuousState, CreatureState, Normal}
import demoGame.gameplay.spells.GeometricBall.LaunchGeometricBall
import demoGame.graphics.particles.ParticleUtils
import org.slf4j.LoggerFactory

object GeometricExplosion {
  val logger = LoggerFactory.getLogger(classOf[GeometricExplosion].getName)


  def fromLeverAndCaster(level:Int, caster:CreatureControl):GeometricExplosion = {
    val res = new GeometricExplosion(caster)
    res.minDamage = 10 + level
    res.maxDamage = 10 + level * 2
    res.minDamageDist = 5f + level / 3f
    res.maxDist = 10f + level
    res
  }

  class GeometricExplosion(val caster: CreatureControl) extends ContinuousState {
    var n: Node = _
    var p: ParticleEmitter = _
    var time: Float = 0

    var minDamage = 10
    var maxDamage = 50
    var minDamageDist = 5f
    var maxDist = 30f




    /** Функция должна обновить стейт на ChannelingAction чтобы были вызваны Ends, Continues, Interrupted */
    override def onStateStarts(): Option[CreatureState] = {
      logger.info(s"Casting geometric explosion  ...")
      n = new Node("geometric explosion ")
      p = ParticleUtils.makeGeometricExplosion(0f, minDamageDist)(caster.level.app)
      n.attachChild(p)
      caster.level.levelNode.attachChild(n)
      n.setLocalTranslation(caster.getSpatial.getLocalTranslation)
      p.emitAllParticles()
      Some(InContinuousState(.5f, this))
    }

    override def onStateInterrupted(): Unit = {
      logger.info(s"Casting geometric explosion  interrupted ...")
      p.removeFromParent()
    }

    override def onStateContinues(tpf: Float): Unit = {
      logger.trace(s"Casting geometric explosion  tick ${caster.state}...")
      time += tpf

    }

    override def onStateEnds(): Option[ContinuousState] = {
      logger.info(s"Casting geometric explosion  cast finished ...")
      p.removeFromParent()
      val ghost = new GhostControl(new SphereCollisionShape(30))
      n.addControl(ghost)
      caster.level.physicSpace.add(ghost)
      JmeImplicitsFHelper.overlappingCreatures(ghost).filter(_ != caster).foreach{ target =>
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
          val multiplier = 1- dist / maxDist
          body.applyImpulse(dir_.mult(body.getMass * 30f * math.sqrt(multiplier).toFloat), Vector3f.ZERO)
        }
      }
      caster.level.physicSpace.remove(ghost)
      n.removeControl(ghost)
      new GeometricExplosionControl(this)
      None
    }
  }

  class GeometricExplosionControl(act:GeometricExplosion) extends AbstractControl {
    val p = ParticleUtils.makeGeometricExplosion(100f, act.minDamageDist)(act.caster.level.app)
    p.setStartColor(new ColorRGBA(1f, .5f, 1f, 1f))
    p.setEndColor(new ColorRGBA(.5f, 0f, 1f, 1f))

    act.n.attachChild(p)
    p.emitAllParticles()

    act.n.addControl(this)

    override def controlUpdate(tpf: Float): Unit = {
      if(p.getNumVisibleParticles == 0){
        act.n.removeFromParent()
      }
    }

    override def controlRender(rm: RenderManager, vp: ViewPort): Unit = {

    }
  }
}
