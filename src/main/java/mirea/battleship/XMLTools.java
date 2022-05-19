package mirea.battleship;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.fasterxml.jackson.dataformat.xml.ser.ToXmlGenerator;
import mirea.battleship.Backend.Battle;
import mirea.battleship.Backend.BattleSet;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class XMLTools {
    public static void saveBattleSet(final BattleSet battleSet) {
        final XmlMapper outXMLMapper = new XmlMapper();
        outXMLMapper.configure(ToXmlGenerator.Feature.WRITE_XML_DECLARATION, true);
        try {
            outXMLMapper.writerWithDefaultPrettyPrinter().writeValue(new File(Settings.BATTLE_SET_OUT_FILE_PATH),
                    battleSet);
        } catch(IOException e) {
            e.printStackTrace();
        }
    }

    public static BattleSet getBattleSet() throws IOException {
        return new XmlMapper().readValue(inputStreamToString(Settings.BATTLE_SET_IN_FILE_PATH), BattleSet.class);
    }

    public static void saveBattle(final Battle battle) {
        if(battle != null) {
            final XmlMapper outXMLMapper = new XmlMapper();
            outXMLMapper.configure(ToXmlGenerator.Feature.WRITE_XML_DECLARATION, true);
            try {
                outXMLMapper.writerWithDefaultPrettyPrinter().withRootName("Battle").writeValue(
                        new File(Settings.BATTLE_OUT_FILE_PATH), battle.getXMLMap());
            } catch(IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static Battle getBattle() throws IOException, IllegalXMLException {
        return new Battle(new XmlMapper().readValue(inputStreamToString(Settings.BATTLE_IN_FILE_PATH), Map.class));
    }

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

    private static String inputStreamToString(final String fileName) throws IOException {
        StringBuilder sb = new StringBuilder();
        String line;
        BufferedReader br = new BufferedReader(
                new InputStreamReader(new FileInputStream(fileName), StandardCharsets.UTF_8));
        while((line = br.readLine()) != null) {
            sb.append(line).append('\n');
        }
        br.close();
        return sb.toString();
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

    private static Object getNonNull(final Map<String, Object> XMLMap, final String key) throws IllegalXMLException {
        Object o = Objects.requireNonNull(XMLMap).get(key);
        if(o == null) { throw new IllegalXMLException("Отсутствует необходимый элемент " + angleBrc(key) + '.'); }
        return o;
    }

    public static String angleBrc(final String key) {
        return '<' + key + '>';
    }
}
