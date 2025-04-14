package button

import spinal.core._
import spinal.lib._
import spinal.core.sim._


// Counter to delay the propgation of a pressed button. To clean the signal
// the counter counts to zero thus mitigating bounces a during push and release
case class DebounceCounter(width: Int) extends Component {
  val io = new Bundle {
    val input = in Bool ()
    val counter_input = in UInt (width bits)
    val output = out Bool ()
  }
  val ztimer = Reg(Bool()) init (True)
  val timer = Reg(UInt(width bits)) init (0)
  val output_o = Reg(Bool()) init (False)
  val last = Reg(Bool()) init (False)
  val different = Reg(Bool()) init (False)

  val c_ff = Reg(UInt(width bits))
  val c_internal = Reg(UInt(width bits))

  io.output := output_o
  last := io.input

  different := (different && !ztimer) || (output_o =/= io.input)

  c_internal := c_ff
  c_ff := io.counter_input

  when(ztimer) {
    output_o := last
  }

  when(ztimer && different) {
    timer := c_internal
    ztimer := False
  } elsewhen (!ztimer) {
    timer := timer - 1
    ztimer := timer(width - 1 downto 0) === 0
  } otherwise {
    timer := 0
    ztimer := True
  }
}

object DebounceCounterVerilog extends App {
  Config.spinal.generateVerilog(DebounceCounter(64))
}
