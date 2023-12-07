<h1>Microservices E-Commerce</h1>
<p>Welcome to our microservices-based e-commerce application, utilizing technologies such as Consul Discovery, Spring Cloud Config, Spring Cloud Gateway, Angular, and other specific services.</p>
<h3>Architecture:</h3>
<img src="captures/capture1.png"></img>
<details>
<summary style="font-size:15px;cursor:pointer">📌 1. CONFIG SERVICE: (Click to expand 🖱)</summary>
        <h5>Consul registered services:</h5>
    <img src="captures/all-registered.jpg"></img>


</details>

<details>
<summary style="font-size:15px;cursor:pointer">📌 2. CUSTOMER-SERVICE (Click to expand 🖱)</summary>
        <h5>Entity Customer</h5>


```javascript
@Entity
@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class Customer {
        @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
        private Long id;
        private String name;
        private String email;
}
```

<h5>Repository CustomerRepository</h5>

```javascript
@RepositoryRestResource
public interface CustomerRepository extends JpaRepository<Customer, Long> {
}
```

<h5>Données de test</h5>

```javascript
@Bean
CommandLineRunner start(CustomerRepository customerRepository){
        return args -> {
                customerRepository.saveAll(List.of(
                        Customer.builder().name("Mohamed").email("med@gmail.com").build(),
                        Customer.builder().name("Hassan").email("hasan@gmail.com").build(),
                        Customer.builder().name("IMane").email("imane@gmail.com").build()
                ));
                customerRepository.findAll().forEach(System.out::println);
        };
}
```
<h5>Customer service Test</h5>
<img src="captures/customer-service-test.jpg" width="700">
</details>
<details>
<summary style="font-size:15px;cursor:pointer">📌 3. GATEWAY-SERVICE (Click to expand 🖱)</summary>
        <h5>Bean de configuration</h5>
        <img src="captures/gateway-bean.jpg" width="700">
        <h5>Configuration de la Gateway</h5>
        <img src="captures/gateway-properties.jpg" width="700">
        <h5>Test de la gateway</h5>
        <img src="captures/order-service-full-order.jpg" width="700">
        </details>

<details>
        <summary style="font-size:15px;cursor:pointer">📌 4. INVENTORY-SERVICE (Click to expand 🖱)</summary>
<h5>Entity Product</h5>

```javascript
@Entity
@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class Product {
        @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
        private Long id;
        private String name;
        private double price;
        private int quantity;
}
```

<h5>Repository ProductRepository</h5>

```javascript
@RepositoryRestResource
public interface ProductRepository extends JpaRepository<Product, Long> {
}

```

<h5>Données de test</h5>


```javascript
@Bean
CommandLineRunner start(ProductRepository productRepository)
{
        return args -> {
                Random
                random = new Random();
                for (int i = 1;
                i < 10;
                i++
        )
                {
                        productRepository.saveAll(List.of(
                                Product.builder()
                                        .name("Laptop " + i)
                                        .price(1200 + Math.random() * 10000)
                                        .quantity(1 + random.nextInt(200)).build()
                        ));
                }

        };
}
```

<h5>Test de l'inventory service</h5>
        <img src="captures/inventory-test-.jpg" width="700">
        </details>

<details>
        <summary style="font-size:15px;cursor:pointer">📌 5. ORDER-SERVICE (Click to expand 🖱)</summary>
        <h5>Entity Order</h5>

```javascript
@Entity
@Table(name="orders")
@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class Order {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Date createdAt;
    private OrderStatus status;
    private Long customerId;
    @Transient
    private Customer customer;
    @OneToMany(mappedBy = "order")
    private List<ProductItem> productItems;

    public double getTotal(){
        double somme=0;
        for(ProductItem pi:productItems){
            somme+=pi.getAmount();
        }
        return somme;
    }
}
```
<h5>Entity ProductItem</h5>

```javascript
@Entity
@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class ProductItem {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Long productId;
    @Transient
    private Product product;
    private double price;
    private int quantity;
    private double discount;
    @ManyToOne
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private Order order;
    public double getAmount(){
        return price*quantity*(1-discount);
    }
}
```
<h5>Customer Model</h5>

```javascript
@Data
public class Customer {
    private Long id;
    private String name;
    private String email;
}
```

