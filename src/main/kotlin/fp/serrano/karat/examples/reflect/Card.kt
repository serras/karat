package fp.serrano.karat.examples.reflect

import fp.serrano.karat.*
import fp.serrano.karat.ast.*
import fp.serrano.karat.ui.visualize

interface Name

@abstract sealed interface Power {
  object FIRE: Power
  object WATER: Power
  object AIR: Power
  object GROUND: Power
}

data class Attack(
  val name: Name,
  val cost: Set<Power>
)

@abstract sealed interface Card
data class MonsterCard(
  val name: Name,
  val attacks: Set<Attack>,
  val mutatesFrom: MonsterCard?
): Card {
  companion object {
    fun InstanceFact<MonsterCard>.noSelfMutation(): KFormula =
      not(self `in` self / MonsterCard::mutatesFrom)
  }
}
data class PowerCard(
  val type: Power
)

fun main() {
  execute {
    reflect(reflectAll = true,
      Power::class, Power.FIRE::class, Power.WATER::class, Power.AIR::class, Power.GROUND::class,
      Name::class, Attack::class,
      Card::class, MonsterCard::class, PowerCard::class
    )

    run(10) {
      Constants.TRUE
    }.visualize()
  }
}