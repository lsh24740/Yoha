package com.gooey.common.network.retrofit

import com.squareup.moshi.*
import com.squareup.moshi.internal.Util
import com.squareup.moshi.internal.Util.generatedAdapter
import com.squareup.moshi.internal.Util.resolve
import java.lang.reflect.Modifier
import java.lang.reflect.Type
import java.util.AbstractMap.SimpleEntry
import kotlin.collections.Map.Entry
import kotlin.reflect.*
import kotlin.reflect.full.findAnnotation
import kotlin.reflect.full.memberProperties
import kotlin.reflect.full.primaryConstructor
import kotlin.reflect.jvm.internal.impl.types.KotlinType
import kotlin.reflect.jvm.internal.impl.types.checker.SimpleClassicTypeSystemContext
import kotlin.reflect.jvm.internal.impl.types.model.SimpleTypeMarker
import kotlin.reflect.jvm.isAccessible
import kotlin.reflect.jvm.javaField
import kotlin.reflect.jvm.javaType

/** Classes annotated with this are eligible for this adapter. */
private val KOTLIN_METADATA = Metadata::class.java

/**
 * Placeholder value used when a field is absent from the JSON. Note that this code
 * distinguishes between absent values and present-but-null values.
 */
private val ABSENT_VALUE = Any()

/**
 * This class encodes Kotlin classes using their properties. It decodes them by first invoking the
 * constructor, and then by setting any additional properties that exist, if any.
 */
internal class KotlinJsonAdapter<T>(
    val constructor: KFunction<T>,
    val allBindings: List<Binding<T, Any?>?>,
    val nonTransientBindings: List<Binding<T, Any?>>,
    val options: JsonReader.Options
) : JsonAdapter<T>() {

    override fun fromJson(reader: JsonReader): T {
        val constructorSize = constructor.parameters.size

        // Read each value into its slot in the array.
        val values = Array<Any?>(allBindings.size) { ABSENT_VALUE }
        reader.beginObject()
        while (reader.hasNext()) {
            val index = reader.selectName(options)
            if (index == -1) {
                reader.skipName()
                reader.skipValue()
                continue
            }
            val binding = nonTransientBindings[index]

            val propertyIndex = binding.propertyIndex
            if (values[propertyIndex] !== ABSENT_VALUE) {
                throw JsonDataException(
                    "Multiple values for '${binding.property.name}' at ${reader.path}"
                )
            }

            values[propertyIndex] = binding.adapter.fromJson(reader)

            if (values[propertyIndex] == null && !binding.property.returnType.isMarkedNullable) {
                throw Util.unexpectedNull(
                    binding.property.name,
                    binding.jsonName,
                    reader
                )
            }
        }
        reader.endObject()

        // Confirm all parameters are present, optional, or nullable.
        for (i in 0 until constructorSize) {
            if (values[i] === ABSENT_VALUE && !constructor.parameters[i].isOptional) {
                if (!constructor.parameters[i].type.isMarkedNullable) {
                    throw Util.missingProperty(
                        constructor.parameters[i].name,
                        allBindings[i]?.jsonName,
                        reader
                    )
                }
                values[i] = null // Replace absent with null.
            }
        }

        // Call the constructor using a Map so that absent optionals get defaults.
        val result = constructor.callBy(IndexedParameterMap(constructor.parameters, values))

        // Set remaining properties.
        for (i in constructorSize until allBindings.size) {
            val binding = allBindings[i]!!
            val value = values[i]
            binding.set(result, value)
        }

        return result
    }

    override fun toJson(writer: JsonWriter, value: T?) {
        if (value == null) throw NullPointerException("value == null")

        writer.beginObject()
        for (binding in allBindings) {
            if (binding == null) continue // Skip constructor parameters that aren't properties.

            writer.name(binding.name)
            binding.adapter.toJson(writer, binding.get(value))
        }
        writer.endObject()
    }

    override fun toString() = "KotlinJsonAdapter(${constructor.returnType})"

    data class Binding<K, P>(
        val name: String,
        val jsonName: String?,
        val adapter: JsonAdapter<P>,
        val property: KProperty1<K, P>,
        val parameter: KParameter?,
        val propertyIndex: Int
    ) {
        fun get(value: K) = property.get(value)

        fun set(result: K, value: P) {
            if (value !== ABSENT_VALUE) {
                (property as KMutableProperty1<K, P>).set(result, value)
            }
        }
    }

    /** A simple [Map] that uses parameter indexes instead of sorting or hashing. */
    class IndexedParameterMap(
        private val parameterKeys: List<KParameter>,
        private val parameterValues: Array<Any?>
    ) : AbstractMap<KParameter, Any?>() {

        override val entries: Set<Entry<KParameter, Any?>>
            get() {
                val allPossibleEntries = parameterKeys.mapIndexed { index, value ->
                    SimpleEntry<KParameter, Any?>(value, parameterValues[index])
                }
                return allPossibleEntries.filterTo(LinkedHashSet<Entry<KParameter, Any?>>()) {
                    it.value !== ABSENT_VALUE
                }
            }

        override fun containsKey(key: KParameter) = parameterValues[key.index] !== ABSENT_VALUE

        override fun get(key: KParameter): Any? {
            val value = parameterValues[key.index]
            return if (value !== ABSENT_VALUE) value else null
        }
    }
}

