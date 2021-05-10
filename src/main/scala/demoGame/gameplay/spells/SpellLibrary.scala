package demoGame.gameplay.spells

import demoGame.gameplay.CreatureControl
import CreatureSpell.{CreatureSpell, SpellLevel}

object SpellLibrary {

  type SpellMaker = (CreatureControl, SpellLevel) => CreatureSpell

  val makeGeometricExplosion: SpellMaker = (c, lvl) =>
    new CreatureSpell(
      c,
      "Geometric explosion",
      "/assets/Interface/spellIcons/geometricExplosion.png",
      l => 10 + l,
      GeometricExplosion.fromLeverAndCaster,
      lvl
    )

  val makeGeometricBall: SpellMaker = (c, lvl) => new CreatureSpell(
    c,
    "Geometric ball",
    "/assets/Interface/spellIcons/geometricBall.png",
    l => 10 + l,
    GeometricBall.fromLeverAndCaster,
    lvl
  )


}
