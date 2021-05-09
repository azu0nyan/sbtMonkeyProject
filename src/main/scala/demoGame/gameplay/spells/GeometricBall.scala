package demoGame.gameplay.spells

import com.jme3.bullet.collision.shapes.SphereCollisionShape
import com.jme3.bullet.control.{BetterCharacterControl, GhostControl}
import com.jme3.effect.ParticleEmitter
import com.jme3.math.Vector3f
import com.jme3.renderer.{RenderManager, ViewPort}
import com.jme3.scene.{Node, Spatial}
import com.jme3.scene.control.AbstractControl
import demoGame.JmeImplicitsFHelper._
import demoGame.gameplay.{CreatureControl, CreatureMovementControl, CreatureState, GameLevelAppState}
import demoGame.gameplay.CreatureState.{ContinuousState, CreatureState, InContinuousState, Normal}
import demoGame.gameplay.spells.GeometricExplosion.GeometricExplosion
import demoGame.graphics.particles.ParticleUtils
import org.slf4j.LoggerFactory

import java.util.logging.Logger

object GeometricBall {
  val logger = LoggerFactory.getLogger(classOf[GeometricBall].getName)

  def fromLeverAndCaster(level:Int, caster:CreatureControl):GeometricBall = {
    val res = new GeometricBall(caster)
    res.startSpeed = 10f + level
    res.acceleration = 20 + level * 3f
    res.distance = 30 + level * 5f
    res.stunTime = .1f + level * .04f
    res.castTime = math.max(.1f, .7f - level * .05f)
    res.hitScanRadius = math.min(1f, .3f + level * .05f)
    res.damage = 15 + level * 5
    res
  }

  class GeometricBall(val caster: CreatureControl) extends ContinuousState {

    var startSpeed: Float = 20f
    var acceleration: Float = 50f
    var maxSpeed: Float = 100f
    var distance: Float = 50f
    var stunTime:Float = .25f
    var castTime:Float = .5f
    var damage:Int = 50
    var hitScanRadius:Float = .5f

    var spellNode: Node = _
    var geometricBallParticle: ParticleEmitter = _
    var time: Float = 0

    val dir:Vector3f = caster.movement.getSightDirection.clone()

    /** Функция должна обновить стейт на ChannelingAction чтобы были вызваны Ends, Continues, Interrupted */
    override def onStateStarts(): Option[CreatureState] = {
      logger.info(s"Casting geometricBall  ...")
      spellNode = new Node("geometricBall node")
      geometricBallParticle = ParticleUtils.makeGeometricBall()(caster.level.app)
      spellNode.attachChild(geometricBallParticle)
      caster.level.levelNode.attachChild(spellNode)
      //анимация каста начинается перед персонажем, вычисляем смещение
      val forwardOffset = dir.normalize() * (boundingRadius(caster.getSpatial.getWorldBound) + .2f)
      spellNode.setLocalTranslation(caster.getSpatial.getLocalTranslation + forwardOffset)
      Some(InContinuousState(castTime, this))
    }

    override def onStateInterrupted(): Unit = {
      logger.info(s"Casting geometricBall interrupted ...")
      geometricBallParticle.removeFromParent()
    }

    override def onStateContinues(tpf: Float): Unit = {
      logger.trace(s"Casting geometricBall tick ${caster.state}...")
      time += tpf

    }

    override def onStateEnds(): Option[ContinuousState] = {
      logger.info(s"Casting geometricBall cast finished ...")
      new LaunchGeometricBall(this)
      None
    }
  }

  class LaunchGeometricBall(act:GeometricBall,
                      ) extends AbstractControl {
    act.spellNode.addControl(this)

    val ghost = new GhostControl(new SphereCollisionShape(act.hitScanRadius))
    act.spellNode.addControl(ghost)
    act.caster.level.physicSpace.add(ghost)

    var curSpeed: Float = act.startSpeed
    val start: Vector3f = act.spellNode.getLocalTranslation.clone()

    val flyState = 0
    val bangState = 1
    var state:Int = flyState
    var bang:ParticleEmitter = _
    override def controlUpdate(tpf: Float): Unit = state match {
      case `flyState` =>
        //если мы попали в кого-то
        overlappingCreatures(ghost).find(p => p != act.caster).foreach { cc =>
          logger.info(s"geometricBall hit creature ${cc.name}")
          cc.receiveDamage(act.damage)
          cc.stun(act.stunTime)
          val body = cc.movement.asInstanceOf[CreatureMovementControl].controlledRigidBody
          body.applyImpulse(act.dir.mult(body.getMass * 50f), Vector3f.ZERO)
          state = bangState
          makeBang()
        }
        //Если не в кого не попали, то возможно врезались в стену
        if(state == flyState){
          overlappingSpatials(ghost).find(sp => sp.getName == "wall").foreach { sp =>
            logger.info(s"geometricBall hit wall")
           makeBang()
          }
        }
        //Если не попали и не врезались, продолжаем лететь
        if(state == flyState) {
          curSpeed += math.min(act.maxSpeed, act.acceleration * tpf)
          act.spellNode.setLocalTranslation(act.spellNode.getLocalTranslation + act.dir.normalize() * tpf * curSpeed)
          if (act.spellNode.getLocalTranslation.distance(start) > act.distance) {
            logger.info(s"geometricBall out of range")
            clear()
          }
        }
      case `bangState` =>
        if(bang.getNumVisibleParticles == 0) clear()
      //
    }

    private def makeBang():Unit = {
      state = bangState

      bang = ParticleUtils.makeFireExplosion()(act.caster.level.app)
      act.geometricBallParticle.removeFromParent()
      act.spellNode.attachChild(bang)
      bang.emitAllParticles()
    }

    def clear():Unit = {
      spatial.removeFromParent()
      act.caster.level.physicSpace.remove(ghost)
    }

    override def controlRender(rm: RenderManager, vp: ViewPort): Unit = {}
  }
}
