package giselle.mmel.util;

import org.bukkit.Location;
import org.bukkit.entity.Entity;

import de.slikey.effectlib.util.DynamicLocation;
import giselle.mmel.MythicMobsEffectLib;

public class DirectionTrackingLocation extends DynamicLocation
{
	private DynamicLocation target;

	public DirectionTrackingLocation(Entity origin, DynamicLocation target)
	{
		super(origin);
		this.target = target;
	}

	public DirectionTrackingLocation(Location origin, DynamicLocation target)
	{
		super(origin);
		this.target = target;
	}

	@Override
	public void update()
	{
		super.update();

		this.getTarget().update();
	}

	@Override
	public Location getLocation()
	{
		Location originLocation = super.getLocation().clone();
		Location targetLocation = this.getTarget().getLocation();
		originLocation.setDirection(MythicMobsEffectLib.getVector(originLocation, targetLocation));
		return originLocation;
	}

	public void setTarget(DynamicLocation target)
	{
		this.target = target;
	}

	public DynamicLocation getTarget()
	{
		return this.target;
	}

}
