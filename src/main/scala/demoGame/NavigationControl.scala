package demoGame

import com.jme3.app.SimpleApplication
import com.jme3.bullet.control.BetterCharacterControl
import com.jme3.math.{ColorRGBA, Vector3f}
import com.jme3.renderer.{RenderManager, ViewPort}
import com.jme3.scene.Spatial
import com.jme3.scene.control.AbstractControl
import demoGame.Vector3FHelper._


class NavigationControl(control: BetterCharacterControl,
                        nav: Navigation,
                        var speed:Float,
                       )(implicit app:SimpleApplication) extends AbstractControl {
  def currentPosition: Vector3f = control.getSpatial.getLocalTranslation
  var drawPath = true
  var drawingPath:Seq[Spatial] = Seq()

  var arrivalTolerance = .3f
  private var _moveTo: Vector3f = currentPosition


  var pathExists = false
  var pathCompleted = false
  var currentPath: Seq[Vector3f] = Seq()

  def setMoveTo(target: Vector3f):Unit   = {
    _moveTo = target.clone()
    recalculatePath()
  }

  def getMoveTo:Vector3f = _moveTo

  def recalculatePath(): Unit = {
    val pathOpt = nav.findPath(currentPosition, _moveTo)
    pathOpt match {
      case Some(p) =>
        currentPath = p
        pathExists = true
        pathCompleted = false
        if(drawPath){
          drawingPath.foreach(s => s.removeFromParent())
          drawingPath = if(p.size >= 2)
            p.sliding(2).toSeq.map(x => MakerUtils.makeArrow(x(0), x(1), "pathArrow", MakerUtils.makeUnshaded(ColorRGBA.Yellow)))
          else Seq[Spatial]()
        }
      case None =>
        currentPath = Seq()
        pathExists = false
        pathCompleted = false
    }
  }

  override def controlUpdate(tpf: Float): Unit = {
    if(pathExists && !pathCompleted){
      currentPath = currentPath.dropWhile(v => v.distance(currentPosition) < arrivalTolerance)
      if(currentPath.isEmpty) {
        pathCompleted = true
        control.setViewDirection(new Vector3f(0, 0, 0))
      }
      else {
        val target = currentPath.head
        val direction = target - currentPosition
        control.setWalkDirection(direction.normalize() * speed)
      }
    } else {
      control.setViewDirection(new Vector3f(0, 0, 0))
    }
  }

  override def controlRender(rm: RenderManager, vp: ViewPort): Unit = {}
}
