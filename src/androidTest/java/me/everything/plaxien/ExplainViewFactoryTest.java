package me.everything.plaxien;

import android.test.InstrumentationTestCase;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;


public class ExplainViewFactoryTest extends InstrumentationTestCase {

    Explain.Node mNode;
    ExplainViewFactory mFactory;
    @Override
    public void setUp() {

        mNode = new Explain.Node("Test", null, true);
        mNode.addValue("Foo", "Bar").addChild("Child 1").addValue("Bar", "Baz").addChild("Child2");
        mFactory = new ExplainViewFactory(getInstrumentation().getContext());

    }

    private class MockExplainer implements Explainer {

        @Override
        public Explain.Node getExplain(Object... args) {
            return mNode;
        }
    }

    public void testGetSectionView() throws Exception {

        View sectionView = mFactory.getSectionView(mNode);
        assertNotNull(sectionView);
        assertEquals(sectionView.getClass(), LinearLayout.class);

        TextView tv = (TextView) sectionView.findViewById(R.id.section_title);
        assertEquals(tv.getText(), mNode.title);

        LinearLayout ll = (LinearLayout) sectionView.findViewById(R.id.section_items);
        assertEquals(ll.getChildCount(), 1);

    }

    public void testGetSectionView1() throws Exception {


        View sectionView = mFactory.getSectionView(new MockExplainer());
        assertNotNull(sectionView);
        assertEquals(sectionView.getClass(), LinearLayout.class);

        TextView tv = (TextView) sectionView.findViewById(R.id.section_title);
        assertEquals(tv.getText(), mNode.title);

        LinearLayout ll = (LinearLayout) sectionView.findViewById(R.id.section_items);
        assertEquals(ll.getChildCount(),1);
    }

    public void testGetExplainView() throws Exception {
        View sectionView = mFactory.getExplainView(new MockExplainer());
        assertNotNull(sectionView);
        assertEquals(sectionView.getClass(), LinearLayout.class);

        TextView tv = (TextView) sectionView.findViewById(R.id.node_title);
        assertEquals(tv.getText(), mNode.title);


        LinearLayout ll = (LinearLayout) sectionView.findViewById(R.id.node_items);
        assertEquals(ll.getChildCount(), mNode.size());
    }

    public void testGetNodeView() throws Exception {
        View sectionView = mFactory.getNodeView(mNode);
        assertNotNull(sectionView);
        assertEquals(sectionView.getClass(), LinearLayout.class);

        TextView tv = (TextView) sectionView.findViewById(R.id.node_title);
        assertEquals(tv.getText(), mNode.title);

        LinearLayout ll = (LinearLayout) sectionView.findViewById(R.id.node_items);
        assertEquals(ll.getChildCount(), mNode.size());
    }

    public void testJsonSerialization() {
        Explain.Node node = new Explain.Node("title", true);
        node.addChild("Child 1").addValue("Foo", "Bar");
        node.addChild("Child 2").addChild("Foo").addChild("gazi");

        String json = node.toJSON();
        assertFalse(json.isEmpty());

        node = Explain.Node.fromJSON(json);
        assertNotNull(node);
        View sectionView = mFactory.getNodeView(node);
        assertNotNull(sectionView);

    }


}