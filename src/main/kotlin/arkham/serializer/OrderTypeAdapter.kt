package arkham.serializer

import com.antik.utils.arkham.request.OrderType
import com.google.gson.*
import java.lang.reflect.Type

class OrderTypeAdapter : JsonSerializer<OrderType>, JsonDeserializer<OrderType> {
    override fun serialize(src: OrderType?, typeOfSrc: Type?, context: JsonSerializationContext?): JsonElement {
        return JsonPrimitive(src?.value)
    }

    override fun deserialize(json: JsonElement?, typeOfT: Type?, context: JsonDeserializationContext?): OrderType {
        val value = json?.asString
        return OrderType.entries.first { it.value == value }
    }
}
