package com.marcos.fractalstudio.presentation.navigation;

import javafx.scene.Group;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class ViewRegistryTest {

    @Test
    void createsRegisteredViewAndReturnsSpec() {
        ViewRegistry viewRegistry = new ViewRegistry();
        RouteSpec routeSpec = new RouteSpec(Route.STUDIO, "Studio Workspace", "Studio");
        viewRegistry.register(routeSpec, Group::new);

        assertEquals(routeSpec, viewRegistry.routeSpec(Route.STUDIO));
        assertTrue(viewRegistry.create(Route.STUDIO) instanceof Group);
    }
}