class KotlinJsonAdapterFactory : JsonAdapter.Factory {
    override fun create(type: Type, annotations: MutableSet<out Annotation>, moshi: Moshi)
            : JsonAdapter<*>? {
        if (annotations.isNotEmpty()) return null

        val rawType = Types.getRawType(type)
        if (rawType.isInterface) return null
        if (rawType.isEnum) return null
        if (!rawType.isAnnotationPresent(KOTLIN_METADATA)) return null
        if (Util.isPlatformType(rawType)) return null
        try {
            val generatedAdapter = generatedAdapter(moshi, type, rawType)
            if (generatedAdapter != null) {
                return generatedAdapter
            }
        } catch (e: RuntimeException) {
            if (e.cause !is ClassNotFoundException) {
                throw e
            }
            // Fall back to a reflective adapter when the generated adapter is not found.
        }

        require(!rawType.isLocalClass) {
            "Cannot serialize local class or object expression ${rawType.name}"
        }
        val rawTypeKotlin = rawType.kotlin
        require(!rawTypeKotlin.isAbstract) {
            "Cannot serialize abstract class ${rawType.name}"
        }
        require(!rawTypeKotlin.isInner) {
            "Cannot serialize inner class ${rawType.name}"
        }
        require(rawTypeKotlin.objectInstance == null) {
            "Cannot serialize object declaration ${rawType.name}"
        }
        require(!rawTypeKotlin.isSealed) {
            "Cannot reflectively serialize sealed class ${rawType.name}. Please register an adapter."
        }

        val constructor = rawTypeKotlin.primaryConstructor ?: return null
        val parametersByName = constructor.parameters.associateBy { it.name }
        constructor.isAccessible = true

        val bindingsByName = LinkedHashMap<String, KotlinJsonAdapter.Binding<Any, Any?>>()

        for (property in rawTypeKotlin.memberProperties) {
            val parameter = parametersByName[property.name]

            if (Modifier.isTransient(property.javaField?.modifiers ?: 0)) {
                require(parameter == null || parameter.isOptional) {
                    "No default value for transient constructor $parameter"
                }
                continue
            }

            val parameterType = parameter?.type
            val propertyReturnType = property.returnType
            require(parameter == null || parameterType == propertyReturnType) {
                "'${property.name}' has a constructor parameter of type ${parameter!!.type} but a property of type ${property.returnType}."
            }

            if (property !is KMutableProperty1 && parameter == null) continue

            property.isAccessible = true
            val allAnnotations = property.annotations.toMutableList()
            var jsonAnnotation = property.findAnnotation<Json>()

            if (parameter != null) {
                allAnnotations += parameter.annotations
                if (jsonAnnotation == null) {
                    jsonAnnotation = parameter.findAnnotation()
                }
            }

            val name = jsonAnnotation?.name ?: property.name
            val resolvedPropertyType = resolve(type, rawType, property.returnType.javaType)
            val adapter = moshi.adapter<Any>(
                resolvedPropertyType, Util.jsonAnnotations(allAnnotations.toTypedArray()), property.name
            )

            @Suppress("UNCHECKED_CAST")
            bindingsByName[property.name] = KotlinJsonAdapter.Binding(
                name,
                jsonAnnotation?.name ?: name,
                adapter,
                property as KProperty1<Any, Any?>,
                parameter,
                parameter?.index ?: -1
            )
        }

        val bindings = ArrayList<KotlinJsonAdapter.Binding<Any, Any?>?>()

        for (parameter in constructor.parameters) {
            val binding = bindingsByName.remove(parameter.name)
            require(binding != null || parameter.isOptional) {
                "No property for required constructor $parameter"
            }
            bindings += binding
        }

        var index = bindings.size
        for (bindingByName in bindingsByName) {
            bindings += bindingByName.value.copy(propertyIndex = index++)
        }

        val nonTransientBindings = bindings.filterNotNull()
        val options = JsonReader.Options.of(*nonTransientBindings.map { it.name }.toTypedArray())
        return KotlinJsonAdapter(constructor, bindings, nonTransientBindings, options).nullSafe()
    }

