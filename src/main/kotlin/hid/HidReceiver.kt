package hid

import org.hid4java.HidDevice
import java.util.concurrent.Callable

class HidReceiver(private val hidDevice: HidDevice) : Callable<KeyboardLayer> {
    override fun call(): KeyboardLayer {
        return receiveKeyboardLayer(hidDevice)
    }

    private fun receiveKeyboardLayer(hidDevice: HidDevice): KeyboardLayer {
        return hidDevice.open().run {
            hidDevice.read(1, 32).let {
                KeyboardLayer.fromIndex(it[0].toInt())
            }
        }.also {
            hidDevice.close()
        }
    }
}
