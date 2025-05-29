package UI;

import java.util.List;

import org.springframework.boot.CommandLineRunner;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import Application.DTOs.ProductDTO;
import Application.DTOs.UserDTO;
import Application.ItemService;
import Application.MarketService;
import Application.ProductService;
import Application.StoreService;
import Application.TokenService;
import Application.UserService;
import Domain.management.PermissionType;

@Component
@Order(1)
public class AppInitializer implements CommandLineRunner, Ordered {

    private final UserService userService;
    private final StoreService storeService;
    private final ProductService productService;
    private final ItemService itemService;
    private final MarketService marketService;
    private final TokenService tokenService;

    public AppInitializer(
            UserService userService,
            StoreService storeService,
            ProductService productService,
            ItemService itemService,
            MarketService marketService,
            TokenService tokenService) {
        this.userService = userService;
        this.storeService = storeService;
        this.productService = productService;
        this.itemService = itemService;
        this.marketService = marketService;
        this.tokenService = tokenService;
    }

    @Override
    public int getOrder() {
        return 1; 
    }

    @Override
    public void run(String... args) {
        System.out.println("\uD83C\uDF31 Starting App Initialization");

        UserDTO guest1 = userService.guestEntry().getValue();
        UserDTO guest2 = userService.guestEntry().getValue();

        var adminResp = userService.register(guest1.getSessionToken(), "admin", "Admin123!@", "admin@g.com");
        if (adminResp.errorOccurred()) {
            System.err.println("❌ Failed to register admin: " + adminResp.getErrorMessage());
            return;
        }
        UserDTO admin = adminResp.getValue();

        var buyerResp = userService.register(guest2.getSessionToken(), "buyer", "Buyer123!@", "buyer@g.com");
        if (buyerResp.errorOccurred()) {
            System.err.println("❌ Failed to register buyer: " + buyerResp.getErrorMessage());
            return;
        }
        UserDTO buyer = buyerResp.getValue();

        String adminToken = admin.getSessionToken();
        String buyerToken = buyer.getSessionToken();

        var openResp = marketService.openMarket(adminToken);
        if (openResp.errorOccurred()) {
            System.err.println("❌ Failed to open market: " + openResp.getErrorMessage());
            return;
        }

        var store1Resp = storeService.addStore(adminToken, "SuperTech", "Electronics");
        if (store1Resp.errorOccurred()) {
            System.err.println("❌ Failed to create store1: " + store1Resp.getErrorMessage());
            return;
        }
        var store1 = store1Resp.getValue();

        var store2Resp = storeService.addStore(adminToken, "ComfyHome", "Home & Furniture");
        if (store2Resp.errorOccurred()) {
            System.err.println("❌ Failed to create store2: " + store2Resp.getErrorMessage());
            return;
        }
        var store2 = store2Resp.getValue();

        var appointResp = marketService.appointStoreManager(adminToken, tokenService.extractId(buyer.getSessionToken()), store1.getId());
        System.out.println("Appointing buyer as store manager for store1: " + store1.getManagers().toString());
        if (appointResp.errorOccurred()) {
            System.err.println("❌ Failed to appoint store manager: " + appointResp.getErrorMessage());
            return;
        }

        var laptop = productService.addProduct(adminToken, "Laptop", List.of("Electronics"), List.of("All tech"));
        if (laptop.errorOccurred()) {
            System.err.println("❌ Failed to add Laptop product: " + laptop.getErrorMessage());
            return;
        }

        var chair = productService.addProduct(adminToken, "Chair", List.of("Furniture"), List.of("Comfort"));
        if (chair.errorOccurred()) {
            System.err.println("❌ Failed to add Chair product: " + chair.getErrorMessage());
            return;
        }

        ProductDTO laptopDTO = laptop.getValue();
        ProductDTO chairDTO = chair.getValue();

        var addItem1 = itemService.add(adminToken, store1.getId(), laptopDTO.getId(), 1000.0, 10, "Gaming Laptop");
        if (addItem1.errorOccurred()) {
            System.err.println("❌ Failed to add item to store1: " + addItem1.getErrorMessage());
            return;
        }

        var addItem2 = itemService.add(adminToken, store2.getId(), chairDTO.getId(), 120.0, 15, "Ergonomic Chair");
        if (addItem2.errorOccurred()) {
            System.err.println("❌ Failed to add item to store2: " + addItem2.getErrorMessage());
            return;
        }

        var permResp = marketService.changeManagerPermissions(adminToken, tokenService.extractId(buyer.getSessionToken()), store1.getId(),
                List.of(PermissionType.HANDLE_INVENTORY, PermissionType.OVERSEE_OFFERS));
        if (permResp.errorOccurred()) {
            System.err.println("❌ Failed to change manager permissions: " + permResp.getErrorMessage());
            return;
        }

        var logoutResp = userService.exit(adminToken);
        if (logoutResp.errorOccurred()) {
            System.err.println("❌ Failed to log out admin: " + logoutResp.getErrorMessage());
            return;
        }

        var logoutResp2 = userService.exit(buyerToken);
        if (logoutResp2.errorOccurred()) {
            System.err.println("❌ Failed to log out buyer: " + logoutResp2.getErrorMessage());
            return;
        }

        System.out.println("✅ App Initialization Complete");
    }
}