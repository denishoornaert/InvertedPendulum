package button

import spinal.core._
import spinal.lib._
import spinal.lib.bus.amba4.axi._

import kr260._
import ultrascaleplus.configport._
import scripts._

// Integrates debouncer with the KV260 board. The debouncer gets its input from
// PMOD0 and triggers and interrupt once a bounce is detected
case class ButtonController() extends KR260(config = new KR260Config(withLPD_HPM0 = true, withIO_PMOD0 = true, withPL_PS_IRQ0 = 1)) {

  val configport = ConfigPort(io.lpd.hpm0, io.lpd.hpm0.getPartialName())
  val counter_threshold = Reg(UInt(64 bits)) init (0x3fd000)
  val debouncer = Debouncer()
  io.pmod0.asInput()
  configport.readAndWrite(counter_threshold, io.lpd.hpm0.apertures(0).base)
  debouncer.io.input := io.pmod0(0)
  debouncer.io.counter_input := counter_threshold
  io.irq.toPS0(0) := debouncer.io.output.rise()
}

object ButtonControllerVerilog extends App {
  Config.spinal.generateVerilog(ButtonController())
}
