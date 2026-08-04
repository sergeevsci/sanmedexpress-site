# Backend СанМедЭкспресс

Spring Boot backend для заявок с сайта.

## Что есть

- `POST /api/requests` - прием заявки с формы.
- PostgreSQL - хранение клиентов и заявок.
- Flyway - миграции БД.
- `/admin/` - простая админка заявок.
- Basic Auth для админки.
- Email-уведомление о новой заявке, включается через переменные окружения.
- Telegram-уведомление о новой заявке, включается через переменные окружения.

## Переменные окружения

- `SPRING_DATASOURCE_URL` - JDBC URL PostgreSQL.
- `SPRING_DATASOURCE_USERNAME` - пользователь БД.
- `SPRING_DATASOURCE_PASSWORD` - пароль БД.
- `ADMIN_USERNAME` - логин админки.
- `ADMIN_PASSWORD` - пароль админки.
- `EMAIL_ENABLED` - `true` или `false`, включает отправку email.
- `EMAIL_TO` - куда отправлять заявки, сейчас по умолчанию `sergeevsci@yandex.ru`.
- `EMAIL_FROM` - почта отправителя.
- `MAIL_HOST`, `MAIL_PORT`, `MAIL_USERNAME`, `MAIL_PASSWORD` - SMTP отправителя.
- `MAIL_SMTP_AUTH`, `MAIL_SMTP_STARTTLS` - настройки SMTP.
- `TELEGRAM_ENABLED` - `true` или `false`, включает Telegram-уведомления.
- `TELEGRAM_BOT_TOKEN` - токен бота от `@BotFather`.
- `TELEGRAM_CHAT_ID` - ID чата, куда отправлять заявки.

## Где менять email получателя

В `.env` в корне проекта:

```env
EMAIL_TO=sergeevsci@yandex.ru
```

## Где менять SMTP-почту отправителя

В `.env` в корне проекта:

```env
EMAIL_FROM=site@your-domain.ru
MAIL_HOST=smtp.example.com
MAIL_PORT=587
MAIL_USERNAME=site@your-domain.ru
MAIL_PASSWORD=mail-password
```
