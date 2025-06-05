# E-Commerce Application Data Model

This document describes the data model for the e-commerce application, including all entities and their relationships.

## Core Entities

### User
Represents users of the application including customers and administrators.

**Key Attributes:**
- `id`: Unique identifier
- `email`: User's email address (unique)
- `password`: Encrypted password
- `firstName`, `lastName`: User's name
- `phoneNumber`: Contact information
- `role`: ADMIN or CUSTOMER
- `active`: Account status flag

**Relationships:**
- One-to-Many with `Address`: A user can have multiple shipping/billing addresses
- One-to-Many with `Order`: A user can place multiple orders
- One-to-One with `Cart`: Each user has a single shopping cart
- One-to-Many with `Review`: A user can submit multiple product reviews

### Product
The core entity representing items available for purchase.

**Key Attributes:**
- `id`: Unique identifier
- `name`, `description`: Product details
- `price`, `discountPrice`: Pricing information
- `sku`: Stock Keeping Unit (unique product code)
- `imageUrl`: Product image
- `brand`, `manufacturer`: Product origin
- `dimensions` and `weight`: Physical characteristics
- `active`: Product availability flag

**Relationships:**
- Many-to-One with `Category`: A product belongs to one category
- One-to-Many with `Review`: A product can have multiple reviews
- One-to-Many with `CartItem`: A product can be in multiple shopping carts
- One-to-Many with `OrderItem`: A product can be in multiple orders
- One-to-Many with `Inventory`: A product can have inventory in multiple warehouses

### Category
Organizes products into hierarchical categories.

**Key Attributes:**
- `id`: Unique identifier
- `name`: Category name
- `description`: Category details
- `imageUrl`: Category image

**Relationships:**
- Self-referencing relationship: Categories can have parent/child relationships
- One-to-Many with `Product`: A category can contain multiple products

### Order
Represents a customer's purchase.

**Key Attributes:**
- `id`: Unique identifier
- `orderNumber`: Unique order reference number
- `totalAmount`: Order total
- `orderStatus`: PROCESSING, SHIPPED, DELIVERED, CANCELLED
- `paymentStatus`: PENDING, COMPLETED, FAILED, REFUNDED
- `trackingNumber`: Shipping tracking information
- `orderedAt`: Timestamp of order placement

**Relationships:**
- Many-to-One with `User`: An order belongs to one user
- One-to-Many with `OrderItem`: An order contains multiple order items
- Many-to-One with `Address`: An order has shipping and billing addresses

### Cart
Represents a user's shopping cart.

**Key Attributes:**
- `id`: Unique identifier
- `totalAmount`: Total cost of items in cart

**Relationships:**
- One-to-One with `User`: A cart belongs to one user
- One-to-Many with `CartItem`: A cart contains multiple cart items

## Inventory Management

### Inventory
Tracks stock levels for products across warehouses.

**Key Attributes:**
- `id`: Unique identifier
- `quantity`: Stock quantity
- `reservedQuantity`: Quantity reserved for pending orders
- `reorderThreshold`: Level at which to reorder
- `reorderQuantity`: Amount to order when threshold reached
- `status`: AVAILABLE, LOW_STOCK, OUT_OF_STOCK, DISCONTINUED
- `batchNumber`, `expiryDate`: For batch tracking

**Relationships:**
- Many-to-One with `Product`: An inventory record belongs to one product
- Many-to-One with `Warehouse`: An inventory record belongs to one warehouse

### Warehouse
Physical locations where products are stored.

**Key Attributes:**
- `id`: Unique identifier
- `name`, `code`: Warehouse identifiers
- `description`: Additional information
- `active`: Warehouse status
- `phoneNumber`, `email`, `managerName`: Contact information

**Relationships:**
- One-to-One with `Address`: A warehouse has one address
- One-to-Many with `Inventory`: A warehouse contains multiple inventory records

## Support Entities

### Address
Represents physical addresses for users and warehouses.

**Key Attributes:**
- `id`: Unique identifier
- `streetAddress`, `city`, `state`, `country`, `zipCode`: Address details
- `isDefault`: Flag for a user's default address

**Relationships:**
- Many-to-One with `User`: An address belongs to one user
- One-to-One with `Warehouse`: An address can be associated with one warehouse

### OrderItem
Represents an individual product in an order.

**Key Attributes:**
- `id`: Unique identifier
- `quantity`: Number of products ordered
- `unitPrice`: Price per unit at time of order
- `subtotal`: Total price for this item

**Relationships:**
- Many-to-One with `Order`: An order item belongs to one order
- Many-to-One with `Product`: An order item refers to one product

### CartItem
Represents an individual product in a shopping cart.

**Key Attributes:**
- `id`: Unique identifier
- `quantity`: Number of products in cart
- `unitPrice`: Current price per unit
- `subtotal`: Total price for this item

**Relationships:**
- Many-to-One with `Cart`: A cart item belongs to one cart
- Many-to-One with `Product`: A cart item refers to one product

### Review
Customer feedback and ratings for products.

**Key Attributes:**
- `id`: Unique identifier
- `rating`: Numeric rating (typically 1-5)
- `comment`: Text review
- `verified`: Flag indicating if reviewer purchased the product

**Relationships:**
- Many-to-One with `Product`: A review is for one product
- Many-to-One with `User`: A review is written by one user

## Entity Relationship Diagram (Conceptual)

```
User 1──* Address
 │
 │1
 ▼
Cart 1──* CartItem ───* Product *─── Category
                        │    ▲
                        │    │
                        ▼    │
                     Review *─1 User
                        │
                        │
User 1───* Order 1───* OrderItem *─1 Product
           │
           │
           ▼
        Address
        
Product 1───* Inventory *───1 Warehouse ───1 Address
```

## Key Design Decisions

1. **User Roles**: The system distinguishes between admin and customer users using the Role enum.

2. **Inventory Management**: Products don't directly store stock quantity but calculate it from Inventory records, enabling multi-warehouse inventory tracking.

3. **Order Status**: Orders have separate statuses for fulfillment (OrderStatus) and payment (PaymentStatus).

4. **Shopping Cart**: Each user has a dedicated cart that persists between sessions.

5. **Category Hierarchy**: Categories can have subcategories through a self-referencing relationship.

6. **Address Reuse**: Addresses are linked to users rather than embedded in orders, allowing for address reuse across orders.

7. **Stock Reservation**: The inventory system tracks both total quantity and reserved quantity to prevent overselling.

8. **Audit Fields**: Most entities include created/updated timestamps for auditing purposes.
