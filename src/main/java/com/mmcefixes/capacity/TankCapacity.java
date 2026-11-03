package com.mmcefixes.capacity;


public final class TankCapacity {

    private TankCapacity() {}

    /**
     * Calculate the tank capacity based on the base capacity and the number of upgrades.
     * The math is overflow-safe, ensuring that the capacity does not exceed Integer.MAX_VALUE.
     *
     * @param base The base capacity of the tank.
     * @param upgrades The number of capacity upgrades applied to the tank.
     * @return The calculated tank capacity.
     */
    public static int calculate(int base, int upgrades) {
        long capacity = Math.max(1, base);

        // Capacity cards multiply tank size by four, so clamp before the next step would overflow
        for (int remaining = Math.max(0, upgrades); remaining > 0; remaining--) {
            if (capacity > Integer.MAX_VALUE / 4L) return Integer.MAX_VALUE;

            capacity *= 4L;
        }

        return (int) capacity;
    }
}
