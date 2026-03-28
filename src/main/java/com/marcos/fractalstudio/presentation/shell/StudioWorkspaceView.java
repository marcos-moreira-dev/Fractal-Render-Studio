package com.marcos.fractalstudio.presentation.shell;

import com.marcos.fractalstudio.presentation.dialogs.BookmarkLabelDialog;
import com.marcos.fractalstudio.presentation.dialogs.KeyframeLabelDialog;
import com.marcos.fractalstudio.presentation.explorer.FractalExplorerView;
import com.marcos.fractalstudio.presentation.inspector.InspectorView;
import com.marcos.fractalstudio.presentation.renderqueue.RenderQueueView;
import com.marcos.fractalstudio.presentation.timeline.TimelineView;

import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TreeCell;
import javafx.scene.image.ImageView;
import javafx.scene.control.SplitPane;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TreeView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

public final class StudioWorkspaceView extends BorderPane {

    private final BookmarkLabelDialog bookmarkLabelDialog = new BookmarkLabelDialog();
    private final KeyframeLabelDialog keyframeLabelDialog = new KeyframeLabelDialog();

    public StudioWorkspaceView(StudioShellViewModel viewModel) {
        getStyleClass().add("workspace-root");
        setPadding(new Insets(12.0, 12.0, 0.0, 12.0));
        BooleanProperty bottomPanelVisible = viewModel.workspaceDrawerVisibleProperty();

        TreeView<SidebarTreeNode> projectTree = new TreeView<>();
        projectTree.rootProperty().bind(viewModel.projectTreeRootProperty());
        projectTree.setShowRoot(true);
        projectTree.getStyleClass().add("project-tree");
        projectTree.setCellFactory(ignored -> new TreeCell<>() {
            private final Label label = new Label();
            private final ImageView thumbnailView = new ImageView();
            private final HBox bookmarkGraphic = new HBox(10.0);
            {
                label.setEllipsisString("...");
                label.setWrapText(true);
                label.setMaxWidth(Double.MAX_VALUE);
                label.prefWidthProperty().bind(projectTree.widthProperty().subtract(52.0));
                thumbnailView.setFitWidth(58.0);
                thumbnailView.setFitHeight(34.0);
                thumbnailView.setPreserveRatio(true);
                thumbnailView.getStyleClass().add("sidebar-bookmark-thumbnail");
                bookmarkGraphic.setAlignment(Pos.CENTER_LEFT);
                bookmarkGraphic.getChildren().addAll(thumbnailView, label);
            }

            @Override
            protected void updateItem(SidebarTreeNode item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setGraphic(null);
                    setText(null);
                    setContextMenu(null);
                    return;
                }
                label.setText(item.label());
                thumbnailView.imageProperty().unbind();
                if (item.kind() == SidebarTreeNode.Kind.BOOKMARK && item.referenceId() != null) {
                    thumbnailView.imageProperty().bind(viewModel.bookmarkThumbnailProperty(item.referenceId()));
                    setGraphic(bookmarkGraphic);
                } else {
                    setGraphic(label);
                }
                setText(null);
                setContextMenu(buildNodeContextMenu(viewModel, item));
            }
        });
        projectTree.getSelectionModel().selectedItemProperty().addListener((ignored, oldItem, newItem) -> {
            if (newItem == null || newItem.getValue() == null) {
                return;
            }
            SidebarTreeNode selectedNode = newItem.getValue();
            if (selectedNode.kind() == SidebarTreeNode.Kind.KEYFRAME && selectedNode.referenceId() != null) {
                viewModel.focusKeyframe(selectedNode.referenceId());
                return;
            }
            if (selectedNode.kind() == SidebarTreeNode.Kind.BOOKMARK && selectedNode.referenceId() != null) {
                viewModel.focusBookmark(selectedNode.referenceId());
            }
        });
        VBox.setVgrow(projectTree, Priority.ALWAYS);

        Label sidebarTitle = new Label("Proyecto");
        sidebarTitle.getStyleClass().add("panel-title");
        Label sidebarSubtitle = new Label();
        sidebarSubtitle.textProperty().bind(
                viewModel.bookmarkSummaryProperty()
                        .concat("  |  ")
                        .concat(viewModel.projectDefaultFpsProperty())
        );
        sidebarSubtitle.getStyleClass().add("panel-subtitle");

        VBox projectSidebar = new VBox(10.0, sidebarTitle, sidebarSubtitle, projectTree);
        projectSidebar.getStyleClass().addAll("panel", "project-sidebar");
        projectSidebar.setPadding(new Insets(16.0));
        projectSidebar.setPrefWidth(286.0);
        projectSidebar.setMinWidth(286.0);
        projectSidebar.setMaxWidth(286.0);

        FractalExplorerView explorerView = new FractalExplorerView(
                viewModel,
                bottomPanelVisible,
                viewModel::togglePointsDrawer
        );
        explorerView.setMinWidth(420.0);
        InspectorView inspectorView = new InspectorView(viewModel);
        inspectorView.setPrefWidth(300.0);
        inspectorView.setMinWidth(300.0);
        inspectorView.setMaxWidth(300.0);

        SplitPane mainSplitPane = new SplitPane(projectSidebar, explorerView, inspectorView);
        mainSplitPane.setDividerPositions(0.20, 0.77);
        SplitPane.setResizableWithParent(projectSidebar, false);
        SplitPane.setResizableWithParent(inspectorView, false);
        StackPane workspaceStack = new StackPane();
        workspaceStack.getChildren().add(mainSplitPane);
        setCenter(workspaceStack);
        Platform.runLater(() -> mainSplitPane.setDividerPositions(0.20, 0.77));

