package pwm

import spinal.core._
import spinal.lib._

case class PWM(period: Int) extends Component {
  val io = new Bundle {
    val threshold = slave(Flow(UInt(log2Up(period) bits)))
    val signal = out(Bool())
  }

  val threshold_current = Reg(UInt(log2Up(period) bits)) init (0)
  val threshold_latest = Reg(UInt(log2Up(period) bits)) init (0)
  when(io.threshold.valid) {
    threshold_latest := io.threshold.payload
  }

  // Count the fractions up to 100%
  val counter = CounterFreeRun(period)
  when(counter === period - 1) {
    threshold_current := threshold_latest
    counter.clear()
  }

  // Drive out signal
  io.signal := (counter < threshold_current)
}

object MyTopLevelVerilog extends App {
  Config.spinal.generateVerilog(PWM(1000))
}

object MyTopLevelVhdl extends App {
  Config.spinal.generateVhdl(PWM(1000))
}
