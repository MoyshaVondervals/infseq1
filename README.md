# Информационная безопасность

## Лабораторная работа №1

### Стек

- Java/Spring
- Hibernate
- PostgreSQL

### Описание API

`POST /auth/login`: метод для аутентификации пользователя. Принимает логин и пароль, проверяет учетные данные в базе данных и при успешном входе возвращает JWT-токен.

```json
{
  "login": "admin",
  "password": "admin123"
}
```

`GET /api/data`: метод для получения списка данных из PostgreSQL. Доступ разрешен только аутентифицированным пользователям с действующим JWT-токеном, переданным в заголовке `Authorization: Bearer <token>`.

`POST /auth/logout`: метод для выхода из системы. Удаляет текущий JWT-токен из списка активных токенов, после чего его нельзя использовать для доступа к защищенным эндпоинтам.

### Описание реализованных мер защиты

- От **SQLi** код защищен с помощью ORM Hibernate и параметризованных запросов.
- От **XSS** код защищен экранированием возвращаемых данных через `HtmlUtils.htmlEscape()`.
- Пароли хранятся в виде BCrypt-хешей и не записываются в базу в открытом виде.
- После успешного входа выдается JWT, подписанный алгоритмом HS256 и имеющий ограниченный срок действия.
- Секрет подписи JWT передается через переменную окружения `JWT_SECRET` и не хранится в коде.
- Middleware `AuthFilter` проверяет подпись, срок действия JWT и наличие токена среди активных токенов в PostgreSQL.
- При вызове `POST /auth/logout` токен удаляется из базы и становится недействительным.

```java
AppUser user = userRepository.findByUsername(username).orElse(null);
```

```java
List<String> items = dataItemRepository.findAllByOrderByIdAsc().stream()
        .map(DataItem::getContent)
        .map(HtmlUtils::htmlEscape)
        .toList();
```

```java
return Jwts.builder()
        .subject(username)
        .issuedAt(Date.from(issuedAt))
        .expiration(Date.from(issuedAt.plusSeconds(expirationSeconds)))
        .signWith(signingKey, Jwts.SIG.HS256)
        .compact();
```

### Отчеты из pipeline

Dependency-check:

![Снимок экрана 2026-09-16 в 22.07.27.png](imgs/%D0%A1%D0%BD%D0%B8%D0%BC%D0%BE%D0%BA%20%D1%8D%D0%BA%D1%80%D0%B0%D0%BD%D0%B0%202026-09-16%20%D0%B2%2022.07.27.png)

Spotbugs:

![Снимок экрана 2026-09-16 в 22.13.42.png](imgs/%D0%A1%D0%BD%D0%B8%D0%BC%D0%BE%D0%BA%20%D1%8D%D0%BA%D1%80%D0%B0%D0%BD%D0%B0%202026-09-16%20%D0%B2%2022.13.42.png)

Последний запуск пайплайна
https://github.com/MoyshaVondervals/infseq1/actions/runs/35139402878