        TimelineView timelineView = new TimelineView(viewModel);
        RenderQueueView renderQueueView = new RenderQueueView(viewModel);

        TabPane bottomTabs = new TabPane(
                createTab("Timeline", timelineView),
                createTab("Render Queue", renderQueueView)
        );
        bottomTabs.visibleProperty().bind(bottomPanelVisible);
        bottomTabs.managedProperty().bind(bottomPanelVisible);
        bottomTabs.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);
        bottomTabs.setPrefHeight(300.0);
        bottomTabs.setMinHeight(260.0);

        Label drawerTitle = new Label("Puntos y Render Queue");
        drawerTitle.getStyleClass().add("panel-title");
        Label drawerHint = new Label("Panel temporal para editar puntos y seguir el render. El viewport principal no se redimensiona.");
        drawerHint.getStyleClass().add("panel-subtitle");
        drawerHint.setWrapText(true);
        drawerHint.setMaxWidth(Double.MAX_VALUE);

        Region drawerSpacer = new Region();
        HBox.setHgrow(drawerSpacer, Priority.ALWAYS);
        HBox.setHgrow(drawerHint, Priority.ALWAYS);

        javafx.scene.control.Button closeDrawerButton = new javafx.scene.control.Button("Ocultar");
        closeDrawerButton.getStyleClass().add("drawer-close-button");
        closeDrawerButton.setOnAction(event -> viewModel.closeWorkspaceDrawer());

        HBox drawerHeader = new HBox(12.0, drawerTitle, drawerHint, drawerSpacer, closeDrawerButton);
        drawerHeader.setAlignment(Pos.CENTER_LEFT);

        VBox bottomDrawer = new VBox(10.0, drawerHeader, bottomTabs);
        bottomDrawer.getStyleClass().add("workspace-drawer");
        bottomDrawer.setPadding(new Insets(14.0, 16.0, 16.0, 16.0));
        bottomDrawer.setFillWidth(true);
        bottomDrawer.setMaxWidth(Double.MAX_VALUE);
        bottomDrawer.setMaxHeight(340.0);
        bottomDrawer.visibleProperty().bind(bottomPanelVisible);
        bottomDrawer.managedProperty().bind(bottomPanelVisible);

        workspaceStack.getChildren().add(bottomDrawer);
        StackPane.setAlignment(bottomDrawer, Pos.BOTTOM_CENTER);
        StackPane.setMargin(bottomDrawer, new Insets(0.0, 12.0, 0.0, 12.0));
        viewModel.workspaceDrawerTabProperty().addListener((ignored, oldTab, newTab) -> {
            if (newTab == null) {
                return;
            }
            if (newTab == WorkspaceDrawerTab.RENDER_QUEUE) {
                bottomTabs.getSelectionModel().select(1);
                return;
            }
            bottomTabs.getSelectionModel().select(0);
        });
    }

    private Tab createTab(String title, javafx.scene.Node content) {
        Tab tab = new Tab(title);
        tab.setContent(content);
        return tab;
    }

    private ContextMenu buildNodeContextMenu(StudioShellViewModel viewModel, SidebarTreeNode item) {
        if (item.kind() == SidebarTreeNode.Kind.BOOKMARK && item.referenceId() != null) {
            MenuItem goToBookmark = new MenuItem("Ir al punto");
            goToBookmark.setOnAction(event -> viewModel.focusBookmark(item.referenceId()));

            MenuItem renameBookmark = new MenuItem("Renombrar punto");
            renameBookmark.setOnAction(event -> bookmarkLabelDialog.show(
                    getScene() == null ? null : getScene().getWindow(),
                    viewModel.currentBookmarkLabel(item.referenceId())
            ).ifPresent(newLabel -> viewModel.renameBookmark(item.referenceId(), newLabel)));

            MenuItem createKeyframe = new MenuItem("Agregar al timeline");
            createKeyframe.setOnAction(event -> viewModel.createKeyframeFromBookmark(item.referenceId()));

            MenuItem moveUpBookmark = new MenuItem("Mover arriba");
            moveUpBookmark.setOnAction(event -> viewModel.moveBookmark(item.referenceId(), -1));

            MenuItem moveDownBookmark = new MenuItem("Mover abajo");
            moveDownBookmark.setOnAction(event -> viewModel.moveBookmark(item.referenceId(), 1));

            MenuItem deleteBookmark = new MenuItem("Eliminar punto");
            deleteBookmark.setOnAction(event -> viewModel.deleteBookmark(item.referenceId()));

            return new ContextMenu(
                    goToBookmark,
                    renameBookmark,
                    createKeyframe,
                    moveUpBookmark,
                    moveDownBookmark,
                    deleteBookmark
            );
        }
        if (item.kind() == SidebarTreeNode.Kind.KEYFRAME && item.referenceId() != null) {
            MenuItem goToKeyframe = new MenuItem("Ir al punto");
            goToKeyframe.setOnAction(event -> viewModel.focusKeyframe(item.referenceId()));
            MenuItem renameKeyframe = new MenuItem("Renombrar punto");
            renameKeyframe.setOnAction(event -> keyframeLabelDialog.show(
                    getScene() == null ? null : getScene().getWindow(),
                    viewModel.currentKeyframeLabel(item.referenceId())
            ).ifPresent(newLabel -> viewModel.renameKeyframe(item.referenceId(), newLabel)));
            MenuItem deleteKeyframe = new MenuItem("Eliminar punto");
            deleteKeyframe.setOnAction(event -> viewModel.deleteKeyframe(item.referenceId()));
            return new ContextMenu(goToKeyframe, renameKeyframe, deleteKeyframe);
        }
        return null;
    }
}
