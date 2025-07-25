package Application;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import Application.DTOs.ProductDTO;
import Application.utils.Error;
import Application.utils.Response;
import Application.utils.TradingLogger;
import Domain.Store.Product;
import Domain.Store.ProductFacade;
import Domain.management.PermissionManager;

@Service
public class ProductService {
    private final ProductFacade productFacade;
    private PermissionManager permissionManager;
    private TokenService tokenService;


    public ProductService(ProductFacade productFacade, TokenService tokenService, PermissionManager permissionManager) {
        this.productFacade = productFacade;
        this.tokenService = tokenService;
        this.permissionManager = permissionManager;
    }

    @Transactional
    public Response<ProductDTO> addProduct(String sessionToken, String name, List<String> categories, List<String> catDesc) {
        String method = "addProduct";
        try {
            if (!tokenService.validateToken(sessionToken)) {
                return Response.error("Invalid token");
            }
            String userId = this.tokenService.extractId(sessionToken);
            if (permissionManager.isBanned(userId)) {
                throw new Exception("User is banned from adding products.");
            }
            Product product = productFacade.addProduct(name, categories, catDesc);
            TradingLogger.logEvent("ProductService", method, "Product added successfully.");
            return new Response<>(new ProductDTO(product));
        } catch (Exception ex) {
            TradingLogger.logError("ProductService", method, ex.getMessage());
            return new Response<>(new Error(ex.getMessage()));
        }
    }

    @Transactional
    public Response<ProductDTO> getProductByName(String sessionToken, String name) {
        String method = "getProductByName";
        try {
            if (!tokenService.validateToken(sessionToken)) {
                return Response.error("Invalid token");
            }
            //String userId = this.tokenService.extractId(sessionToken);
            Product product = productFacade.getProductByName(name);
            TradingLogger.logEvent("ProductService", method, "Fetched product by name: " + name);
            return new Response<>(new ProductDTO(product));
        } catch (Exception ex) {
            TradingLogger.logError("ProductService", method, ex.getMessage());
            return new Response<>(new Error(ex.getMessage()));
        }
    }

    @Transactional
    public Response<Set<ProductDTO>> getAllProducts(String sessionToken) {
        String method = "getAllProducts";
        try {
            if (!tokenService.validateToken(sessionToken)) {
                return Response.error("Invalid token");
            }
            // String userId = this.tokenService.extractId(sessionToken);

            Set<Product> products = productFacade.getAllProducts();

            TradingLogger.logEvent("ProductService", method, "Fetched all products.");
            return new Response<>(products.stream().map(ProductDTO::new).collect(Collectors.toSet()));
        } catch (Exception ex) {
            TradingLogger.logError("ProductService", method, ex.getMessage());
            return new Response<>(new Error(ex.getMessage()));
        }
    }
    
}
