package com.sysref.commands.ui

import com.sysref.commands.Category
import com.sysref.commands.Platform
import com.sysref.commands.SearchFilter
import com.sysref.commands.resources.Res
import com.sysref.commands.resources.category_ad
import com.sysref.commands.resources.category_archive
import com.sysref.commands.resources.category_boot
import com.sysref.commands.resources.category_cron
import com.sysref.commands.resources.category_disks
import com.sysref.commands.resources.category_docker
import com.sysref.commands.resources.category_files
import com.sysref.commands.resources.category_firewall
import com.sysref.commands.resources.category_git
import com.sysref.commands.resources.category_logs
import com.sysref.commands.resources.category_lvm
import com.sysref.commands.resources.category_nettools
import com.sysref.commands.resources.category_network
import com.sysref.commands.resources.category_packages
import com.sysref.commands.resources.category_perms
import com.sysref.commands.resources.category_process
import com.sysref.commands.resources.category_registry
import com.sysref.commands.resources.category_remote
import com.sysref.commands.resources.category_services
import com.sysref.commands.resources.category_shell
import com.sysref.commands.resources.category_system
import com.sysref.commands.resources.category_systemd
import com.sysref.commands.resources.category_tasks
import com.sysref.commands.resources.category_text
import com.sysref.commands.resources.category_users
import com.sysref.commands.resources.filter_all
import com.sysref.commands.resources.platform_arch_blurb
import com.sysref.commands.resources.platform_arch_title
import com.sysref.commands.resources.platform_cmd_blurb
import com.sysref.commands.resources.platform_cmd_title
import com.sysref.commands.resources.platform_debian_blurb
import com.sysref.commands.resources.platform_debian_title
import com.sysref.commands.resources.platform_linux_blurb
import com.sysref.commands.resources.platform_linux_title
import com.sysref.commands.resources.platform_ps_blurb
import com.sysref.commands.resources.platform_ps_title
import com.sysref.commands.resources.platform_rhel_blurb
import com.sysref.commands.resources.platform_rhel_title
import com.sysref.commands.resources.platform_suse_blurb
import com.sysref.commands.resources.platform_suse_title
import org.jetbrains.compose.resources.StringResource

internal val Platform.titleRes: StringResource
    get() = when (this) {
        Platform.DEBIAN -> Res.string.platform_debian_title
        Platform.RHEL -> Res.string.platform_rhel_title
        Platform.SUSE -> Res.string.platform_suse_title
        Platform.ARCH -> Res.string.platform_arch_title
        Platform.LINUX -> Res.string.platform_linux_title
        Platform.CMD -> Res.string.platform_cmd_title
        Platform.PS -> Res.string.platform_ps_title
    }

internal val Platform.blurbRes: StringResource
    get() = when (this) {
        Platform.DEBIAN -> Res.string.platform_debian_blurb
        Platform.RHEL -> Res.string.platform_rhel_blurb
        Platform.SUSE -> Res.string.platform_suse_blurb
        Platform.ARCH -> Res.string.platform_arch_blurb
        Platform.LINUX -> Res.string.platform_linux_blurb
        Platform.CMD -> Res.string.platform_cmd_blurb
        Platform.PS -> Res.string.platform_ps_blurb
    }

internal val Category.titleRes: StringResource
    get() = when (this) {
        Category.PACKAGES -> Res.string.category_packages
        Category.SERVICES -> Res.string.category_services
        Category.FIREWALL -> Res.string.category_firewall
        Category.NETWORK -> Res.string.category_network
        Category.NETTOOLS -> Res.string.category_nettools
        Category.USERS -> Res.string.category_users
        Category.DISKS -> Res.string.category_disks
        Category.LVM -> Res.string.category_lvm
        Category.FILES -> Res.string.category_files
        Category.PERMS -> Res.string.category_perms
        Category.ARCHIVE -> Res.string.category_archive
        Category.TEXT -> Res.string.category_text
        Category.PROCESS -> Res.string.category_process
        Category.SYSTEMD -> Res.string.category_systemd
        Category.CRON -> Res.string.category_cron
        Category.LOGS -> Res.string.category_logs
        Category.SHELL -> Res.string.category_shell
        Category.REMOTE -> Res.string.category_remote
        Category.GIT -> Res.string.category_git
        Category.DOCKER -> Res.string.category_docker
        Category.SYSTEM -> Res.string.category_system
        Category.BOOT -> Res.string.category_boot
        Category.TASKS -> Res.string.category_tasks
        Category.REGISTRY -> Res.string.category_registry
        Category.AD -> Res.string.category_ad
    }

internal val SearchFilter.labelRes: StringResource?
    get() = if (this == SearchFilter.ALL) Res.string.filter_all else null
