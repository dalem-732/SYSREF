#!/usr/bin/env python3
import json
import sys
from collections import Counter
from pathlib import Path

from entries_linux import register as register_linux
from entries_windows import register as register_windows

ROOT = Path(__file__).resolve().parents[1]
OUT = ROOT / "shared" / "src" / "commonMain" / "composeResources" / "files" / "commands.json"

PLATFORMS = {"debian", "rhel", "suse", "arch", "linux", "cmd", "ps"}
DISTROS = {"debian", "rhel", "suse", "arch"}
CATEGORIES = {
    "packages",
    "services",
    "firewall",
    "network",
    "nettools",
    "users",
    "disks",
    "lvm",
    "files",
    "perms",
    "archive",
    "text",
    "process",
    "systemd",
    "cron",
    "logs",
    "shell",
    "remote",
    "git",
    "docker",
    "system",
    "boot",
    "tasks",
    "registry",
    "ad",
}

CATEGORY_BY_PREFIX = [
    (("flatpak ",), "packages"),
    (("wmic ",), "system"),
    (("tasklist", "taskkill"), "process"),
    (("wevtutil", "auditpol"), "logs"),
    (("shutdown", "powercfg"), "boot"),
]

DANGEROUS = {
    ("linux", "rm -r <каталог>"),
    ("linux", "rm -rf <каталог>"),
    ("linux", "wipefs -a /dev/sdX1"),
    ("linux", "mkfs.ext4 /dev/sdX1"),
    ("linux", "mkfs.xfs /dev/sdX1"),
    ("linux", "mkfs.btrfs /dev/sdX1"),
    ("linux", "dd if=/dev/zero of=<файл> bs=1M count=10 status=progress"),
    ("linux", "pvremove /dev/sdX1"),
    ("linux", "vgremove <vg>"),
    ("linux", "lvremove /dev/<vg>/<lv>"),
    ("linux", "userdel -r <пользователь>"),
    ("linux", "crontab -r"),
    ("linux", "git reset --hard HEAD~1"),
    ("linux", "git clean -fd"),
    ("linux", "iptables -F"),
    ("debian", "ufw reset"),
    ("rhel", "setenforce 0"),
    ("cmd", "clean"),
    ("cmd", "format fs=ntfs quick"),
    ("cmd", "rmdir /s /q <каталог>"),
    ("cmd", "del /s /q <каталог>\\*"),
    ("cmd", "netsh advfirewall set allprofiles state off"),
    ("ps", "Initialize-Disk -Number 1 -PartitionStyle GPT"),
    ("ps", "Format-Volume -DriveLetter E -FileSystem NTFS -NewFileSystemLabel Data"),
    ("ps", "Remove-Item C:\\temp\\dir -Recurse -Force"),
    ("ps", "Remove-LocalUser -Name <пользователь>"),
    ("ps", "Remove-ADUser -Identity <пользователь>"),
}

entries = []


def add(platform, category, command, description):
    if platform not in PLATFORMS:
        raise SystemExit(f"bad platform {platform}: {command}")
    if category not in CATEGORIES:
        raise SystemExit(f"bad category {category}: {command}")
    command = command.strip()
    description = description.strip()
    if not command or not description:
        raise SystemExit("empty command or description")
    entries.append(
        {
            "command": command,
            "description": description,
            "platform": platform,
            "category": category,
        }
    )


def many(platform, category, pairs):
    for command, description in pairs:
        add(platform, category, command, description)


def build():
    register_linux(add, many)
    register_windows(add, many)

    linux_commands = {item["command"] for item in entries if item["platform"] == "linux"}
    result = []
    for item in entries:
        if item["platform"] in DISTROS and item["command"] in linux_commands:
            continue
        for prefixes, category in CATEGORY_BY_PREFIX:
            if item["command"].startswith(prefixes):
                item["category"] = category
        if (item["platform"], item["command"]) in DANGEROUS:
            item["danger"] = True
        result.append(item)

    seen = set()
    for item in result:
        key = (item["platform"], item["category"], item["command"])
        if key in seen:
            raise SystemExit(f"duplicate command id {'/'.join(key)}")
        seen.add(key)

    flagged = {(item["platform"], item["command"]) for item in result if item.get("danger")}
    missing = DANGEROUS - flagged
    if missing:
        raise SystemExit(f"dangerous commands not found: {sorted(missing)}")

    if len(result) < 400:
        raise SystemExit(f"too few commands: {len(result)}")
    return result


def main():
    commands = build()
    text = json.dumps({"version": 1, "commands": commands}, ensure_ascii=False, indent=2) + "\n"
    if "--check" in sys.argv[1:]:
        current = OUT.read_text(encoding="utf-8") if OUT.is_file() else ""
        if current != text:
            raise SystemExit(f"{OUT.relative_to(ROOT)} is out of date: run python3 tools/build_commands.py")
        print(f"{OUT.relative_to(ROOT)} is up to date ({len(commands)} commands)")
        return
    OUT.parent.mkdir(parents=True, exist_ok=True)
    OUT.write_text(text, encoding="utf-8")
    print(f"wrote {len(commands)} commands -> {OUT}")
    print("platforms:", dict(Counter(i["platform"] for i in commands)))
    print("categories:", dict(Counter(i["category"] for i in commands)))
    print("dangerous:", sum(1 for i in commands if i.get("danger")))


if __name__ == "__main__":
    main()
