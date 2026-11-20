package com.ctnh.ctnhastral.data;

import com.gregtechceu.gtceu.api.data.damagesource.DamageTypeData;

import net.minecraft.data.worldgen.BootstapContext;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.damagesource.DamageScaling;
import net.minecraft.world.damagesource.DamageType;

/**
 * Astral 环境系统的伤害类型注册。
 *
 * <p>
 * {@code MedicalCondition} 构造器内建的 damage type 键锁定
 * {@code gtceu:medical_condition/<name>} 命名空间且实例私有，但运行时伤害源按
 * 注册表 key 查找——此处以相同 key 重建 {@link DamageTypeData} 并经 datagen
 * bootstrap 注册（scaling/tag 与 MedicalCondition 内部构造保持一致），
 * {@code CAMedicalConditions} 的伤害路径即可命中。输出 JSON 位于
 * {@code data/gtceu/damage_type/medical_condition/}（跨命名空间属 API 硬约束）。
 *
 * <p>
 * BYPASSES_ARMOR 标签由 {@link CADamageTypeTagsProvider} 落盘（环境伤害无从被
 * 护甲减免——缺氧/失温/热射是环境作用，与 GT 自家医疗条件同语义）。
 */
public class CADamageTypes {

    /** 医疗条件伤害类型：绕过护甲、不随难度缩放（与 MedicalCondition 内建构造一致）。 */
    private static DamageTypeData medical(String name) {
        return DamageTypeData.builder()
                .simpleId("medical_condition/" + name)
                .scaling(DamageScaling.NEVER)
                .tag(DamageTypeTags.BYPASSES_ARMOR)
                .build();
    }

    public static final DamageTypeData HYPOXIA_MILD = medical("hypoxia_mild");
    public static final DamageTypeData HYPOXIA_SEVERE = medical("hypoxia_severe");
    public static final DamageTypeData HYPOTHERMIA = medical("hypothermia");
    public static final DamageTypeData HYPERTHERMIA = medical("hyperthermia");

    public static void init() {}

    public static void bootstrap(BootstapContext<DamageType> ctx) {
        HYPOXIA_MILD.register(ctx);
        HYPOXIA_SEVERE.register(ctx);
        HYPOTHERMIA.register(ctx);
        HYPERTHERMIA.register(ctx);
    }
}
