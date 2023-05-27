package giselle.mmel;

import java.io.File;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.Vector;

import de.slikey.effectlib.EffectManager;
import io.lumine.mythic.bukkit.events.MythicReloadedEvent;
import io.lumine.mythic.core.skills.SkillExecutor;
import io.lumine.mythic.core.skills.SkillMechanic;
import io.lumine.mythic.core.utils.annotations.AnnotationUtil;
import io.lumine.mythic.core.utils.annotations.MythicMechanic;

public class MythicMobsEffectLib extends JavaPlugin implements Listener
{
	private static MythicMobsEffectLib INSTANCE = null;

	public static MythicMobsEffectLib instance()
	{
		return INSTANCE;
	}

	private EffectManager effectManager;
	private Map<String, ConfigurationSection> effects;

	@Override
	public void onLoad()
	{
		super.onLoad();

		this.inject();
	}

	@Override
	public void onEnable()
	{
		INSTANCE = this;

		Bukkit.getPluginManager().registerEvents(this, this);

		this.effectManager = new EffectManager(this);
		this.effects = new HashMap<>();

		this.reloadEffects();
	}

	@SuppressWarnings("unchecked")
	private void inject()
	{
		Logger logger = this.getLogger();
		logger.info("Starting inject component classes");

		try
		{
			Collection<Class<?>> mechanicsClasses = AnnotationUtil.getAnnotatedClasses(this, "giselle.mmel", MythicMechanic.class);
			logger.info("Found " + mechanicsClasses.size() + " MythicMechanic classes");

			Class<SkillExecutor> c = SkillExecutor.class;
			Field field = c.getDeclaredField("MECHANICS");
			field.setAccessible(true);
			Map<String, Class<? extends SkillMechanic>> map = (Map<String, Class<? extends SkillMechanic>>) field.get(c);

			for (Class<?> clazz : mechanicsClasses)
			{
				try
				{
					String name = ((MythicMechanic) clazz.getAnnotation(MythicMechanic.class)).name();
					String[] aliases = ((MythicMechanic) clazz.getAnnotation(MythicMechanic.class)).aliases();

					if (SkillMechanic.class.isAssignableFrom(clazz))
					{
						map.put(name.toUpperCase(), (Class<? extends SkillMechanic>) clazz);

						for (String alias : aliases)
						{
							map.put(alias.toUpperCase(), (Class<? extends SkillMechanic>) clazz);
						}

						logger.info("Inject MythicMechanic: " + clazz);
					}

				}
				catch (Exception e)
				{
					e.printStackTrace();
				}

			}

		}
		catch (NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException e)
		{
			e.printStackTrace();
		}

		logger.info("Finishing inject component classes");
	}

