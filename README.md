# Minecraft-MMEL

Add MythicMobs mechanic what play EffectLib's effect.
Effects be defined from yaml files.

# Dependencies

1. EffectLib-9.0: https://github.com/elBukkit/EffectLib/
1. MythicMobs-5.2.6: https://git.mythiccraft.io/mythiccraft/MythicMobs

# Commands

1. /mmel reload: Reload all effects.
2. /mmel list {page}: Show effects list.
3. /mmel play {effect_name}: Play effect to self. (For test)

# Define Effect

1. Create {your_file_name}.yaml file in '\plugins\MythicMobsEffectLib\effects'.
2. Write effect information as 'EffectLib's Configuration-Driven format'.
3. Reload MythicMobs or this plugin.
4. Check effects are loaded to see message or command.

See also:
1. https://dev.bukkit.org/projects/effectlib
2. https://reference.elmakers.com/#effectlib

# Mechanic

## 1. mmel_at

Playe effect at target's location.

### Attributes

|attribute|type|description|note|
|-|-|-|-|
|name|String|Effect name to play|Required|

### Example

Effects Yaml
```yaml
vortexTest:
 iterations: 4
 class: VortexEffect
 particle: smoke
 helixes: 16
 circles: 7
 grow: 0.1
 radius: 1
```

MythicMobs Skills Yaml
```yaml
mmel_at{name=vortexTest} @LivingInRadius{r=25}
```

## 2. mmel_fire

Play effect and fire it to targets.

### Attributes

|attributes|type|description|note|
|-|-|-|-|
|name|String|Effect name to play|Required|
|from|String|Targeters Expression to fire effect|Optional,<br>If empty, fired from caster|
|tracking|Boolean|Effect will chase targeted entity|Optional,<br>Default is True|

### Example

Effects Yaml
```yaml
equationTest:
 class: Equation
 type: repeating
 iterations: 100
 particle: redstone
 particles: 1
 particles2: 0
 x_equation: t
 y_equation: 0
 z_equation: 0
 asynchronous: true
 auto_orient: false
 color: #FFFFFF
 delay: 0
 disappear_with_origin_entity: false
 disappear_with_target_entity: false
 duration: 1000
```

MythicMobs Skills Yaml

Caster Fire 'equationTest' effect to all livings with in 25 blocks
```yaml
mmel_at{name=equationTest} @LivingInRadius{r=25}
```

All livings with in 25 blocks from caster will fire 'equationTest' effect to caster
```yaml
mmel_fire{name=equationTest;from="@LivingInRadius{r=25}"} @Self
```
