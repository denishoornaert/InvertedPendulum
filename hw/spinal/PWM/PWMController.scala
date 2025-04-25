package pwm

import spinal.core._
import spinal.lib._
import spinal.lib.bus.amba4.axi._

import kr260._
import ultrascaleplus.configport._
import scripts._

case class PWMController() extends KR260(config = new KR260Config(withLPD_HPM0 = true, withIO_PMOD0 = true)) {

  for (i <- 1 until 8) {
    io.pmod0(i) := False
  }
  val period = 1000

  val pwm = PWM(period = period)
  val configport = ConfigPort(io.lpd.hpm0, io.lpd.hpm0.getPartialName())
  val threshold = Reg(UInt(32 bits)) init (0)
  configport.readAndWrite(threshold, io.lpd.hpm0.apertures(0).base)
  val previous = Reg(UInt(log2Up(period) bits)) init (0)
  previous := threshold.resized
  pwm.io.threshold.valid := threshold =/= previous
  pwm.io.threshold.payload := threshold.resized
  io.pmod0(0) := pwm.io.signal
}

object PWMControllerVerilog extends App {
  Config.spinal.generateVerilog(PWMController())
}
