package com.xybean.taskmanager.download

/**
 * Author @xybean on 2018/7/16.
 */
interface IDownloadTask {

    var url: String

    var id: Int

    var status: Int

    var listener: DownloadListener?

    var targetPath: String

    var targetName: String
}