<h5>Product Model</h5>

```javascript
@Data
public class Product {
    private Long id;
    private String name;
    private double price;
    private int quantity;
}
```

<h5>Repository OrderRepository</h5>

```javascript
@RepositoryRestResource
public interface OrderRepository extends JpaRepository<Order, Long> {
    @RestResource(path = "/byCustomerId")
    List<Order> findByCustomerId(@Param("customerId") Long customerId);
}
```
<h5>Customer Rest Client</h5>

```javascript
@FeignClient(name = "customer-service")
public interface CustomerRestClientService {
@GetMapping("/customers/{id}?projection=fullCustomer")
    public Customer customerById(@PathVariable Long id);
@GetMapping("/customers?projection=fullCustomer")
    public PagedModel<Customer> allCustomers();
}
```
<h5>Inventory Rest Client</h5>

```javascript
@FeignClient(name = "inventory-service")
public interface InventoryRestClientService {
    @GetMapping("/products/{id}?projection=fullProduct")
    public Product productById(@PathVariable Long id);
    @GetMapping("/products?projection=fullProduct")
    public PagedModel<Product> allProducts();
}
```
<h5>Configuration</h5>
<img src="captures/open-feign-config.jpg" width="700">
<h5>fullOrder</h5>

```javascript
@GetMapping("/fullOrder/{id}")
public Order getOrder(@PathVariable Long id){
    Order order=orderRepository.findById(id).get();
    Customer customer=customerRestClientService.customerById(order.getCustomerId());
    order.setCustomer(customer);
    order.getProductItems().forEach(pi->{
        Product product=inventoryRestClientService.productById(pi.getProductId());
        pi.setProduct(product);
    });
    return order;
}
```

<img src="captures/order-service-full-order.jpg" width="700">
        </details>
        <details>
        <summary style="font-size:15px;cursor:pointer">📌 6. BILLING-SERVICE avec consul config et vault (Click to expand 🖱)</summary>
        <h5>Dependencies</h5>

```javascript
<dependency>
    <groupId>org.springframework.cloud</groupId>
    <artifactId>spring-cloud-starter-consul-config</artifactId>
</dependency>
<dependency>
    <groupId>org.springframework.cloud</groupId>
    <artifactId>spring-cloud-starter-consul-discovery</artifactId>
</dependency>
<dependency>
    <groupId>org.springframework.cloud</groupId>
    <artifactId>spring-cloud-starter-vault-config</artifactId>
</dependency>
```
<h5>Consul config</h5>
<img src="captures/token-key-value.jpg" width="700">
<h5>Controlleur de test</h5>

```javascript
@RestController
public class ConsulConfigRestController {
    @Autowired
    private MyConsulConfig myConsulConfig;
    @Autowired
    private MyVaultConfig myVaultConfig;
    @Value("${token.accessTokenTimeout}")
    private long accessTokenTimeout;
    @Value("${token.refreshTokenTimeout}")
    private long refreshTokenTimeout;
}
``` 
<h5>Avec class de configuration</h5>

```java
@RestController
public class ConsulConfigRestController {
    @Autowired
    private MyConsulConfig myConsulConfig;
    @Autowired
    private MyVaultConfig myVaultConfig;
    //@Value("${token.accessTokenTimeout}")
    //private long accessTokenTimeout;
    //@Value("${token.refreshTokenTimeout}")
    //private long refreshTokenTimeout;
    @GetMapping("/myConfig")
    public Map<String,Object> myConfig(){
        return Map.of("consulConfig",myConsulConfig, "vaultConfig",myVaultConfig);
    }
}
```

<h5>Configuration des secrets avec vault</h5>
<img src="captures/secrets.PNG" width="700">
        </details>
        <details>
        <summary style="font-size:15px;cursor:pointer">📌 7. FRONTEND ANGULAR (Click to expand 🖱)</summary>
<h5>Customers list</h5>
<img src="captures/customers.jpg" width="700">
<h5>Products list</h5>
<img src="captures/products.jpg" width="700">
<h5>Orders list</h5>
<img src="captures/cust-orders.jpg" width="700">
<h5>Order details</h5>
<img src="captures/order-details.jpg" width="700">
</details>

