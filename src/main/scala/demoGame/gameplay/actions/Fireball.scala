package demoGame.gameplay.actions

import com.jme3.bullet.collision.shapes.SphereCollisionShape
import com.jme3.bullet.control.{BetterCharacterControl, GhostControl}
import com.jme3.effect.ParticleEmitter
import com.jme3.math.Vector3f
import com.jme3.renderer.{RenderManager, ViewPort}
import com.jme3.scene.{Node, Spatial}
import com.jme3.scene.control.AbstractControl
import demoGame.JmeImplicits3FHelper._
import demoGame.gameplay.{CreatureControl, CreatureMovementControl, CreatureState, GameLevelAppState}
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
      //анимация каста начинается перед персонажем, вычисляем смещение
      val forwardOffset = dir.normalize() * (boundingRadius(creature.getSpatial.getWorldBound) + .2f)
      n.setLocalTranslation(creature.getSpatial.getLocalTranslation + forwardOffset)
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

  class LaunchFireball(dir: Vector3f, spellNode: Node, fireball: ParticleEmitter, caster: CreatureControl,
                       speed: Float = 20f, acceleration: Float = 50f, maxSpeed: Float = 100f, distance: Float = 50f) extends AbstractControl {
    spellNode.addControl(this)

    val ghost = new GhostControl(new SphereCollisionShape(.5f))
    spellNode.addControl(ghost)
    caster.level.physicSpace.add(ghost)

    var curSpeed: Float = speed
    val start: Vector3f = spellNode.getLocalTranslation.clone()

    val flyState = 0
    val bangState = 1
    var state:Int = flyState
    var bang:ParticleEmitter = _
    override def controlUpdate(tpf: Float): Unit = state match {
      case `flyState` =>
        //если мы попали в кого-то
        overlappingCreatures(ghost).find(p => p != caster).foreach { cc =>
          logger.info(s"fireball hit creature ${cc.name}")
          cc.receiveDamage(50)
          cc.stun(.25f)
//          val body = cc.movement.asInstanceOf[CreatureMovementControl].controlledRigidBody
//          body.applyImpulse(new Vector3f(1f, 0f, 1f).mult(body.getMass * 50f), Vector3f.ZERO)
          state = bangState
          makeBang()
        }
        //Если не в кого не попали, то возможно врезались в стену
        if(state == flyState){
          overlappingSpatials(ghost).find(sp => sp.getName == "wall").foreach { sp =>
            logger.info(s"fireball hit wall")
           makeBang()
          }
        }
        //Если не попали и не врезались, продолжаем лететь
        if(state == flyState) {
          curSpeed += math.min(maxSpeed, acceleration * tpf)
          spellNode.setLocalTranslation(spellNode.getLocalTranslation + dir.normalize() * tpf * curSpeed)
          if (spellNode.getLocalTranslation.distance(start) > distance) {
            logger.info(s"fireball out of range")
            clear()
          }
        }
      case `bangState` =>
        if(bang.getNumVisibleParticles == 0) clear()
      //
    }

    private def makeBang():Unit = {
      state = bangState

      bang = ParticleUtils.makeFireExplosion()(caster.level.app)
      fireball.removeFromParent()
      spellNode.attachChild(bang)
      bang.emitAllParticles()
    }

    def clear():Unit = {
      spatial.removeFromParent()
      caster.level.physicSpace.remove(ghost)
    }

    override def controlRender(rm: RenderManager, vp: ViewPort): Unit = {}
  }
}
