package examlples

import com.jme3.font.BitmapText
import com.jme3.app.SimpleApplication
import com.jme3.collision.{CollisionResult, CollisionResults}
import com.jme3.input.{KeyInput, MouseInput}
import com.jme3.input.controls.{ActionListener, AnalogListener, KeyTrigger, MouseButtonTrigger}
import com.jme3.material.Material
import com.jme3.math.{ColorRGBA, Ray, Vector3f}
import com.jme3.scene.Geometry
import com.jme3.scene.shape.Box

import scala.util.Random

object PickingExample {
  def main(args: Array[String]): Unit = {
    val app = new PickingExample()
    app.start() // start the game

  }
}
class PickingExample extends SimpleApplication {

  def makeBox(pos: Vector3f, name: String, color: ColorRGBA): Unit = {
    val b = new Box(.5f, .5f, .5f) // create cube shape
    val box = new Geometry(name, b) // create cube geometry from the shape
    val mat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md") // create a simple material
    mat.setColor("Color", color) // set color of material to blue

    box.setMaterial(mat) // set the cube's material
    box.setLocalTranslation(pos)
    rootNode.attachChild(box) // make the cube appear in the scene
  }

  override def simpleInitApp(): Unit = {
    //    flyCam.setMoveSpeed(0f)

    //    makeBox(new Vector3f(0f, 0f, 0f), "one", ColorRGBA.Red)
    //    makeBox(new Vector3f(3f, 0f, 0f), "three", ColorRGBA.Blue)
    //    makeBox(new Vector3f(5f, 0f, 0f), "five", ColorRGBA.Yellow)
    //    makeBox(new Vector3f(7f, 0f, 0f), "seven", ColorRGBA.Cyan)
    for (i <- 0 until 10; j <- 0 until 10) {
      makeBox(new Vector3f(i.toFloat * 2f, j.toFloat * 2f, 0f), s"$i $j", new ColorRGBA(math.random().toFloat, math.random().toFloat, math.random().toFloat, 1f))
    }

    initCrossHairs()
  }


  def initCrossHairs(): Unit = {
    setDisplayStatView(false)
    guiFont = assetManager.loadFont("Interface/Fonts/Default.fnt")
    val ch = new BitmapText(guiFont, false)
    ch.setSize(guiFont.getCharSet.getRenderedSize * 2 toFloat)
    ch.setText("+") // crosshairs

    ch.setLocalTranslation( // center
      settings.getWidth / 2 - ch.getLineWidth / 2, settings.getHeight / 2 + ch.getLineHeight / 2, 0)
    guiNode.attachChild(ch)

    inputManager.addMapping("shoot", new KeyTrigger(KeyInput.KEY_SPACE), new MouseButtonTrigger(MouseInput.BUTTON_RIGHT))

    inputManager.addListener(new ActionListener {
      override def onAction(name: String, isPressed: Boolean, tpf: Float): Unit = {
        if (isPressed) {
          println("cast")

          val c = new CollisionResults()
          val ray = new Ray(cam.getLocation, cam.getDirection)
          rootNode.collideWith(ray, c)

          if (c.size() > 0) {
            val closest = c.getClosestCollision
            println(s"${closest.getGeometry.getName}")
            closest.getGeometry.removeFromParent()
//            closest.getGeometry.getMaterial.setColor()
          }
        }

      }
    }, "shoot")
  }

  override def simpleUpdate(tpf: Float): Unit = {
    // geom.setLocalTranslation(geom.getLocalTranslation.add(new Vector3f(tpf, tpf, tpf)))
  }
}

