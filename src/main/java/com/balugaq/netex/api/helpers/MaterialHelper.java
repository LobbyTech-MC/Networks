package com.balugaq.netex.api.helpers;

import com.google.common.base.Preconditions;
import lombok.experimental.UtilityClass;
import net.guizhanss.guizhanlib.utils.StringUtil;
import org.bukkit.Material;

import javax.annotation.Nonnull;

@UtilityClass
public final class MaterialHelper {
    @Nonnull
    public static String getName(@Nonnull Material mat) {
        return LanguageHelper.getLangOrKey(getKey(mat));
    }

    @Nonnull
    public static String getKey(@Nonnull Material mat) {
        Preconditions.checkArgument(mat != null, "材料不能为空");
        String type = mat.isBlock() ? "block" : "item";
        return type + "_" + mat.getKey().getNamespace() + "_" + mat.getKey().getKey();
    }

    @Nonnull
    public static String getName(@Nonnull String material) {
        return getName(material, false);
    }

    @Nonnull
    public static String getName(@Nonnull String material, boolean emptyString) {
        Material mat = Material.getMaterial(material);
        if (mat == null) {
            return emptyString ? "" : StringUtil.humanize(material);
        } else {
            return getName(mat);
        }
    }
}
