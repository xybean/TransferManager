package com.xybean.transfermanager.upload

import com.xybean.transfermanager.id.IdGenerator
import com.xybean.transfermanager.monitor.MonitorListener
import com.xybean.transfermanager.upload.processor.IUploadProcessor
import com.xybean.transfermanager.upload.provider.IFileProvider

/**
 * Author @xybean on 2018/7/27.
 */
class UploadConfig {

    internal var processorFactory: IUploadProcessor.Factory? = null
    internal var fileFactory: IFileProvider.Factory? = null
    internal var idGenerator: IdGenerator? = null
    internal var offset = -1L
    internal var forceReload = false
    internal var headers = HashMap<String, String>()
    internal var fileName = ""
    internal var fileBody = ""
    internal var mimeType = ""
    internal var priority = 0
    internal var monitor: MonitorListener? = null

    class Builder {

        private val config = UploadConfig()

        fun processor(processorFactory: IUploadProcessor.Factory) = apply {
            config.processorFactory = processorFactory
        }

        fun file(fileFactory: IFileProvider.Factory) = apply {
            config.fileFactory = fileFactory
        }

        fun idGenerator(idGenerator: IdGenerator) = apply {
            config.idGenerator = idGenerator
        }

        fun offset(offset: Long) = apply {
            config.offset = offset
        }

        fun fileName(fileName: String) = apply {
            config.fileName = fileName
        }

        fun fileBody(fileBody: String) = apply {
            config.fileBody = fileBody
        }

        fun forceReload(force: Boolean) = apply {
            config.forceReload = force
        }

        fun priority(priority: Int) = apply {
            config.priority = priority
        }

        fun addHeader(name: String, value: String) = apply {
            config.headers[name] = value
        }

        fun addHeaders(headers: Map<String, String>) = apply {
            config.headers.putAll(headers)
        }

        fun monitor(monitorListener: MonitorListener) = apply {
            config.monitor = monitorListener
        }

        fun build() = config

    }

}