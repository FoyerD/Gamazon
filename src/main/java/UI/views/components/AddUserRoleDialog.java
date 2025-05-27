package UI.views.components;

import java.util.List;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Supplier;


import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.combobox.MultiSelectComboBox;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.FlexComponent.JustifyContentMode;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;

import Application.DTOs.UserDTO;
import Domain.management.PermissionFactory;
import Domain.management.PermissionType;
import UI.views.dataobjects.UserPermission;

public class AddUserRoleDialog extends Dialog {
    private final ComboBox<UserDTO> candidateSelect;
    private final Supplier<List<UserDTO>> candidatesSupplier;
    /**
     * Function to handle the addition of a user with their permissions.
     * Returns true if the user permissions was successfully added, false otherwise.
     */
    private final Function<UserPermission, Boolean> onAddUser;
    private final VerticalLayout dialogLayout = new VerticalLayout();

    
    public AddUserRoleDialog(String title, 
                            Supplier<List<UserDTO>> candidatesSupplier, 
                            Function<UserPermission, Boolean> onAddUser) {
        this.candidatesSupplier = candidatesSupplier;
        this.onAddUser = onAddUser;
        this.setHeaderTitle(title);

        dialogLayout.setSpacing(true);
        dialogLayout.setPadding(true);

        candidateSelect = new ComboBox<>("Select User");
                
        setupDialog();
    }

    private void setupDialog() {
        candidateSelect.setItemLabelGenerator(user -> user.getUsername() + " (" + user.getEmail() + ") - " + user.getId());
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
                } else if (onAddUser.apply(new UserPermission(selectedUser, List.copyOf(initialPermissions)))) {
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


    public void refreshCandidates() {
        List<UserDTO> candidates = candidatesSupplier.get();
        if (candidates == null) {
            Notification.show("Failed to refresh user list.");
        } else if (candidates.isEmpty()) {
            dialogLayout.removeAll();
            Span noCandidatesMessage = new Span("No users available to appoint as manager.");
            noCandidatesMessage.getStyle().set("color", "var(--lumo-secondary-text-color)");
            dialogLayout.add(noCandidatesMessage);
        } else {
            candidateSelect.setItems(candidates);
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


