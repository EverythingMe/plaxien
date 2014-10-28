package me.everything.plaxien.json;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import me.everything.plaxien.Explain;
import me.everything.plaxien.Explainer;


/**
 * This bridge class enables you to parse JSON objects and render explain trees from them.
 * This is useful when you want to render explains from servers, or from legacy code.
 *
 * We assume that dictionaries are always "Nodes", and everything else is rendered as values
 *
 */
public class JSONExplainBridge {



    JsonParser mParser;

    public JSONExplainBridge() {

        mParser = new JsonParser();
    }



    void parseArray(JsonArray arr, Explain.Node node) {


        for (int i =0; i < arr.size(); i ++) {

            JsonElement elem = arr.get(i);

            if (elem.isJsonObject()) {
                Explain.Node child = node.addChild(String.format("[%d]", i+1));
                parseMap(elem.getAsJsonObject(), child);

            } else if(elem.isJsonArray()) {
                Explain.Node child = node.addChild(String.format("[%d]", i+1));
                parseArray(elem.getAsJsonArray(), child);
            } else {

                node.addValue(elem.toString());
            }
        }
    }

    /**
     * Internal function - parse a json map object into an explain node, recursively
     * @param obj the map boject
     * @param node the node representing the map. Each entry in the json map is rendered as a child
     *             of that node
     */

    void parseMap(JsonObject obj, Explain.Node node) {


        for (Map.Entry<String, JsonElement> entry : obj.entrySet()) {
            String key = entry.getKey();
            JsonElement value = entry.getValue();

            if (value.isJsonObject()) {
                Explain.Node child = node.addChild(key);
                parseMap(value.getAsJsonObject(), child);

            } else if(value.isJsonArray()) {
                Explain.Node child = node.addChild(key);
                parseArray(value.getAsJsonArray(), child);
            }  else {

                node.addValue(key, value.toString());
            }
        }
    }


    /**
     * Parse a json string into an Explain tree, with the root being a node with the given title
     * @param rawJson the JSON representing the tree
     * @param title The title of the tree's root node
     * @param expanded whether the tree root should be expanded
     * @return an explain tree node
     */
    public Explain.Node parseJSON(String rawJson, String title, boolean expanded) {


        JsonElement root = mParser.parse(rawJson);

        if (!(root.isJsonObject())) {
            throw new RuntimeException("Invalid JSON: Root must be a dictionary");
        }

        Explain.Node tree = new Explain.Node(title, null, expanded);
        parseMap(root.getAsJsonObject(), tree);

        return tree;


    }



}
