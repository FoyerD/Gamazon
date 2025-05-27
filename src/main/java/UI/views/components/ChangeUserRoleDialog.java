package UI.views.components;

import java.util.List;
import java.util.Set;
import java.util.function.Function;


import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.combobox.MultiSelectComboBox;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.FlexComponent.JustifyContentMode;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;

import Domain.management.PermissionFactory;
import Domain.management.PermissionType;
import UI.views.dataobjects.UserPermission;

public class ChangeUserRoleDialog extends Dialog {

    private final UserPermission userPermission;
    private final Function<UserPermission, Boolean> permissionChanger;
    private final VerticalLayout dialogLayout = new VerticalLayout();


    public ChangeUserRoleDialog(UserPermission userPermission,
                                Function<UserPermission, Boolean> permissionChanger) {
    
        this.userPermission = userPermission;
        this.permissionChanger = permissionChanger;
        this.setHeaderTitle("Change Role for " + userPermission.user.getUsername());

        dialogLayout.setSpacing(true);
        dialogLayout.setPadding(true);

                
        setupDialog();
    }

    private void setupDialog() {
 

        MultiSelectComboBox<PermissionType> permissionsSelect = new MultiSelectComboBox<>("Initial Permissions");
        permissionsSelect.setItems(PermissionFactory.AVAILABLE_MANAGER_PERMISSIONS);
        permissionsSelect.setItemLabelGenerator(PermissionType::toString);
        permissionsSelect.setWidth("100%");
        permissionsSelect.select(userPermission.permissions);

        dialogLayout.add(permissionsSelect);

        Button saveButton = new Button("Save", e -> {
            Set<PermissionType> selectedPermissions = permissionsSelect.getSelectedItems();
            if (selectedPermissions.isEmpty()) {
                Notification.show("Please select at least one permission");
            } else if (permissionChanger.apply(new UserPermission(userPermission.user, List.copyOf(selectedPermissions)))) {
                this.close();
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



