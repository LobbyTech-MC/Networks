package io.github.sefiraat.networks.utils.datatypes;

import com.jeff_media.morepersistentdatatypes.DataType;
import io.github.sefiraat.networks.network.stackcaches.CardInstance;
import io.github.sefiraat.networks.network.stackcaches.QuantumCache;
import io.github.sefiraat.networks.utils.Keys;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataAdapterContext;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import com.jeff_media.morepersistentdatatypes.DataType;

import io.github.sefiraat.networks.network.stackcaches.CardInstance;
import io.github.sefiraat.networks.network.stackcaches.QuantumCache;
import io.github.sefiraat.networks.utils.Keys;

/**
 * A {@link PersistentDataType} for {@link CardInstance}
 * Creatively thieved from {@see <a href="https://github.com/baked-libs/dough/blob/main/dough-data/src/main/java/io/github/bakedlibs/dough/data/persistent/PersistentUUIDDataType.java">PersistentUUIDDataType}
 *
 * @author Sfiguz7
 * @author Walshy
 */

public class PersistentQuantumStorageType implements PersistentDataType<PersistentDataContainer, QuantumCache> {

    public static final PersistentDataType<PersistentDataContainer, QuantumCache> TYPE = new PersistentQuantumStorageType();

    @Override
    @Nonnull
    public Class<PersistentDataContainer> getPrimitiveType() {
        return PersistentDataContainer.class;
    }

    @Override
    @Nonnull
    public Class<QuantumCache> getComplexType() {
        return QuantumCache.class;
    }

    @Override
    @Nonnull
    public PersistentDataContainer toPrimitive(@Nonnull QuantumCache complex, @Nonnull PersistentDataAdapterContext context) {
        final PersistentDataContainer container = context.newPersistentDataContainer();

        container.set(Keys.ITEM, DataType.ITEM_STACK, complex.getItemStack());
        container.set(Keys.AMOUNT, DataType.LONG, complex.getAmount());
        container.set(Keys.MAX_AMOUNT, DataType.INTEGER, complex.getLimit());
        container.set(Keys.VOID, DataType.BOOLEAN, complex.isVoidExcess());
        container.set(Keys.SUPPORTS_CUSTOM_MAX_AMOUNT, DataType.BOOLEAN, complex.supportsCustomMaxAmount());
        return container;
    }

    @Override
    @Nonnull
    public QuantumCache fromPrimitive(@Nonnull PersistentDataContainer primitive, @Nonnull PersistentDataAdapterContext context) {
        ItemStack item = primitive.get(Keys.ITEM, DataType.ITEM_STACK);
        if (item == null) {
            item = primitive.get(Keys.ITEM2, DataType.ITEM_STACK);
        }
        if (item == null) {
            item = primitive.get(Keys.ITEM3, DataType.ITEM_STACK);
        }

        Long amount;
        try {
            amount = primitive.get(Keys.AMOUNT, DataType.LONG);
            if (amount == null) {
                amount = primitive.get(Keys.AMOUNT2, DataType.LONG);
            }
            if (amount == null) {
                amount = primitive.getOrDefault(Keys.AMOUNT3, DataType.LONG, 0L);
            }
        } catch (Throwable ignored) {
            Integer amountI;
            amountI = primitive.get(Keys.AMOUNT, DataType.INTEGER);
            if (amountI == null) {
                amountI = primitive.get(Keys.AMOUNT2, DataType.INTEGER);
            }
            if (amountI == null) {
                amountI = primitive.getOrDefault(Keys.AMOUNT3, DataType.INTEGER, 0);
            }
            amount = amountI.longValue();
        }

        Integer limit = primitive.get(Keys.MAX_AMOUNT, DataType.INTEGER);
        if (limit == null) {
            limit = primitive.get(Keys.MAX_AMOUNT2, DataType.INTEGER);
        }
        if (limit == null) {
            limit = primitive.getOrDefault(Keys.MAX_AMOUNT3, DataType.INTEGER, 64);
        }

        Boolean voidExcess = primitive.get(Keys.VOID, DataType.BOOLEAN);
        if (voidExcess == null) {
            voidExcess = primitive.get(Keys.VOID2, DataType.BOOLEAN);
        }
        if (voidExcess == null) {
            voidExcess = primitive.getOrDefault(Keys.VOID3, DataType.BOOLEAN, false);
        }

        boolean supportsCustomMaxAmount = primitive.getOrDefault(Keys.SUPPORTS_CUSTOM_MAX_AMOUNT, DataType.BOOLEAN, false);

        return new QuantumCache(item, amount, limit, voidExcess, supportsCustomMaxAmount);
    }
}
