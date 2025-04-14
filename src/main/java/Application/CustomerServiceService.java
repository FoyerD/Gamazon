package Application;

import Domain.Store.IOrderRepository;

public class CustomerServiceService {
    IOrderRepository orderRepository;
    
    public CustomerServiceService(IOrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }
    public CustomerServiceService() {
        this.orderRepository = null;
    }

    public Response<Boolean> setOrderRepository(IOrderRepository orderRepository) {
        this.orderRepository = orderRepository;
        return new Response<>(true);
    }

    public Response<Boolean> addOrder(String customerId, )
    
}
