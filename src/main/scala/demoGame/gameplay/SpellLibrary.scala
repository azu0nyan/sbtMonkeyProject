package demoGame.gameplay

import com.jme3.effect.ParticleEmitter
import com.jme3.scene.Node
import demoGame.gameplay.CreatureState.{CreatureAction, Normal}

object SpellLibrary {





  sealed trait Spell{
    def manaCost:Int
    def castTime:Float
  }



}