    private fun dumpType(type: KType?, name: String): String {
        if (type == null) {
            return "$name: null"
        }
        val sb = StringBuilder()
        sb.append("$name.javaClass.name: ${type.javaClass.name}\n")
        sb.append("$name.hashCode(): ${type.hashCode()}\n")
        try {
            val kotlinTypeField = type.javaClass.getDeclaredField("type")
            val accessible = kotlinTypeField.isAccessible
            try {
                kotlinTypeField.isAccessible = true
                val kotlinType = kotlinTypeField.get(type) as? KotlinType
                if (kotlinType != null) {
                    sb.append("$name.type.hashCode(): ${kotlinType.hashCode()}\n")
                    dumpKotlinType(kotlinType, "$name.type", sb)
                } else {
                    sb.append("$name.type: null\n")
                }
            } finally {
                kotlinTypeField.isAccessible = accessible
            }
        } catch (e: NoSuchFieldException) {

        }
        return sb.toString()
    }

    private fun dumpKotlinType(type: KotlinType, path: String, out: StringBuilder) {
        out.append("$path.isMarkedNullable: ${type.isMarkedNullable}\n")
        val simpleTypeMarker = SimpleClassicTypeSystemContext.INSTANCE.asSimpleType(type)
        if (simpleTypeMarker != null) {
            out.append("$path.asSimpleType: success\n")
            dumpSimpleType(simpleTypeMarker, "$path.type", out)
        } else {
            out.append("$path.asSimpleType: null\n")
            val flexibleTypeMarker = SimpleClassicTypeSystemContext.INSTANCE.asFlexibleType(type)
            if (flexibleTypeMarker != null) {
                out.append("$path.asFlexibleType: success\n")
                val lowerBound = SimpleClassicTypeSystemContext.INSTANCE.lowerBound(flexibleTypeMarker)
                out.append("$path.asFlexibleType.lowerBound dump begin...\n")
                dumpSimpleType(lowerBound, "$path.type.asFlexibleType.lowerBound", out)
                val upperBound = SimpleClassicTypeSystemContext.INSTANCE.upperBound(flexibleTypeMarker)
                out.append("$path.asFlexibleType.upperBound dump begin...\n")
                dumpSimpleType(upperBound, "$path.type.asFlexibleType.upperBound", out)
            } else {
                out.append("$path.asFlexibleType: null\n")
            }
        }
    }

    private fun dumpSimpleType(type: SimpleTypeMarker, path: String, out: StringBuilder) {
        out.append("$path.argumentsCount: ${SimpleClassicTypeSystemContext.INSTANCE.argumentsCount(type)}\n")
        out.append("$path.isMarkedNullable: ${SimpleClassicTypeSystemContext.INSTANCE.isMarkedNullable(type)}\n")
        out.append("$path.asDefinitelyNotNullType == null: ${SimpleClassicTypeSystemContext.INSTANCE.asDefinitelyNotNullType(type) == null}\n")
        val typeConstructor = SimpleClassicTypeSystemContext.INSTANCE.typeConstructor(type)
        out.append("$path.typeConstructor: $typeConstructor@${typeConstructor.hashCode()}\n")
        for (i in 0 until SimpleClassicTypeSystemContext.INSTANCE.argumentsCount(type)) {
            val arg = SimpleClassicTypeSystemContext.INSTANCE.getArgument(type, i)
            out.append("$path.argument[$i].isStarProjection: ${SimpleClassicTypeSystemContext.INSTANCE.isStarProjection(arg)}\n")
            if (!SimpleClassicTypeSystemContext.INSTANCE.isStarProjection(arg)) {
                val kotlinType = SimpleClassicTypeSystemContext.INSTANCE.getType(arg) as? KotlinType
                if (kotlinType != null) {
                    out.append("$path.argument[$i].getType is KotlinType\n")
                    dumpKotlinType(kotlinType, "$path.argument[$i]", out)
                } else {
                    out.append("$path.argument[$i].getType is not KotlinType\n")
                }
            }
        }
    }
}
