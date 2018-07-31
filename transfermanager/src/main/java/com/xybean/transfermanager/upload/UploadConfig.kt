package com.xybean.transfermanager.upload

import com.xybean.transfermanager.id.IdGenerator
import com.xybean.transfermanager.upload.connection.IUploadConnection
import com.xybean.transfermanager.upload.stream.IUploadStream

/**
 * Author @xybean on 2018/7/27.
 */
class UploadConfig {

    internal var connectionFactory: IUploadConnection.Factory? = null
    internal var streamFactory: IUploadStream.Factory? = null
    internal var idGenerator: IdGenerator? = null
    internal var offset = -1L
    internal var forceReload = false
    internal var headers = HashMap<String, String>()
    internal var fileName = ""
    internal var fileBody = ""
    internal var mimeType = ""

    class Builder {

        private val config = UploadConfig()

        fun connection(connection: IUploadConnection.Factory) = apply {
            config.connectionFactory = connection
        }

        fun stream(stream: IUploadStream.Factory) = apply {
            config.streamFactory = stream
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

        fun addHeader(name: String, value: String) = apply {
            config.headers[name] = value
        }

        fun addHeaders(headers: Map<String, String>) = apply {
            config.headers.putAll(headers)
        }

        fun build() = config

    }

}