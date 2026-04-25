package net.enelson.soppets.service;

import java.lang.reflect.Method;
import net.enelson.soppets.SopPetsPlugin;
import org.bukkit.entity.Entity;

public final class FmmBridgeService {
    private final SopPetsPlugin plugin;
    private Class<?> dynamicEntityClass;
    private Class<?> modeledEntityManagerClass;
    private Method createMethod;
    private Method removeMethod;
    private Method hasAnimationMethod;
    private Method playAnimationMethod;
    private Method stopCurrentAnimationsMethod;
    private Method modelExistsMethod;
    private boolean available;

    public FmmBridgeService(SopPetsPlugin plugin) {
        this.plugin = plugin;
        reload();
    }

    public void reload() {
        this.available = false;
        this.dynamicEntityClass = null;
        this.modeledEntityManagerClass = null;
        this.createMethod = null;
        this.removeMethod = null;
        this.hasAnimationMethod = null;
        this.playAnimationMethod = null;
        this.stopCurrentAnimationsMethod = null;
        this.modelExistsMethod = null;

        if (!this.plugin.getConfig().getBoolean("models.prefer-freeminecraftmodels", true)) {
            return;
        }
        if (this.plugin.getServer().getPluginManager().getPlugin("FreeMinecraftModels") == null) {
            return;
        }

        try {
            this.dynamicEntityClass = findClass(
                "com.magmaguy.freeminecraftmodels.customentity.DynamicEntity",
                "customentity.com.magmaguy.freeminecraftmodels.DynamicEntity"
            );
            this.modeledEntityManagerClass = findClass(
                "api.com.magmaguy.freeminecraftmodels.ModeledEntityManager",
                "com.magmaguy.freeminecraftmodels.api.ModeledEntityManager"
            );
            this.removeMethod = this.dynamicEntityClass.getMethod("remove");
            for (Method method : this.dynamicEntityClass.getMethods()) {
                if (!method.getName().equals("create")) {
                    continue;
                }
                Class<?>[] parameters = method.getParameterTypes();
                if (parameters.length != 2) {
                    continue;
                }
                if (parameters[0] != String.class) {
                    continue;
                }
                if (!parameters[1].isAssignableFrom(Entity.class) && !Entity.class.isAssignableFrom(parameters[1])) {
                    continue;
                }
                this.createMethod = method;
                break;
            }
            if (this.createMethod == null) {
                throw new NoSuchMethodException("DynamicEntity.create(String, <entity>)");
            }
            this.hasAnimationMethod = this.dynamicEntityClass.getMethod("hasAnimation", String.class);
            this.stopCurrentAnimationsMethod = this.dynamicEntityClass.getMethod("stopCurrentAnimations");
            for (Method method : this.dynamicEntityClass.getMethods()) {
                if (!method.getName().equals("playAnimation")) {
                    continue;
                }
                Class<?>[] parameters = method.getParameterTypes();
                if (parameters.length < 1) {
                    continue;
                }
                if (parameters[0] != String.class) {
                    continue;
                }
                this.playAnimationMethod = method;
                break;
            }
            if (this.playAnimationMethod == null) {
                throw new NoSuchMethodException("DynamicEntity.playAnimation(String, ...)");
            }
            if (this.modeledEntityManagerClass != null) {
                this.modelExistsMethod = this.modeledEntityManagerClass.getMethod("modelExists", String.class);
            }
            this.available = true;
        } catch (Exception exception) {
            this.plugin.getLogger().warning("FreeMinecraftModels detected, but its API could not be hooked: " + exception.getMessage());
        }
    }

    public boolean isAvailable() {
        return this.available;
    }

    public boolean modelExists(String modelId) {
        if (!this.available || modelId == null || modelId.isEmpty()) {
            return false;
        }
        if (this.modelExistsMethod == null) {
            return true;
        }
        try {
            return Boolean.TRUE.equals(this.modelExistsMethod.invoke(null, modelId));
        } catch (Exception exception) {
            this.plugin.getLogger().warning("Failed to check FMM model '" + modelId + "': " + exception.getMessage());
            return false;
        }
    }

