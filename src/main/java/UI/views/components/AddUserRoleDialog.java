package UI.views.components;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Supplier;


import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.combobox.MultiSelectComboBox;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.FlexComponent.JustifyContentMode;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.tabs.Tab;
import com.vaadin.flow.component.tabs.Tabs;

import Application.DTOs.UserDTO;
import Domain.management.PermissionFactory;
import Domain.management.PermissionType;
import UI.views.dataobjects.UserPermission;

public class AddUserRoleDialog extends Dialog {
    private final ComboBox<UserDTO> candidateSelect;
    private final MultiSelectComboBox<PermissionType> permissionsSelect;
    private final Supplier<List<UserDTO>> managementCandidatesSupplier;
    private final Supplier<List<UserDTO>> ownershipCandidatesSupplier;
    /**
     * Function to handle the addition of a user with their permissions.
     * Returns true if the user permissions was successfully added, false otherwise.
     */
    private final Function<UserPermission, Boolean> onAddManager;
    private final Function<UserDTO, Boolean> onAddOwner;
    private final VerticalLayout dialogLayout = new VerticalLayout();

    
    public AddUserRoleDialog(Supplier<List<UserDTO>> managementCandidatesSupplier, 
                            Supplier<List<UserDTO>> ownershipCandidatesSupplier,
                            Function<UserPermission, Boolean> onAddManager,
                            Function<UserDTO, Boolean> onAddOwner) {

        this.managementCandidatesSupplier = managementCandidatesSupplier;
        this.ownershipCandidatesSupplier = ownershipCandidatesSupplier;
        this.onAddManager = onAddManager;
        this.onAddOwner = onAddOwner;

        dialogLayout.setSpacing(true);
        dialogLayout.setPadding(true);

        candidateSelect = new ComboBox<>("Select User");
        candidateSelect.setItemLabelGenerator(UserDTO::getUsername);
        permissionsSelect = new MultiSelectComboBox<>("Initial Permissions");
        permissionsSelect.setItemLabelGenerator(PermissionType::toString);
        permissionsSelect.setWidth("100%");
        permissionsSelect.setHelperText("Select initial permissions for the manager");
        permissionsSelect.setItems(PermissionFactory.AVAILABLE_MANAGER_PERMISSIONS);

        VerticalLayout additionalInfo = new VerticalLayout();
        

        Button saveButton = new Button("Save", e -> {
            UserDTO selectedUser = candidateSelect.getValue();
            if (selectedUser == null) {

                Notification.show("Please Choose a user");
            } else {
                Set<PermissionType> initialPermissions = permissionsSelect.getSelectedItems();
                if (initialPermissions.isEmpty()) {
                    Notification.show("Please select at least one permission");
                } else if (onAddManager.apply(new UserPermission(selectedUser, List.copyOf(initialPermissions)))) {
                    this.close();
                }
            }
        });

        styleButton(saveButton, "var(--lumo-primary-color)");

        Button cancelButton = new Button("Cancel", e -> this.close());
        styleButton(cancelButton, " #9e9e9e");

        HorizontalLayout buttons = new HorizontalLayout(saveButton, cancelButton);
        buttons.setJustifyContentMode(JustifyContentMode.END);

        dialogLayout.add(candidateSelect, additionalInfo, buttons);

        Tab addManagerTab = new Tab(VaadinIcon.USERS.create(), new Span("Add Manager"));

        Tab addOwnerTab = new Tab(VaadinIcon.USER_STAR.create(), new Span("Add Owner"));
        Tabs tabs = new Tabs(addManagerTab, addOwnerTab);


        tabs.addSelectedChangeListener(event -> {
            additionalInfo.removeAll();
            List<UserDTO> users = new ArrayList<>();
            if (tabs.getSelectedTab().equals(addManagerTab)) {
                users = this.managementCandidatesSupplier.get();
                additionalInfo.add(permissionsSelect);

                saveButton.addClickListener(e -> saveManager());
            } else if (tabs.getSelectedTab().equals(addOwnerTab)) {
                users = this.ownershipCandidatesSupplier.get();
                saveButton.addClickListener(e -> saveOwner());
            }
            // TODO: addCLickListener might be problematic
            if (users != null) {
                if (users.isEmpty()) {
                    Notification.show("No more candidates left");
                }
                candidateSelect.setItems(users);
            }

        });
        this.add(tabs, dialogLayout);
        
        
        // setupDialog();
    }

    private void saveManager() {
        UserDTO selectedUser = candidateSelect.getValue();
        if (selectedUser == null) {
            Notification.show("Please Choose a user");
        } else {
            Set<PermissionType> initialPermissions = permissionsSelect.getSelectedItems();
            if (initialPermissions.isEmpty()) {
                Notification.show("Please select at least one permission");
            } else if (onAddManager.apply(new UserPermission(selectedUser, List.copyOf(initialPermissions)))) {
                this.close();
            }
        }
    }

    private void saveOwner() {
        UserDTO selectedUser = candidateSelect.getValue();
        if (selectedUser == null) {
            Notification.show("Please Choose a user");
        } else if (onAddOwner.apply(selectedUser)) {
            this.close();
        }
        
    }
    

    private void setupDialog() {
        candidateSelect.setItemLabelGenerator(user -> user.getUsername());
        candidateSelect.setWidth("100%");
        candidateSelect.setHelperText("Select a user to appoint as manager");
        candidateSelect.setRequired(true);
        

        MultiSelectComboBox<PermissionType> permissionsSelect = new MultiSelectComboBox<>("Initial Permissions");
        permissionsSelect.setItems(PermissionFactory.MANAGER_PERMISSIONS);
        permissionsSelect.setItemLabelGenerator(PermissionType::toString);
        permissionsSelect.setWidth("100%");
        permissionsSelect.setHelperText("Select initial permissions for the manager");

        dialogLayout.add(candidateSelect, permissionsSelect);

        Button saveButton = new Button("Save", e -> {
            UserDTO selectedUser = candidateSelect.getValue();
            if (selectedUser == null) {

                Notification.show("Please Choose a user");
            } else {
                Set<PermissionType> initialPermissions = permissionsSelect.getSelectedItems();
                if (initialPermissions.isEmpty()) {
                    Notification.show("Please select at least one permission");
                } else if (onAddManager.apply(new UserPermission(selectedUser, List.copyOf(initialPermissions)))) {
                    this.close();
                }
            }
        });

        styleButton(saveButton, "var(--lumo-primary-color)");

        Button cancelButton = new Button("Cancel", e -> this.close());
        styleButton(cancelButton, " #9e9e9e");

        HorizontalLayout buttons = new HorizontalLayout(saveButton, cancelButton);
        buttons.setJustifyContentMode(JustifyContentMode.END);
        dialogLayout.add(buttons);
        this.add(dialogLayout);
    }

    private void showAddOwner() {

    }

    private void showAddManager() {

    }

    // public void refreshCandidates() {
    //     List<UserDTO> candidates = candidatesSupplier.get();
    //     if (candidates == null) {
    //         Notification.show("Failed to refresh user list.");
    //     } else if (candidates.isEmpty()) {
    //         dialogLayout.removeAll();
    //         Span noCandidatesMessage = new Span("No users available to appoint as manager.");
    //         noCandidatesMessage.getStyle().set("color", "var(--lumo-secondary-text-color)");
    //         dialogLayout.add(noCandidatesMessage);
    //     } else {
    //         candidateSelect.setItems(candidates);
    //     }
    // }
    

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


