package demoGame.graphics.particles

import com.jme3.app.SimpleApplication
import com.jme3.effect.ParticleEmitter
import com.jme3.effect.ParticleMesh.Type
import com.jme3.effect.shapes.EmitterSphereShape
import com.jme3.math.{ColorRGBA, Vector3f}

object ParticleUtils {


  def makeFireExplosion()(implicit app: SimpleApplication) = {
    val fire = new ParticleEmitter("fireExplosion", Type.Triangle, 100)
    import com.jme3.material.Material
    val flash_mat = new Material(app.getAssetManager, "Common/MatDefs/Misc/Particle.j3md")
    flash_mat.setTexture("Texture", app.getAssetManager.loadTexture("Effects/Explosion/flame.png"))
    fire.setMaterial(flash_mat)
    fire.setImagesX(2) // columns
    fire.setImagesY(2) // rows
    fire.setSelectRandomImage(true)

    fire.setParticleInfluencer(new SphereParticleInfluencer(normalizeFromCenter = false))
    fire.setShape(new EmitterSphereSurfaceShape(Vector3f.ZERO, 1f))

    fire.setParticlesPerSec(0)
    fire.setStartColor(new ColorRGBA(1f, .8f, 0f, 1f))
    fire.setEndColor(new ColorRGBA(1f, .8f, .5f, .7f))
    fire.getParticleInfluencer().setInitialVelocity(new Vector3f(0, 2, 0))
    fire.setStartSize(.5f)
    fire.setEndSize(0.1f)
    fire.setGravity(0, 0, 0)
    fire.setLowLife(0.3f)
    fire.setHighLife(.5f)
    //fire.getParticleInfluencer().setVelocityVariation(0.3f)

    fire
  }

  def makeArcaneExplosion()(implicit app: SimpleApplication) = {
    val fire = new ParticleEmitter("fireExplosion", Type.Triangle, 100)
    import com.jme3.material.Material
    val flash_mat = new Material(app.getAssetManager, "Common/MatDefs/Misc/Particle.j3md")
    flash_mat.setTexture("Texture", app.getAssetManager.loadTexture("Effects/Explosion/shockwave.png"))
    fire.setMaterial(flash_mat)
    fire.setImagesX(1) // columns
    fire.setImagesY(1) // rows
    fire.setSelectRandomImage(true)

    fire.setParticleInfluencer(new SphereParticleInfluencer())
    fire.setShape(new EmitterSphereSurfaceShape(Vector3f.ZERO, 3.5f))

    fire.setParticlesPerSec(0)
    fire.setStartColor(new ColorRGBA(0f, 0f, 1f, 0.7f))
    fire.setEndColor(new ColorRGBA(1f, 1f, 1f, 1f))
    fire.getParticleInfluencer().setInitialVelocity(new Vector3f(0, 2, 0))
    fire.setStartSize(.5f)
    fire.setEndSize(0.1f)
    fire.setGravity(0, 0, 0)
    fire.setLowLife(0.3f)
    fire.setHighLife(.5f)
    //fire.getParticleInfluencer().setVelocityVariation(0.3f)

    fire
  }

  def makeGeometricExplosion(initialVel:Float)(implicit app: SimpleApplication) = {
    val explosion = new ParticleEmitter("GeometricExplosion", Type.Triangle, 100)
    import com.jme3.material.Material
    val flash_mat = new Material(app.getAssetManager, "Common/MatDefs/Misc/Particle.j3md")
    flash_mat.setTexture("Texture", app.getAssetManager.loadTexture("assets/Effect/4shapes.png"))
    explosion.setMaterial(flash_mat)
    explosion.setImagesX(2) // columns
    explosion.setImagesY(2) // rows
    explosion.setSelectRandomImage(true)

    explosion.setParticleInfluencer(new SphereParticleInfluencer(fromCenterVelocity = initialVel))
    explosion.setShape(new EmitterSphereSurfaceShape(Vector3f.ZERO, 5.5f))
    explosion.getParticleInfluencer().setInitialVelocity(Vector3f.ZERO)
    explosion.getParticleInfluencer().setVelocityVariation(0f)

    explosion.setParticlesPerSec(0)
    explosion.setStartColor(new ColorRGBA(0f, 0f, .5f, .1f))
    explosion.setEndColor(new ColorRGBA(1f, 1f, 1f, 1f))
    explosion.setRandomAngle(false)
    explosion.setStartSize(.5f)
    explosion.setEndSize(0.5f)
    explosion.setGravity(0, 0, 0)
    explosion.setLowLife(0.5f)
    explosion.setHighLife(.5f)

    explosion
  }



  def makeFireball()(implicit app: SimpleApplication): ParticleEmitter = {
    val fire = new ParticleEmitter("fireball", Type.Triangle, 30)
    import com.jme3.material.Material
    val flash_mat = new Material(app.getAssetManager, "Common/MatDefs/Misc/Particle.j3md")
    flash_mat.setTexture("Texture", app.getAssetManager.loadTexture("Effects/Explosion/flame.png"))
    fire.setMaterial(flash_mat)
    fire.setImagesX(2) // columns
    fire.setImagesY(2) // rows
    fire.setSelectRandomImage(true)

    fire.setEndColor(new ColorRGBA(1f, 0f, 0f, 1f)) // red
    fire.setStartColor(new ColorRGBA(1f, 1f, 0f, 0.5f)) // yellow
    fire.getParticleInfluencer().setInitialVelocity(new Vector3f(0, 2, 0))
    fire.setStartSize(.5f)
    fire.setEndSize(0.1f)
    fire.setGravity(0, 0, 0)
    fire.setLowLife(0.5f)
    fire.setHighLife(3f)
    fire.getParticleInfluencer().setVelocityVariation(0.3f)
    fire
  }



}
