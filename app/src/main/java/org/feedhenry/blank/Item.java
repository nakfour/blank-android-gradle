package org.feedhenry.blank;


public class Item {
    String name;
    String id;

    public Item(String name, String id)
    {
        this.name=name;
        this.id=id;
    }

    public String getId() {

        return this.id;
    }

    public String getName() {
        return this.name;
    }
}
