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
import JmeImplicitsFHelper._
import com.jme3.material.Material
import com.jme3.renderer.queue.RenderQueue.ShadowMode
import demoGame.gameplay.shop.ShopControl
import demoGame.gameplay.shop.ShopLot.{BuySpell, UpgradeHp, UpgradeMana, UpgradeSpeed, UpgradeSpellLevel}
import demoGame.gameplay.spells.SpellLibrary
import demoGame.graphics.particles.ParticleUtils
import jme3tools.optimize.GeometryBatchFactory
import org.slf4j.LoggerFactory

import javax.imageio.ImageIO
import scala.jdk.CollectionConverters._
import scala.util.Random

class GameLevelAppState(val levelName: String = "lvl1", val blockSize: Float = 4f, val blockHeight: Float = 5f)(implicit val app: SimpleApplication) extends BaseAppState {
  val logger = LoggerFactory.getLogger(classOf[GameLevelAppState].getName)

  implicit val level: GameLevelAppState = this
  implicit var bulletAppState: BulletAppState = _
  def physicSpace: PhysicsSpace = bulletAppState.getPhysicsSpace
  //  implicit val  app2:SimpleApplication = app
  var nav: Navigation = _
  var playerCharacter: Node = _
  var levelNode: Node = _
  var levelGeomNode: Node = _
  var chaseCamera:ChaseCamera = _

  var shops: Seq[ShopControl] = Seq()

  override def initialize(application: Application): Unit = {
    logger.info(s"Game level init...")
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

    chaseCamera = new ChaseCamera(app.getCamera, playerCharacter, app.getInputManager)
    chaseCamera.setInvertVerticalAxis(true)
    chaseCamera.setDragToRotate(false)
    chaseCamera.setMaxDistance(150)
    chaseCamera.setDefaultDistance(100)
    chaseCamera.setLookAtOffset(new Vector3f(0, 3, 0))


    //    val p = ParticleUtils.makeGeometricBall()
    //    p.emitAllParticles()
    //    playerCharacter.attachChild(p)

    for (i <- 0 until 5) {
      spawnRandomEnemy()

    }

    for (i <- 0 until 10)
      spawnRandomGold()

  }

  def spawnEnemy(pos: Vector3f, creatureType: CreatureType) = {
    val (sp, cc, nc) = CreatureOps.makeCreatureFromType(pos, creatureType)

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
    val (sp, cc, nc) = CreatureOps.makeCreature(new Vector3f(0f, 0f, 0f),
      new CreatureInfo("Player", 1000, 1000, AngryBox(.5f), 200000, 10))
    nc.setEnabled(false)
    CreatureInfo.addAllSpells(sp.getControl(classOf[CreatureControl]))
    sp.getControl(classOf[CreatureControl]).setSpeed(50f)
    sp.addControl(new CharacterInputControl(cc, sp.getControl(classOf[CreatureControl])))
    sp
  }


  def spawnRandomGold() = {
    spawnGold(findSpawnPosition(), (new Random().nextInt(5) + 1) * 100)
  }


  def spawnGold(at: Vector3f, amount: Int) = {
    new GoldPileControl(at, amount)
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
    //todo add to map
    new ShopControl(new Vector3f(30, 9.5f, 30), Seq(
      BuySpell(100, SpellLibrary.GeometricBallMaker, "/assets/Interface/spellIcons/geometricBall.png.png", "Fires big sphere filled with deadly geometry."),
      BuySpell(100, SpellLibrary.GeometricExplosionMaker, "/assets/Interface/spellIcons/geometricExplosion.png", "You inner geometry explodes dealing damage to all nearby enemies."),
      UpgradeSpellLevel(lvl => lvl * lvl * 100, SpellLibrary.GeometricBallMaker.name, "/assets/Interface/spellIcons/geometricBall.png", "Upgrades geometric ball to contain ever deadliest geometries inside."),
      UpgradeSpellLevel(lvl => lvl * lvl * 100, SpellLibrary.GeometricExplosionMaker.name, "/assets/Interface/spellIcons/geometricExplosion.png", "Upgrades geometric explosion increasing damage and radius."),
      UpgradeHp,
      UpgradeMana,
      UpgradeSpeed,
    ))

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
        val c = ColorUtils.colorRGBAFromInt(manaMap(i)(j))
        new ManaDomeControl(pos, c.getAlpha)
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
