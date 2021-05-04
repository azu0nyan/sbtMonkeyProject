package demoGame.graphics

import com.jme3.app.SimpleApplication
import com.jme3.effect.ParticleEmitter
import com.jme3.effect.ParticleMesh.Type
import com.jme3.math.{ColorRGBA, Vector3f}

object ParticleUtils {
    def makeFireball()(implicit app:SimpleApplication):ParticleEmitter = {
      val fire = new ParticleEmitter("fireball", Type.Triangle, 30)
      import com.jme3.material.Material
      val flash_mat = new Material(app.getAssetManager, "Common/MatDefs/Misc/Particle.j3md")
      flash_mat.setTexture("Texture", app.getAssetManager.loadTexture("Effects/Explosion/flame.png"))
      fire.setMaterial(flash_mat)
      fire.setImagesX(2) // columns
      fire.setImagesY(2) // rows
      fire.setSelectRandomImage(true)

      fire.setEndColor(  new ColorRGBA(1f, 0f, 0f, 1f));   // red
      fire.setStartColor(new ColorRGBA(1f, 1f, 0f, 0.5f)); // yellow
      fire.getParticleInfluencer().setInitialVelocity(new Vector3f(0,2,0));
      fire.setStartSize(1.5f);
      fire.setEndSize(0.1f);
      fire.setGravity(0,0,0);
      fire.setLowLife(0.5f);
      fire.setHighLife(3f);
      fire.getParticleInfluencer().setVelocityVariation(0.3f);
      fire
    }
}
