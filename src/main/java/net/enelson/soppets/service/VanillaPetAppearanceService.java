package net.enelson.soppets.service;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import net.enelson.soppets.SopPetsPlugin;
import net.enelson.soppets.model.PetDefinition;
import org.bukkit.Bukkit;
import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.Axolotl;
import org.bukkit.entity.Cat;
import org.bukkit.entity.Creeper;
import org.bukkit.entity.Enderman;
import org.bukkit.entity.Fox;
import org.bukkit.entity.Frog;
import org.bukkit.entity.Goat;
import org.bukkit.entity.Horse;
import org.bukkit.entity.Llama;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.MagmaCube;
import org.bukkit.entity.MushroomCow;
import org.bukkit.entity.Panda;
import org.bukkit.entity.Parrot;
import org.bukkit.entity.Phantom;
import org.bukkit.entity.Pig;
import org.bukkit.entity.PufferFish;
import org.bukkit.entity.Rabbit;
import org.bukkit.entity.Sheep;
import org.bukkit.entity.Slime;
import org.bukkit.entity.Snowman;
import org.bukkit.entity.Strider;
import org.bukkit.entity.TropicalFish;
import org.bukkit.entity.Villager;
import org.bukkit.entity.Wolf;
import org.bukkit.entity.ZombieVillager;

public final class VanillaPetAppearanceService {
    private final SopPetsPlugin plugin;

    public VanillaPetAppearanceService(SopPetsPlugin plugin) {
        this.plugin = plugin;
    }

    public void apply(LivingEntity entity, PetDefinition definition) {
        if (entity == null || definition == null || definition.getAppearanceSettings().isEmpty()) {
            return;
        }

        applySharedAppearance(entity, definition);

        if (entity instanceof Fox) {
            applyFox((Fox) entity, definition);
        }
        if (entity instanceof Frog) {
            applyFrog((Frog) entity, definition);
        }
        if (entity instanceof Sheep) {
            applySheep((Sheep) entity, definition);
        }
        if (entity instanceof Cat) {
            applyCat((Cat) entity, definition);
        }
        if (entity instanceof Wolf) {
            applyWolf((Wolf) entity, definition);
        }
        if (entity instanceof Rabbit) {
            applyRabbit((Rabbit) entity, definition);
        }
        if (entity instanceof Horse) {
            applyHorse((Horse) entity, definition);
        }
        if (entity instanceof Llama) {
            applyLlama((Llama) entity, definition);
        }
        if (entity instanceof Parrot) {
            applyParrot((Parrot) entity, definition);
        }
        if (entity instanceof MushroomCow) {
            applyMooshroom((MushroomCow) entity, definition);
        }
        if (entity instanceof Panda) {
            applyPanda((Panda) entity, definition);
        }
        if (entity instanceof TropicalFish) {
            applyTropicalFish((TropicalFish) entity, definition);
        }
        if (entity instanceof Axolotl) {
            applyAxolotl((Axolotl) entity, definition);
        }
        if (entity instanceof Villager) {
            applyVillager((Villager) entity, definition);
        }
        if (entity instanceof ZombieVillager) {
            applyZombieVillager((ZombieVillager) entity, definition);
        }
        if (entity instanceof Goat) {
            applyGoat((Goat) entity, definition);
        }
        if (entity instanceof Slime) {
            applySlime((Slime) entity, definition);
        }
        if (entity instanceof MagmaCube) {
            applySlime((MagmaCube) entity, definition);
        }
        if (entity instanceof Phantom) {
            applyPhantom((Phantom) entity, definition);
        }
        if (entity instanceof PufferFish) {
            applyPufferFish((PufferFish) entity, definition);
        }
        if (entity instanceof Creeper) {
            applyCreeper((Creeper) entity, definition);
        }
        if (entity instanceof Pig) {
            applyPig((Pig) entity, definition);
        }
        if (entity instanceof Strider) {
            applyStrider((Strider) entity, definition);
        }
        if (entity instanceof Snowman) {
            applySnowman((Snowman) entity, definition);
        }
        if (entity instanceof Enderman) {
            applyEnderman((Enderman) entity, definition);
        }
    }

    private void applySharedAppearance(LivingEntity entity, PetDefinition definition) {
        String bodyColor = definition.getAppearanceString("color");
        if (bodyColor != null) {
            if (entity instanceof Sheep) {
                applySheep((Sheep) entity, definition);
            }
        }
    }

    private void applyFox(Fox fox, PetDefinition definition) {
        applyEnum(definition, "fox-type", Fox.Type.class, fox::setFoxType);
    }

    private void applyFrog(Frog frog, PetDefinition definition) {
        applyEnum(definition, "frog-variant", Frog.Variant.class, frog::setVariant);
    }

