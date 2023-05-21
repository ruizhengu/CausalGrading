package org.example;

import org.example.nested.Entity;
import org.example.nested.Playground;
import org.junit.jupiter.api.*;

@DisplayName("ConfoundingEntity")
public class TestEntity {


    @Test
    void createEntity() {
        Playground.createEntity();
    }

    @Test
    void editEntity() {
        Playground.editEntity("Test");
    }

//    @Nested
//    @DisplayName("Handling Confounding")
//    class ConfoundingEditEntity {
//        @BeforeEach
//        void createEntity() {
//            entity = new Entity("New");
//        }
//
//        @Test
//        void editEntity() {
//            entity.setName("Edit");
//            Assertions.assertEquals(entity.getName(), "Edit");
//        }
//    }
}
