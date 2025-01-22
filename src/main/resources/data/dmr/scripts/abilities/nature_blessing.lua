function onTick()
    if isServer then
        local pos = dragon:getPosition()

        if world:isBlock(pos.x, pos.y, pos.z, "minecraft:grass") or world:hasBlockTag(pos.x, pos.y, pos.z, "minecraft:flowers") or world:hasBlockTag(pos.x, pos.y, pos.z, "minecraft:saplings") then
            dragon:addEffect("minecraft:regeneration", 40)

            if dragon:hasPassenger(owner) or dragon:getDistance(owner) <= 5 then
                owner:addEffect("minecraft:regeneration", 40)
            end
        end
    end
end