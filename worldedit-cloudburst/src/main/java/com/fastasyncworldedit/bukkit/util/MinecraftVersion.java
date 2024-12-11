package com.fastasyncworldedit.bukkit.util;

import com.google.common.collect.ComparisonChain;
import cn.nukkit.network.protocol.ProtocolInfo;

import javax.annotation.Nonnull;

/**
 * Utility class for retrieving and comparing minecraft server versions.
 */
public class MinecraftVersion implements Comparable<MinecraftVersion> {

    public static final MinecraftVersion NETHER = new MinecraftVersion(1, 16);
    public static final MinecraftVersion CAVES_17 = new MinecraftVersion(1, 17);
    public static final MinecraftVersion CAVES_18 = new MinecraftVersion(1, 18);
    private static MinecraftVersion current = null;

    private final int major;
    private final int minor;
    private final int release;

    /**
     * Construct a new version with major, minor and release version.
     *
     * @param major   Major part of the version, only {@code 1} would make sense.
     * @param minor   Minor part, full updates, e.g. Nether &amp; Caves &amp; Cliffs
     * @param release Release, changes for the server software during a minor update.
     */
    public MinecraftVersion(int major, int minor, int release) {
        this.major = major;
        this.minor = minor;
        this.release = release;
    }

    /**
     * Construct a new version with major and minor version.
     * The release version is set to 0, therefore ignored.
     *
     * @see MinecraftVersion#MinecraftVersion(int, int, int)
     */
    public MinecraftVersion(int major, int minor) {
        this(major, minor, 0);
    }

    /**
     * Construct a new version with major, minor and release based on the server version.
     * @deprecated use {@link MinecraftVersion#getCurrent()} instead.
     */
    @Deprecated(since = "2.4.10", forRemoval = true)
    public MinecraftVersion() {
        MinecraftVersion current = getCurrent();

        this.major = current.getMajor();
        this.minor = current.getMinor();
        this.release = current.getRelease();
    }

    /**
     * Get the minecraft version that the server is currently running
     *
     * @since 2.1.0
     */
    public static MinecraftVersion getCurrent() {
        if (current == null) {
            return current = detectMinecraftVersion();
        }
        return current;
    }

    private static MinecraftVersion detectMinecraftVersion() {
        int major;
        int minor;
        int release;

        String[] parts = getPackageVersion().split("\\.");
        if (parts.length != 3) {
            throw new IllegalStateException("Failed to determine minecraft version!");
        }
        major = Integer.parseInt(parts[0]);
        minor = Integer.parseInt(parts[1]);
        release = Integer.parseInt(parts[2]);

        return new MinecraftVersion(major, minor, release);
    }

    /**
     * Determines the server version based on the CraftBukkit package path, e.g. {@code 1.21.50},
     * where 1.21.50 is the resolved version.
     *
     * @return The package version.
     */
    private static String getPackageVersion() {
        return ProtocolInfo.MINECRAFT_VERSION;
    }

    /**
     * @param other The other version to compare against.
     * @return {@code true} if this version is equal to the other version.
     */
    public boolean isEqual(MinecraftVersion other) {
        return compareTo(other) == 0;
    }

    /**
     * @param other The other version to compare against.
     * @return {@code true} if this version is higher or equal compared to the other version.
     */
    public boolean isEqualOrHigherThan(MinecraftVersion other) {
        return compareTo(other) >= 0;
    }

    /**
     * @param other The other version to compare against.
     * @return {@code true} if this version is lower or equal compared to the other version.
     */
    public boolean isEqualOrLowerThan(MinecraftVersion other) {
        return compareTo(other) <= 0;
    }

    /**
     * @param other The other version to compare against.
     * @return {@code true} if this version is higher than the other version.
     */
    public boolean isHigherThan(MinecraftVersion other) {
        return compareTo(other) > 0;
    }

    /**
     * @param other The other version to compare against.
     * @return {@code true} if this version is lower than to the other version.
     */
    public boolean isLowerThan(MinecraftVersion other) {
        return compareTo(other) < 0;
    }

    public int getMajor() {
        return major;
    }

    public int getMinor() {
        return minor;
    }

    public int getRelease() {
        return release;
    }

    @Override
    public int compareTo(@Nonnull MinecraftVersion other) {
        if (other.equals(this)) {
            return 0;
        }
        return ComparisonChain.start()
                .compare(getMajor(), other.getMajor())
                .compare(getMinor(), other.getMinor())
                .compare(getRelease(), other.getRelease()).result();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        MinecraftVersion that = (MinecraftVersion) o;

        if (getMajor() != that.getMajor()) {
            return false;
        }
        if (getMinor() != that.getMinor()) {
            return false;
        }
        return getRelease() == that.getRelease();
    }

    @Override
    public String toString() {
        return major + "." + minor + "." + release;
    }

}
