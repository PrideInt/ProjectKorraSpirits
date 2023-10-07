# ProjectKorraSpirits

> [!IMPORTANT]
> ## IN DEVELOPMENT

> Some or most things described below may not be fully developed yet.

> ***- developed by Pride***

## Overview

This is a Spirits side-plugin (not officially) for ProjectKorra made by me (Pride). This is the upgraded
(technically and literally), brand-spanking-new version of my previous Spirits project; 
[Spirits: The Complete Set](https://github.com/PrideInt/Spirits-The-Complete-Set/). Only now we don't 
need any dependencies (except for ProjectKorra obviously). This is a standalone Spirits plugin and 
ability pack--with new abilities plus a whole API--which is made to supersede previous Spirits projects.

A whole new game function including a **spirit world**, **a boss**, **spirit items** and other cool
stuff. From new lore (which is kinda basically just ripped off from another game, *if you know, you know*).

> [!NOTE]
> One thing to keep in mind: 
> *If you are annoyed about it not being 100% in canonity, **then get the hell 
out of here***.

## API

This side plugin has its own API apart from ProjectKorra's, which allows developers to work with 
functionalities including **events**, **spirit spawning** and **spirit destroying**.

### Spirit

The base abstract superclass of all **`Spirit`** instances. Here we define most of the functions
these instances use. E.g., `of(Entity)`, `exists(Entity)`, `destroy(Entity)`, `spawnEntity(...)`,
`remove()`.

`Spirit` is an object that holds references to the `World` it is spawned in, the `Location` it is
spawned at, the `Entity` that is used to portray the `Spirit`, and a `SpiritRecord` that holds
definitions of the `Spirit` including: name, type, `EntityType` and revert time.

### ReplaceableSpirit

`ReplaceableSpirit` extends `Spirit` (similar to the way `LivingEntity` extends `Entity`). Used to
implement spirits that need to replace already existing entities or mobs (either temporarily or 
permanently).

For example, if we have some ability that "corrupts" other entities and turn them to `DarkSpirit`s.
If we want them to permanently replace said entity, we can do that. If we want them to only temporarily
replace the entity and then convert them back to the original entity, we can do that too. Any
`Spirit` instance that extends `ReplaceableSpirit` will be able to do this.

Holds an `Optional<ReplacedCache>` reference, storing data of the original replaced entity. Also, the 
`SpiritRecord` reference.

### LightSpirit

`LightSpirit` extends `ReplaceableSpirit`. Used to create `Spirit`s that are primarily defined with
`SpirtType.LIGHT_SPIRIT` within `SpiritRecord` upon creation and instantiation.

### DarkSpirit

`DarkSpirit` extends `ReplaceableSpirit`. Used to create `Spirit`s that are primarily defined with
`SpirtType.DARK_SPIRIT` within `SpiritRecord` upon creation and instantiation.

### SpiritBuilder

`SpiritBuilder` is a Builder object that allows ease of creation of `Spirit` instances.

Example:

```java
DarkSpirit spirit = new SpiritBuilder()
                        .spiritName("A dark spirit")
                        .entityType(EntityType.ENDERMITE)
                        .revertTime(0)
                        .spawn(world, location)
                        .build();
```

### SpiritElement

The `Element` used to for whatever spirit purposes of this API.

### SpiritAbility

The `ElementalAbility` used to define an ability as a neutral spirit type.

### LightSpiritAbility

The `ElementalAbility` used to define an ability as a light spirit type.

### DarkSpiritAbility

The `ElementalAbility` used to define an ability as a dark spirit type.

## Spirits game

### Spirecite

Spirecite is a material that can be found when mining gold ores in the form of Spirecite Fragments, 
or by defeating the **Ancient Soulweaver**. When crafted with an **Ancient Spirit-Science Station** 
in a certain order, one can make powerful spirit items that can be held and used by any player 
(bender or nonbender).

### Ancient Spirit-Science Station

The Ancient Spirit-Science Station is a crafting station that can only be used in an old, ruined, 
ancient place. It must be powered with Spirecite and upon use, will be broken by unknown forces.
Only one may exist in each ancient location.

### Ancient Soulweaver

The Ancient Soulweaver is a ***powerful*** boss that is spawned upon providing a skeleton of a
being with its heart. Its abilities are terrifying, and the nightmare cycle it enters is even 
more terrifying. It won't be an easy fight. Though, upon its defeat, you will be rewarded generously 
with its essence.

> ![NOTE]
> You should probably have a team of 4 or however many to defeat this boss. Going solo is like.
> Really hard.

### Spirit world

A world where things are kinda weird. Sometimes water hurts you, sometimes the grass slows you down,
sometimes the light burns your skin. Weird weather and climate conditions all around that makes 
venturing into this world dangerous.

Administrators can make a spirit world through commands or config. A world will thus be defined as 
a spirit world.