package demoGame

import com.jme3.math.{FastMath, Vector2f}

object MyMathUtils {

  def directedSmallestAngle(v1:Vector2f, v2:Vector2f):Float = {
    v1.smallestAngleBetween(v2) * math.signum(v1.x * v2.y - v1.y * v2.x)
  }

//  def to0twoPi(a:Float) :Float = {
//    if(a < 0) a % (FastMath.TWO_PI) + FastMath.TWO_PI
//    else if(a >= FastMath.TWO_PI) a % FastMath.TWO_PI
//    else a
//  }

}
