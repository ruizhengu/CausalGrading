package org.example.nested;

public class Playground {
    static Entity entity;

    public static void main(String[] args) {
        createEntity();
        editEntity("Edit");
    }

    public static void createEntity() {
        entity = new Entity("New");
    }

    public static void editEntity(String newName) {
        entity.setName(newName);
    }
}
