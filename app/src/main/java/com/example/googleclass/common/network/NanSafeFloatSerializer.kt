package com.example.googleclass.common.network

import kotlinx.serialization.KSerializer
import kotlinx.serialization.builtins.nullable
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

object NanSafeFloatSerializer : KSerializer<Float?> {
    private val delegate = Float.serializer().nullable

    override val descriptor: SerialDescriptor = delegate.descriptor

    override fun deserialize(decoder: Decoder): Float? {
        val value = delegate.deserialize(decoder)
        return value?.takeIf { it.isFinite() }
    }

    override fun serialize(encoder: Encoder, value: Float?) {
        delegate.serialize(encoder, value)
    }
}
