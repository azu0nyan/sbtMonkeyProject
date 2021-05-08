package demoGame.gameplay

import com.jme3.app.state.{AbstractAppState, AppStateManager, BaseAppState}
import com.jme3.app.{Application, SimpleApplication}
import com.jme3.bullet.{BulletAppState, PhysicsSpace}
import com.jme3.bullet.control.BetterCharacterControl
import com.jme3.collision.CollisionResults
import com.jme3.input.ChaseCamera
import com.jme3.math._
import com.jme3.scene.{Geometry, Node, Spatial}
import demoGame.graphics.{ColorUtils, SetColorFromTime}
import demoGame.{CharacterInputControl, _}
import demoGame.gameplay.CreatureInfo.{AngryBox, AwakenCylinder, CreatureInfo, CreatureType}
import JmeImplicits3FHelper._
import com.jme3.material.Material
import com.jme3.renderer.queue.RenderQueue.ShadowMode
import jme3tools.optimize.GeometryBatchFactory

import javax.imageio.ImageIO
import scala.jdk.CollectionConverters._
import scala.util.Random

class GameLevelAppState(val levelName: String = "lvl1", val blockSize: Float = 4f, val blockHeight: Float = 5f)(implicit val app: SimpleApplication) extends BaseAppState {
  implicit val level: GameLevelAppState = this
  implicit var bulletAppState: BulletAppState = _
  def physicSpace: PhysicsSpace = bulletAppState.getPhysicsSpace
  //  implicit val  app2:SimpleApplication = app
  var nav: Navigation = _
  var playerCharacter: Node = _
  var levelNode: Node = _
  var levelGeomNode: Node = _

  override def initialize(application: Application): Unit = {

    bulletAppState = new BulletAppState()
//        bulletAppState.setDebugEnabled(true)
    app.getStateManager.attach(bulletAppState)
    levelNode = new Node("level")
    levelGeomNode = new Node("levelGeom")
    levelGeomNode.setShadowMode(ShadowMode.CastAndReceive)
    levelNode.attachChild(levelGeomNode)
    app.getRootNode.attachChild(levelNode)
    //    bulletAppState.setDebugEnabled(true)

    nav = new Navigation(false)

    val levelSolidObjects = initMap()
    //nav mesh
    nav.setObjects(levelSolidObjects)

    playerCharacter = spawnPlayerCharacter()
    val chaseCam = new ChaseCamera(app.getCamera, playerCharacter, app.getInputManager)
    chaseCam.setInvertVerticalAxis(true)
    chaseCam.setDragToRotate(true)
    chaseCam.setMaxDistance(150)
    chaseCam.setDefaultDistance(100)
    chaseCam.setLookAtOffset(new Vector3f(0, 3, 0))


    //    val p = ParticleUtils.makeFireball()
    //    p.emitAllParticles()
    //    playerCharacter.attachChild(p)

    for (i <- 0 until 5) {
      spawnRandomEnemy()

    }

    for (i <- 0 until 10)
      spawnRandomGold()

  }

  def spawnEnemy(pos: Vector3f, creatureType: CreatureType) = {
    val (sp, cc, nc) = CreatureOps.makeCreature(pos, CreatureInfo.infoFromType(creatureType))

    val botAi = new BotAiControl(nc, sp.getControl(classOf[CreatureControl]), pos)
    sp.addControl(botAi)
    botAi
  }

