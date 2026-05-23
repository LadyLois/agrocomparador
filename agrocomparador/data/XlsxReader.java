package agrocomparador.data;

import java.util.*;
import java.util.zip.*;
import javax.xml.parsers.*;
import org.w3c.dom.*;

public class XlsxReader {

    public static List<String> readSharedStrings(ZipFile zip) throws Exception {
        List<String> strings = new ArrayList<>();
        ZipEntry entry = zip.getEntry("xl/sharedStrings.xml");
        if (entry == null) return strings;

        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(false);
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document doc = builder.parse(zip.getInputStream(entry));

        NodeList siNodes = doc.getElementsByTagName("si");
        for (int i = 0; i < siNodes.getLength(); i++) {
            Element si = (Element) siNodes.item(i);
            StringBuilder sb = new StringBuilder();
            NodeList tNodes = si.getElementsByTagName("t");
            for (int j = 0; j < tNodes.getLength(); j++) {
                sb.append(tNodes.item(j).getTextContent());
            }
            strings.add(sb.toString().trim());
        }
        return strings;
    }

    // Returns rowNumber -> (columnIndex -> cellValue), both 0-based
    public static Map<Integer, Map<Integer, String>> readSheet(ZipFile zip, String sheetPath, List<String> sharedStrings) throws Exception {
        Map<Integer, Map<Integer, String>> result = new TreeMap<>();
        ZipEntry entry = zip.getEntry(sheetPath);
        if (entry == null) return result;

        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(false);
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document doc = builder.parse(zip.getInputStream(entry));

        NodeList rows = doc.getElementsByTagName("row");
        for (int i = 0; i < rows.getLength(); i++) {
            Element row = (Element) rows.item(i);
            int rowNum = Integer.parseInt(row.getAttribute("r"));
            Map<Integer, String> rowData = new LinkedHashMap<>();

            NodeList cells = row.getElementsByTagName("c");
            for (int j = 0; j < cells.getLength(); j++) {
                Element cell = (Element) cells.item(j);
                String cellRef = cell.getAttribute("r");
                int colIndex = columnToIndex(cellRef.replaceAll("[0-9]", ""));
                String type = cell.getAttribute("t");

                NodeList vNodes = cell.getElementsByTagName("v");
                if (vNodes.getLength() == 0) continue;
                String rawValue = vNodes.item(0).getTextContent();

                String value;
                if ("s".equals(type)) {
                    int idx = Integer.parseInt(rawValue);
                    value = idx < sharedStrings.size() ? sharedStrings.get(idx) : "";
                } else {
                    value = rawValue;
                }

                if (!value.isEmpty()) {
                    rowData.put(colIndex, value);
                }
            }

            if (!rowData.isEmpty()) {
                result.put(rowNum, rowData);
            }
        }
        return result;
    }

    public static int columnToIndex(String col) {
        int result = 0;
        for (char c : col.toUpperCase().toCharArray()) {
            result = result * 26 + (c - 'A' + 1);
        }
        return result - 1;
    }
}
