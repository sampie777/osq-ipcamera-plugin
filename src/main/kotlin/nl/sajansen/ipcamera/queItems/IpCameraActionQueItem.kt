package nl.sajansen.ipcamera.queItems

import nl.sajansen.ipcamera.IpCameraPlugin
import nl.sajansen.ipcamera.cameras.Camera
import nl.sajansen.ipcamera.cameras.DahuaCamera
import nl.sajansen.ipcamera.cameras.SchaapsoundCamera
import objects.notifications.Notifications
import objects.que.JsonQueue
import objects.que.QueItem
import java.awt.Color
import java.util.logging.Logger

class IpCameraActionQueItem(
    override val plugin: IpCameraPlugin,
    override val name: String,
    val camera: Camera,
    val actionUrl: String
) : QueItem {

    private val logger = Logger.getLogger(IpCameraActionQueItem::class.java.name)

    companion object {
        fun fromJson(plugin: IpCameraPlugin, jsonQueItem: JsonQueue.QueueItem): IpCameraActionQueItem {
            val camera = plugin.cameras.find { it.name == jsonQueItem.data["camera"]!! } as Camera
            return IpCameraActionQueItem(
                plugin,
                jsonQueItem.name,
                camera,
                jsonQueItem.data["actionUrl"]!!
            )
        }
    }

    override var executeAfterPrevious = false
    override var quickAccessColor: Color? = plugin.quickAccessColor

    override fun renderText(): String = name

    override fun activate() {
        logger.info("Activating item on url: $actionUrl")

        try {
            Thread {
                asyncActivate(camera)
            }.start()
        } catch (e: Exception) {
            logger.warning("Failed to create thread for activating command '$name'")
            e.printStackTrace()
            Notifications.add("Failed to activate command '$name': ${e.localizedMessage}", "IP Camera")
        }
    }

    fun asyncActivate(camera: Camera) {
        logger.info("Async activating item")
        try {
            if (camera is DahuaCamera) {
                camera.apiGet(actionUrl)
            } else if (camera is SchaapsoundCamera) {
                camera.apiGet(actionUrl)
            } else {
                throw NotImplementedError("Default activation of presets is not yet implemented")
            }
        } catch (e: Exception) {
            logger.warning("Failed to send command '$name' to IP Camera")
            e.printStackTrace()
            Notifications.add("Failed to send command '$name' to IP Camera: ${e.localizedMessage}", "IP Camera")
        }
    }

    override fun deactivate() {}

    override fun toConfigString(): String {
        throw NotImplementedError("This method is deprecated")
    }

    override fun toJson(): JsonQueue.QueueItem {
        val jsonItem = super.toJson()
        jsonItem.data["camera"] = camera.name
        jsonItem.data["actionUrl"] = actionUrl
        return jsonItem
    }
}