package encoder

import spinal.core._
import spinal.lib._


// Routes the input signal through a series of flip-flops
// Contains additional logic to flush the series of flip-flops if the output is not ready
// This is useful for enabling adjustments to the reference position downstream
case class FFSync(len: Int = 3) extends Component {
  val io = new Bundle {
    val input = in Bool ()
    val pinOutput = master(Stream(Bool())) //Output stream
  }

  val sync = Vec(Reg(Bool()) init (False), len)
  val valid = Vec(Reg(Bool()) init (False), len)
  val flush = !io.pinOutput.ready // Flush signal is active when the output is not ready
  sync(0) := io.input
  for (i <- 1 until len) {
    sync(i) := sync(i - 1)
  }
  valid(0) := True
  when(flush) {
    for (i <- 1 until len) {
      valid(i) := False
    }
  } otherwise {
    for (i <- 1 until len) {
      valid(i) := valid(i - 1)
    }
  }
  io.pinOutput.payload := sync(len - 1)
  io.pinOutput.valid := valid(len - 1) 
}
