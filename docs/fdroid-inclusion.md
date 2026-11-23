# Подготовка SYSREF к F-Droid

Повторяемая инструкция: как репозиторий готовили к заявке в [fdroiddata](https://gitlab.com/fdroid/fdroiddata) и как сделать то же самое для следующего выпуска.

Идентификатор приложения: `com.sysref.commands`. Корень Gradle — корень репозитория (`settings.gradle.kts`, `gradlew`). Модуль — `app`. В метаданных fdroiddata из-за этого стоит `subdir: app`: каталог `build/` появляется в `app/`, а `gradlew` F-Droid находит уровнем выше.

Черновик рецепта сборки лежит в `docs/fdroid-metadata.yml`. В сборку приложения он не входит. В fdroiddata тот же текст кладут как `metadata/com.sysref.commands.yml`. Файл `metadata/<applicationId>.yml` в этом репозитории не создают.

## Когда что делать

Первая публикация:

1. Проверить дерево, подпись, версию, зависимости и лицензии.
2. Обновить Fastlane.
3. Собрать неподписанный release и debug.
4. Прогнать `fdroid readmeta` и `fdroid lint`.
5. Влить изменения в `main`.
6. Поставить тег на этот коммит.
7. Открыть merge request в fdroiddata.

Следующий выпуск: поднять `versionCode` и `versionName`, дописать changelog, заново проверить неподписанную сборку, поставить новый тег. Поля `AutoUpdateMode: Version` и `UpdateCheckMode: Tags` сами предложат новую строку `Builds`, когда тег появится в origin.

Тег ставят после того, как литералы версии уже есть в коммите, на который он указывает. В том же pull request, что меняет версию, тег не создают и в `main` напрямую не пушат.

## 1. Осмотр репозитория

Сверьте:

| Что | Где | Ожидание |
| --- | --- | --- |
| Корень Gradle | `settings.gradle.kts`, `gradlew` | корень репозитория, `include(":app")` |
| Модуль | `app/build.gradle.kts` | `applicationId = "com.sysref.commands"` |
| Версия | `defaultConfig` | литералы `versionCode` и `versionName` |
| Подпись CI | `.github/workflows/release.yml` | `KEYSTORE_FILE`, `KEYSTORE_PASSWORD`, `KEY_ALIAS`, `KEY_PASSWORD` |
| Манифест | `app/src/main/AndroidManifest.xml` | нет `INTERNET` и чужих SDK |
| Шрифт | `third_party/jetbrains-mono/OFL.txt` | SIL OFL 1.1, упоминание в README |

Репозитории зависимостей только `google()` и `mavenCentral()`. Сторонних maven-url, Play Services, Firebase и аналитики быть не должно.

Единственный допустимый готовый бинарник в дереве — `gradle/wrapper/gradle-wrapper.jar`. Его SHA-256 должен совпадать с jar из тега Gradle, указанного в `gradle/wrapper/gradle-wrapper.properties` (сейчас 8.11.1):

```bash
curl -fsSL -o /tmp/gradle-wrapper-upstream.jar \
  https://raw.githubusercontent.com/gradle/gradle/v8.11.1/gradle/wrapper/gradle-wrapper.jar
cmp /tmp/gradle-wrapper-upstream.jar gradle/wrapper/gradle-wrapper.jar
```

`gradlew` в git должен быть исполняемым (`100755`), иначе `./gradlew` на Linux не запустится. Workflow всё равно делает `chmod +x`, но локальная команда из README — нет.

## 2. Подпись, которая не ломает F-Droid

F-Droid сам запускает `assembleRelease` без наших ключей и подписывает APK своим ключом. Если `signingConfig` задан всегда, такая сборка падает или требует keystore, которого на сервере нет.

Блок подписи создаётся только когда задана переменная `KEYSTORE_FILE`. Имена переменных те же, что в GitHub Actions:

```kotlin
val releaseKeystore = System.getenv("KEYSTORE_FILE")?.takeIf { it.isNotBlank() }?.let { file(it) }

android {
    if (releaseKeystore != null) {
        signingConfigs {
            create("release") {
                storeFile = releaseKeystore
                storePassword = System.getenv("KEYSTORE_PASSWORD")
                keyAlias = System.getenv("KEY_ALIAS")
                keyPassword = System.getenv("KEY_PASSWORD")
            }
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            if (releaseKeystore != null) {
                signingConfig = signingConfigs.getByName("release")
            }
        }
    }
}
```

`file()` понимает абсолютный путь. Workflow кладёт хранилище в `${{ runner.temp }}/sysref.jks` и передаёт этот путь в `KEYSTORE_FILE`.

Перед сборкой F-Droid вырезает из `build.gradle` и `build.gradle.kts` блок, строка которого целиком равна `signingConfigs {`, и строки вида `signingConfig = ...`. Условный `if` после этого остаётся пустым и по-прежнему компилируется. Не прячьте `signingConfigs` так, чтобы вырезание сломало скобки: открывающая `{` на той же строке, что и `signingConfigs`, присваивание `signingConfig` — отдельной строкой.

Без `KEYSTORE_FILE` workflow теперь не соберёт «тихий» неподписанный APK: переменная всё равно выставлена, и подпись упадёт, если секретов нет. Для F-Droid это правильно: у него этих переменных нет.

Пароли и `*.jks` в репозиторий не кладут. Они уже в `.gitignore`.

## 3. Версия литералами

В `defaultConfig` только прямые значения, без `project.findProperty`, без чтения из файла:

```kotlin
versionCode = 1
versionName = "1.0.0"
```

Проверка обновлений F-Droid читает их регулярными выражениями из `app/build.gradle.kts`. Выражение или подстановка в манифесте через `@string/` ломает `UpdateCheckMode: Tags`.

`versionCode` — целое, у каждой публикации больше предыдущего. `versionName` — строка в кавычках. Для первого выпуска это `1` и `"1.0.0"`. Тег к ним — `v1.0.0`: префикс `v` режим `Version` снимает сам.

## 4. Fastlane

Каталоги `fastlane/metadata/android/ru/` и `fastlane/metadata/android/en-US/`. В каждом:

```text
title.txt                      SYSREF, до 50 символов
short_description.txt          до 80 символов
full_description.txt           до 4000 символов
changelogs/<versionCode>.txt   до 500 символов
images/icon.png                PNG 512×512
images/phoneScreenshots/       снимки телефона
```

Тексты берутся из README, без новых возможностей. Короткое описание — одна строка. Для 1.0.0:

- en-US: `Offline console-style reference of Linux and Windows admin commands.`
- ru: `Офлайн-справочник команд Linux и Windows в виде терминала.`

Полное описание: офлайн-справочник команд Linux и Windows в виде терминала, поиск по фрагменту на русском и английском, фильтры платформ, копирование нажатием, без сети и без слежки. Интерфейс и тексты команд русские — это пишут и в английской карточке.

Changelog называется по `versionCode`, не по `versionName`: для первого выпуска это `changelogs/1.txt` в обеих локалях. У следующего выпуска с `versionCode = 2` будет `changelogs/2.txt`.

Снимки копируют из `docs/screenshots/` в обе локали. Имена с порядковым префиксом, иначе F-Droid отсортирует их по алфавиту:

```bash
for loc in en-US ru; do
  dest="fastlane/metadata/android/$loc/images/phoneScreenshots"
  mkdir -p "$dest"
  cp docs/screenshots/platforms.png       "$dest/01-platforms.png"
  cp docs/screenshots/packages.png        "$dest/02-packages.png"
  cp docs/screenshots/search-nmap.png     "$dest/03-search-nmap.png"
  cp docs/screenshots/search-firewall.png "$dest/04-search-firewall.png"
done
```

Значок рисуют из `app/src/main/res/drawable/ic_launcher.xml` (вьюпорт 108, чёрный фон, шеврон `#3CFF6E`, курсор `#FFB000`). Нужен Pillow.

```bash
python3 << 'PY'
from PIL import Image, ImageDraw

scale = 8
size = 512 * scale
factor = size / 108
image = Image.new("RGBA", (size, size), (0, 0, 0, 255))
draw = ImageDraw.Draw(image)

def point(x, y):
    return (x * factor, y * factor)

draw.polygon([
    point(22, 28), point(42, 54), point(22, 80),
    point(32, 80), point(52, 54), point(32, 28),
], fill=(0x3C, 0xFF, 0x6E, 255))
draw.polygon([
    point(58, 72), point(92, 72), point(92, 82), point(58, 82),
], fill=(0xFF, 0xB0, 0x00, 255))
icon = image.resize((512, 512), Image.Resampling.LANCZOS)
for locale in ("en-US", "ru"):
    icon.save(f"fastlane/metadata/android/{locale}/images/icon.png")
PY
```

Проверка длин (лимит считается без завершающего перевода строки):

```bash
python3 << 'PY'
from pathlib import Path
limits = {
    "title.txt": 50,
    "short_description.txt": 80,
    "full_description.txt": 4000,
    "changelogs/1.txt": 500,
}
for path in Path("fastlane/metadata/android").rglob("*.txt"):
    text = path.read_text(encoding="utf-8")
    body = text[:-1] if text.endswith("\n") else text
    key = "changelogs/1.txt" if path.parent.name == "changelogs" else path.name
    print(f"{len(body):4}/{limits[key]:<4} {path}")
    if len(body) > limits[key]:
        raise SystemExit(path)
PY
```

Для нового `versionCode` замените `changelogs/1.txt` в словаре на актуальное имя.

## 5. Критерии включения

Перед заявкой:

- В `app/build.gradle.kts` нет `com.google.android.gms`, Firebase, Crashlytics, рекламных и аналитических артефактов.
- В манифесте нет разрешения `INTERNET`, если приложению сеть не нужна. У SYSREF его нет. Единственное `uses-permission` в собранном APK — служебное `com.sysref.commands.DYNAMIC_RECEIVER_NOT_EXPORTED_PERMISSION` от AndroidX. Это не сеть.
- В дереве нет `.jar`, `.aar`, `.so`, кроме `gradle/wrapper/gradle-wrapper.jar`. Файлы `.ttf` JetBrains Mono — шрифт под OFL, лицензия и авторы лежат в `third_party/jetbrains-mono/`, в README это названо.
- `commands.json` собирается скриптом в репозитории и коммитится. На сборочном сервере Python для приложения не нужен.

Нативная `libandroidx.graphics.path.so` попадает в APK из зависимости AndroidX (Apache-2.0), а не из git. Сканер исходников на неё не ругается. Это обычная транзитивная библиотека Compose.

## 6. Черновик метаданных

`docs/fdroid-metadata.yml` для выпуска 1.0.0:

```yaml
Categories:
  - Development
  - System
License: MIT
AuthorName: Antiokh Lebedev
SourceCode: https://github.com/dalem-732/SYSREF
IssueTracker: https://github.com/dalem-732/SYSREF/issues

AutoName: SYSREF

RepoType: git
Repo: https://github.com/dalem-732/SYSREF

Builds:
  - versionName: 1.0.0
    versionCode: 1
    commit: v1.0.0
    subdir: app
    gradle:
      - yes

AutoUpdateMode: Version
UpdateCheckMode: Tags
CurrentVersion: 1.0.0
CurrentVersionCode: 1
```

Поля:

- `Categories` — существующие категории fdroiddata. `Development` и `System` подходят.
- `License: MIT` совпадает с файлом `LICENSE`.
- `RepoType: git` нужен, иначе fdroidserver не клонирует репозиторий.
- `Repo` без суффикса `.git`. Клонирование его принимает, `fdroid lint` на лишний `.git` у части URL ругается.
- `subdir: app` — каталог модуля, где появляется `app/build`. Не корень: `gradlew` лежит выше, и F-Droid запускает Gradle из `app/`.
- `gradle: - yes` в YAML 1.2 — строка `yes`, не булево `true`. Это `assembleRelease` без flavor.
- `commit: v1.0.0` — тег. Режим `AutoUpdateMode: Version` при `UpdateCheckMode: Tags` пишет в новые сборки именно тег. Документация F-Droid для уже собранной строки предпочитает полный хеш коммита, чтобы сдвинутый тег не менял сборку. После `git rev-parse v1.0.0` хеш можно подставить вместо тега в первой строке `Builds`. Пока тега нет, в черновике остаётся `v1.0.0`.
- `CurrentVersion` / `CurrentVersionCode` совпадают с последней строкой `Builds`.

Пустая строка между группами полей — тот вид, который выдаёт `fdroid rewritemeta`. После правки прогоните его и заберите результат обратно в `docs/fdroid-metadata.yml`.

## 7. Проверка сборки

Нужны JDK 17 или 21 и Android SDK: платформа 35 и Build-Tools 35.0.0. `ANDROID_HOME` указывает на SDK. `local.properties` с `sdk.dir` в git не входит.

Неподписанный release и debug. Переменные подписи должны быть пусты:

```bash
unset KEYSTORE_FILE KEYSTORE_PASSWORD KEY_ALIAS KEY_PASSWORD
./gradlew assembleRelease
./gradlew assembleDebug
./gradlew testDebugUnitTest
( cd app && ../gradlew assembleRelease )
```

Последняя команда повторяет рабочий каталог F-Droid.

Дальше подставьте свой `aapt` и `apksigner` из `build-tools`:

```bash
BT="$ANDROID_HOME/build-tools/35.0.0"
APK=app/build/outputs/apk/release/app-release-unsigned.apk
"$BT/aapt" dump badging "$APK" | head -5
"$BT/aapt" dump permissions "$APK"
"$BT/apksigner" verify "$APK"
```

Ожидание: `name='com.sysref.commands'`, `versionCode='1'`, `versionName='1.0.0'`, нет `android.permission.INTERNET`, `apksigner verify` завершается с ошибкой `Missing META-INF/MANIFEST.MF`.

Отдельная проверка, что CI-имена переменных подписывают APK. Хранилище временное, в git его нет:

```bash
keytool -genkeypair -keystore /tmp/sysref-test.jks \
  -storepass testpass -keypass testpass -alias sysref \
  -keyalg RSA -keysize 2048 -validity 1000 -dname "CN=SYSREF Test"
export KEYSTORE_FILE=/tmp/sysref-test.jks
export KEYSTORE_PASSWORD=testpass
export KEY_ALIAS=sysref
export KEY_PASSWORD=testpass
./gradlew assembleRelease
"$BT/apksigner" verify app/build/outputs/apk/release/app-release.apk
unset KEYSTORE_FILE KEYSTORE_PASSWORD KEY_ALIAS KEY_PASSWORD
./gradlew assembleRelease
"$BT/apksigner" verify app/build/outputs/apk/release/app-release-unsigned.apk
```

Первый `verify` успешен (схема v2). После `unset` снова появляется неподписанный APK, и `verify` падает.

Имеет смысл прогнать файл через тот же вырезатель подписи, что и сервер F-Droid, собрать и вернуть оригинал:

```bash
mkdir -p /tmp/strip-test
cp app/build.gradle.kts /tmp/build.gradle.kts.orig
cp app/build.gradle.kts /tmp/strip-test/build.gradle.kts
/tmp/fdroid-venv/bin/python - << 'PY'
from fdroidserver.common import remove_signing_keys
remove_signing_keys("/tmp/strip-test")
PY
cp /tmp/strip-test/build.gradle.kts app/build.gradle.kts
./gradlew assembleRelease
cp /tmp/build.gradle.kts.orig app/build.gradle.kts
git diff -- app/build.gradle.kts
```

Сборка с вырезанным блоком проходит. `git diff` после возврата файла пустой. Каталог `/tmp/fdroid-venv` появляется на следующем шаге; этот прогон делают после установки fdroidserver.

## 8. fdroid lint и сканер

```bash
python3 -m venv /tmp/fdroid-venv
/tmp/fdroid-venv/bin/pip install -U pip fdroidserver yamllint
```

На Ubuntu для `venv` нужен пакет `python3-venv`. Минимальный репозиторий метаданных, не внутри SYSREF:

```bash
mkdir -p /tmp/fdroiddata/metadata
cat > /tmp/fdroiddata/config.yml << EOF
repo_url: https://example.com/fdroid/repo
repo_name: SYSREF lint
repo_icon: dummy.png
repo_description: lint only
sdk_path: ${ANDROID_HOME}
categories:
  - Development
  - System
EOF
cp docs/fdroid-metadata.yml /tmp/fdroiddata/metadata/com.sysref.commands.yml
cd /tmp/fdroiddata
/tmp/fdroid-venv/bin/fdroid readmeta
/tmp/fdroid-venv/bin/fdroid rewritemeta com.sysref.commands
/tmp/fdroid-venv/bin/fdroid lint -f com.sysref.commands
```

Все три команды завершаются с кодом 0. Если `rewritemeta` поменял кавычки или пустые строки, скопируйте файл обратно в `docs/fdroid-metadata.yml` и закоммитьте уже этот вид. Строка `gradle: - yes` должна остаться строкой `yes`.

Сканер по чистому дереву, без `build/` и `.gradle/`:

```bash
rm -rf /tmp/srcscan
mkdir -p /tmp/srcscan
tar --exclude=build --exclude=.gradle --exclude=.git --exclude=local.properties \
  -cf - . | tar -C /tmp/srcscan -xf -
/tmp/fdroid-venv/bin/python - << 'PY'
from fdroidserver.scanner import scan_source
count = scan_source("/tmp/srcscan")
print("SCAN_PROBLEMS", count)
if count:
    raise SystemExit(count)
PY
```

Сообщения `Removing gradlew` и `Removing gradle-wrapper.jar` означают, что эти файлы в белом списке, а не что сканер нашёл нарушение. `SCAN_PROBLEMS` должен быть `0`.

## 9. Тег и заявка

После вливания коммита с литералами версии:

```bash
git checkout main
git pull origin main
git tag v1.0.0
git push origin v1.0.0
```

Тег указывает на коммит, где `versionName` равен `1.0.0`, а `versionCode` равен `1`. Имя тега без `v` совпадает с `versionName`.

Дальше форк [fdroiddata](https://gitlab.com/fdroid/fdroiddata), файл `metadata/com.sysref.commands.yml` с содержимым `docs/fdroid-metadata.yml`, merge request. Если вместо тега в `commit` подставлен полный хеш, возьмите его так:

```bash
git rev-parse v1.0.0
```

Описание MR: приложение офлайн, лицензия MIT, шрифт OFL, сборка `gradle: yes` из `subdir: app`, секретов и несвободных зависимостей нет.

Пока тега `v1.0.0` нет, F-Droid этот `commit` не выпишет. Сборку на сервере F-Droid запускают уже они, после принятия метаданных.

## 10. Следующий выпуск

1. В `defaultConfig` увеличить `versionCode` на 1 и записать новый `versionName` строковым литералом, например `"1.1.0"`.
2. Добавить `fastlane/metadata/android/ru/changelogs/<versionCode>.txt` и такой же файл в `en-US`. Старые changelog не удалять.
3. Обновить снимки, если изменился интерфейс. Значок пересобрать, если изменился вектор.
4. Повторить неподписанный `assembleRelease` без `KEYSTORE_*` и проверку `apksigner`.
5. Влить в `main`, поставить тег `v1.1.0` на этот коммит, запушить тег.
6. В `docs/fdroid-metadata.yml` для собственной памяти можно дописать новую строку `Builds` и поднять `CurrentVersion` / `CurrentVersionCode`. В fdroiddata при `AutoUpdateMode: Version` это сделает `fdroid checkupdates`, когда увидит тег. Ручной MR нужен, только если автообновление не подхватило тег.

Не поднимайте `versionName`, оставляя прежний `versionCode`: F-Droid публикует код версии, а не имя.
