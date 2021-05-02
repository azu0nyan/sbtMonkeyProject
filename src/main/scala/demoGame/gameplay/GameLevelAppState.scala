package demoGame.gameplay

import com.jme3.app.state.{AbstractAppState, AppStateManager, BaseAppState}
import com.jme3.app.{Application, SimpleApplication}
import com.jme3.bullet.BulletAppState
import com.jme3.bullet.control.BetterCharacterControl
import com.jme3.collision.CollisionResults
import com.jme3.input.ChaseCamera
import com.jme3.math._
import com.jme3.scene.{Geometry, Node, Spatial}
import demoGame.graphics.{ColorUtils, SetColorFromTime}
import demoGame.{CharacterInputControl, _}
import demoGame.gameplay.CreatureInfo.{AngryBox, AwakenCylinder, CreatureInfo}
import JmeImplicits3FHelper._

import javax.imageio.ImageIO
import scala.jdk.CollectionConverters._
import scala.util.Random

class GameLevelAppState(val levelName: String = "maps/lvl1.png")(implicit val app: SimpleApplication) extends BaseAppState {
  implicit val level: GameLevelAppState = this
  implicit var bulletAppState: BulletAppState = _

  //  implicit val  app2:SimpleApplication = app
  var nav: Navigation = _
  var playerCharacter: Spatial = _

  override def initialize( application: Application): Unit = {

    bulletAppState = new BulletAppState()
    app.getStateManager.attach(bulletAppState)
    //    bulletAppState.setDebugEnabled(true)


    val levelSolidObjects = initMap()
    //nav mesh
    nav = new Navigation(levelSolidObjects, false)

    playerCharacter = spawnPlayerCharacter()
    val chaseCam = new ChaseCamera(app.getCamera, playerCharacter, app.getInputManager)
    chaseCam.setInvertVerticalAxis(true)
    chaseCam.setDragToRotate(true)
    chaseCam.setMaxDistance(150)
    chaseCam.setDefaultDistance(100)
    chaseCam.setLookAtOffset(new Vector3f(0, 3, 0))


    for (i <- 0 until 5) {
      spawnRandomEnemy()

    }

    for(i <- 0 until 10)
      spawnRandomGold()

  }


  def spawnRandomEnemy() = {
    val pos = findSpawnPosition()
    val (sp, cc, nc) = CreatureOps.makeCreature(pos, CreatureInfo.infoFromType(AngryBox(math.random().toFloat)))
    val botAi = new BotAiControl(nc, playerCharacter)
    sp.addControl(botAi)
    botAi
  }

  def randomLocation: Vector3f = {
    new Vector3f((new Random().nextFloat() - .5f) * 100, .5f, (new Random().nextFloat() - .5f) * 100f)
  }

  def findSpawnPosition(attempts: Int = 10): Vector3f = {
    Iterator.continually(randomLocation)
      .take(attempts).find(v => isValidSpawnLocation(v)).getOrElse(randomLocation)
  }

  def isValidSpawnLocation(pos: Vector3f): Boolean = {
    val from = pos + new Vector3f(0f, 1000f, 0f)
    val to = pos
    val tr = bulletAppState.getPhysicsSpace.rayTest(from, to)
    tr.isEmpty | tr.asScala.forall(t => (1f - t.getHitFraction) * 1000f < .1f)
  }

  def spawnPlayerCharacter(): Spatial = {
    val (sp, cc, nc) = CreatureOps.makeCreature(new Vector3f(0f, 0f, 0f), new CreatureInfo("Player", 100, 100, 10, 20, AngryBox(.2f), 10))
    nc.setEnabled(false)
    sp.addControl(new CharacterInputControl(cc))
    sp
  }


  def spawnRandomGold() = {
    spawnGold(findSpawnPosition(), (new Random().nextInt(5) + 1) * 100)
  }
  def spawnGold(at: Vector3f, amount: Int) = {
    val size = math.pow(amount, 1 / 3f).toFloat * .1f
    val gold = MakerUtils.makeBox(at, new Vector3f(size, size, size), "gold", MakerUtils.makeShaded(ColorRGBA.Yellow))
    new GoldPileControl(gold, amount)
  }


  def initMap(): Seq[Geometry] = {
    var solid: Seq[Geometry] = Seq()
    val mapSize = 100f

    val mapImg = ImageIO.read(getClass.getClassLoader.getResource(levelName))
    val (msx, msy) = (mapImg.getWidth, mapImg.getHeight)
    val blockSize = mapSize / msx
    val map: IndexedSeq[IndexedSeq[Int]] =
      for (i <- 0 until msx) yield for (j <- 0 until msy) yield mapImg.getRGB(i, j)
    val floor = MakerUtils.makeBox(new Vector3f(0f, -0.1f, 0f), new Vector3f(mapSize / 2, 0.1f, mapSize / 2), "floor", MakerUtils.makeShaded(ColorRGBA.Gray))
    MakerUtils.makeRigid(floor, 0)
    solid = solid :+ floor
    val angle = new Vector3f(-mapSize / 2f, blockSize / 2f, -mapSize / 2f) + new Vector3f(blockSize / 2f, 0f, blockSize / 2f)

    for (i <- 0 until msx; j <- 0 until msy) {
      val pos = angle + new Vector3f(blockSize, 0, blockSize) * new Vector3f(i.toFloat, 0f, j.toFloat)
      map(i)(j) match {
        case z if (0xFF000000 & z) == 0 => //skip transparent
        case 0xFFFF0000 =>
          val mat = MakerUtils.makeUnshaded(ColorRGBA.Red)
          val b = MakerUtils.makeBox(pos, blockSize / 2f, "controlPoints", mat)
          MakerUtils.makeUtility(b)
          b.addControl(new SetColorFromTime(mat, timeFunc = x => FastMath.sin(x * 3), color0 = ColorRGBA.Red.setAlpha(.3f), color1 = ColorRGBA.Red.setAlpha(.5f)))
        case _ =>
          val c = ColorUtils.colorRGBAFromInt(map(i)(j))
          val b = MakerUtils.makeBox(pos, blockSize / 2f, "wall", MakerUtils.makeShaded(c))
          MakerUtils.makeRigid(b, 0f)
          solid = solid :+ b

      }
    }
    solid
  }


  override def update(tpf: Float): Unit = {

  }
  override def cleanup(app: Application): Unit = {}
  override def onEnable(): Unit = {}
  override def onDisable(): Unit = {}
}
