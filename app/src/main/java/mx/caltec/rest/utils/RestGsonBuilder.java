package mx.caltec.rest.utils;

import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

import java.lang.reflect.Type;
import java.util.Date;


public final class RestGsonBuilder {

    public static GsonBuilder getGsonBuilder() {
        GsonBuilder builder = new GsonBuilder();

        //add custome serialized classes
        //builder.registerTypeAdapter(ClasName.class, new EventoSerializer());

        //date-datetime handling
        JsonSerializer<Date> ser = new JsonSerializer<Date>() {
            @Override
            public JsonElement serialize(Date src, Type typeOfSrc, JsonSerializationContext context) {
                return src == null ? null : new JsonPrimitive(src.getTime());
            }
        };

        JsonDeserializer<Date> deser = new JsonDeserializer<Date>() {
            @Override
            public Date deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
                return json == null ? null : new Date(json.getAsLong());
            }
        };

        builder.registerTypeAdapter(Date.class, ser);
        builder.registerTypeAdapter(java.sql.Date.class, ser);
        builder.registerTypeAdapter(java.sql.Timestamp.class, ser);
        builder.registerTypeAdapter(Date.class, deser);
        builder.registerTypeAdapter(java.sql.Date.class, deser);
        builder.registerTypeAdapter(java.sql.Timestamp.class, deser);

        return builder;
    }

}
