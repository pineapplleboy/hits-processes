package com.example.googleclass.feature.taskdetail.data.progress

import okhttp3.MediaType
import okhttp3.RequestBody
import okio.Buffer
import okio.BufferedSink
import okio.ForwardingSink
import okio.Sink
import okio.buffer

class ProgressRequestBody(
    private val delegate: RequestBody,
    private val onProgress: (bytesWritten: Long, totalBytes: Long) -> Unit,
) : RequestBody() {

    override fun contentType(): MediaType? = delegate.contentType()

    override fun contentLength(): Long = delegate.contentLength()

    override fun writeTo(sink: BufferedSink) {
        val totalBytes = contentLength()
        val progressSink = ProgressSink(sink, totalBytes, onProgress).buffer()
        delegate.writeTo(progressSink)
        progressSink.flush()
    }

    private class ProgressSink(
        delegate: Sink,
        private val totalBytes: Long,
        private val onProgress: (Long, Long) -> Unit,
    ) : ForwardingSink(delegate) {

        private var bytesWritten: Long = 0L

        override fun write(source: Buffer, byteCount: Long) {
            super.write(source, byteCount)
            bytesWritten += byteCount
            onProgress(bytesWritten, totalBytes)
        }
    }
}
