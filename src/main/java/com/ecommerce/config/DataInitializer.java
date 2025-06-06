package com.ecommerce.config;

import com.ecommerce.entity.*;
import com.ecommerce.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.Set;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;
    private final ProductRepository productRepository;
    private final WarehouseRepository warehouseRepository;
    private final InventoryRepository inventoryRepository;
    private final AddressRepository addressRepository;
    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final ReviewRepository reviewRepository;
    private final PasswordEncoder passwordEncoder;

    private final Random random = new Random();

    @Override
    @Transactional
    public void run(String... args) {
        if (userRepository.count() > 0) {
            log.info("Database already initialized, skipping data initialization");
            return;
        }

        log.info("Starting data initialization...");

        createUsers();
        createCategories();
        createProducts();
        createWarehouses();
        createInventory();
        createOrders();
        createReviews();
        addItemsToUserCarts();

        log.info("Data initialization completed successfully");
    }

    private void createUsers() {
        log.info("Creating sample users...");

        // Create admin user
        User admin = new User();
        admin.setEmail("admin@example.com");
        admin.setPassword(passwordEncoder.encode("admin123"));
        admin.setFirstName("Admin");
        admin.setLastName("User");
        admin.setPhoneNumber("1234567890");
        admin.setRole(User.Role.ADMIN);
        admin.setActive(true);

        // Create customer users
        User customer1 = new User();
        customer1.setEmail("john@example.com");
        customer1.setPassword(passwordEncoder.encode("password123"));
        customer1.setFirstName("John");
        customer1.setLastName("Doe");
        customer1.setPhoneNumber("9876543210");
        customer1.setRole(User.Role.CUSTOMER);
        customer1.setActive(true);

        User customer2 = new User();
        customer2.setEmail("jane@example.com");
        customer2.setPassword(passwordEncoder.encode("password123"));
        customer2.setFirstName("Jane");
        customer2.setLastName("Smith");
        customer2.setPhoneNumber("5551234567");
        customer2.setRole(User.Role.CUSTOMER);
        customer2.setActive(true);

        User customer3 = new User();
        customer3.setEmail("bob@example.com");
        customer3.setPassword(passwordEncoder.encode("password123"));
        customer3.setFirstName("Bob");
        customer3.setLastName("Johnson");
        customer3.setPhoneNumber("3334445555");
        customer3.setRole(User.Role.CUSTOMER);
        customer3.setActive(true);

        // Save users
        List<User> users = userRepository.saveAll(Arrays.asList(admin, customer1, customer2, customer3));

        // Create addresses for users
        createAddresses(users);

        // Create carts for users
        createCarts(users);

        log.info("Created {} users", users.size());
    }

    private void createAddresses(List<User> users) {
        log.info("Creating addresses for users...");

        for (User user : users) {
            // Home address
            Address homeAddress = new Address();
            homeAddress.setUser(user);
            homeAddress.setStreetAddress(random.nextInt(1000) + " Main St");
            homeAddress.setCity("Springfield");
            homeAddress.setState("IL");
            homeAddress.setCountry("USA");
            homeAddress.setZipCode("62701");
            homeAddress.setIsDefault(true);

            // Work address
            Address workAddress = new Address();
            workAddress.setUser(user);
            workAddress.setStreetAddress(random.nextInt(500) + " Business Ave, Suite " + random.nextInt(100));
            workAddress.setCity("Springfield");
            workAddress.setState("IL");
            workAddress.setCountry("USA");
            workAddress.setZipCode("62702");
            workAddress.setIsDefault(false);

            addressRepository.saveAll(Arrays.asList(homeAddress, workAddress));
        }

        log.info("Created addresses for {} users", users.size());
    }

    private void createCarts(List<User> users) {
        log.info("Creating carts for users...");

        for (User user : users) {
            Cart cart = new Cart();
            cart.setUser(user);
            cartRepository.save(cart);
        }

        log.info("Created carts for {} users", users.size());
    }

    private void createCategories() {
        log.info("Creating product categories...");

        // Main categories
        Category electronics = new Category();
        electronics.setName("Electronics");
        electronics.setDescription("Electronic devices and accessories");
        electronics.setImageUrl("https://example.com/images/electronics.jpg");

        Category clothing = new Category();
        clothing.setName("Clothing");
        clothing.setDescription("Apparel and fashion items");
        clothing.setImageUrl("https://example.com/images/clothing.jpg");

        Category homeAndGarden = new Category();
        homeAndGarden.setName("Home & Garden");
        homeAndGarden.setDescription("Home improvement and garden supplies");
        homeAndGarden.setImageUrl("https://example.com/images/home-garden.jpg");

        // Save main categories
        electronics = categoryRepository.save(electronics);
        clothing = categoryRepository.save(clothing);
        homeAndGarden = categoryRepository.save(homeAndGarden);

        // Electronics subcategories
        Category smartphones = new Category();
        smartphones.setName("Smartphones");
        smartphones.setDescription("Mobile phones and accessories");
        smartphones.setImageUrl("https://example.com/images/smartphones.jpg");
        smartphones.setParent(electronics);

        Category laptops = new Category();
        laptops.setName("Laptops");
        laptops.setDescription("Notebook computers and accessories");
        laptops.setImageUrl("https://example.com/images/laptops.jpg");
        laptops.setParent(electronics);

        Category audioDevices = new Category();
        audioDevices.setName("Audio Devices");
        audioDevices.setDescription("Headphones, speakers, and audio equipment");
        audioDevices.setImageUrl("https://example.com/images/audio.jpg");
        audioDevices.setParent(electronics);

        // Clothing subcategories
        Category menClothing = new Category();
        menClothing.setName("Men's Clothing");
        menClothing.setDescription("Clothing for men");
        menClothing.setImageUrl("https://example.com/images/men-clothing.jpg");
        menClothing.setParent(clothing);

        Category womenClothing = new Category();
        womenClothing.setName("Women's Clothing");
        womenClothing.setDescription("Clothing for women");
        womenClothing.setImageUrl("https://example.com/images/women-clothing.jpg");
        womenClothing.setParent(clothing);

        // Home & Garden subcategories
        Category furniture = new Category();
        furniture.setName("Furniture");
        furniture.setDescription("Home and office furniture");
        furniture.setImageUrl("https://example.com/images/furniture.jpg");
        furniture.setParent(homeAndGarden);

        Category kitchenware = new Category();
        kitchenware.setName("Kitchenware");
        kitchenware.setDescription("Kitchen appliances and utensils");
        kitchenware.setImageUrl("https://example.com/images/kitchenware.jpg");
        kitchenware.setParent(homeAndGarden);

        // Save subcategories
        List<Category> subcategories = categoryRepository.saveAll(Arrays.asList(
                smartphones, laptops, audioDevices,
                menClothing, womenClothing,
                furniture, kitchenware
        ));

        log.info("Created {} main categories and {} subcategories", 3, subcategories.size());
    }

    private void createProducts() {
        log.info("Creating sample products...");

        List<Category> categories = categoryRepository.findAll();
        if (categories.isEmpty()) {
            log.warn("No categories found, skipping product creation");
            return;
        }

        // Create products for each category
        for (Category category : categories) {
            // Skip parent categories
            if (category.getParent() == null) {
                continue;
            }

            // Create 3-5 products per category
            int numProducts = 3 + random.nextInt(3);
            for (int i = 0; i < numProducts; i++) {
                Product product = createProductForCategory(category, i);
                productRepository.save(product);
            }
        }

        log.info("Created {} products", productRepository.count());
    }

    private Product createProductForCategory(Category category, int index) {
        Product product = new Product();
        String categoryName = category.getName();

        // Set basic product information
        product.setName(generateProductName(categoryName, index));
        product.setDescription(generateProductDescription(categoryName, index));
        product.setPrice(generatePrice());

        // 30% chance of having a discount
        if (random.nextInt(100) < 30) {
            BigDecimal discountPrice = product.getPrice().multiply(
                    BigDecimal.valueOf(0.7 + (random.nextDouble() * 0.2)));
            product.setDiscountPrice(discountPrice.setScale(2, BigDecimal.ROUND_HALF_UP));
        }

        product.setSku(generateSku(categoryName, index));
        product.setImageUrl("https://example.com/images/products/" +
                categoryName.toLowerCase().replace(" ", "-") + "-" + index + ".jpg");

        product.setBrand(generateBrand(categoryName));
        product.setManufacturer(generateManufacturer(categoryName));

        // Set physical attributes
        product.setWeight(1.0 + random.nextDouble() * 5.0);
        product.setWeightUnit("kg");
        product.setWidth(10.0 + random.nextDouble() * 50.0);
        product.setHeight(5.0 + random.nextDouble() * 30.0);
        product.setDepth(5.0 + random.nextDouble() * 20.0);
        product.setDimensionUnit("cm");

        product.setActive(true);
        product.setCategory(category);

        return product;
    }

    private String generateProductName(String categoryName, int index) {
        String[] adjectives = {"Premium", "Deluxe", "Professional", "Ultimate", "Essential", "Classic"};
        String[] models = {"Pro", "Max", "Elite", "Plus", "Lite", "Standard"};

        String adjective = adjectives[random.nextInt(adjectives.length)];
        String model = models[random.nextInt(models.length)];

        return adjective + " " + categoryName.replace("'s", "") + " " + model + " " + (2023 + index % 3);
    }

    private String generateProductDescription(String categoryName, int index) {
        return "High-quality " + categoryName.toLowerCase() + " with premium features. " +
                "This product offers excellent performance and durability. " +
                "Perfect for everyday use with its sleek design and reliable functionality. " +
                "Model #" + (1000 + index) + " comes with a 1-year warranty.";
    }

    private BigDecimal generatePrice() {
        // Generate prices between $10 and $2000
        double basePrice = 10.0 + random.nextDouble() * 1990.0;
        // Round to 2 decimal places and common price points (.99)
        double roundedPrice = Math.floor(basePrice) + 0.99;
        return BigDecimal.valueOf(roundedPrice).setScale(2, BigDecimal.ROUND_HALF_UP);
    }

    private String generateSku(String categoryName, int index) {
        // Create SKU based on category and index
        String categoryCode = categoryName.substring(0, Math.min(3, categoryName.length())).toUpperCase();
        return categoryCode + "-" + String.format("%04d", 1000 + index);
    }

    private String generateBrand(String categoryName) {
        String[] brands = {
            "TechMaster", "EliteGear", "PrimeBrands", "InnovateX",
            "NextLevel", "TopChoice", "QualityOne", "PremiumTech"
        };
        return brands[random.nextInt(brands.length)];
    }

    private String generateManufacturer(String categoryName) {
        String[] manufacturers = {
            "Global Industries", "Apex Manufacturing", "Superior Products Inc.",
            "Worldwide Exports", "Quality Creators Ltd.", "Prime Manufacturing"
        };
        return manufacturers[random.nextInt(manufacturers.length)];
    }

    private void createWarehouses() {
        log.info("Creating warehouses...");

        // Get admin user to associate with warehouse addresses
        User adminUser = userRepository.findByEmail("admin@example.com")
                .orElseThrow(() -> new RuntimeException("Admin user not found"));

        // Create addresses for warehouses
        Address address1 = new Address();
        address1.setUser(adminUser);
        address1.setStreetAddress("123 Distribution Center Rd");
        address1.setCity("Chicago");
        address1.setState("IL");
        address1.setCountry("USA");
        address1.setZipCode("60007");
        address1 = addressRepository.save(address1);

        Address address2 = new Address();
        address2.setUser(adminUser);
        address2.setStreetAddress("456 Fulfillment Blvd");
        address2.setCity("Los Angeles");
        address2.setState("CA");
        address2.setCountry("USA");
        address2.setZipCode("90001");
        address2 = addressRepository.save(address2);

        Address address3 = new Address();
        address3.setUser(adminUser);
        address3.setStreetAddress("789 Shipping Lane");
        address3.setCity("New York");
        address3.setState("NY");
        address3.setCountry("USA");
        address3.setZipCode("10001");
        address3 = addressRepository.save(address3);

        // Create warehouses in different locations
        Warehouse warehouse1 = new Warehouse();
        warehouse1.setName("Main Distribution Center");
        warehouse1.setCode("DC-MAIN");
        warehouse1.setDescription("Primary distribution center for all products");
        warehouse1.setActive(true);
        warehouse1.setAddress(address1);
        warehouse1.setPhoneNumber("312-555-1234");
        warehouse1.setEmail("dc-main@example.com");
        warehouse1.setManagerName("John Manager");

        Warehouse warehouse2 = new Warehouse();
        warehouse2.setName("West Coast Fulfillment Center");
        warehouse2.setCode("FC-WEST");
        warehouse2.setDescription("Fulfillment center serving the western region");
        warehouse2.setActive(true);
        warehouse2.setAddress(address2);
        warehouse2.setPhoneNumber("213-555-6789");
        warehouse2.setEmail("fc-west@example.com");
        warehouse2.setManagerName("Lisa Supervisor");

        Warehouse warehouse3 = new Warehouse();
        warehouse3.setName("East Coast Fulfillment Center");
        warehouse3.setCode("FC-EAST");
        warehouse3.setDescription("Fulfillment center serving the eastern region");
        warehouse3.setActive(true);
        warehouse3.setAddress(address3);
        warehouse3.setPhoneNumber("212-555-4321");
        warehouse3.setEmail("fc-east@example.com");
        warehouse3.setManagerName("Mark Director");

        List<Warehouse> warehouses = warehouseRepository.saveAll(
                Arrays.asList(warehouse1, warehouse2, warehouse3));

        log.info("Created {} warehouses", warehouses.size());
    }

    private void createInventory() {
        log.info("Creating inventory records...");

        List<Product> products = productRepository.findAll();
        List<Warehouse> warehouses = warehouseRepository.findAll();

        if (products.isEmpty() || warehouses.isEmpty()) {
            log.warn("No products or warehouses found, skipping inventory creation");
            return;
        }

        // Create inventory records for each product across warehouses
        for (Product product : products) {
            // Distribute inventory across warehouses
            for (Warehouse warehouse : warehouses) {
                // Not all products are in all warehouses (70% chance)
                if (random.nextInt(100) < 70) {
                    Inventory inventory = new Inventory();
                    inventory.setProduct(product);
                    inventory.setWarehouse(warehouse);

                    // Generate random quantity between 5 and 100
                    int quantity = 5 + random.nextInt(96);
                    inventory.setQuantity(quantity);

                    // Reserve some items (0-20% of total)
                    int reservedQuantity = (int)(quantity * (random.nextDouble() * 0.2));
                    inventory.setReservedQuantity(reservedQuantity);

                    inventoryRepository.save(inventory);
                }
            }
        }

        log.info("Created {} inventory records", inventoryRepository.count());
    }

    private void createOrders() {
        log.info("Creating sample orders...");

        List<User> users = userRepository.findAll();
        List<Product> products = productRepository.findAll();

        if (users.isEmpty() || products.isEmpty()) {
            log.warn("No users or products found, skipping order creation");
            return;
        }

        // Filter to get only customer users
        List<User> customers = users.stream()
                .filter(user -> user.getRole() == User.Role.CUSTOMER)
                .toList();

        if (customers.isEmpty()) {
            log.warn("No customer users found, skipping order creation");
            return;
        }

        // Create 2-4 orders for each customer
        for (User customer : customers) {
            int numOrders = 2 + random.nextInt(3);

            for (int i = 0; i < numOrders; i++) {
                // Create order with random products
                // Force at least one order to be DELIVERED for each customer
                boolean forceDelivered = (i == 0);
                createOrderForCustomer(customer, products, i, forceDelivered);
            }
        }

        log.info("Created {} orders", orderRepository.count());
    }

    private void createOrderForCustomer(User customer, List<Product> products, int orderIndex, boolean forceDelivered) {
        // Get a random address for the customer
        List<Address> customerAddresses = addressRepository.findByUserId(customer.getId());
        if (customerAddresses.isEmpty()) {
            return;
        }

        Address deliveryAddress = customerAddresses.get(random.nextInt(customerAddresses.size()));

        // Create the order
        Order order = new Order();
        order.setUser(customer);
        order.setOrderNumber("ORD-" + System.currentTimeMillis() + "-" + orderIndex);
        order.setTotalAmount(BigDecimal.ZERO);
        
        // If we need to force a delivered order, set the order date to be older
        if (forceDelivered) {
            order.setOrderedAt(LocalDateTime.now().minusDays(25 + random.nextInt(5)));
        } else {
            order.setOrderedAt(LocalDateTime.now().minusDays(random.nextInt(30)));
        }

        // Set order status based on order date (older orders more likely to be delivered)
        int daysAgo = (int) java.time.temporal.ChronoUnit.DAYS.between(order.getOrderedAt(), LocalDateTime.now());
        if (forceDelivered || daysAgo > 20) {
            order.setOrderStatus(Order.OrderStatus.DELIVERED);
            // Add shipping and delivery dates for delivered orders
            LocalDateTime shippingDate = order.getOrderedAt().plusDays(1);
            LocalDateTime deliveryDate = shippingDate.plusDays(2 + random.nextInt(3));
            // These fields might not exist in the Order entity, so we'll need to check
            if (hasField(order, "shippingDate")) {
                setField(order, "shippingDate", shippingDate);
            }
            if (hasField(order, "deliveryDate")) {
                setField(order, "deliveryDate", deliveryDate);
            }
        } else if (daysAgo > 10) {
            order.setOrderStatus(Order.OrderStatus.SHIPPED);
            // Add shipping date for shipped orders
            LocalDateTime shippingDate = order.getOrderedAt().plusDays(1);
            if (hasField(order, "shippingDate")) {
                setField(order, "shippingDate", shippingDate);
            }
        } else if (daysAgo > 5) {
            order.setOrderStatus(Order.OrderStatus.PROCESSING);
        } else {
            order.setOrderStatus(Order.OrderStatus.PROCESSING);
        }

        // Set payment details
        order.setPaymentMethod("CREDIT_CARD");
        order.setPaymentStatus(daysAgo > 2 ? Order.PaymentStatus.COMPLETED : Order.PaymentStatus.PENDING);

        // Set shipping and billing address
        order.setShippingAddress(deliveryAddress);
        order.setBillingAddress(deliveryAddress); // Using same address for billing

        // Save the order first to get an ID
        order = orderRepository.save(order);

        // Add 1-5 random products to the order
        int numItems = 1 + random.nextInt(5);
        BigDecimal totalAmount = BigDecimal.ZERO;

        for (int i = 0; i < numItems; i++) {
            // Get a random product
            Product product = products.get(random.nextInt(products.size()));

            // Create order item
            OrderItem orderItem = new OrderItem();
            orderItem.setOrder(order);
            orderItem.setProduct(product);

            // Set quantity between 1 and 3
            int quantity = 1 + random.nextInt(3);
            orderItem.setQuantity(quantity);

            // Use discount price if available
            BigDecimal unitPrice = product.getDiscountPrice() != null ?
                    product.getDiscountPrice() : product.getPrice();
            orderItem.setUnitPrice(unitPrice);

            // Calculate subtotal
            BigDecimal subtotal = unitPrice.multiply(BigDecimal.valueOf(quantity));
            orderItem.setSubtotal(subtotal);

            // Add to total amount
            totalAmount = totalAmount.add(subtotal);

            orderItemRepository.save(orderItem);
        }

        // Update order with total amount
        order.setTotalAmount(totalAmount);

        // Save updated order
        orderRepository.save(order);
    }

    private void createReviews() {
        log.info("Creating product reviews...");
        
        try {
            // Instead of using findByOrderStatus, get all orders and filter manually
            List<Order> allOrders = orderRepository.findAll();
            List<Order> deliveredOrders = allOrders.stream()
                .filter(order -> order.getOrderStatus() == Order.OrderStatus.DELIVERED)
                .toList();
                
            if (deliveredOrders.isEmpty()) {
                log.warn("No delivered orders found, skipping review creation");
                return;
            }
            
            // For each delivered order, create reviews for some of the products
            for (Order order : deliveredOrders) {
                // Get the user who placed the order
                User user = order.getUser();
                
                // Get the order items
                Set<OrderItem> orderItems = order.getOrderItems();
                if (orderItems.isEmpty()) {
                    continue;
                }
                
                // Create a review for each product (with 70% probability)
                for (OrderItem orderItem : orderItems) {
                    if (random.nextDouble() <= 0.7) {
                        Product product = orderItem.getProduct();
                        
                        Review review = new Review();
                        review.setUser(user);
                        review.setProduct(product);
                        
                        // Generate a rating between 1 and 5
                        int rating = 1 + random.nextInt(5);
                        review.setRating(rating);
                        
                        // Generate a comment based on the rating
                        review.setComment(generateReviewComment(rating));
                        
                        // Set verified status
                        review.setVerified(true);
                        
                        // Set review date after order date but before now
                        LocalDateTime orderDate = order.getOrderedAt();
                        LocalDateTime now = LocalDateTime.now();
                        long daysBetween = java.time.temporal.ChronoUnit.DAYS.between(orderDate, now);
                        
                        if (daysBetween > 0) {
                            long randomDays = 1 + random.nextInt((int) Math.min(daysBetween, 14)); // Review within 14 days of order
                            LocalDateTime reviewDate = orderDate.plusDays(randomDays);
                            // Use reflection to set createdAt since it's managed by @PrePersist
                            setField(review, "createdAt", reviewDate);
                            setField(review, "updatedAt", reviewDate);
                        }
                        
                        reviewRepository.save(review);
                    }
                }
            }
            
            log.info("Created product reviews");
        } catch (Exception e) {
            log.error("Error creating reviews: {}", e.getMessage());
        }
    }

    private String generateReviewComment(int rating) {
        String[] positiveComments = {
            "This product exceeded my expectations. The quality is excellent and it works perfectly.",
            "I'm very happy with this purchase. Fast shipping and the product is exactly as described.",
            "Excellent product that offers great value for money. Would definitely buy again.",
            "This is exactly what I was looking for. High quality and works perfectly.",
            "Very impressed with this product. It's well-made and performs great."
        };

        String[] neutralComments = {
            "Decent product that does what it's supposed to do. Nothing extraordinary but no complaints.",
            "Product is good but could be better. It meets my basic needs.",
            "Satisfied with the purchase. It's a good product for the price.",
            "Works as expected. No issues so far but nothing exceptional either.",
            "Good product overall. Some minor issues but nothing significant."
        };

        if (rating >= 4) {
            return positiveComments[random.nextInt(positiveComments.length)];
        } else {
            return neutralComments[random.nextInt(neutralComments.length)];
        }
    }

    private void addItemsToUserCarts() {
        log.info("Adding items to users' carts...");

        List<User> users = userRepository.findAll();
        List<Product> products = productRepository.findAll();

        // Skip the admin user (first user)
        for (int i = 1; i < users.size(); i++) {
            User user = users.get(i);
            Optional<Cart> cartOptional = cartRepository.findByUserId(user.getId());

            if (cartOptional.isEmpty()) {
                log.warn("No cart found for user {}, skipping", user.getId());
                continue;
            }

            Cart cart = cartOptional.get();

            // Add 1-3 random products to each user's cart
            int numItems = 1 + random.nextInt(3);
            BigDecimal cartTotal = BigDecimal.ZERO;

            for (int j = 0; j < numItems; j++) {
                // Get a random product
                Product product = products.get(random.nextInt(products.size()));

                // Create cart item
                CartItem cartItem = new CartItem();
                cartItem.setCart(cart);
                cartItem.setProduct(product);

                // Set quantity between 1 and 3
                int quantity = 1 + random.nextInt(3);
                cartItem.setQuantity(quantity);

                // Use discount price if available
                BigDecimal unitPrice = product.getDiscountPrice() != null ?
                        product.getDiscountPrice() : product.getPrice();
                cartItem.setUnitPrice(unitPrice);

                // Calculate subtotal
                BigDecimal subtotal = unitPrice.multiply(BigDecimal.valueOf(quantity));
                cartItem.setSubtotal(subtotal);

                // Add to cart total
                cartTotal = cartTotal.add(subtotal);

                cartItemRepository.save(cartItem);
            }

            // Update cart total
            cart.setTotalAmount(cartTotal);
            cartRepository.save(cart);
        }

        log.info("Added items to users' carts");
    }

    // Helper methods for reflection-based field access
    private boolean hasField(Object object, String fieldName) {
        try {
            object.getClass().getDeclaredField(fieldName);
            return true;
        } catch (NoSuchFieldException e) {
            return false;
        }
    }

    private void setField(Object object, String fieldName, Object value) {
        try {
            java.lang.reflect.Field field = object.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            field.set(object, value);
        } catch (Exception e) {
            log.warn("Could not set field {} on {}: {}", fieldName, object.getClass().getSimpleName(), e.getMessage());
        }
    }
}
