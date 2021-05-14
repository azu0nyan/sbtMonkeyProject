package demoGame.gameplay.shop

import com.jme3.bullet.collision.shapes.BoxCollisionShape
import com.jme3.bullet.control.GhostControl
import com.jme3.effect.ParticleEmitter
import com.jme3.material.RenderState.FaceCullMode
import com.jme3.math.{ColorRGBA, Vector3f}
import com.jme3.renderer.queue.RenderQueue.ShadowMode
import com.jme3.renderer.{RenderManager, ViewPort}
import com.jme3.scene.Node
import com.jme3.scene.Spatial.CullHint
import com.jme3.scene.control.AbstractControl
import demoGame.JmeImplicitsFHelper.overlappingCreatures
import demoGame.{JmeImplicitsFHelper, MakerUtils}
import demoGame.gameplay.{CreatureControl, GameLevelAppState}
import demoGame.gameplay.shop.ShopLot.ShopLot
import demoGame.graphics.SetColorFromTime
import demoGame.graphics.particles.ParticleUtils
import org.slf4j.LoggerFactory

class ShopControl(pos:Vector3f, lots:Seq[ShopLot])(implicit level:GameLevelAppState) extends AbstractControl{
  val log = LoggerFactory.getLogger(this.getClass)
  level.shops = level.shops :+ this

  val node = new Node("Shop")
  node.addControl(this)
  level.levelNode.attachChild(node)
  node.setLocalTranslation(pos)

  val boxSize = new Vector3f(10, 10, 10)
  val shape = MakerUtils.makeBox(Vector3f.ZERO, boxSize, "SHOP", MakerUtils.makeWireframe(ColorRGBA.Yellow)(level.app), Some(node))(level.app)
  shape.addControl(new SetColorFromTime(shape.getMaterial,"Color", x => math.abs(math.sin(x))toFloat,
    new ColorRGBA(0f, 1f, 0f, 1f), new ColorRGBA(1f, 1f, 0f, .5f))(level.app))
  MakerUtils.makeTransparent(shape)
  shape.setShadowMode(ShadowMode.Off)
  shape.getMaterial.getAdditionalRenderState.setFaceCullMode(FaceCullMode.Off)

  val ghost = new GhostControl(new BoxCollisionShape(boxSize))
  node.addControl(ghost)
  level.physicSpace.add(ghost)

  val onBuyParticles:ParticleEmitter = ParticleUtils.makeShopOnBuy(boxSize)(level.app)
  node.attachChild(onBuyParticles)

  level.levelNode.updateModelBound()
  node.updateModelBound()

  def availableLots(cr:CreatureControl):Seq[ShopLot] = lots.filter(_.satisfyRequirements(cr))

  def buy(cr:CreatureControl, lot:ShopLot) :Unit = {
    if(buyers.contains(cr) &&  lots.contains(lot) && lot.satisfyRequirements(cr) ){
      val price = lot.price(cr)
      if(price <= cr.gold) {
        lot.buy(cr)
        cr.spendGold(price)
        log.info(s"Creature ${cr.info.name} bought ${lot.itemDescription} for $price.")
      } else {
        log.info(s"Creature ${cr.info.name} cant buy ${lot.itemDescription} for $price, no money.")
      }
    } else{
      log.warn(s"Creature ${cr.info.name} attempted to buy ${lot.itemDescription}.")
    }
  }

  def buyById(cr:CreatureControl, lotId:Int):Unit =
    if(lots.indices.contains(lotId)) buy(cr, lots(lotId))

  def  buyers:Seq[CreatureControl] = overlappingCreatures(ghost)
  override def controlUpdate(tpf: Float): Unit = {
//    onBuyParticles.emitParticles(10)
  }

  override def controlRender(rm: RenderManager, vp: ViewPort): Unit = {

  }
}
