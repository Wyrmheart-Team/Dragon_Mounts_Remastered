function onTick()
    if isServer then
        local pos = dragon:getPosition()

        local x = pos.x()
        local y = pos.y()
        local z = pos.z()

        if world:isBlock(x, y, z, "minecraft:grass") or world:hasBlockTag(x, y, z, "minecraft:flowers") or world:hasBlockTag(x, y, z, "minecraft:saplings") then
            dragon:addEffect("minecraft:regeneration", 40)

            if dragon:hasPassenger(owner) or dragon:getDistance(owner) <= 5 then
                owner:addEffect("minecraft:regeneration", 40)
            end
        end
    end
end