    private void applySheep(Sheep sheep, PetDefinition definition) {
        DyeColor color = parseEnum(definition, "sheep-color", DyeColor.class);
        if (color == null) {
            color = parseEnum(definition, "color", DyeColor.class);
        }
        if (color != null) {
            sheep.setColor(color);
        }
        if (definition.hasAppearanceSetting("sheared")) {
            sheep.setSheared(definition.getAppearanceBoolean("sheared", false));
        }
    }

    private void applyCat(Cat cat, PetDefinition definition) {
        applyEnum(definition, "cat-type", Cat.Type.class, cat::setCatType);
        applyEnum(definition, "cat-collar-color", DyeColor.class, cat::setCollarColor);
        if (!definition.hasAppearanceSetting("cat-collar-color")) {
            applyEnum(definition, "collar-color", DyeColor.class, cat::setCollarColor);
        }
    }

    private void applyWolf(Wolf wolf, PetDefinition definition) {
        applyEnum(definition, "wolf-variant", Wolf.Variant.class, wolf::setVariant);
        applyEnum(definition, "wolf-collar-color", DyeColor.class, wolf::setCollarColor);
        if (!definition.hasAppearanceSetting("wolf-collar-color")) {
            applyEnum(definition, "collar-color", DyeColor.class, wolf::setCollarColor);
        }
        if (definition.hasAppearanceSetting("angry")) {
            wolf.setAngry(definition.getAppearanceBoolean("angry", false));
        }
    }

    private void applyRabbit(Rabbit rabbit, PetDefinition definition) {
        applyEnum(definition, "rabbit-type", Rabbit.Type.class, rabbit::setRabbitType);
    }

    private void applyHorse(Horse horse, PetDefinition definition) {
        applyEnum(definition, "horse-color", Horse.Color.class, horse::setColor);
        applyEnum(definition, "horse-style", Horse.Style.class, horse::setStyle);
    }

    private void applyLlama(Llama llama, PetDefinition definition) {
        applyEnum(definition, "llama-color", Llama.Color.class, llama::setColor);
        if (definition.hasAppearanceSetting("llama-strength")) {
            llama.setStrength(clamp(definition.getAppearanceInt("llama-strength", 1), 1, 5));
        }
    }

    private void applyParrot(Parrot parrot, PetDefinition definition) {
        applyEnum(definition, "parrot-variant", Parrot.Variant.class, parrot::setVariant);
    }

    private void applyMooshroom(MushroomCow cow, PetDefinition definition) {
        applyEnum(definition, "mooshroom-variant", MushroomCow.Variant.class, cow::setVariant);
    }

    private void applyPanda(Panda panda, PetDefinition definition) {
        applyEnum(definition, "panda-main-gene", Panda.Gene.class, panda::setMainGene);
        applyEnum(definition, "panda-hidden-gene", Panda.Gene.class, panda::setHiddenGene);
    }

    private void applyTropicalFish(TropicalFish fish, PetDefinition definition) {
        applyEnum(definition, "tropical-fish-pattern", TropicalFish.Pattern.class, fish::setPattern);
        applyEnum(definition, "tropical-fish-body-color", DyeColor.class, fish::setBodyColor);
        applyEnum(definition, "tropical-fish-pattern-color", DyeColor.class, fish::setPatternColor);
    }

    private void applyAxolotl(Axolotl axolotl, PetDefinition definition) {
        applyEnum(definition, "axolotl-variant", Axolotl.Variant.class, axolotl::setVariant);
    }

    private void applyVillager(Villager villager, PetDefinition definition) {
        applyEnum(definition, "villager-type", Villager.Type.class, villager::setVillagerType);
        applyEnum(definition, "villager-profession", Villager.Profession.class, villager::setProfession);
        if (definition.hasAppearanceSetting("villager-level")) {
            villager.setVillagerLevel(clamp(definition.getAppearanceInt("villager-level", 1), 1, 5));
        }
    }

    private void applyZombieVillager(ZombieVillager villager, PetDefinition definition) {
        applyEnum(definition, "villager-type", Villager.Type.class, villager::setVillagerType);
        applyEnum(definition, "villager-profession", Villager.Profession.class, villager::setVillagerProfession);
    }

    private void applyGoat(Goat goat, PetDefinition definition) {
        if (definition.hasAppearanceSetting("goat-left-horn")) {
            goat.setLeftHorn(definition.getAppearanceBoolean("goat-left-horn", true));
        }
        if (definition.hasAppearanceSetting("goat-right-horn")) {
            goat.setRightHorn(definition.getAppearanceBoolean("goat-right-horn", true));
        }
    }

    private void applySlime(Slime slime, PetDefinition definition) {
        if (definition.hasAppearanceSetting("slime-size")) {
            slime.setSize(clamp(definition.getAppearanceInt("slime-size", 1), 1, 50));
        }
    }

