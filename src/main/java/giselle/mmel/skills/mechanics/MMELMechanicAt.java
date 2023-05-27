package giselle.mmel.skills.mechanics;

import java.io.File;

import org.bukkit.configuration.ConfigurationSection;

import giselle.mmel.MythicMobsEffectLib;
import io.lumine.mythic.api.adapters.AbstractEntity;
import io.lumine.mythic.api.config.MythicLineConfig;
import io.lumine.mythic.api.skills.ITargetedEntitySkill;
import io.lumine.mythic.api.skills.SkillMetadata;
import io.lumine.mythic.api.skills.SkillResult;
import io.lumine.mythic.api.skills.placeholders.PlaceholderString;
import io.lumine.mythic.core.logging.MythicLogger;
import io.lumine.mythic.core.skills.SkillExecutor;
import io.lumine.mythic.core.skills.SkillMechanic;
import io.lumine.mythic.core.utils.annotations.MythicMechanic;

@MythicMechanic(author = "gisellevonbingen", name = "mmel_at", aliases = {}, description = "Play effect at targets")
public class MMELMechanicAt extends SkillMechanic implements ITargetedEntitySkill
{
	private final PlaceholderString name;

	public MMELMechanicAt(SkillExecutor manager, File file, String line, MythicLineConfig mlc)
	{
		super(manager, file, line, mlc);

		this.name = mlc.getPlaceholderString("name", null);

		if (name == null)
		{
			MythicLogger.errorMechanicConfig(this, mlc, "The 'name' option is required");
		}

	}

	@Override
	public SkillResult castAtEntity(SkillMetadata meta, AbstractEntity target)
	{
		String name = this.name.get(meta);
		ConfigurationSection effectConfig = MythicMobsEffectLib.instance().getEffects().get(name);

		if (effectConfig == null)
		{
			MythicLogger.errorMechanic(this, "Unknown effect name: " + name);
			return SkillResult.ERROR;
		}

		String className = effectConfig.getString("class", null);

		for (AbstractEntity target2 : meta.getEntityTargets())
		{
			MythicMobsEffectLib.instance().getEffectManager().start(className, effectConfig, target2.getBukkitEntity());
		}

		return null;
	}

}
