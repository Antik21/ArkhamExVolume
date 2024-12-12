package arkham.serializer

import com.antik.utils.arkham.request.OrderSide
import com.google.gson.*
import java.lang.reflect.Type

class OrderSideAdapter : JsonSerializer<OrderSide>, JsonDeserializer<OrderSide> {
    override fun serialize(src: OrderSide?, typeOfSrc: Type?, context: JsonSerializationContext?): JsonElement {
        return JsonPrimitive(src?.value)
    }

    override fun deserialize(json: JsonElement?, typeOfT: Type?, context: JsonDeserializationContext?): OrderSide {
        val value = json?.asString
        return OrderSide.entries.first { it.value == value }
    }
}
