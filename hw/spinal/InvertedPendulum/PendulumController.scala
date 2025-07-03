package invertedpendulum

import spinal.core._
import spinal.lib._
import spinal.lib.bus.amba4.axi._
import spinal.lib.com.uart._

import encoder.EncoderCounter
import button.Debouncer

import kr260._
import ultrascaleplus.configport._
import ultrascaleplus.scripts._

import scala.annotation.switch
import spinal.lib.bus.misc.SizeMapping
import spinal.core

// Integration of debouncer, encoder and PWM controller
case class PendulumController(width: Int = 32, debounceCycles: Long = 0x500000)
    extends KR260(frequency = 200 MHz, config = new KR260Config(withLPD_HPM0 = true, withIO_PMOD0 = true, withIO_PMOD1 = true, withPL_PS_IRQ0 = 0)) {

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
  val character = Reg(Bits(8 bits))
  val uart = UartController(200 MHz)

  io.pmod1.asOutput()
  io.pmod0.asInput()
  for(i <- 1 until 8) {
    io.pmod1(i) := False
  }
  encoder(0).io.pinA := io.pmod0(0)
  encoder(0).io.pinB := io.pmod0(1)
  encoder(0).io.pinIndex := False
  encoder(1).io.pinA := io.pmod0(3)
  encoder(1).io.pinB := io.pmod0(4)
  encoder(1).io.pinIndex := False
  debouncer(0).io.input := !io.pmod0(6)
  debouncer(1).io.input := !io.pmod0(7)

  debouncer(0).io.counter_input := debounceCycles
  debouncer(1).io.counter_input := debounceCycles

  io.pmod1(0) := uart.io.tx
  uart.io.rx := io.pmod0(2)

  for (i <- 0 until 2) {
    axifactory.read(encoder(i).io.position, base, i * encoder_mmio)
    // Spinal Flow to correctly reset the logic upon update
    axifactory.driveFlow(encoder(i).io.position_update, base, i * encoder_mmio + width) // position update via AXI

  }
   for(i <- 0 until 2){
    axifactory.read(debouncer(i).io.output.asBits(16 bits), base + offset(2 * encoder_mmio), i * 16)
  }
  axifactory.driveFlow(uart.io.data, base + offset(2 * encoder_mmio), 32)
  axifactory.read(uart.io.ready.asBits(8 bits), base + offset(2 * encoder_mmio), 40)
  axifactory.readStreamNonBlocking(uart.io.rdata, base + offset(2 * encoder_mmio), 48, 56)
  this.generate()
}

object PendulumControllerVerilog extends App() {
  Config.spinal.generateVerilog(PendulumController(32))
}
