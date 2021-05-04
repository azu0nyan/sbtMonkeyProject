package demoGame.gameplay

import com.jme3.app.SimpleApplication
import com.jme3.bullet.control.BetterCharacterControl
import com.jme3.collision.CollisionResults
import com.jme3.math.{ColorRGBA, FastMath, Quaternion, Ray, Vector3f}
import com.jme3.scene.{Node, Spatial}
import demoGame.{MakerUtils, NavigationControl}
import demoGame.gameplay.CreatureInfo.{CreatureInfo, CreatureType}

object CreatureOps {


  def makeCreature(pos: Vector3f, info: CreatureInfo)(implicit level: GameLevelAppState): (Node, CreatureMovementControl, NavigationControl) = {
    /*  val r = new Ray(new Vector3f(0, 100, 0), new Vector3f(0, -1, 0))
   val res = new CollisionResults
   app.getRootNode.collideWith(r, res)
   val pos = if (res.size() >= 1) {
     res.getClosestCollision.getContactPoint + new Vector3f(0f, .75f, 0f)
   } else new Vector3f(0f, 2f, 0f)*/
    val sp = makeCreatureNode(info.creatureType)(level)
    sp.setLocalTranslation(pos)
    val cc = MakerUtils.makeCharacterControl(sp)(level.app)
    val nc = new NavigationControl(cc, level.nav)(level.app)
    sp.addControl(nc)
    sp.addControl(new CreatureControl(info))
    (sp, cc, nc)
  }

  def makeCreatureNode(creatureType: CreatureType)(implicit level: GameLevelAppState): Node =
    creatureType match {
      case CreatureInfo.AngryBox(angry) =>
        val size =.2f + angry
        val g = MakerUtils.makeBox(new Vector3f(), new Vector3f(size, size, size), "AngryBox",
          MakerUtils.makeShadedCached(ColorRGBA.Red)(level.app), None)(level.app)
        val dummyParent = new Node()
        level.levelNode.attachChild(dummyParent)
        g.setLocalTranslation(0f, size, 0f)
        dummyParent.attachChild(g)
        dummyParent
      case CreatureInfo.AwakenCylinder() =>
        val g = MakerUtils.makeCylinder(new Vector3f(0f, .75f, 0f), .5f, 1.5f, "AwakenCylinder",
          MakerUtils.makeShadedCached(ColorRGBA.Pink)(level.app), None)(level.app)
        val rot = new Quaternion().fromAngleAxis(FastMath.HALF_PI, new Vector3f(1, 0, 0))
        g.setLocalRotation(rot)
        val dummyParent = new Node()
        level.levelNode.attachChild(dummyParent)
        dummyParent.attachChild(g)
        dummyParent
    }


}
