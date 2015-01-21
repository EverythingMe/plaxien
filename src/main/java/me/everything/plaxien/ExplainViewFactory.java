    package me.everything.plaxien;

    import android.content.Context;
    import android.content.Intent;
    import android.util.Log;
    import android.view.LayoutInflater;
    import android.view.View;
    import android.widget.ImageView;
    import android.widget.LinearLayout;
    import android.widget.TextView;

    import java.net.URISyntaxException;

    /**
     * A factory that generates Explain views from Explain trees
     */
    public class ExplainViewFactory {
        LayoutInflater mInflater;
        Context mContext;
        ExplainViewStyle mExplainViewStyle = new Builder().build();

        public ExplainViewFactory(Context context) {
            mContext = context;
            mInflater = LayoutInflater.from(context);

        }

        public ExplainViewFactory(Context context, ExplainViewStyle viewStyle) {
            this(context);
            mExplainViewStyle = viewStyle;
        }

        /**
         * Get a "section" view for a multi-explain tree
         * @param tree the root node of the tree
         * @return a rendered view of the explain section
         */

        public View getSectionView(Explain.Node tree) {

            View ret = mInflater.inflate(R.layout.explain_view, null);
            TextView titleTextView = (TextView)ret.findViewById(R.id.section_title);
            mExplainViewStyle.applyTitleStyle(titleTextView);
            titleTextView.setText(tree.title);

            LinearLayout ll = (LinearLayout)ret.findViewById(R.id.section_items);

            for (Explain.BaseNode root : tree.children) {

                if (root.type != Explain.Node.NODE) {
                    continue;
                }
                View v = getNodeView((Explain.Node)root);
                if (v != null) {
                    ll.addView(v);
                }
            }

            return ret;


        }

        /**
         * Get a section view by calling an explainer's explain call
         * @param explainer an explainer implementor
         * @return a "Section" view
         */
        public View getSectionView(Explainer explainer) {
            Explain.Node node = explainer.getExplain();
            if (node == null) {
                return null;
            }

            return getSectionView(node);
        }


        /**
         * Get a plain explain view by calling an explainer's explain call
         * @param explainer an explainer implementor
         * @return an "Explain" node view
         */
        public View getExplainView(Explainer explainer) {

            Explain.Node node = explainer.getExplain();
            if (node == null) {
                return null;
            }

            return getNodeView(node);
        }



        /**
         * Get the tree view for a single node.
         * @param node The node holding the data
         * @return a Node view
         */
        public View getNodeView(Explain.Node node) {
            View view = mInflater.inflate(R.layout.explain_node, null);
            if (view == null) {
                return null;
            }
            final NodeHolder holder = new NodeHolder(view, node);

            holder.titleView.setText(node.title);
            holder.numChildrenView.setText(String.valueOf(node.children.size()));


            // Hide not-expanded nodes
            if (!node.expanded) {
                holder.toggle();
            } else {
                holder.renderChildren();
            }


            holder.header.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    holder.toggle();
                }
            });

            return view;

        }

        /**
         * A selective function for a base node, rendering according to its type
         * @param node a child or branch node to be rendered
         * @return the rendered view
         */

        View getView(Explain.BaseNode node) {

            if (node.type == Explain.BaseNode.NODE) {
                return getNodeView((Explain.Node) node);
            } else if (node.type == Explain.BaseNode.VALUE_NODE) {
                return getValueView((Explain.ValueNode) node);
            }
            return null;
        }


        /**
         * Render a value view for a value node
         */
        private View getValueView(Explain.ValueNode node) {
            View view = mInflater.inflate(R.layout.explain_value, null);
            ValueNodeHolder holder = new ValueNodeHolder(view);
            holder.titleView.setText(node.title);
            holder.valueView.setText(node.toString());

            // If this node has a click intent uri, we try to recreate the intent and then start an activity from it
            if (node.onClickUri != null) {

                try {
                    final Intent i = Intent.parseUri(node.onClickUri, 0);

                    View.OnClickListener listener = new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            mContext.startActivity(i);
                        }
                    };

                    holder.valueView.setOnClickListener(listener);
                    holder.titleView.setOnClickListener(listener);
                    view.setOnClickListener(listener);

                } catch (URISyntaxException e) {
                    Log.e("ExplainViewFactory", "Error parsing intent uri: " + node.onClickUri, e);
                }
            }

            return view;
        }





        class NodeHolder {
            TextView titleView;
            LinearLayout itemsView;
            ImageView icon;
            TextView numChildrenView;
            View header;
            Explain.Node mNode;


            public NodeHolder(View view, Explain.Node node) {
                mNode = node;
                titleView = (TextView) view.findViewById(R.id.node_title);
                mExplainViewStyle.applyNodeNameStyle(titleView);
                itemsView = (LinearLayout) view.findViewById(R.id.node_items);
                icon = (ImageView) view.findViewById(R.id.node_icon);
                numChildrenView = (TextView)view.findViewById(R.id.node_num_children);
                mExplainViewStyle.applyNodeCounterStyle(numChildrenView);
                header = view.findViewById(R.id.node_header);
            }

            /**
             * Toggle visible/invisible
             */
            public void toggle() {

                    if (itemsView.getVisibility() == View.VISIBLE) {
                        icon.setImageResource(R.drawable.arrow_down);
                        itemsView.setVisibility(View.GONE);
                    } else {

                        if (itemsView.getChildCount() == 0) {
                            renderChildren();
                        }

                        icon.setImageResource(R.drawable.arrow_up);
                        itemsView.setVisibility(View.VISIBLE);

                    }
            }

            /**
             * Renders the node's children lazily - only when the node is opened
             */
            public void renderChildren() {
                // Recursively render the children
                if (mNode.children != null) {
                    for (Explain.BaseNode child : mNode.children) {

                        View childView = getView(child);
                        if (childView != null) {
                            itemsView.addView(childView);
                        }

                    }
                }
            }


        }


        class ValueNodeHolder {
            TextView titleView;
            TextView valueView;

            public ValueNodeHolder(View view) {
                titleView = (TextView) view.findViewById(R.id.vname);
                mExplainViewStyle.applyValueNameStyle(titleView);
                valueView = (TextView) view.findViewById(R.id.vvalue);
                mExplainViewStyle.applyValueStyle(valueView);
            }
        }

        /**
         * The customization styling of Explain view
         */
        public static class ExplainViewStyle {

            private Builder mBuilder;

            ExplainViewStyle(Builder builder) {
                mBuilder = builder;
            }

            public void applyTitleStyle(TextView view) {
                view.setTextAppearance(view.getContext(), mBuilder.titleStyle);
            }

            public void applyNodeNameStyle(TextView view) {
                view.setTextAppearance(view.getContext(), mBuilder.nodeNameStyle);
            }

            public void applyNodeCounterStyle(TextView view) {
                view.setTextAppearance(view.getContext(), mBuilder.nodeCounterStyle);
            }

            public void applyValueStyle(TextView view) {
                view.setTextAppearance(view.getContext(), mBuilder.valueStyle);
            }

            public void applyValueNameStyle(TextView view) {
                view.setTextAppearance(view.getContext(), mBuilder.valueNameStyle);
            }
        }

        public static class Builder {

            int titleStyle = R.style.PlaxienTitle;
            int nodeNameStyle = R.style.PlaxienNodeName;
            int nodeCounterStyle = R.style.PlaxienNodeCounter;
            int valueNameStyle = R.style.PlaxienValueName;
            int valueStyle = R.style.PlaxienValue;

            /**
             * Set the style resource of the title text
             * @param style The style resource
             * @return {@link me.everything.plaxien.ExplainViewFactory.Builder}
             */
            public Builder setTitleStyle(int style) {
                titleStyle = style;
                return this;
            }

            /**
             * Set the style resource of the node name
             * @param style The style resource
             * @return {@link me.everything.plaxien.ExplainViewFactory.Builder}
             */
            public Builder setNodeNameStyle(int style) {
                nodeNameStyle = style;
                return this;
            }

            /**
             * Set the style resource of the counter text
             * @param style The style resource
             * @return {@link me.everything.plaxien.ExplainViewFactory.Builder}
             */
            public Builder setNodeCounterStyle(int style) {
                nodeCounterStyle = style;
                return this;
            }

            /**
             * Set the style resource of the value name
             * @param style The style resource
             * @return {@link me.everything.plaxien.ExplainViewFactory.Builder}
             */
            public Builder setValueNameStyle(int style) {
                valueNameStyle = style;
                return this;
            }

            /**
             * Set the style of value
             * @param style The style resource
             * @return {@link me.everything.plaxien.ExplainViewFactory.Builder}
             */
            public Builder setValueStyle(int style) {
                valueStyle = style;
                return this;
            }

            /**
             * Build the view style
             * @return @return {@link me.everything.plaxien.ExplainViewFactory.ExplainViewStyle}
             */
            public ExplainViewStyle build() {
                return new ExplainViewStyle(this);
            }

        }

    }
