package demoGame

import com.jme3.math
import com.jme3.math.{ColorRGBA, Vector3f}

object JmeImplicits3FHelper {

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


}
