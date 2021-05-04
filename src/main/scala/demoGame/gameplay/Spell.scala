package demoGame.gameplay

object Spell {


  sealed trait Spell{
    def manaCost:Int
    def castTime:Float
  }



}
