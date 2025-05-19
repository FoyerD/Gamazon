package Application;

import Application.utils.Response;
import Application.utils.TradingLogger;
import Domain.Store.Product;
import Domain.Store.ProductFacade;

import java.util.List;

import org.springframework.stereotype.Service;

import Application.DTOs.ProductDTO;
import Application.utils.Error;

@Service
public class ProductService {
    private final ProductFacade productFacade;
    private TokenService tokenService;


    public ProductService(ProductFacade productFacade, TokenService tokenService) {
        this.productFacade = productFacade;
        this.tokenService = tokenService;
    }

    public Response<ProductDTO> addProduct(String sessionToken, String name, List<String> categories, List<String> catDesc) {
        String method = "addProduct";
        try {
            if (!tokenService.validateToken(sessionToken)) {
                return Response.error("Invalid token");
            }
            //String userId = this.tokenService.extractId(sessionToken);
            Product product = productFacade.addProduct(name, categories, catDesc);
            TradingLogger.logEvent("ProductService", method, "Product added successfully.");
            return new Response<>(new ProductDTO(product));
        } catch (Exception ex) {
            TradingLogger.logError("ProductService", method, ex.getMessage());
            return new Response<>(new Error(ex.getMessage()));
        }
    }

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
    
}
