package giselle.mmel.skills.mechanics;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Entity;

import de.slikey.effectlib.Effect;
import de.slikey.effectlib.util.DynamicLocation;
import giselle.mmel.MythicMobsEffectLib;
import giselle.mmel.util.DirectionTrackingLocation;
import io.lumine.mythic.api.adapters.AbstractEntity;
import io.lumine.mythic.api.config.MythicLineConfig;
import io.lumine.mythic.api.skills.ITargetedEntitySkill;
import io.lumine.mythic.api.skills.SkillMetadata;
import io.lumine.mythic.api.skills.SkillResult;
import io.lumine.mythic.api.skills.placeholders.PlaceholderString;
import io.lumine.mythic.core.logging.MythicLogger;
import io.lumine.mythic.core.skills.SkillExecutor;
import io.lumine.mythic.core.skills.SkillMechanic;
import io.lumine.mythic.core.skills.targeters.IEntitySelector;
import io.lumine.mythic.core.utils.annotations.MythicMechanic;

@MythicMechanic(author = "gisellevonbingen", name = "mmel_fire", aliases = {}, description = "Fire effect to targets")
public class MMELMechanicFire extends SkillMechanic implements ITargetedEntitySkill
{
	private final PlaceholderString name;
	private final PlaceholderString from;
	private final boolean tracking;

	public MMELMechanicFire(SkillExecutor manager, File file, String line, MythicLineConfig mlc)
	{
		super(manager, file, line, mlc);

		this.name = mlc.getPlaceholderString("name", null);
		this.from = mlc.getPlaceholderString("from", null);
		this.tracking = mlc.getBoolean("tracking", true);

		if (this.name == null)
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

		List<Entity> froms = new ArrayList<>();

		if (this.from != null && parseSkillTargeter(this.from.get(meta)) instanceof IEntitySelector entitySelector)
		{
			entitySelector.getEntities(meta).stream().map(AbstractEntity::getBukkitEntity).forEach(froms::add);
		}
		else
		{
			froms.add(meta.getCaster().getEntity().getBukkitEntity());
		}

		String className = effectConfig.getString("class", null);

		for (Entity from : froms)
		{
			Location fromLocation = from.getLocation();

			meta.getEntityTargets().stream().map(AbstractEntity::getBukkitEntity).forEach(target2 ->
			{
				fromLocation.setDirection(MythicMobsEffectLib.getVector(fromLocation, target2.getLocation()));
				Effect effect = MythicMobsEffectLib.instance().getEffectManager().start(className, effectConfig, fromLocation);

				if (this.tracking == true)
				{
					effect.setDynamicOrigin(new DirectionTrackingLocation(from, new DynamicLocation(target2)));
				}

			});

		}

		return SkillResult.SUCCESS;
	}

}
