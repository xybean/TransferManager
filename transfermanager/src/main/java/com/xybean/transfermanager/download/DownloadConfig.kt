package com.xybean.transfermanager.download

import com.xybean.transfermanager.download.connection.IDownloadConnection
import com.xybean.transfermanager.download.stream.IDownloadStream
import com.xybean.transfermanager.id.IdGenerator

/**
 * Author @xybean on 2018/7/26.
 */
class DownloadConfig {

    internal var connectionFactory: IDownloadConnection.Factory? = null
    internal var streamFactory: IDownloadStream.Factory? = null
    internal var idGenerator: IdGenerator? = null
    internal var headers = HashMap<String, String>()
    internal var offset = -1L
    internal var total = -1L
    internal var forceReload = false

    class Builder {

        private val config = DownloadConfig()

        fun connection(connection: IDownloadConnection.Factory) = apply {
            config.connectionFactory = connection
        }

        fun stream(stream: IDownloadStream.Factory) = apply {
            config.streamFactory = stream
        }

        fun idGenerator(idGenerator: IdGenerator) = apply {
            config.idGenerator = idGenerator
        }

        fun offset(offset: Long) = apply {
            config.offset = offset
        }

        fun offset(offset: Long, total: Long) = apply {
            config.offset = offset
            config.total = total
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