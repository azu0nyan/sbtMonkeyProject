package demoCar

import com.jme3.math.Vector3f

object Vector3fImplicits {


  implicit class Vector3fImplicits(val v: Vector3f) extends AnyVal {
    def +(ot: Vector3f): Vector3f = v.add(ot)

    def -(ot: Vector3f): Vector3f = v.subtract(ot)

    def *(ot: Vector3f): Vector3f = v.mult(ot)

    def /(ot: Vector3f): Vector3f = v.divide(ot)

    def +(ot: Float): Vector3f = v.add(new Vector3f(ot, ot, ot))

    def -(ot: Float): Vector3f = v.subtract(new Vector3f(ot, ot, ot))

    def *(ot: Float): Vector3f = v.mult(new Vector3f(ot, ot, ot))

    def /(ot: Float): Vector3f = v.divide(new Vector3f(ot, ot, ot))

    def **(ot: Vector3f): Float = v.dot(ot)

    def x(ot: Vector3f): Vector3f = v.cross(ot)

  }
}
