package encoder

import encoder._

import spinal.core._
import spinal.lib._

case class EncoderCounter(width: Int = 64) extends Component {
  val io = new Bundle {
    val pinA = in Bool ()
    val pinB = in Bool ()
    val pinIndex = in Bool ()
    val position_update = slave(Flow(SInt(width bits)))
    val invalid = out Bool ()
    val index = out Bool ()
    val position = out SInt (width bits)
  }
  val encoder1 = Encoder(width)
  val position = Reg(SInt(width bits)) init (0)
  val index = Reg(Bool()) init (False)
  val last_position = Reg(SInt(width bits)) init (0)
  val reg_state = Reg(UInt(4 bits)) init (0)
  val update_in_progress = Reg(Bool()) init (False)

  encoder1.io.pinA := io.pinA
  encoder1.io.pinB := io.pinB
  encoder1.io.enable := !io.position_update.valid
  encoder1.io.pinIndex := io.pinIndex

  val rising = io.pinIndex.rise()

  when(io.position_update.valid) {
    position := io.position_update.payload
    last_position := io.position_update.payload
  } elsewhen (rising) {
    position := last_position
  } otherwise {
    position := position + encoder1.io.delta
  }

  when(io.position_update.valid) {
    index := False
  } otherwise {
    index := rising
  }

  io.position := position
  io.index := index
  io.invalid := io.position_update.valid
}

object CounterVerilog extends App {
  Config.spinal.generateVerilog(EncoderCounter())
}