  def spawnRandomEnemy() = {
    spawnEnemy(findSpawnPosition(), AngryBox(math.random().toFloat))
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

  def spawnPlayerCharacter(): Node = {
    val (sp, cc, nc) = CreatureOps.makeCreature(new Vector3f(0f, 0f, 0f), new CreatureInfo("Player", 100, 100, 10, 20, AngryBox(.5f), 10))
    nc.setEnabled(false)
    sp.getControl(classOf[CreatureControl]).setSpeed(50f)
    sp.addControl(new CharacterInputControl(cc, sp.getControl(classOf[CreatureControl])))
    sp
  }


  def spawnRandomGold() = {
    spawnGold(findSpawnPosition(), (new Random().nextInt(5) + 1) * 100)
  }

  lazy val goldMaterial: Material = {
    val goldColor = new ColorRGBA(1f, .8f, .0f, 1f)
    val mat = MakerUtils.makeShaded(goldColor)
    mat.setColor("GlowColor", goldColor)
    mat.setColor("Specular", goldColor)
    mat.setFloat("Shininess", 64f)
    mat.setColor("Ambient", goldColor.mult(0.4f).add(ColorRGBA.White.mult(0.1f)))

    mat
  }

  def spawnGold(at: Vector3f, amount: Int) = {
    val size = math.pow(amount, 1 / 3f).toFloat * .1f
    val gold = MakerUtils.makeBox(at, new Vector3f(size, size, size), "gold", goldMaterial, Some(levelNode))
    new GoldPileControl(gold, amount)
  }


  def enemiesResourceName: String = s"maps/${levelName}/enemies.png"
  def wallsResourceName: String = s"maps/${levelName}/walls.png"
  def floorResourceName: String = s"maps/${levelName}/floor.png"
  def manaResourceName: String = s"maps/${levelName}/mana.png"
  def goldResourceName: String = s"maps/${levelName}/gold.png"

  def loadImage(name: String): IndexedSeq[IndexedSeq[Int]] = {
    val mapImg = ImageIO.read(getClass.getClassLoader.getResource(name))
    val (msx, msy) = (mapImg.getWidth, mapImg.getHeight)
    val map: IndexedSeq[IndexedSeq[Int]] =
      for (i <- 0 until msx) yield for (j <- 0 until msy) yield mapImg.getRGB(i, j)
    map
  }

  def initWalls(): Seq[Geometry] = {
    var solid: Seq[Geometry] = Seq()

    val wallMap = loadImage(wallsResourceName)
    val floorMap = loadImage(floorResourceName)
    val goldMap = loadImage(goldResourceName)
    val manaMap = loadImage(manaResourceName)
    val enemiesMap = loadImage(enemiesResourceName)
    val (msx, msy) = (wallMap.size, wallMap.head.size)

    val mapSize = blockSize * msx

    //todo perPixel
    val floor = MakerUtils.makeBox(new Vector3f(0f, -0.1f, 0f), new Vector3f(mapSize / 2, 0.1f, mapSize / 2), "floor",
      MakerUtils.makeShadedCached(ColorRGBA.Gray), Some(levelGeomNode))
    MakerUtils.makeRigid(floor, 0)
    solid = solid :+ floor

    val minAngle = new Vector3f(-mapSize / 2f, blockSize / 2f, -mapSize / 2f) + new Vector3f(blockSize / 2f, 0f, blockSize / 2f)

    for (i <- 0 until msx; j <- 0 until msy) {
      val pos = minAngle + new Vector3f(blockSize, 0, blockSize) * new Vector3f(i.toFloat, 0f, j.toFloat)
      if ((0xFF000000 & wallMap(i)(j)) != 0) {
        val c = ColorUtils.colorRGBAFromInt(wallMap(i)(j))
        //todo fix bottom of boxes
        val b = MakerUtils.makeBox(pos, new Vector3f(blockSize / 2f, blockSize * blockHeight * c.getAlpha, blockSize / 2f), "wall", MakerUtils.makeShadedCached(c), Some(levelGeomNode))
        MakerUtils.makeRigid(b, 0f)
        solid = solid :+ b
      }
      if ((0xFF000000 & manaMap(i)(j)) != 0) {
        val mat = MakerUtils.makeUnshaded(ColorRGBA.Blue)
        val b = MakerUtils.makeSphere(pos, blockSize * 1.5f, "manaRegen", mat, Some(levelNode))
        MakerUtils.makeUtility(b)
        b.addControl(new SetColorFromTime(mat, timeFunc = x => FastMath.sin(x * 3), color0 = ColorRGBA.Blue.setAlpha(.3f), color1 = ColorRGBA.Blue.setAlpha(.5f)))
      }
      if ((0xFF000000 & goldMap(i)(j)) != 0) {
        spawnGold(pos, 300)
      }

      if ((0xFF000000 & enemiesMap(i)(j)) != 0) {
        spawnEnemy(pos, AngryBox(1f))
      }

    }

    //todo what with solid
    GeometryBatchFactory.optimize(levelGeomNode)
    solid

  }

  def initMap(): Seq[Geometry] = {
    val solid = initWalls()

    solid

  }


  override def update(tpf: Float): Unit = {

  }
  override def cleanup(app: Application): Unit = {}
  override def onEnable(): Unit = {}
  override def onDisable(): Unit = {}
}
