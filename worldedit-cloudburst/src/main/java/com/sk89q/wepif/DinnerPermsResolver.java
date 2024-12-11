/*
 * WorldEdit, a Minecraft world manipulation toolkit
 * Copyright (C) sk89q <http://www.sk89q.com>
 * Copyright (C) WorldEdit team and contributors
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package com.sk89q.wepif;

import com.sk89q.util.yaml.YAMLProcessor;
import cn.nukkit.OfflinePlayer;
import cn.nukkit.Player;
import cn.nukkit.Server;
import cn.nukkit.permission.Permissible;
import cn.nukkit.permission.Permission;
import cn.nukkit.permission.PermissionAttachmentInfo;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class DinnerPermsResolver implements PermissionsResolver {

    public static final String GROUP_PREFIX = "group.";
    protected final Server server;

    public DinnerPermsResolver(Server server) {
        this.server = server;
    }

    public static PermissionsResolver factory(Server server, YAMLProcessor config) {
        return new DinnerPermsResolver(server);
    }

    @Override
    public void load() {
    }

    @Override
    @SuppressWarnings("deprecation")
    public boolean hasPermission(String name, String permission) {
        return hasPermission((OfflinePlayer) server.getOfflinePlayer(name), permission);
    }

    @Override
    @SuppressWarnings("deprecation")
    public boolean hasPermission(String worldName, String name, String permission) {
        return hasPermission(worldName, (OfflinePlayer) server.getOfflinePlayer(name), permission);
    }

    @Override
    @SuppressWarnings("deprecation")
    public boolean inGroup(String name, String group) {
        return inGroup((OfflinePlayer) server.getOfflinePlayer(name), group);
    }

    @Override
    @SuppressWarnings("deprecation")
    public String[] getGroups(String name) {
        return getGroups((OfflinePlayer) server.getOfflinePlayer(name));
    }

    @Override
    public boolean hasPermission(OfflinePlayer player, String permission) {
        Permissible perms = getPermissible(player);
        if (perms == null) {
            return false; // Permissions are only registered for objects with a Permissible
        }
        switch (internalHasPermission(perms, permission)) {
            case -1:
                return false;
            case 1:
                return true;
            default:
                break;
        }
        int dotPos = permission.lastIndexOf(".");
        while (dotPos > -1) {
            switch (internalHasPermission(perms, permission.substring(0, dotPos + 1) + "*")) {
                case -1:
                    return false;
                case 1:
                    return true;
                default:
                    break;
            }
            dotPos = permission.lastIndexOf(".", dotPos - 1);
        }
        return internalHasPermission(perms, "*") == 1;
    }

    @Override
    public boolean hasPermission(String worldName, OfflinePlayer player, String permission) {
        return hasPermission(player, permission); // no per-world ability to check permissions in dinnerperms
    }

    @Override
    public boolean inGroup(OfflinePlayer player, String group) {
        final Permissible perms = getPermissible(player);
        if (perms == null) {
            return false;
        }

        final String perm = GROUP_PREFIX + group;
        return perms.isPermissionSet(perm) && perms.hasPermission(perm);
    }

    @Override
    public String[] getGroups(OfflinePlayer player) {
        Permissible perms = getPermissible(player);
        if (perms == null) {
            return new String[0];
        }
        List<String> groupNames = new ArrayList<>();
        for (PermissionAttachmentInfo permAttach : perms.getEffectivePermissions().values()) {
            String perm = permAttach.getPermission();
            if (!(perm.startsWith(GROUP_PREFIX) && permAttach.getValue())) {
                continue;
            }
            groupNames.add(perm.substring(GROUP_PREFIX.length()));
        }
        return groupNames.toArray(new String[groupNames.size()]);
    }

    public Permissible getPermissible(OfflinePlayer offline) {
        if (offline == null) {
            return null;
        }
        Permissible perm = null;
        if (offline instanceof Permissible) {
            perm = (Permissible) offline;
        } else {
            Player player = offline.getPlayer();
            if (player != null) {
                perm = player;
            }
        }
        return perm;
    }

    /**
     * Checks the permission from dinnerperms.
     *
     * @param perms      Permissible to check for
     * @param permission The permission to check
     * @return -1 if the permission is explicitly denied, 1 if the permission is allowed,
     *         0 if the permission is denied by a default.
     */
    public int internalHasPermission(Permissible perms, String permission) {
        if (perms.isPermissionSet(permission)) {
            return perms.hasPermission(permission) ? 1 : -1;
        } else {
            Permission perm = server.getPluginManager().getPermission(permission);
            if (perm != null) {
                return Objects.equals(perm.getDefault(), "op") ? 1 : 0;
            } else {
                return 0;
            }
        }
    }

    @Override
    public String getDetectionMessage() {
        return "Using the Bukkit Permissions API.";
    }

}
