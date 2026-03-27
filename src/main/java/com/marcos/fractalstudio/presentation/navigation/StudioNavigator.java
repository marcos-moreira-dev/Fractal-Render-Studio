package com.marcos.fractalstudio.presentation.navigation;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.SimpleObjectProperty;

public final class StudioNavigator {

    private final ObjectProperty<Route> activeRoute = new SimpleObjectProperty<>(Route.STUDIO);

    public ReadOnlyObjectProperty<Route> activeRouteProperty() {
        return activeRoute;
    }

    public void navigate(Route route) {
        activeRoute.set(route);
    }

    public Route activeRoute() {
        return activeRoute.get();
    }
}
