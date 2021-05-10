package demoGame.gameplay.spells

import demoGame.gameplay.CreatureControl
import CreatureSpell.{CreatureSpell, SpellLevel}

object SpellLibrary {

//  case class SpellMaker(CreatureControl, SpellLevel) => CreatureSpell
  sealed trait SpellMaker {
    def name:String
    def apply(c:CreatureControl, lvl:Int):CreatureSpell
  }

  object GeometricExplosionMaker extends SpellMaker {
    override def name: String = "Geometric explosion"
    override def apply(c:CreatureControl, lvl:Int): CreatureSpell = new CreatureSpell(
      c,
      name,
      "/assets/Interface/spellIcons/geometricExplosion.png",
      l => 10 + l * 2,
      GeometricExplosion.fromLeverAndCaster,
      lvl
    )
  }

  object GeometricBallMaker extends SpellMaker {
    override def name: String = "Geometric ball"
    override def apply(c:CreatureControl, lvl:Int): CreatureSpell = new CreatureSpell(
      c,
      name,
      "/assets/Interface/spellIcons/geometricBall.png",
      l => 10 + l,
      GeometricBall.fromLeverAndCaster,
      lvl
    )
  }

}
