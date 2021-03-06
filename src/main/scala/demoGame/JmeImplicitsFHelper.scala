package demoGame

import com.jme3.bounding.{BoundingBox, BoundingSphere, BoundingVolume}
import com.jme3.bullet.control.GhostControl
import com.jme3.math
import com.jme3.math.{ColorRGBA, Vector2f, Vector3f}
import com.jme3.scene.Spatial
import demoGame.gameplay.CreatureControl

import scala.jdk.CollectionConverters.ListHasAsScala

object JmeImplicitsFHelper {

  implicit def floatToVe(f: Float): Vector3f = new Vector3f(f, f, f)

  implicit class V3Helper(val v: Vector3f) extends AnyVal {
    def +(ot: Vector3f): Vector3f = v.add(ot)
    def -(ot: Vector3f): Vector3f = v.subtract(ot)
    def *(ot: Vector3f): Vector3f = v.mult(ot)
    def /(ot: Vector3f): Vector3f = v.divide(ot)
    def **(ot: Vector3f): Float = v.dot(ot)
    def ^(ot: Vector3f): Vector3f = v.cross(ot)
    def +=(ot: Vector3f): Vector3f = v.addLocal(ot)

    def -=(ot: Vector3f): Vector3f = v.subtractLocal(ot)
    def *=(ot: Vector3f): Vector3f = v.multLocal(ot)
    def /=(ot: Vector3f): Vector3f = v.divideLocal(ot)
    def ^=(ot: Vector3f): Vector3f = v.crossLocal(ot)

    def planeProjection:Vector2f = new Vector2f(v.x, v.z)
  }

  implicit class ColorHelper(val c: ColorRGBA) extends AnyVal {
    def setRed(r: Float): ColorRGBA = new ColorRGBA(r, c.g, c.b, c.a)
    def setGreen(g: Float): ColorRGBA = new ColorRGBA(c.r, g, c.b, c.a)
    def setBlue(b: Float): ColorRGBA = new ColorRGBA(c.r, c.g, b, c.a)
    def setAlpha(a: Float): ColorRGBA = new ColorRGBA(c.r, c.g, c.b, a)

    def setAlphaLocal(a: Float): ColorRGBA = {
      c.a = a
      c
    }
  }


  def overlappingSpatials(ghost: GhostControl): Seq[Spatial] = ghost.getOverlappingObjects.asScala
    .filter(o => o.getUserObject != null && o.getUserObject.isInstanceOf[Spatial])
    .map(o => o.getUserObject.asInstanceOf[Spatial]).toSeq

  def overlappingCreatures(ghost: GhostControl): Seq[CreatureControl] =
    overlappingSpatials(ghost)
      .flatMap(sp => Option(sp.getControl(classOf[CreatureControl])))


  def boundingRadius(b:BoundingVolume) :Float = b match {
    case box: BoundingBox => scala.math.max(scala.math.max(box.getXExtent, box.getYExtent), box.getZExtent)
    case sphere: BoundingSphere =>sphere.getRadius
    case _ =>1f
  }



  }
