package demoGame.graphics.particles

import com.jme3.effect.shapes.EmitterSphereShape
import com.jme3.math.{FastMath, Vector3f}

class EmitterSphereSurfaceShape(center: Vector3f = Vector3f.ZERO, radius: Float = 1f) extends EmitterSphereShape(center, radius){

  override def getRandomPoint(store: Vector3f): Unit = {
    do {
      store.x = FastMath.nextRandomFloat * 2f - 1f
      store.y = FastMath.nextRandomFloat * 2f - 1f
      store.z = FastMath.nextRandomFloat * 2f - 1f
    } while (store.lengthSquared > 1  )

    store.normalizeLocal()
    store.multLocal(radius)
    store.addLocal(center)
  }

}
