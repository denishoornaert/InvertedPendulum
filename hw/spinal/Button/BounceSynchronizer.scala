package button

import spinal.core._
import spinal.lib._

//===----------------------------------------------------------------------===//
///
/// Route input signal through arraylen flip flops
///
//===----------------------------------------------------------------------===//
case class BounceSynchronizer(arraylen: Int) extends Component {
  val io = new Bundle {
    val input = in Bool ()
    val output = out Bool ()
  }

  val ff = Vec(Reg(Bool()) init (False), arraylen)
  ff(0) := io.input
  for (i <- 1 until arraylen) {
    ff(i) := ff(i - 1)
  }
  io.output := ff(arraylen - 1)
}

object BounceSynchronizerVerilog extends App {
  Config.spinal.generateVerilog(BounceSynchronizer(2))
}
