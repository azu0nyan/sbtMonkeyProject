package examlples


import com.jme3.app.SimpleApplication
import com.jme3.input.KeyInput
import com.jme3.input.controls.{AnalogListener, KeyTrigger}
import com.jme3.material.Material
import com.jme3.scene.Geometry
import com.jme3.scene.shape.Box
import com.jme3.math.{ColorRGBA, Vector3f}
import com.jme3.system.AppSettings


/** Sample 1 - how to get started with the most simple JME 3 application.
 * Display a blue 3D cube and view from all sides by
 * moving the mouse and pressing the WASD keys. */
object InputExample {
  def main(args: Array[String]): Unit = {
    val app = new InputExample()
    app.start() // start the game

  }
}
class InputExample extends SimpleApplication {
  var geom:Geometry = _

  override def simpleInitApp(): Unit = {
    flyCam.setMoveSpeed(0f)
    val b = new Box(1, 1, 1) // create cube shape
    geom = new Geometry("Box", b) // create cube geometry from the shape
    val mat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md") // create a simple material
    mat.setColor("Color", ColorRGBA.Blue) // set color of material to blue

    geom.setMaterial(mat) // set the cube's material

    rootNode.attachChild(geom) // make the cube appear in the scene

    inputManager.addMapping("LEFT", new KeyTrigger(KeyInput.KEY_E))

    inputManager.addListener(new AnalogListener {
      override def onAnalog(name: String, value: Float, tpf: Float): Unit = {
        geom.setLocalTranslation(geom.getLocalTranslation.add(new Vector3f(tpf, 0f, 0f)))
      }
    }, "LEFT")




  }

  override def simpleUpdate(tpf: Float): Unit = {
   // geom.setLocalTranslation(geom.getLocalTranslation.add(new Vector3f(tpf, tpf, tpf)))
  }
}

