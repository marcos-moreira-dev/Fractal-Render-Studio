package com.marcos.fractalstudio.presentation.navigation;

import javafx.scene.Node;

import java.util.EnumMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.Supplier;

public final class ViewRegistry {

    private final Map<Route, RouteSpec> routeSpecs = new EnumMap<>(Route.class);
    private final Map<Route, Supplier<Node>> viewSuppliers = new EnumMap<>(Route.class);

    public void register(RouteSpec routeSpec, Supplier<Node> viewSupplier) {
        Objects.requireNonNull(routeSpec, "Route spec is required.");
        Objects.requireNonNull(viewSupplier, "View supplier is required.");
        routeSpecs.put(routeSpec.route(), routeSpec);
        viewSuppliers.put(routeSpec.route(), viewSupplier);
    }

    public Node create(Route route) {
        Supplier<Node> supplier = viewSuppliers.get(route);
        if (supplier == null) {
            throw new IllegalArgumentException("No view registered for route " + route);
        }
        return supplier.get();
    }

    public RouteSpec routeSpec(Route route) {
        RouteSpec routeSpec = routeSpecs.get(route);
        if (routeSpec == null) {
            throw new IllegalArgumentException("No route spec registered for route " + route);
        }
        return routeSpec;
    }
}
