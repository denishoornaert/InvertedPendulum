package encoder

import spinal.core._
import spinal.lib._

case class FFSync(len: Int = 3) extends Component {
  val io = new Bundle {
    val input = in Bool ()
    val pinOutput = master(Stream(Bool()))
  }

  val sync = Vec(Reg(Bool()) init (False), len)
  val valid = Vec(Reg(Bool()) init (False), len)
  val flush = !io.pinOutput.ready
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
