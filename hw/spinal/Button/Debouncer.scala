package button

import spinal.core._
import spinal.core.sim._
import spinal.lib._
import spinal.core

//===----------------------------------------------------------------------===//
///
/// Routes the input signal through two flip flops and
/// forwards it to the debounce counter
///
//===----------------------------------------------------------------------===//
case class Debouncer() extends Component {
  val io = new Bundle {
    val input = in Bool ()
    val output = out Bool ()
    val counter_input = in UInt (64 bits)
  }

  val synced = BounceSynchronizer(3)
  val debounceCounter = DebounceCounter(64)

  synced.io.input := io.input

  debounceCounter.io.counter_input := io.counter_input
  debounceCounter.io.input := synced.io.output
  io.output := debounceCounter.io.output
}

object DebouncerVerilog extends App {
  Config.spinal.generateVerilog(Debouncer())
}
