package com.ctnh.ctnhastral.data;

import com.gregtechceu.gtceu.api.data.medicalcondition.MedicalCondition;
import com.gregtechceu.gtceu.api.data.medicalcondition.Symptom;

/**
 * Astral 自定义医疗条件，注册进 GT 的 {@link MedicalCondition#CONDITIONS}。
 *
 * <p>
 * 症状阈值必须按 fork {@code MedicalConditionTracker#calculateStage} 的三项乘积语义配置
 * （stage = 进度 / (阈值 × 满程 × stages)），症状仅在 阈值×stages ≤ 1 时可点亮——
 * 本类一律 stages=1：阈值分位激活，每再进一档阈值分位强度 +1
 * （GT 自带的 .2~.75 × 10 档配置在该实现下休眠，勿模仿）。进度单位为"秒"，
 * HEAL 条件净速率 = 喂给速率 - 1/s。生理学与数值依据见 docs/oxygen-env-design.md，
 * 数值均为待评审的建议值。
 */
public class CAMedicalConditions {

    /** 轻度缺氧（可呼吸下限~重度阈值间）：慢性，满程 450。 */
    public static final MedicalCondition HYPOXIA_MILD = new MedicalCondition("hypoxia_mild", 0x3d85a6, 450,
            MedicalCondition.IdleProgressionType.HEAL, 1, false,
            new Symptom.ConfiguredSymptom(Symptom.MINING_FATIGUE, 1, .3F),
            new Symptom.ConfiguredSymptom(Symptom.WEAKNESS, 1, .5F),
            new Symptom.ConfiguredSymptom(Symptom.SLOWNESS, 1, .7F));

    /** 重度缺氧（海拔 8000m 级）：急性，满程 60。 */
    public static final MedicalCondition HYPOXIA_SEVERE = new MedicalCondition("hypoxia_severe", 0x0b3d91, 60,
            MedicalCondition.IdleProgressionType.HEAL, 1, false,
            new Symptom.ConfiguredSymptom(Symptom.NAUSEA, 1, .2F),
            new Symptom.ConfiguredSymptom(Symptom.BLINDNESS, 1, .4F),
            new Symptom.ConfiguredSymptom(Symptom.WEAKNESS, 1, .6F),
            new Symptom.ConfiguredSymptom(Symptom.DEATH));

    /** 失温（全身性低温，慢性，满程 600）：-20℃ 以下按温差线性累积。 */
    public static final MedicalCondition HYPOTHERMIA = new MedicalCondition("hypothermia", 0x4a9bd4, 600,
            MedicalCondition.IdleProgressionType.HEAL, 1, false,
            new Symptom.ConfiguredSymptom(Symptom.SLOWNESS, 1, .3F),
            new Symptom.ConfiguredSymptom(Symptom.MINING_FATIGUE, 1, .5F),
            new Symptom.ConfiguredSymptom(Symptom.WEAKNESS, 1, .7F),
            new Symptom.ConfiguredSymptom(Symptom.HEALTH_DEBUFF, 1, .85F),
            new Symptom.ConfiguredSymptom(Symptom.DEATH));

    /** 热射病（急性，满程 300）：+40℃ 以上按温差线性累积。 */
    public static final MedicalCondition HYPERTHERMIA = new MedicalCondition("hyperthermia", 0xd4552a, 300,
            MedicalCondition.IdleProgressionType.HEAL, 1, false,
            new Symptom.ConfiguredSymptom(Symptom.WEAKNESS, 1, .3F),
            new Symptom.ConfiguredSymptom(Symptom.NAUSEA, 1, .5F),
            new Symptom.ConfiguredSymptom(Symptom.HUNGER, 1, .65F),
            new Symptom.ConfiguredSymptom(Symptom.RANDOM_DAMAGE, 1, .8F),
            new Symptom.ConfiguredSymptom(Symptom.DEATH));

    public static void init() {}
}
