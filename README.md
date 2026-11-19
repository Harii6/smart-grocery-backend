Smart Grocery

Smart Grocery is a prototype for planning a week's dinners and exploring what the ingredients might cost at Norwegian grocery stores. The aim is to make meal planning, shopping choices, and a household budget easier to see in one place. It is a demonstration of the idea, not a finished shopping service or a live price guarantee.

What the prototype does

Visitors can see a welcome page, create an account, sign in, and sign out.

In Settings, a signed-in user can enter household size, weekly budget, maximum number of stores, location and search radius, food preferences, and allergens.

The Meals page requests recipe suggestions from an AI service using the household settings. Users can choose meals for their week; ingredient amounts are combined into a shopping list.

The Shopping page shows matching grocery products, lets users change product selections, and calculates a basket total against their budget and store limit. It can suggest a lower-cost combination from the product options it retrieved.

The Products page lets users search for grocery products.

The backend stores a snapshot of a generated shopping list and, when the data is comparable, can calculate an estimated difference from a single-store basket. This does not mean the Savings page currently shows historical savings.

Try the user journey

Open the welcome page and create an account.

Sign in and complete Settings. You are sent to Settings after login.

Go to Meals, generate suggestions, and choose meals for the week.

Open Shopping to review product options, adjust selections, and calculate the total.

Visit Products to try a separate product search, and Home to see the app navigation.

Visit Savings to see the planned dashboard, then use Log out in the sidebar.

What is not finished

Savings dashboard: The week/month/year cards and chart are placeholders. There is no completed flow for confirmed purchases or a year of savings history. A stored estimate must not be presented as money the user actually saved.

Price and product accuracy: Product matches and package quantities can be missing or incompatible. Comparisons are limited to retrieved options; prices, stock, travel costs, and delivery costs are not verified. Some comparisons therefore remain unavailable.

Meal safety and quality: AI-generated recipes and nutrition figures are suggestions, not verified facts. Check ingredients and product labels yourself, especially for allergens. The budget is evaluated through shopping estimates; the AI cannot guarantee that every recipe meets it.

Persistence: Some meal choices and shopping state are kept in the browser's local storage, so they may not follow you to a different browser or device. Accounts, settings, and shopping-list snapshots use PostgreSQL.

Deployment: This repository snapshot is a local application, not a hosted website. Hosting, production database setup, secret management, monitoring, and operational ownership are separate handoff decisions.

Technology

Java 21, Spring Boot, Spring Security, PostgreSQL, and plain HTML/CSS/JavaScript. The backend calls the OpenAI API for recipe suggestions, Kassalapp for product search, and OpenStreetMap Nominatim for location lookup. These external services need access and may have usage limits or costs.

Run locally (developer setup)

Install Java 21, Maven, and PostgreSQL. The current configuration expects a database named smart_grocery on localhost:5433. Update src/main/resources/application.properties for a different database or port.

Create the database tables before starting the application. spring.jpa.hibernate.ddl-auto=validate checks the schema but does not create it. The SQL schema is not included in this project snapshot, so a fresh installation is blocked until the project owner adds the schema/migration files. An existing configured local database can be used for a demo.

Supply API keys as environment variables; never commit real keys. In PowerShell:

$env:AI_API_KEY = "your-openai-api-key"
$env:KASSALAPP_API_KEY = "your-kassalapp-api-key"

From the repository root, start the app with mvn spring-boot:run and open http://localhost:8080/. If your checkout includes the Maven wrapper, ./mvnw.cmd spring-boot:run works in PowerShell instead.

The database password currently lives in application.properties for local development. Move it to an environment-based configuration, replace the local default, and review the other production settings before publishing or hosting the app. Do not upload API keys, real user data, or database backups to GitHub.

Tests

Run mvn test from the repository root (or ./mvnw.cmd test if the wrapper is present). Maven's normal test run may not include files ending in IT; to include the security integration test explicitly, run:

mvn '-Dtest=*Test,*Tests,*IT' test

The test profile has test API values, but database-backed integration tests may require a local PostgreSQL instance. Passing automated tests does not establish real-world price accuracy, safe allergen handling, load capacity, or production readiness.

Handoff

The GitHub repository is the source-code handoff. A separate demo video or live demo shows the intended flow to non-developers. If a public link is required, someone must deploy and maintain the application and database under an agreed account, supply the API credentials, and pay any hosting or API charges. A repository link by itself is not a working hosted website.
