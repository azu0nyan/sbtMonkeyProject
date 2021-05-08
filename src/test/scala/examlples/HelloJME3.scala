package examlples


import com.jme3.app.SimpleApplication
import com.jme3.material.Material
import com.jme3.math.ColorRGBA
import com.jme3.scene.Geometry
import com.jme3.scene.shape.Box


/** Sample 1 - how to get started with the most simple JME 3 application.
 * Display a blue 3D cube and view from all sides by
 * moving the mouse and pressing the WASD keys. */
object HelloJME3 {
  def main(args: Array[String]): Unit = {
    val app = new HelloJME3()
    app.start() // start the game

  }
}
class HelloJME3 extends SimpleApplication {
  override def simpleInitApp(): Unit = {

    val b = new Box(1, 1, 1) // create cube shape
    val geom = new Geometry("Box", b) // create cube geometry from the shape
    val mat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md") // create a simple material
    mat.setColor("Color", ColorRGBA.Blue) // set color of material to blue

    geom.setMaterial(mat) // set the cube's material

    rootNode.attachChild(geom) // make the cube appear in the scene

  }
}