    private void applyPhantom(Phantom phantom, PetDefinition definition) {
        if (definition.hasAppearanceSetting("phantom-size")) {
            phantom.setSize(clamp(definition.getAppearanceInt("phantom-size", 0), 0, 64));
        }
    }

    private void applyPufferFish(PufferFish fish, PetDefinition definition) {
        if (definition.hasAppearanceSetting("pufferfish-state")) {
            fish.setPuffState(clamp(definition.getAppearanceInt("pufferfish-state", 0), 0, 2));
        }
    }

    private void applyCreeper(Creeper creeper, PetDefinition definition) {
        if (definition.hasAppearanceSetting("creeper-powered")) {
            creeper.setPowered(definition.getAppearanceBoolean("creeper-powered", false));
        }
    }

    private void applyPig(Pig pig, PetDefinition definition) {
        if (definition.hasAppearanceSetting("pig-saddled")) {
            pig.setSaddle(definition.getAppearanceBoolean("pig-saddled", false));
        }
    }

    private void applyStrider(Strider strider, PetDefinition definition) {
        if (definition.hasAppearanceSetting("strider-saddled")) {
            strider.setSaddle(definition.getAppearanceBoolean("strider-saddled", false));
        }
    }

    private void applySnowman(Snowman snowman, PetDefinition definition) {
        if (definition.hasAppearanceSetting("snowman-derp")) {
            snowman.setDerp(definition.getAppearanceBoolean("snowman-derp", false));
        }
    }

    private void applyEnderman(Enderman enderman, PetDefinition definition) {
        String materialName = definition.getAppearanceString("enderman-carried-material");
        if (materialName == null || materialName.trim().isEmpty()) {
            return;
        }
        try {
            Material material = Material.valueOf(materialName.trim().toUpperCase());
            BlockData blockData = Bukkit.createBlockData(material);
            enderman.setCarriedBlock(blockData);
        } catch (IllegalArgumentException exception) {
            this.plugin.getLogger().warning(
                "Invalid enderman-carried-material '" + materialName + "' for pet '" + definition.getId() + "'."
            );
        }
    }

    private <T> void applyEnum(PetDefinition definition, String key, Class<T> constantClass, ValueConsumer<T> consumer) {
        T constant = parseNamedConstant(definition, key, constantClass);
        if (constant != null) {
            consumer.accept(constant);
        }
    }

    @SuppressWarnings("unchecked")
    private <T> T parseNamedConstant(PetDefinition definition, String key, Class<T> constantClass) {
        String rawValue = definition.getAppearanceString(key);
        if (rawValue == null || rawValue.trim().isEmpty()) {
            return null;
        }
        String normalizedValue = rawValue.trim().toUpperCase();
        if (constantClass.isEnum()) {
            try {
                return (T) Enum.valueOf((Class<? extends Enum>) constantClass.asSubclass(Enum.class), normalizedValue);
            } catch (IllegalArgumentException exception) {
                this.plugin.getLogger().warning(
                    "Invalid " + key + " value '" + rawValue + "' for pet '" + definition.getId() + "'. Expected " + constantClass.getSimpleName() + "."
                );
                return null;
            }
        }
        try {
            for (Field field : constantClass.getFields()) {
                if (!Modifier.isStatic(field.getModifiers())) {
                    continue;
                }
                if (!constantClass.isAssignableFrom(field.getType())) {
                    continue;
                }
                if (!field.getName().equalsIgnoreCase(normalizedValue)) {
                    continue;
                }
                return constantClass.cast(field.get(null));
            }
        } catch (IllegalAccessException exception) {
            this.plugin.getLogger().warning(
                "Failed to read " + constantClass.getSimpleName() + " constants for pet '" + definition.getId() + "': " + exception.getMessage()
            );
            return null;
        }
        this.plugin.getLogger().warning(
            "Invalid " + key + " value '" + rawValue + "' for pet '" + definition.getId() + "'. Expected " + constantClass.getSimpleName() + "."
        );
        return null;
    }

    private <E extends Enum<E>> E parseEnum(PetDefinition definition, String key, Class<E> enumClass) {
        String rawValue = definition.getAppearanceString(key);
        if (rawValue == null || rawValue.trim().isEmpty()) {
            return null;
        }
        try {
            return Enum.valueOf(enumClass, rawValue.trim().toUpperCase());
        } catch (IllegalArgumentException exception) {
            this.plugin.getLogger().warning(
                "Invalid " + key + " value '" + rawValue + "' for pet '" + definition.getId() + "'. Expected " + enumClass.getSimpleName() + "."
            );
            return null;
        }
    }

    private int clamp(int value, int min, int max) {
        return Math.max(min, Math.min(max, value));
    }

    private interface ValueConsumer<T> {
        void accept(T value);
    }
}
