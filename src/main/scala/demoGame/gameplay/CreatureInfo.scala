package demoGame.gameplay

import demoGame.gameplay.spells.CreatureSpell.CreatureSpell
import demoGame.gameplay.spells.{GeometricExplosion, SpellLibrary}

object CreatureInfo {

  var maxId = -1

  class CreatureInfo(
                      var name: String,
                      val maxHp: Int,
                      var maxMana: Int,
                      var atk: Int,
                      var speed: Float,
                      var creatureType: CreatureType,
                      var gold: Int,
                    ) {

    var mana: Int = maxMana
    var hp: Int = maxHp

  }

  sealed trait CreatureType
  case class AngryBox(angry: Float) extends CreatureType
  case class AwakenCylinder() extends CreatureType

  def getId():Int = {
    maxId += 1
    maxId
  }
  def infoFromType(creatureType: CreatureType): CreatureInfo = creatureType match {
    case AngryBox(angry) =>
      new CreatureInfo(s"Angry Box ${getId()}", (50 + angry * 50).toInt, 50, 10, 10, creatureType, 0)
    case AwakenCylinder() =>
      new CreatureInfo(s"Awaken Cylinder ${getId()}", 50, 100, 10, 20, creatureType, 0)
  }


  def addSpells(creatureControl: CreatureControl): Unit = {
    creatureControl.info.creatureType match {
      case AngryBox(angry) =>
        creatureControl.spells = Seq(
          SpellLibrary.makeGeometricBall(creatureControl, 0)
        )
      case AwakenCylinder() =>
        creatureControl.spells = Seq(
          SpellLibrary.makeGeometricExplosion(creatureControl, 0)
        )
    }
  }

  def addAllSpells(creatureControl: CreatureControl): Unit = {
    creatureControl.spells = Seq(
      SpellLibrary.makeGeometricBall(creatureControl, 10),
      SpellLibrary.makeGeometricExplosion(creatureControl, 10)
    )
  }
}


