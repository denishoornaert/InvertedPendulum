package invertedpendulum
import spinal.core._
import spinal.lib._
import spinal.lib.com.uart._

case class UartController(freq: HertzNumber) extends Component{
  val io = new Bundle{
    val data = slave Flow(Bits(8 bits))
    val rdata = master Stream(Bits(8 bits))
    val rx = in Bool()
    val tx = out Bool()
    val ready = out Bool()
  }
  val fifo = new StreamFifo(Bits(8 bits), 256)
  val readfifo = new StreamFifo(Bits(8 bits), 256)
  val ready = RegInit(True)
  val valid = RegInit(False)
  val data = Reg(Bits(8 bits))
  val uart = new UartCtrl()
  uart.io.config.setClockDivider(115200 Hz, freq)

  // 8 bits
  uart.io.config.frame.dataLength := 7
  uart.io.config.frame.stop := UartStopType.ONE
  uart.io.config.frame.parity := UartParityType.NONE
  uart.io.writeBreak := False
  fifo.io.push.valid := io.data.valid
  fifo.io.push.payload := io.data.payload

  ready := ready
  data := data
  valid := valid
  fifo.io.pop.ready := ready
  when(ready){
    data := fifo.io.pop.payload
    valid := fifo.io.pop.valid
    ready := ~fifo.io.pop.valid
  }elsewhen(uart.io.write.ready){
    ready := True
    valid := False
  }
  uart.io.write.payload := data
  uart.io.write.valid := valid

  io.tx := uart.io.uart.txd
  io.ready := fifo.io.push.ready

  uart.io.read >> readfifo.io.push
  readfifo.io.pop >> io.rdata 
  uart.io.uart.rxd := io.rx
}

object UartControllerVerilog extends App{
  Config.spinal.generateVerilog(UartController(200 MHz))
}
