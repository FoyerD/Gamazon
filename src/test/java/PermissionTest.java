import Domain.Permission;
import Domain.PermissionType;
import Domain.RoleType;

import org.junit.Before;
import org.junit.Test;

import java.util.Set;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class PermissionTest {

    private Permission permission;
    private static final String PERMISSION_GIVER = "test1";
    private static final String PERMISSION_OWNER = "test2";

    @Before
    public void setUp() {
        permission = new Permission(PERMISSION_GIVER, PERMISSION_OWNER);
    }

    @Test
    public void testInitStoreManager() {
        permission.initStoreManager();
        assertTrue(permission.isStoreManager());
        assertFalse(permission.isStoreOwner());
        assertFalse(permission.isStoreFounder());
        assertTrue(permission.hasPermission(PermissionType.ACCESS_PURCHASE_RECORDS));
        assertFalse(permission.hasPermission(PermissionType.SUPERVISE_MANAGERS));
        assertFalse(permission.hasPermission(PermissionType.ASSIGN_OR_REMOVE_OWNERS));
        assertFalse(permission.hasPermission(PermissionType.DEACTIVATE_STORE));
        assertFalse(permission.hasPermission(PermissionType.HANDLE_INVENTORY));
        assertFalse(permission.hasPermission(PermissionType.EDIT_STORE_POLICIES));
        assertFalse(permission.hasPermission(PermissionType.MODIFY_OWNER_RIGHTS));
        assertFalse(permission.hasPermission(PermissionType.VIEW_EMPLOYEE_INFO));
        assertFalse(permission.hasPermission(PermissionType.ADMINISTER_STORE));
        assertFalse(permission.hasPermission(PermissionType.OVERSEE_OFFERS));
        assertFalse(permission.hasPermission(PermissionType.CONTROL_CONTRACTS));
    }

    @Test
    public void testInitStoreOwner() {
        permission.initStoreOwner();
        assertFalse(permission.isStoreManager());
        assertTrue(permission.isStoreOwner());
        assertFalse(permission.isStoreFounder());
        assertTrue(permission.hasPermission(PermissionType.SUPERVISE_MANAGERS));
        assertTrue(permission.hasPermission(PermissionType.ASSIGN_OR_REMOVE_OWNERS));
        assertTrue(permission.hasPermission(PermissionType.HANDLE_INVENTORY));
        assertTrue(permission.hasPermission(PermissionType.EDIT_STORE_POLICIES));
        assertTrue(permission.hasPermission(PermissionType.VIEW_EMPLOYEE_INFO));
        assertTrue(permission.hasPermission(PermissionType.ACCESS_PURCHASE_RECORDS));
        assertTrue(permission.hasPermission(PermissionType.ADMINISTER_STORE));
        assertTrue(permission.hasPermission(PermissionType.OVERSEE_OFFERS));
        assertTrue(permission.hasPermission(PermissionType.CONTROL_CONTRACTS));
        assertFalse(permission.hasPermission(PermissionType.DEACTIVATE_STORE));
    }

    @Test
    public void testInitStoreFounder() {
        permission.initStoreFounder();
        assertFalse(permission.isStoreManager());
        assertFalse(permission.isStoreOwner());
        assertTrue(permission.isStoreFounder());
        assertTrue(permission.hasPermission(PermissionType.SUPERVISE_MANAGERS));
        assertTrue(permission.hasPermission(PermissionType.ASSIGN_OR_REMOVE_OWNERS));
        assertTrue(permission.hasPermission(PermissionType.DEACTIVATE_STORE));
        assertTrue(permission.hasPermission(PermissionType.HANDLE_INVENTORY));
        assertTrue(permission.hasPermission(PermissionType.EDIT_STORE_POLICIES));
        assertTrue(permission.hasPermission(PermissionType.MODIFY_OWNER_RIGHTS));
        assertTrue(permission.hasPermission(PermissionType.VIEW_EMPLOYEE_INFO));
        assertTrue(permission.hasPermission(PermissionType.ACCESS_PURCHASE_RECORDS));
        assertTrue(permission.hasPermission(PermissionType.ADMINISTER_STORE));
        assertTrue(permission.hasPermission(PermissionType.OVERSEE_OFFERS));
        assertTrue(permission.hasPermission(PermissionType.CONTROL_CONTRACTS));
    }

    @Test
    public void testSetPermissions() {
        permission.setPermissions(Set.of(
                PermissionType.ASSIGN_OR_REMOVE_OWNERS,
                PermissionType.HANDLE_INVENTORY
        ));
        assertTrue(permission.hasPermission(PermissionType.ASSIGN_OR_REMOVE_OWNERS));
        assertFalse(permission.hasPermission(PermissionType.SUPERVISE_MANAGERS));
        assertTrue(permission.hasPermission(PermissionType.HANDLE_INVENTORY));
        permission.setPermissions(Set.of(
                PermissionType.SUPERVISE_MANAGERS,
                PermissionType.DEACTIVATE_STORE,
                PermissionType.MODIFY_OWNER_RIGHTS
        ));
        assertFalse(permission.hasPermission(PermissionType.ASSIGN_OR_REMOVE_OWNERS));
        assertTrue(permission.hasPermission(PermissionType.SUPERVISE_MANAGERS));
        assertTrue(permission.hasPermission(PermissionType.DEACTIVATE_STORE));
        assertTrue(permission.hasPermission(PermissionType.MODIFY_OWNER_RIGHTS));
        permission.setPermissions(Set.of(
                PermissionType.ASSIGN_OR_REMOVE_OWNERS,
                PermissionType.SUPERVISE_MANAGERS,
                PermissionType.EDIT_STORE_POLICIES,
                PermissionType.OVERSEE_OFFERS
        ));
        assertTrue(permission.hasPermission(PermissionType.ASSIGN_OR_REMOVE_OWNERS));
        assertTrue(permission.hasPermission(PermissionType.SUPERVISE_MANAGERS));
        assertTrue(permission.hasPermission(PermissionType.EDIT_STORE_POLICIES));
        assertTrue(permission.hasPermission(PermissionType.OVERSEE_OFFERS));
        assertFalse(permission.hasPermission(PermissionType.HANDLE_INVENTORY));
    }

    @Test
    public void testStoreManagerNoAssignOwnerPermission() {
        permission.initStoreManager();
        assertFalse(permission.hasPermission(PermissionType.ASSIGN_OR_REMOVE_OWNERS));
    }

    @Test
    public void testStoreManagerHasPurchaseAccessPermission() {
        permission.initStoreManager();
        assertTrue(permission.hasPermission(PermissionType.ACCESS_PURCHASE_RECORDS));
    }

    @Test
    public void testStoreFounderHasAllExpectedPermissions() {
        permission.initStoreFounder();
        assertTrue(permission.hasPermission(PermissionType.ASSIGN_OR_REMOVE_OWNERS));
        assertTrue(permission.hasPermission(PermissionType.SUPERVISE_MANAGERS));
        assertTrue(permission.hasPermission(PermissionType.DEACTIVATE_STORE));
        assertTrue(permission.hasPermission(PermissionType.HANDLE_INVENTORY));
        assertTrue(permission.hasPermission(PermissionType.EDIT_STORE_POLICIES));
        assertTrue(permission.hasPermission(PermissionType.MODIFY_OWNER_RIGHTS));
        assertTrue(permission.hasPermission(PermissionType.VIEW_EMPLOYEE_INFO));
        assertTrue(permission.hasPermission(PermissionType.ACCESS_PURCHASE_RECORDS));
    }

    @Test
    public void testStoreFounderModifyOwnerPermissions() {
        permission.initStoreFounder();
        assertTrue(permission.hasPermission(PermissionType.MODIFY_OWNER_RIGHTS));
    }

    @Test
    public void testIsStoreOwner() {
        permission.setRole(RoleType.STORE_OWNER);
        assertTrue(permission.isStoreOwner());
    }

    @Test
    public void testHasPermission() {
        permission.initStoreOwner();
        assertTrue(permission.hasPermission(PermissionType.SUPERVISE_MANAGERS));
        assertFalse(permission.hasPermission(PermissionType.DEACTIVATE_STORE));
    }
}
