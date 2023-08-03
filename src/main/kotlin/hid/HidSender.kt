package hid

import org.hid4java.HidDevice
import java.util.function.Consumer

class HidSender(private val hidDevice: HidDevice) : Consumer<String> {
    override fun accept(message: String) {
        if (message.length != 160) {
            return
        }

        sendUpdate(message)
    }

    private fun sendUpdate(message: String) {
        hidDevice.open().run {
            var i = 0
            while (i < 160) {
                val j = i + 32
                val b = message.substring(i, j).toByteArray()
                i = j
                hidDevice.write(b, b.size, 0x00)
            }
        }.also {
            hidDevice.close()
        }
    }
}
