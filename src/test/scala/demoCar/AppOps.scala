package demoCar

import com.jme3.app.SimpleApplication
import com.jme3.bullet.BulletAppState
import com.jme3.bullet.control.RigidBodyControl
import com.jme3.light.{AmbientLight, DirectionalLight}
import com.jme3.material.Material
import com.jme3.math.{ColorRGBA, Plane, Transform, Vector3f}
import com.jme3.renderer.queue.RenderQueue.ShadowMode
import com.jme3.scene.{Geometry, Node, Spatial}
import com.jme3.scene.shape.{Box, Cylinder, Sphere}
import com.jme3.terrain.geomipmap.lodcalc.DistanceLodCalculator
import com.jme3.terrain.geomipmap.{TerrainLodControl, TerrainQuad}
import com.jme3.terrain.heightmap.ImageBasedHeightMap
import com.jme3.texture.Texture.WrapMode
import com.jme3.util.TangentBinormalGenerator



abstract class AppOps extends SimpleApplication {
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
    mat.setColor("Ambient", ColorRGBA.White)
    mat.setFloat("Shininess", 64f)
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
    mat.setColor("Ambient", ColorRGBA.White)
    mat
  }


  def makeCylinder(trans: Transform, name: String, mat: Material, parent: Option[Node] = None, radius:Float, height:Float, axisSamples: Int = 2, radialSamples: Int = 16): Geometry = {
    val c = new Cylinder(axisSamples, radialSamples, radius, height, true)
    val cylinder = new Geometry(name, c)

    cylinder.setMaterial(mat)
    cylinder.setLocalTransform(trans)
    parent.getOrElse(rootNode).attachChild(cylinder)

    TangentBinormalGenerator.generate(cylinder)
    cylinder.setShadowMode(ShadowMode.CastAndReceive)
    cylinder
  }
  def makeBox(pos: Vector3f, size: Vector3f, name: String, mat: Material, parent: Option[Node] = None): Geometry = {
    val b = new Box(size.x, size.y, size.z) // create cube shape
    val box = new Geometry(name, b) // create cube geometry from the shape

    box.setMaterial(mat) // set the cube's material
    box.setLocalTranslation(pos)
    parent.getOrElse(rootNode).attachChild(box) // make the cube appear in the scene

    TangentBinormalGenerator.generate(b)
    box.setShadowMode(ShadowMode.CastAndReceive)
    box
  }


  def addSun(): DirectionalLight = {
    val sun: DirectionalLight = new DirectionalLight
    sun.setDirection(new Vector3f(1, -1, -(2)).normalizeLocal)
    sun.setColor(ColorRGBA.White)
    rootNode.addLight(sun)
    sun

  }


  def addAmbient(): AmbientLight = {
    import com.jme3.light.AmbientLight
    import com.jme3.math.ColorRGBA
    val al = new AmbientLight
    al.setColor(ColorRGBA.White.mult(0.3f))
    rootNode.addLight(al)
    al
  }

  def addShadows(light: DirectionalLight) = {
    import com.jme3.post.FilterPostProcessor
    import com.jme3.shadow.DirectionalLightShadowFilter
    import com.jme3.shadow.DirectionalLightShadowRenderer
    val SHADOWMAP_SIZE = 1024
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
    val ssaoFilter = new SSAOFilter(5.1f, 2.5f, 0.2f, 0.1f)
    fpp.addFilter(ssaoFilter)
    viewPort.addProcessor(fpp)
  }

  def addSkyBox() = {
    import com.jme3.util.SkyFactory
    rootNode.attachChild(
      SkyFactory.createSky(getAssetManager, "Textures/Sky/Bright/BrightSky.dds", SkyFactory.EnvMapType.CubeMap)
    )
  }


  var bulletAppState:BulletAppState = _

  def initBullet(): Unit = {
    bulletAppState = new BulletAppState()
    stateManager.attach(bulletAppState)
  }

  def makeRigid(g: Geometry, mass: Float): RigidBodyControl = {
    val phy = new RigidBodyControl(mass)
    g.addControl(phy)
    bulletAppState.getPhysicsSpace.add(phy)
    phy.setKinematicSpatial(mass == 0f)
    //    phy.setCcdSweptSphereRadius(.5f)
    phy
  }

  def makeTerrain():Unit = {
    /** 1. Create terrain material and load four textures into it. *//** 1. Create terrain material and load four textures into it. */

    val mat_terrain = new Material(assetManager, "Common/MatDefs/Terrain/Terrain.j3md")

    /** 1.1) Add ALPHA map (for red-blue-green coded splat textures) */
    mat_terrain.setTexture("Alpha", assetManager.loadTexture("Textures/Terrain/splat/alphamap.png"))

    /** 1.2) Add GRASS texture into the red layer (Tex1). */
    val grass = assetManager.loadTexture("Textures/Terrain/splat/grass.jpg")
    grass.setWrap(WrapMode.Repeat)
    mat_terrain.setTexture("Tex1", grass)
    mat_terrain.setFloat("Tex1Scale", 64f)

    /** 1.3) Add DIRT texture into the green layer (Tex2) */
    val dirt = assetManager.loadTexture("Textures/Terrain/splat/dirt.jpg")
    dirt.setWrap(WrapMode.Repeat)
    mat_terrain.setTexture("Tex2", dirt)
    mat_terrain.setFloat("Tex2Scale", 32f)

    /** 1.4) Add ROAD texture into the blue layer (Tex3) */
    val rock = assetManager.loadTexture("Textures/Terrain/splat/road.jpg")
    rock.setWrap(WrapMode.Repeat)
    mat_terrain.setTexture("Tex3", rock)
    mat_terrain.setFloat("Tex3Scale", 128f)

    /* 2.a Create a custom height map from an image */
    val heightMapImage = assetManager.loadTexture("map.png")
    val heightmap = new ImageBasedHeightMap(heightMapImage.getImage, 0.2f)

    /* 2.b Create a random height map */
    //      HillHeightMap heightmap = null;
    //      HillHeightMap.NORMALIZE_RANGE = 100;
    //      try {
    //          heightmap = new HillHeightMap(513, 1000, 50, 100, (byte) 3);
    //      } catch (Exception ex) {
    //          ex.printStackTrace();
    //      }

    heightmap.load

    /** 3. We have prepared material and heightmap.
     * Now we create the actual terrain:
     * 3.1) Create a TerrainQuad and name it "my terrain".
     * 3.2) A good value for terrain tiles is 64x64 -- so we supply 64+1=65.
     * 3.3) We prepared a heightmap of size 512x512 -- so we supply 512+1=513.
     * 3.4) As LOD step scale we supply Vector3f(1,1,1).
     * 3.5) We supply the prepared heightmap itself.
     */
    val patchSize = 65
    val terrain = new TerrainQuad("my terrain", patchSize, 513, heightmap.getHeightMap)

    /** 4. We give the terrain its material, position & scale it, and attach it. */
    terrain.setMaterial(mat_terrain)
    terrain.setLocalTranslation(0, -100, 0)
    terrain.setLocalScale(2f, 1f, 2f)
    rootNode.attachChild(terrain)

    /** 5. The LOD (level of detail) depends on were the camera is: */
    val control = new TerrainLodControl(terrain, getCamera)
    control.setLodCalculator(new DistanceLodCalculator(patchSize, 2.7f)) // patch size, and a multiplier

    terrain.addControl(control)
    terrain.addControl(new RigidBodyControl(0))



    bulletAppState.getPhysicsSpace.add(terrain)


  }

}
