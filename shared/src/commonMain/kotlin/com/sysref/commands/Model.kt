package com.sysref.commands

data class CommandEntry(
    val command: String,
    val description: String,
    val platform: String,
    val category: String,
    val index: Int,
) {
    /**
     * Stable key for favorites. Built from the entry content, not from its position,
     * so favorites survive reordering or new commands in commands.json.
     * platform + category + command is unique (checked by tools/build_commands.py).
     */
    val id: String get() = "$platform/$category/$command"
}

enum class Platform(
    val id: String,
    val title: String,
    val blurb: String,
    val aliases: List<String>,
) {
    DEBIAN(
        "debian",
        "Debian/Ubuntu",
        "apt, ufw, netplan, AppArmor",
        listOf("debian", "ubuntu", "дебиан", "убунту", "убунт"),
    ),
    RHEL(
        "rhel",
        "RHEL/Fedora",
        "dnf, firewalld, SELinux",
        listOf("rhel", "rocky", "fedora", "centos", "redhat", "федора", "рокки", "альма"),
    ),
    SUSE(
        "suse",
        "openSUSE",
        "zypper, wicked, AppArmor",
        listOf("suse", "opensuse", "sles", "опенсюз", "сюзе"),
    ),
    ARCH(
        "arch",
        "Arch",
        "pacman, systemd",
        listOf("arch", "archlinux", "арч"),
    ),
    LINUX(
        "linux",
        "Общие для Linux",
        "файлы, systemd, сеть, LVM, git, docker",
        listOf("linux", "линукс", "gnu"),
    ),
    CMD(
        "cmd",
        "Windows cmd",
        "cmd, netsh, wmic, bcdedit",
        listOf("cmd", "windows", "виндовс", "винда", "командная"),
    ),
    PS(
        "ps",
        "PowerShell",
        "администрирование, AD, DNS, DHCP",
        listOf("powershell", "pwsh", "повершелл", "windows", "виндовс"),
    ),
}

enum class Category(
    val id: String,
    val title: String,
    val aliases: List<String>,
) {
    PACKAGES("packages", "Пакеты", listOf("пакеты", "пакет", "packages", "package")),
    SERVICES("services", "Службы", listOf("службы", "служба", "services", "service", "сервис")),
    FIREWALL("firewall", "Файрвол", listOf("файрвол", "firewall", "брандмауэр", "межсетевой")),
    NETWORK("network", "Сеть", listOf("сеть", "сети", "network")),
    NETTOOLS("nettools", "Диагностика сети", listOf("диагностика", "сниффер")),
    USERS("users", "Пользователи", listOf("пользователи", "пользователь", "users", "user", "группы", "группа")),
    DISKS("disks", "Диски", listOf("диски", "диск", "disks", "disk", "разделы", "раздел", "файловая")),
    LVM("lvm", "LVM", listOf("lvm", "логические тома")),
    FILES("files", "Файлы и каталоги", listOf("файлы", "файл", "каталоги", "каталог", "files", "filesystem")),
    PERMS("perms", "Права и владельцы", listOf("права", "владелец", "доступ")),
    ARCHIVE("archive", "Архивы", listOf("архивы", "архив", "archive", "tar", "zip")),
    TEXT("text", "Текст", listOf("текст", "text")),
    PROCESS("process", "Процессы", listOf("процессы", "процесс", "process", "память")),
    SYSTEMD("systemd", "systemd", listOf("systemd", "юнит", "юниты", "таймеры")),
    CRON("cron", "Планировщик", listOf("cron", "планировщик", "расписание", "crontab")),
    LOGS("logs", "Журналы и безопасность", listOf("журналы", "журнал", "логи", "лог", "безопасность", "security")),
    SHELL("shell", "Оболочка", listOf("оболочка", "shell", "bash", "терминал")),
    REMOTE("remote", "Удалённый доступ", listOf("ssh", "удалённый", "удаленный", "scp")),
    GIT("git", "Git", listOf("git", "гит", "репозиторий")),
    DOCKER("docker", "Docker", listOf("docker", "докер", "контейнер", "контейнеры")),
    SYSTEM("system", "Система", listOf("система", "system", "сведения")),
    BOOT("boot", "Загрузка и восстановление", listOf("загрузка", "восстановление", "bcdedit", "boot")),
    TASKS("tasks", "Задачи", listOf("задачи", "задача", "планировщик", "schtasks")),
    REGISTRY("registry", "Реестр", listOf("реестр", "registry", "reg")),
    AD("ad", "AD, DNS и DHCP", listOf("active directory", "домен", "ад")),
}

