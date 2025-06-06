# Gamazon inc.
```
 ________  ________  _____ ______   ________  ________  ________  ________      
|\   ____\|\   __  \|\   _ \  _   \|\   __  \|\_____  \|\   __  \|\   ___  \    
\ \  \___|\ \  \|\  \ \  \\\__\ \  \ \  \|\  \\|___/  /\ \  \|\  \ \  \\ \  \   
 \ \  \  __\ \   __  \ \  \\|__| \  \ \   __  \   /  / /\ \  \\\  \ \  \\ \  \  
  \ \  \|\  \ \  \ \  \ \  \    \ \  \ \  \ \  \ /  /_/__\ \  \\\  \ \  \\ \  \ 
   \ \_______\ \__\ \__\ \__\    \ \__\ \__\ \__\\________\ \_______\ \__\\ \__\
    \|_______|\|__|\|__|\|__|     \|__|\|__|\|__|\|_______|\|_______|\|__| \|__|
```

```
████████╗███████╗███╗   ███╗██╗   ██╗     ██████╗ ███╗   ██╗    ███████╗████████╗███████╗██████╗  ██████╗ ██╗██████╗ ███████╗
╚══██╔══╝██╔════╝████╗ ████║██║   ██║    ██╔═══██╗████╗  ██║    ██╔════╝╚══██╔══╝██╔════╝██╔══██╗██╔═══██╗██║██╔══██╗██╔════╝
   ██║   █████╗  ██╔████╔██║██║   ██║    ██║   ██║██╔██╗ ██║    ███████╗   ██║   █████╗  ██████╔╝██║   ██║██║██║  ██║███████╗
   ██║   ██╔══╝  ██║╚██╔╝██║██║   ██║    ██║   ██║██║╚██╗██║    ╚════██║   ██║   ██╔══╝  ██╔══██╗██║   ██║██║██║  ██║╚════██║
   ██║   ███████╗██║ ╚═╝ ██║╚██████╔╝    ╚██████╔╝██║ ╚████║    ███████║   ██║   ███████╗██║  ██║╚██████╔╝██║██████╔╝███████║
   ╚═╝   ╚══════╝╚═╝     ╚═╝ ╚═════╝      ╚═════╝ ╚═╝  ╚═══╝    ╚══════╝   ╚═╝   ╚══════╝╚═╝  ╚═╝ ╚═════╝ ╚═╝╚═════╝ ╚══════╝
```
## IN CODE WE TRUST


# Gamazon E-Commerce Platform

**Gamazon** is a modular e-commerce platform built with **Spring Boot 3**, **Vaadin 24**, and **WebSocket (STOMP)** messaging. It follows an **MVP architecture** and includes support for multi-user scenarios, store management, permission handling, auctions, and real-time updates.

---

## Features

- Guest and member registration and authentication  
- Persistent session and user management  
- Store and product catalog creation  
- Item and stock management  
- Store owner and manager appointment  
- Fine-grained permission system (e.g., `HANDLE_INVENTORY`, `OVERSEE_OFFERS`)  
- Auction-based item listing and bidding  
- Real-time notifications using STOMP over WebSocket  
- Queued message delivery when users reconnect  
- JSON-driven system initialization via `AppInitializer`  
- MVP-structured Vaadin UI for a responsive frontend  
- Dual repository backends: JPA and in-memory  
- Modular domain-service-facade-UI layering  

---

## Getting Started

### Prerequisites

- Java 21 (Eclipse Adoptium or Oracle)  
- Maven 3.x  

###  Run with Maven

```bash
 mvn spring-boot:ru
```

If compiled, use: 
```bash
 "C:\Program Files\Eclipse Adoptium\jdk-21.0.6.7-hotspot\bin\java.exe" "@C:\Users\user1\AppData\Local\Temp\cp_2sbeja7uebqkjdxke03tsvwiz.argfile" "UI.GamazonApplication"
```

### Access the Application
Open your browser and go to: http://localhost:8080


### System Initialization with JSON
At startup, the system can execute a predefined set of actions described in a JSON file.
Place your JSON file at: src/main/resources/init/appInitializer.json

### Format


{
  "marketWithTwoStores": [
    { "action": "guestEntry", "as": "guest1" },
    { "action": "registerUser", "session": "guest1", "username": "admin", "password": "Admin123!@", "email": "admin@g.com", "as": "admin" },
    { "action": "registerUser", "session": "guest2", "username": "buyer", "password": "Buyer123!@", "email": "buyer@g.com", "as": "buyer" },
    { "action": "openMarket", "session": "admin" },
   ...
  ],

  "mentoringScenario": [
    { "action": "guestEntry", "as": "g1" },
    { "action": "registerUser", "session": "g1", "username": "u1", "password": "Admin123!@", "email": "u1@ex.com", "as": "u1" },
   ...
  ]
}

### Supported JSON Actions 
Below is a list of available action types, their parameters, and what they do.

   User Actions
      Action                  Description                            Parameters
   guestEntry	            Creates a guest session	            as (alias for storing session token)
   registerUser	 Registers a user using a guest session	   session, username, password, email, as
   loginUser       Logs in with a username and password	      sername, password, as
   logout          Logs out a session	                         session

   Market & Store Actions
      Action                     Description                            Parameters
   openMarket	               Opens the market	                     Opens the market	
   addStore	                  Adds a store	                        session, name, category, as
   appointManager	            Makes a user a store manager	         session, store, target
   appointStoreManager	      (Alias) same as appointManager	      Same
   appointStoreOwner	         Adds a co-owner to the store	         session, store, target
   changePermissions	         Changes manager permissions	         session, store, target, permissions
   changeManagerPermissions	(Alias) same as above	               Same
   ban	                  Temporarily or permanently bans a user	   session, target, experationDate
   unban	                  Unbans a previously banned user	         session, target


   Product & Item Actions
      Action                     Description                        Parameters
   addProduct	            Adds a new product	   session, name, categories, keywords, as
   addItem        Adds a product to a store's stock      session, store, product, price, quantity,description

   Shopping & Auction Actions
      Action                     Description                            Parameters
   addAuction	      Starts an auction for a product	    session, store, product, auctionEndDate, startPrice
   addToCart	      Adds a product to a user's cart	     session, store, product, quantity
   makeBid	         Places a bid on an auction	           session, auction, price, cardNumber,expiryDate,   
                                                            cvv, andIncrement, clientName, deliveryAddress
   checkout	         Checks out the cart with payment info	   session, cardNumber, expiryDate, cvv, 
                                                               andIncrement, clientName, deliveryAddress
### Example: Adding a New Scenario
   1. Open the file: src/main/resources/config/init.json
   2. Add a new state entry:
      "testScenario": [
            { "action": "guestEntry", "as": "g1" },
            { "action": "registerUser", "session": "g1", "username": "demo", "password": "Demo123!", "email": "demo@g.com", "as": "u1" },
            { "action": "addStore", "session": "u1", "name": "Test Store", "category": "Misc", "as": "s1" }
      ]  


   3. In application.properties set: app.init.state=testScenario
   4. On next run, this script will be executed automatically.

