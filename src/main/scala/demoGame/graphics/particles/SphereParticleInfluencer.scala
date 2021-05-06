package demoGame.graphics.particles

import com.jme3.effect.Particle
import com.jme3.effect.influencers.DefaultParticleInfluencer
import com.jme3.effect.shapes.EmitterShape
import com.jme3.math.{FastMath, Vector3f}
import demoGame.JmeImplicits3FHelper._

class SphereParticleInfluencer(center: Vector3f= Vector3f.ZERO, fromCenterVelocity: Float = 1f, normalizeFromCenter: Boolean = true) extends DefaultParticleInfluencer {

  override def influenceParticle(particle: Particle, emitterShape: EmitterShape): Unit = {
    emitterShape.getRandomPoint(particle.position)
    sphereVelocityVariation(particle)
  }


  def sphereVelocityVariation(particle: Particle): Unit = {
    val fromCenter = particle.position - center
    if (normalizeFromCenter) fromCenter.normalizeLocal()
    val vel = initialVelocity + fromCenter * fromCenterVelocity

    particle.velocity.set(vel)
    temp.set(FastMath.nextRandomFloat, FastMath.nextRandomFloat, FastMath.nextRandomFloat)
    temp.multLocal(2f)
    temp.subtractLocal(1f, 1f, 1f)
    temp.multLocal(initialVelocity.length)
    particle.velocity.interpolateLocal(temp, velocityVariation)
  }

}
