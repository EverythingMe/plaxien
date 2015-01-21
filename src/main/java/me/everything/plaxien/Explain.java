package me.everything.plaxien;

import android.content.Intent;
import android.net.Uri;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

import java.lang.reflect.Type;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 * This is the main Explain API encapsulating the models and building explain trees
 */
public class Explain {


    /**
     * This is the base of all explain nodes - be it value or branch nodes
     */
    static class BaseNode {
        String title;
        final int type;
        final static int VALUE_NODE = 0;
        final static int NODE = 1;

        public String getTitle() {
            return title;
        }

        public int getType() {
            return type;
        }

        BaseNode(int type, String title) {
            this.type= type;
            this.title = title;
        }





    }

    /**
     * A branch node - i.e. a node that has children, whether further branches or value nodes.
     * Each node has a title and child nodes.
     * It can be expanded when first rendered, or collapsed. The default is collapsed
     */
    public static class Node extends  BaseNode {

        List<BaseNode> children;


        boolean expanded = false;

        /**
         * Constructor with a list of children
         * @param title the branch title
         * @param children a list of all the children. If null is passed, we init an empty list
         */
        public Node(String title, BaseNode[] children) {
            this(title, children, false);
        }

        /**
         * Constructor without children
         * @param title
         */
        public Node(String title, boolean expanded) {
            this(title, null, expanded);
        }

        /**
         * Constructor that allows setting pre-expanded nodes
         * @param title the branch title
         * @param children a list of all the children. If null is passed, we init an empty list
         * @param expanded whether this node should be expanded
         */
        public Node(String title, BaseNode[] children, boolean expanded) {
            super(NODE, title);

            this.children = new LinkedList<BaseNode>();
            if (children != null && children.length > 0) {
                Collections.addAll(this.children, children);
            }
            this.expanded = expanded;
        }

        public Node() {
            super(NODE, "");
        }

        /**
         * Add a child node to the node. This allows using a builder pattern for constructing trees
         * @param title the child's title
         * @param expanded whether the child should be expanded
         * @return the child node
         */
        public Node addChild(String title, boolean expanded) {
            Node ret = new Node(title, null, expanded);
            children.add(ret);
            return ret;
        }

        public Node addChild(Node node) {
            children.add(node);
            return node;
        }

        /**
         * Add a child node to the node *and keep it collapsed*. This allows using a builder pattern for constructing trees
         * @param title the child's title
         * This is identical to calling addChild with expanded=false
         */
        public Node addChild(String title) {
            return addChild(title, false);
        }

        /**
         * Add a value child node
         * @param name the value name
         * @param value the value itself. It can be anything, as long as it implements a sane toString()
         * @return the current node, so that you can append more children to it
         */
        public Node addValue(String name, Object value) {

            ValueNode ret = new ValueNode(name, value);
            children.add(ret);
            return this;
        }

        /**
         * Add a *nameless* value child node
         * @param value the value itself. It can be anything, as long as it implements a sane toString()
         * @return the current node, so that you can append more children to it
         */
        public Node addValue(Object value) {
            ValueNode ret = new ValueNode(value.toString(), "");
            children.add(ret);
            return this;
        }

        /**
         * Add a value child node
         * @param name the value name
         * @param value the value itself. It can be anything, as long as it implements a sane toString()
         * @param onClickUri a uri of an intent to be opened when the node is clicked
         * @return the current node, so that you can append more children to it
         */
        public Node addValue(String name, Object value, String onClickUri) {

            ValueNode ret = new ValueNode(name, value, onClickUri);
            children.add(ret);
            return this;
        }

        /**
         * Add a value child node
         * @param name the value name
         * @param value the value itself. It can be anything, as long as it implements a sane toString()
         * @param onClickIntent an intent to be started when the node is clicked
         * @return the current node, so that you can append more children to it
         */
        public Node addValue(String name, Object value, Intent onClickIntent) {

            ValueNode ret = new ValueNode(name, value, onClickIntent);
            children.add(ret);
            return this;
        }

        public int size() {
            return children.size();
        }


        /**
         * To an internal json representation
         * @return
         */
        String toJSON() {
            Gson gson = new Gson();
            String json = gson.toJson(this);

            return json;
        }

        /**
         * From an internal json representation.
         * DO NOT USE THIS FOR SERVER GENERATED JSONS, this is for internal serialization and dumping only
         * @param rawJSON
         * @return
         */
         static Node fromJSON(String rawJSON) {
            Gson gson = new GsonBuilder().registerTypeAdapter(BaseNode.class, new Deserializer()).create();

            try {
                Node ret =  gson.fromJson(rawJSON, Node.class);

                return ret;
            } catch (Exception e) {
                Log.e("Explain", "Could not parse raw JSON", e);
                return null;
            }

        }

    }



    /**
     * A value node holds a terminal key/value pair for display.
     * The value can be any object, and we use the object's toString() to render it
     * Optionally, the node can have an intent Uri to launch when the node is clicked.
     * You can pass a Uri to it, or an intent object that will be serialized to Uri
     */
    public static  class ValueNode extends BaseNode {
        Object value;

        public String onClickUri;

        public ValueNode() {
            super(VALUE_NODE, "");
        }


        /**
         * Constructor with a click uri
         * @param title the node's title
         * @param value the node's value
         * @param onClickUri an optional Uri to launch as an intent on click
         */
        public ValueNode(String title, Object value, String onClickUri) {
            super(VALUE_NODE, title);

            this.title = title;
            this.value = value;
            this.onClickUri = onClickUri;
        }


        public ValueNode(String title, Object value) {
            this(title, value, (String)null);
        }

        public ValueNode(String title, Object value, Intent onClickIntent) {
            this(title, value, onClickIntent.toUri(Intent.URI_INTENT_SCHEME));
        }





        public String toString() {
            return value != null ? value.toString() : "null";
        }
    }

    /** Internal deserializer to distinguish between value and branch nodes.*/
    static class Deserializer implements JsonDeserializer<BaseNode> {
        public BaseNode deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
                throws JsonParseException {
            if (!json.isJsonObject()) {
                return null;
            }
            JsonObject obj = json.getAsJsonObject();
            if (obj.get("type").getAsInt() == BaseNode.NODE) {
                return context.deserialize(json, Node.class);
            } else {
                return context.deserialize(json, ValueNode.class);
            }
        }
    }
}
