# EnterpriseApp – Demo Backend Aplikace

Tento projekt je demonstrační backendová aplikace postavená na frameworku **Spring Boot** (s využitím Maven). Umožňuje základní správu uživatelů přes REST API, autentizaci pomocí JWT tokenu, logování přístupů do databáze a monitoring běhu serveru.

- Tento projekt představuje ukázkovou enterprise webovou aplikaci vyvinutou v rámci cvičení.
- Cílem bylo vytvořit robustní backend systém se správou uživatelů, bezpečnostními mechanismy, vícevrstvou architekturou, validací vstupů, dokumentací API a základním monitoringem.
- Aplikace je postavena na frameworku Spring Boot a používá moderní přístupy k návrhu enterprise backendů.


## Funkce

- ✅ Registrace a mazání uživatelů
- ✏️ Změna e-mailu a hesla
- 🔐 Přihlášení a generování JWT tokenu
- 🔁 Obnovení access tokenu pomocí refresh tokenu
- 📋 Role-based přístup pomocí entit `User` a `Role`
- 📊 Logování každého požadavku do tabulky `audit_log`
- 📈 Monitoring pomocí Spring Actuator
- 📚 Dokumentace API pomocí Swagger UI (OpenAPI)

## Technologie

- **Java 17**
- **Spring Boot 3.4**
- **Spring Security (JWT)**
- **MySQL**
- **Swagger / OpenAPI**
- **Spring Actuator**
- **Maven**

## Architektura systému
- Aplikace využívá vícevrstvou architekturu s jasným oddělením odpovědností:

### Controller vrstva (controller)
- Přijímá HTTP požadavky, volá služby a vrací odpovědi klientům (REST API).

### Service vrstva (service)
- Obsahuje obchodní logiku a koordinuje práci s daty.

### Repository vrstva (repository)
- Přistupuje k databázi pomocí Spring Data JPA.

### Security vrstva (security)
- Zajišťuje autentizaci a autorizaci pomocí JWT tokenů a pravidel RBAC.

### Modely / DTOs (model, dto)
- Definují entity a datové objekty pro komunikaci mezi vrstvami.

## Endpoints (ukázka)

| Metoda | URL                   | Popis                          | Ochrana |
|--------|------------------------|--------------------------------|---------|
| POST   | `/api/auth/register`   | Registrace nového uživatele    | ❌      |
| POST   | `/api/auth/login`      | Přihlášení a získání tokenu    | ❌      |
| POST   | `/api/auth/refresh`    | Obnovení access tokenu         | ❌      |
| DELETE | `/api/users/{id}`      | Smazání uživatele              | ✅      |
| PATCH  | `/api/users/email`     | Změna e-mailu                  | ✅      |
| PATCH  | `/api/users/password`  | Změna hesla                    | ✅      |
| GET    | `/actuator/health`     | Stav serveru                   | ✅      |
| GET    | `/swagger-ui/index.html` | Swagger dokumentace          | ❌      |

## Popis implementovaných bezpečnostních mechanismů

### JWT autentizace
- Po přihlášení je vygenerován JWT token, který klient používá k autorizaci dalších požadavků.

### Role-Based Access Control (RBAC)
- Každý uživatel má přiřazené role. Přístup k chráněným endpointům je řízen podle rolí (ROLE_USER, ROLE_ADMIN).

### Hashování hesel
- Uživatelova hesla jsou ukládána bezpečně pomocí algoritmu BCrypt.

### Ochrana API endpointů
- Přístup na citlivé části (např. /actuator/**) je povolen pouze pro přihlášené administrátory.

### Stateless session management
- Server nespravuje žádné session, vše je řízeno pomocí JWT.

## Monitoring
-Integrovaný pomocí Spring Boot Actuator:

/actuator/health — kontrola dostupnosti systému.
/actuator/metrics — základní metriky aplikace (počty požadavků, paměť, vlákna apod.).

Přístup k Actuator endpointům je zabezpečen — pouze přihlášený uživatel s rolí ADMIN má přístup.

## Databázové schéma

### users
```sql
CREATE TABLE users (
id BIGINT AUTO_INCREMENT PRIMARY KEY,
username VARCHAR(255) NOT NULL UNIQUE,
password VARCHAR(255) NOT NULL,
email VARCHAR(255),
created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
updated_at DATETIME DEFAULT CURRENT_TIMESTAMP
);
```


### roles
```sql
CREATE TABLE roles (
id BIGINT AUTO_INCREMENT PRIMARY KEY,
name VARCHAR(255) NOT NULL
);
```

### users_roles
```sql
CREATE TABLE users_roles (
user_id BIGINT NOT NULL,
role_id BIGINT NOT NULL,
PRIMARY KEY (user_id, role_id),
FOREIGN KEY (user_id) REFERENCES users(id),
FOREIGN KEY (role_id) REFERENCES roles(id)
);
```

### users_roles
```sql
CREATE TABLE audit_log (
id BIGINT AUTO_INCREMENT PRIMARY KEY,
timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
entity_name VARCHAR(255) NOT NULL,
entity_id BIGINT,
field_name VARCHAR(255),
old_value TEXT,
new_value TEXT,
action_type VARCHAR(50) NOT NULL,
ip_address VARCHAR(255) NOT NULL,
log_level VARCHAR(255) NOT NULL,
actor_user_id MEDIUMTEXT
);
```

## Spuštění projektu
* Otevři projekt ve své IDE.
* Uprav připojení k MySQL databázi v application.properties.
* Spusť aplikaci
* mvn spring-boot:run

## Přístup ke Swagger UI:
http://localhost:8080/swagger-ui/index.html
