package demoGame.gameplay.spells

import demoGame.gameplay.CreatureControl
import CreatureSpell.{CreatureSpell, SpellLevel}

object SpellLibrary {

  type SpellMaker = (CreatureControl, SpellLevel) => CreatureSpell

  val makeGeometricExplosion: SpellMaker = (c, lvl) =>
    new CreatureSpell(
      c,
      l => 10 + l,
      GeometricExplosion.fromLeverAndCaster,
      lvl
    )

  val makeFireball: SpellMaker = (c, lvl) => new CreatureSpell(
    c,
    l => 10 + l,
    GeometricBall.fromLeverAndCaster,
    lvl
  )


}
