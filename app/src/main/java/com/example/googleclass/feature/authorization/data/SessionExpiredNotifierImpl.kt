package com.example.googleclass.feature.authorization.data

import com.example.googleclass.feature.authorization.domain.SessionExpiredNotifier
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow

class SessionExpiredNotifierImpl : SessionExpiredNotifier {

    private val channel = Channel<Unit>(Channel.CONFLATED)

    override val sessionExpiredEvents = channel.receiveAsFlow()

    override fun notifySessionExpired() {
        channel.trySend(Unit)
    }
}