    public Object createModel(String modelId, Entity carrier) {
        if (!this.available || modelId == null || modelId.isEmpty() || carrier == null) {
            return null;
        }
        if (!modelExists(modelId)) {
            this.plugin.getLogger().warning("FMM model '" + modelId + "' does not exist or is not loaded.");
            return null;
        }
        try {
            Class<?> carrierType = this.createMethod.getParameterTypes()[1];
            if (!carrierType.isInstance(carrier)) {
                this.plugin.getLogger().warning(
                    "FMM create() expects " + carrierType.getName() + " but got " + carrier.getClass().getName() + "."
                );
                return null;
            }
            Object handle = this.createMethod.invoke(null, modelId, carrier);
            if (handle == null) {
                this.plugin.getLogger().warning("FMM returned null while creating model '" + modelId + "'.");
            }
            return handle;
        } catch (Exception exception) {
            this.plugin.getLogger().warning("Failed to create FMM model '" + modelId + "': " + exception.getMessage());
            return null;
        }
    }

    public void removeModel(Object handle) {
        if (!this.available || handle == null) {
            return;
        }
        try {
            this.removeMethod.invoke(handle);
        } catch (Exception exception) {
            this.plugin.getLogger().warning("Failed to remove FMM model: " + exception.getMessage());
        }
    }

    public boolean hasAnimation(Object handle, String animation) {
        if (!this.available || handle == null || animation == null || animation.isEmpty()) {
            return false;
        }
        try {
            return Boolean.TRUE.equals(this.hasAnimationMethod.invoke(handle, animation));
        } catch (Exception exception) {
            return false;
        }
    }

    public boolean playAnimation(Object handle, String animation, boolean loop) {
        if (!this.available || handle == null || animation == null || animation.isEmpty()) {
            return false;
        }
        try {
            Class<?>[] parameters = this.playAnimationMethod.getParameterTypes();
            if (parameters.length == 1) {
                this.playAnimationMethod.invoke(handle, animation);
            } else if (parameters.length == 2) {
                Object secondArgument = resolveAnimationSecondArgument(parameters[1], loop);
                if (secondArgument == UnsupportedArgument.INSTANCE) {
                    this.plugin.getLogger().warning(
                        "Unsupported FMM playAnimation second parameter type: " + parameters[1].getName()
                    );
                    return false;
                }
                this.playAnimationMethod.invoke(handle, animation, secondArgument);
            } else {
                this.plugin.getLogger().warning("Unsupported FMM playAnimation parameter count: " + parameters.length);
                return false;
            }
            return true;
        } catch (Exception exception) {
            this.plugin.getLogger().warning("Failed to play FMM animation '" + animation + "': " + exception.getMessage());
            return false;
        }
    }

    public void stopCurrentAnimations(Object handle) {
        if (!this.available || handle == null) {
            return;
        }
        try {
            this.stopCurrentAnimationsMethod.invoke(handle);
        } catch (Exception exception) {
            this.plugin.getLogger().warning("Failed to stop FMM animations: " + exception.getMessage());
        }
    }

    private Class<?> findClass(String... classNames) throws ClassNotFoundException {
        ClassNotFoundException last = null;
        for (String className : classNames) {
            try {
                return Class.forName(className);
            } catch (ClassNotFoundException exception) {
                last = exception;
            }
        }
        throw last == null ? new ClassNotFoundException("No matching class name found.") : last;
    }

    private Object resolveAnimationSecondArgument(Class<?> parameterType, boolean loop) {
        if (parameterType == boolean.class || parameterType == Boolean.class) {
            return loop;
        }
        if (parameterType.isEnum()) {
            Object[] constants = parameterType.getEnumConstants();
            if (constants == null || constants.length == 0) {
                return UnsupportedArgument.INSTANCE;
            }
            for (Object constant : constants) {
                String name = ((Enum<?>) constant).name();
                if (loop && (name.equalsIgnoreCase("LOOP") || name.equalsIgnoreCase("REPEAT"))) {
                    return constant;
                }
                if (!loop && (name.equalsIgnoreCase("PLAY_ONCE") || name.equalsIgnoreCase("ONCE") || name.equalsIgnoreCase("SINGLE"))) {
                    return constant;
                }
            }
            return constants[0];
        }
        return UnsupportedArgument.INSTANCE;
    }

    private enum UnsupportedArgument {
        INSTANCE
    }
}
