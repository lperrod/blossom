package com.blossomproject.core.common.json;

import com.fasterxml.jackson.databind.BeanDescription;
import com.fasterxml.jackson.databind.SerializationConfig;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.ser.BeanPropertyWriter;
import com.fasterxml.jackson.databind.ser.BeanSerializerModifier;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import java.util.List;

/**
 * Jackson module that serializes Long/long bean properties named "id" or ending with "Id"
 * as String. This prevents JavaScript precision loss for large Long IDs (> Number.MAX_SAFE_INTEGER).
 * Other Long fields (like totalElements, size, quantity, etc.) remain as numbers.
 */
public class IdAsStringModule extends SimpleModule {

    public IdAsStringModule() {
        super("IdAsString");
        setSerializerModifier(new BeanSerializerModifier() {
            @Override
            public List<BeanPropertyWriter> changeProperties(SerializationConfig config,
                    BeanDescription beanDesc, List<BeanPropertyWriter> beanProperties) {
                for (BeanPropertyWriter writer : beanProperties) {
                    String name = writer.getName();
                    if ("id".equals(name) || name.endsWith("Id")) {
                        Class<?> type = writer.getType().getRawClass();
                        if (type == Long.class || type == long.class) {
                            writer.assignSerializer(ToStringSerializer.instance);
                        }
                    }
                }
                return beanProperties;
            }
        });
    }
}
