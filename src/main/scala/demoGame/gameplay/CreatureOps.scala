package demoGame.gameplay

import com.jme3.app.SimpleApplication
import com.jme3.bullet.control.BetterCharacterControl
import com.jme3.collision.CollisionResults
import com.jme3.math.{ColorRGBA, FastMath, Quaternion, Ray, Vector3f}
import com.jme3.scene.{Node, Spatial}
import demoGame.{MakerUtils, NavigationControl}
import demoGame.gameplay.CreatureInfo.{CreatureInfo, CreatureType}

object CreatureOps {



  def makeCreature(pos: Vector3f, info: CreatureInfo)(implicit level: GameLevelAppState): (Spatial, BetterCharacterControl, NavigationControl) = {
    /*  val r = new Ray(new Vector3f(0, 100, 0), new Vector3f(0, -1, 0))
   val res = new CollisionResults
   app.getRootNode.collideWith(r, res)
   val pos = if (res.size() >= 1) {
     res.getClosestCollision.getContactPoint + new Vector3f(0f, .75f, 0f)
   } else new Vector3f(0f, 2f, 0f)*/
    val sp = makeCreatureSpatial(info.creatureType)(level.app)
    sp.setLocalTranslation(pos)
    val cc = MakerUtils.makeCharacterControl(sp)(level.app)
    val nc = new NavigationControl(cc, level.nav, info.speed)(level.app)
    sp.addControl(nc)
    sp.addControl(new CreatureControl(info))
    (sp, cc, nc)
  }

  def makeCreatureSpatial(creatureType: CreatureType)(implicit app: SimpleApplication): Spatial =
    creatureType match {
      case CreatureInfo.AngryBox(angry) =>
        val size = .2f + angry
        val g = MakerUtils.makeBox(new Vector3f(), new Vector3f(size, size, size), "AngryBox", MakerUtils.makeShaded(ColorRGBA.Red))
        val dummyParent = new Node()
        g.setLocalTranslation(0f, size, 0f)
        app.getRootNode.attachChild(dummyParent)
        g.removeFromParent()
        dummyParent.attachChild(g)
        dummyParent
      case CreatureInfo.AwakenCylinder() =>
        val g = MakerUtils.makeCylinder(new Vector3f(0f, .75f, 0f), .5f, 1.5f, "AwakenCylinder", MakerUtils.makeShaded(ColorRGBA.Pink))
        val rot = new Quaternion().fromAngleAxis(FastMath.HALF_PI, new Vector3f(1, 0, 0))
        g.setLocalRotation(rot)
        val dummyParent = new Node()
        app.getRootNode.attachChild(dummyParent)
        g.removeFromParent()
        dummyParent.attachChild(g)
        dummyParent
    }


}