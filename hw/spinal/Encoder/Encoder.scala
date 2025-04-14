package encoder

import spinal.core._
import spinal.lib._


// State machine to detect the direction of a rotary encoder
// keeps track of the last two states of the encoder
// based on the information of the last two states, the direction is determined
// only enabled if no reference update in progress
case class Encoder(width: Int) extends Component {
  val io = new Bundle {
    val pinA = in Bool ()
    val pinB = in Bool ()
    val pinIndex = in Bool ()
    val enable = in Bool () 
    val delta = out SInt (width bits) // positional change 
    val index = out Bool ()
  }

  val chainA, chainB, chainIndex = FFSync()
  val regA_old, regB_old = Reg(Bool()) init (False)
  val initial = Reg(Bool()) init (False)
  val delta = Reg(SInt(width bits)) init (0)
  val index = Reg(Bool()) init (False)

  chainA.io.input := io.pinA
  chainB.io.input := io.pinB
  chainIndex.io.input := io.pinIndex
  chainA.io.pinOutput.ready := io.enable
  chainB.io.pinOutput.ready := io.enable
  chainIndex.io.pinOutput.ready := io.enable

  // a state is invalid if we have jumped over it or it is no proper state (eg. in the beginning or upon user input)
  val valid = chainA.io.pinOutput.valid && chainB.io.pinOutput.valid && chainIndex.io.pinOutput.valid && io.enable
  val pinA = chainA.io.pinOutput.payload
  val pinB = chainB.io.pinOutput.payload
  val pinIndex = chainIndex.io.pinOutput.payload

  when(!initial) {
    initial := valid
    delta := 0
    index := False
  } otherwise {
    when(valid) {
      switch(pinB ## pinA ## regB_old ## regA_old) {
        is(B"0000") { delta := 0 }
        is(B"0001") { delta := -1 }
        is(B"0010") { delta := 1 }
        is(B"0100") { delta := 1 }
        is(B"0101") { delta := 0 }
        is(B"0111") { delta := -1 }
        is(B"1000") { delta := -1 }
        is(B"1010") { delta := 0 }
        is(B"1011") { delta := 1 }
        is(B"1101") { delta := 1 }
        is(B"1110") { delta := -1 }
        is(B"1111") { delta := 0 }
        default {
          // should not happen
          // fpga should be fast enough to catch all transitions
          delta := 0
        }
      }
      index := pinIndex
    } otherwise {
      delta := 0
      index := False
      initial := False
    }
  }

  // only update the old values if we are in a valid state
  when(valid) {
    regA_old := pinA
    regB_old := pinB
  }

  io.delta := delta
  io.index := index

}

object EncoderVerilog extends App() {
  Config.spinal.generateVerilog(Encoder(64))
}
