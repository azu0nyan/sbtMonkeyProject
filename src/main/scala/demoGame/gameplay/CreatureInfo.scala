package demoGame.gameplay

object CreatureInfo {
  class CreatureInfo(
                      var name: String,
                      val maxHp: Int,
                      var maxMana: Int,
                      var atk: Int,
                      var speed: Float,
                      var creatureType: CreatureType,
                      var gold: Int
                    ) {

    var mana:Int = maxMana
    var hp:Int = maxHp

  }

  sealed trait CreatureType
  case class AngryBox(angry: Float) extends CreatureType
  case class AwakenCylinder() extends CreatureType

  def infoFromType(creatureType: CreatureType): CreatureInfo = creatureType match {
    case AngryBox(angry) => new CreatureInfo("AngryBox", 100, 10, 10, 10, creatureType, 0)
    case AwakenCylinder() => new CreatureInfo("AwakenCylinder", 50, 100, 10, 20, creatureType, 0)
  }
}


