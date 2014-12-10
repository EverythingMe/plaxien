# Plaxien
## Let Your App Explain Itself Beautifully

Plaxien is a small libraries we use at EverythingMe, that allows us to create "Explains" - easy to read representations of internal algorithms and state of the app.

Basically it can represent hierarchical data composed of "nodes" and "key/value" pairs in a nice collapsible tree view. It looks something like this:

![Plaxien Exammple Screenshot](plaxien.png)

## Why did we create Plaxien?

The idea is simple - you want to be able to peek into your app's internal logic beyond logs.
It could be anything - why did a search result return? What's the metadata of the current user? etc.

That's why in debug builds of our app, we add Explain Views for different parts of our system (Smart Folders, Contextual Insights, Ads, In-phone search, etc).
Over the years different features in our product had solved this ad-hoc using a number of techniques,
but we wanted a simple, unified way to create those views.

Plaxien allows us to quickly create them, export the explain data to the servers, and even create these trees from arbitrary JSON trees.

## Example usage

The basic idea is that you build a tree of "Explain Nodes", that can have sub-nodes, or values - which are nodes with a key and a value.

Then you can either embed an `ExplainView` right in any activity, or launch an activity.

```java

        // First we create the root of the tree
        Explain.Node node = new Explain.Node("Exaplaining Plaxien", null);

        // Now we can add sub-nodes.
        node.addChild("Reasons to build it")
                // To sub nodes we can add values
                .addValue("Quick way to describe algorithms", true)
                .addValue("Being able to export the explains", true);

        // Repeat as much as you want and as deep as you want
        node.addChild("What we use it for")
                .addValue("Explain search results", "Yes!")
                .addValue("Explain ad serving", "Yep")
                .addValue("Debug context signals", "YEAH");


        // This is how you launch the activity shown in the image above
        ExplainActivity.explain(this, "My Explain", node, true);

        // Alternatively - this is how you embed the explain view in an activity
        ExplainViewFactory f = new ExplainViewFactory(this);
        someContainerView.addView(f.getSectionView(node));


```


## Adding Plaxien to your App

In Android Studio - just clone this project, import it into Android Studio, and add it as a dependency to your app.

