package demoGame

import com.jme3.math.Vector3f

object Vector3FHelper {

  implicit def floatToVe(f:Float):Vector3f = new Vector3f(f, f, f)

  implicit class V3Helper(val v: Vector3f) extends AnyVal {
    def +(ot:Vector3f):Vector3f = v.add(ot)
    def -(ot:Vector3f):Vector3f = v.subtract(ot)
    def *(ot:Vector3f):Vector3f = v.mult(ot)
    def /(ot:Vector3f):Vector3f = v.divide(ot)
    def **(ot:Vector3f):Float = v.dot(ot)
    def ^(ot:Vector3f):Vector3f = v.cross(ot)
    def +=(ot:Vector3f):Vector3f = v.addLocal(ot)

    def -=(ot:Vector3f):Vector3f = v.subtractLocal(ot)
    def *=(ot:Vector3f):Vector3f = v.multLocal(ot)
    def /=(ot:Vector3f):Vector3f = v.divideLocal(ot)
    def ^=(ot:Vector3f):Vector3f = v.crossLocal(ot)


  }
}
