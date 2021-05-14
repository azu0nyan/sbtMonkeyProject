package demoGame.gameplay.shop

import demoGame.gameplay.CreatureControl
import demoGame.gameplay.spells.CreatureSpell.SpellLevel
import demoGame.gameplay.spells.SpellLibrary.SpellMaker

object ShopLot {
  trait ShopLot {
    def satisfyRequirements(cr: CreatureControl): Boolean
    def price(cr: CreatureControl): Int
    def buy(cr: CreatureControl): Unit
    def shopImage: String
    def itemDescription:String
  }

  case class BuySpell(
                       basePrice: Int,
                       spellMaker: SpellMaker,
                       shopImage: String,
                       itemDescription:String,
                     ) extends ShopLot {
    override def satisfyRequirements(cr: CreatureControl): Boolean = !cr.spells.exists(_.name == spellMaker.name)
    override def price(cr: CreatureControl): Int = basePrice
    override def buy(cr: CreatureControl): Unit = cr.spells = cr.spells :+ spellMaker(cr, 0)

  }

  case class UpgradeSpellLevel(
                        priceFromLevel:SpellLevel => Int,
                        spellName:String,
                        shopImage: String,
                        itemDescription:String
                        ) extends ShopLot {
    override def satisfyRequirements(cr: CreatureControl): Boolean = cr.spells.exists(_.name == spellName)
    override def price(cr: CreatureControl): Int = cr.spells.find(_.name == spellName).map(s => priceFromLevel(s.level + 1)).getOrElse(Int.MaxValue)
    override def buy(cr: CreatureControl): Unit = cr.spells.find(_.name == spellName).foreach(sp => sp.level = sp.level + 1)
  }

  object UpgradeHp extends ShopLot {
    override def satisfyRequirements(cr: CreatureControl): Boolean = true
    override def price(cr: CreatureControl): Int = cr.info.hp
    override def buy(cr: CreatureControl): Unit = cr.info.maxHp += 10
    override def itemDescription: String = "Increase max hit points by 10"
    override def shopImage: String = ""
  }

  object UpgradeMana extends ShopLot {
    override def satisfyRequirements(cr: CreatureControl): Boolean = true
    override def price(cr: CreatureControl): Int = cr.info.mana
    override def buy(cr: CreatureControl): Unit = cr.info.maxMana += 10
    override def itemDescription: String = "Increase maximum mana points by 10"
    override def shopImage: String = ""
  }

  object UpgradeSpeed extends ShopLot {
    override def satisfyRequirements(cr: CreatureControl): Boolean = true
    override def price(cr: CreatureControl): SpellLevel = cr.movement.maxSpeed * 100 toInt
    override def buy(cr: CreatureControl): Unit = cr.movement.setSpeed(cr.movement.maxSpeed * 1.1f)
    override def itemDescription: String = "Increase speed by 10%"
    override def shopImage: String = ""
  }


}


