package turing.model;

import org.json.JSONObject;

import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public abstract class JsonMapper {

    /**
     * Get the default value of a (primitive) data type
     * @param clazz
     * @param <T>
     * @return
     */
    private static <T> T getDefaultValue(Class<T> clazz) {
        // Create a one-element array and return the first element.
        // The one element gets initialized by default so we get the default value
        // of the type.
        return (T) Array.get(Array.newInstance(clazz, 1), 0);
    }
    /**
     * Deserialize an object of type T from its Json representation.
     * The class T must implement the empty MapsJson interface.
     * @param json
     * @param tClass
     * @param <T>
     * @return
     */
    public static <T extends MapsJson> T fromJson(JSONObject json, Class<T> tClass) throws IllegalAccessException, InvocationTargetException, InstantiationException {
        Field[] fields = tClass.getDeclaredFields();
        // Get T's first constructor
        var constructor = (Constructor<T>) tClass.getConstructors()[0];
        var parameters = new ArrayList<>();

        // Find the default values for all of its parameters
        for (var type : constructor.getParameterTypes()) {
            parameters.add(getDefaultValue(type));
        }

        // Create a new instance of T with the default values
        T tInstance = constructor.newInstance(parameters.toArray());

        // Reassign each field with the values from the json mapping
        for (var field : fields) {
            field.setAccessible(true);
            // field is annotated with Maps(from="...", to="...")
            Object value = null;
            if(field.isAnnotationPresent(Maps.class)) {
                var annotation = field.getAnnotation(Maps.class);
                assert(annotation.from().equals(field.getName()));
                value = json.get(annotation.to());
            }
            else {
                value = json.get(field.getName());
            }
            // Special case for UUID
            if (field.getType().equals(UUID.class)) {
                value = UUID.fromString(value.toString());
            }

            field.set(tInstance, value);
        }
        return tInstance;
    }
}
