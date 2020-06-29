package com.songoda.ultimatetimber;

import com.songoda.core.SongodaCore;
import com.songoda.core.SongodaPlugin;
import com.songoda.core.compatibility.CompatibleMaterial;
import com.songoda.core.configuration.Config;
import com.songoda.ultimatetimber.adapter.VersionAdapter;
import com.songoda.ultimatetimber.adapter.current.CurrentAdapter;
import com.songoda.ultimatetimber.adapter.legacy.LegacyAdapter;
import com.songoda.ultimatetimber.commands.CommandReload;
import com.songoda.ultimatetimber.commands.CommandToggle;
import com.songoda.ultimatetimber.manager.*;
import com.songoda.ultimatetimber.utils.NMSUtil;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @author Esophose
 */
public class UltimateTimber extends SongodaPlugin {

    private static UltimateTimber INSTANCE;

    private Set<Manager> managers;

    private VersionAdapter versionAdapter;
    private ChoppingManager choppingManager;
    private ConfigurationManager configurationManager;
    private HookManager hookManager;
    private com.songoda.core.commands.CommandManager commandManager;
    private PlacedBlockManager placedBlockManager;
    private SaplingManager saplingManager;
    private TreeAnimationManager treeAnimationManager;
    private TreeDefinitionManager treeDefinitionManager;
    private TreeDetectionManager treeDetectionManager;
    private TreeFallManager treeFallManager;

    public static UltimateTimber getInstance() {
        return INSTANCE;
    }

    @Override
    public void onPluginLoad() {
        INSTANCE = this;
    }

    @Override
    public void onPluginEnable() {
        // Run Songoda Updater
        SongodaCore.registerPlugin(this, 18, CompatibleMaterial.IRON_AXE);

        // Setup plugin commands
        this.commandManager = new com.songoda.core.commands.CommandManager(this);
        this.commandManager.addMainCommand("ut")
                .addSubCommands(new CommandReload(this),
                        new CommandToggle(this)
                );

        // Register managers
        this.managers = new HashSet<>();
        this.choppingManager = this.registerManager(ChoppingManager.class);
        this.configurationManager = new ConfigurationManager(this);
        this.hookManager = this.registerManager(HookManager.class);
        this.placedBlockManager = this.registerManager(PlacedBlockManager.class);
        this.saplingManager = this.registerManager(SaplingManager.class);
        this.treeAnimationManager = this.registerManager(TreeAnimationManager.class);
        this.treeDefinitionManager = this.registerManager(TreeDefinitionManager.class);
        this.treeDetectionManager = this.registerManager(TreeDetectionManager.class);
        this.treeFallManager = this.registerManager(TreeFallManager.class);

        // Load version adapter and managers
        this.setupVersionAdapter();
        this.reloadConfig();
    }

    @Override
    public void onPluginDisable() {
        this.disable();
    }

    @Override
    public void onConfigReload() {
        this.configurationManager.reload();
        this.managers.forEach(Manager::reload);
        this.setLocale(getConfig().getString("locale"), true);
    }

    @Override
    public List<Config> getExtraConfig() {
        return null;
    }

    /**
     * Disables most of the plugin
     */
    public void disable() {
        this.configurationManager.disable();
        this.managers.forEach(Manager::disable);
    }

    /**
     * Sets up the version adapter
     */
    private void setupVersionAdapter() {
        if (NMSUtil.getVersionNumber() > 12) {
            this.versionAdapter = new CurrentAdapter();
        } else {
            this.versionAdapter = new LegacyAdapter();
        }
    }

    /**
     * Registers a manager
     *
     * @param managerClass The class of the manager to create a new instance of
     * @param <T>          extends Manager
     * @return A new instance of the given manager class
     */
    private <T extends Manager> T registerManager(Class<T> managerClass) {
        try {
            T newManager = managerClass.getConstructor(UltimateTimber.class).newInstance(this);
            this.managers.add(newManager);
            return newManager;
        } catch (Exception ex) {
            ex.printStackTrace();
            return null;
        }
    }

    /**
     * Gets the active version adapter being used
     *
     * @return The VersionAdapter being used for the plugin
     */
    public VersionAdapter getVersionAdapter() {
        return this.versionAdapter;
    }

    /**
     * Gets the chopping manager
     *
     * @return The ChoppingManager instance
     */
    public ChoppingManager getChoppingManager() {
        return this.choppingManager;
    }

    /**
     * Gets the configuration manager
     *
     * @return The ConfigurationManager instance
     */
    public ConfigurationManager getConfigurationManager() {
        return this.configurationManager;
    }

    /**
     * Gets the hook manager
     *
     * @return The HookManager instance
     */
    public HookManager getHookManager() {
        return this.hookManager;
    }

    /**
     * Gets the placed block manager
     *
     * @return The PlacedBlockManager instance
     */
    public PlacedBlockManager getPlacedBlockManager() {
        return this.placedBlockManager;
    }

    /**
     * Gets the sapling manager
     *
     * @return The SaplingManager instance
     */
    public SaplingManager getSaplingManager() {
        return this.saplingManager;
    }

    /**
     * Gets the tree animation manager
     *
     * @return The TreeAnimationManager instance
     */
    public TreeAnimationManager getTreeAnimationManager() {
        return this.treeAnimationManager;
    }

    /**
     * Gets the tree definition manager
     *
     * @return The TreeDefinitionManager instance
     */
    public TreeDefinitionManager getTreeDefinitionManager() {
        return this.treeDefinitionManager;
    }

    /**
     * Gets the tree detection manager
     *
     * @return The TreeDetectionManager instance
     */
    public TreeDetectionManager getTreeDetectionManager() {
        return this.treeDetectionManager;
    }

    /**
     * Gets the tree fall manager
     *
     * @return The TreeFallManager instance
     */
    public TreeFallManager getTreeFallManager() {
        return this.treeFallManager;
    }

}
