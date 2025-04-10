package button

import spinal.core._
import spinal.lib._

//===----------------------------------------------------------------------===//
///
/// Connector class to split up an 8 bit input into 8 individual bool outputs
///
//===----------------------------------------------------------------------===//
case class Connector() extends Component {
  val io = new Bundle {
    val input = in Bits (8 bits)
    val output = out Vec (Bool(), 8)

  }

  for (i <- 0 until 8) {
    io.output(i) := io.input(i)
  }
}

object ConnectorVerilog extends App {
  Config.spinal.generateVerilog(Connector())
}
