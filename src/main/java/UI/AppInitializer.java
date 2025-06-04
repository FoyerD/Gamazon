package UI;

import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import Application.DTOs.UserDTO;
import Application.ItemService;
import Application.MarketService;
import Application.ProductService;
import Application.StoreService;
import Application.TokenService;
import Application.UserService;
import Application.utils.Response;
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

    @Value("${app.init.state:default}")
    private String initState;

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

    private final Map<String, String> sessionTokens = new HashMap<>();
    private final Map<String, String> values = new HashMap<>();

    @Override
    public int getOrder() {
        return 1;
    }

    @Override
    public void run(String... args) {
        System.out.println("\uD83C\uDF31 Starting App Initialization");

        try {
            ObjectMapper mapper = new ObjectMapper();
            InputStream stream = new ClassPathResource("config/init.json").getInputStream();
            Map<String, List<Map<String, Object>>> states = mapper.readValue(stream, new TypeReference<>() {});

            List<Map<String, Object>> commands = states.get(initState);
            if (commands == null) {
                System.err.println("❌ No such init state: " + initState);
                return;
            }

            for (Map<String, Object> cmd : commands) {
                handleCommand(cmd);
            }
        } catch (Exception e) {
            System.err.println("❌ Error loading init.json: " + e.getMessage());
        }

        System.out.println("✅ App Initialization Complete");
    }

    private void handleCommand(Map<String, Object> cmd) {
        String action = (String) cmd.get("action");
        try {
            switch (action) {
                case "guestEntry" -> {
                    Response<UserDTO> guestResp = userService.guestEntry();
                    if (!guestResp.errorOccurred()) {
                        sessionTokens.put((String) cmd.get("as"), guestResp.getValue().getSessionToken());
                    }
                }
                case "registerUser" -> {
                    String guestToken = sessionTokens.get(cmd.get("session").toString());
                    var resp = userService.register(guestToken,
                            (String) cmd.get("username"),
                            (String) cmd.get("password"),
                            (String) cmd.get("email"));
                    if (!resp.errorOccurred()) {
                        sessionTokens.put((String) cmd.get("as"), resp.getValue().getSessionToken());
                    }
                }
                case "loginUser" -> {
                    var resp = userService.login(
                            (String) cmd.get("username"),
                            (String) cmd.get("password"));
                    if (!resp.errorOccurred()) {
                        sessionTokens.put((String) cmd.get("as"), resp.getValue().getSessionToken());
                    } else System.err.println(resp.getErrorMessage());
                }
                case "openMarket" -> {
                    var resp = marketService.openMarket(sessionTokens.get(cmd.get("session").toString()));
                    if (resp.errorOccurred()) System.err.println(resp.getErrorMessage());
                }
                case "addStore" -> {
                    var resp = storeService.addStore(
                            sessionTokens.get(cmd.get("session").toString()),
                            (String) cmd.get("name"),
                            (String) cmd.get("category"));
                    if (!resp.errorOccurred()) {
                        values.put((String) cmd.get("as"), resp.getValue().getId());
                    } else System.err.println(resp.getErrorMessage());
                }
                case "addProduct" -> {
                    var resp = productService.addProduct(
                            sessionTokens.get(cmd.get("session").toString()),
                            (String) cmd.get("name"),
                            (List<String>) cmd.get("categories"),
                            (List<String>) cmd.get("keywords"));
                    if (!resp.errorOccurred()) {
                        values.put((String) cmd.get("as"), resp.getValue().getId());
                    } else System.err.println(resp.getErrorMessage());
                }
                case "addItem" -> {
                    var resp = itemService.add(
                            sessionTokens.get(cmd.get("session").toString()),
                            values.get(cmd.get("store").toString()),
                            values.get(cmd.get("product").toString()),
                            ((Number) cmd.get("price")).doubleValue(),
                            ((Number) cmd.get("quantity")).intValue(),
                            (String) cmd.get("description"));
                    if (resp.errorOccurred()) System.err.println("❌ addItem failed: " + resp.getErrorMessage());
                }
                case "appointManager" -> {
                    var resp = marketService.appointStoreManager(
                            sessionTokens.get(cmd.get("session").toString()),
                            tokenService.extractId(sessionTokens.get(cmd.get("target").toString())),
                            values.get(cmd.get("store").toString()));
                    if (resp.errorOccurred()) System.err.println(resp.getErrorMessage());
                }
                case "changePermissions" -> {
                    var resp = marketService.changeManagerPermissions(
                            sessionTokens.get(cmd.get("session").toString()),
                            tokenService.extractId(sessionTokens.get(cmd.get("target").toString())),
                            values.get(cmd.get("store").toString()),
                            ((List<String>) cmd.get("permissions")).stream()
                                    .map(PermissionType::valueOf).toList());
                    if (resp.errorOccurred()) System.err.println(resp.getErrorMessage());
                }
                case "logout" -> {
                    var resp = userService.exit(sessionTokens.get(cmd.get("session").toString()));
                    if (resp.errorOccurred()) System.err.println(resp.getErrorMessage());
                }
                case "appointStoreManager" -> {
                    var resp = marketService.appointStoreManager(
                            sessionTokens.get(cmd.get("session").toString()),
                            tokenService.extractId(sessionTokens.get(cmd.get("target").toString())),
                            values.get(cmd.get("store").toString()));
                    if (resp.errorOccurred()) System.err.println(resp.getErrorMessage());
                }
                case "appointStoreOwner" -> {
                    var resp = marketService.appointStoreOwner(
                            sessionTokens.get(cmd.get("session").toString()),
                            tokenService.extractId(sessionTokens.get(cmd.get("target").toString())),
                            values.get(cmd.get("store").toString()));
                    if (resp.errorOccurred()) System.err.println(resp.getErrorMessage());
                }
                case "changeManagerPermissions" -> {
                    var resp = marketService.changeManagerPermissions(
                            sessionTokens.get(cmd.get("session").toString()),
                            tokenService.extractId(sessionTokens.get(cmd.get("target").toString())),
                            values.get(cmd.get("store").toString()),
                            ((List<String>) cmd.get("permissions")).stream()
                                    .map(PermissionType::valueOf).toList());
                    if (resp.errorOccurred()) System.err.println(resp.getErrorMessage());
                }
                default -> System.err.println("⚠ Unknown command: " + action);
            }
        } catch (Exception e) {
            System.err.println("❌ Exception in command '" + action + "': " + e.getMessage());
        }
    }
}