private val platformsById = Platform.entries.associateBy { it.id }
private val categoriesById = Category.entries.associateBy { it.id }

fun platformById(id: String): Platform =
    platformsById[id] ?: error("unknown platform: $id")

fun categoryById(id: String): Category =
    categoriesById[id] ?: error("unknown category: $id")

val browsePlatforms: List<Platform> = Platform.entries

private val distroIds = setOf("debian", "rhel", "suse", "arch")
private val linuxFamily = setOf("debian", "rhel", "suse", "arch", "linux")

fun inBrowse(entry: CommandEntry, platformId: String): Boolean = when (platformId) {
    in distroIds -> entry.platform == platformId || entry.platform == "linux"
    else -> entry.platform == platformId
}

fun inFilter(entry: CommandEntry, filter: String): Boolean = when (filter) {
    "all" -> true
    "linall" -> entry.platform in linuxFamily
    in distroIds -> entry.platform == filter || entry.platform == "linux"
    else -> entry.platform == filter
}

fun foldRu(value: String): String = buildString(value.length) {
    for (ch in value) {
        append(
            when (ch) {
                'Ё', 'ё' -> 'е'
                else -> ch.lowercaseChar()
            },
        )
    }
}

fun rankOf(entry: CommandEntry, queryFolded: String): Int {
    val command = foldRu(entry.command)
    if (command == queryFolded) return 0
    if (command.startsWith(queryFolded)) return 1
    if (command.split(Regex("\\s+")).any { it.startsWith(queryFolded) }) return 2
    if (command.contains(queryFolded)) return 3
    if (foldRu(entry.description).contains(queryFolded)) return 4
    if (queryFolded.length >= 2) {
        val category = categoryById(entry.category)
        val categoryHay = foldRu(
            (listOf(category.title, category.id) + category.aliases).joinToString(" "),
        )
        if (categoryHay.contains(queryFolded)) return 5
        val platform = platformById(entry.platform)
        val platformHay = foldRu(
            (listOf(platform.title, platform.id) + platform.aliases).joinToString(" "),
        )
        if (platformHay.contains(queryFolded)) return 6
    }
    return Int.MAX_VALUE
}

fun searchCommands(
    commands: List<CommandEntry>,
    query: String,
    filter: String,
): List<CommandEntry> {
    val folded = foldRu(query.trim())
    if (folded.isEmpty()) return emptyList()
    return commands
        .asSequence()
        .filter { inFilter(it, filter) }
        .map { it to rankOf(it, folded) }
        .filter { it.second != Int.MAX_VALUE }
        .sortedWith(
            compareBy<Pair<CommandEntry, Int>> { it.second }
                .thenBy { if (it.second <= 3) it.first.command.length else 0 }
                .thenBy { foldRu(it.first.command) }
                .thenBy { it.first.index },
        )
        .map { it.first }
        .toList()
}

fun pluralMatches(count: Int): String {
    val mod10 = count % 10
    val mod100 = count % 100
    val word = when {
        mod10 == 1 && mod100 != 11 -> "совпадение"
        mod10 in 2..4 && mod100 !in 12..14 -> "совпадения"
        else -> "совпадений"
    }
    return "$count $word"
}

fun pluralCommands(count: Int): String {
    val mod10 = count % 10
    val mod100 = count % 100
    val word = when {
        mod10 == 1 && mod100 != 11 -> "команда"
        mod10 in 2..4 && mod100 !in 12..14 -> "команды"
        else -> "команд"
    }
    return "$count $word"
}