	@Override
	public void onDisable()
	{
		this.effectManager.dispose();
		HandlerList.unregisterAll((Listener) this);
	}

	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args)
	{
		if (args.length == 0)
		{
			this.printEffects(sender, 0);
		}
		else
		{
			var sub = args[0];

			if (sub.equalsIgnoreCase("reload") == true)
			{
				this.reloadEffects(sender);
				return true;
			}
			else if (sub.equalsIgnoreCase("list") == true)
			{
				var page = 0;

				try
				{
					if (args.length > 1)
					{
						page = Math.max(Integer.parseInt(args[1]) - 1, 0);
					}

				}
				catch (Exception e)
				{

				}

				this.printEffects(sender, page);
				return true;
			}
			else if (sub.equalsIgnoreCase("play") == true)
			{
				if (args.length < 2)
				{
					sender.sendMessage("Effect name is missing");
					return false;
				}

				String name = args[1];
				ConfigurationSection effectConfig = this.effects.get(name);

				if (effectConfig == null)
				{
					sender.sendMessage("Unknown effect name: " + name);
					return false;
				}

				if (sender instanceof Entity entity)
				{
					String className = effectConfig.getString("class", null);
					Location targetLocation = entity.getLocation().add(entity.getLocation().getDirection().multiply(10));
					MythicMobsEffectLib.instance().getEffectManager().start(className, effectConfig, entity).setTargetLocation(targetLocation);
				}

				return true;
			}

		}

		return super.onCommand(sender, command, label, args);
	}

	@Override
	public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args)
	{
		if (args.length == 1)
		{
			return Arrays.asList("reload", "list", "play");
		}
		else
		{
			var sub = args[0];

			if (sub.equalsIgnoreCase("list") == true)
			{
				if (args.length == 2)
				{
					return Arrays.asList("0");
				}

			}
			else if (sub.equalsIgnoreCase("play") == true)
			{
				if (args.length == 2)
				{
					return this.effects.keySet().stream().toList();
				}

			}

		}

		return super.onTabComplete(sender, command, alias, args);
	}

	private void printEffects(CommandSender sender, int page)
	{
		var countPerPage = 5;

		Set<String> keys = this.effects.keySet();
		var pageMaxExclusive = (keys.size() + (countPerPage - 1)) / countPerPage;
		page = Math.min(Math.max(page, 0), pageMaxExclusive);

		List<String> keysInPage = keys.stream().skip(countPerPage * page).limit(countPerPage).toList();
		sender.sendMessage("===== Page " + (page + 1) + " / " + pageMaxExclusive);

		for (var i = 0; i < keysInPage.size(); i++)
		{
			String key = keysInPage.get(i);
			ConfigurationSection effectConfig = this.effects.get(key);
			sender.sendMessage((page * countPerPage) + (i + 1) + ": " + key + " - " + effectConfig.getString("description", ""));
		}

	}

	@EventHandler
	public void onMythicReloaded(MythicReloadedEvent e)
	{
		this.reloadEffects();
	}

	private void reloadEffects()
	{
		this.reloadEffects(this.getServer().getConsoleSender());
	}

	private void reloadEffects(CommandSender sender)
	{
		this.effects.clear();

		sender.sendMessage("Starting load all MythicMobsEffectLib's effects");

		for (File file : this.getEffectsDirecotry().listFiles(f -> f.getName().toLowerCase().endsWith(".yml")))
		{
			try
			{
				var config = new YamlConfiguration();
				config.load(file);

				for (String name : config.getKeys(false))
				{
					ConfigurationSection effectConfig = config.getConfigurationSection(name);
					String className = effectConfig.getString("class", null);

					if (className == null)
					{
						sender.sendMessage("Error in:" + file.getAbsolutePath());
						sender.sendMessage("Class name is missing at: " + name);
						continue;
					}

					this.effects.put(name, effectConfig);
					sender.sendMessage("Found effect name: '" + name + "' in file: '" + file.getName() + "'");
				}

			}
			catch (Exception e2)
			{
				sender.sendMessage("Error in:" + file.getAbsolutePath());
				sender.sendMessage(e2.toString());
			}

		}

		sender.sendMessage("Total " + this.effects.size() + " effects found");
		sender.sendMessage("Finishing load MythicMobsEffectLib's effects");
	}

	public File getEffectsDirecotry()
	{
		File dir = new File(this.getDataFolder(), "effects");
		dir.mkdirs();
		return dir;
	}

	public EffectManager getEffectManager()
	{
		return this.effectManager;
	}

	public Map<String, ConfigurationSection> getEffects()
	{
		return this.effects;
	}

	public static Vector getVector(Location from, Location to)
	{
		double dx = from.getX() - to.getX();
		double dt = from.getY() - to.getY();
		double dz = from.getZ() - to.getZ();
		double yaw = Math.atan2(dz, dx);
		double pitch = Math.atan2(Math.sqrt(dz * dz + dx * dx), dt) + Math.PI;
		double x = Math.sin(pitch) * Math.cos(yaw);
		double y = Math.sin(pitch) * Math.sin(yaw);
		double z = Math.cos(pitch);

		return new Vector(x, z, y);
	}
}
