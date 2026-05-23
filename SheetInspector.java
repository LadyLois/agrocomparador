import java.util.*;
import java.util.zip.*;
import java.io.*;
import javax.xml.parsers.*;
import org.w3c.dom.*;

public class SheetInspector {
    public static void main(String[] args) throws Exception {
        String path = "agrocomparador/excels/Informe Semanal de Coyuntura S-01.xlsx";

        try (ZipFile zip = new ZipFile(path)) {
            List<String> strings = new ArrayList<>();
            DocumentBuilderFactory f = DocumentBuilderFactory.newInstance();
            f.setNamespaceAware(false);
            ZipEntry ss = zip.getEntry("xl/sharedStrings.xml");
            Document doc = f.newDocumentBuilder().parse(zip.getInputStream(ss));
            NodeList si = doc.getElementsByTagName("si");
            for (int i = 0; i < si.getLength(); i++) {
                StringBuilder sb = new StringBuilder();
                NodeList t = ((Element)si.item(i)).getElementsByTagName("t");
                for (int j = 0; j < t.getLength(); j++) sb.append(t.item(j).getTextContent());
                strings.add(sb.toString().trim());
            }

            for (int sheetNum : new int[]{2, 3}) {
                System.out.println("\n=== SHEET " + sheetNum + " first 12 rows ===");
                ZipEntry entry = zip.getEntry("xl/worksheets/sheet" + sheetNum + ".xml");
                byte[] data = zip.getInputStream(entry).readAllBytes();
                Document sheetDoc = f.newDocumentBuilder().parse(new ByteArrayInputStream(data));

                NodeList rows = sheetDoc.getElementsByTagName("row");
                for (int i = 0; i < Math.min(rows.getLength(), 12); i++) {
                    Element row = (Element) rows.item(i);
                    int rowNum = Integer.parseInt(row.getAttribute("r"));
                    Map<String, String> cells = new LinkedHashMap<>();
                    NodeList cs = row.getElementsByTagName("c");
                    for (int j = 0; j < cs.getLength(); j++) {
                        Element cell = (Element) cs.item(j);
                        String ref = cell.getAttribute("r");
                        String type = cell.getAttribute("t");
                        NodeList v = cell.getElementsByTagName("v");
                        if (v.getLength() == 0) continue;
                        String raw = v.item(0).getTextContent();
                        String val = "s".equals(type) ? strings.get(Integer.parseInt(raw)) : raw;
                        cells.put(ref, val);
                    }
                    if (!cells.isEmpty()) System.out.println("Row " + rowNum + ": " + cells);
                }
            }
        }
    }
}
