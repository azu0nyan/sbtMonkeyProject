package demoGame

import com.jme3.app.SimpleApplication
import com.jme3.bullet.BulletAppState
import com.jme3.bullet.control.{BetterCharacterControl, RigidBodyControl}
import com.jme3.material.Material
import com.jme3.material.RenderState.BlendMode
import com.jme3.math.{ColorRGBA, Vector3f}
import com.jme3.renderer.queue.RenderQueue.{Bucket, ShadowMode}
import com.jme3.scene.debug.Arrow
import com.jme3.scene.{Geometry, Node, Spatial}
import com.jme3.scene.shape.{Box, Cylinder, Sphere}
import com.jme3.util.TangentBinormalGenerator
import demoGame.JmeImplicits3FHelper.V3Helper

object MakerUtils {


  def makeCharacterControl(g:Spatial)(implicit app  :SimpleApplication) = {
    val cc = new BetterCharacterControl(.5f, 1.5f, 10f)
    g.addControl(cc)
    app.getStateManager.getState(classOf[BulletAppState]).getPhysicsSpace.add(cc)
    cc.setJumpForce(new Vector3f(0, 50, 0))
    cc
  }

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
    mat.setColor("Ambient", color.mult(0.2f).add(ColorRGBA.White.mult(0.1f)))

    mat
  }

  def makeWireframe(colorRGBA: ColorRGBA)(implicit app:SimpleApplication): Material = {
    val material = new Material(app.getAssetManager, "Common/MatDefs/Misc/Unshaded.j3md")
    material.getAdditionalRenderState.setWireframe(true)
    material.setColor("Color", colorRGBA)
    material
  }

  def makeTransparent(geom:Geometry) = {
    geom.getMaterial.getAdditionalRenderState.setBlendMode(BlendMode.Alpha)
    geom.setQueueBucket(Bucket.Transparent)
  }

  def makeUtility(geom:Geometry) = {
    geom.getMaterial.getAdditionalRenderState.setBlendMode(BlendMode.Alpha)
    geom.setQueueBucket(Bucket.Translucent)
    geom.setShadowMode(ShadowMode.Off)
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
    mat.setColor("Ambient", ColorRGBA.White.mult(0.2f))
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

  def makeArrow(from: Vector3f, to: Vector3f, name: String, mat: Material)(implicit  app:SimpleApplication): Geometry = {
    val b = new Arrow(to - from) // create cube shape
    val box = new Geometry(name, b) // create cube geometry from the shape

    box.setMaterial(mat) // set the cube's material
    box.setLocalTranslation(from)
    app.getRootNode.attachChild(box) // make the cube appear in the scene

    box
  }

  def makeCylinder(pos: Vector3f, radius:Float, height:Float, name: String, mat: Material)(implicit app:SimpleApplication): Geometry = {
    val b = new Cylinder(2, 16, radius, height, true) // create cube shape
    val box = new Geometry(name, b) // create cube geometry from the shape

    box.setMaterial(mat) // set the cube's material
    box.setLocalTranslation(pos)
    app.getRootNode.attachChild(box) // make the cube appear in the scene
    box.setShadowMode(ShadowMode.CastAndReceive)
    box
  }

  def makeSphere(pos: Vector3f, radius:Float,  name: String, mat: Material)(implicit  app:SimpleApplication): Geometry = {
    val b = new Sphere(16, 16, radius) // create cube shape
    val box = new Geometry(name, b) // create cube geometry from the shape

    box.setMaterial(mat) // set the cube's material
    box.setLocalTranslation(pos)
    app.getRootNode.attachChild(box) // make the cube appear in the scene
    box.setShadowMode(ShadowMode.CastAndReceive)
    box
  }
}
