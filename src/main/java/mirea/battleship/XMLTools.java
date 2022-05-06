package mirea.battleship;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;

import java.util.*;

public class XMLTools {
    public static <T> T reXML(final Object o, final Class<T> valueType) throws JsonProcessingException {
        final XmlMapper XMLMapper = new XmlMapper();
        return XMLMapper.readValue(XMLMapper.writeValueAsString(o), valueType);
    }

    public static Map<String, Object> getMapFromXMLMap(final Map<String, Object> XMLMap, final String mapKey)
            throws IllegalXMLException {
        Object map = getNonNull(XMLMap, mapKey);
        if(map instanceof Map<?, ?>) { return (Map<String, Object>) map; }
        throw new IllegalXMLException("Неверный тип элемента " + angleBrc(mapKey) + '.');
    }

    public static ArrayList<Map<String, Object>> getArrayListFromXMLMap(final Map<String, Object> XMLMap,
            final String mapKey, final String arrayKey) throws IllegalXMLException {
        Object arrayList = getNonNull(getMapFromXMLMap(XMLMap, mapKey), arrayKey);
        if(arrayList instanceof ArrayList<?>) { return (ArrayList<Map<String, Object>>) arrayList; }
        if(arrayList instanceof Map<?, ?>) {
            ArrayList<Map<String, Object>> oneElementArrayList = new ArrayList<>();
            oneElementArrayList.add((Map<String, Object>) arrayList);
            return oneElementArrayList;
        } else { throw new IllegalXMLException("Неверный тип элемента " + angleBrc(arrayKey) + '.'); }
    }

    private static Object getNonNull(final Map<String, Object> XMLMap, final String key)
            throws IllegalXMLException {
        Object o = Objects.requireNonNull(XMLMap).get(key);
        if(o == null) { throw new IllegalXMLException("Отсутствует необходимый элемент " + angleBrc(key) + '.'); }
        return o;
    }

    public static String angleBrc(final String key) {
        return '<' + key + '>';
    }
}
