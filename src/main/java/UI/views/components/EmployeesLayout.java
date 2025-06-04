package UI.views.components;

import java.util.List;
import java.util.function.Consumer;
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

import Application.DTOs.EmployeeInfo;
import Application.DTOs.UserDTO;
import UI.views.dataobjects.UserPermission;

public class EmployeesLayout extends VerticalLayout {
    private final Button refreshButton;
    private final Button addUserButton;
    private final Grid<UserPermission> managersGrid;
    private final Grid<UserDTO> ownersGrid;
    private final Span managersCountSpan;
    private final Span ownersCountSpan;
    private final Supplier<EmployeeInfo> employeeInfoSupplier;


    public EmployeesLayout(Supplier<EmployeeInfo> employeeInfoSupplier,
                            Runnable onAddUser,
                            Consumer<UserPermission> onPermissionChange,
                            Consumer<UserDTO> ownerRemover) {
        H3 header = new H3("Store Employees");
        header.getStyle().setColor(" #ffffff");

        this.refreshButton = new Button("Refresh", VaadinIcon.REFRESH.create());
        this.addUserButton = new Button("Add", VaadinIcon.PLUS.create());

        styleButton(refreshButton, "rgb(103, 33, 243)");
        styleButton(addUserButton, " #4caf50");

        this.managersGrid = new Grid<>(UserPermission.class);

        managersCountSpan = new Span();
        managersCountSpan.getStyle()
            .set("font-weight", "bold")
            .set("margin-left", "1em")
            .setColor(" #ffffff");

        ownersCountSpan = new Span();
        ownersCountSpan.getStyle()
            .set("font-weight", "bold")
            .set("margin-left", "1em")
            .setColor(" #ffffff");
        this.employeeInfoSupplier = employeeInfoSupplier;

        // Configure the grid
        managersGrid.addColumn(u -> u.user.getUsername()).setHeader("Username");
        managersGrid.addColumn(u -> u.user.getEmail()).setHeader("Email");
        managersGrid.addComponentColumn(up -> {
            VerticalLayout permissionLayout = new VerticalLayout();
            if (up.permissions == null || up.permissions.isEmpty()) {
                permissionLayout.add("No permissions assigned");
            } else {
                Div detailsDiv = new Div();
                up.permissions.forEach(p -> {
                    Span permissioSpan = new Span("• " + p.toString());
                    permissioSpan.getStyle().set("display", "block");
                    detailsDiv.add(permissioSpan);
                });
                permissionLayout.add(detailsDiv);
            }
            return permissionLayout;
        }).setHeader("Permissions");

        managersGrid.addComponentColumn(up -> {
            Button changeRoleButton = new Button("Change Permissions", VaadinIcon.PENCIL.create());
            changeRoleButton.addClickListener(event -> onPermissionChange.accept(up));
            styleButton(changeRoleButton, "var(--lumo-primary-color)");
            return changeRoleButton;
        }).setHeader("Actions");
        managersGrid.setSizeFull();
        managersGrid.setHeightFull();
        managersGrid.setHeight("400px");



        ownersGrid = new Grid<>(UserDTO.class);

        ownersGrid.removeAllColumns();
        ownersGrid.addColumn(u -> u.getUsername()).setHeader("Username");
        ownersGrid.addColumn(u -> u.getEmail()).setHeader("Email");

        ownersGrid.addComponentColumn(up ->{
            Button removeOwnerButton = new Button("Remove Owner", VaadinIcon.TRASH.create());
            removeOwnerButton.addClickListener(event -> {
                ownerRemover.accept(up);
                refreshUsers(); // Refresh the grid after removing an owner
            });
            styleButton(removeOwnerButton, "rgb(181, 24, 24)");
            return removeOwnerButton;
        }).setHeader("Actions");
        ownersGrid.setSizeFull();
        ownersGrid.setHeight("400px");


        // Add components to the layout
        HorizontalLayout buttonLayout = new HorizontalLayout(refreshButton, addUserButton);
        buttonLayout.setJustifyContentMode(JustifyContentMode.BETWEEN);
        add(header, buttonLayout, managersGrid, managersCountSpan, ownersGrid, ownersCountSpan);
        setFlexGrow(1, header, buttonLayout, managersGrid, managersCountSpan, ownersGrid, ownersCountSpan); // This is key
        expand(managersGrid);

        refreshButton.addClickListener(event -> refreshUsers());
        addUserButton.addClickListener(event -> onAddUser.run());

        setSizeFull();
        setPadding(true);
        setSpacing(true);
    }

    public void refreshUsers() {
        EmployeeInfo users = employeeInfoSupplier.get();
        
        if (users != null) {
            List<UserPermission> managers = users.getManagers().entrySet().stream()
                .map(entry -> new UserPermission(entry.getKey(), entry.getValue()))
                .toList();
            managersGrid.setItems(managers);
            managersCountSpan.setText("Loaded " + managers.size() + " Managers");

            ownersGrid.setItems(users.getOwners());
            ownersCountSpan.setText("Loaded " + users.getOwners().size() + " Owners");
        } else {
            Notification.show("Failed to load users. Please try again later.");
            managersGrid.setItems(List.of()); // Clear grid if no users
            managersCountSpan.setText("Loaded 0 Managers");
            ownersGrid.setItems(List.of());
            ownersCountSpan.setText("Loaded 0 Owners");
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
    

