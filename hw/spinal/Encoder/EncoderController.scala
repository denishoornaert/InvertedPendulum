package encoder

import spinal.core._
import spinal.lib._
import spinal.lib.bus.amba4.axi._

import kr260._
import ultrascaleplus.configport._
import scripts._

// =====================================================
// WARNING: This SpinalHDL component is NOT TESTED!
// =====================================================

// This is a simple example of a rotary encoder controller
// It additionally fires an interrupt when a position updated has been conducted
case class EncoderController(width: Int = 32)
    extends KR260(config = new KR260Config(withLPD_HPM0 = true, withIO_PMOD0 = true, withPL_PS_IRQ0 = 1)) {
  val axifactory = new Axi4SlaveFactory(io.lpd.hpm0)
  val encoder = EncoderCounter(width)
  val position = Reg(SInt(width bits)) init (0)
  val reg_position_base = Reg(SInt(width bits)) init (0)
  io.pmod0.asInput()
  encoder.io.pinA := io.pmod0(0)
  encoder.io.pinB := io.pmod0(1)
  encoder.io.pinIndex := io.pmod0(2)
  io.irq.toPS0(0) := encoder.io.invalid.fall()
  axifactory.read(encoder.io.position, io.lpd.hpm0.apertures(0).base)
  axifactory.driveFlow(encoder.io.position_update, io.lpd.hpm0.apertures(0).base, width)
}

object EncoderControllerVerilog extends App() {
  Config.spinal.generateVerilog(EncoderController())
}
