package me.everything.plaxien;

/**
 * This interface can be implemented by any class, for it to be able to explain itself
 * to an explain view factory
 */
public interface Explainer {
    public Explain.Node getExplain(Object ...args);

}
