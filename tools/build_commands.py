#!/usr/bin/env python3
"""Build the offline command database shipped in the app."""

import json
import sys
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
OUT = ROOT / "app" / "src" / "main" / "assets" / "commands.json"

PLATFORMS = {"debian", "rhel", "suse", "arch", "linux", "cmd", "ps"}
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


from entries_linux import register as register_linux
from entries_windows import register as register_windows

register_linux(add, many)
register_windows(add, many)

DISTROS = {"debian", "rhel", "suse", "arch"}
linux_commands = {item["command"] for item in entries if item["platform"] == "linux"}
deduped = []
for item in entries:
    if item["platform"] in DISTROS and item["command"] in linux_commands:
        continue
    command = item["command"]
    if command.startswith("flatpak "):
        item["category"] = "packages"
    if command.startswith("wmic "):
        item["category"] = "system"
    if command.startswith("tasklist") or command.startswith("taskkill"):
        item["category"] = "process"
    if command.startswith("wevtutil") or command.startswith("auditpol"):
        item["category"] = "logs"
    if command.startswith("shutdown") or command.startswith("powercfg"):
        item["category"] = "boot"
    deduped.append(item)
entries = deduped

seen = set()
for item in entries:
    key = (item["platform"], item["category"], item["command"])
    if key in seen:
        raise SystemExit(f"duplicate: {key}")
    seen.add(key)

if len(entries) < 400:
    raise SystemExit(f"too few commands: {len(entries)}")

OUT.parent.mkdir(parents=True, exist_ok=True)
payload = {"version": 1, "commands": entries}
OUT.write_text(json.dumps(payload, ensure_ascii=False, indent=2) + "\n", encoding="utf-8")

from collections import Counter

print(f"wrote {len(entries)} commands -> {OUT}")
print("platforms:", dict(Counter(i["platform"] for i in entries)))
print("categories:", dict(Counter(i["category"] for i in entries)))
