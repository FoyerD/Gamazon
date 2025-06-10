package UI;

import java.io.InputStream;
import java.sql.Date;
import java.time.LocalDate;
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

import Application.DTOs.PaymentDetailsDTO;
import Application.DTOs.UserDTO;
import Application.ItemService;
import Application.MarketService;
import Application.ProductService;
import Application.ShoppingService;
import Application.StoreService;
import Application.TokenService;
import Application.UserService;
import Application.DTOs.UserDTO;
import Application.utils.Response;
import Domain.ExternalServices.IExternalPaymentService;
import Domain.ExternalServices.IExternalSupplyService;
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
    private final ShoppingService shoppingService;
    private final IExternalPaymentService externalPaymentService;
    private final IExternalSupplyService externalSupplyService;

    @Value("${app.init.state:default}")
    private String initState;

    public AppInitializer(
            UserService userService,
            StoreService storeService,
            ProductService productService,
            ItemService itemService,
            MarketService marketService,
            TokenService tokenService, 
            ShoppingService shoppingService,
            IExternalPaymentService externalPaymentService,
            IExternalSupplyService externalSupplyService) {
        this.userService = userService;
        this.storeService = storeService;
        this.productService = productService;
        this.itemService = itemService;
        this.marketService = marketService;
        this.tokenService = tokenService;
        this.shoppingService = shoppingService;
        this.externalPaymentService = externalPaymentService;
        this.externalSupplyService = externalSupplyService;

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

            // Updating external services URLs
            String URL = (String)(states.get("urlExternalServices").get(0).get("URL"));
            Response<Void> paymentServiceResponse = externalPaymentService.updatePaymentServiceURL(URL);
            Response<Void> supplyServiceResponse = externalSupplyService.updateSupplyServiceURL(URL);
            System.out.println("External services URLs updated successfully. With URL: " + URL);

            List<Map<String, Object>> commands = states.get(initState);
            if (commands == null) {
                System.err.println("❌ No such init state: " + initState);
                return;
            }

            for (Map<String, Object> cmd : commands) {
                handleCommand(cmd);
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to initialize application", e);
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
                    } else throw new RuntimeException("❌ Command '" + action + "' failed: " + resp.getErrorMessage());;
                }
                case "openMarket" -> {
                    var resp = marketService.openMarket(sessionTokens.get(cmd.get("session").toString()));
                    if (resp.errorOccurred()) throw new RuntimeException("❌ Command '" + action + "' failed: " + resp.getErrorMessage());;
                }
                case "addStore" -> {
                    var resp = storeService.addStore(
                            sessionTokens.get(cmd.get("session").toString()),
                            (String) cmd.get("name"),
                            (String) cmd.get("category"));
                    if (!resp.errorOccurred()) {
                        values.put((String) cmd.get("as"), resp.getValue().getId());
                    } else throw new RuntimeException("❌ Command '" + action + "' failed: " + resp.getErrorMessage());;
                }
                case "addProduct" -> {
                    var resp = productService.addProduct(
                            sessionTokens.get(cmd.get("session").toString()),
                            (String) cmd.get("name"),
                            (List<String>) cmd.get("categories"),
                            (List<String>) cmd.get("keywords"));
                    if (!resp.errorOccurred()) {
                        values.put((String) cmd.get("as"), resp.getValue().getId());
                    } else throw new RuntimeException("❌ Command '" + action + "' failed: " + resp.getErrorMessage());;
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
                    if (resp.errorOccurred()) throw new RuntimeException("❌ Command '" + action + "' failed: " + resp.getErrorMessage());;
                }
                case "changePermissions" -> {
                    var resp = marketService.changeManagerPermissions(
                            sessionTokens.get(cmd.get("session").toString()),
                            tokenService.extractId(sessionTokens.get(cmd.get("target").toString())),
                            values.get(cmd.get("store").toString()),
                            ((List<String>) cmd.get("permissions")).stream()
                                    .map(PermissionType::valueOf).toList());
                    if (resp.errorOccurred()) throw new RuntimeException("❌ Command '" + action + "' failed: " + resp.getErrorMessage());;
                }
                case "logout" -> {
                    var resp = userService.exit(sessionTokens.get(cmd.get("session").toString()));
                    if (resp.errorOccurred()) throw new RuntimeException("❌ Command '" + action + "' failed: " + resp.getErrorMessage());;
                }
                case "appointStoreManager" -> {
                    var resp = marketService.appointStoreManager(
                            sessionTokens.get(cmd.get("session").toString()),
                            tokenService.extractId(sessionTokens.get(cmd.get("target").toString())),
                            values.get(cmd.get("store").toString()));
                    if (resp.errorOccurred()) throw new RuntimeException("❌ Command '" + action + "' failed: " + resp.getErrorMessage());;
                }
                case "appointStoreOwner" -> {
                    var resp = marketService.appointStoreOwner(
                            sessionTokens.get(cmd.get("session").toString()),
                            tokenService.extractId(sessionTokens.get(cmd.get("target").toString())),
                            values.get(cmd.get("store").toString()));
                    if (resp.errorOccurred()) throw new RuntimeException("❌ Command '" + action + "' failed: " + resp.getErrorMessage());;
                }
                case "changeManagerPermissions" -> {
                    var resp = marketService.changeManagerPermissions(
                            sessionTokens.get(cmd.get("session").toString()),
                            tokenService.extractId(sessionTokens.get(cmd.get("target").toString())),
                            values.get(cmd.get("store").toString()),
                            ((List<String>) cmd.get("permissions")).stream()
                                    .map(PermissionType::valueOf).toList());
                    if (resp.errorOccurred()) throw new RuntimeException("❌ Command '" + action + "' failed: " + resp.getErrorMessage());;
                }
                case "addAuction" -> {
                    var resp = storeService.addAuction(
                            sessionTokens.get(cmd.get("session").toString()),
                            values.get(cmd.get("store").toString()),
                            values.get(cmd.get("product").toString()),
                            (String) cmd.get("auctionEndDate"),
                            ((Number) cmd.get("startPrice")).doubleValue());
                    if (resp.errorOccurred()) throw new RuntimeException("❌ Command '" + action + "' failed: " + resp.getErrorMessage());;
                }
                case "addToCart" -> {
                    var resp = shoppingService.addProductToCart(
                            values.get(cmd.get("store").toString()),
                            sessionTokens.get(cmd.get("session").toString()),
                            values.get(cmd.get("product").toString()),
                            ((Number) cmd.get("quantity")).intValue());
                    if (resp.errorOccurred()) throw new RuntimeException("❌ Command '" + action + "' failed: " + resp.getErrorMessage());;
                }
                case "makeBid" -> {
                    var resp = shoppingService.makeBid(
                            values.get(cmd.get("auction").toString()),
                            sessionTokens.get(cmd.get("session").toString()),
                            ((Number) cmd.get("price")).floatValue(),
                            (String) cmd.get("cardNumber"),
                            new java.sql.Date(((Number) cmd.get("expiryDate")).longValue()),
                            (String) cmd.get("cvv"),
                            ((Number) cmd.get("andIncrement")).longValue(),
                            (String) cmd.get("clientName"),
                            (String) cmd.get("deliveryAddress"));
                    if (resp.errorOccurred()) throw new RuntimeException("❌ Command '" + action + "' failed: " + resp.getErrorMessage());;
                }
                case "checkout" -> {
                    var resp = shoppingService.checkout(
                            sessionTokens.get(cmd.get("session").toString()),
                            (String) cmd.get("userSSN"),
                            (String) cmd.get("cardNumber"),
                            new java.sql.Date(((Number) cmd.get("expiryDate")).longValue()),
                            (String) cmd.get("cvv"),
                            (String) cmd.get("clientName"),
                            (String) cmd.get("deliveryAddress"),
                            (String) cmd.get("city"),
                            (String) cmd.get("country"),
                            (String) cmd.get("zip"));
                    if (resp.errorOccurred()) throw new RuntimeException("❌ Command '" + action + "' failed: " + resp.getErrorMessage());;
                }
                case "ban" -> {
                    var resp = marketService.banUser(
                            sessionTokens.get(cmd.get("session").toString()),
                            tokenService.extractId(sessionTokens.get(cmd.get("target").toString())),
                            cmd.get("experationDate") != null
                                    ? new Date(((Number) cmd.get("experationDate")).longValue())
                                    : null);
                    if (resp.errorOccurred()) throw new RuntimeException("❌ Command '" + action + "' failed: " + resp.getErrorMessage());;
                }
                case "unban" -> {
                    var resp = marketService.unbanUser(
                            sessionTokens.get(cmd.get("session").toString()),
                            tokenService.extractId(sessionTokens.get(cmd.get("target").toString())));
                    if (resp.errorOccurred()) throw new RuntimeException("❌ Command '" + action + "' failed: " + resp.getErrorMessage());;
                }
                case "makeOffer" -> {
                    var resp = shoppingService.makeOffer(
                            sessionTokens.get(cmd.get("session").toString()),
                            values.get(cmd.get("store").toString()),
                            values.get(cmd.get("product").toString()),
                            ((Number) cmd.get("price")).doubleValue(),
                    new PaymentDetailsDTO(
                            (String) cmd.get("Id"),
                            (String) cmd.get("cardNumber"),
                            LocalDate.of(
                                ((Number) cmd.get("expiryDate/year")).intValue(),
                                ((Number) cmd.get("expiryDate/month")).intValue(),
                                ((Number) cmd.get("expiryDate/day")).intValue()
                            ),
                            (String) cmd.get("cvv"),
                            (String) cmd.get("holder"))
                    );
                    if (!resp.errorOccurred())
                        values.put((String) cmd.get("as"), resp.getValue().getId());
                    else    
                        throw new RuntimeException("❌ Command '" + action + "' failed: " + resp.getErrorMessage());
                }
                case "acceptOfferByMember" -> {
                    var resp = shoppingService.acceptOffer(
                            sessionTokens.get(cmd.get("session").toString()),
                            values.get(cmd.get("offerId").toString()));
                    if (resp.errorOccurred()) throw new RuntimeException("❌ Command '" + action + "' failed: " + resp.getErrorMessage());
                }
                case "acceptOfferByEmployee" -> {
                    var resp = storeService.acceptOffer(
                            sessionTokens.get(cmd.get("session").toString()),
                            values.get(cmd.get("offerId").toString()));
                    if (resp.errorOccurred()) throw new RuntimeException("❌ Command '" + action + "' failed: " + resp.getErrorMessage());
                }
                case "counterOfferByMember" -> {
                    var resp = shoppingService.counterOffer(
                            sessionTokens.get(cmd.get("session").toString()),
                            values.get(cmd.get("offerId").toString()),
                            ((Number) cmd.get("price")).doubleValue()
                    );
                    if (resp.errorOccurred()) throw new RuntimeException("❌ Command '" + action + "' failed: " + resp.getErrorMessage());
                }
                case "counterOfferByEmployee" -> {
                    var resp = storeService.counterOffer(
                            sessionTokens.get(cmd.get("session").toString()),
                            values.get(cmd.get("offerId").toString()),
                            ((Number) cmd.get("price")).doubleValue()
                    );
                    if (resp.errorOccurred()) throw new RuntimeException("❌ Command '" + action + "' failed: " + resp.getErrorMessage());
                }
                default -> System.err.println("⚠ Unknown command: " + action);
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to handle command: " + action, e);
        }
    }
}
