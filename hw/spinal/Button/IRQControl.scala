package debouncer

import spinal.core._
import spinal.lib._
import spinal.core.sim._
import spinal.lib.bus.amba4.axi._

// =====================================================
// WARNING: This SpinalHDL component is NOT TESTED!
// =====================================================

// Experimental implementation of IRQ control logic on the FPGA
// The Software can renable IRQ by writing to the IRQ status register
case class IRQControl() extends Component {
  val io = new Bundle {
    val sig = in Bool ()
    val irq = out Bool ()
  }
  def add_irq_status(mm: Axi4SlaveFactory, address: BigInt, offset: Int = 0) = {
    mm.readAndWrite(irq_status, address, offset)
  }

  val irq_status = Reg(Bits(16 bits)) init (1)
  val irq_signal = Reg(Bool()) init (False)

  when(irq_status =/= 0 && io.sig.rise()) {
    irq_status := 0
    irq_signal := True
  }
  io.irq := irq_signal && irq_status === 0
}
