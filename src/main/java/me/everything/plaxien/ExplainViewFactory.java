    package me.everything.plaxien;

    import android.content.Context;
    import android.view.LayoutInflater;
    import android.view.View;
    import android.widget.ImageView;
    import android.widget.LinearLayout;
    import android.widget.TextView;

    /**
     * A factory that generates Explain views from Explain trees
     */
    public class ExplainViewFactory {
        LayoutInflater mInflater;

        Context mContext;

        public ExplainViewFactory(Context context) {
            mContext = context;
            mInflater = LayoutInflater.from(context);

        }

        /**
         * Get a "section" view for a multi-explain tree
         * @param tree the root node of the tree
         * @return a rendered view of the explain section
         */

        public View getSectionView(Explain.Node tree) {

            View ret = mInflater.inflate(R.layout.explain_view, null);
            ((TextView)ret.findViewById(R.id.section_title)).setText(tree.title);

            LinearLayout ll = (LinearLayout)ret.findViewById(R.id.secion_items);

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
         * @param node
         * @return
         */
        private View getValueView(Explain.ValueNode node) {
            View view = mInflater.inflate(R.layout.explain_value, null);
            ValueNodeHolder holder = new ValueNodeHolder(view);
            holder.titleView.setText(node.title);
            holder.valueView.setText(node.toString());

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
                itemsView = (LinearLayout) view.findViewById(R.id.node_items);
                icon = (ImageView) view.findViewById(R.id.node_icon);
                numChildrenView = (TextView)view.findViewById(R.id.node_num_children);
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
                valueView = (TextView) view.findViewById(R.id.vvalue);
            }
        }

    }
