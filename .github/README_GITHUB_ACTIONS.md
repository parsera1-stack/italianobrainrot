# GitHub Actions — Автоматическая сборка APK

## Быстрый старт

### 1. Загрузите проект на GitHub
```bash
git init
git add .
git commit -m "Initial commit"
git branch -M main
git remote add origin https://github.com/ВАШ_НИК/ItalianoBrainRot.git
git push -u origin main
```

### 2. Сборка Debug APK (автоматически)
- При каждом пуше в `main` ветку
- Или нажмите **Actions → Build APK → Run workflow**
- APK появится в **Actions → последний запуск → Artifacts**

### 3. Сборка подписанного Release APK
1. Создайте keystore локально:
```bash
keytool -genkey -v -keystore italiano_keystore.jks -alias italiano -keyalg RSA -keysize 2048 -validity 10000
```

2. Добавьте секреты в GitHub репозиторий:
   - **Settings → Secrets and variables → Actions → New repository secret**
   - `KEYSTORE_BASE64`: `base64 -w 0 italiano_keystore.jks`
   - `KEYSTORE_PASSWORD`: ваш пароль от keystore
   - `KEY_ALIAS`: `italiano`
   - `KEY_PASSWORD`: пароль от ключа

3. Запустите: **Actions → Build Signed Release APK → Run workflow**

## Артефакты

| Workflow | Триггер | APK |
|----------|---------|-----|
| `build.yml` | Пуш в main / PR / Ручной | Debug + Release (unsigned) |
| `build-signed.yml` | Только ручной | Release (подписанный) |

## Скачивание APK

1. Откройте **Actions** в вашем репозитории
2. Выберите последний успешный запуск (✅ зелёная галочка)
3. Прокрутите вниз до **Artifacts**
4. Скачайте `app-debug` или `app-release`
5. Переименуйте `.zip` в `.apk` (GitHub упаковывает в ZIP)
6. Установите на телефон!
