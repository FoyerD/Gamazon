package UI.views.components;

import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;

import Application.DTOs.UserDTO;
import Domain.management.PermissionType;
import UI.views.dataobjects.UserPermission;

public class UserPermissionsLayout extends VerticalLayout {
    private final Button refreshButton;
    private final Button addUserButton;
    private final Grid<UserPermission> userGrid;
    protected final Supplier<Map<UserDTO, List<PermissionType>>> userPermissionSupplier;


    public UserPermissionsLayout(Supplier<Map<UserDTO, List<PermissionType>>> userPermissionSupplier, Runnable onAddUser) {
        H3 header = new H3("Store Managers");
        this.refreshButton = new Button("Refresh", VaadinIcon.REFRESH.create());
        this.addUserButton = new Button("Add", VaadinIcon.PLUS.create());

        styleButton(refreshButton, "rgb(103, 33, 243)");
        styleButton(addUserButton, " #4caf50");

        this.userGrid = new Grid<>(UserPermission.class);
        this.userPermissionSupplier = userPermissionSupplier;

        // Configure the grid
        userGrid.addColumn(u -> u.user.getUsername()).setHeader("Username");
        userGrid.addColumn(u -> u.user.getEmail()).setHeader("Email");
        userGrid.addComponentColumn(up -> {
            VerticalLayout permissionLayout = new VerticalLayout();
            if (up.permissions == null || up.permissions.isEmpty()) {
                permissionLayout.add("No permissions assigned");
            } else {
                Div detailsDiv = new Div();
                up.permissions.forEach(p -> {
                    Span permissioSpan = new Span(p.toString());
                    permissioSpan.getStyle().set("display", "block");
                    detailsDiv.add(permissioSpan);
                });
                permissionLayout.add(detailsDiv);
            }
            return permissionLayout;
        }).setHeader("Permissions");
        userGrid.setSizeFull();
        userGrid.setHeight("400px");

        // Add components to the layout
        HorizontalLayout buttonLayout = new HorizontalLayout(refreshButton, addUserButton);
        buttonLayout.setJustifyContentMode(JustifyContentMode.BETWEEN);
        add(header, buttonLayout, userGrid);
        expand(userGrid);

        refreshButton.addClickListener(event -> refreshUsers());
        addUserButton.addClickListener(event -> onAddUser.run());
        // Set layout properties
        setSizeFull();
        setHeightFull();
        setPadding(true);
        setSpacing(true);
    }

    public void refreshUsers() {
        Map<UserDTO, List<PermissionType>> users = userPermissionSupplier.get();
        if (users != null) {
            this.add(users.entrySet().size() + " users loaded.");
            userGrid.setItems(users.entrySet().stream()
                .map(entry -> new UserPermission(entry.getKey(), entry.getValue()))
                .toList());
        } else {
            Notification.show("Failed to load users. Please try again later.");
            userGrid.setItems(List.of()); // Clear grid if no users
        }
    }

    private void styleButton(Button button, String color) {
        button.getStyle()
            .set("background-color", color)
            .set("color", "white")
            .set("border-radius", "25px")
            .set("font-weight", "500")
            .set("padding", "0.5em 1.5em")
            .set("cursor", "pointer")
            .set("transition", "transform 0.2s, box-shadow 0.2s")
            .set("box-shadow", "0 2px 5px rgba(0,0,0,0.2)");

        // Add hover effect
        button.getElement().addEventListener("mouseover", e -> 
            button.getStyle()
                .set("transform", "translateY(-2px)")
                .set("box-shadow", "0 4px 10px rgba(0,0,0,0.3)"));

        button.getElement().addEventListener("mouseout", e -> 
            button.getStyle()
                .set("transform", "translateY(0)")
                .set("box-shadow", "0 2px 5px rgba(0,0,0,0.2)"));
    }

}
    

