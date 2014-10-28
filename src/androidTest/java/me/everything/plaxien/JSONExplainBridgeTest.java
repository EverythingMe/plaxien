package me.everything.plaxien;

import junit.framework.TestCase;

import me.everything.plaxien.Explain;
import me.everything.plaxien.json.JSONExplainBridge;

public class JSONExplainBridgeTest extends TestCase {


    public void testParseJSON() throws Exception {

        String rawJSON = "{\"Group 1\": {\"Entry 1\": {\"Key\": \"value\", \"Key2\": 3 ,\"Key3\": [1,2,\"foo\"],\"Key4\": null}}}";


        JSONExplainBridge bridge = new JSONExplainBridge();

        Explain.Node node = bridge.parseJSON(rawJSON, "Foo", true);

        assertTrue(node.size() > 0);

        assertTrue(node.title.equals("Foo"));

        assertTrue(node.children.get(0).title.equals("Group 1"));

        node = (Explain.Node)node.children.get(0);

        assertTrue(node.children.get(0).getType() == Explain.Node.NODE);

        assertEquals(((Explain.Node) node.children.get(0)).children.size(), 4);

        assertTrue(((Explain.Node) node.children.get(0)).children.get(0).title.equals("Key"));






    }
}

