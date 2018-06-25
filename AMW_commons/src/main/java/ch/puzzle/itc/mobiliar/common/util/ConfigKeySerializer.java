package ch.puzzle.itc.mobiliar.common.util;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;

import java.io.IOException;

public class ConfigKeySerializer extends StdSerializer {

    public ConfigKeySerializer() {
        super(ConfigKey.class);
    }

    public ConfigKeySerializer(Class t) {
        super(t);
    }

    @Override
    public void serialize(Object o, JsonGenerator generator, SerializerProvider serializerProvider) throws IOException {
        if (o instanceof ConfigKey) {
            ConfigKey confKey = (ConfigKey) o;
            generator.writeStartObject();
            generator.writeFieldName("value");
            generator.writeString(confKey.getValue());
            generator.writeFieldName("env");
            generator.writeString(confKey.getEnvName());
            generator.writeEndObject();
        }
    }

}
