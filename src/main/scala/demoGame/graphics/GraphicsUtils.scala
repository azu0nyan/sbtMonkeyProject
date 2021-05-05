package demoGame.graphics

import com.jme3.app.SimpleApplication
import com.jme3.light.{AmbientLight, DirectionalLight}
import com.jme3.math.{ColorRGBA, Vector3f}

object GraphicsUtils {
  def addAmbient()(implicit app: SimpleApplication): AmbientLight = {
    import com.jme3.light.AmbientLight
    import com.jme3.math.ColorRGBA
    val al = new AmbientLight
    al.setColor(ColorRGBA.White.mult(1.3f))
    app.getRootNode.addLight(al)
    al
  }

  def addSun()(implicit app: SimpleApplication): DirectionalLight = {
    val sun: DirectionalLight = new DirectionalLight
    sun.setDirection(new Vector3f(1, -5, -(2)).normalizeLocal)
    sun.setColor(ColorRGBA.White.mult(.8f))

    app.getRootNode.addLight(sun)
    sun

  }
  def addShadows(light: DirectionalLight)(implicit app: SimpleApplication) = {
    import com.jme3.post.FilterPostProcessor
    import com.jme3.shadow.{DirectionalLightShadowFilter, DirectionalLightShadowRenderer}
    val SHADOWMAP_SIZE = 2048
    val dlsr = new DirectionalLightShadowRenderer(app.getAssetManager, SHADOWMAP_SIZE, 3)
    dlsr.setLight(light)
    app.getViewPort.addProcessor(dlsr)

    val dlsf = new DirectionalLightShadowFilter(app.getAssetManager, SHADOWMAP_SIZE, 3)
    dlsf.setLight(light)
    dlsf.setEnabled(true)
    val fpp = new FilterPostProcessor(app.getAssetManager)
    fpp.addFilter(dlsf)
    app.getViewPort.addProcessor(fpp)

    //    rootNode.setShadowMode(ShadowMode.CastAndReceive)
  }

  def addSSAO()(implicit app: SimpleApplication) = {
    import com.jme3.post.FilterPostProcessor
    import com.jme3.post.ssao.SSAOFilter
    val fpp = new FilterPostProcessor(app.getAssetManager)
    val ssaoFilter = new SSAOFilter()
    fpp.addFilter(ssaoFilter)
    app.getViewPort.addProcessor(fpp)
  }

  def addSkyBox()(implicit app: SimpleApplication) = {
    import com.jme3.util.SkyFactory
    app.getRootNode.attachChild(
      SkyFactory.createSky(app.getAssetManager, "Textures/Sky/Bright/BrightSky.dds", SkyFactory.EnvMapType.CubeMap)
    )
  }
}
