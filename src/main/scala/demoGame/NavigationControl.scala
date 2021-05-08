package demoGame

import com.jme3.app.SimpleApplication
import com.jme3.bullet.control.BetterCharacterControl
import com.jme3.math.{ColorRGBA, Vector2f, Vector3f}
import com.jme3.renderer.{RenderManager, ViewPort}
import com.jme3.scene.Spatial
import com.jme3.scene.control.AbstractControl
import demoGame.JmeImplicits3FHelper._
import demoGame.gameplay.{CreatureMovement, CreatureMovementControl}


class NavigationControl(control: CreatureMovement,
                        nav: Navigation,
                       )(implicit app: SimpleApplication) extends AbstractControl {
  def currentPosition: Vector3f = getSpatial.getLocalTranslation
  var drawPath = false
  var drawingPath: Seq[Spatial] = Seq()

  var arrivalTolerance = .3f
  private var _moveTo: Vector3f = new Vector3f()


  var pathExists = false
  var pathCompleted = false
  var currentPath: Seq[Vector3f] = Seq()

  def setMoveTo(target: Vector3f): Unit = {
    _moveTo = target.clone()
    recalculatePath()
  }

  def getMoveTo: Vector3f = _moveTo


  def calculatePath(to: Vector3f): Option[Seq[Vector3f]] = nav.findPath(currentPosition, to)

  def setPath(path: Seq[Vector3f]): Unit =
    if (path.nonEmpty) {
      currentPath = path
      pathExists = true
      pathCompleted = false
      _moveTo = path.last
    } else {
      currentPath = Seq()
      pathExists = true
      pathCompleted = true
    }


  def recalculatePath(): Unit = {
    val pathOpt = nav.findPath(currentPosition, _moveTo)
    pathOpt match {
      case Some(p) =>
        currentPath = p
        pathExists = true
        pathCompleted = false
        if (drawPath) {
          drawingPath.foreach(s => s.removeFromParent())
          drawingPath = if (p.size >= 2)
            p.sliding(2).toSeq.map(x => MakerUtils.makeArrow(x(0), x(1), "pathArrow",
              MakerUtils.makeUnshaded(ColorRGBA.Yellow), Some(app.getRootNode)))
          else Seq[Spatial]()
        }
      case None =>
        currentPath = Seq()
        pathExists = false
        pathCompleted = false
    }
  }

  override def controlUpdate(tpf: Float): Unit = {
    if (enabled) {
      if (pathExists && !pathCompleted) {
        //игнорируем вертикальную разницу расстояний
        //пропускаем вейпоинты рядом с которымы мы уже находимся
        val flatPosition = new Vector2f(currentPosition.x, currentPosition.z)
        currentPath = currentPath.dropWhile{
          v =>
            val flatV = new Vector2f(v.x, v.z)
            flatV.distance(flatPosition) < arrivalTolerance
        }
        if (currentPath.isEmpty) {
          pathCompleted = true
//          control.setSightDirection(new Vector3f(0, 0, 0))
        } else {
          val target = currentPath.head
          val direction = target - currentPosition
          direction.y = 0
          direction.normalizeLocal()
          control.setMoveDirection(direction)
          control.setSightDirection(direction)
        }
      } else {
        control.setMoveDirection(new Vector3f(0, 0, 0))
      }
    }
  }

  override def controlRender(rm: RenderManager, vp: ViewPort): Unit = {}
}
