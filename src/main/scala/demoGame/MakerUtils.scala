package demoGame

import com.jme3.app.SimpleApplication
import com.jme3.bullet.BulletAppState
import com.jme3.bullet.control.RigidBodyControl
import com.jme3.material.Material
import com.jme3.math.{ColorRGBA, Vector3f}
import com.jme3.renderer.queue.RenderQueue.ShadowMode
import com.jme3.scene.Geometry
import com.jme3.scene.shape.{Box, Sphere}
import com.jme3.util.TangentBinormalGenerator

object MakerUtils {

  def makeRigid(g:Geometry, mass:Float)(implicit  app:SimpleApplication):RigidBodyControl = {
    val phy = new RigidBodyControl(mass)
    g.addControl(phy)
    app.getStateManager.getState(classOf[BulletAppState]).getPhysicsSpace.add(phy)
    phy.setKinematicSpatial(mass == 0f)
    phy
  }

  def makeUnshaded(color: ColorRGBA)(implicit  app:SimpleApplication): Material = {
    val mat = new Material(app.getAssetManager, "Common/MatDefs/Misc/Unshaded.j3md")
    mat.setColor("Color", color)
    mat
  }

  def makeShaded(color: ColorRGBA)(implicit  app:SimpleApplication): Material = {
    val mat = new Material(app.getAssetManager, "Common/MatDefs/Light/Lighting.j3md")
    mat.setBoolean("UseMaterialColors", true)
    mat.setColor("Diffuse", color)
    mat.setColor("Specular", ColorRGBA.White)
    mat.setFloat("Shininess", 64f)
    mat
  }

  def makeShadedTextured(diffusePath: String = "Textures/Terrain/Pond/Pond.jpg",
                         normalPath: String = "Textures/Terrain/Pond/Pond_normal.png")
                        (implicit  app:SimpleApplication): Material = {
    val mat = new Material(app.getAssetManager, "Common/MatDefs/Light/Lighting.j3md")
    mat.setTexture("DiffuseMap", app.getAssetManager.loadTexture(diffusePath))
    mat.setTexture("NormalMap", app.getAssetManager.loadTexture(normalPath))
    mat.setBoolean("UseMaterialColors", true)
    mat.setColor("Diffuse", ColorRGBA.White)
    mat.setColor("Specular", ColorRGBA.White)
    mat.setFloat("Shininess", 64f) // [0,128]
    mat
  }

  def makeBox(pos: Vector3f, size: Vector3f, name: String, mat: Material)(implicit  app:SimpleApplication): Geometry = {
    val b = new Box(size.x, size.y, size.z) // create cube shape
    val box = new Geometry(name, b) // create cube geometry from the shape

    box.setMaterial(mat) // set the cube's material
    box.setLocalTranslation(pos)
    app.getRootNode.attachChild(box) // make the cube appear in the scene

    TangentBinormalGenerator.generate(b)
    box.setShadowMode(ShadowMode.CastAndReceive)
    box
  }

  def makeSphere(pos: Vector3f, radius:Float,  name: String, mat: Material)(implicit  app:SimpleApplication): Geometry = {
    val b = new Sphere(16, 16, radius) // create cube shape
    val box = new Geometry(name, b) // create cube geometry from the shape

    box.setMaterial(mat) // set the cube's material
    box.setLocalTranslation(pos)
    app.getRootNode.attachChild(box) // make the cube appear in the scene
    box
  }
}
