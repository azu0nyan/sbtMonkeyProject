package examlples


import com.jme3.app.SimpleApplication
import com.jme3.light.{AmbientLight, DirectionalLight, PointLight}
import com.jme3.material.Material
import com.jme3.math.{ColorRGBA, Vector3f}
import com.jme3.renderer.queue.RenderQueue.ShadowMode
import com.jme3.scene.Geometry
import com.jme3.scene.shape.{Box, Sphere}
import com.jme3.util.TangentBinormalGenerator


object LightShadowsTextures {
  def main(args: Array[String]): Unit = {
    val app = new LightShadowsTextures()
    app.setShowSettings(false)
    app.start() // start the game

  }
}
class LightShadowsTextures extends SimpleApplication {

  def makeUnshaded(color: ColorRGBA): Material = {
    val mat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md")
    mat.setColor("Color", color)
    mat
  }

  def makeShaded(color: ColorRGBA): Material = {
    val mat = new Material(assetManager, "Common/MatDefs/Light/Lighting.j3md")
    mat.setBoolean("UseMaterialColors", true)
    mat.setColor("Diffuse", color)
    mat.setColor("Specular", ColorRGBA.White)
    mat.setFloat("Shininess", 64f)
    mat.setColor("Ambient", ColorRGBA.Red)
    mat
  }

  def makeShadedTextured(diffusePath: String = "Textures/Terrain/Pond/Pond.jpg", normalPath: String = "Textures/Terrain/Pond/Pond_normal.png"): Material = {
    val mat = new Material(assetManager, "Common/MatDefs/Light/Lighting.j3md")
    mat.setTexture("DiffuseMap", assetManager.loadTexture(diffusePath))
    mat.setTexture("NormalMap", assetManager.loadTexture(normalPath))
    mat.setBoolean("UseMaterialColors", true)
    mat.setColor("Diffuse", ColorRGBA.White)
    mat.setColor("Specular", ColorRGBA.White)
    mat.setFloat("Shininess", 64f) // [0,128]
    mat.setColor("Ambient", ColorRGBA.Red)
    mat
  }

  def makeBox(pos: Vector3f, size: Vector3f, name: String, mat: Material): Geometry = {
    val b = new Box(size.x, size.y, size.z) // create cube shape
    val box = new Geometry(name, b) // create cube geometry from the shape

    box.setMaterial(mat) // set the cube's material
    box.setLocalTranslation(pos)
    rootNode.attachChild(box) // make the cube appear in the scene

    TangentBinormalGenerator.generate(b)
    box.setShadowMode(ShadowMode.CastAndReceive)
    box
  }

  def addSun(): DirectionalLight = {
    new PointLight()
    val sun: DirectionalLight = new DirectionalLight
    sun.setDirection(new Vector3f(1, -1, -(2)).normalizeLocal)
    sun.setColor(ColorRGBA.White)
    rootNode.addLight(sun)
    sun

  }


  def addAmbient():AmbientLight = {
    import com.jme3.light.AmbientLight
    import com.jme3.math.ColorRGBA
    val al = new AmbientLight
    al.setColor(ColorRGBA.White.mult(0.1f))
    rootNode.addLight(al)
    al
  }

  def addShadows(light:DirectionalLight) = {
    import com.jme3.post.FilterPostProcessor
    import com.jme3.shadow.DirectionalLightShadowFilter
    import com.jme3.shadow.DirectionalLightShadowRenderer
    val SHADOWMAP_SIZE = 2048
    val dlsr = new DirectionalLightShadowRenderer(assetManager, SHADOWMAP_SIZE, 3)
    dlsr.setLight(light)
    viewPort.addProcessor(dlsr)

    val dlsf = new DirectionalLightShadowFilter(assetManager, SHADOWMAP_SIZE, 3)
    dlsf.setLight(light)
    dlsf.setEnabled(true)
    val fpp = new FilterPostProcessor(assetManager)
    fpp.addFilter(dlsf)
    viewPort.addProcessor(fpp)

//    rootNode.setShadowMode(ShadowMode.CastAndReceive)
  }

  def addSSAO() = {
    import com.jme3.post.FilterPostProcessor
    import com.jme3.post.ssao.SSAOFilter
    val fpp = new FilterPostProcessor(assetManager)
    val ssaoFilter = new SSAOFilter(1.94f, 3.92f, 0.2f, 0.1f)
    fpp.addFilter(ssaoFilter)
    viewPort.addProcessor(fpp)
  }

  def addSkyBox() = {
    import com.jme3.util.SkyFactory
    rootNode.attachChild(
      SkyFactory.createSky(getAssetManager, "Textures/Sky/Bright/BrightSky.dds", SkyFactory.EnvMapType.CubeMap)
    )
  }

  override def simpleInitApp(): Unit = {
    flyCam.setMoveSpeed(100)
    makeBox(new Vector3f(0, 0, 0), new Vector3f(1, 1, 1), "", makeUnshaded(ColorRGBA.Red))
    makeBox(new Vector3f(5, 0, 0), new Vector3f(1, 20, 1), "", makeShaded(ColorRGBA.Red))
    makeBox(new Vector3f(-5, 0, 0), new Vector3f(1, 1, 1), "",  makeShadedTextured("assets/Textures/lnmo.jpg"))
    makeBox(new Vector3f(0, -2f, 0), new Vector3f(100, 0.1f, 100), "ground", makeShaded(ColorRGBA.White))
    val sun = addSun()
    addAmbient()
    addShadows(sun)
    addSSAO()
    addSkyBox()
  }


}

