function onTick()
    if hasOwner and isServer and dragon:isInWater() then
        if dragon:hasPassenger(owner) or dragon:getDistance(owner) <= 5 then
            owner:addEffect("minecraft:night_vision", 400, 0)
        end
    end
end