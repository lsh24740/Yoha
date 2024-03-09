package com.gooey.common.network.retrofit

import com.gooey.common.INoProguard
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.JsonReader
import com.squareup.moshi.JsonWriter
import com.squareup.moshi.Moshi
import java.lang.RuntimeException
import java.lang.reflect.Type

/**
 * 自定义Adapter, 避免服务端下发字段value = null导致的crash
 * Created by lishihui on 2019-10-24.
 */
object NullSafeAdapter : JsonAdapter.Factory, INoProguard {
    override fun create(type: Type, annotations: Set<Annotation>, moshi: Moshi): JsonAdapter<*>? {
        return when (type) {
            String::class.java -> StringAdapter
            Int::class.java -> IntAdapter
            Long::class.java -> LongAdapter
            Boolean::class.java -> BooleanAdapter
            Float::class.java -> FloatAdapter
            Double::class.java -> DoubleAdapter
            else -> null
        }
    }

    private object StringAdapter : JsonAdapter<String>() {
        override fun fromJson(reader: JsonReader): String? {
            with(reader) {
                return when (peek()) {
                    JsonReader.Token.NULL -> {
                        nextNull<Unit>()
                        ""
                    }
                    JsonReader.Token.BEGIN_OBJECT -> {
                        reader.fromObject()
                    }

                    JsonReader.Token.BEGIN_ARRAY -> {
                        reader.fromArray()
                    }

                    JsonReader.Token.NAME -> {
                        nextName()
                    }
                    else -> {
                        nextString()
                    }
                }
            }
        }

        private fun JsonReader.fromObject(): String {
            beginObject()
            val sb = StringBuilder("{")
            while (hasNext()) {
                fromNestJson(sb)
            }
            endObject()
            return sb.append("}").toString()
        }

        private fun JsonReader.fromNestJson(sb: StringBuilder) {
            when (peek()) {
                JsonReader.Token.NULL -> {
                    nextNull<Unit>()
                }
                JsonReader.Token.BEGIN_OBJECT -> {
                    sb.append(fromObject())
                        .appendSeparator(this)
                }

                JsonReader.Token.BEGIN_ARRAY -> {
                    sb.append(fromArray())
                        .appendSeparator(this)
                }

                JsonReader.Token.NAME -> {
                    val name = nextName()
                    if (peek() == JsonReader.Token.NULL) {
                        skipValue()
                        if (!hasNext() && sb.isNotEmpty() && sb[sb.lastIndex] == ',') {
                            sb.deleteCharAt(sb.lastIndex)
                        }
                    } else {
                        sb.append("\"").append(name).append("\" : ")
                    }
                }
                JsonReader.Token.BOOLEAN -> {
                    sb.append(nextBoolean()).appendSeparator(this)
                }
                JsonReader.Token.NUMBER -> {
                    sb.append(nextString()).appendSeparator(this)
                }
                else -> {
                    sb.append("\"").append(nextString()).append("\"")
                        .appendSeparator(this)
                }
            }
        }

        private fun StringBuilder.appendSeparator(reader: JsonReader) {
            if (reader.hasNext()) {
                append(",")
            }
        }

        override fun toJson(writer: JsonWriter, value: String?) {
            if (value != null) {
                writer.value(value)
            } else {
                writer.value("")
            }
        }

        private fun JsonReader.fromArray(): String {
            beginArray()
            val sb = StringBuilder("[")
            while (hasNext()) {
                fromNestJson(sb)
            }
            if (sb[sb.lastIndex] == ',') {
                sb.deleteCharAt(sb.lastIndex)
            }
            endArray()
            return sb.append("]").toString()
        }
    }

    private object IntAdapter : PrimitiveJsonAdapter<Int>() {

        override fun toJson(writer: JsonWriter, value: Int?) {
            writer.value(value)
        }

        override fun defaultValue(): Int {
            return 0
        }

        override fun parseNext(reader: JsonReader): Int {
            return reader.nextInt()
        }

        override fun fromString(string: String): Int {
            return string.toInt()
        }
    }

    private object LongAdapter : PrimitiveJsonAdapter<Long>() {
        override fun toJson(writer: JsonWriter, value: Long?) {
            writer.value(value)
        }

        override fun defaultValue(): Long {
            return 0L
        }

        override fun parseNext(reader: JsonReader): Long {
            return reader.nextLong()
        }

        override fun fromString(string: String): Long {
            return string.toLong()
        }
    }

    private object BooleanAdapter : PrimitiveJsonAdapter<Boolean>() {
        override fun toJson(writer: JsonWriter, value: Boolean?) {
            writer.value(value)
        }

        override fun defaultValue(): Boolean {
            return false
        }

        override fun parseNext(reader: JsonReader): Boolean {
            return reader.nextBoolean()
        }

        override fun fromString(string: String): Boolean {
            return string.toBoolean()
        }
    }

    private object FloatAdapter : PrimitiveJsonAdapter<Float>() {

        override fun toJson(writer: JsonWriter, value: Float?) {
            writer.value(value)
        }

        override fun defaultValue(): Float {
            return 0f
        }

        override fun parseNext(reader: JsonReader): Float {
            return reader.nextDouble().toFloat()
        }

        override fun fromString(string: String): Float {
            return string.toFloat()
        }
    }

    private object DoubleAdapter : PrimitiveJsonAdapter<Double>() {

        override fun toJson(writer: JsonWriter, value: Double?) {
            writer.value(value)
        }

        override fun defaultValue(): Double {
            return 0.0
        }

        override fun parseNext(reader: JsonReader): Double {
            return reader.nextDouble()
        }

        override fun fromString(string: String): Double {
            return string.toDouble()
        }
    }
}

abstract class PrimitiveJsonAdapter<T> : JsonAdapter<T>() {
    abstract fun defaultValue(): T
    abstract fun parseNext(reader: JsonReader): T
    abstract fun fromString(string: String): T

    override fun fromJson(reader: JsonReader): T? {
        with(reader) {
            return when (peek()) {
                JsonReader.Token.NULL -> {
                    nextNull<Unit>()
                    defaultValue()
                }
                JsonReader.Token.STRING -> {
                    val string = nextString()
                    if (string != null) {
                        try {
                            fromString(string)
                        } catch (e: RuntimeException) {
                            defaultValue()
                        }
                    } else {
                        defaultValue()
                    }
                }
                else -> {
                    try {
                        parseNext(this)
                    } catch (e: RuntimeException) {
                        skipValue()
                        defaultValue()
                    }
                }
            }
        }
    }
}