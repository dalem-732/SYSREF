package com.sysref.commands

data class CommandEntry(
    val command: String,
    val description: String,
    val platform: String,
    val category: String,
    val index: Int,
    val danger: Boolean = false,
) {
    val id: String = "$platform/$category/$command"

    internal val commandFolded: String = foldRu(command)
    internal val commandWords: List<String> = commandFolded.split(WHITESPACE)
    internal val descriptionFolded: String = foldRu(description)
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

fun platformOrNull(id: String?): Platform? = id?.let { platformsById[it] }

fun categoryOrNull(id: String?): Category? = id?.let { categoriesById[it] }

fun platformById(id: String): Platform =
    platformOrNull(id) ?: error("unknown platform: $id")

fun categoryById(id: String): Category =
    categoryOrNull(id) ?: error("unknown category: $id")

val browsePlatforms: List<Platform> = Platform.entries

private val WHITESPACE = Regex("\\s+")

private val distroIds = setOf(Platform.DEBIAN.id, Platform.RHEL.id, Platform.SUSE.id, Platform.ARCH.id)
private val linuxFamily = distroIds + Platform.LINUX.id

enum class SearchFilter(val id: String, val label: String) {
    ALL("all", "все"),
    LINUX_ALL("linall", "linux"),
    DEBIAN("debian", "deb"),
    RHEL("rhel", "rhel"),
    SUSE("suse", "suse"),
    ARCH("arch", "arch"),
    CMD("cmd", "cmd"),
    PS("ps", "ps"),
    ;

    companion object {
        fun fromId(id: String?): SearchFilter = entries.firstOrNull { it.id == id } ?: ALL
    }
}

fun inBrowse(entry: CommandEntry, platformId: String): Boolean = when (platformId) {
    in distroIds -> entry.platform == platformId || entry.platform == Platform.LINUX.id
    else -> entry.platform == platformId
}

fun inFilter(entry: CommandEntry, filter: SearchFilter): Boolean = when (filter) {
    SearchFilter.ALL -> true
    SearchFilter.LINUX_ALL -> entry.platform in linuxFamily
    else -> inBrowse(entry, filter.id)
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

private val categoryHay: Map<String, String> = Category.entries.associate { category ->
    category.id to foldRu((listOf(category.title, category.id) + category.aliases).joinToString(" "))
}

private val platformHay: Map<String, String> = Platform.entries.associate { platform ->
    platform.id to foldRu((listOf(platform.title, platform.id) + platform.aliases).joinToString(" "))
}

fun rankOf(entry: CommandEntry, queryFolded: String): Int {
    val command = entry.commandFolded
    if (command == queryFolded) return 0
    if (command.startsWith(queryFolded)) return 1
    if (entry.commandWords.any { it.startsWith(queryFolded) }) return 2
    if (command.contains(queryFolded)) return 3
    if (entry.descriptionFolded.contains(queryFolded)) return 4
    if (queryFolded.length >= 2) {
        if (categoryHay[entry.category].orEmpty().contains(queryFolded)) return 5
        if (platformHay[entry.platform].orEmpty().contains(queryFolded)) return 6
    }
    return Int.MAX_VALUE
}

fun searchCommands(
    commands: List<CommandEntry>,
    query: String,
    filter: SearchFilter,
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
                .thenBy { it.first.commandFolded }
                .thenBy { it.first.index },
        )
        .map { it.first }
        .toList()
}

private fun pluralRu(count: Int, one: String, few: String, many: String): String {
    val mod10 = count % 10
    val mod100 = count % 100
    val word = when {
        mod10 == 1 && mod100 != 11 -> one
        mod10 in 2..4 && mod100 !in 12..14 -> few
        else -> many
    }
    return "$count $word"
}

fun pluralMatches(count: Int): String = pluralRu(count, "совпадение", "совпадения", "совпадений")

fun pluralCommands(count: Int): String = pluralRu(count, "команда", "команды", "команд")
