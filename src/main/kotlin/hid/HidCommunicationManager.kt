package hid

import org.hid4java.HidDevice
import org.hid4java.HidManager
import org.hid4java.HidServicesListener
import org.hid4java.HidServicesSpecification
import org.hid4java.event.HidServicesEvent
import java.time.Instant
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.ScheduledFuture
import java.util.concurrent.TimeUnit

class HidCommunicationManager(
    private val vendorId: Int,
    private val productId: Int,
    private val usagePage: Int,
    private val usage: Int,
    private val receiverRefreshIntervalInMilliseconds: Long,
    private val senderRefreshIntervalInSeconds: Long,
    private val messageBuilderService: MessageBuilderService
) : HidServicesListener,
    Runnable {
    private val scheduledExecutorService: ScheduledExecutorService = Executors.newScheduledThreadPool(1)
    private var sender: ScheduledFuture<*>? = null
    private var receiver: ScheduledFuture<*>? = null
    private var keyboardLayer: KeyboardLayer = KeyboardLayer.default()
    private var previousMessage: String? = null
    private var lastUpdateSentAt: Instant = Instant.now()

    override fun hidDeviceAttached(event: HidServicesEvent) {
        if (isSofleChoc(event.hidDevice)) {
            startCommunication(event.hidDevice)
        }
    }

    override fun hidDeviceDetached(event: HidServicesEvent) {
        if (isSofleChoc(event.hidDevice)) {
            stopCommunication()
        }
    }

    override fun hidFailure(event: HidServicesEvent) {
        if (isSofleChoc(event.hidDevice)) {
            stopCommunication()
        }
    }

    override fun hidDataReceived(p0: HidServicesEvent?) {
        // TODO: Figure out if this can replace the receiver to make it all event based (was missing in 0.7.0 hid4java)
    }

    override fun run() {
        val hidServicesSpecification = HidServicesSpecification()
        val hidServices = HidManager.getHidServices(hidServicesSpecification)
        hidServices.addHidServicesListener(this)

        hidServices.attachedHidDevices.firstOrNull { isSofleChoc(it) }?.let { hidDevice ->
            startCommunication(hidDevice)
        }
    }

    private fun startCommunication(hidDevice: HidDevice) {
        startPeriodicSender(hidDevice)
        startReceiver(hidDevice)
    }

    private fun stopCommunication() {
        sender?.cancel(true)
        receiver?.cancel(true)
    }

    private fun startPeriodicSender(hidDevice: HidDevice) {
        sender = scheduledExecutorService.scheduleAtFixedRate(
            {
                if (lastUpdateSentAt.plusSeconds(senderRefreshIntervalInSeconds).isBefore(Instant.now())) {
                    sendUpdate(hidDevice)
                }
            },
            0L,
            senderRefreshIntervalInSeconds,
            TimeUnit.SECONDS
        )
    }

    private fun startReceiver(hidDevice: HidDevice) {
        receiver = scheduledExecutorService.scheduleAtFixedRate(
            { processReceiverResult(hidDevice, HidReceiver(hidDevice).call()) },
            0L,
            receiverRefreshIntervalInMilliseconds,
            TimeUnit.MILLISECONDS
        )
    }

    private fun processReceiverResult(hidDevice: HidDevice, keyboardLayer: KeyboardLayer) {
        if (this.keyboardLayer != keyboardLayer) {
            this.keyboardLayer = keyboardLayer
            sendUpdate(hidDevice)
        } else {
            this.keyboardLayer = keyboardLayer
        }
    }

    private fun sendUpdate(hidDevice: HidDevice) {
        val newMessage = messageBuilderService.assemble(keyboardLayer)

        if (previousMessage != newMessage) {
            previousMessage = newMessage
            lastUpdateSentAt = Instant.now()
            HidSender(hidDevice).accept(newMessage)
        }
    }

    private fun isSofleChoc(hidDevice: HidDevice) =
        vendorId == hidDevice.vendorId
                && productId == hidDevice.productId
                && usagePage == hidDevice.usagePage
                && usage == hidDevice.usage
}

enum class KeyboardLayer(val fiveCharacterLongName: String) {
    QWERTY("Base "),
    LOWER("Lower"),
    RAISE("Upper"),
    ADJUST("Misc ");

    companion object {
        fun fromIndex(index: Int) = when (index) {
            0 -> QWERTY
            1 -> LOWER
            2 -> RAISE
            3 -> ADJUST
            else -> QWERTY
        }

        fun default() = QWERTY
    }
}
