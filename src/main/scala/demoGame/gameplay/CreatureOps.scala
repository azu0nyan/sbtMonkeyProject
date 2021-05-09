package demoGame.gameplay

import com.jme3.app.SimpleApplication
import com.jme3.bullet.control.BetterCharacterControl
import com.jme3.collision.CollisionResults
import com.jme3.math.{ColorRGBA, FastMath, Quaternion, Ray, Vector3f}
import com.jme3.renderer.queue.RenderQueue.ShadowMode
import com.jme3.scene.{Node, Spatial}
import demoGame.{MakerUtils, NavigationControl}
import demoGame.gameplay.CreatureInfo.{CreatureInfo, CreatureType, infoFromType}

object CreatureOps {


  def makeCreatureFromType(pos: Vector3f, creatureType: CreatureType)(implicit level: GameLevelAppState): (Node, CreatureMovementControl, NavigationControl) = {
    val info = infoFromType(creatureType)
    makeCreature(pos, info)
  }

  def makeCreature(pos: Vector3f, info: CreatureInfo)(implicit level: GameLevelAppState): (Node, CreatureMovementControl, NavigationControl) = {
    val sp = makeCreatureNode(info.creatureType)(level)
    sp.setLocalTranslation(pos)
    val cc = MakerUtils.makeCharacterControl(sp)(level.app)
    val nc = new NavigationControl(cc, level.nav)(level.app)
    sp.addControl(nc)
    val creatureControl = new CreatureControl(info)
    sp.addControl(creatureControl)
    CreatureInfo.addSpells(creatureControl)
    (sp, cc, nc)
  }

  def makeCreatureNode(creatureType: CreatureType)(implicit level: GameLevelAppState): Node =
    creatureType match {
      case CreatureInfo.AngryBox(angry) =>
        val size =.2f + angry
        val g = MakerUtils.makeBox(new Vector3f(), new Vector3f(size, size, size), "AngryBox",
          MakerUtils.makeShadedCached(ColorRGBA.Red)(level.app), None)(level.app)

        val eye1 = MakerUtils.makeBox(Vector3f.ZERO, new Vector3f(.05f + size / 5,.05f + size / 10,.05f + size / 10), "AngryBoxEye",
          MakerUtils.makeShadedCached(ColorRGBA.Yellow)(level.app), None)(level.app)

        val eye2 = MakerUtils.makeBox(Vector3f.ZERO, new Vector3f(.05f + size / 5,.05f + size / 10,.05f + size / 10), "AngryBoxEye",
          MakerUtils.makeShadedCached(ColorRGBA.Yellow)(level.app), None)(level.app)

        g.setShadowMode(ShadowMode.Receive)
        eye1.setShadowMode(ShadowMode.Receive)
        eye2.setShadowMode(ShadowMode.Receive)

        eye1.setLocalTranslation(-size / 2f, size + size / 3f, size)
        eye2.setLocalTranslation(size / 2f, size + size / 3f, size)
        val rot1 = new Quaternion().fromAngleAxis(-.3f, Vector3f.UNIT_Z)
        eye1.setLocalRotation(rot1)
        val rot2 = new Quaternion().fromAngleAxis(.3f, Vector3f.UNIT_Z)
        eye2.setLocalRotation(rot2)

        val parent = new Node()
        level.levelNode.attachChild(parent)
        g.setLocalTranslation(0f, size, 0f) //!!!! size
        parent.attachChild(g)
        parent.attachChild(eye1)
        parent.attachChild(eye2)
        parent
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
