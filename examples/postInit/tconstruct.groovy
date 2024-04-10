
// Auto generated groovyscript example file
// MODS_LOADED: tconstruct

println 'mod \'tconstruct\' detected, running script'

// Alloying:
// Add or remove smeltery alloying recipes


mods.tconstruct.alloying.recipeBuilder()
    .fluidOutput(fluid('iron') * 3)
    .fluidInputs(fluid('clay') * 1,fluid('lava') * 2)


mods.tconstruct.alloying.add(fluid('lava') * 144, fluid('water') * 500, fluid('iron') * 5, fluid('clay') * 60)

// Casting Basin:
// Add or remove casting basin recipes

mods.tconstruct.basin.removeByCast(item('minecraft:oak_planks'))
mods.tconstruct.basin.removeByInput(fluid('lava'))
mods.tconstruct.basin.removeByOutput(item('minecraft:iron_block'))

mods.tconstruct.basin.recipeBuilder()
    .fluidInput(fluid('water'))
    .output(item('minecraft:dirt'))
    .cast(item('minecraft:cobblestone'))
    .coolingTime(40)


// Drying Rack:
// Add or remove drying rack recipes


mods.tconstruct.drying.recipeBuilder()
    .input(item('minecraft:clay'))
    .output(item('minecraft:dirt'))
    .time(45)



// Entity Melting:
// Add or remove entity melting recipes


mods.tconstruct.entity_melting.recipeBuilder()
    .fluidOutput(fluid('iron') * 500)
    .input('minecraft',
           'pig')


// Melting:
// Add or remove smeltery melting recipes


mods.tconstruct.melting.recipeBuilder()
    .input(item('minecraft:gravel'))
    .fluidOutput(fluid('lava') * 25)
    .time(80)



// Smeltery Fuel:
// Add or remove smeltery fuel types


mods.tconstruct.smeltery_fuel.addFuel(fluid('water'), 250)

// Casting Table:
// Add or remove casting table recipes

mods.tconstruct.table.removeByCast(item('minecraft:bucket'))
mods.tconstruct.table.removeByInput(fluid('iron'))
mods.tconstruct.table.removeByOutput(item('minecraft:iron_ingot'))

mods.tconstruct.table.recipeBuilder()
    .fluidInput(fluid('lava') * 50)
    .output(item('minecraft:diamond'))
    .castingTime(750)
    .consumesCast(true)
    .cast(ore('gemEmerald'))


