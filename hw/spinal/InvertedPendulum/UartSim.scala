package invertedpendulum

import spinal.core.sim._
import spinal.core._


object UartSim{
  def main(args: Array[String]) : Unit = {
    Config.simcfg.withWave
      .compile(UartController(200 MHz))
      .doSimUntilVoid { dut =>
        dut.clockDomain.forkStimulus(period = 2)
        sleep(1000)
        dut.clockDomain.waitSampling()
        dut.io.data.valid #= true
        dut.io.data.payload #= 0x1e
        dut.clockDomain.waitSampling()
        dut.io.data.valid #= false
        sleep(80000)
        dut.io.data.valid #= true
        dut.io.data.payload #= 0xf0
        dut.clockDomain.waitSampling()
        dut.io.data.valid #= false
        sleep(80000)
        simSuccess()
      }
  }
}
