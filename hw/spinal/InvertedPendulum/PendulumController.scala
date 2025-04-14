package invertedpendulum

import spinal.core._
import spinal.lib._
import spinal.lib.bus.amba4.axi._

import encoder._
import button._
import pwm.PWM

import kv260._
import kv260.interface.axi._
import ultrascaleplus.configport._
import ultrascaleplus.scripts._

import scala.annotation.switch
import spinal.lib.bus.misc.SizeMapping
import spinal.core

// Integration of debouncer, encoder and PWM controller
case class PendulumController(width: Int = 32, debounceCycles: Long = 0x2d0f00)
    extends KV260(withLPD_HPM0 = true, withIO_PMOD0 = true, withIO_PMOD1 = true, withTo_PS_IRQ = true) {

  def offset(base: BigInt): BigInt = {
    return base >> 3
  }
  val encoder_mmio = 2 * width
  val period = 1999980 // period of the PWM
  val base = io.lpd.hpm0.apertures(0).base

  val axifactory = new Axi4SlaveFactory(io.lpd.hpm0)
  val encoder = Array.fill(2)(EncoderCounter(width))
  val debouncer = Array.fill(2)(Debouncer())
  val threshold = Reg(UInt(32 bits)) init (0)
  val pwm = PWM(period = period)

  io.pmod1.asOutput()
  io.pmod1.clearAll()
  encoder(0).io.pinA := io.pmod0(0)
  encoder(0).io.pinB := io.pmod0(1)
  encoder(0).io.pinIndex := False
  encoder(1).io.pinA := io.pmod0(3)
  encoder(1).io.pinB := io.pmod0(4)
  encoder(1).io.pinIndex := False
  debouncer(0).io.input := io.pmod0(6)
  debouncer(1).io.input := io.pmod0(7)
  debouncer(0).io.counter_input := debounceCycles
  debouncer(1).io.counter_input := debounceCycles

  val previous = Reg(UInt(log2Up(period) bits)) init (0)
  previous := threshold.resized

  val encoder_irq = (encoder(0).io.invalid.fall() || encoder(1).io.invalid.fall())
  pwm.io.threshold.valid := threshold =/= previous
  pwm.io.threshold.payload := threshold.resized

  for (i <- 0 until 2) {
    io.pl_to_ps.irq(i) := debouncer(i).io.output.fall()
  }
  io.pl_to_ps.irq(2) := encoder_irq

  for (i <- 0 until 2) {
    axifactory.read(encoder(i).io.position, base + offset(i * encoder_mmio))
    // Spinal Flow to correctly reset the logic upon update
    axifactory.driveFlow(encoder(i).io.position_update, base + offset(i * encoder_mmio), width) // position update via AXI

  }
  axifactory.write(threshold, base + offset(2 * encoder_mmio)) // threshold via AXI
}

object PendulumControllerVerilog extends App() {
  Config.spinal.generateVerilog(PendulumController(64))
}
