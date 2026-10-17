package com.ctnh.ctnhastral.registry;

import com.gregtechceu.gtceu.api.recipe.RecipeCondition;
import com.gregtechceu.gtceu.api.recipe.modifier.RecipeModifier;

import java.util.List;
import java.util.function.Supplier;

/**
 * GT 机器可选的环境消费适配，不属于环境核心，不自动修改任何机器定义。
 * 机器统一通过 {@code environmentRequirements(OxygenCondition::new)} 声明所需条件。
 *
 * <p>
 * 支持组合多个条件构造器；只追加声明的条件，不按机器或配方类型推断需求。
 * 每次应用通过构造器创建条件实例，保留配方已有的同类型条件，不重复追加或覆盖。
 * 内建环境条件仍受 GT hazardsEnabled 门控。
 */
public class CARecipeModifiers {

    @SafeVarargs
    public static RecipeModifier environmentRequirements(Supplier<? extends RecipeCondition<?>>... conditions) {
        var conditionFactories = List.of(conditions);
        return (machine, group, recipe) -> {
            for (var factory : conditionFactories) {
                var condition = factory.get();
                if (recipe.conditions.stream().noneMatch(existing -> existing.getType() == condition.getType())) {
                    recipe.conditions.add(condition);
                }
            }
            return null;
        };
    }
}
